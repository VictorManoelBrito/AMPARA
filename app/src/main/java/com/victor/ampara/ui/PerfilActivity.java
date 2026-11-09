package com.victor.ampara.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FieldValue;
import com.victor.ampara.databinding.ActivityPerfilBinding;
import com.victor.ampara.util.ImagemHelper;
import com.victor.ampara.util.SessaoHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** Perfil do Usuário (3.1.8): exibe os dados do usuário, permite trocar a foto e sair. */
public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private FirebaseUser user;
    private ListenerRegistration listener;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarPerfil.setNavigationOnClickListener(v -> finish());

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null
                            && result.getData().getData() != null) {
                        enviarFoto(result.getData().getData());
                    }
                });

        binding.btnAlterarFoto.setOnClickListener(v -> escolherFoto());
        binding.imgPerfil.setOnClickListener(v -> escolherFoto());
        binding.btnSairPerfil.setOnClickListener(v -> SessaoHelper.sair(this));

        binding.tvPerfilEmail.setText(user.getEmail());
        carregarDados();
    }

    private void carregarDados() {
        listener = FirebaseFirestore.getInstance().collection("Usuarios").document(user.getUid())
                .addSnapshotListener((doc, erro) -> {
                    if (doc == null || !doc.exists()) return;

                    String nome = doc.getString("nome");
                    String telefone = doc.getString("telefone");
                    String fotoUrl = doc.getString("fotoUrl");
                    byte[] foto = ImagemHelper.decodificar(doc.getString("fotoBase64"));
                    Timestamp cadastro = doc.getTimestamp("dataCadastro");

                    binding.tvPerfilNome.setText(nome != null ? nome : "Usuário");
                    binding.tvPerfilTelefone.setText(telefone != null && !telefone.isEmpty() ? telefone : "—");

                    if (cadastro != null) {
                        binding.tvPerfilDesde.setText(new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"))
                                .format(cadastro.toDate()));
                    } else if (user.getMetadata() != null) {
                        binding.tvPerfilDesde.setText(new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"))
                                .format(user.getMetadata().getCreationTimestamp()));
                    }

                    if (foto != null) {
                        Glide.with(this).load(foto).into(binding.imgPerfil);
                    } else if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(this).load(fotoUrl).into(binding.imgPerfil);
                    }
                });
    }

    private void escolherFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /** Comprime a foto (400 px) e salva no documento do usuário, no Firestore. */
    private void enviarFoto(Uri uri) {
        Toast.makeText(this, "Atualizando foto...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                ImagemHelper.Resultado foto = ImagemHelper.comprimir(
                        this, uri, ImagemHelper.PERFIL_LADO_MAX, ImagemHelper.PERFIL_BYTES_MAX);
                runOnUiThread(() -> FirebaseFirestore.getInstance().collection("Usuarios").document(user.getUid())
                        .update("fotoBase64", foto.base64, "fotoUrl", FieldValue.delete())
                        .addOnSuccessListener(a -> Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar a foto", Toast.LENGTH_SHORT).show()));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Não foi possível processar a imagem", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}

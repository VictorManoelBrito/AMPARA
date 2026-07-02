package com.victor.ampara.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.widget.ImageView;
import android.widget.TextView;
import com.victor.ampara.R;
import com.victor.ampara.databinding.ActivityMainBinding;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        configurarLauncher();
        configurarCliques();
        carregarDadosUsuario();
    }

    private void configurarLauncher() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            fazerUploadFotoPerfil(imageUri);
                        }
                    }
                }
        );
    }

    private void carregarDadosUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            
            // Referências aos campos do Drawer (Header)
            View headerView = binding.navigationView.getHeaderView(0);
            TextView tvNavName = headerView.findViewById(R.id.tvNavName);
            TextView tvNavEmail = headerView.findViewById(R.id.tvNavEmail);
            ImageView imgNavProfile = headerView.findViewById(R.id.imgNavProfile);

            // Email do FirebaseAuth
            tvNavEmail.setText(user.getEmail());

            db.collection("Usuarios").document(userId)
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String nome = documentSnapshot.getString("nome");
                            String fotoUrl = documentSnapshot.getString("fotoUrl");
                            
                            if (nome != null && !nome.isEmpty()) {
                                binding.tvUserName.setText("Olá, " + nome + "!");
                                tvNavName.setText(nome);
                            }
                            
                            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                                Glide.with(this).load(fotoUrl).into(binding.imgProfile);
                                Glide.with(this).load(fotoUrl).into(imgNavProfile);
                            }
                        }
                    });
        }
    }

    private void configurarCliques() {
        // Menu Lateral
        binding.imgMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        // Perfil (Mudar foto)
        binding.cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // Cards principais
        binding.cardDenuncia.setOnClickListener(v -> startActivity(new Intent(this, DenunciaActivity.class)));
        binding.cardMinhasDenuncias.setOnClickListener(v -> startActivity(new Intent(this, MinhasDenunciasActivity.class)));
        binding.cardRedeApoio.setOnClickListener(v -> startActivity(new Intent(this, RedeApoioActivity.class)));

        // Educacional
        binding.btnLeis.setOnClickListener(v -> startActivity(new Intent(this, LeisActivity.class)));
        binding.btnConteudo.setOnClickListener(v -> startActivity(new Intent(this, ConteudoActivity.class)));
        binding.btnQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizActivity.class)));
        binding.cardBannerQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizActivity.class)));

        // NavigationView Itens
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_denuncias) {
                startActivity(new Intent(this, MinhasDenunciasActivity.class));
            } else if (id == R.id.nav_apoio) {
                startActivity(new Intent(this, RedeApoioActivity.class));
            } else if (id == R.id.nav_logout) {
                auth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void fazerUploadFotoPerfil(Uri uri) {
        String userId = auth.getCurrentUser().getUid();
        StorageReference ref = storage.getReference().child("perfis/" + userId + ".jpg");

        Toast.makeText(this, "Atualizando foto...", Toast.LENGTH_SHORT).show();

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    db.collection("Usuarios").document(userId)
                            .update("fotoUrl", downloadUri.toString())
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Foto atualizada!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Erro ao salvar link", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Erro no upload", Toast.LENGTH_SHORT).show());
    }
}

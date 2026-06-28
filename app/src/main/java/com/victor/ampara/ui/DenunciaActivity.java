package com.victor.ampara.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.victor.ampara.databinding.ActivityDenunciaBinding;
import com.victor.ampara.model.Denuncia;
import java.util.Calendar;
import java.util.UUID;

public class DenunciaActivity extends AppCompatActivity {

    private ActivityDenunciaBinding binding;
    private ActivityResultLauncher<Intent> pickFileLauncher;
    private Uri selectedFileUri;
    
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDenunciaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        configurarLauncher();
        configurarToolbar();
        configurarDropdownTipo();
        configurarDataHora();
        configurarCliques();
    }

    private void configurarDataHora() {
        binding.editDataHora.setFocusable(false);
        binding.editDataHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String data = dayOfMonth + "/" + (month + 1) + "/" + year;
                new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                    String hora = String.format("%02d:%02d", hourOfDay, minute);
                    binding.editDataHora.setText(data + " às " + hora);
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void configurarLauncher() {
        pickFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        if (selectedFileUri != null) {
                            binding.tvArquivosAnexados.setText("Arquivo selecionado: " + selectedFileUri.getLastPathSegment());
                            binding.tvArquivosAnexados.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            binding.btnRemoverAnexo.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    private void configurarToolbar() {
        binding.toolbarDenuncia.setNavigationOnClickListener(v -> finish());
    }

    private void configurarDropdownTipo() {
        String[] tipos = {"Violência Doméstica", "Assédio", "Ameaça", "Outros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos);
        binding.autoCompleteTipo.setAdapter(adapter);
    }

    private void configurarCliques() {
        binding.btnAnexar.setOnClickListener(v -> {
            abrirSelecionadorDeArquivos();
        });

        binding.btnRemoverAnexo.setOnClickListener(v -> {
            selectedFileUri = null;
            binding.tvArquivosAnexados.setText("Nenhum arquivo selecionado");
            binding.tvArquivosAnexados.setTextColor(getResources().getColor(android.R.color.darker_gray));
            binding.btnRemoverAnexo.setVisibility(View.GONE);
        });

        binding.btnEnviarDenuncia.setOnClickListener(v -> {
            validarEEnviar();
        });
    }

    private void abrirSelecionadorDeArquivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Inicialmente focado em imagens/prints
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        pickFileLauncher.launch(Intent.createChooser(intent, "Selecionar Evidência"));
    }

    private void validarEEnviar() {
        // Limpar erros anteriores
        binding.tilTipo.setError(null);
        binding.tilDataHora.setError(null);
        binding.tilRelato.setError(null);

        String tipo = binding.autoCompleteTipo.getText().toString();
        String dataHora = binding.editDataHora.getText().toString();
        String local = binding.editLocal.getText().toString();
        String relato = binding.editRelato.getText().toString();
        boolean anonimo = binding.switchAnonimo.isChecked();

        boolean valido = true;

        if (tipo.isEmpty()) {
            binding.tilTipo.setError("Selecione o tipo de ocorrência");
            valido = false;
        }
        if (dataHora.isEmpty()) {
            binding.tilDataHora.setError("Informe a data e hora");
            valido = false;
        }
        if (relato.isEmpty()) {
            binding.tilRelato.setError("Descreva o ocorrido");
            valido = false;
        }

        if (!valido) {
            Snackbar.make(binding.getRoot(), "Por favor, preencha os campos obrigatórios", Snackbar.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (selectedFileUri != null) {
            fazerUploadEvidencia(tipo, dataHora, local, relato, anonimo);
        } else {
            salvarNoFirestore(tipo, dataHora, local, relato, anonimo, null);
        }
    }

    private void setLoading(boolean loading) {
        binding.btnEnviarDenuncia.setEnabled(!loading);
        binding.btnEnviarDenuncia.setText(loading ? "Enviando..." : "Enviar Denúncia");
        binding.progressEnvio.setVisibility(loading ? View.VISIBLE : View.GONE);
        
        // Desabilitar campos durante o envio
        binding.autoCompleteTipo.setEnabled(!loading);
        binding.editDataHora.setEnabled(!loading);
        binding.editLocal.setEnabled(!loading);
        binding.editRelato.setEnabled(!loading);
        binding.btnAnexar.setEnabled(!loading);
        binding.btnRemoverAnexo.setEnabled(!loading);
    }

    private void fazerUploadEvidencia(String tipo, String dataHora, String local, String relato, boolean anonimo) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("evidencias/" + fileName);

        ref.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    salvarNoFirestore(tipo, dataHora, local, relato, anonimo, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), "Erro ao enviar anexo: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void salvarNoFirestore(String tipo, String dataHora, String local, String relato, boolean anonimo, String url) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonimo";
        Denuncia denuncia = new Denuncia(tipo, dataHora, local, relato, anonimo, userId);
        denuncia.setEvidenciaUrl(url);

        db.collection("denuncias")
                .add(denuncia)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    Toast.makeText(this, "Denúncia registrada com sucesso!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), "Erro ao salvar: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }
}

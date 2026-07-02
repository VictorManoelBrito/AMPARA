package com.victor.ampara.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.victor.ampara.databinding.ActivityDenunciaBinding;
import com.victor.ampara.model.Denuncia;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DenunciaActivity extends AppCompatActivity {

    private ActivityDenunciaBinding binding;
    private ActivityResultLauncher<Intent> pickFileLauncher;
    private Uri selectedFileUri;
    
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDenunciaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        configurarLauncher();
        configurarToolbar();
        configurarDropdowns();
        configurarDataHora();
        configurarCliques();
        obterLocalizacaoAtual();
    }

    private void obterLocalizacaoAtual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String address = addresses.get(0).getAddressLine(0);
                        binding.editLocal.setText(address);
                    }
                } catch (Exception e) {
                    binding.editLocal.setText(String.format(Locale.US, "%.6f, %.6f", location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    private void configurarDataHora() {
        binding.editDataHora.setFocusable(false);
        binding.editDataHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String data = dayOfMonth + "/" + (month + 1) + "/" + year;
                new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                    String hora = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
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
                            binding.tvArquivosAnexados.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                            binding.btnRemoverAnexo.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    private void configurarToolbar() {
        binding.toolbarDenuncia.setNavigationOnClickListener(v -> finish());
    }

    private void configurarDropdowns() {
        String[] tipos = {"Violência Doméstica", "Assédio", "Ameaça", "Outros"};
        ArrayAdapter<String> adapterTipos = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos);
        binding.autoCompleteTipo.setAdapter(adapterTipos);

        String[] urgencias = {"Baixa", "Média", "Alta", "Urgente (Risco de Vida)"};
        ArrayAdapter<String> adapterUrgencia = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencias);
        binding.autoCompleteUrgencia.setAdapter(adapterUrgencia);
    }

    private void configurarCliques() {
        binding.switchAnonimo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.tvAvisoAnonimo.setVisibility(View.VISIBLE);
                binding.tilUrgencia.setEnabled(false);
                binding.autoCompleteUrgencia.setText("Anônima (Análise Padrão)", false);
            } else {
                binding.tvAvisoAnonimo.setVisibility(View.GONE);
                binding.tilUrgencia.setEnabled(true);
                binding.autoCompleteUrgencia.setText("", false);
            }
        });

        binding.btnAnexar.setOnClickListener(v -> abrirSelecionadorDeArquivos());

        binding.btnRemoverAnexo.setOnClickListener(v -> {
            selectedFileUri = null;
            binding.tvArquivosAnexados.setText("Nenhum arquivo selecionado");
            binding.tvArquivosAnexados.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            binding.btnRemoverAnexo.setVisibility(View.GONE);
        });

        binding.btnEnviarDenuncia.setOnClickListener(v -> validarEEnviar());
    }

    private void abrirSelecionadorDeArquivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        pickFileLauncher.launch(Intent.createChooser(intent, "Selecionar Evidência"));
    }

    private void validarEEnviar() {
        binding.tilTipo.setError(null);
        binding.tilDataHora.setError(null);
        binding.tilRelato.setError(null);

        String tipo = binding.autoCompleteTipo.getText().toString();
        String dataHora = binding.editDataHora.getText().toString();
        String local = binding.editLocal.getText().toString();
        String relato = binding.editRelato.getText().toString();
        String urgencia = binding.autoCompleteUrgencia.getText().toString();
        boolean anonimo = binding.switchAnonimo.isChecked();

        boolean valido = true;

        if (tipo.isEmpty()) {
            binding.tilTipo.setError("Selecione o tipo");
            valido = false;
        }
        if (dataHora.isEmpty()) {
            binding.tilDataHora.setError("Informe a data");
            valido = false;
        }
        if (relato.isEmpty()) {
            binding.tilRelato.setError("Descreva o ocorrido");
            valido = false;
        }

        if (!valido) {
            Snackbar.make(binding.getRoot(), "Preencha os campos obrigatórios", Snackbar.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (selectedFileUri != null) {
            fazerUploadEvidencia(tipo, dataHora, local, relato, urgencia, anonimo);
        } else {
            salvarNoFirestore(tipo, dataHora, local, relato, urgencia, anonimo, null);
        }
    }

    private void setLoading(boolean loading) {
        binding.btnEnviarDenuncia.setEnabled(!loading);
        binding.btnEnviarDenuncia.setText(loading ? "Enviando..." : "Enviar Denúncia");
        binding.progressEnvio.setVisibility(loading ? View.VISIBLE : View.GONE);
        
        binding.autoCompleteTipo.setEnabled(!loading);
        binding.editDataHora.setEnabled(!loading);
        binding.editLocal.setEnabled(!loading);
        binding.editRelato.setEnabled(!loading);
        binding.btnAnexar.setEnabled(!loading);
        binding.switchAnonimo.setEnabled(!loading);
    }

    private void fazerUploadEvidencia(String tipo, String dataHora, String local, String relato, String urgencia, boolean anonimo) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("evidencias/" + fileName);

        ref.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    salvarNoFirestore(tipo, dataHora, local, relato, urgencia, anonimo, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), "Erro no upload: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void salvarNoFirestore(String tipo, String dataHora, String local, String relato, String urgencia, boolean anonimo, String url) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonimo";
        String emailUsuario = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

        // Gerar Protocolo: AMP-2026-XXXX
        String protocolo = "AMP-" + Calendar.getInstance().get(Calendar.YEAR) + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Denuncia denuncia = new Denuncia(tipo, dataHora, local, relato, anonimo, userId);
        denuncia.setEvidenciaUrl(url);
        denuncia.setUrgencia(urgencia);
        denuncia.setProtocolo(protocolo);

        db.collection("denuncias")
                .add(denuncia)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);

                    if (!anonimo && emailUsuario != null) {
                        solicitarEnvioEmailAutomatico(emailUsuario, protocolo, tipo);
                    }
                    
                    Toast.makeText(this, "Denúncia registrada! Protocolo: " + protocolo, Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), "Erro ao salvar: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void solicitarEnvioEmailAutomatico(String email, String protocolo, String tipo) {
        Map<String, Object> mail = new HashMap<>();
        mail.put("to", email);
        
        Map<String, Object> message = new HashMap<>();
        message.put("subject", "Confirmação de Denúncia - AMPARA");
        message.put("text", "Olá,\n\nSua denúncia do tipo '" + tipo + "' foi registrada com sucesso.\n\n" +
                "Protocolo: " + protocolo + "\n\n" +
                "Nossa equipe analisará os dados em breve.\n" +
                "Mantenha-se em segurança.");
        
        mail.put("message", message);

        // O Firebase Extension 'Trigger Email' escuta esta coleção 'mail'
        db.collection("mail").add(mail);
    }
}

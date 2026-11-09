package com.victor.ampara.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.victor.ampara.databinding.ActivityDenunciaBinding;
import com.victor.ampara.model.Denuncia;
import com.victor.ampara.util.ImagemHelper;
import com.victor.ampara.util.NotificationHelper;

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
    private FirebaseAuth auth;
    private FusedLocationProviderClient fusedLocationClient;

    // Localização aproximada (LGPD): apenas bairro e cidade, nunca rua/número.
    private String bairroDetectado;
    private String cidadeDetectada;
    private String localDetectado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityDenunciaBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            configurarLauncher();
            configurarToolbar();
            configurarDropdowns();
            configurarDataHora();
            configurarCliques();
            
            // Tenta obter a localização de forma segura
            obterLocalizacaoSimples();
            
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao carregar tela: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void obterLocalizacaoSimples() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                preencherCampoLocalizacao(location);
            } else {
                obterLocalizacaoAtualReal();
            }
        });
    }

    private void obterLocalizacaoAtualReal() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        preencherCampoLocalizacao(location);
                    }
                });
    }

    /**
     * Converte a posição do GPS em uma localização APROXIMADA (bairro e cidade).
     * Rua, número e coordenadas não são salvos, para proteger a vítima.
     */
    private void preencherCampoLocalizacao(Location location) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);
                    String bairro = a.getSubLocality();
                    String cidade = a.getLocality() != null ? a.getLocality() : a.getSubAdminArea();
                    String estado = a.getAdminArea();

                    StringBuilder texto = new StringBuilder();
                    if (bairro != null && !bairro.isEmpty()) texto.append(bairro);
                    if (cidade != null && !cidade.isEmpty()) {
                        if (texto.length() > 0) texto.append(", ");
                        texto.append(cidade);
                    }
                    if (estado != null && !estado.isEmpty()) {
                        if (texto.length() > 0) texto.append(" - ");
                        texto.append(estado);
                    }

                    if (texto.length() > 0) {
                        bairroDetectado = bairro;
                        cidadeDetectada = cidade;
                        localDetectado = texto.toString();
                        runOnUiThread(() -> binding.editLocal.setText(localDetectado));
                    }
                }
            } catch (Exception e) {
                // Sem conexão ou geocoder indisponível: o usuário pode digitar bairro e cidade.
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacaoSimples();
        }
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
                            Snackbar.make(binding.getRoot(), "✔ Evidência anexada com sucesso!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void configurarToolbar() {
        binding.toolbarDenuncia.setNavigationOnClickListener(v -> finish());
    }

    private void configurarDropdowns() {
        String[] tipos = {
                "Violência Doméstica",
                "Assédio",
                "Ameaça",
                "Discriminação Racial",
                "LGBTfobia",
                "Crime Cibernético - Ameaça Virtual",
                "Crime Cibernético - Invasão de Conta",
                "Crime Cibernético - Divulgação Indevida de Conteúdo",
                "Outros"
        };
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

        if (tipo.isEmpty() || dataHora.isEmpty() || relato.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Preencha os campos obrigatórios", Snackbar.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (selectedFileUri != null) {
            prepararEvidencia(tipo, dataHora, local, relato, urgencia, anonimo);
        } else {
            salvarNoFirestore(tipo, dataHora, local, relato, urgencia, anonimo, null);
        }
    }

    private void setLoading(boolean loading) {
        binding.btnEnviarDenuncia.setEnabled(!loading);
        binding.btnEnviarDenuncia.setText(loading ? "Enviando..." : "Enviar Denúncia");
        binding.progressEnvio.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    /**
     * Comprime a imagem em segundo plano (redimensiona e converte para JPEG)
     * antes de salvar. A imagem é gravada no Firestore, na coleção "evidencias".
     */
    private void prepararEvidencia(String tipo, String dataHora, String local, String relato, String urgencia, boolean anonimo) {
        final Uri uri = selectedFileUri;
        new Thread(() -> {
            try {
                ImagemHelper.Resultado evidencia = ImagemHelper.comprimir(
                        this, uri, ImagemHelper.EVIDENCIA_LADO_MAX, ImagemHelper.EVIDENCIA_BYTES_MAX);
                runOnUiThread(() -> salvarNoFirestore(tipo, dataHora, local, relato, urgencia, anonimo, evidencia));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), "Não foi possível processar a imagem. Tente outra foto.", Snackbar.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void salvarNoFirestore(String tipo, String dataHora, String local, String relato, String urgencia, boolean anonimo, ImagemHelper.Resultado evidencia) {
        // Denúncia anônima: NÃO vincula o usuário (sem usuarioId e sem e-mail).
        String userId = (!anonimo && auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
        String emailUsuario = (!anonimo && auth.getCurrentUser() != null) ? auth.getCurrentUser().getEmail() : null;

        String protocolo = "AMP-" + Calendar.getInstance().get(Calendar.YEAR) + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Denuncia denuncia = new Denuncia(tipo, dataHora, local, relato, anonimo, userId);
        denuncia.setUrgencia(urgencia);
        denuncia.setProtocolo(protocolo);

        // Bairro/cidade só são usados se o usuário manteve a localização detectada.
        if (localDetectado != null && localDetectado.equals(local)) {
            denuncia.setBairro(bairroDetectado);
            denuncia.setCidade(cidadeDetectada);
        }

        // Denúncia e evidência são gravadas juntas (em lote): ou salva tudo, ou nada.
        WriteBatch lote = db.batch();
        DocumentReference denunciaRef = db.collection("denuncias").document();

        if (evidencia != null) {
            DocumentReference evidenciaRef = db.collection("evidencias").document();
            denuncia.setEvidenciaId(evidenciaRef.getId());

            // Entidade "evidencia" do diagrama de classes
            Map<String, Object> dadosEvidencia = new HashMap<>();
            dadosEvidencia.put("denunciaId", denunciaRef.getId());
            dadosEvidencia.put("usuarioId", userId); // null quando a denúncia é anônima
            dadosEvidencia.put("nomeArquivo", "evidencia-" + protocolo + ".jpg");
            dadosEvidencia.put("tipoArquivo", "image/jpeg");
            dadosEvidencia.put("largura", evidencia.largura);
            dadosEvidencia.put("altura", evidencia.altura);
            dadosEvidencia.put("tamanhoBytes", evidencia.bytes);
            dadosEvidencia.put("imagemBase64", evidencia.base64);
            dadosEvidencia.put("dataEnvio", FieldValue.serverTimestamp());
            lote.set(evidenciaRef, dadosEvidencia);
        }
        lote.set(denunciaRef, denuncia);

        lote.commit()
                .addOnSuccessListener(ok -> {
                    setLoading(false);
                    if (emailUsuario != null) {
                        solicitarEnvioEmailAutomatico(emailUsuario, protocolo, tipo);
                    }

                    NotificationHelper.showNotification(this, "Denúncia Registrada", "Sua denúncia foi registrada com sucesso. Protocolo: " + protocolo);
                    mostrarProtocolo(protocolo, anonimo);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), "Erro ao salvar: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    /** Mostra o protocolo para o usuário anotar (essencial na denúncia anônima). */
    private void mostrarProtocolo(String protocolo, boolean anonimo) {
        String mensagem = "Protocolo: " + protocolo + "\n\n" + (anonimo
                ? "Como a denúncia é anônima, ela não fica vinculada à sua conta e não aparecerá em \"Minhas denúncias\". Anote o número do protocolo."
                : "Você pode acompanhar o andamento em \"Minhas denúncias\".");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Denúncia enviada")
                .setMessage(mensagem)
                .setCancelable(false)
                .setNeutralButton("Copiar protocolo", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Protocolo AMPARA", protocolo));
                    Toast.makeText(this, "Protocolo copiado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    private void solicitarEnvioEmailAutomatico(String email, String protocolo, String tipo) {
        Map<String, Object> mail = new HashMap<>();
        mail.put("to", email);
        Map<String, Object> message = new HashMap<>();
        message.put("subject", "Confirmação de Denúncia - AMPARA");
        message.put("text", "Sua denúncia foi registrada. Protocolo: " + protocolo);
        mail.put("message", message);
        db.collection("mail").add(mail);
    }
}

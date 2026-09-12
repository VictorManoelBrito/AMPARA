package com.victor.ampara.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.victor.ampara.R;
import com.victor.ampara.model.Denuncia;

public class DetalhesDenunciaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration statusListener;
    private String denunciaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_denuncia);

        db = FirebaseFirestore.getInstance();
        
        Toolbar toolbar = findViewById(R.id.toolbarDetalhes);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Recuperar a denúncia enviada via Intent
        Denuncia denuncia = (Denuncia) getIntent().getSerializableExtra("denuncia");

        if (denuncia != null) {
            denunciaId = denuncia.getId();
            preencherDados(denuncia);
            iniciarOuvinteRealTime();
        }
    }

    private void iniciarOuvinteRealTime() {
        if (denunciaId == null) return;

        // Escuta mudanças específicas neste documento da denúncia para atualizar status em tempo real
        statusListener = db.collection("denuncias").document(denunciaId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;

                    if (snapshot != null && snapshot.exists()) {
                        Denuncia denunciaAtualizada = snapshot.toObject(Denuncia.class);
                        if (denunciaAtualizada != null) {
                            preencherDados(denunciaAtualizada);
                        }
                    }
                });
    }

    private void preencherDados(Denuncia denuncia) {
        ((TextView) findViewById(R.id.tvDetalheTipo)).setText(denuncia.getTipo());
        ((TextView) findViewById(R.id.tvDetalheProtocolo)).setText("Protocolo: " + (denuncia.getProtocolo() != null ? denuncia.getProtocolo() : "N/A"));
        ((TextView) findViewById(R.id.tvDetalheDataHora)).setText(denuncia.getDataHoraOcorrencia());
        ((TextView) findViewById(R.id.tvDetalheLocal)).setText(denuncia.getLocal() != null ? denuncia.getLocal() : "Não informado");
        ((TextView) findViewById(R.id.tvDetalheRelato)).setText(denuncia.getRelato());
        ((TextView) findViewById(R.id.tvDetalheUrgencia)).setText(denuncia.getUrgencia() != null ? denuncia.getUrgencia() : "Padrão");

        // Lógica de Status e Cores
        String status = denuncia.getStatus() != null ? denuncia.getStatus() : "Em análise";
        TextView tvStatus = findViewById(R.id.tvDetalheStatus);
        tvStatus.setText(status);

        if (status.equalsIgnoreCase("Finalizado") || status.equalsIgnoreCase("Concluído")) {
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_finalized));
        } else {
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_pending));
        }

        // Carregar imagem se houver URL válida
        View layoutEvidencia = findViewById(R.id.layoutEvidencia);
        if (denuncia.getEvidenciaUrl() != null && !denuncia.getEvidenciaUrl().isEmpty()) {
            layoutEvidencia.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(denuncia.getEvidenciaUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into((ImageView) findViewById(R.id.imgEvidencia));
        } else {
            layoutEvidencia.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) {
            statusListener.remove();
        }
    }
}

package com.victor.ampara.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.victor.ampara.R;
import com.victor.ampara.model.Denuncia;

public class DetalhesDenunciaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_denuncia);

        findViewById(R.id.toolbarDetalhes).setOnClickListener(v -> finish());

        // Recuperar a denúncia enviada via Intent
        Denuncia denuncia = (Denuncia) getIntent().getSerializableExtra("denuncia");

        if (denuncia != null) {
            preencherDados(denuncia);
        }
    }

    private void preencherDados(Denuncia denuncia) {
        ((TextView) findViewById(R.id.tvDetalheTipo)).setText(denuncia.getTipo());
        ((TextView) findViewById(R.id.tvDetalheProtocolo)).setText("Protocolo: " + (denuncia.getProtocolo() != null ? denuncia.getProtocolo() : "N/A"));
        ((TextView) findViewById(R.id.tvDetalheDataHora)).setText(denuncia.getDataHoraOcorrencia());
        ((TextView) findViewById(R.id.tvDetalheLocal)).setText(denuncia.getLocal() != null ? denuncia.getLocal() : "Não informado");
        ((TextView) findViewById(R.id.tvDetalheRelato)).setText(denuncia.getRelato());
        ((TextView) findViewById(R.id.tvDetalheUrgencia)).setText(denuncia.getUrgencia() != null ? denuncia.getUrgencia() : "Padrão");

        // Status com cor
        String status = denuncia.getStatus() != null ? denuncia.getStatus() : "Em análise";
        TextView tvStatus = findViewById(R.id.tvDetalheStatus);
        tvStatus.setText(status);
        if (status.equalsIgnoreCase("Finalizado")) {
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_finalized));
        } else {
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_pending));
        }

        // Carregar imagem se houver
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
}

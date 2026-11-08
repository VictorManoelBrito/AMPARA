package com.victor.ampara.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.victor.ampara.databinding.ActivityLeituraBinding;
import com.victor.ampara.model.ConteudoEducativo.Topico;

/** Exibe o texto completo de um tópico educativo ou de uma lei. */
public class LeituraActivity extends AppCompatActivity {

    private static final String EXTRA_TOPICO = "EXTRA_TOPICO";
    private static final String EXTRA_TITULO_TELA = "EXTRA_TITULO_TELA";

    public static void abrir(Context context, Topico topico, String tituloTela) {
        Intent intent = new Intent(context, LeituraActivity.class);
        intent.putExtra(EXTRA_TOPICO, topico);
        intent.putExtra(EXTRA_TITULO_TELA, tituloTela);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLeituraBinding binding = ActivityLeituraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarLeitura.setNavigationOnClickListener(v -> finish());

        String tituloTela = getIntent().getStringExtra(EXTRA_TITULO_TELA);
        if (tituloTela != null) binding.toolbarLeitura.setTitle(tituloTela);

        Topico topico = lerTopico();
        if (topico == null) {
            finish();
            return;
        }
        binding.tvLeituraTitulo.setText(topico.titulo);
        binding.tvLeituraResumo.setText(topico.resumo);
        binding.tvLeituraTexto.setText(topico.texto);
    }

    @SuppressWarnings("deprecation")
    private Topico lerTopico() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getSerializableExtra(EXTRA_TOPICO, Topico.class);
        }
        return (Topico) getIntent().getSerializableExtra(EXTRA_TOPICO);
    }
}

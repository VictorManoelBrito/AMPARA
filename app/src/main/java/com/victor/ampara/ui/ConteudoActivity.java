package com.victor.ampara.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.victor.ampara.databinding.ActivityConteudoBinding;
import com.victor.ampara.model.ConteudoEducativo;

/** Conteúdos Educativos (3.1.9): lista de temas que abrem a tela de leitura. */
public class ConteudoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityConteudoBinding binding = ActivityConteudoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarConteudo.setNavigationOnClickListener(v -> finish());

        binding.rvConteudos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConteudos.setAdapter(new TopicoAdapter(
                ConteudoEducativo.conteudos(),
                topico -> LeituraActivity.abrir(this, topico, "Conteúdos Educativos")));
    }
}

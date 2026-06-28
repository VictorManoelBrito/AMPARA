package com.victor.ampara.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.victor.ampara.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarCliques();
    }

    private void configurarCliques() {
        // Clique no Card de Denúncia
        binding.cardDenuncia.setOnClickListener(v -> {
            startActivity(new Intent(this, DenunciaActivity.class));
        });

        // Clique no Card do Quiz (Carrossel estilo iFood)
        binding.cardBannerQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, QuizActivity.class));
        });

        // Clique no Botão de Quiz (Seção Educacional)
        binding.btnQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, QuizActivity.class));
        });

        // Clique no Botão de Leis
        binding.btnLeis.setOnClickListener(v -> {
            startActivity(new Intent(this, LeisActivity.class));
        });

        // Clique no Botão de Conteúdo
        binding.btnConteudo.setOnClickListener(v -> {
            startActivity(new Intent(this, ConteudoActivity.class));
        });

        // Clique no Card Rede de Apoio
        binding.cardRedeApoio.setOnClickListener(v -> {
            startActivity(new Intent(this, RedeApoioActivity.class));
        });

        // Clique no Card Minhas Denúncias
        binding.cardMinhasDenuncias.setOnClickListener(v -> {
            startActivity(new Intent(this, MinhasDenunciasActivity.class));
        });
    }
}

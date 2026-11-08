package com.victor.ampara.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.victor.ampara.databinding.ActivityLeisBinding;
import com.victor.ampara.model.ConteudoEducativo;

/** Leis e Direitos (3.1.10): lista de legislações que abrem a tela de leitura. */
public class LeisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLeisBinding binding = ActivityLeisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarLeis.setNavigationOnClickListener(v -> finish());

        binding.rvLeis.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLeis.setAdapter(new TopicoAdapter(
                ConteudoEducativo.leis(),
                topico -> LeituraActivity.abrir(this, topico, "Leis e Direitos")));
    }
}

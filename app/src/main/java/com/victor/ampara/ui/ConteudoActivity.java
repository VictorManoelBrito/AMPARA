package com.victor.ampara.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.victor.ampara.databinding.ActivityConteudoBinding;

public class ConteudoActivity extends AppCompatActivity {

    private ActivityConteudoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConteudoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarConteudo.setNavigationOnClickListener(v -> finish());
    }
}

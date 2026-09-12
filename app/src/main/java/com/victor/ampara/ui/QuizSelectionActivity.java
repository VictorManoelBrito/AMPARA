package com.victor.ampara.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.victor.ampara.databinding.ActivityQuizSelectionBinding;

public class QuizSelectionActivity extends AppCompatActivity {

    private ActivityQuizSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarQuizSelection.setNavigationOnClickListener(v -> finish());

        binding.cardQuizRisco.setOnClickListener(v -> iniciarQuiz("risco"));
        binding.cardQuizDireitos.setOnClickListener(v -> iniciarQuiz("direitos"));
        binding.cardQuizCiclo.setOnClickListener(v -> iniciarQuiz("ciclo"));
    }

    private void iniciarQuiz(String tema) {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("TEMA_QUIZ", tema);
        startActivity(intent);
    }
}

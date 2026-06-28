package com.victor.ampara.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.victor.ampara.R;
import com.victor.ampara.databinding.ActivityQuizBinding;

public class QuizActivity extends AppCompatActivity {

    private ActivityQuizBinding binding;
    private int perguntaAtual = 0;
    private int scoreTotal = 0;

    private final String[] perguntas = {
            "O agressor possui ou tem acesso a armas de fogo?",
            "As ameaças ou agressões aumentaram de frequência nos últimos meses?",
            "Ele já tentou te estrangular ou sufocar em algum momento?",
            "Ele controla excessivamente sua vida, o que você faz ou com quem fala?",
            "Ele já ameaçou explicitamente te matar ou ferir seus familiares?"
    };

    private final int[] pesos = {3, 2, 3, 1, 3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarToolbar();
        configurarBotoes();
        atualizarPergunta();
    }

    private void configurarToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarBotoes() {
        binding.btnSim.setOnClickListener(v -> {
            scoreTotal += pesos[perguntaAtual];
            proximaPergunta();
        });

        binding.btnNao.setOnClickListener(v -> proximaPergunta());

        binding.btnVoltar.setOnClickListener(v -> finish());
    }

    private void atualizarPergunta() {
        binding.tvContagem.setText("Pergunta " + (perguntaAtual + 1) + " de " + perguntas.length);
        binding.tvPergunta.setText(perguntas[perguntaAtual]);
        
        int progresso = ((perguntaAtual + 1) * 100) / perguntas.length;
        binding.progressQuiz.setProgress(progresso);
    }

    private void proximaPergunta() {
        perguntaAtual++;
        if (perguntaAtual < perguntas.length) {
            atualizarPergunta();
        } else {
            exibirResultado();
        }
    }

    private void exibirResultado() {
        binding.layoutPergunta.setVisibility(View.GONE);
        binding.layoutResultado.setVisibility(View.VISIBLE);
        binding.progressQuiz.setProgress(100);

        String nivel;
        String orientacao;
        int cor;
        int icone;

        if (scoreTotal >= 8) {
            nivel = "ALTO RISCO";
            cor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
            orientacao = "Sua vida pode estar em perigo imediato. Procure a delegacia mais próxima ou ligue 190 agora. Não hesite em buscar abrigo seguro.";
            icone = android.R.drawable.ic_dialog_alert;
        } else if (scoreTotal >= 4) {
            nivel = "RISCO MÉDIO";
            cor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
            orientacao = "Existem sinais claros de perigo progressivo. Procure orientação jurídica e psicológica. O Ligue 180 pode te ajudar a montar um plano de segurança.";
            icone = android.R.drawable.ic_dialog_info;
        } else {
            nivel = "RISCO MODERADO";
            cor = ContextCompat.getColor(this, R.color.primary_ampara);
            orientacao = "Embora o risco pareça menor, a violência tende a escalar. Fique atenta aos sinais e fortaleça sua rede de apoio com amigos e familiares.";
            icone = android.R.drawable.ic_dialog_info;
        }

        binding.tvTituloResultado.setText(nivel);
        binding.tvTituloResultado.setTextColor(cor);
        binding.tvDescricaoResultado.setText(orientacao);
        binding.imgResultado.setImageResource(icone);
        binding.imgResultado.setColorFilter(cor);
    }
}

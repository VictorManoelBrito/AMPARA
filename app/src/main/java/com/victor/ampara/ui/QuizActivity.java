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
    private String temaAtual;

    private String[] perguntas;
    private int[] pesos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        temaAtual = getIntent().getStringExtra("TEMA_QUIZ");
        configurarQuizPorTema();

        configurarToolbar();
        configurarBotoes();
        atualizarPergunta();
    }

    private void configurarQuizPorTema() {
        if ("direitos".equals(temaAtual)) {
            binding.toolbar.setTitle("Conheça seus Direitos");
            perguntas = new String[]{
                    "A Lei Maria da Penha protege apenas contra violência física?",
                    "A vítima pode desistir da denúncia em qualquer momento na delegacia?",
                    "Medidas protetivas podem ser concedidas em até 48 horas?",
                    "A violência patrimonial (quebrar celular, reter documentos) é crime?",
                    "O agressor pode ser preso mesmo se não houver flagrante?"
            };
            pesos = new int[]{1, 1, 1, 1, 1}; // Quiz informativo: 1 ponto por resposta 'Sim' (que aqui seriam as corretas)
        } else if ("ciclo".equals(temaAtual)) {
            binding.toolbar.setTitle("Ciclo da Violência");
            perguntas = new String[]{
                    "Você sente que precisa 'pisar em ovos' para não irritar o parceiro?",
                    "Após uma briga, ele costuma pedir desculpas exageradas e dar presentes?",
                    "As ofensas verbais são seguidas por momentos de calmaria?",
                    "Você já deixou de sair com amigos para evitar discussões?",
                    "Ele justifica a agressividade como 'excesso de amor' ou estresse?"
            };
            pesos = new int[]{2, 2, 2, 2, 2};
        } else {
            // Default: Avaliação de Risco
            binding.toolbar.setTitle("Avaliação de Risco");
            perguntas = new String[]{
                    "O agressor possui ou tem acesso a armas de fogo?",
                    "As ameaças ou agressões aumentaram de frequência?",
                    "Ele já tentou te estrangular ou sufocar?",
                    "Ele controla excessivamente sua vida e contatos?",
                    "Ele já ameaçou explicitamente te matar?"
            };
            pesos = new int[]{3, 2, 3, 1, 3};
        }
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

        String nivel = "";
        String orientacao = "";
        int cor = ContextCompat.getColor(this, R.color.primary_ampara);
        int icone = android.R.drawable.ic_dialog_info;

        if ("risco".equals(temaAtual) || temaAtual == null) {
            if (scoreTotal >= 8) {
                nivel = "ALTO RISCO";
                cor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
                orientacao = "Sua vida pode estar em perigo imediato. Procure ajuda agora.";
                icone = android.R.drawable.ic_dialog_alert;
            } else if (scoreTotal >= 4) {
                nivel = "RISCO MÉDIO";
                cor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
                orientacao = "Sinais claros de perigo. Busque orientação oficial.";
            } else {
                nivel = "RISCO MODERADO";
                orientacao = "Fique atenta aos sinais. Fortaleça sua rede de apoio.";
            }
        } else {
            // Resultados para os outros quizes (Informativos)
            nivel = "CONCLUÍDO";
            orientacao = "Parabéns por buscar informação! O conhecimento é sua melhor proteção.";
            icone = android.R.drawable.ic_menu_save;
        }

        binding.tvTituloResultado.setText(nivel);
        binding.tvTituloResultado.setTextColor(cor);
        binding.tvDescricaoResultado.setText(orientacao);
        binding.imgResultado.setImageResource(icone);
        binding.imgResultado.setColorFilter(cor);
    }
}

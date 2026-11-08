package com.victor.ampara.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.victor.ampara.R;
import com.victor.ampara.databinding.ActivityQuizEducativoBinding;
import com.victor.ampara.model.QuizData;

import java.util.HashMap;
import java.util.Map;

/**
 * Quiz educativo de múltipla escolha. Ao final, salva o resultado do usuário
 * na coleção "resultados_quiz" (entidade resultado_quiz do diagrama de classes),
 * guardando a melhor pontuação para o progresso e as medalhas.
 */
public class QuizEducativoActivity extends AppCompatActivity {

    public static final String EXTRA_TEMA_ID = "EXTRA_TEMA_ID";
    public static final String COLECAO_RESULTADOS = "resultados_quiz";

    private static final int COR_CERTO = Color.parseColor("#2E7D32");
    private static final int COR_ERRADO = Color.parseColor("#C62828");

    private ActivityQuizEducativoBinding binding;
    private QuizData.Tema tema;
    private MaterialButton[] botoes;
    private int indice = 0;
    private int acertos = 0;
    private boolean respondida = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizEducativoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tema = QuizData.buscar(getIntent().getStringExtra(EXTRA_TEMA_ID));
        if (tema == null) {
            finish();
            return;
        }

        binding.toolbarQuizEducativo.setTitle(tema.titulo);
        binding.toolbarQuizEducativo.setNavigationOnClickListener(v -> finish());

        botoes = new MaterialButton[]{
                binding.btnAlternativa0, binding.btnAlternativa1,
                binding.btnAlternativa2, binding.btnAlternativa3
        };
        for (int i = 0; i < botoes.length; i++) {
            final int escolhida = i;
            botoes[i].setOnClickListener(v -> responder(escolhida));
        }

        binding.btnProxima.setOnClickListener(v -> proxima());
        binding.btnRefazer.setOnClickListener(v -> reiniciar());
        binding.btnVoltarTemas.setOnClickListener(v -> finish());

        mostrarPergunta();
    }

    private void mostrarPergunta() {
        QuizData.Pergunta p = tema.perguntas.get(indice);
        int total = tema.perguntas.size();

        respondida = false;
        binding.tvContagemEducativa.setText("Pergunta " + (indice + 1) + " de " + total);
        binding.tvPerguntaEducativa.setText(p.enunciado);
        binding.progressQuizEducativo.setProgress((indice * 100) / total);

        String[] letras = {"A", "B", "C", "D"};
        for (int i = 0; i < botoes.length; i++) {
            botoes[i].setText(letras[i] + ")  " + p.alternativas[i]);
            botoes[i].setEnabled(true);
            estiloPadrao(botoes[i]);
        }

        binding.layoutFeedback.setVisibility(View.GONE);
        binding.btnProxima.setEnabled(false);
        binding.btnProxima.setText(indice == total - 1 ? "Ver resultado" : "Próxima");
    }

    private void responder(int escolhida) {
        if (respondida) return;
        respondida = true;

        QuizData.Pergunta p = tema.perguntas.get(indice);
        boolean acertou = escolhida == p.correta;
        if (acertou) acertos++;

        for (MaterialButton b : botoes) b.setEnabled(false);
        destacar(botoes[p.correta], COR_CERTO);
        if (!acertou) destacar(botoes[escolhida], COR_ERRADO);

        binding.tvFeedbackTitulo.setText(acertou ? "Resposta correta!" : "Resposta incorreta");
        binding.tvFeedbackTitulo.setTextColor(acertou ? COR_CERTO : COR_ERRADO);
        binding.tvExplicacao.setText(p.explicacao);
        binding.layoutFeedback.setVisibility(View.VISIBLE);
        binding.btnProxima.setEnabled(true);
    }

    private void proxima() {
        indice++;
        if (indice < tema.perguntas.size()) {
            mostrarPergunta();
        } else {
            mostrarResultado();
        }
    }

    private void mostrarResultado() {
        int total = tema.perguntas.size();
        boolean medalha = acertos == total;

        binding.progressQuizEducativo.setProgress(100);
        binding.layoutPerguntaEducativa.setVisibility(View.GONE);
        binding.layoutResultadoEducativo.setVisibility(View.VISIBLE);

        binding.tvPontuacao.setText(acertos + " de " + total);

        if (medalha) {
            binding.imgResultadoEducativo.setImageResource(android.R.drawable.btn_star_big_on);
            binding.imgResultadoEducativo.clearColorFilter();
            binding.tvTituloResultadoEducativo.setText("Medalha conquistada!");
            binding.tvMensagemResultado.setText("Você acertou todas as perguntas de \"" + tema.titulo + "\". Conhecimento é proteção!");
        } else {
            binding.imgResultadoEducativo.setImageResource(android.R.drawable.ic_dialog_info);
            binding.imgResultadoEducativo.setColorFilter(ContextCompat.getColor(this, R.color.primary_ampara));
            binding.tvTituloResultadoEducativo.setText(acertos >= total / 2 + 1 ? "Muito bem!" : "Continue aprendendo!");
            binding.tvMensagemResultado.setText("Acerte todas as perguntas para ganhar a medalha deste tema. Que tal revisar os Conteúdos Educativos e tentar de novo?");
        }

        salvarResultado(total);
    }

    /**
     * Salva/atualiza o documento {uid}_{temaId} em "resultados_quiz",
     * mantendo sempre a melhor pontuação.
     */
    private void salvarResultado(int total) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(COLECAO_RESULTADOS).document(user.getUid() + "_" + tema.id);
        final int pontuacao = acertos;

        ref.get().addOnCompleteListener(task -> {
            long melhorAnterior = 0;
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Long valor = task.getResult().getLong("melhorPontuacao");
                if (valor != null) melhorAnterior = valor;
            }
            long melhor = Math.max(melhorAnterior, pontuacao);

            Map<String, Object> dados = new HashMap<>();
            dados.put("usuarioId", user.getUid());
            dados.put("quizId", tema.id);
            dados.put("tituloQuiz", tema.titulo);
            dados.put("ultimaPontuacao", pontuacao);
            dados.put("melhorPontuacao", melhor);
            dados.put("totalPerguntas", total);
            dados.put("medalha", melhor == total);
            dados.put("tentativas", FieldValue.increment(1));
            dados.put("dataRealizacao", FieldValue.serverTimestamp());

            ref.set(dados, SetOptions.merge());
        });
    }

    private void reiniciar() {
        indice = 0;
        acertos = 0;
        binding.layoutResultadoEducativo.setVisibility(View.GONE);
        binding.layoutPerguntaEducativa.setVisibility(View.VISIBLE);
        mostrarPergunta();
    }

    private void estiloPadrao(MaterialButton b) {
        int primaria = ContextCompat.getColor(this, R.color.primary_ampara);
        b.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        b.setStrokeColor(ColorStateList.valueOf(primaria));
        b.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
    }

    private void destacar(MaterialButton b, int cor) {
        b.setBackgroundTintList(ColorStateList.valueOf(cor));
        b.setStrokeColor(ColorStateList.valueOf(cor));
        b.setTextColor(Color.WHITE);
    }
}

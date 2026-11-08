package com.victor.ampara.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.victor.ampara.databinding.ActivityQuizSelectionBinding;
import com.victor.ampara.model.QuizData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tela de Quiz (3.1.11): lista de temas com o progresso do usuário em cada um
 * (melhor pontuação), progresso geral e medalhas. Também dá acesso às
 * autoavaliações de Sim/Não, que não contam pontos.
 */
public class QuizSelectionActivity extends AppCompatActivity {

    private ActivityQuizSelectionBinding binding;
    private QuizTemaAdapter adapter;
    private final List<QuizData.Tema> temas = QuizData.temas();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarQuizSelection.setNavigationOnClickListener(v -> finish());

        adapter = new QuizTemaAdapter(temas, tema -> {
            Intent intent = new Intent(this, QuizEducativoActivity.class);
            intent.putExtra(QuizEducativoActivity.EXTRA_TEMA_ID, tema.id);
            startActivity(intent);
        });
        binding.rvTemasQuiz.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTemasQuiz.setAdapter(adapter);

        // Autoavaliações (Sim/Não)
        binding.cardQuizRisco.setOnClickListener(v -> iniciarAutoavaliacao("risco"));
        binding.cardQuizCiclo.setOnClickListener(v -> iniciarAutoavaliacao("ciclo"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega ao voltar de um quiz, para atualizar o progresso.
        carregarProgresso();
    }

    private void carregarProgresso() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            atualizarResumo(new HashMap<>());
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(QuizEducativoActivity.COLECAO_RESULTADOS)
                .whereEqualTo("usuarioId", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Long> melhores = new HashMap<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String quizId = doc.getString("quizId");
                        Long melhor = doc.getLong("melhorPontuacao");
                        if (quizId != null && melhor != null) melhores.put(quizId, melhor);
                    }
                    adapter.atualizarProgresso(melhores);
                    atualizarResumo(melhores);
                });
    }

    /** Progresso geral = acertos (melhor tentativa) / total de perguntas de todos os temas. */
    private void atualizarResumo(Map<String, Long> melhores) {
        int totalPerguntas = 0;
        long totalAcertos = 0;
        int realizados = 0;
        int medalhas = 0;

        for (QuizData.Tema tema : temas) {
            int total = tema.perguntas.size();
            totalPerguntas += total;
            Long melhor = melhores.get(tema.id);
            if (melhor != null) {
                realizados++;
                totalAcertos += melhor;
                if (melhor == total) medalhas++;
            }
        }

        int percentual = totalPerguntas == 0 ? 0 : (int) ((totalAcertos * 100) / totalPerguntas);
        binding.progressGeral.setProgress(percentual);
        binding.tvProgressoGeral.setText(percentual + "%");
        binding.tvResumoProgresso.setText(realizados + " de " + temas.size() + " temas realizados • "
                + medalhas + (medalhas == 1 ? " medalha" : " medalhas"));
    }

    private void iniciarAutoavaliacao(String tema) {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("TEMA_QUIZ", tema);
        startActivity(intent);
    }
}

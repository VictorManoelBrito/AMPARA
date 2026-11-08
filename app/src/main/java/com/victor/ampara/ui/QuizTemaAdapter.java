package com.victor.ampara.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.victor.ampara.databinding.ItemQuizTemaBinding;
import com.victor.ampara.model.QuizData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Lista de temas do quiz com o progresso (melhor pontuação) e a medalha de cada um. */
public class QuizTemaAdapter extends RecyclerView.Adapter<QuizTemaAdapter.ViewHolder> {

    public interface OnTemaClickListener {
        void onClick(QuizData.Tema tema);
    }

    private final List<QuizData.Tema> temas;
    private final OnTemaClickListener listener;
    /** quizId -> melhor pontuação do usuário */
    private Map<String, Long> melhores = new HashMap<>();

    public QuizTemaAdapter(List<QuizData.Tema> temas, OnTemaClickListener listener) {
        this.temas = temas;
        this.listener = listener;
    }

    public void atualizarProgresso(Map<String, Long> melhores) {
        this.melhores = melhores;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemQuizTemaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizData.Tema tema = temas.get(position);
        int total = tema.perguntas.size();
        Long melhor = melhores.get(tema.id);

        holder.b.tvTituloTema.setText(tema.titulo);
        holder.b.tvDescricaoTema.setText(tema.descricao);

        if (melhor == null) {
            holder.b.progressTema.setProgress(0);
            holder.b.tvPercentualTema.setText("0%");
            holder.b.tvStatusTema.setText("Não iniciado");
            holder.b.imgMedalhaTema.setVisibility(View.GONE);
        } else {
            int percentual = (int) ((melhor * 100) / total);
            holder.b.progressTema.setProgress(percentual);
            holder.b.tvPercentualTema.setText(percentual + "%");
            holder.b.tvStatusTema.setText("Melhor resultado: " + melhor + "/" + total);
            holder.b.imgMedalhaTema.setVisibility(melhor == total ? View.VISIBLE : View.GONE);
        }

        holder.b.getRoot().setOnClickListener(v -> listener.onClick(tema));
    }

    @Override
    public int getItemCount() {
        return temas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemQuizTemaBinding b;

        ViewHolder(ItemQuizTemaBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

package com.victor.ampara.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.victor.ampara.databinding.ItemTopicoBinding;
import com.victor.ampara.model.ConteudoEducativo.Topico;

import java.util.List;

/** Lista de tópicos usada nas telas de Conteúdos Educativos e de Leis e Direitos. */
public class TopicoAdapter extends RecyclerView.Adapter<TopicoAdapter.ViewHolder> {

    public interface OnTopicoClickListener {
        void onClick(Topico topico);
    }

    private final List<Topico> topicos;
    private final OnTopicoClickListener listener;

    public TopicoAdapter(List<Topico> topicos, OnTopicoClickListener listener) {
        this.topicos = topicos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTopicoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topico t = topicos.get(position);
        holder.b.tvTituloTopico.setText(t.titulo);
        holder.b.tvResumoTopico.setText(t.resumo);
        holder.b.getRoot().setOnClickListener(v -> listener.onClick(t));
    }

    @Override
    public int getItemCount() {
        return topicos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTopicoBinding b;

        ViewHolder(ItemTopicoBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

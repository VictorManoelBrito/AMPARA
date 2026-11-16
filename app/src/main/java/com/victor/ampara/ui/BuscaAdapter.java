package com.victor.ampara.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.victor.ampara.databinding.ItemTopicoBinding;

import java.util.ArrayList;
import java.util.List;

/** Lista de resultados da busca (reaproveita o visual dos itens de tópico). */
public class BuscaAdapter extends RecyclerView.Adapter<BuscaAdapter.ViewHolder> {

    public static class Item {
        final String titulo;
        final String tipo;
        final String textoBusca;
        final Runnable acao;

        Item(String titulo, String tipo, String textoBusca, Runnable acao) {
            this.titulo = titulo;
            this.tipo = tipo;
            this.textoBusca = textoBusca;
            this.acao = acao;
        }
    }

    public interface OnItemClick { void onClick(Item item); }

    private final List<Item> itens = new ArrayList<>();
    private final OnItemClick listener;

    public BuscaAdapter(OnItemClick listener) { this.listener = listener; }

    public void atualizar(List<Item> novos) {
        itens.clear();
        itens.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTopicoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itens.get(position);
        holder.b.tvTituloTopico.setText(item.titulo);
        holder.b.tvResumoTopico.setText(item.tipo);
        holder.b.getRoot().setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() { return itens.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTopicoBinding b;
        ViewHolder(ItemTopicoBinding b) { super(b.getRoot()); this.b = b; }
    }
}

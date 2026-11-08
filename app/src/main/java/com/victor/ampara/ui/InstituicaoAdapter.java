package com.victor.ampara.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.victor.ampara.databinding.ItemInstituicaoBinding;
import com.victor.ampara.model.Instituicao;

import java.util.ArrayList;
import java.util.List;

public class InstituicaoAdapter extends RecyclerView.Adapter<InstituicaoAdapter.ViewHolder> {

    public interface OnInstituicaoClickListener {
        void onClick(Instituicao instituicao);
    }

    private final List<Instituicao> itens = new ArrayList<>();
    private final OnInstituicaoClickListener listener;

    public InstituicaoAdapter(OnInstituicaoClickListener listener) {
        this.listener = listener;
    }

    public void atualizar(List<Instituicao> novos) {
        itens.clear();
        itens.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInstituicaoBinding b = ItemInstituicaoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Instituicao inst = itens.get(position);
        holder.b.tvNomeInstituicao.setText(inst.getNome());
        holder.b.tvCategoriaInstituicao.setText(inst.getCategoria());

        String telefone = inst.getTelefone() != null ? inst.getTelefone() : "";
        holder.b.tvCidadeTelefone.setText(inst.getCidadeUf() + (telefone.isEmpty() ? "" : " • " + telefone));

        holder.b.getRoot().setOnClickListener(v -> listener.onClick(inst));
        holder.b.btnVerInstituicao.setOnClickListener(v -> listener.onClick(inst));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemInstituicaoBinding b;

        ViewHolder(ItemInstituicaoBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

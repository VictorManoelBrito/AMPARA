package com.victor.ampara.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import com.victor.ampara.R;
import com.victor.ampara.databinding.ItemDenunciaBinding;
import com.victor.ampara.model.Denuncia;
import java.util.List;

public class DenunciaAdapter extends RecyclerView.Adapter<DenunciaAdapter.DenunciaViewHolder> {

    private final List<Denuncia> listaDenuncias;

    public DenunciaAdapter(List<Denuncia> listaDenuncias) {
        this.listaDenuncias = listaDenuncias;
    }

    @NonNull
    @Override
    public DenunciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDenunciaBinding binding = ItemDenunciaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DenunciaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DenunciaViewHolder holder, int position) {
        Denuncia denuncia = listaDenuncias.get(position);
        holder.binding.tvTipo.setText(denuncia.getTipo());
        holder.binding.tvDataHora.setText(denuncia.getDataHoraOcorrencia());
        holder.binding.tvResumo.setText(denuncia.getRelato());

        // Clique para abrir detalhes
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalhesDenunciaActivity.class);
            intent.putExtra("denuncia", denuncia);
            v.getContext().startActivity(intent);
        });

        // Configuração do Status
        String status = denuncia.getStatus() != null ? denuncia.getStatus() : "Em análise";
        holder.binding.tvStatus.setText(status);

        if (status.equalsIgnoreCase("Finalizado")) {
            holder.binding.tvStatus.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_status_finalized));
        } else {
            holder.binding.tvStatus.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_status_pending));
        }

        if (denuncia.getEvidenciaUrl() != null && !denuncia.getEvidenciaUrl().isEmpty()) {
            holder.binding.imgHasAttachment.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imgHasAttachment.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaDenuncias.size();
    }

    static class DenunciaViewHolder extends RecyclerView.ViewHolder {
        ItemDenunciaBinding binding;

        public DenunciaViewHolder(ItemDenunciaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

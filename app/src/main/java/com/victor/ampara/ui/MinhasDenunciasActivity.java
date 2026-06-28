package com.victor.ampara.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.victor.ampara.databinding.ActivityMinhasDenunciasBinding;
import com.victor.ampara.model.Denuncia;
import java.util.ArrayList;
import java.util.List;

public class MinhasDenunciasActivity extends AppCompatActivity {

    private ActivityMinhasDenunciasBinding binding;
    private DenunciaAdapter adapter;
    private List<Denuncia> listaDenuncias;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration denunciaListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinhasDenunciasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        configurarToolbar();
        configurarRecyclerView();
        ouvirDenuncias();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (denunciaListener != null) {
            denunciaListener.remove();
        }
    }

    private void configurarToolbar() {
        binding.toolbarMinhasDenuncias.setNavigationOnClickListener(v -> finish());
    }

    private void configurarRecyclerView() {
        listaDenuncias = new ArrayList<>();
        adapter = new DenunciaAdapter(listaDenuncias);
        binding.rvDenuncias.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDenuncias.setAdapter(adapter);
    }

    private void ouvirDenuncias() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonimo";

        binding.progressCarga.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        denunciaListener = db.collection("denuncias")
                .whereEqualTo("usuarioId", userId)
                .orderBy("dataCriacao", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    binding.progressCarga.setVisibility(View.GONE);
                    
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        listaDenuncias.clear();
                        if (value.isEmpty()) {
                            binding.layoutEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutEmptyState.setVisibility(View.GONE);
                            listaDenuncias.addAll(value.toObjects(Denuncia.class));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

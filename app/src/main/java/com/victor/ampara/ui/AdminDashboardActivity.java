package com.victor.ampara.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.victor.ampara.databinding.ActivityAdminDashboardBinding;
import com.victor.ampara.model.Denuncia;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private DenunciaAdapter adapter;
    private List<Denuncia> allDenuncias;
    private FirebaseFirestore db;
    private int lastUrgentCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        binding.toolbarAdmin.setNavigationOnClickListener(v -> finish());

        configurarRecyclerView();
        carregarTodasDenuncias();
    }

    private void configurarRecyclerView() {
        allDenuncias = new ArrayList<>();
        adapter = new DenunciaAdapter(allDenuncias);
        binding.rvAllDenuncias.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllDenuncias.setAdapter(adapter);
    }

    private void carregarTodasDenuncias() {
        // Ordenar por data de criação, mas poderíamos futuramente ordenar por urgência primeiro
        db.collection("denuncias")
                .orderBy("dataCriacao", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        allDenuncias.clear();
                        List<Denuncia> list = value.toObjects(Denuncia.class);
                        
                        // Lógica manual para colocar URGENTES no topo
                        List<Denuncia> urgentes = new ArrayList<>();
                        List<Denuncia> normais = new ArrayList<>();
                        
                        int countUrgent = 0;
                        int countAnalysis = 0;
                        int countFinished = 0;

                        for (Denuncia d : list) {
                            String urgencia = d.getUrgencia() != null ? d.getUrgencia() : "";
                            String status = d.getStatus() != null ? d.getStatus() : "";

                            if (urgencia.contains("Urgente") || urgencia.equalsIgnoreCase("Alta")) {
                                urgentes.add(d);
                                countUrgent++;
                            } else {
                                normais.add(d);
                            }

                            if (status.equalsIgnoreCase("Finalizado") || status.equalsIgnoreCase("Concluída")) {
                                countFinished++;
                            } else {
                                countAnalysis++;
                            }
                        }

                        allDenuncias.addAll(urgentes);
                        allDenuncias.addAll(normais);
                        
                        binding.tvStatUrgent.setText(String.valueOf(countUrgent));
                        binding.tvStatAnalysis.setText(String.valueOf(countAnalysis));
                        binding.tvStatFinished.setText(String.valueOf(countFinished));
                        
                        // Alerta Sonoro para novas denúncias URGENTES
                        if (lastUrgentCount != -1 && countUrgent > lastUrgentCount) {
                            tocarAlertaUrgente();
                        }
                        lastUrgentCount = countUrgent;
                        
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void tocarAlertaUrgente() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

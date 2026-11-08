package com.victor.ampara.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.victor.ampara.databinding.ActivityRedeApoioBinding;
import com.victor.ampara.model.Instituicao;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Rede de Apoio (3.1.6): canais de emergência fixos + instituições cadastradas
 * pelo administrador no painel web (coleção "instituicoes", somente as ativas).
 */
public class RedeApoioActivity extends AppCompatActivity {

    private ActivityRedeApoioBinding binding;
    private InstituicaoAdapter adapter;
    private ListenerRegistration listener;
    private final List<Instituicao> todas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRedeApoioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarToolbar();
        configurarCliques();
        configurarLista();
        configurarBusca();
        carregarInstituicoes();
    }

    private void configurarToolbar() {
        binding.toolbarRedeApoio.setNavigationOnClickListener(v -> finish());
    }

    private void configurarCliques() {
        binding.btnCallPolicia.setOnClickListener(v -> discar("190"));
        binding.btnCall180.setOnClickListener(v -> discar("180"));
    }

    private void configurarLista() {
        adapter = new InstituicaoAdapter(inst -> {
            Intent intent = new Intent(this, DetalhesInstituicaoActivity.class);
            intent.putExtra(DetalhesInstituicaoActivity.EXTRA_INSTITUICAO, inst);
            startActivity(intent);
        });
        binding.rvInstituicoes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvInstituicoes.setAdapter(adapter);
    }

    private void configurarBusca() {
        binding.editBuscaCidade.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { aplicarFiltro(); }
        });
    }

    private void carregarInstituicoes() {
        binding.progressInstituicoes.setVisibility(View.VISIBLE);

        listener = FirebaseFirestore.getInstance()
                .collection("instituicoes")
                .whereEqualTo("ativo", true)
                .addSnapshotListener((snapshot, erro) -> {
                    binding.progressInstituicoes.setVisibility(View.GONE);
                    if (erro != null || snapshot == null) {
                        binding.tvSemInstituicoes.setText("Não foi possível carregar as instituições. Verifique sua conexão.");
                        binding.tvSemInstituicoes.setVisibility(View.VISIBLE);
                        return;
                    }

                    todas.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Instituicao inst = doc.toObject(Instituicao.class);
                        if (inst != null) {
                            inst.setId(doc.getId());
                            todas.add(inst);
                        }
                    }
                    Collections.sort(todas, (a, b) -> normalizar(a.getNome()).compareTo(normalizar(b.getNome())));
                    aplicarFiltro();
                });
    }

    /** Filtra por cidade ou nome, ignorando acentos e maiúsculas. */
    private void aplicarFiltro() {
        String termo = normalizar(binding.editBuscaCidade.getText() != null
                ? binding.editBuscaCidade.getText().toString() : "");

        List<Instituicao> filtradas = new ArrayList<>();
        for (Instituicao inst : todas) {
            if (termo.isEmpty()
                    || normalizar(inst.getCidade()).contains(termo)
                    || normalizar(inst.getNome()).contains(termo)) {
                filtradas.add(inst);
            }
        }

        adapter.atualizar(filtradas);

        if (filtradas.isEmpty()) {
            binding.tvSemInstituicoes.setText(todas.isEmpty()
                    ? "Nenhuma instituição cadastrada ainda."
                    : "Nenhuma instituição encontrada para essa busca.");
            binding.tvSemInstituicoes.setVisibility(View.VISIBLE);
        } else {
            binding.tvSemInstituicoes.setVisibility(View.GONE);
        }
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    private void discar(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}

package com.victor.ampara.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.victor.ampara.databinding.ActivityBuscaBinding;
import com.victor.ampara.model.ConteudoEducativo;
import com.victor.ampara.model.Instituicao;
import com.victor.ampara.model.QuizData;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca simples da tela inicial: procura, pelo texto digitado, entre as funções
 * do aplicativo, os temas educativos, as leis, os temas do quiz e as instituições
 * ativas da rede de apoio (por nome, cidade ou tipo). Nada é gravado: a busca
 * acontece só no aparelho.
 */
public class BuscaActivity extends AppCompatActivity {

    public static final String EXTRA_TERMO = "EXTRA_TERMO";

    private ActivityBuscaBinding binding;
    private final List<BuscaAdapter.Item> indice = new ArrayList<>();
    private BuscaAdapter adapter;

    public static void abrir(Context context, String termo) {
        Intent intent = new Intent(context, BuscaActivity.class);
        intent.putExtra(EXTRA_TERMO, termo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuscaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarBusca.setNavigationOnClickListener(v -> finish());

        adapter = new BuscaAdapter(item -> item.acao.run());
        binding.rvResultados.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResultados.setAdapter(adapter);

        montarIndiceFixo();
        carregarInstituicoes();

        binding.editBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { filtrar(); }
        });

        String termo = getIntent().getStringExtra(EXTRA_TERMO);
        if (termo != null) binding.editBusca.setText(termo);
        binding.editBusca.requestFocus();
        binding.editBusca.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(binding.editBusca, InputMethodManager.SHOW_IMPLICIT);
        });
        filtrar();
    }

    /** Funções do app, temas educativos, leis e temas do quiz (conteúdo que já está no aparelho). */
    private void montarIndiceFixo() {
        adicionar("Registrar denúncia", "Função", "denunciar ocorrencia violencia anonima identificada relato evidencia foto",
                () -> startActivity(new Intent(this, DenunciaActivity.class)));
        adicionar("Minhas denúncias", "Função", "acompanhar protocolo status andamento",
                () -> startActivity(new Intent(this, MinhasDenunciasActivity.class)));
        adicionar("Rede de apoio", "Função", "ajuda instituicao delegacia creas cras telefone 180 190 emergencia",
                () -> startActivity(new Intent(this, RedeApoioActivity.class)));
        adicionar("Conteúdos educativos", "Função", "aprender temas cartilha pdf",
                () -> startActivity(new Intent(this, ConteudoActivity.class)));
        adicionar("Leis e direitos", "Função", "lei legislacao direito",
                () -> startActivity(new Intent(this, LeisActivity.class)));
        adicionar("Quiz educativo", "Função", "perguntas medalha teste",
                () -> startActivity(new Intent(this, QuizSelectionActivity.class)));
        adicionar("Meu perfil", "Função", "foto conta dados",
                () -> startActivity(new Intent(this, PerfilActivity.class)));
        adicionar("Configurações de segurança", "Função", "pin senha bloqueio disfarce bloco de notas",
                () -> startActivity(new Intent(this, SettingsActivity.class)));

        for (ConteudoEducativo.Topico t : ConteudoEducativo.conteudos()) {
            adicionar(t.titulo, "Conteúdo educativo", t.resumo, () -> TopicoActivity.abrir(this, t.id));
        }
        for (ConteudoEducativo.Topico t : ConteudoEducativo.leis()) {
            adicionar(t.titulo, "Lei", t.resumo, () -> LeituraActivity.abrir(this, t, "Leis e Direitos"));
        }
        for (QuizData.Tema tema : QuizData.temas()) {
            adicionar(tema.titulo, "Quiz", tema.descricao, () -> {
                Intent intent = new Intent(this, QuizEducativoActivity.class);
                intent.putExtra(QuizEducativoActivity.EXTRA_TEMA_ID, tema.id);
                startActivity(intent);
            });
        }
    }

    /** Instituições ativas da rede de apoio (as mesmas que aparecem na tela Rede de Apoio). */
    private void carregarInstituicoes() {
        FirebaseFirestore.getInstance().collection("instituicoes")
                .whereEqualTo("ativo", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Instituicao inst = doc.toObject(Instituicao.class);
                        inst.setId(doc.getId());
                        String cidade = inst.getCidade() != null ? inst.getCidade() : "";
                        String categoria = inst.getCategoria() != null ? inst.getCategoria() : "";
                        adicionar(inst.getNome(), "Rede de apoio • " + cidade, categoria + " " + cidade, () -> {
                            Intent intent = new Intent(this, DetalhesInstituicaoActivity.class);
                            intent.putExtra(DetalhesInstituicaoActivity.EXTRA_INSTITUICAO, inst);
                            startActivity(intent);
                        });
                    }
                    filtrar();
                });
        // Sem internet, a busca continua funcionando para o conteúdo que já está no aparelho.
    }

    private void adicionar(String titulo, String tipo, String palavras, Runnable acao) {
        if (titulo == null) return;
        indice.add(new BuscaAdapter.Item(titulo, tipo,
                normalizar(titulo + " " + tipo + " " + (palavras != null ? palavras : "")), acao));
    }

    private void filtrar() {
        String texto = binding.editBusca.getText() != null ? binding.editBusca.getText().toString() : "";
        String termo = normalizar(texto).trim();
        List<BuscaAdapter.Item> resultado = new ArrayList<>();
        if (!termo.isEmpty()) {
            String[] partes = termo.split("\\s+");
            for (BuscaAdapter.Item item : indice) {
                boolean combina = true;
                for (String p : partes) {
                    if (!item.textoBusca.contains(p)) { combina = false; break; }
                }
                if (combina) resultado.add(item);
            }
        }
        adapter.atualizar(resultado);
        binding.tvSemResultados.setVisibility(!termo.isEmpty() && resultado.isEmpty() ? View.VISIBLE : View.GONE);
        binding.tvDicaBusca.setVisibility(termo.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }
}

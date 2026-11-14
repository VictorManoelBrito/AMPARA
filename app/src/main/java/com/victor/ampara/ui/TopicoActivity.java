package com.victor.ampara.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.victor.ampara.databinding.ActivityTopicoBinding;
import com.victor.ampara.util.SalvarPdfHelper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Conteúdo educativo (3.1.9) dividido em telas. O conteúdo vem de
 * assets/conteudos/{id}.json; a última tela traz as referências e o botão
 * para salvar o material completo em PDF (assets/conteudos/pdf/{id}.pdf).
 */
public class TopicoActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "EXTRA_ID";

    private ActivityTopicoBinding binding;
    private String id;
    private int totalTelas;

    // Abre o seletor do Android para a pessoa escolher onde salvar o PDF
    private final ActivityResultLauncher<String> salvarPdf =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), this::copiarPdf);

    public static void abrir(Context context, String id) {
        Intent intent = new Intent(context, TopicoActivity.class);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTopicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbarTopico.setNavigationOnClickListener(v -> finish());

        id = getIntent().getStringExtra(EXTRA_ID);
        JSONObject conteudo = lerConteudo(id);
        if (conteudo == null) {
            Toast.makeText(this, "Conteúdo indisponível.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbarTopico.setTitle(conteudo.optString("titulo"));
        TelaTopicoAdapter adapter = new TelaTopicoAdapter(conteudo, this::confirmarDownload);
        totalTelas = adapter.getItemCount();
        binding.pagerTopico.setAdapter(adapter);
        binding.progressTopico.setMax(totalTelas);

        binding.pagerTopico.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int posicao) {
                atualizarNavegacao(posicao);
            }
        });
        binding.btnAnterior.setOnClickListener(v ->
                binding.pagerTopico.setCurrentItem(binding.pagerTopico.getCurrentItem() - 1));
        binding.btnProximo.setOnClickListener(v -> {
            int atual = binding.pagerTopico.getCurrentItem();
            if (atual < totalTelas - 1) {
                binding.pagerTopico.setCurrentItem(atual + 1);
            } else {
                finish(); // última tela: "Concluir"
            }
        });
        atualizarNavegacao(0);
    }

    private void atualizarNavegacao(int posicao) {
        binding.tvPaginaTopico.setText((posicao + 1) + " de " + totalTelas);
        binding.progressTopico.setProgressCompat(posicao + 1, true);
        binding.btnAnterior.setEnabled(posicao > 0);
        binding.btnAnterior.setAlpha(posicao > 0 ? 1f : 0.4f);
        binding.btnProximo.setText(posicao == totalTelas - 1 ? "Concluir" : "Próximo");
    }

    private JSONObject lerConteudo(String id) {
        if (id == null) return null;
        try (InputStream in = getAssets().open("conteudos/" + id + ".json")) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] bloco = new byte[8192];
            int lidos;
            while ((lidos = in.read(bloco)) != -1) buffer.write(bloco, 0, lidos);
            return new JSONObject(buffer.toString(StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            return null;
        }
    }

    private void confirmarDownload() {
        SalvarPdfHelper.confirmar(this, () -> salvarPdf.launch("material-educativo-" + id + ".pdf"));
    }

    private void copiarPdf(Uri destino) {
        SalvarPdfHelper.copiar(this, "conteudos/pdf/" + id + ".pdf", destino);
    }
}

package com.victor.ampara.ui;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.victor.ampara.databinding.ActivityConteudoBinding;
import com.victor.ampara.model.ConteudoEducativo;
import com.victor.ampara.util.SalvarPdfHelper;

/** Conteúdos Educativos (3.1.9): lista de temas que abrem a tela de leitura. */
public class ConteudoActivity extends AppCompatActivity {

    // PDF único com todos os temas (assets/conteudos/pdf/conteudo_completo.pdf)
    private static final String PDF_COMPLETO = "conteudos/pdf/conteudo_completo.pdf";

    private final ActivityResultLauncher<String> salvarPdf =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"),
                    destino -> SalvarPdfHelper.copiar(this, PDF_COMPLETO, destino));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityConteudoBinding binding = ActivityConteudoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarConteudo.setNavigationOnClickListener(v -> finish());

        binding.rvConteudos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConteudos.setAdapter(new TopicoAdapter(
                ConteudoEducativo.conteudos(),
                topico -> {
                    if (topico.id != null) {
                        TopicoActivity.abrir(this, topico.id);
                    } else {
                        LeituraActivity.abrir(this, topico, "Conteúdos Educativos");
                    }
                }));

        binding.btnBaixarTudo.setOnClickListener(v -> SalvarPdfHelper.confirmar(this,
                () -> salvarPdf.launch("material-educativo-completo.pdf")));
    }
}

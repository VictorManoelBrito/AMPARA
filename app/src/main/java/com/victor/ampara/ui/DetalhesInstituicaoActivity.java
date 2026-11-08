package com.victor.ampara.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.victor.ampara.databinding.ActivityDetalhesInstituicaoBinding;
import com.victor.ampara.model.Instituicao;

/**
 * Detalhes da Instituição (3.1.7): telefone, e-mail/site, endereço e descrição,
 * com atalhos para ligar e abrir o mapa.
 */
public class DetalhesInstituicaoActivity extends AppCompatActivity {

    public static final String EXTRA_INSTITUICAO = "EXTRA_INSTITUICAO";

    private ActivityDetalhesInstituicaoBinding binding;
    private Instituicao instituicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalhesInstituicaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarDetalhesInstituicao.setNavigationOnClickListener(v -> finish());

        instituicao = lerExtra();
        if (instituicao == null) {
            Toast.makeText(this, "Instituição não encontrada.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        preencherTela();
        configurarAcoes();
    }

    @SuppressWarnings("deprecation")
    private Instituicao lerExtra() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getSerializableExtra(EXTRA_INSTITUICAO, Instituicao.class);
        }
        return (Instituicao) getIntent().getSerializableExtra(EXTRA_INSTITUICAO);
    }

    private void preencherTela() {
        binding.tvDetNome.setText(instituicao.getNome());
        binding.tvDetCategoria.setText(instituicao.getCategoria());
        binding.tvDetCidade.setText(instituicao.getCidadeUf());

        mostrarOuEsconder(binding.tvDetDescricao, binding.tvDetDescricao, instituicao.getDescricao());
        mostrarOuEsconder(binding.rowTelefone, binding.tvDetTelefone, instituicao.getTelefone());
        mostrarOuEsconder(binding.rowEmail, binding.tvDetEmail, instituicao.getEmail());
        mostrarOuEsconder(binding.rowEndereco, binding.tvDetEndereco, instituicao.getEndereco());

        binding.btnLigarInstituicao.setVisibility(
                TextUtils.isEmpty(instituicao.getTelefone()) ? View.GONE : View.VISIBLE);
        binding.btnMapaInstituicao.setVisibility(
                TextUtils.isEmpty(instituicao.getEndereco()) ? View.GONE : View.VISIBLE);
    }

    private void mostrarOuEsconder(View container, android.widget.TextView texto, String valor) {
        if (TextUtils.isEmpty(valor)) {
            container.setVisibility(View.GONE);
        } else {
            texto.setText(valor);
            container.setVisibility(View.VISIBLE);
        }
    }

    private void configurarAcoes() {
        View.OnClickListener ligar = v -> discar(instituicao.getTelefone());
        binding.rowTelefone.setOnClickListener(ligar);
        binding.btnLigarInstituicao.setOnClickListener(ligar);

        View.OnClickListener mapa = v -> abrirMapa();
        binding.rowEndereco.setOnClickListener(mapa);
        binding.btnMapaInstituicao.setOnClickListener(mapa);

        binding.rowEmail.setOnClickListener(v -> abrirEmailOuSite(instituicao.getEmail()));
    }

    private void discar(String telefone) {
        if (TextUtils.isEmpty(telefone)) return;
        String numero = telefone.replaceAll("[^0-9+]", "");
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero)));
    }

    private void abrirMapa() {
        String consulta = instituicao.getEndereco() + ", " + instituicao.getCidadeUf();
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(consulta));
        abrir(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void abrirEmailOuSite(String valor) {
        if (TextUtils.isEmpty(valor)) return;
        if (valor.contains("@") && !valor.startsWith("http")) {
            abrir(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + valor)));
        } else {
            String url = valor.startsWith("http") ? valor : "https://" + valor;
            abrir(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    private void abrir(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nenhum aplicativo disponível para abrir.", Toast.LENGTH_SHORT).show();
        }
    }
}

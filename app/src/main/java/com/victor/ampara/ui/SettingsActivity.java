package com.victor.ampara.ui;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.victor.ampara.databinding.ActivitySettingsBinding;
import com.victor.ampara.util.BloqueioHelper;

/**
 * Configurações de privacidade e segurança:
 * - Modo disfarçado (ícone e tela de "Bloco de Notas"), que exige PIN.
 * - Senha do aplicativo (PIN de 4 a 6 números).
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private boolean atualizandoTela = false;

    private interface AoConfirmar {
        void executar(String pin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarSettings.setNavigationOnClickListener(v -> finish());

        binding.switchDisfarce.setOnCheckedChangeListener((b, ligado) -> {
            if (!atualizandoTela) aoMudarDisfarce(ligado);
        });
        binding.switchPin.setOnCheckedChangeListener((b, ligado) -> {
            if (!atualizandoTela) aoMudarPin(ligado);
        });
        binding.btnAlterarPin.setOnClickListener(v -> alterarPin());

        atualizarTela();
    }

    private void atualizarTela() {
        atualizandoTela = true;
        boolean pin = BloqueioHelper.pinAtivo(this);
        binding.switchPin.setChecked(pin);
        binding.switchDisfarce.setChecked(BloqueioHelper.disfarceAtivo(this));
        binding.btnAlterarPin.setVisibility(pin ? View.VISIBLE : View.GONE);
        atualizandoTela = false;
    }

    // ---------------------------------------------------------------- Modo disfarçado

    private void aoMudarDisfarce(boolean ligar) {
        if (!ligar) {
            BloqueioHelper.definirDisfarce(this, false);
            Toast.makeText(this, "O ícone do AMPARA voltará em instantes.", Toast.LENGTH_LONG).show();
            atualizarTela();
            return;
        }

        if (BloqueioHelper.pinAtivo(this)) {
            ativarDisfarce();
        } else {
            // O disfarce depende do PIN: sem ele não haveria como entrar no AMPARA.
            criarPin("Para usar o modo disfarçado, crie um PIN. Você vai digitá-lo no bloco de notas para entrar no AMPARA.",
                    pin -> ativarDisfarce());
        }
    }

    private void ativarDisfarce() {
        BloqueioHelper.definirDisfarce(this, true);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Modo disfarçado ativado")
                .setMessage("Em instantes o ícone vira \"Bloco de Notas\". Ao abrir, aparece um bloco de notas comum: "
                        + "para entrar no AMPARA, digite seu PIN na nota e toque em Salvar.")
                .setPositiveButton("Entendi", null)
                .show();
        atualizarTela();
    }

    // ---------------------------------------------------------------- PIN

    private void aoMudarPin(boolean ligar) {
        if (ligar) {
            criarPin("Crie um PIN de 4 a 6 números para proteger o acesso ao AMPARA.", pin ->
                    Toast.makeText(this, "PIN ativado.", Toast.LENGTH_SHORT).show());
            return;
        }

        if (BloqueioHelper.disfarceAtivo(this)) {
            Toast.makeText(this, "Desative o modo disfarçado antes de remover o PIN.", Toast.LENGTH_LONG).show();
            atualizarTela();
            return;
        }

        confirmarPinAtual(() -> {
            BloqueioHelper.removerPin(this);
            Toast.makeText(this, "PIN removido.", Toast.LENGTH_SHORT).show();
            atualizarTela();
        });
    }

    private void alterarPin() {
        confirmarPinAtual(() -> criarPin("Digite o novo PIN.", pin ->
                Toast.makeText(this, "PIN alterado.", Toast.LENGTH_SHORT).show()));
    }

    /** Diálogo para criar um PIN (digitado duas vezes). */
    private void criarPin(String mensagem, AoConfirmar aoCriar) {
        EditText campo1 = campoPin("Novo PIN");
        EditText campo2 = campoPin("Repita o PIN");
        LinearLayout layout = layoutCampos(campo1, campo2);

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle("Criar PIN")
                .setMessage(mensagem)
                .setView(layout)
                .setNegativeButton("Cancelar", (d, w) -> atualizarTela())
                .setPositiveButton("Salvar", null)
                .setOnCancelListener(d -> atualizarTela())
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pin = campo1.getText().toString();
            if (!BloqueioHelper.pinValido(pin)) {
                campo1.setError("Use de 4 a 6 números");
                return;
            }
            if (!pin.equals(campo2.getText().toString())) {
                campo2.setError("Os PINs não conferem");
                return;
            }
            BloqueioHelper.definirPin(this, pin);
            dialogo.dismiss();
            aoCriar.executar(pin);
            atualizarTela();
        }));
        dialogo.show();
    }

    /** Pede o PIN atual antes de remover ou trocar. */
    private void confirmarPinAtual(Runnable aoConfirmar) {
        EditText campo = campoPin("PIN atual");
        LinearLayout layout = layoutCampos(campo);

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle("Confirme seu PIN")
                .setView(layout)
                .setNegativeButton("Cancelar", (d, w) -> atualizarTela())
                .setPositiveButton("Confirmar", null)
                .setOnCancelListener(d -> atualizarTela())
                .create();

        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int espera = BloqueioHelper.segundosDeEspera(this);
            if (espera > 0) {
                campo.setError("Muitas tentativas. Aguarde " + espera + " s.");
                return;
            }
            if (BloqueioHelper.verificarPin(this, campo.getText().toString())) {
                dialogo.dismiss();
                aoConfirmar.run();
            } else {
                campo.setText("");
                campo.setError("PIN incorreto");
            }
        }));
        dialogo.show();
    }

    private EditText campoPin(String dica) {
        EditText campo = new EditText(this);
        campo.setHint(dica);
        campo.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        campo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        return campo;
    }

    private LinearLayout layoutCampos(EditText... campos) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);
        for (EditText c : campos) layout.addView(c);
        return layout;
    }
}

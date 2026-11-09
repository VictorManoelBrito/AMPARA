package com.victor.ampara.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victor.ampara.databinding.ActivityBloqueioBinding;
import com.victor.ampara.util.BloqueioHelper;

/** Tela de PIN exibida ao abrir o app (quando a senha está ativa e o modo disfarçado não). */
public class BloqueioActivity extends AppCompatActivity {

    public static final String EXTRA_SOBREPOR = "EXTRA_SOBREPOR";

    private ActivityBloqueioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBloqueioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnDesbloquear.setOnClickListener(v -> tentar());
        binding.editPin.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                tentar();
                return true;
            }
            return false;
        });
        binding.btnEsqueciPin.setOnClickListener(v -> esqueciPin());

        // Voltar não pula o bloqueio: apenas manda o app para segundo plano.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });
    }

    private void tentar() {
        binding.tilPin.setError(null);

        int espera = BloqueioHelper.segundosDeEspera(this);
        if (espera > 0) {
            binding.tilPin.setError("Muitas tentativas. Aguarde " + espera + " s.");
            return;
        }

        String pin = binding.editPin.getText() != null ? binding.editPin.getText().toString() : "";
        if (BloqueioHelper.verificarPin(this, pin)) {
            finish();
            overridePendingTransition(0, 0);
        } else {
            binding.editPin.setText("");
            int novaEspera = BloqueioHelper.segundosDeEspera(this);
            binding.tilPin.setError(novaEspera > 0
                    ? "Muitas tentativas. Aguarde " + novaEspera + " s."
                    : "PIN incorreto");
        }
    }

    /** Remove o PIN após confirmar a senha da conta (reautenticação no Firebase). */
    private void esqueciPin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null) {
            // Sem sessão: os dados continuam protegidos pelo login da conta.
            BloqueioHelper.removerPin(this);
            BloqueioHelper.marcarDesbloqueado();
            Toast.makeText(this, "PIN removido. Entre com sua conta e crie um novo PIN em Configurações.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        EditText campoSenha = new EditText(this);
        campoSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        campoSenha.setHint("Senha da conta");
        FrameLayout container = new FrameLayout(this);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, 0);
        container.addView(campoSenha);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Esqueci meu PIN")
                .setMessage("Para remover o PIN, confirme a senha da conta " + user.getEmail() + ".")
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (d, w) -> {
                    String senha = campoSenha.getText().toString();
                    if (senha.isEmpty()) return;
                    AuthCredential credencial = EmailAuthProvider.getCredential(user.getEmail(), senha);
                    user.reauthenticate(credencial)
                            .addOnSuccessListener(ok -> {
                                BloqueioHelper.removerPin(this);
                                BloqueioHelper.marcarDesbloqueado();
                                Toast.makeText(this, "PIN removido. Crie um novo em Configurações.", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Senha da conta incorreta.", Toast.LENGTH_SHORT).show());
                })
                .show();
    }
}

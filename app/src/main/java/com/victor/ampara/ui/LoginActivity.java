package com.victor.ampara.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.victor.ampara.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onStart(){
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            verificarContaAtiva(null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> abrirDialogoRedefinirSenha());

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString();
            String password = binding.edtPassword.getText().toString();

            if (validarCampos(email, password)) {
                autenticarUsuario(v, email, password);
            }
        });
    }

    private boolean validarCampos(String email, String password) {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        if (email.isEmpty()) {
            binding.tilEmail.setError("Informe seu e-mail");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Informe um e-mail válido");
            return false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Informe sua senha");
            return false;
        }

        return true;
    }

    private void autenticarUsuario(View v, String email, String password) {
        // Desabilitar botão para evitar cliques duplos
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Acessando...");

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        verificarContaAtiva(v);
                    } else {
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText("Entrar");
                        
                        // Mensagem genérica por segurança: não diz se o erro é no e-mail ou na senha
                        String erroSeguro = "E-mail ou senha incorretos. Tente novamente.";
                        exibirSnackbar(v, erroSeguro);
                        
                        // Opcional: destacar ambos os campos em vermelho
                        binding.tilEmail.setError(" ");
                        binding.tilPassword.setError(" ");
                    }
                });
    }

    /**
     * Contas desativadas pelo administrador (campo "ativo" = false em Usuarios)
     * não podem acessar o aplicativo.
     */
    private void verificarContaAtiva(View v) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("Usuarios").document(uid).get()
                .addOnCompleteListener(task -> {
                    boolean desativada = task.isSuccessful()
                            && task.getResult() != null
                            && Boolean.FALSE.equals(task.getResult().getBoolean("ativo"));

                    if (desativada) {
                        FirebaseAuth.getInstance().signOut();
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText("Entrar");
                        exibirSnackbar(v != null ? v : binding.getRoot(),
                                "Sua conta está desativada. Entre em contato com o suporte do AMPARA.");
                    } else {
                        irParaTelaPrincipal();
                    }
                });
    }

    /**
     * "Esqueci minha senha": pede o e-mail e o Firebase Authentication envia
     * um link para a pessoa criar uma nova senha. O app nunca vê a senha.
     */
    private void abrirDialogoRedefinirSenha() {
        TextInputLayout til = new TextInputLayout(this, null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        til.setHint("E-mail da conta");
        TextInputEditText campo = new TextInputEditText(til.getContext());
        campo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        campo.setText(binding.edtEmail.getText());
        til.addView(campo);

        FrameLayout container = new FrameLayout(this);
        int margem = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(margem, margem / 2, margem, 0);
        container.addView(til);

        androidx.appcompat.app.AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle("Redefinir senha")
                .setMessage("Informe o e-mail da sua conta. Vamos enviar um link para você criar uma nova senha.")
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar", null)
                .create();

        // O botão "Enviar" só fecha a janela se o e-mail for válido
        dialogo.setOnShowListener(d -> dialogo
                .getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(b -> {
                    String email = campo.getText() == null ? "" : campo.getText().toString().trim();
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        til.setError("Informe um e-mail válido");
                        return;
                    }
                    til.setError(null);
                    dialogo.dismiss();
                    enviarEmailRedefinicao(email);
                }));
        dialogo.show();
    }

    private void enviarEmailRedefinicao(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("pt"); // e-mail do Firebase em português

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            // E-mail sem conta também cai aqui: a resposta é a mesma, para não revelar quem usa o app
            if (task.isSuccessful()
                    || task.getException() instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                // Mensagem neutra: não confirma se o e-mail tem conta (privacidade)
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Verifique seu e-mail")
                        .setMessage("Se existir uma conta com " + email + ", você vai receber um link "
                                + "para criar uma nova senha. Confira também a caixa de spam.")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                exibirSnackbar(binding.getRoot(),
                        "Não foi possível enviar agora. Verifique sua internet e tente novamente.");
            }
        });
    }

    private void irParaTelaPrincipal(){
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void exibirSnackbar(View v, String mensagem) {
        Snackbar.make(v, mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.WHITE)
                .setTextColor(Color.BLACK)
                .show();
    }
}

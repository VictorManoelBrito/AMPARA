package com.victor.ampara.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.victor.ampara.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onStart(){
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            irParaTelaPrincipal();
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
                        irParaTelaPrincipal();
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

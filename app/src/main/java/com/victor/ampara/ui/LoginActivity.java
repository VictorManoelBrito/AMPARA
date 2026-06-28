package com.victor.ampara.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
    private final String[] mensagens = {"Preencha todos os campos", "Login efetuado com sucesso"};

    @Override
    protected void onStart(){
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            irParaTelaPrincipal();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Clique para abrir a tela de Cadastro
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Clique para realizar o Login
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString();
            String password = binding.edtPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                exibirSnackbar(v, mensagens[0]);
            } else {
                autenticarUsuario(v, email, password);
            }
        });
    }

    private void autenticarUsuario(View v, String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    exibirSnackbar(v, mensagens[1]);
                    irParaTelaPrincipal();
                } else {
                    String erro = "Erro ao fazer login. Verifique e-mail e senha.";
                    exibirSnackbar(v, erro);
                }
            }
        });
    }
    private void irParaTelaPrincipal(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void exibirSnackbar(View v, String mensagem) {
        Snackbar snackbar = Snackbar.make(v, mensagem, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.WHITE);
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();
    }
}

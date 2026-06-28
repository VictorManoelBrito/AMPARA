package com.victor.ampara.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.victor.ampara.databinding.ActivityRegisterBinding;



import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private final String[] mensagens = {"Preencha todos os campos", "Senhas não conferem", "Erro ao cadastrar", "Cadastro realizado com sucesso"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializa o ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.edtName.getText().toString();
                String phone = binding.edtPhone.getText().toString();
                String email = binding.edtEmail.getText().toString();
                String password = binding.edtPassword.getText().toString();
                String confirmPassword = binding.edtConfirmPassword.getText().toString();

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    exibirSnackbar(v, mensagens[0]);
                } else if (!password.equals(confirmPassword)) {
                    exibirSnackbar(v, mensagens[1]);
                } else {
                    cadastrarUsuario(v, name, phone, email, password);
                }
            }
        });
    }

    private void cadastrarUsuario(View v, String name, String phone, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    
                    // 1. Salva Nome e Telefone no Firestore
                    salvarDadosUsuario(name, phone);
                    
                    // 2. Mostra sucesso
                    exibirSnackbar(v, mensagens[3]);
                    
                    // 3. Volta para a tela anterior (Login) após 2 segundos
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);

                } else {
                    String erro;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erro = "A senha deve ter pelo menos 6 caracteres";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erro = "Este e-mail já está cadastrado";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "E-mail inválido";
                    } catch (Exception e) {
                        erro = "Erro ao cadastrar usuário";
                    }
                    exibirSnackbar(v, erro);
                }
            }
        });
    }

    private void salvarDadosUsuario(String name, String phone) {
        // Pega o ID único do usuário recém criado
        String usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Cria o mapa de dados
        Map<String, Object> usuarios = new HashMap<>();
        usuarios.put("nome", name);
        usuarios.put("telefone", phone);

        // Salva no banco de dados na coleção "Usuarios"
        FirebaseFirestore.getInstance().collection("Usuarios").document(usuarioID)
                .set(usuarios);
    }

    private void exibirSnackbar(View v, String mensagem) {
        Snackbar snackbar = Snackbar.make(v, mensagem, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.WHITE);
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();
    }
}

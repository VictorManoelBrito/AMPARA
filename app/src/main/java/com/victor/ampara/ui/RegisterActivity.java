package com.victor.ampara.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.victor.ampara.databinding.ActivityRegisterBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private final String[] mensagens = {"Preencha todos os campos", "Senhas não conferem", "Erro ao cadastrar", "Cadastro realizado com sucesso"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarMascaras();

        binding.btnRegister.setOnClickListener(v -> {
            limparErros();
            
            String name = binding.edtName.getText().toString();
            String phone = binding.edtPhone.getText().toString();
            String email = binding.edtEmail.getText().toString();
            String password = binding.edtPassword.getText().toString();
            String confirmPassword = binding.edtConfirmPassword.getText().toString();

            if (validarCampos(name, phone, email, password, confirmPassword)) {
                cadastrarUsuario(v, name, phone, email, password);
            }
        });
    }

    private void configurarMascaras() {
        binding.edtPhone.addTextChangedListener(new MaskWatcher("(##) #####-####"));
    }

    private void limparErros() {
        binding.tilName.setError(null);
        binding.tilPhone.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private boolean validarCampos(String name, String phone, String email, String password, String confirmPassword) {
        boolean valido = true;

        if (name.isEmpty()) {
            binding.tilName.setError("Informe seu nome completo");
            valido = false;
        }

        if (phone.length() < 14) {
            binding.tilPhone.setError("Informe um telefone válido");
            valido = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Informe um e-mail válido");
            valido = false;
        }

        if (!validarSenhaForte(password)) {
            binding.tilPassword.setError("A senha deve conter no mínimo 8 caracteres, uma letra maiúscula, um número e um caractere especial");
            valido = false;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("As senhas não conferem");
            valido = false;
        }

        return valido;
    }

    private boolean validarSenhaForte(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return Pattern.compile(regex).matcher(password).matches();
    }

    private void cadastrarUsuario(View v, String name, String phone, String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        salvarDadosUsuario(name, phone, email);
                        exibirSnackbar(v, mensagens[3]);
                        new Handler().postDelayed(this::finish, 2000);
                    } else {
                        String erro;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            erro = "Senha muito fraca";
                        } catch (FirebaseAuthUserCollisionException e) {
                            erro = "Este e-mail já está cadastrado";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            erro = "E-mail inválido";
                        } catch (Exception e) {
                            erro = "Erro ao cadastrar usuário";
                        }
                        exibirSnackbar(v, erro);
                    }
                });
    }

    /**
     * Dados do usuário (entidade usuario do diagrama de classes).
     * Seguindo a LGPD (minimização), não coletamos CPF.
     */
    private void salvarDadosUsuario(String name, String phone, String email) {
        String usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> usuarios = new HashMap<>();
        usuarios.put("nome", name);
        usuarios.put("telefone", phone);
        usuarios.put("email", email);
        usuarios.put("ativo", true);
        usuarios.put("dataCadastro", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("Usuarios").document(usuarioID)
                .set(usuarios);
    }

    private void exibirSnackbar(View v, String mensagem) {
        Snackbar.make(v, mensagem, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.WHITE)
                .setTextColor(Color.BLACK)
                .show();
    }

    private static class MaskWatcher implements TextWatcher {
        private boolean isRunning = false;
        private boolean isDeleting = false;
        private final String mask;

        public MaskWatcher(String mask) {
            this.mask = mask;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            isDeleting = count > after;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (isRunning || isDeleting) return;
            isRunning = true;

            String editableValue = editable.toString();
            StringBuilder formatted = new StringBuilder();
            String unformatted = editableValue.replaceAll("[^\\d]", "");

            int i = 0;
            for (char m : mask.toCharArray()) {
                if (m != '#') {
                    if (i < unformatted.length()) formatted.append(m);
                    continue;
                }
                if (i < unformatted.length()) {
                    formatted.append(unformatted.charAt(i));
                    i++;
                } else {
                    break;
                }
            }

            editable.replace(0, editable.length(), formatted.toString());
            isRunning = false;
        }
    }
}

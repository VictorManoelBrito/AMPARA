package com.victor.ampara.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import android.widget.ImageView;
import android.widget.TextView;
import com.victor.ampara.R;
import com.victor.ampara.databinding.ActivityMainBinding;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration statusNotificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        configurarCliques();
        carregarDadosUsuario();
        ouvirMudancasStatus();
        configurarNotificacoes();
    }

    /** Firebase Cloud Messaging: canal, permissão (Android 13+) e registro do aparelho. */
    private void configurarNotificacoes() {
        com.victor.ampara.util.NotificationHelper.createNotificationChannel(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                && androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 200);
        }

        com.victor.ampara.util.SessaoHelper.registrarNotificacoes();
    }

    private void ouvirMudancasStatus() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            statusNotificationListener = db.collection("denuncias")
                    .whereEqualTo("usuarioId", user.getUid())
                    .addSnapshotListener((value, error) -> {
                        if (value != null && !value.getMetadata().hasPendingWrites()) {
                            for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                                    String novoStatus = dc.getDocument().getString("status");
                                    String tipo = dc.getDocument().getString("tipo");
                                    com.victor.ampara.util.NotificationHelper.showNotification(
                                            this, 
                                            "Status Atualizado", 
                                            "Sua denúncia de '" + tipo + "' agora está: " + novoStatus
                                    );
                                }
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusNotificationListener != null) {
            statusNotificationListener.remove();
        }
    }

    private void carregarDadosUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            
            // Referências aos campos do Drawer (Header)
            View headerView = binding.navigationView.getHeaderView(0);
            TextView tvNavName = headerView.findViewById(R.id.tvNavName);
            TextView tvNavEmail = headerView.findViewById(R.id.tvNavEmail);
            ImageView imgNavProfile = headerView.findViewById(R.id.imgNavProfile);

            // Email do FirebaseAuth
            tvNavEmail.setText(user.getEmail());

            db.collection("Usuarios").document(userId)
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // LGPD: o CPF não é mais coletado; remove de cadastros antigos.
                            if (documentSnapshot.contains("cpf")) {
                                documentSnapshot.getReference().update("cpf", com.google.firebase.firestore.FieldValue.delete());
                            }

                            String nome = documentSnapshot.getString("nome");
                            String fotoUrl = documentSnapshot.getString("fotoUrl");
                            byte[] foto = com.victor.ampara.util.ImagemHelper.decodificar(documentSnapshot.getString("fotoBase64"));
                            
                            if (nome != null && !nome.isEmpty()) {
                                binding.tvUserName.setText("Olá, " + nome + "!");
                                tvNavName.setText(nome);
                            }
                            
                            if (foto != null) {
                                Glide.with(this).load(foto).into(binding.imgProfile);
                                Glide.with(this).load(foto).into(imgNavProfile);
                            } else if (fotoUrl != null && !fotoUrl.isEmpty()) {
                                Glide.with(this).load(fotoUrl).into(binding.imgProfile);
                                Glide.with(this).load(fotoUrl).into(imgNavProfile);
                            }
                        }
                    });
        }
    }

    private void configurarCliques() {
        // Menu Lateral
        binding.imgMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        // Perfil do Usuário (3.1.8)
        binding.cardProfile.setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));

        // Cards principais
        binding.cardDenuncia.setOnClickListener(v -> startActivity(new Intent(this, DenunciaActivity.class)));
        binding.cardMinhasDenuncias.setOnClickListener(v -> startActivity(new Intent(this, MinhasDenunciasActivity.class)));
        binding.cardRedeApoio.setOnClickListener(v -> startActivity(new Intent(this, RedeApoioActivity.class)));

        // Educacional
        binding.btnLeis.setOnClickListener(v -> startActivity(new Intent(this, LeisActivity.class)));
        binding.btnConteudo.setOnClickListener(v -> startActivity(new Intent(this, ConteudoActivity.class)));
        binding.btnQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizSelectionActivity.class)));
        binding.cardBannerQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizSelectionActivity.class)));

        // NavigationView Itens
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_denuncias) {
                startActivity(new Intent(this, MinhasDenunciasActivity.class));
            } else if (id == R.id.nav_apoio) {
                startActivity(new Intent(this, RedeApoioActivity.class));
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                com.victor.ampara.util.SessaoHelper.sair(this);
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}

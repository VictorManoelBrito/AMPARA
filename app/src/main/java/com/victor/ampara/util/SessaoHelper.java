package com.victor.ampara.util;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.victor.ampara.ui.LoginActivity;

import java.util.HashMap;
import java.util.Map;

/** Ações de sessão compartilhadas: registrar o aparelho no FCM e sair da conta. */
public final class SessaoHelper {

    public static final String TOPICO_GERAL = "geral";

    private SessaoHelper() {
    }

    /**
     * Registra o aparelho no Firebase Cloud Messaging: salva o token no usuário
     * (para notificações individuais, ex.: mudança de status da denúncia) e
     * inscreve no tópico "geral" (avisos enviados a todos pelo Console do Firebase).
     */
    public static void registrarNotificacoes() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPICO_GERAL);
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(SessaoHelper::salvarToken);
    }

    public static void salvarToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || token == null) return;

        Map<String, Object> dados = new HashMap<>();
        dados.put("fcmToken", token);
        FirebaseFirestore.getInstance().collection("Usuarios").document(user.getUid())
                .update(dados);
    }

    /** Remove o token do aparelho (para não receber notificações de outra conta) e sai. */
    public static void sair(Activity activity) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("Usuarios").document(user.getUid())
                    .update("fcmToken", FieldValue.delete());
        }
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}

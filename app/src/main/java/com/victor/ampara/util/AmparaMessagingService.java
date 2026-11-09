package com.victor.ampara.util;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Serviço do Firebase Cloud Messaging.
 * - Com o app FECHADO/em segundo plano, o próprio Android exibe a notificação.
 * - Com o app ABERTO, este método recebe a mensagem e nós a exibimos.
 */
public class AmparaMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        SessaoHelper.salvarToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Mudanças de status com o app aberto já são avisadas pelo listener da MainActivity;
        // evitamos notificação duplicada.
        if ("status".equals(message.getData().get("tipo"))) return;

        String titulo = "AMPARA";
        String texto = "";
        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) titulo = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null) texto = message.getNotification().getBody();
        } else {
            if (message.getData().get("titulo") != null) titulo = message.getData().get("titulo");
            if (message.getData().get("mensagem") != null) texto = message.getData().get("mensagem");
        }

        if (!texto.isEmpty()) {
            NotificationHelper.showNotification(this, titulo, texto);
        }
    }
}

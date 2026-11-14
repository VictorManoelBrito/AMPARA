package com.victor.ampara.util;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Salva um PDF que vem dentro do app (pasta assets) num local escolhido pela pessoa.
 * O app não grava nada sozinho em Downloads: o seletor do Android é aberto para que
 * ela escolha uma pasta e um nome discretos.
 */
public final class SalvarPdfHelper {

    private SalvarPdfHelper() {
    }

    /** Aviso de privacidade antes de abrir o seletor. */
    public static void confirmar(Activity activity, Runnable abrirSeletor) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Salvar material em PDF")
                .setMessage("O arquivo ficará salvo no seu celular, e quem tiver acesso ao aparelho poderá vê-lo. "
                        + "Na próxima tela, escolha uma pasta e um nome de arquivo com os quais você se sinta segura(o).")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Escolher local", (d, w) -> abrirSeletor.run())
                .show();
    }

    /** Copia o PDF de assets para o destino escolhido (em segundo plano). */
    public static void copiar(Activity activity, String caminhoAsset, Uri destino) {
        if (destino == null) return; // a pessoa cancelou
        new Thread(() -> {
            boolean ok;
            try (InputStream in = activity.getAssets().open(caminhoAsset);
                 OutputStream out = activity.getContentResolver().openOutputStream(destino)) {
                if (out == null) throw new IllegalStateException("Destino indisponível");
                byte[] bloco = new byte[8192];
                int lidos;
                while ((lidos = in.read(bloco)) != -1) out.write(bloco, 0, lidos);
                ok = true;
            } catch (Exception e) {
                ok = false;
            }
            boolean sucesso = ok;
            activity.runOnUiThread(() -> Toast.makeText(activity,
                    sucesso ? "PDF salvo com sucesso." : "Não foi possível salvar o PDF.",
                    Toast.LENGTH_LONG).show());
        }).start();
    }
}

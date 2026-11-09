package com.victor.ampara.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Compressão de imagens para armazenamento no Cloud Firestore.
 *
 * Desde 2024 o Cloud Storage for Firebase exige o plano pago (Blaze). Para manter o
 * protótipo no plano gratuito, as imagens são redimensionadas, comprimidas em JPEG e
 * gravadas no Firestore em Base64 (cada documento aceita até 1 MiB).
 */
public final class ImagemHelper {

    /** Evidências: lado maior com até 1600 px e no máximo ~650 KB (cabe com folga em 1 MiB após o Base64). */
    public static final int EVIDENCIA_LADO_MAX = 1600;
    public static final int EVIDENCIA_BYTES_MAX = 650 * 1024;

    /** Foto de perfil: pequena, pois aparece só em miniatura. */
    public static final int PERFIL_LADO_MAX = 400;
    public static final int PERFIL_BYTES_MAX = 60 * 1024;

    private ImagemHelper() {
    }

    public static class Resultado {
        public final String base64;
        public final int largura;
        public final int altura;
        public final int bytes;

        Resultado(String base64, int largura, int altura, int bytes) {
            this.base64 = base64;
            this.largura = largura;
            this.altura = altura;
            this.bytes = bytes;
        }
    }

    /**
     * Lê a imagem, corrige a rotação da câmera, reduz o tamanho e comprime.
     * Deve ser chamado fora da thread principal.
     */
    public static Resultado comprimir(Context context, Uri uri, int ladoMax, int bytesMax) throws IOException {
        // 1) Lê só as dimensões para decodificar já reduzido (economiza memória)
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, opcoes);
        }
        if (opcoes.outWidth <= 0 || opcoes.outHeight <= 0) {
            throw new IOException("Arquivo não é uma imagem válida.");
        }

        int amostra = 1;
        while (Math.max(opcoes.outWidth, opcoes.outHeight) / (amostra * 2) >= ladoMax) {
            amostra *= 2;
        }

        BitmapFactory.Options decodificar = new BitmapFactory.Options();
        decodificar.inSampleSize = amostra;
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, decodificar);
        }
        if (bitmap == null) throw new IOException("Não foi possível ler a imagem.");

        // 2) Corrige a rotação (fotos da câmera)
        bitmap = corrigirRotacao(context, uri, bitmap);

        // 3) Redimensiona para o lado maior = ladoMax
        bitmap = redimensionar(bitmap, ladoMax);

        // 4) Comprime, baixando a qualidade (e depois o tamanho) até caber no limite
        int qualidade = 85;
        byte[] dados = jpeg(bitmap, qualidade);
        while (dados.length > bytesMax) {
            if (qualidade > 45) {
                qualidade -= 10;
            } else {
                bitmap = redimensionar(bitmap, (int) (Math.max(bitmap.getWidth(), bitmap.getHeight()) * 0.8));
            }
            dados = jpeg(bitmap, qualidade);
        }

        Resultado r = new Resultado(Base64.encodeToString(dados, Base64.NO_WRAP),
                bitmap.getWidth(), bitmap.getHeight(), dados.length);
        bitmap.recycle();
        return r;
    }

    /** Converte o texto Base64 salvo no Firestore de volta em bytes (para exibir com o Glide). */
    public static byte[] decodificar(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            return Base64.decode(base64, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static byte[] jpeg(Bitmap bitmap, int qualidade) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, qualidade, out);
        return out.toByteArray();
    }

    private static Bitmap redimensionar(Bitmap original, int ladoMax) {
        int maior = Math.max(original.getWidth(), original.getHeight());
        if (maior <= ladoMax) return original;
        float escala = (float) ladoMax / maior;
        Bitmap novo = Bitmap.createScaledBitmap(original,
                Math.max(1, Math.round(original.getWidth() * escala)),
                Math.max(1, Math.round(original.getHeight() * escala)), true);
        if (novo != original) original.recycle();
        return novo;
    }

    private static Bitmap corrigirRotacao(Context context, Uri uri, Bitmap bitmap) {
        int graus = 0;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in != null) {
                int orientacao = new ExifInterface(in).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (orientacao == ExifInterface.ORIENTATION_ROTATE_90) graus = 90;
                else if (orientacao == ExifInterface.ORIENTATION_ROTATE_180) graus = 180;
                else if (orientacao == ExifInterface.ORIENTATION_ROTATE_270) graus = 270;
            }
        } catch (Exception ignorada) {
            // Sem EXIF (ex.: prints): mantém como está.
        }
        if (graus == 0) return bitmap;

        Matrix m = new Matrix();
        m.postRotate(graus);
        Bitmap girado = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        if (girado != bitmap) bitmap.recycle();
        return girado;
    }
}

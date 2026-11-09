package com.victor.ampara.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Base64;

import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Senha (PIN) para entrar no aplicativo e controle do modo disfarçado.
 *
 * O PIN NUNCA é guardado em texto: armazenamos apenas um hash PBKDF2 com "salt"
 * aleatório, somente neste aparelho (não vai para o Firebase).
 */
public final class BloqueioHelper {

    private static final String PREFS = "ampara_seguranca";
    private static final String CHAVE_HASH = "pin_hash";
    private static final String CHAVE_SALT = "pin_salt";
    private static final String CHAVE_ERROS = "pin_erros";
    private static final String CHAVE_BLOQUEADO_ATE = "pin_bloqueado_ate";

    private static final int ITERACOES = 10000;
    private static final int TENTATIVAS_MAX = 5;
    private static final long ESPERA_MS = 30_000;

    /** Tempo em segundo plano após o qual o PIN é pedido de novo. */
    public static final long TEMPO_BLOQUEIO_MS = 30_000;

    // Ícones na tela inicial (activity-alias do AndroidManifest)
    public static final String ALIAS_NORMAL = "com.victor.ampara.ui.LauncherAmpara";
    public static final String ALIAS_DISFARCE = "com.victor.ampara.ui.LauncherDisfarce";

    // Estado da sessão (memória): zera quando o app é fechado
    private static boolean desbloqueado = false;
    private static long saiuEm = 0;

    private BloqueioHelper() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---------------------------------------------------------------- PIN

    public static boolean pinAtivo(Context c) {
        return prefs(c).getString(CHAVE_HASH, null) != null;
    }

    public static boolean pinValido(String pin) {
        return pin != null && pin.matches("\\d{4,6}");
    }

    public static void definirPin(Context c, String pin) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        prefs(c).edit()
                .putString(CHAVE_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(CHAVE_HASH, gerarHash(pin, salt))
                .putInt(CHAVE_ERROS, 0)
                .putLong(CHAVE_BLOQUEADO_ATE, 0)
                .apply();
        desbloqueado = true;
    }

    public static void removerPin(Context c) {
        prefs(c).edit().remove(CHAVE_HASH).remove(CHAVE_SALT)
                .remove(CHAVE_ERROS).remove(CHAVE_BLOQUEADO_ATE).apply();
    }

    /** Confere o PIN. Após 5 erros seguidos, bloqueia novas tentativas por 30 segundos. */
    public static boolean verificarPin(Context c, String pin) {
        SharedPreferences p = prefs(c);
        String hash = p.getString(CHAVE_HASH, null);
        String saltTxt = p.getString(CHAVE_SALT, null);
        if (hash == null || saltTxt == null || pin == null || segundosDeEspera(c) > 0) return false;

        boolean ok = hash.equals(gerarHash(pin, Base64.decode(saltTxt, Base64.NO_WRAP)));
        if (ok) {
            p.edit().putInt(CHAVE_ERROS, 0).apply();
            desbloqueado = true;
        } else {
            int erros = p.getInt(CHAVE_ERROS, 0) + 1;
            SharedPreferences.Editor e = p.edit().putInt(CHAVE_ERROS, erros);
            if (erros >= TENTATIVAS_MAX) {
                e.putInt(CHAVE_ERROS, 0).putLong(CHAVE_BLOQUEADO_ATE, System.currentTimeMillis() + ESPERA_MS);
            }
            e.apply();
        }
        return ok;
    }

    /** Segundos restantes de espera após muitas tentativas erradas (0 = pode tentar). */
    public static int segundosDeEspera(Context c) {
        long ate = prefs(c).getLong(CHAVE_BLOQUEADO_ATE, 0);
        long resta = ate - System.currentTimeMillis();
        return resta > 0 ? (int) Math.ceil(resta / 1000.0) : 0;
    }

    private static String gerarHash(String pin, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, ITERACOES, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar hash do PIN", e);
        }
    }

    // ---------------------------------------------------------------- Sessão

    public static void marcarDesbloqueado() {
        desbloqueado = true;
    }

    public static void aoIrParaSegundoPlano() {
        saiuEm = SystemClock.elapsedRealtime();
    }

    /** Chamado quando o app volta ao primeiro plano: após 30 s fora, exige o PIN de novo. */
    public static void aoVoltarAoApp() {
        if (desbloqueado && saiuEm > 0 && SystemClock.elapsedRealtime() - saiuEm > TEMPO_BLOQUEIO_MS) {
            desbloqueado = false;
        }
        saiuEm = 0;
    }

    /** O app deve pedir o PIN agora? */
    public static boolean precisaBloquear(Context c) {
        return pinAtivo(c) && !desbloqueado;
    }

    // ---------------------------------------------------------------- Modo disfarçado

    public static boolean disfarceAtivo(Context c) {
        int estado = c.getPackageManager().getComponentEnabledSetting(new ComponentName(c, ALIAS_DISFARCE));
        return estado == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    /** Troca o ícone da tela inicial entre "AMPARA" e "Bloco de Notas". */
    public static void definirDisfarce(Context c, boolean ativo) {
        PackageManager pm = c.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(c, ALIAS_DISFARCE),
                ativo ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(c, ALIAS_NORMAL),
                ativo ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}

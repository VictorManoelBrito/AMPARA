package com.victor.ampara;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.victor.ampara.ui.BloqueioActivity;
import com.victor.ampara.ui.NotasActivity;
import com.victor.ampara.util.BloqueioHelper;

/**
 * Controla o bloqueio por PIN em todo o aplicativo.
 * - Pede o PIN ao abrir o app e quando ele volta após 30 segundos em segundo plano.
 * - A verificação acontece sempre que QUALQUER tela aparece, então nenhuma tela
 *   do AMPARA fica visível sem o PIN.
 * - No modo disfarçado, mostra o "Bloco de Notas" em vez da tela de PIN.
 */
public class AmparaApp extends Application implements Application.ActivityLifecycleCallbacks {

    private int atividadesVisiveis = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        // A versão antiga do modo disfarçado desativava a LoginActivity; garante que ela esteja ativa.
        getPackageManager().setComponentEnabledSetting(
                new android.content.ComponentName(this, com.victor.ampara.ui.LoginActivity.class),
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                android.content.pm.PackageManager.DONT_KILL_APP);
    }

    private boolean ehTelaDeBloqueio(Activity a) {
        return a instanceof BloqueioActivity || a instanceof NotasActivity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (atividadesVisiveis == 0) BloqueioHelper.aoVoltarAoApp();
        atividadesVisiveis++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (ehTelaDeBloqueio(activity) || !BloqueioHelper.precisaBloquear(this)) return;

        // Alguma tela do AMPARA apareceu sem o PIN: traz a tela de bloqueio para frente
        // (reaproveita a que já existir, ex.: quando o login abre a tela principal por cima).
        Class<?> destino = BloqueioHelper.disfarceAtivo(this) ? NotasActivity.class : BloqueioActivity.class;
        Intent intent = new Intent(activity, destino);
        intent.putExtra(BloqueioActivity.EXTRA_SOBREPOR, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        atividadesVisiveis = Math.max(0, atividadesVisiveis - 1);
        if (atividadesVisiveis == 0) BloqueioHelper.aoIrParaSegundoPlano();
    }

    @Override public void onActivityCreated(@NonNull Activity a, @Nullable Bundle b) { }
    @Override public void onActivityPaused(@NonNull Activity a) { }
    @Override public void onActivitySaveInstanceState(@NonNull Activity a, @NonNull Bundle b) { }
    @Override public void onActivityDestroyed(@NonNull Activity a) { }
}

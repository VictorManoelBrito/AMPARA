package com.victor.ampara.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.victor.ampara.databinding.ActivityNotasBinding;
import com.victor.ampara.util.BloqueioHelper;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modo disfarçado: abre como um "Bloco de Notas" funcional.
 * O AMPARA só é aberto quando a pessoa digita o PIN na nota e toca em "Salvar".
 * Qualquer outro texto é salvo como uma nota comum, sem revelar nada.
 */
public class NotasActivity extends AppCompatActivity {

    private static final String PREFS = "bloco_notas";
    private static final String CHAVE = "notas";

    private ActivityNotasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disfarce ativado sem PIN (versões antigas): mantém o comportamento anterior.
        if (!BloqueioHelper.pinAtivo(this)) {
            abrirAmpara();
            return;
        }

        binding = ActivityNotasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSalvarNota.setOnClickListener(v -> salvar());
        mostrarNotas();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });
    }

    private void salvar() {
        String texto = binding.editNota.getText().toString().trim();
        if (texto.isEmpty()) return;

        if (BloqueioHelper.pinValido(texto) && BloqueioHelper.verificarPin(this, texto)) {
            binding.editNota.setText("");
            abrirAmpara();
            return;
        }

        // Nota comum
        JSONArray notas = lerNotas();
        String data = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(new Date());
        JSONArray novas = new JSONArray();
        novas.put(data + "\n" + texto);
        for (int i = 0; i < notas.length(); i++) novas.put(notas.optString(i));
        prefs().edit().putString(CHAVE, novas.toString()).apply();

        binding.editNota.setText("");
        Toast.makeText(this, "Nota salva", Toast.LENGTH_SHORT).show();
        mostrarNotas();
    }

    private void abrirAmpara() {
        BloqueioHelper.marcarDesbloqueado();
        if (getIntent().getBooleanExtra(BloqueioActivity.EXTRA_SOBREPOR, false)) {
            // Estava sobre uma tela do AMPARA (app voltou do segundo plano): só fecha.
            finish();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        overridePendingTransition(0, 0);
    }

    private void mostrarNotas() {
        binding.listaNotas.removeAllViews();
        JSONArray notas = lerNotas();
        binding.tvTituloNotas.setText(notas.length() == 0 ? "Nenhuma nota ainda" : "Minhas notas");

        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        for (int i = 0; i < notas.length(); i++) {
            final int indice = i;
            TextView tv = new TextView(this);
            tv.setText(notas.optString(i));
            tv.setTextSize(15);
            tv.setTextColor(0xFF212121);
            tv.setBackgroundColor(0xFFFFFFFF);
            tv.setPadding(pad, pad, pad, pad);
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = pad / 2;
            tv.setLayoutParams(lp);
            tv.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setMessage("Excluir esta nota?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Excluir", (d, w) -> excluir(indice))
                        .show();
                return true;
            });
            binding.listaNotas.addView(tv);
        }
    }

    private void excluir(int indice) {
        JSONArray notas = lerNotas();
        JSONArray restantes = new JSONArray();
        for (int i = 0; i < notas.length(); i++) if (i != indice) restantes.put(notas.optString(i));
        prefs().edit().putString(CHAVE, restantes.toString()).apply();
        mostrarNotas();
    }

    private JSONArray lerNotas() {
        try {
            return new JSONArray(prefs().getString(CHAVE, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }
}

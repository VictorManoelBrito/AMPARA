package com.victor.ampara.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.victor.ampara.R;
import com.victor.ampara.databinding.ItemTelaTopicoBinding;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Monta cada tela do conteúdo educativo a partir dos blocos do JSON:
 * p (parágrafo), lista, caixa (destaque azul), alerta (destaque laranja),
 * fonte (texto pequeno) e contatos (telefones e serviços).
 */
public class TelaTopicoAdapter extends RecyclerView.Adapter<TelaTopicoAdapter.ViewHolder> {

    public interface OnBaixarPdf {
        void baixar();
    }

    private static final int AZUL = Color.parseColor("#1565C0");
    private static final int AZUL_CLARO = Color.parseColor("#42A5F5");
    private static final int FUNDO_AZUL = Color.parseColor("#E3F2FD");
    private static final int LARANJA = Color.parseColor("#E65100");
    private static final int FUNDO_LARANJA = Color.parseColor("#FFF3E0");
    private static final int TEXTO = Color.parseColor("#263238");
    private static final int CINZA = Color.parseColor("#607D8B");

    private final JSONObject conteudo;
    private final JSONArray telas;
    private final OnBaixarPdf onBaixarPdf;

    public TelaTopicoAdapter(JSONObject conteudo, OnBaixarPdf onBaixarPdf) {
        this.conteudo = conteudo;
        this.telas = conteudo.optJSONArray("telas");
        this.onBaixarPdf = onBaixarPdf;
    }

    @Override
    public int getItemCount() {
        return telas == null ? 0 : telas.length();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTelaTopicoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int posicao) {
        LinearLayout c = holder.b.containerBlocos;
        c.removeAllViews();
        Context ctx = c.getContext();
        JSONObject tela = telas.optJSONObject(posicao);
        if (tela == null) return;

        if (posicao == 0) {
            TextView sub = texto(ctx, conteudo.optString("subtitulo"), 14, CINZA, false);
            margemBaixo(sub, 12);
            c.addView(sub);
        }
        c.addView(cabecalho(ctx, tela.optString("titulo"), tela.optInt("numero", 0)));

        JSONArray blocos = tela.optJSONArray("blocos");
        for (int i = 0; blocos != null && i < blocos.length(); i++) {
            View v = bloco(ctx, blocos.optJSONObject(i));
            if (v != null) c.addView(v);
        }

        if (posicao == getItemCount() - 1) adicionarFinal(ctx, c);
    }

    // ---------- Blocos ----------

    private View bloco(Context ctx, JSONObject b) {
        if (b == null) return null;
        switch (b.optString("t")) {
            case "p": {
                TextView t = texto(ctx, b.optString("texto"), 15, TEXTO, false);
                margemBaixo(t, 10);
                return t;
            }
            case "lista":
                return lista(ctx, b.optJSONArray("itens"));
            case "caixa":
                return caixa(ctx, b.optString("titulo"), b.optString("texto"), AZUL, AZUL_CLARO, FUNDO_AZUL);
            case "alerta":
                return caixa(ctx, b.optString("titulo"), b.optString("texto"), LARANJA, LARANJA, FUNDO_LARANJA);
            case "fonte": {
                TextView t = texto(ctx, b.optString("texto"), 12, CINZA, false);
                margemBaixo(t, 10);
                return t;
            }
            case "contatos":
                return contatos(ctx, b.optJSONArray("itens"));
            default:
                return null;
        }
    }

    private View cabecalho(Context ctx, String titulo, int numero) {
        LinearLayout linha = new LinearLayout(ctx);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        margemBaixo(linha, 14);
        if (numero > 0) {
            TextView badge = texto(ctx, String.valueOf(numero), 18, Color.WHITE, true);
            badge.setGravity(Gravity.CENTER);
            GradientDrawable fundo = new GradientDrawable();
            fundo.setColor(AZUL_CLARO);
            fundo.setCornerRadius(dp(ctx, 8));
            badge.setBackground(fundo);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 36));
            lp.setMarginEnd(dp(ctx, 12));
            linha.addView(badge, lp);
        }
        TextView t = texto(ctx, titulo, 21, AZUL, true);
        linha.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return linha;
    }

    private View lista(Context ctx, JSONArray itens) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        margemBaixo(box, 10);
        for (int i = 0; itens != null && i < itens.length(); i++) {
            LinearLayout linha = new LinearLayout(ctx);
            linha.setOrientation(LinearLayout.HORIZONTAL);
            TextView marcador = texto(ctx, "•", 18, AZUL_CLARO, true);
            LinearLayout.LayoutParams lpM = new LinearLayout.LayoutParams(dp(ctx, 18), ViewGroup.LayoutParams.WRAP_CONTENT);
            linha.addView(marcador, lpM);
            TextView t = texto(ctx, itens.optString(i), 15, TEXTO, false);
            linha.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            margemBaixo(linha, 6);
            box.addView(linha);
        }
        return box;
    }

    private View caixa(Context ctx, String titulo, String corpo, int corTitulo, int corBorda, int corFundo) {
        LinearLayout externa = new LinearLayout(ctx);
        externa.setOrientation(LinearLayout.HORIZONTAL);
        externa.setBackgroundColor(corFundo);
        View borda = new View(ctx);
        borda.setBackgroundColor(corBorda);
        externa.addView(borda, new LinearLayout.LayoutParams(dp(ctx, 4), ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout interna = new LinearLayout(ctx);
        interna.setOrientation(LinearLayout.VERTICAL);
        interna.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
        TextView t = texto(ctx, titulo, 15, corTitulo, true);
        margemBaixo(t, 4);
        interna.addView(t);
        interna.addView(texto(ctx, corpo, 14, TEXTO, false));
        externa.addView(interna, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(ctx, 4), 0, dp(ctx, 14));
        externa.setLayoutParams(lp);
        return externa;
    }

    private View contatos(Context ctx, JSONArray itens) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        margemBaixo(box, 12);
        for (int i = 0; itens != null && i < itens.length(); i++) {
            JSONArray par = itens.optJSONArray(i);
            if (par == null) continue;
            LinearLayout linha = new LinearLayout(ctx);
            linha.setOrientation(LinearLayout.HORIZONTAL);
            linha.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
            TextView rotulo = texto(ctx, par.optString(0), 16, AZUL, true);
            linha.addView(rotulo, new LinearLayout.LayoutParams(dp(ctx, 92), ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView desc = texto(ctx, par.optString(1), 14, TEXTO, false);
            linha.addView(desc, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            box.addView(linha);
            if (i < itens.length() - 1) {
                View divisor = new View(ctx);
                divisor.setBackgroundColor(FUNDO_AZUL);
                box.addView(divisor, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 1)));
            }
        }
        return box;
    }

    /** Última tela: referências + botão para salvar o PDF completo. */
    private void adicionarFinal(Context ctx, LinearLayout c) {
        JSONArray refs = conteudo.optJSONArray("referencias");
        if (refs != null && refs.length() > 0) {
            TextView titulo = texto(ctx, "Referências", 17, AZUL, true);
            titulo.setPadding(0, dp(ctx, 16), 0, dp(ctx, 6));
            c.addView(titulo);
            for (int i = 0; i < refs.length(); i++) {
                TextView r = texto(ctx, refs.optString(i), 12, CINZA, false);
                margemBaixo(r, 6);
                c.addView(r);
            }
        }

        MaterialButton botao = new MaterialButton(ctx);
        botao.setText("Baixar conteúdo completo (PDF)");
        botao.setAllCaps(false);
        botao.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.primary_ampara));
        botao.setIcon(ContextCompat.getDrawable(ctx, android.R.drawable.stat_sys_download));
        botao.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        botao.setOnClickListener(v -> onBaixarPdf.baixar());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(ctx, 20);
        c.addView(botao, lp);
    }

    // ---------- Utilitários ----------

    private TextView texto(Context ctx, String html, float sp, int cor, boolean negrito) {
        TextView t = new TextView(ctx);
        Spanned s = Html.fromHtml(html == null ? "" : html, Html.FROM_HTML_MODE_COMPACT);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(cor);
        t.setLineSpacing(dp(ctx, 3), 1f);
        if (negrito) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }

    private void margemBaixo(View v, int dp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(v.getContext(), dp);
        v.setLayoutParams(lp);
    }

    private static int dp(Context ctx, int valor) {
        return Math.round(valor * ctx.getResources().getDisplayMetrics().density);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTelaTopicoBinding b;

        ViewHolder(ItemTelaTopicoBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

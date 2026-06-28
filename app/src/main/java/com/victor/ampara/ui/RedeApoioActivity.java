package com.victor.ampara.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.victor.ampara.databinding.ActivityRedeApoioBinding;

public class RedeApoioActivity extends AppCompatActivity {

    private ActivityRedeApoioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRedeApoioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarToolbar();
        configurarCliques();
    }

    private void configurarToolbar() {
        binding.toolbarRedeApoio.setNavigationOnClickListener(v -> finish());
    }

    private void configurarCliques() {
        binding.btnCallPolicia.setOnClickListener(v -> discar("190"));
        binding.btnCall180.setOnClickListener(v -> discar("180"));

        binding.btnMapDelegacia.setOnClickListener(v -> abrirMapa("Delegacia da Mulher"));
        binding.btnMapCras.setOnClickListener(v -> abrirMapa("CRAS"));
    }

    private void discar(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    private void abrirMapa(String local) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + local);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback para qualquer app de mapa se o Google Maps não estiver instalado
            startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
        }
    }
}

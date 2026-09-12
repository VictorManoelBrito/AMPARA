package com.victor.ampara.ui;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.victor.ampara.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private static final String MAIN_ACTIVITY = "com.victor.ampara.ui.LoginActivity";
    private static final String ALIAS_ACTIVITY = "com.victor.ampara.ui.LoginActivityAlias";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarSettings.setNavigationOnClickListener(v -> finish());

        checkCurrentState();

        binding.switchDisfarce.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleDisfarce(isChecked);
        });
    }

    private void checkCurrentState() {
        PackageManager pm = getPackageManager();
        int state = pm.getComponentEnabledSetting(new ComponentName(this, ALIAS_ACTIVITY));
        binding.switchDisfarce.setChecked(state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    private void toggleDisfarce(boolean enabled) {
        PackageManager pm = getPackageManager();
        
        // Ativar/Desativar o Original
        pm.setComponentEnabledSetting(
                new ComponentName(this, MAIN_ACTIVITY),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        // Ativar/Desativar o Disfarce
        pm.setComponentEnabledSetting(
                new ComponentName(this, ALIAS_ACTIVITY),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        Toast.makeText(this, "O ícone mudará em instantes na tela inicial.", Toast.LENGTH_LONG).show();
    }
}

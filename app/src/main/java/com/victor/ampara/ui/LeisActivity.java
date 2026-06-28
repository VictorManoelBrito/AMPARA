package com.victor.ampara.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.victor.ampara.databinding.ActivityLeisBinding;

public class LeisActivity extends AppCompatActivity {

    private ActivityLeisBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarLeis.setNavigationOnClickListener(v -> finish());
    }
}

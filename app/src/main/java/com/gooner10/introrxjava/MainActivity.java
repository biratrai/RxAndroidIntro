package com.gooner10.introrxjava;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gooner10.introrxjava.databinding.ActivityMainBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity  {
    private MainActivityViewModel activityViewModel;
    private ActivityMainBinding binding;

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        activityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        Button fetchButton = findViewById(R.id.fetch_gist);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityViewModel.getGistData().observe(MainActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String gist) {
                        showData(gist);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unsubscribing the subscription since activity got destroyed and may get memory leak
        activityViewModel.unsubscribe();
    }

    public void showData(String gistData) {
        binding.gistText.setText(gistData);
    }
}

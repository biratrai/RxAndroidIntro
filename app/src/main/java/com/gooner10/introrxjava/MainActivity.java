package com.gooner10.introrxjava;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gooner10.introrxjava.databinding.ActivityMainBinding;

import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {
    private MainActivityContract.Presenter presenter;
    private ActivityMainBinding binding;

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        presenter = new MainActivityPresenter(this);
        presenter.subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unsubscribing the subscription since activity got destroyed and may get memory leak
        presenter.unsubscribe();
    }

    @Override
    public void showData(String gistData) {
        binding.setGist(gistData);
    }
}

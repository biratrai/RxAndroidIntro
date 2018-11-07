package com.gooner10.introrxjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {
    private MainActivityContract.Presenter presenter;

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        TextView text = findViewById(R.id.gist_text);
        text.setText(gistData);
    }
}

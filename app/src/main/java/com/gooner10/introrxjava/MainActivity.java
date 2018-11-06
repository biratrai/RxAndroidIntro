package com.gooner10.introrxjava;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private Subscription subscription;

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subscription = getGistObservable()
                .subscribeOn(Schedulers.io()) // Gets the work off the mainUi thread
                .observeOn(AndroidSchedulers.mainThread()) // Delivers the result on mainUiThread
                .subscribe(new Subscriber<Gist>() {
                    @DebugLog
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    @DebugLog
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage(), e);
                    }

                    @Override
                    @DebugLog
                    public void onNext(Gist gist) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
                            stringBuilder.append(entry.getKey());
                            stringBuilder.append(" - ");
                            stringBuilder.append("Length of file ");
                            stringBuilder.append(entry.getValue().content.length());
                            stringBuilder.append("\n");
                        }

                        TextView text = findViewById(R.id.gist_text);
                        text.setText(stringBuilder.toString());
                    }
                });

    }

    @Nullable
    @DebugLog
    private Gist getGist() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.github.com/gists/db72a05cc03ef523ee74")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().charStream(), Gist.class);
        }
        return null;

    }

    // Creating a method that returns Gist Observable
    @DebugLog
    public Observable<Gist> getGistObservable() {
        return Observable.defer(new Func0<Observable<Gist>>() {
            @Override
            @DebugLog
            public Observable<Gist> call() {
                try {
                    return Observable.just(getGist());
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
                    return null;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();  // Release the subscription method for the Garbage collection
        }
    }
}

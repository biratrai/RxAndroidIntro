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

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subscription = getGistObservable()
                .subscribeOn(Schedulers.io()) // Gets the work off the mainUi thread
                .observeOn(AndroidSchedulers.mainThread()) // Delivers the result on mainUiThread
                .subscribe(new Subscriber<Gist>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }

                    @Override
                    public void onNext(Gist gist) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
                            stringBuilder.append(entry.getKey());
                            stringBuilder.append(" - ");
                            stringBuilder.append("Length of file ");
                            stringBuilder.append(entry.getValue().content.length());
                            stringBuilder.append("\n");
                        }

                        TextView text = (TextView) findViewById(R.id.gist_text);
                        text.setText(stringBuilder.toString());
                    }
                });

    }

    @Nullable
    private Gist getGist() throws IOException {
        // Throwing some divide by zero exception
        // Which will be handled by the Observable.error
        int z = 1/0;

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.github.com/gists/db72a05cc03ef523ee74")
                .build();


        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            Gist gist = new Gson().fromJson(response.body().charStream(), Gist.class);
            return gist;
        }
        return null;

    }

    // Creating a method that returns Gist Observable
    public Observable<Gist> getGistObservable() {
        return Observable.defer(new Func0<Observable<Gist>>() {
            @Override
            public Observable<Gist> call() {
                try {
                    return Observable.just(getGist());
                } catch (IOException e) {
                    e.printStackTrace();
                    return Observable.error(e);  // Returning Observable.error to be handled
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

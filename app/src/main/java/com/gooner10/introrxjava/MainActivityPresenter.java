package com.gooner10.introrxjava;

import android.support.annotation.Nullable;
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
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivityPresenter implements MainActivityContract.Presenter {
    public static final String TAG = MainActivityPresenter.class.getSimpleName();
    private final MainActivityContract.View view;
    private Subscription subscription;

    public MainActivityPresenter(MainActivityContract.View view) {
        this.view = view;
    }

    @Override
    public void subscribe() {
        subscription = getGistObservable()
                .subscribeOn(Schedulers.io()) // Gets the work off the mainUi thread
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<Gist>() {
                    @Override
                    public void call(Gist gist) {
                        saveToDb();
                    }
                })
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
                        view.showData(stringBuilder.toString());
                    }
                });
    }

    private void saveToDb() {
        Log.i(TAG, "saveToDb: ");
    }

    @Override
    public void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();  // Release the subscription method for the Garbage collection
        }
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
        // Returns an Observable that calls an Observable factory to create an Observable for each
        // new Observer that subscribes
        return Observable.defer(new Func0<Observable<Gist>>() {
            // Func0 is a function with zero arguments
            @Override
            @DebugLog
            public Observable<Gist> call() {
                try {
                    // Returns an Observable that emits a single item and then completes.
                    return Observable.just(getGist());
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
                    return null;
                }
            }
        });
    }
}

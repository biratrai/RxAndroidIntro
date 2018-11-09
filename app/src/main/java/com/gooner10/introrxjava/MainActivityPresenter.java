package com.gooner10.introrxjava;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import hugo.weaving.DebugLog;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter implements MainActivityContract.Presenter {
    public static final String TAG = MainActivityPresenter.class.getSimpleName();
    private final MainActivityContract.View view;
    private Subscription subscription;

    public MainActivityPresenter(MainActivityContract.View view) {
        this.view = view;
    }

    @Override
    public void subscribe() {
        getGistObservable()
                .subscribeOn(Schedulers.io()) // Gets the work off the mainUi thread
//                .observeOn(Schedulers.io())
//                .doOnNext(new Consumer<Gist>() {
//                    @Override
//                    public void accept(Gist gist) {
//                        saveToDb();
//                    }
//                })
                .observeOn(AndroidSchedulers.mainThread()) // Delivers the result on mainUiThread
                .subscribe(new SingleObserver<Gist>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onSuccess(Gist gist) {
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

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, t.getMessage(), t);
                    }
                });
    }

    private void saveToDb() {
        Log.i(TAG, "saveToDb: ");
    }

    @Override
    public void unsubscribe() {
//        if (subscription != null && !subscription.isUnsubscribed()) {
//            subscription.unsubscribe();  // Release the subscription method for the Garbage collection
//        }
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
    public Single<Gist> getGistObservable() {
        // Returns an Observable that calls an Observable factory to create an Observable for each
        // new Observer that subscribes
        return Single.defer(new Callable<SingleSource<? extends Gist>>() {
            // Func0 is a function with zero arguments
            @Override
            @DebugLog
            public Single<Gist> call() {
                try {
                    // Returns an Observable that emits a single item and then completes.
                    return Single.just(getGist());
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
                    return null;
                }
            }
        });
    }
}

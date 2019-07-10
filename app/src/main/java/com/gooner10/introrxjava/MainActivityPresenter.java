package com.gooner10.introrxjava;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter implements MainActivityContract.Presenter {
    public static final String TAG = MainActivityPresenter.class.getSimpleName();
    private final MainActivityContract.View view;
    private Disposable disposable;

    public MainActivityPresenter(MainActivityContract.View view) {
        this.view = view;
    }

    @Override
    public void subscribe() {
        disposable = getGistObservable()
                .subscribeOn(Schedulers.io()) // Gets the work off the mainUi thread
                .observeOn(AndroidSchedulers.mainThread()) // Delivers the result on mainUiThread
                .subscribeWith(new DisposableObserver<Gist>() {
                    @Override
                    @DebugLog
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage(), e);
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete: ");
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

    @Override
    public void unsubscribe() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();  // Release the disposable method for the Garbage collection
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
        return Observable.create(new ObservableOnSubscribe<Gist>() {
            @Override
            public void subscribe(ObservableEmitter<Gist> emitter) {
                try {
                    // Returns an Observable that emits a single item and then completes.
                    emitter.onNext(getGist());
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
                    emitter.onError(e);
                }
            }
        });
    }
}

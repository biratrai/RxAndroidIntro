package com.gooner10.introrxjava;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivityViewModel extends ViewModel {
    public static final String TAG = MainActivityViewModel.class.getSimpleName();
    private Disposable disposable;
    private MutableLiveData<String> gistData;

    public MainActivityViewModel() {
    }

    public LiveData<String> getGistData() {
        if (gistData == null) {
            gistData = new MutableLiveData<>();
            subscribe();
        }
        return gistData;
    }

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
                        gistData.setValue(stringBuilder.toString());
                    }
                });
    }

    public void unsubscribe() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();  // Release the disposable method for the Garbage collection
        }
    }

    @Nullable
    @DebugLog
    private Gist fetchGist() throws IOException {
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
                    emitter.onNext(fetchGist());
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
                    emitter.onError(e);
                }
            }
        });
    }
}

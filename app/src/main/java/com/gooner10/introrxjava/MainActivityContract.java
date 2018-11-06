package com.gooner10.introrxjava;

public interface MainActivityContract {
    interface View {
        void showData(String gistData);
    }

    interface Presenter {
        void subscribe();

        void unsubscribe();
    }
}

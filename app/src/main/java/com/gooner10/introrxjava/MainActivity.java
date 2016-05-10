package com.gooner10.introrxjava;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load a AsyncTask to make a HTTP Call with OkHttp client
        new AsyncTask<Void, Void, Gist>() {

            @Override
            protected Gist doInBackground(Void... params) {
                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://api.github.com/gists/db72a05cc03ef523ee74")
                        .build();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Gist gist = new Gson().fromJson(response.body().charStream(), Gist.class);
                        return gist;
                    }
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Gist gist) {
                super.onPostExecute(gist);

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
        }.execute();
    }
}

package com.example.drishteeeko;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private final OkHttpClient client;

    public NetworkUtils() {
        client = new OkHttpClient();
    }

    public interface ResponseCallback {
        void onSuccess(String response);
        void onFailure(String errorMessage);
        void onError(int errorCode, String errorMessage);
    }

    public void captureFingerprint(String captureUrl, String xmlData, ResponseCallback callback) {
        MediaType mediaType = MediaType.parse("application/xml; charset=utf-8"); // Ensure the charset is defined
        RequestBody body = RequestBody.create(mediaType, xmlData);

        Request request = new Request.Builder()
                .url(captureUrl)
                .post(body) // Use POST method to send XML data
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Capture failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.i(TAG, "Capture successful: " + responseData);
                    callback.onSuccess(responseData);
                } else {
                    Log.e(TAG, "Capture failed with HTTP code: " + response.code());
                    callback.onError(response.code(), response.message());
                }
            }
        });
    }
}

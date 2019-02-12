package io.firekast.appjava;

import android.app.Application;
import android.support.annotation.Nullable;

import io.firekast.Firekast;

public class App extends Application {

    @Nullable
    public static String latestStreamId = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Firekast.initialize(this, "YOUR_CLIENT_KEY", "YOUR_APPLICATION_ID");
    }
}

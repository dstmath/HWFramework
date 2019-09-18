package android.test.mock;

import android.app.Application;
import android.content.res.Configuration;

@Deprecated
public class MockApplication extends Application {
    public void onCreate() {
        throw new UnsupportedOperationException();
    }

    public void onTerminate() {
        throw new UnsupportedOperationException();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        throw new UnsupportedOperationException();
    }
}

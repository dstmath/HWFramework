package android.test.mock;

import android.app.Application;
import android.content.res.Configuration;

@Deprecated
public class MockApplication extends Application {
    @Override // android.app.Application
    public void onCreate() {
        throw new UnsupportedOperationException();
    }

    @Override // android.app.Application
    public void onTerminate() {
        throw new UnsupportedOperationException();
    }

    @Override // android.app.Application, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        throw new UnsupportedOperationException();
    }
}

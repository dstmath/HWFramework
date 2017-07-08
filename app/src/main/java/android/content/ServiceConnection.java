package android.content;

import android.os.IBinder;

public interface ServiceConnection {
    void onServiceConnected(ComponentName componentName, IBinder iBinder);

    void onServiceDisconnected(ComponentName componentName);
}

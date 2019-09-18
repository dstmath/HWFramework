package huawei.com.android.server.fingerprint;

import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class SingleModeContentObserver extends ContentObserver {
    private static final String TAG = "SingleModeContentObserver";
    private ICallBack mCallback;

    public interface ICallBack {
        void onContentChange();
    }

    public SingleModeContentObserver(Handler handler, ICallBack callback) {
        super(handler);
        this.mCallback = callback;
    }

    public void onChange(boolean selfChange) {
        Log.e(TAG, "SingleModeContentObserver change");
        if (this.mCallback != null) {
            this.mCallback.onContentChange();
        }
    }
}

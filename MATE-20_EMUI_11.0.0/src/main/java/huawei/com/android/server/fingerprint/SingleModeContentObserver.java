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

    @Override // android.database.ContentObserver
    public void onChange(boolean isSelfChange) {
        Log.i(TAG, "SingleModeContentObserver change");
        ICallBack iCallBack = this.mCallback;
        if (iCallBack != null) {
            iCallBack.onContentChange();
        }
    }
}

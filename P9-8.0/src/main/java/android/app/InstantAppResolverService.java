package android.app;

import android.app.IInstantAppResolver.Stub;
import android.content.Context;
import android.content.Intent;
import android.content.pm.InstantAppResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.SomeArgs;
import java.util.Arrays;
import java.util.List;

public abstract class InstantAppResolverService extends Service {
    private static final boolean DEBUG_EPHEMERAL = Build.IS_DEBUGGABLE;
    public static final String EXTRA_RESOLVE_INFO = "android.app.extra.RESOLVE_INFO";
    public static final String EXTRA_SEQUENCE = "android.app.extra.SEQUENCE";
    private static final String TAG = "PackageManager";
    Handler mHandler;

    public static final class InstantAppResolutionCallback {
        private final IRemoteCallback mCallback;
        private final int mSequence;

        InstantAppResolutionCallback(int sequence, IRemoteCallback callback) {
            this.mCallback = callback;
            this.mSequence = sequence;
        }

        public void onInstantAppResolveInfo(List<InstantAppResolveInfo> resolveInfo) {
            Bundle data = new Bundle();
            data.putParcelableList(InstantAppResolverService.EXTRA_RESOLVE_INFO, resolveInfo);
            data.putInt(InstantAppResolverService.EXTRA_SEQUENCE, this.mSequence);
            try {
                this.mCallback.sendResult(data);
            } catch (RemoteException e) {
                Log.e(InstantAppResolverService.TAG, "onInstantAppResolveInfo()");
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public static final int MSG_GET_INSTANT_APP_INTENT_FILTER = 2;
        public static final int MSG_GET_INSTANT_APP_RESOLVE_INFO = 1;

        public ServiceHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message message) {
            int action = message.what;
            SomeArgs args;
            switch (action) {
                case 1:
                    args = message.obj;
                    IRemoteCallback callback = args.arg1;
                    InstantAppResolverService.this._onGetInstantAppResolveInfo(args.arg2, args.arg3, new InstantAppResolutionCallback(message.arg1, callback));
                    return;
                case 2:
                    args = (SomeArgs) message.obj;
                    int[] digestPrefix = (int[]) args.arg2;
                    String token = (String) args.arg3;
                    String hostName = args.arg4;
                    InstantAppResolverService.this._onGetInstantAppIntentFilter(digestPrefix, token, hostName, new InstantAppResolutionCallback(-1, (IRemoteCallback) args.arg1));
                    return;
                default:
                    throw new IllegalArgumentException("Unknown message: " + action);
            }
        }
    }

    public void onGetInstantAppResolveInfo(int[] digestPrefix, String token, InstantAppResolutionCallback callback) {
        throw new IllegalStateException("Must define");
    }

    public void onGetInstantAppIntentFilter(int[] digestPrefix, String token, InstantAppResolutionCallback callback) {
        throw new IllegalStateException("Must define");
    }

    Looper getLooper() {
        return getBaseContext().getMainLooper();
    }

    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new ServiceHandler(getLooper());
    }

    public final IBinder onBind(Intent intent) {
        return new Stub() {
            public void getInstantAppResolveInfoList(int[] digestPrefix, String token, int sequence, IRemoteCallback callback) {
                if (InstantAppResolverService.DEBUG_EPHEMERAL) {
                    Slog.v(InstantAppResolverService.TAG, "[" + token + "] Phase1 called; posting");
                }
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callback;
                args.arg2 = digestPrefix;
                args.arg3 = token;
                InstantAppResolverService.this.mHandler.obtainMessage(1, sequence, 0, args).sendToTarget();
            }

            public void getInstantAppIntentFilterList(int[] digestPrefix, String token, String hostName, IRemoteCallback callback) {
                if (InstantAppResolverService.DEBUG_EPHEMERAL) {
                    Slog.v(InstantAppResolverService.TAG, "[" + token + "] Phase2 called; posting");
                }
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callback;
                args.arg2 = digestPrefix;
                args.arg3 = token;
                args.arg4 = hostName;
                InstantAppResolverService.this.mHandler.obtainMessage(2, callback).sendToTarget();
            }
        };
    }

    @Deprecated
    void _onGetInstantAppResolveInfo(int[] digestPrefix, String token, InstantAppResolutionCallback callback) {
        if (DEBUG_EPHEMERAL) {
            Slog.d(TAG, "[" + token + "] Phase1 request;" + " prefix: " + Arrays.toString(digestPrefix));
        }
        onGetInstantAppResolveInfo(digestPrefix, token, callback);
    }

    @Deprecated
    void _onGetInstantAppIntentFilter(int[] digestPrefix, String token, String hostName, InstantAppResolutionCallback callback) {
        if (DEBUG_EPHEMERAL) {
            Slog.d(TAG, "[" + token + "] Phase2 request;" + " prefix: " + Arrays.toString(digestPrefix));
        }
        onGetInstantAppIntentFilter(digestPrefix, token, callback);
    }
}

package android.app;

import android.annotation.SystemApi;
import android.app.IInstantAppResolver;
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
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.SomeArgs;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SystemApi
public abstract class InstantAppResolverService extends Service {
    private static final boolean DEBUG_INSTANT = Build.IS_DEBUGGABLE;
    public static final String EXTRA_RESOLVE_INFO = "android.app.extra.RESOLVE_INFO";
    public static final String EXTRA_SEQUENCE = "android.app.extra.SEQUENCE";
    private static final String TAG = "PackageManager";
    Handler mHandler;

    @Deprecated
    public void onGetInstantAppResolveInfo(int[] digestPrefix, String token, InstantAppResolutionCallback callback) {
        throw new IllegalStateException("Must define onGetInstantAppResolveInfo");
    }

    @Deprecated
    public void onGetInstantAppIntentFilter(int[] digestPrefix, String token, InstantAppResolutionCallback callback) {
        throw new IllegalStateException("Must define onGetInstantAppIntentFilter");
    }

    @Deprecated
    public void onGetInstantAppResolveInfo(Intent sanitizedIntent, int[] hostDigestPrefix, String token, InstantAppResolutionCallback callback) {
        if (sanitizedIntent.isWebIntent()) {
            onGetInstantAppResolveInfo(hostDigestPrefix, token, callback);
        } else {
            callback.onInstantAppResolveInfo(Collections.emptyList());
        }
    }

    @Deprecated
    public void onGetInstantAppIntentFilter(Intent sanitizedIntent, int[] hostDigestPrefix, String token, InstantAppResolutionCallback callback) {
        Log.e(TAG, "New onGetInstantAppIntentFilter is not overridden");
        if (sanitizedIntent.isWebIntent()) {
            onGetInstantAppIntentFilter(hostDigestPrefix, token, callback);
        } else {
            callback.onInstantAppResolveInfo(Collections.emptyList());
        }
    }

    public void onGetInstantAppResolveInfo(Intent sanitizedIntent, int[] hostDigestPrefix, UserHandle userHandle, String token, InstantAppResolutionCallback callback) {
        onGetInstantAppResolveInfo(sanitizedIntent, hostDigestPrefix, token, callback);
    }

    public void onGetInstantAppIntentFilter(Intent sanitizedIntent, int[] hostDigestPrefix, UserHandle userHandle, String token, InstantAppResolutionCallback callback) {
        onGetInstantAppIntentFilter(sanitizedIntent, hostDigestPrefix, token, callback);
    }

    /* access modifiers changed from: package-private */
    public Looper getLooper() {
        return getBaseContext().getMainLooper();
    }

    @Override // android.content.ContextWrapper
    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new ServiceHandler(getLooper());
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new IInstantAppResolver.Stub() {
            /* class android.app.InstantAppResolverService.AnonymousClass1 */

            @Override // android.app.IInstantAppResolver
            public void getInstantAppResolveInfoList(Intent sanitizedIntent, int[] digestPrefix, int userId, String token, int sequence, IRemoteCallback callback) {
                if (InstantAppResolverService.DEBUG_INSTANT) {
                    Slog.v(InstantAppResolverService.TAG, "[" + token + "] Phase1 called; posting");
                }
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callback;
                args.arg2 = digestPrefix;
                args.arg3 = Integer.valueOf(userId);
                args.arg4 = token;
                args.arg5 = sanitizedIntent;
                InstantAppResolverService.this.mHandler.obtainMessage(1, sequence, 0, args).sendToTarget();
            }

            @Override // android.app.IInstantAppResolver
            public void getInstantAppIntentFilterList(Intent sanitizedIntent, int[] digestPrefix, int userId, String token, IRemoteCallback callback) {
                if (InstantAppResolverService.DEBUG_INSTANT) {
                    Slog.v(InstantAppResolverService.TAG, "[" + token + "] Phase2 called; posting");
                }
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callback;
                args.arg2 = digestPrefix;
                args.arg3 = Integer.valueOf(userId);
                args.arg4 = token;
                args.arg5 = sanitizedIntent;
                InstantAppResolverService.this.mHandler.obtainMessage(2, args).sendToTarget();
            }
        };
    }

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
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public static final int MSG_GET_INSTANT_APP_INTENT_FILTER = 2;
        public static final int MSG_GET_INSTANT_APP_RESOLVE_INFO = 1;

        public ServiceHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int action = message.what;
            if (action == 1) {
                SomeArgs args = (SomeArgs) message.obj;
                IRemoteCallback callback = (IRemoteCallback) args.arg1;
                int[] digestPrefix = (int[]) args.arg2;
                int userId = ((Integer) args.arg3).intValue();
                String token = (String) args.arg4;
                Intent intent = (Intent) args.arg5;
                int sequence = message.arg1;
                if (InstantAppResolverService.DEBUG_INSTANT) {
                    Slog.d(InstantAppResolverService.TAG, "[" + token + "] Phase1 request; prefix: " + Arrays.toString(digestPrefix) + ", userId: " + userId);
                }
                InstantAppResolverService.this.onGetInstantAppResolveInfo(intent, digestPrefix, UserHandle.of(userId), token, new InstantAppResolutionCallback(sequence, callback));
            } else if (action == 2) {
                SomeArgs args2 = (SomeArgs) message.obj;
                IRemoteCallback callback2 = (IRemoteCallback) args2.arg1;
                int[] digestPrefix2 = (int[]) args2.arg2;
                int userId2 = ((Integer) args2.arg3).intValue();
                String token2 = (String) args2.arg4;
                Intent intent2 = (Intent) args2.arg5;
                if (InstantAppResolverService.DEBUG_INSTANT) {
                    Slog.d(InstantAppResolverService.TAG, "[" + token2 + "] Phase2 request; prefix: " + Arrays.toString(digestPrefix2) + ", userId: " + userId2);
                }
                InstantAppResolverService.this.onGetInstantAppIntentFilter(intent2, digestPrefix2, UserHandle.of(userId2), token2, new InstantAppResolutionCallback(-1, callback2));
            } else {
                throw new IllegalArgumentException("Unknown message: " + action);
            }
        }
    }
}

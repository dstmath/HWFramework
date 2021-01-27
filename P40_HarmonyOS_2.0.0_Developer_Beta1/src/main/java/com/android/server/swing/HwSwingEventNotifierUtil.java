package com.android.server.swing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.huawei.systemserver.swing.IHwSwingEventNotifier;

public class HwSwingEventNotifierUtil {
    private static final String ACTION_SYSTEMUI_SWING_SERVICE = "com.android.systemui.swing.HwSwingService";
    private static final String PKG_SYSTEMUI = "com.android.systemui";
    private static final String TAG = "HwSwingEventNotifierUtil";
    private static HwSwingEventNotifierUtil sInstance;
    private Context mContext;
    private IHwSwingEventNotifier mHwSwingEventNotifier;
    private final Object mLock = new Object();
    private IBinder.DeathRecipient mSerBinderDieListener = new IBinder.DeathRecipient() {
        /* class com.android.server.swing.HwSwingEventNotifierUtil.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Slog.i(HwSwingEventNotifierUtil.TAG, "systemui binderDied");
            synchronized (HwSwingEventNotifierUtil.this.mLock) {
                HwSwingEventNotifierUtil.this.mHwSwingEventNotifier = null;
            }
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.android.server.swing.HwSwingEventNotifierUtil.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.i(HwSwingEventNotifierUtil.TAG, "systemui onServiceConnected");
            synchronized (HwSwingEventNotifierUtil.this.mLock) {
                HwSwingEventNotifierUtil.this.mHwSwingEventNotifier = IHwSwingEventNotifier.Stub.asInterface(service);
                if (HwSwingEventNotifierUtil.this.mHwSwingEventNotifier == null) {
                    Slog.e(HwSwingEventNotifierUtil.TAG, "IHwSwingEventNotifier service get error");
                    return;
                }
                try {
                    service.linkToDeath(HwSwingEventNotifierUtil.this.mSerBinderDieListener, 0);
                } catch (RemoteException e) {
                    Slog.e(HwSwingEventNotifierUtil.TAG, "error link to systemui swingservice");
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Slog.i(HwSwingEventNotifierUtil.TAG, "systemui onServiceDisconnected");
            synchronized (HwSwingEventNotifierUtil.this.mLock) {
                HwSwingEventNotifierUtil.this.mHwSwingEventNotifier = null;
            }
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            Slog.i(HwSwingEventNotifierUtil.TAG, "systemui onBindingDied");
            synchronized (HwSwingEventNotifierUtil.this.mLock) {
                HwSwingEventNotifierUtil.this.mHwSwingEventNotifier = null;
            }
        }
    };

    private HwSwingEventNotifierUtil(Context context) {
        this.mContext = context;
    }

    public static synchronized HwSwingEventNotifierUtil getInstance(Context context) {
        HwSwingEventNotifierUtil hwSwingEventNotifierUtil;
        synchronized (HwSwingEventNotifierUtil.class) {
            if (sInstance == null) {
                sInstance = new HwSwingEventNotifierUtil(context);
            }
            hwSwingEventNotifierUtil = sInstance;
        }
        return hwSwingEventNotifierUtil;
    }

    public IHwSwingEventNotifier getHwSwingEventNotifier() {
        IHwSwingEventNotifier iHwSwingEventNotifier;
        synchronized (this.mLock) {
            iHwSwingEventNotifier = this.mHwSwingEventNotifier;
        }
        return iHwSwingEventNotifier;
    }

    public void bindSystemUiSwingEventService() {
        synchronized (this.mLock) {
            if (this.mHwSwingEventNotifier != null) {
                Slog.w(TAG, "systemui service already bind");
                return;
            }
            Intent intent = new Intent(ACTION_SYSTEMUI_SWING_SERVICE);
            intent.setPackage("com.android.systemui");
            this.mContext.bindService(intent, this.mServiceConnection, 1);
            Slog.i(TAG, "connect to systemui service");
        }
    }

    public void unbindSystemUiSwingEventService() {
        synchronized (this.mLock) {
            if (this.mHwSwingEventNotifier == null) {
                Slog.w(TAG, "systemui service not bind yet");
                return;
            }
            this.mHwSwingEventNotifier = null;
            this.mContext.unbindService(this.mServiceConnection);
            Slog.i(TAG, "disconnect to systemui service");
        }
    }
}

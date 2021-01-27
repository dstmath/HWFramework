package com.android.server.swing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import com.huawei.hiai.awareness.service.AwarenessManager;
import com.huawei.hiai.awareness.service.AwarenessServiceConnection;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HwAwarenessServiceConnector {
    private static final int DEFAULT_LIST_SIZE = 3;
    private static final int MAX_RECONNECT_TIMES = 3;
    private static final String PERMISSION_AWARENESS_SERVICE_CREATE = "com.huawei.hiai.awareness.permission.NOTIFY_RESTART_SERVICE";
    private static final int RECOONECT_WAIT_TIME_MS = 10000;
    private static final String TAG = "HwAwarenessServiceConnector";
    private AwarenessManager mAwarenessManager;
    private AwarenessServiceResetReceiver mAwarenessResetReceiver;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class com.android.server.swing.HwAwarenessServiceConnector.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceConnected() {
            Log.i(HwAwarenessServiceConnector.TAG, "onServiceConnected ");
            HwAwarenessServiceConnector.this.mIsConnected = true;
            HwAwarenessServiceConnector.this.mReconnectTimes = 0;
            HwAwarenessServiceConnector.this.notifyServiceConnected(true);
        }

        @Override // com.huawei.hiai.awareness.service.AwarenessServiceConnection
        public void onServiceDisconnected() {
            Log.e(HwAwarenessServiceConnector.TAG, "onServiceDisconnected oldState=" + HwAwarenessServiceConnector.this.mIsConnected);
            if (HwAwarenessServiceConnector.this.mIsConnected) {
                HwAwarenessServiceConnector.this.mIsConnected = false;
                HwAwarenessServiceConnector.this.notifyServiceConnected(false);
            }
            if (HwAwarenessServiceConnector.this.mHandler == null) {
                HwAwarenessServiceConnector.this.mHandler = new Handler();
            }
            Log.w(HwAwarenessServiceConnector.TAG, "wait 10000 ms to reconnect");
            HwAwarenessServiceConnector.this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.swing.HwAwarenessServiceConnector.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwAwarenessServiceConnector.access$108(HwAwarenessServiceConnector.this);
                    if (!HwAwarenessServiceConnector.this.mIsConnected && HwAwarenessServiceConnector.this.mReconnectTimes < 3 && !HwAwarenessServiceConnector.this.mAwarenessManager.connectService(HwAwarenessServiceConnector.this.mAwarenessServiceConnection)) {
                        Log.w(HwAwarenessServiceConnector.TAG, "onServiceDisconnected() connectService failed!");
                        HwAwarenessServiceConnector.this.mHandler.postDelayed(this, 10000);
                    }
                }
            }, 10000);
        }
    };
    private Context mContext;
    private Handler mHandler;
    private boolean mIsConnected;
    private List<WeakReference<Listener>> mListeners = new ArrayList(3);
    private int mReconnectTimes;

    static /* synthetic */ int access$108(HwAwarenessServiceConnector x0) {
        int i = x0.mReconnectTimes;
        x0.mReconnectTimes = i + 1;
        return i;
    }

    public HwAwarenessServiceConnector(Context context) {
        this.mContext = context;
        this.mAwarenessManager = new AwarenessManager(this.mContext);
    }

    public void connectService() {
        if (!this.mIsConnected) {
            if (this.mAwarenessResetReceiver == null) {
                this.mAwarenessResetReceiver = new AwarenessServiceResetReceiver();
                Context context = this.mContext;
                AwarenessServiceResetReceiver awarenessServiceResetReceiver = this.mAwarenessResetReceiver;
                context.registerReceiver(awarenessServiceResetReceiver, awarenessServiceResetReceiver.getFilter(), "com.huawei.hiai.awareness.permission.NOTIFY_RESTART_SERVICE", null);
            }
            boolean isOk = this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
            Log.i(TAG, "connectService: ret=" + isOk);
        }
    }

    public void addListener(Listener listener) {
        if (listener != null) {
            int tempSize = this.mListeners.size();
            for (int i = 0; i < tempSize; i++) {
                if (this.mListeners.get(i).get() == listener) {
                    return;
                }
            }
            this.mListeners.add(new WeakReference<>(listener));
            listener.setAwarenessManager(this.mAwarenessManager);
            listener.onServiceConnectedStateChanged(this.mIsConnected);
        }
    }

    public void removeListener(Listener listener) {
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            Listener tempListener = this.mListeners.get(i).get();
            if (tempListener == null || tempListener == listener) {
                this.mListeners.remove(i);
            }
        }
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyServiceConnected(boolean isConnected) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            Listener listener = this.mListeners.get(i).get();
            if (listener != null) {
                listener.onServiceConnectedStateChanged(isConnected);
            }
        }
    }

    public class AwarenessServiceResetReceiver extends BroadcastReceiver {
        private static final String ACTION_AWARENESS_SERVICE_CREATE = "com.huawei.hiai.awareness.action.service.create";
        private static final String AWARENESS_SERVICE_CREATE_TYPE = "awareness_service_create_type";
        private static final int DEFAULT_VALUE = -1;
        private static final int SERVICE_EXP_RESTART = 2;
        private static final String TAG = "AwarenessServiceResetReceiver";

        public AwarenessServiceResetReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int notifyType;
            if (intent == null) {
                Log.e(TAG, "onReceive intent is null");
            } else if ("com.huawei.hiai.awareness.action.service.create".equals(intent.getAction()) && (notifyType = intent.getIntExtra("awareness_service_create_type", -1)) == 2) {
                Log.d(TAG, "Awareness engine system restart : notifyType = " + notifyType);
                HwAwarenessServiceConnector.this.mAwarenessManager.connectService(HwAwarenessServiceConnector.this.mAwarenessServiceConnection);
            }
        }

        public IntentFilter getFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.hiai.awareness.action.service.create");
            return filter;
        }
    }

    /* access modifiers changed from: package-private */
    public interface Listener {
        void onServiceConnectedStateChanged(boolean z);

        default void setAwarenessManager(AwarenessManager awarenessManager) {
        }
    }
}

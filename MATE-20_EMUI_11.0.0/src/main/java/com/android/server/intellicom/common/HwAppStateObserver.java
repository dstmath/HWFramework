package com.android.server.intellicom.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseIntArray;
import com.huawei.android.app.ActivityManagerEx2;
import com.huawei.android.app.UidObserverEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.RegistrantEx;
import com.huawei.android.os.RegistrantListEx;
import com.huawei.android.os.UserHandleExt;

public class HwAppStateObserver {
    private static final String DATA_SCHEME_PACKAGE = "package";
    private static final String TAG = "HwAppStateObserver";
    private RegistrantListEx mAppAppearsForegroundRegistrants;
    private RegistrantListEx mAppGoneRegistrants;
    private RegistrantListEx mAppRemovedRegistrants;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private UidObserver mUidObserver;
    private final SparseIntArray mUidState;

    private HwAppStateObserver() {
        this.mAppAppearsForegroundRegistrants = new RegistrantListEx();
        this.mAppRemovedRegistrants = new RegistrantListEx();
        this.mAppGoneRegistrants = new RegistrantListEx();
        this.mUidState = new SparseIntArray();
        this.mUidObserver = new UidObserver();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.intellicom.common.HwAppStateObserver.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    HwAppStateObserver.this.log("intent or intent.getAction is null.");
                    return;
                }
                String action = intent.getAction();
                char c = 65535;
                if (action.hashCode() == 525384130 && action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    c = 0;
                }
                if (c != 0) {
                    HwAppStateObserver hwAppStateObserver = HwAppStateObserver.this;
                    hwAppStateObserver.log("BroadcastReceiver error: " + action);
                    return;
                }
                HwAppStateObserver.this.log("Receive ACTION_PACKAGE_REMOVED");
                HwAppStateObserver.this.mAppRemovedRegistrants.notifyRegistrants(new AsyncResultEx((Object) null, intent.getDataString(), (Throwable) null));
            }
        };
    }

    public void register(Context context) {
        try {
            log("HwAppStateObserver register");
            this.mContext = context;
            registerUidObserver();
            registerBroadcast();
        } catch (RemoteException e) {
            Log.e(TAG, "fail register HwAppStateObserver");
        }
    }

    public static HwAppStateObserver getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void registerForAppAppearsForeground(Handler h, int what, Object obj) {
        this.mAppAppearsForegroundRegistrants.add(new RegistrantEx(h, what, obj));
    }

    public void registerForAppGone(Handler h, int what, Object obj) {
        this.mAppGoneRegistrants.add(new RegistrantEx(h, what, obj));
    }

    public void registerForAppRemoved(Handler h, int what, Object obj) {
        this.mAppRemovedRegistrants.add(new RegistrantEx(h, what, obj));
    }

    public void unregisterForAppAppearsForeground(Handler h) {
        this.mAppAppearsForegroundRegistrants.remove(h);
    }

    public void unregisterForAppGone(Handler h) {
        this.mAppGoneRegistrants.remove(h);
    }

    public void unregisterForAppRemoved(Handler h) {
        this.mAppRemovedRegistrants.remove(h);
    }

    /* access modifiers changed from: private */
    public class UidObserver extends UidObserverEx {
        private UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            HwAppStateObserver.this.handleUidStateChanged(uid, procState);
        }

        public void onUidGone(int uid, boolean disabled) {
            if (!HwAppStateObserver.this.isSystem(uid) && HwAppStateObserver.this.isApplicationUid(uid)) {
                HwAppStateObserver hwAppStateObserver = HwAppStateObserver.this;
                hwAppStateObserver.log("onUidGone, uid = " + uid);
                HwAppStateObserver.this.handleUidGone(uid);
            }
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    }

    private static class SingletonInstance {
        private static final HwAppStateObserver INSTANCE = new HwAppStateObserver();

        private SingletonInstance() {
        }
    }

    private void registerUidObserver() throws RemoteException {
        ActivityManagerEx2.registerUidObserver(this.mUidObserver, 3, -1, (String) null);
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme(DATA_SCHEME_PACKAGE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUidStateChanged(int uid, int procState) {
        if (!isSystem(uid) && isApplicationUid(uid) && this.mUidState.get(uid, 20) != procState) {
            this.mUidState.put(uid, procState);
            if (isForegroundState(procState)) {
                this.mAppAppearsForegroundRegistrants.notifyRegistrants(new AsyncResultEx((Object) null, Integer.valueOf(uid), (Throwable) null));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSystem(int uid) {
        return uid < 10000;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUidGone(int uid) {
        if (!isSystem(uid) && isApplicationUid(uid)) {
            this.mAppGoneRegistrants.notifyRegistrants(new AsyncResultEx((Object) null, Integer.valueOf(uid), (Throwable) null));
        }
    }

    private boolean isForegroundState(int state) {
        return 2 == state;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isApplicationUid(int uid) {
        return UserHandleExt.isApp(uid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        Log.i(TAG, msg);
    }
}

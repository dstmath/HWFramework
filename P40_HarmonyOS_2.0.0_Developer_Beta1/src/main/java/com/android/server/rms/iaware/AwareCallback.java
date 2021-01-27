package com.android.server.rms.iaware;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.app.IUserSwitchObserverEx;
import com.huawei.android.os.IRemoteCallbackEx;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AwareCallback {
    private static final Set<String> ACTIVITY_NOTIFIER_TYPES = new HashSet<String>() {
        /* class com.android.server.rms.iaware.AwareCallback.AnonymousClass1 */

        {
            add(SceneRecogFeature.REASON_INFO);
            add("appSwitch");
        }
    };
    private static final Object LOCK = new Object();
    private static final int NOTIFY_ACTIVITY = 100;
    private static final int NOTIFY_FOREGROUND = 201;
    private static final int NOTIFY_PROC = 202;
    private static final int NOTIFY_USER = 301;
    private static final String TAG = "AwareCb";
    private static AwareCallback sInstance;
    private final Object mActLock = new Object();
    private AwareCbHandler mCbHandler = new AwareCbHandler();
    private ArrayMap<IHwActivityNotifierEx, String> mInActNotifier = new ArrayMap<>();
    private IProcessObserverEx mInProcObserver;
    private IUserSwitchObserverEx mInUserObserver;
    private ArrayMap<IHwActivityNotifierEx, String> mOutActNotifier = new ArrayMap<>();
    private ArraySet<IProcessObserverEx> mOutProcObservers = new ArraySet<>();
    private ArraySet<IUserSwitchObserverEx> mOutUserObservers = new ArraySet<>();
    private final Object mProcLock = new Object();
    private final Object mUserLock = new Object();

    private AwareCallback() {
    }

    public static AwareCallback getInstance() {
        AwareCallback awareCallback;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareCallback();
            }
            awareCallback = sInstance;
        }
        return awareCallback;
    }

    public void registerActivityNotifier(IHwActivityNotifierEx notifier, String reason) {
        synchronized (this.mActLock) {
            if (!this.mOutActNotifier.containsValue(reason)) {
                AwareLog.d(TAG, "registerActivityNotifier=" + reason);
                ActivityNotifierCallBack inNotifier = new ActivityNotifierCallBack();
                this.mInActNotifier.put(inNotifier, reason);
                ActivityManagerEx.registerHwActivityNotifier(inNotifier, reason);
            }
            this.mOutActNotifier.put(notifier, reason);
        }
    }

    public void unregisterActivityNotifier(IHwActivityNotifierEx notifier, String reason) {
        AwareLog.d(TAG, "unregisterActivityNotifier =" + reason);
        synchronized (this.mActLock) {
            this.mOutActNotifier.remove(notifier);
            if (!this.mOutActNotifier.containsValue(reason)) {
                int index = this.mInActNotifier.indexOfValue(reason);
                if (index >= 0) {
                    ActivityManagerEx.unregisterHwActivityNotifier(this.mInActNotifier.keyAt(index));
                    this.mInActNotifier.removeAt(index);
                }
            }
        }
    }

    public void registerProcessObserver(IProcessObserverEx observer) {
        synchronized (this.mProcLock) {
            if (this.mInProcObserver == null) {
                this.mInProcObserver = new ProcessObserver();
                try {
                    AwareLog.d(TAG, "registerProcessObserver");
                    ActivityManagerNativeEx.registerProcessObserver(this.mInProcObserver);
                } catch (RemoteException e) {
                    AwareLog.w(TAG, "register proc observer failed");
                }
            }
            this.mOutProcObservers.add(observer);
        }
    }

    public void unregisterProcessObserver(IProcessObserverEx observer) {
        AwareLog.d(TAG, "unregisterProcessObserver =" + observer);
        synchronized (this.mProcLock) {
            this.mOutProcObservers.remove(observer);
            if (this.mOutProcObservers.isEmpty()) {
                try {
                    ActivityManagerNativeEx.unregisterProcessObserver(this.mInProcObserver);
                    this.mInProcObserver = null;
                } catch (RemoteException e) {
                    AwareLog.w(TAG, "unregister proc observer failed");
                }
            }
        }
    }

    public void registerUserSwitchObserver(IUserSwitchObserverEx observer) {
        synchronized (this.mUserLock) {
            if (this.mInUserObserver == null) {
                this.mInUserObserver = new UserSwitchObserver();
                try {
                    AwareLog.d(TAG, "registerUserSwitchObserver");
                    ActivityManagerNativeEx.registerUserSwitchObserver(this.mInUserObserver, "aware");
                } catch (RemoteException e) {
                    AwareLog.w(TAG, "register userswitch observer failed");
                }
            }
            this.mOutUserObservers.add(observer);
        }
    }

    public void unregisterUserSwitchObserver(IUserSwitchObserverEx observer) {
        AwareLog.d(TAG, "unregisterUserSwitchObserver =" + observer);
        synchronized (this.mUserLock) {
            this.mOutUserObservers.remove(observer);
            if (this.mOutUserObservers.isEmpty()) {
                try {
                    ActivityManagerNativeEx.unregisterUserSwitchObserver(this.mInUserObserver);
                    this.mInUserObserver = null;
                } catch (RemoteException e) {
                    AwareLog.w(TAG, "unregister userswitch observer failed");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ActivityNotifierCallBack extends IHwActivityNotifierEx {
        private ActivityNotifierCallBack() {
        }

        public void call(Bundle extras) {
            if (extras != null) {
                String reason = extras.getString("android.intent.extra.REASON");
                AwareLog.d(AwareCallback.TAG, "ActivityNotifierCallBack =" + reason);
                if (!TextUtils.isEmpty(reason)) {
                    Message msg = Message.obtain(AwareCallback.this.mCbHandler, 100);
                    msg.obj = extras;
                    AwareCallback.this.mCbHandler.sendMessage(msg);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ProcessObserver extends IProcessObserverEx {
        private ProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            synchronized (AwareCallback.this.mProcLock) {
                if (AwareCallback.this.mInProcObserver != null) {
                    AwareLog.d(AwareCallback.TAG, "onForegroundActivitiesChanged =" + pid);
                    Message msg = Message.obtain(AwareCallback.this.mCbHandler, (int) AwareCallback.NOTIFY_FOREGROUND);
                    msg.getData().putBoolean(MemoryConstant.MEM_REPAIR_CONSTANT_FG, foregroundActivities);
                    msg.arg1 = pid;
                    msg.arg2 = uid;
                    AwareCallback.this.mCbHandler.sendMessage(msg);
                }
            }
        }

        public void onProcessDied(int pid, int uid) {
            synchronized (AwareCallback.this.mProcLock) {
                if (AwareCallback.this.mInProcObserver != null) {
                    AwareLog.d(AwareCallback.TAG, "onProcessDied =" + pid);
                    Message msg = Message.obtain(AwareCallback.this.mCbHandler, (int) AwareCallback.NOTIFY_PROC);
                    msg.arg1 = pid;
                    msg.arg2 = uid;
                    AwareCallback.this.mCbHandler.sendMessage(msg);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class UserSwitchObserver extends IUserSwitchObserverEx {
        private UserSwitchObserver() {
        }

        public void onUserSwitching(int newUserId, IRemoteCallbackEx reply) {
            if (reply != null) {
                try {
                    reply.sendResult((Bundle) null);
                } catch (RemoteException e) {
                    AwareLog.e(AwareCallback.TAG, "RemoteException onUserSwitching");
                }
            }
        }

        public void onUserSwitchComplete(int newUserId) {
            AwareLog.d(AwareCallback.TAG, "onUserSwitchComplete =" + newUserId);
            Message msg = Message.obtain(AwareCallback.this.mCbHandler, 301);
            msg.arg1 = newUserId;
            AwareCallback.this.mCbHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class AwareCbHandler extends Handler {
        private AwareCbHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg != null) {
                int i = msg.what;
                if (i == 100) {
                    AwareCallback.this.notifyActivityMsg(msg);
                } else if (i == 301) {
                    AwareCallback.this.notifyUserMsg(msg);
                } else if (i == AwareCallback.NOTIFY_FOREGROUND) {
                    AwareCallback.this.notifyForegroundMsg(msg);
                } else if (i != AwareCallback.NOTIFY_PROC) {
                    AwareLog.w(AwareCallback.TAG, "AwareCb, default msg.what is " + msg.what);
                } else {
                    AwareCallback.this.notifyProcMsg(msg);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyActivityMsg(Message msg) {
        if (msg.obj instanceof Bundle) {
            Bundle bundle = (Bundle) msg.obj;
            String reason = bundle.getString("android.intent.extra.REASON");
            if (!TextUtils.isEmpty(reason)) {
                synchronized (this.mActLock) {
                    if (this.mInActNotifier.containsValue(reason)) {
                        for (Map.Entry<IHwActivityNotifierEx, String> entry : this.mOutActNotifier.entrySet()) {
                            if (reason.equals(entry.getValue())) {
                                entry.getKey().call(bundle);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyForegroundMsg(Message msg) {
        boolean foreground = msg.getData().getBoolean(MemoryConstant.MEM_REPAIR_CONSTANT_FG);
        int pid = msg.arg1;
        int uid = msg.arg2;
        synchronized (this.mProcLock) {
            Iterator<IProcessObserverEx> it = this.mOutProcObservers.iterator();
            while (it.hasNext()) {
                it.next().onForegroundActivitiesChanged(pid, uid, foreground);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyProcMsg(Message msg) {
        int pid = msg.arg1;
        int uid = msg.arg2;
        synchronized (this.mProcLock) {
            Iterator<IProcessObserverEx> it = this.mOutProcObservers.iterator();
            while (it.hasNext()) {
                it.next().onProcessDied(pid, uid);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyUserMsg(Message msg) {
        int newUserId = msg.arg1;
        synchronized (this.mUserLock) {
            Iterator<IUserSwitchObserverEx> it = this.mOutUserObservers.iterator();
            while (it.hasNext()) {
                it.next().onUserSwitchComplete(newUserId);
            }
        }
    }
}

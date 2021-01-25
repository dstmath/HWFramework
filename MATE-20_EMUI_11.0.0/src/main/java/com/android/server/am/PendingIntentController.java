package com.android.server.am;

import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.content.IIntentSender;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.LocalServices;
import com.android.server.am.PendingIntentRecord;
import com.android.server.pm.DumpState;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.SafeActivityOptions;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PendingIntentController {
    private static final int PENDINGINTENT_ALLOW_TIME_THRESHOLD = 2000;
    private static final String TAG = "ActivityManager";
    private static final String TAG_MU = "ActivityManager_MU";
    ActivityManagerInternal mAmInternal;
    final ActivityTaskManagerInternal mAtmInternal;
    final Handler mH;
    final HashMap<PendingIntentRecord.Key, WeakReference<PendingIntentRecord>> mIntentSenderRecords = new HashMap<>();
    final Object mLock = new Object();
    final UserController mUserController;

    PendingIntentController(Looper looper, UserController userController) {
        this.mH = new Handler(looper);
        this.mAtmInternal = (ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class);
        this.mUserController = userController;
    }

    /* access modifiers changed from: package-private */
    public void onActivityManagerInternalAdded() {
        synchronized (this.mLock) {
            this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        }
    }

    public PendingIntentRecord getIntentSender(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle bOptions) {
        synchronized (this.mLock) {
            try {
                if (ActivityManagerDebugConfig.DEBUG_MU) {
                    Slog.v(TAG_MU, "getIntentSender(): uid=" + callingUid);
                }
                boolean updateCurrent = false;
                if (intents != null) {
                    for (Intent intent : intents) {
                        intent.setDefusable(true);
                    }
                }
                Bundle.setDefusable(bOptions, true);
                boolean noCreate = (flags & 536870912) != 0;
                boolean cancelCurrent = (flags & 268435456) != 0;
                if ((flags & DumpState.DUMP_HWFEATURES) != 0) {
                    updateCurrent = true;
                }
                try {
                    PendingIntentRecord.Key key = new PendingIntentRecord.Key(type, packageName, token, resultWho, requestCode, intents, resolvedTypes, flags & -939524097, SafeActivityOptions.fromBundle(bOptions), userId);
                    WeakReference<PendingIntentRecord> ref = this.mIntentSenderRecords.get(key);
                    PendingIntentRecord rec = ref != null ? ref.get() : null;
                    if (rec != null) {
                        if (!cancelCurrent) {
                            if (updateCurrent) {
                                if (rec.key.requestIntent != null) {
                                    rec.key.requestIntent.replaceExtras(intents != null ? intents[intents.length - 1] : null);
                                }
                                if (intents != null) {
                                    intents[intents.length - 1] = rec.key.requestIntent;
                                    rec.key.allIntents = intents;
                                    rec.key.allResolvedTypes = resolvedTypes;
                                } else {
                                    rec.key.allIntents = null;
                                    rec.key.allResolvedTypes = null;
                                }
                            }
                            return rec;
                        }
                        makeIntentSenderCanceled(rec);
                        this.mIntentSenderRecords.remove(key);
                    }
                    if (noCreate) {
                        return rec;
                    }
                    PendingIntentRecord rec2 = new PendingIntentRecord(this, key, callingUid);
                    this.mIntentSenderRecords.put(key, rec2.ref);
                    return rec2;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removePendingIntentsForPackage(String packageName, int userId, int appId, boolean doIt) {
        boolean didSomething = false;
        synchronized (this.mLock) {
            if (this.mIntentSenderRecords.size() <= 0) {
                return false;
            }
            Iterator<WeakReference<PendingIntentRecord>> it = this.mIntentSenderRecords.values().iterator();
            while (it.hasNext()) {
                WeakReference<PendingIntentRecord> wpir = it.next();
                if (wpir == null) {
                    it.remove();
                } else {
                    PendingIntentRecord pir = wpir.get();
                    if (pir == null) {
                        it.remove();
                    } else {
                        if (packageName == null) {
                            if (pir.key.userId != userId) {
                            }
                        } else if (UserHandle.getAppId(pir.uid) == appId) {
                            if (userId == -1 || pir.key.userId == userId) {
                                if (!packageName.equals(pir.key.packageName)) {
                                }
                            }
                        }
                        if (!doIt) {
                            return true;
                        }
                        didSomething = true;
                        it.remove();
                        makeIntentSenderCanceled(pir);
                        if (pir.key.activity != null) {
                            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$PendingIntentController$sPmaborOkBSSEP2wiimxXweYDQ.INSTANCE, this, pir.key.activity, pir.ref));
                        }
                    }
                }
            }
            return didSomething;
        }
    }

    public void cancelIntentSender(IIntentSender sender) {
        if (sender instanceof PendingIntentRecord) {
            synchronized (this.mLock) {
                PendingIntentRecord rec = (PendingIntentRecord) sender;
                try {
                    if (UserHandle.isSameApp(AppGlobals.getPackageManager().getPackageUid(rec.key.packageName, 268435456, UserHandle.getCallingUserId()), Binder.getCallingUid())) {
                        cancelIntentSender(rec, true);
                    } else {
                        String msg = "Permission Denial: cancelIntentSender() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " is not allowed to cancel package " + rec.key.packageName;
                        Slog.w("ActivityManager", msg);
                        throw new SecurityException(msg);
                    }
                } catch (RemoteException e) {
                    throw new SecurityException(e);
                }
            }
        }
    }

    public void cancelIntentSender(PendingIntentRecord rec, boolean cleanActivity) {
        synchronized (this.mLock) {
            makeIntentSenderCanceled(rec);
            this.mIntentSenderRecords.remove(rec.key);
            if (cleanActivity && rec.key.activity != null) {
                this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$PendingIntentController$sPmaborOkBSSEP2wiimxXweYDQ.INSTANCE, this, rec.key.activity, rec.ref));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) {
        boolean isCancelled;
        if (sender instanceof PendingIntentRecord) {
            synchronized (this.mLock) {
                PendingIntentRecord pendingIntent = (PendingIntentRecord) sender;
                isCancelled = pendingIntent.canceled;
                if (!isCancelled) {
                    pendingIntent.registerCancelListenerLocked(receiver);
                }
            }
            if (isCancelled) {
                try {
                    receiver.send(0, (Bundle) null);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver) {
        if (sender instanceof PendingIntentRecord) {
            synchronized (this.mLock) {
                ((PendingIntentRecord) sender).unregisterCancelListenerLocked(receiver);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPendingIntentWhitelistDuration(IIntentSender target, IBinder whitelistToken, long duration) {
        if (!(target instanceof PendingIntentRecord)) {
            Slog.w("ActivityManager", "markAsSentFromNotification(): not a PendingIntentRecord: " + target);
            return;
        }
        synchronized (this.mLock) {
            ((PendingIntentRecord) target).setWhitelistDurationLocked(whitelistToken, duration);
        }
    }

    private void makeIntentSenderCanceled(PendingIntentRecord rec) {
        rec.canceled = true;
        RemoteCallbackList<IResultReceiver> callbacks = rec.detachCancelListenersLocked();
        if (callbacks != null) {
            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$PendingIntentController$pDmmJDvS20vSAAXh9qdzbN0P8N0.INSTANCE, this, callbacks));
        }
    }

    /* access modifiers changed from: private */
    public void handlePendingIntentCancelled(RemoteCallbackList<IResultReceiver> callbacks) {
        int N = callbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                callbacks.getBroadcastItem(i).send(0, (Bundle) null);
            } catch (RemoteException e) {
            }
        }
        callbacks.finishBroadcast();
        callbacks.kill();
    }

    /* access modifiers changed from: private */
    public void clearPendingResultForActivity(IBinder activityToken, WeakReference<PendingIntentRecord> pir) {
        this.mAtmInternal.clearPendingResultForActivity(activityToken, pir);
    }

    /* access modifiers changed from: package-private */
    public void dumpPendingIntents(PrintWriter pw, boolean dumpAll, String dumpPackage) {
        synchronized (this.mLock) {
            boolean printed = false;
            pw.println("ACTIVITY MANAGER PENDING INTENTS (dumpsys activity intents)");
            if (this.mIntentSenderRecords.size() > 0) {
                ArrayMap<String, ArrayList<PendingIntentRecord>> byPackage = new ArrayMap<>();
                ArrayList<WeakReference<PendingIntentRecord>> weakRefs = new ArrayList<>();
                Iterator<WeakReference<PendingIntentRecord>> it = this.mIntentSenderRecords.values().iterator();
                while (it.hasNext()) {
                    WeakReference<PendingIntentRecord> ref = it.next();
                    PendingIntentRecord rec = ref != null ? ref.get() : null;
                    if (rec == null) {
                        weakRefs.add(ref);
                    } else if (dumpPackage == null || dumpPackage.equals(rec.key.packageName)) {
                        ArrayList<PendingIntentRecord> list = byPackage.get(rec.key.packageName);
                        if (list == null) {
                            list = new ArrayList<>();
                            byPackage.put(rec.key.packageName, list);
                        }
                        list.add(rec);
                    }
                }
                for (int i = 0; i < byPackage.size(); i++) {
                    ArrayList<PendingIntentRecord> intents = byPackage.valueAt(i);
                    printed = true;
                    pw.print("  * ");
                    pw.print(byPackage.keyAt(i));
                    pw.print(": ");
                    pw.print(intents.size());
                    pw.println(" items");
                    for (int j = 0; j < intents.size(); j++) {
                        pw.print("    #");
                        pw.print(j);
                        pw.print(": ");
                        pw.println(intents.get(j));
                        if (dumpAll) {
                            intents.get(j).dump(pw, "      ");
                        }
                    }
                }
                if (weakRefs.size() > 0) {
                    printed = true;
                    pw.println("  * WEAK REFS:");
                    for (int i2 = 0; i2 < weakRefs.size(); i2++) {
                        pw.print("    #");
                        pw.print(i2);
                        pw.print(": ");
                        pw.println(weakRefs.get(i2));
                    }
                }
            }
            if (!printed) {
                pw.println("  (nothing)");
            }
        }
    }

    public boolean isStartFromPendingIntent(String packageName, int userId) {
        boolean isFromPengingIntent = false;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mIntentSenderRecords.size() > 0) {
                Iterator<WeakReference<PendingIntentRecord>> it = this.mIntentSenderRecords.values().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    WeakReference<PendingIntentRecord> wpir = it.next();
                    if (wpir != null) {
                        PendingIntentRecord pir = wpir.get();
                        isFromPengingIntent = packageName.equals(pir.key.packageName) && userId == pir.key.userId && pir.sent && System.currentTimeMillis() - pir.sendTime < 2000;
                        if (isFromPengingIntent) {
                            break;
                        }
                    }
                }
            }
        }
        return isFromPengingIntent;
    }
}

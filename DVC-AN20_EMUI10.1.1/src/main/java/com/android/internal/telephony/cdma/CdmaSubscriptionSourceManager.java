package com.android.internal.telephony.cdma;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.provider.Settings;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import java.util.concurrent.atomic.AtomicInteger;

public class CdmaSubscriptionSourceManager extends Handler {
    private static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 1;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_SOURCE = 2;
    private static final int EVENT_RADIO_ON = 3;
    private static final int EVENT_SUBSCRIPTION_STATUS_CHANGED = 4;
    static final String LOG_TAG = "CdmaSSM";
    private static final int SUBSCRIPTION_ACTIVATED = 1;
    public static final int SUBSCRIPTION_FROM_NV = 1;
    public static final int SUBSCRIPTION_FROM_RUIM = 0;
    public static final int SUBSCRIPTION_SOURCE_UNKNOWN = -1;
    private static CdmaSubscriptionSourceManager sInstance;
    private static int sReferenceCount = 0;
    private static final Object sReferenceCountMonitor = new Object();
    private AtomicInteger mCdmaSubscriptionSource = new AtomicInteger(0);
    private RegistrantList mCdmaSubscriptionSourceChangedRegistrants = new RegistrantList();
    private CommandsInterface mCi;

    private CdmaSubscriptionSourceManager(Context context, CommandsInterface ci) {
        this.mCi = ci;
        this.mCi.registerForCdmaSubscriptionChanged(this, 1, null);
        this.mCi.registerForOn(this, 3, null);
        int subscriptionSource = getDefault(context);
        log("cdmaSSM constructor: " + subscriptionSource);
        this.mCdmaSubscriptionSource.set(subscriptionSource);
        this.mCi.registerForSubscriptionStatusChanged(this, 4, null);
    }

    @UnsupportedAppUsage
    public static CdmaSubscriptionSourceManager getInstance(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        synchronized (sReferenceCountMonitor) {
            if (sInstance == null) {
                sInstance = new CdmaSubscriptionSourceManager(context, ci);
            }
            sReferenceCount++;
        }
        sInstance.registerForCdmaSubscriptionSourceChanged(h, what, obj);
        return sInstance;
    }

    public void dispose(Handler h) {
        this.mCdmaSubscriptionSourceChangedRegistrants.remove(h);
        synchronized (sReferenceCountMonitor) {
            sReferenceCount--;
            if (sReferenceCount <= 0) {
                this.mCi.unregisterForCdmaSubscriptionChanged(this);
                this.mCi.unregisterForOn(this);
                this.mCi.unregisterForSubscriptionStatusChanged(this);
                sInstance = null;
            }
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1 || i == 2) {
            log("CDMA_SUBSCRIPTION_SOURCE event = " + msg.what);
            handleGetCdmaSubscriptionSource((AsyncResult) msg.obj);
        } else if (i == 3) {
            this.mCi.getCdmaSubscriptionSource(obtainMessage(2));
        } else if (i != 4) {
            super.handleMessage(msg);
        } else {
            log("EVENT_SUBSCRIPTION_STATUS_CHANGED");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                int actStatus = ((int[]) ar.result)[0];
                log("actStatus = " + actStatus);
                if (actStatus == 1) {
                    Rlog.v(LOG_TAG, "get Cdma Subscription Source");
                    this.mCi.getCdmaSubscriptionSource(obtainMessage(2));
                    return;
                }
                return;
            }
            logw("EVENT_SUBSCRIPTION_STATUS_CHANGED, Exception:" + ar.exception);
        }
    }

    @UnsupportedAppUsage
    public int getCdmaSubscriptionSource() {
        log("getcdmasubscriptionSource: " + this.mCdmaSubscriptionSource.get());
        return this.mCdmaSubscriptionSource.get();
    }

    public static int getDefault(Context context) {
        int subscriptionSource = Settings.Global.getInt(context.getContentResolver(), "subscription_mode", 0);
        Rlog.d(LOG_TAG, "subscriptionSource from settings: " + subscriptionSource);
        return subscriptionSource;
    }

    private void registerForCdmaSubscriptionSourceChanged(Handler h, int what, Object obj) {
        this.mCdmaSubscriptionSourceChangedRegistrants.add(new Registrant(h, what, obj));
    }

    private void handleGetCdmaSubscriptionSource(AsyncResult ar) {
        if (ar.exception != null || ar.result == null || !(ar.result instanceof int[])) {
            logw("Unable to get CDMA Subscription Source, Exception: " + ar.exception + ", result: " + ar.result);
            return;
        }
        int newSubscriptionSource = ((int[]) ar.result)[0];
        if (newSubscriptionSource != this.mCdmaSubscriptionSource.get()) {
            log("Subscription Source Changed : " + this.mCdmaSubscriptionSource + " >> " + newSubscriptionSource);
            this.mCdmaSubscriptionSource.set(newSubscriptionSource);
            this.mCdmaSubscriptionSourceChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, s);
    }
}

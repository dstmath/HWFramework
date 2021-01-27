package com.huawei.internal.telephony.smartnet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;

public class HwSmartNetRouteStrategy {
    private static final String ACTION_HW_SMARTNET_LOCATION_CHANGED = "com.huawei.intent.action.SMARTNET_LOCATION_CHANGED";
    public static final int EVENT_LOCATION_DEFAULT = 0;
    public static final int EVENT_LOCATION_EXIT_COMPANY = 4;
    public static final int EVENT_LOCATION_EXIT_COMPANY_AND_ENTER_HOME = 1;
    public static final int EVENT_LOCATION_EXIT_HOME = 3;
    public static final int EVENT_LOCATION_EXIT_HOME_AND_ENTER_COMPANY = 2;
    private static final String SMARTNET_LOCATION_CHANGED_EVENTID = "smartnet_location_changed_eventid";
    private static final String TAG = "HwSmartNetRouteStrategy";
    private static HwSmartNetRouteStrategy sInstance = null;
    private Context mContext = null;
    private int mLastRouteId = -1;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.huawei.internal.telephony.smartnet.HwSmartNetRouteStrategy.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwSmartNetRouteStrategy.logi("intent is null, return");
            } else if (HwSmartNetRouteStrategy.ACTION_HW_SMARTNET_LOCATION_CHANGED.equals(intent.getAction())) {
                HwSmartNetRouteStrategy.this.processSmartNetLocationChanged(intent.getIntExtra(HwSmartNetRouteStrategy.SMARTNET_LOCATION_CHANGED_EVENTID, 0));
            }
        }
    };
    private int mRouteId = -1;
    private Handler mStateHandler = null;

    private HwSmartNetRouteStrategy(Context context, Handler stateHandler) {
        this.mContext = context;
        this.mStateHandler = stateHandler;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HW_SMARTNET_LOCATION_CHANGED);
        this.mContext.registerReceiver(this.mReceiver, filter);
        logi("create HwSmartNetRouteStrategy");
    }

    /* access modifiers changed from: private */
    public static void logi(String msg) {
        RlogEx.i(TAG, msg);
    }

    public static HwSmartNetRouteStrategy init(Context context, Handler stateHandler) {
        HwSmartNetRouteStrategy hwSmartNetRouteStrategy;
        synchronized (HwSmartNetRouteStrategy.class) {
            if (sInstance == null) {
                sInstance = new HwSmartNetRouteStrategy(context, stateHandler);
            } else {
                logi("init() called multiple times!  sInstance = " + sInstance);
            }
            hwSmartNetRouteStrategy = sInstance;
        }
        return hwSmartNetRouteStrategy;
    }

    public static HwSmartNetRouteStrategy getInstance() {
        HwSmartNetRouteStrategy hwSmartNetRouteStrategy;
        synchronized (HwSmartNetRouteStrategy.class) {
            hwSmartNetRouteStrategy = sInstance;
        }
        return hwSmartNetRouteStrategy;
    }

    public void processSmartNetLocationChanged(int locationState) {
        logi("processSmartNetLocationChanged = " + locationState);
        if (locationState == 1 || locationState == 2) {
            this.mLastRouteId = this.mRouteId;
            this.mRouteId = -1;
            this.mStateHandler.obtainMessage(HwSmartNetConstants.EVENT_STATE_ENTER_HOME_OR_COMPANY).sendToTarget();
        } else if (locationState == 3) {
            this.mRouteId = 1;
            this.mStateHandler.obtainMessage(HwSmartNetConstants.EVENT_STATE_EXIT_HOME_OR_COMPANY).sendToTarget();
        } else if (locationState == 4) {
            this.mRouteId = 2;
            this.mStateHandler.obtainMessage(HwSmartNetConstants.EVENT_STATE_EXIT_HOME_OR_COMPANY).sendToTarget();
        }
    }

    public int getCurrentRouteId() {
        return this.mRouteId;
    }

    public int getLastRouteId() {
        return this.mLastRouteId;
    }

    public void setCurrentRouteId(int routeId) {
        this.mRouteId = routeId;
    }
}

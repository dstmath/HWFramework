package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionManager;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.net.NetworkFactoryEx;
import com.huawei.android.net.StringNetworkSpecifierEx;
import com.huawei.android.util.LocalLogEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;

public class HwSlicesNetworkFactory extends NetworkFactoryEx {
    private static final int EVENT_NETWORK_RELEASE = 2;
    private static final int EVENT_NETWORK_REQUEST = 1;
    private static final int SLICES_NETWORK_SCORE = 52;
    private final String LOG_TAG;
    private Context mContext;
    private final DcTrackerEx mDcTracker;
    private final Handler mInternalHandler;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.dataconnection.HwSlicesNetworkFactory.AnonymousClass1 */

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            HwSlicesNetworkFactory.this.log("onSubscriptionsChanged");
            HwSlicesNetworkFactory.this.onSubIdChange();
        }
    };
    private int mPhoneId;
    private final SubscriptionControllerEx mSubscriptionController = SubscriptionControllerEx.getInstance();
    private int mSubscriptionId = -1;

    public HwSlicesNetworkFactory(DcTrackerEx dcTracker, Looper looper, Context context, int phoneId) {
        super(looper, context, "HwSlicesNetworkFactory[" + phoneId + "]", (NetworkCapabilitiesEx) null);
        this.mPhoneId = phoneId;
        setCapabilityFilter(makeNetworkFilter(this.mSubscriptionController.getSubIdUsingPhoneId(phoneId)));
        setScoreFilter(SLICES_NETWORK_SCORE);
        this.mInternalHandler = new InternalHandler(looper);
        this.mDcTracker = dcTracker;
        this.mContext = context;
        ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        this.LOG_TAG = "HwSlicesNetworkFactory[" + phoneId + "]";
        register();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSubIdChange() {
        int newSubscriptionId = this.mSubscriptionController.getSubIdUsingPhoneId(this.mPhoneId);
        if (this.mSubscriptionId != newSubscriptionId) {
            log("onSubIdChange " + this.mSubscriptionId + "->" + newSubscriptionId);
            this.mSubscriptionId = newSubscriptionId;
            setCapabilityFilter(makeNetworkFilter(this.mSubscriptionId));
        }
    }

    private NetworkCapabilitiesEx makeNetworkFilter(int subscriptionId) {
        NetworkCapabilitiesEx nc = new NetworkCapabilitiesEx();
        nc.addTransportType(0);
        nc.addCapability(13);
        nc.addCapability(33);
        nc.addCapability(34);
        nc.addCapability(35);
        nc.addCapability(36);
        nc.addCapability(37);
        nc.addCapability(38);
        nc.setNetworkSpecifier(new StringNetworkSpecifierEx(String.valueOf(subscriptionId)));
        return nc;
    }

    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        log("needNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(1);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNeedNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        log("onNeedNetworkFor " + networkRequest);
        if (this.mSubscriptionId == this.mSubscriptionController.getDefaultDataSubId()) {
            this.mDcTracker.requestNetwork(networkRequest, DcTrackerEx.REQUEST_TYPE_NORMAL, (Message) null, (LocalLogEx) null);
        }
    }

    public void releaseNetworkFor(NetworkRequest networkRequest) {
        log("releaseNetworkFor - " + networkRequest);
        Message msg = this.mInternalHandler.obtainMessage(2);
        msg.obj = networkRequest;
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReleaseNetworkFor(Message msg) {
        NetworkRequest networkRequest = (NetworkRequest) msg.obj;
        log("onReleaseNetworkFor " + networkRequest);
        this.mDcTracker.releaseNetwork(networkRequest, DcTrackerEx.RELEASE_TYPE_NORMAL, (LocalLogEx) null);
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwSlicesNetworkFactory.this.onNeedNetworkFor(msg);
            } else if (i == 2) {
                HwSlicesNetworkFactory.this.onReleaseNetworkFor(msg);
            }
        }
    }
}

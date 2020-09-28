package com.android.internal.telephony;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.metrics.TelephonyMetrics;

public class CellularNetworkValidator {
    private static final String LOG_TAG = "NetworkValidator";
    private static final int STATE_IDLE = 0;
    private static final int STATE_VALIDATED = 2;
    private static final int STATE_VALIDATING = 1;
    private static CellularNetworkValidator sInstance;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private Handler mHandler = new Handler();
    private ConnectivityNetworkCallback mNetworkCallback;
    private NetworkRequest mNetworkRequest;
    private boolean mReleaseAfterValidation;
    private int mState = 0;
    private int mSubId;
    private int mTimeoutInMs;
    private ValidationCallback mValidationCallback;

    public interface ValidationCallback {
        void onValidationResult(boolean z, int i);
    }

    public static CellularNetworkValidator make(Context context) {
        if (sInstance != null) {
            logd("createCellularNetworkValidator failed. Instance already exists.");
        } else {
            sInstance = new CellularNetworkValidator(context);
        }
        return sInstance;
    }

    public static CellularNetworkValidator getInstance() {
        return sInstance;
    }

    public boolean isValidationFeatureSupported() {
        return PhoneConfigurationManager.getInstance().getCurrentPhoneCapability().validationBeforeSwitchSupported;
    }

    private CellularNetworkValidator(Context context) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
    }

    public synchronized void validate(int subId, int timeoutInMs, boolean releaseAfterValidation, ValidationCallback callback) {
        if (subId != this.mSubId) {
            if (PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId)) == null) {
                logd("Failed to start validation. Inactive subId " + subId);
                callback.onValidationResult(false, subId);
                return;
            }
            if (isValidating()) {
                stopValidation();
            }
            this.mState = 1;
            this.mSubId = subId;
            this.mTimeoutInMs = timeoutInMs;
            this.mValidationCallback = callback;
            this.mReleaseAfterValidation = releaseAfterValidation;
            this.mNetworkRequest = createNetworkRequest();
            logd("Start validating subId " + this.mSubId + " mTimeoutInMs " + this.mTimeoutInMs + " mReleaseAfterValidation " + this.mReleaseAfterValidation);
            this.mNetworkCallback = new ConnectivityNetworkCallback(subId);
            this.mConnectivityManager.requestNetwork(this.mNetworkRequest, this.mNetworkCallback, this.mHandler, this.mTimeoutInMs);
        }
    }

    public synchronized void stopValidation() {
        if (!isValidating()) {
            logd("No need to stop validation.");
        } else {
            this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
            this.mState = 0;
        }
        this.mSubId = -1;
    }

    public synchronized int getSubIdInValidation() {
        return this.mSubId;
    }

    public synchronized boolean isValidating() {
        return this.mState != 0;
    }

    private NetworkRequest createNetworkRequest() {
        return new NetworkRequest.Builder().addCapability(12).addTransportType(0).setNetworkSpecifier(String.valueOf(this.mSubId)).build();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void reportValidationResult(boolean passed, int subId) {
        if (this.mSubId == subId) {
            if (this.mState == 1) {
                this.mValidationCallback.onValidationResult(passed, this.mSubId);
                int i = 2;
                if (this.mReleaseAfterValidation || !passed) {
                    this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
                    this.mState = 0;
                } else {
                    this.mState = 2;
                }
                TelephonyMetrics instance = TelephonyMetrics.getInstance();
                if (passed) {
                    i = 3;
                }
                instance.writeNetworkValidate(i);
            }
            this.mSubId = -1;
        }
    }

    /* access modifiers changed from: package-private */
    public class ConnectivityNetworkCallback extends ConnectivityManager.NetworkCallback {
        private final int mSubId;

        ConnectivityNetworkCallback(int subId) {
            this.mSubId = subId;
        }

        public void onAvailable(Network network) {
            CellularNetworkValidator.logd("network onAvailable " + network);
            if (this.mSubId == CellularNetworkValidator.this.mSubId) {
                TelephonyMetrics.getInstance().writeNetworkValidate(1);
            }
        }

        public void onLosing(Network network, int maxMsToLive) {
            CellularNetworkValidator.logd("network onLosing " + network + " maxMsToLive " + maxMsToLive);
            CellularNetworkValidator.this.reportValidationResult(false, this.mSubId);
        }

        public void onLost(Network network) {
            CellularNetworkValidator.logd("network onLost " + network);
            CellularNetworkValidator.this.reportValidationResult(false, this.mSubId);
        }

        public void onUnavailable() {
            CellularNetworkValidator.logd("onUnavailable");
            CellularNetworkValidator.this.reportValidationResult(false, this.mSubId);
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            if (networkCapabilities.hasCapability(16)) {
                CellularNetworkValidator.logd("onValidated");
                CellularNetworkValidator.this.reportValidationResult(true, this.mSubId);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void logd(String log) {
        Log.d(LOG_TAG, log);
    }
}

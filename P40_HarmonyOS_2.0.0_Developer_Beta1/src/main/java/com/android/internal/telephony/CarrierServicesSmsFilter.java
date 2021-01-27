package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.service.carrier.ICarrierMessagingCallback;
import android.service.carrier.ICarrierMessagingService;
import android.service.carrier.MessagePdu;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.Rlog;
import android.util.LocalLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.SMSDispatcherUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CarrierServicesSmsFilter {
    protected static final boolean DBG = true;
    public static final int EVENT_ON_FILTER_COMPLETE_NOT_CALLED = 1;
    public static final int FILTER_COMPLETE_TIMEOUT_MS = 600000;
    private final CallbackTimeoutHandler mCallbackTimeoutHandler = new CallbackTimeoutHandler();
    private final CarrierServicesSmsFilterCallbackInterface mCarrierServicesSmsFilterCallback;
    private final Context mContext;
    private final int mDestPort;
    private FilterAggregator mFilterAggregator;
    private final LocalLog mLocalLog;
    private final String mLogTag;
    private final String mPduFormat;
    private final byte[][] mPdus;
    private final Phone mPhone;

    @VisibleForTesting
    public interface CarrierServicesSmsFilterCallbackInterface {
        void onFilterComplete(int i);
    }

    @VisibleForTesting
    public CarrierServicesSmsFilter(Context context, Phone phone, byte[][] pdus, int destPort, String pduFormat, CarrierServicesSmsFilterCallbackInterface carrierServicesSmsFilterCallback, String logTag, LocalLog localLog) {
        this.mContext = context;
        this.mPhone = phone;
        this.mPdus = pdus;
        this.mDestPort = destPort;
        this.mPduFormat = pduFormat;
        this.mCarrierServicesSmsFilterCallback = carrierServicesSmsFilterCallback;
        this.mLogTag = logTag;
        this.mLocalLog = localLog;
    }

    @VisibleForTesting
    public boolean filter() {
        Optional<String> carrierAppForFiltering = getCarrierAppPackageForFiltering();
        List<String> smsFilterPackages = new ArrayList<>();
        if (carrierAppForFiltering.isPresent()) {
            smsFilterPackages.add(carrierAppForFiltering.get());
        }
        String carrierImsPackage = CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierImsPackage != null && SMSDispatcherUtil.isSupportHandleSmsBybinderService(this.mContext, carrierImsPackage)) {
            smsFilterPackages.add(carrierImsPackage);
        }
        if (this.mFilterAggregator == null) {
            int numPackages = smsFilterPackages.size();
            if (numPackages <= 0) {
                return false;
            }
            this.mFilterAggregator = new FilterAggregator(numPackages);
            CallbackTimeoutHandler callbackTimeoutHandler = this.mCallbackTimeoutHandler;
            callbackTimeoutHandler.sendMessageDelayed(callbackTimeoutHandler.obtainMessage(1), 600000);
            for (String smsFilterPackage : smsFilterPackages) {
                filterWithPackage(smsFilterPackage, this.mFilterAggregator);
            }
            return true;
        }
        loge("Cannot reuse the same CarrierServiceSmsFilter object for filtering.");
        throw new RuntimeException("Cannot reuse the same CarrierServiceSmsFilter object for filtering.");
    }

    private Optional<String> getCarrierAppPackageForFiltering() {
        List<String> carrierPackages = null;
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card != null) {
            carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        } else {
            Rlog.e(this.mLogTag, "UiccCard not initialized.");
        }
        if (carrierPackages == null || carrierPackages.size() != 1) {
            List<String> systemPackages = getSystemAppForIntent(new Intent("android.service.carrier.CarrierMessagingService"));
            if (systemPackages == null || systemPackages.size() != 1) {
                logv("Unable to find carrier package: " + carrierPackages + ", nor systemPackages: " + systemPackages);
                return Optional.empty();
            }
            log("Found system package.");
            return Optional.of(systemPackages.get(0));
        }
        log("Found carrier package.");
        return Optional.of(carrierPackages.get(0));
    }

    private void filterWithPackage(String packageName, FilterAggregator filterAggregator) {
        CarrierSmsFilter smsFilter = new CarrierSmsFilter(this.mPdus, this.mDestPort, this.mPduFormat);
        CarrierSmsFilterCallback smsFilterCallback = new CarrierSmsFilterCallback(filterAggregator, smsFilter);
        filterAggregator.addToCallbacks(smsFilterCallback);
        smsFilter.filterSms(packageName, smsFilterCallback);
    }

    private List<String> getSystemAppForIntent(Intent intent) {
        List<String> packages = new ArrayList<>();
        PackageManager packageManager = this.mContext.getPackageManager();
        for (ResolveInfo info : packageManager.queryIntentServices(intent, 0)) {
            if (info.serviceInfo == null) {
                loge("Can't get service information from " + info);
            } else {
                String packageName = info.serviceInfo.packageName;
                if (packageManager.checkPermission("android.permission.CARRIER_FILTER_SMS", packageName) == 0) {
                    packages.add(packageName);
                    log("getSystemAppForIntent: added package " + packageName);
                }
            }
        }
        return packages;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String message) {
        Rlog.d(this.mLogTag, message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String message) {
        Rlog.e(this.mLogTag, message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logv(String message) {
        Rlog.e(this.mLogTag, message);
    }

    /* access modifiers changed from: private */
    public final class CarrierSmsFilter extends CarrierMessagingServiceManager {
        private final int mDestPort;
        private final byte[][] mPdus;
        private volatile CarrierSmsFilterCallback mSmsFilterCallback;
        private final String mSmsFormat;

        CarrierSmsFilter(byte[][] pdus, int destPort, String smsFormat) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
        }

        /* access modifiers changed from: package-private */
        public void filterSms(String carrierPackageName, CarrierSmsFilterCallback smsFilterCallback) {
            this.mSmsFilterCallback = smsFilterCallback;
            if (!bindToCarrierMessagingService(CarrierServicesSmsFilter.this.mContext, carrierPackageName)) {
                CarrierServicesSmsFilter.this.loge("bindService() for carrier messaging service failed");
                smsFilterCallback.onFilterComplete(0);
                return;
            }
            CarrierServicesSmsFilter.this.logv("bindService() for carrier messaging service succeeded");
        }

        /* access modifiers changed from: protected */
        @Override // android.telephony.CarrierMessagingServiceManager
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                CarrierServicesSmsFilter.this.log("onServiceReady: calling filterSms");
                carrierMessagingService.filterSms(new MessagePdu(Arrays.asList(this.mPdus)), this.mSmsFormat, this.mDestPort, CarrierServicesSmsFilter.this.mPhone.getSubId(), this.mSmsFilterCallback);
            } catch (RemoteException e) {
                CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
                carrierServicesSmsFilter.loge("Exception filtering the SMS: " + e);
                this.mSmsFilterCallback.onFilterComplete(0);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class CarrierSmsFilterCallback extends ICarrierMessagingCallback.Stub {
        private final CarrierMessagingServiceManager mCarrierMessagingServiceManager;
        private final FilterAggregator mFilterAggregator;
        private boolean mIsOnFilterCompleteCalled = false;

        CarrierSmsFilterCallback(FilterAggregator filterAggregator, CarrierMessagingServiceManager carrierMessagingServiceManager) {
            this.mFilterAggregator = filterAggregator;
            this.mCarrierMessagingServiceManager = carrierMessagingServiceManager;
        }

        public void onFilterComplete(int result) {
            CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
            carrierServicesSmsFilter.log("onFilterComplete called with result: " + result);
            if (!this.mIsOnFilterCompleteCalled) {
                this.mIsOnFilterCompleteCalled = true;
                this.mCarrierMessagingServiceManager.disposeConnection(CarrierServicesSmsFilter.this.mContext);
                this.mFilterAggregator.onFilterComplete(result);
            }
        }

        public void onSendSmsComplete(int result, int messageRef) {
            CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
            carrierServicesSmsFilter.loge("Unexpected onSendSmsComplete call with result: " + result);
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
            carrierServicesSmsFilter.loge("Unexpected onSendMultipartSmsComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
            carrierServicesSmsFilter.loge("Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
            carrierServicesSmsFilter.loge("Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    /* access modifiers changed from: private */
    public final class FilterAggregator {
        private final Set<CarrierSmsFilterCallback> mCallbacks;
        private final Object mFilterLock = new Object();
        private int mFilterResult;
        private int mNumPendingFilters;

        FilterAggregator(int numFilters) {
            this.mNumPendingFilters = numFilters;
            this.mCallbacks = new HashSet();
            this.mFilterResult = 0;
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public void onFilterComplete(int result) {
            synchronized (this.mFilterLock) {
                this.mNumPendingFilters--;
                combine(result);
                if (this.mNumPendingFilters == 0) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        CarrierServicesSmsFilter.this.mCarrierServicesSmsFilterCallback.onFilterComplete(this.mFilterResult);
                        Binder.restoreCallingIdentity(token);
                        CarrierServicesSmsFilter.this.log("onFilterComplete: called successfully with result = " + result);
                        CarrierServicesSmsFilter.this.mCallbackTimeoutHandler.removeMessages(1);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                } else {
                    CarrierServicesSmsFilter.this.log("onFilterComplete: waiting for pending filters " + this.mNumPendingFilters);
                }
            }
        }

        private void combine(int result) {
            this.mFilterResult |= result;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addToCallbacks(CarrierSmsFilterCallback callback) {
            this.mCallbacks.add(callback);
        }
    }

    /* access modifiers changed from: protected */
    public final class CallbackTimeoutHandler extends Handler {
        private static final boolean DBG = true;

        protected CallbackTimeoutHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
            carrierServicesSmsFilter.log("CallbackTimeoutHandler handleMessage(" + msg.what + ")");
            if (msg.what == 1) {
                CarrierServicesSmsFilter.this.mLocalLog.log("CarrierServicesSmsFilter: onFilterComplete timeout: not called before 600000 milliseconds.");
                handleFilterCallbacksTimeout();
            }
        }

        private void handleFilterCallbacksTimeout() {
            for (CarrierSmsFilterCallback callback : CarrierServicesSmsFilter.this.mFilterAggregator.mCallbacks) {
                CarrierServicesSmsFilter.this.log("handleFilterCallbacksTimeout: calling onFilterComplete");
                callback.onFilterComplete(0);
            }
        }
    }
}

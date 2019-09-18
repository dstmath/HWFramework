package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.service.carrier.ICarrierMessagingCallback;
import android.service.carrier.ICarrierMessagingService;
import android.service.carrier.MessagePdu;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.Rlog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CarrierServicesSmsFilter {
    protected static final boolean DBG = true;
    /* access modifiers changed from: private */
    public final CarrierServicesSmsFilterCallbackInterface mCarrierServicesSmsFilterCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final int mDestPort;
    private final String mLogTag;
    private final String mPduFormat;
    private final byte[][] mPdus;
    /* access modifiers changed from: private */
    public final Phone mPhone;

    @VisibleForTesting
    public interface CarrierServicesSmsFilterCallbackInterface {
        void onFilterComplete(int i);
    }

    private final class CarrierSmsFilter extends CarrierMessagingServiceManager {
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
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                carrierMessagingService.filterSms(new MessagePdu(Arrays.asList(this.mPdus)), this.mSmsFormat, this.mDestPort, CarrierServicesSmsFilter.this.mPhone.getSubId(), this.mSmsFilterCallback);
            } catch (RemoteException e) {
                CarrierServicesSmsFilter carrierServicesSmsFilter = CarrierServicesSmsFilter.this;
                carrierServicesSmsFilter.loge("Exception filtering the SMS: " + e);
                this.mSmsFilterCallback.onFilterComplete(0);
            }
        }
    }

    private final class CarrierSmsFilterCallback extends ICarrierMessagingCallback.Stub {
        private final CarrierMessagingServiceManager mCarrierMessagingServiceManager;
        private final FilterAggregator mFilterAggregator;

        CarrierSmsFilterCallback(FilterAggregator filterAggregator, CarrierMessagingServiceManager carrierMessagingServiceManager) {
            this.mFilterAggregator = filterAggregator;
            this.mCarrierMessagingServiceManager = carrierMessagingServiceManager;
        }

        public void onFilterComplete(int result) {
            this.mCarrierMessagingServiceManager.disposeConnection(CarrierServicesSmsFilter.this.mContext);
            this.mFilterAggregator.onFilterComplete(result);
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

    private final class FilterAggregator {
        private final Object mFilterLock = new Object();
        private int mFilterResult;
        private int mNumPendingFilters;

        FilterAggregator(int numFilters) {
            this.mNumPendingFilters = numFilters;
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
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                }
            }
        }

        private void combine(int result) {
            this.mFilterResult |= result;
        }
    }

    @VisibleForTesting
    public CarrierServicesSmsFilter(Context context, Phone phone, byte[][] pdus, int destPort, String pduFormat, CarrierServicesSmsFilterCallbackInterface carrierServicesSmsFilterCallback, String logTag) {
        this.mContext = context;
        this.mPhone = phone;
        this.mPdus = pdus;
        this.mDestPort = destPort;
        this.mPduFormat = pduFormat;
        this.mCarrierServicesSmsFilterCallback = carrierServicesSmsFilterCallback;
        this.mLogTag = logTag;
    }

    @VisibleForTesting
    public boolean filter() {
        Optional<String> carrierAppForFiltering = getCarrierAppPackageForFiltering();
        List<String> smsFilterPackages = new ArrayList<>();
        if (carrierAppForFiltering.isPresent()) {
            smsFilterPackages.add(carrierAppForFiltering.get());
        }
        String carrierImsPackage = CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierImsPackage != null) {
            smsFilterPackages.add(carrierImsPackage);
        }
        FilterAggregator filterAggregator = new FilterAggregator(smsFilterPackages.size());
        for (String smsFilterPackage : smsFilterPackages) {
            filterWithPackage(smsFilterPackage, filterAggregator);
        }
        return smsFilterPackages.size() > 0;
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
        smsFilter.filterSms(packageName, new CarrierSmsFilterCallback(filterAggregator, smsFilter));
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

    private void log(String message) {
        Rlog.d(this.mLogTag, message);
    }

    /* access modifiers changed from: private */
    public void loge(String message) {
        Rlog.e(this.mLogTag, message);
    }

    /* access modifiers changed from: private */
    public void logv(String message) {
        Rlog.e(this.mLogTag, message);
    }
}

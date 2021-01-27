package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.WorkSource;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocaleTracker extends Handler {
    private static final long CELL_INFO_MAX_DELAY_MS = 600000;
    private static final long CELL_INFO_MIN_DELAY_MS = 2000;
    private static final long CELL_INFO_PERIODIC_POLLING_DELAY_MS = 600000;
    private static final boolean DBG = true;
    private static final int EVENT_REQUEST_CELL_INFO = 1;
    private static final int EVENT_RESPONSE_CELL_INFO = 5;
    private static final int EVENT_SERVICE_STATE_CHANGED = 2;
    private static final int EVENT_SIM_STATE_CHANGED = 3;
    private static final int EVENT_UNSOL_CELL_INFO = 4;
    private static final int MAX_FAIL_COUNT = 30;
    private static final String TAG = LocaleTracker.class.getSimpleName();
    private static final String TEST_NETWORK_MCCMNC = "00101";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.LocaleTracker.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.SIM_CARD_STATE_CHANGED".equals(intent.getAction()) && intent.getIntExtra("phone", 0) == LocaleTracker.this.mPhone.getPhoneId()) {
                LocaleTracker.this.obtainMessage(3, intent.getIntExtra("android.telephony.extra.SIM_STATE", 0), 0).sendToTarget();
            }
        }
    };
    private List<CellInfo> mCellInfoList;
    private String mCurrentCountryIso;
    private int mFailCellInfoCount;
    private boolean mIsTracking = false;
    private int mLastServiceState = 3;
    private final LocalLog mLocalLog = new LocalLog(10);
    private final NitzStateMachine mNitzStateMachine;
    private String mOperatorNumeric;
    private final Phone mPhone;
    private int mSimState;

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        int i = msg.what;
        boolean z = true;
        if (i == 1) {
            this.mPhone.requestCellInfoUpdate(getWorkSource(), obtainMessage(5));
        } else if (i == 2) {
            onServiceStateChanged((ServiceState) ((AsyncResult) msg.obj).result);
        } else if (i == 3) {
            onSimCardStateChanged(msg.arg1);
        } else if (i == 4) {
            processCellInfo((AsyncResult) msg.obj);
            List<CellInfo> list = this.mCellInfoList;
            if (list != null && list.size() > 0) {
                requestNextCellInfo(true);
            }
        } else if (i == 5) {
            processCellInfo((AsyncResult) msg.obj);
            List<CellInfo> list2 = this.mCellInfoList;
            if (list2 == null || list2.size() <= 0) {
                z = false;
            }
            requestNextCellInfo(z);
        } else {
            throw new IllegalStateException("Unexpected message arrives. msg = " + msg.what);
        }
    }

    public LocaleTracker(Phone phone, NitzStateMachine nitzStateMachine, Looper looper) {
        super(looper);
        this.mPhone = phone;
        this.mNitzStateMachine = nitzStateMachine;
        this.mSimState = 0;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.SIM_CARD_STATE_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mBroadcastReceiver, filter);
        this.mPhone.registerForServiceStateChanged(this, 2, null);
        this.mPhone.registerForCellInfo(this, 4, null);
    }

    public String getCurrentCountry() {
        String str = this.mCurrentCountryIso;
        return str != null ? str : PhoneConfigurationManager.SSSS;
    }

    private String getMccFromCellInfo() {
        String selectedMcc = null;
        if (this.mCellInfoList != null) {
            Map<String, Integer> countryCodeMap = new HashMap<>();
            int maxCount = 0;
            for (CellInfo cellInfo : this.mCellInfoList) {
                String mcc = null;
                if (cellInfo instanceof CellInfoGsm) {
                    mcc = ((CellInfoGsm) cellInfo).getCellIdentity().getMccString();
                } else if (cellInfo instanceof CellInfoLte) {
                    mcc = ((CellInfoLte) cellInfo).getCellIdentity().getMccString();
                } else if (cellInfo instanceof CellInfoWcdma) {
                    mcc = ((CellInfoWcdma) cellInfo).getCellIdentity().getMccString();
                }
                if (mcc != null) {
                    int count = 1;
                    if (countryCodeMap.containsKey(mcc)) {
                        count = countryCodeMap.get(mcc).intValue() + 1;
                    }
                    countryCodeMap.put(mcc, Integer.valueOf(count));
                    if (count > maxCount) {
                        maxCount = count;
                        selectedMcc = mcc;
                    }
                }
            }
        }
        return selectedMcc;
    }

    private synchronized void onSimCardStateChanged(int state) {
        this.mSimState = state;
        updateLocale();
        updateTrackingStatus();
    }

    private void onServiceStateChanged(ServiceState serviceState) {
        this.mLastServiceState = serviceState.getState();
        updateLocale();
        updateTrackingStatus();
    }

    public void updateOperatorNumeric(String operatorNumeric) {
        if (!Objects.equals(this.mOperatorNumeric, operatorNumeric)) {
            String msg = "Operator numeric changes to \"" + operatorNumeric + "\"";
            log(msg);
            this.mLocalLog.log(msg);
            this.mOperatorNumeric = operatorNumeric;
            updateLocale();
        }
    }

    private void processCellInfo(AsyncResult ar) {
        if (ar == null || ar.exception != null) {
            this.mCellInfoList = null;
            return;
        }
        List<CellInfo> cellInfoList = (List) ar.result;
        log("processCellInfo: cell info=" + cellInfoList);
        this.mCellInfoList = cellInfoList;
        updateLocale();
    }

    private void requestNextCellInfo(boolean succeeded) {
        if (this.mIsTracking) {
            removeMessages(1);
            if (succeeded) {
                resetCellInfoRetry();
                removeMessages(4);
                removeMessages(5);
                sendMessageDelayed(obtainMessage(1), 600000);
                return;
            }
            int i = this.mFailCellInfoCount + 1;
            this.mFailCellInfoCount = i;
            long delay = getCellInfoDelayTime(i);
            log("Can't get cell info. Try again in " + (delay / 1000) + " secs.");
            sendMessageDelayed(obtainMessage(1), delay);
        }
    }

    @VisibleForTesting
    public static long getCellInfoDelayTime(int failCount) {
        return Math.min(Math.max(((long) Math.pow(2.0d, (double) (Math.min(failCount, 30) - 1))) * CELL_INFO_MIN_DELAY_MS, (long) CELL_INFO_MIN_DELAY_MS), 600000L);
    }

    private void resetCellInfoRetry() {
        this.mFailCellInfoCount = 0;
        removeMessages(1);
    }

    private void updateTrackingStatus() {
        int i;
        boolean shouldTrackLocale = true;
        if ((this.mSimState != 1 && !TextUtils.isEmpty(this.mOperatorNumeric)) || !((i = this.mLastServiceState) == 1 || i == 2)) {
            shouldTrackLocale = false;
        }
        if (shouldTrackLocale) {
            startTracking();
        } else {
            stopTracking();
        }
    }

    private void stopTracking() {
        if (this.mIsTracking) {
            this.mIsTracking = false;
            log("Stopping LocaleTracker");
            this.mLocalLog.log("Stopping LocaleTracker");
            this.mCellInfoList = null;
            resetCellInfoRetry();
        }
    }

    private void startTracking() {
        if (!this.mIsTracking) {
            this.mLocalLog.log("Starting LocaleTracker");
            log("Starting LocaleTracker");
            this.mIsTracking = true;
            sendMessage(obtainMessage(1));
        }
    }

    private synchronized void updateLocale() {
        String mcc = null;
        String countryIso = PhoneConfigurationManager.SSSS;
        if (!TextUtils.isEmpty(this.mOperatorNumeric)) {
            try {
                mcc = this.mOperatorNumeric.substring(0, 3);
                countryIso = MccTable.countryCodeForMcc(mcc);
            } catch (StringIndexOutOfBoundsException ex) {
                loge("updateLocale: Can't get country from operator numeric. mcc = " + mcc + ". ex=" + ex);
            }
        }
        if (TextUtils.isEmpty(countryIso)) {
            mcc = getMccFromCellInfo();
            if (!TextUtils.isEmpty(mcc)) {
                countryIso = MccTable.countryCodeForMcc(mcc);
            }
        }
        log("updateLocale: mcc = " + mcc + ", country = " + countryIso);
        boolean countryChanged = false;
        if (!Objects.equals(countryIso, this.mCurrentCountryIso)) {
            String msg = "updateLocale: Change the current country to \"" + countryIso + "\", mcc = " + mcc + ", mCellInfoList = " + this.mCellInfoList;
            log(msg);
            this.mLocalLog.log(msg);
            this.mCurrentCountryIso = countryIso;
            TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "gsm.operator.iso-country", this.mCurrentCountryIso);
            WifiManager wifiManager = (WifiManager) this.mPhone.getContext().getSystemService("wifi");
            if (wifiManager != null) {
                wifiManager.setCountryCode(countryIso);
            } else {
                log("Wifi manager is not available.");
                this.mLocalLog.log("Wifi manager is not available.");
            }
            Intent intent = new Intent("android.telephony.action.NETWORK_COUNTRY_CHANGED");
            intent.putExtra("android.telephony.extra.NETWORK_COUNTRY", countryIso);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendBroadcast(intent);
            countryChanged = true;
        }
        if (!TextUtils.isEmpty(countryIso) || TEST_NETWORK_MCCMNC.equals(this.mOperatorNumeric)) {
            this.mNitzStateMachine.handleNetworkCountryCodeSet(countryChanged);
        } else {
            this.mNitzStateMachine.handleNetworkCountryCodeUnavailable();
        }
    }

    public boolean isTracking() {
        return this.mIsTracking;
    }

    private void log(String msg) {
        Rlog.i(TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        pw.println("LocaleTracker:");
        ipw.increaseIndent();
        ipw.println("mIsTracking = " + this.mIsTracking);
        ipw.println("mOperatorNumeric = " + this.mOperatorNumeric);
        ipw.println("mSimState = " + this.mSimState);
        ipw.println("mCellInfoList = " + this.mCellInfoList);
        ipw.println("mCurrentCountryIso = " + this.mCurrentCountryIso);
        ipw.println("mFailCellInfoCount = " + this.mFailCellInfoCount);
        ipw.println("Local logs:");
        ipw.increaseIndent();
        this.mLocalLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.decreaseIndent();
        ipw.flush();
    }

    private WorkSource getWorkSource() {
        int uid = Binder.getCallingUid();
        return new WorkSource(uid, this.mPhone.getContext().getPackageManager().getNameForUid(uid));
    }
}

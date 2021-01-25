package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.util.Arrays;

public class RatRatcheter {
    private static final String LOG_TAG = "RilRatcheter";
    private static final boolean VDBG = false;
    private BroadcastReceiver mConfigChangedReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.RatRatcheter.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                RatRatcheter.this.resetRatFamilyMap();
            }
        }
    };
    private boolean mDataRatchetEnabled = true;
    private final Phone mPhone;
    private final SparseArray<SparseIntArray> mRatFamilyMap = new SparseArray<>();
    private boolean mVoiceRatchetEnabled = true;

    public static boolean updateBandwidths(int[] bandwidths, ServiceState serviceState) {
        if (bandwidths == null || Arrays.stream(bandwidths).sum() <= Arrays.stream(serviceState.getCellBandwidths()).sum()) {
            return false;
        }
        serviceState.setCellBandwidths(bandwidths);
        return true;
    }

    public RatRatcheter(Phone phone) {
        this.mPhone = phone;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        phone.getContext().registerReceiverAsUser(this.mConfigChangedReceiver, UserHandle.ALL, intentFilter, null, null);
        resetRatFamilyMap();
    }

    private int ratchetRat(int oldNetworkType, int newNetworkType) {
        int oldRat = ServiceState.networkTypeToRilRadioTechnology(oldNetworkType);
        int newRat = ServiceState.networkTypeToRilRadioTechnology(newNetworkType);
        synchronized (this.mRatFamilyMap) {
            SparseIntArray oldFamily = this.mRatFamilyMap.get(oldRat);
            if (oldFamily == null) {
                return newNetworkType;
            }
            SparseIntArray newFamily = this.mRatFamilyMap.get(newRat);
            if (newFamily != oldFamily) {
                return newNetworkType;
            }
            return ServiceState.rilRadioTechnologyToNetworkType(newFamily.get(oldRat, -1) > newFamily.get(newRat, -1) ? oldRat : newRat);
        }
    }

    public void ratchet(ServiceState oldSS, ServiceState newSS, boolean locationChange) {
        if (!locationChange && isSameRatFamily(oldSS, newSS)) {
            updateBandwidths(oldSS.getCellBandwidths(), newSS);
        }
        boolean z = false;
        if (locationChange) {
            this.mVoiceRatchetEnabled = false;
            this.mDataRatchetEnabled = false;
            return;
        }
        boolean newUsingCA = false;
        if (newSS.getRilDataRadioTechnology() == 14 || newSS.getRilDataRadioTechnology() == 19) {
            if (oldSS.isUsingCarrierAggregation() || newSS.isUsingCarrierAggregation() || newSS.getCellBandwidths().length > 1) {
                z = true;
            }
            newUsingCA = z;
        }
        NetworkRegistrationInfo oldCsNri = oldSS.getNetworkRegistrationInfoHw(1, 1);
        NetworkRegistrationInfo newCsNri = newSS.getNetworkRegistrationInfoHw(1, 1);
        if (this.mVoiceRatchetEnabled) {
            newCsNri.setAccessNetworkTechnology(ratchetRat(oldCsNri.getAccessNetworkTechnology(), newCsNri.getAccessNetworkTechnology()));
            newSS.addNetworkRegistrationInfo(newCsNri);
        } else if (oldCsNri.getAccessNetworkTechnology() != newCsNri.getAccessNetworkTechnology()) {
            this.mVoiceRatchetEnabled = true;
        }
        NetworkRegistrationInfo oldPsNri = oldSS.getNetworkRegistrationInfoHw(2, 1);
        NetworkRegistrationInfo newPsNri = newSS.getNetworkRegistrationInfoHw(2, 1);
        if (this.mDataRatchetEnabled) {
            newPsNri.setAccessNetworkTechnology(ratchetRat(oldPsNri.getAccessNetworkTechnology(), newPsNri.getAccessNetworkTechnology()));
            newSS.addNetworkRegistrationInfo(newPsNri);
        } else if (oldPsNri.getAccessNetworkTechnology() != newPsNri.getAccessNetworkTechnology()) {
            this.mDataRatchetEnabled = true;
        }
        newSS.setIsUsingCarrierAggregation(newUsingCA);
    }

    private boolean isSameRatFamily(ServiceState ss1, ServiceState ss2) {
        synchronized (this.mRatFamilyMap) {
            boolean z = true;
            int dataRat1 = ServiceState.networkTypeToRilRadioTechnology(ss1.getNetworkRegistrationInfoHw(2, 1).getAccessNetworkTechnology());
            int dataRat2 = ServiceState.networkTypeToRilRadioTechnology(ss2.getNetworkRegistrationInfoHw(2, 1).getAccessNetworkTechnology());
            if (dataRat1 == dataRat2) {
                return true;
            }
            if (this.mRatFamilyMap.get(dataRat1) == null) {
                return false;
            }
            if (this.mRatFamilyMap.get(dataRat1) != this.mRatFamilyMap.get(dataRat2)) {
                z = false;
            }
            return z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetRatFamilyMap() {
        synchronized (this.mRatFamilyMap) {
            this.mRatFamilyMap.clear();
            CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle b = configManager.getConfig();
                if (b != null) {
                    String[] ratFamilies = b.getStringArray("ratchet_rat_families");
                    if (ratFamilies != null) {
                        for (String ratFamily : ratFamilies) {
                            String[] rats = ratFamily.split(",");
                            if (rats.length >= 2) {
                                SparseIntArray currentFamily = new SparseIntArray(rats.length);
                                int length = rats.length;
                                int pos = 0;
                                int pos2 = 0;
                                while (true) {
                                    if (pos2 >= length) {
                                        break;
                                    }
                                    String ratString = rats[pos2];
                                    try {
                                        int ratInt = Integer.parseInt(ratString.trim());
                                        if (this.mRatFamilyMap.get(ratInt) != null) {
                                            Rlog.e(LOG_TAG, "RAT listed twice: " + ratString);
                                            break;
                                        }
                                        currentFamily.put(ratInt, pos);
                                        this.mRatFamilyMap.put(ratInt, currentFamily);
                                        pos2++;
                                        pos++;
                                    } catch (NumberFormatException e) {
                                        Rlog.e(LOG_TAG, "NumberFormatException on " + ratString);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

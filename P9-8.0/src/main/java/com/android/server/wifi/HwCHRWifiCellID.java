package com.android.server.wifi;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.huawei.device.connectivitychrlog.CSubCellID;

public class HwCHRWifiCellID {
    private static final int LENGTH_OF_MCC = 3;
    private static final String TAG = "HwCHRWifiCellID";
    private static HwCHRWifiCellID mCellId = null;
    private CdmaCellLocationInfo[] mCdmaCellLocationInfo = null;
    private Context mContext;
    private int mCurrentCID = 0;
    private int mCurrentLAC = 0;
    private String mCurrentMCC = "";
    private String mCurrentMNC = "";
    private GsmCellLocationInfo[] mGsmCellLocationInfo = null;
    private ServiceState[] mNewServiceState = null;
    private int mPhoneCount;
    private PhoneStateListener[] mPhoneStateListeners;
    private RegInfo[] mRegInfo = null;
    private TelephonyManager mTelephonyManager;

    private class CdmaCellLocationInfo {
        private int mBaseStationId;
        private int mNetworkId;
        private int mSystemId;

        /* synthetic */ CdmaCellLocationInfo(HwCHRWifiCellID this$0, CdmaCellLocationInfo -this1) {
            this();
        }

        private CdmaCellLocationInfo() {
            this.mBaseStationId = -1;
            this.mSystemId = -1;
            this.mNetworkId = -1;
        }

        public void update(CdmaCellLocation cellLocation) {
            if (-1 != cellLocation.getBaseStationId()) {
                this.mBaseStationId = cellLocation.getBaseStationId();
            }
            if (-1 != cellLocation.getSystemId()) {
                this.mSystemId = cellLocation.getSystemId();
            }
            if (-1 != cellLocation.getNetworkId()) {
                this.mNetworkId = cellLocation.getNetworkId();
            }
            HwCHRWifiCellID.this.mCurrentLAC = this.mNetworkId;
            HwCHRWifiCellID.this.mCurrentCID = this.mBaseStationId;
        }

        public int getBaseStationId() {
            return this.mBaseStationId;
        }

        public int getSystemId() {
            return this.mSystemId;
        }

        public int getNetworkId() {
            return this.mNetworkId;
        }

        public String toString() {
            return "CdmaCellLocationInfo:mBaseStationId: " + this.mBaseStationId + "mSystemId: " + this.mSystemId + "mNetworkId: " + this.mNetworkId;
        }
    }

    private class GsmCellLocationInfo {
        private int mCid;
        private int mLac;

        /* synthetic */ GsmCellLocationInfo(HwCHRWifiCellID this$0, GsmCellLocationInfo -this1) {
            this();
        }

        private GsmCellLocationInfo() {
            this.mLac = -1;
            this.mCid = -1;
        }

        public void update(GsmCellLocation cellLocation) {
            if (-1 != cellLocation.getLac()) {
                this.mLac = cellLocation.getLac();
            }
            if (-1 != cellLocation.getCid()) {
                this.mCid = cellLocation.getCid();
            }
            HwCHRWifiCellID.this.mCurrentLAC = this.mLac;
            HwCHRWifiCellID.this.mCurrentCID = this.mCid;
        }

        public int getLac() {
            return this.mLac;
        }

        public int getCid() {
            return this.mCid;
        }

        public String toString() {
            return "GsmCellLocationInfo:mLac: " + this.mLac + "mCid: " + this.mCid;
        }
    }

    private class PhoneStateListenerWrapper extends PhoneStateListener {
        protected int mSubscription;

        public PhoneStateListenerWrapper(int subscription) {
            this.mSubscription = subscription;
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state == null) {
                Log.d(HwCHRWifiCellID.TAG, "onServiceStateChanged, ss is null, return");
                return;
            }
            HwCHRWifiCellID.this.mNewServiceState[this.mSubscription] = state;
            HwCHRWifiCellID.this.mRegInfo[this.mSubscription].update(state);
            Log.d(HwCHRWifiCellID.TAG, "onServiceStateChanged, state is " + state.toString());
        }

        public void onCellLocationChanged(CellLocation location) {
            if (location == null) {
                Log.e(HwCHRWifiCellID.TAG, "onCellLocationChanged: location is null,return");
                return;
            }
            if (HwCHRWifiCellID.this.isNeedToSaveCellAndSignalInfo(this.mSubscription)) {
                HwCHRWifiCellID.this.updateCellLocationInfo(location, this.mSubscription);
            }
        }
    }

    private class RegInfo {
        private String mMcc;
        private String mMnc;
        private String mOperatorNumeric;

        /* synthetic */ RegInfo(HwCHRWifiCellID this$0, RegInfo -this1) {
            this();
        }

        private RegInfo() {
            this.mOperatorNumeric = null;
            this.mMcc = null;
            this.mMnc = null;
        }

        public String getRegOperatorNumeric() {
            return this.mOperatorNumeric;
        }

        public void update(ServiceState ss) {
            Log.d(HwCHRWifiCellID.TAG, ss.toString());
            String operatorNum = ss.getOperatorNumeric();
            if (operatorNum == null || operatorNum.length() <= 3) {
                Log.w(HwCHRWifiCellID.TAG, "onServiceStateChanged, operatorNumeric: " + this.mOperatorNumeric);
                return;
            }
            this.mOperatorNumeric = operatorNum;
            this.mMcc = this.mOperatorNumeric.substring(0, 3);
            this.mMnc = this.mOperatorNumeric.substring(3);
            HwCHRWifiCellID.this.mCurrentMCC = this.mMcc;
            HwCHRWifiCellID.this.mCurrentMNC = this.mMnc;
        }

        public String toString() {
            return "RegInfo:mOperatorNumeric: " + this.mOperatorNumeric + "mMcc: " + this.mMcc + "mMnc: " + this.mMnc;
        }
    }

    private HwCHRWifiCellID(Context context, int PhoneCount) {
        this.mContext = context;
        this.mPhoneCount = PhoneCount;
        this.mNewServiceState = new ServiceState[this.mPhoneCount];
        this.mRegInfo = new RegInfo[this.mPhoneCount];
        this.mPhoneStateListeners = new PhoneStateListener[this.mPhoneCount];
        this.mGsmCellLocationInfo = new GsmCellLocationInfo[this.mPhoneCount];
        this.mCdmaCellLocationInfo = new CdmaCellLocationInfo[this.mPhoneCount];
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mRegInfo[i] = new RegInfo(this, null);
            this.mNewServiceState[i] = new ServiceState();
            this.mGsmCellLocationInfo[i] = new GsmCellLocationInfo(this, null);
            this.mCdmaCellLocationInfo[i] = new CdmaCellLocationInfo(this, null);
        }
        startPhoneListener();
    }

    public static HwCHRWifiCellID make(Context context, int PhoneCount) {
        if (mCellId != null) {
            return mCellId;
        }
        mCellId = new HwCHRWifiCellID(context, PhoneCount);
        return mCellId;
    }

    private void startPhoneListener() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mPhoneStateListeners[i] = new PhoneStateListenerWrapper(i);
            this.mTelephonyManager.listen(this.mPhoneStateListeners[i], 17);
        }
    }

    private void updateCellLocationInfo(CellLocation location, int sub) {
        if (isSubValid(sub)) {
            if (location instanceof GsmCellLocation) {
                this.mGsmCellLocationInfo[sub].update((GsmCellLocation) location);
            } else if (location instanceof CdmaCellLocation) {
                this.mCdmaCellLocationInfo[sub].update((CdmaCellLocation) location);
            } else {
                Log.e(TAG, "updateCellLocationInfo, location type is wrong, location name=" + location.getClass().getName());
            }
        }
    }

    private boolean isSubValid(int sub) {
        if (sub >= 0 && sub < this.mPhoneCount) {
            return true;
        }
        Log.e(TAG, "invalid sub = " + sub + ", mPhoneNum = " + this.mPhoneCount);
        return false;
    }

    private boolean isNeedToSaveCellAndSignalInfo(int sub) {
        boolean z = true;
        if (this.mNewServiceState == null) {
            Log.e(TAG, "isNeedToSaveCellAndSignalInfo: state is null,return false");
            return false;
        } else if (!isSubValid(sub)) {
            return false;
        } else {
            if (!(this.mNewServiceState[sub].getVoiceRegState() == 0 || this.mNewServiceState[sub].getDataRegState() == 0)) {
                z = false;
            }
            return z;
        }
    }

    public CSubCellID getCellIDCHR() {
        CSubCellID result = new CSubCellID();
        result.iCID.setValue(this.mCurrentCID);
        result.iLAC.setValue(this.mCurrentLAC);
        result.strMCC.setValue(this.mCurrentMCC);
        result.strMNC.setValue(this.mCurrentMNC);
        return result;
    }
}

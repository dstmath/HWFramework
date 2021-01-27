package com.huawei.internal.telephony.smartnet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.WorkSource;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.HwServiceStateTrackerEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.WorkSourceEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import java.util.List;

public class HwSmartNetStateListener extends Handler {
    private static final String ACTION_SIGNAL_STRENGTH_CHANGED = "android.intent.action.SIG_STR";
    private static final int INVALID = -1;
    private static final Object LOCK = new Object();
    private static final String TAG = "HwSmartNetStateListener";
    private static final int TIME_REQUEST_CELLSENSOR_MIN_MS = 5000;
    private static HwSmartNetStateListener sInstance;
    private Context mContext = null;
    private String[] mIccIdHashs = new String[HwSmartNetConstants.SIM_NUM];
    private String[] mIccIds = new String[HwSmartNetConstants.SIM_NUM];
    private LastCellInfo[] mLastCellInfos = new LastCellInfo[HwSmartNetConstants.SIM_NUM];
    private long mLastRequestCellSensorTime;
    private LastServiceState[] mLastServiceStates = new LastServiceState[HwSmartNetConstants.SIM_NUM];
    private LastSignalStrength[] mLastSignalStrengths = new LastSignalStrength[HwSmartNetConstants.SIM_NUM];
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.huawei.internal.telephony.smartnet.HwSmartNetStateListener.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwSmartNetStateListener.this.logi("intent is null, return");
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                int phoneId = intent.getIntExtra("phone", -1000);
                String simState = intent.getStringExtra("ss");
                if (HwSmartNetCommon.isValidSlotId(phoneId)) {
                    if (!"IMSI".equals(simState) || HwSmartNetStateListener.this.mSimStatus[phoneId] != 10) {
                        HwSmartNetStateListener.this.mSimStatus[phoneId] = ((TelephonyManager) HwSmartNetStateListener.this.mContext.getSystemService("phone")).getSimState(phoneId);
                    }
                    if ("LOADED".equals(simState)) {
                        HwSmartNetStateListener.this.mSimStatus[phoneId] = 10;
                        UiccControllerExt uiccController = UiccControllerExt.getInstance();
                        HwSmartNetStateListener.this.mIccIds[phoneId] = uiccController.getUiccCard(phoneId) != null ? uiccController.getUiccCard(phoneId).getIccId() : null;
                        HwSmartNetStateListener.this.mIccIdHashs[phoneId] = HwSmartNetCommon.calcHash(HwSmartNetStateListener.this.mIccIds[phoneId]);
                    } else if (HwSmartNetStateListener.this.mSimStatus[phoneId] != 10) {
                        HwSmartNetStateListener.this.mIccIds[phoneId] = BuildConfig.FLAVOR;
                        HwSmartNetStateListener.this.mIccIdHashs[phoneId] = BuildConfig.FLAVOR;
                    }
                    HwSmartNetStateListener hwSmartNetStateListener = HwSmartNetStateListener.this;
                    hwSmartNetStateListener.logi("ACTION_SIM_STATE_CHANGED slotId:" + phoneId + " simState:" + simState + " mSimStatus:" + HwSmartNetStateListener.this.mSimStatus[phoneId]);
                }
            }
        }
    };
    private Message mRequestCellRspMsg;
    private HwServiceStateTrackerEx.HwServiceStateListener mServiceStateListener = new RealServiceStateListener();
    private int[] mSimStatus = new int[HwSmartNetConstants.SIM_NUM];
    private Handler mStateHandler = null;
    private WorkSource mWorkSource;

    private HwSmartNetStateListener(Context context, Handler stateHandler) {
        this.mContext = context;
        this.mStateHandler = stateHandler;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mWorkSource = new WorkSourceEx(this.mContext.getApplicationInfo().uid, this.mContext.getPackageName());
        logi("create HwSmartNetStateListener");
    }

    public static HwSmartNetStateListener make(Context context, Handler stateHandler) {
        HwSmartNetStateListener hwSmartNetStateListener;
        synchronized (LOCK) {
            sInstance = new HwSmartNetStateListener(context, stateHandler);
            hwSmartNetStateListener = sInstance;
        }
        return hwSmartNetStateListener;
    }

    public static HwSmartNetStateListener getInstance() {
        HwSmartNetStateListener hwSmartNetStateListener;
        synchronized (LOCK) {
            hwSmartNetStateListener = sInstance;
        }
        return hwSmartNetStateListener;
    }

    public void registerSampleListener() {
        for (int i = 0; i < HwSmartNetConstants.SIM_NUM; i++) {
            PhoneFactoryExt.getPhone(i).registerForCellInfo(this, 203, Integer.valueOf(i));
            HwServiceStateTrackerEx.getInstance(i).addHwServiceStateListener(this.mServiceStateListener);
            this.mLastCellInfos[i] = new LastCellInfo();
            this.mLastServiceStates[i] = new LastServiceState();
            this.mLastSignalStrengths[i] = new LastSignalStrength();
        }
    }

    public void unRegisterSampleListener() {
        for (int i = 0; i < HwSmartNetConstants.SIM_NUM; i++) {
            PhoneFactoryExt.getPhone(i).unregisterForCellInfo(this);
            HwServiceStateTrackerEx.getInstance(i).removeHwServiceStateListener(this.mServiceStateListener);
        }
    }

    private void requestCellInfoUpdate(int phoneId, Message response) {
        if (HwSmartNetCommon.isValidSlotId(phoneId)) {
            PhoneFactoryExt.getPhone(phoneId).requestCellInfoUpdate(this.mWorkSource, obtainMessage(HwSmartNetConstants.EVENT_REQUEST_CELL_INFO_DONE, phoneId, -1, response));
            return;
        }
        response.arg1 = phoneId;
        response.sendToTarget();
    }

    public void requestCellSensor(int phoneId, Message response) {
        if (response != null) {
            this.mLastRequestCellSensorTime = System.currentTimeMillis();
            if (this.mLastRequestCellSensorTime - this.mLastCellInfos[phoneId].getLastDate() < 5000) {
                response.obj = getCellSensorByCellinfo(phoneId);
                response.sendToTarget();
            } else if (this.mLastRequestCellSensorTime - this.mLastServiceStates[phoneId].lastDate < 5000) {
                response.obj = getCellSensorByServiceState(phoneId);
                response.sendToTarget();
            } else {
                requestCellInfoUpdate(phoneId, response);
            }
        }
    }

    private CellSensor getCellSensorByCellinfo(int phoneId) {
        CellSensor cellSensor = new CellSensor();
        buildCellSensorCommonInfo(cellSensor, phoneId);
        buildCellSensorByCellInfoList(cellSensor, phoneId);
        buildCellSensorByServiceState(cellSensor, phoneId);
        buildCellSensorBySignalStrength(cellSensor, phoneId);
        logi("getCellSensorByCellinfo:" + cellSensor);
        return cellSensor;
    }

    private CellSensor getCellSensorByServiceState(int phoneId) {
        CellSensor cellSensor = new CellSensor();
        buildCellSensorCommonInfo(cellSensor, phoneId);
        buildCellSensorByServiceState(cellSensor, phoneId);
        buildMainCellInfoByServiceState(cellSensor, phoneId);
        buildCellSensorBySignalStrength(cellSensor, phoneId);
        logi("getCellSensorByServiceState:" + cellSensor.toString());
        return cellSensor;
    }

    private void buildCellSensorCommonInfo(CellSensor cellSensor, int phoneId) {
        if (HwSmartNetCommon.isValidSlotId(phoneId)) {
            cellSensor.setSlotId(phoneId);
            cellSensor.setIccid(this.mIccIds[phoneId]);
            cellSensor.setSamplingTime(this.mLastRequestCellSensorTime);
        }
    }

    private void buildCellSensorByCellInfoList(CellSensor cellSensor, int phoneId) {
        LastCellInfo[] lastCellInfoArr = this.mLastCellInfos;
        if (lastCellInfoArr[phoneId] != null && lastCellInfoArr[phoneId].getCellInfos() != null) {
            List<CellInfo> cellInfos = this.mLastCellInfos[phoneId].getCellInfos();
            if (cellInfos.size() > 0) {
                buildCellSensorCellInfo(cellSensor, cellInfos.get(0), true);
            }
            if (cellInfos.size() > 1) {
                buildCellSensorCellInfo(cellSensor, cellInfos.get(1), false);
            }
        }
    }

    private void buildCellSensorCellInfo(CellSensor cellSensor, CellInfo cellInfo, boolean isMainCellInfo) {
        if (cellSensor != null && cellInfo != null) {
            if (cellInfo instanceof CellInfoNr) {
                buildCellIndentify(cellSensor, (CellIdentityNr) ((CellInfoNr) cellInfo).getCellIdentity(), isMainCellInfo);
            } else if (cellInfo instanceof CellInfoLte) {
                buildCellIndentify(cellSensor, ((CellInfoLte) cellInfo).getCellIdentity(), isMainCellInfo);
            } else if (cellInfo instanceof CellInfoWcdma) {
                buildCellIndentify(cellSensor, ((CellInfoWcdma) cellInfo).getCellIdentity(), isMainCellInfo);
            } else if (cellInfo instanceof CellInfoGsm) {
                buildCellIndentify(cellSensor, ((CellInfoGsm) cellInfo).getCellIdentity(), isMainCellInfo);
            } else if (cellInfo instanceof CellInfoCdma) {
                buildCellIndentify(cellSensor, ((CellInfoCdma) cellInfo).getCellIdentity(), isMainCellInfo);
            } else {
                logi("cellInfo unknown instance.");
            }
        }
    }

    private void buildCellSensorByServiceState(CellSensor cellSensor, int phoneId) {
        LastServiceState[] lastServiceStateArr = this.mLastServiceStates;
        if (lastServiceStateArr[phoneId] != null && lastServiceStateArr[phoneId].getServiceState() != null) {
            ServiceState ss = this.mLastServiceStates[phoneId].getServiceState();
            cellSensor.setVoiceRegState(ServiceStateEx.getVoiceRegState(ss));
            cellSensor.setDataRegState(ServiceStateEx.getDataState(ss));
            cellSensor.setVoiceOperatorNumeric(ServiceStateEx.getVoiceOperatorNumeric(ss));
            cellSensor.setDataOperatorNumeric(ServiceStateEx.getDataOperatorNumeric(ss));
            cellSensor.setVoiceRadioTech(ServiceStateEx.getRilVoiceRadioTechnology(ss));
            cellSensor.setDataRadioTech(ServiceStateEx.getRilDataRadioTechnology(ss));
            cellSensor.setNsaState(ServiceStateEx.getNsaState(ss));
        }
    }

    private void buildMainCellInfoByServiceState(CellSensor cellSensor, int phoneId) {
        LastServiceState[] lastServiceStateArr = this.mLastServiceStates;
        if (lastServiceStateArr[phoneId] != null && lastServiceStateArr[phoneId].getServiceState() != null) {
            buildCellIndentify(cellSensor, ServiceStateEx.getNetworkRegistrationInfoHw(this.mLastServiceStates[phoneId].getServiceState(), 2, 1).getCellIdentity(), true);
        }
    }

    private void buildCellSensorBySignalStrength(CellSensor cellSensor, int phoneId) {
        LastSignalStrength[] lastSignalStrengthArr = this.mLastSignalStrengths;
        if (lastSignalStrengthArr[phoneId] != null && lastSignalStrengthArr[phoneId].getSignalStrength() != null) {
            SignalStrength signalStrength = this.mLastSignalStrengths[phoneId].getSignalStrength();
            cellSensor.setCdmaLevelHw(SignalStrengthEx.getCdmaLevel(signalStrength));
            cellSensor.setGsmLevelHw(SignalStrengthEx.getGsmLevel(signalStrength));
            cellSensor.setWcdmaLevelHw(SignalStrengthEx.getWcdmaLevel(signalStrength));
            cellSensor.setLteLevelHw(SignalStrengthEx.getLteLevel(signalStrength));
            cellSensor.setNrLevelHw(SignalStrengthEx.getNrLevel(signalStrength));
        }
    }

    private void buildCellIndentify(CellSensor cellSensor, CellIdentity cellIdentity, boolean isMainCellInfo) {
        if (cellIdentity instanceof CellIdentityNr) {
            CellIdentityNr cellIdentityNr = (CellIdentityNr) cellIdentity;
            if (isMainCellInfo) {
                cellSensor.setMainCellId(cellIdentityNr.getNci());
                cellSensor.setMainPci(cellIdentityNr.getPci());
                cellSensor.setMainTac(cellIdentityNr.getTac());
                return;
            }
            cellSensor.setNeighboringCellId(cellIdentityNr.getNci());
            cellSensor.setNeighboringPci(cellIdentityNr.getPci());
            cellSensor.setNeighboringTac(cellIdentityNr.getTac());
        } else if (cellIdentity instanceof CellIdentityLte) {
            CellIdentityLte cellIdentityLte = (CellIdentityLte) cellIdentity;
            if (isMainCellInfo) {
                cellSensor.setMainCellId((long) cellIdentityLte.getCi());
                cellSensor.setMainPci(cellIdentityLte.getPci());
                cellSensor.setMainTac(cellIdentityLte.getTac());
                return;
            }
            cellSensor.setNeighboringCellId((long) cellIdentityLte.getCi());
            cellSensor.setNeighboringPci(cellIdentityLte.getPci());
            cellSensor.setNeighboringTac(cellIdentityLte.getTac());
        } else if (cellIdentity instanceof CellIdentityWcdma) {
            CellIdentityWcdma cellIdentityWcdma = (CellIdentityWcdma) cellIdentity;
            if (isMainCellInfo) {
                cellSensor.setMainCellId((long) cellIdentityWcdma.getCid());
                cellSensor.setMainTac(cellIdentityWcdma.getLac());
                return;
            }
            cellSensor.setNeighboringCellId((long) cellIdentityWcdma.getCid());
            cellSensor.setNeighboringTac(cellIdentityWcdma.getLac());
        } else if (cellIdentity instanceof CellIdentityGsm) {
            CellIdentityGsm cellIdentityGsm = (CellIdentityGsm) cellIdentity;
            if (isMainCellInfo) {
                cellSensor.setMainCellId((long) cellIdentityGsm.getCid());
                cellSensor.setMainTac(cellIdentityGsm.getLac());
                return;
            }
            cellSensor.setNeighboringCellId((long) cellIdentityGsm.getCid());
            cellSensor.setNeighboringTac(cellIdentityGsm.getLac());
        } else if (cellIdentity instanceof CellIdentityCdma) {
            CellIdentityCdma cellIdentityCdma = (CellIdentityCdma) cellIdentity;
            if (isMainCellInfo) {
                cellSensor.setMainCellId((long) cellIdentityCdma.getBasestationId());
                cellSensor.setMainTac(cellIdentityCdma.getNetworkId());
                cellSensor.setMainPci(cellIdentityCdma.getSystemId());
                return;
            }
            cellSensor.setNeighboringCellId((long) cellIdentityCdma.getBasestationId());
            cellSensor.setNeighboringTac(cellIdentityCdma.getNetworkId());
            cellSensor.setNeighboringPci(cellIdentityCdma.getSystemId());
        } else {
            logi("buildCellIndentify unknow cellIdentity.");
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        AsyncResultEx ar;
        int i = msg.what;
        if (i == 203) {
            AsyncResultEx ar2 = AsyncResultEx.from(msg.obj);
            if (ar2 != null) {
                int phoneId = ((Integer) ar2.getUserObj()).intValue();
                List<CellInfo> cellInfos = (List) ar2.getResult();
                logi("EVENT_CELL_INFO_CHANGED of phoneId " + phoneId + " " + cellInfos);
                handleCellInfo(phoneId, cellInfos, null);
            }
        } else if (i == 601 && (ar = AsyncResultEx.from(msg.obj)) != null) {
            Message response = (Message) ar.getUserObj();
            List<CellInfo> cellInfos2 = (List) ar.getResult();
            int phoneId2 = response.arg1;
            logi("EVENT_REQUEST_CELL_INFO_DONE of phoneId " + phoneId2 + " " + cellInfos2);
            handleCellInfo(phoneId2, cellInfos2, response);
        }
    }

    private void handleCellInfo(int phoneId, List<CellInfo> cellInfos, Message response) {
        this.mLastCellInfos[phoneId].setLastDate(System.currentTimeMillis());
        this.mLastCellInfos[phoneId].setCellInfos(cellInfos);
        if (response != null) {
            CellSensor cellSensor = getCellSensorByCellinfo(phoneId);
            response.arg1 = phoneId;
            response.obj = cellSensor;
            response.sendToTarget();
            return;
        }
        this.mStateHandler.obtainMessage(203, phoneId, -1, cellInfos).sendToTarget();
    }

    public String[] getIccIdHashs() {
        return this.mIccIdHashs;
    }

    public boolean isAnySimLoaded() {
        boolean isLoaded = true;
        for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
            int[] iArr = this.mSimStatus;
            if (!(iArr[index] == 10 || iArr[index] == 1)) {
                isLoaded = false;
                logi("phone " + index + " not loaded.");
            }
        }
        return isLoaded;
    }

    public boolean isSimLoadedBySlot(int slotId) {
        if (!HwSmartNetCommon.isValidSlotId(slotId) || this.mSimStatus[slotId] != 10) {
            return false;
        }
        return true;
    }

    public boolean isAllSimLoaded() {
        for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
            if (this.mSimStatus[index] != 10) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String msg) {
        RlogEx.i(TAG, msg);
    }

    private class RealServiceStateListener implements HwServiceStateTrackerEx.HwServiceStateListener {
        private RealServiceStateListener() {
        }

        public void onServiceStateChanged(int phoneId, ServiceState ss) {
            HwSmartNetStateListener hwSmartNetStateListener = HwSmartNetStateListener.this;
            hwSmartNetStateListener.logi("onServiceStateChanged:" + phoneId + " " + ss);
            HwSmartNetStateListener.this.mLastServiceStates[phoneId].setLastDate(System.currentTimeMillis());
            HwSmartNetStateListener.this.mLastServiceStates[phoneId].setServiceState(ss);
            HwSmartNetStateListener.this.mStateHandler.obtainMessage(201, phoneId, -1, ss).sendToTarget();
        }

        public void onSignalStrengthChanged(int phoneId, SignalStrength signalStrength) {
            HwSmartNetStateListener hwSmartNetStateListener = HwSmartNetStateListener.this;
            hwSmartNetStateListener.logi("onSignalStrengthChanged:" + phoneId + " " + signalStrength);
            HwSmartNetStateListener.this.mLastSignalStrengths[phoneId].setLastDate(System.currentTimeMillis());
            HwSmartNetStateListener.this.mLastSignalStrengths[phoneId].setSignalStrength(signalStrength);
            HwSmartNetStateListener.this.mStateHandler.obtainMessage(202, phoneId, -1, signalStrength).sendToTarget();
        }
    }

    /* access modifiers changed from: package-private */
    public class LastServiceState {
        private long lastDate;
        private int phoneId;
        private ServiceState serviceState;

        LastServiceState() {
        }

        public int getPhoneId() {
            return this.phoneId;
        }

        public void setPhoneId(int phoneId2) {
            this.phoneId = phoneId2;
        }

        public long getLastDate() {
            return this.lastDate;
        }

        public void setLastDate(long lastDate2) {
            this.lastDate = lastDate2;
        }

        public ServiceState getServiceState() {
            return this.serviceState;
        }

        public void setServiceState(ServiceState serviceState2) {
            this.serviceState = serviceState2;
        }
    }

    /* access modifiers changed from: package-private */
    public class LastSignalStrength {
        private long lastDate;
        private int phoneId;
        private SignalStrength signalStrength;

        LastSignalStrength() {
        }

        public int getPhoneId() {
            return this.phoneId;
        }

        public void setPhoneId(int phoneId2) {
            this.phoneId = phoneId2;
        }

        public long getLastDate() {
            return this.lastDate;
        }

        public void setLastDate(long lastDate2) {
            this.lastDate = lastDate2;
        }

        public SignalStrength getSignalStrength() {
            return this.signalStrength;
        }

        public void setSignalStrength(SignalStrength signalStrength2) {
            this.signalStrength = signalStrength2;
        }
    }

    /* access modifiers changed from: package-private */
    public class LastCellInfo {
        private List<CellInfo> cellInfos;
        private long lastDate;
        private int phoneId;

        LastCellInfo() {
        }

        public int getPhoneId() {
            return this.phoneId;
        }

        public void setPhoneId(int phoneId2) {
            this.phoneId = phoneId2;
        }

        public long getLastDate() {
            return this.lastDate;
        }

        public void setLastDate(long lastDate2) {
            this.lastDate = lastDate2;
        }

        public List<CellInfo> getCellInfos() {
            return this.cellInfos;
        }

        public void setCellInfos(List<CellInfo> cellInfos2) {
            this.cellInfos = cellInfos2;
        }
    }
}

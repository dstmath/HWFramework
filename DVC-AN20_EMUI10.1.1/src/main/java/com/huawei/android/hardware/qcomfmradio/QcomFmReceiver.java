package com.huawei.android.hardware.qcomfmradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.BuildConfig;
import android.mtp.HwMtpConstants;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.android.hardware.fmradio.FmConfig;
import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;
import com.huawei.android.hardware.fmradio.IHwFmService;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.hardware.fmradio.common.BaseFmReceiver;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class QcomFmReceiver extends FmTransceiver implements BaseFmReceiver {
    private static final int BUF_ERT = 12;
    private static final int BUF_RTPLUS = 11;
    static final int DISABLE_LPF = 0;
    static final int ENABLE_LPF = 1;
    private static final int ENCODE_TYPE_IND = 1;
    private static final int ERT_DIR_IND = 2;
    public static final int FM_RX_RDS_GRP_AF_EBL = 8;
    public static final int FM_RX_RDS_GRP_ECC_EBL = 32;
    public static final int FM_RX_RDS_GRP_PS_EBL = 2;
    public static final int FM_RX_RDS_GRP_PS_SIMPLE_EBL = 4;
    public static final int FM_RX_RDS_GRP_PTYN_EBL = 64;
    public static final int FM_RX_RDS_GRP_RT_EBL = 1;
    public static final int FM_RX_RDS_GRP_RT_PLUS_EBL = 128;
    private static final int FM_RX_RSSI_LEVEL_STRONG = -96;
    private static final int FM_RX_RSSI_LEVEL_VERY_STRONG = -90;
    private static final int FM_RX_RSSI_LEVEL_VERY_WEAK = -105;
    private static final int FM_RX_RSSI_LEVEL_WEAK = -100;
    static final int GRP_3A = 64;
    private static final int LEN_IND = 0;
    private static final int NETWORK_TYPE_LTE_CA = 19;
    private static final int RDS_COUNT = 1024;
    private static final int RT_OR_ERT_IND = 1;
    private static final int SEARCH_MPXDCC = 0;
    private static final int SEARCH_SINR_INT = 1;
    static final int STD_BUF_SIZE = 256;
    private static final String TAG = "QCOM-BaseFmReceiver";
    private static final int TAVARUA_BUF_AF_LIST = 5;
    private static final int TAVARUA_BUF_EVENTS = 1;
    private static final int TAVARUA_BUF_MAX = 6;
    private static final int TAVARUA_BUF_PS_RDS = 3;
    private static final int TAVARUA_BUF_RAW_RDS = 4;
    private static final int TAVARUA_BUF_RT_RDS = 2;
    private static final int TAVARUA_BUF_SRCH_LIST = 0;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_IRIS_GET_SPUR_TBL = 9963822;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH = 134217736;
    private static final int V4L2_CTRL_CLASS_USER = 9961472;
    static FmRxEvCallbacks callback;
    protected static QcomFmRxEvCallbacksAdaptor mCallback;
    private static int mEnableLpf1xRtt = 64;
    private static int mEnableLpfCdma = 8;
    private static int mEnableLpfEdge = 2;
    private static int mEnableLpfEhrpd = HwMtpConstants.RESPONSE_UNDEFINED;
    private static int mEnableLpfEvdo0 = 16;
    private static int mEnableLpfEvdoA = 32;
    private static int mEnableLpfEvdoB = 2048;
    private static int mEnableLpfGprs = 1;
    private static int mEnableLpfGsm = 32768;
    private static int mEnableLpfHsdpa = 128;
    private static int mEnableLpfHspa = 512;
    private static int mEnableLpfHspap = 16384;
    private static int mEnableLpfHsupa = 256;
    private static int mEnableLpfIden = RDS_COUNT;
    private static int mEnableLpfIwlan = 131072;
    private static int mEnableLpfLte = 4096;
    private static int mEnableLpfLteCa = 262144;
    private static int mEnableLpfScdma = 65536;
    private static int mEnableLpfUmts = 4;
    private static int mIsBtLpfEnabled = 1;
    private static int mIsWlanLpfEnabled = 2;
    private static int mSearchState = -1;
    private static IHwFmService sService;
    private IntentFilter mBtIntentFilter;
    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        /* class com.huawei.android.hardware.qcomfmradio.QcomFmReceiver.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null || !"android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                Log.d(QcomFmReceiver.TAG, "ACTION_STATE_CHANGED failed");
                return;
            }
            int newState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
            int mBtWlanLpf = SystemPropertiesEx.getInt("persist.btwlan.lpfenabler", 0);
            if (newState == 12) {
                if ((QcomFmReceiver.mIsBtLpfEnabled & mBtWlanLpf) == QcomFmReceiver.mIsBtLpfEnabled) {
                    QcomFmReceiver.this.mControl.enableLPF(FmTransceiver.sFd, 1);
                }
            } else if ((QcomFmReceiver.mIsBtLpfEnabled & mBtWlanLpf) == QcomFmReceiver.mIsBtLpfEnabled) {
                QcomFmReceiver.this.mControl.enableLPF(FmTransceiver.sFd, 0);
            }
        }
    };
    public PhoneStateListener mDataConnectionStateListener;
    private FmReceiverJNI mFmReceiverJNI;
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.huawei.android.hardware.qcomfmradio.QcomFmReceiver.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null || !"android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                Log.d(QcomFmReceiver.TAG, "WIFI_STATE_CHANGED_ACTION failed");
                return;
            }
            int newState = intent.getIntExtra("wifi_state", 4);
            int mBtWlanLpf = SystemPropertiesEx.getInt("persist.btwlan.lpfenabler", 0);
            if (newState == 3) {
                if ((QcomFmReceiver.mIsWlanLpfEnabled & mBtWlanLpf) == QcomFmReceiver.mIsWlanLpfEnabled) {
                    QcomFmReceiver.this.mControl.enableLPF(FmTransceiver.sFd, 1);
                }
            } else if ((QcomFmReceiver.mIsWlanLpfEnabled & mBtWlanLpf) == QcomFmReceiver.mIsWlanLpfEnabled) {
                QcomFmReceiver.this.mControl.enableLPF(FmTransceiver.sFd, 0);
            }
        }
    };

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean configure(FmConfig fmConfig) {
        return super.configure(fmConfig);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean configure(QcomFmConfig qcomFmConfig) {
        return super.configure(qcomFmConfig);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean enable(FmConfig fmConfig, int i) {
        return super.enable(fmConfig, i);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean enable(BaseFmConfig baseFmConfig, int i) {
        return super.enable(baseFmConfig, i);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public /* bridge */ /* synthetic */ boolean getInternalAntenna() {
        return super.getInternalAntenna();
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean registerClient(QcomFmRxEvCallbacksAdaptor qcomFmRxEvCallbacksAdaptor) {
        return super.registerClient(qcomFmRxEvCallbacksAdaptor);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean registerTransmitClient(FmTransmitterCallbacks fmTransmitterCallbacks) {
        return super.registerTransmitClient(fmTransmitterCallbacks);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean setAnalogMode(boolean z) {
        return super.setAnalogMode(z);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public /* bridge */ /* synthetic */ boolean setInternalAntenna(boolean z) {
        return super.setInternalAntenna(z);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ void setNotchFilter(boolean z) {
        super.setNotchFilter(z);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public /* bridge */ /* synthetic */ boolean setStation(int i) {
        return super.setStation(i);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver
    public /* bridge */ /* synthetic */ boolean unregisterTransmitClient() {
        return super.unregisterTransmitClient();
    }

    public boolean isSmdTransportLayer() {
        if (SystemPropertiesEx.get("ro.vendor.qualcomm.bt.hci_transport").equals("smd")) {
            return true;
        }
        return false;
    }

    public static boolean isRomeChip() {
        if (SystemPropertiesEx.get("vendor.qcom.bluetooth.soc").equals("rome")) {
            return true;
        }
        return false;
    }

    public static boolean isCherokeeChip() {
        if (SystemPropertiesEx.get("vendor.qcom.bluetooth.soc").equals("cherokee")) {
            return true;
        }
        return false;
    }

    public void registerDataConnectionStateListener(Context mContext) {
        if (mContext != null) {
            ((TelephonyManager) mContext.getSystemService("phone")).listen(this.mDataConnectionStateListener, 64);
        }
    }

    public void unregisterDataConnectionStateListener(Context mContext) {
        if (mContext != null) {
            ((TelephonyManager) mContext.getSystemService("phone")).listen(this.mDataConnectionStateListener, 0);
        }
    }

    public QcomFmReceiver() {
        this.mControl = new FmRxControls();
        this.mRdsData = new FmRxRdsData(sFd);
        this.mRxEvents = new FmRxEventListner();
    }

    public QcomFmReceiver(String devicePath, FmRxEvCallbacks callback2) throws InstantiationException {
        this.mControl = new FmRxControls();
        this.mRxEvents = new FmRxEventListner();
        mCallback = (QcomFmRxEvCallbacksAdaptor) callback2;
        if (isCherokeeChip()) {
            this.mFmReceiverJNI = new FmReceiverJNI(mCallback);
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean registerClient(FmRxEvCallbacks callback2) {
        return super.registerClient((QcomFmRxEvCallbacksAdaptor) callback2);
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean unregisterClient() {
        return super.unregisterClient();
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean enable(BaseFmConfig configSettings) {
        int state = getFMState();
        this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mBtIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        if (state == 1 || state == 3 || state == 6 || state == 4 || state == 2 || state == 5) {
            return false;
        }
        setFMPowerState(4);
        boolean status = super.enable(configSettings, 1);
        if (status) {
            if (!isCherokeeChip()) {
                status = registerClient(mCallback);
            }
            this.mRdsData = new FmRxRdsData(sFd);
            return status;
        }
        setFMPowerState(0);
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean reset() {
        if (getFMState() == 0) {
            return false;
        }
        setFMPowerState(0);
        boolean status = unregisterClient();
        release("/dev/radio0");
        return status;
    }

    @Override // com.huawei.android.hardware.qcomfmradio.FmTransceiver, com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean disable() {
        int state = getFMState();
        if (state == 0 || state == 6) {
            return false;
        }
        if (state == 3) {
            setSearchState(4);
            cancelSearch();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.d(TAG, "FMState_Srch_InProg InterruptedException");
            }
        } else if (state == 4) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e2) {
                Log.d(TAG, "subPwrLevel_FMRx_Starting InterruptedException");
            }
            if (getFMState() == 4) {
                return false;
            }
        }
        setFMPowerState(6);
        super.disable();
        return true;
    }

    static int getSearchState() {
        return mSearchState;
    }

    static void setSearchState(int state) {
        mSearchState = state;
        int i = mSearchState;
        if (i == 0 || i == 1 || i == 2) {
            setFMPowerState(3);
        } else if (i == 3) {
            mSearchState = -1;
            setFMPowerState(1);
        } else if (i != 4) {
            mSearchState = -1;
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean searchStations(int mode, int dwellPeriod, int direction) {
        int state = getFMState();
        boolean bStatus = true;
        if (state == 0 || state == 3) {
            return false;
        }
        if (!(mode == 0 || mode == 1)) {
            bStatus = false;
        }
        if (dwellPeriod < 0 || dwellPeriod > 7) {
            bStatus = false;
        }
        if (!(direction == 0 || direction == 1)) {
            bStatus = false;
        }
        if (bStatus) {
            if (mode == 0) {
                setSearchState(0);
            } else if (mode == 1) {
                setSearchState(1);
            }
            if (this.mControl.searchStations(sFd, mode, dwellPeriod, direction, 0, 0) != 0) {
                if (getFMState() == 3) {
                    setSearchState(3);
                }
                return false;
            } else if (getFMState() == 0) {
                return false;
            }
        }
        return bStatus;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        boolean bStatus;
        boolean bStatus2 = true;
        int state = getFMState();
        if (state == 0) {
            return false;
        }
        if (state == 3) {
            return false;
        }
        if (!(mode == 4 || mode == 5 || mode == 6 || mode == 7)) {
            bStatus2 = false;
        }
        if (dwellPeriod < 1 || dwellPeriod > 7) {
            bStatus2 = false;
        }
        if (direction == 0 || direction == 1) {
            bStatus = bStatus2;
        } else {
            bStatus = false;
        }
        if (!bStatus) {
            return bStatus;
        }
        setSearchState(1);
        if (this.mControl.searchStations(sFd, mode, dwellPeriod, direction, pty, pi) == 0) {
            return bStatus;
        }
        if (getFMState() == 3) {
            setSearchState(3);
        }
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean searchStationList(int mode, int direction, int maximumStations, int pty) {
        boolean bStatus;
        int re;
        int state = getFMState();
        boolean bStatus2 = true;
        if (state == 0 || state == 3) {
            return false;
        }
        if (!(mode == 2 || mode == 3 || mode == 8 || mode == 9)) {
            bStatus2 = false;
        }
        if (maximumStations < 0 || maximumStations > 12) {
            bStatus2 = false;
        }
        if (direction == 0 || direction == 1) {
            bStatus = bStatus2;
        } else {
            bStatus = false;
        }
        if (!bStatus) {
            return bStatus;
        }
        setSearchState(2);
        if (mode == 8 || mode == 9) {
            re = this.mControl.searchStationList(sFd, mode == 8 ? 2 : 3, 0, direction, pty);
        } else {
            re = this.mControl.searchStationList(sFd, mode, maximumStations, direction, pty);
        }
        if (re == 0) {
            return bStatus;
        }
        if (getFMState() == 3) {
            setSearchState(3);
        }
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean cancelSearch() {
        if (getFMState() != 3) {
            return false;
        }
        setSearchState(4);
        this.mControl.cancelSearch(sFd);
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setMuteMode(int mode) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        if (mode == 0) {
            this.mControl.muteControl(sFd, false);
        } else if (mode == 1) {
            this.mControl.muteControl(sFd, true);
        }
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setStereoMode(boolean stereoEnable) {
        int state = getFMState();
        if (state == 0 || state == 3 || this.mControl.stereoControl(sFd, stereoEnable) != 0) {
            return false;
        }
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setSignalThreshold(int threshold) {
        int rssiLev;
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        if (threshold == 0) {
            rssiLev = FM_RX_RSSI_LEVEL_VERY_WEAK;
        } else if (threshold == 1) {
            rssiLev = FM_RX_RSSI_LEVEL_WEAK;
        } else if (threshold == 2) {
            rssiLev = FM_RX_RSSI_LEVEL_STRONG;
        } else if (threshold != 3) {
            return false;
        } else {
            rssiLev = FM_RX_RSSI_LEVEL_VERY_STRONG;
        }
        if (1 == 0 || FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH, rssiLev) == 0) {
            return true;
        }
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getTunedFrequency() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return -1;
        }
        return FmReceiverJNI.getFreqNative(sFd);
    }

    public FmRxRdsData getPSInfo() {
        byte[] buff = new byte[256];
        if (isCherokeeChip()) {
            buff = FmReceiverJNI.getPsBuffer(buff);
        } else {
            FmReceiverJNI.getBufferNative(sFd, buff, 3);
        }
        int piLower = buff[3] & 255;
        this.mRdsData.setPrgmId(((buff[2] & 255) << 8) | piLower);
        this.mRdsData.setPrgmType(buff[1] & 31);
        int numOfPs = buff[0] & 15;
        try {
            this.mRdsData.setPrgmServices(new String(buff, 5, numOfPs * 8, Charset.forName("UTF-8")));
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "Number of PS names " + numOfPs);
        }
        return this.mRdsData;
    }

    public FmRxRdsData getRTInfo() {
        byte[] buff = new byte[256];
        if (isCherokeeChip()) {
            buff = FmReceiverJNI.getPsBuffer(buff);
        } else {
            FmReceiverJNI.getBufferNative(sFd, buff, 2);
        }
        String rdsStr = new String(buff, Charset.forName("UTF-8"));
        int i = (buff[2] & 255) << 8;
        this.mRdsData.setPrgmId(i | (buff[3] & 255));
        this.mRdsData.setPrgmType(buff[1] & 31);
        try {
            int endIndex = buff[0] + 5;
            if (rdsStr.length() >= endIndex && endIndex >= 5) {
                this.mRdsData.setRadioText(rdsStr.substring(5, endIndex));
            }
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "StringIndexOutOfBoundsException ...");
        }
        return this.mRdsData;
    }

    /* JADX INFO: Multiple debug info for r2v2 byte: [D('tag_code' byte), D('j' int)] */
    /* JADX INFO: Multiple debug info for r7v3 byte: [D('j' int), D('tag_start_pos' byte)] */
    /* JADX INFO: Multiple debug info for r8v1 byte: [D('j' int), D('tag_len' byte)] */
    public FmRxRdsData getRTPlusInfo() {
        String rt;
        byte[] rt_plus = new byte[256];
        int j = 2;
        if (isCherokeeChip()) {
            rt_plus = FmReceiverJNI.getPsBuffer(rt_plus);
        } else {
            FmReceiverJNI.getBufferNative(sFd, rt_plus, BUF_RTPLUS);
        }
        if (rt_plus[0] > 0) {
            if (rt_plus[1] == 0) {
                rt = this.mRdsData.getRadioText();
            } else {
                rt = this.mRdsData.getERadioText();
            }
            if (rt == null || BuildConfig.FLAVOR.equals(rt)) {
                this.mRdsData.setTagNums(0);
            } else {
                int rt_len = rt.length();
                this.mRdsData.setTagNums(0);
                int i = 1;
                while (i <= 2 && j < rt_plus[0]) {
                    int j2 = j + 1;
                    byte tag_code = rt_plus[j];
                    int j3 = j2 + 1;
                    byte tag_start_pos = rt_plus[j2];
                    int j4 = j3 + 1;
                    byte tag_len = rt_plus[j3];
                    if (tag_len + tag_start_pos <= rt_len && tag_code > 0) {
                        this.mRdsData.setTagValue(rt.substring(tag_start_pos, tag_len + tag_start_pos), i);
                        this.mRdsData.setTagCode(tag_code, i);
                    }
                    i++;
                    j = j4;
                }
            }
        } else {
            this.mRdsData.setTagNums(0);
        }
        return this.mRdsData;
    }

    public FmRxRdsData getERTInfo() {
        String s;
        byte[] raw_ert = new byte[256];
        String encoding_type = "UCS-2";
        if (isCherokeeChip()) {
            raw_ert = FmReceiverJNI.getPsBuffer(raw_ert);
        } else {
            FmReceiverJNI.getBufferNative(sFd, raw_ert, 12);
        }
        if (raw_ert[0] > 0) {
            byte[] ert_text = new byte[raw_ert[0]];
            for (int i = 3; i - 3 < raw_ert[0]; i++) {
                ert_text[i - 3] = raw_ert[i];
            }
            if (raw_ert[1] == 1) {
                encoding_type = "UTF-8";
            }
            try {
                s = new String(ert_text, encoding_type);
            } catch (UnsupportedEncodingException e) {
                s = BuildConfig.FLAVOR;
            }
            this.mRdsData.setERadioText(s);
            if (raw_ert[2] == 0) {
                this.mRdsData.setFormatDir(false);
            } else {
                this.mRdsData.setFormatDir(true);
            }
        }
        return this.mRdsData;
    }

    public FmRxRdsData getECCInfo() {
        byte[] raw_ecc = FmReceiverJNI.getPsBuffer(new byte[256]);
        if (raw_ecc[0] > 0) {
            this.mRdsData.setECountryCode(raw_ecc[9] & 255);
        }
        return this.mRdsData;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getAFInfo() {
        byte[] buff = new byte[256];
        int[] AfList = new int[50];
        if (isCherokeeChip()) {
            buff = FmReceiverJNI.getPsBuffer(buff);
        } else {
            FmReceiverJNI.getBufferNative(sFd, buff, 5);
        }
        if (isSmdTransportLayer() || isRomeChip() || isCherokeeChip()) {
            int i = (buff[0] & 255) | ((buff[1] & 255) << 8) | ((buff[2] & 255) << 16) | ((buff[3] & 255) << 24);
            int i2 = ((buff[5] & 255) << 8) | (buff[4] & 255);
            int size_AFLIST = buff[6] & 255;
            for (int i3 = 0; i3 < size_AFLIST; i3++) {
                AfList[i3] = (buff[(i3 * 4) + 6 + 1] & 255) | ((buff[((i3 * 4) + 6) + 2] & 255) << 8) | ((buff[((i3 * 4) + 6) + 3] & 255) << 16) | ((buff[((i3 * 4) + 6) + 4] & 255) << 24);
            }
        } else if (buff[4] <= 0 || buff[4] > 25) {
            return null;
        } else {
            int lowerBand = FmReceiverJNI.getLowerBandNative(sFd);
            for (int i4 = 0; i4 < buff[4]; i4++) {
                AfList[i4] = ((buff[i4 + 4] & 255) * 1000) + lowerBand;
            }
        }
        return AfList;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getAudioQuilty(int value) {
        return FmReceiverJNI.getAudioQuiltyNative(sFd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int setFmSnrThresh(int value) {
        return FmReceiverJNI.setFmSnrThreshNative(sFd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int setFmRssiThresh(int value) {
        return FmReceiverJNI.setFmRssiThreshNative(sFd, value);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public void setFmDeviceConnectionState(int state) {
        try {
            getService().setFmDeviceConnectionState(state);
        } catch (RemoteException e) {
        }
    }

    private static IHwFmService getService() {
        IHwFmService iHwFmService = sService;
        if (iHwFmService != null) {
            return iHwFmService;
        }
        sService = IHwFmService.Stub.asInterface(ServiceManagerEx.getService("hwfm_service"));
        return sService;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setPowerMode(int powerMode) {
        int re;
        if (powerMode == 1) {
            re = this.mControl.setLowPwrMode(sFd, true);
        } else {
            re = this.mControl.setLowPwrMode(sFd, false);
        }
        return re == 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getPowerMode() {
        return this.mControl.getPwrMode(sFd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getRssiLimit() {
        return new int[]{0, 100};
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getSignalThreshold() {
        int signalStrength;
        int state = getFMState();
        if (state == 0 || state == 3) {
            return -1;
        }
        int rmssiThreshold = FmReceiverJNI.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH);
        if (FM_RX_RSSI_LEVEL_VERY_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_WEAK) {
            signalStrength = FM_RX_RSSI_LEVEL_WEAK;
        } else if (FM_RX_RSSI_LEVEL_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_STRONG) {
            signalStrength = FM_RX_RSSI_LEVEL_STRONG;
        } else if (FM_RX_RSSI_LEVEL_STRONG < rmssiThreshold) {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_STRONG;
        } else {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_WEAK;
        }
        if (signalStrength == FM_RX_RSSI_LEVEL_VERY_WEAK) {
            return 0;
        }
        if (signalStrength == FM_RX_RSSI_LEVEL_WEAK) {
            return 1;
        }
        if (signalStrength == FM_RX_RSSI_LEVEL_STRONG) {
            return 2;
        }
        if (signalStrength != FM_RX_RSSI_LEVEL_VERY_STRONG) {
            return 0;
        }
        return 3;
    }

    public int getAFJumpRmssiTh() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return -1;
        }
        return this.mControl.getAFJumpRmssiTh(sFd);
    }

    public boolean setAFJumpRmssiTh(int th) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setAFJumpRmssiTh(sFd, th);
    }

    public int getAFJumpRmssiSamples() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return -1;
        }
        return this.mControl.getAFJumpRmssiSamples(sFd);
    }

    public boolean setAFJumpRmssiSamples(int samples) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setAFJumpRmssiSamples(sFd, samples);
    }

    public int getGdChRmssiTh() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return -1;
        }
        return this.mControl.getGdChRmssiTh(sFd);
    }

    public boolean setGdChRmssiTh(int th) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setGdChRmssiTh(sFd, th);
    }

    public int getSearchAlgoType() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return Integer.MAX_VALUE;
        }
        return this.mControl.getSearchAlgoType(sFd);
    }

    public boolean setSearchAlgoType(int searchType) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        if (searchType == 0 || searchType == 1) {
            return this.mControl.setSearchAlgoType(sFd, searchType);
        }
        return false;
    }

    public int getSinrFirstStage() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return Integer.MAX_VALUE;
        }
        return this.mControl.getSinrFirstStage(sFd);
    }

    public boolean setSinrFirstStage(int sinr) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setSinrFirstStage(sFd, sinr);
    }

    public int getRmssiFirstStage() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return Integer.MAX_VALUE;
        }
        return this.mControl.getRmssiFirstStage(sFd);
    }

    public boolean setRmssiFirstStage(int rmssi) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setRmssiFirstStage(sFd, rmssi);
    }

    public int getCFOMeanTh() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return Integer.MAX_VALUE;
        }
        return this.mControl.getCFOMeanTh(sFd);
    }

    public boolean setCFOMeanTh(int th) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setCFOMeanTh(sFd, th);
    }

    public boolean setPSRxRepeatCount(int count) {
        if (getFMState() == 0) {
            return false;
        }
        return this.mControl.setPSRxRepeatCount(sFd, count);
    }

    public boolean getPSRxRepeatCount() {
        if (getFMState() == 0) {
            return false;
        }
        return this.mControl.getPSRxRepeatCount(sFd);
    }

    public byte getBlendSinr() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return Byte.MAX_VALUE;
        }
        return this.mControl.getBlendSinr(sFd);
    }

    public boolean setBlendSinr(int sinrHi) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setBlendSinr(sFd, sinrHi);
    }

    public byte getBlendRmssi() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return Byte.MAX_VALUE;
        }
        return this.mControl.getBlendRmssi(sFd);
    }

    public boolean setBlendRmssi(int rmssiHi) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return false;
        }
        return this.mControl.setBlendRmssi(sFd, rmssiHi);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean setRdsGroupOptions(int enRdsGrpsMask, int rdsBuffSize, boolean enRdsChangeFilter) {
        int state = getFMState();
        if (state == 0 || state == 3 || this.mRdsData.rdsOn(true) != 0 || this.mRdsData.rdsGrpOptions(enRdsGrpsMask, rdsBuffSize, enRdsChangeFilter) != 0) {
            return false;
        }
        return true;
    }

    public boolean setRawRdsGrpMask() {
        return FmTransceiver.setRDSGrpMask(64);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        int state;
        if (this.mRdsData == null || (state = getFMState()) == 0 || state == 3 || this.mRdsData.rdsOn(true) != 0 || this.mRdsData.rdsOptions(fmGrpsToProc) != 0) {
            return false;
        }
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public boolean enableAFjump(boolean enable) {
        int state = getFMState();
        if (state == 0 || state == 3 || this.mRdsData.rdsOn(true) != 0 || this.mRdsData.enableAFjump(enable) != 0) {
            return false;
        }
        return true;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int[] getStationList() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            return null;
        }
        int[] iArr = new int[100];
        return this.mControl.stationList(sFd);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getRssi() {
        return FmReceiverJNI.getRSSINative(sFd);
    }

    public int getIoverc() {
        return this.mControl.IovercControl(sFd);
    }

    public int getIntDet() {
        return this.mControl.IntDet(sFd);
    }

    public int getMpxDcc() {
        return this.mControl.Mpx_Dcc(sFd);
    }

    public void setHiLoInj(int inj) {
        this.mControl.setHiLoInj(sFd, inj);
    }

    public int getRmssiDelta() {
        return this.mControl.getRmssiDelta(sFd);
    }

    public void setRmssiDel(int delta) {
        this.mControl.setRmssiDel(sFd, delta);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public byte[] getRawRDS(int numBlocks) {
        if (numBlocks <= 0 || numBlocks > RDS_COUNT) {
            return null;
        }
        byte[] rawRds = new byte[(numBlocks * 3)];
        int re = FmReceiverJNI.getRawRdsNative(sFd, rawRds, numBlocks * 3);
        if (re == numBlocks * 3) {
            return rawRds;
        }
        if (re <= 0) {
            return null;
        }
        byte[] buff = new byte[re];
        System.arraycopy(rawRds, 0, buff, 0, re);
        return buff;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmReceiver
    public int getFMState() {
        return getFMPowerState();
    }

    public boolean setOnChannelThreshold(int data) {
        if (this.mControl.setOnChannelThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public boolean getOnChannelThreshold() {
        if (this.mControl.getOnChannelThreshold(sFd) != 0) {
            return false;
        }
        return true;
    }

    public boolean setOffChannelThreshold(int data) {
        if (this.mControl.setOffChannelThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public boolean getOffChannelThreshold() {
        if (this.mControl.getOffChannelThreshold(sFd) != 0) {
            return false;
        }
        return true;
    }

    public int getSINR() {
        return this.mControl.getSINR(sFd);
    }

    public boolean setSINRThreshold(int data) {
        if (this.mControl.setSINRThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getSINRThreshold() {
        return this.mControl.getSINRThreshold(sFd);
    }

    public boolean setRssiThreshold(int data) {
        if (this.mControl.setRssiThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getRssiThreshold() {
        return this.mControl.getRssiThreshold(sFd);
    }

    public boolean setAfJumpRssiThreshold(int data) {
        if (this.mControl.setAfJumpRssiThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getAfJumpRssiThreshold() {
        return this.mControl.getAfJumpRssiThreshold(sFd);
    }

    public boolean setRdsFifoCnt(int data) {
        if (this.mControl.setRdsFifoCnt(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getRdsFifoCnt() {
        return this.mControl.getRdsFifoCnt(sFd);
    }

    public boolean setSINRsamples(int data) {
        if (this.mControl.setSINRsamples(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getSINRsamples() {
        return this.mControl.getSINRsamples(sFd);
    }

    public int updateSpurFreq(int freq, int rmssi, boolean enable) {
        return this.mControl.updateSpurTable(sFd, freq, rmssi, enable);
    }

    public int configureSpurTable() {
        return this.mControl.configureSpurTable(sFd);
    }

    public static int getSpurConfiguration(int freq) {
        int retval = FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_IRIS_GET_SPUR_TBL, freq);
        if (retval != 0) {
            Log.d(TAG, "Failed/No Spurs for " + freq);
        }
        return retval;
    }

    public static void getSpurTableData() {
        byte[] buff = new byte[256];
        FmReceiverJNI.getBufferNative(sFd, buff, 13);
        int freq = (buff[0] & 255) | ((buff[1] & 255) << 8) | ((buff[2] & 255) << 16);
        byte b = buff[3];
        for (int i = 0; i < 3; i++) {
            int rotation_value = (buff[(i * 4) + 4] & 255) | ((buff[(i * 4) + 5] & 255) << 8) | ((buff[(i * 4) + 6] & 15) << 12);
            byte b2 = (byte) (((buff[(i * 4) + 6] & 240) >> 4) & 1);
            byte b3 = (byte) (((buff[(i * 4) + 6] & 240) >> 5) & 3);
            byte b4 = (byte) (((buff[(i * 4) + 6] & 240) >> 7) & 1);
        }
    }

    public void FMcontrolLowPassFilter(int state, int net_type, int enable) {
        int RatConf = SystemPropertiesEx.getInt("persist.fm_wan.ratconf", 0);
        switch (net_type) {
            case 1:
                int i = mEnableLpfGprs;
                if ((i & RatConf) == i) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 2:
                int i2 = mEnableLpfEdge;
                if ((i2 & RatConf) == i2) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 3:
                int i3 = mEnableLpfUmts;
                if ((i3 & RatConf) == i3) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 4:
                int i4 = mEnableLpfCdma;
                if ((i4 & RatConf) == i4) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 5:
                int i5 = mEnableLpfEvdo0;
                if ((i5 & RatConf) == i5) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 6:
                int i6 = mEnableLpfEvdoA;
                if ((i6 & RatConf) == i6) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 7:
                int i7 = mEnableLpf1xRtt;
                if ((i7 & RatConf) == i7) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 8:
                int i8 = mEnableLpfHsdpa;
                if ((i8 & RatConf) == i8) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 9:
                int i9 = mEnableLpfHsupa;
                if ((i9 & RatConf) == i9) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case HwMtpConstants.TYPE_UINT128:
                int i10 = mEnableLpfHspa;
                if ((i10 & RatConf) == i10) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case BUF_RTPLUS /*{ENCODED_INT: 11}*/:
                int i11 = mEnableLpfIden;
                if ((i11 & RatConf) == i11) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 12:
                int i12 = mEnableLpfEvdoB;
                if ((i12 & RatConf) == i12) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 13:
                int i13 = mEnableLpfLte;
                if ((i13 & RatConf) == i13) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 14:
                int i14 = mEnableLpfEhrpd;
                if ((i14 & RatConf) == i14) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 15:
                int i15 = mEnableLpfHspap;
                if ((i15 & RatConf) == i15) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 16:
                int i16 = mEnableLpfGsm;
                if ((i16 & RatConf) == i16) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 17:
                int i17 = mEnableLpfScdma;
                if ((i17 & RatConf) == i17) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 18:
                int i18 = mEnableLpfIwlan;
                if ((i18 & RatConf) == i18) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case NETWORK_TYPE_LTE_CA /*{ENCODED_INT: 19}*/:
                int i19 = mEnableLpfLteCa;
                if ((i19 & RatConf) == i19) {
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void EnableSlimbus(int enable) {
        this.mControl.enableSlimbus(sFd, enable);
    }
}

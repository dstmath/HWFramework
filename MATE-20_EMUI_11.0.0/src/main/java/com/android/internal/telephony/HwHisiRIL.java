package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.data.DataProfile;
import com.android.internal.telephony.uicc.IccUtils;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import vendor.huawei.hardware.hisiradio.V1_0.CellInfoCdma;
import vendor.huawei.hardware.hisiradio.V1_0.CellInfoGsm;
import vendor.huawei.hardware.hisiradio.V1_0.CellInfoLte;
import vendor.huawei.hardware.hisiradio.V1_0.CellInfoWcdma;
import vendor.huawei.hardware.hisiradio.V1_0.CsgNetworkInfo;
import vendor.huawei.hardware.hisiradio.V1_0.IHisiRadio;
import vendor.huawei.hardware.hisiradio.V1_0.RILUICCAUTH;
import vendor.huawei.hardware.hisiradio.V1_1.HwSignalStrength_1_1;
import vendor.huawei.hardware.hisiradio.V1_2.DataProfileInfo;

public class HwHisiRIL extends RIL {
    private static final int CONFIG_ON = 1;
    private static final int EVENT_RIL_CONNECTED = 100;
    public static final boolean IS_VZW = ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb")));
    private static final int PARATYPE_BASIC_COMM = 1;
    private static final int PARATYPE_CELLULAR_CLOUD = 2;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwHisiRIL";
    private static final boolean SHOW_4G_PLUS_ICON = SystemProperties.getBoolean("ro.config.hw_show_4G_Plus_icon", false);
    private static final int TYPEMASK_PARATYPE_BASIC_COMM = 0;
    private static final int TYPEMASK_PARATYPE_CELLULAR_CLOUD = 1;
    private int mApDsFlowConfig;
    private int mApDsFlowOper;
    private int mApDsFlowThreshold;
    private int mApDsFlowTotalThreshold;
    private int mDsFlowNvEnable;
    private int mDsFlowNvInterval;
    private Handler mHisiRilHandler;
    HwHisiRadioIndication mHwHisiRadioIndication;
    HwHisiRadioResponse mHwHisiRadioResponse;
    private final BroadcastReceiver mIntentReceiver;
    private Integer mRilInstanceId;

    public interface HisiRILCommand {
        void excute(IHisiRadio iHisiRadio, int i) throws RemoteException, RuntimeException;
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRilInstanceId = null;
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.TIME_SET".equals(intent.getAction()) || "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                    HwHisiRIL hwHisiRIL = HwHisiRIL.this;
                    hwHisiRIL.riljLog("mIntentReceiver onReceive " + intent.getAction());
                    HwHisiRIL.this.setApTimeToCp();
                }
            }
        };
        this.mApDsFlowConfig = 0;
        this.mApDsFlowThreshold = 0;
        this.mApDsFlowTotalThreshold = 0;
        this.mApDsFlowOper = 0;
        this.mDsFlowNvEnable = 0;
        this.mDsFlowNvInterval = 0;
        this.mHisiRilHandler = new Handler() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == HwHisiRIL.EVENT_RIL_CONNECTED) {
                    HwHisiRIL.this.riljLog("EVENT_RIL_CONNECTED, set AP time to CP.");
                    HwHisiRIL.this.setApTimeToCp();
                }
            }
        };
        this.mHwHisiRadioResponse = new HwHisiRadioResponse(this);
        this.mHwHisiRadioIndication = new HwHisiRadioIndication(this);
        getHisiRadioProxy(null);
        this.mRilInstanceId = instanceId;
        registerIntentReceiver();
        registerForRilConnected(this.mHisiRilHandler, EVENT_RIL_CONNECTED, null);
    }

    public static ArrayList<CellInfo> convertHalCellInfoListEx(ArrayList<vendor.huawei.hardware.hisiradio.V1_0.CellInfo> records) {
        Parcel p;
        Iterator<vendor.huawei.hardware.hisiradio.V1_0.CellInfo> it;
        int i;
        ArrayList<CellInfo> response = new ArrayList<>(records.size());
        Iterator<vendor.huawei.hardware.hisiradio.V1_0.CellInfo> it2 = records.iterator();
        while (it2.hasNext()) {
            vendor.huawei.hardware.hisiradio.V1_0.CellInfo record = it2.next();
            Parcel p2 = Parcel.obtain();
            p2.writeInt(record.cellInfoType);
            p2.writeInt(record.registered ? 1 : 0);
            p2.writeLong(record.timeStamp);
            p2.writeInt(Integer.MAX_VALUE);
            int i2 = record.cellInfoType;
            if (i2 == 1) {
                it = it2;
                i = 0;
                p = p2;
                CellInfoGsm cellInfoGsm = (CellInfoGsm) record.gsm.get(0);
                writeToParcelForGsmHw(p, cellInfoGsm.cellIdentityGsm.lac, cellInfoGsm.cellIdentityGsm.cid, cellInfoGsm.cellIdentityGsm.arfcn, Byte.toUnsignedInt(cellInfoGsm.cellIdentityGsm.bsic), cellInfoGsm.cellIdentityGsm.mcc, cellInfoGsm.cellIdentityGsm.mnc, "", "", cellInfoGsm.signalStrengthGsm.signalStrength, cellInfoGsm.signalStrengthGsm.bitErrorRate, cellInfoGsm.signalStrengthGsm.timingAdvance);
            } else if (i2 == 2) {
                it = it2;
                i = 0;
                p = p2;
                CellInfoCdma cellInfoCdma = (CellInfoCdma) record.cdma.get(0);
                writeToParcelForCdmaHw(p, cellInfoCdma.cellIdentityCdma.networkId, cellInfoCdma.cellIdentityCdma.systemId, cellInfoCdma.cellIdentityCdma.baseStationId, cellInfoCdma.cellIdentityCdma.longitude, cellInfoCdma.cellIdentityCdma.latitude, "", "", cellInfoCdma.signalStrengthCdma.dbm, cellInfoCdma.signalStrengthCdma.ecio, cellInfoCdma.signalStrengthEvdo.dbm, cellInfoCdma.signalStrengthEvdo.ecio, cellInfoCdma.signalStrengthEvdo.signalNoiseRatio);
            } else if (i2 == 3) {
                it = it2;
                i = 0;
                p = p2;
                CellInfoLte cellInfoLte = (CellInfoLte) record.lte.get(0);
                writeToParcelForLteHw(p, cellInfoLte.cellIdentityLte.ci, cellInfoLte.cellIdentityLte.pci, cellInfoLte.cellIdentityLte.tac, cellInfoLte.cellIdentityLte.earfcn, Integer.MAX_VALUE, cellInfoLte.cellIdentityLte.mcc, cellInfoLte.cellIdentityLte.mnc, "", "", cellInfoLte.signalStrengthLte.signalStrength, cellInfoLte.signalStrengthLte.rsrp, cellInfoLte.signalStrengthLte.rsrq, cellInfoLte.signalStrengthLte.rssnr, cellInfoLte.signalStrengthLte.cqi, cellInfoLte.signalStrengthLte.timingAdvance, false);
            } else if (i2 == 4) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) record.wcdma.get(0);
                it = it2;
                i = 0;
                p = p2;
                writeToParcelForWcdmaHw(p2, cellInfoWcdma.cellIdentityWcdma.lac, cellInfoWcdma.cellIdentityWcdma.cid, cellInfoWcdma.cellIdentityWcdma.psc, cellInfoWcdma.cellIdentityWcdma.uarfcn, cellInfoWcdma.cellIdentityWcdma.mcc, cellInfoWcdma.cellIdentityWcdma.mnc, "", "", cellInfoWcdma.signalStrengthWcdma.signalStrength, cellInfoWcdma.signalStrengthWcdma.bitErrorRate, Integer.MAX_VALUE, Integer.MAX_VALUE);
            } else {
                throw new RuntimeException("unexpected cellinfotype: " + record.cellInfoType);
            }
            p.setDataPosition(i);
            p.recycle();
            response.add((CellInfo) CellInfo.CREATOR.createFromParcel(p));
            it2 = it;
        }
        return response;
    }

    private static DataProfileInfo convertToHalDataProfile12(DataProfile dp) {
        DataProfileInfo dpi = new DataProfileInfo();
        dpi.apn = dp.getApn();
        dpi.protocol = dp.getProtocolType();
        dpi.roamingProtocol = dp.getRoamingProtocolType();
        dpi.authType = dp.getAuthType();
        dpi.user = dp.getUserName();
        dpi.password = dp.getPassword();
        dpi.type = dp.getType();
        dpi.maxConnsTime = dp.getMaxConnectionsTime();
        dpi.maxConns = dp.getMaxConnections();
        dpi.waitTime = dp.getWaitTime();
        dpi.enabled = dp.isEnabled();
        dpi.supportedApnTypesBitmap = dp.getSupportedApnTypesBitmask();
        dpi.bearerBitmap = ServiceState.convertNetworkTypeBitmaskToBearerBitmask(dp.getBearerBitmask()) << 1;
        dpi.mtu = dp.getMtu();
        dpi.persistent = dp.isPersistent();
        dpi.preferred = dp.isPreferred();
        dpi.sNssai = dp.getSnssai();
        dpi.sscMode = dp.getSscMode();
        dpi.profileId = dpi.persistent ? dp.getProfileId() : -1;
        return dpi;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setApTimeToCp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(14, -(calendar.get(15) + calendar.get(16)));
        Date utc = calendar.getTime();
        setTime(new SimpleDateFormat("yyyy/MM/dd").format(utc), new SimpleDateFormat("HH:mm:ss").format(utc), String.valueOf(TimeZone.getDefault().getRawOffset() / 3600000), null);
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    /* access modifiers changed from: protected */
    public Object processSolicitedEx(int rilRequest, Parcel p) {
        Object ret = HwHisiRIL.super.processSolicitedEx(rilRequest, p);
        if (ret != null) {
            return ret;
        }
        if (rilRequest == 2093) {
            return responseInts(p);
        }
        if (rilRequest == 2108) {
            return responseVoid(p);
        }
        if (rilRequest != 2132) {
            return ret;
        }
        return responseInts(p);
    }

    public void rejectCallForCause(final int gsmIndex, final int cause, Message result) {
        invokeIRadio(2171, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass3 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.rejectCallWithReason(serial, gsmIndex, cause);
            }
        });
    }

    public void queryCardType(Message result) {
        invokeIRadio(528, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass4 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCardType(serial);
            }
        });
    }

    public void getBalongSim(Message result) {
        invokeIRadio(2029, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass5 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimSlot(serial);
            }
        });
    }

    public void setActiveModemMode(final int mode, Message result) {
        invokeIRadio(2088, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass6 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setActiveModemMode(serial, mode);
            }
        });
    }

    public void switchBalongSim(final int modem1ToSlot, final int modem2ToSlot, final int modem3ToSlot, Message result) {
        invokeIRadio(2028, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass7 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                HwHisiRIL hwHisiRIL = HwHisiRIL.this;
                hwHisiRIL.riljLog("modem1ToSlot: " + modem1ToSlot + " modem2ToSlot: " + modem2ToSlot + " modem3ToSlot: " + modem3ToSlot);
                radio.setSimSlot(serial, modem1ToSlot, modem2ToSlot, modem3ToSlot);
            }
        });
    }

    public void getICCID(Message result) {
        invokeIRadio(2075, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass8 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getIccid(serial);
            }
        });
    }

    public void setLTEReleaseVersion(final int state, Message result) {
        invokeIRadio(2108, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass9 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setLteReleaseVersion(serial, state);
            }
        });
    }

    public void getLteReleaseVersion(Message result) {
        invokeIRadio(2109, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass10 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLteReleaseVersion(serial);
            }
        });
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = p.readInt();
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public void notifyVpStatus(byte[] data) {
        int len = data.length;
        riljLog("notifyVpStatus: len = " + len);
        if (len == 1) {
            this.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, data, (Throwable) null));
        }
    }

    /* access modifiers changed from: package-private */
    public void riljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mRilInstanceId != null) {
            str = " [SUB" + this.mRilInstanceId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.i(RILJ_LOG_TAG, sb.toString());
    }

    public void switchVoiceCallBackgroundState(final int state, Message result) {
        invokeIRadio(2019, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass11 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVoicecallBackGroundState(serial, state);
            }
        });
    }

    public void getLocationInfo(Message result) {
        invokeIRadio(534, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass12 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLocationInfo(serial);
            }
        });
    }

    public void queryServiceCellBand(Message result) {
        invokeIRadio(2129, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass13 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.queryServiceCellBand(serial);
            }
        });
    }

    public void getSimState(Message result) {
        invokeIRadio(2038, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass14 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getVsimSimState(serial);
            }
        });
    }

    public void setSimState(final int index, final int enable, Message result) {
        invokeIRadio(2037, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass15 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVsimSimState(serial, index, enable, -1);
            }
        });
    }

    public void hotSwitchSimSlot(final int modem0, final int modem1, final int modem2, Message result) {
        invokeIRadio(2094, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass16 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setSciChgCfg(serial, modem0, modem1, modem2);
            }
        });
    }

    public void getSimHotPlugState(Message result) {
        invokeIRadio(533, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass17 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimHotplugState(serial);
            }
        });
    }

    public void setUEOperationMode(final int mode, Message result) {
        invokeIRadio(2119, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass18 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setUeOperationMode(serial, mode);
            }
        });
    }

    public String getHwPrlVersion() {
        return SystemProperties.get("persist.radio.hwprlversion", "0");
    }

    public String getHwUimid() {
        return SystemProperties.get("persist.radio.hwuimid", "0");
    }

    public void setNetworkRatAndSrvDomainCfg(final int rat, final int srvDomain, Message result) {
        invokeIRadio(2022, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass19 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setNetworkRatAndSrvDomain(serial, rat, srvDomain);
            }
        });
    }

    public void setHwVSimPower(int power, Message result) {
        invokeIRadio(2120, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass20 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vsimPower(serial);
            }
        });
    }

    public void setISMCOEX(final String ismCoexContent, Message result) {
        invokeIRadio(2068, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass21 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setIsmcoex(serial, ismCoexContent);
            }
        });
    }

    public void sendCloudMessageToModem(int eventId) {
        byte[] request = new byte[21];
        ByteBuffer buf = ByteBuffer.wrap(request);
        buf.order(ByteOrder.nativeOrder());
        buf.put("00000000".getBytes(StandardCharsets.UTF_8));
        buf.putInt(210);
        buf.putInt(5);
        buf.putInt(eventId);
        buf.put((byte) 0);
        invokeOemRilRequestRaw(request, null);
    }

    public void getRegPlmn(Message result) {
        invokeIRadio(2042, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass22 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getPlmnInfo(serial);
            }
        });
    }

    public void getModemSupportVSimVersion(Message result) {
        invokeIRadio(2131, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass23 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vsimBasebandVersion(serial);
            }
        });
    }

    public void setApDsFlowCfg(Message result) {
        int i = this.mApDsFlowConfig;
        if (i == 1) {
            setApDsFlowCfg(i, this.mApDsFlowThreshold, this.mApDsFlowTotalThreshold, this.mApDsFlowOper, result);
        }
    }

    public void setApDsFlowCfg(final int config, final int threshold, final int totalThreshold, final int oper, Message result) {
        this.mApDsFlowConfig = config;
        this.mApDsFlowThreshold = threshold;
        this.mApDsFlowTotalThreshold = totalThreshold;
        this.mApDsFlowOper = oper;
        invokeIRadio(2110, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass24 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setApDsFlowReportConfig(serial, config, threshold, totalThreshold, oper);
            }
        });
    }

    public void setDsFlowNvCfg(Message result) {
        int i = this.mDsFlowNvEnable;
        if (i == 1) {
            setDsFlowNvCfg(i, this.mDsFlowNvInterval, result);
        }
    }

    public void setDsFlowNvCfg(final int enable, final int interval, Message result) {
        this.mDsFlowNvEnable = enable;
        this.mDsFlowNvInterval = interval;
        invokeIRadio(2112, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass25 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setDsFlowNvWriteConfigPara(serial, enable, interval);
            }
        });
    }

    public void setImsDomainConfig(final int selectDomain, Message result) {
        riljLog("setImsDomainConfig: " + selectDomain);
        invokeIRadio(2124, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass26 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setImsDomain(serial, selectDomain);
            }
        });
    }

    public void getImsDomain(Message result) {
        riljLog("getImsDomain");
        invokeIRadio(2126, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass27 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio radio12 = vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio.castFrom(radio);
                if (radio12 != null) {
                    radio12.getImsDomainV1_1(serial);
                } else {
                    radio.getImsDomain(serial);
                }
            }
        });
    }

    public void handleUiccAuth(int authType, byte[] rand, byte[] auth, Message result) {
        riljLog("handleUiccAuth");
        final RILUICCAUTH uiccAuth = new RILUICCAUTH();
        uiccAuth.authType = authType;
        String randStr = IccUtils.bytesToHexString(rand);
        String authStr = IccUtils.bytesToHexString(auth);
        if (randStr != null) {
            uiccAuth.authParams.randLen = randStr.length();
        }
        uiccAuth.authParams.rand = randStr;
        if (authStr != null) {
            uiccAuth.authParams.authLen = authStr.length();
        }
        uiccAuth.authParams.auth = authStr;
        invokeIRadio(2128, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass28 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.uiccAuth(serial, uiccAuth);
            }
        });
    }

    public void handleMapconImsaReq(byte[] Msg, Message result) {
        riljLog("handleMapconImsaReq: Msg = 0x" + IccUtils.bytesToHexString(Msg));
        final ArrayList<Byte> arrList = new ArrayList<>();
        for (byte b : Msg) {
            arrList.add(Byte.valueOf(b));
        }
        invokeIRadio(2125, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass29 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vowifiToImsaMsg(serial, arrList);
            }
        });
    }

    public void setTime(final String date, final String time, final String timezone, Message result) {
        if (date == null || time == null || timezone == null) {
            Rlog.e(RILJ_LOG_TAG, "setTime check");
        }
        invokeIRadio(2130, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass30 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setTime(serial, date, time, timezone);
            }
        });
    }

    private void registerIntentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter2);
    }

    public void notifyCellularCommParaReady(final int paratype, final int pathtype, Message result) {
        riljLog("notifyCellularCommParaReady: paratype = " + paratype + ", pathtype = " + pathtype);
        if (paratype == 1) {
            invokeIRadio(2132, result, new HisiRILCommand() {
                /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass31 */

                @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    HwHisiRIL.this.riljLog("RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY");
                    radio.notifyCellularCommParaReady(serial, paratype, pathtype);
                }
            });
        }
        if (paratype == 2) {
            invokeIRadio(2133, result, new HisiRILCommand() {
                /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass32 */

                @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    HwHisiRIL.this.riljLog("RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY");
                    radio.notifyCellularCloudParaReady(serial, paratype, pathtype);
                }
            });
        }
    }

    public void send(RILRequestReference rr) {
        riljLog("not use socket send");
    }

    public void getLteFreqWithWlanCoex(Message result) {
        invokeIRadio(2087, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass33 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLwclash(serial);
            }
        });
    }

    public void sendPseudocellCellInfo(final int infoType, final int lac, final int cid, final int radiotech, final String plmn, Message result) {
        invokeIRadio(2154, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass34 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendPseudocellCellInfo(serial, infoType, lac, cid, radiotech, plmn);
            }
        });
    }

    public void getAvailableCSGNetworks(Message result) {
        if (IS_VZW) {
            invokeIRadio(2167, result, new HisiRILCommand() {
                /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass35 */

                @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    radio.getAvailableCsgIds_1_1(serial);
                }
            });
        } else {
            invokeIRadio(2155, result, new HisiRILCommand() {
                /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass36 */

                @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    radio.getAvailableCsgIds(serial);
                }
            });
        }
    }

    public void setCSGNetworkSelectionModeManual(Object csgInfo, Message result) {
        final CsgNetworkInfo hisiCsg = new CsgNetworkInfo();
        if (csgInfo instanceof HwHisiCsgNetworkInfo) {
            HwHisiCsgNetworkInfo tempCsg = (HwHisiCsgNetworkInfo) csgInfo;
            hisiCsg.csgId = tempCsg.getCSGId();
            hisiCsg.plmn = tempCsg.getOper();
            hisiCsg.networkRat = tempCsg.getRat();
        }
        invokeIRadio(2156, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass37 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.manualSelectionCsgId(serial, hisiCsg);
            }
        });
    }

    public void setMobileDataEnable(final int state, Message response) {
        invokeIRadio(2165, response, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass38 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setMobileDataEnable(serial, state);
            }
        });
    }

    public void setRoamingDataEnable(final int state, Message response) {
        invokeIRadio(2166, response, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass39 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setRoamingDataEnable(serial, state);
            }
        });
    }

    public void sendLaaCmd(final int cmd, final String reserved, Message result) {
        invokeIRadio(2157, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass40 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendLaaCmd(serial, cmd, reserved);
            }
        });
    }

    public void getLaaDetailedState(final String reserved, Message result) {
        invokeIRadio(2158, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass41 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLaaDetailedState(serial, reserved);
            }
        });
    }

    public void setupEIMEDataCall(Message result) {
        riljLog("setupEIMEDataCall");
        invokeIRadio(2173, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass42 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setupDataCallEmergency(serial);
            }
        });
    }

    public void deactivateEIMEDataCall(Message result) {
        riljLog("deactivateEIMEDataCall");
        invokeIRadio(2174, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass43 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.deactivateDataCallEmergency(serial);
            }
        });
    }

    public void getEnhancedCellInfoList(Message result, WorkSource workSource) {
        riljLog("getEnhancedCellInfoList");
        invokeIRadio(2172, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass44 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCellInfoListOtdoa(serial);
            }
        });
    }

    public void setDeepNoDisturbState(final int state, Message result) {
        riljLog("setDeepNoDisturbState");
        invokeIRadio(2175, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass45 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setDeepNoDisturbState(serial, state);
            }
        });
    }

    public void setUplinkfreqEnable(final int state, Message result) {
        riljLog("setUplinkfreqEnable, requestid = 539");
        invokeIRadio(539, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass46 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio radioProxy11 = vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.setUlfreqEnable(serial, state);
                }
            }
        });
    }

    public void getCurrentCallsEx(Message result) {
        invokeIRadio(2176, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass47 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCurrentCallsV1_2(serial);
            }
        });
    }

    public void informModemTetherStatusToChangeGRO(final int enable, final String faceName, Message result) {
        invokeIRadio(538, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass48 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                HwHisiRIL hwHisiRIL = HwHisiRIL.this;
                hwHisiRIL.riljLog("enable = " + enable + " faceName = " + faceName);
                radio.informModemTetherStatusToChangeGRO(serial, enable, faceName);
            }
        });
    }

    public IHisiRadio getHisiRadioProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            riljLog("getHisiRadioProxy: Not calling getService(): wifi-only");
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mHisiRadioProxy != null) {
            return this.mHisiRadioProxy;
        } else {
            IHisiRadio hisiRadio = null;
            try {
                hisiRadio = IHisiRadio.getService(HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()], CommandsInterfaceEx.isNeedRetryGetRadioProxy(this.mContext));
                this.mHisiRadioProxy = hisiRadio;
            } catch (RemoteException | RuntimeException e) {
                try {
                    riljLoge("getHisiRadioProxy: huaweiradioProxy got 1_0 RemoteException | RuntimeException");
                } catch (RemoteException | RuntimeException e2) {
                    this.mHisiRadioProxy = null;
                    riljLoge("HisiRadioProxy getService/setResponseFunctions got RemoteException | RuntimeException");
                }
            }
            if (this.mHisiRadioProxy != null) {
                hisiRadio.setResponseFunctionsHuawei(this.mHwHisiRadioResponse, this.mHwHisiRadioIndication);
            } else {
                riljLoge("getHisiRadioProxy: huawei radioProxy == null");
            }
            if (this.mHisiRadioProxy == null) {
                riljLoge("getHisiRadioProxy: mHisiRadioProxy == null");
                if (result != null) {
                    AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                    result.sendToTarget();
                }
            }
            return this.mHisiRadioProxy;
        }
    }

    public void invokeIRadio(int requestId, Message result, HisiRILCommand cmd) {
        IHisiRadio radioProxy = getHisiRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(requestId, result, getmRILDefaultWorkSourceHw());
            addRequestEx(rr);
            riljLog(rr.serialString() + "> " + requestToString(requestId));
            try {
                cmd.excute(radioProxy, rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(requestId), e, rr);
            }
        }
    }

    public void sendSimChgTypeInfo(final int type, Message result) {
        riljLog("sendSimChgTypeInfo type:" + type);
        invokeIRadio(2178, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass49 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendSimChgTypeInfo(serial, type);
            }
        });
    }

    public boolean getAntiFakeBaseStation(Message response) {
        riljLog("getAntiFakeBaseStation ");
        invokeIRadio(2180, response, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass50 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCapOfRecPseBaseStation(serial);
            }
        });
        return true;
    }

    public void getCardTrayInfo(Message result) {
        riljLog("getCardTrayInfo");
        invokeIRadio(2181, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass51 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCardTrayInfo(serial);
            }
        });
    }

    public void getNvcfgMatchedResult(Message response) {
        riljLog("getNvcfgMatchedResult");
        invokeIRadio(2182, response, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass52 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getNvcfgMatchedResult(serial);
            }
        });
    }

    public void getAttachedApnSettings(Message response) {
        riljLog("getAttachedApnSettings");
        invokeIRadio(2185, response, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass53 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio radioProxy11 = vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.getAttachedApnSettings(serial);
                }
            }
        });
    }

    public void setTemperatureControlToModem(final int level, final int type, Message result) {
        riljLog("setTemperatureControl");
        invokeIRadio(2159, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass54 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio radioProxy11 = vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.setTemperatureControl(serial, level, type);
                }
            }
        });
    }

    public void clearTrafficData(Message result) {
        invokeIRadio(2090, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass55 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.clearDsFlowInfo(serial);
            }
        });
    }

    public void getTrafficData(Message result) {
        invokeIRadio(2089, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass56 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getDsFlowInfo(serial);
            }
        });
    }

    public void getDevSubMode(Message result) {
        invokeIRadio(2092, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass57 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getDeviceVersion(serial);
            }
        });
    }

    public void getSimStateViaSysinfoEx(Message result) {
        invokeIRadio(2041, result, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass58 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSystemInfoEx(serial);
            }
        });
    }

    public void getSignalStrength(Message result) {
        if (getRILReference() != null) {
            getRILReference().getSignalStrength(result);
        }
    }

    public SignalStrength convertHalSignalStrength_1_1(HwSignalStrength_1_1 signalStrength, int phoneId) {
        SignalStrength ss = new SignalStrength(new CellSignalStrengthCdma(signalStrength.cdma.dbm, signalStrength.cdma.ecio, signalStrength.evdo.dbm, signalStrength.evdo.ecio, signalStrength.evdo.signalNoiseRatio), new CellSignalStrengthGsm(signalStrength.gsm.signalStrength, signalStrength.gsm.bitErrorRate, signalStrength.gsm.timingAdvance), new CellSignalStrengthWcdma(signalStrength.wcdma.base.signalStrength, signalStrength.wcdma.base.bitErrorRate, signalStrength.wcdma.rscp, signalStrength.wcdma.ecno), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthLte(signalStrength.lte.signalStrength, signalStrength.lte.rsrp, signalStrength.lte.rsrq, signalStrength.lte.rssnr, signalStrength.lte.cqi, signalStrength.lte.timingAdvance), new CellSignalStrengthNr(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, signalStrength.nr.rsrp, signalStrength.nr.rsrq, signalStrength.nr.rssnr));
        ss.setPhoneId(phoneId);
        return ss;
    }

    public void setNrSaState(final int on, Message response) {
        invokeIRadio(2186, response, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass59 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio radioProxy13 = vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio.castFrom(radio);
                if (radioProxy13 != null) {
                    radioProxy13.setHwNrSaState(serial, on);
                }
            }
        });
    }

    public void getNrSaState(Message response) {
        invokeIRadio(2187, response, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass60 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio radioProxy13 = vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio.castFrom(radio);
                if (radioProxy13 != null) {
                    radioProxy13.getHwNrSaState(serial);
                }
            }
        });
    }

    public void setNrOptionMode(final int mode, Message msg) {
        invokeIRadio(2160, msg, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass61 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio radioProxy12 = vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio.castFrom(radio);
                if (radioProxy12 != null) {
                    radioProxy12.setHwNrOptionMode(serial, mode);
                }
            }
        });
    }

    public void getNrOptionMode(Message msg) {
        invokeIRadio(2161, msg, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass62 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio radioProxy12 = vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio.castFrom(radio);
                if (radioProxy12 != null) {
                    radioProxy12.getHwNrOptionMode(serial);
                }
            }
        });
    }

    public void sendMutiChipSessionConfig(final int sessionConfig, Message message) {
        invokeIRadio(2162, message, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass63 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio radioProxy12 = vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio.castFrom(radio);
                if (radioProxy12 != null) {
                    radioProxy12.sendMutiChipSessionConfig(serial, sessionConfig);
                }
            }
        });
    }

    public void sendVsimDataToModem(Message message) {
        invokeIRadio(2163, message, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass64 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio radioProxy12 = vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio.castFrom(radio);
                if (radioProxy12 != null) {
                    radioProxy12.sendVsimDataToModem(serial);
                }
            }
        });
    }

    public void getNrCellSsbId(Message msg) {
        invokeIRadio(2164, msg, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass65 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio radioProxy13 = vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio.castFrom(radio);
                if (radioProxy13 != null) {
                    radioProxy13.getHwNrSsbInfo(serial);
                }
            }
        });
    }

    public void processSmsAntiAttack(final int operationType, final int smsType, Message message) {
        invokeIRadio(2188, message, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass66 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio radioProxy13 = vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio.castFrom(radio);
                if (radioProxy13 != null) {
                    radioProxy13.processSmsAntiAttack(serial, operationType, smsType);
                }
            }
        });
    }

    public void setupDataCall(int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, Message result) {
        vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio radioProxy12;
        Exception e;
        boolean isNrSlicesSupported = HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported();
        riljLog("HwHisiRIL: setupDataCall");
        IHisiRadio radioProxy = getHisiRadioProxy(result);
        if (radioProxy == null) {
            radioProxy12 = null;
        } else {
            radioProxy12 = vendor.huawei.hardware.hisiradio.V1_2.IHisiRadio.castFrom(radioProxy);
        }
        if (!isNrSlicesSupported || radioProxy12 == null) {
            riljLog("HwHisiRIL: isNrSlicesSupported = false or radioProxy12 == null or radioProxy == null");
            HwHisiRIL.super.setupDataCall(accessNetworkType, dataProfile, isRoaming, allowRoaming, reason, linkProperties, result);
            return;
        }
        RILRequest rr = RILRequest.obtain(27, result, this.mRILDefaultWorkSource);
        addRequestEx(rr);
        ArrayList<String> addresses = new ArrayList<>();
        ArrayList<String> dnses = new ArrayList<>();
        if (linkProperties != null) {
            for (InetAddress address : linkProperties.getAddresses()) {
                addresses.add(address.getHostAddress());
            }
            for (InetAddress dns : linkProperties.getDnsServers()) {
                dnses.add(dns.getHostAddress());
            }
        }
        try {
            DataProfileInfo dpi = convertToHalDataProfile12(dataProfile);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ",accessNetworkType = " + accessNetworkType + ",isRoaming = " + isRoaming + ",allowRoaming = " + allowRoaming);
            try {
                radioProxy12.setupDataCall_1_2(rr.mSerial, accessNetworkType, dpi, allowRoaming, reason, addresses, dnses);
            } catch (RemoteException | RuntimeException e2) {
                e = e2;
            }
        } catch (RemoteException | RuntimeException e3) {
            e = e3;
            handleRadioProxyExceptionForRREx("setupDataCall", e, rr);
        }
    }

    public void setVoNrSwitch(final int status, Message msg) {
        invokeIRadio(2189, msg, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass67 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio radioProxy13 = vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio.castFrom(radio);
                if (radioProxy13 != null) {
                    radioProxy13.setVonrSwitch(serial, status);
                }
            }
        });
    }

    public void getRrcConnectionState(Message msg) {
        invokeIRadio(2199, msg, new HisiRILCommand() {
            /* class com.android.internal.telephony.HwHisiRIL.AnonymousClass68 */

            @Override // com.android.internal.telephony.HwHisiRIL.HisiRILCommand
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio radioProxy13 = vendor.huawei.hardware.hisiradio.V1_3.IHisiRadio.castFrom(radio);
                if (radioProxy13 != null) {
                    radioProxy13.getHwRrcConnectionState(serial);
                }
            }
        });
    }
}

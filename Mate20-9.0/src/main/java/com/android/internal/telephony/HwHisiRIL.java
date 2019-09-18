package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.CellInfo;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import com.android.internal.telephony.AbstractRIL;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

public class HwHisiRIL extends RIL {
    private static final int EVENT_RIL_CONNECTED = 100;
    private static final int PARATYPE_BASIC_COMM = 1;
    private static final int PARATYPE_CELLULAR_CLOUD = 2;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwHisiRIL";
    private static final boolean SHOW_4G_PLUS_ICON = SystemProperties.getBoolean("ro.config.hw_show_4G_Plus_icon", false);
    private static final int TYPEMASK_PARATYPE_BASIC_COMM = 0;
    private static final int TYPEMASK_PARATYPE_CELLULAR_CLOUD = 1;
    public static final boolean isVZW;
    protected int mApDsFlowConfig;
    protected int mApDsFlowOper;
    protected int mApDsFlowThreshold;
    protected int mApDsFlowTotalThreshold;
    protected int mDsFlowNvEnable;
    protected int mDsFlowNvInterval;
    private Handler mHisiRilHandler;
    HwHisiRadioIndication mHwHisiRadioIndication;
    HwHisiRadioResponse mHwHisiRadioResponse;
    private final BroadcastReceiver mIntentReceiver;
    private Integer mRilInstanceId;

    static {
        boolean z = false;
        if ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb"))) {
            z = true;
        }
        isVZW = z;
    }

    /* access modifiers changed from: private */
    public void setApTimeToCp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(14, -(calendar.get(15) + calendar.get(16)));
        Date utc = calendar.getTime();
        setTime(new SimpleDateFormat("yyyy/MM/dd").format(utc), new SimpleDateFormat("HH:mm:ss").format(utc), String.valueOf(TimeZone.getDefault().getRawOffset() / 3600000), null);
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRilInstanceId = null;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.TIME_SET".equals(intent.getAction()) || "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                    HwHisiRIL hwHisiRIL = HwHisiRIL.this;
                    hwHisiRIL.riljLog("mIntentReceiver onReceive " + intent.getAction());
                    HwHisiRIL.this.setApTimeToCp();
                }
            }
        };
        this.mHisiRilHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    HwHisiRIL.this.riljLog("EVENT_RIL_CONNECTED, set AP time to CP.");
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
        this.mHwHisiRadioResponse = new HwHisiRadioResponse(this);
        this.mHwHisiRadioIndication = new HwHisiRadioIndication(this);
        getHisiRadioProxy(null);
        this.mRilInstanceId = instanceId;
        registerIntentReceiver();
        registerForRilConnected(this.mHisiRilHandler, 100, null);
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    /* access modifiers changed from: protected */
    public Object processSolicitedEx(int rilRequest, Parcel p) {
        Object ret;
        Object ret2 = HwHisiRIL.super.processSolicitedEx(rilRequest, p);
        if (ret2 != null) {
            return ret2;
        }
        if (rilRequest == 2093) {
            ret = responseInts(p);
        } else if (rilRequest == 2108) {
            ret = responseVoid(p);
        } else if (rilRequest != 2132) {
            return ret2;
        } else {
            ret = responseInts(p);
        }
        return ret;
    }

    public void rejectCallForCause(final int gsmIndex, final int cause, Message result) {
        invokeIRadio(2171, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.rejectCallWithReason(serial, gsmIndex, cause);
            }
        });
    }

    public void queryCardType(Message result) {
        invokeIRadio(528, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCardType(serial);
            }
        });
    }

    public void getBalongSim(Message result) {
        invokeIRadio(2029, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimSlot(serial);
            }
        });
    }

    public void setActiveModemMode(final int mode, Message result) {
        invokeIRadio(2088, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setActiveModemMode(serial, mode);
            }
        });
    }

    public void switchBalongSim(final int modem1ToSlot, final int modem2ToSlot, final int modem3ToSlot, Message result) {
        invokeIRadio(2028, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                HwHisiRIL hwHisiRIL = HwHisiRIL.this;
                hwHisiRIL.riljLog("modem1ToSlot: " + modem1ToSlot + " modem2ToSlot: " + modem2ToSlot + " modem3ToSlot: " + modem3ToSlot);
                radio.setSimSlot(serial, modem1ToSlot, modem2ToSlot, modem3ToSlot);
            }
        });
    }

    public void getICCID(Message result) {
        invokeIRadio(2075, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getIccid(serial);
            }
        });
    }

    public void setLTEReleaseVersion(final int state, Message result) {
        invokeIRadio(2108, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setLteReleaseVersion(serial, state);
            }
        });
    }

    public void getLteReleaseVersion(Message result) {
        invokeIRadio(2109, result, new AbstractRIL.HisiRILCommand() {
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
        Rlog.d(RILJ_LOG_TAG, "notifyVpStatus: len = " + len);
        if (1 == len) {
            this.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult(null, data, null));
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
        Rlog.d(RILJ_LOG_TAG, sb.toString());
    }

    public void switchVoiceCallBackgroundState(final int state, Message result) {
        invokeIRadio(2019, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVoicecallBackGroundState(serial, state);
            }
        });
    }

    public void getLocationInfo(Message result) {
        invokeIRadio(534, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLocationInfo(serial);
            }
        });
    }

    public void queryServiceCellBand(Message result) {
        invokeIRadio(2129, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.queryServiceCellBand(serial);
            }
        });
    }

    public void getSimState(Message result) {
        invokeIRadio(2038, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getVsimSimState(serial);
            }
        });
    }

    public void setSimState(final int index, final int enable, Message result) {
        invokeIRadio(2037, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVsimSimState(serial, index, enable, -1);
            }
        });
    }

    public void hotSwitchSimSlot(final int modem0, final int modem1, final int modem2, Message result) {
        invokeIRadio(2094, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setSciChgCfg(serial, modem0, modem1, modem2);
            }
        });
    }

    public void getSimHotPlugState(Message result) {
        invokeIRadio(533, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimHotplugState(serial);
            }
        });
    }

    public void setUEOperationMode(final int mode, Message result) {
        invokeIRadio(2119, result, new AbstractRIL.HisiRILCommand() {
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
        invokeIRadio(2022, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setNetworkRatAndSrvDomain(serial, rat, srvDomain);
            }
        });
    }

    public void setHwVSimPower(int power, Message result) {
        invokeIRadio(2120, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vsimPower(serial);
            }
        });
    }

    public void setISMCOEX(final String ISMCoexContent, Message result) {
        invokeIRadio(2068, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setIsmcoex(serial, ISMCoexContent);
            }
        });
    }

    public void sendCloudMessageToModem(int event_id) {
        int mEventId = event_id;
        try {
            byte[] request = new byte[21];
            ByteBuffer buf = ByteBuffer.wrap(request);
            buf.order(ByteOrder.nativeOrder());
            buf.put("00000000".getBytes("utf-8"));
            buf.putInt(210);
            buf.putInt(5);
            buf.putInt(mEventId);
            buf.put((byte) 0);
            invokeOemRilRequestRaw(request, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            riljLog("HwCloudOTAService UnsupportedEncodingException");
        }
    }

    public void getRegPlmn(Message result) {
        invokeIRadio(2042, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getPlmnInfo(serial);
            }
        });
    }

    public void getModemSupportVSimVersion(Message result) {
        invokeIRadio(2131, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vsimBasebandVersion(serial);
            }
        });
    }

    public void setApDsFlowCfg(int config, int threshold, int total_threshold, int oper, Message result) {
        this.mApDsFlowConfig = config;
        this.mApDsFlowThreshold = threshold;
        this.mApDsFlowTotalThreshold = total_threshold;
        this.mApDsFlowOper = oper;
        final int i = config;
        final int i2 = threshold;
        final int i3 = total_threshold;
        final int i4 = oper;
        AnonymousClass24 r0 = new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setApDsFlowReportConfig(serial, i, i2, i3, i4);
            }
        };
        invokeIRadio(2110, result, r0);
    }

    public void setDsFlowNvCfg(final int enable, final int interval, Message result) {
        this.mDsFlowNvEnable = enable;
        this.mDsFlowNvInterval = interval;
        invokeIRadio(2112, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setDsFlowNvWriteConfigPara(serial, enable, interval);
            }
        });
    }

    public void setImsDomainConfig(final int selectDomain, Message result) {
        riljLog("setImsDomainConfig: " + selectDomain);
        invokeIRadio(2124, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setImsDomain(serial, selectDomain);
            }
        });
    }

    public void getImsDomain(Message result) {
        riljLog("getImsDomain");
        invokeIRadio(2126, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getImsDomain(serial);
            }
        });
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message result) {
        riljLog("handleUiccAuth");
        final RILUICCAUTH uiccAuth = new RILUICCAUTH();
        uiccAuth.authType = auth_type;
        String rand_str = IccUtils.bytesToHexString(rand);
        String auth_str = IccUtils.bytesToHexString(auth);
        if (rand_str != null) {
            uiccAuth.authParams.randLen = rand_str.length();
        }
        uiccAuth.authParams.rand = rand_str;
        if (auth_str != null) {
            uiccAuth.authParams.authLen = auth_str.length();
        }
        uiccAuth.authParams.auth = auth_str;
        invokeIRadio(2128, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.uiccAuth(serial, uiccAuth);
            }
        });
    }

    public void handleMapconImsaReq(byte[] Msg, Message result) {
        riljLog("handleMapconImsaReq: Msg = 0x" + IccUtils.bytesToHexString(Msg));
        final ArrayList<Byte> arrList = new ArrayList<>();
        for (byte valueOf : Msg) {
            arrList.add(Byte.valueOf(valueOf));
        }
        invokeIRadio(2125, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vowifiToImsaMsg(serial, arrList);
            }
        });
    }

    public void setTime(final String date, final String time, final String timezone, Message result) {
        if (date == null || time == null || timezone == null) {
            Rlog.e(RILJ_LOG_TAG, "setTime check");
        }
        invokeIRadio(2130, result, new AbstractRIL.HisiRILCommand() {
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
        if (1 == paratype) {
            invokeIRadio(2132, result, new AbstractRIL.HisiRILCommand() {
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    HwHisiRIL.this.riljLog("RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY");
                    radio.notifyCellularCommParaReady(serial, paratype, pathtype);
                }
            });
        }
        if (2 == paratype) {
            invokeIRadio(2133, result, new AbstractRIL.HisiRILCommand() {
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    HwHisiRIL.this.riljLog("RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY");
                    radio.notifyCellularCloudParaReady(serial, paratype, pathtype);
                }
            });
        }
    }

    public void send(RILRequestReference rr) {
        Rlog.d(RILJ_LOG_TAG, "not use socket send");
    }

    public void getLteFreqWithWlanCoex(Message result) {
        invokeIRadio(2087, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLwclash(serial);
            }
        });
    }

    public void sendPseudocellCellInfo(int infoType, int lac, int cid, int radiotech, String plmn, Message result) {
        final int i = infoType;
        final int i2 = lac;
        final int i3 = cid;
        final int i4 = radiotech;
        final String str = plmn;
        AnonymousClass34 r0 = new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendPseudocellCellInfo(serial, i, i2, i3, i4, str);
            }
        };
        invokeIRadio(2154, result, r0);
    }

    public void getAvailableCSGNetworks(Message result) {
        if (isVZW) {
            invokeIRadio(2167, result, new AbstractRIL.HisiRILCommand() {
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    radio.getAvailableCsgIds_1_1(serial);
                }
            });
        } else {
            invokeIRadio(2155, result, new AbstractRIL.HisiRILCommand() {
                public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                    radio.getAvailableCsgIds(serial);
                }
            });
        }
    }

    public void setCSGNetworkSelectionModeManual(Object csgInfo, Message result) {
        final CsgNetworkInfo hisiCsg = new CsgNetworkInfo();
        HwHisiCsgNetworkInfo tempCsg = (HwHisiCsgNetworkInfo) csgInfo;
        hisiCsg.csgId = tempCsg.getCSGId();
        hisiCsg.plmn = tempCsg.getOper();
        hisiCsg.networkRat = tempCsg.getRat();
        invokeIRadio(2156, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.manualSelectionCsgId(serial, hisiCsg);
            }
        });
    }

    public void setMobileDataEnable(final int state, Message response) {
        invokeIRadio(2165, response, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setMobileDataEnable(serial, state);
            }
        });
    }

    public void setRoamingDataEnable(final int state, Message response) {
        invokeIRadio(2166, response, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setRoamingDataEnable(serial, state);
            }
        });
    }

    public void sendLaaCmd(final int cmd, final String reserved, Message result) {
        invokeIRadio(2157, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendLaaCmd(serial, cmd, reserved);
            }
        });
    }

    public void getLaaDetailedState(final String reserved, Message result) {
        invokeIRadio(2158, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLaaDetailedState(serial, reserved);
            }
        });
    }

    public void setupEIMEDataCall(Message result) {
        riljLog("setupEIMEDataCall");
        invokeIRadio(2173, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setupDataCallEmergency(serial);
            }
        });
    }

    public void deactivateEIMEDataCall(Message result) {
        riljLog("deactivateEIMEDataCall");
        invokeIRadio(2174, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.deactivateDataCallEmergency(serial);
            }
        });
    }

    public void getEnhancedCellInfoList(Message result, WorkSource workSource) {
        riljLog("getEnhancedCellInfoList");
        invokeIRadio(2172, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCellInfoListOtdoa(serial);
            }
        });
    }

    public void setDeepNoDisturbState(final int state, Message result) {
        riljLog("setDeepNoDisturbState");
        invokeIRadio(2175, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setDeepNoDisturbState(serial, state);
            }
        });
    }

    public void setUplinkfreqEnable(final int state, Message result) {
        riljLog("setUplinkfreqEnable, requestid = 539");
        invokeIRadio(539, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio radioProxy11 = vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.setUlfreqEnable(serial, state);
                }
            }
        });
    }

    public void getCurrentCallsEx(Message result) {
        invokeIRadio(2176, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCurrentCallsV1_2(serial);
            }
        });
    }

    public void informModemTetherStatusToChangeGRO(final int enable, final String faceName, Message result) {
        invokeIRadio(538, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                Rlog.d(HwHisiRIL.RILJ_LOG_TAG, "enable = " + enable + " faceName = " + faceName);
                radio.informModemTetherStatusToChangeGRO(serial, enable, faceName);
            }
        });
    }

    public IHisiRadio getHisiRadioProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            riljLog("getHisiRadioProxy: Not calling getService(): wifi-only");
            if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mHisiRadioProxy != null) {
            return this.mHisiRadioProxy;
        } else {
            IHisiRadio hisiRadio = null;
            try {
                hisiRadio = IHisiRadio.getService(HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()], true);
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
                    AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                    result.sendToTarget();
                }
            }
            return this.mHisiRadioProxy;
        }
    }

    public void invokeIRadio(int requestId, Message result, AbstractRIL.HisiRILCommand cmd) {
        IHisiRadio radioProxy = getHisiRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(requestId, result, this.mRILDefaultWorkSource);
            addRequestEx(rr);
            riljLog(rr.serialString() + "> " + requestToString(requestId));
            try {
                cmd.excute(radioProxy, rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(requestId), e, rr);
            }
        }
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
            p2.writeInt(record.timeStampType);
            p2.writeLong(record.timeStamp);
            p2.writeInt(Integer.MAX_VALUE);
            switch (record.cellInfoType) {
                case 1:
                    it = it2;
                    i = 0;
                    p = p2;
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) record.gsm.get(0);
                    writeToParcelForGsm(p, cellInfoGsm.cellIdentityGsm.lac, cellInfoGsm.cellIdentityGsm.cid, cellInfoGsm.cellIdentityGsm.arfcn, Byte.toUnsignedInt(cellInfoGsm.cellIdentityGsm.bsic), cellInfoGsm.cellIdentityGsm.mcc, cellInfoGsm.cellIdentityGsm.mnc, "", "", cellInfoGsm.signalStrengthGsm.signalStrength, cellInfoGsm.signalStrengthGsm.bitErrorRate, cellInfoGsm.signalStrengthGsm.timingAdvance);
                    break;
                case 2:
                    it = it2;
                    p = p2;
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) record.cdma.get(0);
                    int i2 = cellInfoCdma.cellIdentityCdma.networkId;
                    int i3 = cellInfoCdma.cellIdentityCdma.systemId;
                    int i4 = cellInfoCdma.cellIdentityCdma.baseStationId;
                    int i5 = cellInfoCdma.cellIdentityCdma.longitude;
                    int i6 = cellInfoCdma.cellIdentityCdma.latitude;
                    int i7 = cellInfoCdma.signalStrengthCdma.dbm;
                    int i8 = cellInfoCdma.signalStrengthCdma.ecio;
                    int i9 = cellInfoCdma.signalStrengthEvdo.dbm;
                    int i10 = cellInfoCdma.signalStrengthEvdo.ecio;
                    int i11 = cellInfoCdma.signalStrengthEvdo.signalNoiseRatio;
                    CellInfoCdma cellInfoCdma2 = cellInfoCdma;
                    i = 0;
                    writeToParcelForCdma(p, i2, i3, i4, i5, i6, "", "", i7, i8, i9, i10, i11);
                    break;
                case 3:
                    CellInfoLte cellInfoLte = (CellInfoLte) record.lte.get(0);
                    int i12 = cellInfoLte.cellIdentityLte.ci;
                    int i13 = cellInfoLte.cellIdentityLte.pci;
                    int i14 = cellInfoLte.cellIdentityLte.tac;
                    int i15 = cellInfoLte.cellIdentityLte.earfcn;
                    String str = cellInfoLte.cellIdentityLte.mcc;
                    String str2 = cellInfoLte.cellIdentityLte.mnc;
                    int i16 = cellInfoLte.signalStrengthLte.signalStrength;
                    it = it2;
                    int i17 = i16;
                    CellInfoLte cellInfoLte2 = cellInfoLte;
                    p = p2;
                    writeToParcelForLte(p2, i12, i13, i14, i15, Integer.MAX_VALUE, str, str2, "", "", i17, cellInfoLte.signalStrengthLte.rsrp, cellInfoLte.signalStrengthLte.rsrq, cellInfoLte.signalStrengthLte.rssnr, cellInfoLte.signalStrengthLte.cqi, cellInfoLte.signalStrengthLte.timingAdvance);
                    i = 0;
                    break;
                case 4:
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) record.wcdma.get(0);
                    CellInfoWcdma cellInfoWcdma2 = cellInfoWcdma;
                    writeToParcelForWcdma(p2, cellInfoWcdma.cellIdentityWcdma.lac, cellInfoWcdma.cellIdentityWcdma.cid, cellInfoWcdma.cellIdentityWcdma.psc, cellInfoWcdma.cellIdentityWcdma.uarfcn, cellInfoWcdma.cellIdentityWcdma.mcc, cellInfoWcdma.cellIdentityWcdma.mnc, "", "", cellInfoWcdma.signalStrengthWcdma.signalStrength, cellInfoWcdma.signalStrengthWcdma.bitErrorRate);
                    it = it2;
                    i = 0;
                    p = p2;
                    break;
                default:
                    Parcel parcel = p2;
                    throw new RuntimeException("unexpected cellinfotype: " + record.cellInfoType);
            }
            Parcel p3 = p;
            p3.setDataPosition(i);
            p3.recycle();
            response.add((CellInfo) CellInfo.CREATOR.createFromParcel(p3));
            it2 = it;
        }
        return response;
    }

    public void sendSimChgTypeInfo(final int type, Message result) {
        riljLog("sendSimChgTypeInfo type:" + type);
        invokeIRadio(2178, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendSimChgTypeInfo(serial, type);
            }
        });
    }

    public boolean getAntiFakeBaseStation(Message response) {
        riljLog("getAntiFakeBaseStation ");
        invokeIRadio(2180, response, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCapOfRecPseBaseStation(serial);
            }
        });
        return true;
    }

    public void getCardTrayInfo(Message result) {
        riljLog("getCardTrayInfo");
        invokeIRadio(2181, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCardTrayInfo(serial);
            }
        });
    }

    public void getNvcfgMatchedResult(Message response) {
        riljLog("getNvcfgMatchedResult");
        invokeIRadio(2182, response, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getNvcfgMatchedResult(serial);
            }
        });
    }

    public void getAttachedApnSettings(Message response) {
        riljLog("getAttachedApnSettings");
        invokeIRadio(2185, response, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio radioProxy11 = vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.getAttachedApnSettings(serial);
                }
            }
        });
    }

    public void getSignalStrength(Message result) {
        if (getRILReference() != null) {
            getRILReference().getSignalStrength(result);
        }
    }

    public SignalStrength convertHalSignalStrength_1_1(HwSignalStrength_1_1 signalStrength, int phoneId) {
        HwSignalStrength_1_1 hwSignalStrength_1_1 = signalStrength;
        SignalStrength ss = new SignalStrength(hwSignalStrength_1_1.gsm.signalStrength, hwSignalStrength_1_1.gsm.bitErrorRate, hwSignalStrength_1_1.cdma.dbm, hwSignalStrength_1_1.cdma.ecio, hwSignalStrength_1_1.evdo.dbm, hwSignalStrength_1_1.evdo.ecio, hwSignalStrength_1_1.evdo.signalNoiseRatio, hwSignalStrength_1_1.lte.signalStrength, hwSignalStrength_1_1.lte.rsrp, hwSignalStrength_1_1.lte.rsrq, hwSignalStrength_1_1.lte.rssnr, hwSignalStrength_1_1.lte.cqi, hwSignalStrength_1_1.wcdma.rscp, hwSignalStrength_1_1.wcdma.base.signalStrength, hwSignalStrength_1_1.wcdma.rscp, -1, phoneId);
        ss.setNrSigStr(hwSignalStrength_1_1.nr.signalStrength, hwSignalStrength_1_1.nr.rsrp, hwSignalStrength_1_1.nr.rsrq, hwSignalStrength_1_1.nr.rssnr, hwSignalStrength_1_1.nr.cqi);
        return ss;
    }

    public void setPreferredNetworkType(int networkType, Message result) {
        if (HwModemCapability.isCapabilitySupport(29)) {
            riljLog("setPreferredNetworkType : is NR");
            IHisiRadio radioProxy = getHisiRadioProxy(result);
            if (radioProxy != null) {
                vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio radioProxy11 = vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio.castFrom(radioProxy);
                if (radioProxy11 != null) {
                    riljLog("setPreferredNetworkType : Hisi.V1_1 is not null.");
                    RILRequest rr = RILRequest.obtain(73, result, this.mRILDefaultWorkSource);
                    addRequestEx(rr);
                    riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " networkType = " + networkType);
                    this.mPreferredNetworkType = networkType;
                    custSetModemProperties();
                    TelephonyMetrics.getInstance().writeSetPreferredNetworkType(this.mPhoneId.intValue(), networkType);
                    try {
                        radioProxy11.setHwPreferredNetworkType_1_1(rr.mSerial, networkType);
                    } catch (RemoteException | RuntimeException e) {
                        handleRadioProxyExceptionForRREx("setPreferredNetworkType", e, rr);
                    }
                    return;
                }
                riljLog("ERROR: V1_1.IHisiRadio is null, will try to call super.setPreferredNetworkType().");
            }
        } else {
            riljLog("MODEM_CAP_SUPPORT_NR is false, will call super.setPreferredNetworkType().");
        }
        HwHisiRIL.super.setPreferredNetworkType(networkType, result);
    }

    public void setTemperatureControlToModem(final int level, final int type, Message result) {
        riljLog("setTemperatureControl");
        invokeIRadio(2159, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio radioProxy11 = vendor.huawei.hardware.hisiradio.V1_1.IHisiRadio.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.setTemperatureControl(serial, level, type);
                }
            }
        });
    }
}

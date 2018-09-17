package com.android.internal.telephony;

import android.telephony.HwVSimManager;
import com.huawei.chrfile.client.NcMetricConstant;
import huawei.android.app.admin.HwDeviceAdminInfo;
import huawei.android.pfw.HwPFWStartupPackageList;
import huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper;
import huawei.android.view.HwMotionEvent;
import huawei.com.android.internal.widget.HwFragmentContainer;

public class HwIccCardConstants {
    public static final String INTENT_VALUE_ICC_DEACTIVED = "DEACTIVED";
    public static final String INTENT_VALUE_ICC_SIM_REFRESH = "SIM_REFRESH";
    public static final String INTENT_VALUE_LOCKED_CORPORATE = "SIM CORPORATE";
    public static final String INTENT_VALUE_LOCKED_CORPORATE_PUK = "SIM LOCK CORPORATE BLOCK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_PUK = "SIM LOCK BLOCK";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET = "SIM NETWORK SUBSET";
    public static final String INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK = "SIM LOCK NETWORK SUBSET BLOCK";
    public static final String INTENT_VALUE_LOCKED_RUIM_CORPORATE = "RUIM CORPORATE";
    public static final String INTENT_VALUE_LOCKED_RUIM_HRPD = "RUIM HRPD";
    public static final String INTENT_VALUE_LOCKED_RUIM_NETWORK1 = "RUIM NETWORK1";
    public static final String INTENT_VALUE_LOCKED_RUIM_NETWORK2 = "RUIM NETWORK2";
    public static final String INTENT_VALUE_LOCKED_RUIM_RUIM = "RUIM RUIM";
    public static final String INTENT_VALUE_LOCKED_RUIM_SERVICE_PROVIDER = "RUIM SERVICE PROVIDER";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER = "SIM SERVICE PROVIDER";
    public static final String INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK = "SIM LOCK SERVICE PROVIDERBLOCK";
    public static final String INTENT_VALUE_LOCKED_SIM = "SIM SIM";

    public enum HwState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwIccCardConstants.HwState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwIccCardConstants.HwState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwIccCardConstants.HwState.<clinit>():void");
        }

        public String getIntentString() {
            switch (-getcom-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues()[ordinal()]) {
                case HwFragmentContainer.TRANSITION_FADE /*1*/:
                    return "ABSENT";
                case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                    return "CARD_IO_ERROR";
                case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                    return HwIccCardConstants.INTENT_VALUE_ICC_DEACTIVED;
                case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                case HuaweiTelephonyManagerWrapper.SINGLE_MODE_SIM_CARD /*10*/:
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_APN /*11*/:
                case HwVSimManager.NETWORK_TYPE_EVDO_B /*12*/:
                case HwVSimManager.NETWORK_TYPE_LTE /*13*/:
                case NcMetricConstant.GPS_METRIC_ID /*14*/:
                case NcMetricConstant.WIFI_METRIC_ID /*15*/:
                case PduHeaders.MMS_VERSION_1_0 /*16*/:
                case HwVSimManager.NETWORK_TYPE_TDS /*17*/:
                case PduHeaders.MMS_VERSION_1_2 /*18*/:
                case PduHeaders.MMS_VERSION_1_3 /*19*/:
                case HuaweiTelephonyManagerWrapper.SINGLE_MODE_USIM_CARD /*20*/:
                case 21:
                case 22:
                case 23:
                    return "LOCKED";
                case HwMotionEvent.TOOL_TYPE_FINGER_TIP /*5*/:
                    return "NOT_READY";
                case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                    return "LOCKED";
                case HwMotionEvent.TOOL_TYPE_FINGER_KNUCKLE /*7*/:
                    return "LOCKED";
                case HwMotionEvent.TOOL_TYPE_BEZEL /*8*/:
                    return "LOCKED";
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_EMAIL /*9*/:
                    return "READY";
                default:
                    return "UNKNOWN";
            }
        }

        public String getReason() {
            switch (-getcom-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues()[ordinal()]) {
                case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                    return "CARD_IO_ERROR";
                case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                    return "NETWORK";
                case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                    return "PERM_DISABLED";
                case HwMotionEvent.TOOL_TYPE_FINGER_KNUCKLE /*7*/:
                    return "PIN";
                case HwMotionEvent.TOOL_TYPE_BEZEL /*8*/:
                    return "PUK";
                case HuaweiTelephonyManagerWrapper.SINGLE_MODE_SIM_CARD /*10*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_CORPORATE;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_APN /*11*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_HRPD;
                case HwVSimManager.NETWORK_TYPE_EVDO_B /*12*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK1;
                case HwVSimManager.NETWORK_TYPE_LTE /*13*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_NETWORK2;
                case NcMetricConstant.GPS_METRIC_ID /*14*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_RUIM;
                case NcMetricConstant.WIFI_METRIC_ID /*15*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_RUIM_SERVICE_PROVIDER;
                case PduHeaders.MMS_VERSION_1_0 /*16*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE;
                case HwVSimManager.NETWORK_TYPE_TDS /*17*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_CORPORATE_PUK;
                case PduHeaders.MMS_VERSION_1_2 /*18*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_PUK;
                case PduHeaders.MMS_VERSION_1_3 /*19*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET;
                case HuaweiTelephonyManagerWrapper.SINGLE_MODE_USIM_CARD /*20*/:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET_PUK;
                case 21:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER;
                case 22:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER_PUK;
                case 23:
                    return HwIccCardConstants.INTENT_VALUE_LOCKED_SIM;
                default:
                    return null;
            }
        }
    }

    public HwIccCardConstants() {
    }
}

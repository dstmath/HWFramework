package ohos.dcall;

import android.content.Context;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;
import ohos.utils.net.Uri;

public class AospCallAdapter {
    private static final int LOG_DOMAIN_DCALL = 218111744;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218111744, TAG);
    private static final String TAG = "AospCallAdapter";
    private static volatile AospCallAdapter sInstance;
    private static boolean sNativeAvailable;
    private Context mContext;
    private AospInCallService mService;

    private native void nativeInitDCALL();

    static {
        sNativeAvailable = false;
        try {
            HiLog.info(LOG_LABEL, "AospCallAdapter load libdcall_jni.z.so", new Object[0]);
            System.loadLibrary("dcall_jni.z");
            sNativeAvailable = true;
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LOG_LABEL, "AospCallAdapter Could not load libdcall_jni.z.so", new Object[0]);
        }
    }

    public static AospCallAdapter getInstance() {
        if (sInstance == null) {
            synchronized (AospCallAdapter.class) {
                if (sInstance == null) {
                    HiLog.info(LOG_LABEL, "AospCallAdapter: new AospCallAdapter", new Object[0]);
                    sInstance = new AospCallAdapter();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        if (!sNativeAvailable) {
            HiLog.error(LOG_LABEL, "init error! libdcall_jni.z.so is not available", new Object[0]);
            return;
        }
        nativeInitDCALL();
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
    }

    /* access modifiers changed from: package-private */
    public void setAospInCallService(AospInCallService aospInCallService) {
        this.mService = aospInCallService;
    }

    /* access modifiers changed from: package-private */
    public AospInCallService getAospInCallService() {
        return this.mService;
    }

    static AospInCallService getInCallService() {
        return getInstance().getAospInCallService();
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    static boolean answerCall(int i, int i2) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.answer(i, i2);
        }
        HiLog.error(LOG_LABEL, "answerCall fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean disconnectCall(int i) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.disconnect(i);
        }
        HiLog.error(LOG_LABEL, "disconnectCall fail, no InCallService.", new Object[0]);
        CallAppAbilityProxy instance = CallAppAbilityProxy.getInstance();
        if (instance != null) {
            return instance.disconnectPreAddedCall(i);
        }
        return false;
    }

    static boolean playDtmfTone(int i, int i2) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.playDtmfTone(i, (char) i2);
        }
        HiLog.error(LOG_LABEL, "playDtmfTone fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean stopDtmfTone(int i) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.stopDtmfTone(i);
        }
        HiLog.error(LOG_LABEL, "stopDtmfTone fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean postDialContinue(int i, boolean z) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.postDialContinue(i, z);
        }
        HiLog.error(LOG_LABEL, "postDialContinue fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean rejectCall(int i, boolean z, String str) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.reject(i, z, str);
        }
        HiLog.error(LOG_LABEL, "rejectCall fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean distributeCallEvent(int i, String str, HashMap<String, Object> hashMap) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.sendCallEvent(i, str, PacMapUtils.convertIntoBundle(convertHashMapToPacMap(hashMap)));
        }
        HiLog.error(LOG_LABEL, "distributeCallEvent fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean canAddCall() {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            boolean canAddCall = inCallService.canAddCall();
            HiLog.info(LOG_LABEL, "canAddCall, %{public}b.", new Object[]{Boolean.valueOf(canAddCall)});
            return canAddCall;
        }
        HiLog.error(LOG_LABEL, "canAddCall fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean setMuted(boolean z) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.setMuted(z);
            return true;
        }
        HiLog.error(LOG_LABEL, "setMuted fail, no InCallService", new Object[0]);
        return false;
    }

    static boolean setAudioRoute(int i) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            inCallService.setAudioRoute(i);
            return true;
        }
        HiLog.error(LOG_LABEL, "setAudioRoute fail, no InCallService", new Object[0]);
        return false;
    }

    static boolean holdCall(int i) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.hold(i);
        }
        HiLog.error(LOG_LABEL, "holdCall fail, no InCallService.", new Object[0]);
        return false;
    }

    static boolean unHoldCall(int i) {
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.unHold(i);
        }
        HiLog.error(LOG_LABEL, "unHoldCall fail, no InCallService.", new Object[0]);
        return false;
    }

    static List<String> getPredefinedRejectMessages(int i) {
        ArrayList arrayList = new ArrayList();
        AospInCallService inCallService = getInCallService();
        if (inCallService != null) {
            return inCallService.getPredefinedRejectMessages(i);
        }
        HiLog.error(LOG_LABEL, "getPredefinedRejectMessages fail, no InCallService.", new Object[0]);
        return arrayList;
    }

    static boolean dialWithExtras(String str, HashMap<String, Object> hashMap) {
        TelecomManager telecomManager = getTelecomManager();
        if (telecomManager == null) {
            HiLog.error(LOG_LABEL, "dialWithExtras fail, no telecomManager.", new Object[0]);
            return false;
        }
        preOnCallAdded(str, hashMap);
        telecomManager.placeCall(convertToAospUri(convertToOhosUri(str)), PacMapUtils.convertIntoBundle(convertHashMapToPacMap(hashMap)));
        return true;
    }

    static boolean initDialEnv(HashMap<String, Object> hashMap) {
        if (hashMap == null) {
            HiLog.error(LOG_LABEL, "initDialEnv: no extras.", new Object[0]);
            return false;
        }
        Object obj = hashMap.get(DistributedCallManager.PRE_CONNECT_ABILITY);
        boolean booleanValue = obj instanceof Boolean ? ((Boolean) obj).booleanValue() : false;
        int i = -1;
        Object obj2 = hashMap.get("CALL_TYPE");
        if (obj2 instanceof Integer) {
            i = ((Integer) obj2).intValue();
        }
        Object obj3 = hashMap.get("CALL_COMPONENT_NAME");
        String str = null;
        String str2 = obj3 instanceof String ? (String) obj3 : null;
        Object obj4 = hashMap.get("CALL_ABILITY_NAME");
        if (obj4 instanceof String) {
            str = (String) obj4;
        }
        HiLog.info(LOG_LABEL, "initDialEnv, isPreBind %{public}b, callType %{public}d", new Object[]{Boolean.valueOf(booleanValue), Integer.valueOf(i)});
        if (str2 == null) {
            HiLog.error(LOG_LABEL, "initDialEnv: no call component name.", new Object[0]);
            return false;
        } else if (str == null) {
            HiLog.error(LOG_LABEL, "initDialEnv: no call ability name.", new Object[0]);
            return false;
        } else {
            CallAppAbilityProxy instance = CallAppAbilityProxy.getInstance();
            if (instance == null) {
                HiLog.error(LOG_LABEL, "initDialEnv: no proxy.", new Object[0]);
                return false;
            }
            instance.setContext(getInstance().getContext());
            if (booleanValue) {
                instance.preConnectCallAppAbility(i, str2, str);
            } else {
                instance.preDisconnectCallAppAbility(i, str2, str);
            }
            return true;
        }
    }

    static boolean preOnCallAdded(String str, HashMap<String, Object> hashMap) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        String numberFromAddress = getNumberFromAddress(str);
        if (TextUtils.isEmpty(numberFromAddress)) {
            HiLog.error(LOG_LABEL, "preOnCallAdded: no number", new Object[0]);
            return false;
        } else if (hashMap == null) {
            HiLog.error(LOG_LABEL, "preOnCallAdded: no extras.", new Object[0]);
            return false;
        } else {
            Object obj = hashMap.get(DistributedCallManager.PRE_ON_CALL_CREATED);
            boolean booleanValue = obj instanceof Boolean ? ((Boolean) obj).booleanValue() : false;
            if (!booleanValue) {
                HiLog.info(LOG_LABEL, "preOnCallAdded: no need pre add call.", new Object[0]);
                return false;
            }
            int i = -1;
            Object obj2 = hashMap.get("CALL_TYPE");
            if (obj2 instanceof Integer) {
                i = ((Integer) obj2).intValue();
            }
            Object obj3 = hashMap.get("CALL_COMPONENT_NAME");
            String str2 = obj3 instanceof String ? (String) obj3 : null;
            Object obj4 = hashMap.get("CALL_ABILITY_NAME");
            String str3 = obj4 instanceof String ? (String) obj4 : null;
            HiLog.info(LOG_LABEL, "preOnCallAdded, isPreOnCallAdded %{public}b, callType %{public}d", new Object[]{Boolean.valueOf(booleanValue), Integer.valueOf(i)});
            Object obj5 = hashMap.get("android.telecom.extra.START_CALL_WITH_VIDEO_STATE");
            int intValue = obj5 instanceof Integer ? ((Integer) obj5).intValue() : 0;
            CallAppAbilityProxy instance = CallAppAbilityProxy.getInstance();
            if (instance == null) {
                return false;
            }
            instance.setContext(getInstance().getContext());
            return instance.preOnCallAdded(numberFromAddress, intValue, i, str2, str3);
        }
    }

    static String getNumberFromAddress(String str) {
        Uri convertToOhosUri;
        if (!TextUtils.isEmpty(str) && (convertToOhosUri = convertToOhosUri(str)) != null) {
            return convertToOhosUri.getDecodedSchemeSpecificPart();
        }
        return "";
    }

    static TelecomManager getTelecomManager() {
        Context context = getInstance().getContext();
        if (context == null) {
            HiLog.error(LOG_LABEL, "getTelecomManager fail, no context.", new Object[0]);
            return null;
        }
        Object systemService = context.getSystemService("telecom");
        if (systemService instanceof TelecomManager) {
            return (TelecomManager) systemService;
        }
        return null;
    }

    static Uri convertToOhosUri(String str) {
        if (str != null) {
            try {
                return Uri.parse(str);
            } catch (NullPointerException unused) {
                HiLog.error(LOG_LABEL, "convertToOhosUri fail, got NullPointerException", new Object[0]);
            } catch (IllegalArgumentException unused2) {
                HiLog.error(LOG_LABEL, "convertToOhosUri fail, got IllegalArgumentException", new Object[0]);
            }
        }
        return null;
    }

    static android.net.Uri convertToAospUri(Uri uri) {
        if (uri != null) {
            try {
                return UriConverter.convertToAndroidUri(uri);
            } catch (NullPointerException unused) {
                HiLog.error(LOG_LABEL, "convertToAospUri fail, got NullPointerException", new Object[0]);
            } catch (IllegalArgumentException unused2) {
                HiLog.error(LOG_LABEL, "convertToAospUri fail, got IllegalArgumentException", new Object[0]);
            }
        }
        return null;
    }

    static PacMap convertHashMapToPacMap(HashMap<String, Object> hashMap) {
        if (hashMap == null) {
            return null;
        }
        PacMap pacMap = new PacMap();
        pacMap.putAll(hashMap);
        return pacMap;
    }

    private AospCallAdapter() {
        HiLog.info(LOG_LABEL, "constructor", new Object[0]);
    }
}

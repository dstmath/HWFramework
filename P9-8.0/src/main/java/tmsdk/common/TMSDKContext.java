package tmsdk.common;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.TrafficCorrectionManager;
import tmsdk.common.utils.f;
import tmsdk.common.utils.o;
import tmsdkobf.gf;
import tmsdkobf.gw;
import tmsdkobf.ij;
import tmsdkobf.im;
import tmsdkobf.ir;
import tmsdkobf.kr;
import tmsdkobf.kt;
import tmsdkobf.md;
import tmsdkobf.ob;
import tmsdkobf.pu;

public final class TMSDKContext {
    public static final String PRE_HTTP_SERVER_URL = "http_server_url";
    public static final String PRE_IS_TEST = "is_t";
    public static final String PRE_LIB_PATH = "pre_lib_path";
    public static final String PRE_TCP_SERVER_ADDRESS = "tcp_server_address";
    public static final String PRE_USE_IP_LIST = "use_ip_list";
    private static final String SDK_VERSION_INFO = "6.1.0 20180205154202";
    private static final String SDK_VERSION_MFR = "6.1.0";
    private static final String TAG = "TMSDKContext";
    private static Context sApplication;
    private static Context sCurrentApplication;
    private static Map<String, String> sEnvMap = new HashMap();
    private static boolean sInitialized = false;
    private static Class<? extends TMSService> sSecureServiceClass;

    static {
        sEnvMap.put(PRE_HTTP_SERVER_URL, "http://pmir.3g.qq.com");
        sEnvMap.put(PRE_TCP_SERVER_ADDRESS, "mazu.3g.qq.com");
        sEnvMap.put(PRE_USE_IP_LIST, "true");
        sEnvMap.put(PRE_IS_TEST, "false");
        sEnvMap.put(PRE_LIB_PATH, null);
    }

    public static boolean checkLisence() {
        return ir.bU().bS();
    }

    private static native int doRegisterNatives(int i, Class<?> cls);

    public static Context getApplicaionContext() {
        return sApplication.getApplicationContext();
    }

    public static Context getCurrentContext() {
        return sCurrentApplication;
    }

    private static native int getProcBitStatus();

    public static String getSDKVersionInfo() {
        return SDK_VERSION_INFO;
    }

    public static String getStrFromEnvMap(String str) {
        Class cls = TMSDKContext.class;
        synchronized (TMSDKContext.class) {
            String str2 = (String) sEnvMap.get(str);
            return str2;
        }
    }

    public static synchronized <T extends TMSService> boolean init(Context context, Class<T> cls, ITMSApplicaionConfig iTMSApplicaionConfig) {
        synchronized (TMSDKContext.class) {
            if (isInitialized()) {
                return true;
            } else if (context != null) {
                f.d(TAG, "TMSDK version=" + getSDKVersionInfo());
                sApplication = context.getApplicationContext();
                sSecureServiceClass = cls;
                if (o.iY()) {
                    Class cls2 = TMSDKContext.class;
                    synchronized (TMSDKContext.class) {
                        if (iTMSApplicaionConfig != null) {
                            Map config = iTMSApplicaionConfig.config(new HashMap(sEnvMap));
                            String str = (String) config.get(PRE_HTTP_SERVER_URL);
                            if (!TextUtils.isEmpty(str)) {
                                sEnvMap.put(PRE_HTTP_SERVER_URL, str);
                            }
                            String str2 = (String) config.get(PRE_TCP_SERVER_ADDRESS);
                            if (!TextUtils.isEmpty(str2)) {
                                sEnvMap.put(PRE_TCP_SERVER_ADDRESS, str2);
                            }
                            sEnvMap.put(PRE_USE_IP_LIST, config.get(PRE_USE_IP_LIST));
                            sEnvMap.put(PRE_IS_TEST, config.get(PRE_IS_TEST));
                            String str3 = (String) config.get(PRE_LIB_PATH);
                            if (!TextUtils.isEmpty(str3)) {
                                sEnvMap.put(PRE_LIB_PATH, str3);
                            }
                        }
                        if (im.bH()) {
                            gw.be();
                            ob.gy();
                            kr.init();
                            im.bJ().addTask(new Runnable() {
                                public void run() {
                                    try {
                                        TMSDKContext.start();
                                        ManagerCreatorB.getManager(TrafficCorrectionManager.class);
                                        if (im.bG()) {
                                            im.bP();
                                        }
                                        Thread.sleep(30000);
                                    } catch (InterruptedException e) {
                                    }
                                    if (gf.S().ag().booleanValue()) {
                                        pu.hW().hY();
                                    }
                                }
                            }, "icheck");
                            sInitialized = true;
                            return true;
                        }
                        return false;
                    }
                }
                sApplication = null;
                throw new RuntimeException("tms cannot proguard!");
            } else {
                throw new RuntimeException("contxt is null when TMSDK init!");
            }
        }
    }

    public static boolean isInitialized() {
        return sInitialized;
    }

    public static boolean is_arm64v8a() {
        return getProcBitStatus() == 109;
    }

    public static boolean is_armeabi() {
        int procBitStatus = getProcBitStatus();
        return procBitStatus >= 100 && procBitStatus <= 104;
    }

    public static void onImsiChanged() {
        im.bP();
    }

    public static void registerNatives(int i, Class<?> cls) {
        im.bI();
        int doRegisterNatives = doRegisterNatives(i, cls);
        if (doRegisterNatives != 0) {
            throw new UnsatisfiedLinkError("Failed to register " + cls.toString() + "(err=" + doRegisterNatives + ")");
        }
    }

    public static void reportChannelInfo() {
        ij.reportChannelInfo();
    }

    public static void requestDelUserData() {
        im.requestDelUserData();
    }

    public static void setAutoConnectionSwitch(boolean z) {
        im.setAutoConnectionSwitch(z);
        if (sApplication != null) {
            if (im.bG()) {
                kt.aE(29987);
            } else {
                kt.aE(29988);
            }
            if (z && isInitialized()) {
                reportChannelInfo();
                kr.dz();
            }
        }
    }

    public static void setCurrentContext(Context context) {
        sCurrentApplication = context;
    }

    public static void setDualPhoneInfoFetcher(IDualPhoneInfoFetcher iDualPhoneInfoFetcher) {
        im.setDualPhoneInfoFetcher(iDualPhoneInfoFetcher);
    }

    public static void setTMSDKLogEnable(boolean z) {
        f.Q(z);
    }

    private static void start() {
        if (sSecureServiceClass != null) {
            Context context = sCurrentApplication;
            if (context == null) {
                context = sApplication;
            }
            context.startService(new Intent(context, sSecureServiceClass));
        }
        new md("tms").a("reportlc", false, true);
        try {
            int i = getApplicaionContext().getApplicationInfo().uid;
            i = ((Integer) Class.forName(UserHandle.class.getName()).getMethod("getAppId", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(i)})).intValue();
            kt.f(1320066, "" + i);
            f.d(TAG, "appId:[" + i + "]");
        } catch (Throwable th) {
            f.d(TAG, "e:[" + th + "]");
        }
        if (im.bG()) {
            reportChannelInfo();
            kr.dz();
        }
    }

    public static boolean startPersistentLink() {
        if (im.bK() == null) {
            return false;
        }
        im.bK().gC();
        return true;
    }

    public static void stopPersistentLink() {
        im.bK().gD();
    }
}

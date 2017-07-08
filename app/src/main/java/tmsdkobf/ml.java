package tmsdkobf;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.telephony.TelephonyManager;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class ml {
    public static int AX;
    public static String AY;
    public static int AZ;
    public static byte Ba;
    public static boolean Bb;
    public static boolean Bc;
    public static byte Bd;
    public static String Be;
    public static byte Bf;
    public static int Bg;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ml.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ml.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ml.<clinit>():void");
    }

    private static int a(Context context, NetworkInfo networkInfo) {
        int i = 1;
        if (networkInfo == null) {
            return 0;
        }
        try {
            if (1 != networkInfo.getType()) {
                if (networkInfo.getType() == 0) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (telephonyManager != null) {
                        switch (telephonyManager.getNetworkType()) {
                            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                                i = 2;
                                break;
                            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                                i = 3;
                                break;
                            case FileInfo.TYPE_BIGFILE /*3*/:
                                i = 4;
                                break;
                            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                                i = 8;
                                break;
                            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                                i = 9;
                                break;
                            case UrlCheckType.TIPS_CHEAT /*6*/:
                                i = 10;
                                break;
                            case UrlCheckType.TIPS_DEFAULT /*7*/:
                                i = 11;
                                break;
                            case RubbishType.SCAN_FLAG_APK /*8*/:
                                i = 5;
                                break;
                            case UrlCheckType.MAKE_MONEY /*9*/:
                                i = 6;
                                break;
                            case UrlCheckType.SEX /*10*/:
                                i = 7;
                                break;
                            default:
                                i = 17;
                                break;
                        }
                    }
                }
                i = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    private static boolean bD(int i) {
        return i == 2 || i == 0;
    }

    private static void cv(String str) {
        if (str != null) {
            if (str.contains("cmwap")) {
                Be = "cmwap";
                Bf = (byte) 0;
            } else if (str.contains("cmnet")) {
                Be = "cmnet";
                Bf = (byte) 1;
            } else if (str.contains("3gwap")) {
                Be = "3gwap";
                Bf = (byte) 2;
            } else if (str.contains("3gnet")) {
                Be = "3gnet";
                Bf = (byte) 3;
            } else if (str.contains("uniwap")) {
                Be = "uniwap";
                Bf = (byte) 4;
            } else if (str.contains("uninet")) {
                Be = "uninet";
                Bf = (byte) 5;
            } else if (str.contains("ctwap")) {
                Be = "ctwap";
                Bf = (byte) 6;
            } else if (str.contains("ctnet")) {
                Be = "ctnet";
                Bf = (byte) 7;
            } else if (str.contains("#777")) {
                Be = "#777";
                Bf = (byte) 8;
            }
        }
    }

    public static void init(Context context) {
        NetworkInfo activeNetworkInfo;
        String str = null;
        try {
            activeNetworkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            activeNetworkInfo = null;
        }
        d.e("Apn", "networkInfo : " + activeNetworkInfo);
        int i = -1;
        try {
            AX = 0;
            Bd = (byte) 4;
            if (activeNetworkInfo != null) {
                i = activeNetworkInfo.getType();
                d.e("Apn", "type: " + activeNetworkInfo.getType());
                d.e("Apn", "typeName: " + activeNetworkInfo.getTypeName());
                str = activeNetworkInfo.getExtraInfo();
                if (str != null) {
                    str = str.trim().toLowerCase();
                } else {
                    AX = 0;
                }
            }
            d.e("Apn", "extraInfo : " + str);
            if (i != 1) {
                cv(str);
                if (str == null) {
                    AX = 0;
                } else if (str.contains("cmwap") || str.contains("uniwap") || str.contains("3gwap") || str.contains("ctwap")) {
                    Bd = (byte) 1;
                    if (str.contains("3gwap")) {
                        Bd = (byte) 2;
                    }
                    AX = 2;
                } else if (str.contains("cmnet") || str.contains("uninet") || str.contains("3gnet") || str.contains("ctnet")) {
                    Bd = (byte) 1;
                    if (str.contains("3gnet") || str.contains("ctnet")) {
                        Bd = (byte) 2;
                    }
                    AX = 1;
                } else if (str.contains("#777")) {
                    Bd = (byte) 2;
                    AX = 0;
                } else {
                    AX = 0;
                }
                Bb = false;
                if (bD(AX)) {
                    AY = Proxy.getDefaultHost();
                    AZ = Proxy.getDefaultPort();
                    if (AY != null) {
                        AY = AY.trim();
                    }
                    if (AY == null || "".equals(AY)) {
                        Bb = false;
                        AX = 1;
                    } else {
                        Bb = true;
                        AX = 2;
                        if ("10.0.0.200".equals(AY)) {
                            Ba = (byte) 1;
                        } else {
                            Ba = (byte) 0;
                        }
                    }
                }
            } else {
                AX = 4;
                Bb = false;
                Bd = (byte) 3;
                Be = "unknown";
                Bf = (byte) 9;
            }
            d.e("Apn", "NETWORK_TYPE : " + Bd);
            d.e("Apn", "M_APN_TYPE : " + AX);
            d.e("Apn", "M_USE_PROXY : " + Bb);
            d.e("Apn", "M_APN_PROXY : " + AY);
            d.e("Apn", "M_APN_PORT : " + AZ);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        Bg = a(context, activeNetworkInfo);
        d.d("Apn", "init() Apn.APN_NAME_VALUE: " + Bf + " APN_NAME_DRI: " + Be + " NETWORK_TYPE: " + Bd + " ENT_VALUE: " + Bg);
    }

    public static void n(Context context) {
        if (!Bc) {
            synchronized (ml.class) {
                if (Bc) {
                    return;
                }
                init(context);
                Bc = true;
            }
        }
    }
}

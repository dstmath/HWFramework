package tmsdkobf;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.telephony.TelephonyManager;
import tmsdk.common.TMServiceFactory;

public class ln {
    public static int yI = 4;
    public static String yJ = null;
    public static int yK = 80;
    public static byte yL = (byte) 0;
    public static boolean yM = false;
    public static boolean yN = false;
    public static byte yO = (byte) 4;
    public static String yP = "unknown";
    public static byte yQ = (byte) 9;
    public static int yR = 17;

    private static int a(Context context, NetworkInfo networkInfo) {
        int i = 0;
        if (networkInfo == null) {
            return 0;
        }
        try {
            if (1 != networkInfo.getType()) {
                if (networkInfo.getType() == 0) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (telephonyManager != null) {
                        switch (telephonyManager.getNetworkType()) {
                            case 1:
                                i = 2;
                                break;
                            case 2:
                                i = 3;
                                break;
                            case 3:
                                i = 4;
                                break;
                            case 4:
                                i = 8;
                                break;
                            case 5:
                                i = 9;
                                break;
                            case 6:
                                i = 10;
                                break;
                            case 7:
                                i = 11;
                                break;
                            case 8:
                                i = 5;
                                break;
                            case 9:
                                i = 6;
                                break;
                            case 10:
                                i = 7;
                                break;
                            default:
                                i = 17;
                                break;
                        }
                    }
                }
            }
            i = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    private static boolean aN(int i) {
        return i == 2 || i == 0;
    }

    private static void bI(String str) {
        if (str != null) {
            if (str.contains("cmwap")) {
                yP = "cmwap";
                yQ = (byte) 0;
            } else if (str.contains("cmnet")) {
                yP = "cmnet";
                yQ = (byte) 1;
            } else if (str.contains("3gwap")) {
                yP = "3gwap";
                yQ = (byte) 2;
            } else if (str.contains("3gnet")) {
                yP = "3gnet";
                yQ = (byte) 3;
            } else if (str.contains("uniwap")) {
                yP = "uniwap";
                yQ = (byte) 4;
            } else if (str.contains("uninet")) {
                yP = "uninet";
                yQ = (byte) 5;
            } else if (str.contains("ctwap")) {
                yP = "ctwap";
                yQ = (byte) 6;
            } else if (str.contains("ctnet")) {
                yP = "ctnet";
                yQ = (byte) 7;
            } else if (str.contains("#777")) {
                yP = "#777";
                yQ = (byte) 8;
            }
        }
    }

    public static void init(Context context) {
        NetworkInfo networkInfo = null;
        try {
            networkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            mb.s("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
        }
        mb.d("Apn", "networkInfo : " + networkInfo);
        int i = -1;
        try {
            yI = 0;
            yO = (byte) 4;
            String str = null;
            if (networkInfo != null) {
                i = networkInfo.getType();
                mb.d("Apn", "type: " + networkInfo.getType());
                mb.d("Apn", "typeName: " + networkInfo.getTypeName());
                str = networkInfo.getExtraInfo();
                if (str != null) {
                    str = str.trim().toLowerCase();
                } else {
                    yI = 0;
                }
            }
            mb.d("Apn", "extraInfo : " + str);
            if (i != 1) {
                bI(str);
                if (str == null) {
                    yI = 0;
                } else if (str.contains("cmwap") || str.contains("uniwap") || str.contains("3gwap") || str.contains("ctwap")) {
                    yO = (byte) 1;
                    if (str.contains("3gwap")) {
                        yO = (byte) 2;
                    }
                    yI = 2;
                } else if (str.contains("cmnet") || str.contains("uninet") || str.contains("3gnet") || str.contains("ctnet")) {
                    yO = (byte) 1;
                    if (str.contains("3gnet") || str.contains("ctnet")) {
                        yO = (byte) 2;
                    }
                    yI = 1;
                } else if (str.contains("#777")) {
                    yO = (byte) 2;
                    yI = 0;
                } else {
                    yI = 0;
                }
                yM = false;
                if (aN(yI)) {
                    yJ = Proxy.getDefaultHost();
                    yK = Proxy.getDefaultPort();
                    if (yJ != null) {
                        yJ = yJ.trim();
                    }
                    if (yJ != null) {
                        if (!"".equals(yJ)) {
                            yM = true;
                            yI = 2;
                            if ("10.0.0.200".equals(yJ)) {
                                yL = (byte) 1;
                            } else {
                                yL = (byte) 0;
                            }
                        }
                    }
                    yM = false;
                    yI = 1;
                }
            } else {
                yI = 4;
                yM = false;
                yO = (byte) 3;
                yP = "unknown";
                yQ = (byte) 9;
            }
            mb.d("Apn", "NETWORK_TYPE : " + yO);
            mb.d("Apn", "M_APN_TYPE : " + yI);
            mb.d("Apn", "M_USE_PROXY : " + yM);
            mb.d("Apn", "M_APN_PROXY : " + yJ);
            mb.d("Apn", "M_APN_PORT : " + yK);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        yR = a(context, networkInfo);
        mb.n("Apn", "init() Apn.APN_NAME_VALUE: " + yQ + " APN_NAME_DRI: " + yP + " NETWORK_TYPE: " + yO + " ENT_VALUE: " + yR);
    }

    public static void q(Context context) {
        if (!yN) {
            Class cls = ln.class;
            synchronized (ln.class) {
                if (yN) {
                    return;
                }
                init(context);
                yN = true;
            }
        }
    }
}

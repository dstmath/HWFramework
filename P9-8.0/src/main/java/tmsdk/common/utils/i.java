package tmsdk.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Proxy;
import android.os.Build.VERSION;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdkobf.eb;
import tmsdkobf.ln;

public class i {
    private static int LH;

    public static ConnectivityManager I(Context context) {
        ConnectivityManager connectivityManager = null;
        try {
            return (ConnectivityManager) context.getSystemService("connectivity");
        } catch (Exception e) {
            e.printStackTrace();
            return connectivityManager;
        }
    }

    public static int J(Context context) {
        if (!ln.yN) {
            ln.yN = false;
            ln.q(context);
        }
        switch (ln.yR) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 11;
            case 12:
                return 12;
            case 13:
                return 13;
            case 14:
                return 14;
            case 15:
                return 15;
            case 16:
                return 16;
            case 17:
                return 0;
            default:
                return 0;
        }
    }

    public static boolean K(Context context) {
        try {
            NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1);
            return networkInfo != null && networkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getNetworkName() {
        String str = "";
        NetworkInfo networkInfo = null;
        try {
            networkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            f.g("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
        }
        if (networkInfo == null) {
            return str;
        }
        str = networkInfo.getType() != 1 ? networkInfo.getExtraInfo() : u.getSSID();
        if (str == null) {
            str = "";
        }
        return str;
    }

    public static boolean hm() {
        NetworkInfo hn = hn();
        return hn != null ? hn.isConnected() : false;
    }

    public static NetworkInfo hn() {
        NetworkInfo networkInfo = null;
        try {
            return TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            f.g("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            return networkInfo;
        }
    }

    public static boolean iE() {
        try {
            ConnectivityManager I = I(TMSDKContext.getApplicaionContext());
            if (I != null) {
                NetworkInfo[] allNetworkInfo = I.getAllNetworkInfo();
                if (allNetworkInfo != null) {
                    for (NetworkInfo state : allNetworkInfo) {
                        if (state.getState() == State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable th) {
            f.e("NetworkUtil", th);
        }
        return false;
    }

    public static boolean iF() {
        NetworkInfo networkInfo = null;
        try {
            networkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            f.g("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
        }
        return networkInfo != null && networkInfo.getType() == 0 && (networkInfo.getSubtype() == 1 || networkInfo.getSubtype() == 4 || networkInfo.getSubtype() == 2);
    }

    public static eb iG() {
        NetworkInfo networkInfo = null;
        try {
            networkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            f.g("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
        }
        if (networkInfo == null) {
            return eb.iH;
        }
        if (networkInfo.getType() == 1) {
            return eb.iJ;
        }
        if (networkInfo.getType() != 0) {
            return eb.iL;
        }
        String iI = iI();
        return (iI != null && iI.length() > 0 && iJ() > 0) ? eb.iK : eb.iL;
    }

    public static boolean iH() {
        return VERSION.SDK_INT >= 14;
    }

    public static String iI() {
        return !iH() ? Proxy.getHost(TMSDKContext.getApplicaionContext()) : System.getProperty("http.proxyHost");
    }

    public static int iJ() {
        int parseInt;
        if (iH()) {
            try {
                parseInt = Integer.parseInt(System.getProperty("http.proxyPort"));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        parseInt = Proxy.getPort(TMSDKContext.getApplicaionContext());
        return parseInt;
    }

    public static boolean iK() {
        if (n.iX() < 11) {
            return true;
        }
        if (LH < 1) {
            LH = TMSDKContext.getApplicaionContext().getApplicationInfo().targetSdkVersion;
        }
        return LH < 10;
    }
}

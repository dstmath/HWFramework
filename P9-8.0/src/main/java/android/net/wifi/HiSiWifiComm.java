package android.net.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ProxyInfo;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Slog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HiSiWifiComm {
    private static String SLEEP_POLICY_FILE = "/sys/hi110x/sleep_policy";
    private static final String TAG = "HiSiWifiComm";
    public static final int checkboxNoSelected = 0;
    public static final int checkboxSelected = 1;
    public static final int mGroupOwnerIntent = 3;
    private Context mContext;

    public HiSiWifiComm(Context context) {
        this.mContext = context;
    }

    public static boolean hisiWifiEnabled() {
        if (SystemProperties.get("ro.connectivity.chiptype").equals("hi110x")) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0049 A:{SYNTHETIC, Splitter: B:15:0x0049} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005b A:{SYNTHETIC, Splitter: B:21:0x005b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void writeSleepOption(int policy) {
        Throwable th;
        Slog.d(TAG, "WriteSleepOption,policy is " + policy);
        FileOutputStream os = null;
        try {
            FileOutputStream os2 = new FileOutputStream(new File(SLEEP_POLICY_FILE), true);
            try {
                os2.write(policy);
                if (os2 != null) {
                    try {
                        os2.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "Failed close fileOutputStream");
                    }
                }
                os = os2;
            } catch (IOException e2) {
                os = os2;
                try {
                    Slog.e(TAG, "Failed setting sleep policy");
                    if (os == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e3) {
                            Slog.e(TAG, "Failed close fileOutputStream");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                os = os2;
                if (os != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            Slog.e(TAG, "Failed setting sleep policy");
            if (os == null) {
                try {
                    os.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "Failed close fileOutputStream");
                }
            }
        }
    }

    public boolean isP2pConnect() {
        NetworkInfo mNetworkInfo = null;
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectivityManager != null) {
            mNetworkInfo = mConnectivityManager.getNetworkInfo(13);
        }
        if (mNetworkInfo == null) {
            return false;
        }
        boolean isConneted = mNetworkInfo.isConnected();
        Slog.d(TAG, "p2p isConneted: " + isConneted);
        return isConneted;
    }

    public int getSettingsGlobalIntValue(String key) {
        if (key == null || (key.equals(ProxyInfo.LOCAL_EXCL_LIST) ^ 1) == 0) {
            return 0;
        }
        return Global.getInt(this.mContext.getContentResolver(), key, 0);
    }

    public void changeShowDialogFlag(String key, boolean value) {
        if (key == null || (key.equals(ProxyInfo.LOCAL_EXCL_LIST) ^ 1) == 0) {
            Slog.d(TAG, "invalid key");
        } else if (value) {
            Global.putInt(this.mContext.getContentResolver(), key, 1);
        } else {
            Global.putInt(this.mContext.getContentResolver(), key, 0);
        }
    }
}

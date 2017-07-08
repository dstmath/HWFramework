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
    private static String SLEEP_POLICY_FILE = null;
    private static final String TAG = "HiSiWifiComm";
    public static final int checkboxNoSelected = 0;
    public static final int checkboxSelected = 1;
    public static final int mGroupOwnerIntent = 3;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.HiSiWifiComm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.HiSiWifiComm.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.HiSiWifiComm.<clinit>():void");
    }

    public HiSiWifiComm(Context context) {
        this.mContext = context;
    }

    public static boolean hisiWifiEnabled() {
        if (SystemProperties.get("ro.connectivity.chiptype").equals("hi110x")) {
            return true;
        }
        return false;
    }

    public static void writeSleepOption(int policy) {
        Throwable th;
        Slog.d(TAG, "WriteSleepOption,policy is " + policy);
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream os = new FileOutputStream(new File(SLEEP_POLICY_FILE), true);
            try {
                os.write(policy);
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "Failed close fileOutputStream");
                    }
                }
                fileOutputStream = os;
            } catch (IOException e2) {
                fileOutputStream = os;
                try {
                    Slog.e(TAG, "Failed setting sleep policy");
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                            Slog.e(TAG, "Failed close fileOutputStream");
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "Failed close fileOutputStream");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = os;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            Slog.e(TAG, "Failed setting sleep policy");
            if (fileOutputStream != null) {
                fileOutputStream.close();
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
        if (key == null || key.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            return checkboxNoSelected;
        }
        return Global.getInt(this.mContext.getContentResolver(), key, checkboxNoSelected);
    }

    public void changeShowDialogFlag(String key, boolean value) {
        if (key == null || key.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            Slog.d(TAG, "invalid key");
        } else if (value) {
            Global.putInt(this.mContext.getContentResolver(), key, checkboxSelected);
        } else {
            Global.putInt(this.mContext.getContentResolver(), key, checkboxNoSelected);
        }
    }
}

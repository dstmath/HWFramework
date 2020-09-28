package android.os;

import android.net.LocalSocket;
import android.util.Log;
import com.android.internal.app.ProcessMap;
import com.android.internal.os.IHwZygoteProcessEx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class HwZygoteProcessEx implements IHwZygoteProcessEx {
    private static final String LOG_TAG = "HwZygoteProcessEx";
    private static ProcessMap<LocalSocket> mLocalSocketSessions = new ProcessMap<>();
    private static HwZygoteProcessEx sInstance;

    public static synchronized HwZygoteProcessEx getDefault() {
        HwZygoteProcessEx hwZygoteProcessEx;
        synchronized (HwZygoteProcessEx.class) {
            if (sInstance == null) {
                sInstance = new HwZygoteProcessEx();
            }
            hwZygoteProcessEx = sInstance;
        }
        return hwZygoteProcessEx;
    }

    public void putUsapSessionSocket(String processName, int uid, LocalSocket usapSessionSocket) throws IOException {
        LocalSocket usapSessionSocketChecked = getUsapSessionSocketFromArray(processName, uid);
        if (usapSessionSocketChecked != null) {
            Log.w(LOG_TAG, "!!! This socket is existing, why create a new one for " + processName);
            mLocalSocketSessions.remove(processName, uid);
            usapSessionSocketChecked.close();
        }
        if (processName != null && uid != -1) {
            mLocalSocketSessions.put(processName, uid, usapSessionSocket);
        }
    }

    public void removeUsapSessionSocket(String processName, int uid) {
        mLocalSocketSessions.remove(processName, uid);
    }

    public LocalSocket getUsapSessionSocketFromArray(String processName, int uid) {
        ProcessMap<LocalSocket> processMap = mLocalSocketSessions;
        if (processMap == null || processMap.size() == 0 || processName == null || uid == -1) {
            return null;
        }
        return (LocalSocket) mLocalSocketSessions.get(processName, uid);
    }

    private String[] getProcAndUid(String msgStr) {
        BufferedReader reader = new BufferedReader(new StringReader(msgStr));
        String[] retStr = {null, "-1"};
        try {
            String argcString = reader.readLine();
            if (argcString == null) {
                return null;
            }
            int argc = Integer.parseInt(argcString);
            for (int argIndex = 0; argIndex < argc; argIndex++) {
                String arg = reader.readLine();
                if (arg == null) {
                    return retStr;
                }
                if (arg.startsWith("--nice-name=")) {
                    retStr[0] = arg.substring(arg.indexOf(61) + 1);
                } else if (arg.startsWith("--setuid=")) {
                    retStr[1] = arg.substring(arg.indexOf(61) + 1);
                }
            }
            return retStr;
        } catch (IOException ex) {
            Log.e(LOG_TAG, "IO Exception reading string" + ex.toString());
        }
    }

    public LocalSocket getOrCreateUsapSessionSocket(String msgStr) {
        String[] argStrings = getProcAndUid(msgStr);
        LocalSocket usapSessionSocket = null;
        if (argStrings != null) {
            String processName = argStrings[0];
            int uid = -1;
            try {
                uid = Integer.parseInt(argStrings[1]);
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "getOrCreateUsapSessionSocket catch NumberFormatException : " + e.toString());
            }
            usapSessionSocket = getUsapSessionSocketFromArray(processName, uid);
            if (usapSessionSocket != null) {
                mLocalSocketSessions.remove(processName, uid);
            }
        }
        return usapSessionSocket;
    }

    public boolean isAppStartForMaple(String startArgs) {
        int argc = 0;
        boolean ret = false;
        BufferedReader reader = new BufferedReader(new StringReader(startArgs), startArgs.length() + 1);
        try {
            String argcString = reader.readLine();
            if (argcString == null) {
                return false;
            }
            try {
                argc = Integer.parseInt(argcString);
            } catch (NumberFormatException ex) {
                Log.e(LOG_TAG, "Number FormatException parse int" + ex.toString());
            }
            for (int argIndex = 0; argIndex < argc; argIndex++) {
                String arg = reader.readLine();
                if (arg == null) {
                    return ret;
                }
                if (arg.startsWith("--isForMaple=")) {
                    ret = Boolean.parseBoolean(arg.substring(arg.indexOf(61) + 1));
                }
            }
            return ret;
        } catch (IOException ex2) {
            Log.e(LOG_TAG, "IO Exception reading string" + ex2.toString());
        }
    }
}

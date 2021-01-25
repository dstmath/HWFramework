package android.os;

import android.net.LocalSocket;
import android.util.Log;
import com.android.internal.app.ProcessMap;
import com.android.internal.os.IHwZygoteProcessEx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class HwZygoteProcessEx implements IHwZygoteProcessEx {
    private static final int ILLEGAL_UID = -1;
    private static final String LOG_TAG = "HwZygoteProcessEx";
    private static HwZygoteProcessEx sInstance;
    private static ProcessMap<LocalSocket> sLocalSocketProcessMap = new ProcessMap<>();

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
            Log.w(LOG_TAG, "!!! This socket is existing, why create the new one for " + processName);
            sLocalSocketProcessMap.remove(processName, uid);
            usapSessionSocketChecked.close();
        }
        if (processName != null && uid != -1) {
            sLocalSocketProcessMap.put(processName, uid, usapSessionSocket);
        }
    }

    public void removeUsapSessionSocket(String processName, int uid) {
        sLocalSocketProcessMap.remove(processName, uid);
    }

    public LocalSocket getUsapSessionSocketFromArray(String processName, int uid) {
        ProcessMap<LocalSocket> processMap = sLocalSocketProcessMap;
        if (processMap == null || processMap.size() == 0 || processName == null || uid == -1) {
            return null;
        }
        return (LocalSocket) sLocalSocketProcessMap.get(processName, uid);
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
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception reading string");
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
                sLocalSocketProcessMap.remove(processName, uid);
            }
        }
        return usapSessionSocket;
    }

    public boolean isAppStartForMaple(String startArgs) {
        int argc = 0;
        boolean isStart = false;
        if (startArgs == null) {
            return false;
        }
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
                    return isStart;
                }
                if (arg.startsWith("--isForMaple=")) {
                    isStart = Boolean.parseBoolean(arg.substring(arg.indexOf(61) + 1));
                }
            }
            return isStart;
        } catch (IOException ex2) {
            Log.e(LOG_TAG, "IO Exception reading string" + ex2.toString());
        }
    }
}

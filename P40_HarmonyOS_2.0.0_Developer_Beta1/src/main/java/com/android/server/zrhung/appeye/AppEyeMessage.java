package com.android.server.zrhung.appeye;

import android.util.Log;
import com.huawei.hwpartdfr.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppEyeMessage {
    public static final String KILL_MULTI_PROCESSES = "killMultiTarget";
    public static final String KILL_PROCESS = "killTarget";
    public static final String NOTIFY_USER = "notifyUser";
    private static final String TAG = "AppEyeMessage";
    private String mCommand = BuildConfig.FLAVOR;
    private String mPackageName = BuildConfig.FLAVOR;
    private String mPid = BuildConfig.FLAVOR;
    private String mProcessName = BuildConfig.FLAVOR;
    private int mUid = 0;

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006c, code lost:
        if (r6.equals("pid") != false) goto L_0x007a;
     */
    public int parseMsg(List<String> msgList) {
        if (msgList == null || msgList.isEmpty()) {
            Log.e(TAG, "empty mssage");
            return -1;
        }
        Iterator<String> it = msgList.iterator();
        while (true) {
            char c = 0;
            if (it.hasNext()) {
                String msg = it.next();
                String[] fields = msg.split(":");
                if (fields.length == 2) {
                    String str = fields[0];
                    switch (str.hashCode()) {
                        case 68795:
                            if (str.equals("END")) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 110987:
                            break;
                        case 115792:
                            if (str.equals("uid")) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 79219778:
                            if (str.equals("START")) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 202325402:
                            if (str.equals("processName")) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case 908759025:
                            if (str.equals("packageName")) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case 950394699:
                            if (str.equals("command")) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            this.mPid = fields[1];
                            break;
                        case 1:
                            this.mUid = parseInt(fields[1]);
                            break;
                        case 2:
                            this.mProcessName = fields[1];
                            break;
                        case 3:
                            this.mPackageName = fields[1];
                            break;
                        case 4:
                            this.mCommand = fields[1];
                            break;
                        case 5:
                        case 6:
                            break;
                        default:
                            Log.e(TAG, "unknow received message: " + msg);
                            break;
                    }
                } else {
                    return -1;
                }
            } else {
                return 0;
            }
        }
    }

    private int parseInt(String string) {
        if (string == null || string.length() == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt NumberFormatException");
            return -1;
        }
    }

    public String getCommand() {
        return this.mCommand;
    }

    public int getAppPid() {
        return parseInt(this.mPid);
    }

    public List<Integer> getAppPidList() {
        List<Integer> pidLists = new ArrayList<>(16);
        for (String str : this.mPid.split(",")) {
            int value = parseInt(str);
            if (value >= 0) {
                pidLists.add(Integer.valueOf(value));
            }
        }
        return pidLists;
    }

    public int getAppUid() {
        return this.mUid;
    }

    public String getAppProcessName() {
        return this.mProcessName;
    }

    public String getAppPkgName() {
        return this.mPackageName;
    }

    public String toString() {
        return " mCommand = " + this.mCommand + " mPid = " + this.mPid + " mUid = " + this.mUid + " mProcessName = " + this.mProcessName + " mPackageName = " + this.mPackageName;
    }
}

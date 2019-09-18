package com.android.server.zrhung.appeye;

import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public class AppEyeMessage {
    public static final String KILL_MUlTI_PROCESSES = "killMultiTarget";
    public static final String KILL_PROCESS = "killTarget";
    public static final String NOTIFY_USER = "notifyUser";
    private static final String TAG = "AppEyeMessage";
    private String mCommand = "";
    private String mPackageName = "";
    private String mPid = "";
    private String mProcessName = "";
    private int mUid = 0;

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006e, code lost:
        if (r5.equals("pid") != false) goto L_0x007c;
     */
    public int parseMsg(ArrayList<String> msgList) {
        if (msgList == null || msgList.isEmpty()) {
            Log.e(TAG, "empty mssage");
            return -1;
        }
        Iterator<String> it = msgList.iterator();
        while (true) {
            char c = 0;
            if (it.hasNext()) {
                String[] fields = it.next().split(":");
                if (fields.length == 2) {
                    String str = fields[0];
                    switch (str.hashCode()) {
                        case 68795:
                            if (str.equals("END")) {
                                c = 6;
                                break;
                            }
                        case 110987:
                            break;
                        case 115792:
                            if (str.equals("uid")) {
                                c = 1;
                                break;
                            }
                        case 79219778:
                            if (str.equals("START")) {
                                c = 5;
                                break;
                            }
                        case 202325402:
                            if (str.equals("processName")) {
                                c = 2;
                                break;
                            }
                        case 908759025:
                            if (str.equals("packageName")) {
                                c = 3;
                                break;
                            }
                        case 950394699:
                            if (str.equals("command")) {
                                c = 4;
                                break;
                            }
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
        int value = -1;
        if (string == null || string.length() == 0) {
            return -1;
        }
        try {
            value = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
        }
        return value;
    }

    public String getCommand() {
        return this.mCommand;
    }

    public int getAppPid() {
        return parseInt(this.mPid);
    }

    public ArrayList<Integer> getAppPidList() {
        ArrayList<Integer> list = new ArrayList<>();
        for (String str : this.mPid.split(",")) {
            int value = parseInt(str);
            if (value >= 0) {
                list.add(Integer.valueOf(value));
            }
        }
        return list;
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

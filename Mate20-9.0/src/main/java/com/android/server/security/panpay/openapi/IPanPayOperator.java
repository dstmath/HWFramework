package com.android.server.security.panpay.openapi;

import java.util.HashMap;

public interface IPanPayOperator {
    int activateApplet(String str, String str2, String str3, String str4);

    int checkEligibility(String str, String str2);

    int checkEligibilityEx(String str, String str2, String str3);

    int commonExecute(String str, String str2, String str3, String str4);

    int createSSD(String str, String str2, String str3, String str4, String str5);

    int createSSDEx(String str, String str2, String str3, String str4);

    int deleteApplet(String str, String str2, String str3, String str4, String str5);

    int deleteSSD(String str, String str2, String str3, String str4, String str5);

    int deleteSSDEx(String str, String str2, String str3, String str4);

    String getCIN(String str, String str2);

    String getCPLC(String str, String str2);

    String getIIN(String str, String str2);

    String[] getLastErrorInfo(String str, String str2);

    boolean getSwitch(String str, String str2);

    int installApplet(String str, String str2, String str3, String str4, String str5);

    int lockApplet(String str, String str2, String str3, String str4, String str5);

    int setConfig(String str, String str2, HashMap hashMap);

    int setSwitch(String str, boolean z, String str2);

    int syncSeInfo(String str, String str2, String str3, String str4);

    int syncSeInfoEx(String str, String str2, String str3);

    int unlockApplet(String str, String str2, String str3, String str4, String str5);
}

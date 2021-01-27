package com.huawei.se;

public interface ISeOperator {
    int checkEligibility(String str);

    int commonExecute(String str, String str2, String str3);

    int createSSD(String str, String str2, String str3, String str4);

    int deleteSSD(String str, String str2, String str3, String str4);

    String getCIN(String str);

    String getCPLC(String str);

    String getIIN(String str);

    String[] getLastErrorInfo(String str);

    boolean isEnable(String str);

    int setEnable(String str, boolean z);

    int syncSeInfo(String str, String str2, String str3);
}

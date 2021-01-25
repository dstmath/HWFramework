package ohos.security.seoperator;

import java.util.Map;
import ohos.rpc.IRemoteBroker;

interface ISeOper extends IRemoteBroker {
    public static final String DESCRIPTOR = "ISeOper";

    int activateApplet(String str, String str2, String str3);

    int checkEligibility(String str);

    int checkEligibilityEx(String str, String str2);

    int commonExecute(String str, String str2, String str3);

    int createSSD(String str, String str2, String str3, String str4);

    int createSSDEx(String str, String str2, String str3);

    int deleteApplet(String str, String str2, String str3, String str4);

    int deleteSSD(String str, String str2, String str3, String str4);

    int deleteSSDEx(String str, String str2, String str3);

    String getCIN(String str);

    String getCplc(String str);

    String getIIN(String str);

    String[] getLastErrorInfo(String str);

    boolean getSwitch(String str, String str2);

    int installApplet(String str, String str2, String str3, String str4);

    int lockApplet(String str, String str2, String str3, String str4);

    int setConfig(String str, Map<String, String> map);

    int setSwitch(String str, String str2, boolean z);

    int syncSeInfo(String str, String str2, String str3);

    int syncSeInfoEx(String str, String str2);

    int unlockApplet(String str, String str2, String str3, String str4);
}

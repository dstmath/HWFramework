package ohos.security.seoperator;

import com.huawei.se.SeOperator;
import com.huawei.ukey.UKeyManager;
import java.util.Map;
import ohos.rpc.IRemoteObject;

public class SeOperatorProxy implements ISeOper {
    private static final int BAD_PARAM = -2;
    private static final String CAR_SPID_FRONT = "NFCDK_";
    private static final int F_NO_EXIST = -1;
    private final IRemoteObject mRemote;

    @Override // ohos.security.seoperator.ISeOper
    public int activateApplet(String str, String str2, String str3) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int checkEligibilityEx(String str, String str2) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int createSSDEx(String str, String str2, String str3) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int deleteApplet(String str, String str2, String str3, String str4) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int deleteSSDEx(String str, String str2, String str3) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int installApplet(String str, String str2, String str3, String str4) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int lockApplet(String str, String str2, String str3, String str4) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int setConfig(String str, Map<String, String> map) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int syncSeInfoEx(String str, String str2) {
        return -1;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int unlockApplet(String str, String str2, String str3, String str4) {
        return -1;
    }

    public SeOperatorProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int checkEligibility(String str) {
        return SeOperator.getInstance().checkEligibility(str);
    }

    @Override // ohos.security.seoperator.ISeOper
    public int syncSeInfo(String str, String str2, String str3) {
        return SeOperator.getInstance().syncSeInfo(str, str2, str3);
    }

    @Override // ohos.security.seoperator.ISeOper
    public int createSSD(String str, String str2, String str3, String str4) {
        return SeOperator.getInstance().createSSD(str, str2, str3, str4);
    }

    @Override // ohos.security.seoperator.ISeOper
    public int deleteSSD(String str, String str2, String str3, String str4) {
        return SeOperator.getInstance().deleteSSD(str, str2, str3, str4);
    }

    @Override // ohos.security.seoperator.ISeOper
    public int commonExecute(String str, String str2, String str3) {
        return SeOperator.getInstance().commonExecute(str, str2, str3);
    }

    @Override // ohos.security.seoperator.ISeOper
    public String getCplc(String str) {
        return SeOperator.getInstance().getCPLC(str);
    }

    @Override // ohos.security.seoperator.ISeOper
    public String getCIN(String str) {
        return SeOperator.getInstance().getCIN(str);
    }

    @Override // ohos.security.seoperator.ISeOper
    public String getIIN(String str) {
        return SeOperator.getInstance().getIIN(str);
    }

    @Override // ohos.security.seoperator.ISeOper
    public boolean getSwitch(String str, String str2) {
        if (str == null) {
            return false;
        }
        if (str.startsWith(CAR_SPID_FRONT)) {
            return SeOperator.getInstance().isEnable(str);
        }
        if (UKeyManager.getInstance().isUKeySwitchDisabled(str2) == 1) {
            return true;
        }
        return false;
    }

    @Override // ohos.security.seoperator.ISeOper
    public int setSwitch(String str, String str2, boolean z) {
        if (str == null) {
            return -2;
        }
        if (str.startsWith(CAR_SPID_FRONT)) {
            return SeOperator.getInstance().setEnable(str, z);
        }
        return UKeyManager.getInstance().setUKeySwitchDisabled(str2, !z);
    }

    @Override // ohos.security.seoperator.ISeOper
    public String[] getLastErrorInfo(String str) {
        return SeOperator.getInstance().getLastErrorInfo(str);
    }
}

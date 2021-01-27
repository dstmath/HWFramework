package ohos.security.seoperator;

import java.util.Map;
import ohos.sysability.samgr.SysAbilityManager;

public class SeOperator {
    private static final int SA_ID = 3599;
    private static SeOperatorAbsSingleton sSeOperator = new SeOperatorAbsSingleton();

    private SeOperator() {
    }

    public static SEOperatorAbs getInstance() {
        return sSeOperator;
    }

    private static class SeOperatorAbsSingleton extends SEOperatorAbs {
        private ISeOper proxy = new SeOperatorProxy(SysAbilityManager.getSysAbility(SeOperator.SA_ID));

        SeOperatorAbsSingleton() {
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public int checkEligibility(String str) {
            return this.proxy.checkEligibility(str);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public int syncSeInfo(String str, String str2, String str3) {
            return this.proxy.syncSeInfo(str, str2, str3);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public int createSSD(String str, String str2, String str3, String str4) {
            return this.proxy.createSSD(str, str2, str3, str4);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public int deleteSSD(String str, String str2, String str3, String str4) {
            return this.proxy.deleteSSD(str, str2, str3, str4);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public int commonExecute(String str, String str2, String str3) {
            return this.proxy.commonExecute(str, str2, str3);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public String getCplc(String str) {
            return this.proxy.getCplc(str);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public boolean isEnable(String str, String str2) {
            return this.proxy.getSwitch(str, str2);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public int setEnable(String str, String str2, boolean z) {
            return this.proxy.setSwitch(str, str2, z);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public String[] getLastErrorInfo(String str) {
            return this.proxy.getLastErrorInfo(str);
        }

        @Override // ohos.security.seoperator.SEOperatorAbs
        public int setConfig(String str, Map<String, String> map) {
            return this.proxy.setConfig(str, map);
        }
    }
}

package com.huawei.se;

import huawei.android.security.panpay.PanPayImpl;

public class SeOperator {
    private static MySeOperator mSeOperator = new MySeOperator();

    public static ISeOperator getInstance() {
        return mSeOperator;
    }

    private static class MySeOperator implements ISeOperator {
        private PanPayImpl panPayImpl;

        private MySeOperator() {
            this.panPayImpl = PanPayImpl.getInstance();
        }

        @Override // com.huawei.se.ISeOperator
        public int checkEligibility(String spID) {
            return this.panPayImpl.checkEligibility(spID);
        }

        @Override // com.huawei.se.ISeOperator
        public int syncSeInfo(String spID, String sign, String timeStamp) {
            return this.panPayImpl.syncSeInfo(spID, sign, timeStamp);
        }

        @Override // com.huawei.se.ISeOperator
        public int createSSD(String spID, String sign, String timeStamp, String ssdAid) {
            return this.panPayImpl.createSSD(spID, sign, timeStamp, ssdAid);
        }

        @Override // com.huawei.se.ISeOperator
        public int deleteSSD(String spID, String sign, String timeStamp, String ssdAid) {
            return this.panPayImpl.deleteSSD(spID, sign, timeStamp, ssdAid);
        }

        @Override // com.huawei.se.ISeOperator
        public int commonExecute(String spID, String serviceId, String funCallId) {
            return this.panPayImpl.commonExecute(spID, serviceId, funCallId);
        }

        @Override // com.huawei.se.ISeOperator
        public String getCPLC(String spID) {
            return this.panPayImpl.getCPLC(spID);
        }

        @Override // com.huawei.se.ISeOperator
        public String getCIN(String spID) {
            return this.panPayImpl.getCIN(spID);
        }

        @Override // com.huawei.se.ISeOperator
        public String getIIN(String spID) {
            return this.panPayImpl.getIIN(spID);
        }

        @Override // com.huawei.se.ISeOperator
        public boolean isEnable(String spID) {
            return this.panPayImpl.getSwitch(spID);
        }

        @Override // com.huawei.se.ISeOperator
        public int setEnable(String spID, boolean choice) {
            return this.panPayImpl.setSwitch(spID, choice);
        }

        @Override // com.huawei.se.ISeOperator
        public String[] getLastErrorInfo(String spID) {
            return this.panPayImpl.getLastErrorInfo(spID);
        }
    }
}

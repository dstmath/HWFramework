package com.huawei.se;

import huawei.android.security.panpay.PanPayImpl;

public class SeOperator {
    private static MySeOperator mSeOperator = new MySeOperator();

    private static class MySeOperator implements ISeOperator {
        private PanPayImpl panPayImpl;

        private MySeOperator() {
            this.panPayImpl = PanPayImpl.getInstance();
        }

        public int checkEligibility(String spID) {
            return this.panPayImpl.checkEligibility(spID);
        }

        public int syncSeInfo(String spID, String sign, String timeStamp) {
            return this.panPayImpl.syncSeInfo(spID, sign, timeStamp);
        }

        public int createSSD(String spID, String sign, String timeStamp, String ssdAid) {
            return this.panPayImpl.createSSD(spID, sign, timeStamp, ssdAid);
        }

        public int deleteSSD(String spID, String sign, String timeStamp, String ssdAid) {
            return this.panPayImpl.deleteSSD(spID, sign, timeStamp, ssdAid);
        }

        public int commonExecute(String spID, String serviceId, String funCallId) {
            return this.panPayImpl.commonExecute(spID, serviceId, funCallId);
        }

        public String getCPLC(String spID) {
            return this.panPayImpl.getCPLC(spID);
        }

        public String getCIN(String spID) {
            return this.panPayImpl.getCIN(spID);
        }

        public String getIIN(String spID) {
            return this.panPayImpl.getIIN(spID);
        }

        public boolean isEnable(String spID) {
            return this.panPayImpl.getSwitch(spID);
        }

        public int setEnable(String spID, boolean choice) {
            return this.panPayImpl.setSwitch(spID, choice);
        }

        public String[] getLastErrorInfo(String spID) {
            return this.panPayImpl.getLastErrorInfo(spID);
        }
    }

    public static ISeOperator getInstance() {
        return mSeOperator;
    }
}

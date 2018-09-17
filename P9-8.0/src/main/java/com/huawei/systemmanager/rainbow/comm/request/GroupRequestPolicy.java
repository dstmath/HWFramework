package com.huawei.systemmanager.rainbow.comm.request;

public class GroupRequestPolicy {

    public enum FailRequestPolicy {
        RETURN_WHEN_FAILED(false),
        CONTINUE_WHEN_FAILED(true);
        
        private boolean mCont;

        private FailRequestPolicy(boolean cont) {
            this.mCont = cont;
        }

        public boolean shouldContinue() {
            return this.mCont;
        }
    }
}

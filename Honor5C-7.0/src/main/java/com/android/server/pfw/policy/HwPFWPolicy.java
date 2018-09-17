package com.android.server.pfw.policy;

import android.content.Context;
import android.content.Intent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class HwPFWPolicy {
    public static final int POLICY_TYPE_APP_WAKELOCK = 1;
    public static final int POLICY_TYPE_AUTO_STARTUP = 3;
    public static final int POLICY_TYPE_GOOGLE_SERVICE = 2;
    public static final int POLICY_TYPE_PROC_HIGH_FREQ_RESTART = 0;
    private static final String TAG = "PFW.HwPFWPolicy";
    protected Context mContext;
    protected List<Integer> mParams;
    private boolean mPolicyEnabled;

    public HwPFWPolicy(Context context) {
        this.mContext = null;
        this.mParams = new ArrayList();
        this.mPolicyEnabled = true;
        this.mContext = context;
    }

    public void handleBroadcastIntent(Intent intent) {
    }

    protected boolean verifyParamLength(List<Integer> list) {
        return true;
    }

    public boolean isPolicyEnabled() {
        return this.mPolicyEnabled;
    }

    public void setPolicyEnabled(boolean enabled) {
        this.mPolicyEnabled = enabled;
    }

    public void startAction() {
    }

    public void stopAction() {
    }

    public void cloneArrayList(List<Integer> src) {
        if (src != null) {
            for (Integer val : src) {
                this.mParams.add(val);
            }
        }
    }

    public boolean setParams(List<Integer> paramList) {
        cloneArrayList(paramList);
        return true;
    }

    public void printDumpInfo(PrintWriter pw) {
    }
}

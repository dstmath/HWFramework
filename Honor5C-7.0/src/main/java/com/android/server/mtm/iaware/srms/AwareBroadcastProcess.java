package com.android.server.mtm.iaware.srms;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.am.HwMtmBroadcastResourceManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class AwareBroadcastProcess {
    private static final int IAWARE_FIRST_UNPROXY_DELAY_TIME = 5;
    private static final int IAWARE_PROXY_QUEUE_MIN_LENGTH = 10;
    private static final int IAWARE_UNPROXY_SCREEN_OFF_TIME = 5000;
    private static final int MSG_ENQUEUE_PARALL_BR = 102;
    private static final int MSG_PROCESS_FGAPP_PARALL_BR = 103;
    private static final int MSG_PROCESS_PARALL_BR = 101;
    static final String TAG = "AwareBroadcastProcess";
    private String mBrName;
    private final IawareBroadcastHandler mHandler;
    private AwareBroadcastPolicy mIawareBrPolicy;
    private final ArrayList<HwBroadcastRecord> mIawareParallelBroadcasts;
    private int mIawareUnProxyTime;
    private int mProxyKeyBrIndex;
    private boolean mStartUnproxy;
    private int mUnproxyMaxDuration;
    private int mUnproxyMaxSpeed;
    private int mUnproxyMinSpeed;

    private final class IawareBroadcastHandler extends Handler {
        public IawareBroadcastHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AwareBroadcastProcess.MSG_PROCESS_PARALL_BR /*101*/:
                    AwareBroadcastProcess.this.unproxyEachBroacast();
                case AwareBroadcastProcess.MSG_ENQUEUE_PARALL_BR /*102*/:
                    if (msg.obj instanceof HwBroadcastRecord) {
                        AwareBroadcastProcess.this.insertProxyParalledBroadcast(msg.obj);
                    }
                case AwareBroadcastProcess.MSG_PROCESS_FGAPP_PARALL_BR /*103*/:
                    AwareBroadcastProcess.this.unProxyForegroundAppBroadcast(msg.arg1, msg.arg2);
                default:
            }
        }
    }

    public AwareBroadcastProcess(AwareBroadcastPolicy iawareBrPolicy, Handler handler, String name) {
        this.mIawareParallelBroadcasts = new ArrayList();
        this.mStartUnproxy = false;
        this.mIawareUnProxyTime = 150;
        this.mProxyKeyBrIndex = -1;
        this.mIawareBrPolicy = null;
        this.mBrName = null;
        this.mUnproxyMaxDuration = 20000;
        this.mUnproxyMaxSpeed = 150;
        this.mUnproxyMinSpeed = 20;
        this.mIawareBrPolicy = iawareBrPolicy;
        this.mHandler = new IawareBroadcastHandler(handler.getLooper());
        this.mBrName = name;
    }

    public void enqueueIawareProxyBroacast(boolean isParallel, HwBroadcastRecord r) {
        if (isParallel) {
            Message msg = this.mHandler.obtainMessage(MSG_ENQUEUE_PARALL_BR, this);
            msg.obj = r;
            this.mHandler.sendMessage(msg);
        }
    }

    public void starUnproxyBroadcast() {
        if (!this.mStartUnproxy && this.mIawareParallelBroadcasts.size() > 0) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br start unproxy, mStartBgUnproxy = true, queue name : " + this.mBrName);
            }
            this.mStartUnproxy = true;
            unProxyBroadcast(IAWARE_FIRST_UNPROXY_DELAY_TIME);
        }
    }

    private void insertProxyParalledBroadcast(HwBroadcastRecord r) {
        synchronized (this.mIawareParallelBroadcasts) {
            this.mIawareParallelBroadcasts.add(r);
        }
    }

    private void unProxyBroadcast(int unproxyTime) {
        if (!this.mHandler.hasMessages(MSG_PROCESS_PARALL_BR)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_PROCESS_PARALL_BR, this), (long) unproxyTime);
        }
    }

    private void unproxyEachBroacast() {
        if (this.mIawareParallelBroadcasts.size() == 0) {
            this.mStartUnproxy = false;
            return;
        }
        if (this.mIawareParallelBroadcasts.size() <= IAWARE_PROXY_QUEUE_MIN_LENGTH) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "iaware_br unproxy all, queueLength [" + this.mBrName + "] : " + this.mIawareParallelBroadcasts.size());
            }
            iAwareUnproxyAllBroadcast();
        } else {
            ArrayList<HwBroadcastRecord> parallelList = new ArrayList();
            synchronized (this.mIawareParallelBroadcasts) {
                if (this.mIawareParallelBroadcasts.size() > 0) {
                    parallelList.add((HwBroadcastRecord) this.mIawareParallelBroadcasts.remove(0));
                    if (this.mProxyKeyBrIndex >= 0) {
                        this.mProxyKeyBrIndex--;
                    }
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.i(TAG, "iaware_br unproxy parallel broadcast by each, queueLength " + this.mBrName + ": " + this.mIawareParallelBroadcasts.size() + " : " + ((HwBroadcastRecord) parallelList.get(0)).getBrReceivers().get(0) + ", mProxyKeyBrIndex--: " + this.mProxyKeyBrIndex + " :action : " + ((HwBroadcastRecord) parallelList.get(0)).getAction());
                    }
                }
            }
            iAwareUnproxyBroadcastList(parallelList);
            parallelList.clear();
            calculateUnproxySpeed();
            unProxyBroadcast(this.mIawareUnProxyTime);
        }
    }

    private void calculateUnproxySpeed() {
        if (!this.mIawareBrPolicy.isScreenOff()) {
            if (this.mIawareBrPolicy.isSpeedNoCtrol()) {
                this.mIawareUnProxyTime = IAWARE_FIRST_UNPROXY_DELAY_TIME;
                return;
            }
            int length = this.mIawareParallelBroadcasts.size();
            if (length > 0) {
                int minDuration;
                int i;
                int duration;
                if (this.mProxyKeyBrIndex < 0) {
                    minDuration = Integer.MAX_VALUE;
                    for (i = 0; i < length; i++) {
                        duration = (int) (((((HwBroadcastRecord) this.mIawareParallelBroadcasts.get(i)).getDispatchClockTime() + ((long) this.mUnproxyMaxDuration)) - System.currentTimeMillis()) / ((long) (i + 1)));
                        if (duration < minDuration) {
                            minDuration = duration;
                            this.mProxyKeyBrIndex = i;
                        }
                    }
                    this.mIawareUnProxyTime = minDuration;
                } else if (this.mProxyKeyBrIndex < length - 1) {
                    minDuration = this.mIawareUnProxyTime;
                    for (i = this.mProxyKeyBrIndex + 1; i < length; i++) {
                        duration = (int) (((((HwBroadcastRecord) this.mIawareParallelBroadcasts.get(i)).getDispatchClockTime() + ((long) this.mUnproxyMaxDuration)) - System.currentTimeMillis()) / ((long) (i + 1)));
                        if (duration < minDuration) {
                            minDuration = duration;
                            this.mProxyKeyBrIndex = i;
                        }
                    }
                    this.mIawareUnProxyTime = minDuration;
                }
            }
            this.mIawareUnProxyTime = Math.min(this.mUnproxyMaxSpeed, Math.max(this.mIawareUnProxyTime, this.mUnproxyMinSpeed));
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br calculateUnproxySpeed, end, queueLength" + this.mBrName + ": " + length + ", unproxy speed: " + this.mIawareUnProxyTime);
            }
        }
    }

    public void iAwareUnproxyAllBroadcast() {
        ArrayList<HwBroadcastRecord> parallelList = new ArrayList();
        while (this.mIawareParallelBroadcasts.size() > 0) {
            synchronized (this.mIawareParallelBroadcasts) {
                int size_parallel = this.mIawareParallelBroadcasts.size();
                for (int i = 0; i < size_parallel; i++) {
                    parallelList.add((HwBroadcastRecord) this.mIawareParallelBroadcasts.remove(0));
                    this.mProxyKeyBrIndex--;
                }
                this.mStartUnproxy = false;
            }
            if (this.mIawareBrPolicy.isEmptyIawareBrList()) {
                this.mIawareBrPolicy.setStartProxy(false);
            }
            iAwareUnproxyBroadcastList(parallelList);
            parallelList.clear();
        }
    }

    private void iAwareUnproxyBroadcastList(ArrayList<HwBroadcastRecord> parallelList) {
        if (parallelList.size() > 0) {
            HwMtmBroadcastResourceManager.insertIawareBroadcast(parallelList, this.mBrName);
        }
    }

    public int getIawareBrSize() {
        int size;
        synchronized (this.mIawareParallelBroadcasts) {
            size = this.mIawareParallelBroadcasts.size();
        }
        return size;
    }

    public void setUnProxySpeedScreenOff() {
        int size = this.mIawareParallelBroadcasts.size();
        if (size > 0) {
            this.mIawareUnProxyTime = Math.min(IAWARE_UNPROXY_SCREEN_OFF_TIME / size, this.mUnproxyMinSpeed);
        }
    }

    public void setUnProxyMaxDuration(int duration) {
        this.mUnproxyMaxDuration = duration;
    }

    public void setUnProxyMaxSpeed(int speed) {
        this.mUnproxyMaxSpeed = speed;
    }

    public void setUnProxyMinSpeed(int speed) {
        this.mUnproxyMinSpeed = speed;
    }

    public void dump(PrintWriter pw) {
        ArrayList<HwBroadcastRecord> iawareParallelBroadcasts;
        pw.println("      Proxy broadcast [" + this.mBrName + "]");
        Set<String> proxyActions = new ArraySet();
        Set<String> proxyPkgs = new ArraySet();
        synchronized (this.mIawareParallelBroadcasts) {
            iawareParallelBroadcasts = new ArrayList(this.mIawareParallelBroadcasts);
        }
        for (HwBroadcastRecord hwBr : iawareParallelBroadcasts) {
            proxyActions.add(hwBr.getAction());
            proxyPkgs.add(hwBr.getReceiverPkg(hwBr.getBrReceivers().get(0)));
        }
        pw.println("      proxy action :" + proxyActions);
        pw.println("      proxy pkg :" + proxyPkgs);
        int length = iawareParallelBroadcasts.size();
        if (length == 0) {
            pw.println("      Unproxy speed :0");
        } else {
            pw.println("      Unproxy speed :" + this.mIawareUnProxyTime);
        }
        pw.println("      Proxy broadcast count :" + length);
    }

    public void startUnproxyFgAppBroadcast(int pid, int uid) {
        if (getIawareBrSize() != 0) {
            Message msg = this.mHandler.obtainMessage(MSG_PROCESS_FGAPP_PARALL_BR, this);
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    private void unProxyForegroundAppBroadcast(int pid, int uid) {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.d("TAG", "iaware_br unProxyForegroundAppBroadcast, uid:" + uid + ", pid: " + pid);
        }
        ArrayList<HwBroadcastRecord> parallelList = new ArrayList();
        synchronized (this.mIawareParallelBroadcasts) {
            iAwareUnproxyBroadcastInner(this.mIawareParallelBroadcasts, uid, parallelList);
        }
        iAwareUnproxyBroadcastList(parallelList);
        parallelList.clear();
    }

    private void iAwareUnproxyBroadcastInner(ArrayList<HwBroadcastRecord> pendingBroadcasts, int unProxyUid, ArrayList<HwBroadcastRecord> unProxyBroadcasts) {
        Iterator it = pendingBroadcasts.iterator();
        while (it.hasNext()) {
            HwBroadcastRecord br = (HwBroadcastRecord) it.next();
            if (br.getReceiverUid() == unProxyUid) {
                unProxyBroadcasts.add(br);
                it.remove();
            }
        }
    }
}

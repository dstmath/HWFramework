package com.android.server.mtm.iaware.srms;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.am.HwMtmBroadcastResourceManager;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.huawei.android.content.IntentExt;
import com.huawei.android.os.HandlerEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AwareBroadcastProcess {
    private static final int IAWARE_FIRST_UNPROXY_DELAY_TIME = 5;
    private static final int IAWARE_PARALLEL_EXCEPTION_LENGTH = 10000;
    private static final int IAWARE_PARALLEL_PROXY_LIST_HIGH_ID = 1;
    private static final int IAWARE_PARALLEL_PROXY_LIST_LOW_ID = 3;
    private static final int IAWARE_PARALLEL_PROXY_LIST_MIDDLE_ID = 2;
    private static final int IAWARE_PARALLEL_PROXY_LIST_NUM = 4;
    private static final int IAWARE_PARALLEL_PROXY_LIST_TOP_ID = 0;
    private static final int IAWARE_UNPROXY_SCREEN_OFF_TIME = 5000;
    private static final int MSG_ENQUEUE_PARALL_BR = 102;
    private static final int MSG_PROCESS_FG_APP_PARALL_BR = 103;
    private static final int MSG_PROCESS_PARALL_BR = 101;
    private static final String TAG = "AwareBroadcastProcess";
    private AwareBroadcastPolicy mAwareBrPolicy = null;
    private final HashMap<Integer, ArrayList<HwBroadcastRecord>> mAwareParallelProxyBrMap = new HashMap<>();
    private int mAwareUnProxyTime = CpuFeature.MSG_SET_VIP_THREAD_PARAMS;
    private String mBrName = null;
    private final AwareBroadcastHandler mHandler;
    private int mProxyKeyBrIndex = -1;
    private boolean mStartUnproxy = false;
    private int mUnproxyHighSpeed = 20;
    private int mUnproxyMaxDuration = 20000;
    private int mUnproxyMaxSpeed = CpuFeature.MSG_SET_VIP_THREAD_PARAMS;
    private int mUnproxyMiddleSpeed = 40;
    private int mUnproxyMinSpeed = 60;

    public AwareBroadcastProcess(AwareBroadcastPolicy iawareBrPolicy, Handler handler, String name) {
        this.mAwareBrPolicy = iawareBrPolicy;
        this.mHandler = new AwareBroadcastHandler(handler.getLooper());
        this.mBrName = name;
        for (int index = 0; index < 4; index++) {
            this.mAwareParallelProxyBrMap.put(Integer.valueOf(index), new ArrayList<>());
        }
    }

    /* access modifiers changed from: private */
    public final class AwareBroadcastHandler extends HandlerEx {
        public AwareBroadcastHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    AwareBroadcastProcess.this.processException();
                    AwareBroadcastProcess.this.unproxyEachBroacast();
                    return;
                case 102:
                    if (msg.obj instanceof HwBroadcastRecord) {
                        AwareBroadcastProcess.this.insertProxyParalledBroadcast((HwBroadcastRecord) msg.obj);
                        return;
                    }
                    return;
                case 103:
                    AwareBroadcastProcess.this.unProxyForegroundAppBroadcast(msg.arg1, msg.arg2);
                    return;
                default:
                    return;
            }
        }
    }

    public void enqueueAwareProxyBroacast(boolean isParallel, HwBroadcastRecord record) {
        if (isParallel) {
            Message msg = this.mHandler.obtainMessage(102, this);
            msg.obj = record;
            this.mHandler.sendMessage(msg);
        }
    }

    public void startUnproxyBroadcast() {
        if (!this.mStartUnproxy && getAwareBrSize() > 0) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br start unproxy, mStartBgUnproxy = true, queue name : " + this.mBrName);
            }
            this.mStartUnproxy = true;
            unProxyBroadcast(5);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void insertProxyParalledBroadcast(HwBroadcastRecord record) {
        synchronized (this.mAwareParallelProxyBrMap) {
            int curAdj = record.getReceiverCurAdj();
            if (record.getReceiverCurProcState() == 2) {
                if (!record.isSysApp()) {
                    trimProxyBr(record, 0);
                }
                this.mAwareParallelProxyBrMap.get(0).add(record);
            } else if (curAdj < 0) {
                trimProxyBr(record, 1);
                this.mAwareParallelProxyBrMap.get(1).add(record);
            } else if (curAdj >= 900) {
                trimProxyBr(record, 3);
                this.mAwareParallelProxyBrMap.get(3).add(record);
            } else {
                processAdjChange(record);
                trimProxyBr(record, 2);
                this.mAwareParallelProxyBrMap.get(2).add(record);
            }
        }
    }

    private void unProxyBroadcast(int unproxyTime) {
        if (!this.mHandler.hasMessages(101)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101, this), (long) unproxyTime);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unproxyEachBroacast() {
        if (isEmptyProxyMap()) {
            this.mStartUnproxy = false;
            return;
        }
        ArrayList<HwBroadcastRecord> parallelList = new ArrayList<>();
        synchronized (this.mAwareParallelProxyBrMap) {
            HwBroadcastRecord proxyBr = getNextUnProxyBr();
            if (proxyBr != null) {
                parallelList.add(proxyBr);
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.i(TAG, "iaware_br unproxy cost time : " + (System.currentTimeMillis() - proxyBr.getDispatchClockTime()) + " : receiver :" + proxyBr.getBrReceivers().get(0) + " : action : " + proxyBr.getAction() + " : processState : " + proxyBr.getReceiverCurProcState() + ": adj : " + proxyBr.getReceiverCurAdj() + ": unproxy speed: " + this.mAwareUnProxyTime + " : size:" + getAwareBrSize());
                }
            }
            if (proxyBr != null && proxyBr.getReceiverCurAdj() <= 0 && this.mProxyKeyBrIndex >= 0) {
                this.mProxyKeyBrIndex--;
            }
        }
        iAwareUnproxyBroadcastList(parallelList);
        parallelList.clear();
        calculateUnproxySpeed();
        unProxyBroadcast(this.mAwareUnProxyTime);
    }

    private void doCalculateUnproxySpeed() {
        int i;
        ArrayList<HwBroadcastRecord> iawareParallelBroadcasts = getProcessingList();
        int length = iawareParallelBroadcasts.size();
        if (length > 0 && (i = this.mProxyKeyBrIndex) < length - 1) {
            if (i < 0) {
                int minDuration = Integer.MAX_VALUE;
                for (int i2 = 0; i2 < length; i2++) {
                    int duration = (int) (((iawareParallelBroadcasts.get(i2).getDispatchClockTime() + ((long) this.mUnproxyMaxDuration)) - System.currentTimeMillis()) / ((long) (i2 + 1)));
                    if (duration < minDuration) {
                        minDuration = duration;
                        this.mProxyKeyBrIndex = i2;
                    }
                }
                this.mAwareUnProxyTime = minDuration;
                return;
            }
            int minDuration2 = this.mAwareUnProxyTime;
            for (int i3 = i + 1; i3 < length; i3++) {
                int duration2 = (int) (((iawareParallelBroadcasts.get(i3).getDispatchClockTime() + ((long) this.mUnproxyMaxDuration)) - System.currentTimeMillis()) / ((long) (i3 + 1)));
                if (duration2 < minDuration2) {
                    minDuration2 = duration2;
                    this.mProxyKeyBrIndex = i3;
                }
            }
            this.mAwareUnProxyTime = minDuration2;
        }
    }

    private void calculateUnproxySpeed() {
        if (!this.mAwareBrPolicy.isScreenOff()) {
            if (!isRestrictTopApp() && isProcessProxyTopList()) {
                if (this.mAwareBrPolicy.isSpeedNoCtrol()) {
                    this.mAwareUnProxyTime = this.mUnproxyHighSpeed;
                } else {
                    this.mAwareUnProxyTime = this.mUnproxyMaxSpeed;
                }
            }
            if (isProcessProxyHighAdjList()) {
                this.mAwareUnProxyTime = this.mUnproxyHighSpeed;
            } else if (!this.mAwareBrPolicy.isSpeedNoCtrol()) {
                doCalculateUnproxySpeed();
                resetUnproxyTime();
            } else if (isProcessProxyMiddleAdjList()) {
                this.mAwareUnProxyTime = this.mUnproxyMiddleSpeed;
            } else {
                this.mAwareUnProxyTime = this.mUnproxyMinSpeed;
            }
        }
    }

    private void resetUnproxyTime() {
        int tempUnProxyTime = this.mAwareUnProxyTime;
        int i = this.mUnproxyMinSpeed;
        if (tempUnProxyTime <= i) {
            tempUnProxyTime = i;
        }
        int i2 = this.mUnproxyMaxSpeed;
        if (i2 >= tempUnProxyTime) {
            i2 = tempUnProxyTime;
        }
        this.mAwareUnProxyTime = i2;
    }

    private void iAwareUnproxyBroadcastList(ArrayList<HwBroadcastRecord> parallelList) {
        if (parallelList.size() > 0) {
            HwMtmBroadcastResourceManager.insertAwareBroadcast(parallelList, this.mBrName);
        }
    }

    public int getAwareBrSize() {
        int length;
        synchronized (this.mAwareParallelProxyBrMap) {
            length = 0;
            for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mAwareParallelProxyBrMap.entrySet()) {
                length += ent.getValue().size();
            }
        }
        return length;
    }

    public void setUnProxySpeedScreenOff() {
        int size = getAwareBrSize();
        if (size > 0) {
            int speed = IAWARE_UNPROXY_SCREEN_OFF_TIME / size;
            int i = this.mUnproxyMinSpeed;
            if (speed < i) {
                i = speed;
            }
            this.mAwareUnProxyTime = i;
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

    public void setUnProxyMiddleSpeed(int speed) {
        this.mUnproxyMiddleSpeed = speed;
    }

    public void setUnProxyHighSpeed(int speed) {
        this.mUnproxyHighSpeed = speed;
    }

    private void dumpProxyInfo(ArrayList<HwBroadcastRecord> iawareTopParallelBroadcasts, ArrayList<HwBroadcastRecord> iawareHighAdjParallelBroadcasts, ArrayList<HwBroadcastRecord> iawareMiddleAdjParallelBroadcasts, ArrayList<HwBroadcastRecord> iawareLowAdjParallelBroadcasts, PrintWriter pw) {
        Set<String> proxyActions = new ArraySet<>();
        Set<String> proxyTopPkgs = new ArraySet<>();
        int listSize = iawareTopParallelBroadcasts.size();
        for (int index = 0; index < listSize; index++) {
            HwBroadcastRecord hwBr = iawareTopParallelBroadcasts.get(index);
            proxyActions.add(hwBr.getAction());
            proxyTopPkgs.add(hwBr.getReceiverPkg());
        }
        Set<String> proxyHighAdjPkgs = new ArraySet<>();
        int listSize2 = iawareHighAdjParallelBroadcasts.size();
        for (int index2 = 0; index2 < listSize2; index2++) {
            HwBroadcastRecord hwBr2 = iawareHighAdjParallelBroadcasts.get(index2);
            proxyActions.add(hwBr2.getAction());
            proxyHighAdjPkgs.add(hwBr2.getReceiverPkg());
        }
        Set<String> proxyMiddleAdjPkgs = new ArraySet<>();
        int listSize3 = iawareMiddleAdjParallelBroadcasts.size();
        for (int index3 = 0; index3 < listSize3; index3++) {
            HwBroadcastRecord hwBr3 = iawareMiddleAdjParallelBroadcasts.get(index3);
            proxyActions.add(hwBr3.getAction());
            proxyMiddleAdjPkgs.add(hwBr3.getReceiverPkg());
        }
        Set<String> proxyLowAdjPkgs = new ArraySet<>();
        int listSize4 = iawareLowAdjParallelBroadcasts.size();
        for (int index4 = 0; index4 < listSize4; index4++) {
            HwBroadcastRecord hwBr4 = iawareLowAdjParallelBroadcasts.get(index4);
            proxyActions.add(hwBr4.getAction());
            proxyLowAdjPkgs.add(hwBr4.getReceiverPkg());
        }
        pw.println("      proxy action :" + proxyActions);
        pw.println("      proxy Top pkg :" + proxyTopPkgs);
        pw.println("      proxy high adj pkg :" + proxyHighAdjPkgs);
        pw.println("      proxy middle adj pkg :" + proxyMiddleAdjPkgs);
        pw.println("      proxy low adj pkg :" + proxyLowAdjPkgs);
    }

    public void dump(PrintWriter pw) {
        ArrayList<HwBroadcastRecord> iawareTopParallelBroadcasts;
        ArrayList<HwBroadcastRecord> iawareHighAdjParallelBroadcasts;
        ArrayList<HwBroadcastRecord> iawareMiddleAdjParallelBroadcasts;
        ArrayList<HwBroadcastRecord> iawareLowAdjParallelBroadcasts;
        if (pw != null) {
            pw.println("      Proxy broadcast [" + this.mBrName + "]");
            synchronized (this.mAwareParallelProxyBrMap) {
                iawareTopParallelBroadcasts = new ArrayList<>(this.mAwareParallelProxyBrMap.get(0));
                iawareHighAdjParallelBroadcasts = new ArrayList<>(this.mAwareParallelProxyBrMap.get(1));
                iawareMiddleAdjParallelBroadcasts = new ArrayList<>(this.mAwareParallelProxyBrMap.get(2));
                iawareLowAdjParallelBroadcasts = new ArrayList<>(this.mAwareParallelProxyBrMap.get(3));
            }
            dumpProxyInfo(iawareTopParallelBroadcasts, iawareHighAdjParallelBroadcasts, iawareMiddleAdjParallelBroadcasts, iawareLowAdjParallelBroadcasts, pw);
            int lengthTop = iawareTopParallelBroadcasts.size();
            int lengthHighAdj = iawareHighAdjParallelBroadcasts.size();
            int lengthMiddleAdj = iawareMiddleAdjParallelBroadcasts.size();
            int lengthLowAdj = iawareLowAdjParallelBroadcasts.size();
            int length = lengthTop + lengthHighAdj + lengthMiddleAdj + lengthLowAdj;
            if (length == 0) {
                pw.println("      Unproxy speed :0");
            } else {
                pw.println("      Unproxy speed :" + this.mAwareUnProxyTime);
            }
            pw.println("      Proxy broadcast count :" + length + ", Top count:" + lengthTop + ", high adj count:" + lengthHighAdj + ", middle adj count:" + lengthMiddleAdj + ", low adj count:" + lengthLowAdj);
        }
    }

    public void startUnproxyFgAppBroadcast(int pid, int uid) {
        if (getAwareBrSize() != 0) {
            Message msg = this.mHandler.obtainMessage(103, this);
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unProxyForegroundAppBroadcast(int pid, int uid) {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.d(TAG, "iaware_br unProxyForegroundAppBroadcast, uid:" + uid + ", pid: " + pid);
        }
        ArrayList<HwBroadcastRecord> parallelList = this.mAwareParallelProxyBrMap.get(0);
        synchronized (this.mAwareParallelProxyBrMap) {
            String pkgSys = null;
            if (uid == 1000) {
                pkgSys = getSysPkg(pid, uid);
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.i(TAG, "iaware_br unProxyForegroundAppBroadcast system app, uid:" + uid + ", pid: " + pid + ": pkg : " + pkgSys);
                }
                if (pkgSys == null) {
                    return;
                }
            }
            for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mAwareParallelProxyBrMap.entrySet()) {
                if (ent.getKey().intValue() != 0) {
                    iAwareUnproxyBroadcastInner(ent.getValue(), uid, parallelList, pkgSys);
                }
            }
        }
    }

    private void iAwareUnproxyBroadcastInner(ArrayList<HwBroadcastRecord> pendingBroadcasts, int unProxyUid, ArrayList<HwBroadcastRecord> unProxyBroadcasts, String unProxyPkgSys) {
        Iterator<HwBroadcastRecord> it = pendingBroadcasts.iterator();
        while (it.hasNext()) {
            HwBroadcastRecord br = it.next();
            if (unProxyUid != 1000 || unProxyPkgSys == null) {
                if (br.getReceiverUid() == unProxyUid) {
                    unProxyBroadcasts.add(br);
                    it.remove();
                }
            } else if (unProxyPkgSys.equals(br.getReceiverPkg())) {
                unProxyBroadcasts.add(br);
                it.remove();
            }
        }
    }

    private boolean isEmptyProxyMap() {
        for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mAwareParallelProxyBrMap.entrySet()) {
            if (ent.getValue().size() > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isProcessProxyTopList() {
        return this.mAwareParallelProxyBrMap.get(0).size() != 0;
    }

    private boolean isProcessProxyHighAdjList() {
        return this.mAwareParallelProxyBrMap.get(1).size() != 0;
    }

    private boolean isProcessProxyMiddleAdjList() {
        return this.mAwareParallelProxyBrMap.get(0).size() == 0 && this.mAwareParallelProxyBrMap.get(1).size() == 0 && this.mAwareParallelProxyBrMap.get(2).size() != 0;
    }

    private HwBroadcastRecord getNextUnProxyBr() {
        for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mAwareParallelProxyBrMap.entrySet()) {
            ArrayList<HwBroadcastRecord> iawareParallelBrList = ent.getValue();
            if (!isRestrictTopApp() || ent.getKey().intValue() != 0) {
                if (iawareParallelBrList.size() > 0) {
                    return iawareParallelBrList.remove(0);
                }
            } else if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "iaware_br don't unproxy top app's br");
            }
        }
        return null;
    }

    private ArrayList<HwBroadcastRecord> getProcessingList() {
        if (isProcessProxyMiddleAdjList()) {
            return this.mAwareParallelProxyBrMap.get(2);
        }
        return this.mAwareParallelProxyBrMap.get(3);
    }

    private void processAdjChange(HwBroadcastRecord record) {
        String pkg = record.getReceiverPkg();
        if (pkg != null) {
            int pid = record.getReceiverPid();
            ArrayList<HwBroadcastRecord> iawareMiddleBrList = this.mAwareParallelProxyBrMap.get(2);
            Iterator<HwBroadcastRecord> it = this.mAwareParallelProxyBrMap.get(3).iterator();
            while (it.hasNext()) {
                HwBroadcastRecord br = it.next();
                if (br.getReceiverPid() == pid && pkg.equals(br.getReceiverPkg())) {
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.d(TAG, "iaware_br adj change, transfer: " + br.getBrReceivers().get(0));
                    }
                    iawareMiddleBrList.add(br);
                    it.remove();
                }
            }
        }
    }

    private boolean isTrimProxyBr(Iterator<HwBroadcastRecord> it, HwBroadcastRecord record) {
        HwBroadcastRecord br = it.next();
        if (!canTrim(record, br)) {
            return false;
        }
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.d(TAG, "iaware_br brtrim success: " + br.getBrReceivers().get(0) + ": action: " + record.getAction());
        }
        it.remove();
        return true;
    }

    private void trimProxyBr(HwBroadcastRecord record, int level) {
        if (this.mAwareBrPolicy.isTrimAction(record.getAction())) {
            Iterator<HwBroadcastRecord> it = this.mAwareParallelProxyBrMap.get(Integer.valueOf(level)).iterator();
            while (it.hasNext() && !isTrimProxyBr(it, record)) {
            }
        }
    }

    private boolean canTrim(HwBroadcastRecord newBr, HwBroadcastRecord oldBr) {
        String newPkg = newBr.getReceiverPkg();
        String oldPkg = oldBr.getReceiverPkg();
        if (newPkg == null || oldPkg == null || !newPkg.equals(oldPkg) || !newBr.isSameReceiver(oldBr)) {
            return false;
        }
        String newAction = newBr.getAction();
        String oldAction = oldBr.getAction();
        if (newAction == null || oldAction == null || !newAction.equals(oldAction)) {
            return false;
        }
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(newAction)) {
            return canConnectivityDataTrim(newBr.getIntent(), oldBr.getIntent());
        }
        if ("android.net.wifi.STATE_CHANGE".equals(newAction)) {
            return canWifiDataTrim(newBr.getIntent(), oldBr.getIntent());
        }
        return true;
    }

    private boolean canConnectivityDataTrim(Intent newIntent, Intent oldIntent) {
        int newType = newIntent.getIntExtra("networkType", -1);
        if (newType != oldIntent.getIntExtra("networkType", -1)) {
            return false;
        }
        if (newType == 0 || newType == 1) {
            NetworkInfo newInfo = null;
            NetworkInfo oldInfo = null;
            Object newIntentInfo = IntentExt.getExtra(newIntent, "networkInfo");
            Object oldIntentInfo = IntentExt.getExtra(oldIntent, "networkInfo");
            if ((newIntentInfo instanceof NetworkInfo) && (oldIntentInfo instanceof NetworkInfo)) {
                newInfo = (NetworkInfo) newIntentInfo;
                oldInfo = (NetworkInfo) oldIntentInfo;
            } else if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.e(TAG, "iaware_br get NetworkInfo from intent error.");
            }
            if (newInfo != null && oldInfo != null && newInfo.getState() == oldInfo.getState()) {
                return true;
            }
        }
        return false;
    }

    private boolean canWifiDataTrim(Intent newIntent, Intent oldIntent) {
        NetworkInfo newNetworkInfo = null;
        NetworkInfo oldNetworkInfo = null;
        Object newIntentInfo = newIntent.getParcelableExtra("networkInfo");
        Object oldIntentInfo = oldIntent.getParcelableExtra("networkInfo");
        if ((newIntentInfo instanceof NetworkInfo) && (oldIntentInfo instanceof NetworkInfo)) {
            newNetworkInfo = (NetworkInfo) newIntentInfo;
            oldNetworkInfo = (NetworkInfo) oldIntentInfo;
        }
        if (newNetworkInfo == null || oldNetworkInfo == null || newNetworkInfo.getState() != oldNetworkInfo.getState() || newNetworkInfo.getDetailedState() != oldNetworkInfo.getDetailedState()) {
            return false;
        }
        return true;
    }

    private String getSysPkg(int pid, int uid) {
        for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mAwareParallelProxyBrMap.entrySet()) {
            ArrayList<HwBroadcastRecord> iawareBrList = ent.getValue();
            int listSize = iawareBrList.size();
            int index = 0;
            while (true) {
                if (index < listSize) {
                    HwBroadcastRecord hwBr = iawareBrList.get(index);
                    if (hwBr.getReceiverPid() == pid && hwBr.getReceiverUid() == uid) {
                        return hwBr.getReceiverPkg();
                    }
                    index++;
                }
            }
        }
        return null;
    }

    private boolean isRestrictTopApp() {
        return this.mAwareBrPolicy.isInstallApp() && !this.mAwareBrPolicy.isSpeedNoCtrol() && isProcessProxyTopList();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processException() {
        synchronized (this.mAwareParallelProxyBrMap) {
            int length = 0;
            for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mAwareParallelProxyBrMap.entrySet()) {
                length += ent.getValue().size();
            }
            if (length > 10000) {
                AwareLog.w(TAG, "iaware_br proxy length more than " + length + ", clear all proxy br");
                for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent2 : this.mAwareParallelProxyBrMap.entrySet()) {
                    ent2.getValue().clear();
                }
            }
        }
    }
}

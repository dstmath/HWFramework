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
    private static final int IAWARE_PROXY_QUEUE_MIN_LENGTH = 10;
    private static final int IAWARE_UNPROXY_SCREEN_OFF_TIME = 5000;
    private static final int MSG_ENQUEUE_PARALL_BR = 102;
    private static final int MSG_PROCESS_FGAPP_PARALL_BR = 103;
    private static final int MSG_PROCESS_PARALL_BR = 101;
    static final String TAG = "AwareBroadcastProcess";
    private String mBrName;
    private final IawareBroadcastHandler mHandler;
    private AwareBroadcastPolicy mIawareBrPolicy;
    private final HashMap<Integer, ArrayList<HwBroadcastRecord>> mIawareParallelProxyBrMap = new HashMap<>();
    private int mIawareUnProxyTime;
    private int mProxyKeyBrIndex;
    private boolean mStartUnproxy;
    private int mUnproxyHighSpeed;
    private int mUnproxyMaxDuration;
    private int mUnproxyMaxSpeed;
    private int mUnproxyMiddleSpeed;
    private int mUnproxyMinSpeed;

    private final class IawareBroadcastHandler extends Handler {
        public IawareBroadcastHandler(Looper looper) {
            super(looper, null, true);
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

    public AwareBroadcastProcess(AwareBroadcastPolicy iawareBrPolicy, Handler handler, String name) {
        this.mStartUnproxy = false;
        this.mIawareUnProxyTime = 150;
        this.mProxyKeyBrIndex = -1;
        this.mIawareBrPolicy = null;
        this.mBrName = null;
        this.mUnproxyMaxDuration = 20000;
        this.mUnproxyMaxSpeed = 150;
        this.mUnproxyMinSpeed = 60;
        this.mUnproxyMiddleSpeed = 40;
        this.mUnproxyHighSpeed = 20;
        this.mIawareBrPolicy = iawareBrPolicy;
        this.mHandler = new IawareBroadcastHandler(handler.getLooper());
        this.mBrName = name;
        for (int index = 0; index < 4; index++) {
            this.mIawareParallelProxyBrMap.put(Integer.valueOf(index), new ArrayList<>());
        }
    }

    public void enqueueIawareProxyBroacast(boolean isParallel, HwBroadcastRecord r) {
        if (isParallel) {
            Message msg = this.mHandler.obtainMessage(102, this);
            msg.obj = r;
            this.mHandler.sendMessage(msg);
        }
    }

    public void starUnproxyBroadcast() {
        if (!this.mStartUnproxy && getIawareBrSize() > 0) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br start unproxy, mStartBgUnproxy = true, queue name : " + this.mBrName);
            }
            this.mStartUnproxy = true;
            unProxyBroadcast(5);
        }
    }

    /* access modifiers changed from: private */
    public void insertProxyParalledBroadcast(HwBroadcastRecord r) {
        synchronized (this.mIawareParallelProxyBrMap) {
            int curAdj = r.getReceiverCurAdj();
            if (2 == r.getReceiverCurProcState()) {
                if (!r.isSysApp()) {
                    trimProxyBr(r, 0);
                }
                this.mIawareParallelProxyBrMap.get(0).add(r);
            } else if (curAdj < 0) {
                trimProxyBr(r, 1);
                this.mIawareParallelProxyBrMap.get(1).add(r);
            } else if (curAdj >= 900) {
                trimProxyBr(r, 3);
                this.mIawareParallelProxyBrMap.get(3).add(r);
            } else {
                processAdjChange(r);
                trimProxyBr(r, 2);
                this.mIawareParallelProxyBrMap.get(2).add(r);
            }
        }
    }

    private void unProxyBroadcast(int unproxyTime) {
        if (!this.mHandler.hasMessages(101)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101, this), (long) unproxyTime);
        }
    }

    /* access modifiers changed from: private */
    public void unproxyEachBroacast() {
        if (isEmptyProxyMap()) {
            this.mStartUnproxy = false;
            return;
        }
        ArrayList<HwBroadcastRecord> parallelList = new ArrayList<>();
        synchronized (this.mIawareParallelProxyBrMap) {
            HwBroadcastRecord proxyBr = getNextUnProxyBr();
            if (proxyBr != null) {
                parallelList.add(proxyBr);
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.i(TAG, "iaware_br unproxy cost time : " + (System.currentTimeMillis() - proxyBr.getDispatchClockTime()) + " : receiver :" + proxyBr.getBrReceivers().get(0) + " : action : " + proxyBr.getAction() + " : processState : " + proxyBr.getReceiverCurProcState() + ": adj : " + proxyBr.getReceiverCurAdj() + ": unproxy speed: " + this.mIawareUnProxyTime + " : size:" + getIawareBrSize());
                }
            }
            if (proxyBr != null && proxyBr.getReceiverCurAdj() <= 0 && this.mProxyKeyBrIndex >= 0) {
                this.mProxyKeyBrIndex--;
            }
        }
        iAwareUnproxyBroadcastList(parallelList);
        parallelList.clear();
        calculateUnproxySpeed();
        unProxyBroadcast(this.mIawareUnProxyTime);
    }

    private void calculateUnproxySpeed() {
        if (!this.mIawareBrPolicy.isScreenOff()) {
            if (!isRestrictTopApp() && isProcessProxyTopList()) {
                if (this.mIawareBrPolicy.isSpeedNoCtrol()) {
                    this.mIawareUnProxyTime = this.mUnproxyHighSpeed;
                } else {
                    this.mIawareUnProxyTime = this.mUnproxyMaxSpeed;
                }
            } else if (isProcessProxyHighAdjList()) {
                this.mIawareUnProxyTime = this.mUnproxyHighSpeed;
            } else if (this.mIawareBrPolicy.isSpeedNoCtrol()) {
                if (isProcessProxyMiddleAdjList()) {
                    this.mIawareUnProxyTime = this.mUnproxyMiddleSpeed;
                } else {
                    this.mIawareUnProxyTime = this.mUnproxyMinSpeed;
                }
            } else {
                ArrayList<HwBroadcastRecord> iawareParallelBroadcasts = getProcessingList();
                int length = iawareParallelBroadcasts.size();
                if (length > 0) {
                    if (this.mProxyKeyBrIndex < 0) {
                        int minDuration = Integer.MAX_VALUE;
                        for (int i = 0; i < length; i++) {
                            int duration = (int) (((iawareParallelBroadcasts.get(i).getDispatchClockTime() + ((long) this.mUnproxyMaxDuration)) - System.currentTimeMillis()) / ((long) (i + 1)));
                            if (duration < minDuration) {
                                minDuration = duration;
                                this.mProxyKeyBrIndex = i;
                            }
                        }
                        this.mIawareUnProxyTime = minDuration;
                    } else if (this.mProxyKeyBrIndex < length - 1) {
                        int minDuration2 = this.mIawareUnProxyTime;
                        int i2 = this.mProxyKeyBrIndex;
                        while (true) {
                            i2++;
                            if (i2 >= length) {
                                break;
                            }
                            int duration2 = (int) (((iawareParallelBroadcasts.get(i2).getDispatchClockTime() + ((long) this.mUnproxyMaxDuration)) - System.currentTimeMillis()) / ((long) (i2 + 1)));
                            if (duration2 < minDuration2) {
                                minDuration2 = duration2;
                                this.mProxyKeyBrIndex = i2;
                            }
                        }
                        this.mIawareUnProxyTime = minDuration2;
                    }
                }
                resetUnproxyTime();
            }
        }
    }

    private void resetUnproxyTime() {
        int tempUnProxyTime = this.mIawareUnProxyTime > this.mUnproxyMinSpeed ? this.mIawareUnProxyTime : this.mUnproxyMinSpeed;
        this.mIawareUnProxyTime = this.mUnproxyMaxSpeed < tempUnProxyTime ? this.mUnproxyMaxSpeed : tempUnProxyTime;
    }

    private void iAwareUnproxyBroadcastList(ArrayList<HwBroadcastRecord> parallelList) {
        if (parallelList.size() > 0) {
            HwMtmBroadcastResourceManager.insertIawareBroadcast(parallelList, this.mBrName);
        }
    }

    public int getIawareBrSize() {
        int length;
        synchronized (this.mIawareParallelProxyBrMap) {
            length = 0;
            for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mIawareParallelProxyBrMap.entrySet()) {
                length += ent.getValue().size();
            }
        }
        return length;
    }

    public void setUnProxySpeedScreenOff() {
        int size = getIawareBrSize();
        if (size > 0) {
            int speed = IAWARE_UNPROXY_SCREEN_OFF_TIME / size;
            this.mIawareUnProxyTime = speed < this.mUnproxyMinSpeed ? speed : this.mUnproxyMinSpeed;
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

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0203, code lost:
        r0 = th;
     */
    public void dump(PrintWriter pw) {
        ArrayList<HwBroadcastRecord> iawareTopParallelBroadcasts;
        ArrayList<HwBroadcastRecord> iawareHighAdjParallelBroadcasts;
        ArrayList<HwBroadcastRecord> iawareMiddleAdjParallelBroadcasts;
        ArrayList<HwBroadcastRecord> iawareLowAdjParallelBroadcasts;
        PrintWriter printWriter = pw;
        if (printWriter != null) {
            printWriter.println("      Proxy broadcast [" + this.mBrName + "]");
            ArraySet arraySet = new ArraySet();
            Set<String> proxyTopPkgs = new ArraySet<>();
            Set<String> proxyHighAdjPkgs = new ArraySet<>();
            Set<String> proxyMiddleAdjPkgs = new ArraySet<>();
            Set<String> proxyLowAdjPkgs = new ArraySet<>();
            synchronized (this.mIawareParallelProxyBrMap) {
                try {
                    iawareTopParallelBroadcasts = new ArrayList<>(this.mIawareParallelProxyBrMap.get(0));
                    iawareHighAdjParallelBroadcasts = new ArrayList<>(this.mIawareParallelProxyBrMap.get(1));
                    iawareMiddleAdjParallelBroadcasts = new ArrayList<>(this.mIawareParallelProxyBrMap.get(2));
                    iawareLowAdjParallelBroadcasts = new ArrayList<>(this.mIawareParallelProxyBrMap.get(3));
                } catch (Throwable th) {
                    th = th;
                    ArraySet arraySet2 = arraySet;
                    while (true) {
                        throw th;
                    }
                }
            }
            int lengthTop = iawareTopParallelBroadcasts.size();
            int lengthHighAdj = iawareHighAdjParallelBroadcasts.size();
            int lengthMiddleAdj = iawareMiddleAdjParallelBroadcasts.size();
            int lengthLowAdj = iawareLowAdjParallelBroadcasts.size();
            int index = 0;
            int listSize = iawareTopParallelBroadcasts.size();
            while (true) {
                int listSize2 = listSize;
                if (index >= listSize2) {
                    break;
                }
                ArrayList<HwBroadcastRecord> iawareTopParallelBroadcasts2 = iawareTopParallelBroadcasts;
                HwBroadcastRecord hwBr = iawareTopParallelBroadcasts.get(index);
                int listSize3 = listSize2;
                String action = hwBr.getAction();
                arraySet.add(action);
                String str = action;
                proxyTopPkgs.add(hwBr.getReceiverPkg());
                index++;
                iawareTopParallelBroadcasts = iawareTopParallelBroadcasts2;
                listSize = listSize3;
            }
            int index2 = 0;
            int listSize4 = iawareHighAdjParallelBroadcasts.size();
            while (index2 < listSize4) {
                HwBroadcastRecord hwBr2 = iawareHighAdjParallelBroadcasts.get(index2);
                int listSize5 = listSize4;
                String action2 = hwBr2.getAction();
                arraySet.add(action2);
                String str2 = action2;
                proxyHighAdjPkgs.add(hwBr2.getReceiverPkg());
                index2++;
                listSize4 = listSize5;
            }
            int index3 = 0;
            int listSize6 = iawareMiddleAdjParallelBroadcasts.size();
            while (index3 < listSize6) {
                HwBroadcastRecord hwBr3 = iawareMiddleAdjParallelBroadcasts.get(index3);
                int listSize7 = listSize6;
                String action3 = hwBr3.getAction();
                arraySet.add(action3);
                String str3 = action3;
                proxyMiddleAdjPkgs.add(hwBr3.getReceiverPkg());
                index3++;
                listSize6 = listSize7;
            }
            int index4 = 0;
            int listSize8 = iawareLowAdjParallelBroadcasts.size();
            while (index4 < listSize8) {
                HwBroadcastRecord hwBr4 = iawareLowAdjParallelBroadcasts.get(index4);
                int listSize9 = listSize8;
                String action4 = hwBr4.getAction();
                arraySet.add(action4);
                String str4 = action4;
                proxyLowAdjPkgs.add(hwBr4.getReceiverPkg());
                index4++;
                listSize8 = listSize9;
            }
            printWriter.println("      proxy action :" + arraySet);
            printWriter.println("      proxy Top pkg :" + proxyTopPkgs);
            printWriter.println("      proxy high adj pkg :" + proxyHighAdjPkgs);
            printWriter.println("      proxy middle adj pkg :" + proxyMiddleAdjPkgs);
            printWriter.println("      proxy low adj pkg :" + proxyLowAdjPkgs);
            int length = lengthTop + lengthHighAdj + lengthMiddleAdj + lengthLowAdj;
            if (length == 0) {
                printWriter.println("      Unproxy speed :0");
                ArraySet arraySet3 = arraySet;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("      Unproxy speed :");
                ArraySet arraySet4 = arraySet;
                sb.append(this.mIawareUnProxyTime);
                printWriter.println(sb.toString());
            }
            printWriter.println("      Proxy broadcast count :" + length + ", Top count:" + lengthTop + ", high adj count:" + lengthHighAdj + ", middle adj count:" + lengthMiddleAdj + ", low adj count:" + lengthLowAdj);
        }
    }

    public void startUnproxyFgAppBroadcast(int pid, int uid) {
        if (getIawareBrSize() != 0) {
            Message msg = this.mHandler.obtainMessage(103, this);
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void unProxyForegroundAppBroadcast(int pid, int uid) {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.d(TAG, "iaware_br unProxyForegroundAppBroadcast, uid:" + uid + ", pid: " + pid);
        }
        ArrayList<HwBroadcastRecord> parallelList = this.mIawareParallelProxyBrMap.get(0);
        synchronized (this.mIawareParallelProxyBrMap) {
            String pkgSys = null;
            if (1000 == uid) {
                try {
                    pkgSys = getSysPkg(pid, uid);
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.i(TAG, "iaware_br unProxyForegroundAppBroadcast system app, uid:" + uid + ", pid: " + pid + ": pkg : " + pkgSys);
                    }
                    if (pkgSys == null) {
                        return;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mIawareParallelProxyBrMap.entrySet()) {
                if (ent.getKey().intValue() != 0) {
                    iAwareUnproxyBroadcastInner(ent.getValue(), uid, parallelList, pkgSys);
                }
            }
        }
    }

    private void iAwareUnproxyBroadcastInner(ArrayList<HwBroadcastRecord> pendingBroadcasts, int unProxyUid, ArrayList<HwBroadcastRecord> unProxyBroadcasts, String unProxyPkgSys) {
        Iterator it = pendingBroadcasts.iterator();
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
        for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mIawareParallelProxyBrMap.entrySet()) {
            if (ent.getValue().size() > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isProcessProxyTopList() {
        return this.mIawareParallelProxyBrMap.get(0).size() != 0;
    }

    private boolean isProcessProxyHighAdjList() {
        return this.mIawareParallelProxyBrMap.get(1).size() != 0;
    }

    private boolean isProcessProxyMiddleAdjList() {
        return this.mIawareParallelProxyBrMap.get(0).size() == 0 && this.mIawareParallelProxyBrMap.get(1).size() == 0 && this.mIawareParallelProxyBrMap.get(2).size() != 0;
    }

    private HwBroadcastRecord getNextUnProxyBr() {
        for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mIawareParallelProxyBrMap.entrySet()) {
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
            return this.mIawareParallelProxyBrMap.get(2);
        }
        return this.mIawareParallelProxyBrMap.get(3);
    }

    private void processAdjChange(HwBroadcastRecord r) {
        String pkg = r.getReceiverPkg();
        if (pkg != null) {
            int pid = r.getReceiverPid();
            ArrayList<HwBroadcastRecord> iawareMiddleBrList = this.mIawareParallelProxyBrMap.get(2);
            Iterator it = this.mIawareParallelProxyBrMap.get(3).iterator();
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

    private void trimProxyBr(HwBroadcastRecord r, int level) {
        if (this.mIawareBrPolicy.isTrimAction(r.getAction())) {
            Iterator it = this.mIawareParallelProxyBrMap.get(Integer.valueOf(level)).iterator();
            while (it.hasNext()) {
                HwBroadcastRecord br = (HwBroadcastRecord) it.next();
                if (canTrim(r, br)) {
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.d(TAG, "iaware_br brtrim success: " + br.getBrReceivers().get(0) + ": action: " + r.getAction());
                    }
                    it.remove();
                    return;
                }
            }
        }
    }

    private boolean canTrim(HwBroadcastRecord newBr, HwBroadcastRecord oldBr) {
        String pkg1 = newBr.getReceiverPkg();
        String pkg2 = oldBr.getReceiverPkg();
        if (pkg1 == null || pkg2 == null || !pkg1.equals(pkg2) || !newBr.isSameReceiver(oldBr)) {
            return false;
        }
        String action1 = newBr.getAction();
        String action2 = oldBr.getAction();
        if (action1 == null || action2 == null || !action1.equals(action2)) {
            return false;
        }
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action1)) {
            return canConnectivityDataTrim(newBr.getIntent(), oldBr.getIntent());
        }
        if ("android.net.wifi.STATE_CHANGE".equals(action1)) {
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
            try {
                newInfo = (NetworkInfo) newIntent.getExtra("networkInfo");
                oldInfo = (NetworkInfo) oldIntent.getExtra("networkInfo");
            } catch (ClassCastException e) {
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.e(TAG, "iaware_br get NetworkInfo from intent error.");
                }
            }
            if (newInfo == null || oldInfo == null || newInfo.getState() != oldInfo.getState()) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean canWifiDataTrim(Intent newIntent, Intent oldIntent) {
        NetworkInfo newNetworkInfo = (NetworkInfo) newIntent.getParcelableExtra("networkInfo");
        NetworkInfo oldNetworkInfo = (NetworkInfo) oldIntent.getParcelableExtra("networkInfo");
        if (newNetworkInfo == null || oldNetworkInfo == null || newNetworkInfo.getState() != oldNetworkInfo.getState() || newNetworkInfo.getDetailedState() != oldNetworkInfo.getDetailedState()) {
            return false;
        }
        return true;
    }

    private String getSysPkg(int pid, int uid) {
        for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mIawareParallelProxyBrMap.entrySet()) {
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
        return this.mIawareBrPolicy.isInstallApp() && !this.mIawareBrPolicy.isSpeedNoCtrol() && isProcessProxyTopList();
    }

    /* access modifiers changed from: private */
    public void processException() {
        synchronized (this.mIawareParallelProxyBrMap) {
            int length = 0;
            for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent : this.mIawareParallelProxyBrMap.entrySet()) {
                length += ent.getValue().size();
            }
            if (length > 10000) {
                AwareLog.w(TAG, "iaware_br proxy length more than " + length + ", clear all proxy br");
                for (Map.Entry<Integer, ArrayList<HwBroadcastRecord>> ent2 : this.mIawareParallelProxyBrMap.entrySet()) {
                    ent2.getValue().clear();
                }
            }
        }
    }
}

package com.android.server.rms.iaware.cpu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.qos.AwareQosFeatureManager;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.ProcessExt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CpuKeyBackground {
    private static final String GRP_BACKGROUND = "/background";
    public static final String GRP_KEY_BACKGROUND = "/key-background";
    private static final int KBG_MSG_LEN = 12;
    private static final String KEY_BUNDLE_GRP = "KEY_GRP";
    private static final String KEY_BUNDLE_PID = "KEY_PID";
    private static final String KEY_BUNDLE_UID = "KEY_UID";
    private static final Object SLOCK = new Object();
    private static final String TAG = "CpuKeyBackground";
    private static CpuKeyBackground sInstance;
    private AwareStateCallback mAwareStateCallback;
    private CpuFeature mCpuFeatureInstance;
    private KeyBackgroundHandler mKeyBackgroundHandler;

    private CpuKeyBackground() {
    }

    public static CpuKeyBackground getInstance() {
        CpuKeyBackground cpuKeyBackground;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuKeyBackground();
            }
            cpuKeyBackground = sInstance;
        }
        return cpuKeyBackground;
    }

    public void start(CpuFeature feature) {
        initHandler();
        this.mCpuFeatureInstance = feature;
        registerStateCallback();
    }

    private void registerStateCallback() {
        if (this.mAwareStateCallback == null) {
            this.mAwareStateCallback = new AwareStateCallback();
            AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 0);
            AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 5);
            AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 2);
        }
    }

    public void destroy() {
        unregisterStateCallback();
    }

    private void unregisterStateCallback() {
        if (this.mAwareStateCallback != null) {
            AwareAppKeyBackgroup.getInstance().unregisterStateCallback(this.mAwareStateCallback, 0);
            AwareAppKeyBackgroup.getInstance().unregisterStateCallback(this.mAwareStateCallback, 5);
            AwareAppKeyBackgroup.getInstance().unregisterStateCallback(this.mAwareStateCallback, 2);
            this.mAwareStateCallback = null;
        }
    }

    private void initHandler() {
        if (this.mKeyBackgroundHandler == null) {
            this.mKeyBackgroundHandler = new KeyBackgroundHandler();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkKeyBackgroundAndSendMsg(int pid, int uid) {
        KeyBackgroundHandler keyBackgroundHandler;
        boolean isKbg = AwareAppKeyBackgroup.getInstance().checkIsKeyBackgroup(pid, uid);
        if (isProtectPackage(getAppPkgName(pid, uid)) && isKbg) {
            setProcessGroup(pid, -1);
        } else if (isKbg && (keyBackgroundHandler = this.mKeyBackgroundHandler) != null) {
            Message msg = keyBackgroundHandler.obtainMessage(104);
            msg.arg1 = pid;
            this.mKeyBackgroundHandler.sendMessage(msg);
        }
    }

    public void sendSwitchGroupMessage(int pid, int messgaeId) {
        KeyBackgroundHandler keyBackgroundHandler = this.mKeyBackgroundHandler;
        if (keyBackgroundHandler != null) {
            Message msg = keyBackgroundHandler.obtainMessage(messgaeId);
            msg.arg1 = pid;
            this.mKeyBackgroundHandler.sendMessage(msg);
        }
    }

    private void moveForkToBackground(int uid, int pid) {
        if (this.mKeyBackgroundHandler != null) {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                fis = new FileInputStream("/acct/uid_" + uid + "/pid_" + pid + "/cgroup.procs");
                isr = new InputStreamReader(fis, "UTF-8");
                br = new BufferedReader(isr);
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    int heriPid = Integer.parseInt(line.trim());
                    if (heriPid != pid) {
                        if (checkIsTargetGroup(heriPid, GRP_KEY_BACKGROUND)) {
                            Message msg = this.mKeyBackgroundHandler.obtainMessage(105);
                            msg.arg1 = heriPid;
                            this.mKeyBackgroundHandler.sendMessage(msg);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
            } catch (FileNotFoundException e2) {
                AwareLog.e(TAG, "Invalid file");
            } catch (UnsupportedEncodingException e3) {
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
            } catch (IOException e4) {
                AwareLog.e(TAG, "IOException " + e4.getMessage());
            } catch (Throwable th) {
                FileContent.closeBufferedReader(null);
                FileContent.closeInputStreamReader(null);
                FileContent.closeFileInputStream(null);
                throw th;
            }
            FileContent.closeBufferedReader(br);
            FileContent.closeInputStreamReader(isr);
            FileContent.closeFileInputStream(fis);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessagesByUid(int code, int uid, String grp) {
        List<ProcessInfo> procs = getProcessesByUid(uid);
        if (!(procs.isEmpty() || this.mKeyBackgroundHandler == null)) {
            int procsSize = procs.size();
            for (int i = 0; i < procsSize; i++) {
                ProcessInfo info = procs.get(i);
                if (info != null && checkIsTargetGroup(info.mPid, grp)) {
                    Message msg = this.mKeyBackgroundHandler.obtainMessage(code);
                    msg.arg1 = info.mPid;
                    this.mKeyBackgroundHandler.sendMessage(msg);
                    if (code == 105) {
                        moveForkToBackground(uid, info.mPid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getAppPkgName(int pid, int uid) {
        if (pid > 0) {
            return InnerUtils.getAwarePkgName(pid);
        }
        return InnerUtils.getPackageNameByUid(uid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkIsNativeKeyBackgroupApp(String pkgName) {
        if (pkgName != null && AwareDefaultConfigList.getInstance().getKeyHabitAppList().contains(pkgName)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class AwareStateCallback implements AwareAppKeyBackgroup.IAwareStateCallback {
        private AwareStateCallback() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback
        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            if (stateType == 5) {
                NetManager.getInstance().sendMsgToNetMng(uid, eventType, 3);
            }
            if (stateType == 2) {
                NetManager.getInstance().sendMsgToNetMng(uid, eventType, 4);
            }
            if (stateType == 0 && CpuFeature.isCpusetEnable()) {
                String pkgName = CpuKeyBackground.this.getAppPkgName(pid, uid);
                if (!CpuKeyBackground.this.checkIsNativeKeyBackgroupApp(pkgName)) {
                    if (!CpuKeyBackground.this.isProtectPackage(pkgName)) {
                        if (eventType == 1) {
                            CpuKeyBackground.this.sendMessagesByUid(104, uid, CpuKeyBackground.GRP_BACKGROUND);
                        } else if (eventType == 2) {
                            CpuKeyBackground.this.sendMessagesByUid(105, uid, CpuKeyBackground.GRP_KEY_BACKGROUND);
                        } else {
                            AwareLog.d(CpuKeyBackground.TAG, "onStateChanged invaild stateType");
                        }
                    } else if (eventType == 1) {
                        CpuKeyBackground.this.setProcessGroupByUid(uid);
                    }
                }
            }
        }
    }

    private List<ProcessInfo> getProcessesByUid(int uid) {
        List<ProcessInfo> procs = new ArrayList<>();
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList.isEmpty()) {
            AwareLog.e(TAG, "getProcessesByUid procList is null!");
            return procs;
        }
        Iterator<ProcessInfo> it = procList.iterator();
        while (it.hasNext()) {
            ProcessInfo info = it.next();
            if (info != null && uid == info.mUid) {
                procs.add(info);
            }
        }
        return procs;
    }

    public void notifyProcessGroupChange(int pid, int uid, int grp) {
        KeyBackgroundHandler keyBackgroundHandler;
        if (CpuFeature.isCpusetEnable() && (keyBackgroundHandler = this.mKeyBackgroundHandler) != null) {
            Message msg = keyBackgroundHandler.obtainMessage(CpuFeature.MSG_PROCESS_GROUP_CHANGE);
            Bundle bundle = new Bundle();
            bundle.putInt("KEY_PID", pid);
            bundle.putInt(KEY_BUNDLE_UID, uid);
            bundle.putInt(KEY_BUNDLE_GRP, grp);
            msg.setData(bundle);
            this.mKeyBackgroundHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doKeyBackground(int pid, int oldGroup, int newGroup) {
        if (CpuFeature.isCpusetEnable()) {
            int isLimit = 0;
            if (newGroup == 8 && !AwareQosFeatureManager.getInstance().doCheckPreceptible(pid)) {
                isLimit = 1;
            }
            HwActivityManager.requestProcessGroupChange(pid, oldGroup, newGroup, isLimit);
        }
    }

    /* access modifiers changed from: private */
    public class KeyBackgroundHandler extends Handler {
        private KeyBackgroundHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 104:
                    CpuKeyBackground.this.doKeyBackground(msg.arg1, 0, 8);
                    return;
                case 105:
                    CpuKeyBackground.this.doKeyBackground(msg.arg1, 8, 0);
                    return;
                case CpuFeature.MSG_PROCESS_GROUP_CHANGE /* 106 */:
                    Bundle bundle = msg.getData();
                    if (bundle == null) {
                        AwareLog.e(CpuKeyBackground.TAG, "handleMessage inavlid params null bundle!");
                        return;
                    }
                    int pid = bundle.getInt("KEY_PID");
                    int uid = bundle.getInt(CpuKeyBackground.KEY_BUNDLE_UID);
                    if (bundle.getInt(CpuKeyBackground.KEY_BUNDLE_GRP) == 0) {
                        CpuKeyBackground.this.checkKeyBackgroundAndSendMsg(pid, uid);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public boolean checkIsTargetGroup(int pid, String grp) {
        if (grp == null) {
            AwareLog.e(TAG, "checkIsTargetGroup invalid params");
            return false;
        }
        File file = new File("/proc/" + pid + "/cpuset");
        FileInputStream inputStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream inputStream2 = new FileInputStream(file);
            while (true) {
                int temp = inputStream2.read();
                if (temp != -1) {
                    char ch = (char) temp;
                    if (!(ch == '\n' || ch == '\r')) {
                        sb.append(ch);
                    }
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e.getMessage());
                    }
                }
            }
            inputStream2.close();
            if (sb.length() == 0) {
                AwareLog.e(TAG, "checkIsTargetGroup read fail");
                return false;
            } else if (grp.equals(sb.toString())) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e2) {
            AwareLog.e(TAG, "checkIsTargetGroup read catch IOException msg = " + e2.getMessage());
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e3.getMessage());
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e4.getMessage());
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isProtectPackage(String pkgName) {
        if (pkgName == null || !pkgName.contains("com.hicloud.android.clone")) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setProcessGroupByUid(int uid) {
        List<ProcessInfo> procs = getProcessesByUid(uid);
        int procsSize = procs.size();
        for (int i = 0; i < procsSize; i++) {
            ProcessInfo info = procs.get(i);
            if (info != null && checkIsTargetGroup(info.mPid, GRP_BACKGROUND)) {
                setProcessGroup(info.mPid, -1);
            }
        }
    }

    private void setProcessGroup(int pid, int schedGroup) {
        try {
            ProcessExt.setProcessGroup(pid, schedGroup);
        } catch (IllegalArgumentException | SecurityException e) {
            AwareLog.e(TAG, "setProcessGroup pid " + pid + e.getMessage());
        } catch (RuntimeException e2) {
            AwareLog.e(TAG, "setProcessGroup pid " + pid + e2.getMessage());
        }
    }
}

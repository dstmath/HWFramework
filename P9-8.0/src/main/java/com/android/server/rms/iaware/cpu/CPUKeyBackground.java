package com.android.server.rms.iaware.cpu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CPUKeyBackground {
    private static final String GRP_BACKGROUND = "/background";
    public static final String GRP_KEY_BACKGROUND = "/key-background";
    private static final String GRP_TOP_APP = "/top-app";
    private static final String KEY_BUNDLE_GRP = "KEY_GRP";
    private static final String KEY_BUNDLE_PID = "KEY_PID";
    private static final String KEY_BUNDLE_UID = "KEY_UID";
    private static final String TAG = "CPUKeyBackground";
    private static CPUKeyBackground sInstance;
    private AwareStateCallback mAwareStateCallback;
    private CPUFeature mCPUFeatureInstance;
    private KeyBackgroundHandler mKeyBackgroundHandler;

    private class AwareStateCallback implements IAwareStateCallback {
        /* synthetic */ AwareStateCallback(CPUKeyBackground this$0, AwareStateCallback -this1) {
            this();
        }

        private AwareStateCallback() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            if (stateType == 5) {
                NetManager.getInstance().sendMsgToNetMng(uid, eventType, 3);
            } else if (stateType == 2) {
                NetManager.getInstance().sendMsgToNetMng(uid, eventType, 4);
            }
            if (stateType == 0 && CPUFeature.isCpusetEnable() && !CPUKeyBackground.this.checkIsNativeKeyBackgroupApp(pid, uid)) {
                if (1 == eventType) {
                    CPUKeyBackground.this.sendMessagesByUid(104, uid, CPUKeyBackground.GRP_BACKGROUND);
                } else if (2 == eventType) {
                    CPUKeyBackground.this.sendMessagesByUid(105, uid, CPUKeyBackground.GRP_KEY_BACKGROUND);
                }
            }
        }
    }

    private class KeyBackgroundHandler extends Handler {
        /* synthetic */ KeyBackgroundHandler(CPUKeyBackground this$0, KeyBackgroundHandler -this1) {
            this();
        }

        private KeyBackgroundHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 104:
                    CPUKeyBackground.this.doKBackground(104, msg.arg1);
                    break;
                case 105:
                    CPUKeyBackground.this.doKBackground(105, msg.arg1);
                    break;
                case 106:
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        int pid = bundle.getInt(CPUKeyBackground.KEY_BUNDLE_PID);
                        int uid = bundle.getInt(CPUKeyBackground.KEY_BUNDLE_UID);
                        if (bundle.getInt(CPUKeyBackground.KEY_BUNDLE_GRP) == 0) {
                            CPUKeyBackground.this.checkKeyBackgroundAndSendMsg(pid, uid);
                            break;
                        }
                    }
                    AwareLog.e(CPUKeyBackground.TAG, "handleMessage inavlid params null bundle!");
                    return;
                    break;
            }
        }
    }

    private CPUKeyBackground() {
    }

    public static synchronized CPUKeyBackground getInstance() {
        CPUKeyBackground cPUKeyBackground;
        synchronized (CPUKeyBackground.class) {
            if (sInstance == null) {
                sInstance = new CPUKeyBackground();
            }
            cPUKeyBackground = sInstance;
        }
        return cPUKeyBackground;
    }

    public void start(CPUFeature feature) {
        initHandler();
        this.mCPUFeatureInstance = feature;
        registerStateCallback();
    }

    private void registerStateCallback() {
        if (this.mAwareStateCallback == null) {
            this.mAwareStateCallback = new AwareStateCallback(this, null);
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
            this.mKeyBackgroundHandler = new KeyBackgroundHandler(this, null);
        }
    }

    private boolean checkKeyBackgroundAndSendMsg(int pid, int uid) {
        boolean isKeyBG = AwareAppKeyBackgroup.getInstance().checkIsKeyBackgroup(pid, uid);
        if (isKeyBG && this.mKeyBackgroundHandler != null) {
            Message msg = this.mKeyBackgroundHandler.obtainMessage(104);
            msg.arg1 = pid;
            this.mKeyBackgroundHandler.sendMessage(msg);
        }
        return isKeyBG;
    }

    public void sendSwitchGroupMessage(int pid, int messgaeId) {
        if (this.mKeyBackgroundHandler != null) {
            Message msg = this.mKeyBackgroundHandler.obtainMessage(messgaeId);
            msg.arg1 = pid;
            this.mKeyBackgroundHandler.sendMessage(msg);
        }
    }

    private void closeBufferedReader(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeBufferedReader exception " + e.getMessage());
            }
        }
    }

    private void closeInputStreamReader(InputStreamReader isr) {
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeInputStreamReader exception " + e.getMessage());
            }
        }
    }

    private void closeFileInputStream(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "closeFileInputStream exception " + e.getMessage());
            }
        }
    }

    private void moveForkToBackground(int uid, int pid) {
        NumberFormatException e;
        Throwable th;
        FileNotFoundException e2;
        UnsupportedEncodingException e3;
        IOException e4;
        if (this.mKeyBackgroundHandler != null) {
            StringBuilder strPath = new StringBuilder();
            strPath.append("/acct/uid_").append(uid).append("/pid_").append(pid).append("/cgroup.procs");
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                InputStreamReader isr2;
                FileInputStream fis2 = new FileInputStream(strPath.toString());
                try {
                    isr2 = new InputStreamReader(fis2, "UTF-8");
                } catch (NumberFormatException e5) {
                    e = e5;
                    fis = fis2;
                    try {
                        AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (Throwable th2) {
                        th = th2;
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                        throw th;
                    }
                } catch (FileNotFoundException e6) {
                    e2 = e6;
                    fis = fis2;
                    AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (UnsupportedEncodingException e7) {
                    e3 = e7;
                    fis = fis2;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (IOException e8) {
                    e4 = e8;
                    fis = fis2;
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (Throwable th3) {
                    th = th3;
                    fis = fis2;
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                    throw th;
                }
                try {
                    BufferedReader br2 = new BufferedReader(isr2);
                    try {
                        String str = "";
                        while (true) {
                            str = br2.readLine();
                            if (str == null) {
                                break;
                            }
                            int heriPid = Integer.parseInt(str.trim());
                            if (heriPid != pid && checkIsTargetGroup(heriPid, GRP_KEY_BACKGROUND)) {
                                Message msg = this.mKeyBackgroundHandler.obtainMessage(105);
                                msg.arg1 = heriPid;
                                this.mKeyBackgroundHandler.sendMessage(msg);
                            }
                        }
                        closeBufferedReader(br2);
                        closeInputStreamReader(isr2);
                        closeFileInputStream(fis2);
                    } catch (NumberFormatException e9) {
                        e = e9;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (FileNotFoundException e10) {
                        e2 = e10;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (UnsupportedEncodingException e11) {
                        e3 = e11;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (IOException e12) {
                        e4 = e12;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "IOException " + e4.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (Throwable th4) {
                        th = th4;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                        throw th;
                    }
                } catch (NumberFormatException e13) {
                    e = e13;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (FileNotFoundException e14) {
                    e2 = e14;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (UnsupportedEncodingException e15) {
                    e3 = e15;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (IOException e16) {
                    e4 = e16;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                } catch (Throwable th5) {
                    th = th5;
                    isr = isr2;
                    fis = fis2;
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                    throw th;
                }
            } catch (NumberFormatException e17) {
                e = e17;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            } catch (FileNotFoundException e18) {
                e2 = e18;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            } catch (UnsupportedEncodingException e19) {
                e3 = e19;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            } catch (IOException e20) {
                e4 = e20;
                AwareLog.e(TAG, "IOException " + e4.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
            }
        }
    }

    private void sendMessagesByUid(int code, int uid, String grp) {
        List<ProcessInfo> procs = getProcessesByUid(uid);
        if (!procs.isEmpty() && this.mKeyBackgroundHandler != null) {
            int procsSize = procs.size();
            for (int i = 0; i < procsSize; i++) {
                ProcessInfo info = (ProcessInfo) procs.get(i);
                if (info != null && checkIsTargetGroup(info.mPid, grp)) {
                    Message msg = this.mKeyBackgroundHandler.obtainMessage(code);
                    msg.arg1 = info.mPid;
                    this.mKeyBackgroundHandler.sendMessage(msg);
                    if (105 == code) {
                        moveForkToBackground(uid, info.mPid);
                    }
                }
            }
        }
    }

    private boolean checkIsNativeKeyBackgroupApp(int pid, int uid) {
        String pkgName;
        if (pid > 0) {
            pkgName = InnerUtils.getAwarePkgName(pid);
        } else {
            pkgName = InnerUtils.getPackageNameByUid(uid);
        }
        List<String> keyHabitPkgs = AwareDefaultConfigList.getInstance().getKeyHabitAppList();
        if (pkgName == null || !keyHabitPkgs.contains(pkgName)) {
            return false;
        }
        return true;
    }

    private List<ProcessInfo> getProcessesByUid(int uid) {
        List<ProcessInfo> procs = new ArrayList();
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList.isEmpty()) {
            AwareLog.e(TAG, "getProcessesByUid procList is null!");
            return procs;
        }
        int procListSize = procList.size();
        for (int i = 0; i < procListSize; i++) {
            ProcessInfo info = (ProcessInfo) procList.get(i);
            if (info != null && uid == info.mUid) {
                procs.add(info);
            }
        }
        return procs;
    }

    public void notifyProcessGroupChange(int pid, int uid, int grp) {
        if (CPUFeature.isCpusetEnable() && this.mKeyBackgroundHandler != null) {
            Message msg = this.mKeyBackgroundHandler.obtainMessage(106);
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_BUNDLE_PID, pid);
            bundle.putInt(KEY_BUNDLE_UID, uid);
            bundle.putInt(KEY_BUNDLE_GRP, grp);
            msg.setData(bundle);
            this.mKeyBackgroundHandler.sendMessage(msg);
        }
    }

    private int doKBackground(int msg, int pid) {
        long time = System.currentTimeMillis();
        if (!CPUFeature.isCpusetEnable()) {
            return 0;
        }
        if (this.mCPUFeatureInstance == null) {
            AwareLog.e(TAG, "doKBackground mCPUFeatureInstance = null!");
            return -1;
        }
        StringBuilder strMsg = new StringBuilder();
        strMsg.append("doKBackground(int msg, int pid), msg is ").append(msg);
        strMsg.append(" event pid is ").append(pid).append(" process name is ").append(getProcName(pid));
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(msg);
        buffer.putInt(pid);
        int resCode = this.mCPUFeatureInstance.sendPacket(buffer);
        if (resCode != 1) {
            AwareLog.e(TAG, "doKBackground sendPacked failed, send error code:" + resCode);
        }
        String operation = strMsg.toString();
        String reason = "handle the msg" + msg;
        if (104 == msg) {
            CpuDumpRadar.getInstance().insertDumpInfo(time, operation, reason, CpuDumpRadar.STATISTICS_BG_TO_KBG_POLICY);
        } else {
            CpuDumpRadar.getInstance().insertDumpInfo(time, operation, reason, CpuDumpRadar.STATISTICS_KBG_TO_BG_POLICY);
        }
        return 0;
    }

    public void insertCgroupProcsPidList() {
        CpuDumpRadar.getInstance().insertDumpInfo(System.currentTimeMillis(), "ta:( " + getCgroupProcsPidList(GRP_TOP_APP) + ")" + " kbg:( " + getCgroupProcsPidList(GRP_KEY_BACKGROUND) + ")" + " bg:( " + getCgroupProcsPidList(GRP_BACKGROUND) + ")", "insertCgroupProcsPid", CpuDumpRadar.STATISTICS_INSERT_CGROUP_PROCS_POLICY);
    }

    private String getCgroupProcsPidList(String fileName) {
        FileNotFoundException e;
        Throwable th;
        UnsupportedEncodingException e2;
        IOException e3;
        String filePath = "/dev/cpuset" + fileName + "/cgroup.procs";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder strAllPids = new StringBuilder();
        try {
            FileInputStream fis2 = new FileInputStream(filePath);
            try {
                InputStreamReader isr2 = new InputStreamReader(fis2, "UTF-8");
                try {
                    BufferedReader br2 = new BufferedReader(isr2);
                    try {
                        String str = "";
                        while (true) {
                            str = br2.readLine();
                            if (str == null) {
                                break;
                            }
                            strAllPids.append(str).append(' ');
                        }
                        closeBufferedReader(br2);
                        closeInputStreamReader(isr2);
                        closeFileInputStream(fis2);
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        try {
                            AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                            closeBufferedReader(br);
                            closeInputStreamReader(isr);
                            closeFileInputStream(fis);
                            return strAllPids.toString();
                        } catch (Throwable th2) {
                            th = th2;
                            closeBufferedReader(br);
                            closeInputStreamReader(isr);
                            closeFileInputStream(fis);
                            throw th;
                        }
                    } catch (UnsupportedEncodingException e5) {
                        e2 = e5;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                        return strAllPids.toString();
                    } catch (IOException e6) {
                        e3 = e6;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        AwareLog.e(TAG, "IOException " + e3.getMessage());
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                        return strAllPids.toString();
                    } catch (Throwable th3) {
                        th = th3;
                        br = br2;
                        isr = isr2;
                        fis = fis2;
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                        throw th;
                    }
                } catch (FileNotFoundException e7) {
                    e = e7;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                    return strAllPids.toString();
                } catch (UnsupportedEncodingException e8) {
                    e2 = e8;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                    return strAllPids.toString();
                } catch (IOException e9) {
                    e3 = e9;
                    isr = isr2;
                    fis = fis2;
                    AwareLog.e(TAG, "IOException " + e3.getMessage());
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                    return strAllPids.toString();
                } catch (Throwable th4) {
                    th = th4;
                    isr = isr2;
                    fis = fis2;
                    closeBufferedReader(br);
                    closeInputStreamReader(isr);
                    closeFileInputStream(fis);
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e = e10;
                fis = fis2;
                AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
                return strAllPids.toString();
            } catch (UnsupportedEncodingException e11) {
                e2 = e11;
                fis = fis2;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
                return strAllPids.toString();
            } catch (IOException e12) {
                e3 = e12;
                fis = fis2;
                AwareLog.e(TAG, "IOException " + e3.getMessage());
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
                return strAllPids.toString();
            } catch (Throwable th5) {
                th = th5;
                fis = fis2;
                closeBufferedReader(br);
                closeInputStreamReader(isr);
                closeFileInputStream(fis);
                throw th;
            }
        } catch (FileNotFoundException e13) {
            e = e13;
            AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
            closeBufferedReader(br);
            closeInputStreamReader(isr);
            closeFileInputStream(fis);
            return strAllPids.toString();
        } catch (UnsupportedEncodingException e14) {
            e2 = e14;
            AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
            closeBufferedReader(br);
            closeInputStreamReader(isr);
            closeFileInputStream(fis);
            return strAllPids.toString();
        } catch (IOException e15) {
            e3 = e15;
            AwareLog.e(TAG, "IOException " + e3.getMessage());
            closeBufferedReader(br);
            closeInputStreamReader(isr);
            closeFileInputStream(fis);
            return strAllPids.toString();
        }
        return strAllPids.toString();
    }

    private String getProcName(int pid) {
        ProcessInfo info = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (info == null) {
            return "null";
        }
        return info.mProcessName;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ce A:{SYNTHETIC, Splitter: B:38:0x00ce} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x006f A:{SYNTHETIC, Splitter: B:21:0x006f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkIsTargetGroup(int pid, String grp) {
        IOException e;
        Throwable th;
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
                try {
                    int temp = inputStream2.read();
                    if (temp == -1) {
                        break;
                    }
                    char ch = (char) temp;
                    if (!(ch == 10 || ch == 13)) {
                        sb.append(ch);
                    }
                } catch (IOException e2) {
                    e = e2;
                    inputStream = inputStream2;
                    try {
                        AwareLog.e(TAG, "checkIsTargetGroup read catch IOException msg = " + e.getMessage());
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e3) {
                                AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e3.getMessage());
                            }
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inputStream2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e32) {
                            AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e32.getMessage());
                        }
                    }
                    throw th;
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e322) {
                    AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e322.getMessage());
                }
            }
            inputStream = inputStream2;
            if (sb.length() == 0) {
                AwareLog.e(TAG, "checkIsTargetGroup read fail");
                return false;
            } else if (grp.equals(sb.toString())) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e4) {
            e322 = e4;
            AwareLog.e(TAG, "checkIsTargetGroup read catch IOException msg = " + e322.getMessage());
            if (inputStream != null) {
            }
            return false;
        }
    }
}

package com.android.server.rms.iaware.cpu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.IOTController;
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
        private AwareStateCallback() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            if (stateType == 0 && CPUFeature.isCpusetEnable() && !CPUKeyBackground.this.checkIsNativeKeyBackgroupApp(pid, uid)) {
                if (1 == eventType) {
                    CPUKeyBackground.this.sendMessagesByUid(IOTController.EV_CANCEL_AUTH_ALL, uid, CPUKeyBackground.GRP_BACKGROUND);
                } else if (2 == eventType) {
                    CPUKeyBackground.this.sendMessagesByUid(CPUFeature.MSG_MOVETO_BACKGROUND, uid, CPUKeyBackground.GRP_KEY_BACKGROUND);
                }
            }
        }
    }

    private class KeyBackgroundHandler extends Handler {
        private KeyBackgroundHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case IOTController.EV_CANCEL_AUTH_ALL /*104*/:
                    CPUKeyBackground.this.doKBackground(IOTController.EV_CANCEL_AUTH_ALL, msg.arg1);
                    break;
                case CPUFeature.MSG_MOVETO_BACKGROUND /*105*/:
                    CPUKeyBackground.this.doKBackground(CPUFeature.MSG_MOVETO_BACKGROUND, msg.arg1);
                    break;
                case CPUFeature.MSG_PROCESS_GROUP_CHANGE /*106*/:
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
            this.mAwareStateCallback = new AwareStateCallback();
            AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 0);
        }
    }

    public void destroy() {
        unregisterStateCallback();
    }

    private void unregisterStateCallback() {
        if (this.mAwareStateCallback != null) {
            AwareAppKeyBackgroup.getInstance().unregisterStateCallback(this.mAwareStateCallback, 0);
            this.mAwareStateCallback = null;
        }
    }

    private void initHandler() {
        if (this.mKeyBackgroundHandler == null) {
            this.mKeyBackgroundHandler = new KeyBackgroundHandler();
        }
    }

    private boolean checkKeyBackgroundAndSendMsg(int pid, int uid) {
        boolean isKeyBG = AwareAppKeyBackgroup.getInstance().checkIsKeyBackgroup(pid, uid);
        if (isKeyBG && this.mKeyBackgroundHandler != null) {
            Message msg = this.mKeyBackgroundHandler.obtainMessage(IOTController.EV_CANCEL_AUTH_ALL);
            msg.arg1 = pid;
            this.mKeyBackgroundHandler.sendMessage(msg);
        }
        return isKeyBG;
    }

    public void sendSwitchGroupMessage(int pid, int messgaeId) {
        Message msg = this.mKeyBackgroundHandler.obtainMessage(messgaeId);
        msg.arg1 = pid;
        this.mKeyBackgroundHandler.sendMessage(msg);
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
        InputStreamReader isr;
        BufferedReader br;
        NumberFormatException e;
        Throwable th;
        FileNotFoundException e2;
        UnsupportedEncodingException e3;
        IOException e4;
        StringBuilder strPath = new StringBuilder();
        strPath.append("/acct/uid_").append(uid).append("/pid_").append(pid).append("/cgroup.procs");
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            FileInputStream fis = new FileInputStream(strPath.toString());
            try {
                isr = new InputStreamReader(fis, "UTF-8");
                try {
                    br = new BufferedReader(isr);
                } catch (NumberFormatException e5) {
                    e = e5;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    try {
                        AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(fileInputStream);
                    } catch (Throwable th2) {
                        th = th2;
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(fileInputStream);
                        throw th;
                    }
                } catch (FileNotFoundException e6) {
                    e2 = e6;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                } catch (UnsupportedEncodingException e7) {
                    e3 = e7;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                } catch (IOException e8) {
                    e4 = e8;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "IOException " + e4.getMessage());
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                } catch (Throwable th3) {
                    th = th3;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (NumberFormatException e9) {
                e = e9;
                fileInputStream = fis;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (FileNotFoundException e10) {
                e2 = e10;
                fileInputStream = fis;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (UnsupportedEncodingException e11) {
                e3 = e11;
                fileInputStream = fis;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (IOException e12) {
                e4 = e12;
                fileInputStream = fis;
                AwareLog.e(TAG, "IOException " + e4.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fis;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                throw th;
            }
            try {
                String str = AppHibernateCst.INVALID_PKG;
                while (true) {
                    str = br.readLine();
                    if (str != null) {
                        int heriPid = Integer.parseInt(str.trim());
                        if (heriPid != pid) {
                            if (checkIsTargetGroup(heriPid, GRP_KEY_BACKGROUND)) {
                                Message msg = this.mKeyBackgroundHandler.obtainMessage(CPUFeature.MSG_MOVETO_BACKGROUND);
                                msg.arg1 = heriPid;
                                this.mKeyBackgroundHandler.sendMessage(msg);
                            }
                        }
                    } else {
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                        return;
                    }
                }
            } catch (NumberFormatException e13) {
                e = e13;
                bufferedReader = br;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (FileNotFoundException e14) {
                e2 = e14;
                bufferedReader = br;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (UnsupportedEncodingException e15) {
                e3 = e15;
                bufferedReader = br;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (IOException e16) {
                e4 = e16;
                bufferedReader = br;
                inputStreamReader = isr;
                fileInputStream = fis;
                AwareLog.e(TAG, "IOException " + e4.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
            } catch (Throwable th5) {
                th = th5;
                bufferedReader = br;
                inputStreamReader = isr;
                fileInputStream = fis;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                throw th;
            }
        } catch (NumberFormatException e17) {
            e = e17;
            AwareLog.e(TAG, "NumberFormatException " + e.getMessage());
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
        } catch (FileNotFoundException e18) {
            e2 = e18;
            AwareLog.e(TAG, "FileNotFoundException " + e2.getMessage());
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
        } catch (UnsupportedEncodingException e19) {
            e3 = e19;
            AwareLog.e(TAG, "UnsupportedEncodingException " + e3.getMessage());
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
        } catch (IOException e20) {
            e4 = e20;
            AwareLog.e(TAG, "IOException " + e4.getMessage());
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
        }
    }

    private void sendMessagesByUid(int code, int uid, String grp) {
        List<ProcessInfo> procs = getProcessesByUid(uid);
        if (!procs.isEmpty() && this.mKeyBackgroundHandler != null) {
            for (int i = 0; i < procs.size(); i++) {
                ProcessInfo info = (ProcessInfo) procs.get(i);
                if (info != null && checkIsTargetGroup(info.mPid, grp)) {
                    Message msg = this.mKeyBackgroundHandler.obtainMessage(code);
                    msg.arg1 = info.mPid;
                    this.mKeyBackgroundHandler.sendMessage(msg);
                    if (CPUFeature.MSG_MOVETO_BACKGROUND == code) {
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
        for (ProcessInfo info : procList) {
            if (info != null && uid == info.mUid) {
                procs.add(info);
            }
        }
        return procs;
    }

    public void notifyProcessGroupChange(int pid, int uid, int grp) {
        if (CPUFeature.isCpusetEnable() && this.mKeyBackgroundHandler != null) {
            Message msg = this.mKeyBackgroundHandler.obtainMessage(CPUFeature.MSG_PROCESS_GROUP_CHANGE);
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
        if (IOTController.EV_CANCEL_AUTH_ALL == msg) {
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
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder strAllPids = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            try {
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                try {
                    BufferedReader br = new BufferedReader(isr);
                    try {
                        String str = AppHibernateCst.INVALID_PKG;
                        while (true) {
                            str = br.readLine();
                            if (str == null) {
                                break;
                            }
                            strAllPids.append(str).append(' ');
                        }
                        closeBufferedReader(br);
                        closeInputStreamReader(isr);
                        closeFileInputStream(fis);
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        bufferedReader = br;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        try {
                            AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                            closeBufferedReader(bufferedReader);
                            closeInputStreamReader(inputStreamReader);
                            closeFileInputStream(fileInputStream);
                            return strAllPids.toString();
                        } catch (Throwable th2) {
                            th = th2;
                            closeBufferedReader(bufferedReader);
                            closeInputStreamReader(inputStreamReader);
                            closeFileInputStream(fileInputStream);
                            throw th;
                        }
                    } catch (UnsupportedEncodingException e5) {
                        e2 = e5;
                        bufferedReader = br;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(fileInputStream);
                        return strAllPids.toString();
                    } catch (IOException e6) {
                        e3 = e6;
                        bufferedReader = br;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        AwareLog.e(TAG, "IOException " + e3.getMessage());
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(fileInputStream);
                        return strAllPids.toString();
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = br;
                        inputStreamReader = isr;
                        fileInputStream = fis;
                        closeBufferedReader(bufferedReader);
                        closeInputStreamReader(inputStreamReader);
                        closeFileInputStream(fileInputStream);
                        throw th;
                    }
                } catch (FileNotFoundException e7) {
                    e = e7;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    return strAllPids.toString();
                } catch (UnsupportedEncodingException e8) {
                    e2 = e8;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    return strAllPids.toString();
                } catch (IOException e9) {
                    e3 = e9;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    AwareLog.e(TAG, "IOException " + e3.getMessage());
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    return strAllPids.toString();
                } catch (Throwable th4) {
                    th = th4;
                    inputStreamReader = isr;
                    fileInputStream = fis;
                    closeBufferedReader(bufferedReader);
                    closeInputStreamReader(inputStreamReader);
                    closeFileInputStream(fileInputStream);
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e = e10;
                fileInputStream = fis;
                AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return strAllPids.toString();
            } catch (UnsupportedEncodingException e11) {
                e2 = e11;
                fileInputStream = fis;
                AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return strAllPids.toString();
            } catch (IOException e12) {
                e3 = e12;
                fileInputStream = fis;
                AwareLog.e(TAG, "IOException " + e3.getMessage());
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                return strAllPids.toString();
            } catch (Throwable th5) {
                th = th5;
                fileInputStream = fis;
                closeBufferedReader(bufferedReader);
                closeInputStreamReader(inputStreamReader);
                closeFileInputStream(fileInputStream);
                throw th;
            }
        } catch (FileNotFoundException e13) {
            e = e13;
            AwareLog.e(TAG, "FileNotFoundException " + e.getMessage());
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            return strAllPids.toString();
        } catch (UnsupportedEncodingException e14) {
            e2 = e14;
            AwareLog.e(TAG, "UnsupportedEncodingException " + e2.getMessage());
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            return strAllPids.toString();
        } catch (IOException e15) {
            e3 = e15;
            AwareLog.e(TAG, "IOException " + e3.getMessage());
            closeBufferedReader(bufferedReader);
            closeInputStreamReader(inputStreamReader);
            closeFileInputStream(fileInputStream);
            return strAllPids.toString();
        }
        return strAllPids.toString();
    }

    private String getProcName(int pid) {
        ProcessInfo info = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (info == null) {
            return HwCertification.SIGNATURE_DEFAULT;
        }
        return info.mProcessName;
    }

    public boolean checkIsTargetGroup(int pid, String grp) {
        IOException e;
        Throwable th;
        if (grp == null) {
            AwareLog.e(TAG, "checkIsTargetGroup invalid params");
            return false;
        }
        File file = new File("/proc/" + pid + "/cpuset");
        FileInputStream fileInputStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            while (true) {
                try {
                    int temp = inputStream.read();
                    if (temp == -1) {
                        break;
                    }
                    char ch = (char) temp;
                    if (!(ch == '\n' || ch == '\r')) {
                        sb.append(ch);
                    }
                } catch (IOException e2) {
                    e = e2;
                    fileInputStream = inputStream;
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = inputStream;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e3.getMessage());
                }
            }
            fileInputStream = inputStream;
            if (sb.length() == 0) {
                AwareLog.e(TAG, "checkIsTargetGroup read fail");
                return false;
            } else if (grp.equals(sb.toString())) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e4) {
            e3 = e4;
            try {
                AwareLog.e(TAG, "checkIsTargetGroup read catch IOException msg = " + e3.getMessage());
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e32) {
                        AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e32.getMessage());
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e322) {
                        AwareLog.e(TAG, "checkIsTargetGroup close catch IOException msg = " + e322.getMessage());
                    }
                }
                throw th;
            }
        }
    }
}

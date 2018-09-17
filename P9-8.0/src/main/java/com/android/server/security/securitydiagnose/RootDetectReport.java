package com.android.server.security.securitydiagnose;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class RootDetectReport {
    private static final int BIT_RO_QEMU = 1;
    private static final int BIT_RPROC_CLEAR = 15;
    private static final int BIT_RPROC_KERNEL = 16;
    private static final int BIT_SERVICE_ADB_ROOT = 2;
    private static final int BIT_SETGID = 2;
    private static final int BIT_SETRESGID = 8;
    private static final int BIT_SETRESUID = 4;
    private static final int BIT_SETUID = 1;
    private static final int EVT_ROOT_STATUS_REPORT = 1000;
    private static final int FILE_NOT_FOUND_ERR = -3;
    private static final String FILE_PROC_ROOT_SCAN = "/proc/root_scan";
    private static final int GENERIC_ERR = -1;
    private static final boolean HW_DEBUG;
    private static final int IO_EXCEPTION = -4;
    private static final int JSON_LEN_MAX = 1000;
    private static final String KERNEL_ROOT_STATUS_PATTER_MATCHER = "^\\d{1,5}$";
    private static final String LOG_VERSION_DOMESTIC = "3";
    private static final String LOG_VERSION_OVERSEA = "5";
    private static final int MAX_RSCAN_VALUE_LEN = 20;
    private static final int PUNC_COUNT = 6;
    private static final int ROOT_PROC_LIST_LEN_DEFAULT = 600;
    private static final int ROOT_SCAN_ENV_NOT_READY = -2;
    private static final String ROOT_STATE_MATCH = "DEVPATH=/kernel/hw_root_scanner";
    private static final boolean RS_DEBUG = false;
    private static final String TAG = "RootDetectReport";
    private static final String isEngneringMode = "1048576";
    private static RootDetectReport mInstance;
    private static long mStartTime = 0;
    private Context mContext;
    private UEventHandler mHandler;
    private HandlerThread mHandlerThread;
    private Listener mListener;
    private Timer mTriggerTimer = null;
    private final UEventObserver mUEventObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "onUEvent event: " + event);
            }
            if (RootDetectReport.this.needRootScan()) {
                long end = System.currentTimeMillis();
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "from trigger to receive the result run TIME = " + (end - RootDetectReport.mStartTime) + "ms");
                }
                RootDetectReport.this.mHandler.sendMessage(RootDetectReport.this.mHandler.obtainMessage(1000, event));
            }
        }
    };

    public interface Listener {
        void onRootReport(JSONObject jSONObject, boolean z);
    }

    private static class RootDataBundle {
        private static final int GID_UNCHANGED = -1;
        private static final int MAX_DATA_RECORDS = 100;
        private static final String RSCAN_LIST_FILE = "root_scan.list";
        private static final int RSCAN_LIST_FILE_MODE = 384;
        private static final String SYSTEM_DIR = "system/";
        private static final int UID_UNCHANGED = -1;
        private ArrayList<String> mRootDataList;

        public RootDataBundle() {
            this.mRootDataList = null;
            this.mRootDataList = new ArrayList();
        }

        private File getRootDataFile() {
            return new File(Environment.getDataDirectory(), "system/root_scan.list");
        }

        private String sha256(byte[] data) {
            if (data == null) {
                return null;
            }
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(data);
                return bytesToString(md.digest());
            } catch (NoSuchAlgorithmException e) {
                Log.e(RootDetectReport.TAG, "sha256 algorithm failed");
                return null;
            }
        }

        private String bytesToString(byte[] bytes) {
            if (bytes == null) {
                return null;
            }
            char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            char[] chars = new char[(bytes.length * 2)];
            for (int j = 0; j < bytes.length; j++) {
                int byteValue = bytes[j] & 255;
                chars[j * 2] = hexChars[byteValue >>> 4];
                chars[(j * 2) + 1] = hexChars[byteValue & 15];
            }
            return new String(chars).toUpperCase(Locale.US);
        }

        private void writeRootData() {
            Object fos;
            Throwable th;
            synchronized (this.mRootDataList) {
                AutoCloseable fos2 = null;
                BufferedOutputStream bos = null;
                try {
                    BufferedOutputStream bos2;
                    FileOutputStream fos3 = new FileOutputStream(getRootDataFile());
                    try {
                        bos2 = new BufferedOutputStream(fos3);
                    } catch (IOException e) {
                        fos2 = fos3;
                        try {
                            Log.e(RootDetectReport.TAG, "Failed to write root result data");
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(fos2);
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(fos2);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fos2 = fos3;
                        IoUtils.closeQuietly(bos);
                        IoUtils.closeQuietly(fos2);
                        throw th;
                    }
                    try {
                        StringBuilder sb = new StringBuilder();
                        for (String data : this.mRootDataList) {
                            sb.setLength(0);
                            sb.append(data);
                            sb.append(10);
                            bos2.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                        }
                        bos2.flush();
                        IoUtils.closeQuietly(bos2);
                        IoUtils.closeQuietly(fos3);
                        bos = bos2;
                    } catch (IOException e2) {
                        bos = bos2;
                        fos2 = fos3;
                        Log.e(RootDetectReport.TAG, "Failed to write root result data");
                        IoUtils.closeQuietly(bos);
                        IoUtils.closeQuietly(fos2);
                    } catch (Throwable th4) {
                        th = th4;
                        bos = bos2;
                        fos2 = fos3;
                        IoUtils.closeQuietly(bos);
                        IoUtils.closeQuietly(fos2);
                        throw th;
                    }
                } catch (IOException e3) {
                    Log.e(RootDetectReport.TAG, "Failed to write root result data");
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(fos2);
                }
            }
        }

        /* JADX WARNING: Missing block: B:9:0x0022, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void readRootData() {
            Object reader;
            Throwable th;
            synchronized (this.mRootDataList) {
                this.mRootDataList.clear();
                File file = getRootDataFile();
                if (file.exists()) {
                    AutoCloseable reader2 = null;
                    try {
                        BufferedReader reader3 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                        String hashCod;
                        do {
                            try {
                                hashCod = reader3.readLine();
                                if (!TextUtils.isEmpty(hashCod)) {
                                    this.mRootDataList.add(hashCod);
                                    continue;
                                }
                            } catch (FileNotFoundException e) {
                                reader2 = reader3;
                                Log.e(RootDetectReport.TAG, "file root result list cannot be found");
                                IoUtils.closeQuietly(reader2);
                            } catch (IOException e2) {
                                reader2 = reader3;
                                try {
                                    Log.e(RootDetectReport.TAG, "Failed to read root result list");
                                    IoUtils.closeQuietly(reader2);
                                } catch (Throwable th2) {
                                    th = th2;
                                    IoUtils.closeQuietly(reader2);
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                reader2 = reader3;
                                IoUtils.closeQuietly(reader2);
                                throw th;
                            }
                        } while (hashCod != null);
                        IoUtils.closeQuietly(reader3);
                        BufferedReader bufferedReader = reader3;
                    } catch (FileNotFoundException e3) {
                        Log.e(RootDetectReport.TAG, "file root result list cannot be found");
                        IoUtils.closeQuietly(reader2);
                    } catch (IOException e4) {
                        Log.e(RootDetectReport.TAG, "Failed to read root result list");
                        IoUtils.closeQuietly(reader2);
                    }
                } else if (RootDetectReport.HW_DEBUG) {
                    Log.e(RootDetectReport.TAG, "readRootData file NOT exist!");
                }
            }
        }

        public boolean hasSame(JSONObject data) {
            if (data == null) {
                Log.e(RootDetectReport.TAG, "hasSame The data is NULL!");
                return false;
            }
            readRootData();
            Object rootDataHash = null;
            try {
                rootDataHash = sha256(data.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(RootDetectReport.TAG, "hasSame encoding unsupported");
            }
            if (TextUtils.isEmpty(rootDataHash)) {
                Log.e(RootDetectReport.TAG, "hasSame HASHCODE is null!");
                return false;
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "hasSame rootDataHash = " + rootDataHash);
            }
            synchronized (this.mRootDataList) {
                if (!this.mRootDataList.isEmpty() && this.mRootDataList.contains(rootDataHash)) {
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "addDataList has existed");
                    }
                    return true;
                } else if (this.mRootDataList.size() < 100) {
                    this.mRootDataList.add(rootDataHash);
                } else {
                    try {
                        this.mRootDataList.remove(0);
                        this.mRootDataList.add(rootDataHash);
                    } catch (IndexOutOfBoundsException e2) {
                        Log.e(RootDetectReport.TAG, "IndexOutOfBoundsException");
                    }
                }
            }
            writeRootData();
            return false;
        }
    }

    private class RootScanner {
        private static final String DEFAULT_PATH = "/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin";
        private static final String DEFAULT_STATUS = "0";
        private static final String DIR_PROC = "/proc";
        private static final String FILE_NAME_SU = "/su";
        private static final String FILE_PROC_CMDLINE = "/proc/cmdline";
        private static final String FILE_PROC_MOUNTS = "/proc/self/mounts";
        private static final String FILE_SETIDS = "/proc/check_root";
        private static final String KEY_ROOTFS = "rootfs /";
        private static final String KEY_RW = " rw,";
        private static final String KEY_SETGID = "setgid ";
        private static final String KEY_SETRESGID = "setresgid ";
        private static final String KEY_SETRESUID = "setresuid ";
        private static final String KEY_SETUID = "setuid ";
        private static final String KEY_SYSTEM = " /system ";
        private static final String PROC_NAME_MATCHER = "^[\\d]+$";
        private static final String RECOVERY_FLAG_HISI = "recovery_flag:";
        private static final int UIDS_TO_SKIP = 3;
        private static final int XUIDS_COUNT = 4;
        private int mAdbdStatus;
        private int mCheckCode;
        private int mErrorCode;
        private int mRealRootStatus;
        private String mRootProcList;
        private int mRootStatusMaskCode;
        private int mSeHook;
        private int mSelinuxStatus;
        private int mSetIdsStatus;
        private int mSuCount;
        private int mSysMounts;
        private int mSysProps;
        private int mSystemCall;
        private int mVerifyBoot;

        /* synthetic */ RootScanner(RootDetectReport this$0, UEvent event, RootScanner -this2) {
            this(event);
        }

        private RootScanner() {
            this.mSysMounts = 0;
            this.mSuCount = 0;
            this.mAdbdStatus = 0;
            this.mVerifyBoot = 0;
            this.mSysProps = 0;
            this.mSetIdsStatus = 0;
            this.mErrorCode = 0;
            this.mCheckCode = 0;
            this.mSystemCall = 0;
            this.mSeHook = 0;
            this.mSelinuxStatus = 0;
            this.mRootStatusMaskCode = 0;
            this.mRealRootStatus = 0;
            this.mRootProcList = null;
        }

        private RootScanner(UEvent event) {
            this.mSysMounts = 0;
            this.mSuCount = 0;
            this.mAdbdStatus = 0;
            this.mVerifyBoot = 0;
            this.mSysProps = 0;
            this.mSetIdsStatus = 0;
            this.mErrorCode = 0;
            this.mCheckCode = 0;
            this.mSystemCall = 0;
            this.mSeHook = 0;
            this.mSelinuxStatus = 0;
            this.mRootStatusMaskCode = 0;
            this.mRealRootStatus = 0;
            this.mRootProcList = null;
            getRootStatus();
            parseUevent(event);
            setRootStatusProperty();
        }

        private void setRootStatusProperty() {
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "setRootStatusProperty mRootStatusMaskCode = " + this.mRealRootStatus);
            }
            if (this.mRealRootStatus == 0) {
                try {
                    SystemProperties.set("persist.sys.root.status", Integer.toString(this.mRealRootStatus));
                    return;
                } catch (Exception e) {
                    if (RootDetectReport.HW_DEBUG) {
                        Log.e(RootDetectReport.TAG, "setRootStatusProperty failed, mRealRootStatus = " + this.mRealRootStatus);
                        return;
                    }
                    return;
                }
            }
            WakeLock mWakeLock = ((PowerManager) RootDetectReport.this.mContext.getSystemService("power")).newWakeLock(1, "setRootStatusProperty");
            mWakeLock.acquire();
            try {
                SystemProperties.set("persist.sys.root.status", Integer.toString(this.mRealRootStatus));
            } catch (Exception e2) {
                if (RootDetectReport.HW_DEBUG) {
                    Log.e(RootDetectReport.TAG, "setRootStatusProperty failed, mRealRootStatus = " + this.mRealRootStatus);
                }
            }
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }

        private void parseKernelRootStatus(String status) {
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "parseKernelRootStatus STATUS = " + status);
            }
            int kernelRoot = 0;
            if (status != null) {
                try {
                    kernelRoot = Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    Log.e(RootDetectReport.TAG, "some data is not of type of Integer during parsing kernel result");
                    return;
                }
            }
            int proce = kernelRoot & 16;
            this.mRootStatusMaskCode |= kernelRoot & 15;
            this.mRealRootStatus = this.mRootStatusMaskCode;
            if (proce > 0) {
                this.mRootStatusMaskCode |= 1024;
            }
        }

        private void replaceRprocs(String regex, String replacement) {
            this.mRootProcList = Pattern.compile(regex).matcher(this.mRootProcList).replaceAll(replacement);
        }

        private void trimRprocs(String procName) {
            String logSysInfo = SystemProperties.get("ro.logsystem.usertype", "0");
            if (logSysInfo.equals("3") || logSysInfo.equals("5")) {
                replaceRprocs(":?(/[a-z]+?)+?/" + procName + ":?", ":");
                replaceRprocs("::", ":");
                replaceRprocs("^:|:$", "");
            }
        }

        private void parseUevent(UEvent event) {
            if (event != null) {
                try {
                    parseKernelRootStatus(event.get(HwSecDiagnoseConstant.ROOT_STATUS));
                    this.mCheckCode = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_CHECK_CODE, "0"));
                    this.mSystemCall = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_SYS_CALL, "0"));
                    this.mSeHook = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_SE_HOOKS, "0"));
                    this.mSelinuxStatus = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_SE_STATUS, "0"));
                    this.mErrorCode = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_ERR_CODE, "0"));
                    this.mRootProcList = event.get(HwSecDiagnoseConstant.ROOT_ROOT_PRO);
                    trimRprocs("tcpdump");
                } catch (NumberFormatException e) {
                    Log.e(RootDetectReport.TAG, "some data is not of the type Integer during parsing UEvent");
                } catch (Exception e2) {
                    Log.e(RootDetectReport.TAG, "something wrong during the parsing UEvent");
                }
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "parseUevent mCheckCode = " + this.mCheckCode + " mSystemCall = " + this.mSystemCall + " mSeHook = " + this.mSeHook + " mSelinuxStatus = " + this.mSelinuxStatus);
                }
            }
        }

        private String getRootProcList(int jsonLen) {
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "getRootProcList = " + this.mRootProcList + " jsonLen = " + jsonLen);
            }
            if (!TextUtils.isEmpty(this.mRootProcList)) {
                this.mRootProcList = this.mRootProcList.replaceAll("/", "-");
                if (this.mRootProcList.length() + jsonLen >= 1000) {
                    this.mRootProcList = this.mRootProcList.substring(0, 1000 - jsonLen);
                }
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "getRootProcList END = " + this.mRootProcList);
            }
            return this.mRootProcList;
        }

        public boolean isRooted() {
            return this.mRootStatusMaskCode > 0 && this.mRootStatusMaskCode != 1024;
        }

        public JSONObject parcelRootData() {
            JSONObject json = new JSONObject();
            try {
                json.put(HwSecDiagnoseConstant.ROOT_STATUS, this.mRealRootStatus);
                json.put(HwSecDiagnoseConstant.ROOT_ERR_CODE, this.mErrorCode);
                json.put(HwSecDiagnoseConstant.ROOT_CHECK_CODE, this.mCheckCode);
                json.put(HwSecDiagnoseConstant.ROOT_SYS_CALL, this.mSystemCall);
                json.put(HwSecDiagnoseConstant.ROOT_SE_HOOKS, this.mSeHook);
                json.put(HwSecDiagnoseConstant.ROOT_SE_STATUS, this.mSelinuxStatus);
                json.put(HwSecDiagnoseConstant.ROOT_CHECK_SU, this.mSuCount);
                json.put(HwSecDiagnoseConstant.ROOT_SYS_RW, this.mSysMounts);
                json.put(HwSecDiagnoseConstant.ROOT_CHECK_ADBD, this.mAdbdStatus);
                json.put(HwSecDiagnoseConstant.ROOT_VB_STATUS, this.mVerifyBoot);
                json.put(HwSecDiagnoseConstant.ROOT_CHECK_PROP, this.mSysProps);
                json.put(HwSecDiagnoseConstant.ROOT_CHECK_SETIDS, this.mSetIdsStatus);
                return json;
            } catch (JSONException e) {
                Log.e(RootDetectReport.TAG, "parcel root data, something wrong with the json object");
                return null;
            }
        }

        private int getPidOwner(int pid) {
            Object reader;
            Throwable th;
            int pidOwner = 0;
            File status = new File("/proc/" + pid + "/status");
            AutoCloseable reader2 = null;
            int[] xUids = new int[4];
            Pattern pattern = Pattern.compile("Uid:\t(\\d+)\t(\\d+)\t(\\d+)\t(\\d+)");
            try {
                BufferedReader reader3 = new BufferedReader(new InputStreamReader(new FileInputStream(status), "UTF-8"));
                while (true) {
                    try {
                        String line = reader3.readLine();
                        if (line == null) {
                            break;
                        } else if (line.contains("Uid:")) {
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                for (int i = 0; i < 4; i++) {
                                    xUids[i] = Integer.parseInt(matcher.group(i + 1));
                                    pidOwner += xUids[i] != 0 ? 0 : 1;
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        reader2 = reader3;
                        Log.e(RootDetectReport.TAG, "get pid owner, the given pid is not correct, for it's not of the type integer");
                        IoUtils.closeQuietly(reader2);
                        return pidOwner;
                    } catch (FileNotFoundException e2) {
                        reader2 = reader3;
                        Log.e(RootDetectReport.TAG, "get pid owner, failed to find the file, the pid is: " + pid);
                        IoUtils.closeQuietly(reader2);
                        return pidOwner;
                    } catch (IOException e3) {
                        reader2 = reader3;
                        try {
                            Log.e(RootDetectReport.TAG, "get pid owner, failed to read the file, the pid is: " + pid);
                            IoUtils.closeQuietly(reader2);
                            return pidOwner;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(reader2);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader2 = reader3;
                        IoUtils.closeQuietly(reader2);
                        throw th;
                    }
                }
                IoUtils.closeQuietly(reader3);
                BufferedReader bufferedReader = reader3;
            } catch (NumberFormatException e4) {
                Log.e(RootDetectReport.TAG, "get pid owner, the given pid is not correct, for it's not of the type integer");
                IoUtils.closeQuietly(reader2);
                return pidOwner;
            } catch (FileNotFoundException e5) {
                Log.e(RootDetectReport.TAG, "get pid owner, failed to find the file, the pid is: " + pid);
                IoUtils.closeQuietly(reader2);
                return pidOwner;
            } catch (IOException e6) {
                Log.e(RootDetectReport.TAG, "get pid owner, failed to read the file, the pid is: " + pid);
                IoUtils.closeQuietly(reader2);
                return pidOwner;
            }
            return pidOwner;
        }

        private void checkSu() {
            String env = System.getenv("PATH");
            if (env == null) {
                Log.e(RootDetectReport.TAG, "checkSu cannot obtain $PATH, using default");
                env = DEFAULT_PATH;
            }
            for (String path : env.split(":")) {
                File suFile = new File(path + FILE_NAME_SU);
                if (suFile.exists()) {
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "checkSu PATH = " + suFile.getAbsolutePath());
                    }
                    this.mSuCount++;
                }
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "checkSu mSuCount = " + this.mSuCount);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int checkSysRw() {
            Throwable th;
            Object reader;
            AutoCloseable reader2 = null;
            int errCode = 0;
            try {
                BufferedReader reader3 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FILE_PROC_MOUNTS)), "UTF-8"));
                while (true) {
                    try {
                        String line = reader3.readLine();
                        if (line == null) {
                            break;
                        } else if ((line.contains(KEY_ROOTFS) || line.contains(KEY_SYSTEM)) && line.contains(KEY_RW)) {
                            this.mSysMounts++;
                        }
                    } catch (FileNotFoundException e) {
                        reader2 = reader3;
                        try {
                            Log.e(RootDetectReport.TAG, "checkSysRw, failed to find the file");
                            errCode = -3;
                            IoUtils.closeQuietly(reader2);
                            if (RootDetectReport.HW_DEBUG) {
                            }
                            return errCode;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(reader2);
                            throw th;
                        }
                    } catch (IOException e2) {
                        reader2 = reader3;
                        Log.e(RootDetectReport.TAG, "checkSysRw, failed to read the file");
                        IoUtils.closeQuietly(reader2);
                        if (RootDetectReport.HW_DEBUG) {
                        }
                        return errCode;
                    } catch (Throwable th3) {
                        th = th3;
                        reader2 = reader3;
                        IoUtils.closeQuietly(reader2);
                        throw th;
                    }
                }
                IoUtils.closeQuietly(reader3);
            } catch (FileNotFoundException e3) {
                Log.e(RootDetectReport.TAG, "checkSysRw, failed to find the file");
                errCode = -3;
                IoUtils.closeQuietly(reader2);
                if (RootDetectReport.HW_DEBUG) {
                }
                return errCode;
            } catch (IOException e4) {
                Log.e(RootDetectReport.TAG, "checkSysRw, failed to read the file");
                IoUtils.closeQuietly(reader2);
                if (RootDetectReport.HW_DEBUG) {
                }
                return errCode;
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "checkSysRw mSysMounts = " + this.mSysMounts);
            }
            return errCode;
        }

        private int checkAdbd() {
            int i;
            Throwable th;
            BufferedReader reader = null;
            int errCode = 0;
            File procDir = new File(DIR_PROC);
            if (procDir.isDirectory()) {
                File[] fileList = procDir.listFiles();
                if (fileList == null || fileList.length == 0) {
                    Log.e(RootDetectReport.TAG, "/proccheckAdbd have no file");
                    return 0;
                }
                i = 0;
                int length = fileList.length;
                while (true) {
                    BufferedReader reader2 = reader;
                    if (i < length) {
                        File file = fileList[i];
                        String fileName = file.getName();
                        if (!file.isDirectory()) {
                            reader = reader2;
                        } else if (!fileName.matches(PROC_NAME_MATCHER)) {
                            reader = reader2;
                        } else if (Integer.parseInt(fileName) < 3) {
                            reader = reader2;
                        } else {
                            try {
                                File cmdLine = new File(file.getCanonicalPath() + "/cmdline");
                                if (cmdLine.exists()) {
                                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(cmdLine), "UTF-8"));
                                    try {
                                        String line = reader.readLine();
                                        if (line == null) {
                                            IoUtils.closeQuietly(reader);
                                        } else {
                                            if (line.contains("/sbin/adbd")) {
                                                this.mAdbdStatus += getPidOwner(Integer.parseInt(fileName));
                                                if (RootDetectReport.HW_DEBUG) {
                                                    Log.d(RootDetectReport.TAG, fileName + " checkAdbd contains adbd, mAdbdStatus = " + this.mAdbdStatus);
                                                }
                                            }
                                            IoUtils.closeQuietly(reader);
                                        }
                                    } catch (NumberFormatException e) {
                                    } catch (FileNotFoundException e2) {
                                        Log.e(RootDetectReport.TAG, "checkAdbd, file can't be found");
                                        errCode = -3;
                                        IoUtils.closeQuietly(reader);
                                        i++;
                                    } catch (IOException e3) {
                                        try {
                                            Log.e(RootDetectReport.TAG, "checkAdbd, failed to read the file");
                                            IoUtils.closeQuietly(reader);
                                            i++;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                } else {
                                    IoUtils.closeQuietly(reader2);
                                    reader = reader2;
                                }
                            } catch (NumberFormatException e4) {
                                reader = reader2;
                            } catch (FileNotFoundException e5) {
                                reader = reader2;
                                Log.e(RootDetectReport.TAG, "checkAdbd, file can't be found");
                                errCode = -3;
                                IoUtils.closeQuietly(reader);
                                i++;
                            } catch (IOException e6) {
                                reader = reader2;
                                Log.e(RootDetectReport.TAG, "checkAdbd, failed to read the file");
                                IoUtils.closeQuietly(reader);
                                i++;
                            } catch (Throwable th3) {
                                th = th3;
                                reader = reader2;
                            }
                        }
                        i++;
                    } else {
                        if (RootDetectReport.HW_DEBUG) {
                            Log.d(RootDetectReport.TAG, "checkAdbd mAdbdStatus = " + this.mAdbdStatus);
                        }
                        return errCode;
                    }
                }
            }
            Log.e(RootDetectReport.TAG, "/proc is not a directory");
            return 0;
            Log.e(RootDetectReport.TAG, "checkAdbd, the value is not of type Integer");
            IoUtils.closeQuietly(reader);
            i++;
            IoUtils.closeQuietly(reader);
            throw th;
        }

        private int checkVerifyBoot() {
            boolean isEnforcingMode = SystemProperties.get("ro.boot.veritymode", "enforcing").equals("enforcing");
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "checkVerifyBoot isEnforceMode = " + isEnforcingMode);
            }
            this.mVerifyBoot = isEnforcingMode ? 0 : 1;
            return this.mVerifyBoot;
        }

        private int checkSystemProperties() {
            boolean isRoKernelQemu = SystemProperties.get("ro.kernel.qemu", "0").equals("1");
            if (isRoKernelQemu) {
                this.mSysProps |= 1;
            }
            boolean isAdbServiceRoot = SystemProperties.get("service.adb.root", "0").equals("1");
            if (isAdbServiceRoot) {
                this.mSysProps |= 2;
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "checkSystemProperties isRoKernelQemu = " + isRoKernelQemu + " isAdbServiceRoot = " + isAdbServiceRoot);
            }
            return this.mSysProps;
        }

        private String getEndString(String str, String subStr) {
            if (TextUtils.isEmpty(str) || TextUtils.isEmpty(subStr)) {
                return null;
            }
            String trimStr = str.trim();
            if (trimStr.startsWith(subStr)) {
                return str.substring(subStr.length(), trimStr.length());
            }
            return null;
        }

        /* JADX WARNING: Removed duplicated region for block: B:31:0x0092  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0092  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0092  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0092  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int checkSetids() {
            Throwable th;
            int errCode = 0;
            File file = new File(FILE_SETIDS);
            AutoCloseable autoCloseable = null;
            try {
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                    while (true) {
                        try {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            if (RootDetectReport.HW_DEBUG) {
                                Log.d(RootDetectReport.TAG, "checkSetids = " + line);
                            }
                            if (line.contains(KEY_SETUID)) {
                                if (!"0".equals(getEndString(line, KEY_SETUID))) {
                                    this.mSetIdsStatus |= 1;
                                }
                            } else if (line.contains(KEY_SETGID)) {
                                if (!"0".equals(getEndString(line, KEY_SETGID))) {
                                    this.mSetIdsStatus |= 2;
                                }
                            } else if (line.contains(KEY_SETRESUID)) {
                                if (!"0".equals(getEndString(line, KEY_SETRESUID))) {
                                    this.mSetIdsStatus |= 4;
                                }
                            } else if (line.contains(KEY_SETRESGID) && !"0".equals(getEndString(line, KEY_SETRESGID))) {
                                this.mSetIdsStatus |= 8;
                            }
                        } catch (FileNotFoundException e) {
                            autoCloseable = reader;
                            try {
                                Log.e(RootDetectReport.TAG, "checkSetids, failed to find the result file");
                                errCode = -3;
                                IoUtils.closeQuietly(autoCloseable);
                                if (RootDetectReport.HW_DEBUG) {
                                }
                                return errCode;
                            } catch (Throwable th2) {
                                th = th2;
                                IoUtils.closeQuietly(autoCloseable);
                                throw th;
                            }
                        } catch (IOException e2) {
                            autoCloseable = reader;
                            Log.e(RootDetectReport.TAG, "checkSetids, failed to read check root result");
                            IoUtils.closeQuietly(autoCloseable);
                            if (RootDetectReport.HW_DEBUG) {
                            }
                            return errCode;
                        } catch (Throwable th3) {
                            th = th3;
                            autoCloseable = reader;
                            IoUtils.closeQuietly(autoCloseable);
                            throw th;
                        }
                    }
                    IoUtils.closeQuietly(reader);
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "checkSetids mSetIdsStatus = " + this.mSetIdsStatus);
                    }
                    return errCode;
                }
                if (RootDetectReport.HW_DEBUG) {
                    Log.e(RootDetectReport.TAG, "checkSetids file NOT exist!");
                }
                IoUtils.closeQuietly(null);
                return -3;
            } catch (FileNotFoundException e3) {
                Log.e(RootDetectReport.TAG, "checkSetids, failed to find the result file");
                errCode = -3;
                IoUtils.closeQuietly(autoCloseable);
                if (RootDetectReport.HW_DEBUG) {
                }
                return errCode;
            } catch (IOException e4) {
                Log.e(RootDetectReport.TAG, "checkSetids, failed to read check root result");
                IoUtils.closeQuietly(autoCloseable);
                if (RootDetectReport.HW_DEBUG) {
                }
                return errCode;
            }
        }

        private int getRootStatus() {
            if (RootDetectReport.this.needRootScan()) {
                checkSu();
                if (checkSysRw() != 0) {
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "check system mounts failed");
                    }
                    this.mErrorCode |= 32;
                }
                if (checkAdbd() != 0) {
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "check adbd failed");
                    }
                    this.mErrorCode |= 64;
                }
                if (checkSetids() != 0) {
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "check setids failed");
                    }
                    this.mErrorCode |= 512;
                }
                if (this.mSuCount != 0) {
                    this.mRootStatusMaskCode |= 16;
                }
                if (this.mSysMounts != 0) {
                    this.mRootStatusMaskCode |= 32;
                }
                if (this.mAdbdStatus != 0) {
                    this.mRootStatusMaskCode |= 64;
                }
                if (checkVerifyBoot() != 0) {
                    this.mRootStatusMaskCode |= 128;
                }
                if (checkSystemProperties() != 0) {
                    this.mRootStatusMaskCode |= 256;
                }
                if (this.mSetIdsStatus != 0) {
                    this.mRootStatusMaskCode |= 512;
                }
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "rootStatus is " + this.mRootStatusMaskCode);
                }
                return this.mRootStatusMaskCode;
            }
            Log.e(RootDetectReport.TAG, "getRootStatus needRootScan not run in normal mode");
            return Integer.parseInt(RootDetectReport.isEngneringMode);
        }
    }

    private class UEventHandler extends Handler {
        public UEventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    RootDetectReport.this.processUEvent((UEvent) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    private void processUEvent(UEvent event) {
        long start = System.currentTimeMillis();
        RootScanner rootScanner = new RootScanner(this, event, null);
        JSONObject json = rootScanner.parcelRootData();
        if (HW_DEBUG) {
            Log.d(TAG, "processUEvent json = " + json);
        }
        if (rootScanner.isRooted()) {
            boolean hasSame = new RootDataBundle().hasSame(json);
            boolean needReport = hasSame ^ 1;
            if (HW_DEBUG) {
                Log.d(TAG, "processUEvent hasSame = " + hasSame + " needReport = " + needReport);
            }
            try {
                json.put(HwSecDiagnoseConstant.ROOT_ROOT_PRO, rootScanner.getRootProcList(json.toString().length()));
            } catch (JSONException e) {
                Log.e(TAG, "proclist put error");
            }
            this.mListener.onRootReport(json, needReport);
        } else if (HW_DEBUG) {
            Log.d(TAG, "device not rooted, pass record and reporter.");
        }
        long end = System.currentTimeMillis();
        if (HW_DEBUG) {
            Log.d(TAG, "framework root scan run TIME = " + (end - start) + "ms");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "whole root scan run TIME = " + (end - mStartTime) + "ms");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0078  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int triggerRootScan() {
        Object reader;
        Throwable th;
        int ret = 0;
        mStartTime = System.currentTimeMillis();
        File rootScan = new File(FILE_PROC_ROOT_SCAN);
        AutoCloseable reader2 = null;
        if (needRootScan()) {
            try {
                BufferedReader reader3 = new BufferedReader(new InputStreamReader(new FileInputStream(rootScan), "UTF-8"));
                try {
                    String kernelRootStatus = reader3.readLine();
                    if (kernelRootStatus != null && kernelRootStatus.matches(KERNEL_ROOT_STATUS_PATTER_MATCHER)) {
                        ret = Integer.parseInt(kernelRootStatus);
                        Log.d(TAG, "kernel root status is " + ret);
                    }
                    IoUtils.closeQuietly(reader3);
                    BufferedReader bufferedReader = reader3;
                } catch (FileNotFoundException e) {
                    reader2 = reader3;
                    Log.e(TAG, "triggerRootScan, trigger file cannot be found");
                    ret = -3;
                    IoUtils.closeQuietly(reader2);
                    if (HW_DEBUG) {
                    }
                    return ret;
                } catch (IOException e2) {
                    reader2 = reader3;
                    Log.e(TAG, "failed to read the trigger proc file");
                    ret = -4;
                    IoUtils.closeQuietly(reader2);
                    if (HW_DEBUG) {
                    }
                    return ret;
                } catch (NumberFormatException e3) {
                    reader2 = reader3;
                    try {
                        Log.e(TAG, "some data is not of the type Integer during parsing trigger file");
                        ret = -1;
                        IoUtils.closeQuietly(reader2);
                        if (HW_DEBUG) {
                        }
                        return ret;
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(reader2);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader2 = reader3;
                    IoUtils.closeQuietly(reader2);
                    throw th;
                }
            } catch (FileNotFoundException e4) {
                Log.e(TAG, "triggerRootScan, trigger file cannot be found");
                ret = -3;
                IoUtils.closeQuietly(reader2);
                if (HW_DEBUG) {
                }
                return ret;
            } catch (IOException e5) {
                Log.e(TAG, "failed to read the trigger proc file");
                ret = -4;
                IoUtils.closeQuietly(reader2);
                if (HW_DEBUG) {
                }
                return ret;
            } catch (NumberFormatException e6) {
                Log.e(TAG, "some data is not of the type Integer during parsing trigger file");
                ret = -1;
                IoUtils.closeQuietly(reader2);
                if (HW_DEBUG) {
                }
                return ret;
            }
            if (HW_DEBUG) {
                Log.d(TAG, "triggerRootScan return value = " + ret);
            }
            return ret;
        }
        try {
            SystemProperties.set("persist.sys.root.status", isEngneringMode);
        } catch (Exception e7) {
            if (HW_DEBUG) {
                Log.e(TAG, "triggerRootScan failed, isEngneringMode = 1048576");
            }
        }
        return -2;
    }

    void setListener(Listener listener) {
        this.mListener = listener;
    }

    private boolean needRootScan() {
        boolean isEngMode = SystemProperties.get("ro.secure", "1").equals("0");
        boolean isRunModeNormal = SystemProperties.get("ro.runmode", "normal").equals("normal");
        if (HW_DEBUG) {
            Log.e(TAG, "needRootScan isEngMode = " + isEngMode + " isRunModeNormal = " + isRunModeNormal);
        }
        return !isEngMode ? isRunModeNormal : false;
    }

    private RootDetectReport(Context context) {
        this.mContext = context;
        this.mUEventObserver.startObserving(ROOT_STATE_MATCH);
        this.mHandlerThread = new HandlerThread("uevent handler");
        this.mHandlerThread.start();
        this.mHandler = new UEventHandler(this.mHandlerThread.getLooper());
    }

    public static void init(Context context) {
        synchronized (RootDetectReport.class) {
            if (mInstance == null) {
                mInstance = new RootDetectReport(context);
            }
        }
    }

    public static RootDetectReport getInstance() {
        RootDetectReport rootDetectReport;
        synchronized (RootDetectReport.class) {
            rootDetectReport = mInstance;
        }
        return rootDetectReport;
    }
}

package com.android.server.security.securitydiagnose;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.PPPOEStateMachine;
import com.android.server.display.Utils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
import java.util.TimerTask;
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
    private static final long DELAY_TRIGGER_ROOT_SCAN = 43200000;
    private static final int EVT_ROOT_STATUS_REPORT = 1000;
    private static final int FILE_NOT_FOUND_ERR = -3;
    private static final String FILE_PROC_ROOT_SCAN = "/proc/root_scan";
    private static final int GENERIC_ERR = -1;
    private static final boolean HW_DEBUG = false;
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
    private static RootDetectReport mInstance;
    private Context mContext;
    private UEventHandler mHandler;
    private HandlerThread mHandlerThread;
    private Listener mListener;
    private Timer mTriggerTimer;
    private final UEventObserver mUEventObserver;

    public interface Listener {
        void onRootReport(JSONObject jSONObject);
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
            char[] chars = new char[(bytes.length * RootDetectReport.BIT_SETGID)];
            for (int j = 0; j < bytes.length; j += RootDetectReport.BIT_SETUID) {
                int byteValue = bytes[j] & Utils.MAXINUM_TEMPERATURE;
                chars[j * RootDetectReport.BIT_SETGID] = hexChars[byteValue >>> RootDetectReport.BIT_SETRESUID];
                chars[(j * RootDetectReport.BIT_SETGID) + RootDetectReport.BIT_SETUID] = hexChars[byteValue & RootDetectReport.BIT_RPROC_CLEAR];
            }
            return new String(chars).toUpperCase(Locale.US);
        }

        private void writeRootData() {
            Object fos;
            Throwable th;
            synchronized (this.mRootDataList) {
                AutoCloseable autoCloseable = null;
                BufferedOutputStream bos = null;
                try {
                    BufferedOutputStream bos2;
                    FileOutputStream fos2 = new FileOutputStream(getRootDataFile());
                    try {
                        bos2 = new BufferedOutputStream(fos2);
                    } catch (IOException e) {
                        fos = fos2;
                        try {
                            Log.e(RootDetectReport.TAG, "Failed to write root result data");
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(autoCloseable);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fos = fos2;
                        IoUtils.closeQuietly(bos);
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                    try {
                        StringBuilder sb = new StringBuilder();
                        for (String data : this.mRootDataList) {
                            sb.setLength(0);
                            sb.append(data);
                            sb.append('\n');
                            bos2.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                        }
                        bos2.flush();
                        IoUtils.closeQuietly(bos2);
                        IoUtils.closeQuietly(fos2);
                        bos = bos2;
                    } catch (IOException e2) {
                        bos = bos2;
                        autoCloseable = fos2;
                        Log.e(RootDetectReport.TAG, "Failed to write root result data");
                        IoUtils.closeQuietly(bos);
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Throwable th4) {
                        th = th4;
                        bos = bos2;
                        fos = fos2;
                        IoUtils.closeQuietly(bos);
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (IOException e3) {
                    Log.e(RootDetectReport.TAG, "Failed to write root result data");
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(autoCloseable);
                }
            }
        }

        private void readRootData() {
            Object obj;
            synchronized (this.mRootDataList) {
                this.mRootDataList.clear();
                File file = getRootDataFile();
                if (file.exists()) {
                    AutoCloseable autoCloseable = null;
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                        String hashCod;
                        do {
                            try {
                                hashCod = reader.readLine();
                                if (!TextUtils.isEmpty(hashCod)) {
                                    this.mRootDataList.add(hashCod);
                                    continue;
                                }
                            } catch (FileNotFoundException e) {
                                obj = reader;
                            } catch (IOException e2) {
                                obj = reader;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                obj = reader;
                            }
                        } while (hashCod != null);
                        IoUtils.closeQuietly(reader);
                        BufferedReader bufferedReader = reader;
                    } catch (FileNotFoundException e3) {
                        Log.e(RootDetectReport.TAG, "file root result list cannot be found");
                        IoUtils.closeQuietly(autoCloseable);
                        return;
                    } catch (IOException e4) {
                        try {
                            Log.e(RootDetectReport.TAG, "Failed to read root result list");
                            IoUtils.closeQuietly(autoCloseable);
                            return;
                        } catch (Throwable th3) {
                            th2 = th3;
                            IoUtils.closeQuietly(autoCloseable);
                            throw th2;
                        }
                    }
                    return;
                }
                if (RootDetectReport.HW_DEBUG) {
                    Log.e(RootDetectReport.TAG, "readRootData file NOT exist!");
                }
            }
        }

        public boolean hasSame(JSONObject data) {
            if (data == null) {
                Log.e(RootDetectReport.TAG, "hasSame The data is NULL!");
                return RootDetectReport.RS_DEBUG;
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
                return RootDetectReport.RS_DEBUG;
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "hasSame rootDataHash = " + rootDataHash);
            }
            synchronized (this.mRootDataList) {
                if (this.mRootDataList.isEmpty() || !this.mRootDataList.contains(rootDataHash)) {
                    if (this.mRootDataList.size() < MAX_DATA_RECORDS) {
                        this.mRootDataList.add(rootDataHash);
                    } else {
                        try {
                            this.mRootDataList.remove(0);
                            this.mRootDataList.add(rootDataHash);
                        } catch (IndexOutOfBoundsException e2) {
                            Log.e(RootDetectReport.TAG, "IndexOutOfBoundsException");
                        }
                    }
                    writeRootData();
                    return RootDetectReport.RS_DEBUG;
                }
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "addDataList has existed");
                }
                return true;
            }
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
            SystemProperties.set(HwSecDiagnoseConstant.PROPERTY_ROOT_STATUS, Integer.toString(this.mRealRootStatus));
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
            int proce = kernelRoot & RootDetectReport.BIT_RPROC_KERNEL;
            this.mRootStatusMaskCode |= kernelRoot & RootDetectReport.BIT_RPROC_CLEAR;
            this.mRealRootStatus = this.mRootStatusMaskCode;
            if (proce > 0) {
                this.mRootStatusMaskCode |= HwGlobalActionsData.FLAG_SILENTMODE_NORMAL;
            }
        }

        private void replaceRprocs(String regex, String replacement) {
            this.mRootProcList = Pattern.compile(regex).matcher(this.mRootProcList).replaceAll(replacement);
        }

        private void trimRprocs(String procName) {
            String logSysInfo = SystemProperties.get("ro.logsystem.usertype", DEFAULT_STATUS);
            if (logSysInfo.equals(RootDetectReport.LOG_VERSION_DOMESTIC) || logSysInfo.equals(RootDetectReport.LOG_VERSION_OVERSEA)) {
                replaceRprocs(":?(/[a-z]+?)+?/" + procName + ":?", ":");
                replaceRprocs("::", ":");
                replaceRprocs("^:|:$", AppHibernateCst.INVALID_PKG);
            }
        }

        private void parseUevent(UEvent event) {
            if (event != null) {
                try {
                    parseKernelRootStatus(event.get(HwSecDiagnoseConstant.ROOT_STATUS));
                    this.mCheckCode = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_CHECK_CODE, DEFAULT_STATUS));
                    this.mSystemCall = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_SYS_CALL, DEFAULT_STATUS));
                    this.mSeHook = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_SE_HOOKS, DEFAULT_STATUS));
                    this.mSelinuxStatus = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_SE_STATUS, DEFAULT_STATUS));
                    this.mErrorCode = Integer.parseInt(event.get(HwSecDiagnoseConstant.ROOT_ERR_CODE, DEFAULT_STATUS));
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
                if (this.mRootProcList.length() + jsonLen >= RootDetectReport.JSON_LEN_MAX) {
                    this.mRootProcList = this.mRootProcList.substring(0, 1000 - jsonLen);
                }
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "getRootProcList END = " + this.mRootProcList);
            }
            return this.mRootProcList;
        }

        public boolean isRooted() {
            return this.mRootStatusMaskCode > 0 ? true : RootDetectReport.RS_DEBUG;
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
                json.put(HwSecDiagnoseConstant.ROOT_ROOT_PRO, getRootProcList(json.toString().length()));
                return json;
            } catch (JSONException e) {
                Log.e(RootDetectReport.TAG, "parcel root data, something wrong with the json object");
                return null;
            }
        }

        private int getPidOwner(int pid) {
            Object obj;
            Throwable th;
            int pidOwner = 0;
            File status = new File("/proc/" + pid + "/status");
            AutoCloseable autoCloseable = null;
            int[] xUids = new int[XUIDS_COUNT];
            Pattern pattern = Pattern.compile("Uid:\t(\\d+)\t(\\d+)\t(\\d+)\t(\\d+)");
            try {
                Matcher matcher;
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(status), "UTF-8"));
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        } else if (line.contains("Uid:")) {
                            matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        obj = reader;
                    } catch (FileNotFoundException e2) {
                        obj = reader;
                    } catch (IOException e3) {
                        obj = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        obj = reader;
                    }
                }
                for (int i = 0; i < XUIDS_COUNT; i += RootDetectReport.BIT_SETUID) {
                    xUids[i] = Integer.parseInt(matcher.group(i + RootDetectReport.BIT_SETUID));
                    pidOwner += xUids[i] != 0 ? 0 : RootDetectReport.BIT_SETUID;
                }
                IoUtils.closeQuietly(reader);
                BufferedReader bufferedReader = reader;
            } catch (NumberFormatException e4) {
                Log.e(RootDetectReport.TAG, "get pid owner, the given pid is not correct, for it's not of the type integer");
                IoUtils.closeQuietly(autoCloseable);
                return pidOwner;
            } catch (FileNotFoundException e5) {
                Log.e(RootDetectReport.TAG, "get pid owner, failed to find the file, the pid is: " + pid);
                IoUtils.closeQuietly(autoCloseable);
                return pidOwner;
            } catch (IOException e6) {
                try {
                    Log.e(RootDetectReport.TAG, "get pid owner, failed to read the file, the pid is: " + pid);
                    IoUtils.closeQuietly(autoCloseable);
                    return pidOwner;
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            }
            return pidOwner;
        }

        private void checkSu() {
            String env = System.getenv("PATH");
            if (env == null) {
                Log.e(RootDetectReport.TAG, "checkSu cannot obtain $PATH, using default");
                env = DEFAULT_PATH;
            }
            String[] paths = env.split(":");
            int length = paths.length;
            for (int i = 0; i < length; i += RootDetectReport.BIT_SETUID) {
                File suFile = new File(paths[i] + FILE_NAME_SU);
                if (suFile.exists()) {
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "checkSu PATH = " + suFile.getAbsolutePath());
                    }
                    this.mSuCount += RootDetectReport.BIT_SETUID;
                }
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "checkSu mSuCount = " + this.mSuCount);
            }
        }

        private int checkSysRw() {
            Object reader;
            Throwable th;
            AutoCloseable autoCloseable = null;
            int errCode = 0;
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FILE_PROC_MOUNTS)), "UTF-8"));
                while (true) {
                    try {
                        String line = reader2.readLine();
                        if (line == null) {
                            break;
                        } else if ((line.contains(KEY_ROOTFS) || line.contains(KEY_SYSTEM)) && line.contains(KEY_RW)) {
                            this.mSysMounts += RootDetectReport.BIT_SETUID;
                        }
                    } catch (FileNotFoundException e) {
                        autoCloseable = reader2;
                    } catch (IOException e2) {
                        reader = reader2;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                    }
                }
                IoUtils.closeQuietly(reader2);
            } catch (FileNotFoundException e3) {
                try {
                    Log.e(RootDetectReport.TAG, "checkSysRw, failed to find the file");
                    errCode = RootDetectReport.FILE_NOT_FOUND_ERR;
                    IoUtils.closeQuietly(autoCloseable);
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "checkSysRw mSysMounts = " + this.mSysMounts);
                    }
                    return errCode;
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (IOException e4) {
                Log.e(RootDetectReport.TAG, "checkSysRw, failed to read the file");
                IoUtils.closeQuietly(autoCloseable);
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "checkSysRw mSysMounts = " + this.mSysMounts);
                }
                return errCode;
            }
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "checkSysRw mSysMounts = " + this.mSysMounts);
            }
            return errCode;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int checkAdbd() {
            BufferedReader reader;
            Throwable th;
            int errCode = 0;
            File procDir = new File(DIR_PROC);
            if (procDir.isDirectory()) {
                File[] fileList = procDir.listFiles();
                if (fileList == null || fileList.length == 0) {
                    Log.e(RootDetectReport.TAG, "/proccheckAdbd have no file");
                    return 0;
                }
                int i = 0;
                int length = fileList.length;
                BufferedReader reader2 = null;
                while (i < length) {
                    File file = fileList[i];
                    String fileName = file.getName();
                    if (file.isDirectory()) {
                        if (!fileName.matches(PROC_NAME_MATCHER)) {
                            reader = reader2;
                        } else if (Integer.parseInt(fileName) < UIDS_TO_SKIP) {
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
                                                int adbdPid = Integer.parseInt(fileName);
                                                this.mAdbdStatus += getPidOwner(adbdPid);
                                                if (RootDetectReport.HW_DEBUG) {
                                                    StringBuilder stringBuilder = new StringBuilder();
                                                    Log.d(RootDetectReport.TAG, r19.append(fileName).append(" checkAdbd contains adbd, mAdbdStatus = ").append(this.mAdbdStatus).toString());
                                                }
                                            }
                                            IoUtils.closeQuietly(reader);
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.e(RootDetectReport.TAG, "checkAdbd, the value is not of type Integer");
                                        IoUtils.closeQuietly(reader);
                                        i += RootDetectReport.BIT_SETUID;
                                        reader2 = reader;
                                    } catch (FileNotFoundException e2) {
                                        Log.e(RootDetectReport.TAG, "checkAdbd, file can't be found");
                                        errCode = RootDetectReport.FILE_NOT_FOUND_ERR;
                                        IoUtils.closeQuietly(reader);
                                        i += RootDetectReport.BIT_SETUID;
                                        reader2 = reader;
                                    } catch (IOException e3) {
                                        try {
                                            Log.e(RootDetectReport.TAG, "checkAdbd, failed to read the file");
                                            IoUtils.closeQuietly(reader);
                                            i += RootDetectReport.BIT_SETUID;
                                            reader2 = reader;
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
                                Log.e(RootDetectReport.TAG, "checkAdbd, the value is not of type Integer");
                                IoUtils.closeQuietly(reader);
                                i += RootDetectReport.BIT_SETUID;
                                reader2 = reader;
                            } catch (FileNotFoundException e5) {
                                reader = reader2;
                                Log.e(RootDetectReport.TAG, "checkAdbd, file can't be found");
                                errCode = RootDetectReport.FILE_NOT_FOUND_ERR;
                                IoUtils.closeQuietly(reader);
                                i += RootDetectReport.BIT_SETUID;
                                reader2 = reader;
                            } catch (IOException e6) {
                                reader = reader2;
                                Log.e(RootDetectReport.TAG, "checkAdbd, failed to read the file");
                                IoUtils.closeQuietly(reader);
                                i += RootDetectReport.BIT_SETUID;
                                reader2 = reader;
                            } catch (Throwable th3) {
                                th = th3;
                                reader = reader2;
                            }
                        }
                    } else {
                        reader = reader2;
                    }
                    i += RootDetectReport.BIT_SETUID;
                    reader2 = reader;
                }
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "checkAdbd mAdbdStatus = " + this.mAdbdStatus);
                }
                return errCode;
            }
            Log.e(RootDetectReport.TAG, "/proc is not a directory");
            return 0;
        }

        private int checkVerifyBoot() {
            boolean isEnforcingMode = SystemProperties.get("ro.boot.veritymode", "enforcing").equals("enforcing");
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "checkVerifyBoot isEnforceMode = " + isEnforcingMode);
            }
            this.mVerifyBoot = isEnforcingMode ? 0 : RootDetectReport.BIT_SETUID;
            return this.mVerifyBoot;
        }

        private int checkSystemProperties() {
            boolean isRoKernelQemu = SystemProperties.get("ro.kernel.qemu", DEFAULT_STATUS).equals(PPPOEStateMachine.PHASE_INITIALIZE);
            if (isRoKernelQemu) {
                this.mSysProps |= RootDetectReport.BIT_SETUID;
            }
            boolean isAdbServiceRoot = SystemProperties.get("service.adb.root", DEFAULT_STATUS).equals(PPPOEStateMachine.PHASE_INITIALIZE);
            if (isAdbServiceRoot) {
                this.mSysProps |= RootDetectReport.BIT_SETGID;
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
                                if (!DEFAULT_STATUS.equals(getEndString(line, KEY_SETUID))) {
                                    this.mSetIdsStatus |= RootDetectReport.BIT_SETUID;
                                }
                            } else if (line.contains(KEY_SETGID)) {
                                if (!DEFAULT_STATUS.equals(getEndString(line, KEY_SETGID))) {
                                    this.mSetIdsStatus |= RootDetectReport.BIT_SETGID;
                                }
                            } else if (line.contains(KEY_SETRESUID)) {
                                if (!DEFAULT_STATUS.equals(getEndString(line, KEY_SETRESUID))) {
                                    this.mSetIdsStatus |= XUIDS_COUNT;
                                }
                            } else if (line.contains(KEY_SETRESGID) && !DEFAULT_STATUS.equals(getEndString(line, KEY_SETRESGID))) {
                                this.mSetIdsStatus |= RootDetectReport.BIT_SETRESGID;
                            }
                        } catch (FileNotFoundException e) {
                            autoCloseable = reader;
                        } catch (IOException e2) {
                            autoCloseable = reader;
                        } catch (Throwable th2) {
                            th = th2;
                            autoCloseable = reader;
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
                return RootDetectReport.FILE_NOT_FOUND_ERR;
            } catch (FileNotFoundException e3) {
                try {
                    Log.e(RootDetectReport.TAG, "checkSetids, failed to find the result file");
                    errCode = RootDetectReport.FILE_NOT_FOUND_ERR;
                    IoUtils.closeQuietly(autoCloseable);
                    if (RootDetectReport.HW_DEBUG) {
                        Log.d(RootDetectReport.TAG, "checkSetids mSetIdsStatus = " + this.mSetIdsStatus);
                    }
                    return errCode;
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (IOException e4) {
                Log.e(RootDetectReport.TAG, "checkSetids, failed to read check root result");
                IoUtils.closeQuietly(autoCloseable);
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "checkSetids mSetIdsStatus = " + this.mSetIdsStatus);
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
                    this.mErrorCode |= HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE;
                }
                if (this.mSuCount != 0) {
                    this.mRootStatusMaskCode |= RootDetectReport.BIT_RPROC_KERNEL;
                }
                if (this.mSysMounts != 0) {
                    this.mRootStatusMaskCode |= 32;
                }
                if (this.mAdbdStatus != 0) {
                    this.mRootStatusMaskCode |= 64;
                }
                if (checkVerifyBoot() != 0) {
                    this.mRootStatusMaskCode |= HwSecDiagnoseConstant.BIT_VERIFYBOOT;
                }
                if (checkSystemProperties() != 0) {
                    this.mRootStatusMaskCode |= HwGlobalActionsData.FLAG_SILENTMODE_SILENT;
                }
                if (this.mSetIdsStatus != 0) {
                    this.mRootStatusMaskCode |= HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE;
                }
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "rootStatus is " + this.mRootStatusMaskCode);
                }
                return this.mRootStatusMaskCode;
            }
            Log.e(RootDetectReport.TAG, "getRootStatus needRootScan not run in normal mode");
            return 0;
        }
    }

    private class TriggerTimerTask extends TimerTask {
        private TriggerTimerTask() {
        }

        public void run() {
            if (RootDetectReport.HW_DEBUG) {
                Log.d(RootDetectReport.TAG, "run timer expire ");
            }
            RootDetectReport.this.triggerRootScan();
            RootDetectReport.this.startTimer();
        }
    }

    private class UEventHandler extends Handler {
        public UEventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RootDetectReport.JSON_LEN_MAX /*1000*/:
                    RootDetectReport.this.processUEvent((UEvent) msg.obj);
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.securitydiagnose.RootDetectReport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.securitydiagnose.RootDetectReport.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.securitydiagnose.RootDetectReport.<clinit>():void");
    }

    private void startTimer() {
        if (this.mTriggerTimer != null) {
            this.mTriggerTimer.cancel();
            this.mTriggerTimer = null;
        }
        this.mTriggerTimer = new Timer();
        this.mTriggerTimer.schedule(new TriggerTimerTask(), DELAY_TRIGGER_ROOT_SCAN);
    }

    private void processUEvent(UEvent event) {
        boolean z = true;
        boolean needReport = RS_DEBUG;
        long start = System.currentTimeMillis();
        RootScanner rootScanner = new RootScanner(event, null);
        JSONObject json = rootScanner.parcelRootData();
        if (HW_DEBUG) {
            Log.d(TAG, "processUEvent json = " + json);
        }
        if (rootScanner.isRooted()) {
            z = new RootDataBundle().hasSame(json);
            needReport = z ? RS_DEBUG : true;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "processUEvent hasSame = " + z + " needReport = " + needReport);
        }
        if (needReport) {
            this.mListener.onRootReport(json);
        }
        long end = System.currentTimeMillis();
        if (HW_DEBUG) {
            Log.d(TAG, "processUEvent run TIME = " + (end - start));
        }
    }

    public int triggerRootScan() {
        Object obj;
        Throwable th;
        int ret = 0;
        File rootScan = new File(FILE_PROC_ROOT_SCAN);
        AutoCloseable autoCloseable = null;
        if (!needRootScan()) {
            return ROOT_SCAN_ENV_NOT_READY;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rootScan), "UTF-8"));
            try {
                String kernelRootStatus = reader.readLine();
                if (kernelRootStatus != null && kernelRootStatus.matches(KERNEL_ROOT_STATUS_PATTER_MATCHER)) {
                    ret = Integer.parseInt(kernelRootStatus);
                    Log.d(TAG, "kernel root status is " + ret);
                }
                IoUtils.closeQuietly(reader);
                BufferedReader bufferedReader = reader;
            } catch (FileNotFoundException e) {
                obj = reader;
                Log.e(TAG, "triggerRootScan, trigger file cannot be found");
                ret = FILE_NOT_FOUND_ERR;
                IoUtils.closeQuietly(autoCloseable);
                if (HW_DEBUG) {
                    Log.d(TAG, "triggerRootScan return value = " + ret);
                }
                return ret;
            } catch (IOException e2) {
                obj = reader;
                Log.e(TAG, "failed to read the trigger proc file");
                ret = IO_EXCEPTION;
                IoUtils.closeQuietly(autoCloseable);
                if (HW_DEBUG) {
                    Log.d(TAG, "triggerRootScan return value = " + ret);
                }
                return ret;
            } catch (NumberFormatException e3) {
                obj = reader;
                try {
                    Log.e(TAG, "some data is not of the type Integer during parsing trigger file");
                    ret = GENERIC_ERR;
                    IoUtils.closeQuietly(autoCloseable);
                    if (HW_DEBUG) {
                        Log.d(TAG, "triggerRootScan return value = " + ret);
                    }
                    return ret;
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                obj = reader;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "triggerRootScan, trigger file cannot be found");
            ret = FILE_NOT_FOUND_ERR;
            IoUtils.closeQuietly(autoCloseable);
            if (HW_DEBUG) {
                Log.d(TAG, "triggerRootScan return value = " + ret);
            }
            return ret;
        } catch (IOException e5) {
            Log.e(TAG, "failed to read the trigger proc file");
            ret = IO_EXCEPTION;
            IoUtils.closeQuietly(autoCloseable);
            if (HW_DEBUG) {
                Log.d(TAG, "triggerRootScan return value = " + ret);
            }
            return ret;
        } catch (NumberFormatException e6) {
            Log.e(TAG, "some data is not of the type Integer during parsing trigger file");
            ret = GENERIC_ERR;
            IoUtils.closeQuietly(autoCloseable);
            if (HW_DEBUG) {
                Log.d(TAG, "triggerRootScan return value = " + ret);
            }
            return ret;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "triggerRootScan return value = " + ret);
        }
        return ret;
    }

    void setListener(Listener listener) {
        this.mListener = listener;
    }

    private boolean needRootScan() {
        boolean isEngMode = SystemProperties.get("ro.secure", PPPOEStateMachine.PHASE_INITIALIZE).equals(PPPOEStateMachine.PHASE_DEAD);
        boolean isRunModeNormal = SystemProperties.get("ro.runmode", "normal").equals("normal");
        if (HW_DEBUG) {
            Log.e(TAG, "needRootScan isEngMode = " + isEngMode + " isRunModeNormal = " + isRunModeNormal);
        }
        return !isEngMode ? isRunModeNormal : RS_DEBUG;
    }

    private RootDetectReport(Context context) {
        this.mTriggerTimer = null;
        this.mUEventObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                if (RootDetectReport.HW_DEBUG) {
                    Log.d(RootDetectReport.TAG, "onUEvent event: " + event);
                }
                if (RootDetectReport.this.needRootScan()) {
                    RootDetectReport.this.mHandler.sendMessage(RootDetectReport.this.mHandler.obtainMessage(RootDetectReport.JSON_LEN_MAX, event));
                }
            }
        };
        this.mContext = context;
        this.mUEventObserver.startObserving(ROOT_STATE_MATCH);
        startTimer();
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

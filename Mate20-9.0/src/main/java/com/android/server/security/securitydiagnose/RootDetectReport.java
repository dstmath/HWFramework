package com.android.server.security.securitydiagnose;

import android.content.Context;
import android.os.Environment;
import android.os.SystemProperties;
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
import java.util.Iterator;
import java.util.Locale;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class RootDetectReport {
    private static final int BIT_RPROC_CLEAR = -1025;
    private static final int FILE_NOT_FOUND_ERR = -3;
    private static final String FILE_PROC_ROOT_SCAN = (File.separator + "proc" + File.separator + "root_scan");
    private static final int GENERIC_ERR = -1;
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (Log.HWINFO || RS_DEBUG || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int IO_EXCEPTION = -4;
    private static final boolean RS_DEBUG = SystemProperties.get("ro.secure", "1").equals("0");
    private static final String TAG = "RootDetectReport";
    private static long mEndTime = 0;
    private static RootDetectReport mInstance;
    private static long mStartTime = 0;
    private Context mContext;
    private Listener mListener;
    private boolean rootScanHasTrigger = false;

    public interface Listener {
        void onRootReport(JSONObject jSONObject, boolean z);
    }

    private static class RootDataBundle {
        private static final int MAX_DATA_RECORDS = 100;
        private static final int MAX_STR_LEN = 6800;
        private static final String RSCAN_LIST_FILE = "root_scan.list";
        private static final String SYSTEM_DIR = ("system" + File.separator);
        private ArrayList<String> mRootDataList;

        public RootDataBundle() {
            this.mRootDataList = null;
            this.mRootDataList = new ArrayList<>();
        }

        private File getRootDataFile() {
            File dataDir = Environment.getDataDirectory();
            return new File(dataDir, SYSTEM_DIR + RSCAN_LIST_FILE);
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
            char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            char[] chars = new char[(bytes.length * 2)];
            for (int j = 0; j < bytes.length; j++) {
                int byteValue = bytes[j] & 255;
                chars[j * 2] = hexChars[byteValue >>> 4];
                chars[(j * 2) + 1] = hexChars[byteValue & 15];
            }
            return new String(chars).toUpperCase(Locale.ENGLISH);
        }

        private void writeRootData() {
            synchronized (this.mRootDataList) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(getRootDataFile());
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    Iterator<String> it = this.mRootDataList.iterator();
                    while (it.hasNext()) {
                        bos.write(it.next().getBytes(StandardCharsets.UTF_8));
                        bos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                    }
                    IoUtils.closeQuietly(bos);
                } catch (IOException e) {
                    try {
                        Log.e(RootDetectReport.TAG, "Failed to write root result data");
                        IoUtils.closeQuietly(null);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        throw th;
                    }
                }
                IoUtils.closeQuietly(fos);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0021, code lost:
            return;
         */
        private void readRootData() {
            synchronized (this.mRootDataList) {
                this.mRootDataList.clear();
                File file = getRootDataFile();
                if (file.exists()) {
                    BufferedReader reader = null;
                    try {
                        FileInputStream mFileInputStream = new FileInputStream(file);
                        InputStreamReader mInputStreamReader = new InputStreamReader(mFileInputStream, "UTF-8");
                        reader = new BufferedReader(mInputStreamReader);
                        StringBuffer sb = new StringBuffer(MAX_STR_LEN);
                        while (true) {
                            int read = reader.read();
                            int intChar = read;
                            if (read == -1) {
                                break;
                            } else if (sb.length() >= MAX_STR_LEN) {
                                break;
                            } else {
                                sb.append((char) intChar);
                            }
                        }
                        String[] temp = sb.toString().split(System.lineSeparator());
                        for (String add : temp) {
                            this.mRootDataList.add(add);
                        }
                        IoUtils.closeQuietly(mFileInputStream);
                        IoUtils.closeQuietly(mInputStreamReader);
                    } catch (FileNotFoundException e) {
                        Log.e(RootDetectReport.TAG, "file root result list cannot be found");
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                    } catch (IOException e2) {
                        try {
                            Log.e(RootDetectReport.TAG, "Failed to read root result list");
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(null);
                        } catch (Throwable th) {
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(null);
                            throw th;
                        }
                    }
                    IoUtils.closeQuietly(reader);
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
            String rootDataHash = null;
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

    private void setRootStatusProperty(int rootstatus) {
        try {
            SystemProperties.set("persist.sys.root.status", Integer.toString(rootstatus & BIT_RPROC_CLEAR));
        } catch (NumberFormatException e) {
            Log.e(TAG, "get number format exception when set root status property");
        } catch (Exception e2) {
            Log.e(TAG, "setRootStatusProperty failed, stpGetStatusAllIDRetValue = " + (rootstatus & BIT_RPROC_CLEAR));
        }
    }

    public JSONObject parcelStpHidlRootData(int rootstatus) {
        JSONObject json = new JSONObject();
        try {
            json.put(HwSecDiagnoseConstant.ROOT_STATUS, rootstatus & BIT_RPROC_CLEAR);
            int i = 0;
            json.put(HwSecDiagnoseConstant.ROOT_ERR_CODE, 0);
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_CODE, (rootstatus & 1) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SYS_CALL, (rootstatus & 2) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SE_HOOKS, (rootstatus & 4) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SE_STATUS, (rootstatus & 8) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_SU, (rootstatus & 16) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SYS_RW, (rootstatus & 32) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_ADBD, (rootstatus & 64) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_VB_STATUS, (rootstatus & 128) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_PROP, (rootstatus & 256) > 0 ? 1 : 0);
            if ((rootstatus & 512) > 0) {
                i = 1;
            }
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_SETIDS, i);
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "parcel root data, something wrong with the json object");
            return null;
        }
    }

    private void reportRootStatus() {
        boolean needReport = false;
        int ret = AppLayerStpProxy.getInstance().getEachItemRootStatus();
        if (ret < 0) {
            Log.e(TAG, "get each item root status failed. ret = " + ret);
            return;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "all item root scan result from hidl is:" + ret);
        }
        setRootStatusProperty(ret);
        JSONObject json = parcelStpHidlRootData(ret);
        if (json == null) {
            Log.e(TAG, "parcel root data failed. ret = " + ret);
            return;
        }
        if (ret > 0) {
            needReport = !new RootDataBundle().hasSame(json);
            if (HW_DEBUG) {
                Log.d(TAG, "parcelStpHidlRootData hasSame = " + hasSame + " needReport = " + needReport);
            }
        }
        try {
            json.put(HwSecDiagnoseConstant.ROOT_ROOT_PRO, (ret & 1024) > 0 ? 1 : 0);
            if (HW_DEBUG) {
                Log.d(TAG, "parcelStpHidlRootData json = " + json);
            }
            this.mListener.onRootReport(json, needReport);
        } catch (JSONException e) {
            Log.e(TAG, "proclist put error");
        }
    }

    private int getRootStatusAndReport() {
        int ret = AppLayerStpProxy.getInstance().getRootStatusSync();
        if (ret < 0) {
            Log.e(TAG, "get root status by category failed. ret = " + ret);
        } else if (ret == 0) {
            setRootStatusProperty(ret);
            Log.i(TAG, "root status is ok. ret = " + ret);
        } else {
            reportRootStatus();
        }
        return ret;
    }

    private void triggerRootScanProc() {
        int ret = 0;
        if (!this.rootScanHasTrigger) {
            BufferedReader reader = null;
            InputStreamReader mInputStreamReader = null;
            FileInputStream mFileInputStream = null;
            try {
                mFileInputStream = new FileInputStream(new File(FILE_PROC_ROOT_SCAN));
                mInputStreamReader = new InputStreamReader(mFileInputStream, "UTF-8");
                reader = new BufferedReader(mInputStreamReader);
                if (reader.read() != -1) {
                    this.rootScanHasTrigger = true;
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "triggerRootScan, trigger file cannot be found");
                ret = -3;
            } catch (IOException e2) {
                Log.e(TAG, "failed to read the trigger proc file");
                ret = -4;
            } catch (NumberFormatException e3) {
                Log.e(TAG, "some data is not of the type Integer during parsing trigger file");
                ret = -1;
            } catch (Throwable th) {
                IoUtils.closeQuietly(null);
                IoUtils.closeQuietly(null);
                IoUtils.closeQuietly(null);
                throw th;
            }
            IoUtils.closeQuietly(mFileInputStream);
            IoUtils.closeQuietly(mInputStreamReader);
            IoUtils.closeQuietly(reader);
            if (HW_DEBUG) {
                Log.d(TAG, "bootcompleted trigger return value = " + ret);
            }
        }
    }

    public void triggerRootScan() {
        mStartTime = System.currentTimeMillis();
        triggerRootScanProc();
        getRootStatusAndReport();
        mEndTime = System.currentTimeMillis();
        if (HW_DEBUG) {
            Log.d(TAG, "trigger root scan success!, whole rootscan run TIME = " + (mEndTime - mStartTime) + "ms");
        }
    }

    /* access modifiers changed from: package-private */
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    private RootDetectReport(Context context) {
        this.mContext = context;
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

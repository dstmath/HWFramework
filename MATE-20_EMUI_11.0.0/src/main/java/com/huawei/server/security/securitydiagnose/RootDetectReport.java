package com.huawei.server.security.securitydiagnose;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.HiLog;
import android.util.HiLogLabel;
import com.huawei.android.os.SystemPropertiesEx;
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
import org.json.JSONException;
import org.json.JSONObject;

public class RootDetectReport {
    private static final int BIT_RPROC_CLEAR = -1025;
    private static final int DOMAIN = 218115848;
    private static final int FILE_NOT_FOUND_ERR = -3;
    private static final String FILE_PROC_ROOT_SCAN = (File.separator + "proc" + File.separator + "root_scan");
    private static final int GENERIC_ERR = -1;
    private static final HiLogLabel HILOG_LABEL = new HiLogLabel(3, (int) DOMAIN, TAG);
    private static final int IO_EXCEPTION = -4;
    private static final int LIST_SIZE = 10;
    private static final String TAG = "RootDetectReport";
    private static long sEndTime = 0;
    private static RootDetectReport sInstance;
    private static long sStartTime = 0;
    private Context mContext;
    private boolean mIsRootScanHasTrigger = false;
    private Listener mListener;

    public interface Listener {
        void onRootReport(JSONObject jSONObject, boolean z);
    }

    private RootDetectReport(Context context) {
        this.mContext = context;
    }

    private void setRootStatusProperty(int rootstatus) {
        try {
            SystemPropertiesEx.set(HwSecDiagnoseConstant.PROPERTY_ROOT_STATUS, Integer.toString(rootstatus & BIT_RPROC_CLEAR));
        } catch (NumberFormatException e) {
            HiLog.error(HILOG_LABEL, "get number format exception when set root status property", new Object[0]);
        } catch (Exception e2) {
            HiLog.error(HILOG_LABEL, "setRootStatusProperty failed, stpGetStatusAllIDRetValue = %{public}d", new Object[]{Integer.valueOf(rootstatus & BIT_RPROC_CLEAR)});
        }
    }

    public JSONObject parcelStpHidlRootData(int rootstatus) {
        JSONObject json = new JSONObject();
        try {
            json.put(HwSecDiagnoseConstant.ROOT_STATUS, rootstatus & BIT_RPROC_CLEAR);
            json.put(HwSecDiagnoseConstant.ROOT_ERR_CODE, 0);
            int i = 1;
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_CODE, (rootstatus & 1) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SYS_CALL, (rootstatus & 2) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SE_HOOKS, (rootstatus & 4) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SE_STATUS, (rootstatus & 8) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_SU, (rootstatus & 16) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_SYS_RW, (rootstatus & 32) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_ADBD, (rootstatus & 64) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_VB_STATUS, (rootstatus & 128) > 0 ? 1 : 0);
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_PROP, (rootstatus & HwSecDiagnoseConstant.BIT_SYS_PROPS) > 0 ? 1 : 0);
            if ((rootstatus & HwSecDiagnoseConstant.BIT_SETIDS) <= 0) {
                i = 0;
            }
            json.put(HwSecDiagnoseConstant.ROOT_CHECK_SETIDS, i);
            return json;
        } catch (JSONException e) {
            HiLog.error(HILOG_LABEL, "parcel root data, something wrong with the json object", new Object[0]);
            return null;
        }
    }

    private void reportRootStatus() {
        int ret = AppLayerStpProxy.getInstance().getEachItemRootStatus();
        if (ret < 0) {
            HiLog.error(HILOG_LABEL, "get each item root status failed. ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return;
        }
        HiLog.debug(HILOG_LABEL, "all item root scan result from hidl is:%{public}d", new Object[]{Integer.valueOf(ret)});
        setRootStatusProperty(ret);
        JSONObject json = parcelStpHidlRootData(ret);
        if (json == null) {
            HiLog.error(HILOG_LABEL, "parcel root data failed. ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            return;
        }
        boolean isNeedReport = false;
        if (ret > 0) {
            boolean hasSame = new RootDataBundle().hasSame(json);
            isNeedReport = !hasSame;
            HiLog.debug(HILOG_LABEL, "parcelStpHidlRootData hasSame = %{public}b isNeedReport = %{public}b", new Object[]{Boolean.valueOf(hasSame), Boolean.valueOf(isNeedReport)});
        }
        try {
            json.put(HwSecDiagnoseConstant.ROOT_ROOT_PRO, (ret & HwSecDiagnoseConstant.BIT_RPROC) > 0 ? 1 : 0);
            HiLog.debug(HILOG_LABEL, "parcelStpHidlRootData json = %{public}s", new Object[]{json});
            this.mListener.onRootReport(json, isNeedReport);
        } catch (JSONException e) {
            HiLog.error(HILOG_LABEL, "proclist put error", new Object[0]);
        }
    }

    private int getRootStatusAndReport() {
        int ret = AppLayerStpProxy.getInstance().getRootStatusSync();
        if (ret < 0) {
            HiLog.error(HILOG_LABEL, "get root status by category failed. ret = %{public}b", new Object[]{Integer.valueOf(ret)});
        } else if (ret == 0) {
            setRootStatusProperty(ret);
            HiLog.info(HILOG_LABEL, "root status is ok. ret = %{public}b", new Object[]{Integer.valueOf(ret)});
        } else {
            reportRootStatus();
        }
        return ret;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0036, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003c, code lost:
        r7.addSuppressed(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003f, code lost:
        throw r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0042, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0047, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0048, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004b, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x004e, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0053, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0054, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0057, code lost:
        throw r6;
     */
    private void triggerRootScanProc() {
        int ret = 0;
        if (!this.mIsRootScanHasTrigger) {
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(FILE_PROC_ROOT_SCAN));
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader reader = new BufferedReader(inputStreamReader);
                if (reader.read() != -1) {
                    this.mIsRootScanHasTrigger = true;
                }
                reader.close();
                inputStreamReader.close();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                HiLog.error(HILOG_LABEL, "triggerRootScan, trigger file cannot be found", new Object[0]);
                ret = -3;
            } catch (IOException e2) {
                HiLog.error(HILOG_LABEL, "failed to read the trigger proc file", new Object[0]);
                ret = -4;
            } catch (NumberFormatException e3) {
                HiLog.error(HILOG_LABEL, "some data is not of the type Integer during parsing trigger file", new Object[0]);
                ret = -1;
            }
            HiLog.debug(HILOG_LABEL, "bootcompleted trigger return value = %{public}b", new Object[]{Integer.valueOf(ret)});
        }
    }

    public void triggerRootScan() {
        sStartTime = System.currentTimeMillis();
        triggerRootScanProc();
        getRootStatusAndReport();
        sEndTime = System.currentTimeMillis();
        HiLog.debug(HILOG_LABEL, "trigger root scan success!, whole rootscan run TIME = %{public}d ms", new Object[]{Long.valueOf(sEndTime - sStartTime)});
    }

    /* access modifiers changed from: package-private */
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public static void init(Context context) {
        synchronized (RootDetectReport.class) {
            if (sInstance == null) {
                sInstance = new RootDetectReport(context);
            }
        }
    }

    public static RootDetectReport getInstance() {
        RootDetectReport rootDetectReport;
        synchronized (RootDetectReport.class) {
            rootDetectReport = sInstance;
        }
        return rootDetectReport;
    }

    /* access modifiers changed from: private */
    public static class RootDataBundle {
        private static final int MAX_DATA_RECORDS = 100;
        private static final int MAX_STR_LEN = 6800;
        private static final String RSCAN_LIST_FILE = "root_scan.list";
        private static final String SYSTEM_DIR = ("system" + File.separator);
        private final ArrayList<String> mRootDataList = new ArrayList<>(10);

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
                HiLog.error(RootDetectReport.HILOG_LABEL, "sha256 algorithm failed", new Object[0]);
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

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0049, code lost:
            r6 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x004a, code lost:
            r4.addSuppressed(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
            throw r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0050, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0055, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0056, code lost:
            r3.addSuppressed(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0059, code lost:
            throw r4;
         */
        private void writeRootData() {
            synchronized (this.mRootDataList) {
                try {
                    FileOutputStream fos = new FileOutputStream(getRootDataFile());
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    Iterator<String> it = this.mRootDataList.iterator();
                    while (it.hasNext()) {
                        bos.write(it.next().getBytes(StandardCharsets.UTF_8));
                        bos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                    }
                    bos.close();
                    fos.close();
                } catch (IOException e) {
                    HiLog.error(RootDetectReport.HILOG_LABEL, "Failed to write root result data", new Object[0]);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0076, code lost:
            r8 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
            r6.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x007b, code lost:
            r9 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x007c, code lost:
            r7.addSuppressed(r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x007f, code lost:
            throw r8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x0082, code lost:
            r7 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
            r5.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x0087, code lost:
            r8 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x0088, code lost:
            r6.addSuppressed(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x008b, code lost:
            throw r7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x008e, code lost:
            r6 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x0093, code lost:
            r7 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x0094, code lost:
            r5.addSuppressed(r7);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x0097, code lost:
            throw r6;
         */
        private void readRootData() {
            String[] temp;
            synchronized (this.mRootDataList) {
                this.mRootDataList.clear();
                File file = getRootDataFile();
                if (!file.exists()) {
                    HiLog.error(RootDetectReport.HILOG_LABEL, "readRootData file NOT exist!", new Object[0]);
                    return;
                }
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    StringBuffer sb = new StringBuffer((int) MAX_STR_LEN);
                    while (true) {
                        int intChar = reader.read();
                        if (intChar == -1) {
                            break;
                        } else if (sb.length() >= MAX_STR_LEN) {
                            break;
                        } else {
                            sb.append((char) intChar);
                        }
                    }
                    for (String str : sb.toString().split(System.lineSeparator())) {
                        this.mRootDataList.add(str);
                    }
                    reader.close();
                    inputStreamReader.close();
                    fileInputStream.close();
                } catch (FileNotFoundException e) {
                    HiLog.error(RootDetectReport.HILOG_LABEL, "file root result list cannot be found", new Object[0]);
                } catch (IOException e2) {
                    HiLog.error(RootDetectReport.HILOG_LABEL, "Failed to read root result list", new Object[0]);
                }
            }
        }

        public boolean hasSame(JSONObject data) {
            if (data == null) {
                HiLog.error(RootDetectReport.HILOG_LABEL, "hasSame The data is NULL!", new Object[0]);
                return false;
            }
            readRootData();
            String rootDataHash = null;
            try {
                rootDataHash = sha256(data.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                HiLog.error(RootDetectReport.HILOG_LABEL, "hasSame encoding unsupported", new Object[0]);
            }
            if (TextUtils.isEmpty(rootDataHash)) {
                HiLog.error(RootDetectReport.HILOG_LABEL, "hasSame HASHCODE is null!", new Object[0]);
                return false;
            }
            HiLog.debug(RootDetectReport.HILOG_LABEL, "hasSame rootDataHash = %{public}s", new Object[]{rootDataHash});
            synchronized (this.mRootDataList) {
                if (this.mRootDataList.isEmpty() || !this.mRootDataList.contains(rootDataHash)) {
                    if (this.mRootDataList.size() < MAX_DATA_RECORDS) {
                        this.mRootDataList.add(rootDataHash);
                    } else {
                        try {
                            this.mRootDataList.remove(0);
                            this.mRootDataList.add(rootDataHash);
                        } catch (IndexOutOfBoundsException e2) {
                            HiLog.error(RootDetectReport.HILOG_LABEL, "IndexOutOfBoundsException", new Object[0]);
                        }
                    }
                    writeRootData();
                    return false;
                }
                HiLog.debug(RootDetectReport.HILOG_LABEL, "addDataList has existed", new Object[0]);
                return true;
            }
        }
    }
}

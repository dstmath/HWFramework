package com.huawei.server.security.securitydiagnose;

import android.content.Context;
import android.util.HiLog;
import android.util.HiLogLabel;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class RootDetectReport {
    private static final int BIT_RPROC_CLEAR = -1025;
    private static final int DOMAIN = 218115848;
    private static final int FILE_NOT_FOUND_ERR = -3;
    private static final String FILE_PROC_ROOT_SCAN = (File.separator + "proc" + File.separator + "root_scan");
    private static final int GENERIC_ERR = -1;
    private static final HiLogLabel HILOG_LABEL = new HiLogLabel(3, (int) DOMAIN, TAG);
    private static final int IO_EXCEPTION = -4;
    private static final String TAG = "RootDetectReport";
    private static long sEndTime = 0;
    private static RootDetectReport sInstance;
    private static long sStartTime = 0;
    private Context mContext;
    private boolean mIsRootScanHasTrigger = false;
    private Listener mListener;

    public interface Listener {
        void onRootReport();
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

    private int getRootStatusAndReport() {
        int ret = AppLayerStpProxy.getInstance().getRootStatusSync();
        if (ret < 0) {
            HiLog.error(HILOG_LABEL, "get root status by category failed. ret = %{public}b", new Object[]{Integer.valueOf(ret)});
        } else if (ret == 0) {
            setRootStatusProperty(ret);
            HiLog.info(HILOG_LABEL, "root status is ok. ret = %{public}d", new Object[]{Integer.valueOf(ret)});
        } else {
            ret = AppLayerStpProxy.getInstance().getEachItemRootStatus();
            if (ret < 0) {
                HiLog.error(HILOG_LABEL, "get each item root status failed. ret = %{public}d", new Object[]{Integer.valueOf(ret)});
                ret = -1;
            }
            setRootStatusProperty(ret);
            this.mListener.onRootReport();
            HiLog.info(HILOG_LABEL, "root status is risk. ret = %{public}d", new Object[]{Integer.valueOf(ret)});
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
            HiLog.debug(HILOG_LABEL, "bootcompleted trigger return value = %{public}d", new Object[]{Integer.valueOf(ret)});
        }
    }

    /* access modifiers changed from: package-private */
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void triggerRootScan() {
        sStartTime = System.currentTimeMillis();
        triggerRootScanProc();
        getRootStatusAndReport();
        sEndTime = System.currentTimeMillis();
        HiLog.debug(HILOG_LABEL, "trigger root scan success!, whole rootscan run TIME = %{public}d ms", new Object[]{Long.valueOf(sEndTime - sStartTime)});
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
}

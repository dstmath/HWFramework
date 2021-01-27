package com.android.server.pm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.appactcontrol.AppActConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ComponentChangeMonitor {
    static final String COMCHANGE_REPORT_FILE_DIR = (Environment.getDataDirectory() + "/log/com_change/");
    static final String COM_CHANGE_REPORT_FILE_LATEST = "com_change_report.1";
    static final String COM_CHANGE_REPORT_FILE_PREFIX = "com_change_report.";
    static final boolean IS_DEBUG = false;
    static final int MAX_FILE_COUNT = 5;
    static final int MAX_FILE_SIZE = 102400;
    static final int MSG_WIRTE_FILE = 0;
    static final String TAG = "ComponentChangeMonitor";
    private Context mContext;
    private Looper mHandlerLooper = null;
    private List<String> mMonitorList = new ArrayList();
    private WriteFileHandler mWriteHandler = null;

    public ComponentChangeMonitor(Context context, Looper looper) {
        this.mContext = context;
        this.mHandlerLooper = looper;
        init();
    }

    private void init() {
        parseComChangeMonitorsXml();
        if (!this.mMonitorList.isEmpty()) {
            initComChangeDir();
            if (this.mHandlerLooper == null) {
                HandlerThread handlerThread = new HandlerThread(TAG);
                handlerThread.start();
                this.mHandlerLooper = handlerThread.getLooper();
            }
            this.mWriteHandler = new WriteFileHandler(this.mHandlerLooper);
        }
    }

    /* access modifiers changed from: private */
    public class WriteFileHandler extends Handler {
        WriteFileHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 0) {
                Log.w(ComponentChangeMonitor.TAG, "WriteFileHandler unsupport operator " + msg.what);
            } else if (msg.obj instanceof String) {
                ComponentChangeMonitor.this.writeLogToFile((String) msg.obj);
            }
        }
    }

    private void initComChangeDir() {
        File gmsDir = new File(COMCHANGE_REPORT_FILE_DIR);
        if (!gmsDir.exists()) {
            boolean isDir = gmsDir.mkdir();
            Log.i(TAG, "comChange directory not exist, make it: " + isDir);
        }
    }

    /* access modifiers changed from: protected */
    public void writeComponetChangeLogToFile(ComponentName componentName, int newState, int userId) {
        if (componentName != null && this.mWriteHandler != null && isMonitorComponent(componentName.getPackageName())) {
            String callPkgName = getAppNameByPid(Binder.getCallingPid());
            String enable = newState <= 1 ? "enable" : AppActConstant.VALUE_DISABLE;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long nowTime = System.currentTimeMillis();
            String writeLine = "User{" + userId + "}: " + callPkgName + " " + enable + " " + componentName + " at " + sdf.format(new Date(nowTime)) + "." + System.lineSeparator();
            Log.i(TAG, "writeComponetChangeLogToFile writeLine: " + writeLine);
            this.mWriteHandler.sendMessage(this.mWriteHandler.obtainMessage(0, writeLine));
        }
    }

    private boolean isMonitorComponent(String appName) {
        if (this.mMonitorList.isEmpty()) {
            return false;
        }
        return this.mMonitorList.contains(appName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003a, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003e, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0042, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0045, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0048, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0049, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004c, code lost:
        throw r3;
     */
    private void writeLogToFile(String oneLine) {
        try {
            FileOutputStream fos = new FileOutputStream(COMCHANGE_REPORT_FILE_DIR + COM_CHANGE_REPORT_FILE_LATEST, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
            BufferedWriter out = new BufferedWriter(osw);
            out.write(oneLine);
            $closeResource(null, out);
            $closeResource(null, osw);
            $closeResource(null, fos);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST FileNotFoundException");
        } catch (IOException e2) {
            Log.e(TAG, "COM_CHANGE_REPORT_FILE_LATEST IOException");
        } catch (Exception e3) {
            Log.e(TAG, "writeLogToFile Exception");
        }
        checkFilesStatus();
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private void checkFilesStatus() {
        File[] subFiles = new File(COMCHANGE_REPORT_FILE_DIR).listFiles();
        if (subFiles != null) {
            int dirFileCount = subFiles.length;
            if (dirFileCount > 5) {
                Log.w(TAG, "deleteOldAndCreateNewFile abnormal file Count: " + dirFileCount);
                for (File subFile : subFiles) {
                    if (subFile != null && subFile.exists() && !subFile.delete()) {
                        Log.w(TAG, "checkFilesStatus delete failed: " + subFile);
                    }
                }
                return;
            }
            File latestFile = new File(COMCHANGE_REPORT_FILE_DIR + COM_CHANGE_REPORT_FILE_LATEST);
            if (latestFile.exists() && latestFile.length() > 102400) {
                for (int i = dirFileCount; i > 0; i--) {
                    File tempFile = new File(COMCHANGE_REPORT_FILE_DIR + COM_CHANGE_REPORT_FILE_PREFIX + i);
                    if (tempFile.exists()) {
                        if (!tempFile.renameTo(new File(COMCHANGE_REPORT_FILE_DIR + COM_CHANGE_REPORT_FILE_PREFIX + (i + 1)))) {
                            Log.w(TAG, "checkFilesStatus rename failed: " + i);
                        }
                    }
                }
                if (dirFileCount == 5) {
                    File delFile = new File(COMCHANGE_REPORT_FILE_DIR + COM_CHANGE_REPORT_FILE_PREFIX + 6);
                    if (delFile.exists() && !delFile.delete()) {
                        Log.w(TAG, "checkFilesStatus delete last file failed");
                    }
                }
            }
        }
    }

    private String getAppNameByPid(int pid) {
        List<ActivityManager.RunningAppProcessInfo> processes = getRunningProcesses();
        if (processes == null || processes.size() == 0) {
            Log.d(TAG, "get app name, get running process failed");
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }

    private List<ActivityManager.RunningAppProcessInfo> getRunningProcesses() {
        Context context = this.mContext;
        if (context == null) {
            Log.d(TAG, "getRunningProcesses, mContext is null");
            return new ArrayList(0);
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager != null) {
            return activityManager.getRunningAppProcesses();
        }
        Log.d(TAG, "get process status, get ams service failed");
        return new ArrayList(0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002b, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002e, code lost:
        throw r4;
     */
    private void parseComChangeMonitorsXml() {
        File monitorFile = null;
        try {
            monitorFile = HwCfgFilePolicy.getCfgFile("xml/com_change_monitors.xml", 0);
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (monitorFile == null) {
            Log.i(TAG, "com_change_monitors.xml is not exist");
            return;
        }
        try {
            InputStream inputstream = new FileInputStream(monitorFile);
            parseComChangeData(inputstream);
            $closeResource(null, inputstream);
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "com_change_monitors.xml FileNotFoundException");
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "com_change_monitors.xml XmlPullParserException");
        } catch (IOException e4) {
            Log.e(TAG, "com_change_monitors.xml IOException");
        } catch (Exception e5) {
            Log.e(TAG, "parseComChangeMonitorsXml Exception");
        }
    }

    private void parseComChangeData(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, null);
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            if (event != 0) {
                if (event != 2) {
                    if (event != 3) {
                    }
                } else if ("component".equals(pullParser.getName())) {
                    String itemName = pullParser.nextText();
                    if (!TextUtils.isEmpty(itemName)) {
                        itemName = itemName.intern();
                    }
                    this.mMonitorList.add(itemName);
                }
            }
        }
    }
}

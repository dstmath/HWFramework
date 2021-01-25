package com.huawei.securitycenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwTvSecurityManager {
    private static final int BIND_SERVICE_RETRY_TIME = 20;
    private static final int BIND_SERVICE_WAIT_ONE_TIME = 50;
    private static final String CHECK_UNINSTALL_RESULT_CODE = "result_code";
    private static final String CHECK_UNINSTALL_VIRUS_TYPE = "virus_type";
    public static final String FILE_PATH_KEY = "file_path";
    private static final int MSG_BIND_SCAN_SERVICE = 0;
    private static final int MSG_SCAN_APK_FILE_END = 2;
    private static final int MSG_SCAN_APK_FILE_START = 1;
    private static final int MSG_SCAN_APK_TIME_OUT = 3;
    private static final int RESULT_CODE_ERROR_PARAM = 1;
    private static final int RESULT_CODE_ERROR_TIMEOUT = 3;
    private static final int RESULT_CODE_OK = 0;
    private static final int RESULT_TYPE_UNKNOWN = -1;
    private static final String SCAN_APK_FILE_SERVICE = "com.huawei.antivirus.engine.ScanApkFileService";
    private static final String SCAN_RESULT_KEY = "scan_result";
    private static final String TAG = "HwTvSecurityManager";
    private static final String TV_SYSTEM_MANAGER_PACKAGE_NAME = "com.huawei.tvsystemmanager";
    private static Context sContext = ActivityThreadEx.currentApplication().getApplicationContext();
    private static volatile HwTvSecurityManager sInstance;

    private HwTvSecurityManager() {
    }

    public static HwTvSecurityManager getInstance() {
        if (sInstance == null) {
            synchronized (HwTvSecurityManager.class) {
                if (sInstance == null) {
                    sInstance = new HwTvSecurityManager();
                }
            }
        }
        return sInstance;
    }

    @Nullable
    public Bundle checkUninstalledApk(@NonNull String filePath, String sourcePkg, long timeOutMillis) {
        String pkgName;
        PackageInfo packageInfo;
        if (TextUtils.isEmpty(filePath)) {
            Log.e(TAG, "checkUninstallApk path is empty, return error");
            return generateResult(1, -1);
        }
        Context context = sContext;
        if (context == null || (packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, 1)) == null) {
            pkgName = null;
        } else {
            pkgName = packageInfo.packageName;
        }
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "source file pkgName = null");
            return generateResult(1, -1);
        }
        Log.i(TAG, "checkUninstallApk name=" + pkgName + ", source=" + sourcePkg);
        return check(pkgName, filePath, sourcePkg, new CheckUninstallApkRequest(), timeOutMillis);
    }

    private Bundle check(String name, String path, String source, CheckUninstallApkRequest request, long timeout) {
        long begin = SystemClock.elapsedRealtime();
        request.scanApkFile(name, path, request);
        request.waitRequestResult(timeout);
        request.unbindScanApkService(sContext);
        long end = SystemClock.elapsedRealtime();
        Log.i(TAG, "check time=" + (end - begin) + ", type=" + request.getResult());
        if (request.isExpired()) {
            return generateResult(3, -1);
        }
        return generateResult(0, request.getResult());
    }

    private Bundle generateResult(int resultCode, int virusType) {
        Bundle result = new Bundle();
        result.putInt(CHECK_UNINSTALL_RESULT_CODE, resultCode);
        result.putInt(CHECK_UNINSTALL_VIRUS_TYPE, virusType);
        return result;
    }

    /* access modifiers changed from: private */
    public static class CheckUninstallApkRequest {
        private Messenger mClientMessenger;
        private AtomicBoolean mExpired = new AtomicBoolean(false);
        private HandlerThread mHandleThread;
        private final Object mLock = new Object();
        private int mResult = -1;
        private Messenger mServerMessenger;
        private ServiceConnection mServiceConnection = new ServiceConnection() {
            /* class com.huawei.securitycenter.HwTvSecurityManager.CheckUninstallApkRequest.AnonymousClass1 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                CheckUninstallApkRequest.this.mServerMessenger = new Messenger(service);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                CheckUninstallApkRequest.this.mServerMessenger = null;
            }
        };

        public synchronized int getResult() {
            return this.mResult;
        }

        public synchronized void setResult(int result) {
            this.mResult = result;
        }

        public void waitRequestResult(long timeout) {
            synchronized (this.mLock) {
                boolean isTimeLeft = true;
                while (isTimeLeft) {
                    try {
                        long begin = SystemClock.elapsedRealtime();
                        this.mLock.wait(timeout);
                        isTimeLeft = false;
                        if (SystemClock.elapsedRealtime() - begin >= timeout) {
                            this.mExpired.set(true);
                        }
                    } catch (InterruptedException e) {
                        Log.e(HwTvSecurityManager.TAG, "waitRequestResult InterruptedException");
                    }
                }
            }
        }

        public void releaseLock() {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
        }

        public boolean isExpired() {
            return this.mExpired.get();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void scanApkFile(String packageName, final String filePath, final CheckUninstallApkRequest request) {
            final Intent service = new Intent();
            service.setComponent(new ComponentName(HwTvSecurityManager.TV_SYSTEM_MANAGER_PACKAGE_NAME, HwTvSecurityManager.SCAN_APK_FILE_SERVICE));
            this.mHandleThread = new HandlerThread("scan_apk_file");
            this.mHandleThread.start();
            Handler handler = new Handler(this.mHandleThread.getLooper()) {
                /* class com.huawei.securitycenter.HwTvSecurityManager.CheckUninstallApkRequest.AnonymousClass2 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    if (msg != null) {
                        int what = msg.what;
                        Log.i(HwTvSecurityManager.TAG, "handle receive msg what = " + what);
                        if (what == 0) {
                            CheckUninstallApkRequest.this.bindScanFileService(HwTvSecurityManager.sContext, service, filePath);
                        } else if (what == 2) {
                            Bundle bundle = msg.getData();
                            if (bundle == null) {
                                Log.i(HwTvSecurityManager.TAG, "bundle is null");
                                request.setResult(-1);
                            } else {
                                int result = bundle.getInt(HwTvSecurityManager.SCAN_RESULT_KEY);
                                Log.i(HwTvSecurityManager.TAG, "scan apk file result is " + result);
                                request.setResult(result);
                            }
                            request.releaseLock();
                        }
                    }
                }
            };
            this.mClientMessenger = new Messenger(handler);
            handler.sendMessage(handler.obtainMessage(0));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void bindScanFileService(Context context, Intent intent, String filePath) {
            if (intent != null) {
                try {
                    context.bindService(intent, this.mServiceConnection, 1);
                } catch (SecurityException e) {
                    Log.e(HwTvSecurityManager.TAG, "bind service error");
                }
                int retry = 0;
                while (this.mServerMessenger == null && retry < 20) {
                    try {
                        Thread.sleep(50);
                        retry++;
                    } catch (InterruptedException e2) {
                        Log.e(HwTvSecurityManager.TAG, "interrupted exception when sleep for bind service");
                    }
                }
                if (retry >= 20) {
                    Log.i(HwTvSecurityManager.TAG, "check uninstall apk caller bind scan apk service failed");
                } else if (this.mServerMessenger != null) {
                    Message msgScanFile = Message.obtain();
                    msgScanFile.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString(HwTvSecurityManager.FILE_PATH_KEY, filePath);
                    msgScanFile.setData(bundle);
                    msgScanFile.replyTo = this.mClientMessenger;
                    try {
                        this.mServerMessenger.send(msgScanFile);
                    } catch (RemoteException e3) {
                        Log.e(HwTvSecurityManager.TAG, "check uninstall apk caller send msg failed");
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void unbindScanApkService(Context context) {
            if (this.mServerMessenger != null) {
                context.unbindService(this.mServiceConnection);
            }
            HandlerThread handlerThread = this.mHandleThread;
            if (handlerThread != null) {
                handlerThread.quitSafely();
                this.mHandleThread = null;
            }
        }
    }
}

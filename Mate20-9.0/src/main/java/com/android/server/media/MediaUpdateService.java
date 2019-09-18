package com.android.server.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.media.IMediaExtractorUpdateService;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.pm.DumpState;

public class MediaUpdateService extends SystemService {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String EXTRACTOR_UPDATE_SERVICE_NAME = "media.extractor.update";
    private static final String MEDIA_UPDATE_PACKAGE_NAME = SystemProperties.get("ro.mediacomponents.package");
    private static final String TAG = "MediaUpdateService";
    final Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public IMediaExtractorUpdateService mMediaExtractorUpdateService;

    public MediaUpdateService(Context context) {
        super(context);
    }

    public void onStart() {
        if (("userdebug".equals(Build.TYPE) || "eng".equals(Build.TYPE)) && !TextUtils.isEmpty(MEDIA_UPDATE_PACKAGE_NAME)) {
            connect();
            registerBroadcastReceiver();
        }
    }

    /* access modifiers changed from: private */
    public void connect() {
        IBinder binder = ServiceManager.getService(EXTRACTOR_UPDATE_SERVICE_NAME);
        if (binder != null) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        Slog.w(MediaUpdateService.TAG, "mediaextractor died; reconnecting");
                        IMediaExtractorUpdateService unused = MediaUpdateService.this.mMediaExtractorUpdateService = null;
                        MediaUpdateService.this.connect();
                    }
                }, 0);
            } catch (Exception e) {
                binder = null;
            }
        }
        if (binder != null) {
            this.mMediaExtractorUpdateService = IMediaExtractorUpdateService.Stub.asInterface(binder);
            this.mHandler.post(new Runnable() {
                public void run() {
                    MediaUpdateService.this.packageStateChanged();
                }
            });
            return;
        }
        Slog.w(TAG, "media.extractor.update not found.");
    }

    private void registerBroadcastReceiver() {
        BroadcastReceiver updateReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0033, code lost:
                if (r0.equals("android.intent.action.PACKAGE_REMOVED") != false) goto L_0x0041;
             */
            /* JADX WARNING: Removed duplicated region for block: B:19:0x0045  */
            /* JADX WARNING: Removed duplicated region for block: B:20:0x004b  */
            /* JADX WARNING: Removed duplicated region for block: B:21:0x0051  */
            public void onReceive(Context context, Intent intent) {
                char c = 0;
                if (intent.getIntExtra("android.intent.extra.user_handle", 0) == 0) {
                    String action = intent.getAction();
                    int hashCode = action.hashCode();
                    if (hashCode != 172491798) {
                        if (hashCode != 525384130) {
                            if (hashCode == 1544582882 && action.equals("android.intent.action.PACKAGE_ADDED")) {
                                c = 2;
                                switch (c) {
                                    case 0:
                                        if (!intent.getExtras().getBoolean("android.intent.extra.REPLACING")) {
                                            MediaUpdateService.this.packageStateChanged();
                                            break;
                                        } else {
                                            return;
                                        }
                                    case 1:
                                        MediaUpdateService.this.packageStateChanged();
                                        break;
                                    case 2:
                                        MediaUpdateService.this.packageStateChanged();
                                        break;
                                }
                            }
                        }
                    } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                        }
                    }
                    c = 65535;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme("package");
        filter.addDataSchemeSpecificPart(MEDIA_UPDATE_PACKAGE_NAME, 0);
        getContext().registerReceiverAsUser(updateReceiver, UserHandle.ALL, filter, null, null);
    }

    /* access modifiers changed from: private */
    public void packageStateChanged() {
        ApplicationInfo packageInfo = null;
        boolean pluginsAvailable = false;
        try {
            packageInfo = getContext().getPackageManager().getApplicationInfo(MEDIA_UPDATE_PACKAGE_NAME, DumpState.DUMP_DEXOPT);
            pluginsAvailable = packageInfo.enabled;
        } catch (Exception e) {
            Slog.v(TAG, "package '" + MEDIA_UPDATE_PACKAGE_NAME + "' not installed");
        }
        if (!(packageInfo == null || Build.VERSION.SDK_INT == packageInfo.targetSdkVersion)) {
            Slog.w(TAG, "This update package is not for this platform version. Ignoring. platform:" + Build.VERSION.SDK_INT + " targetSdk:" + packageInfo.targetSdkVersion);
            pluginsAvailable = false;
        }
        loadExtractorPlugins((packageInfo == null || !pluginsAvailable) ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : packageInfo.sourceDir);
    }

    private void loadExtractorPlugins(String apkPath) {
        try {
            if (this.mMediaExtractorUpdateService != null) {
                this.mMediaExtractorUpdateService.loadPlugins(apkPath);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Error in loadPlugins", e);
        }
    }
}

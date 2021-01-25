package com.android.server.gpu;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.gamedriver.GameDriverProto;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.Base64;
import com.android.framework.protobuf.InvalidProtocolBufferException;
import com.android.internal.annotations.GuardedBy;
import com.android.server.SystemService;
import com.android.server.pm.DumpState;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GpuService extends SystemService {
    private static final int BASE64_FLAGS = 3;
    public static final boolean DEBUG = false;
    private static final String GAME_DRIVER_WHITELIST_FILENAME = "whitelist.txt";
    private static final String PROPERTY_GFX_DRIVER = "ro.gfx.driver.0";
    public static final String TAG = "GpuService";
    @GuardedBy({"mLock"})
    private GameDriverProto.Blacklists mBlacklists;
    private ContentResolver mContentResolver;
    private final Context mContext;
    private DeviceConfigListener mDeviceConfigListener;
    private final Object mDeviceConfigLock = new Object();
    private final String mDriverPackageName;
    private long mGameDriverVersionCode;
    private final Object mLock = new Object();
    private final PackageManager mPackageManager;
    private SettingsObserver mSettingsObserver;

    public GpuService(Context context) {
        super(context);
        this.mContext = context;
        this.mDriverPackageName = SystemProperties.get(PROPERTY_GFX_DRIVER);
        this.mGameDriverVersionCode = -1;
        this.mPackageManager = context.getPackageManager();
        String str = this.mDriverPackageName;
        if (str != null && !str.isEmpty()) {
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageFilter.addDataScheme("package");
            getContext().registerReceiverAsUser(new PackageReceiver(), UserHandle.ALL, packageFilter, null, null);
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 1000) {
            this.mContentResolver = this.mContext.getContentResolver();
            String str = this.mDriverPackageName;
            if (str != null && !str.isEmpty()) {
                this.mSettingsObserver = new SettingsObserver();
                this.mDeviceConfigListener = new DeviceConfigListener();
                fetchGameDriverPackageProperties();
                processBlacklists();
                setBlacklist();
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri mGameDriverBlackUri = Settings.Global.getUriFor("game_driver_blacklists");

        SettingsObserver() {
            super(new Handler());
            GpuService.this.mContentResolver.registerContentObserver(this.mGameDriverBlackUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null && this.mGameDriverBlackUri.equals(uri)) {
                GpuService.this.processBlacklists();
                GpuService.this.setBlacklist();
            }
        }
    }

    private final class DeviceConfigListener implements DeviceConfig.OnPropertiesChangedListener {
        DeviceConfigListener() {
            DeviceConfig.addOnPropertiesChangedListener("game_driver", GpuService.this.mContext.getMainExecutor(), this);
        }

        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            synchronized (GpuService.this.mDeviceConfigLock) {
                if (properties.getKeyset().contains("game_driver_blacklists")) {
                    GpuService.this.parseBlacklists(properties.getString("game_driver_blacklists", ""));
                    GpuService.this.setBlacklist();
                }
            }
        }
    }

    private final class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x003e, code lost:
            if (r4.equals("android.intent.action.PACKAGE_ADDED") == false) goto L_0x0055;
         */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0058 A[ADDED_TO_REGION] */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getData().getSchemeSpecificPart().equals(GpuService.this.mDriverPackageName)) {
                boolean z = false;
                intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                String action = intent.getAction();
                int hashCode = action.hashCode();
                if (hashCode != 172491798) {
                    if (hashCode != 525384130) {
                        if (hashCode == 1544582882) {
                        }
                    } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        z = true;
                        if (z || z || z) {
                            GpuService.this.fetchGameDriverPackageProperties();
                            GpuService.this.setBlacklist();
                        }
                        return;
                    }
                } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                    z = true;
                    if (z) {
                    }
                    GpuService.this.fetchGameDriverPackageProperties();
                    GpuService.this.setBlacklist();
                }
                z = true;
                if (z) {
                }
                GpuService.this.fetchGameDriverPackageProperties();
                GpuService.this.setBlacklist();
            }
        }
    }

    private static void assetToSettingsGlobal(Context context, Context driverContext, String fileName, String settingsGlobal, CharSequence delimiter) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(driverContext.getAssets().open(fileName)));
            ArrayList<String> assetStrings = new ArrayList<>();
            while (true) {
                String assetString = reader.readLine();
                if (assetString != null) {
                    assetStrings.add(assetString);
                } else {
                    Settings.Global.putString(context.getContentResolver(), settingsGlobal, String.join(delimiter, assetStrings));
                    return;
                }
            }
        } catch (IOException e) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fetchGameDriverPackageProperties() {
        try {
            ApplicationInfo driverInfo = this.mPackageManager.getApplicationInfo(this.mDriverPackageName, DumpState.DUMP_DEXOPT);
            if (driverInfo.targetSdkVersion >= 26) {
                Settings.Global.putString(this.mContentResolver, "game_driver_whitelist", "");
                this.mGameDriverVersionCode = driverInfo.longVersionCode;
                try {
                    assetToSettingsGlobal(this.mContext, this.mContext.createPackageContext(this.mDriverPackageName, 4), GAME_DRIVER_WHITELIST_FILENAME, "game_driver_whitelist", ",");
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        } catch (PackageManager.NameNotFoundException e2) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processBlacklists() {
        String base64String = DeviceConfig.getProperty("game_driver", "game_driver_blacklists");
        if (base64String == null) {
            base64String = Settings.Global.getString(this.mContentResolver, "game_driver_blacklists");
        }
        parseBlacklists(base64String != null ? base64String : "");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void parseBlacklists(String base64String) {
        synchronized (this.mLock) {
            this.mBlacklists = null;
            try {
                this.mBlacklists = GameDriverProto.Blacklists.parseFrom(Base64.decode(base64String, 3));
            } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBlacklist() {
        Settings.Global.putString(this.mContentResolver, "game_driver_blacklist", "");
        synchronized (this.mLock) {
            if (this.mBlacklists != null) {
                for (GameDriverProto.Blacklist blacklist : this.mBlacklists.getBlacklistsList()) {
                    if (blacklist.getVersionCode() == this.mGameDriverVersionCode) {
                        Settings.Global.putString(this.mContentResolver, "game_driver_blacklist", String.join(",", blacklist.getPackageNamesList()));
                        return;
                    }
                }
            }
        }
    }
}

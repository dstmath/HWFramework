package com.android.server.dreams;

import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;
import com.android.server.power.HwCustPowerManagerServiceImpl;
import java.util.ArrayList;
import java.util.List;

public class HwCustDreamManagerServiceImpl extends HwCustDreamManagerService {
    private static final String CHARGING_ALBUM_SERVICE = "com.android.dreams.album/.AlbumDream";
    private static final String TAG = "HwCustDreamManagerServiceImpl";
    public static final boolean mChargingAlbumSupported;
    private boolean mChargingAlbumEnabled;
    private Context mContext;
    private final Object mLock;

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            Slog.d(HwCustDreamManagerServiceImpl.TAG, "DreamManagerService: SettingsObserver:onChange");
            synchronized (HwCustDreamManagerServiceImpl.this.mLock) {
                HwCustDreamManagerServiceImpl.this.updateSettingsLocked();
            }
        }
    }

    static {
        mChargingAlbumSupported = SystemProperties.getBoolean("ro.config.ChargingAlbum", false);
    }

    public HwCustDreamManagerServiceImpl(Context context) {
        super(context);
        this.mLock = new Object();
        this.mContext = context;
    }

    public boolean isChargingAlbumEnabled() {
        if (!mChargingAlbumSupported) {
            return super.isChargingAlbumEnabled();
        }
        Slog.d(TAG, "isChargingAlbumEnabled " + this.mChargingAlbumEnabled + " supported " + mChargingAlbumSupported);
        updateSettingsLocked();
        return this.mChargingAlbumEnabled ? mChargingAlbumSupported : false;
    }

    public void systemReady() {
        if (mChargingAlbumSupported) {
            updateSettingsLocked();
        }
    }

    private void updateSettingsLocked() {
        boolean z = true;
        if (mChargingAlbumSupported) {
            int i;
            Slog.d(TAG, "updateSettingsLocked");
            ContentResolver resolver = this.mContext.getContentResolver();
            String str = HwCustPowerManagerServiceImpl.CHARGING_ALBUM_ENABLED;
            if (mChargingAlbumSupported) {
                i = 1;
            } else {
                i = 0;
            }
            if (System.getIntForUser(resolver, str, i, -2) == 0) {
                z = false;
            }
            this.mChargingAlbumEnabled = z;
        }
    }

    public ComponentName[] getChargingAlbumForUser(int userId) {
        Slog.d(TAG, "getChargingAlbumForUser");
        if (!mChargingAlbumSupported) {
            return super.getChargingAlbumForUser(userId);
        }
        ComponentName[] componentNameArr;
        String names = System.getStringForUser(this.mContext.getContentResolver(), HwCustPowerManagerServiceImpl.CHARGING_ALBUM_COMPONENTS, userId);
        ComponentName[] components;
        if (names == null || "".equals(names)) {
            components = componentsFromString(CHARGING_ALBUM_SERVICE);
        } else {
            components = componentsFromString(names);
        }
        List<ComponentName> validComponents = new ArrayList();
        for (ComponentName component : components) {
            if (serviceExists(component)) {
                validComponents.add(component);
            } else {
                Slog.w(TAG, "Dream " + component + " does not exist");
            }
        }
        if (validComponents.size() != 0) {
            componentNameArr = (ComponentName[]) validComponents.toArray(new ComponentName[validComponents.size()]);
        } else {
            componentNameArr = new ComponentName[0];
        }
        return componentNameArr;
    }

    private boolean serviceExists(ComponentName name) {
        boolean z = false;
        if (name != null) {
            try {
                if (this.mContext.getPackageManager().getServiceInfo(name, 0) != null) {
                    z = true;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }
        return z;
    }

    private static ComponentName[] componentsFromString(String names) {
        if (names == null) {
            return new ComponentName[0];
        }
        String[] namesArray = names.split(",");
        ComponentName[] componentNames = new ComponentName[namesArray.length];
        for (int i = 0; i < namesArray.length; i++) {
            componentNames[i] = ComponentName.unflattenFromString(namesArray[i]);
        }
        return componentNames;
    }

    public boolean isCoverOpened() {
        if (!mChargingAlbumSupported) {
            return super.isCoverOpened();
        }
        Slog.d(TAG, "iscovered " + HwFrameworkFactory.getCoverManager().isCoverOpen());
        return HwFrameworkFactory.getCoverManager().isCoverOpen();
    }
}

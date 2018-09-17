package com.android.server.input;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings.Secure;

public class HwCustHwInputManagerServiceImpl extends HwCustHwInputManagerService {
    static final String TAG = "HwCustHwInputManagerService";
    private Context mContext;
    private ContentResolver mResolver;

    public HwCustHwInputManagerServiceImpl(Context context) {
        super(context);
        this.mContext = context;
        if (this.mContext != null) {
            this.mResolver = this.mContext.getContentResolver();
        }
    }

    public int registerContentObserverForFingerprintNavigation(ContentObserver co) {
        if (this.mResolver == null) {
            return 0;
        }
        this.mResolver.registerContentObserver(Secure.getUriFor(HwCustFingerprintNavigationImpl.FINGERPRINT_LAUNCHER_SLIDE), false, co);
        this.mResolver.registerContentObserver(Secure.getUriFor(HwCustFingerprintNavigationImpl.FINGERPRINT_GALLERY_SLIDE), false, co);
        return 1;
    }

    public boolean isFingerprintNavigationEnable() {
        if (HwCustFingerprintNavigationImpl.FINGERPRINT_NAVIGATION_ENABLE) {
            return (Secure.getInt(this.mResolver, HwCustFingerprintNavigationImpl.FINGERPRINT_LAUNCHER_SLIDE, 0) == 0 && Secure.getInt(this.mResolver, HwCustFingerprintNavigationImpl.FINGERPRINT_GALLERY_SLIDE, 0) == 0) ? false : true;
        } else {
            return false;
        }
    }
}

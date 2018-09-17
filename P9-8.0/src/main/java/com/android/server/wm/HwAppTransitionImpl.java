package com.android.server.wm;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.android.server.AttributeCache.Entry;

public class HwAppTransitionImpl implements IHwAppTransition {
    public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    public static final boolean IS_NOVA_PERF = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    private static final String TAG = "HwAppTransitionImpl";
    private Context mHwextContext = null;

    public Entry overrideAnimation(LayoutParams lp, int animAttr, Context mContext, Entry mEnt, AppTransition appTransition) {
        Entry ent = null;
        if (mEnt == null) {
            return null;
        }
        Context context = mEnt.context;
        if (mEnt.array.getResourceId(animAttr, 0) != 0) {
            String packageName = "androidhwext";
            if (this.mHwextContext == null) {
                try {
                    this.mHwextContext = mContext.createPackageContext(packageName, 0);
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "overrideAnimation : no hwext package");
                }
            }
            if (this.mHwextContext != null) {
                int hwAnimResId;
                int anim = 0;
                String title = lp.getTitle().toString();
                if (!(title == null || (title.equals("") ^ 1) == 0)) {
                    if (IS_EMUI_LITE || IS_NOVA_PERF) {
                        hwAnimResId = this.mHwextContext.getResources().getIdentifier("HwAnimation_lite." + title, "style", packageName);
                    } else {
                        hwAnimResId = this.mHwextContext.getResources().getIdentifier("HwAnimation." + title, "style", packageName);
                    }
                    if (hwAnimResId != 0) {
                        ent = appTransition.getCachedAnimations(packageName, hwAnimResId);
                        if (ent != null) {
                            context = ent.context;
                            anim = ent.array.getResourceId(animAttr, 0);
                        }
                    }
                }
                if ((IS_EMUI_LITE || IS_NOVA_PERF) && anim == 0) {
                    hwAnimResId = this.mHwextContext.getResources().getIdentifier("HwAnimation_lite", "style", packageName);
                    if (hwAnimResId != 0) {
                        ent = appTransition.getCachedAnimations(packageName, hwAnimResId);
                        if (ent != null) {
                            context = ent.context;
                            anim = ent.array.getResourceId(animAttr, 0);
                        }
                    }
                }
                if (anim == 0) {
                    hwAnimResId = this.mHwextContext.getResources().getIdentifier("HwAnimation", "style", packageName);
                    if (hwAnimResId != 0) {
                        ent = appTransition.getCachedAnimations(packageName, hwAnimResId);
                        if (ent != null) {
                            context = ent.context;
                            anim = ent.array.getResourceId(animAttr, 0);
                        }
                    }
                }
                if (anim == 0) {
                    return null;
                }
            }
        }
        return ent;
    }
}

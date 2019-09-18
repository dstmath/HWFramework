package com.android.server.wm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.Log;
import android.view.WindowManager;
import com.android.server.AttributeCache;

public class HwAppTransitionImpl implements IHwAppTransition {
    public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    public static final boolean IS_NOVA_PERF = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    private static final String TAG = "HwAppTransitionImpl";
    private Context mHwextContext = null;

    public AttributeCache.Entry overrideAnimation(WindowManager.LayoutParams lp, int animAttr, Context mContext, AttributeCache.Entry mEnt, AppTransition appTransition) {
        int i = animAttr;
        AttributeCache.Entry entry = mEnt;
        AppTransition appTransition2 = appTransition;
        AttributeCache.Entry ent = null;
        if (entry != null) {
            Context context = entry.context;
            if (entry.array.getResourceId(i, 0) != 0) {
                if (this.mHwextContext == null) {
                    try {
                        this.mHwextContext = mContext.createPackageContext("androidhwext", 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "overrideAnimation : no hwext package");
                    }
                } else {
                    Context context2 = mContext;
                }
                if (this.mHwextContext != null) {
                    int anim = 0;
                    int hwAnimResId = 0;
                    String title = lp.getTitle().toString();
                    if (title != null && !title.equals("")) {
                        if (IS_EMUI_LITE || IS_NOVA_PERF) {
                            Resources resources = this.mHwextContext.getResources();
                            hwAnimResId = resources.getIdentifier("HwAnimation_lite." + title, "style", "androidhwext");
                        } else {
                            Resources resources2 = this.mHwextContext.getResources();
                            hwAnimResId = resources2.getIdentifier("HwAnimation." + title, "style", "androidhwext");
                        }
                        if (hwAnimResId != 0) {
                            ent = appTransition2.getCachedAnimations("androidhwext", hwAnimResId);
                            if (ent != null) {
                                Context context3 = ent.context;
                                anim = ent.array.getResourceId(i, 0);
                            }
                        }
                    }
                    if ((IS_EMUI_LITE || IS_NOVA_PERF) && anim == 0) {
                        hwAnimResId = this.mHwextContext.getResources().getIdentifier("HwAnimation_lite", "style", "androidhwext");
                        if (hwAnimResId != 0) {
                            ent = appTransition2.getCachedAnimations("androidhwext", hwAnimResId);
                            if (ent != null) {
                                Context context4 = ent.context;
                                anim = ent.array.getResourceId(i, 0);
                            }
                        }
                    }
                    if (anim == 0) {
                        hwAnimResId = this.mHwextContext.getResources().getIdentifier("HwAnimation", "style", "androidhwext");
                        if (hwAnimResId != 0) {
                            ent = appTransition2.getCachedAnimations("androidhwext", hwAnimResId);
                            if (ent != null) {
                                Context context5 = ent.context;
                                anim = ent.array.getResourceId(i, 0);
                            }
                        }
                    }
                    int i2 = hwAnimResId;
                    int anim2 = anim;
                    int hwAnimResId2 = i2;
                    if (anim2 == 0) {
                        return null;
                    }
                }
            } else {
                Context context6 = mContext;
            }
            return ent;
        }
        Context context7 = mContext;
        return null;
    }
}

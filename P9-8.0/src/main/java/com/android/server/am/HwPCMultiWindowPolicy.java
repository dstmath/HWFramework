package com.android.server.am;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.WindowManagerPolicy.WindowState;

public class HwPCMultiWindowPolicy {
    static final float PAD_LANDSCAPE_RATIO = 1.7777778f;
    static final float PAD_LANDSCAPE_WIDTH = 0.65f;
    static final float PAD_MARGIN_LEFT_RATIO = 0.1f;
    static final float PAD_MARGIN_TOP_RATIO = 0.04f;
    static final float PAD_PORTRAIT_HEIGHT = 0.9f;
    static final float PAD_PORTRAIT_RATIO = 0.5625f;
    static int mDefLandscapeHeight = 550;
    static int mDefPortraitWidth = 360;
    static int mLandscapeMaxWidth = 1200;
    static float mLandscapeRatio = PAD_LANDSCAPE_RATIO;
    static int mPortraitMaxHeightDp = 900;
    static float mPortraitRatio = PAD_PORTRAIT_RATIO;
    static int mWindowMarginLeft = 0;
    static int mWindowMarginTop = 0;

    static void initialize(Context context, HwPCMultiWindowManager multiWindowMgr) {
        try {
            updateDefaultSize(context, multiWindowMgr);
        } catch (Exception e) {
            Log.e("HwPCMultiWindowPolicy", "initialize error", e);
        }
    }

    public static int dpToPx(Context context, int dps) {
        return Math.round(context.getResources().getDisplayMetrics().density * ((float) dps));
    }

    public static int pxToDp(Context context, int px) {
        return Math.round(((float) px) / context.getResources().getDisplayMetrics().density);
    }

    public static Rect pxRectToDpRect(Context context, Rect rect) {
        return new Rect(pxToDp(context, rect.left), pxToDp(context, rect.top), pxToDp(context, rect.right), pxToDp(context, rect.bottom));
    }

    public static Rect dpRectToPxRect(Context context, Rect rect) {
        return new Rect(dpToPx(context, rect.left), dpToPx(context, rect.top), dpToPx(context, rect.right), dpToPx(context, rect.bottom));
    }

    public static void updateDefaultSize(Context context, HwPCMultiWindowManager multiWindowMgr) {
        if (multiWindowMgr != null) {
            Rect rect = multiWindowMgr.getMaximizedBounds();
            int appWidth = rect.width();
            int appHeight = rect.height();
            if (HwPCUtils.enabledInPad()) {
                updateDefaultSizeInPad(context, appWidth, appHeight);
            } else {
                updateDefaultSizeInPhone(context, appWidth, appHeight);
            }
        }
    }

    private static void updateDefaultSizeInPhone(Context context, int appWidth, int appHeight) {
        int maxHeight = dpToPx(context, mPortraitMaxHeightDp);
        float tmpHeight = ((float) appHeight) * 0.8f;
        if (tmpHeight >= ((float) maxHeight)) {
            tmpHeight = (float) maxHeight;
        }
        mDefPortraitWidth = (int) (((float) ((int) tmpHeight)) * mPortraitRatio);
        int maxWidth = dpToPx(context, mLandscapeMaxWidth);
        float tmpWidth = ((float) appWidth) * 0.6f;
        if (tmpWidth >= ((float) maxWidth)) {
            tmpWidth = (float) maxWidth;
        }
        mDefLandscapeHeight = (int) (((float) ((int) tmpWidth)) / mLandscapeRatio);
        mWindowMarginTop = (int) (((float) appHeight) * 0.1f);
        mWindowMarginLeft = (int) (((float) appWidth) * 0.1f);
    }

    private static void updateDefaultSizeInPad(Context context, int appWidth, int appHeight) {
        mPortraitRatio = PAD_PORTRAIT_RATIO;
        mDefPortraitWidth = (int) ((((float) appHeight) * PAD_PORTRAIT_HEIGHT) * PAD_PORTRAIT_RATIO);
        mLandscapeRatio = PAD_LANDSCAPE_RATIO;
        mDefLandscapeHeight = (int) ((((float) appWidth) * PAD_LANDSCAPE_WIDTH) / PAD_LANDSCAPE_RATIO);
        mWindowMarginTop = (int) (((float) appHeight) * PAD_MARGIN_TOP_RATIO);
        mWindowMarginLeft = (int) (((float) appWidth) * 0.1f);
    }

    public static void layoutWindowForPadPCMode(WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int contentBottom) {
        if (HwPCUtils.enabledInPad()) {
            int windowState = -1;
            ActivityRecord r = ActivityRecord.forToken((IBinder) win.getAppToken());
            if (r != null) {
                if (r instanceof HwActivityRecord) {
                    windowState = ((HwActivityRecord) r).getWindowState();
                }
                if (windowState != -1) {
                    r.service.mWindowManager.layoutWindowForPadPCMode(win, pf, df, cf, vf, contentBottom);
                }
            }
        }
    }
}

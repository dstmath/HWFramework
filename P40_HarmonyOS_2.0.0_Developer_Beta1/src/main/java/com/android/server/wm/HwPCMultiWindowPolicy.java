package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.util.HwPCUtils;
import android.util.Log;

public class HwPCMultiWindowPolicy {
    private static final float DEF_HEIGHT_RATIO = 0.8f;
    private static final int DEF_LANDSCAPE_HEIGHT = 550;
    private static final int DEF_PORTAIT_WIDTH = 360;
    private static final float DEF_WIDTH_RATIO = 0.6f;
    private static final int LANDSCAPE_MAX_WIDTH = 1200;
    private static final float LANDSCAPE_RATION = 1.7777778f;
    private static final float MARGIN_LEFT_RATIO = 0.1f;
    private static final float MARGIN_TOP_RATIO = 0.1f;
    private static final float PAD_LANDSCAPE_RATIO = 1.7777778f;
    private static final float PAD_LANDSCAPE_WIDTH = 0.65f;
    private static final float PAD_MARGIN_LEFT_RATIO = 0.1f;
    private static final float PAD_MARGIN_TOP_RATIO = 0.04f;
    private static final float PAD_PORTRAIT_HEIGHT = 0.9f;
    private static final float PAD_PORTRAIT_RATIO = 0.5625f;
    private static final int PORTRAIT_MAX_HEIGHT_DP = 900;
    private static final float PORTRAIT_RATION = 0.5625f;
    static int mDefLandscapeHeight = DEF_LANDSCAPE_HEIGHT;
    static int mDefPortraitWidth = DEF_PORTAIT_WIDTH;
    static float mLandscapeRatio = 1.7777778f;
    static float mPortraitRatio = 0.5625f;
    static int mWindowMarginLeft = 0;
    static int mWindowMarginTop = 0;

    private HwPCMultiWindowPolicy() {
    }

    static void initialize(Context context, HwPCMultiWindowManager multiWindowMgr) {
        try {
            updateDefaultSize(context, multiWindowMgr);
        } catch (Exception e) {
            Log.e("HwPCMultiWindowPolicy", "initialize error");
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
        int maxHeight = dpToPx(context, PORTRAIT_MAX_HEIGHT_DP);
        float tmpHeight = ((float) appHeight) * DEF_HEIGHT_RATIO;
        mDefPortraitWidth = (int) (((float) ((int) (tmpHeight < ((float) maxHeight) ? tmpHeight : (float) maxHeight))) * mPortraitRatio);
        int maxWidth = dpToPx(context, LANDSCAPE_MAX_WIDTH);
        float tmpWidth = ((float) appWidth) * DEF_WIDTH_RATIO;
        mDefLandscapeHeight = (int) (((float) ((int) (tmpWidth < ((float) maxWidth) ? tmpWidth : (float) maxWidth))) / mLandscapeRatio);
        mWindowMarginTop = (int) (((float) appHeight) * 0.1f);
        mWindowMarginLeft = (int) (((float) appWidth) * 0.1f);
    }

    private static void updateDefaultSizeInPad(Context context, int appWidth, int appHeight) {
        mPortraitRatio = 0.5625f;
        mDefPortraitWidth = (int) (((float) appHeight) * PAD_PORTRAIT_HEIGHT * 0.5625f);
        mLandscapeRatio = 1.7777778f;
        mDefLandscapeHeight = (int) ((((float) appWidth) * PAD_LANDSCAPE_WIDTH) / 1.7777778f);
        mWindowMarginTop = (int) (((float) appHeight) * PAD_MARGIN_TOP_RATIO);
        mWindowMarginLeft = (int) (((float) appWidth) * 0.1f);
    }
}

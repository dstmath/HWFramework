package com.huawei.android.view;

import android.graphics.Rect;
import android.widget.RemoteViews;
import huawei.android.view.HwExtDisplayUIManager;
import java.util.List;

public class ExtDisplayUIManagerEx {
    public static final int PHONE_ANIMATION = 0;

    public static void executeSideAnimation(int type, boolean isStart) {
        HwExtDisplayUIManager.executeSideAnimation(type, isStart);
    }

    public static void addCustomViews(List<Rect> rects, List<RemoteViews> customViews) {
        HwExtDisplayUIManager.addCustomViews(rects, customViews);
    }

    public static void removeCustomViews(List<Rect> rects) {
        HwExtDisplayUIManager.removeCustomViews(rects);
    }

    public static void setTouchMapping(List<Rect> fromRects, List<Rect> toRects) {
        HwExtDisplayUIManager.setTouchMapping(fromRects, toRects);
    }
}

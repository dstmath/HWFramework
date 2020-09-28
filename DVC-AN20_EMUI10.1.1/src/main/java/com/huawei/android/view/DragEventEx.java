package com.huawei.android.view;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import com.android.internal.view.IDragAndDropPermissions;

public class DragEventEx {
    public static DragEvent obtain(int action, float x, float y, Object localState, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, boolean result) {
        return DragEvent.obtain(action, x, y, localState, description, data, dragAndDropPermissions, result);
    }

    public static DragEvent obtain(int action, float x, float y, Object localState, ClipDescription description, ClipData data, boolean result) {
        return DragEvent.obtain(action, x, y, localState, description, data, null, result);
    }
}

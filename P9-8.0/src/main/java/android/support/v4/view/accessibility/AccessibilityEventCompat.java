package android.support.v4.view.accessibility;

import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityRecord;

public final class AccessibilityEventCompat {
    public static final int CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION = 4;
    public static final int CONTENT_CHANGE_TYPE_SUBTREE = 1;
    public static final int CONTENT_CHANGE_TYPE_TEXT = 2;
    public static final int CONTENT_CHANGE_TYPE_UNDEFINED = 0;
    private static final AccessibilityEventCompatBaseImpl IMPL;
    public static final int TYPES_ALL_MASK = -1;
    public static final int TYPE_ANNOUNCEMENT = 16384;
    public static final int TYPE_ASSIST_READING_CONTEXT = 16777216;
    public static final int TYPE_GESTURE_DETECTION_END = 524288;
    public static final int TYPE_GESTURE_DETECTION_START = 262144;
    @Deprecated
    public static final int TYPE_TOUCH_EXPLORATION_GESTURE_END = 1024;
    @Deprecated
    public static final int TYPE_TOUCH_EXPLORATION_GESTURE_START = 512;
    public static final int TYPE_TOUCH_INTERACTION_END = 2097152;
    public static final int TYPE_TOUCH_INTERACTION_START = 1048576;
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUSED = 32768;
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED = 65536;
    public static final int TYPE_VIEW_CONTEXT_CLICKED = 8388608;
    @Deprecated
    public static final int TYPE_VIEW_HOVER_ENTER = 128;
    @Deprecated
    public static final int TYPE_VIEW_HOVER_EXIT = 256;
    @Deprecated
    public static final int TYPE_VIEW_SCROLLED = 4096;
    @Deprecated
    public static final int TYPE_VIEW_TEXT_SELECTION_CHANGED = 8192;
    public static final int TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY = 131072;
    public static final int TYPE_WINDOWS_CHANGED = 4194304;
    @Deprecated
    public static final int TYPE_WINDOW_CONTENT_CHANGED = 2048;

    static class AccessibilityEventCompatBaseImpl {
        AccessibilityEventCompatBaseImpl() {
        }

        public void setContentChangeTypes(AccessibilityEvent event, int types) {
        }

        public int getContentChangeTypes(AccessibilityEvent event) {
            return 0;
        }

        public void setMovementGranularity(AccessibilityEvent event, int granularity) {
        }

        public int getMovementGranularity(AccessibilityEvent event) {
            return 0;
        }

        public void setAction(AccessibilityEvent event, int action) {
        }

        public int getAction(AccessibilityEvent event) {
            return 0;
        }
    }

    @RequiresApi(16)
    static class AccessibilityEventCompatApi16Impl extends AccessibilityEventCompatBaseImpl {
        AccessibilityEventCompatApi16Impl() {
        }

        public void setMovementGranularity(AccessibilityEvent event, int granularity) {
            event.setMovementGranularity(granularity);
        }

        public int getMovementGranularity(AccessibilityEvent event) {
            return event.getMovementGranularity();
        }

        public void setAction(AccessibilityEvent event, int action) {
            event.setAction(action);
        }

        public int getAction(AccessibilityEvent event) {
            return event.getAction();
        }
    }

    @RequiresApi(19)
    static class AccessibilityEventCompatApi19Impl extends AccessibilityEventCompatApi16Impl {
        AccessibilityEventCompatApi19Impl() {
        }

        public void setContentChangeTypes(AccessibilityEvent event, int types) {
            event.setContentChangeTypes(types);
        }

        public int getContentChangeTypes(AccessibilityEvent event) {
            return event.getContentChangeTypes();
        }
    }

    static {
        if (VERSION.SDK_INT >= 19) {
            IMPL = new AccessibilityEventCompatApi19Impl();
        } else if (VERSION.SDK_INT >= 16) {
            IMPL = new AccessibilityEventCompatApi16Impl();
        } else {
            IMPL = new AccessibilityEventCompatBaseImpl();
        }
    }

    private AccessibilityEventCompat() {
    }

    @Deprecated
    public static int getRecordCount(AccessibilityEvent event) {
        return event.getRecordCount();
    }

    @Deprecated
    public static void appendRecord(AccessibilityEvent event, AccessibilityRecordCompat record) {
        event.appendRecord((AccessibilityRecord) record.getImpl());
    }

    @Deprecated
    public static AccessibilityRecordCompat getRecord(AccessibilityEvent event, int index) {
        return new AccessibilityRecordCompat(event.getRecord(index));
    }

    @Deprecated
    public static AccessibilityRecordCompat asRecord(AccessibilityEvent event) {
        return new AccessibilityRecordCompat(event);
    }

    public static void setContentChangeTypes(AccessibilityEvent event, int changeTypes) {
        IMPL.setContentChangeTypes(event, changeTypes);
    }

    public static int getContentChangeTypes(AccessibilityEvent event) {
        return IMPL.getContentChangeTypes(event);
    }

    public void setMovementGranularity(AccessibilityEvent event, int granularity) {
        IMPL.setMovementGranularity(event, granularity);
    }

    public int getMovementGranularity(AccessibilityEvent event) {
        return IMPL.getMovementGranularity(event);
    }

    public void setAction(AccessibilityEvent event, int action) {
        IMPL.setAction(event, action);
    }

    public int getAction(AccessibilityEvent event) {
        return IMPL.getAction(event);
    }
}

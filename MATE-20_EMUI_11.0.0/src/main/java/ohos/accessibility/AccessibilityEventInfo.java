package ohos.accessibility;

import java.util.ArrayList;

public class AccessibilityEventInfo {
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUSED_EVENT = 32768;
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED_EVENT = 65536;
    public static final int TYPE_VIEW_CLICKED_EVENT = 1;
    public static final int TYPE_VIEW_FOCUSED_EVENT = 8;
    public static final int TYPE_VIEW_HOVER_ENTER_EVENT = 128;
    public static final int TYPE_VIEW_HOVER_EXIT_EVENT = 256;
    public static final int TYPE_VIEW_LONG_CLICKED_EVENT = 2;
    public static final int TYPE_VIEW_SCROLLED_EVENT = 4096;
    public static final int TYPE_VIEW_SELECTED_EVENT = 4;
    public static final int TYPE_VIEW_TEXT_CHANGED_EVENT = 16;
    public static final int TYPE_VIEW_TEXT_SELECTION_CHANGED_EVENT = 8192;
    public static final int WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED = 128;
    public static final int WINDOWS_CHANGE_ACTIVE = 32;
    public static final int WINDOWS_CHANGE_ADDED = 1;
    public static final int WINDOWS_CHANGE_BOUNDS = 8;
    public static final int WINDOWS_CHANGE_CHILDREN = 512;
    public static final int WINDOWS_CHANGE_FOCUSED = 64;
    public static final int WINDOWS_CHANGE_LAYER = 16;
    public static final int WINDOWS_CHANGE_PARENT = 256;
    public static final int WINDOWS_CHANGE_PIP = 1024;
    public static final int WINDOWS_CHANGE_REMOVED = 2;
    public static final int WINDOWS_CHANGE_TITLE = 4;
    private int accessibilityEventType;
    private int action;
    private CharSequence bundleName;
    private CharSequence className;
    private int count;
    private int currentIndex;
    private CharSequence description;
    private int moveStep;
    private ArrayList<AccessibilityEventInfo> records;
    private int viewId;
    private int windowChangeTypes;

    public AccessibilityEventInfo() {
    }

    public AccessibilityEventInfo(int i) {
        this.accessibilityEventType = i;
    }

    public AccessibilityEventInfo(AccessibilityEventInfo accessibilityEventInfo) {
        if (accessibilityEventInfo != null) {
            this.action = accessibilityEventInfo.action;
            this.viewId = accessibilityEventInfo.viewId;
            this.moveStep = accessibilityEventInfo.moveStep;
            this.bundleName = accessibilityEventInfo.bundleName;
            this.windowChangeTypes = accessibilityEventInfo.windowChangeTypes;
            this.accessibilityEventType = accessibilityEventInfo.accessibilityEventType;
            ArrayList<AccessibilityEventInfo> arrayList = accessibilityEventInfo.records;
            if (arrayList != null) {
                this.records = arrayList;
            }
        }
    }

    public int getTriggerAction() {
        return this.action;
    }

    public int getWindowChangeTypes() {
        return this.windowChangeTypes;
    }

    public int getAccessibilityEventType() {
        return this.accessibilityEventType;
    }

    public int getTextMoveStep() {
        return this.moveStep;
    }

    public CharSequence getBundleName() {
        return this.bundleName;
    }

    public int getViewId() {
        return this.viewId;
    }

    public void addRecord(AccessibilityEventInfo accessibilityEventInfo) {
        if (this.records == null) {
            this.records = new ArrayList<>();
        }
        this.records.add(accessibilityEventInfo);
    }

    public ArrayList<AccessibilityEventInfo> getRecords() {
        if (this.records == null) {
            this.records = new ArrayList<>();
        }
        return this.records;
    }

    public void setTriggerAction(int i) {
        this.action = i;
    }

    public void setTextMoveStep(int i) {
        this.moveStep = i;
    }

    public void setBundleName(CharSequence charSequence) {
        this.bundleName = charSequence;
    }

    public void setAccessibilityEventType(int i) {
        this.accessibilityEventType = i;
    }

    public void setWindowChangeTypes(int i) {
        this.windowChangeTypes = i;
    }

    public void setViewId(int i) {
        this.viewId = i;
    }

    public CharSequence getClassName() {
        return this.className;
    }

    public void setClassName(CharSequence charSequence) {
        this.className = charSequence;
    }

    public CharSequence getDescription() {
        return this.description;
    }

    public void setDescription(CharSequence charSequence) {
        this.description = charSequence;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int i) {
        this.count = i;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public void setCurrentIndex(int i) {
        this.currentIndex = i;
    }
}

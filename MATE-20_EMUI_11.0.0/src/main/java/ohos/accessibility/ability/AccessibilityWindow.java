package ohos.accessibility.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.accessibility.adapter.ability.AccessibilityWindowAdapter;
import ohos.agp.utils.Rect;

public class AccessibilityWindow {
    public static final int TYPE_ACCESSIBILITY_OVERLAY = 4;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_INPUT_METHOD = 2;
    public static final int TYPE_SPLIT_SCREEN_DIVIDER = 5;
    public static final int TYPE_SYSTEM = 3;
    public static final int UNDEFINED_ID = -1;
    private long anchorId = -1;
    private List<Integer> childIds = new ArrayList();
    private int connectionId = -1;
    private boolean isAccessibilityFocused;
    private boolean isActive;
    private boolean isFocused;
    private int parentId = -1;
    private final Rect rectInScreen = new Rect();
    private int windowId = -1;
    private int windowLayer = -1;
    private CharSequence windowTitle;
    private int windowType = -1;

    private AccessibilityWindow() {
    }

    public static AccessibilityWindow get() {
        return new AccessibilityWindow();
    }

    public static AccessibilityWindow get(AccessibilityWindow accessibilityWindow) {
        AccessibilityWindow accessibilityWindow2 = get();
        accessibilityWindow2.connectionId = accessibilityWindow.connectionId;
        accessibilityWindow2.windowId = accessibilityWindow.windowId;
        accessibilityWindow2.windowType = accessibilityWindow.windowType;
        accessibilityWindow2.windowLayer = accessibilityWindow.windowLayer;
        accessibilityWindow2.windowTitle = accessibilityWindow.windowTitle;
        accessibilityWindow2.parentId = accessibilityWindow.parentId;
        accessibilityWindow2.anchorId = accessibilityWindow.anchorId;
        accessibilityWindow2.isFocused = accessibilityWindow.isFocused;
        accessibilityWindow2.isAccessibilityFocused = accessibilityWindow.isAccessibilityFocused;
        accessibilityWindow2.isActive = accessibilityWindow.isActive;
        accessibilityWindow2.rectInScreen.set(accessibilityWindow.rectInScreen.left, accessibilityWindow.rectInScreen.top, accessibilityWindow.rectInScreen.right, accessibilityWindow.rectInScreen.bottom);
        List<Integer> list = accessibilityWindow.childIds;
        if (list != null && !list.isEmpty()) {
            accessibilityWindow2.childIds = new ArrayList(accessibilityWindow.childIds.size());
            accessibilityWindow2.childIds.addAll(accessibilityWindow.childIds);
        }
        return accessibilityWindow2;
    }

    public int getWindowId() {
        return this.windowId;
    }

    public int getWindowType() {
        return this.windowType;
    }

    public int getWindowLayer() {
        return this.windowLayer;
    }

    public CharSequence getWindowTitle() {
        return this.windowTitle;
    }

    public Rect getRectInScreen() {
        return new Rect(this.rectInScreen.left, this.rectInScreen.top, this.rectInScreen.right, this.rectInScreen.bottom);
    }

    public boolean isAccessibilityFocused() {
        return this.isAccessibilityFocused;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public int getChildNum() {
        List<Integer> list = this.childIds;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public Optional<AccessibilityWindow> getChild(int i) {
        List<Integer> list = this.childIds;
        if (list != null && i < list.size()) {
            return AccessibilityWindowAdapter.getChild(this.childIds.get(i).intValue(), this.connectionId);
        }
        throw new IndexOutOfBoundsException();
    }

    public Optional<AccessibilityWindow> getParent() {
        return AccessibilityWindowAdapter.getParent(this.parentId, this.connectionId);
    }

    public Optional<AccessibilityInfo> getRootAccessibilityInfo() {
        return AccessibilityWindowAdapter.getRootAccessibilityInfoByWindowId(this.windowId, this.connectionId);
    }

    public Optional<AccessibilityInfo> getAnchor() {
        return AccessibilityWindowAdapter.getAnchor(this.parentId, (int) this.anchorId, this.connectionId);
    }

    public void setWindowType(int i) {
        this.windowType = i;
    }

    public void setWindowLayer(int i) {
        this.windowLayer = i;
    }

    public void setWindowId(int i) {
        this.windowId = i;
    }

    public void setConnectionId(int i) {
        this.connectionId = i;
    }

    public void setParentId(int i) {
        this.parentId = i;
    }

    public void setAnchorId(long j) {
        this.anchorId = j;
    }

    public void setRectInScreen(int i, int i2, int i3, int i4) {
        this.rectInScreen.set(i, i2, i3, i4);
    }

    public void setWindowTitle(CharSequence charSequence) {
        this.windowTitle = charSequence;
    }

    public void setAccessibilityFocused(boolean z) {
        this.isAccessibilityFocused = z;
    }

    public void setActive(boolean z) {
        this.isActive = z;
    }

    public void setFocused(boolean z) {
        this.isFocused = z;
    }

    public void addChild(int i) {
        if (this.childIds == null) {
            this.childIds = new ArrayList();
        }
        this.childIds.add(Integer.valueOf(i));
    }
}

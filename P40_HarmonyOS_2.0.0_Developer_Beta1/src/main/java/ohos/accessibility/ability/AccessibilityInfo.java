package ohos.accessibility.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import ohos.accessibility.adapter.ability.AccessibilitySearchAdapter;
import ohos.accessibility.adapter.ability.PerformActionAdapter;
import ohos.agp.utils.Rect;

public class AccessibilityInfo {
    public static final int ACCESSIBILITY_OPERATION_ACCESSIBILITY_FOCUS = 64;
    public static final int ACCESSIBILITY_OPERATION_CLEAR_ACCESSIBILITY_FOCUS = 128;
    public static final int ACCESSIBILITY_OPERATION_CLEAR_FOCUS = 2;
    public static final int ACCESSIBILITY_OPERATION_CLEAR_SELECTION = 8;
    public static final int ACCESSIBILITY_OPERATION_CLICK = 16;
    public static final int ACCESSIBILITY_OPERATION_COPY = 16384;
    public static final int ACCESSIBILITY_OPERATION_CUT = 65536;
    public static final int ACCESSIBILITY_OPERATION_FOCUS = 1;
    public static final int ACCESSIBILITY_OPERATION_LONG_CLICK = 32;
    public static final int ACCESSIBILITY_OPERATION_PASTE = 32768;
    public static final int ACCESSIBILITY_OPERATION_SCROLL_BACKWARD = 8192;
    public static final int ACCESSIBILITY_OPERATION_SCROLL_FORWARD = 4096;
    public static final int ACCESSIBILITY_OPERATION_SELECT = 4;
    public static final int ACCESSIBILITY_OPERATION_SET_SELECTION = 131072;
    public static final int ACCESSIBILITY_OPERATION_SET_TEXT = 2097152;
    public static final int DIRECTION_BACKWARD = 1;
    public static final int DIRECTION_DOWN = 130;
    public static final int DIRECTION_FORWARD = 2;
    public static final int DIRECTION_LEFT = 17;
    public static final int DIRECTION_RIGHT = 66;
    public static final int DIRECTION_UP = 33;
    public static final int FOCUS_TYPE_ACCESSIBILITY = 2;
    public static final int FOCUS_TYPE_INPUT = 1;
    private static final int UNDEFINED_ID = -1;
    private long accessibilityId;
    private CharSequence bundleName;
    private List<Long> childIds = new ArrayList();
    private String className;
    private String componentResourceId;
    private int connectionId = -1;
    private CharSequence content;
    private CharSequence description;
    private String hint;
    private boolean isAccessibilityFocused;
    private boolean isCheckable;
    private boolean isChecked;
    private boolean isClickable;
    private boolean isDismissible;
    private boolean isEditable;
    private boolean isEnabled;
    private boolean isFocusable;
    private boolean isFocused;
    private boolean isImportant;
    private boolean isLongClickable;
    private boolean isMultiline;
    private boolean isPassword;
    private boolean isScrollable;
    private boolean isSelected;
    private boolean isShowingHintText;
    private boolean isSupportPopup;
    private boolean isVisibleToUser;
    private int maxTextLength;
    private List<AccessibleOperation> operations;
    private final Rect rectInScreen = new Rect();
    private int windowId = -1;

    public long getAccessibilityId() {
        return this.accessibilityId;
    }

    public String getComponentResourceId() {
        return this.componentResourceId;
    }

    public CharSequence getContent() {
        return this.content;
    }

    public Rect getRectInScreen() {
        return new Rect(this.rectInScreen.left, this.rectInScreen.top, this.rectInScreen.right, this.rectInScreen.bottom);
    }

    public CharSequence getBundleName() {
        return this.bundleName;
    }

    public CharSequence getDescriptionInfo() {
        return this.description;
    }

    public Optional<AccessibilityInfo> getChild(int i) {
        List<Long> list = this.childIds;
        if (list != null && list.size() > i) {
            return AccessibilitySearchAdapter.getChild(this.connectionId, this.windowId, this.childIds.get(i).longValue());
        }
        throw new IndexOutOfBoundsException();
    }

    public int getChildCount() {
        List<Long> list = this.childIds;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public long getChildId(int i) {
        List<Long> list = this.childIds;
        if (list != null && list.size() > i) {
            return this.childIds.get(i).longValue();
        }
        throw new IndexOutOfBoundsException();
    }

    public void setAccessibilityId(long j) {
        this.accessibilityId = j;
    }

    public void setConnectionId(int i) {
        this.connectionId = i;
    }

    public void setWindowId(int i) {
        this.windowId = i;
    }

    public void setComponentResourceId(String str) {
        this.componentResourceId = str;
    }

    public void setContent(CharSequence charSequence) {
        this.content = charSequence;
    }

    public void setBundleName(CharSequence charSequence) {
        this.bundleName = charSequence;
    }

    public void setDescription(CharSequence charSequence) {
        this.description = charSequence;
    }

    public void setRectInScreen(int i, int i2, int i3, int i4) {
        this.rectInScreen.set(i, i2, i3, i4);
    }

    public void addChildId(long j) {
        if (this.childIds == null) {
            this.childIds = new ArrayList();
        }
        this.childIds.add(Long.valueOf(j));
    }

    public Optional<AccessibilityInfo> gainFocus(int i) {
        if (i != 2) {
            return Optional.empty();
        }
        return AccessibilitySearchAdapter.gainFocus(this.connectionId, this.windowId, this.accessibilityId, i);
    }

    public Optional<AccessibilityInfo> gainNextFocus(int i) {
        return AccessibilitySearchAdapter.gainNextFocus(this.connectionId, this.windowId, this.accessibilityId, i);
    }

    public List<AccessibilityInfo> getAccessibleInfoViaContent(String str) {
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChild(i).ifPresent(new AbilityConsumer(str, arrayList));
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public static class AbilityConsumer implements Consumer<AccessibilityInfo> {
        private List<AccessibilityInfo> accessibilityInfos;
        private String content;

        AbilityConsumer(String str, List<AccessibilityInfo> list) {
            this.content = str;
            this.accessibilityInfos = list;
        }

        public void accept(AccessibilityInfo accessibilityInfo) {
            if (Objects.equals(this.content, accessibilityInfo.content) || Objects.equals(this.content, accessibilityInfo.description)) {
                this.accessibilityInfos.add(accessibilityInfo);
            }
            if (accessibilityInfo.getChildCount() > 0) {
                this.accessibilityInfos.addAll(accessibilityInfo.getAccessibleInfoViaContent(this.content));
            }
        }
    }

    public boolean executeOperation(int i) {
        return PerformActionAdapter.performAction(this.connectionId, this.windowId, this.accessibilityId, i);
    }

    public void addOperation(AccessibleOperation accessibleOperation) {
        if (this.operations == null) {
            this.operations = new ArrayList();
        }
        this.operations.add(accessibleOperation);
    }

    public List<AccessibleOperation> getOperations() {
        return this.operations;
    }

    public boolean deleteOperation(AccessibleOperation accessibleOperation) {
        List<AccessibleOperation> list = this.operations;
        if (list == null) {
            return true;
        }
        return list.remove(accessibleOperation);
    }

    public boolean isPopupSupported() {
        return this.isSupportPopup;
    }

    public void setPopupSupported(boolean z) {
        this.isSupportPopup = z;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public String getHint() {
        return this.hint;
    }

    public void setHint(String str) {
        this.hint = str;
    }

    public int getMaximumLength() {
        return this.maxTextLength;
    }

    public void setMaximumLength(int i) {
        this.maxTextLength = i;
    }

    public boolean hasGainedAccessibilitySelected() {
        return this.isAccessibilityFocused;
    }

    public void setAccessibleFocusStatus(boolean z) {
        this.isAccessibilityFocused = z;
    }

    public boolean isCheckable() {
        return this.isCheckable;
    }

    public void setCheckable(boolean z) {
        this.isCheckable = z;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean z) {
        this.isChecked = z;
    }

    public boolean isClickable() {
        return this.isClickable;
    }

    public void setClickable(boolean z) {
        this.isClickable = z;
    }

    public boolean isDeletable() {
        return this.isDismissible;
    }

    public void setDeletable(boolean z) {
        this.isDismissible = z;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean z) {
        this.isEditable = z;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean z) {
        this.isEnabled = z;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void setFocused(boolean z) {
        this.isFocused = z;
    }

    public boolean isFocusable() {
        return this.isFocusable;
    }

    public void setFocusable(boolean z) {
        this.isFocusable = z;
    }

    public boolean isEssential() {
        return this.isImportant;
    }

    public void setEssential(boolean z) {
        this.isImportant = z;
    }

    public boolean isLongClickable() {
        return this.isLongClickable;
    }

    public void setLongClickable(boolean z) {
        this.isLongClickable = z;
    }

    public boolean isPluralLineSupported() {
        return this.isMultiline;
    }

    public void setPluralLineSupported(boolean z) {
        this.isMultiline = z;
    }

    public boolean isPassword() {
        return this.isPassword;
    }

    public void setPassword(boolean z) {
        this.isPassword = z;
    }

    public boolean isScrollable() {
        return this.isScrollable;
    }

    public void setScrollable(boolean z) {
        this.isScrollable = z;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean z) {
        this.isSelected = z;
    }

    public boolean isGivingHint() {
        return this.isShowingHintText;
    }

    public void setHinting(boolean z) {
        this.isShowingHintText = z;
    }

    public boolean isVisible() {
        return this.isVisibleToUser;
    }

    public void setVisible(boolean z) {
        this.isVisibleToUser = z;
    }
}

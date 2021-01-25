package ohos.accessibility.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.accessibility.adapter.ability.AccessibilitySearchAdapter;
import ohos.agp.utils.Rect;

public class AccessibilityInfo {
    public static final int FOCUS_TYPE_ACCESSIBILITY = 2;
    public static final int FOCUS_TYPE_INPUT = 1;
    private static final int UNDEFINED_ID = -1;
    private long accessibilityId;
    private List<Long> childIds = new ArrayList();
    private String componentResourceId;
    private int connectionId = -1;
    private CharSequence content;
    private CharSequence description;
    private CharSequence packageName;
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

    public CharSequence getPackageName() {
        return this.packageName;
    }

    public CharSequence getDescription() {
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

    public void setPackageName(CharSequence charSequence) {
        this.packageName = charSequence;
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
}

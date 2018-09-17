package android.view.accessibility;

import android.os.Parcelable;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class AccessibilityRecord {
    private static final int GET_SOURCE_PREFETCH_FLAGS = 7;
    private static final int MAX_POOL_SIZE = 10;
    private static final int PROPERTY_CHECKED = 1;
    private static final int PROPERTY_ENABLED = 2;
    private static final int PROPERTY_FULL_SCREEN = 128;
    private static final int PROPERTY_IMPORTANT_FOR_ACCESSIBILITY = 512;
    private static final int PROPERTY_PASSWORD = 4;
    private static final int PROPERTY_SCROLLABLE = 256;
    private static final int UNDEFINED = -1;
    private static AccessibilityRecord sPool;
    private static final Object sPoolLock = new Object();
    private static int sPoolSize;
    int mAddedCount = -1;
    CharSequence mBeforeText;
    int mBooleanProperties = 0;
    CharSequence mClassName;
    int mConnectionId = -1;
    CharSequence mContentDescription;
    int mCurrentItemIndex = -1;
    int mFromIndex = -1;
    private boolean mIsInPool;
    int mItemCount = -1;
    int mMaxScrollX = -1;
    int mMaxScrollY = -1;
    private AccessibilityRecord mNext;
    Parcelable mParcelableData;
    int mRemovedCount = -1;
    int mScrollX = -1;
    int mScrollY = -1;
    boolean mSealed;
    AccessibilityNodeInfo mSourceNode;
    int mSourceWindowId = -1;
    final List<CharSequence> mText = new ArrayList();
    int mToIndex = -1;

    AccessibilityRecord() {
    }

    public void setSource(View source) {
        setSource(source, -1);
    }

    public void setSource(View root, int virtualDescendantId) {
        enforceNotSealed();
        boolean important = true;
        this.mSourceWindowId = -1;
        clearSourceNode();
        if (root != null) {
            if (virtualDescendantId == -1 || virtualDescendantId == Integer.MAX_VALUE || virtualDescendantId == -1) {
                important = root.isImportantForAccessibility();
                this.mSourceNode = root.createAccessibilityNodeInfo();
            } else {
                AccessibilityNodeProvider provider = root.getAccessibilityNodeProvider();
                if (provider != null) {
                    this.mSourceNode = provider.createAccessibilityNodeInfo(virtualDescendantId);
                }
            }
            this.mSourceWindowId = root.getAccessibilityWindowId();
        }
        setBooleanProperty(512, important);
    }

    public void setSource(AccessibilityNodeInfo info) {
        enforceNotSealed();
        clearSourceNode();
        this.mSourceWindowId = -1;
        if (info != null) {
            this.mSourceNode = AccessibilityNodeInfo.obtain(info);
            setBooleanProperty(512, this.mSourceNode.isImportantForAccessibility());
            this.mSourceWindowId = info.getWindowId();
        }
    }

    public AccessibilityNodeInfo getSource() {
        enforceSealed();
        if (this.mSourceNode != null) {
            return AccessibilityNodeInfo.obtain(this.mSourceNode);
        }
        return null;
    }

    public void setWindowId(int windowId) {
        this.mSourceWindowId = windowId;
    }

    public int getWindowId() {
        return this.mSourceWindowId;
    }

    public boolean isChecked() {
        return getBooleanProperty(1);
    }

    public void setChecked(boolean isChecked) {
        enforceNotSealed();
        setBooleanProperty(1, isChecked);
    }

    public boolean isEnabled() {
        return getBooleanProperty(2);
    }

    public void setEnabled(boolean isEnabled) {
        enforceNotSealed();
        setBooleanProperty(2, isEnabled);
    }

    public boolean isPassword() {
        return getBooleanProperty(4);
    }

    public void setPassword(boolean isPassword) {
        enforceNotSealed();
        setBooleanProperty(4, isPassword);
    }

    public boolean isFullScreen() {
        return getBooleanProperty(128);
    }

    public void setFullScreen(boolean isFullScreen) {
        enforceNotSealed();
        setBooleanProperty(128, isFullScreen);
    }

    public boolean isScrollable() {
        return getBooleanProperty(256);
    }

    public void setScrollable(boolean scrollable) {
        enforceNotSealed();
        setBooleanProperty(256, scrollable);
    }

    public boolean isImportantForAccessibility() {
        return getBooleanProperty(512);
    }

    public int getItemCount() {
        return this.mItemCount;
    }

    public void setItemCount(int itemCount) {
        enforceNotSealed();
        this.mItemCount = itemCount;
    }

    public int getCurrentItemIndex() {
        return this.mCurrentItemIndex;
    }

    public void setCurrentItemIndex(int currentItemIndex) {
        enforceNotSealed();
        this.mCurrentItemIndex = currentItemIndex;
    }

    public int getFromIndex() {
        return this.mFromIndex;
    }

    public void setFromIndex(int fromIndex) {
        enforceNotSealed();
        this.mFromIndex = fromIndex;
    }

    public int getToIndex() {
        return this.mToIndex;
    }

    public void setToIndex(int toIndex) {
        enforceNotSealed();
        this.mToIndex = toIndex;
    }

    public int getScrollX() {
        return this.mScrollX;
    }

    public void setScrollX(int scrollX) {
        enforceNotSealed();
        this.mScrollX = scrollX;
    }

    public int getScrollY() {
        return this.mScrollY;
    }

    public void setScrollY(int scrollY) {
        enforceNotSealed();
        this.mScrollY = scrollY;
    }

    public int getMaxScrollX() {
        return this.mMaxScrollX;
    }

    public void setMaxScrollX(int maxScrollX) {
        enforceNotSealed();
        this.mMaxScrollX = maxScrollX;
    }

    public int getMaxScrollY() {
        return this.mMaxScrollY;
    }

    public void setMaxScrollY(int maxScrollY) {
        enforceNotSealed();
        this.mMaxScrollY = maxScrollY;
    }

    public int getAddedCount() {
        return this.mAddedCount;
    }

    public void setAddedCount(int addedCount) {
        enforceNotSealed();
        this.mAddedCount = addedCount;
    }

    public int getRemovedCount() {
        return this.mRemovedCount;
    }

    public void setRemovedCount(int removedCount) {
        enforceNotSealed();
        this.mRemovedCount = removedCount;
    }

    public CharSequence getClassName() {
        return this.mClassName;
    }

    public void setClassName(CharSequence className) {
        enforceNotSealed();
        this.mClassName = className;
    }

    public List<CharSequence> getText() {
        return this.mText;
    }

    public CharSequence getBeforeText() {
        return this.mBeforeText;
    }

    public void setBeforeText(CharSequence beforeText) {
        CharSequence charSequence = null;
        enforceNotSealed();
        if (beforeText != null) {
            charSequence = beforeText.subSequence(0, beforeText.length());
        }
        this.mBeforeText = charSequence;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public void setContentDescription(CharSequence contentDescription) {
        CharSequence charSequence = null;
        enforceNotSealed();
        if (contentDescription != null) {
            charSequence = contentDescription.subSequence(0, contentDescription.length());
        }
        this.mContentDescription = charSequence;
    }

    public Parcelable getParcelableData() {
        return this.mParcelableData;
    }

    public void setParcelableData(Parcelable parcelableData) {
        enforceNotSealed();
        this.mParcelableData = parcelableData;
    }

    public long getSourceNodeId() {
        return this.mSourceNode != null ? this.mSourceNode.getSourceNodeId() : -1;
    }

    public void setConnectionId(int connectionId) {
        enforceNotSealed();
        this.mConnectionId = connectionId;
        if (this.mSourceNode != null) {
            this.mSourceNode.setConnectionId(this.mConnectionId);
        }
    }

    public void setSealed(boolean sealed) {
        this.mSealed = sealed;
        if (this.mSourceNode != null) {
            this.mSourceNode.setSealed(sealed);
        }
    }

    boolean isSealed() {
        return this.mSealed;
    }

    void enforceSealed() {
        if (!isSealed()) {
            throw new IllegalStateException("Cannot perform this action on a not sealed instance.");
        }
    }

    void enforceNotSealed() {
        if (isSealed()) {
            throw new IllegalStateException("Cannot perform this action on a sealed instance.");
        }
    }

    private boolean getBooleanProperty(int property) {
        return (this.mBooleanProperties & property) == property;
    }

    private void setBooleanProperty(int property, boolean value) {
        if (value) {
            this.mBooleanProperties |= property;
        } else {
            this.mBooleanProperties &= ~property;
        }
    }

    public static AccessibilityRecord obtain(AccessibilityRecord record) {
        AccessibilityRecord clone = obtain();
        clone.init(record);
        return clone;
    }

    public static AccessibilityRecord obtain() {
        synchronized (sPoolLock) {
            if (sPool != null) {
                AccessibilityRecord record = sPool;
                sPool = sPool.mNext;
                sPoolSize--;
                record.mNext = null;
                record.mIsInPool = false;
                return record;
            }
            AccessibilityRecord accessibilityRecord = new AccessibilityRecord();
            return accessibilityRecord;
        }
    }

    public void recycle() {
        if (this.mIsInPool) {
            throw new IllegalStateException("Record already recycled!");
        }
        clear();
        synchronized (sPoolLock) {
            if (sPoolSize <= 10) {
                this.mNext = sPool;
                sPool = this;
                this.mIsInPool = true;
                sPoolSize++;
            }
        }
    }

    void init(AccessibilityRecord record) {
        this.mSealed = record.mSealed;
        this.mBooleanProperties = record.mBooleanProperties;
        this.mCurrentItemIndex = record.mCurrentItemIndex;
        this.mItemCount = record.mItemCount;
        this.mFromIndex = record.mFromIndex;
        this.mToIndex = record.mToIndex;
        this.mScrollX = record.mScrollX;
        this.mScrollY = record.mScrollY;
        this.mMaxScrollX = record.mMaxScrollX;
        this.mMaxScrollY = record.mMaxScrollY;
        this.mAddedCount = record.mAddedCount;
        this.mRemovedCount = record.mRemovedCount;
        this.mClassName = record.mClassName;
        this.mContentDescription = record.mContentDescription;
        this.mBeforeText = record.mBeforeText;
        this.mParcelableData = record.mParcelableData;
        this.mText.addAll(record.mText);
        this.mSourceWindowId = record.mSourceWindowId;
        if (record.mSourceNode != null) {
            this.mSourceNode = AccessibilityNodeInfo.obtain(record.mSourceNode);
        }
        this.mConnectionId = record.mConnectionId;
    }

    void clear() {
        this.mSealed = false;
        this.mBooleanProperties = 0;
        this.mCurrentItemIndex = -1;
        this.mItemCount = -1;
        this.mFromIndex = -1;
        this.mToIndex = -1;
        this.mScrollX = -1;
        this.mScrollY = -1;
        this.mMaxScrollX = -1;
        this.mMaxScrollY = -1;
        this.mAddedCount = -1;
        this.mRemovedCount = -1;
        this.mClassName = null;
        this.mContentDescription = null;
        this.mBeforeText = null;
        this.mParcelableData = null;
        this.mText.clear();
        clearSourceNode();
        this.mSourceWindowId = -1;
        this.mConnectionId = -1;
    }

    private void clearSourceNode() {
        if (this.mSourceNode != null) {
            this.mSourceNode.recycle();
            this.mSourceNode = null;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" [ ClassName: ").append(this.mClassName);
        builder.append("; Text: ").append(this.mText);
        builder.append("; ContentDescription: ").append(this.mContentDescription);
        builder.append("; ItemCount: ").append(this.mItemCount);
        builder.append("; CurrentItemIndex: ").append(this.mCurrentItemIndex);
        builder.append("; IsEnabled: ").append(getBooleanProperty(2));
        builder.append("; IsPassword: ").append(getBooleanProperty(4));
        builder.append("; IsChecked: ").append(getBooleanProperty(1));
        builder.append("; IsFullScreen: ").append(getBooleanProperty(128));
        builder.append("; Scrollable: ").append(getBooleanProperty(256));
        builder.append("; BeforeText: ").append(this.mBeforeText);
        builder.append("; FromIndex: ").append(this.mFromIndex);
        builder.append("; ToIndex: ").append(this.mToIndex);
        builder.append("; ScrollX: ").append(this.mScrollX);
        builder.append("; ScrollY: ").append(this.mScrollY);
        builder.append("; MaxScrollX: ").append(this.mMaxScrollX);
        builder.append("; MaxScrollY: ").append(this.mMaxScrollY);
        builder.append("; AddedCount: ").append(this.mAddedCount);
        builder.append("; RemovedCount: ").append(this.mRemovedCount);
        builder.append("; ParcelableData: ").append(this.mParcelableData);
        builder.append(" ]");
        return builder.toString();
    }
}

package android.view.accessibility;

import android.os.Parcelable;
import android.view.View;
import com.android.internal.os.HwBootFail;
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
    private static final Object sPoolLock = null;
    private static int sPoolSize;
    int mAddedCount;
    CharSequence mBeforeText;
    int mBooleanProperties;
    CharSequence mClassName;
    int mConnectionId;
    CharSequence mContentDescription;
    int mCurrentItemIndex;
    int mFromIndex;
    private boolean mIsInPool;
    int mItemCount;
    int mMaxScrollX;
    int mMaxScrollY;
    private AccessibilityRecord mNext;
    Parcelable mParcelableData;
    int mRemovedCount;
    int mScrollX;
    int mScrollY;
    boolean mSealed;
    AccessibilityNodeInfo mSourceNode;
    int mSourceWindowId;
    final List<CharSequence> mText;
    int mToIndex;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityRecord.<clinit>():void");
    }

    private void setBooleanProperty(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityRecord.setBooleanProperty(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityRecord.setBooleanProperty(int, boolean):void");
    }

    AccessibilityRecord() {
        this.mBooleanProperties = 0;
        this.mCurrentItemIndex = UNDEFINED;
        this.mItemCount = UNDEFINED;
        this.mFromIndex = UNDEFINED;
        this.mToIndex = UNDEFINED;
        this.mScrollX = UNDEFINED;
        this.mScrollY = UNDEFINED;
        this.mMaxScrollX = UNDEFINED;
        this.mMaxScrollY = UNDEFINED;
        this.mAddedCount = UNDEFINED;
        this.mRemovedCount = UNDEFINED;
        this.mSourceWindowId = UNDEFINED;
        this.mText = new ArrayList();
        this.mConnectionId = UNDEFINED;
    }

    public void setSource(View source) {
        setSource(source, UNDEFINED);
    }

    public void setSource(View root, int virtualDescendantId) {
        enforceNotSealed();
        boolean important = true;
        this.mSourceWindowId = UNDEFINED;
        clearSourceNode();
        if (root != null) {
            if (virtualDescendantId == UNDEFINED || virtualDescendantId == HwBootFail.STAGE_BOOT_SUCCESS) {
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
        setBooleanProperty(PROPERTY_IMPORTANT_FOR_ACCESSIBILITY, important);
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
        return getBooleanProperty(PROPERTY_CHECKED);
    }

    public void setChecked(boolean isChecked) {
        enforceNotSealed();
        setBooleanProperty(PROPERTY_CHECKED, isChecked);
    }

    public boolean isEnabled() {
        return getBooleanProperty(PROPERTY_ENABLED);
    }

    public void setEnabled(boolean isEnabled) {
        enforceNotSealed();
        setBooleanProperty(PROPERTY_ENABLED, isEnabled);
    }

    public boolean isPassword() {
        return getBooleanProperty(PROPERTY_PASSWORD);
    }

    public void setPassword(boolean isPassword) {
        enforceNotSealed();
        setBooleanProperty(PROPERTY_PASSWORD, isPassword);
    }

    public boolean isFullScreen() {
        return getBooleanProperty(PROPERTY_FULL_SCREEN);
    }

    public void setFullScreen(boolean isFullScreen) {
        enforceNotSealed();
        setBooleanProperty(PROPERTY_FULL_SCREEN, isFullScreen);
    }

    public boolean isScrollable() {
        return getBooleanProperty(PROPERTY_SCROLLABLE);
    }

    public void setScrollable(boolean scrollable) {
        enforceNotSealed();
        setBooleanProperty(PROPERTY_SCROLLABLE, scrollable);
    }

    public boolean isImportantForAccessibility() {
        return getBooleanProperty(PROPERTY_IMPORTANT_FOR_ACCESSIBILITY);
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
        enforceNotSealed();
        this.mBeforeText = beforeText;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public void setContentDescription(CharSequence contentDescription) {
        enforceNotSealed();
        this.mContentDescription = contentDescription;
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
                sPoolSize += UNDEFINED;
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
            if (sPoolSize <= MAX_POOL_SIZE) {
                this.mNext = sPool;
                sPool = this;
                this.mIsInPool = true;
                sPoolSize += PROPERTY_CHECKED;
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
        this.mCurrentItemIndex = UNDEFINED;
        this.mItemCount = UNDEFINED;
        this.mFromIndex = UNDEFINED;
        this.mToIndex = UNDEFINED;
        this.mScrollX = UNDEFINED;
        this.mScrollY = UNDEFINED;
        this.mMaxScrollX = UNDEFINED;
        this.mMaxScrollY = UNDEFINED;
        this.mAddedCount = UNDEFINED;
        this.mRemovedCount = UNDEFINED;
        this.mClassName = null;
        this.mContentDescription = null;
        this.mBeforeText = null;
        this.mParcelableData = null;
        this.mText.clear();
        clearSourceNode();
        this.mSourceWindowId = UNDEFINED;
        this.mConnectionId = UNDEFINED;
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
        builder.append("; IsEnabled: ").append(getBooleanProperty(PROPERTY_ENABLED));
        builder.append("; IsPassword: ").append(getBooleanProperty(PROPERTY_PASSWORD));
        builder.append("; IsChecked: ").append(getBooleanProperty(PROPERTY_CHECKED));
        builder.append("; IsFullScreen: ").append(getBooleanProperty(PROPERTY_FULL_SCREEN));
        builder.append("; Scrollable: ").append(getBooleanProperty(PROPERTY_SCROLLABLE));
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

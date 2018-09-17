package android.view.accessibility;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LongArray;
import android.util.Pools.SynchronizedPool;

public final class AccessibilityWindowInfo implements Parcelable {
    private static final int BOOLEAN_PROPERTY_ACCESSIBILITY_FOCUSED = 4;
    private static final int BOOLEAN_PROPERTY_ACTIVE = 1;
    private static final int BOOLEAN_PROPERTY_FOCUSED = 2;
    public static final Creator<AccessibilityWindowInfo> CREATOR = null;
    private static final boolean DEBUG = false;
    private static final int MAX_POOL_SIZE = 10;
    public static final int TYPE_ACCESSIBILITY_OVERLAY = 4;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_INPUT_METHOD = 2;
    public static final int TYPE_SPLIT_SCREEN_DIVIDER = 5;
    public static final int TYPE_SYSTEM = 3;
    private static final int UNDEFINED = -1;
    private static final SynchronizedPool<AccessibilityWindowInfo> sPool = null;
    private int mAnchorId;
    private int mBooleanProperties;
    private final Rect mBoundsInScreen;
    private LongArray mChildIds;
    private int mConnectionId;
    private int mId;
    private int mLayer;
    private int mParentId;
    private CharSequence mTitle;
    private int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityWindowInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityWindowInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityWindowInfo.<clinit>():void");
    }

    private void setBooleanProperty(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityWindowInfo.setBooleanProperty(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityWindowInfo.setBooleanProperty(int, boolean):void");
    }

    private AccessibilityWindowInfo() {
        this.mType = UNDEFINED;
        this.mLayer = UNDEFINED;
        this.mId = UNDEFINED;
        this.mParentId = UNDEFINED;
        this.mBoundsInScreen = new Rect();
        this.mAnchorId = UNDEFINED;
        this.mConnectionId = UNDEFINED;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getLayer() {
        return this.mLayer;
    }

    public void setLayer(int layer) {
        this.mLayer = layer;
    }

    public AccessibilityNodeInfo getRoot() {
        if (this.mConnectionId == UNDEFINED) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mId, AccessibilityNodeInfo.ROOT_NODE_ID, true, TYPE_ACCESSIBILITY_OVERLAY);
    }

    public void setAnchorId(int anchorId) {
        this.mAnchorId = anchorId;
    }

    public AccessibilityNodeInfo getAnchor() {
        if (this.mConnectionId == UNDEFINED || this.mAnchorId == UNDEFINED || this.mParentId == UNDEFINED) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mParentId, (long) this.mAnchorId, true, 0);
    }

    public AccessibilityWindowInfo getParent() {
        if (this.mConnectionId == UNDEFINED || this.mParentId == UNDEFINED) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().getWindow(this.mConnectionId, this.mParentId);
    }

    public void setParentId(int parentId) {
        this.mParentId = parentId;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public void setConnectionId(int connectionId) {
        this.mConnectionId = connectionId;
    }

    public void getBoundsInScreen(Rect outBounds) {
        outBounds.set(this.mBoundsInScreen);
    }

    public void setBoundsInScreen(Rect bounds) {
        this.mBoundsInScreen.set(bounds);
    }

    public boolean isActive() {
        return getBooleanProperty(TYPE_APPLICATION);
    }

    public void setActive(boolean active) {
        setBooleanProperty(TYPE_APPLICATION, active);
    }

    public boolean isFocused() {
        return getBooleanProperty(TYPE_INPUT_METHOD);
    }

    public void setFocused(boolean focused) {
        setBooleanProperty(TYPE_INPUT_METHOD, focused);
    }

    public boolean isAccessibilityFocused() {
        return getBooleanProperty(TYPE_ACCESSIBILITY_OVERLAY);
    }

    public void setAccessibilityFocused(boolean focused) {
        setBooleanProperty(TYPE_ACCESSIBILITY_OVERLAY, focused);
    }

    public int getChildCount() {
        return this.mChildIds != null ? this.mChildIds.size() : 0;
    }

    public AccessibilityWindowInfo getChild(int index) {
        if (this.mChildIds == null) {
            throw new IndexOutOfBoundsException();
        } else if (this.mConnectionId == UNDEFINED) {
            return null;
        } else {
            return AccessibilityInteractionClient.getInstance().getWindow(this.mConnectionId, (int) this.mChildIds.get(index));
        }
    }

    public void addChild(int childId) {
        if (this.mChildIds == null) {
            this.mChildIds = new LongArray();
        }
        this.mChildIds.add((long) childId);
    }

    public static AccessibilityWindowInfo obtain() {
        AccessibilityWindowInfo info = (AccessibilityWindowInfo) sPool.acquire();
        if (info == null) {
            return new AccessibilityWindowInfo();
        }
        return info;
    }

    public static AccessibilityWindowInfo obtain(AccessibilityWindowInfo info) {
        AccessibilityWindowInfo infoClone = obtain();
        infoClone.mType = info.mType;
        infoClone.mLayer = info.mLayer;
        infoClone.mBooleanProperties = info.mBooleanProperties;
        infoClone.mId = info.mId;
        infoClone.mParentId = info.mParentId;
        infoClone.mBoundsInScreen.set(info.mBoundsInScreen);
        infoClone.mTitle = info.mTitle;
        infoClone.mAnchorId = info.mAnchorId;
        if (info.mChildIds != null && info.mChildIds.size() > 0) {
            if (infoClone.mChildIds == null) {
                infoClone.mChildIds = info.mChildIds.clone();
            } else {
                infoClone.mChildIds.addAll(info.mChildIds);
            }
        }
        infoClone.mConnectionId = info.mConnectionId;
        return infoClone;
    }

    public void recycle() {
        clear();
        sPool.release(this);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mType);
        parcel.writeInt(this.mLayer);
        parcel.writeInt(this.mBooleanProperties);
        parcel.writeInt(this.mId);
        parcel.writeInt(this.mParentId);
        this.mBoundsInScreen.writeToParcel(parcel, flags);
        parcel.writeCharSequence(this.mTitle);
        parcel.writeInt(this.mAnchorId);
        LongArray childIds = this.mChildIds;
        if (childIds == null) {
            parcel.writeInt(0);
        } else {
            int childCount = childIds.size();
            parcel.writeInt(childCount);
            for (int i = 0; i < childCount; i += TYPE_APPLICATION) {
                parcel.writeInt((int) childIds.get(i));
            }
        }
        parcel.writeInt(this.mConnectionId);
    }

    private void initFromParcel(Parcel parcel) {
        this.mType = parcel.readInt();
        this.mLayer = parcel.readInt();
        this.mBooleanProperties = parcel.readInt();
        this.mId = parcel.readInt();
        this.mParentId = parcel.readInt();
        this.mBoundsInScreen.readFromParcel(parcel);
        this.mTitle = parcel.readCharSequence();
        this.mAnchorId = parcel.readInt();
        int childCount = parcel.readInt();
        if (childCount > 0) {
            if (this.mChildIds == null) {
                this.mChildIds = new LongArray(childCount);
            }
            for (int i = 0; i < childCount; i += TYPE_APPLICATION) {
                this.mChildIds.add((long) parcel.readInt());
            }
        }
        this.mConnectionId = parcel.readInt();
    }

    public int hashCode() {
        return this.mId;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return DEBUG;
        }
        if (this.mId != ((AccessibilityWindowInfo) obj).mId) {
            z = DEBUG;
        }
        return z;
    }

    public String toString() {
        boolean z;
        boolean z2 = DEBUG;
        StringBuilder builder = new StringBuilder();
        builder.append("AccessibilityWindowInfo[");
        builder.append("title=").append(this.mTitle);
        builder.append("id=").append(this.mId);
        builder.append(", type=").append(typeToString(this.mType));
        builder.append(", layer=").append(this.mLayer);
        builder.append(", bounds=").append(this.mBoundsInScreen);
        builder.append(", focused=").append(isFocused());
        builder.append(", active=").append(isActive());
        builder.append(", hasParent=").append(this.mParentId != UNDEFINED ? true : DEBUG);
        StringBuilder append = builder.append(", isAnchored=");
        if (this.mAnchorId != UNDEFINED) {
            z = true;
        } else {
            z = DEBUG;
        }
        append.append(z);
        StringBuilder append2 = builder.append(", hasChildren=");
        if (this.mChildIds != null && this.mChildIds.size() > 0) {
            z2 = true;
        }
        append2.append(z2);
        builder.append(']');
        return builder.toString();
    }

    private void clear() {
        this.mType = UNDEFINED;
        this.mLayer = UNDEFINED;
        this.mBooleanProperties = 0;
        this.mId = UNDEFINED;
        this.mParentId = UNDEFINED;
        this.mBoundsInScreen.setEmpty();
        if (this.mChildIds != null) {
            this.mChildIds.clear();
        }
        this.mConnectionId = UNDEFINED;
        this.mAnchorId = UNDEFINED;
        this.mTitle = null;
    }

    private boolean getBooleanProperty(int property) {
        return (this.mBooleanProperties & property) != 0 ? true : DEBUG;
    }

    private static String typeToString(int type) {
        switch (type) {
            case TYPE_APPLICATION /*1*/:
                return "TYPE_APPLICATION";
            case TYPE_INPUT_METHOD /*2*/:
                return "TYPE_INPUT_METHOD";
            case TYPE_SYSTEM /*3*/:
                return "TYPE_SYSTEM";
            case TYPE_ACCESSIBILITY_OVERLAY /*4*/:
                return "TYPE_ACCESSIBILITY_OVERLAY";
            case TYPE_SPLIT_SCREEN_DIVIDER /*5*/:
                return "TYPE_SPLIT_SCREEN_DIVIDER";
            default:
                return "<UNKNOWN>";
        }
    }

    public boolean changed(AccessibilityWindowInfo other) {
        if (other.mId != this.mId) {
            throw new IllegalArgumentException("Not same window.");
        } else if (other.mType != this.mType) {
            throw new IllegalArgumentException("Not same type.");
        } else if (!this.mBoundsInScreen.equals(other.mBoundsInScreen) || this.mLayer != other.mLayer || this.mBooleanProperties != other.mBooleanProperties || this.mParentId != other.mParentId) {
            return true;
        } else {
            if (this.mChildIds == null) {
                if (other.mChildIds != null) {
                    return true;
                }
            } else if (!this.mChildIds.equals(other.mChildIds)) {
                return true;
            }
            return DEBUG;
        }
    }
}

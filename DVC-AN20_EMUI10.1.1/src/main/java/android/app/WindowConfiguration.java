package android.app;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.proto.ProtoInputStream;
import android.util.proto.ProtoOutputStream;
import android.util.proto.WireTypeMismatchException;
import android.view.Surface;
import java.io.IOException;

public class WindowConfiguration implements Parcelable, Comparable<WindowConfiguration> {
    public static final int ACTIVITY_TYPE_ASSISTANT = 4;
    public static final int ACTIVITY_TYPE_HOME = 2;
    public static final int ACTIVITY_TYPE_RECENTS = 3;
    public static final int ACTIVITY_TYPE_STANDARD = 1;
    public static final int ACTIVITY_TYPE_UNDEFINED = 0;
    private static final int ALWAYS_ON_TOP_OFF = 2;
    private static final int ALWAYS_ON_TOP_ON = 1;
    private static final int ALWAYS_ON_TOP_UNDEFINED = 0;
    public static final Parcelable.Creator<WindowConfiguration> CREATOR = new Parcelable.Creator<WindowConfiguration>() {
        /* class android.app.WindowConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WindowConfiguration createFromParcel(Parcel in) {
            return new WindowConfiguration(in);
        }

        @Override // android.os.Parcelable.Creator
        public WindowConfiguration[] newArray(int size) {
            return new WindowConfiguration[size];
        }
    };
    public static final int HWPC_WINDOWING_MODE_FREEFORM = 10;
    private static final int HW_MULTI_WINDOWING_MODE_FISRT = 99;
    public static final int HW_MULTI_WINDOWING_MODE_FREEFORM = 102;
    public static final int HW_MULTI_WINDOWING_MODE_MAGIC = 103;
    public static final int HW_MULTI_WINDOWING_MODE_PRIMARY = 100;
    public static final int HW_MULTI_WINDOWING_MODE_SECONDARY = 101;
    public static final int PINNED_WINDOWING_MODE_ELEVATION_IN_DIP = 5;
    public static final int ROTATION_UNDEFINED = -1;
    public static final int WINDOWING_MODE_COORDINATION_PRIMARY = 11;
    public static final int WINDOWING_MODE_COORDINATION_SECONDARY = 12;
    public static final int WINDOWING_MODE_FREEFORM = 5;
    public static final int WINDOWING_MODE_FULLSCREEN = 1;
    public static final int WINDOWING_MODE_FULLSCREEN_OR_SPLIT_SCREEN_SECONDARY = 4;
    public static final int WINDOWING_MODE_PINNED = 2;
    public static final int WINDOWING_MODE_SPLIT_SCREEN_PRIMARY = 3;
    public static final int WINDOWING_MODE_SPLIT_SCREEN_SECONDARY = 4;
    public static final int WINDOWING_MODE_UNDEFINED = 0;
    public static final int WINDOW_CONFIG_ACTIVITY_TYPE = 8;
    public static final int WINDOW_CONFIG_ALWAYS_ON_TOP = 16;
    public static final int WINDOW_CONFIG_APP_BOUNDS = 2;
    public static final int WINDOW_CONFIG_BOUNDS = 1;
    public static final int WINDOW_CONFIG_DISPLAY_WINDOWING_MODE = 64;
    public static final int WINDOW_CONFIG_ROTATION = 32;
    public static final int WINDOW_CONFIG_WINDOWING_MODE = 4;
    @ActivityType
    private int mActivityType;
    @AlwaysOnTop
    private int mAlwaysOnTop;
    private Rect mAppBounds;
    private Rect mBounds;
    @WindowingMode
    private int mDisplayWindowingMode;
    private int mRotation;
    @WindowingMode
    private int mWindowingMode;

    public @interface ActivityType {
    }

    private @interface AlwaysOnTop {
    }

    public @interface WindowConfig {
    }

    public @interface WindowingMode {
    }

    public WindowConfiguration() {
        this.mBounds = new Rect();
        this.mRotation = -1;
        unset();
    }

    public WindowConfiguration(WindowConfiguration configuration) {
        this.mBounds = new Rect();
        this.mRotation = -1;
        setTo(configuration);
    }

    private WindowConfiguration(Parcel in) {
        this.mBounds = new Rect();
        this.mRotation = -1;
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mBounds, flags);
        dest.writeParcelable(this.mAppBounds, flags);
        dest.writeInt(this.mWindowingMode);
        dest.writeInt(this.mActivityType);
        dest.writeInt(this.mAlwaysOnTop);
        dest.writeInt(this.mRotation);
        dest.writeInt(this.mDisplayWindowingMode);
    }

    private void readFromParcel(Parcel source) {
        this.mBounds = (Rect) source.readParcelable(Rect.class.getClassLoader());
        this.mAppBounds = (Rect) source.readParcelable(Rect.class.getClassLoader());
        this.mWindowingMode = source.readInt();
        this.mActivityType = source.readInt();
        this.mAlwaysOnTop = source.readInt();
        this.mRotation = source.readInt();
        this.mDisplayWindowingMode = source.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void setBounds(Rect rect) {
        if (rect == null) {
            this.mBounds.setEmpty();
        } else {
            this.mBounds.set(rect);
        }
    }

    public void setAppBounds(Rect rect) {
        if (rect == null) {
            this.mAppBounds = null;
        } else {
            setAppBounds(rect.left, rect.top, rect.right, rect.bottom);
        }
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.mAlwaysOnTop = alwaysOnTop ? 1 : 2;
    }

    private void setAlwaysOnTop(@AlwaysOnTop int alwaysOnTop) {
        this.mAlwaysOnTop = alwaysOnTop;
    }

    public void setAppBounds(int left, int top, int right, int bottom) {
        if (this.mAppBounds == null) {
            this.mAppBounds = new Rect();
        }
        this.mAppBounds.set(left, top, right, bottom);
    }

    public Rect getAppBounds() {
        return this.mAppBounds;
    }

    public Rect getBounds() {
        return this.mBounds;
    }

    public int getRotation() {
        return this.mRotation;
    }

    public void setRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void setWindowingMode(@WindowingMode int windowingMode) {
        this.mWindowingMode = windowingMode;
    }

    @WindowingMode
    public int getWindowingMode() {
        return this.mWindowingMode;
    }

    public void setDisplayWindowingMode(@WindowingMode int windowingMode) {
        this.mDisplayWindowingMode = windowingMode;
    }

    public void setActivityType(@ActivityType int activityType) {
        if (this.mActivityType != activityType) {
            if (!ActivityThread.isSystem() || this.mActivityType == 0 || activityType == 0) {
                this.mActivityType = activityType;
                return;
            }
            throw new IllegalStateException("Can't change activity type once set: " + this + " activityType=" + activityTypeToString(activityType));
        }
    }

    @ActivityType
    public int getActivityType() {
        return this.mActivityType;
    }

    public void setTo(WindowConfiguration other) {
        setBounds(other.mBounds);
        setAppBounds(other.mAppBounds);
        setWindowingMode(other.mWindowingMode);
        setActivityType(other.mActivityType);
        setAlwaysOnTop(other.mAlwaysOnTop);
        setRotation(other.mRotation);
        setDisplayWindowingMode(other.mDisplayWindowingMode);
    }

    public void unset() {
        setToDefaults();
    }

    public void setToDefaults() {
        setAppBounds(null);
        setBounds(null);
        setWindowingMode(0);
        setActivityType(0);
        setAlwaysOnTop(0);
        setRotation(-1);
        setDisplayWindowingMode(0);
    }

    @WindowConfig
    public int updateFrom(WindowConfiguration delta) {
        int changed = 0;
        if (!delta.mBounds.isEmpty() && !delta.mBounds.equals(this.mBounds)) {
            changed = 0 | 1;
            setBounds(delta.mBounds);
        }
        Rect rect = delta.mAppBounds;
        if (rect != null && !rect.equals(this.mAppBounds)) {
            changed |= 2;
            setAppBounds(delta.mAppBounds);
        }
        int i = delta.mWindowingMode;
        if (!(i == 0 || this.mWindowingMode == i)) {
            changed |= 4;
            setWindowingMode(i);
        }
        int i2 = delta.mActivityType;
        if (!(i2 == 0 || this.mActivityType == i2)) {
            changed |= 8;
            setActivityType(i2);
        }
        int i3 = delta.mAlwaysOnTop;
        if (!(i3 == 0 || this.mAlwaysOnTop == i3)) {
            changed |= 16;
            setAlwaysOnTop(i3);
        }
        int i4 = delta.mRotation;
        if (!(i4 == -1 || i4 == this.mRotation)) {
            changed |= 32;
            setRotation(i4);
        }
        int i5 = delta.mDisplayWindowingMode;
        if (i5 == 0 || this.mDisplayWindowingMode == i5) {
            return changed;
        }
        int changed2 = changed | 64;
        setDisplayWindowingMode(i5);
        return changed2;
    }

    @WindowConfig
    public long diff(WindowConfiguration other, boolean compareUndefined) {
        Rect rect;
        Rect rect2;
        long changes = 0;
        if (!this.mBounds.equals(other.mBounds)) {
            changes = 0 | 1;
        }
        if ((compareUndefined || other.mAppBounds != null) && (rect = this.mAppBounds) != (rect2 = other.mAppBounds) && (rect == null || !rect.equals(rect2))) {
            changes |= 2;
        }
        if ((compareUndefined || other.mWindowingMode != 0) && this.mWindowingMode != other.mWindowingMode) {
            changes |= 4;
        }
        if ((compareUndefined || other.mActivityType != 0) && this.mActivityType != other.mActivityType) {
            changes |= 8;
        }
        if ((compareUndefined || other.mAlwaysOnTop != 0) && this.mAlwaysOnTop != other.mAlwaysOnTop) {
            changes |= 16;
        }
        if ((compareUndefined || other.mRotation != -1) && this.mRotation != other.mRotation) {
            changes |= 32;
        }
        if ((compareUndefined || other.mDisplayWindowingMode != 0) && this.mDisplayWindowingMode != other.mDisplayWindowingMode) {
            return changes | 64;
        }
        return changes;
    }

    public int compareTo(WindowConfiguration that) {
        if (this.mAppBounds == null && that.mAppBounds != null) {
            return 1;
        }
        if (this.mAppBounds != null && that.mAppBounds == null) {
            return -1;
        }
        Rect rect = this.mAppBounds;
        if (!(rect == null || that.mAppBounds == null)) {
            int n = rect.left - that.mAppBounds.left;
            if (n != 0) {
                return n;
            }
            int n2 = this.mAppBounds.top - that.mAppBounds.top;
            if (n2 != 0) {
                return n2;
            }
            int n3 = this.mAppBounds.right - that.mAppBounds.right;
            if (n3 != 0) {
                return n3;
            }
            int n4 = this.mAppBounds.bottom - that.mAppBounds.bottom;
            if (n4 != 0) {
                return n4;
            }
        }
        int n5 = this.mBounds.left - that.mBounds.left;
        if (n5 != 0) {
            return n5;
        }
        int n6 = this.mBounds.top - that.mBounds.top;
        if (n6 != 0) {
            return n6;
        }
        int n7 = this.mBounds.right - that.mBounds.right;
        if (n7 != 0) {
            return n7;
        }
        int n8 = this.mBounds.bottom - that.mBounds.bottom;
        if (n8 != 0) {
            return n8;
        }
        int n9 = this.mWindowingMode - that.mWindowingMode;
        if (n9 != 0) {
            return n9;
        }
        int n10 = this.mActivityType - that.mActivityType;
        if (n10 != 0) {
            return n10;
        }
        int n11 = this.mAlwaysOnTop - that.mAlwaysOnTop;
        if (n11 != 0) {
            return n11;
        }
        int n12 = this.mRotation - that.mRotation;
        if (n12 != 0) {
            return n12;
        }
        int n13 = this.mDisplayWindowingMode - that.mDisplayWindowingMode;
        if (n13 != 0) {
            return n13;
        }
        return n13;
    }

    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        if (that == this) {
            return true;
        }
        if ((that instanceof WindowConfiguration) && compareTo((WindowConfiguration) that) == 0) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = 0;
        Rect rect = this.mAppBounds;
        if (rect != null) {
            result = (0 * 31) + rect.hashCode();
        }
        return (((((((((((result * 31) + this.mBounds.hashCode()) * 31) + this.mWindowingMode) * 31) + this.mActivityType) * 31) + this.mAlwaysOnTop) * 31) + this.mRotation) * 31) + this.mDisplayWindowingMode;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("{ mBounds=");
        sb.append(this.mBounds);
        sb.append(" mAppBounds=");
        sb.append(this.mAppBounds);
        sb.append(" mWindowingMode=");
        sb.append(windowingModeToString(this.mWindowingMode));
        sb.append(" mDisplayWindowingMode=");
        sb.append(windowingModeToString(this.mDisplayWindowingMode));
        sb.append(" mActivityType=");
        sb.append(activityTypeToString(this.mActivityType));
        sb.append(" mAlwaysOnTop=");
        sb.append(alwaysOnTopToString(this.mAlwaysOnTop));
        sb.append(" mRotation=");
        int i = this.mRotation;
        if (i == -1) {
            str = "undefined";
        } else {
            str = Surface.rotationToString(i);
        }
        sb.append(str);
        sb.append("}");
        return sb.toString();
    }

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        long token = protoOutputStream.start(fieldId);
        Rect rect = this.mAppBounds;
        if (rect != null) {
            rect.writeToProto(protoOutputStream, 1146756268033L);
        }
        protoOutputStream.write(1120986464258L, this.mWindowingMode);
        protoOutputStream.write(1120986464259L, this.mActivityType);
        Rect rect2 = this.mBounds;
        if (rect2 != null) {
            rect2.writeToProto(protoOutputStream, 1146756268036L);
        }
        protoOutputStream.end(token);
    }

    public void readFromProto(ProtoInputStream proto, long fieldId) throws IOException, WireTypeMismatchException {
        long token = proto.start(fieldId);
        while (proto.nextField() != -1) {
            try {
                int fieldNumber = proto.getFieldNumber();
                if (fieldNumber == 1) {
                    this.mAppBounds = new Rect();
                    this.mAppBounds.readFromProto(proto, 1146756268033L);
                } else if (fieldNumber == 2) {
                    this.mWindowingMode = proto.readInt(1120986464258L);
                } else if (fieldNumber == 3) {
                    this.mActivityType = proto.readInt(1120986464259L);
                } else if (fieldNumber == 4) {
                    this.mBounds = new Rect();
                    this.mBounds.readFromProto(proto, 1146756268036L);
                }
            } finally {
                proto.end(token);
            }
        }
    }

    public boolean hasWindowShadow() {
        return tasksAreFloating();
    }

    public boolean hasWindowDecorCaption() {
        int i;
        if (this.mActivityType == 1 && ((i = this.mWindowingMode) == 5 || i == 10 || this.mDisplayWindowingMode == 5 || inHwMultiStackWindowingMode())) {
            return true;
        }
        return false;
    }

    public boolean canResizeTask() {
        int i = this.mWindowingMode;
        return i == 5 || i == 10;
    }

    public boolean persistTaskBounds() {
        int i = this.mWindowingMode;
        return i == 5 || i == 10;
    }

    public boolean tasksAreFloating() {
        return isFloating(this.mWindowingMode);
    }

    public static boolean isFloating(int windowingMode) {
        return windowingMode == 5 || windowingMode == 2 || windowingMode == 10 || windowingMode == 102 || windowingMode == 103;
    }

    public static boolean isSplitScreenWindowingMode(int windowingMode) {
        return windowingMode == 3 || windowingMode == 4;
    }

    public boolean canReceiveKeys() {
        return this.mWindowingMode != 2;
    }

    public boolean isAlwaysOnTop() {
        int i = this.mWindowingMode;
        if (i != 2) {
            return (i == 5 || i == 102) && this.mAlwaysOnTop == 1;
        }
        return true;
    }

    public boolean keepVisibleDeadAppWindowOnScreen() {
        return this.mWindowingMode != 2;
    }

    public boolean useWindowFrameForBackdrop() {
        int i = this.mWindowingMode;
        return i == 5 || i == 2 || i == 10;
    }

    public boolean windowsAreScaleable() {
        return this.mWindowingMode == 2;
    }

    public boolean hasMovementAnimations() {
        return this.mWindowingMode != 2;
    }

    public boolean supportSplitScreenWindowingMode() {
        return supportSplitScreenWindowingMode(this.mActivityType);
    }

    public static boolean supportSplitScreenWindowingMode(int activityType) {
        return activityType != 4;
    }

    public static String windowingModeToString(@WindowingMode int windowingMode) {
        if (windowingMode == 0) {
            return "undefined";
        }
        if (windowingMode == 1) {
            return "fullscreen";
        }
        if (windowingMode == 2) {
            return ContactsContract.ContactOptionsColumns.PINNED;
        }
        if (windowingMode == 3) {
            return "split-screen-primary";
        }
        if (windowingMode == 4) {
            return "split-screen-secondary";
        }
        if (windowingMode == 5) {
            return "freeform";
        }
        switch (windowingMode) {
            case 10:
                return "hw-pc-freeform";
            case 11:
                return "coordination_primary";
            case 12:
                return "coordination_secondary";
            default:
                switch (windowingMode) {
                    case 100:
                        return "hwMultiwindow-primary";
                    case 101:
                        return "hwMultiwindow-secondary";
                    case 102:
                        return "hwMultiwindow-freeform";
                    case 103:
                        return "hw-magic-windows";
                    default:
                        return String.valueOf(windowingMode);
                }
        }
    }

    public static String activityTypeToString(@ActivityType int applicationType) {
        if (applicationType == 0) {
            return "undefined";
        }
        if (applicationType == 1) {
            return "standard";
        }
        if (applicationType == 2) {
            return CalendarContract.CalendarCache.TIMEZONE_TYPE_HOME;
        }
        if (applicationType == 3) {
            return "recents";
        }
        if (applicationType != 4) {
            return String.valueOf(applicationType);
        }
        return Settings.Secure.ASSISTANT;
    }

    public static String alwaysOnTopToString(@AlwaysOnTop int alwaysOnTop) {
        if (alwaysOnTop == 0) {
            return "undefined";
        }
        if (alwaysOnTop == 1) {
            return Camera.Parameters.FLASH_MODE_ON;
        }
        if (alwaysOnTop != 2) {
            return String.valueOf(alwaysOnTop);
        }
        return "off";
    }

    public static boolean isHwSplitScreenWindowingMode(int windowingMode) {
        return windowingMode == 100 || windowingMode == 101;
    }

    public boolean inHwSplitScreenWindowingMode() {
        return isHwSplitScreenWindowingMode(this.mWindowingMode);
    }

    public static boolean isHwSplitScreenPrimaryWindowingMode(int windowingMode) {
        return windowingMode == 100;
    }

    public boolean inHwSplitScreenPrimaryWindowingMode() {
        return isHwSplitScreenPrimaryWindowingMode(this.mWindowingMode);
    }

    public static boolean isHwSplitScreenSecondaryWindowingMode(int windowingMode) {
        return windowingMode == 101;
    }

    public boolean inHwSplitScreenSecondaryWindowingMode() {
        return isHwSplitScreenSecondaryWindowingMode(this.mWindowingMode);
    }

    public static boolean isHwFreeFormWindowingMode(int windowingMode) {
        return windowingMode == 102;
    }

    public boolean inHwFreeFormWindowingMode() {
        return isHwFreeFormWindowingMode(this.mWindowingMode);
    }

    public static boolean isHwMultiWindowingMode(int windowingMode) {
        return windowingMode > 99;
    }

    public boolean inHwMultiWindowingMode() {
        return isHwMultiWindowingMode(this.mWindowingMode);
    }

    public static boolean isHwMultiStackWindowingMode(int windowingMode) {
        return windowingMode == 100 || windowingMode == 101 || windowingMode == 102;
    }

    public boolean inHwMultiStackWindowingMode() {
        return isHwMultiStackWindowingMode(this.mWindowingMode);
    }

    public static boolean isHwMagicWindowingMode(int windowingMode) {
        return windowingMode == 103;
    }

    public boolean inHwMagicWindowingMode() {
        return isHwMagicWindowingMode(this.mWindowingMode);
    }

    public static boolean isIncompatibleWindowingMode(int windowMode, int otherWindowMode) {
        return windowMode != otherWindowMode && (isHwMultiStackWindowingMode(windowMode) || isHwMultiStackWindowingMode(otherWindowMode));
    }
}

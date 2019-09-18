package android.app;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;

public class WindowConfiguration implements Parcelable, Comparable<WindowConfiguration> {
    public static final int ACTIVITY_TYPE_ASSISTANT = 4;
    public static final int ACTIVITY_TYPE_HOME = 2;
    public static final int ACTIVITY_TYPE_RECENTS = 3;
    public static final int ACTIVITY_TYPE_STANDARD = 1;
    public static final int ACTIVITY_TYPE_UNDEFINED = 0;
    public static final Parcelable.Creator<WindowConfiguration> CREATOR = new Parcelable.Creator<WindowConfiguration>() {
        public WindowConfiguration createFromParcel(Parcel in) {
            return new WindowConfiguration(in);
        }

        public WindowConfiguration[] newArray(int size) {
            return new WindowConfiguration[size];
        }
    };
    public static final int HWPC_WINDOWING_MODE_FREEFORM = 10;
    public static final int PINNED_WINDOWING_MODE_ELEVATION_IN_DIP = 5;
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
    public static final int WINDOW_CONFIG_APP_BOUNDS = 2;
    public static final int WINDOW_CONFIG_BOUNDS = 1;
    public static final int WINDOW_CONFIG_WINDOWING_MODE = 4;
    @ActivityType
    private int mActivityType;
    private Rect mAppBounds;
    private Rect mBounds;
    @WindowingMode
    private int mWindowingMode;

    public @interface ActivityType {
    }

    public @interface WindowConfig {
    }

    public @interface WindowingMode {
    }

    public WindowConfiguration() {
        this.mBounds = new Rect();
        unset();
    }

    public WindowConfiguration(WindowConfiguration configuration) {
        this.mBounds = new Rect();
        setTo(configuration);
    }

    private WindowConfiguration(Parcel in) {
        this.mBounds = new Rect();
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mBounds, flags);
        dest.writeParcelable(this.mAppBounds, flags);
        dest.writeInt(this.mWindowingMode);
        dest.writeInt(this.mActivityType);
    }

    private void readFromParcel(Parcel source) {
        this.mBounds = (Rect) source.readParcelable(Rect.class.getClassLoader());
        this.mAppBounds = (Rect) source.readParcelable(Rect.class.getClassLoader());
        this.mWindowingMode = source.readInt();
        this.mActivityType = source.readInt();
    }

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

    public void setWindowingMode(@WindowingMode int windowingMode) {
        this.mWindowingMode = windowingMode;
    }

    @WindowingMode
    public int getWindowingMode() {
        return this.mWindowingMode;
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
    }

    public void unset() {
        setToDefaults();
    }

    public void setToDefaults() {
        setAppBounds(null);
        setBounds(null);
        setWindowingMode(0);
        setActivityType(0);
    }

    @WindowConfig
    public int updateFrom(WindowConfiguration delta) {
        int changed = 0;
        if (!delta.mBounds.isEmpty() && !delta.mBounds.equals(this.mBounds)) {
            changed = 0 | 1;
            setBounds(delta.mBounds);
        }
        if (delta.mAppBounds != null && !delta.mAppBounds.equals(this.mAppBounds)) {
            changed |= 2;
            setAppBounds(delta.mAppBounds);
        }
        if (!(delta.mWindowingMode == 0 || this.mWindowingMode == delta.mWindowingMode)) {
            changed |= 4;
            setWindowingMode(delta.mWindowingMode);
        }
        if (delta.mActivityType == 0 || this.mActivityType == delta.mActivityType) {
            return changed;
        }
        int changed2 = changed | 8;
        setActivityType(delta.mActivityType);
        return changed2;
    }

    @WindowConfig
    public long diff(WindowConfiguration other, boolean compareUndefined) {
        long changes = 0;
        if (!this.mBounds.equals(other.mBounds)) {
            changes = 0 | 1;
        }
        if ((compareUndefined || other.mAppBounds != null) && this.mAppBounds != other.mAppBounds && (this.mAppBounds == null || !this.mAppBounds.equals(other.mAppBounds))) {
            changes |= 2;
        }
        if ((compareUndefined || other.mWindowingMode != 0) && this.mWindowingMode != other.mWindowingMode) {
            changes |= 4;
        }
        if ((compareUndefined || other.mActivityType != 0) && this.mActivityType != other.mActivityType) {
            return changes | 8;
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
        if (!(this.mAppBounds == null || that.mAppBounds == null)) {
            int n = this.mAppBounds.left - that.mAppBounds.left;
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
        return n10;
    }

    public boolean equals(Object that) {
        boolean z = false;
        if (that == null) {
            return false;
        }
        if (that == this) {
            return true;
        }
        if (!(that instanceof WindowConfiguration)) {
            return false;
        }
        if (compareTo((WindowConfiguration) that) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int result = 0;
        if (this.mAppBounds != null) {
            result = (31 * 0) + this.mAppBounds.hashCode();
        }
        return (31 * ((31 * ((31 * result) + this.mBounds.hashCode())) + this.mWindowingMode)) + this.mActivityType;
    }

    public String toString() {
        return "{ mBounds=" + this.mBounds + " mAppBounds=" + this.mAppBounds + " mWindowingMode=" + windowingModeToString(this.mWindowingMode) + " mActivityType=" + activityTypeToString(this.mActivityType) + "}";
    }

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        long token = protoOutputStream.start(fieldId);
        if (this.mAppBounds != null) {
            this.mAppBounds.writeToProto(protoOutputStream, 1146756268033L);
        }
        protoOutputStream.write(1120986464258L, this.mWindowingMode);
        protoOutputStream.write(1120986464259L, this.mActivityType);
        protoOutputStream.end(token);
    }

    public boolean hasWindowShadow() {
        return tasksAreFloating();
    }

    public boolean hasWindowDecorCaption() {
        return this.mWindowingMode == 5 || this.mWindowingMode == 10;
    }

    public boolean canResizeTask() {
        return this.mWindowingMode == 5 || this.mWindowingMode == 10;
    }

    public boolean persistTaskBounds() {
        return this.mWindowingMode == 5 || this.mWindowingMode == 10;
    }

    public boolean tasksAreFloating() {
        return isFloating(this.mWindowingMode);
    }

    public static boolean isFloating(int windowingMode) {
        return windowingMode == 5 || windowingMode == 2 || windowingMode == 10;
    }

    public boolean canReceiveKeys() {
        return this.mWindowingMode != 2;
    }

    public boolean isAlwaysOnTop() {
        return this.mWindowingMode == 2 || this.mWindowingMode == 5;
    }

    public boolean keepVisibleDeadAppWindowOnScreen() {
        return this.mWindowingMode != 2;
    }

    public boolean useWindowFrameForBackdrop() {
        return this.mWindowingMode == 5 || this.mWindowingMode == 2 || this.mWindowingMode == 10;
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
        switch (windowingMode) {
            case 0:
                return "undefined";
            case 1:
                return "fullscreen";
            case 2:
                return "pinned";
            case 3:
                return "split-screen-primary";
            case 4:
                return "split-screen-secondary";
            case 5:
                return "freeform";
            default:
                switch (windowingMode) {
                    case 10:
                        return "hw-pc-freeform";
                    case 11:
                        return "coordination_primary";
                    case 12:
                        return "coordination_secondary";
                    default:
                        return String.valueOf(windowingMode);
                }
        }
    }

    public static String activityTypeToString(@ActivityType int applicationType) {
        switch (applicationType) {
            case 0:
                return "undefined";
            case 1:
                return "standard";
            case 2:
                return "home";
            case 3:
                return "recents";
            case 4:
                return "assistant";
            default:
                return String.valueOf(applicationType);
        }
    }
}

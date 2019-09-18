package com.android.server.wm;

import android.app.WindowConfiguration;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.proto.ProtoOutputStream;
import com.android.server.wm.ConfigurationContainer;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class ConfigurationContainer<E extends ConfigurationContainer> {
    static final int BOUNDS_CHANGE_NONE = 0;
    static final int BOUNDS_CHANGE_POSITION = 1;
    static final int BOUNDS_CHANGE_SIZE = 2;
    private ArrayList<ConfigurationContainerListener> mChangeListeners = new ArrayList<>();
    private Configuration mFullConfiguration = new Configuration();
    private boolean mHasOverrideConfiguration;
    private Configuration mMergedOverrideConfiguration = new Configuration();
    private Configuration mOverrideConfiguration = new Configuration();
    private Rect mReturnBounds = new Rect();
    private final Configuration mTmpConfig = new Configuration();
    private final Rect mTmpRect = new Rect();

    /* access modifiers changed from: protected */
    public abstract E getChildAt(int i);

    /* access modifiers changed from: protected */
    public abstract int getChildCount();

    /* access modifiers changed from: protected */
    public abstract ConfigurationContainer getParent();

    public Configuration getConfiguration() {
        return this.mFullConfiguration;
    }

    public void onConfigurationChanged(Configuration newParentConfig) {
        this.mFullConfiguration.setTo(newParentConfig);
        this.mFullConfiguration.updateFrom(this.mOverrideConfiguration);
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).onConfigurationChanged(this.mFullConfiguration);
        }
    }

    public Configuration getOverrideConfiguration() {
        return this.mOverrideConfiguration;
    }

    public void onOverrideConfigurationChanged(Configuration overrideConfiguration) {
        this.mHasOverrideConfiguration = !Configuration.EMPTY.equals(overrideConfiguration);
        this.mOverrideConfiguration.setTo(overrideConfiguration);
        ConfigurationContainer parent = getParent();
        onConfigurationChanged(parent != null ? parent.getConfiguration() : Configuration.EMPTY);
        onMergedOverrideConfigurationChanged();
        this.mTmpConfig.setTo(this.mOverrideConfiguration);
        for (int i = this.mChangeListeners.size() - 1; i >= 0; i--) {
            this.mChangeListeners.get(i).onOverrideConfigurationChanged(this.mTmpConfig);
        }
    }

    public Configuration getMergedOverrideConfiguration() {
        return this.mMergedOverrideConfiguration;
    }

    /* access modifiers changed from: package-private */
    public void onMergedOverrideConfigurationChanged() {
        ConfigurationContainer parent = getParent();
        if (parent != null) {
            this.mMergedOverrideConfiguration.setTo(parent.getMergedOverrideConfiguration());
            this.mMergedOverrideConfiguration.updateFrom(this.mOverrideConfiguration);
        } else {
            this.mMergedOverrideConfiguration.setTo(this.mOverrideConfiguration);
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).onMergedOverrideConfigurationChanged();
        }
    }

    public boolean matchParentBounds() {
        return getOverrideBounds().isEmpty();
    }

    public boolean equivalentOverrideBounds(Rect bounds) {
        return equivalentBounds(getOverrideBounds(), bounds);
    }

    public static boolean equivalentBounds(Rect bounds, Rect other) {
        return bounds == other || (bounds != null && (bounds.equals(other) || (bounds.isEmpty() && other == null))) || (other != null && other.isEmpty() && bounds == null);
    }

    public Rect getBounds() {
        this.mReturnBounds.set(getConfiguration().windowConfiguration.getBounds());
        return this.mReturnBounds;
    }

    public void getBounds(Rect outBounds) {
        outBounds.set(getBounds());
    }

    public Rect getOverrideBounds() {
        this.mReturnBounds.set(getOverrideConfiguration().windowConfiguration.getBounds());
        return this.mReturnBounds;
    }

    public boolean hasOverrideBounds() {
        return !getOverrideBounds().isEmpty();
    }

    public void getOverrideBounds(Rect outBounds) {
        outBounds.set(getOverrideBounds());
    }

    public int setBounds(Rect bounds) {
        int boundsChange = diffOverrideBounds(bounds);
        if (boundsChange == 0) {
            return boundsChange;
        }
        this.mTmpConfig.setTo(getOverrideConfiguration());
        this.mTmpConfig.windowConfiguration.setBounds(bounds);
        onOverrideConfigurationChanged(this.mTmpConfig);
        return boundsChange;
    }

    public int setBounds(int left, int top, int right, int bottom) {
        this.mTmpRect.set(left, top, right, bottom);
        return setBounds(this.mTmpRect);
    }

    /* access modifiers changed from: package-private */
    public int diffOverrideBounds(Rect bounds) {
        if (equivalentOverrideBounds(bounds)) {
            return 0;
        }
        int boundsChange = 0;
        Rect existingBounds = getOverrideBounds();
        if (!(bounds != null && existingBounds.left == bounds.left && existingBounds.top == bounds.top)) {
            boundsChange = 0 | 1;
        }
        if (!(bounds != null && existingBounds.width() == bounds.width() && existingBounds.height() == bounds.height())) {
            boundsChange |= 2;
        }
        return boundsChange;
    }

    public WindowConfiguration getWindowConfiguration() {
        return this.mFullConfiguration.windowConfiguration;
    }

    public int getWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode();
    }

    public void setWindowingMode(int windowingMode) {
        this.mTmpConfig.setTo(getOverrideConfiguration());
        this.mTmpConfig.windowConfiguration.setWindowingMode(windowingMode);
        onOverrideConfigurationChanged(this.mTmpConfig);
    }

    public boolean inMultiWindowMode() {
        int windowingMode = this.mFullConfiguration.windowConfiguration.getWindowingMode();
        return (windowingMode == 1 || windowingMode == 0) ? false : true;
    }

    public boolean inSplitScreenWindowingMode() {
        int windowingMode = this.mFullConfiguration.windowConfiguration.getWindowingMode();
        return windowingMode == 3 || windowingMode == 4;
    }

    public boolean inSplitScreenSecondaryWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode() == 4;
    }

    public boolean inSplitScreenPrimaryWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode() == 3;
    }

    public boolean supportsSplitScreenWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.supportSplitScreenWindowingMode();
    }

    public boolean inPinnedWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode() == 2;
    }

    public boolean inFreeformWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode() == 5;
    }

    public int getActivityType() {
        return this.mFullConfiguration.windowConfiguration.getActivityType();
    }

    public void setActivityType(int activityType) {
        int currentActivityType = getActivityType();
        if (currentActivityType != activityType) {
            if (currentActivityType == 0) {
                this.mTmpConfig.setTo(getOverrideConfiguration());
                this.mTmpConfig.windowConfiguration.setActivityType(activityType);
                onOverrideConfigurationChanged(this.mTmpConfig);
                return;
            }
            throw new IllegalStateException("Can't change activity type once set: " + this + " activityType=" + WindowConfiguration.activityTypeToString(activityType));
        }
    }

    public boolean isActivityTypeHome() {
        return getActivityType() == 2;
    }

    public boolean isActivityTypeRecents() {
        return getActivityType() == 3;
    }

    public boolean isActivityTypeAssistant() {
        return getActivityType() == 4;
    }

    public boolean isActivityTypeStandard() {
        return getActivityType() == 1;
    }

    public boolean isActivityTypeStandardOrUndefined() {
        int activityType = getActivityType();
        return activityType == 1 || activityType == 0;
    }

    public boolean hasCompatibleActivityType(ConfigurationContainer other) {
        int thisType = getActivityType();
        int otherType = other.getActivityType();
        boolean z = true;
        if (thisType == otherType) {
            return true;
        }
        if (thisType == 4 || thisType == 2) {
            return false;
        }
        if (!(thisType == 0 || otherType == 0)) {
            z = false;
        }
        return z;
    }

    public boolean isCompatible(int windowingMode, int activityType) {
        int thisActivityType = getActivityType();
        int thisWindowingMode = getWindowingMode();
        boolean sameWindowingMode = false;
        boolean sameActivityType = thisActivityType == activityType;
        if (thisWindowingMode == windowingMode) {
            sameWindowingMode = true;
        }
        if (sameActivityType && sameWindowingMode) {
            return true;
        }
        if ((activityType == 0 || activityType == 1) && isActivityTypeStandardOrUndefined()) {
            return sameWindowingMode;
        }
        return sameActivityType;
    }

    public void registerConfigurationChangeListener(ConfigurationContainerListener listener) {
        if (!this.mChangeListeners.contains(listener)) {
            this.mChangeListeners.add(listener);
            listener.onOverrideConfigurationChanged(this.mOverrideConfiguration);
        }
    }

    public void unregisterConfigurationChangeListener(ConfigurationContainerListener listener) {
        this.mChangeListeners.remove(listener);
    }

    /* access modifiers changed from: protected */
    public void onParentChanged() {
        ConfigurationContainer parent = getParent();
        if (parent != null) {
            onConfigurationChanged(parent.mFullConfiguration);
            onMergedOverrideConfigurationChanged();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        if (!trim || this.mHasOverrideConfiguration) {
            this.mOverrideConfiguration.writeToProto(proto, 1146756268033L);
        }
        if (!trim) {
            this.mFullConfiguration.writeToProto(proto, 1146756268034L);
            this.mMergedOverrideConfiguration.writeToProto(proto, 1146756268035L);
        }
        proto.end(token);
    }

    public void dumpChildrenNames(PrintWriter pw, String prefix) {
        String childPrefix = prefix + " ";
        pw.println(getName() + " type=" + WindowConfiguration.activityTypeToString(getActivityType()) + " mode=" + WindowConfiguration.windowingModeToString(getWindowingMode()));
        for (int i = getChildCount() + -1; i >= 0; i += -1) {
            E cc = getChildAt(i);
            pw.print(childPrefix + "#" + i + " ");
            cc.dumpChildrenNames(pw, childPrefix);
        }
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return toString();
    }

    /* access modifiers changed from: package-private */
    public boolean isAlwaysOnTop() {
        return this.mFullConfiguration.windowConfiguration.isAlwaysOnTop();
    }

    public boolean inHwPCFreeformWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode() == 10;
    }

    public boolean inCoordinationPrimaryWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode() == 11;
    }

    public boolean inCoordinationSecondaryWindowingMode() {
        return this.mFullConfiguration.windowConfiguration.getWindowingMode() == 12;
    }
}

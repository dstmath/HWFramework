package android.appwidget;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ResourceId;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AppWidgetProviderInfo implements Parcelable {
    public static final Parcelable.Creator<AppWidgetProviderInfo> CREATOR = new Parcelable.Creator<AppWidgetProviderInfo>() {
        /* class android.appwidget.AppWidgetProviderInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppWidgetProviderInfo createFromParcel(Parcel parcel) {
            return new AppWidgetProviderInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AppWidgetProviderInfo[] newArray(int size) {
            return new AppWidgetProviderInfo[size];
        }
    };
    public static final int RESIZE_BOTH = 3;
    public static final int RESIZE_HORIZONTAL = 1;
    public static final int RESIZE_NONE = 0;
    public static final int RESIZE_VERTICAL = 2;
    public static final int WIDGET_CATEGORY_HOME_SCREEN = 1;
    public static final int WIDGET_CATEGORY_KEYGUARD = 2;
    public static final int WIDGET_CATEGORY_SEARCHBOX = 4;
    public static final int WIDGET_FEATURE_HIDE_FROM_PICKER = 2;
    public static final int WIDGET_FEATURE_RECONFIGURABLE = 1;
    public int autoAdvanceViewId;
    public ComponentName configure;
    public int icon;
    public int initialKeyguardLayout;
    public int initialLayout;
    @Deprecated
    public String label;
    public int minHeight;
    public int minResizeHeight;
    public int minResizeWidth;
    public int minWidth;
    public int previewImage;
    public ComponentName provider;
    @UnsupportedAppUsage
    public ActivityInfo providerInfo;
    public int resizeMode;
    public int updatePeriodMillis;
    public int widgetCategory;
    public int widgetFeatures;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CategoryFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FeatureFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResizeModeFlags {
    }

    public AppWidgetProviderInfo() {
    }

    public AppWidgetProviderInfo(Parcel in) {
        this.provider = (ComponentName) in.readTypedObject(ComponentName.CREATOR);
        this.minWidth = in.readInt();
        this.minHeight = in.readInt();
        this.minResizeWidth = in.readInt();
        this.minResizeHeight = in.readInt();
        this.updatePeriodMillis = in.readInt();
        this.initialLayout = in.readInt();
        this.initialKeyguardLayout = in.readInt();
        this.configure = (ComponentName) in.readTypedObject(ComponentName.CREATOR);
        this.label = in.readString();
        this.icon = in.readInt();
        this.previewImage = in.readInt();
        this.autoAdvanceViewId = in.readInt();
        this.resizeMode = in.readInt();
        this.widgetCategory = in.readInt();
        this.providerInfo = (ActivityInfo) in.readTypedObject(ActivityInfo.CREATOR);
        this.widgetFeatures = in.readInt();
    }

    public final String loadLabel(PackageManager packageManager) {
        CharSequence label2 = this.providerInfo.loadLabel(packageManager);
        if (label2 != null) {
            return label2.toString().trim();
        }
        return null;
    }

    public final Drawable loadIcon(Context context, int density) {
        return loadDrawable(context, density, this.providerInfo.getIconResource(), true);
    }

    public final Drawable loadPreviewImage(Context context, int density) {
        return loadDrawable(context, density, this.previewImage, false);
    }

    public final UserHandle getProfile() {
        return new UserHandle(UserHandle.getUserId(this.providerInfo.applicationInfo.uid));
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedObject(this.provider, flags);
        out.writeInt(this.minWidth);
        out.writeInt(this.minHeight);
        out.writeInt(this.minResizeWidth);
        out.writeInt(this.minResizeHeight);
        out.writeInt(this.updatePeriodMillis);
        out.writeInt(this.initialLayout);
        out.writeInt(this.initialKeyguardLayout);
        out.writeTypedObject(this.configure, flags);
        out.writeString(this.label);
        out.writeInt(this.icon);
        out.writeInt(this.previewImage);
        out.writeInt(this.autoAdvanceViewId);
        out.writeInt(this.resizeMode);
        out.writeInt(this.widgetCategory);
        out.writeTypedObject(this.providerInfo, flags);
        out.writeInt(this.widgetFeatures);
    }

    public AppWidgetProviderInfo clone() {
        AppWidgetProviderInfo that = new AppWidgetProviderInfo();
        ComponentName componentName = this.provider;
        ComponentName componentName2 = null;
        that.provider = componentName == null ? null : componentName.clone();
        that.minWidth = this.minWidth;
        that.minHeight = this.minHeight;
        int i = this.minResizeHeight;
        that.minResizeWidth = i;
        that.minResizeHeight = i;
        that.updatePeriodMillis = this.updatePeriodMillis;
        that.initialLayout = this.initialLayout;
        that.initialKeyguardLayout = this.initialKeyguardLayout;
        ComponentName componentName3 = this.configure;
        if (componentName3 != null) {
            componentName2 = componentName3.clone();
        }
        that.configure = componentName2;
        that.label = this.label;
        that.icon = this.icon;
        that.previewImage = this.previewImage;
        that.autoAdvanceViewId = this.autoAdvanceViewId;
        that.resizeMode = this.resizeMode;
        that.widgetCategory = this.widgetCategory;
        that.providerInfo = this.providerInfo;
        that.widgetFeatures = this.widgetFeatures;
        return that;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private Drawable loadDrawable(Context context, int density, int resourceId, boolean loadDefaultIcon) {
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(this.providerInfo.applicationInfo);
            if (ResourceId.isValid(resourceId)) {
                if (density < 0) {
                    density = 0;
                }
                return resources.getDrawableForDensity(resourceId, density, null);
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
        }
        if (loadDefaultIcon) {
            return this.providerInfo.loadIcon(context.getPackageManager());
        }
        return null;
    }

    public void updateDimensions(DisplayMetrics displayMetrics) {
        this.minWidth = TypedValue.complexToDimensionPixelSize(this.minWidth, displayMetrics);
        this.minHeight = TypedValue.complexToDimensionPixelSize(this.minHeight, displayMetrics);
        this.minResizeWidth = TypedValue.complexToDimensionPixelSize(this.minResizeWidth, displayMetrics);
        this.minResizeHeight = TypedValue.complexToDimensionPixelSize(this.minResizeHeight, displayMetrics);
    }

    public String toString() {
        return "AppWidgetProviderInfo(" + getProfile() + '/' + this.provider + ')';
    }
}

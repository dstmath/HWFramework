package android.appwidget;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ResourceId;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class AppWidgetProviderInfo implements Parcelable {
    public static final Creator<AppWidgetProviderInfo> CREATOR = new Creator<AppWidgetProviderInfo>() {
        public AppWidgetProviderInfo createFromParcel(Parcel parcel) {
            return new AppWidgetProviderInfo(parcel);
        }

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
    public ActivityInfo providerInfo;
    public int resizeMode;
    public int updatePeriodMillis;
    public int widgetCategory;

    public AppWidgetProviderInfo(Parcel in) {
        if (in.readInt() != 0) {
            this.provider = new ComponentName(in);
        }
        this.minWidth = in.readInt();
        this.minHeight = in.readInt();
        this.minResizeWidth = in.readInt();
        this.minResizeHeight = in.readInt();
        this.updatePeriodMillis = in.readInt();
        this.initialLayout = in.readInt();
        this.initialKeyguardLayout = in.readInt();
        if (in.readInt() != 0) {
            this.configure = new ComponentName(in);
        }
        this.label = in.readString();
        this.icon = in.readInt();
        this.previewImage = in.readInt();
        this.autoAdvanceViewId = in.readInt();
        this.resizeMode = in.readInt();
        this.widgetCategory = in.readInt();
        this.providerInfo = (ActivityInfo) in.readParcelable(null);
    }

    public final String loadLabel(PackageManager packageManager) {
        CharSequence label = this.providerInfo.loadLabel(packageManager);
        if (label != null) {
            return label.toString().trim();
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

    public void writeToParcel(Parcel out, int flags) {
        if (this.provider != null) {
            out.writeInt(1);
            this.provider.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.minWidth);
        out.writeInt(this.minHeight);
        out.writeInt(this.minResizeWidth);
        out.writeInt(this.minResizeHeight);
        out.writeInt(this.updatePeriodMillis);
        out.writeInt(this.initialLayout);
        out.writeInt(this.initialKeyguardLayout);
        if (this.configure != null) {
            out.writeInt(1);
            this.configure.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeString(this.label);
        out.writeInt(this.icon);
        out.writeInt(this.previewImage);
        out.writeInt(this.autoAdvanceViewId);
        out.writeInt(this.resizeMode);
        out.writeInt(this.widgetCategory);
        out.writeParcelable(this.providerInfo, flags);
    }

    public AppWidgetProviderInfo clone() {
        String str = null;
        AppWidgetProviderInfo that = new AppWidgetProviderInfo();
        that.provider = this.provider == null ? null : this.provider.clone();
        that.minWidth = this.minWidth;
        that.minHeight = this.minHeight;
        that.minResizeWidth = this.minResizeHeight;
        that.minResizeHeight = this.minResizeHeight;
        that.updatePeriodMillis = this.updatePeriodMillis;
        that.initialLayout = this.initialLayout;
        that.initialKeyguardLayout = this.initialKeyguardLayout;
        that.configure = this.configure == null ? null : this.configure.clone();
        if (this.label != null) {
            str = this.label.substring(0);
        }
        that.label = str;
        that.icon = this.icon;
        that.previewImage = this.previewImage;
        that.autoAdvanceViewId = this.autoAdvanceViewId;
        that.resizeMode = this.resizeMode;
        that.widgetCategory = this.widgetCategory;
        that.providerInfo = this.providerInfo;
        return that;
    }

    public int describeContents() {
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x001c A:{ExcHandler: android.content.pm.PackageManager.NameNotFoundException (e android.content.pm.PackageManager$NameNotFoundException), Splitter: B:1:0x0001} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Drawable loadDrawable(Context context, int density, int resourceId, boolean loadDefaultIcon) {
        Drawable drawable = null;
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(this.providerInfo.applicationInfo);
            if (ResourceId.isValid(resourceId)) {
                if (density < 0) {
                    density = 0;
                }
                return resources.getDrawableForDensity(resourceId, density, null);
            }
        } catch (NameNotFoundException e) {
        }
        if (loadDefaultIcon) {
            drawable = this.providerInfo.loadIcon(context.getPackageManager());
        }
        return drawable;
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

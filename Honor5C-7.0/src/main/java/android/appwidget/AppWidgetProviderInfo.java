package android.appwidget;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;

public class AppWidgetProviderInfo implements Parcelable {
    public static final Creator<AppWidgetProviderInfo> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.appwidget.AppWidgetProviderInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.appwidget.AppWidgetProviderInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.appwidget.AppWidgetProviderInfo.<clinit>():void");
    }

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
            out.writeInt(WIDGET_CATEGORY_HOME_SCREEN);
            this.provider.writeToParcel(out, flags);
        } else {
            out.writeInt(RESIZE_NONE);
        }
        out.writeInt(this.minWidth);
        out.writeInt(this.minHeight);
        out.writeInt(this.minResizeWidth);
        out.writeInt(this.minResizeHeight);
        out.writeInt(this.updatePeriodMillis);
        out.writeInt(this.initialLayout);
        out.writeInt(this.initialKeyguardLayout);
        if (this.configure != null) {
            out.writeInt(WIDGET_CATEGORY_HOME_SCREEN);
            this.configure.writeToParcel(out, flags);
        } else {
            out.writeInt(RESIZE_NONE);
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
            str = this.label.substring(RESIZE_NONE);
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
        return RESIZE_NONE;
    }

    private Drawable loadDrawable(Context context, int density, int resourceId, boolean loadDefaultIcon) {
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(this.providerInfo.applicationInfo);
            if (resourceId > 0) {
                if (density <= 0) {
                    density = context.getResources().getDisplayMetrics().densityDpi;
                }
                return resources.getDrawableForDensity(resourceId, density);
            }
        } catch (NameNotFoundException e) {
        }
        return loadDefaultIcon ? this.providerInfo.loadIcon(context.getPackageManager()) : null;
    }

    public String toString() {
        return "AppWidgetProviderInfo(" + getProfile() + '/' + this.provider + ')';
    }
}

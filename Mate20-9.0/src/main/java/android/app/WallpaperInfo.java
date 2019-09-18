package android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Printer;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public final class WallpaperInfo implements Parcelable {
    public static final Parcelable.Creator<WallpaperInfo> CREATOR = new Parcelable.Creator<WallpaperInfo>() {
        public WallpaperInfo createFromParcel(Parcel source) {
            return new WallpaperInfo(source);
        }

        public WallpaperInfo[] newArray(int size) {
            return new WallpaperInfo[size];
        }
    };
    static final String TAG = "WallpaperInfo";
    final int mAuthorResource;
    final int mContextDescriptionResource;
    final int mContextUriResource;
    final int mDescriptionResource;
    final ResolveInfo mService;
    final String mSettingsActivityName;
    final boolean mShowMetadataInPreview;
    final boolean mSupportsAmbientMode;
    final int mThumbnailResource;

    public WallpaperInfo(Context context, ResolveInfo service) throws XmlPullParserException, IOException {
        this.mService = service;
        ServiceInfo si = service.serviceInfo;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        try {
            parser = si.loadXmlMetaData(pm, "android.service.wallpaper");
            if (parser != null) {
                Resources res = pm.getResourcesForApplication(si.applicationInfo);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                while (true) {
                    int next = parser.next();
                    int type = next;
                    if (next == 1 || type == 2) {
                    }
                }
                if ("wallpaper".equals(parser.getName())) {
                    TypedArray sa = res.obtainAttributes(attrs, R.styleable.Wallpaper);
                    this.mSettingsActivityName = sa.getString(1);
                    this.mThumbnailResource = sa.getResourceId(2, -1);
                    this.mAuthorResource = sa.getResourceId(3, -1);
                    this.mDescriptionResource = sa.getResourceId(0, -1);
                    this.mContextUriResource = sa.getResourceId(4, -1);
                    this.mContextDescriptionResource = sa.getResourceId(5, -1);
                    this.mShowMetadataInPreview = sa.getBoolean(6, false);
                    this.mSupportsAmbientMode = sa.getBoolean(7, false);
                    sa.recycle();
                    if (parser != null) {
                        parser.close();
                        return;
                    }
                    return;
                }
                throw new XmlPullParserException("Meta-data does not start with wallpaper tag");
            }
            throw new XmlPullParserException("No android.service.wallpaper meta-data");
        } catch (PackageManager.NameNotFoundException e) {
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    WallpaperInfo(Parcel source) {
        this.mSettingsActivityName = source.readString();
        this.mThumbnailResource = source.readInt();
        this.mAuthorResource = source.readInt();
        this.mDescriptionResource = source.readInt();
        this.mContextUriResource = source.readInt();
        this.mContextDescriptionResource = source.readInt();
        boolean z = false;
        this.mShowMetadataInPreview = source.readInt() != 0;
        this.mSupportsAmbientMode = source.readInt() != 0 ? true : z;
        this.mService = ResolveInfo.CREATOR.createFromParcel(source);
    }

    public String getPackageName() {
        return this.mService.serviceInfo.packageName;
    }

    public String getServiceName() {
        return this.mService.serviceInfo.name;
    }

    public ServiceInfo getServiceInfo() {
        return this.mService.serviceInfo;
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public Drawable loadThumbnail(PackageManager pm) {
        if (this.mThumbnailResource < 0) {
            return null;
        }
        return pm.getDrawable(this.mService.serviceInfo.packageName, this.mThumbnailResource, this.mService.serviceInfo.applicationInfo);
    }

    public CharSequence loadAuthor(PackageManager pm) throws Resources.NotFoundException {
        if (this.mAuthorResource > 0) {
            String packageName = this.mService.resolvePackageName;
            ApplicationInfo applicationInfo = null;
            if (packageName == null) {
                packageName = this.mService.serviceInfo.packageName;
                applicationInfo = this.mService.serviceInfo.applicationInfo;
            }
            return pm.getText(packageName, this.mAuthorResource, applicationInfo);
        }
        throw new Resources.NotFoundException();
    }

    public CharSequence loadDescription(PackageManager pm) throws Resources.NotFoundException {
        String packageName = this.mService.resolvePackageName;
        ApplicationInfo applicationInfo = null;
        if (packageName == null) {
            packageName = this.mService.serviceInfo.packageName;
            applicationInfo = this.mService.serviceInfo.applicationInfo;
        }
        if (this.mService.serviceInfo.descriptionRes != 0) {
            return pm.getText(packageName, this.mService.serviceInfo.descriptionRes, applicationInfo);
        }
        if (this.mDescriptionResource > 0) {
            return pm.getText(packageName, this.mDescriptionResource, this.mService.serviceInfo.applicationInfo);
        }
        throw new Resources.NotFoundException();
    }

    public Uri loadContextUri(PackageManager pm) throws Resources.NotFoundException {
        if (this.mContextUriResource > 0) {
            String packageName = this.mService.resolvePackageName;
            ApplicationInfo applicationInfo = null;
            if (packageName == null) {
                packageName = this.mService.serviceInfo.packageName;
                applicationInfo = this.mService.serviceInfo.applicationInfo;
            }
            String contextUriString = pm.getText(packageName, this.mContextUriResource, applicationInfo).toString();
            if (contextUriString == null) {
                return null;
            }
            return Uri.parse(contextUriString);
        }
        throw new Resources.NotFoundException();
    }

    public CharSequence loadContextDescription(PackageManager pm) throws Resources.NotFoundException {
        if (this.mContextDescriptionResource > 0) {
            String packageName = this.mService.resolvePackageName;
            ApplicationInfo applicationInfo = null;
            if (packageName == null) {
                packageName = this.mService.serviceInfo.packageName;
                applicationInfo = this.mService.serviceInfo.applicationInfo;
            }
            return pm.getText(packageName, this.mContextDescriptionResource, applicationInfo).toString();
        }
        throw new Resources.NotFoundException();
    }

    public boolean getShowMetadataInPreview() {
        return this.mShowMetadataInPreview;
    }

    public boolean getSupportsAmbientMode() {
        return this.mSupportsAmbientMode;
    }

    public String getSettingsActivity() {
        return this.mSettingsActivityName;
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "Service:");
        ResolveInfo resolveInfo = this.mService;
        resolveInfo.dump(pw, prefix + "  ");
        pw.println(prefix + "mSettingsActivityName=" + this.mSettingsActivityName);
    }

    public String toString() {
        return "WallpaperInfo{" + this.mService.serviceInfo.name + ", settings: " + this.mSettingsActivityName + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSettingsActivityName);
        dest.writeInt(this.mThumbnailResource);
        dest.writeInt(this.mAuthorResource);
        dest.writeInt(this.mDescriptionResource);
        dest.writeInt(this.mContextUriResource);
        dest.writeInt(this.mContextDescriptionResource);
        dest.writeInt(this.mShowMetadataInPreview ? 1 : 0);
        dest.writeInt(this.mSupportsAmbientMode ? 1 : 0);
        this.mService.writeToParcel(dest, flags);
    }

    public int describeContents() {
        return 0;
    }

    public void removeThumbnailCache(PackageManager pm) {
        if (this.mThumbnailResource >= 0 && (pm instanceof ApplicationPackageManager)) {
            ((ApplicationPackageManager) pm).removeCacheIcon(this.mService.serviceInfo.packageName, this.mThumbnailResource);
        }
    }

    public int getThumbnailResource() {
        return this.mThumbnailResource;
    }
}

package android.app;

import android.annotation.SystemApi;
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
import android.service.wallpaper.WallpaperService;
import android.util.AttributeSet;
import android.util.Printer;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public final class WallpaperInfo implements Parcelable {
    public static final Parcelable.Creator<WallpaperInfo> CREATOR = new Parcelable.Creator<WallpaperInfo>() {
        /* class android.app.WallpaperInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WallpaperInfo createFromParcel(Parcel source) {
            return new WallpaperInfo(source);
        }

        @Override // android.os.Parcelable.Creator
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
    final String mSettingsSliceUri;
    final boolean mShowMetadataInPreview;
    final boolean mSupportMultipleDisplays;
    final boolean mSupportsAmbientMode;
    final int mThumbnailResource;

    public WallpaperInfo(Context context, ResolveInfo service) throws XmlPullParserException, IOException {
        XmlResourceParser parser;
        this.mService = service;
        ServiceInfo si = service.serviceInfo;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser2 = null;
        try {
            parser = si.loadXmlMetaData(pm, WallpaperService.SERVICE_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th) {
            if (0 != 0) {
                parser2.close();
            }
            throw th;
        }
        if (parser != null) {
            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            while (true) {
                int type = parser.next();
                if (type == 1 || type == 2) {
                    break;
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
                this.mSettingsSliceUri = sa.getString(8);
                this.mSupportMultipleDisplays = sa.getBoolean(9, false);
                sa.recycle();
                parser.close();
                return;
            }
            throw new XmlPullParserException("Meta-data does not start with wallpaper tag");
        }
        throw new XmlPullParserException("No android.service.wallpaper meta-data");
    }

    WallpaperInfo(Parcel source) {
        this.mSettingsActivityName = source.readString();
        this.mThumbnailResource = source.readInt();
        this.mAuthorResource = source.readInt();
        this.mDescriptionResource = source.readInt();
        this.mContextUriResource = source.readInt();
        this.mContextDescriptionResource = source.readInt();
        boolean z = true;
        this.mShowMetadataInPreview = source.readInt() != 0;
        this.mSupportsAmbientMode = source.readInt() != 0;
        this.mSettingsSliceUri = source.readString();
        this.mSupportMultipleDisplays = source.readInt() == 0 ? false : z;
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
        int i = this.mDescriptionResource;
        if (i > 0) {
            return pm.getText(packageName, i, this.mService.serviceInfo.applicationInfo);
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

    @SystemApi
    public boolean supportsAmbientMode() {
        return this.mSupportsAmbientMode;
    }

    public String getSettingsActivity() {
        return this.mSettingsActivityName;
    }

    public Uri getSettingsSliceUri() {
        String str = this.mSettingsSliceUri;
        if (str == null) {
            return null;
        }
        return Uri.parse(str);
    }

    public boolean supportsMultipleDisplays() {
        return this.mSupportMultipleDisplays;
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSettingsActivityName);
        dest.writeInt(this.mThumbnailResource);
        dest.writeInt(this.mAuthorResource);
        dest.writeInt(this.mDescriptionResource);
        dest.writeInt(this.mContextUriResource);
        dest.writeInt(this.mContextDescriptionResource);
        dest.writeInt(this.mShowMetadataInPreview ? 1 : 0);
        dest.writeInt(this.mSupportsAmbientMode ? 1 : 0);
        dest.writeString(this.mSettingsSliceUri);
        dest.writeInt(this.mSupportMultipleDisplays ? 1 : 0);
        this.mService.writeToParcel(dest, flags);
    }

    @Override // android.os.Parcelable
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

package android.printservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public final class PrintServiceInfo implements Parcelable {
    public static final Creator<PrintServiceInfo> CREATOR = new Creator<PrintServiceInfo>() {
        public PrintServiceInfo createFromParcel(Parcel parcel) {
            return new PrintServiceInfo(parcel);
        }

        public PrintServiceInfo[] newArray(int size) {
            return new PrintServiceInfo[size];
        }
    };
    private static final String LOG_TAG = PrintServiceInfo.class.getSimpleName();
    private static final String TAG_PRINT_SERVICE = "print-service";
    private final String mAddPrintersActivityName;
    private final String mAdvancedPrintOptionsActivityName;
    private final String mId;
    private boolean mIsEnabled;
    private final ResolveInfo mResolveInfo;
    private final String mSettingsActivityName;

    public PrintServiceInfo(Parcel parcel) {
        boolean z = false;
        this.mId = parcel.readString();
        if (parcel.readByte() != (byte) 0) {
            z = true;
        }
        this.mIsEnabled = z;
        this.mResolveInfo = (ResolveInfo) parcel.readParcelable(null);
        this.mSettingsActivityName = parcel.readString();
        this.mAddPrintersActivityName = parcel.readString();
        this.mAdvancedPrintOptionsActivityName = parcel.readString();
    }

    public PrintServiceInfo(ResolveInfo resolveInfo, String settingsActivityName, String addPrintersActivityName, String advancedPrintOptionsActivityName) {
        this.mId = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name).flattenToString();
        this.mResolveInfo = resolveInfo;
        this.mSettingsActivityName = settingsActivityName;
        this.mAddPrintersActivityName = addPrintersActivityName;
        this.mAdvancedPrintOptionsActivityName = advancedPrintOptionsActivityName;
    }

    public ComponentName getComponentName() {
        return new ComponentName(this.mResolveInfo.serviceInfo.packageName, this.mResolveInfo.serviceInfo.name);
    }

    public static PrintServiceInfo create(Context context, ResolveInfo resolveInfo) {
        String str = null;
        String str2 = null;
        String str3 = null;
        PackageManager packageManager = context.getPackageManager();
        XmlResourceParser parser = resolveInfo.serviceInfo.loadXmlMetaData(packageManager, PrintService.SERVICE_META_DATA);
        if (parser != null) {
            int type = 0;
            while (type != 1 && type != 2) {
                try {
                    type = parser.next();
                } catch (IOException ioe) {
                    Log.w(LOG_TAG, "Error reading meta-data:" + ioe);
                    if (parser != null) {
                        parser.close();
                    }
                } catch (XmlPullParserException xppe) {
                    Log.w(LOG_TAG, "Error reading meta-data:" + xppe);
                    if (parser != null) {
                        parser.close();
                    }
                } catch (NameNotFoundException e) {
                    Log.e(LOG_TAG, "Unable to load resources for: " + resolveInfo.serviceInfo.packageName);
                    if (parser != null) {
                        parser.close();
                    }
                } catch (Throwable th) {
                    if (parser != null) {
                        parser.close();
                    }
                }
            }
            if (TAG_PRINT_SERVICE.equals(parser.getName())) {
                TypedArray attributes = packageManager.getResourcesForApplication(resolveInfo.serviceInfo.applicationInfo).obtainAttributes(Xml.asAttributeSet(parser), R.styleable.PrintService);
                str = attributes.getString(0);
                str2 = attributes.getString(1);
                str3 = attributes.getString(3);
                attributes.recycle();
            } else {
                Log.e(LOG_TAG, "Ignoring meta-data that does not start with print-service tag");
            }
            if (parser != null) {
                parser.close();
            }
        }
        return new PrintServiceInfo(resolveInfo, str, str2, str3);
    }

    public String getId() {
        return this.mId;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
    }

    public ResolveInfo getResolveInfo() {
        return this.mResolveInfo;
    }

    public String getSettingsActivityName() {
        return this.mSettingsActivityName;
    }

    public String getAddPrintersActivityName() {
        return this.mAddPrintersActivityName;
    }

    public String getAdvancedOptionsActivityName() {
        return this.mAdvancedPrintOptionsActivityName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flagz) {
        parcel.writeString(this.mId);
        parcel.writeByte((byte) (this.mIsEnabled ? 1 : 0));
        parcel.writeParcelable(this.mResolveInfo, 0);
        parcel.writeString(this.mSettingsActivityName);
        parcel.writeString(this.mAddPrintersActivityName);
        parcel.writeString(this.mAdvancedPrintOptionsActivityName);
    }

    public int hashCode() {
        return (this.mId == null ? 0 : this.mId.hashCode()) + 31;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrintServiceInfo other = (PrintServiceInfo) obj;
        if (this.mId == null) {
            if (other.mId != null) {
                return false;
            }
        } else if (!this.mId.equals(other.mId)) {
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PrintServiceInfo{");
        builder.append("id=").append(this.mId);
        builder.append("isEnabled=").append(this.mIsEnabled);
        builder.append(", resolveInfo=").append(this.mResolveInfo);
        builder.append(", settingsActivityName=").append(this.mSettingsActivityName);
        builder.append(", addPrintersActivityName=").append(this.mAddPrintersActivityName);
        builder.append(", advancedPrintOptionsActivityName=").append(this.mAdvancedPrintOptionsActivityName);
        builder.append("}");
        return builder.toString();
    }
}

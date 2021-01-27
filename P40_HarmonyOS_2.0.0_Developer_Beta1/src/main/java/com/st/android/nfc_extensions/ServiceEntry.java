package com.st.android.nfc_extensions;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class ServiceEntry implements Parcelable {
    public static final Parcelable.Creator<ServiceEntry> CREATOR = new Parcelable.Creator<ServiceEntry>() {
        /* class com.st.android.nfc_extensions.ServiceEntry.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ServiceEntry createFromParcel(Parcel source) {
            ComponentName component;
            String tag = source.readString();
            String title = source.readString();
            Integer banner = Integer.valueOf(source.readInt());
            boolean z = true;
            Boolean wasEnabled = new Boolean(source.readInt() != 0);
            if (source.readInt() == 0) {
                z = false;
            }
            Boolean wantEnabled = new Boolean(z);
            if (getClass().getClassLoader() != null) {
                component = (ComponentName) source.readParcelable(getClass().getClassLoader());
            } else {
                component = null;
            }
            return new ServiceEntry(component, tag, title, banner, wasEnabled, wantEnabled);
        }

        @Override // android.os.Parcelable.Creator
        public ServiceEntry[] newArray(int size) {
            return new ServiceEntry[size];
        }
    };
    private static String TAG = "ServiceEntry";
    Integer banner;
    ComponentName component;
    String tag;
    String title;
    Boolean wantEnabled;
    Boolean wasEnabled;

    public ServiceEntry(ComponentName component2, String tag2, String title2, Integer banner2, Boolean wasEnabled2, Boolean wantEnabled2) {
        this.component = component2;
        this.tag = tag2;
        this.title = title2;
        this.banner = banner2;
        this.wasEnabled = wasEnabled2;
        this.wantEnabled = wantEnabled2;
    }

    public Drawable getIcon(PackageManager pm) {
        try {
            return pm.getApplicationIcon(this.component.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not load icon.");
            return null;
        }
    }

    public Drawable getBanner(PackageManager pm) {
        try {
            return pm.getResourcesForApplication(this.component.getPackageName()).getDrawable(this.banner.intValue());
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner.");
            return null;
        }
    }

    public String getTag() {
        return this.tag;
    }

    public String getTitle() {
        return this.title;
    }

    public Boolean getWasEnabled() {
        return this.wasEnabled;
    }

    public Boolean getWantEnabled() {
        return this.wantEnabled;
    }

    public void setWantEnabled(Boolean enable) {
        this.wantEnabled = enable;
    }

    public ComponentName getComponent() {
        return this.component;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tag);
        dest.writeString(this.title);
        dest.writeInt(this.banner.intValue());
        if (this.wasEnabled.booleanValue()) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        if (this.wantEnabled.booleanValue()) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeParcelable(this.component, flags);
    }
}

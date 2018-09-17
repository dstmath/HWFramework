package android.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class MediaDescription implements Parcelable {
    public static final long BT_FOLDER_TYPE_ALBUMS = 2;
    public static final long BT_FOLDER_TYPE_ARTISTS = 3;
    public static final long BT_FOLDER_TYPE_GENRES = 4;
    public static final long BT_FOLDER_TYPE_MIXED = 0;
    public static final long BT_FOLDER_TYPE_PLAYLISTS = 5;
    public static final long BT_FOLDER_TYPE_TITLES = 1;
    public static final long BT_FOLDER_TYPE_YEARS = 6;
    public static final Creator<MediaDescription> CREATOR = new Creator<MediaDescription>() {
        public MediaDescription createFromParcel(Parcel in) {
            return new MediaDescription(in, null);
        }

        public MediaDescription[] newArray(int size) {
            return new MediaDescription[size];
        }
    };
    public static final String EXTRA_BT_FOLDER_TYPE = "android.media.extra.BT_FOLDER_TYPE";
    private final CharSequence mDescription;
    private final Bundle mExtras;
    private final Bitmap mIcon;
    private final Uri mIconUri;
    private final String mMediaId;
    private final Uri mMediaUri;
    private final CharSequence mSubtitle;
    private final CharSequence mTitle;

    public static class Builder {
        private CharSequence mDescription;
        private Bundle mExtras;
        private Bitmap mIcon;
        private Uri mIconUri;
        private String mMediaId;
        private Uri mMediaUri;
        private CharSequence mSubtitle;
        private CharSequence mTitle;

        public Builder setMediaId(String mediaId) {
            this.mMediaId = mediaId;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setSubtitle(CharSequence subtitle) {
            this.mSubtitle = subtitle;
            return this;
        }

        public Builder setDescription(CharSequence description) {
            this.mDescription = description;
            return this;
        }

        public Builder setIconBitmap(Bitmap icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setIconUri(Uri iconUri) {
            this.mIconUri = iconUri;
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public Builder setMediaUri(Uri mediaUri) {
            this.mMediaUri = mediaUri;
            return this;
        }

        public MediaDescription build() {
            return new MediaDescription(this.mMediaId, this.mTitle, this.mSubtitle, this.mDescription, this.mIcon, this.mIconUri, this.mExtras, this.mMediaUri, null);
        }
    }

    /* synthetic */ MediaDescription(Parcel in, MediaDescription -this1) {
        this(in);
    }

    /* synthetic */ MediaDescription(String mediaId, CharSequence title, CharSequence subtitle, CharSequence description, Bitmap icon, Uri iconUri, Bundle extras, Uri mediaUri, MediaDescription -this8) {
        this(mediaId, title, subtitle, description, icon, iconUri, extras, mediaUri);
    }

    private MediaDescription(String mediaId, CharSequence title, CharSequence subtitle, CharSequence description, Bitmap icon, Uri iconUri, Bundle extras, Uri mediaUri) {
        this.mMediaId = mediaId;
        this.mTitle = title;
        this.mSubtitle = subtitle;
        this.mDescription = description;
        this.mIcon = icon;
        this.mIconUri = iconUri;
        this.mExtras = extras;
        this.mMediaUri = mediaUri;
    }

    private MediaDescription(Parcel in) {
        this.mMediaId = in.readString();
        this.mTitle = in.readCharSequence();
        this.mSubtitle = in.readCharSequence();
        this.mDescription = in.readCharSequence();
        this.mIcon = (Bitmap) in.readParcelable(null);
        this.mIconUri = (Uri) in.readParcelable(null);
        this.mExtras = in.readBundle();
        this.mMediaUri = (Uri) in.readParcelable(null);
    }

    public String getMediaId() {
        return this.mMediaId;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    public CharSequence getDescription() {
        return this.mDescription;
    }

    public Bitmap getIconBitmap() {
        return this.mIcon;
    }

    public Uri getIconUri() {
        return this.mIconUri;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public Uri getMediaUri() {
        return this.mMediaUri;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mMediaId);
        dest.writeCharSequence(this.mTitle);
        dest.writeCharSequence(this.mSubtitle);
        dest.writeCharSequence(this.mDescription);
        dest.writeParcelable(this.mIcon, flags);
        dest.writeParcelable(this.mIconUri, flags);
        dest.writeBundle(this.mExtras);
        dest.writeParcelable(this.mMediaUri, flags);
    }

    public String toString() {
        return this.mTitle + ", " + this.mSubtitle + ", " + this.mDescription;
    }
}

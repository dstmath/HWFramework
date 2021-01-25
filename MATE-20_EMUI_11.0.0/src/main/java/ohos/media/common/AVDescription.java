package ohos.media.common;

import ohos.media.image.PixelMap;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public final class AVDescription implements Sequenceable {
    public static final Sequenceable.Producer<AVDescription> CREATOR = new Sequenceable.Producer<AVDescription>() {
        /* class ohos.media.common.AVDescription.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public AVDescription createFromParcel(Parcel parcel) {
            return new AVDescription(parcel);
        }
    };
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVDescription.class);
    private CharSequence description;
    private PacMap extras;
    private PixelMap icon;
    private Uri iconUri;
    private String mediaId;
    private Uri mediaUri;
    private CharSequence subTitle;
    private CharSequence title;

    public String getMediaId() {
        return this.mediaId;
    }

    public CharSequence getTitle() {
        return this.title;
    }

    public CharSequence getSubTitle() {
        return this.subTitle;
    }

    public CharSequence getDescription() {
        return this.description;
    }

    public PixelMap getIcon() {
        return this.icon;
    }

    public Uri getIconUri() {
        return this.iconUri;
    }

    public PacMap getExtras() {
        return this.extras;
    }

    public Uri getMediaUri() {
        return this.mediaUri;
    }

    private AVDescription(String str, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, PixelMap pixelMap, Uri uri, PacMap pacMap, Uri uri2) {
        this.mediaId = str;
        this.title = charSequence;
        this.subTitle = charSequence2;
        this.description = charSequence3;
        this.icon = pixelMap;
        this.iconUri = uri;
        this.extras = pacMap;
        this.mediaUri = uri2;
    }

    private AVDescription(Parcel parcel) {
        if (parcel == null) {
            LOGGER.error("parcel cannot be null", new Object[0]);
            return;
        }
        this.mediaId = parcel.readString();
        this.title = parcel.readString();
        this.subTitle = parcel.readString();
        this.description = parcel.readString();
        this.icon = PixelMap.PRODUCER.createFromParcel(parcel);
        this.iconUri = Uri.readFromParcel(parcel);
        this.extras = PacMap.PRODUCER.createFromParcel(parcel);
        this.mediaUri = Uri.readFromParcel(parcel);
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            LOGGER.error("parcel out cannot be null", new Object[0]);
            return false;
        }
        String str = this.mediaId;
        if (str != null) {
            parcel.writeString(str);
        }
        CharSequence charSequence = this.title;
        if (charSequence != null) {
            parcel.writeString(charSequence.toString());
        }
        CharSequence charSequence2 = this.subTitle;
        if (charSequence2 != null) {
            parcel.writeString(charSequence2.toString());
        }
        CharSequence charSequence3 = this.description;
        if (charSequence3 != null) {
            parcel.writeString(charSequence3.toString());
        }
        parcel.writeSequenceable(this.icon);
        parcel.writeSequenceable(this.iconUri);
        parcel.writeSequenceable(this.extras);
        parcel.writeSequenceable(this.mediaUri);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            LOGGER.error("parcel in cannot be null", new Object[0]);
            return false;
        }
        try {
            this.mediaId = parcel.readString();
            this.title = parcel.readString();
            this.subTitle = parcel.readString();
            this.description = parcel.readString();
            this.icon = PixelMap.PRODUCER.createFromParcel(parcel);
            this.iconUri = Uri.readFromParcel(parcel);
            this.extras = PacMap.PRODUCER.createFromParcel(parcel);
            this.mediaUri = Uri.readFromParcel(parcel);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.error("unmarshalling parameter failed, message:%{public}s", e.getMessage());
            return false;
        } catch (ParcelException e2) {
            LOGGER.error("unmarshalling failed, message:%{public}s", e2.getMessage());
            return false;
        }
    }

    public static class Builder {
        private CharSequence description;
        private PacMap extras;
        private PixelMap icon;
        private Uri iconUri;
        private String mediaId;
        private Uri mediaUri;
        private CharSequence subTitle;
        private CharSequence title;

        public Builder setMediaId(String str) {
            this.mediaId = str;
            return this;
        }

        public Builder setTitle(CharSequence charSequence) {
            this.title = charSequence;
            return this;
        }

        public Builder setSubTitle(CharSequence charSequence) {
            this.subTitle = charSequence;
            return this;
        }

        public Builder setDescription(CharSequence charSequence) {
            this.description = charSequence;
            return this;
        }

        public Builder setIcon(PixelMap pixelMap) {
            this.icon = pixelMap;
            return this;
        }

        public Builder setIconUri(Uri uri) {
            this.iconUri = uri;
            return this;
        }

        public Builder setExtras(PacMap pacMap) {
            this.extras = pacMap;
            return this;
        }

        public Builder setIMediaUri(Uri uri) {
            this.mediaUri = uri;
            return this;
        }

        public AVDescription build() {
            return new AVDescription(this.mediaId, this.title, this.subTitle, this.description, this.icon, this.iconUri, this.extras, this.mediaUri);
        }
    }
}

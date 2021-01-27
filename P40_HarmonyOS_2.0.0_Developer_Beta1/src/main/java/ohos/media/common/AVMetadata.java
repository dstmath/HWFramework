package ohos.media.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ohos.media.common.AVDescription;
import ohos.media.image.PixelMap;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public final class AVMetadata implements Sequenceable {
    private static final Map<String, Integer> AV_METADATA_MAPPER;
    public static final Sequenceable.Producer<AVMetadata> CREATOR = new Sequenceable.Producer<AVMetadata>() {
        /* class ohos.media.common.AVMetadata.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public AVMetadata createFromParcel(Parcel parcel) {
            return new AVMetadata(parcel);
        }
    };
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVMetadata.class);
    private static final int PRIORITY_DESCRIPTION_LEN = 2;
    @AVTextKey
    private static final String[] PRIORITY_MEDIA_DESCRIPTION = {AVTextKey.TITLE, AVTextKey.ARTIST, AVTextKey.ALBUM, AVTextKey.ALBUM_ARTIST, AVTextKey.WRITER, AVTextKey.AUTHOR, AVTextKey.COMPOSER};
    @AVPixelMapKey
    private static final String[] PRIORITY_PIXELMAP_KEY = {AVPixelMapKey.ICON};
    @AVTextKey
    private static final String[] PRIORITY_URI_KEY = {AVTextKey.ICON_URI};
    private AVDescription avDescription;
    private final PacMap pacMap;

    public @interface AVLongKey {
        public static final String DISC = "ohos.av.metadata.DISC_NUMBER";
        public static final String DURATION = "ohos.av.metadata.DURATION";
        public static final String TOTAL_TRACKS = "ohos.av.metadata.NUM_TRACKS";
        public static final String TRACK = "ohos.av.metadata.TRACK_NUMBER";
        public static final String YEAR = "ohos.av.metadata.YEAR";
    }

    private @interface AVMetadataType {
        public static final int AV_METADATA_TYPE_INVALID = -1;
        public static final int AV_METADATA_TYPE_LONG = 0;
        public static final int AV_METADATA_TYPE_PIXELMAP = 2;
        public static final int AV_METADATA_TYPE_TEXT = 1;
    }

    public @interface AVPixelMapKey {
        public static final String ICON = "ohos.av.metadata.DISPLAY_ICON";
    }

    public @interface AVTextKey {
        public static final String ALBUM = "ohos.av.metadata.ALBUM";
        public static final String ALBUM_ARTIST = "ohos.av.metadata.ALBUM_ARTIST";
        public static final String ARTIST = "ohos.av.metadata.ARTIST";
        public static final String AUTHOR = "ohos.av.metadata.AUTHOR";
        public static final String COMPILATION = "ohos.av.metadata.COMPILATION";
        public static final String COMPOSER = "ohos.av.metadata.COMPOSER";
        public static final String DATE = "ohos.av.metadata.DATE";
        public static final String GENRE = "ohos.av.metadata.GENRE";
        public static final String ICON_URI = "ohos.av.metadata.DISPLAY_ICON_URI";
        public static final String META_ID = "ohos.av.metadata.MEDIA_ID";
        public static final String META_URI = "ohos.av.metadata.MEDIA_URI";
        public static final String SUBTITLE = "ohos.av.metadata.DISPLAY_SUBTITLE";
        public static final String TITLE = "ohos.av.metadata.TITLE";
        public static final String WRITER = "ohos.av.metadata.WRITER";
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(AVTextKey.TITLE, 1);
        hashMap.put(AVTextKey.ARTIST, 1);
        hashMap.put(AVLongKey.DURATION, 0);
        hashMap.put(AVTextKey.ALBUM, 1);
        hashMap.put(AVTextKey.AUTHOR, 1);
        hashMap.put(AVTextKey.WRITER, 1);
        hashMap.put(AVTextKey.COMPOSER, 1);
        hashMap.put(AVTextKey.COMPILATION, 1);
        hashMap.put(AVTextKey.DATE, 1);
        hashMap.put(AVLongKey.YEAR, 0);
        hashMap.put(AVTextKey.GENRE, 1);
        hashMap.put(AVLongKey.TRACK, 0);
        hashMap.put(AVLongKey.TOTAL_TRACKS, 0);
        hashMap.put(AVLongKey.DISC, 0);
        hashMap.put(AVTextKey.ALBUM_ARTIST, 1);
        hashMap.put(AVTextKey.SUBTITLE, 1);
        hashMap.put(AVPixelMapKey.ICON, 2);
        hashMap.put(AVTextKey.ICON_URI, 1);
        hashMap.put(AVTextKey.META_ID, 1);
        hashMap.put(AVTextKey.META_URI, 1);
        AV_METADATA_MAPPER = Collections.unmodifiableMap(hashMap);
    }

    private AVMetadata(PacMap pacMap2) {
        this.pacMap = pacMap2;
    }

    private AVMetadata(Parcel parcel) {
        this.pacMap = PacMap.PRODUCER.createFromParcel(parcel);
    }

    public boolean hasKey(String str) {
        return this.pacMap.hasKey(str);
    }

    public CharSequence getText(@AVTextKey String str) {
        return this.pacMap.getString(str);
    }

    public String getString(@AVTextKey String str) {
        return this.pacMap.getString(str);
    }

    public long getLong(@AVLongKey String str) {
        return this.pacMap.getLongValue(str, 0);
    }

    public Set<String> getKeysSet() {
        return this.pacMap.getKeys();
    }

    public PixelMap getPixelMap(@AVPixelMapKey String str) {
        try {
            return (PixelMap) this.pacMap.getObjectValue(str).get();
        } catch (Exception e) {
            LOGGER.error("getPixelMap failed, key: %{public}s, ex: %{public}s", str, e.getMessage());
            return null;
        }
    }

    public Object getObject(String str) {
        return this.pacMap.getObjectValue(str).orElse(null);
    }

    public AVDescription getAVDescription() {
        AVDescription aVDescription = this.avDescription;
        if (aVDescription != null) {
            return aVDescription;
        }
        AVDescription.Builder builder = new AVDescription.Builder();
        builder.setMediaId(getString(AVTextKey.META_ID));
        setTitleDescription(builder);
        String[] strArr = PRIORITY_PIXELMAP_KEY;
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            PixelMap pixelMap = getPixelMap(strArr[i2]);
            if (pixelMap != null) {
                builder.setIcon(pixelMap);
                break;
            }
            i2++;
        }
        String[] strArr2 = PRIORITY_URI_KEY;
        int length2 = strArr2.length;
        while (true) {
            if (i >= length2) {
                break;
            }
            String string = getString(strArr2[i]);
            if (!isEmptyText(string)) {
                builder.setIconUri(Uri.parse(string));
                break;
            }
            i++;
        }
        String string2 = getString(AVTextKey.META_URI);
        if (!isEmptyText(string2)) {
            builder.setIMediaUri(Uri.parse(string2));
        }
        this.avDescription = builder.build();
        return this.avDescription;
    }

    private void setTitleDescription(AVDescription.Builder builder) {
        CharSequence[] charSequenceArr = new CharSequence[2];
        CharSequence text = getText(AVTextKey.TITLE);
        if (!isEmptyText(text)) {
            charSequenceArr[0] = text;
            charSequenceArr[1] = getText(AVTextKey.SUBTITLE);
        } else {
            String[] strArr = PRIORITY_MEDIA_DESCRIPTION;
            int i = 0;
            for (String str : strArr) {
                if (i >= charSequenceArr.length) {
                    break;
                }
                CharSequence text2 = getText(str);
                if (!isEmptyText(text2)) {
                    charSequenceArr[i] = text2;
                    i++;
                }
            }
        }
        builder.setTitle(charSequenceArr[0]);
        builder.setSubTitle(charSequenceArr[1]);
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        Objects.requireNonNull(parcel, "parcel in cannot be null");
        PacMap pacMap2 = this.pacMap;
        if (pacMap2 == null) {
            parcel.writeInt(-1);
            return false;
        }
        parcel.writeSequenceable(pacMap2);
        return true;
    }

    private boolean isEmptyText(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static final class Builder {
        private final PacMap pacMap;

        public Builder() {
            this.pacMap = new PacMap();
        }

        public Builder(AVMetadata aVMetadata) {
            this.pacMap = aVMetadata.pacMap;
        }

        public Builder setText(@AVTextKey String str, CharSequence charSequence) {
            if (!AVMetadata.AV_METADATA_MAPPER.containsKey(str) || ((Integer) AVMetadata.AV_METADATA_MAPPER.get(str)).intValue() == 1) {
                this.pacMap.putString(str, charSequence.toString());
                return this;
            }
            throw new IllegalArgumentException("The " + str + " mapper text is incorrect");
        }

        public Builder setString(@AVTextKey String str, String str2) {
            if (!AVMetadata.AV_METADATA_MAPPER.containsKey(str) || ((Integer) AVMetadata.AV_METADATA_MAPPER.get(str)).intValue() == 1) {
                this.pacMap.putString(str, str2);
                return this;
            }
            throw new IllegalArgumentException("The " + str + " mapper string is incorrect");
        }

        public Builder setLong(@AVLongKey String str, long j) {
            if (!AVMetadata.AV_METADATA_MAPPER.containsKey(str) || ((Integer) AVMetadata.AV_METADATA_MAPPER.get(str)).intValue() == 0) {
                this.pacMap.putLongValue(str, j);
                return this;
            }
            throw new IllegalArgumentException("The " + str + " mapper long is incorrect");
        }

        public Builder setPixelMap(@AVPixelMapKey String str, PixelMap pixelMap) {
            if (!AVMetadata.AV_METADATA_MAPPER.containsKey(str) || ((Integer) AVMetadata.AV_METADATA_MAPPER.get(str)).intValue() == 2) {
                this.pacMap.putSequenceableObject(str, pixelMap);
                return this;
            }
            throw new IllegalArgumentException("The " + str + " mapper pixelmap is incorrect");
        }

        public AVMetadata build() {
            return new AVMetadata(this.pacMap);
        }
    }
}

package ohos.media.common.sessioncore;

import ohos.media.common.AVDescription;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AVElement implements Sequenceable {
    public static final int AVELEMENT_FLAG_PLAYABLE = 2;
    public static final int AVELEMENT_FLAG_SCANNABLE = 1;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVElement.class);
    private AVDescription description;
    private int flags;

    public AVElement(AVDescription aVDescription, int i) throws IllegalArgumentException {
        if (aVDescription != null) {
            String mediaId = aVDescription.getMediaId();
            if (mediaId == null || mediaId.length() == 0) {
                LOGGER.error("mediaId cannot be null or empty", new Object[0]);
                throw new IllegalArgumentException("description must have a non-empty media id");
            }
            this.flags = i;
            this.description = aVDescription;
            return;
        }
        LOGGER.error("description cannot be null", new Object[0]);
        throw new IllegalArgumentException("description cannot be null");
    }

    public int getFlags() {
        return this.flags;
    }

    public boolean isScannable() {
        return (this.flags & 1) != 0;
    }

    public boolean isPlayable() {
        return (this.flags & 2) != 0;
    }

    public AVDescription getAVDescription() {
        return this.description;
    }

    public String getMediaId() {
        return this.description.getMediaId();
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            LOGGER.error("Parcel out cannot be null", new Object[0]);
            return false;
        }
        parcel.writeInt(this.flags);
        return this.description.marshalling(parcel);
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            LOGGER.error("Parcel in cannot be null", new Object[0]);
            return false;
        }
        this.flags = parcel.readInt();
        return this.description.unmarshalling(parcel);
    }
}

package ohos.media.common.sessioncore;

import ohos.media.common.AVDescription;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class AVQueueElement implements Sequenceable {
    public static final Sequenceable.Producer<AVQueueElement> CREATOR = new Sequenceable.Producer<AVQueueElement>() {
        /* class ohos.media.common.sessioncore.AVQueueElement.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public AVQueueElement createFromParcel(Parcel parcel) {
            return new AVQueueElement(parcel);
        }
    };
    public static final long INVALID_ID = -1;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVQueueElement.class);
    private AVDescription description;
    private long elementId;

    public AVQueueElement(AVDescription aVDescription, long j) {
        this.description = null;
        this.elementId = -1;
        if (aVDescription == null) {
            LOGGER.error("description cannot be null", new Object[0]);
        } else if (j == -1) {
            LOGGER.error("elementId is invalid", new Object[0]);
        } else {
            this.description = aVDescription;
            this.elementId = j;
        }
    }

    private AVQueueElement(Parcel parcel) {
        this.description = null;
        this.elementId = -1;
        this.elementId = parcel.readLong();
    }

    public AVDescription getDescription() {
        return this.description;
    }

    public long getElementId() {
        return this.elementId;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            LOGGER.error("Parcel out cannot be null", new Object[0]);
            return false;
        }
        this.description.marshalling(parcel);
        parcel.writeLong(this.elementId);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            LOGGER.error("Parcel in cannot be null", new Object[0]);
            return false;
        }
        this.description.unmarshalling(parcel);
        this.elementId = parcel.readLong();
        return true;
    }
}

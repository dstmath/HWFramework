package ohos.event.notification;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class MessageUser implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    public static final Sequenceable.Producer<MessageUser> PRODUCER = $$Lambda$MessageUser$gfdVTC5E9FGvEaWetajvuCq__Dk.INSTANCE;
    private static final String TAG = "MessageUser";
    private boolean important;
    private String key;
    private boolean machine;
    private String name;
    private PixelMap pixelMap;
    private String uri;

    static /* synthetic */ MessageUser lambda$static$0(Parcel parcel) {
        MessageUser messageUser = new MessageUser();
        messageUser.unmarshalling(parcel);
        return messageUser;
    }

    public String getName() {
        return this.name;
    }

    public MessageUser setName(String str) {
        this.name = str;
        return this;
    }

    public PixelMap getPixelMap() {
        return this.pixelMap;
    }

    public MessageUser setPixelMap(PixelMap pixelMap2) {
        this.pixelMap = pixelMap2;
        return this;
    }

    public String getUri() {
        return this.uri;
    }

    public MessageUser setUri(String str) {
        this.uri = str;
        return this;
    }

    public String getKey() {
        return this.key;
    }

    public MessageUser setKey(String str) {
        this.key = str;
        return this;
    }

    public boolean isUserImportant() {
        return this.important;
    }

    public MessageUser setUserAsImportant(boolean z) {
        this.important = z;
        return this;
    }

    public boolean isMachine() {
        return this.machine;
    }

    public MessageUser setMachine(boolean z) {
        this.machine = z;
        return this;
    }

    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeString(this.name)) {
            HiLog.warn(LABEL, "MessageUser: marshalling write name failed.", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.uri)) {
            HiLog.warn(LABEL, "MessageUser: marshalling write uri failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.important)) {
            HiLog.warn(LABEL, "MessageUser: marshalling write important failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.machine)) {
            HiLog.warn(LABEL, "MessageUser: marshalling write machine failed.", new Object[0]);
            return false;
        } else if (parcel.writeString(this.key)) {
            return true;
        } else {
            HiLog.warn(LABEL, "MessageUser: marshalling write key failed.", new Object[0]);
            return false;
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.name = parcel.readString();
        this.uri = parcel.readString();
        this.important = parcel.readBoolean();
        this.machine = parcel.readBoolean();
        this.key = parcel.readString();
        return true;
    }
}

package ohos.nfc.tag;

import java.util.Optional;
import ohos.aafwk.content.IntentParams;

public class NfcVTag extends TagManager {
    private static final String EXTRA_DSFID = "dsfid";
    private static final String EXTRA_RESP_FLAGS = "respflags";
    private byte mDsfId;
    private byte mResponseFlags;

    public static Optional<NfcVTag> getInstance(TagInfo tagInfo) {
        if (!tagInfo.isProfileSupported(4)) {
            return Optional.empty();
        }
        return Optional.of(new NfcVTag(tagInfo));
    }

    public NfcVTag(TagInfo tagInfo) {
        super(tagInfo, 5);
        IntentParams orElse = tagInfo.getProfileExtras(4).orElse(null);
        if (orElse != null) {
            if (orElse.getParam(EXTRA_RESP_FLAGS) instanceof Byte) {
                this.mResponseFlags = ((Byte) orElse.getParam(EXTRA_RESP_FLAGS)).byteValue();
            }
            if (orElse.getParam(EXTRA_DSFID) instanceof Byte) {
                this.mDsfId = ((Byte) orElse.getParam(EXTRA_DSFID)).byteValue();
            }
        }
    }

    public byte getResponseFlags() {
        return this.mResponseFlags;
    }

    public byte getDsfId() {
        return this.mDsfId;
    }
}

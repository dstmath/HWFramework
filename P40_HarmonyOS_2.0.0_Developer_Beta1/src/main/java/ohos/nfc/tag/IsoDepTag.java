package ohos.nfc.tag;

import java.util.Arrays;
import java.util.Optional;
import ohos.aafwk.content.IntentParams;

public class IsoDepTag extends TagManager {
    public static final String EXTRA_HILAYER_RESP = "hiresp";
    public static final String EXTRA_HIST_BYTES = "histbytes";
    private byte[] mHiLayerResponse = null;
    private byte[] mHistBytes = null;

    public static Optional<IsoDepTag> getInstance(TagInfo tagInfo) {
        if (!tagInfo.isProfileSupported(3)) {
            return Optional.empty();
        }
        return Optional.of(new IsoDepTag(tagInfo));
    }

    public IsoDepTag(TagInfo tagInfo) {
        super(tagInfo, 3);
        IntentParams orElse;
        if (tagInfo.isProfileSupported(3) && (orElse = tagInfo.getProfileExtras(3).orElse(null)) != null) {
            if (orElse.getParam(EXTRA_HILAYER_RESP) instanceof byte[]) {
                this.mHiLayerResponse = (byte[]) orElse.getParam(EXTRA_HILAYER_RESP);
            }
            if (orElse.getParam(EXTRA_HIST_BYTES) instanceof byte[]) {
                this.mHistBytes = (byte[]) orElse.getParam(EXTRA_HIST_BYTES);
            }
        }
    }

    public byte[] getHistoricalBytes() {
        byte[] bArr = this.mHistBytes;
        return bArr != null ? Arrays.copyOf(bArr, bArr.length) : new byte[0];
    }

    public byte[] getHiLayerResponse() {
        byte[] bArr = this.mHiLayerResponse;
        return bArr != null ? Arrays.copyOf(bArr, bArr.length) : new byte[0];
    }
}

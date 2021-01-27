package ohos.nfc.tag;

import java.util.Optional;
import ohos.aafwk.content.IntentParams;

public class NfcFTag extends TagManager {
    private static final String EXTRA_PMM = "pmm";
    private static final String EXTRA_SC = "systemcode";
    private byte[] mPMm = null;
    private byte[] mSystemCode = null;

    public static Optional<NfcFTag> getInstance(TagInfo tagInfo) {
        if (!tagInfo.isProfileSupported(4)) {
            return Optional.empty();
        }
        return Optional.of(new NfcFTag(tagInfo));
    }

    public NfcFTag(TagInfo tagInfo) {
        super(tagInfo, 4);
        IntentParams orElse;
        if (tagInfo.isProfileSupported(4) && (orElse = tagInfo.getProfileExtras(4).orElse(null)) != null) {
            if (orElse.getParam(EXTRA_SC) instanceof byte[]) {
                this.mSystemCode = (byte[]) orElse.getParam(EXTRA_SC);
            }
            if (orElse.getParam(EXTRA_PMM) instanceof byte[]) {
                this.mPMm = (byte[]) orElse.getParam(EXTRA_PMM);
            }
        }
    }

    public byte[] getSystemCode() {
        return this.mSystemCode;
    }

    public byte[] getPMm() {
        return this.mPMm;
    }
}

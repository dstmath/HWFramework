package ohos.nfc.tag;

import java.util.Arrays;
import ohos.aafwk.content.IntentParams;

public class NfcATag extends TagManager {
    private static final int ATQA_LENGTH = 2;
    public static final String EXTRA_ATQA = "atqa";
    public static final String EXTRA_SAK = "sak";
    private byte[] mAtqa;
    private short mSak;

    public static NfcATag getInstance(TagInfo tagInfo) {
        if (tagInfo == null) {
            throw new NullPointerException("NfcATag tagInfo is null");
        } else if (!tagInfo.isProfileSupported(1)) {
            return null;
        } else {
            return new NfcATag(tagInfo);
        }
    }

    private NfcATag(TagInfo tagInfo) {
        super(tagInfo, 1);
        IntentParams orElse;
        IntentParams orElse2;
        this.mSak = 0;
        this.mAtqa = new byte[2];
        this.mSak = 0;
        if (tagInfo.isProfileSupported(8) && (orElse2 = tagInfo.getProfileExtras(8).orElse(null)) != null) {
            this.mSak = ((Short) orElse2.getParam(EXTRA_SAK)).shortValue();
        }
        if (tagInfo.isProfileSupported(1) && (orElse = tagInfo.getProfileExtras(1).orElse(null)) != null) {
            this.mSak = (short) (this.mSak | ((Short) orElse.getParam(EXTRA_SAK)).shortValue());
            if (orElse.getParam(EXTRA_ATQA) instanceof byte[]) {
                this.mAtqa = (byte[]) orElse.getParam(EXTRA_ATQA);
            }
        }
    }

    public short getSak() {
        return this.mSak;
    }

    public byte[] getAtqa() {
        byte[] bArr = this.mAtqa;
        return bArr != null ? Arrays.copyOf(bArr, bArr.length) : new byte[0];
    }
}

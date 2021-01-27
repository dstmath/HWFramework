package ohos.nfc.tag;

import java.io.IOException;
import ohos.aafwk.content.IntentParams;

public class MifareUltralightTag extends TagManager {
    public static final String EXTRA_IS_ULC = "isulc";
    private static final int MAX_PAGE_COUNT = 256;
    public static final int MIFARE_ULTRALIGHT = 1;
    public static final int MIFARE_ULTRALIGHT_C = 2;
    public static final int MIFARE_UNKNOWN = -1;
    private static final int NFC_MANUFACTURER_ID = 4;
    private static final int SAK_00 = 0;
    private int mMifareType = -1;

    public static MifareUltralightTag getInstance(TagInfo tagInfo) {
        if (tagInfo == null) {
            throw new NullPointerException("MifareUltralightTag tagInfo is null");
        } else if (!tagInfo.isProfileSupported(1)) {
            return null;
        } else {
            return new MifareUltralightTag(tagInfo);
        }
    }

    private MifareUltralightTag(TagInfo tagInfo) {
        super(tagInfo, 9);
        IntentParams orElse;
        NfcATag instance = NfcATag.getInstance(tagInfo);
        short s = -1;
        if ((instance != null ? instance.getSak() : s) == 0 && tagInfo.getTagId()[0] == 4 && (orElse = tagInfo.getProfileExtras(9).orElse(null)) != null && (orElse.getParam(EXTRA_IS_ULC) instanceof Boolean)) {
            if (((Boolean) orElse.getParam(EXTRA_IS_ULC)).booleanValue()) {
                this.mMifareType = 2;
            } else {
                this.mMifareType = 1;
            }
        }
    }

    public int getMifareType() {
        return this.mMifareType;
    }

    public byte[] readFourPages(int i) throws IOException {
        checkPageIdx(i);
        checkConnected();
        return sendData(new byte[]{48, (byte) i});
    }

    public void writeOnePage(int i, byte[] bArr) throws IOException {
        checkPageIdx(i);
        checkConnected();
        byte[] bArr2 = new byte[(bArr.length + 2)];
        bArr2[0] = -94;
        bArr2[1] = (byte) i;
        System.arraycopy(bArr, 0, bArr2, 2, bArr.length);
        sendData(bArr2);
    }

    private static void checkPageIdx(int i) {
        if (i < 0 || i >= 256) {
            throw new IllegalArgumentException("Invalid page index: " + i);
        }
    }
}

package ohos.nfc.tag;

import ohos.aafwk.content.IntentParams;

public class NfcBTag extends TagManager {
    public static final String EXTRA_RESPAPPDATA = "appdata";
    public static final String EXTRA_RESPPROTOCOL = "protinfo";
    private byte[] mRespAppData;
    private byte[] mRespProtocol;

    public static NfcBTag getInstance(TagInfo tagInfo) {
        if (tagInfo == null) {
            throw new NullPointerException("NfcBTag tagInfo is null");
        } else if (!tagInfo.isProfileSupported(2)) {
            return null;
        } else {
            return new NfcBTag(tagInfo);
        }
    }

    private NfcBTag(TagInfo tagInfo) {
        super(tagInfo, 2);
        IntentParams orElse;
        if (tagInfo.isProfileSupported(2) && (orElse = tagInfo.getProfileExtras(2).orElse(null)) != null) {
            if (orElse.getParam(EXTRA_RESPAPPDATA) instanceof byte[]) {
                this.mRespAppData = (byte[]) orElse.getParam(EXTRA_RESPAPPDATA);
            }
            if (orElse.getParam(EXTRA_RESPPROTOCOL) instanceof byte[]) {
                this.mRespProtocol = (byte[]) orElse.getParam(EXTRA_RESPPROTOCOL);
            }
        }
    }

    public byte[] getRespAppData() {
        return this.mRespAppData;
    }

    public byte[] getRespProtocol() {
        return this.mRespProtocol;
    }
}

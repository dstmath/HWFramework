package android.media.hwmnote;

import java.io.IOException;

public class HwMnoteInterface {
    private static final String TAG = "HwMnoteInterface";
    private IHwMnoteInterface mHwMnoteInterface;

    public HwMnoteInterface() {
        this.mHwMnoteInterface = null;
        this.mHwMnoteInterface = new HwMnoteInterfaceImpl();
    }

    public void readHwMnote(byte[] mnote) throws IOException {
        IHwMnoteInterface iHwMnoteInterface = this.mHwMnoteInterface;
        if (iHwMnoteInterface != null) {
            iHwMnoteInterface.readHwMnote(mnote);
        }
    }

    public byte[] getHwMnote() throws IOException {
        IHwMnoteInterface iHwMnoteInterface = this.mHwMnoteInterface;
        if (iHwMnoteInterface != null) {
            return iHwMnoteInterface.getHwMnote();
        }
        return new byte[0];
    }

    public Integer getTagIntValue(int tagId) {
        IHwMnoteInterface iHwMnoteInterface = this.mHwMnoteInterface;
        if (iHwMnoteInterface != null) {
            return iHwMnoteInterface.getTagIntValue(tagId);
        }
        return null;
    }

    public Long getTagLongValue(int tagId) {
        IHwMnoteInterface iHwMnoteInterface = this.mHwMnoteInterface;
        if (iHwMnoteInterface != null) {
            return iHwMnoteInterface.getTagLongValue(tagId);
        }
        return null;
    }

    public byte[] getTagByteValues(int tagId) {
        IHwMnoteInterface iHwMnoteInterface = this.mHwMnoteInterface;
        if (iHwMnoteInterface != null) {
            return iHwMnoteInterface.getTagByteValues(tagId);
        }
        return new byte[0];
    }

    public boolean setTagValue(int tagId, Object value) {
        IHwMnoteInterface iHwMnoteInterface = this.mHwMnoteInterface;
        if (iHwMnoteInterface != null) {
            return iHwMnoteInterface.setTagValue(tagId, value);
        }
        return false;
    }
}

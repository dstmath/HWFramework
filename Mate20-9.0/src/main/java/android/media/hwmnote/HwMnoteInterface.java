package android.media.hwmnote;

import android.common.HwFrameworkFactory;
import java.io.IOException;

public class HwMnoteInterface {
    private static final String TAG = "HwMnoteInterface";
    private IHwMnoteInterface mHwMnoteInterface;

    public HwMnoteInterface() {
        this.mHwMnoteInterface = null;
        this.mHwMnoteInterface = HwFrameworkFactory.getHwMnoteInterface();
    }

    public void readHwMnote(byte[] mnote) throws IOException {
        if (this.mHwMnoteInterface != null) {
            this.mHwMnoteInterface.readHwMnote(mnote);
        }
    }

    public byte[] getHwMnote() throws IOException {
        if (this.mHwMnoteInterface != null) {
            return this.mHwMnoteInterface.getHwMnote();
        }
        return new byte[0];
    }

    public Integer getTagIntValue(int tagId) {
        if (this.mHwMnoteInterface != null) {
            return this.mHwMnoteInterface.getTagIntValue(tagId);
        }
        return null;
    }

    public Long getTagLongValue(int tagId) {
        if (this.mHwMnoteInterface != null) {
            return this.mHwMnoteInterface.getTagLongValue(tagId);
        }
        return null;
    }

    public byte[] getTagByteValues(int tagId) {
        if (this.mHwMnoteInterface != null) {
            return this.mHwMnoteInterface.getTagByteValues(tagId);
        }
        return new byte[0];
    }

    public boolean setTagValue(int tagId, Object val) {
        if (this.mHwMnoteInterface != null) {
            return this.mHwMnoteInterface.setTagValue(tagId, val);
        }
        return false;
    }
}

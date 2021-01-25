package android.media.hwmnote;

import java.io.IOException;

public interface IHwMnoteInterface {
    byte[] getHwMnote() throws IOException;

    byte[] getTagByteValues(int i);

    Integer getTagIntValue(int i);

    Long getTagLongValue(int i);

    void readHwMnote(byte[] bArr) throws IOException;

    boolean setTagValue(int i, Object obj);
}

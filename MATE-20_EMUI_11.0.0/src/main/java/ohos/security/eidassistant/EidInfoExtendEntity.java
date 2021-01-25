package ohos.security.eidassistant;

import ohos.security.eidassistant.EidAssistant;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

/* access modifiers changed from: package-private */
public class EidInfoExtendEntity implements Sequenceable {
    private byte[] content;
    private int[] contentLen;

    EidInfoExtendEntity(EidAssistant.EidInfo eidInfo) {
        this(eidInfo.getContent(), new int[]{eidInfo.getContentLen()});
    }

    EidInfoExtendEntity(byte[] bArr, int[] iArr) {
        this.contentLen = new int[1];
        this.content = bArr;
        this.contentLen[0] = iArr[0];
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] bArr) {
        this.content = bArr;
    }

    public int[] getContentLen() {
        return this.contentLen;
    }

    public void setContentLen(int[] iArr) {
        this.contentLen = iArr;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeByteArray(this.content);
        parcel.writeIntArray(this.contentLen);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.content = parcel.readByteArray();
        this.contentLen = parcel.readIntArray();
        return true;
    }
}

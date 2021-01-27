package ohos.security.eidassistant;

import ohos.security.eidassistant.EidAssistant;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

class EidInfoEntity implements Sequenceable {
    private byte[] content;
    private int contentLen;

    EidInfoEntity(EidAssistant.EidInfo eidInfo) {
        this(eidInfo.getContent(), eidInfo.getContentLen());
    }

    EidInfoEntity(byte[] bArr, int i) {
        this.contentLen = 0;
        this.content = bArr;
        this.contentLen = i;
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] bArr) {
        this.content = bArr;
    }

    public int getContentLen() {
        return this.contentLen;
    }

    public void setContentLen(int i) {
        this.contentLen = i;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeByteArray(this.content);
        parcel.writeInt(this.contentLen);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.content = parcel.readByteArray();
        this.contentLen = parcel.readInt();
        return true;
    }
}

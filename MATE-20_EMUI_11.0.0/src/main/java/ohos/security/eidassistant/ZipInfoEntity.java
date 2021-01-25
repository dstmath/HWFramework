package ohos.security.eidassistant;

import ohos.security.eidassistant.EidAssistant;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

/* access modifiers changed from: package-private */
public class ZipInfoEntity implements Sequenceable {
    private EidInfoEntity certificate;
    private int encryptionMethod;
    private EidInfoEntity hash;
    private EidInfoEntity imageZip;

    ZipInfoEntity(EidAssistant.ImageZipContainer imageZipContainer, EidAssistant.EncryptionFactor encryptionFactor) {
        this.hash = new EidInfoEntity(imageZipContainer.getHash(), imageZipContainer.getHashLen());
        this.imageZip = new EidInfoEntity(imageZipContainer.getImageZip(), imageZipContainer.getImageZipLen());
        this.certificate = new EidInfoEntity(encryptionFactor.getCertificate(), encryptionFactor.getCertificateLen());
        this.encryptionMethod = encryptionFactor.getEncryptionMethod();
    }

    public EidInfoEntity getHash() {
        return this.hash;
    }

    public EidInfoEntity getImageZip() {
        return this.imageZip;
    }

    public EidInfoEntity getCertificate() {
        return this.certificate;
    }

    public int getEncryptionMethod() {
        return this.encryptionMethod;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        this.hash.marshalling(parcel);
        this.imageZip.marshalling(parcel);
        this.certificate.marshalling(parcel);
        parcel.writeInt(this.encryptionMethod);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.hash.unmarshalling(parcel);
        this.imageZip.unmarshalling(parcel);
        this.certificate.unmarshalling(parcel);
        this.encryptionMethod = parcel.readInt();
        return true;
    }
}

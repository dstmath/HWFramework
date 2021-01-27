package ohos.security.eidassistant;

import ohos.sysability.samgr.SysAbilityManager;

public class EidAssistant {
    private static final int MAX_CONTENT_LEN = 1048576;
    private static final int RET_DEFAULT_ERROR_VALUE = -2001;
    private static final int SA_ID = 3599;
    private static volatile EidAssistant sInstance;
    private IEidAssistant proxy = new EidAssistantProxy(SysAbilityManager.getSysAbility(SA_ID));

    private EidAssistant() {
    }

    public static EidAssistant getInstance() {
        if (sInstance == null) {
            synchronized (EidAssistant.class) {
                if (sInstance == null) {
                    sInstance = new EidAssistant();
                }
            }
        }
        return sInstance;
    }

    public int initEid(EidInfo eidInfo, EidInfo eidInfo2, EidInfo eidInfo3) {
        if (eidInfo == null || eidInfo2 == null || eidInfo3 == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        return this.proxy.eidInit(new EidInfoEntity(eidInfo), new EidInfoEntity(eidInfo2), new EidInfoEntity(eidInfo3));
    }

    public int finishEid() {
        return this.proxy.eidFinish();
    }

    public int getEidImage(ControlWord controlWord, EidInfo eidInfo, EidInfo eidInfo2, EidInfo eidInfo3) {
        if (controlWord == null || eidInfo == null || eidInfo2 == null || eidInfo3 == null || controlWord.getEncryptionMethod() < 0 || controlWord.getTransportCounter() < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        ControlWordEntity controlWordEntity = new ControlWordEntity(controlWord);
        EidInfoEntity eidInfoEntity = new EidInfoEntity(eidInfo);
        EidInfoExtendEntity eidInfoExtendEntity = new EidInfoExtendEntity(eidInfo2);
        EidInfoExtendEntity eidInfoExtendEntity2 = new EidInfoExtendEntity(eidInfo3);
        int eidGetImage = this.proxy.eidGetImage(controlWordEntity, eidInfoEntity, eidInfoExtendEntity, eidInfoExtendEntity2);
        eidInfo2.setContent(eidInfoExtendEntity.getContent());
        eidInfo2.setContentLen(eidInfoExtendEntity.getContentLen()[0]);
        eidInfo3.setContent(eidInfoExtendEntity2.getContent());
        eidInfo3.setContentLen(eidInfoExtendEntity2.getContentLen()[0]);
        return eidGetImage;
    }

    public int getEidUnsecImage(EidInfo eidInfo, ControlWord controlWord, EidInfo eidInfo2, EidInfo eidInfo3, EidInfo eidInfo4) {
        if (eidInfo == null || controlWord == null || eidInfo2 == null || eidInfo3 == null || eidInfo4 == null || controlWord.getEncryptionMethod() < 0 || controlWord.getTransportCounter() < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        EidInfoEntity eidInfoEntity = new EidInfoEntity(eidInfo);
        ControlWordEntity controlWordEntity = new ControlWordEntity(controlWord);
        EidInfoEntity eidInfoEntity2 = new EidInfoEntity(eidInfo2);
        EidInfoExtendEntity eidInfoExtendEntity = new EidInfoExtendEntity(eidInfo3);
        EidInfoExtendEntity eidInfoExtendEntity2 = new EidInfoExtendEntity(eidInfo4);
        int eidGetUnsecImage = this.proxy.eidGetUnsecImage(eidInfoEntity, controlWordEntity, eidInfoEntity2, eidInfoExtendEntity, eidInfoExtendEntity2);
        eidInfo3.setContent(eidInfoExtendEntity.getContent());
        eidInfo3.setContentLen(eidInfoExtendEntity.getContentLen()[0]);
        eidInfo4.setContent(eidInfoExtendEntity2.getContent());
        eidInfo4.setContentLen(eidInfoExtendEntity2.getContentLen()[0]);
        return eidGetUnsecImage;
    }

    public int getEidCertificateRequestMessage(EidInfo eidInfo) {
        if (eidInfo == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        EidInfoExtendEntity eidInfoExtendEntity = new EidInfoExtendEntity(eidInfo);
        int eidGetCertificateRequestMessage = this.proxy.eidGetCertificateRequestMessage(eidInfoExtendEntity);
        eidInfo.setContent(eidInfoExtendEntity.getContent());
        eidInfo.setContentLen(eidInfoExtendEntity.getContentLen()[0]);
        return eidGetCertificateRequestMessage;
    }

    public int getEidSignInfo(ControlWord controlWord, EidInfo eidInfo, EidInfo eidInfo2) {
        if (controlWord == null || eidInfo == null || eidInfo2 == null || controlWord.getEncryptionMethod() < 0 || controlWord.getTransportCounter() < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        ControlWordEntity controlWordEntity = new ControlWordEntity(controlWord);
        EidInfoEntity eidInfoEntity = new EidInfoEntity(eidInfo);
        EidInfoExtendEntity eidInfoExtendEntity = new EidInfoExtendEntity(eidInfo2);
        int eidSignInfo = this.proxy.eidSignInfo(controlWordEntity, eidInfoEntity, eidInfoExtendEntity);
        eidInfo2.setContent(eidInfoExtendEntity.getContent());
        eidInfo2.setContentLen(eidInfoExtendEntity.getContentLen()[0]);
        return eidSignInfo;
    }

    public int getEidIdentityInformation(EidInfo eidInfo) {
        if (eidInfo == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        EidInfoExtendEntity eidInfoExtendEntity = new EidInfoExtendEntity(eidInfo);
        int eidGetIdentityInformation = this.proxy.eidGetIdentityInformation(eidInfoExtendEntity);
        eidInfo.setContent(eidInfoExtendEntity.getContent());
        eidInfo.setContentLen(eidInfoExtendEntity.getContentLen()[0]);
        return eidGetIdentityInformation;
    }

    public int getEidFaceIsChanged(int i) {
        return i < 0 ? RET_DEFAULT_ERROR_VALUE : this.proxy.eidGetFaceIsChanged(i);
    }

    public String getEidVersion() {
        return this.proxy.eidGetVersion();
    }

    public int eidGetSecImageZip(ImageZipContainer imageZipContainer, CutCoordinate cutCoordinate, EncryptionFactor encryptionFactor, SecImageZip secImageZip) {
        if (imageZipContainer == null || encryptionFactor == null || cutCoordinate == null || secImageZip == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        ZipInfoEntity zipInfoEntity = new ZipInfoEntity(imageZipContainer, encryptionFactor);
        CoordinateEntity coordinateEntity = new CoordinateEntity(cutCoordinate);
        OutZipEntity outZipEntity = new OutZipEntity(secImageZip);
        int eidGetSecImgZip = this.proxy.eidGetSecImgZip(coordinateEntity, zipInfoEntity, outZipEntity);
        copyZipData(secImageZip, outZipEntity);
        return eidGetSecImgZip;
    }

    public int eidGetUnsecImageZip(ImageZipContainer imageZipContainer, EncryptionFactor encryptionFactor, SecImageZip secImageZip) {
        if (imageZipContainer == null || encryptionFactor == null || secImageZip == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        ZipInfoEntity zipInfoEntity = new ZipInfoEntity(imageZipContainer, encryptionFactor);
        OutZipEntity outZipEntity = new OutZipEntity(secImageZip);
        int eidGetUnsecImgZip = this.proxy.eidGetUnsecImgZip(zipInfoEntity, outZipEntity);
        copyZipData(secImageZip, outZipEntity);
        return eidGetUnsecImgZip;
    }

    private void copyZipData(SecImageZip secImageZip, OutZipEntity outZipEntity) {
        secImageZip.setDeSkey(outZipEntity.getDeSkey().getContent());
        secImageZip.setSecImage(outZipEntity.getOutImage().getContent());
        secImageZip.setDeSkeyLen(outZipEntity.getDeSkey().getContentLen());
        secImageZip.setSecImageLen(outZipEntity.getOutImage().getContentLen());
    }

    public static final class EidInfo {
        private byte[] content;
        private int contentLen = 0;

        public EidInfo(int i) throws IllegalArgumentException {
            if (i < 0 || i > 1048576) {
                throw new IllegalArgumentException("contentLen is invalid!");
            }
            this.content = new byte[i];
            this.contentLen = i;
        }

        public byte[] getContent() {
            byte[] bArr = this.content;
            return bArr != null ? (byte[]) bArr.clone() : new byte[0];
        }

        public void setContent(byte[] bArr) throws IllegalArgumentException {
            if (bArr != null) {
                int length = bArr.length;
                byte[] bArr2 = this.content;
                if (length <= bArr2.length) {
                    System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
                    return;
                }
            }
            throw new IllegalArgumentException("content is invalid!");
        }

        public int getContentLen() {
            return this.contentLen;
        }

        public void setContentLen(int i) throws IllegalArgumentException {
            if (i < 0 || i > 1048576) {
                throw new IllegalArgumentException("contentLen is invalid!");
            }
            this.contentLen = i;
        }
    }

    public static final class ControlWord {
        private int encryptionMethod;
        private int transportCounter;

        public int getTransportCounter() {
            return this.transportCounter;
        }

        public void setTransportCounter(int i) {
            this.transportCounter = i;
        }

        public int getEncryptionMethod() {
            return this.encryptionMethod;
        }

        public void setEncryptionMethod(int i) {
            this.encryptionMethod = i;
        }
    }

    public static final class CutCoordinate {
        private int down = 0;
        private int left = 0;
        private int right = 0;
        private int up = 0;

        public int getUp() {
            return this.up;
        }

        public void setUp(int i) {
            this.up = i;
        }

        public int getDown() {
            return this.down;
        }

        public void setDown(int i) {
            this.down = i;
        }

        public int getLeft() {
            return this.left;
        }

        public void setLeft(int i) {
            this.left = i;
        }

        public int getRight() {
            return this.right;
        }

        public void setRight(int i) {
            this.right = i;
        }
    }

    public static final class ImageZipContainer {
        private static final int MAX_HASH_LEN = 32;
        private static final int MAX_IMAGE_ZIP_LEN = 131072;
        private final byte[] hash = new byte[32];
        private int hashLen = 0;
        private final byte[] imageZip = new byte[131072];
        private int imageZipLen = 0;

        public int getHashLen() {
            return this.hashLen;
        }

        public void setHashLen(int i) throws IllegalArgumentException {
            if (i <= 0 || i > 32) {
                throw new IllegalArgumentException("hashLen is invalid!");
            }
            this.hashLen = i;
        }

        public int getImageZipLen() {
            return this.imageZipLen;
        }

        public void setImageZipLen(int i) throws IllegalArgumentException {
            if (i <= 0 || i > 131072) {
                throw new IllegalArgumentException("imageZipLen is invalid!");
            }
            this.imageZipLen = i;
        }

        public byte[] getHash() {
            byte[] bArr = this.hash;
            return bArr != null ? (byte[]) bArr.clone() : new byte[0];
        }

        public void setHash(byte[] bArr) throws IllegalArgumentException {
            if (bArr == null || bArr.length > 32) {
                throw new IllegalArgumentException("hash[] is invalid!");
            }
            System.arraycopy(bArr, 0, this.hash, 0, bArr.length);
        }

        public byte[] getImageZip() {
            byte[] bArr = this.imageZip;
            return bArr != null ? (byte[]) bArr.clone() : new byte[0];
        }

        public void setImageZip(byte[] bArr) throws IllegalArgumentException {
            if (bArr == null || bArr.length > 131072) {
                throw new IllegalArgumentException("imageZip[] is invalid!");
            }
            System.arraycopy(bArr, 0, this.imageZip, 0, bArr.length);
        }
    }

    public static final class EncryptionFactor {
        private static final int MAX_CERT_LEN = 8192;
        private final byte[] certificate = new byte[8192];
        private int certificateLen = 0;
        private int encryptionMethod = 0;

        public int getCertificateLen() {
            return this.certificateLen;
        }

        public byte[] getCertificate() {
            byte[] bArr = this.certificate;
            return bArr != null ? (byte[]) bArr.clone() : new byte[0];
        }

        public void setCertificate(byte[] bArr) throws IllegalArgumentException {
            if (bArr == null || bArr.length > 8192) {
                throw new IllegalArgumentException("certificate[] is invalid!");
            }
            System.arraycopy(bArr, 0, this.certificate, 0, bArr.length);
        }

        public void setCertificateLen(int i) throws IllegalArgumentException {
            if (i <= 0 || i > 8192) {
                throw new IllegalArgumentException("certificateLen is invalid!");
            }
            this.certificateLen = i;
        }

        public void setEncryptionMethod(int i) throws IllegalArgumentException {
            if (i >= 0) {
                this.encryptionMethod = i;
                return;
            }
            throw new IllegalArgumentException("encryptionMethod invalid!");
        }

        public int getEncryptionMethod() {
            return this.encryptionMethod;
        }
    }

    public static final class SecImageZip {
        private static final int MAX_DESKEY_LEN = 131072;
        private static final int MAX_SEC_IMAGE_LEN = 131072;
        private final byte[] deSkey = new byte[131072];
        private final int[] deSkeyLen = new int[1];
        private final byte[] secImage = new byte[131072];
        private final int[] secImageLen = new int[1];

        public byte[] getDeSkey() {
            byte[] bArr = this.deSkey;
            return bArr != null ? (byte[]) bArr.clone() : new byte[0];
        }

        public int[] getDeSkeyLen() {
            int[] iArr = this.deSkeyLen;
            return iArr != null ? (int[]) iArr.clone() : new int[0];
        }

        public byte[] getSecImage() {
            byte[] bArr = this.secImage;
            return bArr != null ? (byte[]) bArr.clone() : new byte[0];
        }

        public int[] getSecImageLen() {
            int[] iArr = this.secImageLen;
            return iArr != null ? (int[]) iArr.clone() : new int[0];
        }

        public void setDeSkey(byte[] bArr) throws IllegalArgumentException {
            if (bArr == null || bArr.length > 131072) {
                throw new IllegalArgumentException("deSkey is invalid");
            }
            System.arraycopy(bArr, 0, this.deSkey, 0, bArr.length);
        }

        public void setDeSkeyLen(int[] iArr) throws IllegalArgumentException {
            if (iArr == null || iArr.length < 1 || iArr[0] < 0) {
                throw new IllegalArgumentException("length is invalid");
            }
            this.deSkeyLen[0] = iArr[0];
        }

        public void setSecImage(byte[] bArr) {
            if (bArr == null || bArr.length > 131072) {
                throw new IllegalArgumentException("secImage is invalid");
            }
            System.arraycopy(bArr, 0, this.secImage, 0, bArr.length);
        }

        public void setSecImageLen(int[] iArr) throws IllegalArgumentException {
            if (iArr == null || iArr.length < 1 || iArr[0] < 0) {
                throw new IllegalArgumentException("length is invalid");
            }
            this.secImageLen[0] = iArr[0];
        }
    }
}

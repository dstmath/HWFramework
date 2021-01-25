package android.eidservice;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.security.ControlWordEntity;
import com.huawei.security.CoordinateEntity;
import com.huawei.security.EidInfoEntity;
import com.huawei.security.EidInfoExtendEntity;
import com.huawei.security.IHwEidPlugin;
import huawei.android.security.IHwSecurityService;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class HwEidServiceManager {
    private static final int DES_KEY_ENTITY_INDEX = 1;
    private static final int EID_INFO_EXTEND_LIST_NUMS = 2;
    private static final int HW_EID_PLUGIN_ID = 15;
    private static final int IMAGE_ENTITY_INDEX = 0;
    private static final int RET_DEFAULT_ERROR_VALUE = -2001;
    private static final int RET_EXCEPTION_WHEN_EID_FINISH_CALL = -2003;
    private static final int RET_EXCEPTION_WHEN_EID_INIT_CALL = -2002;
    private static final int RET_EXCEPTION_WHEN_GET_CERTIFICATE_CALL = -2006;
    private static final int RET_EXCEPTION_WHEN_GET_FACE_CHANGED_CALL = -2009;
    private static final int RET_EXCEPTION_WHEN_GET_IDENTITY_CALL = -2008;
    private static final int RET_EXCEPTION_WHEN_GET_IMAGE_CALL = -2004;
    private static final int RET_EXCEPTION_WHEN_GET_INFO_SIGN_CALL = -2007;
    private static final int RET_EXCEPTION_WHEN_GET_SEC_IMAGE_ZIP_CALL = -2010;
    private static final int RET_EXCEPTION_WHEN_GET_UNSEC_IMAGE_CALL = -2005;
    private static final int RET_EXCEPTION_WHEN_GET_UNSEC_IMAGE_ZIP_CALL = -2011;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HwEidServiceManager";
    private static volatile HwEidServiceManager instance = null;
    private IHwEidPlugin hwEidPlugin;
    private IHwSecurityService hwSecurityService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));

    private HwEidServiceManager() {
        if (this.hwSecurityService == null) {
            Log.e(TAG, "Error, securityservice was null");
        }
    }

    public static HwEidServiceManager getInstance() {
        if (instance == null) {
            synchronized (HwEidServiceManager.class) {
                if (instance == null) {
                    instance = new HwEidServiceManager();
                }
            }
        }
        return instance;
    }

    private Optional<IHwEidPlugin> getHwEidPlugin() {
        IHwEidPlugin iHwEidPlugin = this.hwEidPlugin;
        if (iHwEidPlugin != null) {
            return Optional.of(iHwEidPlugin);
        }
        IHwSecurityService iHwSecurityService = this.hwSecurityService;
        if (iHwSecurityService != null) {
            try {
                this.hwEidPlugin = IHwEidPlugin.Stub.asInterface(iHwSecurityService.querySecurityInterface(15));
                if (this.hwEidPlugin == null) {
                    Log.e(TAG, "Error, the obtained IHwEidPlugin is null");
                }
                return Optional.of(this.hwEidPlugin);
            } catch (RemoteException e) {
                Log.e(TAG, "Error, Get getHwEidPlugin failed");
            }
        }
        return Optional.empty();
    }

    public int eid_init(byte[] hwAid, int hwAidLen, byte[] eidAid, int eidAidLen, byte[] logo, int logoSize) {
        if (hwAid == null || eidAid == null || logo == null || hwAidLen < 0 || eidAidLen < 0 || logoSize < 0 || !getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        EidInfoEntity aidInfo = new EidInfoEntity();
        aidInfo.setContent(hwAid);
        aidInfo.setContentLen(hwAidLen);
        EidInfoEntity eidAidInfo = new EidInfoEntity();
        eidAidInfo.setContent(eidAid);
        eidAidInfo.setContentLen(eidAidLen);
        EidInfoEntity logoInfo = new EidInfoEntity();
        logoInfo.setContent(logo);
        logoInfo.setContentLen(logoSize);
        try {
            return this.hwEidPlugin.eidInit(aidInfo, eidAidInfo, logoInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid init jnis is invoked");
            return RET_EXCEPTION_WHEN_EID_INIT_CALL;
        }
    }

    public int eid_finish() {
        if (!getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        try {
            return this.hwEidPlugin.eidFinish();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid finish jni is invoked");
            return RET_EXCEPTION_WHEN_EID_FINISH_CALL;
        }
    }

    public int eid_get_image(int transpotCounter, int encryptionMethod, byte[] certificate, int certificateLen, byte[] image, int[] imageLen, byte[] deSkey, int[] deSkeyLen) {
        if (transpotCounter >= 0 && encryptionMethod >= 0 && certificate != null) {
            if (certificateLen >= 0) {
                if (image != null && imageLen != null && imageLen.length > 0 && imageLen[0] >= 0) {
                    if (deSkey != null) {
                        if (deSkeyLen != null && deSkeyLen.length > 0) {
                            if (deSkeyLen[0] >= 0) {
                                if (!getHwEidPlugin().isPresent()) {
                                    return RET_DEFAULT_ERROR_VALUE;
                                }
                                ControlWordEntity controlWord = new ControlWordEntity();
                                controlWord.setTransportCounter(transpotCounter);
                                controlWord.setEncryptionMethod(encryptionMethod);
                                EidInfoExtendEntity deSkeyInfo = new EidInfoExtendEntity();
                                deSkeyInfo.setContent(deSkey);
                                deSkeyInfo.setContentLen(deSkeyLen);
                                EidInfoEntity certificateInfo = new EidInfoEntity();
                                EidInfoExtendEntity imageInfo = new EidInfoExtendEntity();
                                certificateInfo.setContent(certificate);
                                certificateInfo.setContentLen(certificateLen);
                                imageInfo.setContent(image);
                                imageInfo.setContentLen(imageLen);
                                try {
                                    int ret = this.hwEidPlugin.eidGetImage(controlWord, certificateInfo, imageInfo, deSkeyInfo);
                                    System.arraycopy(imageInfo.getContent(), 0, image, 0, Math.min(imageInfo.getContent().length, image.length));
                                    System.arraycopy(imageInfo.getContentLen(), 0, imageLen, 0, Math.min(imageInfo.getContentLen().length, imageLen.length));
                                    System.arraycopy(deSkeyInfo.getContent(), 0, deSkey, 0, Math.min(deSkeyInfo.getContent().length, deSkey.length));
                                    System.arraycopy(deSkeyInfo.getContentLen(), 0, deSkeyLen, 0, Math.min(deSkeyInfo.getContentLen().length, deSkeyLen.length));
                                    return ret;
                                } catch (RemoteException e) {
                                    Log.e(TAG, "RemoteException when eid get image jni is invoked");
                                    return RET_EXCEPTION_WHEN_GET_IMAGE_CALL;
                                }
                            }
                        }
                        return RET_DEFAULT_ERROR_VALUE;
                    }
                }
                return RET_DEFAULT_ERROR_VALUE;
            }
        }
        return RET_DEFAULT_ERROR_VALUE;
    }

    public int eid_get_unsec_image(byte[] srcImage, int srcImageLen, int transpotCounter, int encryptionMethod, byte[] certificate, int certificateLen, byte[] image, int[] imageLen, byte[] deSkey, int[] deSkeyLen) {
        RemoteException e;
        if (srcImage != null && srcImageLen >= 0 && transpotCounter >= 0) {
            if (encryptionMethod >= 0) {
                if (certificate != null && certificateLen >= 0 && image != null) {
                    if (imageLen != null) {
                        if (imageLen.length > 0 && imageLen[0] >= 0 && deSkey != null && deSkeyLen != null && deSkeyLen.length > 0) {
                            if (deSkeyLen[0] >= 0) {
                                if (!getHwEidPlugin().isPresent()) {
                                    return RET_DEFAULT_ERROR_VALUE;
                                }
                                EidInfoEntity srcInfoEntity = new EidInfoEntity();
                                srcInfoEntity.setContent(srcImage);
                                srcInfoEntity.setContentLen(srcImageLen);
                                ControlWordEntity controlWord = new ControlWordEntity();
                                controlWord.setTransportCounter(transpotCounter);
                                controlWord.setEncryptionMethod(encryptionMethod);
                                EidInfoEntity certificateInfo = new EidInfoEntity();
                                certificateInfo.setContent(certificate);
                                certificateInfo.setContentLen(certificateLen);
                                EidInfoExtendEntity imageInfo = new EidInfoExtendEntity();
                                imageInfo.setContent(image);
                                imageInfo.setContentLen(imageLen);
                                EidInfoExtendEntity deSkeyInfo = new EidInfoExtendEntity();
                                deSkeyInfo.setContent(deSkey);
                                deSkeyInfo.setContentLen(deSkeyLen);
                                try {
                                    try {
                                        int ret = this.hwEidPlugin.eidGetUnsecImage(srcInfoEntity, controlWord, certificateInfo, imageInfo, deSkeyInfo);
                                        System.arraycopy(imageInfo.getContent(), 0, image, 0, Math.min(imageInfo.getContent().length, image.length));
                                        System.arraycopy(imageInfo.getContentLen(), 0, imageLen, 0, Math.min(imageInfo.getContentLen().length, imageLen.length));
                                        System.arraycopy(deSkeyInfo.getContent(), 0, deSkey, 0, Math.min(deSkeyInfo.getContent().length, deSkey.length));
                                        System.arraycopy(deSkeyInfo.getContentLen(), 0, deSkeyLen, 0, Math.min(deSkeyInfo.getContentLen().length, deSkeyLen.length));
                                        return ret;
                                    } catch (RemoteException e2) {
                                        e = e2;
                                    }
                                } catch (RemoteException e3) {
                                    e = e3;
                                    Log.e(TAG, "RemoteException when eid_get unsec image is invoked" + e);
                                    return RET_EXCEPTION_WHEN_GET_UNSEC_IMAGE_CALL;
                                }
                            }
                        }
                        return RET_DEFAULT_ERROR_VALUE;
                    }
                }
                return RET_DEFAULT_ERROR_VALUE;
            }
        }
        return RET_DEFAULT_ERROR_VALUE;
    }

    public int eid_get_certificate_request_message(byte[] requestMessage, int[] messageLen) {
        if (requestMessage == null || messageLen == null || messageLen.length <= 0 || messageLen[0] < 0 || !getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        try {
            return this.hwEidPlugin.eidGetCertificateRequestMessage(requestMessage, messageLen);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid get certificate request message jni is invoked");
            return RET_EXCEPTION_WHEN_GET_CERTIFICATE_CALL;
        }
    }

    public int eid_sign_info(int transpotCounter, int encryptionMethod, byte[] info, int infoLen, byte[] sign, int[] signLen) {
        if (transpotCounter < 0 || encryptionMethod < 0 || info == null || infoLen < 0 || sign == null || signLen == null || signLen.length <= 0 || signLen[0] < 0 || !getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        ControlWordEntity controlWord = new ControlWordEntity();
        controlWord.setTransportCounter(transpotCounter);
        controlWord.setEncryptionMethod(encryptionMethod);
        EidInfoEntity infoEntity = new EidInfoEntity();
        infoEntity.setContent(info);
        infoEntity.setContentLen(infoLen);
        EidInfoExtendEntity signEntity = new EidInfoExtendEntity();
        signEntity.setContent(sign);
        signEntity.setContentLen(signLen);
        try {
            int ret = this.hwEidPlugin.eidSignInfo(controlWord, infoEntity, signEntity);
            System.arraycopy(signEntity.getContent(), 0, sign, 0, Math.min(signEntity.getContent().length, sign.length));
            System.arraycopy(signEntity.getContentLen(), 0, signLen, 0, Math.min(signEntity.getContentLen().length, signLen.length));
            return ret;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid sign info is invoked");
            return RET_EXCEPTION_WHEN_GET_INFO_SIGN_CALL;
        }
    }

    public int eid_get_identity_information(byte[] identityInfo, int[] identityInfoLen) {
        if (identityInfo == null || identityInfoLen == null || identityInfoLen.length <= 0 || identityInfoLen[0] < 0 || !getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        try {
            return this.hwEidPlugin.eidGetIdentityInformation(identityInfo, identityInfoLen);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid get identity information jni is invoked");
            return RET_EXCEPTION_WHEN_GET_IDENTITY_CALL;
        }
    }

    public int eid_get_face_is_changed(int cmdId) {
        if (cmdId < 0 || !getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        try {
            return this.hwEidPlugin.eidGetFaceIsChanged(cmdId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid get face is changed jni is invoked");
            return RET_EXCEPTION_WHEN_GET_FACE_CHANGED_CALL;
        }
    }

    public String eid_get_version() {
        if (!getHwEidPlugin().isPresent()) {
            return null;
        }
        try {
            return this.hwEidPlugin.eidGetVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid get version");
            return null;
        }
    }

    public int eidGetSecImageZip(ImageZipContainer container, CutCoordinate coordinate, EncryptionFactor factor, SecImageZip zip) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (container == null || coordinate == null || factor == null || zip == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin().isPresent()) {
            CoordinateEntity coordinateEntity = new CoordinateEntity();
            ControlWordEntity controlWord = new ControlWordEntity();
            List<EidInfoEntity> eidInfoEntityList = new LinkedList<>();
            List<EidInfoExtendEntity> eidInfoExtendEntityList = new LinkedList<>();
            initCoordinateEntity(coordinateEntity, coordinate);
            initEidInfoEntities(eidInfoEntityList, container, factor);
            initOtherEntities(controlWord, eidInfoExtendEntityList, factor, zip);
            try {
                ret = this.hwEidPlugin.eidGetSecImageZip(coordinateEntity, controlWord, eidInfoEntityList, eidInfoExtendEntityList);
                if (eidInfoExtendEntityList.size() == 2) {
                    EidInfoExtendEntity secImageEntity = eidInfoExtendEntityList.get(0);
                    EidInfoExtendEntity deSkeyEntity = eidInfoExtendEntityList.get(1);
                    if (secImageEntity != null) {
                        if (deSkeyEntity != null) {
                            System.arraycopy(secImageEntity.getContentLen(), 0, zip.secImageLen, 0, Math.min(secImageEntity.getContentLen().length, zip.secImageLen.length));
                            System.arraycopy(secImageEntity.getContent(), 0, zip.secImage, 0, Math.min(secImageEntity.getContent().length, zip.secImage.length));
                            System.arraycopy(deSkeyEntity.getContentLen(), 0, zip.deSkeyLen, 0, Math.min(deSkeyEntity.getContentLen().length, zip.deSkeyLen.length));
                            System.arraycopy(deSkeyEntity.getContent(), 0, zip.deSkey, 0, Math.min(deSkeyEntity.getContent().length, zip.deSkey.length));
                        }
                    }
                    return ret;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get sec image zip jni is invoked");
                return RET_EXCEPTION_WHEN_GET_SEC_IMAGE_ZIP_CALL;
            }
        }
        return ret;
    }

    private void initCoordinateEntity(CoordinateEntity coordinateEntity, CutCoordinate coordinate) {
        coordinateEntity.setUp(coordinate.up);
        coordinateEntity.setDown(coordinate.down);
        coordinateEntity.setLeft(coordinate.left);
        coordinateEntity.setRight(coordinate.right);
    }

    private void initEidInfoEntities(List<EidInfoEntity> eidInfoEntityList, ImageZipContainer container, EncryptionFactor factor) {
        EidInfoEntity hashEntity = new EidInfoEntity();
        hashEntity.setContentLen(container.getHashLen());
        hashEntity.setContent(container.getHash());
        EidInfoEntity imageZipEntity = new EidInfoEntity();
        imageZipEntity.setContentLen(container.getImageZipLen());
        imageZipEntity.setContent(container.getImageZip());
        EidInfoEntity certificateEntity = new EidInfoEntity();
        certificateEntity.setContentLen(factor.getCertificateLen());
        certificateEntity.setContent(factor.getCertificate());
        eidInfoEntityList.add(certificateEntity);
        eidInfoEntityList.add(hashEntity);
        eidInfoEntityList.add(imageZipEntity);
    }

    private void initOtherEntities(ControlWordEntity controlWord, List<EidInfoExtendEntity> eidInfoExtendEntityList, EncryptionFactor factor, SecImageZip zip) {
        EidInfoExtendEntity secImageEntity = new EidInfoExtendEntity();
        EidInfoExtendEntity deSkeyEntity = new EidInfoExtendEntity();
        controlWord.setEncryptionMethod(factor.getEncryptionMethod());
        secImageEntity.setContentLen(zip.secImageLen);
        secImageEntity.setContent(zip.secImage);
        deSkeyEntity.setContentLen(zip.deSkeyLen);
        deSkeyEntity.setContent(zip.deSkey);
        eidInfoExtendEntityList.add(secImageEntity);
        eidInfoExtendEntityList.add(deSkeyEntity);
    }

    public int eidGetUnsecImageZip(ImageZipContainer container, EncryptionFactor factor, SecImageZip zip) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (container == null || factor == null || zip == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin().isPresent()) {
            ControlWordEntity controlWord = new ControlWordEntity();
            List<EidInfoEntity> eidInfoEntityList = new LinkedList<>();
            List<EidInfoExtendEntity> eidInfoExtendEntityList = new LinkedList<>();
            initEidInfoEntities(eidInfoEntityList, container, factor);
            initOtherEntities(controlWord, eidInfoExtendEntityList, factor, zip);
            try {
                ret = this.hwEidPlugin.eidGetUnsecImageZip(controlWord, eidInfoEntityList, eidInfoExtendEntityList);
                if (eidInfoExtendEntityList.size() == 2) {
                    EidInfoExtendEntity unSecImageEntity = eidInfoExtendEntityList.get(0);
                    EidInfoExtendEntity deSkeyEntity = eidInfoExtendEntityList.get(1);
                    if (unSecImageEntity != null) {
                        if (deSkeyEntity != null) {
                            System.arraycopy(unSecImageEntity.getContentLen(), 0, zip.secImageLen, 0, Math.min(unSecImageEntity.getContentLen().length, zip.secImageLen.length));
                            System.arraycopy(unSecImageEntity.getContent(), 0, zip.secImage, 0, Math.min(unSecImageEntity.getContent().length, zip.secImage.length));
                            System.arraycopy(deSkeyEntity.getContentLen(), 0, zip.deSkeyLen, 0, Math.min(deSkeyEntity.getContentLen().length, zip.deSkeyLen.length));
                            System.arraycopy(deSkeyEntity.getContent(), 0, zip.deSkey, 0, Math.min(deSkeyEntity.getContent().length, zip.deSkey.length));
                        }
                    }
                    return ret;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get unsec image zip jni is invoked");
                return RET_EXCEPTION_WHEN_GET_UNSEC_IMAGE_ZIP_CALL;
            }
        }
        return ret;
    }

    public final class CutCoordinate {
        public int down = 0;
        public int left = 0;
        public int right = 0;
        public int up = 0;

        public CutCoordinate() {
        }
    }

    public final class ImageZipContainer {
        private static final int MAX_HASH_LEN = 32;
        private static final int MAX_IMAGE_ZIP_LEN = 131072;
        private int hashLen = 0;
        private final byte[] hashs = new byte[32];
        private int imageZipLen = 0;
        private final byte[] imageZips = new byte[131072];

        public ImageZipContainer() {
        }

        public int getHashLen() {
            return this.hashLen;
        }

        public void setHashLen(int hashLen2) throws Exception {
            if (hashLen2 <= 0 || hashLen2 > 32) {
                throw new Exception("hashLen is invalid");
            }
            this.hashLen = hashLen2;
        }

        public int getImageZipLen() {
            return this.imageZipLen;
        }

        public void setImageZipLen(int imageZipLen2) throws Exception {
            if (imageZipLen2 <= 0 || imageZipLen2 > 131072) {
                throw new Exception("imageZipLen is invalid");
            }
            this.imageZipLen = imageZipLen2;
        }

        public byte[] getHash() {
            byte[] bArr = this.hashs;
            if (bArr != null) {
                return (byte[]) bArr.clone();
            }
            return new byte[0];
        }

        public void setHash(byte[] hash) throws Exception {
            if (hash == null || hash.length > 32) {
                throw new Exception("hash[] is invalid");
            }
            System.arraycopy(hash, 0, this.hashs, 0, hash.length);
        }

        public byte[] getImageZip() {
            byte[] bArr = this.imageZips;
            if (bArr != null) {
                return (byte[]) bArr.clone();
            }
            return null;
        }

        public void setimageZip(byte[] imageZip) throws Exception {
            if (imageZip == null || imageZip.length > 131072) {
                throw new Exception("imageZip[] is invalid");
            }
            System.arraycopy(imageZip, 0, this.imageZips, 0, imageZip.length);
        }
    }

    public final class EncryptionFactor {
        private static final int MAX_CERTIFICATE_LEN = 8192;
        private final byte[] certificate = new byte[8192];
        private int certificateLen = 0;
        public int encryptionMethod = 0;

        public EncryptionFactor() {
        }

        public int getCertificateLen() {
            return this.certificateLen;
        }

        public byte[] getCertificate() {
            byte[] bArr = this.certificate;
            if (bArr != null) {
                return (byte[]) bArr.clone();
            }
            return null;
        }

        public void setCertificate(byte[] certificate2) throws Exception {
            if (certificate2 == null || certificate2.length > 8192) {
                throw new Exception("certificate[] is invalid");
            }
            System.arraycopy(certificate2, 0, this.certificate, 0, certificate2.length);
        }

        public void setCertificateLen(int certificateLen2) throws Exception {
            if (certificateLen2 <= 0 || certificateLen2 > 8192) {
                throw new Exception("certificateLen is invalid");
            }
            this.certificateLen = certificateLen2;
        }

        public void setEncryptionMethod(int encryptionMethod2) throws Exception {
            if (encryptionMethod2 >= 0) {
                this.encryptionMethod = encryptionMethod2;
                return;
            }
            throw new Exception("encryptionMethod invalid");
        }

        public int getEncryptionMethod() {
            return this.encryptionMethod;
        }
    }

    public final class SecImageZip {
        private static final int MAX_DE_SKEY_LEN = 2048;
        private static final int MAX_SEC_IMAGE_LEN = 131072;
        public final byte[] deSkey = new byte[2048];
        public final int[] deSkeyLen = new int[1];
        public final byte[] secImage = new byte[131072];
        public final int[] secImageLen = new int[1];

        public SecImageZip() {
        }
    }
}

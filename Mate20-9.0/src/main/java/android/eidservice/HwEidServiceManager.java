package android.eidservice;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwEidPlugin;
import huawei.android.security.IHwSecurityService;

public class HwEidServiceManager {
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final int HW_EID_PLUGIN_ID = 15;
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
    private static volatile HwEidServiceManager sInstance = null;
    private IHwEidPlugin mIHwEidPlugin;
    private IHwSecurityService mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));

    public final class CutCoordinate {
        public int down = 0;
        public int left = 0;
        public int right = 0;
        public int up = 0;

        public CutCoordinate() {
        }
    }

    public final class EncryptionFactor {
        private final byte[] certificate = new byte[this.maxCertificateLen];
        private int certificateLen = 0;
        public int encryptionMethod = 0;
        private int maxCertificateLen = 8192;

        public EncryptionFactor() {
        }

        public int getCertificateLen() {
            return this.certificateLen;
        }

        public byte[] getCertificate() {
            return this.certificate;
        }

        public void setCertificate(byte[] certificate2) throws Exception {
            if (certificate2 == null || certificate2.length > this.maxCertificateLen) {
                throw new Exception("certificate[] is invalid!");
            }
            System.arraycopy(certificate2, 0, this.certificate, 0, certificate2.length);
        }

        public void setCertificateLen(int certificateLen2) throws Exception {
            if (certificateLen2 <= 0 || certificateLen2 > this.maxCertificateLen) {
                throw new Exception("certificateLen is invalid!");
            }
            this.certificateLen = certificateLen2;
        }

        public void setEncryptionMethod(int encryptionMethod2) throws Exception {
            if (encryptionMethod2 >= 0) {
                this.encryptionMethod = encryptionMethod2;
                return;
            }
            throw new Exception("encryptionMethod invalid!");
        }

        public int getEncryptionMethod() {
            return this.encryptionMethod;
        }
    }

    public final class ImageZipContainer {
        private final byte[] hash = new byte[this.maxHashLen];
        private int hashLen = 0;
        private final byte[] imageZip = new byte[this.maxImageZipLen];
        private int imageZipLen = 0;
        private int maxHashLen = 32;
        private int maxImageZipLen = 131072;

        public ImageZipContainer() {
        }

        public int getHashLen() {
            return this.hashLen;
        }

        public void setHashLen(int hashLen2) throws Exception {
            if (hashLen2 <= 0 || hashLen2 > this.maxHashLen) {
                throw new Exception("hashLen is invalid!");
            }
            this.hashLen = hashLen2;
        }

        public int getImageZipLen() {
            return this.imageZipLen;
        }

        public void setImageZipLen(int imageZipLen2) throws Exception {
            if (imageZipLen2 <= 0 || imageZipLen2 > this.maxImageZipLen) {
                throw new Exception("imageZipLen is invalid!");
            }
            this.imageZipLen = imageZipLen2;
        }

        public byte[] getHash() {
            return this.hash;
        }

        public void setHash(byte[] hash2) throws Exception {
            if (hash2 == null || hash2.length > this.maxHashLen) {
                throw new Exception("hash[] is invalid!");
            }
            System.arraycopy(hash2, 0, this.hash, 0, hash2.length);
        }

        public byte[] getImageZip() {
            return this.imageZip;
        }

        public void setimageZip(byte[] imageZip2) throws Exception {
            if (imageZip2 == null || imageZip2.length > this.maxImageZipLen) {
                throw new Exception("imageZip[] is invalid!");
            }
            System.arraycopy(imageZip2, 0, this.imageZip, 0, imageZip2.length);
        }
    }

    public final class SecImageZip {
        public final byte[] deSkey = new byte[this.maxDeSkeyLen];
        public final int[] deSkeyLen = new int[1];
        private int maxDeSkeyLen = 2048;
        private int maxSecImageLen = 131072;
        public final byte[] secImage = new byte[this.maxSecImageLen];
        public final int[] secImageLen = new int[1];

        public SecImageZip() {
        }
    }

    private HwEidServiceManager() {
        if (this.mSecurityService == null) {
            Log.e(TAG, "error, securityservice was null");
        }
    }

    public static HwEidServiceManager getInstance() {
        if (sInstance == null) {
            synchronized (HwEidServiceManager.class) {
                if (sInstance == null) {
                    sInstance = new HwEidServiceManager();
                }
            }
        }
        return sInstance;
    }

    private IHwEidPlugin getHwEidPlugin() {
        if (this.mIHwEidPlugin != null) {
            return this.mIHwEidPlugin;
        }
        if (this.mSecurityService != null) {
            try {
                this.mIHwEidPlugin = IHwEidPlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(15));
                if (this.mIHwEidPlugin == null) {
                    Log.e(TAG, "error, IHwEidPlugin is null");
                }
                return this.mIHwEidPlugin;
            } catch (RemoteException e) {
                Log.e(TAG, "Get getHwEidPlugin failed!");
            }
        }
        return null;
    }

    public int eid_init(byte[] hw_aid, int hw_aid_len, byte[] eid_aid, int eid_aid_len, byte[] logo, int logo_size) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (hw_aid == null || eid_aid == null || logo_size < 0 || hw_aid_len < 0 || eid_aid_len < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_init(hw_aid, hw_aid_len, eid_aid, eid_aid_len, logo, logo_size);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid init jnis is invoked");
                return RET_EXCEPTION_WHEN_EID_INIT_CALL;
            }
        }
        return ret;
    }

    public int eid_finish() {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_finish();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid finish jni is invoked");
                return RET_EXCEPTION_WHEN_EID_FINISH_CALL;
            }
        }
        return ret;
    }

    public int eid_get_image(int transpotCounter, int encryption_method, byte[] certificate, int certificate_len, byte[] image, int[] image_len, byte[] de_skey, int[] de_skey_len) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (transpotCounter < 0 || encryption_method < 0 || certificate == null || certificate_len < 0 || image == null || image_len == null || image_len[0] < 0 || de_skey == null || de_skey_len == null || de_skey_len[0] < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_get_image(transpotCounter, encryption_method, certificate, certificate_len, image, image_len, de_skey, de_skey_len);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get image jni is invoked");
                return RET_EXCEPTION_WHEN_GET_IMAGE_CALL;
            }
        }
        return ret;
    }

    public int eid_get_unsec_image(byte[] src_image, int src_image_len, int transpotCounter, int encryption_method, byte[] certificate, int certificate_len, byte[] image, int[] image_len, byte[] de_skey, int[] de_skey_len) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (src_image == null || src_image_len < 0 || transpotCounter < 0 || encryption_method < 0 || certificate == null || certificate_len < 0 || image == null || image_len == null || image_len[0] < 0 || de_skey == null || de_skey_len == null || de_skey_len[0] < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_get_unsec_image(src_image, src_image_len, transpotCounter, encryption_method, certificate, certificate_len, image, image_len, de_skey, de_skey_len);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid_get unsec image is invoked" + e);
                e.printStackTrace();
                return RET_EXCEPTION_WHEN_GET_UNSEC_IMAGE_CALL;
            }
        }
        return ret;
    }

    public int eid_get_certificate_request_message(byte[] request_message, int[] message_len) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (request_message == null || message_len == null || message_len[0] < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_get_certificate_request_message(request_message, message_len);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get certificate request message jni is invoked");
                return RET_EXCEPTION_WHEN_GET_CERTIFICATE_CALL;
            }
        }
        return ret;
    }

    public int eid_sign_info(int transpotCounter, int encryption_method, byte[] info, int info_len, byte[] sign, int[] sign_len) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (transpotCounter < 0 || encryption_method < 0 || info == null || info_len < 0 || sign == null || sign_len == null || sign_len[0] < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_sign_info(transpotCounter, encryption_method, info, info_len, sign, sign_len);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid sign info is invoked");
                return RET_EXCEPTION_WHEN_GET_INFO_SIGN_CALL;
            }
        }
        return ret;
    }

    public int eid_get_identity_information(byte[] identity_info, int[] identity_info_len) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (identity_info == null || identity_info_len == null || identity_info_len[0] < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_get_identity_information(identity_info, identity_info_len);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get identity information jni is invoked");
                return RET_EXCEPTION_WHEN_GET_IDENTITY_CALL;
            }
        }
        return ret;
    }

    public int eid_get_face_is_changed(int cmd_id) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (cmd_id < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eid_get_face_is_changed(cmd_id);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get face is changed jni is invoked");
                return RET_EXCEPTION_WHEN_GET_FACE_CHANGED_CALL;
            }
        }
        return ret;
    }

    public String eid_get_version() {
        if (getHwEidPlugin() == null) {
            return null;
        }
        try {
            return this.mIHwEidPlugin.eid_get_version();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid get version");
            return null;
        }
    }

    public int eidGetSecImageZip(ImageZipContainer container, CutCoordinate coordinate, EncryptionFactor factor, SecImageZip zip) {
        CutCoordinate cutCoordinate = coordinate;
        SecImageZip secImageZip = zip;
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (container == null || cutCoordinate == null || factor == null || secImageZip == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eidGetSecImageZip(container.getHashLen(), container.getHash(), container.getImageZipLen(), container.getImageZip(), cutCoordinate.up, cutCoordinate.down, cutCoordinate.left, cutCoordinate.right, factor.getEncryptionMethod(), factor.getCertificateLen(), factor.getCertificate(), secImageZip.secImageLen, secImageZip.secImage, secImageZip.deSkeyLen, secImageZip.deSkey);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get sec image zip jni is invoked");
                return RET_EXCEPTION_WHEN_GET_SEC_IMAGE_ZIP_CALL;
            }
        }
        return ret;
    }

    public int eidGetUnsecImageZip(ImageZipContainer container, EncryptionFactor factor, SecImageZip zip) {
        SecImageZip secImageZip = zip;
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (container == null || factor == null || secImageZip == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getHwEidPlugin() != null) {
            try {
                ret = this.mIHwEidPlugin.eidGetUnsecImageZip(container.getHashLen(), container.getHash(), container.getImageZipLen(), container.getImageZip(), factor.getEncryptionMethod(), factor.getCertificateLen(), factor.getCertificate(), secImageZip.secImageLen, secImageZip.secImage, secImageZip.deSkeyLen, secImageZip.deSkey);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid get unsec image zip jni is invoked");
                return RET_EXCEPTION_WHEN_GET_UNSEC_IMAGE_ZIP_CALL;
            }
        }
        return ret;
    }
}

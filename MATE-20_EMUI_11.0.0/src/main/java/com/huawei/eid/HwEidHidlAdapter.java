package com.huawei.eid;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;
import com.huawei.security.ControlWordEntity;
import com.huawei.security.CoordinateEntity;
import com.huawei.security.EidInfoEntity;
import com.huawei.security.EidInfoExtendEntity;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import vendor.huawei.hardware.eid.V1_0.CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S;
import vendor.huawei.hardware.eid.V1_0.CERTIFICATE_REQUEST_MESSAGE_S;
import vendor.huawei.hardware.eid.V1_0.ENCRYPTION_FACTOR_S;
import vendor.huawei.hardware.eid.V1_0.FACE_CHANGE_INPUT_INFO_S;
import vendor.huawei.hardware.eid.V1_0.FACE_CHANGE_OUTPUT_INFO_S;
import vendor.huawei.hardware.eid.V1_0.IDENTITY_INFORMATION_INPUT_INFO_S;
import vendor.huawei.hardware.eid.V1_0.IDENTITY_INFORMATION_S;
import vendor.huawei.hardware.eid.V1_0.IEid;
import vendor.huawei.hardware.eid.V1_0.IMAGE_CONTAINER_S;
import vendor.huawei.hardware.eid.V1_0.INFO_SIGN_INPUT_INFO_S;
import vendor.huawei.hardware.eid.V1_0.INFO_SIGN_OUTPUT_INFO_S;
import vendor.huawei.hardware.eid.V1_0.INIT_TA_MSG_S;
import vendor.huawei.hardware.eid.V1_0.SEC_IMAGE_S;
import vendor.huawei.hardware.eid.V1_1.CUT_COORDINATE_S;
import vendor.huawei.hardware.eid.V1_1.HIDL_VERSION_S;
import vendor.huawei.hardware.eid.V1_1.IEid;
import vendor.huawei.hardware.eid.V1_1.IMAGE_ZIP_CONTAINER_S;
import vendor.huawei.hardware.eid.V1_1.INIT_CTID_TA_MSG_S;
import vendor.huawei.hardware.eid.V1_1.SEC_IMAGE_ZIP_S;

@HwSystemApi
public class HwEidHidlAdapter {
    private static final int CERTIFICATE_ENTITY_INDEX = 0;
    private static final int CTID_VERSION = 1;
    private static final int DES_KEY_ENTITY_INDEX = 1;
    private static final String EID_HIDL_SERVICE_NAME = "eid";
    private static final int EID_INFO_EXTEND_LIST_NUMS = 2;
    private static final int EID_INFO_LIST_NUMS = 3;
    private static final int ENCRY_SET_SECMODE = 3;
    private static final int HASH_ENTITY_INDEX = 1;
    private static final boolean HW_DEBUG = (SystemProperties.get("ro.secure", "1").equals("0") || Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int IMAGEZIP_ENTITY_INDEX = 2;
    private static final int IMAGE_ENTITY_INDEX = 0;
    private static final int IMAGE_NV21_HEIGH = 640;
    private static final int IMAGE_NV21_WEIGHT = 480;
    private static final int INPUT_TRANSPOT_TIMES = 3;
    private static final int MAX_EID_HIDL_DEAMON_REGISTER_TIMES = 10;
    private static final int MSG_EID_HIDL_DEAMON_SERVIE_REGISTER = 1;
    private static final int RET_DEFAULT_ERROR_VALUE = -1001;
    private static final int RET_EID_HIDL_DEAMON_IS_NOT_READY = -1000;
    private static final int RET_EXCEPTION_WHEN_EID_CALL = -1002;
    private static final String TAG = "HwEidHidlAdapter";
    private static final int TA_PATH_LEN_MIN = 511;
    private static final int TRY_GET_HIDL_DEAMON_DEALY_MILLIS = 1000;
    private static final int UUID_LEN_MIN = 16;
    private String mEidGetVersionRet = "";
    private IHwBinder.DeathRecipient mEidHidlDeamonDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass1 */

        public void serviceDied(long cookie) {
            if (HwEidHidlAdapter.this.mHwEidHidlHandler != null) {
                Log.e(HwEidHidlAdapter.TAG, "Eid hidl deamon service has died, try to reconnect it later");
                HwEidHidlAdapter.this.mEidInstance = null;
                HwEidHidlAdapter.this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    };
    private int mEidHidlDeamonRegisterTryTimes = 0;
    private IEid mEidInstance;
    private int mHwEidGetCertificateRequestMessageRetValue = -1001;
    private int mHwEidGetFaceIsChangedRetValue = -1001;
    private int mHwEidGetIdentityInformationRetValue = -1001;
    private int mHwEidGetImageRetValue = -1001;
    private int mHwEidGetInfoSignRetValue = -1001;
    private int mHwEidGetSecImageZipRetValue = -1001;
    private int mHwEidGetUnSecImageZipRetValue = -1001;
    private int mHwEidGetUnsecImageRetValue = -1001;
    private HwEidHidlHandler mHwEidHidlHandler;
    private HandlerThread mHwEidThread = new HandlerThread(TAG);

    static /* synthetic */ int access$208(HwEidHidlAdapter x0) {
        int i = x0.mEidHidlDeamonRegisterTryTimes;
        x0.mEidHidlDeamonRegisterTryTimes = i + 1;
        return i;
    }

    public HwEidHidlAdapter() {
        this.mHwEidThread.start();
        this.mHwEidHidlHandler = new HwEidHidlHandler(this.mHwEidThread.getLooper());
        getEidService(true);
    }

    public void stopHwEidHidlAdapter() {
        if (this.mHwEidHidlHandler != null) {
            this.mHwEidHidlHandler = null;
        }
        HandlerThread handlerThread = this.mHwEidThread;
        if (handlerThread != null) {
            try {
                handlerThread.quitSafely();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Stop error: java.lang.UnsatisfiedLinkError");
            }
            this.mHwEidThread = null;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Close HwEidHidlAdapter");
        }
    }

    /* access modifiers changed from: private */
    public final class HwEidHidlHandler extends Handler {
        HwEidHidlHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Log.e(HwEidHidlAdapter.TAG, "Handler thread received unknown message: " + msg.what);
                return;
            }
            try {
                HwEidHidlAdapter.this.mEidInstance = IEid.getService(HwEidHidlAdapter.EID_HIDL_SERVICE_NAME);
            } catch (RemoteException e) {
                Log.e(HwEidHidlAdapter.TAG, "Try get eid hidl deamon servcie failed in handler message");
            }
            if (HwEidHidlAdapter.this.mEidInstance != null) {
                HwEidHidlAdapter.this.mEidHidlDeamonRegisterTryTimes = 0;
                try {
                    HwEidHidlAdapter.this.mEidInstance.linkToDeath(HwEidHidlAdapter.this.mEidHidlDeamonDeathRecipient, 0);
                } catch (RemoteException e2) {
                    Log.e(HwEidHidlAdapter.TAG, "Exception occured when linkToDeath in handle message");
                }
            } else {
                HwEidHidlAdapter.access$208(HwEidHidlAdapter.this);
                if (HwEidHidlAdapter.this.mEidHidlDeamonRegisterTryTimes < 10) {
                    Log.i(HwEidHidlAdapter.TAG, "Eid hidl daemon service is not ready, try times: " + HwEidHidlAdapter.this.mEidHidlDeamonRegisterTryTimes);
                    HwEidHidlAdapter.this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    Log.e(HwEidHidlAdapter.TAG, "Eid hidl daemon service connection failed");
                }
            }
            if (HwEidHidlAdapter.HW_DEBUG) {
                Log.d(HwEidHidlAdapter.TAG, "Handler thread received request eid hidl deamon message");
            }
        }
    }

    private void freeBinderMemory() {
        try {
            IEid.getService(EID_HIDL_SERVICE_NAME);
            Log.d(TAG, "Eid binder memory");
        } catch (RemoteException e) {
            Log.e(TAG, "Eid binder memory fail");
        }
    }

    private Optional<CUT_COORDINATE_S> getNewEidCoordinate(int up, int down, int left, int right) {
        if (up < 0) {
            Log.e(TAG, "GetNewEidCoordinate: The para up down is error, up: " + up + " down: " + down);
            return Optional.empty();
        } else if (down > IMAGE_NV21_HEIGH || up >= down) {
            Log.e(TAG, "GetNewEidCoordinate: The para up down is error, up: " + up + " down: " + down);
            return Optional.empty();
        } else if (left < 0) {
            Log.e(TAG, "GetNewEidCoordinate: The para left down is error, left: " + left + " right: " + right);
            return Optional.empty();
        } else if (right > IMAGE_NV21_WEIGHT || left >= right) {
            Log.e(TAG, "GetNewEidCoordinate: The para left down is error, left: " + left + " right: " + right);
            return Optional.empty();
        } else {
            CUT_COORDINATE_S coordinate = new CUT_COORDINATE_S();
            coordinate.up = up;
            coordinate.down = down;
            coordinate.left = left;
            coordinate.right = right;
            return Optional.of(coordinate);
        }
    }

    private Optional<ENCRYPTION_FACTOR_S> getNewEidFactor(int encryptionMethod, int splitTime, int certificateLen, byte[] certificate) {
        if (certificate == null) {
            Log.e(TAG, "The para certificate is null");
            return Optional.empty();
        }
        ENCRYPTION_FACTOR_S factor = new ENCRYPTION_FACTOR_S();
        factor.encryptionMethod = encryptionMethod;
        factor.certificateLen = certificateLen;
        factor.splitTimes = splitTime;
        if (certificate.length > factor.certificate.length || certificateLen > certificate.length) {
            Log.e(TAG, "GetNewEidFactor: certificateLen: " + certificateLen + " certificate.length:" + certificate.length + " factor.certificate.length: " + factor.certificate.length);
            return Optional.empty();
        }
        System.arraycopy(certificate, 0, factor.certificate, 0, certificateLen);
        return Optional.of(factor);
    }

    private Optional<IMAGE_ZIP_CONTAINER_S> getNewEidContainer(int hashLen, byte[] hash, int imageZipLen, byte[] imageZip) {
        if (hash == null || imageZip == null) {
            Log.e(TAG, "GetNewEidContainer: The para hash or imageZip is null");
            return Optional.empty();
        }
        IMAGE_ZIP_CONTAINER_S container = new IMAGE_ZIP_CONTAINER_S();
        container.hash_len = hashLen;
        if (hash.length > container.hash.length || hashLen > hash.length) {
            Log.e(TAG, "GetNewEidContainer: hash.length:" + hash.length + ", container.hash.length: " + container.hash.length + " hashLen: " + hashLen);
            return Optional.empty();
        }
        System.arraycopy(hash, 0, container.hash, 0, hashLen);
        container.image_len = imageZipLen;
        if (imageZip.length > container.image.length || imageZipLen > imageZip.length) {
            Log.e(TAG, "GetNewEidContainer: imageZip.length: " + imageZip.length + " container.image.length: " + container.image.length + " imageZipLen: " + imageZipLen);
            return Optional.empty();
        }
        System.arraycopy(imageZip, 0, container.image, 0, imageZipLen);
        return Optional.of(container);
    }

    public int eidInitHidlAdapter(EidInfoEntity aidInfo, EidInfoEntity eidAidInfo, EidInfoEntity logoInfo) {
        int ret;
        if (aidInfo == null || eidAidInfo == null || logoInfo == null) {
            return -1001;
        }
        int hwAidLen = aidInfo.getContentLen();
        int eidAidLen = eidAidInfo.getContentLen();
        int logoSize = logoInfo.getContentLen();
        INIT_TA_MSG_S input = new INIT_TA_MSG_S();
        input.hw_aid_len = hwAidLen;
        input.eid_aid_len = eidAidLen;
        input.logo_size = logoSize;
        byte[] hwAid = aidInfo.getContent();
        if (hwAid != null) {
            System.arraycopy(hwAid, 0, input.hw_aid, 0, Math.min(hwAid.length, input.hw_aid.length));
        }
        byte[] eidAid = eidAidInfo.getContent();
        if (eidAid != null) {
            System.arraycopy(eidAid, 0, input.eid_aid, 0, Math.min(eidAid.length, input.eid_aid.length));
        }
        byte[] logo = logoInfo.getContent();
        if (logo != null) {
            System.arraycopy(logo, 0, input.eid_logo, 0, Math.min(logo.length, input.eid_logo.length));
        }
        getEidService(true);
        IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                ret = iEid.HWEidInitTa(input);
                if (HW_DEBUG) {
                    Log.d(TAG, "Eid init ret: " + ret);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Eid init from hidl failed");
                return -1002;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid init");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid init from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidFinishHidlAdapter() {
        int ret;
        getEidService(false);
        IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                ret = iEid.HWEidFiniTa();
                if (HW_DEBUG) {
                    Log.d(TAG, "Eid finish ta ret: " + ret);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Eid finish from mEid hidl failed");
                return -1002;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid finish");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid finish from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetImageHidlAdapter(ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) {
        int ret;
        if (controlWord == null || certificateInfo == null || imageInfo == null || deSkeyInfo == null) {
            return -1001;
        }
        boolean isExecption = false;
        getEidService(false);
        if (this.mEidInstance != null) {
            try {
                int encryptionMethod = controlWord.getEncryptionMethod();
                int certificateLen = certificateInfo.getContentLen();
                int transpotCounter = controlWord.getTransportCounter();
                ENCRYPTION_FACTOR_S factor = new ENCRYPTION_FACTOR_S();
                factor.encryptionMethod = encryptionMethod;
                factor.certificateLen = certificateLen;
                factor.splitTimes = transpotCounter;
                byte[] certificate = certificateInfo.getContent();
                if (certificate != null) {
                    System.arraycopy(certificate, 0, factor.certificate, 0, Math.min(certificate.length, factor.certificate.length));
                }
                updateImage(controlWord, imageInfo, deSkeyInfo, factor);
                freeBinderMemory();
            } catch (RemoteException e) {
                isExecption = true;
                Log.e(TAG, "Eid get image from mEid hidl failed.");
            }
            ret = isExecption ? -1002 : this.mHwEidGetImageRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid get image");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get image from mEid hidl ret: " + ret);
        }
        return ret;
    }

    private void updateImage(ControlWordEntity controlWord, final EidInfoExtendEntity imageInfo, final EidInfoExtendEntity deSkeyInfo, final ENCRYPTION_FACTOR_S factor) throws RemoteException {
        final int encryptionMethod = controlWord.getEncryptionMethod();
        final byte[] image = imageInfo.getContent();
        final int[] imageLen = imageInfo.getContentLen();
        final byte[] deSkey = deSkeyInfo.getContent();
        final int[] deSkeyLen = deSkeyInfo.getContentLen();
        this.mEidInstance.HWEidGetImage(factor, new IEid.HWEidGetImageCallback() {
            /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass2 */

            @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetImageCallback
            public void onValues(int hwEidGetImageRet, SEC_IMAGE_S secImage) {
                int[] iArr;
                int[] iArr2;
                if (hwEidGetImageRet == 0 && encryptionMethod != 3) {
                    if (image != null) {
                        System.arraycopy(secImage.image, 0, image, 0, Math.min(secImage.image.length, image.length));
                        imageInfo.setContent(image);
                    }
                    if (factor.splitTimes == 1 && deSkey != null) {
                        System.arraycopy(secImage.deSkey, 0, deSkey, 0, Math.min(secImage.deSkeyLen, deSkey.length));
                        deSkeyInfo.setContent(deSkey);
                    }
                    if (factor.splitTimes == 1 && (iArr2 = imageLen) != null && iArr2.length > 0) {
                        iArr2[0] = secImage.len;
                        imageInfo.setContentLen(imageLen);
                    }
                    if (factor.splitTimes == 1 && (iArr = deSkeyLen) != null && iArr.length > 0) {
                        iArr[0] = secImage.deSkeyLen;
                        deSkeyInfo.setContentLen(deSkeyLen);
                    }
                }
                HwEidHidlAdapter.this.mHwEidGetImageRetValue = hwEidGetImageRet;
                if (HwEidHidlAdapter.HW_DEBUG) {
                    Log.d(HwEidHidlAdapter.TAG, "Eid get image call counter: " + factor.splitTimes + System.lineSeparator() + "Eid get image mHwEidGetImageRetValue: " + HwEidHidlAdapter.this.mHwEidGetImageRetValue + " hwEidGetImageRet:" + hwEidGetImageRet);
                }
            }
        });
    }

    public int eidGetUnsecImageHidlAdapter(EidInfoEntity srcInfoEntity, ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) {
        int ret;
        if (srcInfoEntity == null || controlWord == null || certificateInfo == null || imageInfo == null || deSkeyInfo == null) {
            return -1001;
        }
        boolean isExecption = false;
        getEidService(false);
        if (this.mEidInstance != null) {
            try {
                IMAGE_CONTAINER_S container = new IMAGE_CONTAINER_S();
                ENCRYPTION_FACTOR_S factor = new ENCRYPTION_FACTOR_S();
                initFactorAndContainer(srcInfoEntity, controlWord, certificateInfo, container, factor);
                getUnsecImage(imageInfo, deSkeyInfo, container, factor);
                freeBinderMemory();
            } catch (RemoteException e) {
                Log.e(TAG, "Eid get unsec image from mEid hidl failed");
                isExecption = true;
            }
            ret = isExecption ? -1002 : this.mHwEidGetUnsecImageRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid get unsec image");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get unsec image from mEid hidl ret: " + ret);
        }
        return ret;
    }

    private void getUnsecImage(final EidInfoExtendEntity imageInfo, final EidInfoExtendEntity deSkeyInfo, IMAGE_CONTAINER_S container, final ENCRYPTION_FACTOR_S factor) throws RemoteException {
        this.mEidInstance.HWEidGetUnsecImage(container, factor, new IEid.HWEidGetUnsecImageCallback() {
            /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass3 */

            @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetUnsecImageCallback
            public void onValues(int hwEidGetUnsecImageRet, SEC_IMAGE_S secImage) {
                HwEidHidlAdapter.this.updateUnsecImages(factor, imageInfo, deSkeyInfo, secImage, hwEidGetUnsecImageRet);
                if (HwEidHidlAdapter.HW_DEBUG) {
                    Log.d(HwEidHidlAdapter.TAG, "Eid get unsec image call counter: " + factor.splitTimes);
                }
                HwEidHidlAdapter.this.mHwEidGetUnsecImageRetValue = hwEidGetUnsecImageRet;
                if (HwEidHidlAdapter.HW_DEBUG) {
                    Log.d(HwEidHidlAdapter.TAG, "Eid get unsec image mHwEidGetUnsecImageRetValue : " + HwEidHidlAdapter.this.mHwEidGetUnsecImageRetValue + " hwEidGetUnsecImageRet:" + hwEidGetUnsecImageRet);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUnsecImages(ENCRYPTION_FACTOR_S factor, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo, SEC_IMAGE_S secImage, int hwEidGetUnsecImageRet) {
        byte[] image = imageInfo.getContent();
        int[] imageLen = imageInfo.getContentLen();
        byte[] deSkey = deSkeyInfo.getContent();
        int[] deSkeyLen = deSkeyInfo.getContentLen();
        if (factor.splitTimes >= 3 && hwEidGetUnsecImageRet == 0 && factor.splitTimes == 3) {
            if (deSkey != null) {
                System.arraycopy(secImage.deSkey, 0, deSkey, 0, Math.min(secImage.deSkey.length, deSkey.length));
                deSkeyInfo.setContent(deSkey);
            }
            if (imageLen != null && imageLen.length > 0) {
                imageLen[0] = secImage.len;
                imageInfo.setContentLen(imageLen);
            }
            if (deSkeyLen != null && deSkeyLen.length > 0) {
                deSkeyLen[0] = secImage.deSkeyLen;
                deSkeyInfo.setContentLen(deSkeyLen);
            }
            if (image != null) {
                System.arraycopy(secImage.image, 0, image, 0, Math.min(secImage.image.length, image.length));
                imageInfo.setContent(image);
            }
        }
    }

    private void initFactorAndContainer(EidInfoEntity srcInfoEntity, ControlWordEntity controlWord, EidInfoEntity certificateInfo, IMAGE_CONTAINER_S container, ENCRYPTION_FACTOR_S factor) {
        byte[] srcImage = srcInfoEntity.getContent();
        int srcImageLen = srcInfoEntity.getContentLen();
        int transpotCounter = controlWord.getTransportCounter();
        byte[] certificate = certificateInfo.getContent();
        factor.splitTimes = transpotCounter;
        if (factor.splitTimes < 3) {
            if (srcImage != null) {
                System.arraycopy(srcImage, 0, container.image, 0, Math.min(srcImage.length, container.image.length));
            }
        } else if (factor.splitTimes == 3) {
            container.len = srcImageLen;
            if (srcImage != null) {
                System.arraycopy(srcImage, 0, container.image, 0, Math.min(srcImage.length, container.image.length));
            }
            if (certificate != null) {
                System.arraycopy(certificate, 0, factor.certificate, 0, Math.min(certificate.length, factor.certificate.length));
            }
            int certificateLen = certificateInfo.getContentLen();
            factor.encryptionMethod = controlWord.getEncryptionMethod();
            factor.certificateLen = certificateLen;
        }
    }

    public int eidGetCertificateRequestMessageHidlAdapter(final byte[] requestMessage, final int[] messageLen) {
        int ret;
        if (requestMessage == null || messageLen == null) {
            return -1001;
        }
        boolean isExecption = false;
        CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S input = new CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S();
        getEidService(false);
        vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                iEid.HWEidGetCertificateRequestMessage(input, new IEid.HWEidGetCertificateRequestMessageCallback() {
                    /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass4 */

                    @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetCertificateRequestMessageCallback
                    public void onValues(int hwEidGetCertificateRequestMessageRet, CERTIFICATE_REQUEST_MESSAGE_S certReqMsg) {
                        HwEidHidlAdapter.this.updateCertificateInfo(certReqMsg, requestMessage, messageLen, hwEidGetCertificateRequestMessageRet);
                    }
                });
            } catch (RemoteException e) {
                isExecption = true;
                Log.e(TAG, "Eid get certificate request message from mEid hidl failed");
            }
            ret = isExecption ? -1002 : this.mHwEidGetCertificateRequestMessageRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid get certificate request message");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get certificate request message from mEid hidl ret: " + ret);
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCertificateInfo(CERTIFICATE_REQUEST_MESSAGE_S certReqMsg, byte[] requestMessage, int[] messageLen, int hwEidGetCertificateRequestMessageRet) {
        if (hwEidGetCertificateRequestMessageRet == 0 && messageLen.length > 0) {
            System.arraycopy(certReqMsg.message, 0, requestMessage, 0, Math.min(certReqMsg.len, requestMessage.length));
            messageLen[0] = certReqMsg.len;
            if (HW_DEBUG) {
                Log.d(TAG, "Eid get certificate request message message len: " + messageLen[0] + " certReqMsg len:" + certReqMsg.len);
            }
        }
        this.mHwEidGetCertificateRequestMessageRetValue = hwEidGetCertificateRequestMessageRet;
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get certificate request message mHwEidGetCertificateRequestMessageRetValue: " + this.mHwEidGetCertificateRequestMessageRetValue + " hwEidGetCertificateRequestMessageRet: " + hwEidGetCertificateRequestMessageRet);
        }
    }

    public int eidSignInfoHidlAdapter(ControlWordEntity controlWord, EidInfoEntity infoEntity, EidInfoExtendEntity signEntity) {
        int ret;
        if (controlWord == null || infoEntity == null || signEntity == null) {
            return -1001;
        }
        boolean isExecption = false;
        Log.d(TAG, "Info: eid_sign_begin");
        getEidService(false);
        if (this.mEidInstance != null) {
            try {
                INFO_SIGN_INPUT_INFO_S input = new INFO_SIGN_INPUT_INFO_S();
                input.encryptionMethod = controlWord.getEncryptionMethod();
                input.infoLen = infoEntity.getContentLen();
                input.splitTimes = controlWord.getTransportCounter();
                byte[] info = infoEntity.getContent();
                if (info != null) {
                    System.arraycopy(info, 0, input.info, 0, Math.min(info.length, input.info.length));
                }
                updateInfoSign(signEntity, signEntity.getContent(), signEntity.getContentLen(), input);
            } catch (RemoteException e) {
                isExecption = true;
                Log.e(TAG, "Eid sign info from mEid hidl failed");
            }
            ret = isExecption ? -1002 : this.mHwEidGetInfoSignRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid sign info");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid sign info from mEid hidl ret: " + ret);
        }
        return ret;
    }

    private void updateInfoSign(final EidInfoExtendEntity signEntity, final byte[] sign, final int[] signLen, final INFO_SIGN_INPUT_INFO_S input) throws RemoteException {
        this.mEidInstance.HWEidGetInfoSign(input, new IEid.HWEidGetInfoSignCallback() {
            /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass5 */

            @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetInfoSignCallback
            public void onValues(int hwEidGetInfoSignRet, INFO_SIGN_OUTPUT_INFO_S output) {
                int[] iArr;
                if (hwEidGetInfoSignRet == 0 && input.splitTimes == 1 && (iArr = signLen) != null && iArr.length > 0) {
                    iArr[0] = output.infoLen;
                    signEntity.setContentLen(signLen);
                }
                if (sign != null && hwEidGetInfoSignRet == 0) {
                    System.arraycopy(output.signInfo, 0, sign, 0, Math.min(output.signInfo.length, sign.length));
                    signEntity.setContent(sign);
                }
                HwEidHidlAdapter.this.mHwEidGetInfoSignRetValue = hwEidGetInfoSignRet;
                if (HwEidHidlAdapter.HW_DEBUG) {
                    Log.d(HwEidHidlAdapter.TAG, "Eid sign info call counter: " + input.splitTimes + " Eid sign info mHwEidGetInfoSignRetValue: " + HwEidHidlAdapter.this.mHwEidGetInfoSignRetValue + " hwEidGetInfoSignRet:" + hwEidGetInfoSignRet);
                }
            }
        });
    }

    public int eidGetIdentityInformationHidlAdapter(final byte[] identityInfo, final int[] identityInfoLen) {
        int ret;
        if (identityInfo == null || identityInfoLen == null) {
            return -1001;
        }
        boolean isExecption = false;
        IDENTITY_INFORMATION_INPUT_INFO_S input = new IDENTITY_INFORMATION_INPUT_INFO_S();
        getEidService(false);
        vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                iEid.HWEidGetIdentityInformation(input, new IEid.HWEidGetIdentityInformationCallback() {
                    /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass6 */

                    @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetIdentityInformationCallback
                    public void onValues(int hwEidGetIdentityInformationRet, IDENTITY_INFORMATION_S idInfo) {
                        HwEidHidlAdapter.this.updateIdentity(hwEidGetIdentityInformationRet, identityInfo, idInfo, identityInfoLen);
                    }
                });
            } catch (RemoteException e) {
                isExecption = true;
                Log.e(TAG, "Eid get identity information from mEid hidl failed");
            }
            ret = isExecption ? -1002 : this.mHwEidGetIdentityInformationRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid get identity information");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get identity information from mEid hidl ret: " + ret);
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIdentity(int hwEidGetIdentityInformationRet, byte[] identityInfo, IDENTITY_INFORMATION_S idInfo, int[] identityInfoLen) {
        if (hwEidGetIdentityInformationRet == 0 && identityInfo != null) {
            System.arraycopy(idInfo.info, 0, identityInfo, 0, Math.min(idInfo.len, identityInfo.length));
        }
        if (hwEidGetIdentityInformationRet == 0 && identityInfoLen != null && identityInfoLen.length > 0) {
            identityInfoLen[0] = idInfo.len;
        }
        this.mHwEidGetIdentityInformationRetValue = hwEidGetIdentityInformationRet;
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get identity information mHwEidGetIdentityInformationRetValue: " + this.mHwEidGetIdentityInformationRetValue + " hwEidGetIdentityInformationRet:" + hwEidGetIdentityInformationRet);
        }
    }

    public int eidGetFaceIsChangedHidlAdapter(int cmdId) {
        int ret;
        if (cmdId < 0) {
            return -1001;
        }
        boolean isExecption = false;
        FACE_CHANGE_INPUT_INFO_S input = new FACE_CHANGE_INPUT_INFO_S();
        input.cmdID = cmdId;
        getEidService(false);
        vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                iEid.HWEidGetFaceIsChanged(input, new IEid.HWEidGetFaceIsChangedCallback() {
                    /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass7 */

                    @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetFaceIsChangedCallback
                    public void onValues(int hwEidGetFaceIsChangedRet, FACE_CHANGE_OUTPUT_INFO_S output) {
                        HwEidHidlAdapter.this.updateFaceState(hwEidGetFaceIsChangedRet);
                    }
                });
            } catch (RemoteException e) {
                isExecption = true;
                Log.e(TAG, "Eid get face is changed from mEid hidl failed");
            }
            ret = isExecption ? -1002 : this.mHwEidGetFaceIsChangedRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid get face is changed");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get face is changed from mEid hidl ret: " + ret);
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFaceState(int hwEidGetFaceIsChangedRet) {
        this.mHwEidGetFaceIsChangedRetValue = hwEidGetFaceIsChangedRet;
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get face is changed mHwEidGetFaceIsChangedRetValue: " + this.mHwEidGetFaceIsChangedRetValue + " hwEidGetFaceIsChangedRet:" + hwEidGetFaceIsChangedRet);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c1 A[Catch:{ RemoteException -> 0x00c9 }, RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c2 A[Catch:{ RemoteException -> 0x00c9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00f0  */
    public int eidGetSecImageZipHidlAdapter(CoordinateEntity coordinateEntity, ControlWordEntity controlWord, List<EidInfoEntity> eidInfoEntityList, List<EidInfoExtendEntity> eidInfoExtendEntityList) {
        int ret;
        Optional<ENCRYPTION_FACTOR_S> optionF;
        boolean isParamValid;
        if (coordinateEntity == null || controlWord == null || eidInfoExtendEntityList == null || eidInfoEntityList == null || eidInfoEntityList.size() != 3 || eidInfoExtendEntityList.size() != 2) {
            return -1001;
        }
        EidInfoEntity certificateEntity = eidInfoEntityList.get(0);
        EidInfoEntity hashEntity = eidInfoEntityList.get(1);
        EidInfoEntity imageZipEntity = eidInfoEntityList.get(2);
        if (certificateEntity == null || hashEntity == null || imageZipEntity == null) {
            return -1001;
        }
        int hashLen = hashEntity.getContentLen();
        byte[] hash = hashEntity.getContent();
        int imageZipLen = imageZipEntity.getContentLen();
        byte[] imageZip = imageZipEntity.getContent();
        boolean isExecption = false;
        getEidService(false);
        if (this.mEidInstance != null) {
            try {
                Optional<IMAGE_ZIP_CONTAINER_S> option = getNewEidContainer(hashLen, hash, imageZipLen, imageZip);
                try {
                    optionF = getNewEidFactor(controlWord.getEncryptionMethod(), 1, certificateEntity.getContentLen(), certificateEntity.getContent());
                } catch (RemoteException e) {
                    isExecption = true;
                    if (!isExecption) {
                    }
                    if (HW_DEBUG) {
                    }
                    return ret;
                }
                try {
                    Optional<CUT_COORDINATE_S> optionC = getNewEidCoordinate(coordinateEntity.getUp(), coordinateEntity.getDown(), coordinateEntity.getLeft(), coordinateEntity.getRight());
                    ENCRYPTION_FACTOR_S factor = null;
                    IMAGE_ZIP_CONTAINER_S container = option.isPresent() ? option.get() : null;
                    CUT_COORDINATE_S coordinate = optionC.isPresent() ? optionC.get() : null;
                    if (optionF.isPresent()) {
                        factor = optionF.get();
                    }
                    if (!(container == null || factor == null)) {
                        if (coordinate != null) {
                            isParamValid = false;
                            if (!isParamValid) {
                                return -1001;
                            }
                            getSecImageZip(eidInfoExtendEntityList, container, coordinate, factor);
                            freeBinderMemory();
                            ret = !isExecption ? -1002 : this.mHwEidGetSecImageZipRetValue;
                        }
                    }
                    isParamValid = true;
                    if (!isParamValid) {
                    }
                } catch (RemoteException e2) {
                    isExecption = true;
                    if (!isExecption) {
                    }
                    if (HW_DEBUG) {
                    }
                    return ret;
                }
            } catch (RemoteException e3) {
                isExecption = true;
                if (!isExecption) {
                }
                if (HW_DEBUG) {
                }
                return ret;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
        }
        if (HW_DEBUG) {
            Log.d(TAG, "EidGetSecImageZip from mEid hidl ret: " + ret);
        }
        return ret;
    }

    private void getSecImageZip(List<EidInfoExtendEntity> eidInfoExtendEntityList, IMAGE_ZIP_CONTAINER_S container, CUT_COORDINATE_S coordinate, ENCRYPTION_FACTOR_S factor) throws RemoteException {
        final EidInfoExtendEntity secImageEntity = eidInfoExtendEntityList.get(0);
        final EidInfoExtendEntity deSkeyEntity = eidInfoExtendEntityList.get(1);
        if (secImageEntity == null) {
            return;
        }
        if (deSkeyEntity != null) {
            final int[] secImageLen = secImageEntity.getContentLen();
            final byte[] secImage = secImageEntity.getContent();
            final int[] deSkeyLen = deSkeyEntity.getContentLen();
            final byte[] deSkey = deSkeyEntity.getContent();
            this.mEidInstance.HWEidGetSecImageZip(container, coordinate, factor, new IEid.HWEidGetSecImageZipCallback() {
                /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass8 */

                @Override // vendor.huawei.hardware.eid.V1_1.IEid.HWEidGetSecImageZipCallback
                public void onValues(int hwEidGetSecImageZipRet, SEC_IMAGE_ZIP_S output) {
                    int[] iArr;
                    int[] iArr2;
                    if (hwEidGetSecImageZipRet == 0) {
                        byte[] bArr = secImage;
                        if (!(bArr == null || (iArr2 = secImageLen) == null || iArr2.length <= 0)) {
                            iArr2[0] = Math.min(bArr.length, output.len);
                            secImageEntity.setContentLen(secImageLen);
                            System.arraycopy(output.image, 0, secImage, 0, secImageLen[0]);
                            secImageEntity.setContent(secImage);
                        }
                        byte[] bArr2 = deSkey;
                        if (!(bArr2 == null || (iArr = deSkeyLen) == null || iArr.length <= 0)) {
                            iArr[0] = Math.min(bArr2.length, output.deSkeyLen);
                            deSkeyEntity.setContentLen(deSkeyLen);
                            System.arraycopy(output.deSkey, 0, deSkey, 0, deSkeyLen[0]);
                            deSkeyEntity.setContent(deSkey);
                        }
                    }
                    HwEidHidlAdapter.this.mHwEidGetSecImageZipRetValue = hwEidGetSecImageZipRet;
                }
            });
        }
    }

    public int eidGetUnsecImageZipHidlAdapter(ControlWordEntity controlWord, List<EidInfoEntity> eidInfoEntityList, List<EidInfoExtendEntity> eidInfoExtendEntityList) {
        int ret;
        if (controlWord == null || eidInfoEntityList == null || eidInfoExtendEntityList == null || eidInfoEntityList.size() != 3 || eidInfoExtendEntityList.size() != 2) {
            return -1001;
        }
        EidInfoEntity certificateEntity = eidInfoEntityList.get(0);
        EidInfoEntity hashEntity = eidInfoEntityList.get(1);
        EidInfoEntity imageZipEntity = eidInfoEntityList.get(2);
        if (certificateEntity == null || hashEntity == null || imageZipEntity == null) {
            return -1001;
        }
        int encryptionMethod = controlWord.getEncryptionMethod();
        int certificateLen = certificateEntity.getContentLen();
        byte[] certificate = certificateEntity.getContent();
        boolean isExecption = false;
        getEidService(false);
        if (this.mEidInstance != null) {
            try {
                Optional<IMAGE_ZIP_CONTAINER_S> option = getNewEidContainer(hashEntity.getContentLen(), hashEntity.getContent(), imageZipEntity.getContentLen(), imageZipEntity.getContent());
                Optional<ENCRYPTION_FACTOR_S> optionF = getNewEidFactor(encryptionMethod, 1, certificateLen, certificate);
                ENCRYPTION_FACTOR_S factor = null;
                IMAGE_ZIP_CONTAINER_S container = option.isPresent() ? option.get() : null;
                if (optionF.isPresent()) {
                    factor = optionF.get();
                }
                if (container != null) {
                    if (factor != null) {
                        getUnsecImageZip(eidInfoExtendEntityList, container, factor);
                        freeBinderMemory();
                        ret = isExecption ? -1002 : this.mHwEidGetUnSecImageZipRetValue;
                    }
                }
                Log.e(TAG, "EidGetUnsecImageZip new container or new factor error, ret:-1001");
                return -1001;
            } catch (RemoteException e) {
                Log.e(TAG, "EidGetUnSecImageZip from mEid hidl failed");
                isExecption = true;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when HWEidGetUnsecImageZip");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "EidGetUnSecImageZip from mEid hidl ret: " + ret);
        }
        return ret;
    }

    private void getUnsecImageZip(List<EidInfoExtendEntity> eidInfoExtendEntityList, IMAGE_ZIP_CONTAINER_S container, ENCRYPTION_FACTOR_S factor) throws RemoteException {
        final EidInfoExtendEntity unSecImageEntity = eidInfoExtendEntityList.get(0);
        final EidInfoExtendEntity deSkeyEntity = eidInfoExtendEntityList.get(1);
        if (unSecImageEntity == null) {
            return;
        }
        if (deSkeyEntity != null) {
            final int[] unsecImageLen = unSecImageEntity.getContentLen();
            final byte[] unsecImage = unSecImageEntity.getContent();
            final int[] deSkeyLen = deSkeyEntity.getContentLen();
            final byte[] deSkey = deSkeyEntity.getContent();
            this.mEidInstance.HWEidGetUnsecImageZip(container, factor, new IEid.HWEidGetUnsecImageZipCallback() {
                /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass9 */

                @Override // vendor.huawei.hardware.eid.V1_1.IEid.HWEidGetUnsecImageZipCallback
                public void onValues(int hwEidGetUnSecImageZipRet, SEC_IMAGE_ZIP_S output) {
                    int[] iArr;
                    int[] iArr2;
                    if (hwEidGetUnSecImageZipRet == 0 && (iArr = unsecImageLen) != null && iArr.length > 0 && (iArr2 = deSkeyLen) != null && iArr2.length > 0) {
                        iArr[0] = Math.min(output.len, unsecImage.length);
                        unSecImageEntity.setContentLen(unsecImageLen);
                        System.arraycopy(output.image, 0, unsecImage, 0, unsecImageLen[0]);
                        unSecImageEntity.setContent(unsecImage);
                        deSkeyLen[0] = Math.min(output.deSkeyLen, deSkey.length);
                        deSkeyEntity.setContentLen(deSkeyLen);
                        System.arraycopy(output.deSkey, 0, deSkey, 0, deSkeyLen[0]);
                        deSkeyEntity.setContent(deSkey);
                    }
                    HwEidHidlAdapter.this.mHwEidGetUnSecImageZipRetValue = hwEidGetUnSecImageZipRet;
                }
            });
        }
    }

    public String eidGetVersionHidlAdapter() {
        String ret = null;
        getEidService(false);
        vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                iEid.HWEidGetVersion(new IEid.HWEidGetVersionCallback() {
                    /* class com.huawei.eid.HwEidHidlAdapter.AnonymousClass10 */

                    @Override // vendor.huawei.hardware.eid.V1_1.IEid.HWEidGetVersionCallback
                    public void onValues(int hwmEidGetVersionRet, HIDL_VERSION_S output) {
                        if (hwmEidGetVersionRet == 0) {
                            HwEidHidlAdapter hwEidHidlAdapter = HwEidHidlAdapter.this;
                            hwEidHidlAdapter.mEidGetVersionRet = output.main + "." + output.sub;
                        }
                    }
                });
                ret = this.mEidGetVersionRet;
                if (HW_DEBUG) {
                    Log.d(TAG, "HWEidGetVersion from mEid hidl ret:" + ret);
                }
                freeBinderMemory();
            } catch (RemoteException e) {
                Log.e(TAG, "HWEidGetVersion from mEid hidl failed");
            }
        } else {
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when HWEidGetVersion");
        }
        return ret;
    }

    public int ctidSetSecModeHidlAdapter() {
        int ret;
        getEidService(false);
        vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                ret = iEid.HWCtidSetSecMode();
                if (HW_DEBUG) {
                    Log.d(TAG, "Ctid SetSecMode ret: " + ret);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "HW CtidSetSecMode from hidl failed, RemoteException occurred");
                return -1002;
            }
        } else {
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when eid init");
            ret = -1000;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid SetSecMode from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int ctidGetSecImageHidlAdapter() {
        int ret;
        getEidService(false);
        vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEidInstance;
        if (iEid != null) {
            try {
                ret = iEid.HWCtidGetImage();
                if (HW_DEBUG) {
                    Log.d(TAG, "Ctid getSecImage ret: " + ret);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "HW Ctid getSecImage from hidl failed, RemoteException occurred");
                return -1002;
            }
        } else {
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Ctid hidl deamon is not ready when eid init");
            ret = -1000;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid getSecImage from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int ctidGetServiceVerionInfoHidlAdapter(byte[] uuid, int uuidLen, String taPath, int[] cmdList, int cmdCount) {
        int ret;
        boolean isExecption = false;
        getEidService(false);
        int i = 1;
        if (this.mEidInstance != null) {
            try {
                INIT_CTID_TA_MSG_S input = new INIT_CTID_TA_MSG_S();
                input.uuid_len = Math.min(16, uuidLen);
                input.cmdlist_cnt = cmdCount;
                if (uuid != null) {
                    System.arraycopy(uuid, 0, input.uuid, 0, input.uuid_len);
                }
                if (taPath != null) {
                    input.ta_path_len = Math.min(511, taPath.length());
                    System.arraycopy(taPath.getBytes("UTF-8"), 0, input.ta_path, 0, input.ta_path_len);
                }
                if (cmdList != null) {
                    System.arraycopy(cmdList, 0, input.cmd_list, 0, Math.min(cmdList.length, input.cmdlist_cnt));
                }
                if (HW_DEBUG && taPath != null) {
                    Log.d(TAG, "Ctid init param uuid_len: " + uuidLen + " ta path len:" + taPath.length() + "cmd cnt:" + cmdCount);
                }
                this.mEidInstance.HWCtidInitTa(input);
            } catch (RemoteException e) {
                isExecption = true;
                Log.e(TAG, "Ctid get service version from mEid hidl failed, RemoteException occurred");
            } catch (UnsupportedEncodingException e2) {
                isExecption = true;
                Log.e(TAG, "TaPath get bytes failed");
            }
            if (isExecption) {
                i = -1002;
            }
            ret = i;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "Eid hidl deamon is not ready when ctid get service version");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid get version info from mEid hidl ret: " + ret);
        }
        return ret;
    }

    private void getEidService(boolean flag) {
        if (this.mEidInstance == null) {
            try {
                this.mEidInstance = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
                if (flag) {
                    try {
                        this.mEidInstance.linkToDeath(this.mEidHidlDeamonDeathRecipient, 0);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Exception occured when linkToDeath in handle message");
                    }
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
    }
}

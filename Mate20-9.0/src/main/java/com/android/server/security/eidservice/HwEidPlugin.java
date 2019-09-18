package com.android.server.security.eidservice;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.security.core.IHwSecurityPlugin;
import huawei.android.security.IHwEidPlugin;
import java.util.Arrays;
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

public class HwEidPlugin extends IHwEidPlugin.Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (HwEidPlugin.HW_DEBUG) {
                Log.d(HwEidPlugin.TAG, "create HwEidPlugin");
            }
            return new HwEidPlugin(context);
        }

        public String getPluginPermission() {
            return HwEidPlugin.EID_MANAGER_PERMISSION;
        }
    };
    private static final int CTID_VERSION = 1;
    private static final String EID_HIDL_SERVICE_NAME = "eid";
    private static final String EID_MANAGER_PERMISSION = "huawei.android.permission.EID_PERMISSION";
    private static final String EID_VERSION = "1.0";
    private static final int ENCRY_SET_SECMODE = 3;
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (SystemProperties.get("ro.secure", "1").equals("0") || Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int IMAGE_NV21_HEIGH = 640;
    private static final int IMAGE_NV21_WEIGHT = 480;
    private static final int INPUT_MAX_TRANSPOT_LEN = 153600;
    private static final int INPUT_TRANSPOT_TIMES = 3;
    private static final int MAX_EID_HIDL_DEAMON_REGISTER_TIMES = 10;
    private static final int MSG_EID_HIDL_DEAMON_SERVIE_REGISTER = 1;
    private static final int OUTPUT_MAX_TRANSPOT_LEN = 163840;
    private static final int OUTPUT_TRANSPOT_TIMES = 3;
    private static final int RET_DEFAULT_ERROR_VALUE = -1001;
    private static final int RET_EID_HIDL_DEAMON_IS_NOT_READY = -1000;
    private static final int RET_EXCEPTION_WHEN_EID_CALL = -1002;
    private static final String TAG = "HwEidPlugin";
    private static final int TRY_GET_HIDL_DEAMON_DEALY_MILLIS = 1000;
    /* access modifiers changed from: private */
    public int HWEidGetCertificateRequestMessageRetValue = -1001;
    /* access modifiers changed from: private */
    public int HWEidGetFaceIsChangedRetValue = -1001;
    /* access modifiers changed from: private */
    public int HWEidGetIdentityInformationRetValue = -1001;
    /* access modifiers changed from: private */
    public int HWEidGetImageRetValue = -1001;
    /* access modifiers changed from: private */
    public int HWEidGetInfoSignRetValue = -1001;
    /* access modifiers changed from: private */
    public int HWEidGetSecImageZipRetValue = -1001;
    /* access modifiers changed from: private */
    public int HWEidGetUnSecImageZipRetValue = -1001;
    /* access modifiers changed from: private */
    public int HWEidGetUnsecImageRetValue = -1001;
    /* access modifiers changed from: private */
    public String eidGetVersionRet = "";
    private Context mContext = null;
    /* access modifiers changed from: private */
    public IEid mEid;
    /* access modifiers changed from: private */
    public IHwBinder.DeathRecipient mEidHidlDeamonDeathRecipient = new IHwBinder.DeathRecipient() {
        public void serviceDied(long cookie) {
            if (HwEidPlugin.this.mHwEidHidlHandler != null) {
                Log.e(HwEidPlugin.TAG, "eid hidl deamon service has died, try to reconnect it later.");
                IEid unused = HwEidPlugin.this.mEid = null;
                HwEidPlugin.this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mEidHidlDeamonRegisterTryTimes = 0;
    /* access modifiers changed from: private */
    public HwEidHidlHandler mHwEidHidlHandler;
    private HandlerThread mHwEidThread;

    private final class HwEidHidlHandler extends Handler {
        public HwEidHidlHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Log.e(HwEidPlugin.TAG, "handler thread received unknown message : " + msg.what);
                return;
            }
            try {
                IEid unused = HwEidPlugin.this.mEid = IEid.getService(HwEidPlugin.EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(HwEidPlugin.TAG, "Try get eid hidl deamon servcie failed in handler message.");
            }
            if (HwEidPlugin.this.mEid != null) {
                int unused2 = HwEidPlugin.this.mEidHidlDeamonRegisterTryTimes = 0;
                try {
                    HwEidPlugin.this.mEid.linkToDeath(HwEidPlugin.this.mEidHidlDeamonDeathRecipient, 0);
                } catch (Exception e2) {
                    Log.e(HwEidPlugin.TAG, "Exception occured when linkToDeath in handle message");
                }
            } else {
                int unused3 = HwEidPlugin.this.mEidHidlDeamonRegisterTryTimes = HwEidPlugin.this.mEidHidlDeamonRegisterTryTimes + 1;
                if (HwEidPlugin.this.mEidHidlDeamonRegisterTryTimes < 10) {
                    Log.e(HwEidPlugin.TAG, "eid hidl daemon service is not ready, try times : " + HwEidPlugin.this.mEidHidlDeamonRegisterTryTimes);
                    HwEidPlugin.this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    Log.e(HwEidPlugin.TAG, "eid hidl daemon service connection failed.");
                }
            }
            if (HwEidPlugin.HW_DEBUG) {
                Log.d(HwEidPlugin.TAG, "handler thread received request eid hidl deamon message.");
            }
        }
    }

    public HwEidPlugin(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.security.eidservice.HwEidPlugin, android.os.IBinder] */
    public IBinder asBinder() {
        checkPermission(EID_MANAGER_PERMISSION);
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Log.d(TAG, "HwEidPlugin start");
        }
        this.mHwEidThread = new HandlerThread(TAG);
        this.mHwEidThread.start();
        this.mHwEidHidlHandler = new HwEidHidlHandler(this.mHwEidThread.getLooper());
    }

    public void onStop() {
        if (this.mHwEidHidlHandler != null) {
            this.mHwEidHidlHandler = null;
        }
        if (this.mHwEidThread != null) {
            this.mHwEidThread.quitSafely();
            this.mHwEidThread = null;
        }
        try {
            if (HW_DEBUG) {
                Log.d(TAG, "close HwEidPlugin");
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "stop error" + e.toString());
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    /* access modifiers changed from: private */
    public int minArrayLen(int arrLenA, int arrLenB) {
        return arrLenA > arrLenB ? arrLenB : arrLenA;
    }

    private void freeBinderMemory() {
        try {
            IEid service = IEid.getService(EID_HIDL_SERVICE_NAME);
            Log.d(TAG, "eid get image release binder memory.");
        } catch (Exception e) {
            Log.e(TAG, "eid get image release binder memory fail.");
        }
    }

    private CUT_COORDINATE_S getNewEidCoordinate(int up, int down, int left, int right) {
        if (up < 0 || down > IMAGE_NV21_HEIGH || up >= down) {
            Log.e(TAG, "getNewEidCoordinate: The para up down is error, up : " + up + " down : " + down);
            return null;
        } else if (left < 0 || right > IMAGE_NV21_WEIGHT || left >= right) {
            Log.e(TAG, "getNewEidCoordinate: The para left down is error, left : " + left + " right : " + right);
            return null;
        } else {
            CUT_COORDINATE_S coordinate = new CUT_COORDINATE_S();
            coordinate.up = up;
            coordinate.down = down;
            coordinate.left = left;
            coordinate.right = right;
            return coordinate;
        }
    }

    private ENCRYPTION_FACTOR_S getNewEidFactor(int encryption_method, int splitTime, int certificate_len, byte[] certificate) {
        if (certificate == null) {
            Log.e(TAG, "The para certificate is null");
            return null;
        }
        ENCRYPTION_FACTOR_S factor = new ENCRYPTION_FACTOR_S();
        factor.encryptionMethod = encryption_method;
        factor.certificateLen = certificate_len;
        factor.splitTimes = splitTime;
        if (certificate.length > factor.certificate.length || certificate_len > certificate.length) {
            Log.e(TAG, "getNewEidFactor: certificate_len: " + certificate_len + " certificate.length:" + certificate.length + " factor.certificate.length: " + factor.certificate.length);
            return null;
        }
        System.arraycopy(certificate, 0, factor.certificate, 0, certificate_len);
        return factor;
    }

    private IMAGE_ZIP_CONTAINER_S getNewEidContainer(int hash_len, byte[] hash, int image_zip_len, byte[] image_zip) {
        if (hash == null || image_zip == null) {
            Log.e(TAG, "getNewEidContainer: The para hash or image_zip is null");
            return null;
        }
        IMAGE_ZIP_CONTAINER_S container = new IMAGE_ZIP_CONTAINER_S();
        container.hash_len = hash_len;
        if (hash.length > container.hash.length || hash_len > hash.length) {
            Log.e(TAG, "getNewEidContainer: hash.length:" + hash.length + ", container.hash.length : " + container.hash.length + " hash_len: " + hash_len);
            return null;
        }
        System.arraycopy(hash, 0, container.hash, 0, hash_len);
        container.image_len = image_zip_len;
        if (image_zip.length > container.image.length || image_zip_len > image_zip.length) {
            Log.e(TAG, "getNewEidContainer: image_zip.length: " + image_zip.length + " container.image.length: " + container.image.length + " image_zip_len: " + image_zip_len);
            return null;
        }
        System.arraycopy(image_zip, 0, container.image, 0, image_zip_len);
        return container;
    }

    public int eid_init(byte[] hw_aid, int hw_aid_len, byte[] eid_aid, int eid_aid_len, byte[] logo, int logo_size) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hidlServiceErr = false;
        INIT_TA_MSG_S input = new INIT_TA_MSG_S();
        input.hw_aid_len = hw_aid_len;
        input.eid_aid_len = eid_aid_len;
        input.logo_size = logo_size;
        if (hw_aid != null) {
            System.arraycopy(hw_aid, 0, input.hw_aid, 0, minArrayLen(hw_aid.length, input.hw_aid.length));
        }
        if (eid_aid != null) {
            System.arraycopy(eid_aid, 0, input.eid_aid, 0, minArrayLen(eid_aid.length, input.eid_aid.length));
        }
        if (logo != null) {
            System.arraycopy(logo, 0, input.eid_logo, 0, minArrayLen(logo.length, input.eid_logo.length));
        }
        if (this.mEid == null) {
            try {
                this.mEid = IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
                hidlServiceErr = true;
            }
            if (!hidlServiceErr) {
                try {
                    this.mEid.linkToDeath(this.mEidHidlDeamonDeathRecipient, 0);
                } catch (Exception e2) {
                    Log.e(TAG, "Exception occured when linkToDeath in handle message");
                }
            }
        }
        if (this.mEid != null) {
            try {
                ret = this.mEid.HWEidInitTa(input);
                if (HW_DEBUG) {
                    Log.d(TAG, "eid init ret : " + ret);
                }
            } catch (RemoteException e3) {
                Log.e(TAG, "eid init from hidl failed.");
                Log.e(TAG, "RemoteException e:" + e3);
                return -1002;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "eid hidl deamon is not ready when eid init");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid init from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public int eid_finish() {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (this.mEid == null) {
            try {
                this.mEid = IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                ret = this.mEid.HWEidFiniTa();
                if (HW_DEBUG) {
                    Log.d(TAG, "eid finish ta ret : " + ret);
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "eid finish from mEid hidl failed.");
                return -1002;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "eid hidl deamon is not ready when eid finish");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid finish from mEid hidl ret : " + ret);
        }
        return ret;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b7  */
    public int eid_get_image(int transpotCounter, int encryption_method, byte[] certificate, int certificate_len, byte[] image, int[] image_len, byte[] de_skey, int[] de_skey_len) {
        int ret;
        byte[] bArr = certificate;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        if (this.mEid == null) {
            try {
                this.mEid = IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                ENCRYPTION_FACTOR_S factor = new ENCRYPTION_FACTOR_S();
                int i = encryption_method;
                try {
                    factor.encryptionMethod = i;
                    try {
                        factor.certificateLen = certificate_len;
                        factor.splitTimes = transpotCounter;
                        if (bArr != null) {
                            System.arraycopy(bArr, 0, factor.certificate, 0, minArrayLen(bArr.length, factor.certificate.length));
                        }
                        IEid iEid = this.mEid;
                        final int i2 = i;
                        final byte[] bArr2 = image;
                        final ENCRYPTION_FACTOR_S encryption_factor_s = factor;
                        final byte[] bArr3 = de_skey;
                        AnonymousClass3 r10 = r1;
                        final int[] iArr = image_len;
                        final int[] iArr2 = de_skey_len;
                        AnonymousClass3 r1 = new IEid.HWEidGetImageCallback() {
                            public void onValues(int HWEidGetImageRet, SEC_IMAGE_S secImage) {
                                if (HWEidGetImageRet == 0 && i2 != 3) {
                                    if (bArr2 != null) {
                                        System.arraycopy(secImage.image, 0, bArr2, 0, HwEidPlugin.this.minArrayLen(secImage.image.length, bArr2.length));
                                    }
                                    if (1 == encryption_factor_s.splitTimes) {
                                        if (bArr3 != null) {
                                            System.arraycopy(secImage.deSkey, 0, bArr3, 0, HwEidPlugin.this.minArrayLen(secImage.deSkeyLen, bArr3.length));
                                        }
                                        iArr[0] = secImage.len;
                                        if (iArr2 != null) {
                                            iArr2[0] = secImage.deSkeyLen;
                                        }
                                    }
                                }
                                if (HwEidPlugin.HW_DEBUG) {
                                    Log.d(HwEidPlugin.TAG, "eid get image call counter: " + encryption_factor_s.splitTimes);
                                }
                                int unused = HwEidPlugin.this.HWEidGetImageRetValue = HWEidGetImageRet;
                                if (HwEidPlugin.HW_DEBUG) {
                                    Log.d(HwEidPlugin.TAG, "eid get image HWEidGetImageRetValue : " + HwEidPlugin.this.HWEidGetImageRetValue + " HWEidGetImageRet:" + HWEidGetImageRet);
                                }
                            }
                        };
                        iEid.HWEidGetImage(factor, r10);
                        try {
                            vendor.huawei.hardware.eid.V1_1.IEid service = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
                            Log.d(TAG, "eid get image release binder memory.");
                        } catch (Exception e2) {
                            Log.e(TAG, "eid get image release binder memory fail.");
                        }
                    } catch (RemoteException e3) {
                        e = e3;
                    }
                } catch (RemoteException e4) {
                    e = e4;
                    int i3 = certificate_len;
                    Log.e(TAG, "eid get image from mEid hidl failed." + e);
                    hasExecption = true;
                    if (hasExecption) {
                    }
                    if (HW_DEBUG) {
                    }
                    return ret;
                }
            } catch (RemoteException e5) {
                e = e5;
                int i4 = encryption_method;
                int i32 = certificate_len;
                Log.e(TAG, "eid get image from mEid hidl failed." + e);
                hasExecption = true;
                if (hasExecption) {
                }
                if (HW_DEBUG) {
                }
                return ret;
            }
            ret = hasExecption ? -1002 : this.HWEidGetImageRetValue;
        } else {
            int i5 = encryption_method;
            int i6 = certificate_len;
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when eid get image");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid get image from mEid hidl ret : " + ret);
        }
        return ret;
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00e4  */
    public int eid_get_unsec_image(byte[] src_image, int src_image_len, int transpotCounter, int encryption_method, byte[] certificate, int certificate_len, byte[] image, int[] image_len, byte[] de_skey, int[] de_skey_len) {
        int ret;
        byte[] bArr = src_image;
        byte[] bArr2 = certificate;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                IMAGE_CONTAINER_S container = new IMAGE_CONTAINER_S();
                ENCRYPTION_FACTOR_S factor = new ENCRYPTION_FACTOR_S();
                try {
                    factor.splitTimes = transpotCounter;
                    if (factor.splitTimes < 3) {
                        if (bArr != null) {
                            System.arraycopy(bArr, 0, container.image, 0, minArrayLen(bArr.length, container.image.length));
                        }
                    } else if (factor.splitTimes == 3) {
                        try {
                            container.len = src_image_len;
                            if (bArr != null) {
                                System.arraycopy(bArr, 0, container.image, 0, minArrayLen(bArr.length, container.image.length));
                            }
                            if (bArr2 != null) {
                                System.arraycopy(bArr2, 0, factor.certificate, 0, minArrayLen(bArr2.length, factor.certificate.length));
                            }
                            try {
                                factor.encryptionMethod = encryption_method;
                                factor.certificateLen = certificate_len;
                                vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEid;
                                final ENCRYPTION_FACTOR_S encryption_factor_s = factor;
                                AnonymousClass4 r9 = r1;
                                final byte[] bArr3 = de_skey;
                                final int[] iArr = image_len;
                                final int[] iArr2 = de_skey_len;
                                final byte[] bArr4 = image;
                                AnonymousClass4 r1 = new IEid.HWEidGetUnsecImageCallback() {
                                    public void onValues(int HWEidGetUnsecImageRet, SEC_IMAGE_S secImage) {
                                        if (encryption_factor_s.splitTimes >= 3 && HWEidGetUnsecImageRet == 0) {
                                            if (encryption_factor_s.splitTimes == 3) {
                                                if (bArr3 != null) {
                                                    System.arraycopy(secImage.deSkey, 0, bArr3, 0, HwEidPlugin.this.minArrayLen(secImage.deSkey.length, bArr3.length));
                                                }
                                                if (iArr != null) {
                                                    iArr[0] = secImage.len;
                                                }
                                                if (iArr2 != null) {
                                                    iArr2[0] = secImage.deSkeyLen;
                                                }
                                            }
                                            if (bArr4 != null) {
                                                System.arraycopy(secImage.image, 0, bArr4, 0, HwEidPlugin.this.minArrayLen(secImage.image.length, bArr4.length));
                                            }
                                        }
                                        if (HwEidPlugin.HW_DEBUG) {
                                            Log.d(HwEidPlugin.TAG, "eid get unsec image call counter: " + encryption_factor_s.splitTimes);
                                        }
                                        int unused = HwEidPlugin.this.HWEidGetUnsecImageRetValue = HWEidGetUnsecImageRet;
                                        if (HwEidPlugin.HW_DEBUG) {
                                            Log.d(HwEidPlugin.TAG, "eid get unsec image HWEidGetUnsecImageRetValue : " + HwEidPlugin.this.HWEidGetUnsecImageRetValue + " HWEidGetUnsecImageRet:" + HWEidGetUnsecImageRet);
                                        }
                                    }
                                };
                                iEid.HWEidGetUnsecImage(container, factor, r9);
                                vendor.huawei.hardware.eid.V1_1.IEid service = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
                                Log.d(TAG, "eid get unsec image release binder memory.");
                            } catch (RemoteException e2) {
                                int i = certificate_len;
                                Log.e(TAG, "eid get unsec image from mEid hidl failed.");
                                hasExecption = true;
                                if (hasExecption) {
                                }
                                if (HW_DEBUG) {
                                }
                                return ret;
                            }
                        } catch (RemoteException e3) {
                            int i2 = encryption_method;
                            int i3 = certificate_len;
                            Log.e(TAG, "eid get unsec image from mEid hidl failed.");
                            hasExecption = true;
                            if (hasExecption) {
                            }
                            if (HW_DEBUG) {
                            }
                            return ret;
                        }
                        ret = hasExecption ? -1002 : this.HWEidGetUnsecImageRetValue;
                    }
                    int i4 = src_image_len;
                    int i5 = encryption_method;
                    int i6 = certificate_len;
                    vendor.huawei.hardware.eid.V1_1.IEid iEid2 = this.mEid;
                    final ENCRYPTION_FACTOR_S encryption_factor_s2 = factor;
                    AnonymousClass4 r92 = r1;
                    final byte[] bArr32 = de_skey;
                    final int[] iArr3 = image_len;
                    final int[] iArr22 = de_skey_len;
                    final byte[] bArr42 = image;
                    AnonymousClass4 r12 = new IEid.HWEidGetUnsecImageCallback() {
                        public void onValues(int HWEidGetUnsecImageRet, SEC_IMAGE_S secImage) {
                            if (encryption_factor_s2.splitTimes >= 3 && HWEidGetUnsecImageRet == 0) {
                                if (encryption_factor_s2.splitTimes == 3) {
                                    if (bArr32 != null) {
                                        System.arraycopy(secImage.deSkey, 0, bArr32, 0, HwEidPlugin.this.minArrayLen(secImage.deSkey.length, bArr32.length));
                                    }
                                    if (iArr3 != null) {
                                        iArr3[0] = secImage.len;
                                    }
                                    if (iArr22 != null) {
                                        iArr22[0] = secImage.deSkeyLen;
                                    }
                                }
                                if (bArr42 != null) {
                                    System.arraycopy(secImage.image, 0, bArr42, 0, HwEidPlugin.this.minArrayLen(secImage.image.length, bArr42.length));
                                }
                            }
                            if (HwEidPlugin.HW_DEBUG) {
                                Log.d(HwEidPlugin.TAG, "eid get unsec image call counter: " + encryption_factor_s2.splitTimes);
                            }
                            int unused = HwEidPlugin.this.HWEidGetUnsecImageRetValue = HWEidGetUnsecImageRet;
                            if (HwEidPlugin.HW_DEBUG) {
                                Log.d(HwEidPlugin.TAG, "eid get unsec image HWEidGetUnsecImageRetValue : " + HwEidPlugin.this.HWEidGetUnsecImageRetValue + " HWEidGetUnsecImageRet:" + HWEidGetUnsecImageRet);
                            }
                        }
                    };
                    iEid2.HWEidGetUnsecImage(container, factor, r92);
                    try {
                        vendor.huawei.hardware.eid.V1_1.IEid service2 = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
                        Log.d(TAG, "eid get unsec image release binder memory.");
                    } catch (Exception e4) {
                        Log.e(TAG, "eid get unsec image release binder memory fail.");
                    }
                } catch (RemoteException e5) {
                }
            } catch (RemoteException e6) {
                int i7 = transpotCounter;
                Log.e(TAG, "eid get unsec image from mEid hidl failed.");
                hasExecption = true;
                if (hasExecption) {
                }
                if (HW_DEBUG) {
                }
                return ret;
            }
            if (hasExecption) {
            }
        } else {
            int i8 = transpotCounter;
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when eid get unsec image");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid get unsec image from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public int eid_get_certificate_request_message(final byte[] request_message, final int[] message_len) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S input = new CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S();
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                this.mEid.HWEidGetCertificateRequestMessage(input, new IEid.HWEidGetCertificateRequestMessageCallback() {
                    public void onValues(int HWEidGetCertificateRequestMessageRet, CERTIFICATE_REQUEST_MESSAGE_S certReqMsg) {
                        if (HWEidGetCertificateRequestMessageRet == 0) {
                            System.arraycopy(certReqMsg.message, 0, request_message, 0, HwEidPlugin.this.minArrayLen(certReqMsg.len, request_message.length));
                            message_len[0] = certReqMsg.len;
                            if (HwEidPlugin.HW_DEBUG) {
                                Log.d(HwEidPlugin.TAG, "eid get certificate request message message len : " + message_len[0] + " certReqMsg len:" + certReqMsg.len);
                            }
                        }
                        int unused = HwEidPlugin.this.HWEidGetCertificateRequestMessageRetValue = HWEidGetCertificateRequestMessageRet;
                        if (HwEidPlugin.HW_DEBUG) {
                            Log.d(HwEidPlugin.TAG, "eid get certificate request message HWEidGetCertificateRequestMessageRetValue : " + HwEidPlugin.this.HWEidGetCertificateRequestMessageRetValue + " HWEidGetCertificateRequestMessageRet:" + HWEidGetCertificateRequestMessageRet);
                        }
                    }
                });
            } catch (RemoteException e2) {
                hasExecption = true;
                Log.e(TAG, "eid get certificate request message from mEid hidl failed.");
            }
            ret = hasExecption ? -1002 : this.HWEidGetCertificateRequestMessageRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when eid get certificate request message");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid get certificate request message from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public int eid_sign_info(int transpotCounter, int encryption_method, byte[] info, int info_len, final byte[] sign, final int[] sign_len) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        Log.d(TAG, "eid_sign_begin");
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                final INFO_SIGN_INPUT_INFO_S input = new INFO_SIGN_INPUT_INFO_S();
                input.encryptionMethod = encryption_method;
                input.infoLen = info_len;
                input.splitTimes = transpotCounter;
                if (info != null) {
                    System.arraycopy(info, 0, input.info, 0, minArrayLen(info.length, input.info.length));
                }
                this.mEid.HWEidGetInfoSign(input, new IEid.HWEidGetInfoSignCallback() {
                    public void onValues(int HWEidGetInfoSignRet, INFO_SIGN_OUTPUT_INFO_S output) {
                        if (HWEidGetInfoSignRet == 0) {
                            if (1 == input.splitTimes && sign_len != null) {
                                sign_len[0] = output.infoLen;
                            }
                            if (sign != null) {
                                System.arraycopy(output.signInfo, 0, sign, 0, HwEidPlugin.this.minArrayLen(output.signInfo.length, sign.length));
                            }
                        }
                        if (HwEidPlugin.HW_DEBUG) {
                            Log.d(HwEidPlugin.TAG, "eid sign info call counter: " + input.splitTimes);
                        }
                        int unused = HwEidPlugin.this.HWEidGetInfoSignRetValue = HWEidGetInfoSignRet;
                        if (HwEidPlugin.HW_DEBUG) {
                            Log.d(HwEidPlugin.TAG, "eid sign info HWEidGetInfoSignRetValue : " + HwEidPlugin.this.HWEidGetInfoSignRetValue + " HWEidGetInfoSignRet:" + HWEidGetInfoSignRet);
                        }
                    }
                });
            } catch (RemoteException e2) {
                hasExecption = true;
                Log.e(TAG, "eid sign info from mEid hidl failed.");
            }
            ret = hasExecption ? -1002 : this.HWEidGetInfoSignRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when eid sign info");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid sign info from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public int eid_get_identity_information(final byte[] identity_info, final int[] identity_info_len) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        IDENTITY_INFORMATION_INPUT_INFO_S input = new IDENTITY_INFORMATION_INPUT_INFO_S();
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                this.mEid.HWEidGetIdentityInformation(input, new IEid.HWEidGetIdentityInformationCallback() {
                    public void onValues(int HWEidGetIdentityInformationRet, IDENTITY_INFORMATION_S idInfo) {
                        if (HWEidGetIdentityInformationRet == 0) {
                            if (identity_info != null) {
                                System.arraycopy(idInfo.info, 0, identity_info, 0, HwEidPlugin.this.minArrayLen(idInfo.len, identity_info.length));
                            }
                            if (identity_info_len != null) {
                                identity_info_len[0] = idInfo.len;
                            }
                        }
                        int unused = HwEidPlugin.this.HWEidGetIdentityInformationRetValue = HWEidGetIdentityInformationRet;
                        if (HwEidPlugin.HW_DEBUG) {
                            Log.d(HwEidPlugin.TAG, "eid get identity information HWEidGetIdentityInformationRetValue : " + HwEidPlugin.this.HWEidGetIdentityInformationRetValue + " HWEidGetIdentityInformationRet:" + HWEidGetIdentityInformationRet);
                        }
                    }
                });
            } catch (RemoteException e2) {
                hasExecption = true;
                Log.e(TAG, "eid get identity information from mEid hidl failed.");
            }
            ret = hasExecption ? -1002 : this.HWEidGetIdentityInformationRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when eid get identity information");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid get identity information from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public int eid_get_face_is_changed(int cmd_id) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        FACE_CHANGE_INPUT_INFO_S input = new FACE_CHANGE_INPUT_INFO_S();
        input.cmdID = cmd_id;
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                this.mEid.HWEidGetFaceIsChanged(input, new IEid.HWEidGetFaceIsChangedCallback() {
                    public void onValues(int HWEidGetFaceIsChangedRet, FACE_CHANGE_OUTPUT_INFO_S output) {
                        int unused = HwEidPlugin.this.HWEidGetFaceIsChangedRetValue = HWEidGetFaceIsChangedRet;
                        if (HwEidPlugin.HW_DEBUG) {
                            Log.d(HwEidPlugin.TAG, "eid get face is changed HWEidGetFaceIsChangedRetValue : " + HwEidPlugin.this.HWEidGetFaceIsChangedRetValue + " HWEidGetFaceIsChangedRet:" + HWEidGetFaceIsChangedRet);
                        }
                    }
                });
            } catch (RemoteException e2) {
                hasExecption = true;
                Log.e(TAG, "eid get face is changed from mEid hidl failed.");
            }
            ret = hasExecption ? -1002 : this.HWEidGetFaceIsChangedRetValue;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when eid get face is changed");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eid get face is changed from mEid hidl ret : " + ret);
        }
        return ret;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00c2  */
    public int eidGetSecImageZip(int hash_len, byte[] hash, int image_zip_len, byte[] image_zip, int up, int down, int left, int right, int encryption_method, int certificate_len, byte[] certificate, int[] sec_image_len, byte[] sec_image, int[] de_skey_len, byte[] de_skey) {
        int ret;
        boolean hasExecption;
        checkPermission(EID_MANAGER_PERMISSION);
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                IMAGE_ZIP_CONTAINER_S container = getNewEidContainer(hash_len, hash, image_zip_len, image_zip);
                try {
                    CUT_COORDINATE_S coordinate = getNewEidCoordinate(up, down, left, right);
                    try {
                        ENCRYPTION_FACTOR_S factor = getNewEidFactor(encryption_method, 1, certificate_len, certificate);
                        if (coordinate == null || container == null) {
                            ENCRYPTION_FACTOR_S encryption_factor_s = factor;
                        } else if (factor == null) {
                            ENCRYPTION_FACTOR_S encryption_factor_s2 = factor;
                        } else {
                            r1 = r1;
                            AnonymousClass9 r8 = r1;
                            vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEid;
                            final int[] iArr = sec_image_len;
                            ENCRYPTION_FACTOR_S factor2 = factor;
                            final byte[] bArr = sec_image;
                            final int[] iArr2 = de_skey_len;
                            final byte[] bArr2 = de_skey;
                            try {
                                AnonymousClass9 r1 = new IEid.HWEidGetSecImageZipCallback() {
                                    public void onValues(int HWEidGetSecImageZipRet, SEC_IMAGE_ZIP_S output) {
                                        if (HWEidGetSecImageZipRet == 0) {
                                            iArr[0] = HwEidPlugin.this.minArrayLen(bArr.length, output.len);
                                            System.arraycopy(output.image, 0, bArr, 0, iArr[0]);
                                            iArr2[0] = HwEidPlugin.this.minArrayLen(bArr2.length, output.deSkeyLen);
                                            System.arraycopy(output.deSkey, 0, bArr2, 0, iArr2[0]);
                                        }
                                        int unused = HwEidPlugin.this.HWEidGetSecImageZipRetValue = HWEidGetSecImageZipRet;
                                    }
                                };
                                iEid.HWEidGetSecImageZip(container, coordinate, factor2, r8);
                                freeBinderMemory();
                                hasExecption = false;
                            } catch (RemoteException e2) {
                                Log.e(TAG, "eidGetSecImageZip from mEid hidl failed.");
                                hasExecption = true;
                                if (hasExecption) {
                                }
                                boolean z = hasExecption;
                                if (HW_DEBUG) {
                                }
                                return ret;
                            }
                            ret = hasExecption ? -1002 : this.HWEidGetSecImageZipRetValue;
                            boolean z2 = hasExecption;
                        }
                        Log.e(TAG, "eidGetSecImageZip new container, coordinate or factor error, ret : " + -1001);
                        return -1001;
                    } catch (RemoteException e3) {
                        Log.e(TAG, "eidGetSecImageZip from mEid hidl failed.");
                        hasExecption = true;
                        if (hasExecption) {
                        }
                        boolean z22 = hasExecption;
                        if (HW_DEBUG) {
                        }
                        return ret;
                    }
                } catch (RemoteException e4) {
                    int i = encryption_method;
                    Log.e(TAG, "eidGetSecImageZip from mEid hidl failed.");
                    hasExecption = true;
                    if (hasExecption) {
                    }
                    boolean z222 = hasExecption;
                    if (HW_DEBUG) {
                    }
                    return ret;
                }
            } catch (RemoteException e5) {
                int i2 = left;
                int i3 = right;
                int i4 = encryption_method;
                Log.e(TAG, "eidGetSecImageZip from mEid hidl failed.");
                hasExecption = true;
                if (hasExecption) {
                }
                boolean z2222 = hasExecption;
                if (HW_DEBUG) {
                }
                return ret;
            }
        } else {
            int i5 = left;
            int i6 = right;
            int i7 = encryption_method;
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when HWEidGetSecImageZip");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eidGetSecImageZip from mEid hidl ret : " + ret);
        }
        return ret;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009c  */
    public int eidGetUnsecImageZip(int hash_len, byte[] hash, int image_zip_len, byte[] image_zip, int encryption_method, int certificate_len, byte[] certificate, int[] unsec_image_len, byte[] unsec_image, int[] de_skey_len, byte[] de_skey) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                IMAGE_ZIP_CONTAINER_S container = getNewEidContainer(hash_len, hash, image_zip_len, image_zip);
                try {
                    ENCRYPTION_FACTOR_S factor = getNewEidFactor(encryption_method, 1, certificate_len, certificate);
                    if (container != null) {
                        if (factor != null) {
                            vendor.huawei.hardware.eid.V1_1.IEid iEid = this.mEid;
                            final int[] iArr = unsec_image_len;
                            final byte[] bArr = unsec_image;
                            final int[] iArr2 = de_skey_len;
                            final byte[] bArr2 = de_skey;
                            AnonymousClass10 r1 = new IEid.HWEidGetUnsecImageZipCallback() {
                                public void onValues(int HWEidGetUnSecImageZipRet, SEC_IMAGE_ZIP_S output) {
                                    if (HWEidGetUnSecImageZipRet == 0) {
                                        iArr[0] = HwEidPlugin.this.minArrayLen(output.len, bArr.length);
                                        System.arraycopy(output.image, 0, bArr, 0, iArr[0]);
                                        iArr2[0] = HwEidPlugin.this.minArrayLen(output.deSkeyLen, bArr2.length);
                                        System.arraycopy(output.deSkey, 0, bArr2, 0, iArr2[0]);
                                    }
                                    int unused = HwEidPlugin.this.HWEidGetUnSecImageZipRetValue = HWEidGetUnSecImageZipRet;
                                }
                            };
                            iEid.HWEidGetUnsecImageZip(container, factor, r1);
                            freeBinderMemory();
                            ret = hasExecption ? -1002 : this.HWEidGetUnSecImageZipRetValue;
                        }
                    }
                    Log.e(TAG, "eidGetUnsecImageZip new container or new factor error, ret : " + -1001);
                    return -1001;
                } catch (RemoteException e2) {
                    Log.e(TAG, "eidGetUnSecImageZip from mEid hidl failed.");
                    hasExecption = true;
                    if (hasExecption) {
                    }
                    if (HW_DEBUG) {
                    }
                    return ret;
                }
            } catch (RemoteException e3) {
                int i = encryption_method;
                int i2 = certificate_len;
                byte[] bArr3 = certificate;
                Log.e(TAG, "eidGetUnSecImageZip from mEid hidl failed.");
                hasExecption = true;
                if (hasExecption) {
                }
                if (HW_DEBUG) {
                }
                return ret;
            }
        } else {
            int i3 = encryption_method;
            int i4 = certificate_len;
            byte[] bArr4 = certificate;
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when HWEidUnGetSecImageZip");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "eidGetSecImageZip from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public String eid_get_version() {
        checkPermission(EID_MANAGER_PERMISSION);
        String ret = null;
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                this.mEid.HWEidGetVersion(new IEid.HWEidGetVersionCallback() {
                    public void onValues(int HWEidGetVersionRet, HIDL_VERSION_S output) {
                        if (HWEidGetVersionRet == 0) {
                            HwEidPlugin hwEidPlugin = HwEidPlugin.this;
                            String unused = hwEidPlugin.eidGetVersionRet = output.main + "." + output.sub;
                        }
                    }
                });
                ret = this.eidGetVersionRet;
                if (HW_DEBUG) {
                    Log.d(TAG, "HWEidGetVersion from mEid hidl ret : " + ret);
                }
                freeBinderMemory();
            } catch (RemoteException e2) {
                Log.e(TAG, "HWEidGetVersion from mEid hidl failed.");
            }
        } else {
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when HWEidGetVersion");
        }
        return ret;
    }

    public int ctid_set_sec_mode() {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                ret = this.mEid.HWCtidSetSecMode();
                if (HW_DEBUG) {
                    Log.d(TAG, "Ctid SetSecMode ret : " + ret);
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "HW CtidSetSecMode from hidl failed.");
                Log.e(TAG, "RemoteException e:" + e2);
                this.mEid = null;
                return -1002;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "eid hidl deamon is not ready when eid init");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid SetSecMode from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public int ctid_get_sec_image() {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        if (this.mEid != null) {
            try {
                ret = this.mEid.HWCtidGetImage();
                if (HW_DEBUG) {
                    Log.d(TAG, "Ctid getSecImage ret : " + ret);
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "HW getSecImage from hidl failed.");
                Log.e(TAG, "RemoteException e:" + e2);
                this.mEid = null;
                return -1002;
            }
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "eid hidl deamon is not ready when eid init");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid getSecImage from mEid hidl ret : " + ret);
        }
        return ret;
    }

    public int ctid_get_service_verion_info(byte[] uuid, int uuid_len, String ta_path, int[] cmd_list, int cmd_count) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        boolean hasExecption = false;
        if (this.mEid == null) {
            try {
                this.mEid = vendor.huawei.hardware.eid.V1_1.IEid.getService(EID_HIDL_SERVICE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Try get mEid hidl deamon servcie failed");
            }
        }
        int i = 1;
        if (this.mEid != null) {
            try {
                INIT_CTID_TA_MSG_S input = new INIT_CTID_TA_MSG_S();
                input.uuid_len = minArrayLen(16, uuid_len);
                input.ta_path_len = minArrayLen(511, ta_path.length());
                input.cmdlist_cnt = cmd_count;
                if (HW_DEBUG) {
                    Log.d(TAG, ta_path);
                }
                if (HW_DEBUG) {
                    Log.d(TAG, Arrays.toString(cmd_list));
                }
                if (uuid != null) {
                    System.arraycopy(uuid, 0, input.uuid, 0, input.uuid_len);
                }
                System.arraycopy(ta_path.getBytes(), 0, input.ta_path, 0, input.ta_path_len);
                if (cmd_list != null) {
                    System.arraycopy(cmd_list, 0, input.cmd_list, 0, minArrayLen(cmd_list.length, input.cmdlist_cnt));
                }
                if (HW_DEBUG) {
                    Log.d(TAG, "ctid init param uuid_len : " + uuid_len + " ta path len:" + ta_path.length() + "cmd cnt:" + cmd_count);
                }
                this.mEid.HWCtidInitTa(input);
            } catch (RemoteException e2) {
                hasExecption = true;
                Log.e(TAG, "eid get unsec image from mEid hidl failed.");
                Log.e(TAG, "RemoteException e:" + e2);
                this.mEid = null;
            }
            if (hasExecption) {
                i = -1002;
            }
            ret = i;
        } else {
            ret = -1000;
            this.mHwEidHidlHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "mEid hidl deamon is not ready when eid get unsec image");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "ctid get version info from mEid hidl ret : " + ret);
        }
        return ret;
    }
}

package com.huawei.server.security.eidservice;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.eid.HwEidHidlAdapter;
import com.huawei.security.ControlWordEntity;
import com.huawei.security.CoordinateEntity;
import com.huawei.security.EidInfoEntity;
import com.huawei.security.EidInfoExtendEntity;
import com.huawei.security.IHwEidPlugin;
import com.huawei.server.security.core.IHwSecurityPlugin;
import java.util.List;
import java.util.Optional;

public class HwEidPlugin extends IHwEidPlugin.Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.eidservice.HwEidPlugin.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            if (HwEidPlugin.HW_DEBUG) {
                Log.d(HwEidPlugin.TAG, "create HwEidPlugin");
            }
            return new HwEidPlugin(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return HwEidPlugin.EID_MANAGER_PERMISSION;
        }
    };
    private static final int EID_INFO_EXTEND_LIST_NUMS = 2;
    private static final int EID_INFO_LIST_NUMS = 3;
    private static final String EID_MANAGER_PERMISSION = "huawei.android.permission.EID_PERMISSION";
    private static final boolean HW_DEBUG = SystemPropertiesEx.get("ro.secure", "1").equals("0");
    private static final int RET_DEFAULT_ERROR_VALUE = -1001;
    private static final int RET_EID_HIDL_DEAMON_IS_NOT_READY = -1000;
    private static final int RET_EXCEPTION_WHEN_EID_CALL = -1002;
    private static final String TAG = "HwEidPlugin";
    private static HwEidHidlAdapter sEidInstance;
    private Context mContext;

    private HwEidPlugin(Context context) {
        this.mContext = null;
        this.mContext = context;
    }

    private Optional<HwEidHidlAdapter> getEidHidlAdapter() {
        HwEidHidlAdapter hwEidHidlAdapter = sEidInstance;
        if (hwEidHidlAdapter != null) {
            return Optional.of(hwEidHidlAdapter);
        }
        sEidInstance = new HwEidHidlAdapter();
        HwEidHidlAdapter hwEidHidlAdapter2 = sEidInstance;
        if (hwEidHidlAdapter2 != null) {
            return Optional.of(hwEidHidlAdapter2);
        }
        Log.e(TAG, "Error, the obtained getEidHidlAdapter is null");
        return Optional.empty();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.huawei.server.security.eidservice.HwEidPlugin */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        checkPermission(EID_MANAGER_PERMISSION);
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        if (HW_DEBUG) {
            Log.d(TAG, "HwEidPlugin start");
        }
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        sEidInstance.stopHwEidHidlAdapter();
        if (HW_DEBUG) {
            Log.d(TAG, "Close HwEidPlugin");
        }
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingPermission(permission, "Must have " + permission + " permission");
    }

    public int eidInit(EidInfoEntity aidInfo, EidInfoEntity eidAidInfo, EidInfoEntity logoInfo) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (aidInfo == null || eidAidInfo == null || logoInfo == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidInitHidlAdapter(aidInfo, eidAidInfo, logoInfo);
            if (HW_DEBUG) {
                Log.d(TAG, "Eid init ret: " + ret);
            }
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid init");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid init from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidFinish() {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidFinishHidlAdapter();
            if (HW_DEBUG) {
                Log.d(TAG, "Eid finish ta ret: " + ret);
            }
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid finish");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid finish from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetImage(ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (controlWord == null || certificateInfo == null || imageInfo == null || deSkeyInfo == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetImageHidlAdapter(controlWord, certificateInfo, imageInfo, deSkeyInfo);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid get image");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get image from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetUnsecImage(EidInfoEntity srcInfoEntity, ControlWordEntity controlWord, EidInfoEntity certificateInfo, EidInfoExtendEntity imageInfo, EidInfoExtendEntity deSkeyInfo) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (srcInfoEntity == null || controlWord == null || certificateInfo == null || imageInfo == null || deSkeyInfo == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetUnsecImageHidlAdapter(srcInfoEntity, controlWord, certificateInfo, imageInfo, deSkeyInfo);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid get unsec image");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get unsec image from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetCertificateRequestMessage(byte[] requestMessage, int[] messageLen) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (requestMessage == null || messageLen == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetCertificateRequestMessageHidlAdapter(requestMessage, messageLen);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid get certificate request message");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get certificate request message from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidSignInfo(ControlWordEntity controlWord, EidInfoEntity infoEntity, EidInfoExtendEntity signEntity) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (controlWord == null || infoEntity == null || signEntity == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        Log.d(TAG, "Info: eid_sign_begin");
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidSignInfoHidlAdapter(controlWord, infoEntity, signEntity);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid sign info");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid sign info from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetIdentityInformation(byte[] identityInfo, int[] identityInfoLen) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (identityInfo == null || identityInfoLen == null) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetIdentityInformationHidlAdapter(identityInfo, identityInfoLen);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid get identity information");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get identity information from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetFaceIsChanged(int cmdId) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (cmdId < 0) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetFaceIsChangedHidlAdapter(cmdId);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when eid get face is changed");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Eid get face is changed from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetSecImageZip(CoordinateEntity coordinateEntity, ControlWordEntity controlWord, List<EidInfoEntity> eidInfoEntityList, List<EidInfoExtendEntity> eidInfoExtendEntityList) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (coordinateEntity == null || controlWord == null || eidInfoExtendEntityList == null || eidInfoEntityList == null || eidInfoEntityList.size() != 3 || eidInfoExtendEntityList.size() != 2) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetSecImageZipHidlAdapter(coordinateEntity, controlWord, eidInfoEntityList, eidInfoExtendEntityList);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "EidGetSecImageZip from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int eidGetUnsecImageZip(ControlWordEntity controlWord, List<EidInfoEntity> eidInfoEntityList, List<EidInfoExtendEntity> eidInfoExtendEntityList) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (controlWord == null || eidInfoEntityList == null || eidInfoExtendEntityList == null || eidInfoEntityList.size() != 3 || eidInfoExtendEntityList.size() != 2) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetUnsecImageZipHidlAdapter(controlWord, eidInfoEntityList, eidInfoExtendEntityList);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when HWEidGetUnsecImageZip");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "EidGetUnSecImageZip from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public String eidGetVersion() {
        checkPermission(EID_MANAGER_PERMISSION);
        String ret = null;
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.eidGetVersionHidlAdapter();
            if (HW_DEBUG) {
                Log.d(TAG, "HWEidGetVersion from mEid hidl ret:" + ret);
            }
        } else {
            Log.e(TAG, "Eid hidl deamon is not ready when HWEidGetVersion");
        }
        return ret;
    }

    public int ctidSetSecMode() {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.ctidSetSecModeHidlAdapter();
            if (HW_DEBUG) {
                Log.d(TAG, "Ctid SetSecMode ret: " + ret);
            }
        } else {
            Log.e(TAG, "Eid hidl deamon is not ready when eid init");
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid SetSecMode from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int ctidGetSecImage() {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.ctidGetSecImageHidlAdapter();
            if (HW_DEBUG) {
                Log.d(TAG, "Ctid getSecImage ret: " + ret);
            }
        } else {
            Log.e(TAG, "Ctid hidl deamon is not ready when eid init");
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid getSecImage from mEid hidl ret: " + ret);
        }
        return ret;
    }

    public int ctidGetServiceVerionInfo(byte[] uuid, int uuidLen, String taPath, int[] cmdList, int cmdCount) {
        int ret;
        checkPermission(EID_MANAGER_PERMISSION);
        if (getEidHidlAdapter().isPresent()) {
            ret = sEidInstance.ctidGetServiceVerionInfoHidlAdapter(uuid, uuidLen, taPath, cmdList, cmdCount);
        } else {
            ret = RET_EID_HIDL_DEAMON_IS_NOT_READY;
            Log.e(TAG, "Eid hidl deamon is not ready when ctid get service version");
        }
        if (HW_DEBUG) {
            Log.d(TAG, "Ctid get version info from mEid hidl ret: " + ret);
        }
        return ret;
    }
}

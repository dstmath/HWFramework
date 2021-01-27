package com.huawei.server.security.permissionmanager.recommendpermission;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.util.SlogEx;
import com.huawei.permission.cloud.RecommendPermissionInfo;
import com.huawei.server.security.permissionmanager.RecommendPermDbAdapter;
import com.huawei.server.security.permissionmanager.struct.HwPermissionInfo;
import com.huawei.server.security.permissionmanager.util.PermConst;
import com.huawei.server.security.permissionmanager.util.PermissionClass;
import com.huawei.server.security.permissionmanager.util.PermissionType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class GetRecommendPermissionsCaller {
    private static final int DEFAULT_RES_ID = 0;
    private static final int DEFAULT_SIZE = 16;
    private static final long DIRTY_PERM_TYPE = 258234908672L;
    private static final int ILLEGAL_VALUE = -1;
    private static final Object LOCK = new Object();
    private static final int PERMISSION_CFG_INDEX = 1;
    private static final int PERMISSION_CODE_INDEX = 0;
    private static final int PERMISSION_TYPE_ALLOWED = 0;
    private static final int PERMISSION_TYPE_BLOCKED = 2;
    private static final int PERMISSION_TYPE_FOREGROUND_ALLOWED = 3;
    private static final int PERMISSION_TYPE_REMIND = 1;
    private static final int PERMISSION_UNTRUST = 0;
    private static final String PERMISSION_VALID_KEY = "permissionValidKey";
    private static final int PERMISSION_VALID_VALUE = 9;
    private static final String PURE_ANDROID_KEY_GREEN = "GREEN";
    private static final String PURE_ANDROID_KEY_UNKNOWN = "UNKNOWN";
    private static final String PURE_ANDROID_LABEL_KEY = "pureAndroidLabel";
    private static final String TAG = "GetRecommendPermissionsCaller";
    private static final String TARGET_SDK = "targetSdk";
    private static volatile GetRecommendPermissionsCaller sInstance;
    private Context mContext;
    private RecommendPermDbAdapter mRecommendPermDbAdapter;

    private GetRecommendPermissionsCaller(Context context) {
        this.mContext = context.getApplicationContext();
        this.mRecommendPermDbAdapter = RecommendPermDbAdapter.getInstance(context);
    }

    public static GetRecommendPermissionsCaller getInstance(Context context) {
        if (context != null) {
            if (sInstance == null) {
                synchronized (LOCK) {
                    if (sInstance == null) {
                        sInstance = new GetRecommendPermissionsCaller(context);
                    }
                }
            }
            return sInstance;
        }
        throw new IllegalArgumentException("Input value is null!");
    }

    public Bundle getRecommendPermissions(String pkgName, Bundle params) {
        Bundle result = new Bundle();
        if (params == null) {
            SlogEx.e(TAG, "getRecommendPermissions args is null");
            return result;
        }
        int targetSdk = params.getInt(TARGET_SDK, -1);
        String pureAndroidLabel = params.getString(PURE_ANDROID_LABEL_KEY, PURE_ANDROID_KEY_UNKNOWN);
        SlogEx.i(TAG, "Recommend Perm:" + pkgName + ", sdk:" + targetSdk + ", pure:" + pureAndroidLabel);
        if (TextUtils.isEmpty(pkgName) || targetSdk < 0) {
            return result;
        }
        long[] recommendInfoSets = this.mRecommendPermDbAdapter.getRecommendPermValue(pkgName);
        if (recommendInfoSets == null) {
            SlogEx.e(TAG, "recommendInfo is null");
            return result;
        }
        ArrayList<RecommendPermissionInfo> permissionList = getPermissionList(recommendInfoSets, targetSdk);
        result.putParcelableArrayList("recommendPermissionList", permissionList);
        if (permissionList.size() > 0) {
            result.putInt(PERMISSION_VALID_KEY, PERMISSION_VALID_VALUE);
        }
        result.putString(PermConst.PACKAGE_NAME, pkgName);
        result.putInt("permissionTrustKey", 0);
        SlogEx.i(TAG, "Send recommend permissions info:" + result);
        return result;
    }

    private ArrayList<RecommendPermissionInfo> getPermissionList(long[] recommendInfo, int targetSdk) {
        ArrayList<RecommendPermissionInfo> resultList = new ArrayList<>(16);
        Iterator<HwPermissionInfo> it = RecommendPermUtil.getRecommendPermissions().iterator();
        while (it.hasNext()) {
            RecommendPermissionInfo permInfo = getRecommendPermInfo(it.next(), recommendInfo, targetSdk);
            if (permInfo != null) {
                permInfo.setDescriptionRes(0);
                resultList.add(permInfo);
            }
        }
        return resultList;
    }

    private RecommendPermissionInfo getRecommendPermInfo(HwPermissionInfo info, long[] recommendInfo, int targetSdk) {
        RecommendPermissionInfo permissionInfo = getRecommendPermInfoInner(info, recommendInfo, targetSdk);
        if (permissionInfo == null) {
            return null;
        }
        permissionInfo.setType((info.getPermissionCode() & PermissionClass.DANGEROUS_PERMISSION) != 0 ? 1 : 0);
        if (info.getPermissionCode() == 16) {
            permissionInfo.setFlags(1 | permissionInfo.getFlags());
        }
        if (info.getPermissionCode() == PermissionType.SEND_MMS || info.getPermissionCode() == PermissionType.CALL_FORWARD) {
            permissionInfo.setFlags(permissionInfo.getFlags() | 2);
        }
        permissionInfo.setResPackageName(PermConst.SYSTEM_MANAGER_PACKAGE_NAME);
        return permissionInfo;
    }

    private RecommendPermissionInfo getRecommendPermInfoInner(HwPermissionInfo info, long[] recommendInfo, int targetSdk) {
        if (info.getPermissionCode() == 8) {
            return getLocationRecommendInfo(info, recommendInfo, targetSdk);
        }
        RecommendPermissionInfo permissionInfo = new RecommendPermissionInfo();
        permissionInfo.setRequestPermissions(info.getPermissionStr());
        permissionInfo.setKey(RecommendPermUtil.getHwPermissionKeyMap().get(info.getPermissionCode()));
        int status = 0;
        long permissionCode = recommendInfo[0];
        long permissionCfg = recommendInfo[1];
        SlogEx.i(TAG, "permissionCode: " + permissionCode + ", info.getPermissionCode" + info.getPermissionCode() + ", permissionCfg:" + permissionCfg);
        if ((info.getPermissionCode() & permissionCode) != 0) {
            if ((info.getPermissionCode() & permissionCfg) != 0) {
                status = 2;
            }
            SlogEx.i(TAG, "permissionCode name: " + Arrays.toString(info.getPermissionStr()) + ", status:" + status);
            permissionInfo.setStatus(status);
        } else if ((info.getPermissionCode() & PermissionClass.DANGEROUS_PERMISSION) != 0) {
            int status2 = 1;
            if (targetSdk <= 22) {
                status2 = 0;
                permissionInfo.setStatus(0);
            } else {
                permissionInfo.setStatus(1);
            }
            SlogEx.i(TAG, "zero permissionCode name: " + Arrays.toString(info.getPermissionStr()) + ", status:" + status2);
        } else if (PermissionClass.isClassEType(info.getPermissionCode())) {
            SlogEx.e(TAG, "Permission: " + info.getPermissionCode() + " is E class!");
            permissionInfo.setStatus(2);
        } else {
            SlogEx.e(TAG, "Permission: " + info.getPermissionCode() + " has no default policy!");
            return null;
        }
        return permissionInfo;
    }

    private RecommendPermissionInfo getLocationRecommendInfo(HwPermissionInfo info, long[] recommendInfo, int targetSdk) {
        RecommendPermissionInfo permissionInfo = new RecommendPermissionInfo();
        permissionInfo.setRequestPermissions(info.getPermissionStr());
        permissionInfo.setKey(RecommendPermUtil.getHwPermissionKeyMap().get(info.getPermissionCode()));
        int status = 0;
        long permissionCode = recommendInfo[0];
        long permissionCfg = recommendInfo[1];
        if ((permissionCode & PermissionType.LOCATION_BACKGROUND) == 0 || (PermissionType.LOCATION_BACKGROUND & permissionCfg) != 0) {
            if ((info.getPermissionCode() & permissionCode) != 0) {
                if ((info.getPermissionCode() & permissionCfg) != 0) {
                    status = 2;
                }
                SlogEx.i(TAG, "Location permission status:" + status);
                permissionInfo.setStatus(status);
            } else {
                int status2 = 1;
                if (targetSdk <= 22) {
                    status2 = 0;
                }
                permissionInfo.setStatus(status2);
                SlogEx.i(TAG, "Location permission status:" + status2);
            }
            SlogEx.i(TAG, "Location permissionCode: " + permissionCode + ", permissionCfg:" + permissionCfg);
            return permissionInfo;
        }
        SlogEx.i(TAG, "location foreground allowed");
        permissionInfo.setStatus(3);
        return permissionInfo;
    }
}

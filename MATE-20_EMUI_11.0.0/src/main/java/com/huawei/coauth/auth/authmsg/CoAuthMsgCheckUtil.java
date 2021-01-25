package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.CoAuthDevice;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import java.util.List;

/* access modifiers changed from: package-private */
public class CoAuthMsgCheckUtil {
    private CoAuthMsgCheckUtil() {
    }

    protected static boolean checkCoAuthPairGroupEntity(CoAuthPairGroupEntity coAuthPairGroupEntity) {
        if (coAuthPairGroupEntity == null || coAuthPairGroupEntity.getSrcDid() == null || coAuthPairGroupEntity.getSrcDid().isEmpty() || coAuthPairGroupEntity.getDstDid() == null || coAuthPairGroupEntity.getDstDid().isEmpty()) {
            return true;
        }
        return false;
    }

    protected static boolean checkQueryCoAuthMethodCheck(CoAuthContext coAuthContext) {
        if (coAuthContext == null || coAuthContext.getCoAuthGroup() == null || coAuthContext.getCoAuthGroup().getGroupId() == null || coAuthContext.getCoAuthGroup().getGroupId().isEmpty()) {
            return true;
        }
        return false;
    }

    protected static boolean checkCoAuthContext(CoAuthContext coAuthContext) {
        if (coAuthContext == null || coAuthContext.getCoAuthGroup() == null || coAuthContext.getCoAuthGroup().getGroupId().isEmpty() || coAuthContext.getVerifyDeviceId() == null || coAuthContext.getVerifyDeviceId().isEmpty() || coAuthContext.getAuthType() == null || coAuthContext.getSensorDeviceId() == null || coAuthContext.getSensorDeviceId().isEmpty()) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x000e  */
    protected static boolean checkInitCoAuthIdmGroup(List<CoAuthDevice> devList) {
        if (devList == null) {
            return true;
        }
        for (CoAuthDevice authDevice : devList) {
            if (authDevice.getDeviceId() == null || authDevice.getDeviceId().isEmpty() || authDevice.getIp() == null || authDevice.getIp().isEmpty()) {
                return true;
            }
            while (r1.hasNext()) {
            }
        }
        return false;
    }

    protected static boolean checkCoAuthHeaderEntity(CoAuthHeaderEntity coAuthHeaderEntity) {
        if (coAuthHeaderEntity == null || coAuthHeaderEntity.getDstDid() == null || coAuthHeaderEntity.getDstDid().isEmpty() || coAuthHeaderEntity.getSrcDid() == null || coAuthHeaderEntity.getSrcDid().isEmpty()) {
            return true;
        }
        return false;
    }

    protected static boolean checkGetProperty(CoAuthContext coAuthContext, byte[] key) {
        if (coAuthContext == null || coAuthContext.getCoAuthGroup() == null || coAuthContext.getCoAuthGroup().getGroupId().isEmpty()) {
            return true;
        }
        if ((coAuthContext.getSensorDeviceId() == null && coAuthContext.getVerifyDeviceId() == null) || key == null) {
            return true;
        }
        return false;
    }
}

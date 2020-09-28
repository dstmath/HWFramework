package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;

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

    protected static boolean checkCoAuthContext(CoAuthContext coAuthContext) {
        if (coAuthContext == null || coAuthContext.getCoAuthGroup() == null || coAuthContext.getCoAuthGroup().getGroupId().isEmpty() || coAuthContext.getVerifyDeviceId() == null || coAuthContext.getVerifyDeviceId().isEmpty() || coAuthContext.getAuthType() == null || coAuthContext.getSensorDeviceId() == null || coAuthContext.getSensorDeviceId().isEmpty()) {
            return true;
        }
        return false;
    }

    protected static boolean checkCoAuthHeaderEntity(CoAuthHeaderEntity coAuthHeaderEntity) {
        if (coAuthHeaderEntity == null || coAuthHeaderEntity.getDstDid() == null || coAuthHeaderEntity.getDstDid().isEmpty() || coAuthHeaderEntity.getSrcDid() == null || coAuthHeaderEntity.getSrcDid().isEmpty()) {
            return true;
        }
        return false;
    }
}

package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import com.huawei.coauth.auth.authmsg.CoAuthOperationType;
import java.util.Map;

class CoAuthResponseProcesser {
    CoAuthResponseProcesser() {
    }

    /* access modifiers changed from: package-private */
    public void responseCreateCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuthHeaderEntity coAuthHeaderEntity, CoAuth.ICreateCallback createCallback, Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap) {
        if (checkCoAuthResponseEntity(coAuthResponseEntity)) {
            Log.e(CoAuthUtil.TAG, "responseCreateCallback checkCoAuthResponseEntity false");
        } else if (checkCoAuthHeaderEntity(coAuthHeaderEntity)) {
            Log.e(CoAuthUtil.TAG, "responseCreateCallback checkCoAuthHeaderEntity false");
        } else if (createCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseCreateCallback createCallback false");
        } else if (checkCoAuthPairGroupMap(coAuthPairGroupMap)) {
            Log.e(CoAuthUtil.TAG, "responseCreateCallback checkCoAuthPairGroupMap false");
        } else if (coAuthResponseEntity.getCoAuthOperationType().getValue() == CoAuthOperationType.CREATE_CO_AUTH_PAIR_GROUP.getValue()) {
            createCallback(coAuthResponseEntity, createCallback, coAuthHeaderEntity, coAuthPairGroupMap);
        }
    }

    /* access modifiers changed from: package-private */
    public void responseDestroyCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuth.IDestroyCallback destroyCallback, Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap) {
        if (checkCoAuthResponseEntity(coAuthResponseEntity)) {
            Log.e(CoAuthUtil.TAG, "responseDestroyCallback checkCoAuthResponseEntity false");
        } else if (destroyCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseDestroyCallback destroyCallback false");
        } else if (checkCoAuthPairGroupMap(coAuthPairGroupMap)) {
            Log.e(CoAuthUtil.TAG, "responseDestroyCallback checkCoAuthPairGroupMap false");
        } else if (coAuthResponseEntity.getCoAuthOperationType().getValue() == CoAuthOperationType.DESTROY_CO_AUTH_PAIR_GROUP.getValue()) {
            destroyCallback(coAuthResponseEntity, coAuthPairGroupMap, destroyCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public void responseCoAuthCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuthContext coAuthContext, CoAuth.ICoAuthCallback coAuthCallback) {
        if (checkCoAuthResponseEntity(coAuthResponseEntity)) {
            Log.e(CoAuthUtil.TAG, "responseCoAuthCallback checkCoAuthResponseEntity false");
        } else if (checkCoAuthContext(coAuthContext)) {
            Log.e(CoAuthUtil.TAG, "responseCoAuthCallback checkCoAuthContext false");
        } else if (coAuthCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseCoAuthCallback coAuthCallback false");
        } else {
            CoAuthOperationType coAuthOperationType = coAuthResponseEntity.getCoAuthOperationType();
            if (coAuthOperationType.getValue() == CoAuthOperationType.CO_AUTH.getValue()) {
                coAuthCallback(coAuthResponseEntity, coAuthContext, coAuthCallback);
            } else if (coAuthOperationType.getValue() == CoAuthOperationType.CO_AUTH_START_RESPONSE.getValue()) {
                coAuthStartCallback(coAuthResponseEntity, coAuthContext, coAuthCallback);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void responseCancelCoAuthCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuthContext coAuthContext, CoAuth.ICancelCoAuthCallback cancelCoAuthCallback) {
        if (checkCoAuthResponseEntity(coAuthResponseEntity)) {
            Log.e(CoAuthUtil.TAG, "responseCancelCoAuthCallback checkCoAuthResponseEntity false");
        } else if (checkCoAuthContext(coAuthContext)) {
            Log.e(CoAuthUtil.TAG, "responseCancelCoAuthCallback checkCoAuthContext false");
        } else if (cancelCoAuthCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseCancelCoAuthCallback cancelCoAuthCallback false");
        } else if (coAuthResponseEntity.getCoAuthOperationType().getValue() == CoAuthOperationType.CANCEL_CO_AUTH.getValue()) {
            cancelCoAuthFinish(coAuthResponseEntity, coAuthContext, cancelCoAuthCallback);
        }
    }

    private void cancelCoAuthFinish(CoAuthResponseEntity coAuthResponseEntity, CoAuthContext coAuthContext, CoAuth.ICancelCoAuthCallback cancelCoAuthCallback) {
        String str = CoAuthUtil.TAG;
        Log.i(str, "callback with response message, coAuthResponseEntity.getResultCode() = " + coAuthResponseEntity.getResultCode());
        String str2 = CoAuthUtil.TAG;
        Log.d(str2, "callback with response message, cancelCoAuth coAuthContext = " + coAuthContext);
        cancelCoAuthCallback.onCancelCoAuthFinish(coAuthResponseEntity.getResultCode());
    }

    private void coAuthCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuthContext coAuthContext, CoAuth.ICoAuthCallback coAuthCallback) {
        String str = CoAuthUtil.TAG;
        Log.i(str, "callback with response message, CoAuthCallback.onCoAuthFinish = " + coAuthResponseEntity.getResultCode());
        String str2 = CoAuthUtil.TAG;
        Log.d(str2, "callback with response message, coAuth coAuthContext = " + coAuthContext);
        coAuthCallback.onCoAuthFinish(coAuthResponseEntity.getResultCode(), coAuthContext);
    }

    private void coAuthStartCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuthContext coAuthContext, CoAuth.ICoAuthCallback coAuthCallback) {
        String str = CoAuthUtil.TAG;
        Log.i(str, "callback with response message, CoAuthCallback.onCoAuthStart = " + coAuthResponseEntity.getResultCode());
        String str2 = CoAuthUtil.TAG;
        Log.d(str2, "callback with response message, coAuth coAuthContext = " + coAuthContext);
        coAuthCallback.onCoAuthStart(coAuthContext);
    }

    private void destroyCallback(CoAuthResponseEntity coAuthResponseEntity, Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap, CoAuth.IDestroyCallback destroyCallback) {
        if (coAuthResponseEntity.getResultCode() == 0) {
            Log.i(CoAuthUtil.TAG, "callback with response message, DestroyCallback.onSuccess.");
            destroyCallback.onSuccess();
            clearCoAuthPairGroupMap(coAuthResponseEntity, coAuthPairGroupMap);
            return;
        }
        String str = CoAuthUtil.TAG;
        Log.e(str, "callback with response message, DestroyCallback.onFailed = " + coAuthResponseEntity.getResultCode());
        destroyCallback.onFailed(coAuthResponseEntity.getResultCode());
    }

    private void createCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuth.ICreateCallback createCallback, CoAuthHeaderEntity coAuthHeaderEntity, Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap) {
        if (coAuthResponseEntity.getResultCode() == 0) {
            CoAuthGroup coAuthGroup = new CoAuthGroup(coAuthResponseEntity.getGroupId());
            String str = CoAuthUtil.TAG;
            Log.i(str, "callback with response message, CreateCallback.onSuccess = " + coAuthGroup.getGroupId());
            coAuthPairGroupMap.put(coAuthGroup.getGroupId(), new CoAuthPairGroupEntity.Builder().setGroupId(coAuthResponseEntity.getGroupId()).setVersion(coAuthHeaderEntity.getVersion()).setSrcDid(coAuthHeaderEntity.getSrcDid()).setSrcModule(coAuthHeaderEntity.getSrcModule()).setDstDid(coAuthHeaderEntity.getDstDid()).setDstModule(coAuthHeaderEntity.getDstModule()).build());
            createCallback.onSuccess(coAuthGroup);
            return;
        }
        String str2 = CoAuthUtil.TAG;
        Log.e(str2, "callback with response message, CreateCallback.onFailed = " + coAuthResponseEntity.getResultCode());
        createCallback.onFailed(coAuthResponseEntity.getResultCode());
    }

    private void clearCoAuthPairGroupMap(CoAuthResponseEntity coAuthResponseEntity, Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap) {
        coAuthPairGroupMap.remove(CoAuthUtil.bytesToHexString(coAuthResponseEntity.getGroupId()).get());
    }

    private boolean checkCoAuthPairGroupMap(Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap) {
        if (coAuthPairGroupMap == null) {
            return true;
        }
        return false;
    }

    private boolean checkCoAuthContext(CoAuthContext coAuthContext) {
        if (coAuthContext == null) {
            return true;
        }
        return false;
    }

    private boolean checkCoAuthHeaderEntity(CoAuthHeaderEntity coAuthHeaderEntity) {
        if (coAuthHeaderEntity == null || coAuthHeaderEntity.getSrcDid() == null || coAuthHeaderEntity.getSrcDid().isEmpty() || coAuthHeaderEntity.getDstDid() == null || coAuthHeaderEntity.getDstDid().isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean checkCoAuthResponseEntity(CoAuthResponseEntity coAuthResponseEntity) {
        if (coAuthResponseEntity == null || coAuthResponseEntity.getCoAuthOperationType() == null) {
            return true;
        }
        return false;
    }
}

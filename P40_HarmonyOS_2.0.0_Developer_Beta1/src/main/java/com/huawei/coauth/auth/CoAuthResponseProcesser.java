package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthIdmGroupEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.auth.authentity.CoAuthPropertyEntity;
import com.huawei.coauth.auth.authentity.CoAuthQueryMethodEntity;
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
    public void responseDestroyCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuth.IDestroyCallback destroyCallback) {
        if (checkCoAuthResponseEntity(coAuthResponseEntity)) {
            Log.e(CoAuthUtil.TAG, "responseDestroyCallback checkCoAuthResponseEntity false");
        } else if (destroyCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseDestroyCallback destroyCallback false");
        } else if (coAuthResponseEntity.getCoAuthOperationType().getValue() == CoAuthOperationType.DESTROY_CO_AUTH_PAIR_GROUP.getValue()) {
            destroyCallback(coAuthResponseEntity, destroyCallback);
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
    public void responseQueryCallback(CoAuthQueryMethodEntity entity, CoAuth.IQueryAuthCallback queryAuthCallback) {
        if (entity == null || entity.getCoAuthOperationType() == null) {
            Log.e(CoAuthUtil.TAG, "responseQueryCallback checkCoAuthResponseEntity false");
        } else if (queryAuthCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseCoAuthCallback coAuthCallback false");
        } else {
            coAuthQueryMethodCallback(entity, queryAuthCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public void responseGetPropertyCallback(CoAuthPropertyEntity entity, CoAuth.IGetPropCallback getPropCallback) {
        if (entity == null || entity.getValue().length == 0) {
            Log.e(CoAuthUtil.TAG, "responseGetPropertyCallback coAuthPropertyEntity false");
        } else if (getPropCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseGetPropertyCallback coAuthCallback false");
        } else {
            coAuthGetPropertyCallback(entity, getPropCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public void responseSetPropertyCallback(CoAuthPropertyEntity entity, CoAuth.ISetPropCallback setPropCallback) {
        if (entity == null) {
            Log.e(CoAuthUtil.TAG, "responseSetPropertyCallback coAuthPropertyEntity false");
        } else if (setPropCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseSetPropertyCallback coAuthCallback false");
        } else {
            coAuthSetPropertyCallback(entity, setPropCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public void responseInitIdmGroupCallback(CoAuthIdmGroupEntity entity, CoAuth.IInitCallback initCallback) {
        if (entity == null) {
            Log.e(CoAuthUtil.TAG, "responseInitIdmGroupCallback CoAuthIdmGroupEntity false");
        } else if (initCallback == null) {
            Log.e(CoAuthUtil.TAG, "responseInitIdmGroupCallback initCallback false");
        } else {
            coAuthInitIdmGroupCallback(entity, initCallback);
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
        if (coAuthResponseEntity.getResultCode() == CoAuthRetCode.CO_AUTH_RET_SUCCESS.getValue()) {
            coAuthContext.setCoAuthToken(coAuthResponseEntity.getCoAuthToken());
        }
        coAuthCallback.onCoAuthFinish(coAuthResponseEntity.getResultCode(), coAuthContext);
    }

    private void coAuthStartCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuthContext coAuthContext, CoAuth.ICoAuthCallback coAuthCallback) {
        String str = CoAuthUtil.TAG;
        Log.i(str, "callback with response message, CoAuthCallback.onCoAuthStart = " + coAuthResponseEntity.getResultCode());
        String str2 = CoAuthUtil.TAG;
        Log.d(str2, "callback with response message, coAuth coAuthContext = " + coAuthContext);
        coAuthCallback.onCoAuthStart(coAuthContext);
    }

    private void coAuthQueryMethodCallback(CoAuthQueryMethodEntity entity, CoAuth.IQueryAuthCallback coAuthCallback) {
        String str = CoAuthUtil.TAG;
        Log.i(str, "callback with response msg, coAuthQueryMethodCallback list size is " + entity.getContextList().size());
        coAuthCallback.onResult(entity.getContextList());
    }

    private void coAuthGetPropertyCallback(CoAuthPropertyEntity entity, CoAuth.IGetPropCallback callback) {
        callback.onGetResult(entity.getResult(), entity.getValue());
    }

    private void coAuthSetPropertyCallback(CoAuthPropertyEntity entity, CoAuth.ISetPropCallback callback) {
        callback.onGetResult(entity.getResult());
    }

    private void coAuthInitIdmGroupCallback(CoAuthIdmGroupEntity entity, CoAuth.IInitCallback callback) {
        callback.onResult(entity.getResultCode(), entity.getDevList());
    }

    private void destroyCallback(CoAuthResponseEntity coAuthResponseEntity, CoAuth.IDestroyCallback destroyCallback) {
        if (coAuthResponseEntity.getResultCode() == 0) {
            Log.i(CoAuthUtil.TAG, "callback with response message, DestroyCallback.onSuccess.");
            destroyCallback.onSuccess();
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

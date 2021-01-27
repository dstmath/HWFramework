package com.huawei.coauth.auth.authmsg;

import android.util.Log;
import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.coauth.auth.authentity.CoAuthIdmGroupEntity;
import com.huawei.coauth.auth.authentity.CoAuthPropertyEntity;
import com.huawei.coauth.auth.authentity.CoAuthQueryMethodEntity;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import com.huawei.coauth.auth.authmsg.CoAuthContextDecodeUtil;
import com.huawei.coauth.auth.authmsg.CoAuthIdmGroupDecodeUtil;
import com.huawei.coauth.tlv.TlvBase;
import com.huawei.coauth.tlv.TlvTransformException;
import com.huawei.coauth.tlv.TlvWrapper;
import java.util.Optional;
import java.util.function.Consumer;

public class CoAuthMsgDecodeMgr implements IcoAuthMsgDecodeMgr {
    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgDecodeMgr
    public Optional<CoAuthResponseEntity> responseMsg(byte[] msg) {
        return msgDecode(msg);
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgDecodeMgr
    public Optional<CoAuthQueryMethodEntity> queryMethodMsg(byte[] msg) {
        try {
            for (TlvBase tlv : TlvWrapper.deserialize(msg).getTlvList()) {
                if (tlv.getType() == CoAuthOperationType.CO_AUTH_QUERY_METHOD_RESPONSE.getValue()) {
                    return coAuthQueryMethodDecode(tlv.getValue());
                }
            }
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode response message form server error = " + ex.getErrorMsg());
        }
        return Optional.empty();
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgDecodeMgr
    public Optional<CoAuthPropertyEntity> executorPropertyMsg(byte[] msg) {
        try {
            for (TlvBase tlv : TlvWrapper.deserialize(msg).getTlvList()) {
                if (tlv.getType() == CoAuthOperationType.CO_AUTH_GET_PROPERTY_RESPONSE.getValue()) {
                    return coAuthGetSetPropertyDecode(tlv.getValue(), CoAuthOperationType.CO_AUTH_GET_PROPERTY_RESPONSE);
                }
                if (tlv.getType() == CoAuthOperationType.CO_AUTH_SET_PROPERTY_RESPONSE.getValue()) {
                    return coAuthGetSetPropertyDecode(tlv.getValue(), CoAuthOperationType.CO_AUTH_SET_PROPERTY_RESPONSE);
                }
            }
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode response message form server error = " + ex.getErrorMsg());
        }
        return Optional.empty();
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgDecodeMgr
    public Optional<CoAuthIdmGroupEntity> responseIdmGroupMsg(byte[] msg) {
        try {
            for (TlvBase tlv : TlvWrapper.deserialize(msg).getTlvList()) {
                if (tlv.getType() == CoAuthOperationType.INIT_CO_AUTH_IDM_GROUP_RESPONSE.getValue()) {
                    return coAuthResponseIdmGroupDecode(tlv.getValue(), CoAuthOperationType.INIT_CO_AUTH_IDM_GROUP_RESPONSE);
                }
            }
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode idm group response message form server error = " + ex.getErrorMsg());
        }
        return Optional.empty();
    }

    private Optional<CoAuthResponseEntity> msgDecode(byte[] msg) {
        try {
            for (TlvBase tlv : TlvWrapper.deserialize(msg).getTlvList()) {
                if (tlv.getType() == CoAuthOperationType.CREATE_CO_AUTH_PAIR_GROUP_RESPONSE.getValue()) {
                    return createCoAuthPairGroupResponseDecode(tlv.getValue());
                }
                if (tlv.getType() == CoAuthOperationType.DESTROY_CO_AUTH_PAIR_GROUP_RESPONSE.getValue()) {
                    return destroyCoAuthPairGroupResponseDecode(tlv.getValue());
                }
                if (tlv.getType() == CoAuthOperationType.CO_AUTH_RESPONSE.getValue()) {
                    return coAuthResponseDecode(tlv.getValue());
                }
                if (tlv.getType() == CoAuthOperationType.CANCEL_CO_AUTH_RESPONSE.getValue()) {
                    return cancelCoAuthResponseDecode(tlv.getValue());
                }
                if (tlv.getType() == CoAuthOperationType.CO_AUTH_START_RESPONSE.getValue()) {
                    return coAuthStartResponseDecode(tlv.getValue());
                }
            }
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode response message form server error = " + ex.getErrorMsg());
        }
        return Optional.empty();
    }

    private Optional<CoAuthResponseEntity> createCoAuthPairGroupResponseDecode(byte[] msg) {
        try {
            CoAuthResponseEntity.Builder coAuthResponseEntityBuilder = getBuilder(msg);
            coAuthResponseEntityBuilder.setCoAuthOperationType(CoAuthOperationType.CREATE_CO_AUTH_PAIR_GROUP);
            return Optional.ofNullable(coAuthResponseEntityBuilder.build());
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode createCoAuthPairGroup response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private Optional<CoAuthResponseEntity> destroyCoAuthPairGroupResponseDecode(byte[] msg) {
        try {
            CoAuthResponseEntity.Builder coAuthResponseEntityBuilder = getBuilder(msg);
            coAuthResponseEntityBuilder.setCoAuthOperationType(CoAuthOperationType.DESTROY_CO_AUTH_PAIR_GROUP);
            return Optional.ofNullable(coAuthResponseEntityBuilder.build());
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode destroyCoAuthPairGroup response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private Optional<CoAuthResponseEntity> coAuthStartResponseDecode(byte[] msg) {
        try {
            CoAuthResponseEntity.Builder coAuthResponseEntityBuilder = getBuilder(msg);
            coAuthResponseEntityBuilder.setCoAuthOperationType(CoAuthOperationType.CO_AUTH_START_RESPONSE);
            return Optional.ofNullable(coAuthResponseEntityBuilder.build());
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode coAuth response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private Optional<CoAuthResponseEntity> coAuthResponseDecode(byte[] msg) {
        try {
            CoAuthResponseEntity.Builder coAuthResponseEntityBuilder = getBuilder(msg);
            coAuthResponseEntityBuilder.setCoAuthOperationType(CoAuthOperationType.CO_AUTH);
            return Optional.ofNullable(coAuthResponseEntityBuilder.build());
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode coAuth response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private Optional<CoAuthQueryMethodEntity> coAuthQueryMethodDecode(byte[] msg) {
        try {
            CoAuthQueryMethodEntity queryMethodEntity = getQueryMethodEntity(msg);
            queryMethodEntity.setCoAuthOperationType(CoAuthOperationType.CO_AUTH_QUERY_METHOD_RESPONSE);
            return Optional.ofNullable(queryMethodEntity);
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode coAuth response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private Optional<CoAuthPropertyEntity> coAuthGetSetPropertyDecode(byte[] msg, CoAuthOperationType msgType) {
        try {
            CoAuthPropertyEntity entity = getGetSetPropertyEntity(msg);
            entity.setCoAuthOperationType(msgType);
            return Optional.of(entity);
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode coAuth response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private Optional<CoAuthIdmGroupEntity> coAuthResponseIdmGroupDecode(byte[] msg, CoAuthOperationType msgType) {
        try {
            CoAuthIdmGroupEntity entity = getCoAuthIdmGroupEntity(msg);
            entity.setCoAuthOperationType(msgType);
            return Optional.of(entity);
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode coAuth init idm group response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private Optional<CoAuthResponseEntity> cancelCoAuthResponseDecode(byte[] msg) {
        try {
            CoAuthResponseEntity.Builder coAuthResponseEntityBuilder = getBuilder(msg);
            coAuthResponseEntityBuilder.setCoAuthOperationType(CoAuthOperationType.CANCEL_CO_AUTH);
            return Optional.ofNullable(coAuthResponseEntityBuilder.build());
        } catch (TlvTransformException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "decode cancelCoAuth response message form server error = " + ex.getErrorMsg());
            return Optional.empty();
        }
    }

    private CoAuthResponseEntity.Builder getBuilder(byte[] msg) throws TlvTransformException {
        CoAuthResponseEntity.Builder coAuthResponseEntityBuilder = new CoAuthResponseEntity.Builder();
        for (TlvBase tlv : TlvWrapper.deserialize(msg).getTlvList()) {
            if (tlv.getType() == CoAuthMsgTagType.SESSION_ID.getValue()) {
                coAuthResponseEntityBuilder.setSessionId(TlvWrapper.parseLong(tlv).getAsLong());
            } else if (tlv.getType() == CoAuthMsgTagType.GROUP_ID.getValue()) {
                byte[] groupId = TlvWrapper.parseBytes(tlv).get();
                CoAuthUtil.bytesToHexString(groupId).ifPresent(new Consumer<String>() {
                    /* class com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr.AnonymousClass1 */

                    public void accept(String hexString) {
                        String str = CoAuthUtil.TAG;
                        Log.i(str, "receive message from server groupId = " + CoAuthUtil.groupIdToMask(hexString));
                    }
                });
                coAuthResponseEntityBuilder.setGroupId(groupId);
            } else if (tlv.getType() == CoAuthMsgTagType.RESULT_CODE.getValue()) {
                coAuthResponseEntityBuilder.setResultCode(TlvWrapper.parseInt(tlv).getAsInt());
            } else if (tlv.getType() == CoAuthMsgTagType.AUTH_TOKEN_SIGN.getValue()) {
                coAuthResponseEntityBuilder.setCoAuthToken(TlvWrapper.parseBytes(tlv).get());
            }
        }
        return coAuthResponseEntityBuilder;
    }

    private CoAuthQueryMethodEntity getQueryMethodEntity(byte[] msg) throws TlvTransformException {
        CoAuthQueryMethodEntity coAuthQueryMethodEntity = new CoAuthQueryMethodEntity();
        for (TlvBase tlv : TlvWrapper.deserialize(msg).getTlvList()) {
            if (tlv.getType() == CoAuthMsgTagType.SESSION_ID.getValue()) {
                Log.i(CoAuthUtil.TAG, "resolved to sessionId, not used for now");
            } else if (tlv.getType() == CoAuthMsgTagType.CO_AUTH_CONTEXT.getValue()) {
                coAuthQueryMethodEntity.getContextList().add(resolveCoAuthContext(tlv.getValue()));
            }
        }
        return coAuthQueryMethodEntity;
    }

    private CoAuthPropertyEntity getGetSetPropertyEntity(byte[] msg) throws TlvTransformException {
        CoAuthPropertyEntity entity = new CoAuthPropertyEntity();
        for (TlvBase tlv : TlvWrapper.deserialize(msg).getTlvList()) {
            if (tlv.getType() == CoAuthMsgTagType.SESSION_ID.getValue()) {
                Log.i(CoAuthUtil.TAG, "resolved to sessionId, not used for now");
            } else if (tlv.getType() == CoAuthMsgTagType.RESULT_CODE.getValue()) {
                entity.setResult(TlvWrapper.parseInt(tlv).getAsInt());
            } else if (tlv.getType() == CoAuthMsgTagType.PROPERTY_VALUE.getValue()) {
                entity.setValue(TlvWrapper.parseBytes(tlv).get());
            }
        }
        return entity;
    }

    private CoAuthIdmGroupEntity getCoAuthIdmGroupEntity(byte[] msg) throws TlvTransformException {
        TlvWrapper deviceMsg = TlvWrapper.deserialize(msg);
        CoAuthIdmGroupEntity.Builder builder = new CoAuthIdmGroupEntity.Builder();
        for (TlvBase tlv : deviceMsg.getTlvList()) {
            if (tlv.getType() == CoAuthMsgTagType.SESSION_ID.getValue()) {
                Log.i(CoAuthUtil.TAG, "resolved to sessionId, not used for now");
            } else {
                CoAuthIdmGroupDecodeUtil.Strategy strategy = CoAuthIdmGroupDecodeUtil.getStrategy(tlv.getType());
                if (strategy == null) {
                    String str = CoAuthUtil.TAG;
                    Log.e(str, "Error type " + tlv.getType());
                } else {
                    builder = strategy.setFieldToBuilder(builder, tlv);
                }
            }
        }
        return builder.build();
    }

    private CoAuthContext resolveCoAuthContext(byte[] value) throws TlvTransformException {
        TlvWrapper contextMsg = TlvWrapper.deserialize(value);
        CoAuthContext.Builder builder = new CoAuthContext.Builder();
        for (TlvBase tlv : contextMsg.getTlvList()) {
            CoAuthContextDecodeUtil.Strategy strategy = CoAuthContextDecodeUtil.getStrategy(tlv.getType());
            if (strategy == null) {
                String str = CoAuthUtil.TAG;
                Log.e(str, "Error type " + tlv.getType());
            } else {
                builder = strategy.setFieldToBuilder(builder, tlv);
            }
        }
        return builder.build();
    }
}

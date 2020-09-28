package com.huawei.coauth.auth.authmsg;

import android.util.Log;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import com.huawei.coauth.tlv.TlvBase;
import com.huawei.coauth.tlv.TlvTransformException;
import com.huawei.coauth.tlv.TlvWrapper;
import java.util.Arrays;
import java.util.Optional;

public class CoAuthMsgDecodeMgr implements IcoAuthMsgDecodeMgr {
    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgDecodeMgr
    public Optional<CoAuthResponseEntity> responseMsg(byte[] msg) {
        return msgDecode(msg);
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
                String str = CoAuthUtil.TAG;
                Log.i(str, "receive message from server groupId = " + Arrays.toString(groupId));
                coAuthResponseEntityBuilder.setGroupId(groupId);
            } else if (tlv.getType() == CoAuthMsgTagType.RESULT_CODE.getValue()) {
                coAuthResponseEntityBuilder.setResultCode(TlvWrapper.parseInt(tlv).getAsInt());
            }
        }
        return coAuthResponseEntityBuilder;
    }
}

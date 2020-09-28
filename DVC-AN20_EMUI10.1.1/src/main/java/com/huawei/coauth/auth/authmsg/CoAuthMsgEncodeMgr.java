package com.huawei.coauth.auth.authmsg;

import android.util.Log;
import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.tlv.TlvWrapper;
import java.io.UnsupportedEncodingException;

public class CoAuthMsgEncodeMgr implements IcoAuthMsgEncodeMgr {
    private byte[] encodeError = new byte[0];

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] createCoAuthPairGroupMsg(long sessionId, String packageName, String moduleName, CoAuthHeaderEntity coAuthHeaderEntity) {
        Log.d(CoAuthUtil.TAG, "encode createCoAuthPairGroup message for server");
        if (CoAuthMsgCheckUtil.checkCoAuthHeaderEntity(coAuthHeaderEntity)) {
            Log.e(CoAuthUtil.TAG, "encode createCoAuthPairGroup message for server error, invalid coAuthHeaderEntity");
            return this.encodeError;
        }
        try {
            TlvWrapper containerPayload = new TlvWrapper();
            encodeSessionId(containerPayload, sessionId);
            encodePackageName(containerPayload, packageName);
            encodeModuleName(containerPayload, moduleName);
            encodeSelfDid(containerPayload, coAuthHeaderEntity.getSrcDid());
            encodePeerDid(containerPayload, coAuthHeaderEntity.getDstDid());
            encodePeerDeviceIp(containerPayload, coAuthHeaderEntity);
            encodePeerDevicePort(containerPayload, coAuthHeaderEntity);
            TlvWrapper containerHeader = new TlvWrapper();
            encodeHeader(containerHeader, coAuthHeaderEntity);
            encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.CREATE_CO_AUTH_PAIR_GROUP);
            return containerHeader.serialize();
        } catch (UnsupportedEncodingException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "encode createCoAuthPairGroup message for server error = " + ex.getMessage());
            return this.encodeError;
        }
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] destroyCoAuthPairGroupMsg(long sessionId, byte[] groupId, CoAuthPairGroupEntity coAuthPairGroupEntity) {
        Log.d(CoAuthUtil.TAG, "encode destroyCoAuthPairGroup message for server");
        if (CoAuthMsgCheckUtil.checkCoAuthPairGroupEntity(coAuthPairGroupEntity)) {
            Log.e(CoAuthUtil.TAG, "encode destroyCoAuthPairGroup message for server error, invalid coAuthPairGroupEntity");
            return this.encodeError;
        }
        try {
            TlvWrapper containerPayload = new TlvWrapper();
            encodeSessionId(containerPayload, sessionId);
            encodeGroupId(containerPayload, groupId);
            TlvWrapper containerHeader = new TlvWrapper();
            encodeHeader(containerHeader, coAuthPairGroupEntity);
            encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.DESTROY_CO_AUTH_PAIR_GROUP);
            return containerHeader.serialize();
        } catch (UnsupportedEncodingException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "encode destroyCoAuthPairGroup message for server error = " + ex.getMessage());
            return this.encodeError;
        }
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] coAuthMsg(long sessionId, CoAuthContext coAuthContext, CoAuthPairGroupEntity coAuthPairGroupEntity, String consumerPackageName) {
        Log.d(CoAuthUtil.TAG, "encode coAuth message for server");
        if (CoAuthMsgCheckUtil.checkCoAuthPairGroupEntity(coAuthPairGroupEntity)) {
            Log.e(CoAuthUtil.TAG, "encode coAuth message for server error, invalid coAuthPairGroupEntity");
            return this.encodeError;
        } else if (CoAuthMsgCheckUtil.checkCoAuthContext(coAuthContext)) {
            Log.e(CoAuthUtil.TAG, "encode coAuth message for server error, invalid coAuthContext");
            return this.encodeError;
        } else {
            try {
                TlvWrapper containerPayload = getCoAuthMsgPayload(sessionId, coAuthContext);
                encodePackageName(containerPayload, consumerPackageName);
                TlvWrapper containerHeader = new TlvWrapper();
                encodeHeader(containerHeader, coAuthPairGroupEntity);
                encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.CO_AUTH);
                return containerHeader.serialize();
            } catch (UnsupportedEncodingException ex) {
                String str = CoAuthUtil.TAG;
                Log.e(str, "encode coAuth message for server error = " + ex.getMessage());
                return this.encodeError;
            }
        }
    }

    private TlvWrapper getCoAuthMsgPayload(long sessionId, CoAuthContext coAuthContext) throws UnsupportedEncodingException {
        TlvWrapper containerPayload = new TlvWrapper();
        encodeSessionId(containerPayload, sessionId);
        encodeGroupId(containerPayload, CoAuthUtil.hexStringToBytes(coAuthContext.getCoAuthGroup().getGroupId()).get());
        encodeChallenge(containerPayload, coAuthContext.getChallenge());
        encodeAuthType(containerPayload, coAuthContext.getAuthType().getValue());
        encodeSensorDid(containerPayload, coAuthContext.getSensorDeviceId());
        encodeVerifierDid(containerPayload, coAuthContext.getVerifyDeviceId());
        return containerPayload;
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] cancelCoAuth(long sessionId, CoAuthContext coAuthContext, CoAuthPairGroupEntity coAuthPairGroupEntity) {
        Log.d(CoAuthUtil.TAG, "encode cancelCoAuth message for server");
        if (CoAuthMsgCheckUtil.checkCoAuthPairGroupEntity(coAuthPairGroupEntity)) {
            Log.e(CoAuthUtil.TAG, "encode cancelCoAuth message for server error, invalid coAuthPairGroupEntity");
            return this.encodeError;
        } else if (CoAuthMsgCheckUtil.checkCoAuthContext(coAuthContext)) {
            Log.e(CoAuthUtil.TAG, "encode cancelCoAuth message for server error, invalid coAuthContext");
            return this.encodeError;
        } else {
            try {
                TlvWrapper containerPayload = getCoAuthMsgPayload(sessionId, coAuthContext);
                TlvWrapper containerHeader = new TlvWrapper();
                encodeHeader(containerHeader, coAuthPairGroupEntity);
                encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.CANCEL_CO_AUTH);
                return containerHeader.serialize();
            } catch (UnsupportedEncodingException ex) {
                String str = CoAuthUtil.TAG;
                Log.e(str, "encode cancelCoAuth message for server error = " + ex.getMessage());
                return this.encodeError;
            }
        }
    }

    private TlvWrapper encodeHeader(TlvWrapper containerHeader, CoAuthHeaderEntity coAuthHeader) throws UnsupportedEncodingException {
        TlvWrapper containerHeaderPayload = new TlvWrapper();
        containerHeaderPayload.appendInt(CoAuthHeaderTagType.VERSION.getValue(), coAuthHeader.getVersion());
        containerHeaderPayload.appendString(CoAuthHeaderTagType.SRC_DID.getValue(), coAuthHeader.getSrcDid());
        containerHeaderPayload.appendInt(CoAuthHeaderTagType.SRC_MODULE.getValue(), coAuthHeader.getSrcModule());
        containerHeaderPayload.appendString(CoAuthHeaderTagType.DST_DID.getValue(), coAuthHeader.getDstDid());
        containerHeaderPayload.appendInt(CoAuthHeaderTagType.DST_MODULE.getValue(), coAuthHeader.getDstModule());
        containerHeader.appendTlvWrapper(CoAuthOperationType.HEADER.getValue(), containerHeaderPayload);
        return containerHeader;
    }

    private TlvWrapper encodeHeader(TlvWrapper containerHeader, CoAuthPairGroupEntity coAuthPairGroup) throws UnsupportedEncodingException {
        TlvWrapper containerHeaderPayload = new TlvWrapper();
        encodeHeaderVersion(containerHeaderPayload, coAuthPairGroup.getVersion());
        encodeHeaderSrcDid(containerHeaderPayload, coAuthPairGroup.getSrcDid());
        encodeHeaderSrcModule(containerHeaderPayload, coAuthPairGroup.getSrcModule());
        encodeHeadeDstDid(containerHeaderPayload, coAuthPairGroup.getDstDid());
        encodeHeaderDstModule(containerHeaderPayload, coAuthPairGroup.getDstModule());
        containerHeader.appendTlvWrapper(CoAuthOperationType.HEADER.getValue(), containerHeaderPayload);
        return containerHeader;
    }

    private TlvWrapper encodeHeaderDstModule(TlvWrapper containerHeaderPayload, int dstModule) {
        containerHeaderPayload.appendInt(CoAuthHeaderTagType.DST_MODULE.getValue(), dstModule);
        return containerHeaderPayload;
    }

    private TlvWrapper encodeHeaderSrcModule(TlvWrapper containerHeaderPayload, int srcModule) {
        containerHeaderPayload.appendInt(CoAuthHeaderTagType.SRC_MODULE.getValue(), srcModule);
        return containerHeaderPayload;
    }

    private TlvWrapper encodeHeaderVersion(TlvWrapper containerHeaderPayload, int version) {
        containerHeaderPayload.appendInt(CoAuthHeaderTagType.VERSION.getValue(), version);
        return containerHeaderPayload;
    }

    private TlvWrapper encodeHeadeDstDid(TlvWrapper containerHeaderPayload, String dstDid) throws UnsupportedEncodingException {
        containerHeaderPayload.appendString(CoAuthHeaderTagType.DST_DID.getValue(), dstDid);
        return containerHeaderPayload;
    }

    private TlvWrapper encodeHeaderSrcDid(TlvWrapper containerHeaderPayload, String srcDid) throws UnsupportedEncodingException {
        containerHeaderPayload.appendString(CoAuthHeaderTagType.SRC_DID.getValue(), srcDid);
        return containerHeaderPayload;
    }

    private TlvWrapper encodeVerifierDid(TlvWrapper container, String verifierDid) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.VERIFIER_DID.getValue(), verifierDid);
        return container;
    }

    private TlvWrapper encodeSensorDid(TlvWrapper container, String sensorDid) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.SENSOR_DID.getValue(), sensorDid);
        return container;
    }

    private TlvWrapper encodeAuthType(TlvWrapper container, int authType) {
        container.appendInt(CoAuthMsgTagType.AUTH_TYPE.getValue(), authType);
        return container;
    }

    private TlvWrapper encodeChallenge(TlvWrapper container, long challenge) {
        container.appendLong(CoAuthMsgTagType.CHALLENGE.getValue(), challenge);
        return container;
    }

    private TlvWrapper encodeGroupId(TlvWrapper container, byte[] groupId) {
        container.appendBytes(CoAuthMsgTagType.GROUP_ID.getValue(), groupId);
        return container;
    }

    private TlvWrapper encodePeerDeviceIp(TlvWrapper container, CoAuthHeaderEntity coAuthHeaderEntity) throws UnsupportedEncodingException {
        if (coAuthHeaderEntity.getDstDeviceIp() != null && !coAuthHeaderEntity.getDstDeviceIp().isEmpty()) {
            container.appendString(CoAuthMsgTagType.PEER_DEVICE_IP.getValue(), coAuthHeaderEntity.getDstDeviceIp());
        }
        return container;
    }

    private TlvWrapper encodePeerDevicePort(TlvWrapper container, CoAuthHeaderEntity coAuthHeaderEntity) throws UnsupportedEncodingException {
        if (coAuthHeaderEntity.getDstDevicePort() != null && !coAuthHeaderEntity.getDstDevicePort().isEmpty()) {
            container.appendString(CoAuthMsgTagType.PEER_DEVICE_PORT.getValue(), coAuthHeaderEntity.getDstDevicePort());
        }
        return container;
    }

    private TlvWrapper encodePeerDid(TlvWrapper container, String peerDid) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.PEER_DID.getValue(), peerDid);
        return container;
    }

    private TlvWrapper encodeSelfDid(TlvWrapper container, String selfDid) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.SELF_DID.getValue(), selfDid);
        return container;
    }

    private TlvWrapper encodeModuleName(TlvWrapper container, String moduleName) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.MODULE_NAME.getValue(), moduleName);
        return container;
    }

    private TlvWrapper encodePackageName(TlvWrapper container, String packageName) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.PACKAGE_NAME.getValue(), packageName);
        return container;
    }

    private TlvWrapper encodeSessionId(TlvWrapper container, long sessionId) {
        container.appendLong(CoAuthMsgTagType.SESSION_ID.getValue(), sessionId);
        return container;
    }

    private TlvWrapper encodeMsgOperationType(TlvWrapper container, TlvWrapper containerPayload, CoAuthOperationType coAuthOperationType) {
        container.appendTlvWrapper(coAuthOperationType.getValue(), containerPayload);
        return container;
    }
}

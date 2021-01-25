package com.huawei.coauth.auth.authmsg;

import android.util.Log;
import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.CoAuthDevice;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.tlv.TlvWrapper;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.function.Consumer;

public class CoAuthMsgEncodeMgr implements IcoAuthMsgEncodeMgr {
    private static final String LOCK_GUIDE_LINES_KEY = "lockOutGuidelines";
    private static final String SUBTITLE_STRING_KEY = "subTitleString";
    private static final String TITLE_STRING_KEY = "titleString";
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
    public byte[] queryCoAuthMethodMsg(long sessionId, CoAuthContext coAuthContext) {
        if (CoAuthMsgCheckUtil.checkQueryCoAuthMethodCheck(coAuthContext)) {
            Log.e(CoAuthUtil.TAG, "encode queryCoAuthMethod message for server error, invalid coAuthContext.");
            return this.encodeError;
        }
        try {
            TlvWrapper containerPayload = getQueryMethodPayload(coAuthContext, sessionId);
            TlvWrapper containerHeader = new TlvWrapper();
            encodeHeader(containerHeader, coAuthContext.getCoAuthGroup().getGroupId());
            encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.CO_AUTH_QUERY_METHOD);
            return containerHeader.serialize();
        } catch (UnsupportedEncodingException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "encode coAuth message for server error = " + ex.getMessage());
            return this.encodeError;
        }
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] getPropertyMsg(long sessionId, byte[] key, CoAuthContext coAuthContext) {
        if (CoAuthMsgCheckUtil.checkGetProperty(coAuthContext, key)) {
            Log.e(CoAuthUtil.TAG, "encode queryCoAuthMethod message for server error, invalid coAuthContext.");
            return this.encodeError;
        }
        try {
            TlvWrapper containerPayload = propertyMsgPayload(coAuthContext, sessionId, key, null);
            TlvWrapper containerHeader = new TlvWrapper();
            encodeHeader(containerHeader, coAuthContext.getCoAuthGroup().getGroupId());
            encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.GET_PROPERTY);
            return containerHeader.serialize();
        } catch (UnsupportedEncodingException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "encode coAuth message for server error = " + ex.getMessage());
            return this.encodeError;
        }
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] setPropertyMsg(long sessionId, byte[] key, byte[] value, CoAuthContext coAuthContext) {
        if (CoAuthMsgCheckUtil.checkGetProperty(coAuthContext, key)) {
            Log.e(CoAuthUtil.TAG, "encode queryCoAuthMethod message for server error, invalid coAuthContext.");
            return this.encodeError;
        }
        try {
            TlvWrapper containerPayload = propertyMsgPayload(coAuthContext, sessionId, key, value);
            TlvWrapper containerHeader = new TlvWrapper();
            encodeHeader(containerHeader, coAuthContext.getCoAuthGroup().getGroupId());
            encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.SET_PROPERTY);
            return containerHeader.serialize();
        } catch (UnsupportedEncodingException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "encode coAuth message for server error = " + ex.getMessage());
            return this.encodeError;
        }
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] coAuthMsg(long sessionId, CoAuthContext coAuthContext, String consumerPackageName) {
        Log.d(CoAuthUtil.TAG, "encode coAuth message for server");
        if (CoAuthMsgCheckUtil.checkCoAuthContext(coAuthContext)) {
            Log.e(CoAuthUtil.TAG, "encode coAuth message for server error, invalid coAuthContext");
            return this.encodeError;
        }
        try {
            TlvWrapper containerPayload = getCoAuthMsgPayload(sessionId, coAuthContext, consumerPackageName);
            TlvWrapper containerHeader = new TlvWrapper();
            encodeHeader(containerHeader, coAuthContext.getCoAuthGroup().getGroupId());
            encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.CO_AUTH);
            return containerHeader.serialize();
        } catch (UnsupportedEncodingException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "encode coAuth message for server error = " + ex.getMessage());
            return this.encodeError;
        }
    }

    @Override // com.huawei.coauth.auth.authmsg.IcoAuthMsgEncodeMgr
    public byte[] initCoAuthIdmGroupMsg(long sessionId, String idmGid, List<CoAuthDevice> devList) {
        if (CoAuthMsgCheckUtil.checkInitCoAuthIdmGroup(devList)) {
            Log.e(CoAuthUtil.TAG, "encode initCoAuthIdmGroup message for server error, invalid devList.");
            return this.encodeError;
        }
        try {
            TlvWrapper containerPayload = coAuthIdmGroupMsgPayload(sessionId, idmGid, devList);
            TlvWrapper containerHeader = new TlvWrapper();
            encodeHeader(containerHeader, idmGid);
            encodeMsgOperationType(containerHeader, containerPayload, CoAuthOperationType.INIT_CO_AUTH_IDM_GROUP_REQUEST);
            return containerHeader.serialize();
        } catch (UnsupportedEncodingException ex) {
            String str = CoAuthUtil.TAG;
            Log.e(str, "encode initCoAuthIdmGroup message for server error = " + ex.getMessage());
            return this.encodeError;
        }
    }

    private TlvWrapper getCoAuthMsgPayload(long sessionId, CoAuthContext coAuthContext, String consumerPackageName) throws UnsupportedEncodingException {
        TlvWrapper containerPayload = new TlvWrapper();
        if (consumerPackageName != null) {
            encodePackageName(containerPayload, consumerPackageName);
        }
        encodeCoAuthContext(sessionId, containerPayload, coAuthContext);
        return containerPayload;
    }

    private TlvWrapper getQueryMethodPayload(CoAuthContext coAuthContext, long sessionId) throws UnsupportedEncodingException {
        TlvWrapper containerPayload = new TlvWrapper();
        encodeCoAuthContext(sessionId, containerPayload, coAuthContext);
        return containerPayload;
    }

    private TlvWrapper propertyMsgPayload(CoAuthContext coAuthContext, long sessionId, byte[] key, byte[] value) throws UnsupportedEncodingException {
        TlvWrapper containerPayload = new TlvWrapper();
        encodePropertyKey(containerPayload, key);
        if (value != null) {
            encodePropertyValue(containerPayload, value);
        }
        encodeCoAuthContext(sessionId, containerPayload, coAuthContext);
        return containerPayload;
    }

    private void encodeSensorVerifier(CoAuthContext coAuthContext, TlvWrapper containerPayload) throws UnsupportedEncodingException {
        String sensorDid = coAuthContext.getSensorDeviceId();
        if (sensorDid != null) {
            encodeSensorDid(containerPayload, sensorDid);
        }
        String verifierDid = coAuthContext.getVerifyDeviceId();
        if (verifierDid != null) {
            encodeVerifierDid(containerPayload, verifierDid);
        }
    }

    private void encodeAuthUiConfig(CoAuthContext coAuthContext, TlvWrapper containerPayload) throws UnsupportedEncodingException {
        if (coAuthContext.getUiConfig() != null) {
            String titleString = coAuthContext.getUiConfig().getString(TITLE_STRING_KEY);
            if (titleString != null) {
                encodeTitleString(containerPayload, titleString);
            }
            String subTitleString = coAuthContext.getUiConfig().getString(SUBTITLE_STRING_KEY);
            if (subTitleString != null) {
                encodeSubTitleString(containerPayload, subTitleString);
            }
            String lockGuide = coAuthContext.getUiConfig().getString(LOCK_GUIDE_LINES_KEY);
            if (lockGuide != null) {
                encodeLockGuide(containerPayload, lockGuide);
            }
        }
    }

    private void encodeAuthParam(CoAuthContext coAuthContext, final TlvWrapper containerPayload) {
        coAuthContext.getAuthPara().ifPresent(new Consumer<byte[]>() {
            /* class com.huawei.coauth.auth.authmsg.CoAuthMsgEncodeMgr.AnonymousClass1 */

            public void accept(byte[] authPara) {
                CoAuthMsgEncodeMgr.this.encodeAuthPara(containerPayload, authPara);
            }
        });
        coAuthContext.getAddition().ifPresent(new Consumer<byte[]>() {
            /* class com.huawei.coauth.auth.authmsg.CoAuthMsgEncodeMgr.AnonymousClass2 */

            public void accept(byte[] authAddition) {
                CoAuthMsgEncodeMgr.this.encodeAuthPara(containerPayload, authAddition);
            }
        });
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
                TlvWrapper containerPayload = getCoAuthMsgPayload(sessionId, coAuthContext, null);
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

    private TlvWrapper encodeHeader(TlvWrapper containerHeader, String groupId) throws UnsupportedEncodingException {
        TlvWrapper containerHeaderPayload = new TlvWrapper();
        containerHeaderPayload.appendString(CoAuthHeaderTagType.GROUP_ID.getValue(), groupId);
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

    private TlvWrapper encodeCoAuthContext(long sessionId, TlvWrapper payload, CoAuthContext coAuthContext) throws UnsupportedEncodingException {
        final TlvWrapper contextPayload = new TlvWrapper();
        encodeSessionId(contextPayload, sessionId);
        encodeGroupId(contextPayload, CoAuthUtil.hexStringToBytes(coAuthContext.getCoAuthGroup().getGroupId()).get());
        encodeChallenge(contextPayload, coAuthContext.getChallenge());
        encodeSensorVerifier(coAuthContext, contextPayload);
        encodeAuthType(contextPayload, coAuthContext.getAuthType().getValue());
        encodeRetryCount(contextPayload, coAuthContext.getRetryCount());
        encodeAuthParam(coAuthContext, contextPayload);
        encodeAuthExecAbility(contextPayload, coAuthContext.getExecAbility());
        coAuthContext.getTemplateId().ifPresent(new Consumer<byte[]>() {
            /* class com.huawei.coauth.auth.authmsg.CoAuthMsgEncodeMgr.AnonymousClass3 */

            public void accept(byte[] templateId) {
                CoAuthMsgEncodeMgr.this.encodeTemplateId(contextPayload, templateId);
            }
        });
        encodeAuthUiConfig(coAuthContext, contextPayload);
        encodeEnableUi(contextPayload, coAuthContext.isEnableUi());
        if (coAuthContext.getFallbackContext() != null) {
            encodeCoAuthContext(sessionId, contextPayload, coAuthContext.getFallbackContext());
        }
        payload.appendTlvWrapper(CoAuthMsgTagType.CO_AUTH_CONTEXT.getValue(), contextPayload);
        return payload;
    }

    private TlvWrapper encodeVerifierDid(TlvWrapper container, String verifierDid) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.VERIFIER_DID.getValue(), verifierDid);
        return container;
    }

    private TlvWrapper encodeSensorDid(TlvWrapper container, String sensorDid) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.SENSOR_DID.getValue(), sensorDid);
        return container;
    }

    private TlvWrapper encodeTitleString(TlvWrapper container, String titleString) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.TITLE_STRING.getValue(), titleString);
        return container;
    }

    private TlvWrapper encodeSubTitleString(TlvWrapper container, String subTitleString) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.SUB_TITLE_STRING.getValue(), subTitleString);
        return container;
    }

    private TlvWrapper encodeLockGuide(TlvWrapper container, String lockGuide) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.LOCK_GUIDE.getValue(), lockGuide);
        return container;
    }

    private TlvWrapper encodeAuthType(TlvWrapper container, int authType) {
        container.appendInt(CoAuthMsgTagType.AUTH_TYPE.getValue(), authType);
        return container;
    }

    private TlvWrapper encodeEnableUi(TlvWrapper container, boolean isEnableUi) {
        container.appendInt(CoAuthMsgTagType.ENABLE_UI.getValue(), isEnableUi ? 1 : 0);
        return container;
    }

    private TlvWrapper encodeRetryCount(TlvWrapper container, int retryCount) {
        container.appendInt(CoAuthMsgTagType.RETRY_COUNT.getValue(), retryCount);
        return container;
    }

    private TlvWrapper encodeAuthExecAbility(TlvWrapper container, int execAbility) {
        container.appendInt(CoAuthMsgTagType.EXEC_ABILITY.getValue(), execAbility);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private TlvWrapper encodeAuthPara(TlvWrapper container, byte[] para) {
        container.appendBytes(CoAuthMsgTagType.AUTH_PARA.getValue(), para);
        return container;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private TlvWrapper encodeTemplateId(TlvWrapper container, byte[] template) {
        container.appendBytes(CoAuthMsgTagType.TEMPLATE_ID.getValue(), template);
        return container;
    }

    private TlvWrapper encodePropertyKey(TlvWrapper container, byte[] key) {
        container.appendBytes(CoAuthMsgTagType.PROPERTY_KEY.getValue(), key);
        return container;
    }

    private TlvWrapper encodePropertyValue(TlvWrapper container, byte[] value) {
        container.appendBytes(CoAuthMsgTagType.PROPERTY_VALUE.getValue(), value);
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

    private TlvWrapper coAuthIdmGroupMsgPayload(long sessionId, String idmGid, List<CoAuthDevice> devList) throws UnsupportedEncodingException {
        TlvWrapper containerPayload = new TlvWrapper();
        if (idmGid != null) {
            encodeIdmGid(containerPayload, idmGid);
        }
        encodeSessionId(containerPayload, sessionId);
        for (CoAuthDevice coAuthDevice : devList) {
            encodeCoAuthDevice(containerPayload, coAuthDevice);
        }
        return containerPayload;
    }

    private TlvWrapper encodeIdmGid(TlvWrapper container, String idmGid) throws UnsupportedEncodingException {
        container.appendBytes(CoAuthMsgTagType.IDM_GROUP_GID.getValue(), CoAuthUtil.hexStringToBytes(idmGid).get());
        return container;
    }

    private TlvWrapper encodeCoAuthDevice(TlvWrapper container, CoAuthDevice coAuthDevice) throws UnsupportedEncodingException {
        TlvWrapper devicePayload = new TlvWrapper();
        String deviceId = coAuthDevice.getDeviceId();
        if (deviceId != null) {
            encodeDeviceId(devicePayload, deviceId);
        }
        encodeDeviceIP(devicePayload, IpUtil.ipString2Int(coAuthDevice.getIp()));
        encodePeerLinkType(devicePayload, coAuthDevice.getPeerLinkType());
        encodePeerLinkMode(devicePayload, coAuthDevice.getPeerLinkMode());
        String delegatedPkgName = coAuthDevice.getExtraMeta();
        if (delegatedPkgName != null) {
            encodeDeviceDelegatedPkgName(devicePayload, delegatedPkgName);
        }
        container.appendTlvWrapper(CoAuthMsgTagType.IDM_DEVICE_INFO.getValue(), devicePayload);
        return container;
    }

    private TlvWrapper encodeDeviceId(TlvWrapper container, String deviceId) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.IDM_DEVICE_UDID.getValue(), deviceId);
        return container;
    }

    private TlvWrapper encodeDeviceIP(TlvWrapper container, int deviceIp) throws UnsupportedEncodingException {
        container.appendInt(CoAuthMsgTagType.IDM_DEVICE_IP.getValue(), deviceIp);
        return container;
    }

    private TlvWrapper encodePeerLinkType(TlvWrapper container, int peerLinkType) throws UnsupportedEncodingException {
        container.appendInt(CoAuthMsgTagType.IDM_DEVICE_LINK_TYPE.getValue(), peerLinkType);
        return container;
    }

    private TlvWrapper encodePeerLinkMode(TlvWrapper container, int peerLinkMode) throws UnsupportedEncodingException {
        container.appendInt(CoAuthMsgTagType.IDM_DEVICE_LINK_MODE.getValue(), peerLinkMode);
        return container;
    }

    private TlvWrapper encodeDeviceDelegatedPkgName(TlvWrapper container, String delegatedPkgName) throws UnsupportedEncodingException {
        container.appendString(CoAuthMsgTagType.IDM_DELEGATED_PKG_NAME.getValue(), delegatedPkgName);
        return container;
    }
}

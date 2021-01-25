package com.huawei.coauth.auth.authmsg;

import android.util.Log;
import com.huawei.coauth.auth.CoAuthDevice;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.coauth.auth.authentity.CoAuthIdmGroupEntity;
import com.huawei.coauth.auth.authmsg.CoAuthDeviceDecodeUtil;
import com.huawei.coauth.tlv.TlvBase;
import com.huawei.coauth.tlv.TlvTransformException;
import com.huawei.coauth.tlv.TlvWrapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CoAuthIdmGroupDecodeUtil {
    private static Map<Integer, Strategy> parsingStrategy = new HashMap<Integer, Strategy>() {
        /* class com.huawei.coauth.auth.authmsg.CoAuthIdmGroupDecodeUtil.AnonymousClass1 */

        {
            put(Integer.valueOf(CoAuthMsgTagType.IDM_GROUP_GID.getValue()), new IdmGidStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.IDM_DEVICE_INFO.getValue()), new IdmDeviceInfoStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.RESULT_CODE.getValue()), new IdmResultCodeStrategy());
        }
    };

    public interface Strategy {
        CoAuthIdmGroupEntity.Builder setFieldToBuilder(CoAuthIdmGroupEntity.Builder builder, TlvBase tlvBase) throws TlvTransformException;
    }

    public static Strategy getStrategy(int type) {
        return parsingStrategy.get(Integer.valueOf(type));
    }

    static class IdmGidStrategy implements Strategy {
        IdmGidStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthIdmGroupDecodeUtil.Strategy
        public CoAuthIdmGroupEntity.Builder setFieldToBuilder(CoAuthIdmGroupEntity.Builder builder, TlvBase tlv) throws TlvTransformException {
            byte[] groupId = TlvWrapper.parseBytes(tlv).get();
            String str = CoAuthUtil.TAG;
            Log.i(str, "receive message from server groupId = " + Arrays.toString(groupId));
            return builder.setGroupId(groupId);
        }
    }

    static class IdmDeviceInfoStrategy implements Strategy {
        IdmDeviceInfoStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthIdmGroupDecodeUtil.Strategy
        public CoAuthIdmGroupEntity.Builder setFieldToBuilder(CoAuthIdmGroupEntity.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setDevList(CoAuthIdmGroupDecodeUtil.resolveCoAuthDevice(tlv.getValue()));
        }
    }

    static class IdmResultCodeStrategy implements Strategy {
        IdmResultCodeStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthIdmGroupDecodeUtil.Strategy
        public CoAuthIdmGroupEntity.Builder setFieldToBuilder(CoAuthIdmGroupEntity.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setResultCode(TlvWrapper.parseInt(tlv).getAsInt());
        }
    }

    /* access modifiers changed from: private */
    public static CoAuthDevice resolveCoAuthDevice(byte[] value) throws TlvTransformException {
        TlvWrapper deviceMsg = TlvWrapper.deserialize(value);
        CoAuthDevice.Builder builder = new CoAuthDevice.Builder();
        for (TlvBase tlv : deviceMsg.getTlvList()) {
            CoAuthDeviceDecodeUtil.Strategy strategy = CoAuthDeviceDecodeUtil.getStrategy(tlv.getType());
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

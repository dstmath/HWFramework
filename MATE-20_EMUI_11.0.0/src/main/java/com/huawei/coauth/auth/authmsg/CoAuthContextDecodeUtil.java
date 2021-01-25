package com.huawei.coauth.auth.authmsg;

import android.util.Log;
import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.CoAuthType;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.coauth.tlv.TlvBase;
import com.huawei.coauth.tlv.TlvTransformException;
import com.huawei.coauth.tlv.TlvWrapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CoAuthContextDecodeUtil {
    private static Map<Integer, Strategy> parsingStrategy = new HashMap<Integer, Strategy>() {
        /* class com.huawei.coauth.auth.authmsg.CoAuthContextDecodeUtil.AnonymousClass1 */

        {
            put(Integer.valueOf(CoAuthMsgTagType.AUTH_TYPE.getValue()), new AuthTypeStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.SENSOR_DID.getValue()), new SensorDeviceIdStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.VERIFIER_DID.getValue()), new VerifyDeviceIdStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.GROUP_ID.getValue()), new GroupStrategy());
        }
    };

    public interface Strategy {
        CoAuthContext.Builder setFieldToBuilder(CoAuthContext.Builder builder, TlvBase tlvBase) throws TlvTransformException;
    }

    public static Strategy getStrategy(int type) {
        return parsingStrategy.get(Integer.valueOf(type));
    }

    static class AuthTypeStrategy implements Strategy {
        AuthTypeStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthContextDecodeUtil.Strategy
        public CoAuthContext.Builder setFieldToBuilder(CoAuthContext.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setAuthType(CoAuthType.valueOf(TlvWrapper.parseInt(tlv).getAsInt()));
        }
    }

    static class SensorDeviceIdStrategy implements Strategy {
        SensorDeviceIdStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthContextDecodeUtil.Strategy
        public CoAuthContext.Builder setFieldToBuilder(CoAuthContext.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setSensorDeviceId(CoAuthUtil.bytesToString(TlvWrapper.parseBytes(tlv).get()).get());
        }
    }

    static class VerifyDeviceIdStrategy implements Strategy {
        VerifyDeviceIdStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthContextDecodeUtil.Strategy
        public CoAuthContext.Builder setFieldToBuilder(CoAuthContext.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setVerifyDeviceId(CoAuthUtil.bytesToString(TlvWrapper.parseBytes(tlv).get()).get());
        }
    }

    static class GroupStrategy implements Strategy {
        GroupStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthContextDecodeUtil.Strategy
        public CoAuthContext.Builder setFieldToBuilder(final CoAuthContext.Builder builder, TlvBase tlv) throws TlvTransformException {
            byte[] groupId = TlvWrapper.parseBytes(tlv).get();
            CoAuthUtil.bytesToHexString(groupId).ifPresent(new Consumer<String>() {
                /* class com.huawei.coauth.auth.authmsg.CoAuthContextDecodeUtil.GroupStrategy.AnonymousClass1 */

                public void accept(String hexString) {
                    builder.setCoAuthGroup(hexString);
                }
            });
            String str = CoAuthUtil.TAG;
            Log.i(str, "receive message from server groupId = " + Arrays.toString(groupId));
            return builder;
        }
    }
}

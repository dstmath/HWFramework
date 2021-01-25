package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.auth.CoAuthDevice;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.coauth.tlv.TlvBase;
import com.huawei.coauth.tlv.TlvTransformException;
import com.huawei.coauth.tlv.TlvWrapper;
import java.util.HashMap;
import java.util.Map;

public class CoAuthDeviceDecodeUtil {
    private static Map<Integer, Strategy> parsingStrategy = new HashMap<Integer, Strategy>() {
        /* class com.huawei.coauth.auth.authmsg.CoAuthDeviceDecodeUtil.AnonymousClass1 */

        {
            put(Integer.valueOf(CoAuthMsgTagType.IDM_DEVICE_UDID.getValue()), new DeviceUdidStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.IDM_DEVICE_IP.getValue()), new DeviceIpStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.IDM_DEVICE_LINK_TYPE.getValue()), new DevicePeerLinkTypeStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.IDM_DEVICE_LINK_MODE.getValue()), new DevicePeerLinkModeStrategy());
            put(Integer.valueOf(CoAuthMsgTagType.IDM_DELEGATED_PKG_NAME.getValue()), new DeviceDelegatedPkgNameStrategy());
        }
    };

    public interface Strategy {
        CoAuthDevice.Builder setFieldToBuilder(CoAuthDevice.Builder builder, TlvBase tlvBase) throws TlvTransformException;
    }

    public static Strategy getStrategy(int type) {
        return parsingStrategy.get(Integer.valueOf(type));
    }

    static class DeviceUdidStrategy implements Strategy {
        DeviceUdidStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthDeviceDecodeUtil.Strategy
        public CoAuthDevice.Builder setFieldToBuilder(CoAuthDevice.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setDeviceId(CoAuthUtil.bytesToString(TlvWrapper.parseBytes(tlv).get()).get());
        }
    }

    static class DeviceIpStrategy implements Strategy {
        DeviceIpStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthDeviceDecodeUtil.Strategy
        public CoAuthDevice.Builder setFieldToBuilder(CoAuthDevice.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setIp(IpUtil.ipInt2String(TlvWrapper.parseInt(tlv).getAsInt()));
        }
    }

    static class DevicePeerLinkTypeStrategy implements Strategy {
        DevicePeerLinkTypeStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthDeviceDecodeUtil.Strategy
        public CoAuthDevice.Builder setFieldToBuilder(CoAuthDevice.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setPeerLinkType(TlvWrapper.parseInt(tlv).getAsInt());
        }
    }

    static class DevicePeerLinkModeStrategy implements Strategy {
        DevicePeerLinkModeStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthDeviceDecodeUtil.Strategy
        public CoAuthDevice.Builder setFieldToBuilder(CoAuthDevice.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setPeerLinkMode(TlvWrapper.parseInt(tlv).getAsInt());
        }
    }

    static class DeviceDelegatedPkgNameStrategy implements Strategy {
        DeviceDelegatedPkgNameStrategy() {
        }

        @Override // com.huawei.coauth.auth.authmsg.CoAuthDeviceDecodeUtil.Strategy
        public CoAuthDevice.Builder setFieldToBuilder(CoAuthDevice.Builder builder, TlvBase tlv) throws TlvTransformException {
            return builder.setExtraMeta(CoAuthUtil.bytesToString(TlvWrapper.parseBytes(tlv).get()).get());
        }
    }
}

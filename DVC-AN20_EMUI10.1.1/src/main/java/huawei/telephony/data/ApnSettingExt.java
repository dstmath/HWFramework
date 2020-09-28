package huawei.telephony.data;

import android.telephony.data.ApnSetting;
import com.android.internal.telephony.AbstractPhoneConstants;

public class ApnSettingExt {
    public static ApnSetting makeApnSettingForSlice(ApnSetting apn) {
        if (apn == null) {
            return null;
        }
        return ApnSetting.makeApnSetting(apn.getId(), apn.getOperatorNumeric(), apn.getEntryName(), AbstractPhoneConstants.APN_TYPE_SNSSAI, apn.getProxyAddressAsString(), apn.getProxyPort(), apn.getMmsc(), apn.getMmsProxyAddressAsString(), apn.getMmsProxyPort(), apn.getUser(), apn.getPassword(), apn.getAuthType(), 33554432, apn.getProtocol(), apn.getRoamingProtocol(), apn.isEnabled(), apn.getNetworkTypeBitmask(), apn.getProfileId(), apn.isPersistent(), apn.getMaxConns(), apn.getWaitTime(), apn.getMaxConnsTime(), apn.getMtu(), apn.getMvnoType(), apn.getMvnoMatchData(), apn.getApnSetId(), apn.getCarrierId(), apn.getSkip464Xlat());
    }
}

package huawei.telephony.data;

import android.os.Bundle;
import android.telephony.ServiceState;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataProfile;
import android.text.TextUtils;

public class DataProfileEx {
    private static final int INVAILID_PROTOCOL_TYPE = -1;
    private static final String NETWORK_APN = "apn";
    private static final String NTWORK_IP_TYPE = "ipType";
    private static final String SLICE_DNN = "dnn";
    private static final String SLICE_PDU_SESSION_TYPE = "pduSessionType";
    private static final String SLICE_SNSSAI = "snssai";
    private static final String SLICE_SSC_MODE = "sscMode";
    private DataProfile mDataProfile;

    public DataProfileEx createDataProfile(ApnSetting apn, int profileId, boolean isPreferred, Bundle extraData) {
        int profileType;
        String snssai = "";
        String apnName = null;
        byte sscMode = 0;
        int protocolType = -1;
        if (extraData != null) {
            apnName = extraData.getString(SLICE_DNN, null);
            snssai = extraData.getString("snssai", "");
            sscMode = extraData.getByte(SLICE_SSC_MODE, (byte) 0).byteValue();
            protocolType = extraData.getInt(SLICE_PDU_SESSION_TYPE, -1);
            if (TextUtils.isEmpty(apnName) && protocolType == -1 && TextUtils.isEmpty(snssai)) {
                apnName = extraData.getString("apn", null);
                protocolType = extraData.getInt(NTWORK_IP_TYPE, -1);
            }
        }
        int networkTypeBitmask = apn.getNetworkTypeBitmask();
        if (networkTypeBitmask == 0) {
            profileType = 0;
        } else if (ServiceState.bearerBitmapHasCdma(networkTypeBitmask)) {
            profileType = 2;
        } else {
            profileType = 1;
        }
        DataProfile dataProfile = new DataProfile.Builder().setProfileId(profileId).setApn(apnName == null ? apn.getApnName() : apnName).setProtocolType(protocolType == -1 ? apn.getProtocol() : protocolType).setAuthType(apn.getAuthType()).setUserName(apn.getUser()).setPassword(apn.getPassword()).setType(profileType).setMaxConnectionsTime(apn.getMaxConnsTime()).setMaxConnections(apn.getMaxConns()).setWaitTime(apn.getWaitTime()).enable(apn.isEnabled()).setSupportedApnTypesBitmask(apn.getApnTypeBitmask()).setRoamingProtocolType(apn.getRoamingProtocol()).setBearerBitmask(networkTypeBitmask).setMtu(apn.getMtu()).setPersistent(apn.isPersistent()).setPreferred(isPreferred).build();
        dataProfile.setSnssai(snssai);
        dataProfile.setSscMode(sscMode);
        this.mDataProfile = dataProfile;
        return this;
    }

    public DataProfile getDataProfile() {
        return this.mDataProfile;
    }
}

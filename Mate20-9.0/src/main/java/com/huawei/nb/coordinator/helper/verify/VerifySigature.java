package com.huawei.nb.coordinator.helper.verify;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.nb.coordinator.helper.http.HttpRequest;
import com.huawei.nb.utils.DeviceUtil;
import com.huawei.nb.utils.logger.DSLog;
import java.security.SecureRandom;

public class VerifySigature implements IVerify {
    private static final int ORIGIN_CODE_LENGTH = 8;
    private static final String TAG = "VerifySigature";

    public boolean generateAuthorization(Context context, HttpRequest.Builder builder, String appID) {
        String deviceToken = DeviceUtil.getDeviceToken(context);
        if (TextUtils.isEmpty(deviceToken)) {
            DSLog.e("VerifySigature: empty deviceToken.", new Object[0]);
            return false;
        }
        String serialNumber = DeviceUtil.getSerialNumber();
        if (TextUtils.isEmpty(serialNumber)) {
            DSLog.e("VerifySigature empty serialNumber.", new Object[0]);
            return false;
        }
        String originCode = getOriginCode();
        if (TextUtils.isEmpty(originCode)) {
            DSLog.e("VerifySigature: empty originCode.", new Object[0]);
            return false;
        }
        String encryptedCode = getEncryptedCode(context, originCode);
        if (TextUtils.isEmpty(encryptedCode)) {
            DSLog.e("VerifySigature: empty encryptedCode.", new Object[0]);
            return false;
        }
        String emmcID = DeviceUtil.getEMMCID();
        if (TextUtils.isEmpty(emmcID)) {
            DSLog.e("VerifySigature empty emmcID.", new Object[0]);
            return false;
        }
        String packageName = context.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            DSLog.e("VerifySigature empty packageName.", new Object[0]);
            return false;
        }
        builder.addRequestHeader(IVerifyVar.DEVICE_TOKEN, deviceToken);
        builder.addRequestHeader(IVerifyVar.SN_KEY, serialNumber);
        builder.addRequestHeader(IVerifyVar.ENCRYPTEDCODE, encryptedCode);
        builder.addRequestHeader(IVerifyVar.ORIGINALCODE, packageName + originCode + emmcID + IVerifyVar.REQUEST_HEAD_ORIGINCODE_TAIL);
        return true;
    }

    public String verifyTokenHeader() {
        return null;
    }

    private String getOriginCode() {
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(new byte[8]);
        return secureRandom.nextLong() + "";
    }

    private String getEncryptedCode(Context context, String originCode) {
        return new VerifyViaHWMember().getSignature(originCode, context.getPackageName()).replace("\n", "");
    }
}

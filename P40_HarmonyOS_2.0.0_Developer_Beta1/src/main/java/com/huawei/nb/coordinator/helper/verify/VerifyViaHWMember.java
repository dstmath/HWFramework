package com.huawei.nb.coordinator.helper.verify;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import com.huawei.nb.coordinator.common.CoordinatorJsonAnalyzer;
import com.huawei.nb.coordinator.helper.http.HttpClient;
import com.huawei.nb.coordinator.helper.http.HttpRequest;
import com.huawei.nb.coordinator.helper.http.HttpRequestBody;
import com.huawei.nb.coordinator.helper.http.HttpResponse;
import com.huawei.nb.utils.DeviceUtil;
import com.huawei.nb.utils.logger.DSLog;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public class VerifyViaHWMember implements IVerify {
    private static final String DEV_ID = "devId";
    private static final String DEV_ID_TYPE = "devIdType";
    private static final int FAIL_FLAG = -1;
    private static final String HWMEMBER_FLAG = "PUBPRI";
    private static final String HWMEMBER_TOKEN_HEADER = "PKI_token";
    private static final String SIGNATURE_TYPE = "OSPID";
    private static final int START_INDEX = 8;
    private static final String TAG = "VerifyViaHWMember";
    private static final String VERIFY_URL_INTERFACE = "/api/v1/authservice/pki/session";

    @Override // com.huawei.nb.coordinator.helper.verify.IVerify
    public String verifyTokenHeader() {
        return HWMEMBER_TOKEN_HEADER;
    }

    public String getSignature(String str, String str2) {
        try {
            return getSignatureWithoutCatchThrowable(str, str2);
        } catch (Throwable th) {
            DSLog.e("VerifyViaHWMember dit not get the signature", new Object[0]);
            StringBuffer stringBuffer = new StringBuffer();
            for (StackTraceElement stackTraceElement : th.getStackTrace()) {
                stringBuffer.append(stackTraceElement);
            }
            DSLog.e("VerifyViaHWMember caught a throwable,message: " + th.getMessage() + ", cause" + th.getCause() + ", stacktrace: " + ((Object) stringBuffer), new Object[0]);
            return "";
        }
    }

    private String getSignatureWithoutCatchThrowable(String str, String str2) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        int intFiled = DeviceUtil.getIntFiled(DeviceUtil.DEVICE_ATTESTATION_MANAGER, "KEY_INDEX_HWCLOUD", -1);
        if (intFiled == -1) {
            DSLog.e("getAttestationSignature failed: keyIndexHwCloud == -1", new Object[0]);
            return "";
        }
        int intFiled2 = DeviceUtil.getIntFiled(DeviceUtil.DEVICE_ATTESTATION_MANAGER, "DEVICE_ID_TYPE_EMMC", -1);
        if (intFiled2 == -1) {
            DSLog.e("VerifyViaHWMembergetAttestationSignature failed: deviceIdTypeEmmc == -1", new Object[0]);
            return "";
        }
        try {
            Class<?> cls = Class.forName(DeviceUtil.DEVICE_ATTESTATION_MANAGER);
            if (cls == null) {
                DSLog.e("VerifyViaHWMember can not get cls.", new Object[0]);
                return "";
            }
            Object invoke = cls.getDeclaredMethod("getAttestationSignatureWithPkgName", Integer.TYPE, Integer.TYPE, String.class, byte[].class, String.class).invoke(cls.newInstance(), Integer.valueOf(intFiled), Integer.valueOf(intFiled2), "OSPID", bytes, str2);
            if (invoke != null && (invoke instanceof byte[])) {
                return Base64.encodeToString((byte[]) invoke, 0);
            }
            DSLog.e("VerifyViaHWMember can not get signature.", new Object[0]);
            return "";
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            DSLog.e("VerifyViaHWMembergetAttestationSignature failed: ", new Object[0]);
            return "";
        }
    }

    @Override // com.huawei.nb.coordinator.helper.verify.IVerify
    public boolean generateAuthorization(Context context, HttpRequest.Builder builder, String str, Bundle bundle) throws VerifyException {
        HttpResponse sessionAndRand = getSessionAndRand(str, builder.build().getUrl());
        String responseString = sessionAndRand.getResponseString();
        if (sessionAndRand.getStatusCode() == 200) {
            String jsonValue = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.SESSIONID_TYPE);
            String jsonValue2 = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.RAND_TYPE);
            if (TextUtils.isEmpty(jsonValue)) {
                DSLog.e("VerifyViaHWMember: empty sessionId", new Object[0]);
                return false;
            } else if (TextUtils.isEmpty(jsonValue2)) {
                DSLog.e("VerifyViaHWMember: empty rand", new Object[0]);
                return false;
            } else {
                String packageName = context.getPackageName();
                if (TextUtils.isEmpty(packageName)) {
                    DSLog.e("VerifyViaHWMember: empty packageName.", new Object[0]);
                    return false;
                }
                String signature = getSignature(jsonValue2, packageName);
                if (!TextUtils.isEmpty(signature)) {
                    String emmcid = DeviceUtil.getEMMCID();
                    if (TextUtils.isEmpty(emmcid)) {
                        DSLog.e("VerifyViaHWMember empty emmcID.", new Object[0]);
                        return false;
                    }
                    boolean z = true;
                    if (!isDevInfoValid(bundle)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("VerifyViaHWMember empty devInfo, devInfo ==null: ");
                        if (bundle != null) {
                            z = false;
                        }
                        sb.append(z);
                        DSLog.e(sb.toString(), new Object[0]);
                        return false;
                    }
                    String replace = (HWMEMBER_FLAG + " " + IVerifyVar.APPID_KEY + "=" + str + "," + IVerifyVar.SESSION_KEY + "=" + jsonValue + "," + IVerifyVar.SIGNATURE_KEY + "=" + signature + "," + IVerifyVar.EMMCID_KEY + "=" + emmcid + "," + IVerifyVar.PACKAGE_NAME_KEY + "=" + packageName + "," + DEV_ID_TYPE + "=" + bundle.getInt(DEV_ID_TYPE) + "," + DEV_ID + "=" + bundle.getString(DEV_ID)).replace("\n", "");
                    if (TextUtils.isEmpty(replace)) {
                        DSLog.e("VerifyViaHWMember: empty authorization", new Object[0]);
                        return false;
                    }
                    builder.addRequestHeader(IVerifyVar.AUTHORIZATION_KEY, replace);
                    return true;
                }
                DSLog.e("VerifyViaHWMember: empty signature.", new Object[0]);
                throw new VerifyException(-8, " signature is empty");
            }
        } else {
            DSLog.e("VerifyViaHWMember: status code is not 200: " + sessionAndRand.getStatusCode(), new Object[0]);
            throw new VerifyException(sessionAndRand.getStatusCode(), " Fail to verify, error:" + sessionAndRand.getResponseMsg());
        }
    }

    private boolean isDevInfoValid(Bundle bundle) {
        if (bundle != null) {
            return !TextUtils.isEmpty(bundle.getString(DEV_ID));
        }
        DSLog.e("VerifyViaHWMember: devInfo == null.", new Object[0]);
        return false;
    }

    private HttpResponse getSessionAndRand(String str, String str2) {
        String switchUrlEnvironment = switchUrlEnvironment(str2);
        return new HttpClient().newCall(new HttpRequest.Builder().url(switchUrlEnvironment).get(new HttpRequestBody.Builder().add(IVerifyVar.APP_ID, str).build()).build()).syncExecute();
    }

    private String switchUrlEnvironment(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.e(" input url is empty.", new Object[0]);
            return "";
        }
        int indexOf = str.indexOf(47, START_INDEX);
        if (indexOf < 0 || indexOf > str.length()) {
            DSLog.e("VerifyViaHWMember invalid prefix length: " + indexOf, new Object[0]);
            return "";
        }
        String substring = str.substring(0, indexOf);
        return substring + VERIFY_URL_INTERFACE;
    }
}

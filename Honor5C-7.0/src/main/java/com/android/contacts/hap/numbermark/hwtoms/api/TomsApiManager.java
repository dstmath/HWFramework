package com.android.contacts.hap.numbermark.hwtoms.api;

import android.text.TextUtils;
import com.android.contacts.hap.numbermark.hwtoms.model.AccessPath;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestCorrection;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestDetailForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestInfoForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestTelForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseCorrection;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseDetailForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseInfoForHW;
import com.android.contacts.hap.numbermark.hwtoms.model.response.TomsResponseTelForHW;
import com.android.contacts.hap.numbermark.hwtoms.parser.TomsResponseCorrectionParser;
import com.android.contacts.hap.numbermark.hwtoms.parser.TomsResponseDetailForHWParser;
import com.android.contacts.hap.numbermark.hwtoms.parser.TomsResponseInfoForHWParser;
import com.android.contacts.hap.numbermark.hwtoms.parser.TomsResponseTelForHWParser;
import com.android.contacts.hap.numbermark.utils.JsonUtil;
import com.android.contacts.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public final class TomsApiManager implements IApiManager {
    public static final String CHANNAL_NO = "221";
    private static final String JSON_TAG_CORRECTION_ERROR = "error";
    private static final String JSON_TAG_CORRECTION_SUCCESS = "success";
    private static final String JSON_TAG_DATA = "data";
    private static final String JSON_TAG_ERROR_CODE = "errorCode";
    private static final String SERVER_HAS_NO_SUCH_NUM_DATA = "010005";
    private static final String TAG = "TomsApiManager";
    static TomsApiManager infoManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.contacts.hap.numbermark.hwtoms.api.TomsApiManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.contacts.hap.numbermark.hwtoms.api.TomsApiManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.numbermark.hwtoms.api.TomsApiManager.<clinit>():void");
    }

    private TomsApiManager() {
    }

    public static TomsApiManager getInstance() {
        return infoManager;
    }

    public TomsResponseTelForHW telForHuawei(TomsRequestTelForHW request, String appKey) {
        TomsResponseTelForHW telForResponse = null;
        String info = JsonUtil.getInstance().object2Json(request);
        if (info == null) {
            return null;
        }
        String result = AccessPath.getNetworkResposeResult(appKey, info, AccessPath.TEL_FOR_HUAWEI_URL);
        if (AccessPath.CONNECTION_TIMEOUT.equals(result)) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "toms connect timeout");
            }
            return new TomsResponseTelForHW(result);
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            String errorCode = null;
            if (result.contains(JSON_TAG_ERROR_CODE)) {
                errorCode = jsonObject.optString(JSON_TAG_ERROR_CODE);
            }
            if (result.contains(JSON_TAG_DATA)) {
                telForResponse = (TomsResponseTelForHW) new TomsResponseTelForHWParser().listParser(jsonObject.getJSONArray(JSON_TAG_DATA), errorCode).get(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return telForResponse;
    }

    public List<TomsResponseInfoForHW> infoForHuawei(TomsRequestInfoForHW request, String appKey) {
        List<TomsResponseInfoForHW> list = null;
        String info = JsonUtil.getInstance().object2Json(request);
        if (info == null) {
            return null;
        }
        String result = AccessPath.getNetworkResposeResult(appKey, info, AccessPath.INFO_FOR_HUAWEI_URL);
        try {
            JSONObject jsonObject = new JSONObject(result);
            String errorCode = null;
            if (result.contains(JSON_TAG_ERROR_CODE)) {
                errorCode = jsonObject.optString(JSON_TAG_ERROR_CODE);
            }
            if (result.contains(JSON_TAG_DATA)) {
                list = new TomsResponseInfoForHWParser().listParser(jsonObject.getJSONArray(JSON_TAG_DATA), errorCode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TomsResponseDetailForHW> detailForHuawei(TomsRequestDetailForHW request, String appKey) {
        List<TomsResponseDetailForHW> list = null;
        String info = JsonUtil.getInstance().object2Json(request);
        if (info == null) {
            return null;
        }
        String result = AccessPath.getNetworkResposeResult(appKey, info, AccessPath.DETAIL_FOR_HUAWEI_URL);
        try {
            JSONObject jsonObject = new JSONObject(result);
            String str = null;
            if (result.contains(JSON_TAG_ERROR_CODE)) {
                str = jsonObject.optString(JSON_TAG_ERROR_CODE);
                if (!TextUtils.isEmpty(str) && str.equals(SERVER_HAS_NO_SUCH_NUM_DATA)) {
                    return new ArrayList();
                }
            }
            if (result.contains(JSON_TAG_DATA)) {
                list = new TomsResponseDetailForHWParser().listParser(jsonObject.getJSONArray(JSON_TAG_DATA), str);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public TomsResponseCorrection correction(TomsRequestCorrection request, String appKey) {
        TomsResponseCorrection tomsResponseCorrection = null;
        String info = JsonUtil.getInstance().object2Json(request);
        if (info == null) {
            return null;
        }
        String result = AccessPath.getNetworkResposeResult(appKey, info, AccessPath.CORRECTION_URL);
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (result.contains(JSON_TAG_CORRECTION_SUCCESS)) {
                tomsResponseCorrection = new TomsResponseCorrectionParser().objectParser(jsonObject.optString(JSON_TAG_CORRECTION_SUCCESS));
            }
            if (result.contains(JSON_TAG_CORRECTION_ERROR)) {
                tomsResponseCorrection = new TomsResponseCorrectionParser().objectParser(jsonObject.optString(JSON_TAG_CORRECTION_ERROR));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tomsResponseCorrection;
    }
}

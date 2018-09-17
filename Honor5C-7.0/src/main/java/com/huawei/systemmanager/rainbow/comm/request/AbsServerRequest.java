package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.rainbow.comm.request.util.DeviceUtil;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.BasicCloudField;
import org.json.JSONException;
import org.json.JSONObject;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

public abstract class AbsServerRequest extends AbsRequest {
    private static final int MAX_RETRY_TIME = 3;
    private static final String TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest.<clinit>():void");
    }

    protected abstract void addExtPostRequestParam(Context context, JSONObject jSONObject);

    protected abstract int checkResponseCode(Context context, int i);

    protected abstract String getRequestUrl(RequestType requestType);

    protected abstract void parseResponseAndPost(Context context, JSONObject jSONObject) throws JSONException;

    protected void doRequest(Context ctx) {
        Log.d(TAG, "doRequest");
        int retryCount = 0;
        while (retryCount < MAX_RETRY_TIME) {
            if (!innerConnectRequest(ctx)) {
                retryCount++;
            } else {
                return;
            }
        }
        setRequestFailed();
    }

    private boolean innerConnectRequest(Context context) {
        try {
            ICommonRequest conn = JointFactory.getRainbowRequest();
            Preconditions.checkNotNull(conn);
            RequestType type = getRequestType();
            String url = getRequestUrl(type);
            Preconditions.checkNotNull(url);
            String response = "";
            preProcess();
            if (RequestType.REQUEST_GET == type) {
                response = conn.doGetRequest(url);
            } else if (RequestType.REQUEST_POST == type) {
                response = conn.doPostRequest(url, getPostRequestParam(context), getZipEncodingStatus(), context);
            } else {
                Log.w(TAG, "unreachable code");
            }
            return processResponse(context, response);
        } catch (RuntimeException ex) {
            Log.e(TAG, "innerRequest catch RuntimeException: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (Exception ex2) {
            Log.e(TAG, "innerRequest catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
            return false;
        }
    }

    private JSONObject getPostRequestParam(Context context) {
        try {
            JSONObject obj = new JSONObject();
            if (context == null) {
                return obj;
            }
            if (isNeedDefaultParam()) {
                obj.put(BasicCloudField.PHONE_IMEI, DeviceUtil.getTelephoneIMEIFromSys(context));
                obj.put(BasicCloudField.PHONE_TYPE, Build.MODEL);
                obj.put(BasicCloudField.PHONE_OS_VERSION, VERSION.RELEASE);
                obj.put(BasicCloudField.PHONE_SYSTEM, Build.DISPLAY);
                obj.put(BasicCloudField.PHONE_EMUI, DeviceUtil.getTelephoneEMUIVersion());
            }
            addExtPostRequestParam(context, obj);
            Log.i(TAG, "getPostRequestParam success");
            return obj;
        } catch (JSONException ex) {
            Log.e(TAG, "getPostRequestParam catch JSONException: " + ex.getMessage());
            throw new RuntimeException("Failed to prepair JSON request parameters in [getPostRequestParam].");
        } catch (NullPointerException ex2) {
            Log.e(TAG, "getPostRequestParam catch NullPointerException: " + ex2.getMessage());
            throw new RuntimeException("Failed to prepair JSON request parameters in [getPostRequestParam].");
        } catch (Exception ex3) {
            Log.e(TAG, "getPostRequestParam catch Exception: " + ex3.getMessage());
            throw new RuntimeException("Failed to prepair JSON request parameters in [getPostRequestParam].");
        }
    }

    protected boolean processResponse(Context ctx, String response) {
        try {
            Preconditions.checkNotNull(response, "processResponse input response should not be empty!");
            JSONObject jsonResponse = new JSONObject(response);
            int nCheckResult = checkResponseCode(ctx, jsonResponse.getInt(getResultCodeFiled()));
            switch (nCheckResult) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    parseResponseAndPost(ctx, jsonResponse);
                    return true;
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    parseResponseAndPost(ctx, jsonResponse);
                    return false;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    return true;
                case MAX_RETRY_TIME /*3*/:
                    return false;
                default:
                    Log.w(TAG, "processResponse: Invalid response code check result = " + nCheckResult);
                    return false;
            }
        } catch (JSONException ex) {
            Log.e(TAG, "processResponse catch JSONException: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (Exception ex2) {
            Log.e(TAG, "processResponse catch Exception: " + ex2.getMessage());
            return false;
        }
    }

    protected String getResultCodeFiled() {
        return BasicCloudField.SERVRE_RESULT_CODE;
    }

    protected void preProcess() {
    }

    protected RequestType getRequestType() {
        return RequestType.REQUEST_POST;
    }

    protected boolean getZipEncodingStatus() {
        return true;
    }
}

package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.rainbow.comm.request.util.DeviceUtil;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.BasicCloudField;
import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbsServerStreamRequest extends AbsRequest {
    private static final int MAX_RETRY_TIME = 3;
    private static final String TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.systemmanager.rainbow.comm.request.AbsServerStreamRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.systemmanager.rainbow.comm.request.AbsServerStreamRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.comm.request.AbsServerStreamRequest.<clinit>():void");
    }

    protected abstract void addExtPostRequestParam(Context context, JSONObject jSONObject);

    protected abstract int checkResponseCode(Context context, int i);

    protected abstract String getRequestUrl(RequestType requestType);

    protected abstract boolean parseResponseAndPost(Context context, InputStream inputStream) throws JSONException;

    protected void doRequest(Context ctx) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean innerConnectRequest(Context context) {
        InputStream inputStream = null;
        try {
            HsmInputStreamRequest conn = JointFactory.getRainbowInputStreamRequest();
            Preconditions.checkNotNull(conn);
            RequestType type = getRequestType();
            String url = getRequestUrl(type);
            Preconditions.checkNotNull(url);
            preProcess();
            if (RequestType.REQUEST_GET == type) {
                inputStream = conn.doGetRequest(url);
            } else if (RequestType.REQUEST_POST == type) {
                inputStream = conn.doPostRequest(url, getPostRequestParam(context), getZipEncodingStatus(), context);
            } else {
                Log.w(TAG, "unreachable code");
            }
            boolean processResponse = processResponse(context, inputStream);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
            return processResponse;
        } catch (RuntimeException ex) {
            Log.e(TAG, "innerRequest catch RuntimeException: " + ex.getMessage());
            ex.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2) {
                }
            }
        } catch (Exception ex2) {
            Log.e(TAG, "innerRequest catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e3) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                }
            }
        }
    }

    private JSONObject getPostRequestParam(Context context) {
        try {
            Preconditions.checkNotNull(context, "input context is null");
            JSONObject obj = new JSONObject();
            if (isNeedDefaultParam()) {
                obj.put(BasicCloudField.PHONE_IMEI, DeviceUtil.getTelephoneIMEIFromSys(context));
                obj.put(BasicCloudField.PHONE_TYPE, Build.MODEL);
                obj.put(BasicCloudField.PHONE_OS_VERSION, VERSION.RELEASE);
                obj.put(BasicCloudField.PHONE_SYSTEM, Build.DISPLAY);
                obj.put(BasicCloudField.PHONE_EMUI, DeviceUtil.getTelephoneEMUIVersion());
            }
            addExtPostRequestParam(context, obj);
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

    private boolean processResponse(Context ctx, InputStream response) {
        try {
            Preconditions.checkNotNull(response, "processResponse input response should not be empty!");
            return parseResponseAndPost(ctx, response);
        } catch (Exception ex) {
            Log.e(TAG, "processResponse catch Exception: " + ex.getMessage());
            return false;
        }
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

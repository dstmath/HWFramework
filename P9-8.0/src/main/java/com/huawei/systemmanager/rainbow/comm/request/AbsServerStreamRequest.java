package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.rainbow.comm.request.util.DeviceUtil;
import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbsServerStreamRequest extends AbsRequest {
    private static final int MAX_RETRY_TIME = 3;
    private static final String TAG = AbsServerStreamRequest.class.getSimpleName();

    protected abstract void addExtPostRequestParam(Context context, JSONObject jSONObject);

    protected abstract int checkResponseCode(Context context, int i);

    protected abstract String getRequestUrl(RequestType requestType);

    protected abstract boolean parseResponseAndPost(Context context, InputStream inputStream) throws JSONException;

    protected void doRequest(Context ctx) {
        int retryCount = 0;
        while (retryCount < 3) {
            if (!innerConnectRequest(ctx)) {
                retryCount++;
            } else {
                return;
            }
        }
        setRequestFailed();
    }

    private boolean innerConnectRequest(Context context) {
        InputStream response = null;
        try {
            HsmInputStreamRequest conn = JointFactory.getRainbowInputStreamRequest();
            Preconditions.checkNotNull(conn);
            RequestType type = getRequestType();
            String url = getRequestUrl(type);
            Preconditions.checkNotNull(url);
            preProcess();
            if (RequestType.REQUEST_GET == type) {
                response = conn.doGetRequest(url);
            } else if (RequestType.REQUEST_POST == type) {
                response = conn.doPostRequest(url, getPostRequestParam(context), getZipEncodingStatus(), context);
            } else {
                Log.w(TAG, "unreachable code");
            }
            boolean processResponse = processResponse(context, response);
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                }
            }
            return processResponse;
        } catch (RuntimeException ex) {
            Log.e(TAG, "innerRequest catch RuntimeException: " + ex.getMessage());
            ex.printStackTrace();
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e2) {
                }
            }
        } catch (Exception ex2) {
            Log.e(TAG, "innerRequest catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e3) {
                }
            }
        } catch (Throwable th) {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e4) {
                }
            }
        }
        return false;
    }

    private JSONObject getPostRequestParam(Context context) {
        try {
            Preconditions.checkNotNull(context, "input context is null");
            JSONObject obj = new JSONObject();
            if (isNeedDefaultParam()) {
                obj.put("imei", DeviceUtil.getTelephoneIMEIFromSys(context));
                obj.put("model", Build.MODEL);
                obj.put("os", VERSION.RELEASE);
                obj.put("systemid", Build.DISPLAY);
                obj.put("emui", DeviceUtil.getTelephoneEMUIVersion());
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

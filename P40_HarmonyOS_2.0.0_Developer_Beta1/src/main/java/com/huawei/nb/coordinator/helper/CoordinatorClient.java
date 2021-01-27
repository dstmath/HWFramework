package com.huawei.nb.coordinator.helper;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.nb.coordinator.helper.CoordinatorRequest;
import com.huawei.nb.coordinator.helper.http.HttpClient;
import com.huawei.nb.model.coordinator.CoordinatorAudit;
import com.huawei.nb.utils.DeviceUtil;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.nb.utils.reporter.fault.SDKAPIFault;
import java.util.LinkedHashMap;
import java.util.Map;

public class CoordinatorClient {
    private static final int DEFAULT_REQUEST = 0;
    private static final int FRAGMENT_LOAD_REQUEST = 1;
    private static final String TAG = "CoordinatorClient";
    private static final int TRANSFER_FILE_REQUEST = 2;
    private long dataTrafficSize = 0;
    private long delayMs = 0;
    private Bundle devInfo;
    private DataRequestListener listener;
    private String mAppId;
    private String mBusinessType;
    private int mConnectTimeout;
    private Context mContext;
    private String mFileName;
    private String mFileSavePath;
    private String mJsonBody;
    private int mReadTimeout;
    private String mTmpFileDir;
    private String mUrl;
    private int mVerifyMode;
    private Map<String, String> requestBody;
    private Map<String, String> requestHeader;
    private int requestMode;
    private String requestType;
    private int retryTimes = 0;

    public CoordinatorClient(Context context) {
        this.mContext = context;
        this.requestHeader = new LinkedHashMap();
        this.requestBody = new LinkedHashMap();
        this.mVerifyMode = 0;
        this.requestMode = 0;
        this.requestType = HttpClient.GET_TYPE;
    }

    public CoordinatorClient verifyMode(int i) {
        this.mVerifyMode = i;
        return this;
    }

    public CoordinatorClient fileSavePath(String str) {
        this.mFileSavePath = str;
        return this;
    }

    public CoordinatorClient fileName(String str) {
        this.mFileName = str;
        return this;
    }

    public CoordinatorClient appId(String str) {
        this.mAppId = str;
        return this;
    }

    public CoordinatorClient needTransferFile() {
        this.requestMode = 2;
        return this;
    }

    public CoordinatorClient fragmentLoad() {
        this.requestMode = 1;
        return this;
    }

    public CoordinatorClient businessType(String str) {
        this.mBusinessType = str;
        return this;
    }

    public CoordinatorClient url(String str) {
        this.mUrl = str;
        return this;
    }

    public CoordinatorClient connectTimeout(int i) {
        this.mConnectTimeout = i;
        return this;
    }

    public CoordinatorClient readTimeout(int i) {
        this.mReadTimeout = i;
        return this;
    }

    public CoordinatorClient jsonBody(String str) {
        this.mJsonBody = str;
        return this;
    }

    public CoordinatorClient addRequestHeader(String str, String str2) {
        this.requestHeader.put(str, str2);
        return this;
    }

    public CoordinatorClient addRequestBody(String str, String str2) {
        this.requestBody.put(str, str2);
        return this;
    }

    public CoordinatorClient post() {
        this.requestType = HttpClient.POST_TYPE;
        return this;
    }

    public CoordinatorClient get() {
        this.requestType = HttpClient.GET_TYPE;
        return this;
    }

    public CoordinatorClient delete() {
        this.requestType = HttpClient.DELETE_TYPE;
        return this;
    }

    public CoordinatorClient setRetry(int i, int i2) {
        this.retryTimes = i;
        this.delayMs = (long) i2;
        return this;
    }

    public CoordinatorClient setDownloadLimit(long j) {
        this.dataTrafficSize = j;
        return this;
    }

    public CoordinatorClient setDataRequestListener(DataRequestListener dataRequestListener) {
        this.listener = dataRequestListener;
        return this;
    }

    public void sendAsyncRequest() {
        CoordinatorRequest coordinatorRequest = getCoordinatorRequest();
        if (coordinatorRequest != null) {
            coordinatorRequest.sendAsyncHttpRequest();
        } else {
            DSLog.e("CoordinatorClient coordiantorRequest is null. Stop send async request.", new Object[0]);
        }
    }

    public void sendRequest() {
        CoordinatorRequest coordinatorRequest = getCoordinatorRequest();
        if (coordinatorRequest != null) {
            coordinatorRequest.sendHttpRequest();
        } else {
            DSLog.e("CoordinatorClient coordiantorRequest is null. Stop send request.", new Object[0]);
        }
    }

    public CoordinatorClient breakpointResumeDownload(String str) {
        this.requestMode = 3;
        this.mTmpFileDir = str;
        return this;
    }

    public CoordinatorClient setDevInfo(Bundle bundle) {
        this.devInfo = bundle;
        return this;
    }

    private CoordinatorRequest getCoordinatorRequest() {
        CoordinatorRequest.Builder builder = new CoordinatorRequest.Builder(this.mContext);
        builder.appId(this.mAppId);
        builder.connectTimeout(this.mConnectTimeout);
        builder.readTimeout(this.mReadTimeout);
        try {
            builder.url(this.mUrl);
            setRequestType(builder);
            builder.addRequestBody(this.mJsonBody);
            builder.breakpointResumeDownload(this.mTmpFileDir, this.requestMode);
            builder.requestMode(this.requestMode);
            builder.fileSavePath(this.mFileSavePath);
            builder.fileName(this.mFileName);
            builder.businessType(this.mBusinessType);
            Map<String, String> map = this.requestBody;
            if (map != null && map.size() > 0) {
                if (this.mJsonBody != null) {
                    DSLog.i("Both requestBody and JsonBody are not empty. Only JsonBody works.", new Object[0]);
                }
                for (Map.Entry<String, String> entry : this.requestBody.entrySet()) {
                    builder.addRequestBody(entry.getKey(), entry.getValue());
                }
            }
            Map<String, String> map2 = this.requestHeader;
            if (map2 != null && map2.size() > 0) {
                for (Map.Entry<String, String> entry2 : this.requestHeader.entrySet()) {
                    builder.addRequestHeader(entry2.getKey(), entry2.getValue());
                }
            }
            builder.verifyMode(this.mVerifyMode);
            builder.retry(this.retryTimes, this.delayMs);
            builder.dataTrafficSize(this.dataTrafficSize);
            builder.setDevInfo(this.devInfo);
            CoordinatorRequest build = builder.build();
            DataRequestListener dataRequestListener = this.listener;
            if (dataRequestListener == null) {
                DSLog.e(" listener is invalid, request stop.", new Object[0]);
                SDKAPIFault.report("listener is invalid.");
                return null;
            }
            build.setDataRequestListener(dataRequestListener);
            return build;
        } catch (CoordinatorSDKException e) {
            dealInvalidParams(e.getMessage());
            return null;
        }
    }

    private void setRequestType(CoordinatorRequest.Builder builder) throws CoordinatorSDKException {
        if (TextUtils.isEmpty(this.requestType) || !checkRequestType(builder)) {
            throw new CoordinatorSDKException(-2, " request type is invalid.");
        }
    }

    private void dealInvalidParams(String str) {
        DSLog.e("CoordinatorClient " + str, new Object[0]);
        if (this.listener != null) {
            RequestResult requestResult = new RequestResult();
            requestResult.setCode(String.valueOf(-2));
            requestResult.setMessage("fail msg: " + str);
            this.listener.onFailure(requestResult);
        }
        SDKAPIFault.report(" Illegal usage, error: input param is invalid. PackageName: " + this.mContext.getPackageName() + " Version: " + DeviceUtil.getVersionName(this.mContext));
        CoordinatorAudit createCoordinatorAudit = HelperDatabaseManager.createCoordinatorAudit(this.mContext);
        createCoordinatorAudit.setIsRequestSuccess(false);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, createCoordinatorAudit);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0065  */
    private boolean checkRequestType(CoordinatorRequest.Builder builder) {
        char c;
        String str = this.requestType;
        int hashCode = str.hashCode();
        if (hashCode != 70454) {
            if (hashCode != 2461856) {
                if (hashCode == 2012838315 && str.equals(HttpClient.DELETE_TYPE)) {
                    c = 2;
                    if (c != 0) {
                        builder.post();
                    } else if (c == 1) {
                        if (!TextUtils.isEmpty(this.mJsonBody)) {
                            DSLog.e("JsonBody doesn't work when request type is GET.", new Object[0]);
                        }
                        builder.get();
                    } else if (c != 2) {
                        return false;
                    } else {
                        if (!TextUtils.isEmpty(this.mJsonBody)) {
                            DSLog.e("JsonBody doesn't work when request type is DELETE.", new Object[0]);
                        }
                        builder.delete();
                    }
                    return true;
                }
            } else if (str.equals(HttpClient.POST_TYPE)) {
                c = 0;
                if (c != 0) {
                }
                return true;
            }
        } else if (str.equals(HttpClient.GET_TYPE)) {
            c = 1;
            if (c != 0) {
            }
            return true;
        }
        c = 65535;
        if (c != 0) {
        }
        return true;
    }
}

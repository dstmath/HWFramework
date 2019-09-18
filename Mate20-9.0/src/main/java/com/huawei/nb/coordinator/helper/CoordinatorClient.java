package com.huawei.nb.coordinator.helper;

import android.content.Context;
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

    public CoordinatorClient verifyMode(int verifyMode) {
        this.mVerifyMode = verifyMode;
        return this;
    }

    public CoordinatorClient fileSavePath(String fileSavePath) {
        this.mFileSavePath = fileSavePath;
        return this;
    }

    public CoordinatorClient fileName(String fileName) {
        this.mFileName = fileName;
        return this;
    }

    public CoordinatorClient appId(String appId) {
        this.mAppId = appId;
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

    public CoordinatorClient businessType(String businessType) {
        this.mBusinessType = businessType;
        return this;
    }

    public CoordinatorClient url(String url) {
        this.mUrl = url;
        return this;
    }

    public CoordinatorClient connectTimeout(int connectTimeout) {
        this.mConnectTimeout = connectTimeout;
        return this;
    }

    public CoordinatorClient readTimeout(int readTimeout) {
        this.mReadTimeout = readTimeout;
        return this;
    }

    public CoordinatorClient jsonBody(String jsonBody) {
        this.mJsonBody = jsonBody;
        return this;
    }

    public CoordinatorClient addRequestHeader(String key, String value) {
        this.requestHeader.put(key, value);
        return this;
    }

    public CoordinatorClient addRequestBody(String key, String value) {
        this.requestBody.put(key, value);
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

    public CoordinatorClient setRetry(int retryTimes2, int delayMs2) {
        this.retryTimes = retryTimes2;
        this.delayMs = (long) delayMs2;
        return this;
    }

    public CoordinatorClient setDownloadLimit(long dataTrafficSize2) {
        this.dataTrafficSize = dataTrafficSize2;
        return this;
    }

    public CoordinatorClient setDataRequestListener(DataRequestListener listener2) {
        this.listener = listener2;
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

    public CoordinatorClient breakpointResumeDownload(String tmpFileDir) {
        this.requestMode = 3;
        this.mTmpFileDir = tmpFileDir;
        return this;
    }

    private CoordinatorRequest getCoordinatorRequest() {
        CoordinatorRequest.Builder requestBuilder = new CoordinatorRequest.Builder(this.mContext);
        requestBuilder.appId(this.mAppId);
        requestBuilder.connectTimeout(this.mConnectTimeout);
        requestBuilder.readTimeout(this.mReadTimeout);
        try {
            requestBuilder.url(this.mUrl);
            setRequestType(requestBuilder);
            requestBuilder.addRequestBody(this.mJsonBody);
            requestBuilder.breakpointResumeDownload(this.mTmpFileDir, this.requestMode);
            requestBuilder.requestMode(this.requestMode);
            requestBuilder.fileSavePath(this.mFileSavePath);
            requestBuilder.fileName(this.mFileName);
            requestBuilder.businessType(this.mBusinessType);
            if (this.requestBody != null && this.requestBody.size() > 0) {
                if (this.mJsonBody != null) {
                    DSLog.i("Both requestBody and JsonBody are not empty. Only JsonBody works.", new Object[0]);
                }
                for (Map.Entry entry : this.requestBody.entrySet()) {
                    requestBuilder.addRequestBody((String) entry.getKey(), (String) entry.getValue());
                }
            }
            if (this.requestHeader != null && this.requestHeader.size() > 0) {
                for (Map.Entry entry2 : this.requestHeader.entrySet()) {
                    requestBuilder.addRequestHeader((String) entry2.getKey(), (String) entry2.getValue());
                }
            }
            requestBuilder.verifyMode(this.mVerifyMode);
            requestBuilder.retry(this.retryTimes, this.delayMs);
            requestBuilder.dataTrafficSize(this.dataTrafficSize);
            CoordinatorRequest request = requestBuilder.build();
            if (this.listener == null) {
                DSLog.e(" listener is invalid, request stop.", new Object[0]);
                SDKAPIFault.report("listener is invalid.");
                return null;
            }
            request.setDataRequestListener(this.listener);
            return request;
        } catch (CoordinatorSDKException e) {
            dealInvalidParams(e.getMessage());
            return null;
        }
    }

    private void setRequestType(CoordinatorRequest.Builder requestBuilder) throws CoordinatorSDKException {
        if (TextUtils.isEmpty(this.requestType) || !checkRequestType(requestBuilder)) {
            throw new CoordinatorSDKException(-2, " request type is invalid.");
        }
    }

    private void dealInvalidParams(String msg) {
        DSLog.e("CoordinatorClient " + msg, new Object[0]);
        if (this.listener != null) {
            RequestResult requestResult = new RequestResult();
            requestResult.setCode(String.valueOf(-2));
            requestResult.setMessage("fail msg: " + msg);
            this.listener.onFailure(requestResult);
        }
        SDKAPIFault.report(" Illegal usage, error: input param is invalid." + " PackageName: " + this.mContext.getPackageName() + " Version: " + DeviceUtil.getVersionName(this.mContext));
        CoordinatorAudit coordinatorAudit = HelperDatabaseManager.createCoordinatorAudit(this.mContext);
        coordinatorAudit.setIsRequestSuccess(false);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, coordinatorAudit);
    }

    private boolean checkRequestType(CoordinatorRequest.Builder requestBuilder) {
        String str = this.requestType;
        char c = 65535;
        switch (str.hashCode()) {
            case 70454:
                if (str.equals(HttpClient.GET_TYPE)) {
                    c = 1;
                    break;
                }
                break;
            case 2461856:
                if (str.equals(HttpClient.POST_TYPE)) {
                    c = 0;
                    break;
                }
                break;
            case 2012838315:
                if (str.equals(HttpClient.DELETE_TYPE)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                requestBuilder.post();
                break;
            case 1:
                if (!TextUtils.isEmpty(this.mJsonBody)) {
                    DSLog.e("JsonBody doesn't work when request type is GET.", new Object[0]);
                }
                requestBuilder.get();
                break;
            case 2:
                if (!TextUtils.isEmpty(this.mJsonBody)) {
                    DSLog.e("JsonBody doesn't work when request type is DELETE.", new Object[0]);
                }
                requestBuilder.delete();
                break;
            default:
                return false;
        }
        return true;
    }
}

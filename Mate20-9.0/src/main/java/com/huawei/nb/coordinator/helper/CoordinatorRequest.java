package com.huawei.nb.coordinator.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import com.huawei.nb.coordinator.NetWorkStateUtil;
import com.huawei.nb.coordinator.common.CoordinatorJsonAnalyzer;
import com.huawei.nb.coordinator.helper.http.HttpClient;
import com.huawei.nb.coordinator.helper.http.HttpRequest;
import com.huawei.nb.coordinator.helper.http.HttpRequestBody;
import com.huawei.nb.coordinator.helper.http.HttpResponse;
import com.huawei.nb.coordinator.helper.http.ResultStatusCodeEnum;
import com.huawei.nb.coordinator.helper.verify.IVerify;
import com.huawei.nb.coordinator.helper.verify.IVerifyVar;
import com.huawei.nb.coordinator.helper.verify.VerifyException;
import com.huawei.nb.coordinator.helper.verify.VerifyFactory;
import com.huawei.nb.coordinator.helper.verify.VerifyInfoHolder;
import com.huawei.nb.coordinator.helper.verify.VerifyInfoHolderFactory;
import com.huawei.nb.model.coordinator.CoordinatorAudit;
import com.huawei.nb.service.BuildConfig;
import com.huawei.nb.utils.DeviceUtil;
import com.huawei.nb.utils.JsonUtils;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.nb.utils.reporter.audit.CoordinatorCloudErrorAudit;
import com.huawei.nb.utils.reporter.audit.CoordinatorNetworkErrorAudit;
import com.huawei.nb.utils.reporter.fault.DownloadFault;
import com.huawei.nb.utils.reporter.fault.HttpsFault;
import com.huawei.nb.utils.reporter.fault.SDKAPIFault;
import java.util.concurrent.TimeUnit;

final class CoordinatorRequest {
    private static final int DEFAULT = 3;
    private static final long MAX_DEFAULT_SIZE = 524288000;
    private static final int MAX_TIMES = 3;
    private static final int MODEL_UPGRADE = 2;
    private static final int REQUEST_SUCCESS = 200;
    private static final String TAG = "CoordinatorRequest";
    private static final int TRAVEL_ASSISTANT = 1;
    private static final int UNKNOWN = 4;
    /* access modifiers changed from: private */
    public String mAppId;
    /* access modifiers changed from: private */
    public String mBusinessType;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public long mDataTrafficSize;
    /* access modifiers changed from: private */
    public long mDelayMs;
    /* access modifiers changed from: private */
    public String mFileName;
    /* access modifiers changed from: private */
    public String mFileSavePath;
    private DataRequestListener mListener;
    /* access modifiers changed from: private */
    public HttpRequestBody.Builder mRequestBodyBuilder;
    /* access modifiers changed from: private */
    public HttpRequest.Builder mRequestBuilder;
    /* access modifiers changed from: private */
    public int mRequestMode;
    private RequestResult mRequestResult;
    /* access modifiers changed from: private */
    public int mRetryTimes;
    /* access modifiers changed from: private */
    public String mTmpFileDir;
    /* access modifiers changed from: private */
    public VerifyInfoHolder mVerifyInfoHolder;
    /* access modifiers changed from: private */
    public String requestType;
    private IVerify verify;

    public static class Builder {
        private CoordinatorRequest mCoordinatorRequest = new CoordinatorRequest();

        Builder(Context context) {
            Context unused = this.mCoordinatorRequest.mContext = context;
        }

        public Builder get() {
            String unused = this.mCoordinatorRequest.requestType = HttpClient.GET_TYPE;
            return this;
        }

        public Builder appId(String appId) {
            if (!TextUtils.isEmpty(appId)) {
                String unused = this.mCoordinatorRequest.mAppId = appId;
            }
            return this;
        }

        public Builder post() {
            String unused = this.mCoordinatorRequest.requestType = HttpClient.POST_TYPE;
            return this;
        }

        public Builder delete() {
            String unused = this.mCoordinatorRequest.requestType = HttpClient.DELETE_TYPE;
            return this;
        }

        public Builder url(String url) throws CoordinatorSDKException {
            if (!TextUtils.isEmpty(url)) {
                this.mCoordinatorRequest.mRequestBuilder.url(url);
                return this;
            }
            throw new CoordinatorSDKException(-2, " url is empty.");
        }

        public Builder connectTimeout(int connectTimeout) {
            if (connectTimeout > 0) {
                this.mCoordinatorRequest.mRequestBuilder.connectTimeout(connectTimeout);
            }
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            if (readTimeout > 0) {
                this.mCoordinatorRequest.mRequestBuilder.readTimeout(readTimeout);
            }
            return this;
        }

        public Builder verifyMode(int inputVerifyMode) {
            int verifyMode;
            if (inputVerifyMode == 1) {
                verifyMode = 0;
            } else {
                verifyMode = inputVerifyMode;
            }
            VerifyInfoHolder unused = this.mCoordinatorRequest.mVerifyInfoHolder = VerifyInfoHolderFactory.getVerifyInfoHolder(verifyMode);
            return this;
        }

        public Builder addRequestHeader(String key, String value) {
            this.mCoordinatorRequest.mRequestBuilder.addRequestHeader(key, value);
            return this;
        }

        public Builder businessType(String businessType) {
            if (!TextUtils.isEmpty(businessType)) {
                String unused = this.mCoordinatorRequest.mBusinessType = businessType;
            } else {
                DSLog.d(" BusinessType is empty.", new Object[0]);
            }
            return this;
        }

        public Builder fileSavePath(String fileSavePath) {
            if (!TextUtils.isEmpty(fileSavePath)) {
                String unused = this.mCoordinatorRequest.mFileSavePath = fileSavePath;
            }
            return this;
        }

        public Builder fileName(String fileName) {
            if (!TextUtils.isEmpty(fileName)) {
                String unused = this.mCoordinatorRequest.mFileName = fileName;
            }
            return this;
        }

        public Builder addRequestBody(String key, String value) {
            this.mCoordinatorRequest.mRequestBodyBuilder.add(key, value);
            return this;
        }

        public Builder addRequestBody(String json) throws CoordinatorSDKException {
            if (!TextUtils.isEmpty(json)) {
                if (!JsonUtils.isValidJson(json)) {
                    throw new CoordinatorSDKException(-2, " JsonBody is invalid.");
                }
                this.mCoordinatorRequest.mRequestBodyBuilder.addJsonBody(json);
            }
            return this;
        }

        public Builder requestMode(int requestMode) {
            int unused = this.mCoordinatorRequest.mRequestMode = requestMode;
            return this;
        }

        public Builder retry(int inputRetryTimes, long inputDelayMs) {
            int retryTimes = inputRetryTimes;
            long delayMs = inputDelayMs;
            if (retryTimes <= 0) {
                DSLog.i("CoordinatorRequest retryTimes is illegal. Reset to 3 times.", new Object[0]);
                retryTimes = 3;
            }
            if (delayMs < 0) {
                DSLog.i("CoordinatorRequest delayMs is illegal. Reset to 0 ms.", new Object[0]);
                delayMs = 0;
            }
            int unused = this.mCoordinatorRequest.mRetryTimes = retryTimes;
            long unused2 = this.mCoordinatorRequest.mDelayMs = delayMs;
            return this;
        }

        public Builder dataTrafficSize(long inputDataTrafficSize) {
            long dataTrafficSize = inputDataTrafficSize;
            DSLog.d("CoordinatorRequest DataTrafficSize is " + dataTrafficSize, new Object[0]);
            if (dataTrafficSize <= 0) {
                DSLog.d("CoordinatorRequest DataTrafficSize is " + dataTrafficSize + ", reset to " + CoordinatorRequest.MAX_DEFAULT_SIZE, new Object[0]);
                dataTrafficSize = CoordinatorRequest.MAX_DEFAULT_SIZE;
            }
            long unused = this.mCoordinatorRequest.mDataTrafficSize = dataTrafficSize;
            return this;
        }

        public Builder breakpointResumeDownload(String tmpFileDir, int requestMode) throws CoordinatorSDKException {
            if (requestMode == 3) {
                if (TextUtils.isEmpty(tmpFileDir)) {
                    throw new CoordinatorSDKException(-2, " request mode is breakpoint-resume-download, but tmp file dir is empty");
                }
                DSLog.d(CoordinatorRequest.TAG, " allow to resume downloading from breakpoint.");
                String unused = this.mCoordinatorRequest.mTmpFileDir = tmpFileDir;
            }
            return this;
        }

        public CoordinatorRequest build() {
            return this.mCoordinatorRequest;
        }
    }

    private CoordinatorRequest() {
        this.mAppId = IVerifyVar.APPID_VALUE;
        this.mBusinessType = BusinessTypeEnum.BIZ_TYPE_POLICY;
        this.requestType = HttpClient.GET_TYPE;
        this.mVerifyInfoHolder = VerifyInfoHolderFactory.getVerifyInfoHolder(0);
        this.mRequestBodyBuilder = new HttpRequestBody.Builder();
        this.mRequestBuilder = new HttpRequest.Builder();
        this.mRequestResult = new RequestResult();
    }

    public CoordinatorRequest setDataRequestListener(DataRequestListener callbackListener) {
        this.mListener = callbackListener;
        return this;
    }

    public void sendAsyncHttpRequest() {
        Thread requestThread = new Thread(new CoordinatorRequest$$Lambda$0(this), "AsyncHttpRequest");
        requestThread.setUncaughtExceptionHandler(new CoordinatorRequest$$Lambda$1(this));
        requestThread.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: uncaughtException */
    public void bridge$lambda$1$CoordinatorRequest(Thread thread, Throwable throwable) {
        DSLog.e("CoordinatorRequest catch throwable when set async http request, thread " + thread.getId() + " err msg: " + throwable.getMessage(), new Object[0]);
    }

    public void sendHttpRequest() {
        bridge$lambda$0$CoordinatorRequest();
    }

    private void setRequestErrorInfo(String code, String errorMsg) {
        if (this.mRequestResult != null) {
            this.mRequestResult.setCode(code);
            this.mRequestResult.setDesc(errorMsg);
            this.mRequestResult.setMessage(errorMsg);
            if (this.mListener != null) {
                this.mListener.onFailure(this.mRequestResult);
            }
        }
        CoordinatorAudit coordinatorAudit = HelperDatabaseManager.createCoordinatorAudit(this.mContext);
        coordinatorAudit.setIsRequestSuccess(false);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, coordinatorAudit);
        DSLog.e(TAG + errorMsg, new Object[0]);
    }

    private HttpResponse errResponse(int code, String errorMsg) {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(code);
        response.setResponseMsg(errorMsg);
        return response;
    }

    /* access modifiers changed from: private */
    /* renamed from: request */
    public void bridge$lambda$0$CoordinatorRequest() {
        try {
            printNetworkInfo();
            if (!isServiceSwitchOn()) {
                setRequestErrorInfo(String.valueOf(-4), " CoordinatorService is not allowed to start! ");
            } else if (!isOversea() || isAllowedOversea()) {
                HttpRequestBody requestBody = this.mRequestBodyBuilder.build();
                if (HttpClient.GET_TYPE.contentEquals(this.requestType)) {
                    this.mRequestBuilder.get(requestBody);
                } else if (HttpClient.POST_TYPE.contentEquals(this.requestType)) {
                    this.mRequestBuilder.post(requestBody);
                } else if (HttpClient.DELETE_TYPE.contentEquals(this.requestType)) {
                    this.mRequestBuilder.delete(requestBody);
                } else {
                    DSLog.d("CoordinatorRequest request type is invalid.", new Object[0]);
                }
                int verifyMode = this.mVerifyInfoHolder.getVerifyMode();
                DSLog.d("CoordinatorRequest verify mode is " + verifyMode, new Object[0]);
                this.verify = VerifyFactory.getVerify(verifyMode);
                requestWithHttp(this.mRequestBuilder);
            } else {
                setRequestErrorInfo(String.valueOf(-14), " District is oversea, request prohibited.");
            }
        } catch (Throwable throwable) {
            setRequestErrorInfo("code-1", "CoordinatorRequest caught a throwable,message: " + throwable.getMessage() + ", cause:" + throwable.getCause());
        }
    }

    private boolean isAllowedOversea() {
        return false;
    }

    private boolean isOversea() {
        return DeviceUtil.getDistrict().equals("Oversea");
    }

    private boolean isServiceSwitchOn() {
        if (1 == getBusiness()) {
            String packageName = this.mContext.getPackageName();
            DSLog.i("CoordinatorRequest Travel assistant business. the request is from : " + packageName, new Object[0]);
            if (TextUtils.isEmpty(packageName) || BuildConfig.APPLICATION_ID.equals(packageName)) {
                if (!BuildConfig.APPLICATION_ID.equals(packageName)) {
                    return false;
                }
                DSLog.d("CoordinatorRequest From ODMF inside, switch is checked.", new Object[0]);
                return true;
            } else if (!HelperDatabaseManager.getCoordinatorServiceFlag(this.mContext)) {
                DSLog.i("CoordinatorRequest SWITCH_OFF ", new Object[0]);
                return false;
            } else {
                DSLog.i("CoordinatorRequest SWITCH_ON ", new Object[0]);
                return true;
            }
        } else if (3 == getBusiness() || 2 == getBusiness()) {
            return true;
        } else {
            DSLog.e("CoordinatorRequest Not a valid business.", new Object[0]);
            return false;
        }
    }

    private void printNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                int type = networkInfo.getType();
                NetworkInfo.State state = networkInfo.getState();
                if (state != null) {
                    DSLog.d("CoordinatorRequest Network Type:" + type + ",Network State:" + state.toString(), new Object[0]);
                }
            }
        }
    }

    private void requestWithHttp(HttpRequest.Builder builder) {
        HttpResponse result = null;
        DSLog.d("CoordinatorRequest Request with https.", new Object[0]);
        CoordinatorAudit coordinatorAudit = HelperDatabaseManager.createCoordinatorAudit(this.mContext);
        boolean isTokenExpired = false;
        int lastNetworkType = NetWorkStateUtil.getCurrentNetWorkType(this.mContext);
        int i = 1;
        while (true) {
            if (i <= this.mRetryTimes) {
                if (this.mVerifyInfoHolder.isHasToken()) {
                    String verifyToken = this.mVerifyInfoHolder.getVerifyToken();
                    if (TextUtils.isEmpty(verifyToken) || this.mVerifyInfoHolder.isTokenExpired() || isTokenExpired) {
                        result = requestWithAuth(builder, coordinatorAudit);
                    } else {
                        result = requestWithToken(builder, verifyToken, coordinatorAudit);
                    }
                } else {
                    result = requestWithAuth(builder, coordinatorAudit);
                }
                if (!isSuccessCode(result)) {
                    isTokenExpired = isTokenOrSessionExpired(result);
                    if (result.getStatusCode() != -7) {
                        DSLog.e("CoordinatorRequest Need to retry " + this.mRetryTimes + " times. This is the " + i + " time.", new Object[0]);
                        try {
                            TimeUnit.MILLISECONDS.sleep(this.mDelayMs);
                        } catch (InterruptedException e) {
                            DSLog.e("CoordinatorRequest retry delay is interrupted.", new Object[0]);
                        }
                        if (!isRetryAllowed(lastNetworkType, result)) {
                            break;
                        }
                        lastNetworkType = NetWorkStateUtil.getCurrentNetWorkType(this.mContext);
                        i++;
                    } else {
                        DSLog.e("CoordinatorRequest Download was interrupted, stop retry!", new Object[0]);
                        break;
                    }
                } else {
                    if (i == 1) {
                        coordinatorAudit.setIsNeedRetry(0L);
                    } else {
                        coordinatorAudit.setIsNeedRetry(1L);
                    }
                    onRequestSuccess(result, result.getResponseString(), coordinatorAudit);
                    return;
                }
            } else {
                break;
            }
        }
        if (result != null) {
            onRequestFailure(result, result.getResponseString(), coordinatorAudit);
        } else {
            setRequestErrorInfo("code = -2", " Fail to get request result, error: result is null.");
        }
    }

    private HttpResponse requestWithAuth(HttpRequest.Builder builder, CoordinatorAudit coordinatorAudit) {
        DSLog.d("CoordinatorRequest without token, generate authorization from the beginning.", new Object[0]);
        try {
            if (!this.verify.generateAuthorization(this.mContext, builder, this.mAppId)) {
                return errResponse(-3, " verify error.");
            }
            coordinatorAudit.setSuccessVerifyTime(Long.valueOf(System.currentTimeMillis()));
            if (builder == null) {
                return errResponse(-2, "empty requestWithAuthenticate.");
            }
            DSLog.d("CoordinatorRequestrequest WithAuthenticate.", new Object[0]);
            HttpResponse response = getRequestResult(builder, coordinatorAudit);
            if (1 == getBusiness()) {
                String code = CoordinatorJsonAnalyzer.getJsonValue(response.getResponseString(), CoordinatorJsonAnalyzer.CODE_TYPE);
                if (isSuccessCode(response)) {
                    return response;
                }
                if (TextUtils.isEmpty(code) || (!code.equals(IVerifyVar.CLOUD_DEVICE_CA_ERROR) && !code.equals(IVerifyVar.SESSION_HAS_EXPIRED))) {
                    DSLog.e("CoordinatorRequest: requestWithAuth failed this time. code: " + response.getStatusCode() + "msg:" + response.getResponseMsg(), new Object[0]);
                    return response;
                }
                try {
                    if (!this.verify.generateAuthorization(this.mContext, builder, this.mAppId)) {
                        return errResponse(-3, " verify error.");
                    }
                    coordinatorAudit.setSuccessVerifyTime(Long.valueOf(System.currentTimeMillis()));
                    HttpResponse response2 = getRequestResult(builder, coordinatorAudit);
                    this.mVerifyInfoHolder.setDeviceCASentFlag(false);
                    return response2;
                } catch (VerifyException e) {
                    return errResponse(e.getCode(), e.getMsg());
                }
            } else {
                if (2 == getBusiness()) {
                }
                return response;
            }
        } catch (VerifyException e2) {
            return errResponse(e2.getCode(), e2.getMsg());
        }
    }

    private boolean isSuccessCode(HttpResponse response) {
        boolean z = false;
        String responseString = response.getResponseString();
        int statusCode = response.getStatusCode();
        if (1 == getBusiness()) {
            if (this.mRequestMode != 0) {
                if (200 == statusCode) {
                    return true;
                }
                return false;
            } else if (TextUtils.isEmpty(responseString)) {
                DSLog.d("CoordinatorRequest response string is empty.", new Object[0]);
                return false;
            } else {
                String expectedStatusCode = getStatusCode(this.mBusinessType);
                String code = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.CODE_TYPE);
                String msg = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.MSG_TYPE);
                DSLog.d("CoordinatorRequest Response code is " + code, " msg is" + msg);
                if (TextUtils.isEmpty(expectedStatusCode) || TextUtils.isEmpty(code) || !code.equals(expectedStatusCode)) {
                    return false;
                }
                return true;
            }
        } else if (this.mRequestMode == 3) {
            DSLog.d("CoordinatorRequest request mode is TRANSFER_FILE_BREAKPOINT, status code = " + statusCode, new Object[0]);
            if (200 == statusCode || 206 == statusCode) {
                z = true;
            }
            return z;
        } else {
            DSLog.d("CoordinatorRequest default situation, status code = " + statusCode, new Object[0]);
            if (200 != statusCode) {
                return false;
            }
            return true;
        }
    }

    private String getStatusCode(String businessType) {
        if (BusinessTypeEnum.BIZ_TYPE_POLICY.equals(businessType)) {
            return "PS200";
        }
        if (BusinessTypeEnum.BIZ_TYPE_FENCE.equals(businessType)) {
            return "FS20002";
        }
        if (BusinessTypeEnum.BIZ_TYPE_SMART_TRAVEL.equals(businessType)) {
            return "ST20000";
        }
        if (BusinessTypeEnum.BIZ_TYPE_APPLET.equals(businessType)) {
            return "AL20000";
        }
        if (BusinessTypeEnum.BIZ_TYPE_PUSH.equals(businessType)) {
            return "PU20000";
        }
        if (BusinessTypeEnum.BIZ_TYPE_SCENE_PACKAGE.equals(businessType)) {
            return "SP20000";
        }
        if (BusinessTypeEnum.BIZ_TYPE_COLLECT_POLICY.equals(businessType)) {
            return "CP20000";
        }
        return "200";
    }

    private HttpResponse getRequestResult(HttpRequest.Builder requestBuilder, CoordinatorAudit coordinatorAudit) {
        HttpRequest httpRequest = requestBuilder.build();
        coordinatorAudit.setUrl(httpRequest.getUrl());
        return new HttpClient(this.mRequestMode, this.mFileSavePath, this.mFileName).newCall(httpRequest).setDataRequestListener(this.mListener).setDataTrafficSize(Long.valueOf(this.mDataTrafficSize)).setTmpFileDir(this.mTmpFileDir).syncExecute(coordinatorAudit);
    }

    private void onRequestSuccess(HttpResponse response, String responseString, CoordinatorAudit coordinatorAudit) {
        DSLog.d("CoordinatorRequest business type:" + this.mBusinessType + " requests successfully.", new Object[0]);
        if (this.mVerifyInfoHolder.isHasToken() && this.mVerifyInfoHolder.isTokenExpired()) {
            this.mVerifyInfoHolder.updateToken(response, this.verify);
        }
        if (this.mListener != null) {
            String data = responseString;
            if (1 == getBusiness()) {
                data = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.DATA_TYPE);
            } else if (2 == getBusiness()) {
                data = responseString;
            } else {
                DSLog.d("CoordinatorRequest other business requests successfully.", new Object[0]);
            }
            this.mListener.onSuccess(data);
        }
        long currentTimeMillis = System.currentTimeMillis();
        coordinatorAudit.setDataSize(Long.valueOf(response.getResponseSize()));
        coordinatorAudit.setSuccessTransferTime(Long.valueOf(currentTimeMillis));
        coordinatorAudit.setIsRequestSuccess(true);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, coordinatorAudit);
    }

    private void onRequestFailure(HttpResponse httpResponse, String responseString, CoordinatorAudit coordinatorAudit) {
        DSLog.e("CoordinatorRequest business type:" + this.mBusinessType + " request failure.", new Object[0]);
        boolean isShieldCloudError = false;
        if (1 == getBusiness()) {
            String code = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.CODE_TYPE);
            if (!TextUtils.isEmpty(code)) {
                setFailRequestResult(code, CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.DESC_TYPE), CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.MSG_TYPE));
                isShieldCloudError = isShieldCloudError(code);
            } else {
                setFailRequestResult(String.valueOf(httpResponse.getStatusCode()), httpResponse.getResponseMsg(), httpResponse.getHttpExceptionMsg());
            }
        } else {
            setFailRequestResult(String.valueOf(httpResponse.getStatusCode()), httpResponse.getResponseMsg(), httpResponse.getHttpExceptionMsg());
        }
        this.mRequestResult.setUrl(httpResponse.getUrl());
        if (this.mListener != null) {
            this.mListener.onFailure(this.mRequestResult);
        } else {
            DSLog.e("CoordinatorRequest Fail to callback, error: listener is null.", new Object[0]);
        }
        failResultReport(httpResponse, isShieldCloudError, coordinatorAudit);
    }

    private void setFailRequestResult(String code, String desc, String msg) {
        this.mRequestResult.setCode(code);
        this.mRequestResult.setDesc(desc);
        this.mRequestResult.setMessage(msg);
    }

    private void failResultReport(HttpResponse httpResponse, boolean isShieldCloudError, CoordinatorAudit coordinatorAudit) {
        String errDetail = generateErrDetail(httpResponse);
        switch (getErrorType(httpResponse, isShieldCloudError)) {
            case ResultStatusCodeEnum.TIME_OUT:
                DSLog.e("CoordinatorRequestExecute response timeout, audit report.", new Object[0]);
                CoordinatorNetworkErrorAudit.report(String.valueOf(httpResponse.getStatusCode()), httpResponse.getResponseMsg(), coordinatorAudit.getAppPackageName(), coordinatorAudit.getUrl(), coordinatorAudit.getNetWorkState(), coordinatorAudit.getRequestDate());
                break;
            case ResultStatusCodeEnum.GET_SIGNATURE_ERROR:
                DSLog.e("CoordinatorRequest Can not get signature, shield report.", new Object[0]);
                break;
            case ResultStatusCodeEnum.NETWORK_FAULT:
                DSLog.e("CoordinatorRequest Network is not connected, audit report.", new Object[0]);
                CoordinatorNetworkErrorAudit.report(String.valueOf(httpResponse.getStatusCode()), httpResponse.getResponseMsg(), coordinatorAudit.getAppPackageName(), coordinatorAudit.getUrl(), coordinatorAudit.getNetWorkState(), coordinatorAudit.getRequestDate());
                break;
            case ResultStatusCodeEnum.SHIELD_CLOUD_ERROR:
                DSLog.e("CoordinatorRequest Cloud error, audit report.", new Object[0]);
                CoordinatorCloudErrorAudit.report(String.valueOf(httpResponse.getStatusCode()), httpResponse.getHttpExceptionMsg(), this.mBusinessType, httpResponse.getResponseMsg(), coordinatorAudit.getAppPackageName(), coordinatorAudit.getUrl(), coordinatorAudit.getNetWorkState(), coordinatorAudit.getRequestDate());
                break;
            case ResultStatusCodeEnum.CONNECT_CLOUD_ERROR:
                DSLog.e("CoordinatorRequestFailed to connect to cloud, audit report.", new Object[0]);
                CoordinatorNetworkErrorAudit.report(String.valueOf(httpResponse.getStatusCode()), httpResponse.getResponseMsg(), coordinatorAudit.getAppPackageName(), coordinatorAudit.getUrl(), coordinatorAudit.getNetWorkState(), coordinatorAudit.getRequestDate());
                break;
            case ResultStatusCodeEnum.DOWNLOAD_ERROR:
                DownloadFault.report(errDetail);
                break;
            case ResultStatusCodeEnum.VERIFICATION_ERROR_CODE:
                DSLog.e("CoordinatorRequest verify error, audit report.", new Object[0]);
                break;
            case ResultStatusCodeEnum.INVALID_PARAMS_CODE:
                SDKAPIFault.report(errDetail);
                break;
            default:
                HttpsFault.report(errDetail);
                break;
        }
        coordinatorAudit.setIsRequestSuccess(false);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, coordinatorAudit);
    }

    private String generateErrDetail(HttpResponse httpResponse) {
        return " Cause: " + httpResponse.getResponseMsg() + ", Code:" + httpResponse.getStatusCode() + ", Network:" + NetWorkStateUtil.getCurrentNetWorkType(this.mContext) + ", URL: " + httpResponse.getUrl() + ", PackageName" + this.mContext.getPackageName() + ", Version:" + DeviceUtil.getVersionName(this.mContext);
    }

    private int getErrorType(HttpResponse httpResponse, boolean isShieldCloudError) {
        if (isShieldCloudError) {
            return -10;
        }
        if (isNetworkFault(httpResponse)) {
            return -11;
        }
        if (isEmptySignatureFault(httpResponse)) {
            return -12;
        }
        return httpResponse.getStatusCode();
    }

    private boolean isShieldCloudError(String rawCode) {
        String code = rawCode.trim();
        return IVerifyVar.PUBLICKEY_NOT_EXIST.equals(code) || IVerifyVar.CAN_NOT_CONNECT_TO_LDAP_SERVER.equals(code) || IVerifyVar.PUBLICKEY_NOT_EXIST_IN_DCS.equals(code) || IVerifyVar.PUBLICKEY_FORMAT_ERROR.equals(code) || IVerifyVar.DYNAMIC_CARD_INFO_NULL.equals(code);
    }

    private HttpResponse requestWithToken(HttpRequest.Builder builder, String verifyToken, CoordinatorAudit coordinatorAudit) {
        DSLog.d("CoordinatorRequest ready to request with token", new Object[0]);
        HttpRequest request = builder.addRequestHeader(IVerifyVar.AUTHORIZATION_KEY, "PKI " + IVerifyVar.TOKEN_KEY + "=" + verifyToken).build();
        coordinatorAudit.setUrl(request.getUrl());
        return new HttpClient(this.mRequestMode, this.mFileSavePath, this.mFileName).newCall(request).setDataTrafficSize(Long.valueOf(this.mDataTrafficSize)).setDataRequestListener(this.mListener).syncExecute();
    }

    private boolean isTokenOrSessionExpired(HttpResponse result) {
        String responseString = result.getResponseString();
        if (1 != getBusiness()) {
            return false;
        }
        String code = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.CODE_TYPE).trim();
        if (TextUtils.isEmpty(code)) {
            return false;
        }
        if (IVerifyVar.TOKEN_HAS_EXPIRED.equals(code) || IVerifyVar.SESSION_HAS_EXPIRED.equals(code)) {
            return true;
        }
        return false;
    }

    private boolean isRetryAllowed(int lastNetworkType, HttpResponse result) {
        int netWorkState = NetWorkStateUtil.getCurrentNetWorkType(this.mContext);
        if (netWorkState == 1) {
            return true;
        }
        if (netWorkState != 3 || lastNetworkType != 3) {
            return false;
        }
        if (result.isDownloadStart()) {
            return false;
        }
        return true;
    }

    private boolean isNetworkFault(HttpResponse httpResponse) {
        return httpResponse.getStatusCode() == -6;
    }

    private boolean isEmptySignatureFault(HttpResponse httpResponse) {
        return httpResponse.getStatusCode() == -8;
    }

    private int getBusiness() {
        if (this.mBusinessType.compareTo(BusinessTypeEnum.BIZ_TYPE_COLLECT_POLICY) <= 0 && this.mBusinessType.compareTo(BusinessTypeEnum.BIZ_TYPE_POLICY) >= 0) {
            return 1;
        }
        if (this.mBusinessType.equals(BusinessTypeEnum.BIZ_TYPE_AI_MODEL_RESOURCE)) {
            return 2;
        }
        if (this.mBusinessType.equals(BusinessTypeEnum.BIZ_TYPE_DEFAULT)) {
            return 3;
        }
        return 4;
    }
}

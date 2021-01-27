package com.huawei.nb.coordinator.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import java.lang.Thread;
import java.util.concurrent.TimeUnit;

/* access modifiers changed from: package-private */
public final class CoordinatorRequest {
    private static final int DEFAULT = 3;
    private static final long MAX_DEFAULT_SIZE = 524288000;
    private static final int MAX_TIMES = 3;
    private static final int MODEL_UPGRADE = 2;
    private static final int REQUEST_SUCCESS = 200;
    private static final String TAG = "CoordinatorRequest";
    private static final int TRAVEL_ASSISTANT = 1;
    private static final int UNKNOWN = 4;
    private String mAppId;
    private String mBusinessType;
    private Context mContext;
    private long mDataTrafficSize;
    private long mDelayMs;
    private Bundle mDevInfo;
    private String mFileName;
    private String mFileSavePath;
    private DataRequestListener mListener;
    private HttpRequestBody.Builder mRequestBodyBuilder;
    private HttpRequest.Builder mRequestBuilder;
    private int mRequestMode;
    private RequestResult mRequestResult;
    private int mRetryTimes;
    private String mTmpFileDir;
    private VerifyInfoHolder mVerifyInfoHolder;
    private String requestType;
    private IVerify verify;

    private boolean isAllowedOversea() {
        return false;
    }

    public static /* synthetic */ void lambda$OcgMLNt6Uwkm6swF_dKVqEbWL8k(CoordinatorRequest coordinatorRequest, Thread thread, Throwable th) {
        coordinatorRequest.uncaughtException(thread, th);
    }

    public static /* synthetic */ void lambda$hYz7eWZzxul5WnoQeD6mwisOysw(CoordinatorRequest coordinatorRequest) {
        coordinatorRequest.request();
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

    public CoordinatorRequest setDataRequestListener(DataRequestListener dataRequestListener) {
        this.mListener = dataRequestListener;
        return this;
    }

    public void sendAsyncHttpRequest() {
        Thread thread = new Thread(new Runnable() {
            /* class com.huawei.nb.coordinator.helper.$$Lambda$CoordinatorRequest$hYz7eWZzxul5WnoQeD6mwisOysw */

            @Override // java.lang.Runnable
            public final void run() {
                CoordinatorRequest.lambda$hYz7eWZzxul5WnoQeD6mwisOysw(CoordinatorRequest.this);
            }
        }, "AsyncHttpRequest");
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /* class com.huawei.nb.coordinator.helper.$$Lambda$CoordinatorRequest$OcgMLNt6Uwkm6swF_dKVqEbWL8k */

            @Override // java.lang.Thread.UncaughtExceptionHandler
            public final void uncaughtException(Thread thread, Throwable th) {
                CoordinatorRequest.lambda$OcgMLNt6Uwkm6swF_dKVqEbWL8k(CoordinatorRequest.this, thread, th);
            }
        });
        thread.start();
    }

    private void uncaughtException(Thread thread, Throwable th) {
        DSLog.e("CoordinatorRequest catch throwable when set async http request, thread " + thread.getId() + " err msg: " + th.getMessage(), new Object[0]);
    }

    public void sendHttpRequest() {
        request();
    }

    private void setRequestErrorInfo(String str, String str2) {
        RequestResult requestResult = this.mRequestResult;
        if (requestResult != null) {
            requestResult.setCode(str);
            this.mRequestResult.setDesc(str2);
            this.mRequestResult.setMessage(str2);
            DataRequestListener dataRequestListener = this.mListener;
            if (dataRequestListener != null) {
                dataRequestListener.onFailure(this.mRequestResult);
            }
        }
        CoordinatorAudit createCoordinatorAudit = HelperDatabaseManager.createCoordinatorAudit(this.mContext);
        createCoordinatorAudit.setIsRequestSuccess(false);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, createCoordinatorAudit);
        DSLog.e(TAG + str2, new Object[0]);
    }

    private HttpResponse errResponse(int i, String str) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(i);
        httpResponse.setResponseMsg(str);
        return httpResponse;
    }

    private void request() {
        try {
            printNetworkInfo();
            if (!isServiceSwitchOn()) {
                setRequestErrorInfo(String.valueOf(-4), " CoordinatorService is not allowed to start! ");
            } else if (!isOversea() || isAllowedOversea()) {
                HttpRequestBody build = this.mRequestBodyBuilder.build();
                if (HttpClient.GET_TYPE.contentEquals(this.requestType)) {
                    this.mRequestBuilder.get(build);
                } else if (HttpClient.POST_TYPE.contentEquals(this.requestType)) {
                    this.mRequestBuilder.post(build);
                } else if (HttpClient.DELETE_TYPE.contentEquals(this.requestType)) {
                    this.mRequestBuilder.delete(build);
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
        } catch (Throwable th) {
            setRequestErrorInfo("code-1", "CoordinatorRequest caught a throwable,message: " + th.getMessage() + ", cause:" + th.getCause());
        }
    }

    private boolean isOversea() {
        return DeviceUtil.OVERSEA.equals(DeviceUtil.getDistrict());
    }

    private boolean isServiceSwitchOn() {
        if (getBusiness() == 1) {
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
        } else {
            if (!(getBusiness() == 3 || getBusiness() == 2)) {
                DSLog.e("CoordinatorRequest Not a valid business.", new Object[0]);
            }
            return false;
        }
    }

    private void printNetworkInfo() {
        NetworkInfo activeNetworkInfo;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null && (activeNetworkInfo = connectivityManager.getActiveNetworkInfo()) != null) {
            int type = activeNetworkInfo.getType();
            NetworkInfo.State state = activeNetworkInfo.getState();
            if (state != null) {
                DSLog.d("CoordinatorRequest Network Type:" + type + ",Network State:" + state.toString(), new Object[0]);
            }
        }
    }

    private void requestWithHttp(HttpRequest.Builder builder) {
        HttpResponse httpResponse;
        DSLog.d("CoordinatorRequest Request with https.", new Object[0]);
        CoordinatorAudit createCoordinatorAudit = HelperDatabaseManager.createCoordinatorAudit(this.mContext);
        int currentNetWorkType = NetWorkStateUtil.getCurrentNetWorkType(this.mContext);
        int i = 1;
        HttpResponse httpResponse2 = null;
        boolean z = false;
        while (true) {
            if (i > this.mRetryTimes) {
                break;
            }
            if (this.mVerifyInfoHolder.isHasToken()) {
                String verifyToken = this.mVerifyInfoHolder.getVerifyToken();
                if (TextUtils.isEmpty(verifyToken) || this.mVerifyInfoHolder.isTokenExpired() || z) {
                    httpResponse = requestWithAuth(builder, createCoordinatorAudit);
                } else {
                    httpResponse = requestWithToken(builder, verifyToken, createCoordinatorAudit);
                }
            } else {
                httpResponse = requestWithAuth(builder, createCoordinatorAudit);
            }
            httpResponse2 = httpResponse;
            if (isSuccessCode(httpResponse2)) {
                if (i == 1) {
                    createCoordinatorAudit.setIsNeedRetry(0L);
                } else {
                    createCoordinatorAudit.setIsNeedRetry(1L);
                }
                onRequestSuccess(httpResponse2, httpResponse2.getResponseString(), createCoordinatorAudit);
                return;
            }
            z = isTokenOrSessionExpired(httpResponse2);
            if (httpResponse2.getStatusCode() == -7) {
                DSLog.e("CoordinatorRequest Download was interrupted, stop retry!", new Object[0]);
                break;
            }
            DSLog.e("CoordinatorRequest Need to retry " + this.mRetryTimes + " times. This is the " + i + " time.", new Object[0]);
            try {
                TimeUnit.MILLISECONDS.sleep(this.mDelayMs);
            } catch (InterruptedException unused) {
                DSLog.e("CoordinatorRequest retry delay is interrupted.", new Object[0]);
            }
            if (!isRetryAllowed(currentNetWorkType, httpResponse2)) {
                break;
            }
            currentNetWorkType = NetWorkStateUtil.getCurrentNetWorkType(this.mContext);
            i++;
        }
        if (httpResponse2 != null) {
            onRequestFailure(httpResponse2, httpResponse2.getResponseString(), createCoordinatorAudit);
        } else {
            setRequestErrorInfo("code = -2", " Fail to get request result, error: result is null.");
        }
    }

    private HttpResponse requestWithAuth(HttpRequest.Builder builder, CoordinatorAudit coordinatorAudit) {
        DSLog.d("CoordinatorRequest without token, generate authorization from the beginning.", new Object[0]);
        try {
            if (!this.verify.generateAuthorization(this.mContext, builder, this.mAppId, this.mDevInfo)) {
                return errResponse(-3, " verify error.");
            }
            coordinatorAudit.setSuccessVerifyTime(Long.valueOf(System.currentTimeMillis()));
            if (builder == null) {
                return errResponse(-2, "empty requestWithAuthenticate.");
            }
            DSLog.d("CoordinatorRequestrequest WithAuthenticate.", new Object[0]);
            HttpResponse requestResult = getRequestResult(builder, coordinatorAudit);
            if (getBusiness() == 1) {
                String jsonValue = CoordinatorJsonAnalyzer.getJsonValue(requestResult.getResponseString(), CoordinatorJsonAnalyzer.CODE_TYPE);
                if (isSuccessCode(requestResult)) {
                    return requestResult;
                }
                if (TextUtils.isEmpty(jsonValue) || (!jsonValue.equals(IVerifyVar.CLOUD_DEVICE_CA_ERROR) && !jsonValue.equals(IVerifyVar.SESSION_HAS_EXPIRED))) {
                    DSLog.e("CoordinatorRequest: requestWithAuth failed this time. code: " + requestResult.getStatusCode() + "msg:" + requestResult.getResponseMsg(), new Object[0]);
                } else {
                    try {
                        if (!this.verify.generateAuthorization(this.mContext, builder, this.mAppId, this.mDevInfo)) {
                            return errResponse(-3, " verify error.");
                        }
                        coordinatorAudit.setSuccessVerifyTime(Long.valueOf(System.currentTimeMillis()));
                        HttpResponse requestResult2 = getRequestResult(builder, coordinatorAudit);
                        this.mVerifyInfoHolder.setDeviceCASentFlag(false);
                        return requestResult2;
                    } catch (VerifyException e) {
                        return errResponse(e.getCode(), e.getMsg());
                    }
                }
            } else if (getBusiness() == 2) {
            }
            return requestResult;
        } catch (VerifyException e2) {
            return errResponse(e2.getCode(), e2.getMsg());
        }
    }

    private boolean isSuccessCode(HttpResponse httpResponse) {
        String responseString = httpResponse.getResponseString();
        int statusCode = httpResponse.getStatusCode();
        if (getBusiness() == 1) {
            if (this.mRequestMode != 0) {
                return statusCode == 200;
            }
            if (TextUtils.isEmpty(responseString)) {
                DSLog.d("CoordinatorRequest response string is empty.", new Object[0]);
                return false;
            }
            String statusCode2 = getStatusCode(this.mBusinessType);
            String jsonValue = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.CODE_TYPE);
            String str = "CoordinatorRequest Response code is " + jsonValue;
            DSLog.d(str, " msg is" + CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.MSG_TYPE));
            return !TextUtils.isEmpty(statusCode2) && !TextUtils.isEmpty(jsonValue) && jsonValue.equals(statusCode2);
        } else if (this.mRequestMode == 3) {
            DSLog.d("CoordinatorRequest request mode is TRANSFER_FILE_BREAKPOINT, status code = " + statusCode, new Object[0]);
            return statusCode == 200 || statusCode == 206;
        } else {
            DSLog.d("CoordinatorRequest default situation, status code = " + statusCode, new Object[0]);
            return statusCode == 200;
        }
    }

    private String getStatusCode(String str) {
        if (BusinessTypeEnum.BIZ_TYPE_POLICY.equals(str)) {
            return "PS200";
        }
        if (BusinessTypeEnum.BIZ_TYPE_FENCE.equals(str)) {
            return "FS20002";
        }
        if (BusinessTypeEnum.BIZ_TYPE_SMART_TRAVEL.equals(str)) {
            return "ST20000";
        }
        if (BusinessTypeEnum.BIZ_TYPE_APPLET.equals(str)) {
            return "AL20000";
        }
        if (BusinessTypeEnum.BIZ_TYPE_PUSH.equals(str)) {
            return "PU20000";
        }
        if (BusinessTypeEnum.BIZ_TYPE_SCENE_PACKAGE.equals(str)) {
            return "SP20000";
        }
        return BusinessTypeEnum.BIZ_TYPE_COLLECT_POLICY.equals(str) ? "CP20000" : "200";
    }

    private HttpResponse getRequestResult(HttpRequest.Builder builder, CoordinatorAudit coordinatorAudit) {
        HttpRequest build = builder.build();
        coordinatorAudit.setUrl(build.getUrl());
        return new HttpClient(this.mRequestMode, this.mFileSavePath, this.mFileName).newCall(build).setDataRequestListener(this.mListener).setDataTrafficSize(Long.valueOf(this.mDataTrafficSize)).setTmpFileDir(this.mTmpFileDir).syncExecute(coordinatorAudit);
    }

    private void onRequestSuccess(HttpResponse httpResponse, String str, CoordinatorAudit coordinatorAudit) {
        DSLog.d("CoordinatorRequest business type:" + this.mBusinessType + " requests successfully.", new Object[0]);
        if (this.mVerifyInfoHolder.isHasToken() && this.mVerifyInfoHolder.isTokenExpired()) {
            this.mVerifyInfoHolder.updateToken(httpResponse, this.verify);
        }
        if (this.mListener != null) {
            if (getBusiness() == 1) {
                str = CoordinatorJsonAnalyzer.getJsonValue(str, CoordinatorJsonAnalyzer.DATA_TYPE);
            } else {
                DSLog.d("CoordinatorRequest other business requests successfully.", new Object[0]);
            }
            this.mListener.onSuccess(str);
        }
        long currentTimeMillis = System.currentTimeMillis();
        coordinatorAudit.setDataSize(Long.valueOf(httpResponse.getResponseSize()));
        coordinatorAudit.setSuccessTransferTime(Long.valueOf(currentTimeMillis));
        coordinatorAudit.setIsRequestSuccess(true);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, coordinatorAudit);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0080  */
    private void onRequestFailure(HttpResponse httpResponse, String str, CoordinatorAudit coordinatorAudit) {
        boolean z;
        DataRequestListener dataRequestListener;
        DSLog.e("CoordinatorRequest business type:" + this.mBusinessType + " request failure.", new Object[0]);
        if (getBusiness() == 1) {
            String jsonValue = CoordinatorJsonAnalyzer.getJsonValue(str, CoordinatorJsonAnalyzer.CODE_TYPE);
            if (!TextUtils.isEmpty(jsonValue)) {
                setFailRequestResult(jsonValue, CoordinatorJsonAnalyzer.getJsonValue(str, CoordinatorJsonAnalyzer.DESC_TYPE), CoordinatorJsonAnalyzer.getJsonValue(str, CoordinatorJsonAnalyzer.MSG_TYPE));
                z = isShieldCloudError(jsonValue);
                this.mRequestResult.setUrl(httpResponse.getUrl());
                dataRequestListener = this.mListener;
                if (dataRequestListener == null) {
                    dataRequestListener.onFailure(this.mRequestResult);
                } else {
                    DSLog.e("CoordinatorRequest Fail to callback, error: listener is null.", new Object[0]);
                }
                failResultReport(httpResponse, z, coordinatorAudit);
            }
            setFailRequestResult(String.valueOf(httpResponse.getStatusCode()), httpResponse.getResponseMsg(), httpResponse.getHttpExceptionMsg());
        } else {
            setFailRequestResult(String.valueOf(httpResponse.getStatusCode()), httpResponse.getResponseMsg(), httpResponse.getHttpExceptionMsg());
        }
        z = false;
        this.mRequestResult.setUrl(httpResponse.getUrl());
        dataRequestListener = this.mListener;
        if (dataRequestListener == null) {
        }
        failResultReport(httpResponse, z, coordinatorAudit);
    }

    private void setFailRequestResult(String str, String str2, String str3) {
        this.mRequestResult.setCode(str);
        this.mRequestResult.setDesc(str2);
        this.mRequestResult.setMessage(str3);
    }

    private void failResultReport(HttpResponse httpResponse, boolean z, CoordinatorAudit coordinatorAudit) {
        String generateErrDetail = generateErrDetail(httpResponse);
        int errorType = getErrorType(httpResponse, z);
        DSLog.e("CoordinatorRequest Failed to request from cloud, error code: " + errorType, new Object[0]);
        if (errorType == -5) {
            DownloadFault.report(generateErrDetail);
        } else if (errorType != -3) {
            if (errorType != -2) {
                switch (errorType) {
                    case ResultStatusCodeEnum.TIME_OUT /* -13 */:
                    case ResultStatusCodeEnum.NETWORK_FAULT /* -11 */:
                    case ResultStatusCodeEnum.CONNECT_CLOUD_ERROR /* -9 */:
                        CoordinatorNetworkErrorAudit.report(generateNetworkErrorAudit(httpResponse, coordinatorAudit));
                        break;
                    case ResultStatusCodeEnum.GET_SIGNATURE_ERROR /* -12 */:
                        break;
                    case ResultStatusCodeEnum.SHIELD_CLOUD_ERROR /* -10 */:
                        CoordinatorCloudErrorAudit.report(generateCloudErrorAudit(httpResponse, coordinatorAudit, this.mBusinessType));
                        break;
                    default:
                        HttpsFault.report(generateErrDetail);
                        break;
                }
            } else {
                SDKAPIFault.report(generateErrDetail);
            }
        }
        coordinatorAudit.setIsRequestSuccess(false);
        HelperDatabaseManager.insertCoordinatorAudit(this.mContext, coordinatorAudit);
    }

    private String generateErrDetail(HttpResponse httpResponse) {
        return " Cause: " + httpResponse.getResponseMsg() + ", Code:" + httpResponse.getStatusCode() + ", Network:" + NetWorkStateUtil.getCurrentNetWorkType(this.mContext) + ", URL: " + httpResponse.getUrl() + ", PackageName" + this.mContext.getPackageName() + ", Version:" + DeviceUtil.getVersionName(this.mContext);
    }

    private CoordinatorCloudErrorAudit generateCloudErrorAudit(HttpResponse httpResponse, CoordinatorAudit coordinatorAudit, String str) {
        CoordinatorCloudErrorAudit coordinatorCloudErrorAudit = new CoordinatorCloudErrorAudit();
        coordinatorCloudErrorAudit.addStatusCodeToList(String.valueOf(httpResponse.getStatusCode()));
        coordinatorCloudErrorAudit.addExceptionMessageToList(httpResponse.getHttpExceptionMsg());
        coordinatorCloudErrorAudit.addBusinessCodeToList(str);
        coordinatorCloudErrorAudit.addResponseMessageToList(httpResponse.getResponseMsg());
        coordinatorCloudErrorAudit.addPackageNameToList(coordinatorAudit.getAppPackageName());
        coordinatorCloudErrorAudit.addUrlToList(coordinatorAudit.getUrl());
        coordinatorCloudErrorAudit.addNetworkToList(coordinatorAudit.getNetWorkState());
        coordinatorCloudErrorAudit.addDateToList(coordinatorAudit.getRequestDate());
        return coordinatorCloudErrorAudit;
    }

    private CoordinatorNetworkErrorAudit generateNetworkErrorAudit(HttpResponse httpResponse, CoordinatorAudit coordinatorAudit) {
        CoordinatorNetworkErrorAudit coordinatorNetworkErrorAudit = new CoordinatorNetworkErrorAudit();
        coordinatorNetworkErrorAudit.addStatusCodeToList(String.valueOf(httpResponse.getStatusCode()));
        coordinatorNetworkErrorAudit.addResponseMessageToList(httpResponse.getResponseMsg());
        coordinatorNetworkErrorAudit.addPackageNameToList(coordinatorAudit.getAppPackageName());
        coordinatorNetworkErrorAudit.addUrlToList(coordinatorAudit.getUrl());
        coordinatorNetworkErrorAudit.addNetworkToList(coordinatorAudit.getNetWorkState());
        coordinatorNetworkErrorAudit.addDateToList(coordinatorAudit.getRequestDate());
        return coordinatorNetworkErrorAudit;
    }

    private int getErrorType(HttpResponse httpResponse, boolean z) {
        if (z) {
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

    private boolean isShieldCloudError(String str) {
        String trim = str.trim();
        return IVerifyVar.PUBLICKEY_NOT_EXIST.equals(trim) || IVerifyVar.CAN_NOT_CONNECT_TO_LDAP_SERVER.equals(trim) || IVerifyVar.PUBLICKEY_NOT_EXIST_IN_DCS.equals(trim) || IVerifyVar.PUBLICKEY_FORMAT_ERROR.equals(trim) || IVerifyVar.DYNAMIC_CARD_INFO_NULL.equals(trim);
    }

    private HttpResponse requestWithToken(HttpRequest.Builder builder, String str, CoordinatorAudit coordinatorAudit) {
        DSLog.d("CoordinatorRequest ready to request with token", new Object[0]);
        HttpRequest build = builder.addRequestHeader(IVerifyVar.AUTHORIZATION_KEY, "PKI " + IVerifyVar.TOKEN_KEY + "=" + str).build();
        coordinatorAudit.setUrl(build.getUrl());
        return new HttpClient(this.mRequestMode, this.mFileSavePath, this.mFileName).newCall(build).setDataTrafficSize(Long.valueOf(this.mDataTrafficSize)).setDataRequestListener(this.mListener).syncExecute();
    }

    private boolean isTokenOrSessionExpired(HttpResponse httpResponse) {
        String responseString = httpResponse.getResponseString();
        if (getBusiness() != 1) {
            return false;
        }
        String trim = CoordinatorJsonAnalyzer.getJsonValue(responseString, CoordinatorJsonAnalyzer.CODE_TYPE).trim();
        if (TextUtils.isEmpty(trim)) {
            return false;
        }
        return IVerifyVar.TOKEN_HAS_EXPIRED.equals(trim) || IVerifyVar.SESSION_HAS_EXPIRED.equals(trim);
    }

    private boolean isRetryAllowed(int i, HttpResponse httpResponse) {
        int currentNetWorkType = NetWorkStateUtil.getCurrentNetWorkType(this.mContext);
        if (currentNetWorkType == 1) {
            return true;
        }
        if (currentNetWorkType == 3 && i == 3) {
            return !httpResponse.isDownloadStart();
        }
        return false;
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
        return this.mBusinessType.equals(BusinessTypeEnum.BIZ_TYPE_DEFAULT) ? 3 : 4;
    }

    public static class Builder {
        private CoordinatorRequest mCoordinatorRequest = new CoordinatorRequest();

        Builder(Context context) {
            this.mCoordinatorRequest.mContext = context;
        }

        public Builder get() {
            this.mCoordinatorRequest.requestType = HttpClient.GET_TYPE;
            return this;
        }

        public Builder appId(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mCoordinatorRequest.mAppId = str;
            }
            return this;
        }

        public Builder post() {
            this.mCoordinatorRequest.requestType = HttpClient.POST_TYPE;
            return this;
        }

        public Builder delete() {
            this.mCoordinatorRequest.requestType = HttpClient.DELETE_TYPE;
            return this;
        }

        public Builder url(String str) throws CoordinatorSDKException {
            if (!TextUtils.isEmpty(str)) {
                this.mCoordinatorRequest.mRequestBuilder.url(str);
                return this;
            }
            throw new CoordinatorSDKException(-2, " url is empty.");
        }

        public Builder connectTimeout(int i) {
            if (i > 0) {
                this.mCoordinatorRequest.mRequestBuilder.connectTimeout(i);
            }
            return this;
        }

        public Builder readTimeout(int i) {
            if (i > 0) {
                this.mCoordinatorRequest.mRequestBuilder.readTimeout(i);
            }
            return this;
        }

        public Builder verifyMode(int i) {
            if (i == 1) {
                i = 0;
            }
            this.mCoordinatorRequest.mVerifyInfoHolder = VerifyInfoHolderFactory.getVerifyInfoHolder(i);
            return this;
        }

        public Builder addRequestHeader(String str, String str2) {
            this.mCoordinatorRequest.mRequestBuilder.addRequestHeader(str, str2);
            return this;
        }

        public Builder businessType(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mCoordinatorRequest.mBusinessType = str;
            } else {
                DSLog.d(" BusinessType is empty.", new Object[0]);
            }
            return this;
        }

        public Builder fileSavePath(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mCoordinatorRequest.mFileSavePath = str;
            }
            return this;
        }

        public Builder fileName(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mCoordinatorRequest.mFileName = str;
            }
            return this;
        }

        public Builder addRequestBody(String str, String str2) {
            this.mCoordinatorRequest.mRequestBodyBuilder.add(str, str2);
            return this;
        }

        public Builder addRequestBody(String str) throws CoordinatorSDKException {
            if (TextUtils.isEmpty(str)) {
                return this;
            }
            if (JsonUtils.isValidJson(str)) {
                this.mCoordinatorRequest.mRequestBodyBuilder.addJsonBody(str);
                return this;
            }
            throw new CoordinatorSDKException(-2, " JsonBody is invalid.");
        }

        public Builder requestMode(int i) {
            this.mCoordinatorRequest.mRequestMode = i;
            return this;
        }

        public Builder retry(int i, long j) {
            if (i <= 0) {
                DSLog.i("CoordinatorRequest retryTimes is illegal. Reset to 3 times.", new Object[0]);
                i = 3;
            }
            if (j < 0) {
                DSLog.i("CoordinatorRequest delayMs is illegal. Reset to 0 ms.", new Object[0]);
                j = 0;
            }
            this.mCoordinatorRequest.mRetryTimes = i;
            this.mCoordinatorRequest.mDelayMs = j;
            return this;
        }

        public Builder dataTrafficSize(long j) {
            DSLog.d("CoordinatorRequest DataTrafficSize is " + j, new Object[0]);
            if (j <= 0) {
                DSLog.d("CoordinatorRequest DataTrafficSize is " + j + ", reset to " + CoordinatorRequest.MAX_DEFAULT_SIZE, new Object[0]);
                j = 524288000;
            }
            this.mCoordinatorRequest.mDataTrafficSize = j;
            return this;
        }

        public Builder breakpointResumeDownload(String str, int i) throws CoordinatorSDKException {
            if (i != 3) {
                return this;
            }
            if (!TextUtils.isEmpty(str)) {
                DSLog.d(CoordinatorRequest.TAG, " allow to resume downloading from breakpoint.");
                this.mCoordinatorRequest.mTmpFileDir = str;
                return this;
            }
            throw new CoordinatorSDKException(-2, " request mode is breakpoint-resume-download, but tmp file dir is empty");
        }

        public Builder setDevInfo(Bundle bundle) {
            this.mCoordinatorRequest.mDevInfo = bundle;
            return this;
        }

        public CoordinatorRequest build() {
            return this.mCoordinatorRequest;
        }
    }
}

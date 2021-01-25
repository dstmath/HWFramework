package com.hwawei.application.hwpay;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.security.hccm.ClientCertificateManager;
import com.huawei.security.hccm.EnrollmentException;
import com.huawei.security.hccm.common.callback.EnrollCertificateCallback;
import com.huawei.security.hccm.param.EnrollmentContext;
import com.huawei.security.hccm.param.EnrollmentParamsSpec;
import com.huawei.security.hccm.param.ProtocolParamCMP;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.x500.X500Name;
import org.json.JSONException;
import org.json.JSONObject;

public class HWPayServiceManager {
    private static final String KEY_STORE_PROVIDER = "HwUniversalKeyStoreProvider";
    private static final String KEY_STORE_TYPE = "HwKeyStore";
    private static final int MAX_ALIAS_LEN = 89;
    private static final int MAX_TOKEN_LEN = 512;
    private static final int MAX_URL_LEN = 4096;
    private static final int MIN_ALIAS_LEN = 1;
    private static final String MIN_HCCM_SDK_VERSION = "9.0.0.2";
    private static final int MIN_TOKEN_LEN = 1;
    private static final int MIN_URL_LEN = 1;
    private static final String TAG = "HWPayServiceManager";
    private String mAlias;
    private EnrollCertificateCallback mCallback;
    private JSONObject mConSettings;
    private String mErrMsg;
    private ClientCertificateManager mHccm;
    private X509Certificate mRaCertificate;
    private X509Certificate mRootCertificate;
    private String mSigPadding;
    private X500Name mSubject;
    private TokenInfo mToken;
    private String mUrl;

    public HWPayServiceManager() throws EnrollmentException {
        try {
            this.mHccm = ClientCertificateManager.getInstance("HwKeyStore", "HwUniversalKeyStoreProvider");
            int resultCode = checkVersion();
            if (resultCode != 0) {
                Log.e(TAG, "Version not match");
                throw new EnrollmentException("Version ont support", resultCode);
            }
        } catch (EnrollmentException e) {
            this.mErrMsg = "Get instance of ClientCertificateManager failed because " + e.getMessage();
            Log.e(TAG, this.mErrMsg + " and the error code is " + e.getErrorCode());
            throw e;
        }
    }

    private static int checkVersion() {
        String currentHccmSdkVer = ClientCertificateManager.getHccmSdkVersion();
        if (TextUtils.isEmpty(currentHccmSdkVer)) {
            return -6;
        }
        if (ClientCertificateManager.isCurrentVersionSupport(currentHccmSdkVer, "9.0.0.2")) {
            return 0;
        }
        Log.w(TAG, "Current HUKS version does not support hccm");
        return -7;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enrollCertInternal() throws EnrollmentException {
        try {
            if (checkCertEnrollParams()) {
                MessageDigest.getInstance("SHA-256").digest(this.mToken.getCredential().getBytes(StandardCharsets.UTF_8));
                JSONObject userSettings = this.mConSettings.optJSONObject("user_settings");
                JSONObject authHeader = userSettings == null ? new JSONObject() : userSettings;
                authHeader.put("Authorization", this.mToken.getToken());
                this.mConSettings.put("user_settings", authHeader);
                ProtocolParamCMP cmpParam = (ProtocolParamCMP) new ProtocolParamCMP().getInstance(1);
                cmpParam.setRaCertificate(this.mRaCertificate);
                cmpParam.setRootCertificate(this.mRootCertificate);
                EnrollmentParamsSpec enrollmentParamsSpec = new EnrollmentParamsSpec.Builder(this.mAlias, this.mSubject, new URL(this.mUrl), EnrollmentParamsSpec.ENROLLMENT_PROTOCOL_CMP, cmpParam).setConnectionSettings(this.mConSettings).setSigAlgPadding(this.mSigPadding).build();
                Certificate[] chain = this.mHccm.enroll(enrollmentParamsSpec);
                EnrollmentContext enrollmentContext = new EnrollmentContext(enrollmentParamsSpec);
                enrollmentContext.setClientCertificateChain(chain);
                this.mHccm.store(enrollmentContext);
                Log.d(TAG, "Application certificate was stored successfully!");
                return;
            }
            Log.e(TAG, "Invalid parameter for certificate enrollment.");
            throw new EnrollmentException("Invalid parameters", -1);
        } catch (IllegalArgumentException e) {
            throw new EnrollmentException("Invalid params encountered: " + e.getMessage(), -1);
        } catch (EnrollmentException e2) {
            this.mErrMsg = "Encounter EnrollmentException";
            Log.e(TAG, this.mErrMsg + " errorCode is " + e2.getErrorCode());
            throw e2;
        } catch (MalformedURLException | NoSuchAlgorithmException | JSONException e3) {
            Log.e(TAG, "Enroll cert failed for unknown exception: " + e3.getMessage());
            throw new EnrollmentException("EnrollAppCertificate failed: " + e3.getMessage(), -9);
        }
    }

    public void initializeCertParams(X500Name subject, String alias, TokenInfo token, JSONObject connectSetting, String url, X509Certificate raCertificate, X509Certificate rootCertificate, String padding, EnrollCertificateCallback callback) {
        this.mSubject = subject;
        this.mAlias = alias;
        this.mToken = token;
        this.mConSettings = connectSetting;
        this.mUrl = url;
        this.mRaCertificate = raCertificate;
        this.mRootCertificate = rootCertificate;
        this.mSigPadding = padding;
        this.mCallback = callback;
    }

    public void enrollAppCertificate() throws Exception {
        Log.d(TAG, "EnrollAppCertificate sync");
        try {
            enrollCertInternal();
            showResult(0);
        } catch (EnrollmentException e) {
            Log.e(TAG, "Enroll cert failed: " + e.getMessage());
            showResult(e.getErrorCode());
            throw e;
        } catch (Exception e2) {
            Log.e(TAG, "Enroll cert failed for unknown exception: " + e2.getMessage());
            showResult(-9);
            throw new EnrollmentException("EnrollAppCertificate failed: " + e2.getMessage(), -9);
        }
    }

    public void enrollAppCertificateAsync() throws Exception {
        Log.d(TAG, "EnrollAppCertificate Async");
        new CertEnrollTask().execute(new Void[0]);
    }

    private boolean checkCertEnrollParams() {
        TokenInfo tokenInfo;
        if (this.mAlias == null || this.mCallback == null || (tokenInfo = this.mToken) == null || tokenInfo.getType() == null || this.mToken.getCredential() == null || this.mSubject == null || this.mUrl == null || this.mRootCertificate == null) {
            Log.e(TAG, "Params shouldn't be null");
            return false;
        } else if (!TokenInfo.TOKEN_TYPE_ACCESS_TOKEN.equals(this.mToken.getType())) {
            Log.e(TAG, "Token type is invalid");
            return false;
        } else if (this.mUrl.length() < 1 || this.mUrl.length() > 4096) {
            Log.e(TAG, "The length of url is invalid");
            return false;
        } else if (this.mToken.getCredential().length() < 1 || this.mToken.getCredential().length() > 512) {
            Log.e(TAG, "The length of token is invalid");
            return false;
        } else if (this.mAlias.length() >= 1 && this.mAlias.length() <= 89) {
            return true;
        } else {
            Log.e(TAG, "The length of alias is invalid");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showResult(int resultCode) {
        if (resultCode == 0) {
            this.mCallback.onSuccess();
        } else {
            this.mCallback.onError(resultCode);
        }
    }

    private class CertEnrollTask extends AsyncTask<Void, Void, Integer> {
        private CertEnrollTask() {
        }

        /* access modifiers changed from: protected */
        public Integer doInBackground(Void... voids) {
            int retCode = 0;
            try {
                HWPayServiceManager.this.enrollCertInternal();
            } catch (EnrollmentException e) {
                Log.e(HWPayServiceManager.TAG, "Enroll cert failed " + e.getMessage() + " and the error code is " + e.getErrorCode());
                retCode = e.getErrorCode();
            } catch (Exception e2) {
                Log.e(HWPayServiceManager.TAG, "Enroll cert failed with unknown exception");
                retCode = -9;
            }
            return Integer.valueOf(retCode);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Integer result) {
            HWPayServiceManager.this.showResult(result.intValue());
        }
    }
}

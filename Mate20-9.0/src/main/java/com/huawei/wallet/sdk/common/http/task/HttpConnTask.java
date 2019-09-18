package com.huawei.wallet.sdk.common.http.task;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.secure.android.common.ssl.SecureSSLSocketFactory;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.buscard.model.ApplyPayOrderCallback;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.ServiceConfig;
import com.huawei.wallet.sdk.common.apdu.properties.WalletSystemProperties;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;
import com.huawei.wallet.sdk.common.apdu.whitecard.WalletProcessTrace;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.IOUtils;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.huawei.wallet.sdk.common.utils.NetworkUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import com.unionpay.tsmservice.data.Constant;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class HttpConnTask<Result, RequestParams> extends WalletProcessTrace {
    private static final String CONTENT_TYPE_APP_JSON = "application/json";
    private static final int DEFAULT_TIMEOUT = 60000;
    protected static final int ERROR_CODE_CONNECTION_FAILED = -2;
    protected static final int ERROR_CODE_NO_NETWORK = -1;
    protected static final int ERROR_CODE_PARAMS_ERROR = -3;
    protected static final int ERROR_CODE_SERVER_OVERLOAD = -4;
    private static final int SERVER_OVERLOAD_ERRORCODE = 503;
    private static final String TAG = "HttpConnTask";
    private static String logBuildType;
    private int mConnTimeout = DEFAULT_TIMEOUT;
    protected Context mContext;
    private int mSocketTimeout = DEFAULT_TIMEOUT;
    private final String mUrl;
    private String recieveMsg = "Recv_http_msg";
    private String sendMsg = "Send_http_msg";

    /* access modifiers changed from: protected */
    public abstract String prepareRequestStr(RequestParams requestparams);

    /* access modifiers changed from: protected */
    public abstract Result readErrorResponse(int i, String str);

    /* access modifiers changed from: protected */
    public abstract Result readSuccessResponse(int i, String str, JSONObject jSONObject);

    public HttpConnTask(Context context, String url) {
        this.mContext = context;
        this.mUrl = url;
    }

    public HttpConnTask(Context context, String url, int connTimeout, int socketTimeout) {
        this.mContext = context;
        this.mUrl = url;
        this.mConnTimeout = connTimeout;
        this.mSocketTimeout = socketTimeout;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0334, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0335, code lost:
        r6 = r10;
        r10 = null;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x033d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x033e, code lost:
        r6 = r10;
        r10 = null;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0346, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x0347, code lost:
        r6 = r10;
        r10 = null;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x034f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x0350, code lost:
        r6 = r10;
        r10 = null;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x0358, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0359, code lost:
        r6 = r10;
        r10 = null;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0361, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0362, code lost:
        r6 = r10;
        r10 = null;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x03e2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x03e4, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x03e6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x03e8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x03ea, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x03ec, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x03ee, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x03ef, code lost:
        r9 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x0410, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x0411, code lost:
        r12 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x0462, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x0463, code lost:
        r9 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0464, code lost:
        r10 = null;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:?, code lost:
        r2 = r0.getMessage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x04aa, code lost:
        r2 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00cd, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:?, code lost:
        r2 = r0.getMessage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x0509, code lost:
        r2 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00ce, code lost:
        r9 = -2;
        r10 = null;
        r12 = null;
        r6 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:247:?, code lost:
        r2 = r0.getMessage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x0568, code lost:
        r2 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00d6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00d7, code lost:
        r9 = -2;
        r10 = null;
        r12 = null;
        r6 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:?, code lost:
        r2 = r0.getMessage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x05c7, code lost:
        r2 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00df, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:275:?, code lost:
        r2 = r0.getMessage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x0626, code lost:
        r2 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00e0, code lost:
        r9 = -2;
        r10 = null;
        r12 = null;
        r6 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:?, code lost:
        r2 = r0.getMessage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00e8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:290:0x0684, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:291:0x0685, code lost:
        r9 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x0688, code lost:
        r2 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00e9, code lost:
        r9 = -2;
        r10 = null;
        r12 = null;
        r6 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:302:0x06b1, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:303:0x06b2, code lost:
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:304:0x06b3, code lost:
        r9 = null;
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00f1, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00f2, code lost:
        r9 = -2;
        r10 = null;
        r12 = null;
        r6 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00fa, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00fb, code lost:
        r9 = -2;
        r10 = null;
        r12 = null;
        r6 = null;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x03ee A[ExcHandler: all (th java.lang.Throwable), PHI: r15 
      PHI: (r15v34 'outStream' java.io.DataOutputStream) = (r15v0 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream) binds: [B:40:0x015e, B:41:?, B:159:0x03a6, B:160:?, B:162:0x03c5, B:163:?, B:155:0x0370, B:156:?, B:44:0x01b6] A[DONT_GENERATE, DONT_INLINE], Splitter:B:40:0x015e] */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0410 A[ExcHandler: MalformedURLException (e java.net.MalformedURLException), PHI: r15 
      PHI: (r15v21 'outStream' java.io.DataOutputStream) = (r15v0 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream), (r15v35 'outStream' java.io.DataOutputStream) binds: [B:40:0x015e, B:41:?, B:159:0x03a6, B:160:?, B:162:0x03c5, B:163:?, B:155:0x0370, B:156:?, B:44:0x01b6] A[DONT_GENERATE, DONT_INLINE], Splitter:B:40:0x015e] */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x0462 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:15:0x00b3] */
    /* JADX WARNING: Removed duplicated region for block: B:218:0x04a5 A[SYNTHETIC, Splitter:B:218:0x04a5] */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x04aa  */
    /* JADX WARNING: Removed duplicated region for block: B:232:0x0504 A[SYNTHETIC, Splitter:B:232:0x0504] */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x0509  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0563 A[SYNTHETIC, Splitter:B:246:0x0563] */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x0568  */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x05c2 A[SYNTHETIC, Splitter:B:260:0x05c2] */
    /* JADX WARNING: Removed duplicated region for block: B:262:0x05c7  */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x0621 A[SYNTHETIC, Splitter:B:274:0x0621] */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x0626  */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x067f A[SYNTHETIC, Splitter:B:288:0x067f] */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0688  */
    /* JADX WARNING: Removed duplicated region for block: B:302:0x06b1 A[ExcHandler: MalformedURLException (e java.net.MalformedURLException), Splitter:B:15:0x00b3] */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x06e4 A[Catch:{ all -> 0x0713 }] */
    /* JADX WARNING: Removed duplicated region for block: B:309:0x06e9 A[Catch:{ all -> 0x0713 }] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:229:0x04d0=Splitter:B:229:0x04d0, B:215:0x0471=Splitter:B:215:0x0471, B:277:0x0628=Splitter:B:277:0x0628, B:263:0x05c9=Splitter:B:263:0x05c9, B:249:0x056a=Splitter:B:249:0x056a, B:293:0x068a=Splitter:B:293:0x068a, B:235:0x050b=Splitter:B:235:0x050b, B:285:0x064b=Splitter:B:285:0x064b, B:221:0x04ac=Splitter:B:221:0x04ac, B:271:0x05ed=Splitter:B:271:0x05ed, B:257:0x058e=Splitter:B:257:0x058e, B:243:0x052f=Splitter:B:243:0x052f} */
    public Result processTask(RequestParams params) {
        String srcTranID;
        String srcTranID2;
        ByteArrayOutputStream outputStream;
        InputStream is;
        HttpURLConnection conn;
        Result result;
        HttpURLConnection conn2;
        ByteArrayOutputStream outputStream2;
        InputStream is2;
        HttpURLConnection conn3;
        ByteArrayOutputStream outputStream3;
        HttpURLConnection conn4;
        int i;
        String str;
        Result result2;
        HttpURLConnection conn5;
        InputStream is3;
        ByteArrayOutputStream outputStream4;
        HttpURLConnection conn6;
        String str2;
        HttpURLConnection conn7;
        InputStream is4;
        ByteArrayOutputStream outputStream5;
        HttpURLConnection conn8;
        String str3;
        HttpURLConnection conn9;
        InputStream is5;
        ByteArrayOutputStream outputStream6;
        HttpURLConnection conn10;
        String str4;
        HttpURLConnection conn11;
        InputStream is6;
        ByteArrayOutputStream outputStream7;
        HttpURLConnection conn12;
        String str5;
        HttpURLConnection conn13;
        InputStream is7;
        ByteArrayOutputStream outputStream8;
        HttpURLConnection conn14;
        String str6;
        HttpURLConnection conn15;
        HttpURLConnection conn16;
        ByteArrayOutputStream outputStream9;
        InputStream result3;
        int resultCode;
        InputStream inputStream;
        InputStream inputStream2;
        InputStream inputStream3;
        InputStream inputStream4;
        InputStream inputStream5;
        InputStream inputStream6;
        InputStream inputStream7;
        String json;
        String sb;
        StringBuilder sb2;
        InputStream is8;
        if (!NetworkUtil.isNetworkConnected(this.mContext)) {
            LogC.e(getSubProcessPrefix() + "processTask, no network.", false);
            return readErrorResponse(-1, CardServerBaseResponse.RESPONSE_MESSAGE_NO_NETWORK_FAILED);
        }
        String requestStr = prepareRequestStr(params);
        if (requestStr == null) {
            LogC.e(getSubProcessPrefix() + "processTask, invalid request params.", false);
            return readErrorResponse(1, CardServerBaseResponse.RESPONSE_MESSAGE_PARAMS_ERROR);
        }
        try {
            JSONObject head = new JSONObject(JSONHelper.getStringValue(new JSONObject(JSONHelper.getStringValue(new JSONObject(requestStr), SNBConstant.FIELD_DATA)), "header"));
            String srcTranID3 = JSONHelper.getStringValue(head, "srcTranID");
            srcTranID2 = JSONHelper.getStringValue(head, "commander");
            srcTranID = srcTranID3;
        } catch (JSONException e) {
            String srcTranID4 = "";
            LogC.e(getSubProcessPrefix() + "Something wrong when get srcTranID and commander", false);
            onReportEvent("Something wrong when get srcTranID", "-1", "", this.sendMsg, srcTranID4);
            srcTranID = srcTranID4;
            srcTranID2 = "";
        }
        DataOutputStream outStream = null;
        try {
            URL url = new URL(this.mUrl);
            if ("https".equals(url.getProtocol())) {
                conn16 = openHttpsConnection(url);
            } else {
                conn16 = openHttpConnection(url);
            }
            HttpURLConnection conn17 = conn16;
            try {
                conn17.setConnectTimeout(this.mConnTimeout);
                conn17.setReadTimeout(this.mSocketTimeout);
                conn17.setDoInput(true);
                conn17.setDoOutput(true);
                conn17.setUseCaches(false);
                conn17.setRequestMethod("POST");
                conn17.setRequestProperty("Content-Type", CONTENT_TYPE_APP_JSON);
                conn17.setRequestProperty("Charset", AES.CHAR_ENCODING);
                String deviceID = PhoneDeviceUtil.getDeviceID(this.mContext);
                conn17.setRequestProperty("deviceId", deviceID);
                String sign = JSONHelper.parseBussiCertSign(this.mContext, requestStr);
                conn17.setRequestProperty("bussiCertSign", sign);
                conn17.connect();
                String str7 = sign;
                String str8 = deviceID;
                HttpURLConnection conn18 = conn17;
                try {
                    onReportEvent("send success", null, srcTranID2, this.sendMsg, srcTranID);
                    outStream = new DataOutputStream(conn18.getOutputStream());
                    LogC.d(getSubProcessPrefix() + "processTask request string : " + requestStr, true);
                    outStream.write(requestStr.getBytes(AES.CHAR_ENCODING));
                    outStream.flush();
                    int resultCode2 = conn18.getResponseCode();
                    LogC.i(getSubProcessPrefix() + "processTask connection result code : " + resultCode2, true);
                    if (200 == resultCode2) {
                        InputStream is9 = conn18.getInputStream();
                        try {
                            outputStream9 = new ByteArrayOutputStream();
                        } catch (MalformedURLException e2) {
                            urlEx = e2;
                            conn2 = conn18;
                            outputStream2 = null;
                            is2 = is9;
                            try {
                                LogC.e(getSubProcessPrefix() + "processTask url invalid.", true);
                                StringBuilder sb3 = new StringBuilder();
                                sb3.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                                sb3.append(!TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                                result = readErrorResponse(1, sb3.toString());
                                onReportEvent("Send request failed, RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx error ", String.valueOf(1), srcTranID2, this.sendMsg, srcTranID);
                                closeStream(outStream, is2, outputStream2, conn2);
                                ByteArrayOutputStream byteArrayOutputStream = outputStream2;
                                InputStream inputStream8 = is2;
                                HttpURLConnection httpURLConnection = conn2;
                                ByteArrayOutputStream byteArrayOutputStream2 = byteArrayOutputStream;
                                return result;
                            } catch (Throwable th) {
                                result = th;
                                ByteArrayOutputStream byteArrayOutputStream3 = outputStream2;
                                is = is2;
                                conn = conn2;
                                outputStream = byteArrayOutputStream3;
                                closeStream(outStream, is, outputStream, conn);
                                throw result;
                            }
                        } catch (NoSuchAlgorithmException e3) {
                            noSuchAlgorithmExceptionEx = e3;
                            inputStream = is9;
                            conn5 = conn18;
                            outputStream4 = null;
                            is3 = inputStream;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                            StringBuilder sb4 = new StringBuilder();
                            sb4.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                            if (TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage())) {
                            }
                            sb4.append(str);
                            result2 = readErrorResponse(i, sb4.toString());
                            String valueOf = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx error ", valueOf, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (KeyManagementException e4) {
                            keyManagementExceptionEx = e4;
                            inputStream2 = is9;
                            conn7 = conn18;
                            outputStream5 = null;
                            is4 = inputStream2;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                            StringBuilder sb5 = new StringBuilder();
                            sb5.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                            if (TextUtils.isEmpty(keyManagementExceptionEx.getMessage())) {
                            }
                            sb5.append(str2);
                            result2 = readErrorResponse(i, sb5.toString());
                            String valueOf2 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx error ", valueOf2, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (IOException e5) {
                            ioEx = e5;
                            inputStream3 = is9;
                            conn9 = conn18;
                            outputStream6 = null;
                            is5 = inputStream3;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask IOException : " + ioEx.getMessage(), true);
                            StringBuilder sb6 = new StringBuilder();
                            sb6.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                            if (TextUtils.isEmpty(ioEx.getMessage())) {
                            }
                            sb6.append(str3);
                            result2 = readErrorResponse(i, sb6.toString());
                            String valueOf3 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx error ", valueOf3, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (CertificateException e6) {
                            e = e6;
                            inputStream4 = is9;
                            conn11 = conn18;
                            outputStream7 = null;
                            is6 = inputStream4;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask CertificateException : " + e.getMessage(), true);
                            StringBuilder sb7 = new StringBuilder();
                            sb7.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb7.append(str4);
                            result2 = readErrorResponse(i, sb7.toString());
                            String valueOf4 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException error ", valueOf4, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (IllegalAccessException e7) {
                            e = e7;
                            inputStream5 = is9;
                            conn13 = conn18;
                            outputStream8 = null;
                            is7 = inputStream5;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask IllegalAccessException : " + e.getMessage(), true);
                            StringBuilder sb8 = new StringBuilder();
                            sb8.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb8.append(str5);
                            result2 = readErrorResponse(i, sb8.toString());
                            String valueOf5 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException error ", valueOf5, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (KeyStoreException e8) {
                            e = e8;
                            inputStream6 = is9;
                            conn3 = conn18;
                            outputStream3 = null;
                            is = inputStream6;
                            i = -2;
                            try {
                                LogC.e(getSubProcessPrefix() + "processTask KeyStoreException : " + e.getMessage(), true);
                                StringBuilder sb9 = new StringBuilder();
                                sb9.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                                if (TextUtils.isEmpty(e.getMessage())) {
                                }
                                sb9.append(str6);
                                result2 = readErrorResponse(i, sb9.toString());
                                String valueOf6 = String.valueOf(i);
                                conn4 = conn3;
                                try {
                                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException error ", valueOf6, srcTranID2, this.sendMsg, srcTranID);
                                    closeStream(outStream, is, outputStream3, conn4);
                                    return result;
                                } catch (Throwable th2) {
                                    result = th2;
                                }
                            } catch (Throwable th3) {
                                result = th3;
                                conn = conn3;
                                closeStream(outStream, is, outputStream, conn);
                                throw result;
                            }
                        } catch (Throwable th4) {
                            result = th4;
                            inputStream7 = is9;
                            conn = conn18;
                            outputStream = null;
                            is = inputStream7;
                            closeStream(outStream, is, outputStream, conn);
                            throw result;
                        }
                        try {
                            byte[] buffer = new byte[ApplyPayOrderCallback.RETURN_FAILED_CARDINFO_PIN_LOCKED];
                            while (true) {
                                int read = is9.read(buffer);
                                int len = read;
                                if (read == -1) {
                                    break;
                                }
                                try {
                                    outputStream9.write(buffer, 0, len);
                                } catch (MalformedURLException e9) {
                                    urlEx = e9;
                                    is2 = is9;
                                    conn2 = conn18;
                                } catch (NoSuchAlgorithmException e10) {
                                    noSuchAlgorithmExceptionEx = e10;
                                    outputStream4 = outputStream9;
                                    conn5 = conn18;
                                    i = -2;
                                    is3 = is9;
                                    LogC.e(getSubProcessPrefix() + "processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                                    StringBuilder sb42 = new StringBuilder();
                                    sb42.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                                    if (TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage())) {
                                    }
                                    sb42.append(str);
                                    result2 = readErrorResponse(i, sb42.toString());
                                    String valueOf7 = String.valueOf(i);
                                    conn4 = conn3;
                                    onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx error ", valueOf7, srcTranID2, this.sendMsg, srcTranID);
                                    closeStream(outStream, is, outputStream3, conn4);
                                    return result;
                                } catch (KeyManagementException e11) {
                                    keyManagementExceptionEx = e11;
                                    outputStream5 = outputStream9;
                                    conn7 = conn18;
                                    i = -2;
                                    is4 = is9;
                                    LogC.e(getSubProcessPrefix() + "processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                                    StringBuilder sb52 = new StringBuilder();
                                    sb52.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                                    if (TextUtils.isEmpty(keyManagementExceptionEx.getMessage())) {
                                    }
                                    sb52.append(str2);
                                    result2 = readErrorResponse(i, sb52.toString());
                                    String valueOf22 = String.valueOf(i);
                                    conn4 = conn3;
                                    onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx error ", valueOf22, srcTranID2, this.sendMsg, srcTranID);
                                    closeStream(outStream, is, outputStream3, conn4);
                                    return result;
                                } catch (IOException e12) {
                                    ioEx = e12;
                                    outputStream6 = outputStream9;
                                    conn9 = conn18;
                                    i = -2;
                                    is5 = is9;
                                    LogC.e(getSubProcessPrefix() + "processTask IOException : " + ioEx.getMessage(), true);
                                    StringBuilder sb62 = new StringBuilder();
                                    sb62.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                                    if (TextUtils.isEmpty(ioEx.getMessage())) {
                                    }
                                    sb62.append(str3);
                                    result2 = readErrorResponse(i, sb62.toString());
                                    String valueOf32 = String.valueOf(i);
                                    conn4 = conn3;
                                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx error ", valueOf32, srcTranID2, this.sendMsg, srcTranID);
                                    closeStream(outStream, is, outputStream3, conn4);
                                    return result;
                                } catch (CertificateException e13) {
                                    e = e13;
                                    outputStream7 = outputStream9;
                                    conn11 = conn18;
                                    i = -2;
                                    is6 = is9;
                                    LogC.e(getSubProcessPrefix() + "processTask CertificateException : " + e.getMessage(), true);
                                    StringBuilder sb72 = new StringBuilder();
                                    sb72.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                                    if (TextUtils.isEmpty(e.getMessage())) {
                                    }
                                    sb72.append(str4);
                                    result2 = readErrorResponse(i, sb72.toString());
                                    String valueOf42 = String.valueOf(i);
                                    conn4 = conn3;
                                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException error ", valueOf42, srcTranID2, this.sendMsg, srcTranID);
                                    closeStream(outStream, is, outputStream3, conn4);
                                    return result;
                                } catch (IllegalAccessException e14) {
                                    e = e14;
                                    outputStream8 = outputStream9;
                                    conn13 = conn18;
                                    i = -2;
                                    is7 = is9;
                                    LogC.e(getSubProcessPrefix() + "processTask IllegalAccessException : " + e.getMessage(), true);
                                    StringBuilder sb82 = new StringBuilder();
                                    sb82.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                                    if (TextUtils.isEmpty(e.getMessage())) {
                                    }
                                    sb82.append(str5);
                                    result2 = readErrorResponse(i, sb82.toString());
                                    String valueOf52 = String.valueOf(i);
                                    conn4 = conn3;
                                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException error ", valueOf52, srcTranID2, this.sendMsg, srcTranID);
                                    closeStream(outStream, is, outputStream3, conn4);
                                    return result;
                                } catch (KeyStoreException e15) {
                                    e = e15;
                                    outputStream3 = outputStream9;
                                    conn3 = conn18;
                                    i = -2;
                                    is = is9;
                                    LogC.e(getSubProcessPrefix() + "processTask KeyStoreException : " + e.getMessage(), true);
                                    StringBuilder sb92 = new StringBuilder();
                                    sb92.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                                    if (TextUtils.isEmpty(e.getMessage())) {
                                    }
                                    sb92.append(str6);
                                    result2 = readErrorResponse(i, sb92.toString());
                                    String valueOf62 = String.valueOf(i);
                                    conn4 = conn3;
                                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException error ", valueOf62, srcTranID2, this.sendMsg, srcTranID);
                                    closeStream(outStream, is, outputStream3, conn4);
                                    return result;
                                } catch (Throwable th5) {
                                    result = th5;
                                    outputStream = outputStream9;
                                    conn = conn18;
                                    is = is9;
                                    closeStream(outStream, is, outputStream, conn);
                                    throw result;
                                }
                            }
                            json = new String(outputStream9.toByteArray(), AES.CHAR_ENCODING);
                            StringBuilder sb10 = new StringBuilder();
                            URL url2 = url;
                            sb10.append(getSubProcessPrefix());
                            sb10.append(TAG);
                            sb = sb10.toString();
                            sb2 = new StringBuilder();
                            is8 = is9;
                        } catch (MalformedURLException e16) {
                            urlEx = e16;
                            conn2 = conn18;
                            is2 = is9;
                            outputStream2 = outputStream9;
                            LogC.e(getSubProcessPrefix() + "processTask url invalid.", true);
                            StringBuilder sb32 = new StringBuilder();
                            sb32.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                            sb32.append(!TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                            result = readErrorResponse(1, sb32.toString());
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx error ", String.valueOf(1), srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is2, outputStream2, conn2);
                            ByteArrayOutputStream byteArrayOutputStream4 = outputStream2;
                            InputStream inputStream82 = is2;
                            HttpURLConnection httpURLConnection2 = conn2;
                            ByteArrayOutputStream byteArrayOutputStream22 = byteArrayOutputStream4;
                            return result;
                        } catch (NoSuchAlgorithmException e17) {
                            noSuchAlgorithmExceptionEx = e17;
                            inputStream = is9;
                            outputStream4 = outputStream9;
                            conn5 = conn18;
                            is3 = inputStream;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                            StringBuilder sb422 = new StringBuilder();
                            sb422.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                            if (TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage())) {
                            }
                            sb422.append(str);
                            result2 = readErrorResponse(i, sb422.toString());
                            String valueOf72 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx error ", valueOf72, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (KeyManagementException e18) {
                            keyManagementExceptionEx = e18;
                            inputStream2 = is9;
                            outputStream5 = outputStream9;
                            conn7 = conn18;
                            is4 = inputStream2;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                            StringBuilder sb522 = new StringBuilder();
                            sb522.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                            if (TextUtils.isEmpty(keyManagementExceptionEx.getMessage())) {
                            }
                            sb522.append(str2);
                            result2 = readErrorResponse(i, sb522.toString());
                            String valueOf222 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx error ", valueOf222, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (IOException e19) {
                            ioEx = e19;
                            inputStream3 = is9;
                            outputStream6 = outputStream9;
                            conn9 = conn18;
                            is5 = inputStream3;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask IOException : " + ioEx.getMessage(), true);
                            StringBuilder sb622 = new StringBuilder();
                            sb622.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                            if (TextUtils.isEmpty(ioEx.getMessage())) {
                            }
                            sb622.append(str3);
                            result2 = readErrorResponse(i, sb622.toString());
                            String valueOf322 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx error ", valueOf322, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (CertificateException e20) {
                            e = e20;
                            inputStream4 = is9;
                            outputStream7 = outputStream9;
                            conn11 = conn18;
                            is6 = inputStream4;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask CertificateException : " + e.getMessage(), true);
                            StringBuilder sb722 = new StringBuilder();
                            sb722.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb722.append(str4);
                            result2 = readErrorResponse(i, sb722.toString());
                            String valueOf422 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException error ", valueOf422, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (IllegalAccessException e21) {
                            e = e21;
                            inputStream5 = is9;
                            outputStream8 = outputStream9;
                            conn13 = conn18;
                            is7 = inputStream5;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask IllegalAccessException : " + e.getMessage(), true);
                            StringBuilder sb822 = new StringBuilder();
                            sb822.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb822.append(str5);
                            result2 = readErrorResponse(i, sb822.toString());
                            String valueOf522 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException error ", valueOf522, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (KeyStoreException e22) {
                            e = e22;
                            inputStream6 = is9;
                            outputStream3 = outputStream9;
                            conn3 = conn18;
                            is = inputStream6;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask KeyStoreException : " + e.getMessage(), true);
                            StringBuilder sb922 = new StringBuilder();
                            sb922.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb922.append(str6);
                            result2 = readErrorResponse(i, sb922.toString());
                            String valueOf622 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException error ", valueOf622, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (Throwable th6) {
                            result = th6;
                            inputStream7 = is9;
                            outputStream = outputStream9;
                            conn = conn18;
                            is = inputStream7;
                            closeStream(outStream, is, outputStream, conn);
                            throw result;
                        }
                        try {
                            sb2.append("processTask request string : ");
                            sb2.append(requestStr);
                            LogC.d(sb, sb2.toString(), true);
                            LogC.d(getSubProcessPrefix() + TAG, "processTask json string : " + json, true);
                            result = handleResponse(json, srcTranID, srcTranID2);
                            result3 = is8;
                        } catch (MalformedURLException e23) {
                            urlEx = e23;
                            conn2 = conn18;
                            is2 = is8;
                            outputStream2 = outputStream9;
                            LogC.e(getSubProcessPrefix() + "processTask url invalid.", true);
                            StringBuilder sb322 = new StringBuilder();
                            sb322.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                            sb322.append(!TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                            result = readErrorResponse(1, sb322.toString());
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx error ", String.valueOf(1), srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is2, outputStream2, conn2);
                            ByteArrayOutputStream byteArrayOutputStream42 = outputStream2;
                            InputStream inputStream822 = is2;
                            HttpURLConnection httpURLConnection22 = conn2;
                            ByteArrayOutputStream byteArrayOutputStream222 = byteArrayOutputStream42;
                            return result;
                        } catch (NoSuchAlgorithmException e24) {
                            noSuchAlgorithmExceptionEx = e24;
                            ByteArrayOutputStream outputStream10 = outputStream9;
                            HttpURLConnection conn19 = conn18;
                            is3 = is8;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                            StringBuilder sb4222 = new StringBuilder();
                            sb4222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                            if (TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage())) {
                            }
                            sb4222.append(str);
                            result2 = readErrorResponse(i, sb4222.toString());
                            String valueOf722 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx error ", valueOf722, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (KeyManagementException e25) {
                            keyManagementExceptionEx = e25;
                            ByteArrayOutputStream outputStream11 = outputStream9;
                            HttpURLConnection conn20 = conn18;
                            is4 = is8;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                            StringBuilder sb5222 = new StringBuilder();
                            sb5222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                            if (TextUtils.isEmpty(keyManagementExceptionEx.getMessage())) {
                            }
                            sb5222.append(str2);
                            result2 = readErrorResponse(i, sb5222.toString());
                            String valueOf2222 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx error ", valueOf2222, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (IOException e26) {
                            ioEx = e26;
                            ByteArrayOutputStream outputStream12 = outputStream9;
                            HttpURLConnection conn21 = conn18;
                            is5 = is8;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask IOException : " + ioEx.getMessage(), true);
                            StringBuilder sb6222 = new StringBuilder();
                            sb6222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                            if (TextUtils.isEmpty(ioEx.getMessage())) {
                            }
                            sb6222.append(str3);
                            result2 = readErrorResponse(i, sb6222.toString());
                            String valueOf3222 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx error ", valueOf3222, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (CertificateException e27) {
                            e = e27;
                            ByteArrayOutputStream outputStream13 = outputStream9;
                            HttpURLConnection conn22 = conn18;
                            is6 = is8;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask CertificateException : " + e.getMessage(), true);
                            StringBuilder sb7222 = new StringBuilder();
                            sb7222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb7222.append(str4);
                            result2 = readErrorResponse(i, sb7222.toString());
                            String valueOf4222 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException error ", valueOf4222, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (IllegalAccessException e28) {
                            e = e28;
                            ByteArrayOutputStream outputStream14 = outputStream9;
                            HttpURLConnection conn23 = conn18;
                            is7 = is8;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask IllegalAccessException : " + e.getMessage(), true);
                            StringBuilder sb8222 = new StringBuilder();
                            sb8222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb8222.append(str5);
                            result2 = readErrorResponse(i, sb8222.toString());
                            String valueOf5222 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException error ", valueOf5222, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (KeyStoreException e29) {
                            e = e29;
                            ByteArrayOutputStream outputStream15 = outputStream9;
                            HttpURLConnection conn24 = conn18;
                            is = is8;
                            i = -2;
                            LogC.e(getSubProcessPrefix() + "processTask KeyStoreException : " + e.getMessage(), true);
                            StringBuilder sb9222 = new StringBuilder();
                            sb9222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                            if (TextUtils.isEmpty(e.getMessage())) {
                            }
                            sb9222.append(str6);
                            result2 = readErrorResponse(i, sb9222.toString());
                            String valueOf6222 = String.valueOf(i);
                            conn4 = conn3;
                            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException error ", valueOf6222, srcTranID2, this.sendMsg, srcTranID);
                            closeStream(outStream, is, outputStream3, conn4);
                            return result;
                        } catch (Throwable th7) {
                            result = th7;
                            outputStream = outputStream9;
                            conn = conn18;
                            is = is8;
                            closeStream(outStream, is, outputStream, conn);
                            throw result;
                        }
                    } else {
                        URL url3 = url;
                        if (SERVER_OVERLOAD_ERRORCODE == resultCode2) {
                            LogC.e(getSubProcessPrefix() + "processTask resultCode=SERVER_OVERLOAD_ERRORCODE.", true);
                            result = readErrorResponse(-4, CardServerBaseResponse.RESPONSE_MESSAGE_SERVER_OVERLOAD_ERROR);
                            int i2 = resultCode2;
                            onReportEvent(CardServerBaseResponse.RESPONSE_MESSAGE_SERVER_OVERLOAD_ERROR, String.valueOf(resultCode2), srcTranID2, this.sendMsg, srcTranID);
                        } else {
                            LogC.e(getSubProcessPrefix() + "processTask resultCode=" + resultCode, true);
                            i = -2;
                            result = readErrorResponse(-2, CardServerBaseResponse.RESPONSE_MESSAGE_CONNECTION_FAILED);
                            onReportEvent(CardServerBaseResponse.RESPONSE_MESSAGE_CONNECTION_FAILED, String.valueOf(resultCode), srcTranID2, this.sendMsg, srcTranID);
                        }
                        result3 = null;
                        outputStream9 = null;
                    }
                    closeStream(outStream, result3, outputStream9, conn18);
                    ByteArrayOutputStream byteArrayOutputStream5 = outputStream9;
                    HttpURLConnection httpURLConnection3 = conn18;
                    InputStream inputStream9 = result3;
                } catch (MalformedURLException e30) {
                } catch (NoSuchAlgorithmException e31) {
                    noSuchAlgorithmExceptionEx = e31;
                    i = -2;
                    conn6 = conn18;
                    is3 = null;
                    outputStream4 = null;
                    LogC.e(getSubProcessPrefix() + "processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                    StringBuilder sb42222 = new StringBuilder();
                    sb42222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                    if (TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage())) {
                    }
                    sb42222.append(str);
                    result2 = readErrorResponse(i, sb42222.toString());
                    String valueOf7222 = String.valueOf(i);
                    conn4 = conn3;
                    onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx error ", valueOf7222, srcTranID2, this.sendMsg, srcTranID);
                    closeStream(outStream, is, outputStream3, conn4);
                    return result;
                } catch (KeyManagementException e32) {
                    keyManagementExceptionEx = e32;
                    i = -2;
                    conn8 = conn18;
                    is4 = null;
                    outputStream5 = null;
                    LogC.e(getSubProcessPrefix() + "processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                    StringBuilder sb52222 = new StringBuilder();
                    sb52222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                    if (TextUtils.isEmpty(keyManagementExceptionEx.getMessage())) {
                    }
                    sb52222.append(str2);
                    result2 = readErrorResponse(i, sb52222.toString());
                    String valueOf22222 = String.valueOf(i);
                    conn4 = conn3;
                    onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx error ", valueOf22222, srcTranID2, this.sendMsg, srcTranID);
                    closeStream(outStream, is, outputStream3, conn4);
                    return result;
                } catch (IOException e33) {
                    ioEx = e33;
                    i = -2;
                    conn10 = conn18;
                    is5 = null;
                    outputStream6 = null;
                    LogC.e(getSubProcessPrefix() + "processTask IOException : " + ioEx.getMessage(), true);
                    StringBuilder sb62222 = new StringBuilder();
                    sb62222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                    if (TextUtils.isEmpty(ioEx.getMessage())) {
                    }
                    sb62222.append(str3);
                    result2 = readErrorResponse(i, sb62222.toString());
                    String valueOf32222 = String.valueOf(i);
                    conn4 = conn3;
                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx error ", valueOf32222, srcTranID2, this.sendMsg, srcTranID);
                    closeStream(outStream, is, outputStream3, conn4);
                    return result;
                } catch (CertificateException e34) {
                    e = e34;
                    i = -2;
                    conn12 = conn18;
                    is6 = null;
                    outputStream7 = null;
                    LogC.e(getSubProcessPrefix() + "processTask CertificateException : " + e.getMessage(), true);
                    StringBuilder sb72222 = new StringBuilder();
                    sb72222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                    if (TextUtils.isEmpty(e.getMessage())) {
                    }
                    sb72222.append(str4);
                    result2 = readErrorResponse(i, sb72222.toString());
                    String valueOf42222 = String.valueOf(i);
                    conn4 = conn3;
                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException error ", valueOf42222, srcTranID2, this.sendMsg, srcTranID);
                    closeStream(outStream, is, outputStream3, conn4);
                    return result;
                } catch (IllegalAccessException e35) {
                    e = e35;
                    i = -2;
                    conn14 = conn18;
                    is7 = null;
                    outputStream8 = null;
                    LogC.e(getSubProcessPrefix() + "processTask IllegalAccessException : " + e.getMessage(), true);
                    StringBuilder sb82222 = new StringBuilder();
                    sb82222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                    if (TextUtils.isEmpty(e.getMessage())) {
                    }
                    sb82222.append(str5);
                    result2 = readErrorResponse(i, sb82222.toString());
                    String valueOf52222 = String.valueOf(i);
                    conn4 = conn3;
                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException error ", valueOf52222, srcTranID2, this.sendMsg, srcTranID);
                    closeStream(outStream, is, outputStream3, conn4);
                    return result;
                } catch (KeyStoreException e36) {
                    e = e36;
                    i = -2;
                    conn15 = conn18;
                    is = null;
                    outputStream3 = null;
                    LogC.e(getSubProcessPrefix() + "processTask KeyStoreException : " + e.getMessage(), true);
                    StringBuilder sb92222 = new StringBuilder();
                    sb92222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                    if (TextUtils.isEmpty(e.getMessage())) {
                    }
                    sb92222.append(str6);
                    result2 = readErrorResponse(i, sb92222.toString());
                    String valueOf62222 = String.valueOf(i);
                    conn4 = conn3;
                    onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException error ", valueOf62222, srcTranID2, this.sendMsg, srcTranID);
                    closeStream(outStream, is, outputStream3, conn4);
                    return result;
                } catch (Throwable th8) {
                }
            } catch (MalformedURLException e37) {
                urlEx = e37;
                conn2 = conn17;
                is2 = null;
                outputStream2 = null;
                LogC.e(getSubProcessPrefix() + "processTask url invalid.", true);
                StringBuilder sb3222 = new StringBuilder();
                sb3222.append("RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx = ");
                sb3222.append(!TextUtils.isEmpty(urlEx.getMessage()) ? urlEx.getMessage() : "");
                result = readErrorResponse(1, sb3222.toString());
                onReportEvent("Send request failed, RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION,urlEx error ", String.valueOf(1), srcTranID2, this.sendMsg, srcTranID);
                closeStream(outStream, is2, outputStream2, conn2);
                ByteArrayOutputStream byteArrayOutputStream422 = outputStream2;
                InputStream inputStream8222 = is2;
                HttpURLConnection httpURLConnection222 = conn2;
                ByteArrayOutputStream byteArrayOutputStream2222 = byteArrayOutputStream422;
                return result;
            } catch (NoSuchAlgorithmException e38) {
                noSuchAlgorithmExceptionEx = e38;
                i = -2;
                conn5 = conn17;
                is3 = null;
                outputStream4 = null;
                LogC.e(getSubProcessPrefix() + "processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
                StringBuilder sb422222 = new StringBuilder();
                sb422222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
                if (TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage())) {
                }
                sb422222.append(str);
                result2 = readErrorResponse(i, sb422222.toString());
                String valueOf72222 = String.valueOf(i);
                conn4 = conn3;
                onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx error ", valueOf72222, srcTranID2, this.sendMsg, srcTranID);
                closeStream(outStream, is, outputStream3, conn4);
                return result;
            } catch (KeyManagementException e39) {
                keyManagementExceptionEx = e39;
                i = -2;
                conn7 = conn17;
                is4 = null;
                outputStream5 = null;
                LogC.e(getSubProcessPrefix() + "processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
                StringBuilder sb522222 = new StringBuilder();
                sb522222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
                if (TextUtils.isEmpty(keyManagementExceptionEx.getMessage())) {
                }
                sb522222.append(str2);
                result2 = readErrorResponse(i, sb522222.toString());
                String valueOf222222 = String.valueOf(i);
                conn4 = conn3;
                onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx error ", valueOf222222, srcTranID2, this.sendMsg, srcTranID);
                closeStream(outStream, is, outputStream3, conn4);
                return result;
            } catch (IOException e40) {
                ioEx = e40;
                i = -2;
                conn9 = conn17;
                is5 = null;
                outputStream6 = null;
                LogC.e(getSubProcessPrefix() + "processTask IOException : " + ioEx.getMessage(), true);
                StringBuilder sb622222 = new StringBuilder();
                sb622222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
                if (TextUtils.isEmpty(ioEx.getMessage())) {
                }
                sb622222.append(str3);
                result2 = readErrorResponse(i, sb622222.toString());
                String valueOf322222 = String.valueOf(i);
                conn4 = conn3;
                onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx error ", valueOf322222, srcTranID2, this.sendMsg, srcTranID);
                closeStream(outStream, is, outputStream3, conn4);
                return result;
            } catch (CertificateException e41) {
                e = e41;
                i = -2;
                conn11 = conn17;
                is6 = null;
                outputStream7 = null;
                LogC.e(getSubProcessPrefix() + "processTask CertificateException : " + e.getMessage(), true);
                StringBuilder sb722222 = new StringBuilder();
                sb722222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
                if (TextUtils.isEmpty(e.getMessage())) {
                }
                sb722222.append(str4);
                result2 = readErrorResponse(i, sb722222.toString());
                String valueOf422222 = String.valueOf(i);
                conn4 = conn3;
                onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException error ", valueOf422222, srcTranID2, this.sendMsg, srcTranID);
                closeStream(outStream, is, outputStream3, conn4);
                return result;
            } catch (IllegalAccessException e42) {
                e = e42;
                i = -2;
                conn13 = conn17;
                is7 = null;
                outputStream8 = null;
                LogC.e(getSubProcessPrefix() + "processTask IllegalAccessException : " + e.getMessage(), true);
                StringBuilder sb822222 = new StringBuilder();
                sb822222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
                if (TextUtils.isEmpty(e.getMessage())) {
                }
                sb822222.append(str5);
                result2 = readErrorResponse(i, sb822222.toString());
                String valueOf522222 = String.valueOf(i);
                conn4 = conn3;
                onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException error ", valueOf522222, srcTranID2, this.sendMsg, srcTranID);
                closeStream(outStream, is, outputStream3, conn4);
                return result;
            } catch (KeyStoreException e43) {
                e = e43;
                i = -2;
                conn3 = conn17;
                is = null;
                outputStream3 = null;
                LogC.e(getSubProcessPrefix() + "processTask KeyStoreException : " + e.getMessage(), true);
                StringBuilder sb922222 = new StringBuilder();
                sb922222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
                if (TextUtils.isEmpty(e.getMessage())) {
                }
                sb922222.append(str6);
                result2 = readErrorResponse(i, sb922222.toString());
                String valueOf622222 = String.valueOf(i);
                conn4 = conn3;
                onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException error ", valueOf622222, srcTranID2, this.sendMsg, srcTranID);
                closeStream(outStream, is, outputStream3, conn4);
                return result;
            } catch (Throwable th9) {
                result = th9;
                conn = conn17;
                is = null;
                outputStream = null;
                closeStream(outStream, is, outputStream, conn);
                throw result;
            }
        } catch (MalformedURLException e44) {
        } catch (NoSuchAlgorithmException e45) {
            noSuchAlgorithmExceptionEx = e45;
            i = -2;
            conn6 = null;
            is3 = null;
            outputStream4 = null;
            LogC.e(getSubProcessPrefix() + "processTask, NoSuchAlgorithmException : " + noSuchAlgorithmExceptionEx.getMessage(), true);
            StringBuilder sb4222222 = new StringBuilder();
            sb4222222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx = ");
            if (TextUtils.isEmpty(noSuchAlgorithmExceptionEx.getMessage())) {
            }
            sb4222222.append(str);
            result2 = readErrorResponse(i, sb4222222.toString());
            String valueOf722222 = String.valueOf(i);
            conn4 = conn3;
            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION,noSuchAlgorithmExceptionEx error ", valueOf722222, srcTranID2, this.sendMsg, srcTranID);
            closeStream(outStream, is, outputStream3, conn4);
            return result;
        } catch (KeyManagementException e46) {
            keyManagementExceptionEx = e46;
            i = -2;
            conn8 = null;
            is4 = null;
            outputStream5 = null;
            LogC.e(getSubProcessPrefix() + "processTask, KeyManagementException : " + keyManagementExceptionEx.getMessage(), true);
            StringBuilder sb5222222 = new StringBuilder();
            sb5222222.append("RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx = ");
            if (TextUtils.isEmpty(keyManagementExceptionEx.getMessage())) {
            }
            sb5222222.append(str2);
            result2 = readErrorResponse(i, sb5222222.toString());
            String valueOf2222222 = String.valueOf(i);
            conn4 = conn3;
            onReportEvent("Send request failed, RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION,keyManagementExceptionEx error ", valueOf2222222, srcTranID2, this.sendMsg, srcTranID);
            closeStream(outStream, is, outputStream3, conn4);
            return result;
        } catch (IOException e47) {
            ioEx = e47;
            i = -2;
            conn10 = null;
            is5 = null;
            outputStream6 = null;
            LogC.e(getSubProcessPrefix() + "processTask IOException : " + ioEx.getMessage(), true);
            StringBuilder sb6222222 = new StringBuilder();
            sb6222222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx = ");
            if (TextUtils.isEmpty(ioEx.getMessage())) {
            }
            sb6222222.append(str3);
            result2 = readErrorResponse(i, sb6222222.toString());
            String valueOf3222222 = String.valueOf(i);
            conn4 = conn3;
            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,ioEx error ", valueOf3222222, srcTranID2, this.sendMsg, srcTranID);
            closeStream(outStream, is, outputStream3, conn4);
            return result;
        } catch (CertificateException e48) {
            e = e48;
            i = -2;
            conn12 = null;
            is6 = null;
            outputStream7 = null;
            LogC.e(getSubProcessPrefix() + "processTask CertificateException : " + e.getMessage(), true);
            StringBuilder sb7222222 = new StringBuilder();
            sb7222222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException = ");
            if (TextUtils.isEmpty(e.getMessage())) {
            }
            sb7222222.append(str4);
            result2 = readErrorResponse(i, sb7222222.toString());
            String valueOf4222222 = String.valueOf(i);
            conn4 = conn3;
            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,CertificateException error ", valueOf4222222, srcTranID2, this.sendMsg, srcTranID);
            closeStream(outStream, is, outputStream3, conn4);
            return result;
        } catch (IllegalAccessException e49) {
            e = e49;
            i = -2;
            conn14 = null;
            is7 = null;
            outputStream8 = null;
            LogC.e(getSubProcessPrefix() + "processTask IllegalAccessException : " + e.getMessage(), true);
            StringBuilder sb8222222 = new StringBuilder();
            sb8222222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException = ");
            if (TextUtils.isEmpty(e.getMessage())) {
            }
            sb8222222.append(str5);
            result2 = readErrorResponse(i, sb8222222.toString());
            String valueOf5222222 = String.valueOf(i);
            conn4 = conn3;
            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,IllegalAccessException error ", valueOf5222222, srcTranID2, this.sendMsg, srcTranID);
            closeStream(outStream, is, outputStream3, conn4);
            return result;
        } catch (KeyStoreException e50) {
            e = e50;
            i = -2;
            conn15 = null;
            is = null;
            outputStream3 = null;
            LogC.e(getSubProcessPrefix() + "processTask KeyStoreException : " + e.getMessage(), true);
            StringBuilder sb9222222 = new StringBuilder();
            sb9222222.append("RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException = ");
            if (TextUtils.isEmpty(e.getMessage())) {
            }
            sb9222222.append(str6);
            result2 = readErrorResponse(i, sb9222222.toString());
            String valueOf6222222 = String.valueOf(i);
            conn4 = conn3;
            onReportEvent("Send request failed, RESPONSE_CONNECTION_FAILED_MESSAGE,KeyStoreException error ", valueOf6222222, srcTranID2, this.sendMsg, srcTranID);
            closeStream(outStream, is, outputStream3, conn4);
            return result;
        } catch (Throwable th10) {
        }
        return result;
    }

    private void closeStream(DataOutputStream outStream, InputStream is, ByteArrayOutputStream outputStream, HttpURLConnection conn) {
        IOUtils.closeQuietly((OutputStream) outStream);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly((OutputStream) outputStream);
        if (conn != null) {
            conn.disconnect();
        }
    }

    private HttpsURLConnection openHttpsConnection(URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        initHttpsConnection(httpsURLConnection);
        return httpsURLConnection;
    }

    private HttpURLConnection openHttpConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private void initHttpsConnection(HttpsURLConnection httpsURLConnection) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, IllegalAccessException, KeyStoreException, IOException {
        httpsURLConnection.setSSLSocketFactory(SecureSSLSocketFactory.getInstance(this.mContext));
        httpsURLConnection.setHostnameVerifier(new StrictHostnameVerifier());
    }

    /* access modifiers changed from: protected */
    public Result handleResponse(String responseStr, String srcTranID, String commander) {
        int returnCode;
        String responseDataStr;
        String errorMsg;
        int returnCode2;
        String str = responseStr;
        LogC.d(getSubProcessPrefix() + "handleResponse response str : " + str, true);
        String returnDesc = null;
        JSONObject dataObject = null;
        if (str == null) {
            onReportEvent("Error with unknown", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
            return readSuccessResponse(-99, null, null);
        }
        try {
            JSONObject responseJson = new JSONObject(str);
            try {
                int keyIndex = JSONHelper.getIntValue(responseJson, "keyIndex");
                String merchantID = JSONHelper.getStringValue(responseJson, "merchantID");
                String errorCode = JSONHelper.getStringValue(responseJson, Constant.KEY_ERROR_CODE);
                String errorMsg2 = JSONHelper.getStringValue(responseJson, "errorMsg");
                boolean isUmps = false;
                if (responseJson.has("returnCode") && responseJson.has("returnDesc")) {
                    isUmps = true;
                }
                boolean isUmps2 = isUmps;
                if (isUmps2) {
                    if (StringUtil.isEmpty(errorCode, true)) {
                        errorCode = JSONHelper.getStringValue(responseJson, "returnCode");
                        if (!TextUtils.isEmpty(errorCode)) {
                            errorCode = StringUtil.hexStr2Str("Ox" + errorCode);
                        }
                    }
                    if (StringUtil.isEmpty(errorMsg2, true)) {
                        errorMsg2 = JSONHelper.getStringValue(responseJson, "returnDesc");
                    }
                }
                String errorCode2 = errorCode;
                String errorMsg3 = errorMsg2;
                String responseDataStr2 = JSONHelper.getStringValue(responseJson, "response");
                if (errorCode2 != null) {
                    if (isUmps2 && isNumber(errorCode2)) {
                        if (Integer.parseInt(errorCode2) == 0) {
                            responseDataStr = responseDataStr2;
                            errorMsg = errorMsg3;
                            String str2 = errorCode2;
                        }
                    }
                    LogC.w(getSubProcessPrefix() + "handleResponse, return code : " + errorCode2 + "return msg : " + errorMsg3, true);
                    StringBuilder sb = new StringBuilder();
                    sb.append("handleResponse, return code : ");
                    sb.append(errorCode2);
                    sb.append("return msg : ");
                    sb.append(errorMsg3);
                    String result = sb.toString();
                    try {
                        int returnCode3 = Integer.parseInt(errorCode2);
                        String str3 = responseDataStr2;
                        String errorMsg4 = errorMsg3;
                        String str4 = errorCode2;
                        String str5 = merchantID;
                        onReportEvent(result, errorCode2, commander, this.recieveMsg, srcTranID);
                        return readSuccessResponse(returnCode3, errorMsg4, null);
                    } catch (NumberFormatException e) {
                        ex = e;
                        String str6 = result;
                        LogC.e(getSubProcessPrefix() + "readSuccessResponse, NumberFormatException : " + ex.getMessage(), true);
                        returnCode = -99;
                        onReportEvent("readSuccessResponse, NumberFormatException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                        return readSuccessResponse(returnCode, returnDesc, dataObject);
                    } catch (JSONException e2) {
                        ex = e2;
                        String str7 = result;
                        LogC.e(getSubProcessPrefix() + "readSuccessResponse, JSONException : " + ex.getMessage(), true);
                        returnCode = -99;
                        onReportEvent("readSuccessResponse, JSONException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                        return readSuccessResponse(returnCode, returnDesc, dataObject);
                    }
                } else {
                    responseDataStr = responseDataStr2;
                    errorMsg = errorMsg3;
                    String str8 = errorCode2;
                }
                if (!ServiceConfig.getWalletMerchantId().equals(merchantID) || -1 != keyIndex) {
                    String str9 = errorMsg;
                    String str10 = merchantID;
                } else if (StringUtil.isEmpty(responseDataStr, true)) {
                    String str11 = errorMsg;
                    String str12 = merchantID;
                } else {
                    LogC.d(getSubProcessPrefix() + "handleResponse, responseDataStr : " + responseDataStr, true);
                    dataObject = new JSONObject(responseDataStr);
                    String returnCodeStr = JSONHelper.getStringValue(dataObject, "returnCode");
                    if (returnCodeStr == null) {
                        LogC.d(getSubProcessPrefix() + "handleResponse, returnCode is invalid.", true);
                        String str13 = errorMsg;
                        String errorMsg5 = returnCodeStr;
                        String str14 = merchantID;
                        onReportEvent("handleResponse, returnCode is invalid.", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                        return readSuccessResponse(-99, null, null);
                    }
                    String returnCodeStr2 = returnCodeStr;
                    String str15 = merchantID;
                    if (isNumber(returnCodeStr2)) {
                        returnCode2 = Integer.parseInt(returnCodeStr2);
                    } else {
                        returnCode2 = -98;
                    }
                    returnCode = returnCode2;
                    String returnDesc2 = JSONHelper.getStringValue(dataObject, "returnDesc");
                    try {
                        onReportEvent(returnDesc2, String.valueOf(returnCode), commander, this.recieveMsg, srcTranID);
                        returnDesc = returnDesc2;
                    } catch (NumberFormatException e3) {
                        ex = e3;
                        returnDesc = returnDesc2;
                        LogC.e(getSubProcessPrefix() + "readSuccessResponse, NumberFormatException : " + ex.getMessage(), true);
                        returnCode = -99;
                        onReportEvent("readSuccessResponse, NumberFormatException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                        return readSuccessResponse(returnCode, returnDesc, dataObject);
                    } catch (JSONException e4) {
                        ex = e4;
                        returnDesc = returnDesc2;
                        LogC.e(getSubProcessPrefix() + "readSuccessResponse, JSONException : " + ex.getMessage(), true);
                        returnCode = -99;
                        onReportEvent("readSuccessResponse, JSONException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                        return readSuccessResponse(returnCode, returnDesc, dataObject);
                    }
                    return readSuccessResponse(returnCode, returnDesc, dataObject);
                }
                LogC.d(getSubProcessPrefix() + "handleResponse, unexpected error from server.", true);
                onReportEvent("handleResponse, unexpected error from server.", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                return readSuccessResponse(-99, null, null);
            } catch (NumberFormatException e5) {
                ex = e5;
                LogC.e(getSubProcessPrefix() + "readSuccessResponse, NumberFormatException : " + ex.getMessage(), true);
                returnCode = -99;
                onReportEvent("readSuccessResponse, NumberFormatException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                return readSuccessResponse(returnCode, returnDesc, dataObject);
            } catch (JSONException e6) {
                ex = e6;
                LogC.e(getSubProcessPrefix() + "readSuccessResponse, JSONException : " + ex.getMessage(), true);
                returnCode = -99;
                onReportEvent("readSuccessResponse, JSONException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
                return readSuccessResponse(returnCode, returnDesc, dataObject);
            }
        } catch (NumberFormatException e7) {
            ex = e7;
            LogC.e(getSubProcessPrefix() + "readSuccessResponse, NumberFormatException : " + ex.getMessage(), true);
            returnCode = -99;
            onReportEvent("readSuccessResponse, NumberFormatException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
            return readSuccessResponse(returnCode, returnDesc, dataObject);
        } catch (JSONException e8) {
            ex = e8;
            LogC.e(getSubProcessPrefix() + "readSuccessResponse, JSONException : " + ex.getMessage(), true);
            returnCode = -99;
            onReportEvent("readSuccessResponse, JSONException error ", String.valueOf(-99), commander, this.recieveMsg, srcTranID);
            return readSuccessResponse(returnCode, returnDesc, dataObject);
        }
    }

    public boolean isNumber(String str) {
        if (str != null && !"".equals(str.trim()) && Pattern.compile("[0-9]*").matcher(str).matches()) {
            Long number = Long.valueOf(Long.parseLong(str));
            if (number.longValue() <= 2147483647L && number.longValue() >= -2147483648L) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setErrorInfo(JSONObject dataObject, ServerAccessBaseResponse response) {
        if (dataObject != null && dataObject.has("errorInfo")) {
            ErrorInfo errorInfo = null;
            try {
                errorInfo = ErrorInfo.build(dataObject.getJSONObject("errorInfo"));
            } catch (JSONException e) {
                LogC.e(getSubProcessPrefix() + "setErrorInfo, JSONException", true);
                response.returnCode = -99;
            }
            response.setErrorInfo(errorInfo);
        }
    }

    /* access modifiers changed from: protected */
    public String getSrcTranId(ServerAccessBaseResponse response, JSONObject dataObject) {
        if (dataObject == null) {
            return "";
        }
        try {
            if (!dataObject.has("header")) {
                return "";
            }
            JSONObject header = dataObject.getJSONObject("header");
            if (header == null) {
                return "";
            }
            String srcTranId = header.getString("srcTranID");
            response.setSrcTranID(srcTranId);
            return srcTranId;
        } catch (JSONException e) {
            LogC.e(getSubProcessPrefix() + "getSrcTransationId, JSONException", true);
            response.returnCode = -99;
            return "";
        }
    }

    private void onReportEvent(String result, String returnCode, String commander, String action, String srcTranID) {
        String messege = "commander:" + commander + "; srcTranID:" + srcTranID + ";  ; result:" + result + "; returnCode:" + returnCode + "; ";
        LogC.i(getSubProcessPrefix() + "HttpConnTask onReportEvent, action:" + action + ", Report data: " + messege, true);
    }

    protected static boolean isDebugBuild() {
        if (logBuildType == null && WalletSystemProperties.getInstance().containsProperty("LOG_BUILD_TYPE")) {
            logBuildType = WalletSystemProperties.getInstance().getProperty("LOG_BUILD_TYPE", "release");
        }
        if ("Debug".equalsIgnoreCase(logBuildType)) {
            return true;
        }
        return false;
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, "HttpConnTask|");
    }
}

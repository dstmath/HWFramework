package com.android.server.security.tsmagent.server.wallet;

import android.content.Context;
import com.android.server.security.tsmagent.server.CardServerBaseRequest;
import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import com.android.server.security.tsmagent.server.HttpConnectionBase;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.NetworkUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class HttpConnTask extends HttpConnectionBase {
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

    /* JADX WARNING: Removed duplicated region for block: B:55:0x01bf A:{Splitter: B:19:0x00f7, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01bf A:{Splitter: B:19:0x00f7, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x017b A:{Splitter: B:9:0x0039, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x017b A:{Splitter: B:9:0x0039, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01bc A:{Splitter: B:14:0x00b8, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01bc A:{Splitter: B:14:0x00b8, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Missing block: B:39:0x017b, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            com.android.server.security.tsmagent.utils.HwLog.e("processTask, Exception : " + r5.getMessage());
            r14 = readErrorResponse(-2);
     */
    /* JADX WARNING: Missing block: B:42:0x01a0, code:
            closeStream(r9, r6, r11, r4);
     */
    /* JADX WARNING: Missing block: B:53:0x01bc, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:54:0x01bd, code:
            r9 = r10;
     */
    /* JADX WARNING: Missing block: B:55:0x01bf, code:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:56:0x01c0, code:
            r11 = r12;
            r9 = r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CardServerBaseResponse processTask(CardServerBaseRequest params) {
        CardServerBaseResponse result;
        Throwable th;
        if (NetworkUtil.isNetworkConnected(this.mContext)) {
            String requestStr = prepareRequestStr(params);
            if (requestStr == null) {
                HwLog.d(" processTask, invalid request params.");
                return readErrorResponse(-3);
            }
            HttpURLConnection conn = null;
            DataOutputStream outStream = null;
            InputStream is = null;
            ByteArrayOutputStream outputStream = null;
            try {
                URL url = new URL(this.mUrl);
                if ("https".equals(url.getProtocol())) {
                    conn = openHttpsConnection(url);
                } else {
                    conn = openHttpConnection(url);
                }
                conn.setConnectTimeout(this.mConnTimeout);
                conn.setReadTimeout(this.mSocketTimeout);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "xml/json");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.connect();
                DataOutputStream outStream2 = new DataOutputStream(conn.getOutputStream());
                try {
                    outStream2.write(requestStr.getBytes("UTF-8"));
                    outStream2.flush();
                    int resultCode = conn.getResponseCode();
                    HwLog.d("processTask connection result code : " + resultCode);
                    if (200 == resultCode) {
                        is = conn.getInputStream();
                        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                        try {
                            byte[] buffer = new byte[1024];
                            while (true) {
                                int len = is.read(buffer);
                                if (len == -1) {
                                    break;
                                }
                                outputStream2.write(buffer, 0, len);
                            }
                            result = readSuccessResponse(0, new String(outputStream2.toByteArray(), "UTF-8"), null);
                            outputStream = outputStream2;
                        } catch (MalformedURLException e) {
                            outputStream = outputStream2;
                            outStream = outStream2;
                            try {
                                HwLog.e("processTask url invalid: url");
                                result = readErrorResponse(-3);
                                closeStream(outStream, is, outputStream, conn);
                                return result;
                            } catch (Throwable th2) {
                                th = th2;
                                closeStream(outStream, is, outputStream, conn);
                                throw th;
                            }
                        } catch (IOException e2) {
                        } catch (Throwable th3) {
                            th = th3;
                            outputStream = outputStream2;
                            outStream = outStream2;
                            closeStream(outStream, is, outputStream, conn);
                            throw th;
                        }
                    }
                    HwLog.e("Service err. resultCode :" + resultCode);
                    result = readErrorResponse(-2);
                    closeStream(outStream2, is, outputStream, conn);
                } catch (MalformedURLException e3) {
                    outStream = outStream2;
                } catch (IOException e4) {
                } catch (Throwable th4) {
                    th = th4;
                    outStream = outStream2;
                    closeStream(outStream, is, outputStream, conn);
                    throw th;
                }
            } catch (MalformedURLException e5) {
            } catch (IOException e6) {
            }
            return result;
        }
        HwLog.d(" processTask, no network.");
        return readErrorResponse(-1);
    }
}

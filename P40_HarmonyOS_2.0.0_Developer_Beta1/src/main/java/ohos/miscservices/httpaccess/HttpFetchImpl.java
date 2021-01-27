package ohos.miscservices.httpaccess;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import ohos.abilityshell.utils.AbilityContextUtils;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.httpaccess.data.RequestData;
import ohos.miscservices.httpaccess.data.ResponseData;

public class HttpFetchImpl {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "HttpFetchImpl");
    private Context context;

    public HttpFetchImpl(ohos.app.Context context2) {
        Object androidContext = AbilityContextUtils.getAndroidContext(context2);
        if (androidContext instanceof Context) {
            this.context = (Context) androidContext;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void httpUrlFetch(RequestData requestData, HttpProbe httpProbe) {
        Throwable th;
        BufferedInputStream bufferedInputStream;
        char c;
        HttpURLConnection httpURLConnection;
        ResponseData responseData = new ResponseData();
        try {
            if (requestData.getMethod().isEmpty()) {
                requestData.setMethod(HttpConstant.HTTP_METHOD_GET);
            }
            String method = requestData.getMethod();
            switch (method.hashCode()) {
                case -531492226:
                    if (method.equals(HttpConstant.HTTP_METHOD_OPTIONS)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 70454:
                    if (method.equals(HttpConstant.HTTP_METHOD_GET)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 79599:
                    if (method.equals(HttpConstant.HTTP_METHOD_PUT)) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 2213344:
                    if (method.equals(HttpConstant.HTTP_METHOD_HEAD)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 2461856:
                    if (method.equals(HttpConstant.HTTP_METHOD_POST)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 80083237:
                    if (method.equals(HttpConstant.HTTP_METHOD_TRACE)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1669334218:
                    if (method.equals(HttpConstant.HTTP_METHOD_CONNECT)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 2012838315:
                    if (method.equals(HttpConstant.HTTP_METHOD_DELETE)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    httpURLConnection = buildConnectionWithParam(requestData);
                    break;
                case 6:
                case 7:
                    httpURLConnection = buildConnectionWithStream(requestData);
                    break;
                default:
                    HiLog.error(TAG, "no method match!", new Object[0]);
                    responseData.setCode(-1);
                    httpProbe.onResponse(responseData);
                    HttpUtils.closeQuietly((InputStream) null);
                    HttpUtils.closeQuietly((OutputStream) null);
            }
            if (httpURLConnection == null) {
                HiLog.error(TAG, "httpURLConnection is null!", new Object[0]);
                responseData.setCode(-1);
                httpProbe.onResponse(responseData);
                HttpUtils.closeQuietly((InputStream) null);
                HttpUtils.closeQuietly((OutputStream) null);
            }
            httpURLConnection.connect();
            responseData.setHeaders(HttpUtils.analyzeResponseHeaders(httpURLConnection.getHeaderFields()));
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != 200) {
                HiLog.error(TAG, "connect error, response code : %{public}d!", Integer.valueOf(responseCode));
                bufferedInputStream = new BufferedInputStream(httpURLConnection.getErrorStream());
                try {
                    String str = new String(HttpUtils.parseInputStream(bufferedInputStream), "UTF-8");
                    responseData.setCode(responseCode);
                    responseData.setData(str);
                    httpProbe.onResponse(responseData);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) null);
                } catch (MalformedURLException unused) {
                    HiLog.error(TAG, "caught MalformedURLException!", new Object[0]);
                    responseData.setCode(-1);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) null);
                    httpProbe.onResponse(responseData);
                } catch (IOException unused2) {
                    HiLog.error(TAG, "caught IOException!", new Object[0]);
                    responseData.setCode(-1);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) null);
                    httpProbe.onResponse(responseData);
                }
            } else {
                BufferedInputStream bufferedInputStream2 = new BufferedInputStream(httpURLConnection.getInputStream());
                try {
                    String str2 = new String(HttpUtils.parseInputStream(bufferedInputStream2), "UTF-8");
                    responseData.setCode(200);
                    responseData.setData(str2);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream2);
                } catch (MalformedURLException unused3) {
                    bufferedInputStream = bufferedInputStream2;
                    HiLog.error(TAG, "caught MalformedURLException!", new Object[0]);
                    responseData.setCode(-1);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) null);
                    httpProbe.onResponse(responseData);
                } catch (IOException unused4) {
                    bufferedInputStream = bufferedInputStream2;
                    HiLog.error(TAG, "caught IOException!", new Object[0]);
                    responseData.setCode(-1);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) null);
                    httpProbe.onResponse(responseData);
                } catch (Throwable th2) {
                    th = th2;
                    bufferedInputStream = bufferedInputStream2;
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) null);
                    throw th;
                }
                HttpUtils.closeQuietly((OutputStream) null);
                httpProbe.onResponse(responseData);
            }
        } catch (MalformedURLException unused5) {
            bufferedInputStream = null;
            HiLog.error(TAG, "caught MalformedURLException!", new Object[0]);
            responseData.setCode(-1);
            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
            HttpUtils.closeQuietly((OutputStream) null);
            httpProbe.onResponse(responseData);
        } catch (IOException unused6) {
            bufferedInputStream = null;
            HiLog.error(TAG, "caught IOException!", new Object[0]);
            responseData.setCode(-1);
            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
            HttpUtils.closeQuietly((OutputStream) null);
            httpProbe.onResponse(responseData);
        } catch (Throwable th3) {
            th = th3;
            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
            HttpUtils.closeQuietly((OutputStream) null);
            throw th;
        }
    }

    public HttpURLConnection buildConnectionWithParam(RequestData requestData) {
        HttpURLConnection httpURLConnection;
        HiLog.debug(TAG, "begin to build connection with param!", new Object[0]);
        String url = requestData.getUrl();
        try {
            String data = requestData.getData();
            if (!data.isEmpty()) {
                if (url.contains(HttpConstant.URL_PARAM_SEPARATOR)) {
                    int indexOf = url.indexOf(HttpConstant.URL_PARAM_SEPARATOR) + 1;
                    String substring = url.substring(indexOf);
                    StringBuilder sb = new StringBuilder();
                    sb.append(url.substring(0, indexOf));
                    sb.append(URLEncoder.encode(substring + HttpConstant.URL_PARAM_DELIMITER + data, "UTF-8"));
                    url = sb.toString();
                } else {
                    url = url + HttpConstant.URL_PARAM_SEPARATOR + URLEncoder.encode(data, "UTF-8");
                }
            }
            HiLog.debug(TAG, "final url : %{public}s!", url);
            URLConnection openConnection = new URL(url).openConnection();
            if (!(openConnection instanceof HttpURLConnection)) {
                return null;
            }
            httpURLConnection = (HttpURLConnection) openConnection;
            try {
                httpURLConnection.setRequestMethod(requestData.getMethod());
                HttpUtils.setConnProperty(httpURLConnection, requestData, this.context);
                return httpURLConnection;
            } catch (MalformedURLException unused) {
            } catch (UnsupportedEncodingException unused2) {
                HiLog.error(TAG, "buildConnection caught UnsupportedEncodingException!", new Object[0]);
                return httpURLConnection;
            } catch (IOException unused3) {
                HiLog.error(TAG, "buildConnection caught IOException!", new Object[0]);
                return httpURLConnection;
            }
        } catch (MalformedURLException unused4) {
            httpURLConnection = null;
            HiLog.error(TAG, "buildConnection caught MalformedURLException!", new Object[0]);
            return httpURLConnection;
        } catch (UnsupportedEncodingException unused5) {
            httpURLConnection = null;
            HiLog.error(TAG, "buildConnection caught UnsupportedEncodingException!", new Object[0]);
            return httpURLConnection;
        } catch (IOException unused6) {
            httpURLConnection = null;
            HiLog.error(TAG, "buildConnection caught IOException!", new Object[0]);
            return httpURLConnection;
        }
    }

    public HttpURLConnection buildConnectionWithStream(RequestData requestData) {
        Throwable th;
        HttpURLConnection httpURLConnection;
        BufferedOutputStream bufferedOutputStream;
        HiLog.debug(TAG, "begin to build connection with stream!", new Object[0]);
        String url = requestData.getUrl();
        HiLog.debug(TAG, "final url : %{public}s!", url);
        BufferedOutputStream bufferedOutputStream2 = null;
        try {
            URLConnection openConnection = new URL(url).openConnection();
            if (openConnection instanceof HttpURLConnection) {
                httpURLConnection = (HttpURLConnection) openConnection;
                try {
                    HttpUtils.setConnProperty(httpURLConnection, requestData, this.context);
                    httpURLConnection.setRequestMethod(requestData.getMethod());
                    bufferedOutputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
                    try {
                        bufferedOutputStream.write(requestData.getData().getBytes("UTF-8"));
                        bufferedOutputStream.flush();
                    } catch (MalformedURLException unused) {
                        bufferedOutputStream2 = bufferedOutputStream;
                    } catch (UnsupportedEncodingException unused2) {
                        bufferedOutputStream2 = bufferedOutputStream;
                        HiLog.error(TAG, "buildConnection caught UnsupportedEncodingException!", new Object[0]);
                        HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
                        return httpURLConnection;
                    } catch (IOException unused3) {
                        bufferedOutputStream2 = bufferedOutputStream;
                        HiLog.error(TAG, "buildConnection caught IOException!", new Object[0]);
                        HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
                        return httpURLConnection;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedOutputStream2 = bufferedOutputStream;
                        HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
                        throw th;
                    }
                } catch (MalformedURLException unused4) {
                    HiLog.error(TAG, "buildConnection caught MalformedURLException!", new Object[0]);
                    HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
                    return httpURLConnection;
                } catch (UnsupportedEncodingException unused5) {
                    HiLog.error(TAG, "buildConnection caught UnsupportedEncodingException!", new Object[0]);
                    HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
                    return httpURLConnection;
                } catch (IOException unused6) {
                    HiLog.error(TAG, "buildConnection caught IOException!", new Object[0]);
                    HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
                    return httpURLConnection;
                }
            } else {
                bufferedOutputStream = null;
                httpURLConnection = null;
            }
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
        } catch (MalformedURLException unused7) {
            httpURLConnection = null;
            HiLog.error(TAG, "buildConnection caught MalformedURLException!", new Object[0]);
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
            return httpURLConnection;
        } catch (UnsupportedEncodingException unused8) {
            httpURLConnection = null;
            HiLog.error(TAG, "buildConnection caught UnsupportedEncodingException!", new Object[0]);
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
            return httpURLConnection;
        } catch (IOException unused9) {
            httpURLConnection = null;
            HiLog.error(TAG, "buildConnection caught IOException!", new Object[0]);
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
            return httpURLConnection;
        } catch (Throwable th3) {
            th = th3;
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream2);
            throw th;
        }
        return httpURLConnection;
    }
}

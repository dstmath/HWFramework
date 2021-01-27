package ohos.miscservices.httpaccess;

import android.webkit.MimeTypeMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;
import ohos.abilityshell.utils.AbilityContextUtils;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.httpaccess.data.FormFileData;
import ohos.miscservices.httpaccess.data.RequestData;
import ohos.miscservices.httpaccess.data.ResponseData;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.zson.ZSONArray;

public class HttpUploadImpl {
    private static final String APP_CACHE_DIRECTORY = "internal://cache/";
    private static final String CHARSET = "Charset";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final String LINE_END = "\r\n";
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
    private static final String PREFIX = "--";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "HttpUploadImpl");
    private static final String UTF_8 = "UTF-8";
    private Context appContext;
    private android.content.Context context;

    public HttpUploadImpl(Context context2) {
        this.appContext = context2;
        Object androidContext = AbilityContextUtils.getAndroidContext(context2);
        if (androidContext instanceof android.content.Context) {
            this.context = (android.content.Context) androidContext;
        }
    }

    public void httpUrlUpload(RequestData requestData, HttpProbe httpProbe) {
        Throwable th;
        BufferedOutputStream bufferedOutputStream;
        HiLog.debug(TAG, "start upload!", new Object[0]);
        ResponseData responseData = new ResponseData();
        BufferedInputStream bufferedInputStream = null;
        try {
            URLConnection openConnection = new URL(requestData.getUrl()).openConnection();
            if (openConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) openConnection;
                HttpUtils.setConnProperty(httpURLConnection, requestData, this.context);
                String method = requestData.getMethod();
                if ("".equals(method)) {
                    method = HttpConstant.HTTP_METHOD_POST;
                }
                httpURLConnection.setRequestMethod(method);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty(CHARSET, UTF_8);
                String uuid = UUID.randomUUID().toString();
                httpURLConnection.setRequestProperty(CONTENT_TYPE, "multipart/form-data;boundary=" + uuid);
                HttpUtils.addHeaders(httpURLConnection, requestData.getHeader());
                httpURLConnection.setChunkedStreamingMode(0);
                bufferedOutputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
                try {
                    addFormDatas(bufferedOutputStream, requestData, uuid);
                    addFormFileDatas(bufferedOutputStream, requestData, uuid);
                    bufferedOutputStream.write(("\r\n--" + uuid + PREFIX + LINE_END).getBytes(UTF_8));
                    bufferedOutputStream.flush();
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode != 200) {
                        HiLog.error(TAG, "connect error, response code : %{public}d!", Integer.valueOf(responseCode));
                        BufferedInputStream bufferedInputStream2 = new BufferedInputStream(httpURLConnection.getErrorStream());
                        try {
                            String str = new String(HttpUtils.parseInputStream(bufferedInputStream2), UTF_8);
                            responseData.setCode(responseCode);
                            responseData.setData(str);
                            httpProbe.onResponse(responseData);
                            HttpUtils.closeQuietly((InputStream) bufferedInputStream2);
                            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
                        } catch (MalformedURLException unused) {
                            bufferedInputStream = bufferedInputStream2;
                            HiLog.error(TAG, "caught MalformedURLException!", new Object[0]);
                            responseData.setCode(-1);
                            httpProbe.onResponse(responseData);
                            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
                        } catch (IOException unused2) {
                            bufferedInputStream = bufferedInputStream2;
                            HiLog.error(TAG, "caught IOException!", new Object[0]);
                            responseData.setCode(-1);
                            httpProbe.onResponse(responseData);
                            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedInputStream = bufferedInputStream2;
                            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
                            throw th;
                        }
                    } else {
                        BufferedInputStream bufferedInputStream3 = new BufferedInputStream(httpURLConnection.getInputStream());
                        responseData.setData(new String(HttpUtils.parseInputStream(bufferedInputStream3), UTF_8));
                        responseData.setHeaders(HttpUtils.analyzeResponseHeaders(httpURLConnection.getHeaderFields()));
                        responseData.setCode(responseCode);
                        httpProbe.onResponse(responseData);
                        HttpUtils.closeQuietly((InputStream) bufferedInputStream3);
                        HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
                    }
                } catch (MalformedURLException unused3) {
                    HiLog.error(TAG, "caught MalformedURLException!", new Object[0]);
                    responseData.setCode(-1);
                    httpProbe.onResponse(responseData);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
                } catch (IOException unused4) {
                    HiLog.error(TAG, "caught IOException!", new Object[0]);
                    responseData.setCode(-1);
                    httpProbe.onResponse(responseData);
                    HttpUtils.closeQuietly((InputStream) bufferedInputStream);
                    HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
                }
            } else {
                HiLog.error(TAG, "can not transfer to right connection!", new Object[0]);
                responseData.setCode(-1);
                httpProbe.onResponse(responseData);
                HttpUtils.closeQuietly((InputStream) null);
                HttpUtils.closeQuietly((OutputStream) null);
            }
        } catch (MalformedURLException unused5) {
            bufferedOutputStream = null;
            HiLog.error(TAG, "caught MalformedURLException!", new Object[0]);
            responseData.setCode(-1);
            httpProbe.onResponse(responseData);
            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
        } catch (IOException unused6) {
            bufferedOutputStream = null;
            HiLog.error(TAG, "caught IOException!", new Object[0]);
            responseData.setCode(-1);
            httpProbe.onResponse(responseData);
            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
        } catch (Throwable th3) {
            th = th3;
            HttpUtils.closeQuietly((InputStream) bufferedInputStream);
            HttpUtils.closeQuietly((OutputStream) bufferedOutputStream);
            throw th;
        }
    }

    private void addFormDatas(OutputStream outputStream, RequestData requestData, String str) {
        JSONArray parseArray = ZSONArray.parseArray(requestData.getData());
        if (!(parseArray == null || parseArray.size() == 0)) {
            for (int i = 0; i < parseArray.size(); i++) {
                JSONObject jSONObject = parseArray.getJSONObject(i);
                String string = jSONObject.getString("name");
                String string2 = jSONObject.getString("value");
                if (string == null || string2 == null || "".equals(string) || "".equals(string2)) {
                    HiLog.warn(TAG, "formdatas' name or value is null!", new Object[0]);
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(PREFIX + str + LINE_END + "Content-Disposition: form-data; name=\"" + string + "\"" + LINE_END);
                    stringBuffer.append(LINE_END);
                    StringBuilder sb = new StringBuilder();
                    sb.append(string2);
                    sb.append(LINE_END);
                    stringBuffer.append(sb.toString());
                    try {
                        outputStream.write(stringBuffer.toString().getBytes(UTF_8));
                    } catch (IOException unused) {
                        HiLog.error(TAG, "write formdatas caught IOException!", new Object[0]);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0123, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0128, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0129, code lost:
        r0.addSuppressed(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x012c, code lost:
        throw r2;
     */
    private void addFormFileDatas(OutputStream outputStream, RequestData requestData, String str) {
        String str2;
        List<FormFileData> files = requestData.getFiles();
        if (files != null && files.size() > 0) {
            for (FormFileData formFileData : files) {
                if (formFileData != null) {
                    String uri = formFileData.getUri();
                    if (uri.indexOf(APP_CACHE_DIRECTORY) != 0) {
                        HiLog.error(TAG, "upload file uri is not correct : %{public}s!", uri);
                    } else {
                        String name = "".equals(formFileData.getName()) ? "file" : formFileData.getName();
                        String fileNameByUri = "".equals(formFileData.getFileName()) ? getFileNameByUri(uri) : formFileData.getFileName();
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(PREFIX + str + LINE_END + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileNameByUri + "\"" + LINE_END);
                        StringBuilder sb = new StringBuilder();
                        sb.append("Content-Type: ");
                        sb.append(getMimeType(formFileData));
                        sb.append(LINE_END);
                        stringBuffer.append(sb.toString());
                        stringBuffer.append(LINE_END);
                        try {
                            outputStream.write(stringBuffer.toString().getBytes(UTF_8));
                        } catch (IOException unused) {
                            HiLog.error(TAG, "write formfiledatas prefix string caught IOException!", new Object[0]);
                        }
                        try {
                            str2 = this.appContext.getCacheDir().getCanonicalPath() + uri.substring(16);
                        } catch (IOException unused2) {
                            HiLog.error(TAG, "get formfiledatas filePath caught IOException!", new Object[0]);
                            str2 = "";
                        }
                        if ("".equals(str2) || str2.contains("..")) {
                            HiLog.error(TAG, "filePath illegal!", new Object[0]);
                        } else {
                            try {
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(str2));
                                byte[] bArr = new byte[8192];
                                while (true) {
                                    int read = bufferedInputStream.read(bArr);
                                    if (read == -1) {
                                        break;
                                    }
                                    outputStream.write(bArr, 0, read);
                                }
                                bufferedInputStream.close();
                            } catch (IOException unused3) {
                                HiLog.error(TAG, "write formfiledatas file caught IOException!", new Object[0]);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getMimeType(FormFileData formFileData) {
        String type = formFileData.getType();
        if (!"".equals(type)) {
            return getMimeWithSuffix(type);
        }
        String suffix = getSuffix(formFileData.getFileName());
        if (!"".equals(suffix)) {
            return getMimeWithSuffix(suffix);
        }
        String suffix2 = getSuffix(formFileData.getUri());
        return !"".equals(suffix2) ? getMimeWithSuffix(suffix2) : DEFAULT_MIME_TYPE;
    }

    private String getMimeWithSuffix(String str) {
        String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(str);
        if (mimeTypeFromExtension == null || mimeTypeFromExtension.isEmpty()) {
            mimeTypeFromExtension = DEFAULT_MIME_TYPE;
        }
        HiLog.debug(TAG, "final mimeType : %{public}s!", mimeTypeFromExtension);
        return mimeTypeFromExtension;
    }

    private String getSuffix(String str) {
        int lastIndexOf = str.lastIndexOf(".");
        return lastIndexOf > 0 ? str.substring(lastIndexOf + 1) : "";
    }

    private String getFileNameByUri(String str) {
        int lastIndexOf = str.lastIndexOf("/");
        return lastIndexOf > 0 ? str.substring(lastIndexOf + 1) : "";
    }
}

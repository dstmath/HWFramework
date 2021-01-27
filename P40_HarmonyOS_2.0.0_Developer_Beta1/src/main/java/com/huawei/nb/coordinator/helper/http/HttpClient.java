package com.huawei.nb.coordinator.helper.http;

import android.text.TextUtils;
import com.huawei.nb.coordinator.breakpoint.BreakpointResumeDownload;
import com.huawei.nb.coordinator.breakpoint.BreakpointResumeDownloadException;
import com.huawei.nb.coordinator.breakpoint.Range;
import com.huawei.nb.coordinator.helper.DataRequestListener;
import com.huawei.nb.coordinator.helper.FileDataRequestListener;
import com.huawei.nb.coordinator.helper.RefreshDataRequestListener;
import com.huawei.nb.coordinator.helper.RefreshResult;
import com.huawei.nb.coordinator.helper.verify.IVerifyVar;
import com.huawei.nb.coordinator.json.PackageMeta;
import com.huawei.nb.model.coordinator.CoordinatorAudit;
import com.huawei.nb.utils.JsonUtils;
import com.huawei.nb.utils.file.FileUtils;
import com.huawei.nb.utils.logger.DSLog;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public class HttpClient {
    private static final int BIG_BUFFER_SIZE = 4096;
    private static final int BUFFER_SIZE = 1024;
    public static final int DEFAULT_REQUEST = 0;
    public static final String DELETE_TYPE = "DELETE";
    private static final int END_OF_STREAM = -1;
    public static final int FRAGMENT_LOAD_REQUEST = 1;
    public static final String GET_TYPE = "GET";
    private static final String HEAD_TYPE = "HEAD";
    private static final String HTTPS_TYPE = "https";
    private static final String HTTP_TYPE = "http";
    private static final long MAX_DEFAULT_SIZE = 524288000;
    private static final int MAX_REQUEST_BODY_LENGTH = 819200;
    private static final int MAX_URL_LENGTH = 8192;
    private static final int NEED_NO_PERMISSION = -1;
    public static final String POST_TYPE = "POST";
    private static final String TAG = "HttpClient";
    public static final int TRANSFER_FILE_BREAKPOINT = 3;
    public static final int TRANSFER_FILE_REQUEST = 2;
    private String baseURL;
    private DataRequestListener dataRequestListener;
    private long dataTrafficSize;
    private String fileName;
    private String fileSavePath;
    private HttpRequest request;
    private String requestBody;
    private Map<String, String> requestHeaders;
    private String requestMethod;
    private int requestMode;
    private HttpResponse response;
    private String tmpFileDir;

    public HttpClient() {
        this.baseURL = null;
        this.requestBody = null;
        this.requestMethod = null;
        this.request = null;
        this.requestHeaders = null;
        this.response = new HttpResponse();
        this.dataTrafficSize = MAX_DEFAULT_SIZE;
        this.requestMode = 0;
    }

    public HttpClient(int i, String str, String str2) {
        this.baseURL = null;
        this.requestBody = null;
        this.requestMethod = null;
        this.request = null;
        this.requestHeaders = null;
        this.response = new HttpResponse();
        this.dataTrafficSize = MAX_DEFAULT_SIZE;
        this.requestMode = i;
        this.fileSavePath = str;
        this.fileName = str2;
    }

    public HttpClient setDataTrafficSize(Long l) {
        this.dataTrafficSize = l.longValue();
        return this;
    }

    public HttpClient setDataRequestListener(DataRequestListener dataRequestListener2) {
        this.dataRequestListener = dataRequestListener2;
        return this;
    }

    public HttpClient setTmpFileDir(String str) {
        this.tmpFileDir = str;
        return this;
    }

    private boolean isFileParamsValid() throws IOException {
        if (TextUtils.isEmpty(this.fileName) || TextUtils.isEmpty(this.fileSavePath)) {
            setErrorHttpResponse(-5, "HttpClient File save path or fileName is empty !");
            return false;
        }
        File file = new File(this.fileSavePath);
        if (file.exists() && file.isDirectory()) {
            return createFile(this.fileSavePath, this.fileName);
        }
        if (file.exists()) {
            setErrorHttpResponse(-5, "HttpClient File save path is not a valid directory!");
            return false;
        } else if (file.mkdirs()) {
            return createFile(this.fileSavePath, this.fileName);
        } else {
            setErrorHttpResponse(-5, "HttpClient Fail to create save path!");
            return false;
        }
    }

    private boolean createDirectory(String str) throws IOException {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        File file = FileUtils.getFile(str);
        if (file.exists()) {
            if (file.isDirectory()) {
                return true;
            }
            setErrorHttpResponse(-5, "HttpClient FileSavePath is error");
            return false;
        } else if (file.mkdirs()) {
            return true;
        } else {
            setErrorHttpResponse(-5, "HttpClient Create Save Path error !");
            return false;
        }
    }

    private boolean createFile(String str, String str2) throws IOException {
        if (!TextUtils.isEmpty(str2)) {
            return createFile(FileUtils.getFile(str, str2));
        }
        DSLog.e("HttpClient fileName is empty!", new Object[0]);
        return false;
    }

    private boolean deleteDir(File file) {
        String[] list;
        if (file == null) {
            return false;
        }
        if (file.isDirectory() && (list = file.list()) != null) {
            for (String str : list) {
                try {
                    if (!deleteDir(FileUtils.getFile(file.getCanonicalPath(), str))) {
                        DSLog.e("HttpClient delete " + str + " failed.", new Object[0]);
                        return false;
                    }
                } catch (IOException e) {
                    DSLog.e("HttpClient Fail to delete " + str + ", error: " + e.getMessage(), new Object[0]);
                    return false;
                }
            }
        }
        return file.delete();
    }

    private boolean createFile(File file) {
        boolean z;
        if (file == null) {
            DSLog.e("HttpClient file is null.", new Object[0]);
            return false;
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                z = deleteDir(file);
            } else {
                z = file.delete();
            }
            if (!z) {
                DSLog.e("HttpClient Delete old file error !", new Object[0]);
                return false;
            }
        }
        try {
            if (file.createNewFile()) {
                return true;
            }
            DSLog.e("HttpClient Create file error !", new Object[0]);
            return false;
        } catch (IOException e) {
            this.response.setHttpExceptionMsg(e.getMessage());
            DSLog.e("HttpClient Create file error:" + e.getMessage() + "!", new Object[0]);
            return false;
        }
    }

    public HttpResponse syncExecute() {
        return syncExecute(null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0055, code lost:
        if (r0 != null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006a, code lost:
        if (0 == 0) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006c, code lost:
        r0.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0071, code lost:
        return r5.response;
     */
    public HttpResponse syncExecute(CoordinatorAudit coordinatorAudit) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = getUrl();
            if (url == null) {
                return setErrorHttpResponse(-2, "HttpClient url may be too long. Request stops.");
            }
            if (!isRequestBodyValid()) {
                return setErrorHttpResponse(-2, "HttpClient request body may be too long. Request stops.");
            }
            this.response.setUrl(url.toString());
            httpURLConnection = parseUrlConnection(url.openConnection());
            if (httpURLConnection == null) {
                HttpResponse errorHttpResponse = setErrorHttpResponse(-2, " connection is illegal. Request stops.");
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                return errorHttpResponse;
            }
            initConnection(httpURLConnection);
            if (this.requestMethod.equals(POST_TYPE)) {
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                addPostRequestBody(httpURLConnection);
            }
            requestAccordingMode(httpURLConnection, coordinatorAudit);
        } catch (IOException e) {
            this.response.setHttpExceptionMsg(e.getMessage());
            setErrorHttpResponse(-5, "HttpClient caught IOException, error message!");
        } catch (Throwable th) {
            if (0 != 0) {
                httpURLConnection.disconnect();
            }
            throw th;
        }
    }

    private void requestAccordingMode(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit) throws IOException {
        DSLog.d("HttpClient request mode = " + this.requestMode, new Object[0]);
        int i = this.requestMode;
        if (i == 0) {
            requestForResponseBody(httpURLConnection, coordinatorAudit);
        } else if (i == 1) {
            requestForFragmentLoad(httpURLConnection, coordinatorAudit);
        } else if (i != 2) {
            if (i != 3) {
                DSLog.e("HttpClient requestMode:" + this.requestMode + " does not exist!", new Object[0]);
                return;
            }
            requestForTransferBreakpointFile(httpURLConnection, coordinatorAudit);
        } else if (isFileParamsValid()) {
            requestForFileTransfer(httpURLConnection, coordinatorAudit);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:65:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    private void requestForTransferBreakpointFile(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit) {
        Throwable th;
        BreakpointResumeDownload breakpointResumeDownload;
        boolean z;
        String str;
        BreakpointResumeDownloadException e;
        boolean z2 = false;
        String str2 = null;
        try {
            breakpointResumeDownload = new BreakpointResumeDownload(httpURLConnection.getURL().toString(), this.tmpFileDir, this.fileSavePath, this.fileName);
            try {
                str2 = breakpointResumeDownload.generateTmpFilePath();
                if (TextUtils.isEmpty(str2)) {
                    setErrorHttpResponse(-2, " Failed to generate tmp file, the path is empty.");
                    TextUtils.isEmpty(str2);
                } else if (!BreakpointResumeDownload.addPathToSet(str2)) {
                    setErrorHttpResponse(-17, " Failed to add this request to set, maybe it is duplicated.");
                    TextUtils.isEmpty(str2);
                } else {
                    z = true;
                    try {
                        Map<String, List<String>> requestForHeader = requestForHeader(coordinatorAudit);
                        if (requestForHeader.isEmpty()) {
                            setErrorHttpResponse(-16, " response header is empty.");
                            if (!TextUtils.isEmpty(str2)) {
                                BreakpointResumeDownload.removePathFromSet(str2);
                                return;
                            }
                            return;
                        }
                        Range calculatedRange = breakpointResumeDownload.getCalculatedRange(requestForHeader);
                        if (calculatedRange.isAllowedToSetRange()) {
                            DSLog.d("HttpClient range min = " + calculatedRange.getRangeMin() + " range max = " + calculatedRange.getRangeMax(), new Object[0]);
                            httpURLConnection.setRequestProperty("Range", "bytes=" + calculatedRange.getRangeMin() + "-" + calculatedRange.getRangeMax());
                        }
                        if (calculatedRange.isAllowedToSetRange() || calculatedRange.getRangeMin() == 0) {
                            downloadFileFromBreakpoint(httpURLConnection, coordinatorAudit, breakpointResumeDownload);
                            breakpointResumeDownload.postHandle(this.response);
                        } else {
                            requestForFileTransfer(httpURLConnection, coordinatorAudit);
                        }
                        if (!TextUtils.isEmpty(str2)) {
                            BreakpointResumeDownload.removePathFromSet(str2);
                        }
                    } catch (BreakpointResumeDownloadException e2) {
                        e = e2;
                        str = str2;
                        z2 = true;
                        str2 = breakpointResumeDownload;
                        try {
                            setErrorHttpResponse(e.getErrorCode(), e.getMessage());
                            if (str2 == null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            z = z2;
                            breakpointResumeDownload = str2;
                            str2 = str;
                            BreakpointResumeDownload.removePathFromSet(str2);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        BreakpointResumeDownload.removePathFromSet(str2);
                        throw th;
                    }
                }
            } catch (BreakpointResumeDownloadException e3) {
                e = e3;
                str = null;
                str2 = breakpointResumeDownload;
                setErrorHttpResponse(e.getErrorCode(), e.getMessage());
                if (str2 == null) {
                }
            } catch (Throwable th4) {
                th = th4;
                z = false;
                BreakpointResumeDownload.removePathFromSet(str2);
                throw th;
            }
        } catch (BreakpointResumeDownloadException e4) {
            e = e4;
            str = null;
            setErrorHttpResponse(e.getErrorCode(), e.getMessage());
            if (str2 == null && !TextUtils.isEmpty(str) && z2) {
                BreakpointResumeDownload.removePathFromSet(str);
            }
        } catch (Throwable th5) {
            th = th5;
            z = false;
            breakpointResumeDownload = null;
            if (breakpointResumeDownload != null && !TextUtils.isEmpty(str2) && z) {
                BreakpointResumeDownload.removePathFromSet(str2);
            }
            throw th;
        }
    }

    private HttpURLConnection parseUrlConnection(URLConnection uRLConnection) {
        if (this.baseURL.startsWith(HTTPS_TYPE) && (uRLConnection instanceof HttpsURLConnection)) {
            return (HttpsURLConnection) uRLConnection;
        }
        if (this.baseURL.startsWith(HTTP_TYPE) && (uRLConnection instanceof HttpURLConnection)) {
            return (HttpURLConnection) uRLConnection;
        }
        DSLog.e("Connection type is illegal. Request stops.", new Object[0]);
        return null;
    }

    public HttpClient newCall(HttpRequest httpRequest) {
        this.baseURL = httpRequest.getUrl();
        this.request = httpRequest;
        this.requestBody = httpRequest.getRequestBodyString();
        this.requestHeaders = httpRequest.getRequestHeaders();
        this.requestMethod = httpRequest.getRequestMethod();
        return this;
    }

    private URL getUrl() throws MalformedURLException {
        if (!GET_TYPE.equals(this.requestMethod) || TextUtils.isEmpty(this.requestBody)) {
            return new URL(this.baseURL);
        }
        if ((this.baseURL + "?" + this.requestBody).length() > MAX_URL_LENGTH) {
            return null;
        }
        return new URL(this.baseURL + "?" + this.requestBody);
    }

    private void initConnection(HttpURLConnection httpURLConnection) throws ProtocolException {
        httpURLConnection.setRequestMethod(this.requestMethod);
        httpURLConnection.setConnectTimeout(this.request.getConnectTimeout());
        httpURLConnection.setReadTimeout(this.request.getReadTimeout());
        for (Map.Entry<String, String> entry : this.requestHeaders.entrySet()) {
            httpURLConnection.setRequestProperty(checkHeader(entry.getKey()), checkHeader(entry.getValue()));
        }
    }

    private String checkHeader(String str) {
        if (!TextUtils.isEmpty(str)) {
            return str.replace("\n", "").replace("\r", "");
        }
        return "";
    }

    private void addPostRequestBody(HttpURLConnection httpURLConnection) {
        OutputStream outputStream;
        BufferedOutputStream bufferedOutputStream;
        Throwable th;
        IOException e;
        if (!TextUtils.isEmpty(this.requestBody)) {
            try {
                outputStream = httpURLConnection.getOutputStream();
                try {
                    if (isOutputStreamEmpty(outputStream)) {
                        FileUtils.closeCloseable(outputStream);
                        FileUtils.closeCloseable(null);
                        return;
                    }
                    bufferedOutputStream = new BufferedOutputStream(outputStream);
                    try {
                        bufferedOutputStream.write(this.requestBody.getBytes("utf-8"));
                        bufferedOutputStream.flush();
                    } catch (IOException e2) {
                        e = e2;
                        try {
                            DSLog.e("HttpClient addPostRequestBody IOException : " + e.getMessage(), new Object[0]);
                            this.response.setHttpExceptionMsg(e.getMessage());
                            FileUtils.closeCloseable(outputStream);
                            FileUtils.closeCloseable(bufferedOutputStream);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    FileUtils.closeCloseable(outputStream);
                    FileUtils.closeCloseable(bufferedOutputStream);
                } catch (IOException e3) {
                    bufferedOutputStream = null;
                    e = e3;
                    DSLog.e("HttpClient addPostRequestBody IOException : " + e.getMessage(), new Object[0]);
                    this.response.setHttpExceptionMsg(e.getMessage());
                    FileUtils.closeCloseable(outputStream);
                    FileUtils.closeCloseable(bufferedOutputStream);
                } catch (Throwable th3) {
                    bufferedOutputStream = null;
                    th = th3;
                    FileUtils.closeCloseable(outputStream);
                    FileUtils.closeCloseable(bufferedOutputStream);
                    throw th;
                }
            } catch (IOException e4) {
                bufferedOutputStream = null;
                e = e4;
                outputStream = null;
                DSLog.e("HttpClient addPostRequestBody IOException : " + e.getMessage(), new Object[0]);
                this.response.setHttpExceptionMsg(e.getMessage());
                FileUtils.closeCloseable(outputStream);
                FileUtils.closeCloseable(bufferedOutputStream);
            } catch (Throwable th4) {
                bufferedOutputStream = null;
                th = th4;
                outputStream = null;
                FileUtils.closeCloseable(outputStream);
                FileUtils.closeCloseable(bufferedOutputStream);
                throw th;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:58:? A[RETURN, SYNTHETIC] */
    private void requestForResponseBody(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit) {
        InputStream inputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable th;
        IOException e;
        try {
            int responseCode = httpURLConnection.getResponseCode();
            String fullfillEmptyMessage = fullfillEmptyMessage(httpURLConnection.getResponseMessage());
            this.response.setResponseMsg(fullfillEmptyMessage);
            if (!isContentLengthValid(httpURLConnection, coordinatorAudit)) {
                FileUtils.closeCloseable(null);
                FileUtils.closeCloseable(null);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    return;
                }
                return;
            }
            if (responseCode == 200) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
                try {
                    setErrorHttpResponse(responseCode, fullfillEmptyMessage);
                } catch (IOException e2) {
                    byteArrayOutputStream = null;
                    e = e2;
                    try {
                        dealIOExceptionWhenConnecting(e);
                        FileUtils.closeCloseable(inputStream);
                        FileUtils.closeCloseable(byteArrayOutputStream);
                        if (httpURLConnection == null) {
                        }
                        httpURLConnection.disconnect();
                    } catch (Throwable th2) {
                        th = th2;
                        FileUtils.closeCloseable(inputStream);
                        FileUtils.closeCloseable(byteArrayOutputStream);
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    byteArrayOutputStream = null;
                    th = th3;
                    FileUtils.closeCloseable(inputStream);
                    FileUtils.closeCloseable(byteArrayOutputStream);
                    if (httpURLConnection != null) {
                    }
                    throw th;
                }
            }
            if (isInputStreamEmpty(0, inputStream)) {
                FileUtils.closeCloseable(inputStream);
                FileUtils.closeCloseable(null);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    return;
                }
                return;
            }
            byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byte[] bArr = new byte[BUFFER_SIZE];
                long j = 0;
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read != -1) {
                        j += (long) read;
                        if (j <= this.dataTrafficSize) {
                            byteArrayOutputStream.write(bArr, 0, read);
                        } else {
                            throw new IOException("file'size is larger than " + this.dataTrafficSize + " downloadedLength = " + j);
                        }
                    } else {
                        this.response.setResponseSize(j);
                        String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
                        this.response.setStatusCode(responseCode);
                        this.response.setResponseString(byteArrayOutputStream2);
                        FileUtils.closeCloseable(inputStream);
                        FileUtils.closeCloseable(byteArrayOutputStream);
                        if (httpURLConnection == null) {
                            return;
                        }
                    }
                }
            } catch (IOException e3) {
                e = e3;
                dealIOExceptionWhenConnecting(e);
                FileUtils.closeCloseable(inputStream);
                FileUtils.closeCloseable(byteArrayOutputStream);
                if (httpURLConnection == null) {
                    return;
                }
                httpURLConnection.disconnect();
            }
            httpURLConnection.disconnect();
        } catch (IOException e4) {
            byteArrayOutputStream = null;
            e = e4;
            inputStream = null;
            dealIOExceptionWhenConnecting(e);
            FileUtils.closeCloseable(inputStream);
            FileUtils.closeCloseable(byteArrayOutputStream);
            if (httpURLConnection == null) {
            }
            httpURLConnection.disconnect();
        } catch (Throwable th4) {
            byteArrayOutputStream = null;
            th = th4;
            inputStream = null;
            FileUtils.closeCloseable(inputStream);
            FileUtils.closeCloseable(byteArrayOutputStream);
            if (httpURLConnection != null) {
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0085, code lost:
        if (r11.response.getStatusCode() == -7) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a4, code lost:
        if (r11.response.getStatusCode() == -7) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a6, code lost:
        com.huawei.nb.utils.logger.DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ac, code lost:
        checkFileLength();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b6  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00c7  */
    private void requestForFileTransfer(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit) {
        InputStream inputStream;
        Throwable th;
        IOException e;
        RefreshResult refreshResult = new RefreshResult();
        try {
            File file = FileUtils.getFile(this.fileSavePath, this.fileName);
            if (!isConnectResponseValid(httpURLConnection, coordinatorAudit)) {
                FileUtils.closeCloseable(null);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (this.response.getStatusCode() == -7) {
                    DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
                } else {
                    checkFileLength();
                }
            } else {
                inputStream = httpURLConnection.getInputStream();
                try {
                    if (isInputStreamEmpty(2, inputStream)) {
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                        if (this.response.getStatusCode() == -7) {
                            DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
                        } else {
                            checkFileLength();
                        }
                    } else {
                        this.response.setDownloadStart(true);
                        this.response.setHeaderFields(httpURLConnection.getHeaderFields());
                        readInputStreamToFile(file, inputStream, (long) 0, refreshResult, StandardOpenOption.WRITE);
                        this.response.setStatusCode(httpURLConnection.getResponseCode());
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                    }
                } catch (IOException e2) {
                    e = e2;
                    try {
                        dealIOExceptionWhenConnecting(e);
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                        if (this.response.getStatusCode() != -7) {
                            DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
                        } else {
                            checkFileLength();
                        }
                        throw th;
                    }
                }
            }
        } catch (IOException e3) {
            e = e3;
            inputStream = null;
            dealIOExceptionWhenConnecting(e);
            FileUtils.closeCloseable(inputStream);
            if (httpURLConnection != null) {
            }
        } catch (Throwable th3) {
            th = th3;
            inputStream = null;
            FileUtils.closeCloseable(inputStream);
            if (httpURLConnection != null) {
            }
            if (this.response.getStatusCode() != -7) {
            }
            throw th;
        }
    }

    private void checkFileLength() {
        try {
            String headerValue = this.response.getHeaderValue("Content-Length");
            if (TextUtils.isEmpty(headerValue)) {
                DSLog.e("HttpClient Response content-length is empty.", new Object[0]);
                return;
            }
            File file = FileUtils.getFile(this.fileSavePath, this.fileName);
            if (!file.exists()) {
                setErrorHttpResponse(-19, " downloaded file is not exist!");
                return;
            }
            try {
                if (file.length() != Long.parseLong(headerValue)) {
                    setErrorHttpResponse(-19, "File size error!File size:" + file.length() + ",Total size:" + headerValue);
                    return;
                }
                HttpResponse httpResponse = this.response;
                httpResponse.setResponseString("{\"data\":\"File download success!File size:" + file.length() + ",Total size:" + headerValue + "\"}");
            } catch (NumberFormatException e) {
                setErrorHttpResponse(-15, " Fail to parse content-length. error: " + e.getMessage());
            }
        } catch (IOException e2) {
            setErrorHttpResponse(-15, " Get File IOException : " + e2.getMessage());
        }
    }

    private void dealIOExceptionWhenConnecting(IOException iOException) {
        if (TextUtils.isEmpty(iOException.getMessage())) {
            setErrorHttpResponse(-2, " IOException, msg is empty.");
        } else if (iOException.getMessage().startsWith("Unable to resolve host")) {
            setErrorHttpResponse(-6, " network error: Unable to resolve host. ");
        } else if (iOException.getMessage().startsWith("failed to connect to") || iOException.getMessage().startsWith("Failed to connect to")) {
            setErrorHttpResponse(-9, " connect cloud error");
        } else if (iOException.getMessage().startsWith(IVerifyVar.TIME_OUT_HEADER)) {
            setErrorHttpResponse(-13, " timeout for transfer file: " + iOException.getMessage());
        } else {
            this.response.setHttpExceptionMsg(iOException.getMessage());
            DSLog.e("HttpClient executeResponseForTransferFile IOException : " + iOException.getMessage(), new Object[0]);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x00fb  */
    /* JADX WARNING: Removed duplicated region for block: B:67:? A[RETURN, SYNTHETIC] */
    private void requestForFragmentLoad(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit) {
        DataInputStream dataInputStream;
        InputStream inputStream;
        Throwable th;
        IOException e;
        DSLog.d("HttpClient requestForFragmentLoad ", new Object[0]);
        DataInputStream dataInputStream2 = null;
        try {
            if (!createDirectory(this.fileSavePath)) {
                FileUtils.closeCloseable(null);
                FileUtils.closeCloseable(null);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } else if (!isConnectResponseValid(httpURLConnection, coordinatorAudit)) {
                FileUtils.closeCloseable(null);
                FileUtils.closeCloseable(null);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } else {
                inputStream = httpURLConnection.getInputStream();
                try {
                    if (isInputStreamEmpty(1, inputStream)) {
                        FileUtils.closeCloseable(null);
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                            return;
                        }
                        return;
                    }
                    dataInputStream = new DataInputStream(inputStream);
                    try {
                        byte[] bArr = new byte[calculateMetaLength(dataInputStream)];
                        int read = inputStream.read(bArr);
                        if (read == -1) {
                            setErrorHttpResponse(-5, "Failed to get metaString info from connection!");
                            FileUtils.closeCloseable(dataInputStream);
                            FileUtils.closeCloseable(inputStream);
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                                return;
                            }
                            return;
                        }
                        String bytesToString = bytesToString("UTF-8", bArr);
                        List<PackageMeta.PackagesBean> readPackageListFromMetaString = readPackageListFromMetaString(bytesToString);
                        int size = readPackageListFromMetaString.size();
                        long j = (long) read;
                        for (int i = 0; i < size; i++) {
                            PackageMeta.PackagesBean packagesBean = readPackageListFromMetaString.get(i);
                            String name = packagesBean.getName();
                            j += requestForSingleFragment(packagesBean, FileUtils.getFile(this.fileSavePath, name), j, inputStream);
                            if (this.dataRequestListener instanceof FileDataRequestListener) {
                                ((FileDataRequestListener) this.dataRequestListener).onDownloadSuccess(name, String.valueOf(i + 1), bytesToString);
                            }
                        }
                        this.response.setResponseSize(j);
                        this.response.setResponseString("{\"data\":\"Fragment load success!\"}");
                        this.response.setStatusCode(httpURLConnection.getResponseCode());
                        FileUtils.closeCloseable(dataInputStream);
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection == null) {
                            return;
                        }
                        httpURLConnection.disconnect();
                    } catch (IOException e2) {
                        e = e2;
                        dataInputStream2 = dataInputStream;
                        try {
                            this.response.setHttpExceptionMsg(e.getMessage());
                            dealIOExceptionWhenConnecting(e);
                            FileUtils.closeCloseable(dataInputStream2);
                            FileUtils.closeCloseable(inputStream);
                            if (httpURLConnection == null) {
                                return;
                            }
                            httpURLConnection.disconnect();
                        } catch (Throwable th2) {
                            th = th2;
                            dataInputStream = dataInputStream2;
                            FileUtils.closeCloseable(dataInputStream);
                            FileUtils.closeCloseable(inputStream);
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        FileUtils.closeCloseable(dataInputStream);
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                        }
                        throw th;
                    }
                } catch (IOException e3) {
                    e = e3;
                    this.response.setHttpExceptionMsg(e.getMessage());
                    dealIOExceptionWhenConnecting(e);
                    FileUtils.closeCloseable(dataInputStream2);
                    FileUtils.closeCloseable(inputStream);
                    if (httpURLConnection == null) {
                    }
                    httpURLConnection.disconnect();
                }
            }
        } catch (IOException e4) {
            e = e4;
            inputStream = null;
            this.response.setHttpExceptionMsg(e.getMessage());
            dealIOExceptionWhenConnecting(e);
            FileUtils.closeCloseable(dataInputStream2);
            FileUtils.closeCloseable(inputStream);
            if (httpURLConnection == null) {
            }
            httpURLConnection.disconnect();
        } catch (Throwable th4) {
            th = th4;
            inputStream = null;
            dataInputStream = null;
            FileUtils.closeCloseable(dataInputStream);
            FileUtils.closeCloseable(inputStream);
            if (httpURLConnection != null) {
            }
            throw th;
        }
    }

    private String bytesToString(String str, byte[] bArr) throws CharacterCodingException {
        return Charset.forName(str).newDecoder().decode(ByteBuffer.wrap(bArr)).toString();
    }

    private int calculateMetaLength(DataInputStream dataInputStream) throws IOException {
        int readInt = dataInputStream.readInt();
        if (readInt <= 4096) {
            return readInt;
        }
        throw new IOException("Meta length is " + readInt + ", which is too long!");
    }

    private List<PackageMeta.PackagesBean> readPackageListFromMetaString(String str) throws IOException {
        if (JsonUtils.isValidJson(str)) {
            PackageMeta packageMeta = (PackageMeta) JsonUtils.parse(str, PackageMeta.class);
            if (packageMeta != null) {
                List<PackageMeta.PackagesBean> packages = packageMeta.getPackages();
                if (packages != null) {
                    return packages;
                }
                throw new IOException("Failed to get packageList from PackageMeta!");
            }
            throw new IOException("Failed to get PackageMeta info from metaString!");
        }
        throw new IOException("Json string is not valid!");
    }

    private boolean isConnectResponseValid(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit) throws IOException {
        int responseCode = httpURLConnection.getResponseCode();
        String fullfillEmptyMessage = fullfillEmptyMessage(httpURLConnection.getResponseMessage());
        DSLog.d("HttpClient response code of file's transfer : " + responseCode, new Object[0]);
        this.response.setResponseMsg(fullfillEmptyMessage);
        if (responseCode == 200) {
            return isContentLengthValid(httpURLConnection, coordinatorAudit);
        }
        this.response.setStatusCode(responseCode);
        return false;
    }

    private long requestForSingleFragment(PackageMeta.PackagesBean packagesBean, File file, long j, InputStream inputStream) throws IOException {
        OutputStream outputStream;
        Throwable th;
        BufferedOutputStream bufferedOutputStream;
        int i;
        DSLog.d("HttpClient requestForSingleFragment ", new Object[0]);
        if (createFile(file)) {
            int parseInt = Integer.parseInt(packagesBean.getSize());
            try {
                outputStream = getSafeOutputStream(file, false, StandardOpenOption.WRITE);
                try {
                    bufferedOutputStream = new BufferedOutputStream(outputStream);
                } catch (Throwable th2) {
                    th = th2;
                    bufferedOutputStream = null;
                    FileUtils.closeCloseable(bufferedOutputStream);
                    FileUtils.closeCloseable(outputStream);
                    throw th;
                }
                try {
                    byte[] bArr = new byte[BUFFER_SIZE];
                    long j2 = j;
                    int i2 = 0;
                    while (i2 < parseInt) {
                        if (i2 + BUFFER_SIZE <= parseInt) {
                            i = inputStream.read(bArr);
                            if (i != -1) {
                                bufferedOutputStream.write(bArr, 0, i);
                                j2 += (long) i;
                            }
                        } else {
                            byte[] bArr2 = new byte[(parseInt - i2)];
                            int read = inputStream.read(bArr2);
                            if (read != -1) {
                                bufferedOutputStream.write(bArr2, 0, read);
                                j2 += (long) read;
                            }
                            i = read;
                        }
                        if (j2 > this.dataTrafficSize) {
                            throw new IOException(" file size is larger than " + this.dataTrafficSize);
                        } else if (i != -1) {
                            i2 += i;
                        }
                    }
                    bufferedOutputStream.flush();
                    FileUtils.closeCloseable(bufferedOutputStream);
                    FileUtils.closeCloseable(outputStream);
                    return j2;
                } catch (Throwable th3) {
                    th = th3;
                    FileUtils.closeCloseable(bufferedOutputStream);
                    FileUtils.closeCloseable(outputStream);
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                outputStream = null;
                bufferedOutputStream = null;
                FileUtils.closeCloseable(bufferedOutputStream);
                FileUtils.closeCloseable(outputStream);
                throw th;
            }
        } else {
            throw new IOException("Failed to create file!");
        }
    }

    private Map<String, List<String>> requestForHeader(CoordinatorAudit coordinatorAudit) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = getUrl();
            if (url == null) {
                return new HashMap();
            }
            HttpURLConnection parseUrlConnection = parseUrlConnection(url.openConnection());
            if (parseUrlConnection == null) {
                setErrorHttpResponse(-2, " connection is illegal. Request stops.");
                HashMap hashMap = new HashMap();
                if (parseUrlConnection != null) {
                    parseUrlConnection.disconnect();
                }
                return hashMap;
            }
            initConnection(parseUrlConnection);
            parseUrlConnection.setRequestMethod(HEAD_TYPE);
            int responseCode = parseUrlConnection.getResponseCode();
            String fullfillEmptyMessage = fullfillEmptyMessage(parseUrlConnection.getResponseMessage());
            DSLog.d("HttpClient response code of HEAD request : " + responseCode, new Object[0]);
            this.response.setResponseMsg(fullfillEmptyMessage);
            if (responseCode != 200 || isContentLengthValid(parseUrlConnection, coordinatorAudit)) {
                if (parseUrlConnection != null) {
                    parseUrlConnection.disconnect();
                }
                return parseUrlConnection.getHeaderFields();
            }
            HashMap hashMap2 = new HashMap();
            if (parseUrlConnection != null) {
                parseUrlConnection.disconnect();
            }
            return hashMap2;
        } catch (IOException e) {
            dealIOExceptionWhenConnecting(e);
            HashMap hashMap3 = new HashMap();
            if (0 != 0) {
                httpURLConnection.disconnect();
            }
            return hashMap3;
        } catch (Throwable th) {
            if (0 != 0) {
                httpURLConnection.disconnect();
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x00cd  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e3  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ee  */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A[RETURN, SYNTHETIC] */
    private void downloadFileFromBreakpoint(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit, BreakpointResumeDownload breakpointResumeDownload) {
        Throwable th;
        IOException e;
        RefreshResult refreshResult = new RefreshResult();
        InputStream inputStream = null;
        try {
            File tmpFile = breakpointResumeDownload.getTmpFile();
            long length = tmpFile.length();
            int responseCode = httpURLConnection.getResponseCode();
            String fullfillEmptyMessage = fullfillEmptyMessage(httpURLConnection.getResponseMessage());
            DSLog.d("HttpClient response code of breakpoint file's transfer : " + responseCode, new Object[0]);
            this.response.setResponseMsg(fullfillEmptyMessage);
            if (BreakpointResumeDownload.isFailResponse(tmpFile.length(), responseCode)) {
                setErrorHttpResponse(responseCode, fullfillEmptyMessage);
                FileUtils.closeCloseable(null);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (this.response.getStatusCode() == -7) {
                    DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
                }
            } else if (!isContentLengthValid(httpURLConnection, coordinatorAudit)) {
                FileUtils.closeCloseable(null);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (this.response.getStatusCode() == -7) {
                    DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
                }
            } else {
                InputStream inputStream2 = httpURLConnection.getInputStream();
                try {
                    this.response.setDownloadStart(true);
                    this.response.setHeaderFields(httpURLConnection.getHeaderFields());
                    if (!breakpointResumeDownload.updateHeaderMap(httpURLConnection.getHeaderFields())) {
                        setErrorHttpResponse(-16, " Failed to use GET response's header to update header map.");
                    }
                    readInputStreamToFile(tmpFile, inputStream2, length, refreshResult, StandardOpenOption.APPEND);
                    this.response.setStatusCode(responseCode);
                    FileUtils.closeCloseable(inputStream2);
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    if (this.response.getStatusCode() != -7) {
                        return;
                    }
                } catch (IOException e2) {
                    e = e2;
                    inputStream = inputStream2;
                    try {
                        dealIOExceptionWhenConnecting(e);
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                        }
                        if (this.response.getStatusCode() != -7) {
                        }
                        DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
                    } catch (Throwable th2) {
                        th = th2;
                        FileUtils.closeCloseable(inputStream);
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                        if (this.response.getStatusCode() == -7) {
                            DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inputStream2;
                    FileUtils.closeCloseable(inputStream);
                    if (httpURLConnection != null) {
                    }
                    if (this.response.getStatusCode() == -7) {
                    }
                    throw th;
                }
                DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
            }
        } catch (IOException e3) {
            e = e3;
            dealIOExceptionWhenConnecting(e);
            FileUtils.closeCloseable(inputStream);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (this.response.getStatusCode() != -7) {
                return;
            }
            DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
        }
    }

    private void readInputStreamToFile(File file, InputStream inputStream, long j, RefreshResult refreshResult, StandardOpenOption standardOpenOption) throws IOException {
        BufferedOutputStream bufferedOutputStream;
        Throwable th;
        OutputStream outputStream;
        try {
            outputStream = getSafeOutputStream(file, false, standardOpenOption);
            try {
                bufferedOutputStream = new BufferedOutputStream(outputStream);
                try {
                    refreshResult.setFinished(false);
                    refreshResult.setDeltaSize(0);
                    refreshResult.setIndex(0);
                    byte[] bArr = new byte[BUFFER_SIZE];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read != -1) {
                            bufferedOutputStream.write(bArr, 0, read);
                            long j2 = (long) read;
                            j += j2;
                            if (j > this.dataTrafficSize) {
                                throw new IOException("file'size is larger than " + this.dataTrafficSize + ", downloadedLength = " + j);
                            } else if (this.dataRequestListener instanceof RefreshDataRequestListener) {
                                refreshResult.setDeltaSize(j2);
                                refreshResult.increaseIndex();
                                refreshResult.setDownloadedSize(j);
                                if (!((RefreshDataRequestListener) this.dataRequestListener).onRefresh(refreshResult)) {
                                    String str = "download will stop to refresh. Downloaded length:" + j;
                                    setErrorHttpResponse(-7, str);
                                    throw new IOException(str);
                                }
                            }
                        } else {
                            this.response.setResponseSize(j);
                            bufferedOutputStream.flush();
                            if (this.dataRequestListener instanceof RefreshDataRequestListener) {
                                refreshResult.setDeltaSize(0);
                                refreshResult.setDownloadedSize(j);
                                refreshResult.setFinished(true);
                                ((RefreshDataRequestListener) this.dataRequestListener).onRefresh(refreshResult);
                            }
                            FileUtils.closeCloseable(bufferedOutputStream);
                            FileUtils.closeCloseable(outputStream);
                            return;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    FileUtils.closeCloseable(bufferedOutputStream);
                    FileUtils.closeCloseable(outputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedOutputStream = null;
                FileUtils.closeCloseable(bufferedOutputStream);
                FileUtils.closeCloseable(outputStream);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            outputStream = null;
            bufferedOutputStream = null;
            FileUtils.closeCloseable(bufferedOutputStream);
            FileUtils.closeCloseable(outputStream);
            throw th;
        }
    }

    private static OutputStream getSafeOutputStream(File file, boolean z, StandardOpenOption standardOpenOption) throws IOException {
        return FileUtils.openOutputStream(file, -1, -1, z, false, standardOpenOption);
    }

    private boolean isInputStreamEmpty(int i, InputStream inputStream) {
        if (inputStream != null) {
            return false;
        }
        setErrorHttpResponse(-5, ": requestMode= " + i + ". InputStream is empty.");
        return true;
    }

    private boolean isOutputStreamEmpty(OutputStream outputStream) {
        if (outputStream != null) {
            return false;
        }
        setErrorHttpResponse(-5, ": OutputStream is empty.");
        return true;
    }

    private String fullfillEmptyMessage(String str) {
        if (str != null) {
            return str;
        }
        setErrorHttpResponse(-2, " Response msg is empty.");
        return "";
    }

    private boolean isContentLengthValid(HttpURLConnection httpURLConnection, CoordinatorAudit coordinatorAudit) {
        this.response.setHeaderFields(httpURLConnection.getHeaderFields());
        String headerValue = this.response.getHeaderValue("Content-Length");
        if (TextUtils.isEmpty(headerValue)) {
            return true;
        }
        try {
            long parseLong = Long.parseLong(headerValue);
            if (parseLong <= this.dataTrafficSize) {
                if (coordinatorAudit != null) {
                    coordinatorAudit.setDataSize(Long.valueOf(parseLong));
                }
                return true;
            }
            setErrorHttpResponse(-18, "HttpClient content-length = " + parseLong + ", dataTrafficSize = " + this.dataTrafficSize + ". Download suspended.");
            setErrorHttpResponse(-18, "HttpClient: Content-Length is invalid");
            return false;
        } catch (NumberFormatException e) {
            setErrorHttpResponse(-18, "HttpClient: Failed to parse Content-Length, Content-Length" + headerValue + ", Error:" + e.getMessage());
            return false;
        }
    }

    private HttpResponse setErrorHttpResponse(int i, String str) {
        DSLog.e(str, new Object[0]);
        this.response.setStatusCode(i);
        this.response.setResponseMsg(str);
        return this.response;
    }

    private boolean isRequestBodyValid() {
        return this.requestBody.length() <= MAX_REQUEST_BODY_LENGTH;
    }
}

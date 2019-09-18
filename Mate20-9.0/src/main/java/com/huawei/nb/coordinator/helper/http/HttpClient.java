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
    public static final int FRAGMENT_LOAD_REQUEST = 1;
    public static final String GET_TYPE = "GET";
    private static final String HEAD_TYPE = "HEAD";
    private static final String HTTPS_TYPE = "https";
    private static final String HTTP_TYPE = "http";
    private static final long MAX_DEFAULT_SIZE = 524288000;
    private static final int MAX_REQUEST_BODY_LENGTH = 819200;
    private static final int MAX_URL_LENGTH = 8192;
    public static final String POST_TYPE = "POST";
    private static final String TAG = "HttpClient";
    public static final int TRANSFER_FILE_BREAKPOINT = 3;
    public static final int TRANSFER_FILE_REQUEST = 2;
    private String baseURL;
    private DataRequestListener dataRequestListener;
    private Long dataTrafficSize;
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
        this.dataTrafficSize = Long.valueOf(MAX_DEFAULT_SIZE);
        this.requestMode = 0;
    }

    public HttpClient(int requestMode2, String fileSavePath2, String fileName2) {
        this.baseURL = null;
        this.requestBody = null;
        this.requestMethod = null;
        this.request = null;
        this.requestHeaders = null;
        this.response = new HttpResponse();
        this.dataTrafficSize = Long.valueOf(MAX_DEFAULT_SIZE);
        this.requestMode = requestMode2;
        this.fileSavePath = fileSavePath2;
        this.fileName = fileName2;
    }

    public HttpClient setDataTrafficSize(Long dataTrafficSize2) {
        this.dataTrafficSize = dataTrafficSize2;
        return this;
    }

    public HttpClient setDataRequestListener(DataRequestListener dataRequestListener2) {
        this.dataRequestListener = dataRequestListener2;
        return this;
    }

    public HttpClient setTmpFileDir(String mTmpFileDir) {
        this.tmpFileDir = mTmpFileDir;
        return this;
    }

    private boolean isFileParamsValid() throws IOException {
        if (TextUtils.isEmpty(this.fileName) || TextUtils.isEmpty(this.fileSavePath)) {
            setErrorHttpResponse(-5, "HttpClient File save path or fileName is empty !");
            return false;
        }
        File savePath = new File(this.fileSavePath);
        if (savePath.exists() && savePath.isDirectory()) {
            return createFile(this.fileSavePath, this.fileName);
        }
        if (savePath.exists()) {
            setErrorHttpResponse(-5, "HttpClient File save path is not a valid directory!");
            return false;
        } else if (savePath.mkdirs()) {
            return createFile(this.fileSavePath, this.fileName);
        } else {
            setErrorHttpResponse(-5, "HttpClient Fail to create save path!");
            return false;
        }
    }

    private boolean createDirectory(String path) throws IOException {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = FileUtils.getFile(path);
        if (file.exists()) {
            if (!file.isDirectory()) {
                setErrorHttpResponse(-5, "HttpClient FileSavePath is error");
                return false;
            }
        } else if (!file.mkdirs()) {
            setErrorHttpResponse(-5, "HttpClient Create Save Path error !");
            return false;
        }
        return true;
    }

    private boolean createFile(String parentPath, String fileName2) throws IOException {
        if (!TextUtils.isEmpty(fileName2)) {
            return createFile(FileUtils.getFile(parentPath, fileName2));
        }
        DSLog.e("HttpClient fileName is empty!", new Object[0]);
        return false;
    }

    private boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                int length = children.length;
                int i = 0;
                while (i < length) {
                    try {
                        if (!deleteDir(FileUtils.getFile(dir.getCanonicalPath(), children[i]))) {
                            DSLog.e("HttpClient delete " + child + " failed.", new Object[0]);
                            return false;
                        }
                        i++;
                    } catch (IOException e) {
                        DSLog.e("HttpClient Fail to delete " + child + ", error: " + e.getMessage(), new Object[0]);
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    private boolean createFile(File file) {
        boolean deleteResult;
        if (file == null) {
            DSLog.e("HttpClient file is null.", new Object[0]);
            return false;
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                deleteResult = deleteDir(file);
            } else {
                deleteResult = file.delete();
            }
            if (!deleteResult) {
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

    public HttpResponse syncExecute(CoordinatorAudit coordinatorAudit) {
        HttpURLConnection connection = null;
        try {
            URL url = getUrl();
            if (url == null) {
                HttpResponse errorHttpResponse = setErrorHttpResponse(-2, "HttpClient url may be too long. Request stops.");
                if (connection == null) {
                    return errorHttpResponse;
                }
                connection.disconnect();
                return errorHttpResponse;
            } else if (!isRequestBodyValid()) {
                HttpResponse errorHttpResponse2 = setErrorHttpResponse(-2, "HttpClient request body may be too long. Request stops.");
                if (connection == null) {
                    return errorHttpResponse2;
                }
                connection.disconnect();
                return errorHttpResponse2;
            } else {
                this.response.setUrl(url.toString());
                connection = parseUrlConnection(url.openConnection());
                if (connection == null) {
                    HttpResponse errorHttpResponse3 = setErrorHttpResponse(-2, " connection is illegal. Request stops.");
                    if (connection == null) {
                        return errorHttpResponse3;
                    }
                    connection.disconnect();
                    return errorHttpResponse3;
                }
                initConnection(connection);
                if (this.requestMethod.equals(POST_TYPE)) {
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    addPostRequestBody(connection);
                }
                requestAccordingMode(connection, coordinatorAudit);
                if (connection != null) {
                    connection.disconnect();
                }
                return this.response;
            }
        } catch (IOException e) {
            this.response.setHttpExceptionMsg(e.getMessage());
            setErrorHttpResponse(-5, "HttpClient caught IOException, error message:" + e.getMessage());
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Throwable th) {
            if (connection != null) {
                connection.disconnect();
            }
            throw th;
        }
    }

    private void requestAccordingMode(HttpURLConnection connection, CoordinatorAudit coordinatorAudit) throws IOException {
        DSLog.d("HttpClient request mode = " + this.requestMode, new Object[0]);
        switch (this.requestMode) {
            case 0:
                requestForResponseBody(connection, coordinatorAudit);
                return;
            case 1:
                requestForFragmentLoad(connection, coordinatorAudit);
                return;
            case 2:
                if (isFileParamsValid()) {
                    requestForFileTransfer(connection, coordinatorAudit);
                    return;
                }
                return;
            case 3:
                requestForTransferBreakpointFile(connection, coordinatorAudit);
                return;
            default:
                DSLog.e("HttpClient requestMode:" + this.requestMode + " does not exist!", new Object[0]);
                return;
        }
    }

    private void requestForTransferBreakpointFile(HttpURLConnection connection, CoordinatorAudit coordinatorAudit) {
        BreakpointResumeDownload resumeDownload = null;
        String tmpFileCanonicalPath = null;
        boolean isAddPathToSetSuccess = false;
        try {
            BreakpointResumeDownload resumeDownload2 = new BreakpointResumeDownload(connection.getURL().toString(), this.tmpFileDir, this.fileSavePath, this.fileName);
            try {
                tmpFileCanonicalPath = resumeDownload2.generateTmpFilePath();
                if (TextUtils.isEmpty(tmpFileCanonicalPath)) {
                    setErrorHttpResponse(-2, " Failed to generate tmp file, the path is empty.");
                    if (!(resumeDownload2 == null || TextUtils.isEmpty(tmpFileCanonicalPath) || 0 == 0)) {
                        BreakpointResumeDownload.removePathFromSet(tmpFileCanonicalPath);
                    }
                    BreakpointResumeDownload breakpointResumeDownload = resumeDownload2;
                } else if (!BreakpointResumeDownload.addPathToSet(tmpFileCanonicalPath)) {
                    setErrorHttpResponse(-17, " Failed to add this request to set, maybe it is duplicated.");
                    if (!(resumeDownload2 == null || TextUtils.isEmpty(tmpFileCanonicalPath) || 0 == 0)) {
                        BreakpointResumeDownload.removePathFromSet(tmpFileCanonicalPath);
                    }
                    BreakpointResumeDownload breakpointResumeDownload2 = resumeDownload2;
                } else {
                    isAddPathToSetSuccess = true;
                    Map<String, List<String>> responseHeaderMap = requestForHeader(coordinatorAudit);
                    if (responseHeaderMap.isEmpty()) {
                        setErrorHttpResponse(-16, " response header is empty.");
                        if (!(resumeDownload2 == null || TextUtils.isEmpty(tmpFileCanonicalPath) || 1 == 0)) {
                            BreakpointResumeDownload.removePathFromSet(tmpFileCanonicalPath);
                        }
                        BreakpointResumeDownload breakpointResumeDownload3 = resumeDownload2;
                        return;
                    }
                    Range range = resumeDownload2.getCalculatedRange(responseHeaderMap);
                    if (range.isAllowedToSetRange()) {
                        DSLog.d("HttpClient range min = " + range.getRangeMin() + " range max = " + range.getRangeMax(), new Object[0]);
                        connection.setRequestProperty("Range", "bytes=" + range.getRangeMin() + "-" + range.getRangeMax());
                    }
                    if (range.isAllowedToSetRange() || range.getRangeMin() == 0) {
                        downloadFileFromBreakpoint(connection, coordinatorAudit, resumeDownload2);
                        resumeDownload2.postHandle(this.response);
                    } else {
                        requestForFileTransfer(connection, coordinatorAudit);
                    }
                    if (resumeDownload2 == null || TextUtils.isEmpty(tmpFileCanonicalPath) || 1 == 0) {
                        return;
                    }
                    BreakpointResumeDownload.removePathFromSet(tmpFileCanonicalPath);
                    BreakpointResumeDownload breakpointResumeDownload4 = resumeDownload2;
                }
            } catch (BreakpointResumeDownloadException e) {
                e = e;
                resumeDownload = resumeDownload2;
            } catch (Throwable th) {
                th = th;
                resumeDownload = resumeDownload2;
                if (resumeDownload != null && !TextUtils.isEmpty(tmpFileCanonicalPath) && isAddPathToSetSuccess) {
                    BreakpointResumeDownload.removePathFromSet(tmpFileCanonicalPath);
                }
                throw th;
            }
        } catch (BreakpointResumeDownloadException e2) {
            e = e2;
            try {
                setErrorHttpResponse(e.getErrorCode(), e.getMessage());
                if (resumeDownload != null && !TextUtils.isEmpty(tmpFileCanonicalPath) && isAddPathToSetSuccess) {
                    BreakpointResumeDownload.removePathFromSet(tmpFileCanonicalPath);
                }
            } catch (Throwable th2) {
                th = th2;
                BreakpointResumeDownload.removePathFromSet(tmpFileCanonicalPath);
                throw th;
            }
        }
    }

    private HttpURLConnection parseUrlConnection(URLConnection urlConnection) {
        if (this.baseURL.startsWith(HTTPS_TYPE) && (urlConnection instanceof HttpsURLConnection)) {
            return (HttpsURLConnection) urlConnection;
        }
        if (this.baseURL.startsWith(HTTP_TYPE) && (urlConnection instanceof HttpURLConnection)) {
            return (HttpURLConnection) urlConnection;
        }
        DSLog.e("Connection type is illegal. Request stops.", new Object[0]);
        return null;
    }

    public HttpClient newCall(HttpRequest request2) {
        this.baseURL = request2.getUrl();
        this.request = request2;
        this.requestBody = request2.getRequestBodyString();
        this.requestHeaders = request2.getRequestHeaders();
        this.requestMethod = request2.getRequestMethod();
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

    private void initConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod(this.requestMethod);
        connection.setConnectTimeout(this.request.getConnectTimeout());
        connection.setReadTimeout(this.request.getReadTimeout());
        for (Map.Entry<String, String> entry : this.requestHeaders.entrySet()) {
            connection.setRequestProperty(checkHeader(entry.getKey()), checkHeader(entry.getValue()));
        }
    }

    private String checkHeader(String value) {
        if (!TextUtils.isEmpty(value)) {
            return value.replace("\n", "").replace("\r", "");
        }
        return "";
    }

    private void addPostRequestBody(HttpURLConnection connection) {
        BufferedOutputStream bos = null;
        OutputStream outputStream = null;
        if (!TextUtils.isEmpty(this.requestBody)) {
            try {
                outputStream = connection.getOutputStream();
                if (isOutputStreamEmpty(outputStream)) {
                    FileUtils.closeCloseable(outputStream);
                    FileUtils.closeCloseable(null);
                    return;
                }
                BufferedOutputStream bos2 = new BufferedOutputStream(outputStream);
                try {
                    bos2.write(this.requestBody.getBytes("utf-8"));
                    bos2.flush();
                    FileUtils.closeCloseable(outputStream);
                    FileUtils.closeCloseable(bos2);
                    BufferedOutputStream bufferedOutputStream = bos2;
                } catch (IOException e) {
                    e = e;
                    bos = bos2;
                    try {
                        DSLog.e("HttpClient addPostRequestBody IOException : " + e.getMessage(), new Object[0]);
                        this.response.setHttpExceptionMsg(e.getMessage());
                        FileUtils.closeCloseable(outputStream);
                        FileUtils.closeCloseable(bos);
                    } catch (Throwable th) {
                        th = th;
                        FileUtils.closeCloseable(outputStream);
                        FileUtils.closeCloseable(bos);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    bos = bos2;
                    FileUtils.closeCloseable(outputStream);
                    FileUtils.closeCloseable(bos);
                    throw th;
                }
            } catch (IOException e2) {
                e = e2;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r13.response.setResponseSize(r2);
        r9 = r1.toString("UTF-8");
        r13.response.setStatusCode(r7);
        r13.response.setResponseString(r9);
     */
    private void requestForResponseBody(HttpURLConnection connection, CoordinatorAudit coordinatorAudit) {
        InputStream is = null;
        try {
            int responseCode = connection.getResponseCode();
            String responseMessage = fullfillEmptyMessage(connection.getResponseMessage());
            this.response.setResponseMsg(responseMessage);
            if (responseCode == 200) {
                if (isContentLengthValid(connection, coordinatorAudit)) {
                    is = connection.getInputStream();
                    if (!isInputStreamEmpty(0, is)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bb = new byte[BUFFER_SIZE];
                        long downloadedLength = 0;
                        while (true) {
                            int len = is.read(bb);
                            if (len == -1) {
                                break;
                            }
                            downloadedLength += (long) len;
                            if (downloadedLength > this.dataTrafficSize.longValue()) {
                                throw new IOException("file'size is larger than " + this.dataTrafficSize + " downloadedLength = " + downloadedLength);
                            }
                            byteArrayOutputStream.write(bb, 0, len);
                        }
                    } else {
                        FileUtils.closeCloseable(is);
                        if (connection != null) {
                            connection.disconnect();
                            return;
                        }
                        return;
                    }
                } else {
                    FileUtils.closeCloseable(null);
                    if (connection != null) {
                        connection.disconnect();
                        return;
                    }
                    return;
                }
            } else {
                setErrorHttpResponse(responseCode, responseMessage);
            }
            FileUtils.closeCloseable(is);
            if (connection != null) {
                connection.disconnect();
            }
        } catch (IOException e) {
            dealIOExceptionWhenConnecting(e);
            FileUtils.closeCloseable(is);
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Throwable th) {
            FileUtils.closeCloseable(is);
            if (connection != null) {
                connection.disconnect();
            }
            throw th;
        }
    }

    private void requestForFileTransfer(HttpURLConnection connection, CoordinatorAudit coordinatorAudit) {
        InputStream is = null;
        RefreshResult refreshResult = new RefreshResult();
        try {
            File file = FileUtils.getFile(this.fileSavePath, this.fileName);
            if (!isConnectResponseValid(connection, coordinatorAudit)) {
                FileUtils.closeCloseable(null);
                if (connection != null) {
                    connection.disconnect();
                }
                if (this.response.getStatusCode() == -7) {
                    DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
                } else {
                    checkFileLength();
                }
            } else {
                is = connection.getInputStream();
                if (isInputStreamEmpty(2, is)) {
                    FileUtils.closeCloseable(is);
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (this.response.getStatusCode() == -7) {
                        DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
                    } else {
                        checkFileLength();
                    }
                } else {
                    this.response.setDownloadStart(true);
                    this.response.setHeaderFields(connection.getHeaderFields());
                    readInputStreamToFile(file, is, (long) 0, refreshResult, StandardOpenOption.WRITE);
                    this.response.setStatusCode(connection.getResponseCode());
                    FileUtils.closeCloseable(is);
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (this.response.getStatusCode() == -7) {
                        DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
                    } else {
                        checkFileLength();
                    }
                }
            }
        } catch (IOException e) {
            dealIOExceptionWhenConnecting(e);
            FileUtils.closeCloseable(is);
            if (connection != null) {
                connection.disconnect();
            }
            if (this.response.getStatusCode() == -7) {
                DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
            } else {
                checkFileLength();
            }
        } catch (Throwable th) {
            FileUtils.closeCloseable(is);
            if (connection != null) {
                connection.disconnect();
            }
            if (this.response.getStatusCode() == -7) {
                DSLog.w("HttpClient File transfer is interrupted by user. Not to check file length.", new Object[0]);
            } else {
                checkFileLength();
            }
            throw th;
        }
    }

    private void checkFileLength() {
        try {
            String lengthValue = this.response.getHeaderValue("Content-Length");
            if (TextUtils.isEmpty(lengthValue)) {
                DSLog.e("HttpClient Response content-length is empty.", new Object[0]);
                return;
            }
            File file = FileUtils.getFile(this.fileSavePath, this.fileName);
            if (!file.exists()) {
                setErrorHttpResponse(-19, " downloaded file is not exist!");
                return;
            }
            try {
                if (file.length() != Long.parseLong(lengthValue)) {
                    setErrorHttpResponse(-19, "File size error!File size:" + file.length() + ",Total size:" + lengthValue);
                } else {
                    this.response.setResponseString("{\"data\":\"File download success!File size:" + file.length() + ",Total size:" + lengthValue + "\"}");
                }
            } catch (NumberFormatException e) {
                setErrorHttpResponse(-15, " Fail to parse content-length. error: " + e.getMessage());
            }
        } catch (IOException e2) {
            setErrorHttpResponse(-15, " Get File IOException : " + e2.getMessage());
        }
    }

    private void dealIOExceptionWhenConnecting(IOException e) {
        if (TextUtils.isEmpty(e.getMessage())) {
            setErrorHttpResponse(-2, " IOException, msg is empty.");
        } else if (e.getMessage().startsWith("Unable to resolve host")) {
            setErrorHttpResponse(-6, " network error: Unable to resolve host. ");
        } else if (e.getMessage().startsWith("failed to connect to") || e.getMessage().startsWith("Failed to connect to")) {
            setErrorHttpResponse(-9, " connect cloud error for transfer file: " + e.getMessage());
        } else if (e.getMessage().startsWith(IVerifyVar.TIME_OUT_HEADER)) {
            setErrorHttpResponse(-13, " timeout for transfer file: " + e.getMessage());
        } else {
            this.response.setHttpExceptionMsg(e.getMessage());
            DSLog.e("HttpClient executeResponseForTransferFile IOException : " + e.getMessage(), new Object[0]);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x0129  */
    private void requestForFragmentLoad(HttpURLConnection connection, CoordinatorAudit coordinatorAudit) {
        DSLog.d("HttpClient requestForFragmentLoad ", new Object[0]);
        InputStream is = null;
        DataInputStream dis = null;
        try {
            if (!createDirectory(this.fileSavePath)) {
                FileUtils.closeCloseable(null);
                FileUtils.closeCloseable(null);
                if (connection != null) {
                    connection.disconnect();
                }
            } else if (!isConnectResponseValid(connection, coordinatorAudit)) {
                FileUtils.closeCloseable(null);
                FileUtils.closeCloseable(null);
                if (connection != null) {
                    connection.disconnect();
                }
            } else {
                is = connection.getInputStream();
                if (isInputStreamEmpty(1, is)) {
                    FileUtils.closeCloseable(null);
                    FileUtils.closeCloseable(is);
                    if (connection != null) {
                        connection.disconnect();
                        return;
                    }
                    return;
                }
                DataInputStream dis2 = new DataInputStream(is);
                try {
                    byte[] metaBytes = new byte[calculateMetaLength(dis2)];
                    int readLength = is.read(metaBytes);
                    if (readLength == -1) {
                        setErrorHttpResponse(-5, "Failed to get metaString info from connection!");
                        FileUtils.closeCloseable(dis2);
                        FileUtils.closeCloseable(is);
                        if (connection != null) {
                            connection.disconnect();
                        }
                        DataInputStream dataInputStream = dis2;
                        return;
                    }
                    long downloadedLength = (long) readLength;
                    String metaString = bytesToString("UTF-8", metaBytes);
                    List<PackageMeta.PackagesBean> packageList = readPackageListFromMetaString(metaString);
                    int size = packageList.size();
                    for (int i = 0; i < size; i++) {
                        PackageMeta.PackagesBean packageBean = packageList.get(i);
                        String packageBeanName = packageBean.getName();
                        downloadedLength += requestForSingleFragment(packageBean, FileUtils.getFile(this.fileSavePath, packageBeanName), downloadedLength, is);
                        if (this.dataRequestListener instanceof FileDataRequestListener) {
                            ((FileDataRequestListener) this.dataRequestListener).onDownloadSuccess(packageBeanName, String.valueOf(i + 1), metaString);
                        }
                    }
                    this.response.setResponseSize(downloadedLength);
                    this.response.setResponseString("{\"data\":\"Fragment load success!\"}");
                    this.response.setStatusCode(connection.getResponseCode());
                    FileUtils.closeCloseable(dis2);
                    FileUtils.closeCloseable(is);
                    if (connection != null) {
                        connection.disconnect();
                        DataInputStream dataInputStream2 = dis2;
                        return;
                    }
                } catch (IOException e) {
                    e = e;
                    dis = dis2;
                    try {
                        this.response.setHttpExceptionMsg(e.getMessage());
                        dealIOExceptionWhenConnecting(e);
                        FileUtils.closeCloseable(dis);
                        FileUtils.closeCloseable(is);
                        if (connection != null) {
                            connection.disconnect();
                        }
                    } catch (Throwable th) {
                        th = th;
                        FileUtils.closeCloseable(dis);
                        FileUtils.closeCloseable(is);
                        if (connection != null) {
                            connection.disconnect();
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    dis = dis2;
                    FileUtils.closeCloseable(dis);
                    FileUtils.closeCloseable(is);
                    if (connection != null) {
                    }
                    throw th;
                }
            }
        } catch (IOException e2) {
            e = e2;
        }
    }

    private String bytesToString(String charset, byte[] bytes) throws CharacterCodingException {
        return Charset.forName(charset).newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
    }

    private int calculateMetaLength(DataInputStream dis) throws IOException {
        int metaLength = dis.readInt();
        if (metaLength <= BIG_BUFFER_SIZE) {
            return metaLength;
        }
        throw new IOException("Meta length is " + metaLength + ", which is too long!");
    }

    private List<PackageMeta.PackagesBean> readPackageListFromMetaString(String metaString) throws IOException {
        if (!JsonUtils.isValidJson(metaString)) {
            throw new IOException("Json string is not valid!");
        }
        PackageMeta packageMeta = (PackageMeta) JsonUtils.parse(metaString, PackageMeta.class);
        if (packageMeta == null) {
            throw new IOException("Failed to get PackageMeta info from metaString!");
        }
        List<PackageMeta.PackagesBean> packagesBeanList = packageMeta.getPackages();
        if (packagesBeanList != null) {
            return packagesBeanList;
        }
        throw new IOException("Failed to get packageList from PackageMeta!");
    }

    private boolean isConnectResponseValid(HttpURLConnection connection, CoordinatorAudit coordinatorAudit) throws IOException {
        int responseCode = connection.getResponseCode();
        String responseMessage = fullfillEmptyMessage(connection.getResponseMessage());
        DSLog.d("HttpClient response code of file's transfer : " + responseCode, new Object[0]);
        DSLog.d("HttpClient response message of file's transfer : " + responseMessage, new Object[0]);
        this.response.setResponseMsg(responseMessage);
        if (responseCode == 200) {
            return isContentLengthValid(connection, coordinatorAudit);
        }
        this.response.setStatusCode(responseCode);
        return false;
    }

    private long requestForSingleFragment(PackageMeta.PackagesBean packageBean, File file, long downloadedLength, InputStream is) throws IOException {
        int len;
        DSLog.d("HttpClient requestForSingleFragment ", new Object[0]);
        long downloadedFileLength = downloadedLength;
        if (!createFile(file)) {
            throw new IOException("Failed to create file!");
        }
        int fileSize = Integer.parseInt(packageBean.getSize());
        OutputStream os = null;
        BufferedOutputStream bos = null;
        try {
            os = getSafeOutputStream(file, false, StandardOpenOption.WRITE);
            BufferedOutputStream bos2 = new BufferedOutputStream(os);
            try {
                byte[] bb = new byte[BUFFER_SIZE];
                int readPosition = 0;
                while (readPosition < fileSize) {
                    if (readPosition + BUFFER_SIZE <= fileSize) {
                        len = is.read(bb);
                        if (len != -1) {
                            bos2.write(bb, 0, len);
                            downloadedFileLength += (long) len;
                        }
                    } else {
                        byte[] lastbb = new byte[(fileSize - readPosition)];
                        len = is.read(lastbb);
                        if (len != -1) {
                            bos2.write(lastbb, 0, len);
                            downloadedFileLength += (long) len;
                        }
                    }
                    if (downloadedFileLength > this.dataTrafficSize.longValue()) {
                        throw new IOException(" file size is larger than " + this.dataTrafficSize);
                    } else if (len != -1) {
                        readPosition += len;
                    }
                }
                bos2.flush();
                FileUtils.closeCloseable(bos2);
                FileUtils.closeCloseable(os);
                return downloadedFileLength;
            } catch (Throwable th) {
                th = th;
                bos = bos2;
                FileUtils.closeCloseable(bos);
                FileUtils.closeCloseable(os);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            FileUtils.closeCloseable(bos);
            FileUtils.closeCloseable(os);
            throw th;
        }
    }

    private Map<String, List<String>> requestForHeader(CoordinatorAudit coordinatorAudit) {
        HttpURLConnection headConnection = null;
        try {
            URL headUrl = getUrl();
            if (headUrl == null) {
                HashMap hashMap = new HashMap();
                if (headConnection == null) {
                    return hashMap;
                }
                headConnection.disconnect();
                return hashMap;
            }
            headConnection = parseUrlConnection(headUrl.openConnection());
            if (headConnection == null) {
                setErrorHttpResponse(-2, " connection is illegal. Request stops.");
                HashMap hashMap2 = new HashMap();
                if (headConnection == null) {
                    return hashMap2;
                }
                headConnection.disconnect();
                return hashMap2;
            }
            initConnection(headConnection);
            headConnection.setRequestMethod(HEAD_TYPE);
            int responseCode = headConnection.getResponseCode();
            String responseMessage = fullfillEmptyMessage(headConnection.getResponseMessage());
            DSLog.d("HttpClient response code of HEAD request : " + responseCode, new Object[0]);
            DSLog.d("HttpClient response message of HEAD request : " + responseMessage, new Object[0]);
            this.response.setResponseMsg(responseMessage);
            if (responseCode != 200 || isContentLengthValid(headConnection, coordinatorAudit)) {
                if (headConnection != null) {
                    headConnection.disconnect();
                }
                return headConnection.getHeaderFields();
            }
            HashMap hashMap3 = new HashMap();
            if (headConnection == null) {
                return hashMap3;
            }
            headConnection.disconnect();
            return hashMap3;
        } catch (IOException e) {
            dealIOExceptionWhenConnecting(e);
            HashMap hashMap4 = new HashMap();
            if (headConnection == null) {
                return hashMap4;
            }
            headConnection.disconnect();
            return hashMap4;
        } catch (Throwable th) {
            if (headConnection != null) {
                headConnection.disconnect();
            }
            throw th;
        }
    }

    private void downloadFileFromBreakpoint(HttpURLConnection connection, CoordinatorAudit coordinatorAudit, BreakpointResumeDownload resumeDownload) {
        RefreshResult refreshResult = new RefreshResult();
        try {
            File file = resumeDownload.getTmpFile();
            long downloadedLength = file.length();
            int responseCode = connection.getResponseCode();
            String responseMessage = fullfillEmptyMessage(connection.getResponseMessage());
            DSLog.d("HttpClient response code of breakpoint file's transfer : " + responseCode, new Object[0]);
            DSLog.d("HttpClient response message of breakpoint file's transfer : " + responseMessage, new Object[0]);
            this.response.setResponseMsg(responseMessage);
            if (BreakpointResumeDownload.isFailResponse(file.length(), responseCode)) {
                setErrorHttpResponse(responseCode, responseMessage);
                FileUtils.closeCloseable(null);
                if (connection != null) {
                    connection.disconnect();
                }
                if (this.response.getStatusCode() == -7) {
                    DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
                }
            } else if (!isContentLengthValid(connection, coordinatorAudit)) {
                FileUtils.closeCloseable(null);
                if (connection != null) {
                    connection.disconnect();
                }
                if (this.response.getStatusCode() == -7) {
                    DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
                }
            } else {
                InputStream is = connection.getInputStream();
                this.response.setDownloadStart(true);
                this.response.setHeaderFields(connection.getHeaderFields());
                if (!resumeDownload.updateHeaderMap(connection.getHeaderFields())) {
                    setErrorHttpResponse(-16, " Failed to use GET response's header to update header map.");
                }
                readInputStreamToFile(file, is, downloadedLength, refreshResult, StandardOpenOption.APPEND);
                this.response.setStatusCode(responseCode);
                FileUtils.closeCloseable(is);
                if (connection != null) {
                    connection.disconnect();
                }
                if (this.response.getStatusCode() == -7) {
                    DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
                }
            }
        } catch (IOException e) {
            dealIOExceptionWhenConnecting(e);
            FileUtils.closeCloseable(null);
            if (connection != null) {
                connection.disconnect();
            }
            if (this.response.getStatusCode() == -7) {
                DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
            }
        } catch (Throwable th) {
            FileUtils.closeCloseable(null);
            if (connection != null) {
                connection.disconnect();
            }
            if (this.response.getStatusCode() == -7) {
                DSLog.w("HttpClient Breakpoint file transfer is interrupted by user.", new Object[0]);
            }
            throw th;
        }
    }

    private void readInputStreamToFile(File file, InputStream is, long readLength, RefreshResult refreshResult, StandardOpenOption option) throws IOException {
        OutputStream os = null;
        BufferedOutputStream bos = null;
        long downloadedLength = readLength;
        try {
            os = getSafeOutputStream(file, false, option);
            BufferedOutputStream bos2 = new BufferedOutputStream(os);
            try {
                refreshResult.setFinished(false);
                refreshResult.setDeltaSize(0);
                refreshResult.setIndex(0);
                byte[] bb = new byte[BUFFER_SIZE];
                while (true) {
                    int len = is.read(bb);
                    if (len != -1) {
                        bos2.write(bb, 0, len);
                        downloadedLength += (long) len;
                        if (downloadedLength > this.dataTrafficSize.longValue()) {
                            throw new IOException("file'size is larger than " + this.dataTrafficSize + ", downloadedLength = " + downloadedLength);
                        } else if (this.dataRequestListener instanceof RefreshDataRequestListener) {
                            refreshResult.setDeltaSize((long) len);
                            refreshResult.increaseIndex();
                            refreshResult.setDownloadedSize(downloadedLength);
                            if (!((RefreshDataRequestListener) this.dataRequestListener).onRefresh(refreshResult)) {
                                String msg = "download will stop to refresh. Downloaded length:" + downloadedLength;
                                setErrorHttpResponse(-7, msg);
                                throw new IOException(msg);
                            }
                        }
                    } else {
                        this.response.setResponseSize(downloadedLength);
                        bos2.flush();
                        if (this.dataRequestListener instanceof RefreshDataRequestListener) {
                            refreshResult.setDeltaSize(0);
                            refreshResult.setDownloadedSize(downloadedLength);
                            refreshResult.setFinished(true);
                            ((RefreshDataRequestListener) this.dataRequestListener).onRefresh(refreshResult);
                        }
                        FileUtils.closeCloseable(bos2);
                        FileUtils.closeCloseable(os);
                        return;
                    }
                }
            } catch (Throwable th) {
                th = th;
                bos = bos2;
                FileUtils.closeCloseable(bos);
                FileUtils.closeCloseable(os);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            FileUtils.closeCloseable(bos);
            FileUtils.closeCloseable(os);
            throw th;
        }
    }

    private static OutputStream getSafeOutputStream(File safeFile, boolean isGroupReadShare, StandardOpenOption option) throws IOException {
        return FileUtils.openOutputStream(safeFile, -1, -1, isGroupReadShare, false, option);
    }

    private boolean isInputStreamEmpty(int requestMode2, InputStream inputStream) {
        if (inputStream != null) {
            return false;
        }
        setErrorHttpResponse(-5, ": requestMode= " + requestMode2 + ". InputStream is empty.");
        return true;
    }

    private boolean isOutputStreamEmpty(OutputStream outputStream) {
        if (outputStream != null) {
            return false;
        }
        setErrorHttpResponse(-5, ": OutputStream is empty.");
        return true;
    }

    private String fullfillEmptyMessage(String message) {
        if (message != null) {
            return message;
        }
        setErrorHttpResponse(-2, " Response msg is empty.");
        return "";
    }

    private boolean isContentLengthValid(HttpURLConnection connection, CoordinatorAudit coordinatorAudit) {
        this.response.setHeaderFields(connection.getHeaderFields());
        String contentLengthStr = this.response.getHeaderValue("Content-Length");
        if (TextUtils.isEmpty(contentLengthStr)) {
            return true;
        }
        try {
            long contentLength = Long.parseLong(contentLengthStr);
            if (contentLength > this.dataTrafficSize.longValue()) {
                setErrorHttpResponse(-18, "HttpClient content-length = " + contentLength + ", dataTrafficSize = " + this.dataTrafficSize + ". Download suspended.");
                setErrorHttpResponse(-18, "HttpClient: Content-Length is invalid");
                return false;
            } else if (coordinatorAudit == null) {
                return true;
            } else {
                coordinatorAudit.setDataSize(Long.valueOf(contentLength));
                return true;
            }
        } catch (NumberFormatException e) {
            setErrorHttpResponse(-18, "HttpClient: Failed to parse Content-Length, Content-Length" + contentLengthStr + ", Error:" + e.getMessage());
            return false;
        }
    }

    private HttpResponse setErrorHttpResponse(int code, String errMsg) {
        DSLog.e(errMsg, new Object[0]);
        this.response.setStatusCode(code);
        this.response.setResponseMsg(errMsg);
        return this.response;
    }

    private boolean isRequestBodyValid() {
        return this.requestBody.length() <= MAX_REQUEST_BODY_LENGTH;
    }
}

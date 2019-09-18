package com.huawei.nb.coordinator.breakpoint;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.huawei.nb.coordinator.helper.http.HttpResponse;
import com.huawei.nb.security.SHA256Utils;
import com.huawei.nb.utils.JsonUtils;
import com.huawei.nb.utils.file.FileUtils;
import com.huawei.nb.utils.logger.DSLog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BreakpointResumeDownload {
    private static final String ACCEPT_RANGES_LABEL = "Accept-Ranges";
    private static final int BYTE_BUFFER_SIZE = 4096;
    private static final String CONTENT_LENGTH_LABEL = "Content-Length";
    private static final String ETAG_LABEL = "ETag";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final long MAX_JSON_FILE_LENGTH = 1048576;
    private static final String TAG = "BreakpointResumeDownload";
    private static final int TMP_FILE_NAME_SIZE = 20;
    private static final HashSet<String> sCurrentRequestSet = new HashSet<>();
    private String eTag;
    private File formalFile;
    private String formalFileDir;
    private String formalFileName;
    private File jsonFile;
    private Range range = new Range();
    private String signedUrl;
    private File tmpFile;
    private String tmpFileCanonicalPath;
    private String tmpFileDir;
    private String tmpFileName;
    private Map<String, TmpFileRecordBean> tmpFileRecordBeanMap;
    private long totalFileLength;

    private static class TmpFileRecordBeanMap extends TypeToken<Map<String, TmpFileRecordBean>> {
        private TmpFileRecordBeanMap() {
        }
    }

    public BreakpointResumeDownload(String url, String tmpFileDir2, String formalFileDir2, String formalFileName2) {
        this.tmpFileDir = tmpFileDir2;
        this.signedUrl = SHA256Utils.sha256Encrypt(url);
        this.formalFileDir = formalFileDir2;
        this.formalFileName = formalFileName2;
    }

    public String generateTmpFilePath() throws BreakpointResumeDownloadException {
        if (TextUtils.isEmpty(this.signedUrl)) {
            throw new BreakpointResumeDownloadException(-15, " signed url is empty.");
        }
        try {
            this.tmpFileName = this.signedUrl.substring(0, 20);
            try {
                this.tmpFile = generateFile(this.tmpFileDir, this.tmpFileName);
                if (this.tmpFile == null) {
                    throw new BreakpointResumeDownloadException(-15, " Fail to create new directory or file.");
                }
                this.tmpFileCanonicalPath = this.tmpFile.getCanonicalPath();
                return this.tmpFileCanonicalPath;
            } catch (IOException e) {
                throw new BreakpointResumeDownloadException(-15, " Fail to generate tmp file, error:" + e.getMessage());
            }
        } catch (IndexOutOfBoundsException e2) {
            throw new BreakpointResumeDownloadException(-15, " Fail to get tmp file name, error: " + e2.getMessage());
        }
    }

    public Range getCalculatedRange(Map<String, List<String>> responseHeaderMap) throws BreakpointResumeDownloadException {
        try {
            parseHeaderMap(responseHeaderMap);
            this.formalFile = generateFile(this.formalFileDir, this.formalFileName);
            if (this.formalFile == null) {
                throw new BreakpointResumeDownloadException(-19, " Failed to generate formal file");
            }
            this.tmpFileRecordBeanMap = parseJsonToMap();
            if (this.tmpFileRecordBeanMap != null) {
                return calculateRange();
            }
            throw new BreakpointResumeDownloadException(-19, " Failed to parse json file to map, check if json valid.");
        } catch (IOException e) {
            throw new BreakpointResumeDownloadException(-15, " Failed to initialize, error: " + e.getMessage());
        }
    }

    public void parseHeaderMap(Map<String, List<String>> responseHeaderMap) {
        this.range.setAllowToSetRange("bytes".equals(parseHeaderValue(responseHeaderMap, ACCEPT_RANGES_LABEL)));
        try {
            this.totalFileLength = Long.parseLong(parseHeaderValue(responseHeaderMap, CONTENT_LENGTH_LABEL));
        } catch (NumberFormatException e) {
            DSLog.e("BreakpointResumeDownload Failed to get total file length, error: " + e.getMessage(), new Object[0]);
            this.range.setAllowToSetRange(false);
        }
        this.eTag = parseHeaderValue(responseHeaderMap, ETAG_LABEL).replace("\"", "");
    }

    public boolean updateHeaderMap(Map<String, List<String>> responseHeaderMap) {
        if (this.range.getRangeMin() == 0) {
            parseHeaderMap(responseHeaderMap);
            return true;
        }
        return this.eTag.equals(parseHeaderValue(responseHeaderMap, ETAG_LABEL).replace("\"", ""));
    }

    private Range calculateRange() throws BreakpointResumeDownloadException {
        if (shouldResumeDownload()) {
            this.range.setRange(this.tmpFile.length(), this.totalFileLength);
            return this.range;
        }
        DSLog.i("BreakpointResumeDownload Download a new file, delete old files if exist.", new Object[0]);
        if (this.tmpFile.exists() && !this.tmpFile.delete()) {
            throw new BreakpointResumeDownloadException(-19, " Fail to delete old tmp file.");
        } else if (!this.jsonFile.exists() || this.jsonFile.delete()) {
            this.range.setRange(0, this.totalFileLength);
            return this.range;
        } else {
            throw new BreakpointResumeDownloadException(-19, " Fail to delete old json file.");
        }
    }

    private boolean shouldResumeDownload() {
        return this.tmpFile.exists() && this.jsonFile.exists() && isJsonMatchHeaderAndTmpFile();
    }

    private Map<String, TmpFileRecordBean> parseJsonToMap() throws IOException {
        this.jsonFile = FileUtils.getFile(this.tmpFileDir, this.tmpFileName + JSON_FILE_EXTENSION);
        if (!this.jsonFile.exists()) {
            DSLog.d("BreakpointResumeDownload Json file is not exist!", new Object[0]);
            return new HashMap();
        }
        String jsonStr = FileUtils.readFileToString(this.jsonFile, "UTF-8", (long) MAX_JSON_FILE_LENGTH);
        if (!JsonUtils.isJsonFormat(jsonStr)) {
            DSLog.e("BreakpointResumeDownload Json is not valid!", new Object[0]);
            return new HashMap();
        }
        try {
            return (Map) new Gson().fromJson(jsonStr, new TmpFileRecordBeanMap().getType());
        } catch (JsonParseException e) {
            DSLog.e("BreakpointResumeDownload Fail to parse json, error: " + e.getMessage(), new Object[0]);
            return new HashMap();
        }
    }

    private boolean isJsonMatchHeaderAndTmpFile() {
        TmpFileRecordBean tmpFileRecordBean = this.tmpFileRecordBeanMap.get(this.tmpFileName);
        if (tmpFileRecordBean == null) {
            return false;
        }
        if (!this.signedUrl.equals(tmpFileRecordBean.getSignedUrl())) {
            DSLog.e("BreakpointResumeDownload Url in header is not equal to url in json.", new Object[0]);
            return false;
        }
        long jsonTotalFileLength = tmpFileRecordBean.getTotalFileLength();
        if (this.totalFileLength != jsonTotalFileLength) {
            DSLog.e("BreakpointResumeDownload Fail to verify total file length. Total file length in header is " + this.totalFileLength + ". Total file length in json is " + jsonTotalFileLength, new Object[0]);
            return false;
        }
        long jsonFileLength = tmpFileRecordBean.getFileLength();
        if (this.tmpFile.length() != jsonFileLength) {
            DSLog.e("BreakpointResumeDownload Fail to verify file length. Actual file length is " + this.tmpFile.length() + ". Json's file length is " + jsonFileLength, new Object[0]);
            return false;
        }
        String jsonETag = tmpFileRecordBean.getETag();
        if (TextUtils.isEmpty(this.eTag) || TextUtils.isEmpty(jsonETag)) {
            DSLog.e("BreakpointResumeDownload Fail to verify eTag, header's eTag or json's eTag is empty", new Object[0]);
            return false;
        } else if (this.eTag.equals(jsonETag)) {
            return true;
        } else {
            DSLog.e("BreakpointResumeDownload Fail to verify eTag, header's eTag is not equal to json's eTag", new Object[0]);
            return false;
        }
    }

    private File generateFile(String fileDir, String fileName) throws IOException {
        File saveDir = FileUtils.getFile(fileDir);
        if (saveDir.exists()) {
            File file = FileUtils.getFile(fileDir, fileName);
            if (file.exists() || file.createNewFile()) {
                return file;
            }
        } else if (!saveDir.mkdirs()) {
            return null;
        } else {
            File file2 = FileUtils.getFile(fileDir, fileName);
            if (file2.createNewFile()) {
                return file2;
            }
        }
        return null;
    }

    public static boolean isFailResponse(long startPoint, int responseCode) {
        if (startPoint == 0) {
            if (206 == responseCode || 200 == responseCode) {
                return false;
            }
            return true;
        } else if (206 == responseCode) {
            return false;
        } else {
            return true;
        }
    }

    public void postHandle(HttpResponse response) throws BreakpointResumeDownloadException {
        try {
            if (isFailResponse(this.range.getRangeMin(), response.getStatusCode())) {
                updateTmpFileRecord();
                throw new BreakpointResumeDownloadException(response.getStatusCode(), response.getResponseMsg());
            } else if (!isFileLengthMatch()) {
                updateTmpFileRecord();
                throw new BreakpointResumeDownloadException(-15, " File's size is not match. It may be not a complete file.");
            } else if (!moveFile()) {
                updateTmpFileRecord();
                throw new BreakpointResumeDownloadException(-19, " Fail to move tmp file to formal path.");
            } else if (!deleteTmpData()) {
                updateTmpFileRecord();
                throw new BreakpointResumeDownloadException(-15, " Fail to delete tmp data.");
            }
        } catch (IOException e) {
            throw new BreakpointResumeDownloadException(-15, e.getMessage());
        }
    }

    private boolean isFileLengthMatch() throws IOException {
        File file = FileUtils.getFile(this.tmpFileDir, this.tmpFileName);
        if (!file.exists()) {
            DSLog.e("BreakpointResumeDownload file does not exist.", new Object[0]);
            return false;
        } else if (file.length() == this.totalFileLength) {
            return true;
        } else {
            DSLog.e("BreakpointResumeDownload file length does not match. Delete tmp file result is" + this.tmpFile.delete(), new Object[0]);
            return false;
        }
    }

    private boolean moveFile() throws IOException {
        DSLog.d("BreakpointResumeDownload Not to verify due to lack of Content-MD5 value.", new Object[0]);
        return moveFileDirectly();
    }

    private boolean moveFileDirectly() throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.openInputStream(this.tmpFile);
            outputStream = FileUtils.openOutputStream(this.formalFile);
            byte[] buffer = new byte[BYTE_BUFFER_SIZE];
            while (true) {
                int length = inputStream.read(buffer);
                if (length > -1) {
                    outputStream.write(buffer, 0, length);
                } else {
                    outputStream.flush();
                    return true;
                }
            }
        } finally {
            FileUtils.closeCloseable(inputStream);
            FileUtils.closeCloseable(outputStream);
        }
    }

    private boolean deleteTmpData() throws IOException {
        if (!needDeleteJsonRecord()) {
            return this.tmpFile.delete();
        }
        deleteJsonRecord();
        if (this.tmpFileRecordBeanMap.isEmpty()) {
            return this.tmpFile.delete() && this.jsonFile.delete();
        }
        writeJsonStrToFile(this.jsonFile);
        return this.tmpFile.delete();
    }

    private boolean needDeleteJsonRecord() {
        return this.jsonFile.exists() && this.tmpFileRecordBeanMap != null && this.tmpFileRecordBeanMap.containsKey(this.tmpFileName);
    }

    private void deleteJsonRecord() throws IOException {
        this.tmpFileRecordBeanMap.remove(this.tmpFileName);
        Iterator<Map.Entry<String, TmpFileRecordBean>> iterator = this.tmpFileRecordBeanMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!FileUtils.getFile(this.tmpFileDir, iterator.next().getKey()).exists()) {
                iterator.remove();
            }
        }
    }

    private void updateTmpFileRecord() throws IOException {
        TmpFileRecordBean tmpFileRecordBean = createBean();
        if (this.tmpFileRecordBeanMap == null) {
            this.tmpFileRecordBeanMap = new HashMap();
        }
        this.tmpFileRecordBeanMap.put(this.tmpFileName, tmpFileRecordBean);
        DSLog.d("BreakpointResumeDownload insert tmp file record.", new Object[0]);
        parseBeanToJson();
    }

    private TmpFileRecordBean createBean() throws IOException {
        TmpFileRecordBean tmpFileRecordBean = new TmpFileRecordBean();
        File file = FileUtils.getFile(this.tmpFileDir, this.tmpFileName);
        tmpFileRecordBean.setTmpFileName(this.tmpFileName);
        tmpFileRecordBean.setSignedUrl(this.signedUrl);
        tmpFileRecordBean.setFileLength(file.length());
        tmpFileRecordBean.setTotalFileLength(this.totalFileLength);
        tmpFileRecordBean.setETag(this.eTag);
        return tmpFileRecordBean;
    }

    private void parseBeanToJson() throws IOException {
        this.jsonFile = generateFile(this.tmpFileDir, this.tmpFileName + JSON_FILE_EXTENSION);
        if (this.jsonFile != null) {
            writeJsonStrToFile(this.jsonFile);
            return;
        }
        DSLog.e("BreakpointResumeDownload Fail to parse bean to json, delete the downloaded part of file.", new Object[0]);
        if (this.tmpFile.exists() && !this.tmpFile.delete()) {
            DSLog.e("BreakpointResumeDownload Fail to delete the downloaded part of file.", new Object[0]);
        }
    }

    private void writeJsonStrToFile(File file) throws IOException {
        FileUtils.writeStringToFile(file, new Gson().toJson((Object) this.tmpFileRecordBeanMap), false, "UTF-8");
    }

    private String parseHeaderValue(Map<String, List<String>> responseHeaderMap, String key) {
        List<String> contentLengthValueList = responseHeaderMap.get(key);
        if (contentLengthValueList == null || contentLengthValueList.isEmpty()) {
            return "";
        }
        String value = contentLengthValueList.get(0);
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value;
    }

    public static boolean addPathToSet(String path) {
        boolean add;
        synchronized (sCurrentRequestSet) {
            if (sCurrentRequestSet.contains(path)) {
                add = false;
            } else {
                add = sCurrentRequestSet.add(path);
            }
        }
        return add;
    }

    public static void removePathFromSet(String path) {
        synchronized (sCurrentRequestSet) {
            sCurrentRequestSet.remove(path);
        }
    }

    public File getTmpFile() {
        return this.tmpFile;
    }
}

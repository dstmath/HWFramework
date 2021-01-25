package com.huawei.nb.coordinator.breakpoint;

import android.text.TextUtils;
import com.huawei.gson.Gson;
import com.huawei.gson.JsonParseException;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.nb.coordinator.helper.http.HttpResponse;
import com.huawei.nb.security.SHA256Utils;
import com.huawei.nb.utils.JsonUtils;
import com.huawei.nb.utils.file.FileUtils;
import com.huawei.nb.utils.logger.DSLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    public static boolean isFailResponse(long j, int i) {
        return j == 0 ? (i == 206 || i == 200) ? false : true : i != 206;
    }

    public BreakpointResumeDownload(String str, String str2, String str3, String str4) {
        this.tmpFileDir = str2;
        this.signedUrl = SHA256Utils.sha256Encrypt(str);
        this.formalFileDir = str3;
        this.formalFileName = str4;
    }

    public String generateTmpFilePath() throws BreakpointResumeDownloadException {
        if (!TextUtils.isEmpty(this.signedUrl)) {
            try {
                this.tmpFileName = this.signedUrl.substring(0, 20);
                try {
                    this.tmpFile = generateFile(this.tmpFileDir, this.tmpFileName);
                    if (this.tmpFile != null) {
                        this.tmpFileCanonicalPath = this.tmpFile.getCanonicalPath();
                        return this.tmpFileCanonicalPath;
                    }
                    throw new BreakpointResumeDownloadException(-15, " Fail to create new directory or file.");
                } catch (IOException e) {
                    throw new BreakpointResumeDownloadException(-15, " Fail to generate tmp file, error:" + e.getMessage());
                }
            } catch (IndexOutOfBoundsException e2) {
                throw new BreakpointResumeDownloadException(-15, " Fail to get tmp file name, error: " + e2.getMessage());
            }
        } else {
            throw new BreakpointResumeDownloadException(-15, " signed url is empty.");
        }
    }

    public Range getCalculatedRange(Map<String, List<String>> map) throws BreakpointResumeDownloadException {
        try {
            parseHeaderMap(map);
            this.formalFile = generateFile(this.formalFileDir, this.formalFileName);
            if (this.formalFile != null) {
                this.tmpFileRecordBeanMap = parseJsonToMap();
                if (this.tmpFileRecordBeanMap != null) {
                    return calculateRange();
                }
                throw new BreakpointResumeDownloadException(-19, " Failed to parse json file to map, check if json valid.");
            }
            throw new BreakpointResumeDownloadException(-19, " Failed to generate formal file");
        } catch (IOException e) {
            throw new BreakpointResumeDownloadException(-15, " Failed to initialize, error: " + e.getMessage());
        }
    }

    public void parseHeaderMap(Map<String, List<String>> map) {
        this.range.setAllowToSetRange("bytes".equals(parseHeaderValue(map, ACCEPT_RANGES_LABEL)));
        try {
            this.totalFileLength = Long.parseLong(parseHeaderValue(map, CONTENT_LENGTH_LABEL));
        } catch (NumberFormatException e) {
            DSLog.e("BreakpointResumeDownload Failed to get total file length, error: " + e.getMessage(), new Object[0]);
            this.range.setAllowToSetRange(false);
        }
        this.eTag = parseHeaderValue(map, ETAG_LABEL).replace("\"", "");
    }

    public boolean updateHeaderMap(Map<String, List<String>> map) {
        if (this.range.getRangeMin() == 0) {
            parseHeaderMap(map);
            return true;
        }
        return this.eTag.equals(parseHeaderValue(map, ETAG_LABEL).replace("\"", ""));
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
        String str = this.tmpFileDir;
        this.jsonFile = FileUtils.getFile(str, this.tmpFileName + JSON_FILE_EXTENSION);
        if (!this.jsonFile.exists()) {
            DSLog.d("BreakpointResumeDownload Json file is not exist!", new Object[0]);
            return new HashMap();
        }
        String readFileToString = FileUtils.readFileToString(this.jsonFile, "UTF-8", (long) MAX_JSON_FILE_LENGTH);
        if (!JsonUtils.isJsonFormat(readFileToString)) {
            DSLog.e("BreakpointResumeDownload Json is not valid!", new Object[0]);
            return new HashMap();
        }
        try {
            return (Map) new Gson().fromJson(JsonUtils.sanitize(readFileToString), new TmpFileRecordBeanMap().getType());
        } catch (JsonParseException e) {
            DSLog.e("BreakpointResumeDownload Fail to parse json, error: " + e.getMessage(), new Object[0]);
            return new HashMap();
        }
    }

    /* access modifiers changed from: private */
    public static class TmpFileRecordBeanMap extends TypeToken<Map<String, TmpFileRecordBean>> {
        private TmpFileRecordBeanMap() {
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
        long totalFileLength2 = tmpFileRecordBean.getTotalFileLength();
        if (this.totalFileLength != totalFileLength2) {
            DSLog.e("BreakpointResumeDownload Fail to verify total file length. Total file length in header is " + this.totalFileLength + ". Total file length in json is " + totalFileLength2, new Object[0]);
            return false;
        }
        long fileLength = tmpFileRecordBean.getFileLength();
        if (this.tmpFile.length() != fileLength) {
            DSLog.e("BreakpointResumeDownload Fail to verify file length. Actual file length is " + this.tmpFile.length() + ". Json's file length is " + fileLength, new Object[0]);
            return false;
        }
        String eTag2 = tmpFileRecordBean.getETag();
        if (TextUtils.isEmpty(this.eTag) || TextUtils.isEmpty(eTag2)) {
            DSLog.e("BreakpointResumeDownload Fail to verify eTag, header's eTag or json's eTag is empty", new Object[0]);
            return false;
        } else if (this.eTag.equals(eTag2)) {
            return true;
        } else {
            DSLog.e("BreakpointResumeDownload Fail to verify eTag, header's eTag is not equal to json's eTag", new Object[0]);
            return false;
        }
    }

    private File generateFile(String str, String str2) throws IOException {
        File file = FileUtils.getFile(str);
        if (file.exists()) {
            File file2 = FileUtils.getFile(str, str2);
            if (file2.exists() || file2.createNewFile()) {
                return file2;
            }
        } else if (!file.mkdirs()) {
            return null;
        } else {
            File file3 = FileUtils.getFile(str, str2);
            if (file3.createNewFile()) {
                return file3;
            }
        }
        return null;
    }

    public void postHandle(HttpResponse httpResponse) throws BreakpointResumeDownloadException {
        try {
            if (isFailResponse(this.range.getRangeMin(), httpResponse.getStatusCode())) {
                if (httpResponse.getStatusCode() == 416) {
                    boolean deleteTmpData = deleteTmpData();
                    DSLog.d("BreakpointResumeDownload delete tmp data result = " + deleteTmpData, new Object[0]);
                } else {
                    updateTmpFileRecord();
                }
                throw new BreakpointResumeDownloadException(httpResponse.getStatusCode(), httpResponse.getResponseMsg());
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
            boolean delete = this.tmpFile.delete();
            DSLog.e("BreakpointResumeDownload file length does not match. Delete tmp file result is" + delete, new Object[0]);
            return false;
        }
    }

    private boolean moveFile() throws IOException {
        DSLog.d("BreakpointResumeDownload Not to verify due to lack of Content-MD5 value.", new Object[0]);
        return moveFileDirectly();
    }

    private boolean moveFileDirectly() throws IOException {
        Throwable th;
        FileInputStream fileInputStream;
        try {
            fileInputStream = FileUtils.openInputStream(this.tmpFile);
            try {
                OutputStream openOutputStream = FileUtils.openOutputStream(this.formalFile);
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read > -1) {
                        openOutputStream.write(bArr, 0, read);
                    } else {
                        openOutputStream.flush();
                        FileUtils.closeCloseable(fileInputStream);
                        FileUtils.closeCloseable(openOutputStream);
                        return true;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                FileUtils.closeCloseable(fileInputStream);
                FileUtils.closeCloseable(null);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            fileInputStream = null;
            FileUtils.closeCloseable(fileInputStream);
            FileUtils.closeCloseable(null);
            throw th;
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
        Map<String, TmpFileRecordBean> map;
        return this.jsonFile.exists() && (map = this.tmpFileRecordBeanMap) != null && map.containsKey(this.tmpFileName);
    }

    private void deleteJsonRecord() throws IOException {
        this.tmpFileRecordBeanMap.remove(this.tmpFileName);
        Iterator<Map.Entry<String, TmpFileRecordBean>> it = this.tmpFileRecordBeanMap.entrySet().iterator();
        while (it.hasNext()) {
            if (!FileUtils.getFile(this.tmpFileDir, it.next().getKey()).exists()) {
                it.remove();
            }
        }
    }

    private void updateTmpFileRecord() throws IOException {
        TmpFileRecordBean createBean = createBean();
        if (this.tmpFileRecordBeanMap == null) {
            this.tmpFileRecordBeanMap = new HashMap();
        }
        this.tmpFileRecordBeanMap.put(this.tmpFileName, createBean);
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
        String str = this.tmpFileDir;
        this.jsonFile = generateFile(str, this.tmpFileName + JSON_FILE_EXTENSION);
        File file = this.jsonFile;
        if (file != null) {
            writeJsonStrToFile(file);
            return;
        }
        DSLog.e("BreakpointResumeDownload Fail to parse bean to json, delete the downloaded part of file.", new Object[0]);
        if (this.tmpFile.exists() && !this.tmpFile.delete()) {
            DSLog.e("BreakpointResumeDownload Fail to delete the downloaded part of file.", new Object[0]);
        }
    }

    private void writeJsonStrToFile(File file) throws IOException {
        FileUtils.writeStringToFile(file, new Gson().toJson(this.tmpFileRecordBeanMap), false, "UTF-8");
    }

    private String parseHeaderValue(Map<String, List<String>> map, String str) {
        List<String> list = map.get(str);
        if (list == null || list.isEmpty()) {
            return "";
        }
        String str2 = list.get(0);
        if (TextUtils.isEmpty(str2)) {
            return "";
        }
        return str2;
    }

    public static boolean addPathToSet(String str) {
        synchronized (sCurrentRequestSet) {
            if (sCurrentRequestSet.contains(str)) {
                return false;
            }
            return sCurrentRequestSet.add(str);
        }
    }

    public static void removePathFromSet(String str) {
        synchronized (sCurrentRequestSet) {
            sCurrentRequestSet.remove(str);
        }
    }

    public File getTmpFile() {
        return this.tmpFile;
    }
}

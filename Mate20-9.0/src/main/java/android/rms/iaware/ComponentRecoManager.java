package android.rms.iaware;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.json.JSONException;
import org.json.JSONObject;

public class ComponentRecoManager {
    public static final String BAD_FUNC = "badFunc";
    public static final int BAD_FUNC_BAD = 1;
    public static final int BAD_FUNC_DEFAULT = 0;
    private static final String CLOUD_FILE_PATH = "/data/system/iaware/";
    public static final String COMPONENT_CLOUD_UPDATE_ACTION = "COMPONENT_CLOUD_UPDATE_ACTION";
    private static final String COMPONENT_DATA_FILE = "component.data";
    private static final String COMPONENT_DATA_PATH = "iaware/data/";
    public static final String COMP_NAME = "compName";
    public static final String COMP_TYPE = "compType";
    private static final String DATA_HEAD = "{\"version\":";
    private static final int DEFAULT_DATA_VERION = 0;
    public static final String GOOD_FUNC = "goodFunc";
    private static final int SINGLE_LINE_MAX_COUNT = 2048;
    private static final String TAG = "ComponentRecoManager";
    private static final int TOTAL_LINE_COUNT = 20000;
    private static ComponentRecoManager mComponentRecoManager = null;
    private final ArrayMap<String, ComponentCacheInfo> mComponentMap = new ArrayMap<>();
    private boolean mIsReady = false;
    private int mWorkingVersion = -1;

    public static class ComponentCacheInfo {
        private int mBadFunc;
        private int mCompType;
        private int mGoodFunc;

        public ComponentCacheInfo(int compType, int goodFunc, int badFunc) {
            this.mCompType = compType;
            this.mGoodFunc = goodFunc;
            this.mBadFunc = badFunc;
        }

        public int getCompType() {
            return this.mCompType;
        }

        public int getGoodFunc() {
            return this.mGoodFunc;
        }

        public int getBadFunc() {
            return this.mBadFunc;
        }
    }

    public static synchronized ComponentRecoManager getInstance() {
        ComponentRecoManager componentRecoManager;
        synchronized (ComponentRecoManager.class) {
            if (mComponentRecoManager == null) {
                mComponentRecoManager = new ComponentRecoManager();
            }
            componentRecoManager = mComponentRecoManager;
        }
        return componentRecoManager;
    }

    private ComponentRecoManager() {
    }

    public synchronized void init(Context ctx) {
        AwareLog.i(TAG, "init begin.");
        if (ctx != null) {
            if (!this.mIsReady) {
                loadComponentInfo(ctx);
                this.mIsReady = true;
                AwareLog.i(TAG, "init end.");
                return;
            }
        }
        AwareLog.i(TAG, "no need to init");
    }

    public void deinit() {
        synchronized (this.mComponentMap) {
            this.mComponentMap.clear();
        }
        synchronized (this) {
            this.mIsReady = false;
        }
        AwareLog.i(TAG, "deinit.");
    }

    public void handleCloudUpdate(Context ctx) {
        loadComponentInfo(ctx);
    }

    private void loadComponentInfo(Context ctx) {
        if (ctx == null) {
            AwareLog.e(TAG, "wrong parameter!");
            return;
        }
        this.mWorkingVersion = 0;
        File custFile = loadPresetFile(COMPONENT_DATA_FILE);
        int custVersion = 0;
        ArrayMap<String, ComponentCacheInfo> custMap = new ArrayMap<>();
        if (custFile != null && custFile.exists()) {
            custVersion = decryptAndReadFile(ctx, custMap, custFile);
        }
        String fileName = generateFileNameWithMatch(COMPONENT_DATA_FILE);
        File cloudFile = new File(CLOUD_FILE_PATH + fileName);
        ArrayMap<String, ComponentCacheInfo> cloudMap = new ArrayMap<>();
        int cloudVersion = 0;
        if (cloudFile.exists()) {
            cloudVersion = decryptAndReadFile(ctx, cloudMap, cloudFile);
        }
        if (cloudVersion > custVersion) {
            synchronized (this.mComponentMap) {
                this.mComponentMap.clear();
                this.mComponentMap.putAll(cloudMap);
            }
        } else {
            synchronized (this.mComponentMap) {
                this.mComponentMap.clear();
                this.mComponentMap.putAll(custMap);
            }
        }
    }

    private File loadPresetFile(String name) {
        File cfg = HwCfgFilePolicy.getCfgFile(COMPONENT_DATA_PATH + name, 0);
        if (cfg == null) {
            AwareLog.e(TAG, "not find component data file");
            return null;
        }
        AwareLog.d(TAG, "find component data file ok.");
        return cfg;
    }

    private int decryptAndReadFile(Context ctx, ArrayMap<String, ComponentCacheInfo> map, File file) {
        InputStreamReader reader;
        InputStream fis = null;
        try {
            AwareLog.i(TAG, "read the data from base!");
            fis = new FileInputStream(file);
            if (fis.available() <= 0) {
                return 0;
            }
            if (isNormalPreset(file)) {
                AwareLog.i(TAG, "not decrypted data!");
                reader = new InputStreamReader(fis, CharacterSets.DEFAULT_CHARSET_NAME);
            } else {
                AwareLog.i(TAG, "decrypted data!");
                InputStream dis = IAwareDecrypt.decryptInputStream(ctx, fis);
                if (dis == null) {
                    closeStream(null, fis);
                    return 0;
                }
                reader = new InputStreamReader(new GZIPInputStream(dis), CharacterSets.DEFAULT_CHARSET_NAME);
            }
            int parseComponentData = parseComponentData(reader, map);
            closeStream(reader, fis);
            return parseComponentData;
        } catch (FileNotFoundException e) {
            AwareLog.e(TAG, "file not found!");
            return 0;
        } catch (IOException e2) {
            AwareLog.e(TAG, "parse file io error!");
            return 0;
        } catch (RuntimeException e3) {
            AwareLog.e(TAG, "parse file runtime exception!");
            return 0;
        } finally {
            closeStream(null, fis);
        }
    }

    private void closeStream(InputStreamReader reader, InputStream fis) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                AwareLog.w(TAG, "close reader failed!");
                if (fis != null) {
                    try {
                        fis.close();
                        return;
                    } catch (IOException e2) {
                        AwareLog.w(TAG, "close fis failed!");
                        return;
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e3) {
                        AwareLog.w(TAG, "close fis failed!");
                    }
                }
                throw th;
            }
        }
        if (fis != null) {
            fis.close();
        }
    }

    private int parseComponentData(InputStreamReader reader, ArrayMap<String, ComponentCacheInfo> map) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(reader);
            String line = getFirstLine(bufferedReader);
            if (line != null) {
                if (!line.equals("")) {
                    int dataVersion = new JSONObject(line).optInt("version", 0);
                    if (this.mWorkingVersion >= dataVersion) {
                        AwareLog.d(TAG, "component data version is the latest! version is " + this.mWorkingVersion);
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            AwareLog.w(TAG, "parsePresetData close buffered reader failed!");
                        }
                        return 0;
                    }
                    parseReader(bufferedReader, map);
                    AwareLog.d(TAG, "working version is " + this.mWorkingVersion + ";new dataVersion is " + dataVersion);
                    this.mWorkingVersion = dataVersion;
                    try {
                        bufferedReader.close();
                    } catch (IOException e2) {
                        AwareLog.w(TAG, "parsePresetData close buffered reader failed!");
                    }
                    return dataVersion;
                }
            }
            AwareLog.e(TAG, "component data first line is null!");
            try {
                bufferedReader.close();
            } catch (IOException e3) {
                AwareLog.w(TAG, "parsePresetData close buffered reader failed!");
            }
            return 0;
        } catch (IOException e4) {
            AwareLog.e(TAG, "read file exception.");
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e5) {
                    AwareLog.w(TAG, "parsePresetData close buffered reader failed!");
                }
            }
            return 0;
        } catch (JSONException e6) {
            AwareLog.e(TAG, "json format error");
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e7) {
                    AwareLog.w(TAG, "parsePresetData close buffered reader failed!");
                }
            }
            return 0;
        } catch (Throwable th) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e8) {
                    AwareLog.w(TAG, "parsePresetData close buffered reader failed!");
                }
            }
            throw th;
        }
    }

    private void parseReader(BufferedReader reader, ArrayMap<String, ComponentCacheInfo> map) throws IOException, JSONException {
        int count = 0;
        int lineCount = 0;
        StringBuffer s = new StringBuffer();
        while (true) {
            int read = reader.read();
            int result = read;
            if (read != -1) {
                char c = (char) result;
                count++;
                if (count < 2048) {
                    if (c == 13 || c == 10) {
                        count = 0;
                        if (s.length() > 0) {
                            lineCount++;
                            if (lineCount < TOTAL_LINE_COUNT) {
                                insertData(s.toString(), map);
                                s.setLength(0);
                            } else {
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        s.append(c);
                    }
                } else {
                    return;
                }
            } else {
                if (s.length() > 0) {
                    insertData(s.toString(), map);
                }
                return;
            }
        }
    }

    private void insertData(String line, ArrayMap<String, ComponentCacheInfo> map) throws JSONException {
        JSONObject jsonObject = new JSONObject(line);
        map.put(jsonObject.getString(COMP_NAME), new ComponentCacheInfo(jsonObject.getInt(COMP_TYPE), jsonObject.getInt(GOOD_FUNC), jsonObject.getInt(BAD_FUNC)));
    }

    private String getFirstLine(BufferedReader reader) throws IOException {
        StringBuffer s = new StringBuffer();
        int count = 0;
        while (true) {
            int read = reader.read();
            int result = read;
            if (read == -1) {
                return s.toString();
            }
            char c = (char) result;
            count++;
            if (count >= 2048) {
                return null;
            }
            if (c != 13 && c != 10) {
                s.append(c);
            } else if (s.length() > 0) {
                return s.toString();
            }
        }
    }

    private boolean isNormalPreset(File file) {
        BufferedReader buff = null;
        InputStream fis = null;
        try {
            InputStream fis2 = new FileInputStream(file);
            BufferedReader buff2 = new BufferedReader(new InputStreamReader(fis2, CharacterSets.DEFAULT_CHARSET_NAME));
            String line = getFirstLine(buff2);
            if (line != null) {
                boolean startsWith = line.startsWith(DATA_HEAD);
                try {
                    buff2.close();
                } catch (IOException e) {
                    AwareLog.e(TAG, "isNormalPreset BufferedReader IOException!");
                }
                try {
                    fis2.close();
                } catch (IOException e2) {
                    AwareLog.e(TAG, "isNormalPreset fis IOException!");
                }
                return startsWith;
            }
            try {
                buff2.close();
            } catch (IOException e3) {
                AwareLog.e(TAG, "isNormalPreset BufferedReader IOException!");
            }
            try {
                fis2.close();
            } catch (IOException e4) {
                AwareLog.e(TAG, "isNormalPreset fis IOException!");
            }
            return false;
        } catch (IOException e5) {
            AwareLog.e(TAG, "isNormalPreset IOException!");
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e6) {
                    AwareLog.e(TAG, "isNormalPreset BufferedReader IOException!");
                }
            }
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e7) {
                    AwareLog.e(TAG, "isNormalPreset BufferedReader IOException!");
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e8) {
                    AwareLog.e(TAG, "isNormalPreset fis IOException!");
                }
            }
            throw th;
        }
    }

    public int getComponentBadFunc(String compName) {
        ComponentCacheInfo info;
        synchronized (this.mComponentMap) {
            info = this.mComponentMap.get(compName);
        }
        if (info == null) {
            return 0;
        }
        return info.getBadFunc();
    }

    public void dumpBadComponent(PrintWriter pw) {
        Set<String> badComponentSet = new ArraySet<>();
        synchronized (this.mComponentMap) {
            for (Map.Entry<String, ComponentCacheInfo> entry : this.mComponentMap.entrySet()) {
                if ((entry.getValue().getBadFunc() & 1) == 1) {
                    badComponentSet.add(entry.getKey());
                }
            }
        }
        pw.println("component recg bad component:" + badComponentSet);
    }

    private String generateFileNameWithMatch(String fileName) {
        String[] tmp = fileName.split("\\.");
        AwareLog.d(TAG, "handle filename:" + fileName);
        if (tmp.length != 2) {
            AwareLog.e(TAG, "can't get filename without suffix");
            return null;
        }
        String realFileName = tmp[0];
        String fileNameSuffix = tmp[1];
        if (realFileName == null || fileNameSuffix == null) {
            AwareLog.e(TAG, "can't get filename without suffix and suffix name");
            return null;
        }
        String modelId = SystemProperties.get("ro.product.model", null);
        String regionId = SystemProperties.get("ro.product.locale.region", null);
        String incVersion = SystemProperties.get("ro.build.version.incremental", null);
        String releaseVer = SystemProperties.get("ro.build.version.release", null);
        if (TextUtils.isEmpty(modelId) || TextUtils.isEmpty(regionId) || TextUtils.isEmpty(releaseVer) || TextUtils.isEmpty(incVersion)) {
            AwareLog.e(TAG, "Illegal system info, no need set file id!");
            return null;
        }
        String targetFileId = buildValidFatFilename((realFileName + "_" + modelId + "_" + regionId + "_" + releaseVer + "_" + incVersion + "." + fileNameSuffix).replace(" ", "_"));
        if (targetFileId.length() <= 91) {
            return targetFileId;
        }
        AwareLog.e(TAG, "File id too long, no need set file id!");
        return null;
    }

    private String buildValidFatFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        StringBuilder res = new StringBuilder(name.length());
        int size = name.length();
        for (int i = 0; i < size; i++) {
            char c = name.charAt(i);
            if (isValidFatFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        trimFilename(res, 255);
        return res.toString();
    }

    private static void trimFilename(StringBuilder res, int maxBytes) {
        byte[] raw = res.toString().getBytes(StandardCharsets.UTF_8);
        if (raw.length > maxBytes) {
            int maxBytes2 = maxBytes - 3;
            while (raw.length > maxBytes2) {
                res.deleteCharAt(res.length() / 2);
                raw = res.toString().getBytes(StandardCharsets.UTF_8);
            }
            res.insert(res.length() / 2, "...");
        }
    }

    private static boolean isValidFatFilenameChar(char c) {
        if (!((c >= 0 && c <= 31) || c == '\"' || c == '*' || c == '/' || c == ':' || c == '<' || c == '\\' || c == '|' || c == 127)) {
            switch (c) {
                case '>':
                case '?':
                    break;
                default:
                    return true;
            }
        }
        return false;
    }
}

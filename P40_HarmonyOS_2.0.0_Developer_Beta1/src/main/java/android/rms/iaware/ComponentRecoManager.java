package android.rms.iaware;

import android.content.Context;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.huawei.hwpartiaware.BuildConfig;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import org.json.JSONException;
import org.json.JSONObject;

public class ComponentRecoManager {
    public static final String BAD_FUNC = "badFunc";
    public static final int BAD_FUNC_BAD = 1;
    public static final int BAD_FUNC_DEFAULT = 0;
    private static final String CLOUD_FILE_PATH = "/data/system/iaware/hwouc/ThirdPartyAppProperty/";
    private static final String COMPNAME_SEPARATION = "#";
    public static final String COMPONENT_CLOUD_UPDATE_ACTION = "COMPONENT_CLOUD_UPDATE_ACTION";
    private static final String COMPONENT_DATA_FILE = "component.data";
    private static final String COMPONENT_DATA_PATH = "iaware/ThirdPartyAppProperty/";
    private static final int COMPTYPE_DEFAULT = 0;
    private static final int COMPTYPE_SERVICES = 1;
    public static final String COMP_NAME = "compName";
    public static final String COMP_TYPE = "compType";
    private static final String DATA_HEAD = "{\"version\":";
    private static final int DEFAULT_DATA_VERION = 0;
    public static final String GOOD_FUNC = "goodFunc";
    private static final int GOOD_FUNC_GOOD = 1;
    private static final int SINGLE_LINE_MAX_COUNT = 2048;
    private static final Object SLOCK = new Object();
    private static final String TAG = "ComponentRecoManager";
    private static final int TOTAL_LINE_COUNT = 20000;
    private static ComponentRecoManager componentRecoManager = null;
    private final ArrayMap<String, ComponentCacheInfo> mComponentMap = new ArrayMap<>();
    private final ArrayMap<String, ArraySet<String>> mGoodServicesMap = new ArrayMap<>();
    private AtomicBoolean mIsReady = new AtomicBoolean(false);
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

    public static ComponentRecoManager getInstance() {
        ComponentRecoManager componentRecoManager2;
        synchronized (SLOCK) {
            if (componentRecoManager == null) {
                componentRecoManager = new ComponentRecoManager();
            }
            componentRecoManager2 = componentRecoManager;
        }
        return componentRecoManager2;
    }

    private ComponentRecoManager() {
    }

    public void init(Context ctx) {
        synchronized (SLOCK) {
            AwareLog.i(TAG, "init begin.");
            if (ctx != null) {
                if (!this.mIsReady.get()) {
                    loadComponentInfo(ctx);
                    this.mIsReady.set(true);
                    AwareLog.i(TAG, "init end.");
                    return;
                }
            }
            AwareLog.i(TAG, "no need to init");
        }
    }

    public void deinit() {
        synchronized (this.mComponentMap) {
            this.mComponentMap.clear();
        }
        this.mIsReady.set(false);
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
        ArrayMap<String, ArraySet<String>> custServicesMap = new ArrayMap<>();
        if (custFile != null && custFile.exists()) {
            custVersion = decryptAndReadFile(ctx, custMap, custServicesMap, custFile);
        }
        File cloudFile = new File("/data/system/iaware/hwouc/ThirdPartyAppProperty/component.data");
        ArrayMap<String, ComponentCacheInfo> cloudMap = new ArrayMap<>();
        ArrayMap<String, ArraySet<String>> cloudServicesMap = new ArrayMap<>();
        int cloudVersion = 0;
        if (cloudFile.exists()) {
            cloudVersion = decryptAndReadFile(ctx, cloudMap, cloudServicesMap, cloudFile);
        }
        if (cloudVersion > custVersion) {
            synchronized (this.mComponentMap) {
                this.mComponentMap.clear();
                this.mComponentMap.putAll((ArrayMap<? extends String, ? extends ComponentCacheInfo>) cloudMap);
            }
            synchronized (this.mGoodServicesMap) {
                this.mGoodServicesMap.clear();
                this.mGoodServicesMap.putAll((ArrayMap<? extends String, ? extends ArraySet<String>>) cloudServicesMap);
            }
            return;
        }
        synchronized (this.mComponentMap) {
            this.mComponentMap.clear();
            this.mComponentMap.putAll((ArrayMap<? extends String, ? extends ComponentCacheInfo>) custMap);
        }
        synchronized (this.mGoodServicesMap) {
            this.mGoodServicesMap.clear();
            this.mGoodServicesMap.putAll((ArrayMap<? extends String, ? extends ArraySet<String>>) custServicesMap);
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

    private int decryptAndReadFile(Context ctx, ArrayMap<String, ComponentCacheInfo> map, ArrayMap<String, ArraySet<String>> servicesMap, File file) {
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
                reader = new InputStreamReader(fis, "utf-8");
            } else {
                AwareLog.i(TAG, "decrypted data!");
                InputStream dis = IAwareDecrypt.decryptInputStream(ctx, fis);
                if (dis == null) {
                    closeStream(null, "close reader failed!");
                    closeStream(fis, "close fis failed!");
                    return 0;
                }
                reader = new InputStreamReader(new GZIPInputStream(dis), "utf-8");
            }
            int parseComponentData = parseComponentData(reader, map, servicesMap);
            closeStream(reader, "close reader failed!");
            closeStream(fis, "close fis failed!");
            return parseComponentData;
        } catch (FileNotFoundException e) {
            AwareLog.e(TAG, "file not found!");
            return 0;
        } catch (IOException e2) {
            AwareLog.e(TAG, "parse file io error!");
            return 0;
        } catch (ClassCastException e3) {
            AwareLog.e(TAG, "parse file class cast exception!");
            return 0;
        } finally {
            closeStream(null, "close reader failed!");
            closeStream(fis, "close fis failed!");
        }
    }

    private int parseComponentData(InputStreamReader reader, ArrayMap<String, ComponentCacheInfo> map, ArrayMap<String, ArraySet<String>> servicesMap) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(reader);
            String line = getFirstLine(bufferedReader);
            if (line != null) {
                if (!line.equals(BuildConfig.FLAVOR)) {
                    int dataVersion = new JSONObject(line).optInt("version", 0);
                    if (this.mWorkingVersion >= dataVersion) {
                        AwareLog.d(TAG, "component data version is the latest! version is " + this.mWorkingVersion);
                        closeStream(bufferedReader, "parsePresetData close buffered reader failed!");
                        return 0;
                    }
                    parseReader(bufferedReader, map, servicesMap);
                    AwareLog.d(TAG, "working version is " + this.mWorkingVersion + ";new dataVersion is " + dataVersion);
                    this.mWorkingVersion = dataVersion;
                    closeStream(bufferedReader, "parsePresetData close buffered reader failed!");
                    return dataVersion;
                }
            }
            AwareLog.e(TAG, "component data first line is null!");
            return 0;
        } catch (IOException e) {
            AwareLog.e(TAG, "read file exception.");
            return 0;
        } catch (JSONException e2) {
            AwareLog.e(TAG, "json format error");
            return 0;
        } finally {
            closeStream(bufferedReader, "parsePresetData close buffered reader failed!");
        }
    }

    private void parseReader(BufferedReader reader, ArrayMap<String, ComponentCacheInfo> map, ArrayMap<String, ArraySet<String>> servicesMap) throws IOException, JSONException {
        int count = 0;
        int lineCount = 0;
        StringBuffer sb = new StringBuffer();
        while (true) {
            int result = reader.read();
            if (result != -1) {
                char charResult = (char) result;
                count++;
                if (count < SINGLE_LINE_MAX_COUNT) {
                    if (charResult == '\r' || charResult == '\n') {
                        count = 0;
                        if (sb.length() > 0) {
                            lineCount++;
                            if (lineCount < TOTAL_LINE_COUNT) {
                                insertData(sb.toString(), map, servicesMap);
                                sb.setLength(0);
                            } else {
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        sb.append(charResult);
                    }
                } else {
                    return;
                }
            } else if (sb.length() > 0) {
                insertData(sb.toString(), map, servicesMap);
                return;
            } else {
                return;
            }
        }
    }

    private void insertData(String line, ArrayMap<String, ComponentCacheInfo> map, ArrayMap<String, ArraySet<String>> servicesMap) throws JSONException {
        JSONObject jsonObject = new JSONObject(line);
        String compName = jsonObject.getString(COMP_NAME);
        int compType = jsonObject.getInt(COMP_TYPE);
        int goodFunc = jsonObject.getInt(GOOD_FUNC);
        int badFunc = jsonObject.getInt(BAD_FUNC);
        if (compType == 0) {
            map.put(compName, new ComponentCacheInfo(compType, goodFunc, badFunc));
        } else if (isGoodService(compType, goodFunc, badFunc)) {
            String[] serviceInfo = compName.split(COMPNAME_SEPARATION);
            if (serviceInfo.length == 2) {
                String pkg = serviceInfo[0];
                String cls = serviceInfo[1];
                if (pkg != null && cls != null) {
                    ArraySet<String> serviceCls = servicesMap.get(pkg);
                    if (serviceCls == null) {
                        serviceCls = new ArraySet<>();
                    }
                    serviceCls.add(cls);
                    servicesMap.put(pkg, serviceCls);
                }
            }
        }
    }

    private boolean isGoodService(int compType, int goodFunc, int badFunc) {
        return compType == 1 && (badFunc & 1) != 1 && (goodFunc & 1) == 1;
    }

    private String getFirstLine(BufferedReader reader) throws IOException {
        StringBuffer sb = new StringBuffer();
        int count = 0;
        while (true) {
            int result = reader.read();
            if (result == -1) {
                return sb.toString();
            }
            char charResult = (char) result;
            count++;
            if (count >= SINGLE_LINE_MAX_COUNT) {
                return null;
            }
            if (charResult != '\r' && charResult != '\n') {
                sb.append(charResult);
            } else if (sb.length() > 0) {
                return sb.toString();
            }
        }
    }

    private boolean isNormalPreset(File file) {
        BufferedReader buff = null;
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
            buff = new BufferedReader(new InputStreamReader(fis, "utf-8"));
            String line = getFirstLine(buff);
            if (line != null) {
                boolean startsWith = line.startsWith(DATA_HEAD);
                closeStream(buff, "isNormalPreset BufferedReader IOException!");
                closeStream(fis, "isNormalPreset fis IOException!");
                return startsWith;
            }
        } catch (IOException e) {
            AwareLog.e(TAG, "isNormalPreset IOException!");
        } catch (Throwable th) {
            closeStream(null, "isNormalPreset BufferedReader IOException!");
            closeStream(null, "isNormalPreset fis IOException!");
            throw th;
        }
        closeStream(buff, "isNormalPreset BufferedReader IOException!");
        closeStream(fis, "isNormalPreset fis IOException!");
        return false;
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

    public ArrayMap<String, ArraySet<String>> getGoodServices() {
        ArrayMap<String, ArraySet<String>> result = new ArrayMap<>();
        synchronized (this.mGoodServicesMap) {
            result.putAll((ArrayMap<? extends String, ? extends ArraySet<String>>) this.mGoodServicesMap);
        }
        return result;
    }

    private void closeStream(Closeable io, String msg) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                if (msg != null) {
                    AwareLog.w(TAG, msg);
                } else {
                    AwareLog.w(TAG, "closeStream IOException");
                }
            }
        }
    }
}

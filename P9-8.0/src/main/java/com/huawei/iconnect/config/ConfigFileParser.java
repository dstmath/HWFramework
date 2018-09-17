package com.huawei.iconnect.config;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import com.huawei.iconnect.config.btconfig.BtBodyConfigItem;
import com.huawei.iconnect.config.btconfig.condition.HwCondition;
import com.huawei.iconnect.config.guideconfig.DeviceGuideConst;
import com.huawei.iconnect.config.guideconfig.GuideBodyConfigItem;
import com.huawei.iconnect.hwutil.HwLog;
import com.huawei.iconnect.wearable.config.Info;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigFileParser {
    private static final String CFG_INFO_ACTION = "action";
    private static final String CFG_INFO_AUTO_DOWNLOAD = "autoDownload";
    private static final String CFG_INFO_HW_PROTOCOL = "Bt_hw";
    private static final String CFG_INFO_MODEL = "model";
    private static final String CFG_INFO_NEEDGUIDE = "need_guide";
    private static final String CFG_INFO_PROTOCOL = "protocol";
    private static final String CFG_INFO_RECONNECT = "reconnect";
    private static final String CFG_INFO_SCO_ACTION = "sco_action";
    private static final String CFG_INFO_THIRDPARTY_PROTOCOL = "Bt";
    private static final String CFG_INFO_UUID_128 = "uuid_128";
    private static final String CFG_INFO_UUID_16 = "uuid_16";
    private static final String CFG_INFO_UUID_32 = "uuid_32";
    private static final String CFG_INFO_VENDOR = "vendor";
    private static final String CFG_INFO_VERSION = "version";
    private static final String CFG_NODE_ACTION = "action";
    private static final String CFG_NODE_APP = "app";
    private static final String CFG_NODE_APPID = "appid";
    private static final String CFG_NODE_BT_BODY = "btbody";
    private static final String CFG_NODE_CONDITION = "condition";
    private static final String CFG_NODE_CONDITION_COMPANY_ID = "company_id";
    private static final String CFG_NODE_CONDITION_M_DATA = "m_data";
    private static final String CFG_NODE_CONDITION_REGEX = "regex_name";
    private static final String CFG_NODE_CONDITION_STANDARD = "standard";
    private static final String CFG_NODE_DEV_VERSION = "version";
    private static final String CFG_NODE_FILE_VERSION = "version";
    private static final String CFG_NODE_GUIDE_BODY = "guidebody";
    private static final String CFG_NODE_HEADER = "header";
    private static final String CFG_NODE_HUAWEIPARTY = "huaweiparty";
    private static final String CFG_NODE_HW_CONDITION = "hw_condition";
    private static final String CFG_NODE_INFO = "info";
    private static final String CFG_NODE_INFO_TEXT = "info";
    private static final String CFG_NODE_INFO_URL = "url";
    private static final String CFG_NODE_MODEL = "model";
    private static final String CFG_NODE_NAME = "name";
    private static final String CFG_NODE_PARAMS = "params";
    private static final String CFG_NODE_PROTOCOL = "protocol";
    private static final String CFG_NODE_SPEC_VERSION = "specversion";
    private static final String CFG_NODE_THIRDPARTY = "thirdparty";
    private static final String CFG_NODE_THIRDPARTY_CONDITION = "condition";
    private static final String CFG_NODE_VENDOR = "vendor";
    public static final int CMP_EQUAL = 0;
    public static final int CMP_GREATER = 1;
    public static final int CMP_LESS = -1;
    public static final String CONFIG_FILE_REL_NAME = "/emcom/noncell/device_guide.json";
    public static final String CONFIG_FILE_REL_PATH = "/emcom/noncell";
    public static final String CURRENT_SUPPORT_CONFIG_VERSION = "1.0.0";
    public static final String DEFAULT_CONFIG_FILE_OF_DEVICE_GUIDE = "device_guide.json";
    private static final String ICONNECT_PACKAGE_NAME = "com.huawei.iconnect";
    private static final String TAG = ConfigFileParser.class.getSimpleName();

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0057 A:{SYNTHETIC, Splitter: B:21:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ConfigFileStruct readJsonStream(InputStream in) throws IOException {
        Throwable th;
        Throwable th2 = null;
        if (in == null) {
            return null;
        }
        ConfigFileStruct fileStruct = new ConfigFileStruct();
        JsonReader reader = null;
        try {
            JsonReader reader2 = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                reader2.beginObject();
                while (reader2.hasNext()) {
                    String name = reader2.nextName();
                    HwLog.d(TAG, "readJsonStream" + name);
                    if (name.equals(CFG_NODE_HEADER)) {
                        fileStruct.setHeader(readHeader(reader2));
                    } else if (name.equals(CFG_NODE_GUIDE_BODY)) {
                        fileStruct.setGuideBodyItems(readConfigArray(reader2));
                    } else if (name.equals(CFG_NODE_BT_BODY)) {
                        fileStruct.setBtBodyItems(readBtBody(reader2));
                    } else {
                        reader2.skipValue();
                    }
                }
                reader2.endObject();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return fileStruct;
                }
                throw th2;
            } catch (Throwable th4) {
                th = th4;
                reader = reader2;
                if (reader != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 == null) {
                throw th2;
            }
            throw th;
        }
    }

    private static ConfigHeader readHeader(JsonReader reader) throws IOException {
        ConfigHeader header = new ConfigHeader();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("version")) {
                header.setFileVersion(reader.nextString());
                HwLog.d(TAG, "version = " + header.getFileVersion());
            } else if (name.equals(CFG_NODE_SPEC_VERSION)) {
                header.setSpecVersion(reader.nextString());
                HwLog.d(TAG, "setSpecVersion = " + header.getSpecVersion());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return header;
    }

    private static String[] toCovert_128(String[] uuids_16_32) {
        String[] uuids_128 = new String[uuids_16_32.length];
        int i = 0;
        for (String s : uuids_16_32) {
            String uuid = s.substring(2) + "-0000-1000-8000-00805f9b34fb";
            int lengthTmp = uuid.length();
            StringBuilder aBuilder = new StringBuilder();
            if (lengthTmp < 36) {
                int toIncre = 36 - lengthTmp;
                for (int j = 0; j < toIncre; j++) {
                    aBuilder.append("0");
                }
            }
            aBuilder.append(uuid);
            uuids_128[i] = aBuilder.toString();
            i++;
        }
        return uuids_128;
    }

    private static List<BtBodyConfigItem> readBtBody(JsonReader reader) throws IOException {
        List<BtBodyConfigItem> configs = new ArrayList();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(CFG_NODE_HUAWEIPARTY)) {
                readBtBodyConfigArray(reader, configs, "Bt_hw");
            } else if (name.equals(CFG_NODE_THIRDPARTY)) {
                readBtBodyConfigArray(reader, configs, "Bt");
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return configs;
    }

    private static void readBtBodyConfigArray(JsonReader reader, List<BtBodyConfigItem> configs, String protocol) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            Info info = new Info();
            HwCondition hwCondition = new HwCondition();
            readBtBodyConfig(reader, info, hwCondition);
            info.setProtocol(protocol);
            HwLog.d(TAG, hwCondition.toString());
            HwLog.d(TAG, info.toString());
            configs.add(new BtBodyConfigItem(hwCondition, info));
        }
        reader.endArray();
    }

    private static void readBtBodyConfig(JsonReader reader, Info info, HwCondition hwCondition) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(CFG_NODE_HW_CONDITION) || name.equals("condition")) {
                readbtBodyHwCondition(reader, hwCondition);
            } else if (name.equals(DeviceGuideConst.DIALOG_MODE_INFO)) {
                readbtBodyInfo(reader, info);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static void readbtBodyInfo(JsonReader reader, Info info) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(CFG_INFO_SCO_ACTION)) {
                info.setScoAction(reader.nextString());
            } else if (name.equals("protocol")) {
                info.setProtocol(reader.nextString());
            } else if (name.equals("vendor")) {
                info.setVendor(reader.nextString());
            } else if (name.equals("model")) {
                info.setModel(reader.nextString());
            } else if (name.equals("version")) {
                info.setVersion(reader.nextString());
            } else if (name.equals("action")) {
                info.setAction(reader.nextString());
            } else if (name.equals(CFG_INFO_RECONNECT)) {
                info.setReconnect(reader.nextBoolean());
            } else if (name.equals(CFG_INFO_NEEDGUIDE)) {
                info.setNeedGuide(reader.nextBoolean());
            } else if (name.equals(CFG_INFO_AUTO_DOWNLOAD)) {
                info.setAutoDownload(reader.nextBoolean());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static String[] getStringArray(JsonReader reader) throws IOException {
        List<String> modelList = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            modelList.add(reader.nextString());
        }
        reader.endArray();
        return (String[]) modelList.toArray(new String[0]);
    }

    private static void readbtBodyHwCondition(JsonReader reader, HwCondition hwCondition) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(CFG_NODE_CONDITION_REGEX)) {
                hwCondition.setRegexName(getStringArray(reader));
            } else if (name.equals(CFG_NODE_CONDITION_COMPANY_ID)) {
                hwCondition.setCompanyId(Integer.valueOf(reader.nextString().substring(2), 16).intValue());
            } else if (name.equals(CFG_NODE_CONDITION_M_DATA)) {
                hwCondition.setData(reader.nextInt());
            } else if (name.equals(CFG_NODE_CONDITION_STANDARD)) {
                hwCondition.setStandard(reader.nextString());
            } else if (name.equals(CFG_INFO_UUID_16) || name.equals(CFG_INFO_UUID_32)) {
                hwCondition.setUuid128(toCovert_128(getStringArray(reader)));
            } else if (name.equals(CFG_INFO_UUID_128)) {
                hwCondition.setUuid128(getStringArray(reader));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static List<GuideBodyConfigItem> readConfigArray(JsonReader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        List<GuideBodyConfigItem> configs = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            GuideBodyConfigItem configItem = new GuideBodyConfigItem();
            readConfig(reader, configItem);
            configs.add(configItem);
        }
        reader.endArray();
        return configs;
    }

    private static void readConfig(JsonReader reader, GuideBodyConfigItem configItem) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("condition")) {
                readCondition(reader, configItem);
            } else if (name.equals(CFG_NODE_APP)) {
                readApp(reader, configItem);
            } else if (name.equals(DeviceGuideConst.DIALOG_MODE_INFO)) {
                readAlertInfo(reader, configItem);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static void readAlertInfo(JsonReader reader, GuideBodyConfigItem configItem) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(CFG_NODE_INFO_URL)) {
                configItem.setInfoUrl(reader.nextString());
            } else if (name.equals(DeviceGuideConst.DIALOG_MODE_INFO)) {
                configItem.setInfoTextId(reader.nextInt());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static void readCondition(JsonReader reader, GuideBodyConfigItem configItem) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("protocol")) {
                configItem.setProtocol(reader.nextString());
            } else if (name.equals("vendor")) {
                configItem.setVendor(reader.nextString());
            } else if (name.equals("model")) {
                readModelList(reader, configItem);
            } else if (name.equals("version")) {
                configItem.setVersion(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static void readModelList(JsonReader reader, GuideBodyConfigItem configItem) throws IOException {
        List<String> modelList = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            modelList.add(reader.nextString());
        }
        reader.endArray();
        configItem.setModelList(modelList);
    }

    private static void readApp(JsonReader reader, GuideBodyConfigItem configItem) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(CFG_NODE_APPID)) {
                configItem.setAppid(reader.nextString());
            } else if (name.equals("action")) {
                readActionList(reader, configItem);
            } else if (name.equals(CFG_NODE_PARAMS)) {
                configItem.setParams(readParams(reader));
            } else if (name.equals(CFG_NODE_NAME)) {
                readAppName(reader, configItem);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static void readAppName(JsonReader reader, GuideBodyConfigItem configItem) throws IOException {
        HashMap<String, String> apkNameMap = new HashMap();
        reader.beginObject();
        while (reader.hasNext()) {
            apkNameMap.put(reader.nextName(), reader.nextString());
        }
        reader.endObject();
        configItem.setApkName(apkNameMap);
    }

    private static void readActionList(JsonReader reader, GuideBodyConfigItem configItem) throws IOException {
        HashMap<String, String> actionMap = new HashMap();
        reader.beginObject();
        while (reader.hasNext()) {
            actionMap.put(reader.nextName(), reader.nextString());
        }
        reader.endObject();
        configItem.setActionList(actionMap);
    }

    private static HashMap<String, Object> readParams(JsonReader reader) throws IOException {
        HashMap<String, Object> params = new HashMap();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (reader.peek() == JsonToken.STRING) {
                params.put(name, reader.nextString());
            } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                List<String> list = new ArrayList();
                reader.beginArray();
                while (reader.peek() != JsonToken.END_ARRAY) {
                    list.add(reader.nextString());
                }
                reader.endArray();
                params.put(name, list);
            }
        }
        reader.endObject();
        return params;
    }

    public static ConfigFileStruct parseConfigFile(Context context, String path, boolean isAsset) {
        InputStream stream = null;
        HwLog.d(TAG, "path " + path + " isAsset " + isAsset);
        if (isAsset) {
            Context iConnectContext = null;
            try {
                iConnectContext = context.createPackageContext(ICONNECT_PACKAGE_NAME, 0);
            } catch (NameNotFoundException e) {
                try {
                    Log.e(TAG, e.getLocalizedMessage());
                } catch (IOException e2) {
                    Log.e(TAG, "chooseConfigFile() exception " + e2.getLocalizedMessage());
                    e2.printStackTrace();
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "exception " + e22.getLocalizedMessage());
                            e22.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "exception " + e222.getLocalizedMessage());
                            e222.printStackTrace();
                        }
                    }
                }
            }
            if (iConnectContext == null) {
                return null;
            }
            try {
                stream = iConnectContext.getResources().getAssets().open(path);
            } catch (IOException e2222) {
                Log.e(TAG, e2222.getLocalizedMessage());
                e2222.printStackTrace();
            }
        } else {
            stream = new FileInputStream(path);
        }
        Log.e(TAG, "parseConfigFile path " + path);
        ConfigFileStruct cfgStruct = readJsonStream(stream);
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e22222) {
                Log.e(TAG, "exception " + e22222.getLocalizedMessage());
                e22222.printStackTrace();
            }
        }
        return cfgStruct;
    }

    public static int versionCompare(String strA, String strB) {
        String[] argA = strA.split(Pattern.quote("."));
        String[] argB = strB.split(Pattern.quote("."));
        int len = argA.length > argB.length ? argB.length : argA.length;
        int i = 0;
        while (i < len) {
            try {
                int verA = Integer.parseInt(argA[i]);
                int verB = Integer.parseInt(argB[i]);
                HwLog.d(TAG, "verA =" + verA + " verB =" + verB);
                if (verA > verB) {
                    return 1;
                }
                if (verA < verB) {
                    return -1;
                }
                i++;
            } catch (NumberFormatException e) {
                Log.e(TAG, "NumberFormatException occurs in versionCompare() " + e.getLocalizedMessage());
            }
        }
        return 0;
    }
}

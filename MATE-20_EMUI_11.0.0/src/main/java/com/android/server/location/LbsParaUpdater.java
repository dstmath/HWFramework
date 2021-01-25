package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import huawei.cust.HwCfgFilePolicy;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LbsParaUpdater {
    private static final String CONFIG_FILE_DIRECTORY = "emcom/noncell";
    private static final String CONFIG_FILE_NAME = "/lbsconfig.json";
    private static final String CONFIG_HWSUPL_FILE_NAME = "/hwsuplconfig.json";
    private static final String CONFIG_HWSUPL_VERSION_IN_DATABASE = "hwsupl_config_version";
    private static final String CONFIG_VERSION_IN_DATABASE = "config_version";
    private static final int DEFAULT_SIZE = 16;
    private static final int EMCOM_INVOK_PARAM_SIZE = 3;
    private static final String EMCOM_PARA_READY_ACTION = "huawei.intent.action.ACTION_EMCOM_PARA_READY";
    private static final String EXTRA_EMCOM_PARA_READY_REC = "EXTRA_EMCOM_PARA_READY_REC";
    private static final String JSON_CONFIG_LIST_ADD_PREFIX = "+";
    private static final String JSON_CONFIG_LIST_DEL_PREFIX = "-";
    private static final String JSON_CONFIG_TYPE_LIST = "list";
    private static final String JSON_CONFIG_TYPE_PARAM = "param";
    private static final String JSON_TAG_CONFIGRATION = "configration";
    private static final String JSON_TAG_HEADER = "header";
    private static final String JSON_TAG_LIST_ADD = "add";
    private static final String JSON_TAG_LIST_DELETE = "delete";
    private static final String JSON_TAG_NAME = "name";
    private static final String JSON_TAG_TYPE = "type";
    private static final String JSON_TAG_VALUE = "value";
    private static final String JSON_TAG_VERSION = "version";
    private static final int MASKBIT_PARATYPE_NONCELL_BT = 4;
    private static final int PARATYPE_NONCELL_BT = 16;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final int PARA_UPGRADE_FILE_NOTEXIST = 0;
    private static final int PARA_UPGRADE_RESPONSE_FILE_ERROR = 6;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_ALREADY = 4;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_FAILURE = 9;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_PENDING = 7;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_SUCCESS = 8;
    private static final int PARA_UPGRADE_RESPONSE_VERSION_MISMATCH = 5;
    private static final String RECEIVE_EMCOM_PARA_UPGRADE_PERMISSION = "huawei.permission.RECEIVE_EMCOM_PARA_UPGRADE";
    private static final String TAG = "LbsParaUpdater";
    private static final int UNKOWN_CONFIG_VERSION = -1;
    private static volatile LbsParaUpdater sInstance;
    private int configVersion = -1;
    private int hwSuplconfigVersion = -1;
    private boolean isRunning = false;
    private Context mContext;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.LbsParaUpdater.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                LBSLog.w(LbsParaUpdater.TAG, false, "onReceive: intent is null", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                LBSLog.w(LbsParaUpdater.TAG, false, "onReceive: get empty action", new Object[0]);
            } else if ("huawei.intent.action.ACTION_EMCOM_PARA_READY".equals(action)) {
                int cotaParaBitRec = intent.getIntExtra("EXTRA_EMCOM_PARA_READY_REC", 0);
                LBSLog.i(LbsParaUpdater.TAG, false, "onReceive: cotaParaBitRec:%{public}d", Integer.valueOf(cotaParaBitRec));
                if ((cotaParaBitRec & 16) == 0) {
                    LBSLog.i(LbsParaUpdater.TAG, false, "onReceive: broadcast is not for Noncell", new Object[0]);
                } else {
                    LbsParaUpdater.this.runUpgradeThread(true);
                }
            }
        }
    };
    private ArrayList<ConfigeParam> paramList = new ArrayList<>(16);

    private LbsParaUpdater(Context context) {
        this.mContext = context;
    }

    public static LbsParaUpdater getInstance(Context context) {
        LbsParaUpdater lbsParaUpdater;
        synchronized (LbsParaUpdater.class) {
            if (sInstance == null) {
                sInstance = new LbsParaUpdater(context);
            }
            lbsParaUpdater = sInstance;
        }
        return lbsParaUpdater;
    }

    public void init() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("huawei.intent.action.ACTION_EMCOM_PARA_READY");
        this.mContext.registerReceiver(this.myReceiver, intentFilter, "huawei.permission.RECEIVE_EMCOM_PARA_UPGRADE", null);
        runUpgradeThread(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void runUpgradeThread(boolean isCotaBroadcast) {
        if (!this.isRunning) {
            this.isRunning = true;
            UpgradeLbsConfigThread thread = new UpgradeLbsConfigThread(isCotaBroadcast);
            thread.setName("UpgradeLbsConfigThread");
            thread.start();
        }
    }

    /* access modifiers changed from: private */
    public class UpgradeLbsConfigThread extends Thread {
        HwSuplServiceParse mHwSuplServiceParse = HwSuplServiceParse.getInstance(LbsParaUpdater.this.mContext);
        private boolean mIsCotaBroadcast;

        UpgradeLbsConfigThread(boolean isCotaBroadcast) {
            this.mIsCotaBroadcast = isCotaBroadcast;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            String filePath = LbsParaUpdater.this.getUpdateConfigFilePath(this.mIsCotaBroadcast, LbsParaUpdater.CONFIG_FILE_NAME);
            String hwSuplPath = LbsParaUpdater.this.getUpdateHwSuplConfigFilePath(this.mIsCotaBroadcast, LbsParaUpdater.CONFIG_HWSUPL_FILE_NAME);
            if (!"".equals(filePath) || !"".equals(hwSuplPath)) {
                if (LbsParaUpdater.this.parseConfigFile(filePath, this.mIsCotaBroadcast)) {
                    LbsParaUpdater.this.updateConfigToDataBase();
                    LbsParaUpdater.this.reportParseError(this.mIsCotaBroadcast, 8);
                    LBSLog.i(LbsParaUpdater.TAG, false, "config file upgrade success!", new Object[0]);
                } else {
                    LbsParaUpdater.this.reportParseError(this.mIsCotaBroadcast, 9);
                    LBSLog.e(LbsParaUpdater.TAG, false, "config file parse fail!", new Object[0]);
                }
                if (this.mHwSuplServiceParse.isMatchRequirement(hwSuplPath, this.mIsCotaBroadcast)) {
                    this.mHwSuplServiceParse.updateConfigToDataBase(LbsParaUpdater.this.hwSuplconfigVersion);
                    LbsParaUpdater.this.reportParseError(this.mIsCotaBroadcast, 8);
                    LBSLog.i(LbsParaUpdater.TAG, false, "Hwsuplconfig file upgrade success!", new Object[0]);
                } else {
                    LbsParaUpdater.this.reportParseError(this.mIsCotaBroadcast, 9);
                    LBSLog.e(LbsParaUpdater.TAG, false, "Hwconfig file parse fail!", new Object[0]);
                }
                LbsParaUpdater.this.isRunning = false;
                return;
            }
            LBSLog.w(LbsParaUpdater.TAG, false, "no need to update configration", new Object[0]);
            LbsParaUpdater.this.reportParseError(this.mIsCotaBroadcast, 9);
            LbsParaUpdater.this.isRunning = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getUpdateConfigFilePath(boolean isCotaBroadcast, String configPath) {
        String configFile = getCoteParaFilePath(configPath);
        this.configVersion = getConfigVersion(configFile);
        int savedVersion = Settings.Global.getInt(this.mContext.getContentResolver(), CONFIG_VERSION_IN_DATABASE, -1);
        LBSLog.i(TAG, false, "config version : %{public}d, saved version : %{public}d", Integer.valueOf(this.configVersion), Integer.valueOf(savedVersion));
        int i = this.configVersion;
        if (i == -1) {
            reportParseError(isCotaBroadcast, 0);
            return "";
        } else if (savedVersion < i) {
            return configFile;
        } else {
            reportParseError(isCotaBroadcast, 5);
            return "";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getUpdateHwSuplConfigFilePath(boolean isCotaBroadcast, String configPath) {
        String configFile = getCoteParaFilePath(configPath);
        this.hwSuplconfigVersion = getConfigVersion(configFile);
        int savedHwSuplVersion = Settings.Global.getInt(this.mContext.getContentResolver(), CONFIG_HWSUPL_VERSION_IN_DATABASE, -1);
        LBSLog.i(TAG, false, "hwSuplconfigVersion : %{public}d, saved version : %{public}d", Integer.valueOf(this.hwSuplconfigVersion), Integer.valueOf(savedHwSuplVersion));
        int i = this.hwSuplconfigVersion;
        if (i == -1) {
            LBSLog.i(TAG, false, "savedHwSuplVersion : %{public}d", Integer.valueOf(savedHwSuplVersion));
            reportParseError(isCotaBroadcast, 0);
            return "";
        } else if (savedHwSuplVersion < i) {
            return configFile;
        } else {
            LBSLog.i(TAG, false, "savedHwSuplVersion: %{public}d", Integer.valueOf(savedHwSuplVersion));
            reportParseError(isCotaBroadcast, 5);
            return "";
        }
    }

    private int getConfigVersion(String path) {
        String jsonStr = readJsonFile(path);
        if (jsonStr == null || jsonStr.length() == 0) {
            LBSLog.w(TAG, false, "config file not exist or is empty", new Object[0]);
            return -1;
        }
        try {
            JSONObject headerObj = new JSONObject(jsonStr).optJSONObject(JSON_TAG_HEADER);
            if (headerObj == null) {
                return -1;
            }
            int version = headerObj.optInt("version");
            if (version < 0) {
                return -1;
            }
            return version;
        } catch (JSONException e) {
            LBSLog.e(TAG, false, "readJsonFile JSONException", new Object[0]);
            return -1;
        }
    }

    public String readJsonFile(String filePath) {
        String jsonStr;
        FileReader fileReader = null;
        try {
            FileReader fileReader2 = new FileReader(filePath);
            StringBuffer buffer = new StringBuffer(16);
            for (int character = fileReader2.read(); character != -1; character = fileReader2.read()) {
                buffer.append((char) character);
            }
            jsonStr = buffer.toString();
            try {
                fileReader2.close();
            } catch (IOException e) {
                LBSLog.e(TAG, false, "readJsonFile in close, IOException", new Object[0]);
            }
        } catch (IOException e2) {
            LBSLog.e(TAG, false, "readJsonFile IOException", new Object[0]);
            jsonStr = "";
            if (0 != 0) {
                fileReader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fileReader.close();
                } catch (IOException e3) {
                    LBSLog.e(TAG, false, "readJsonFile in close, IOException", new Object[0]);
                }
            }
            throw th;
        }
        return jsonStr;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfigToDataBase() {
        LBSLog.i(TAG, false, "parse config file and restore configration to DB", new Object[0]);
        int listSize = this.paramList.size();
        for (int i = 0; i < listSize; i++) {
            ConfigeParam param = this.paramList.get(i);
            Settings.Global.putString(this.mContext.getContentResolver(), param.getName(), param.getValue());
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), CONFIG_VERSION_IN_DATABASE, this.configVersion);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean parseConfigFile(String filePath, boolean isCotaBroadcast) {
        String jsonStr = readJsonFile(filePath);
        if (jsonStr == null || jsonStr.length() == 0) {
            reportParseError(isCotaBroadcast, 0);
            LBSLog.w(TAG, false, "parseConfigFile fail, because file not exists", new Object[0]);
            return false;
        }
        try {
            JSONArray configArray = new JSONObject(jsonStr).optJSONArray(JSON_TAG_CONFIGRATION);
            if (configArray == null) {
                reportParseError(isCotaBroadcast, 6);
                LBSLog.w(TAG, false, "parseConfigFile fail, because get configration null", new Object[0]);
                return false;
            }
            int length = configArray.length();
            for (int i = 0; i < length; i++) {
                Object obj = configArray.opt(i);
                if (!(obj instanceof JSONObject)) {
                    reportParseError(isCotaBroadcast, 6);
                    LBSLog.w(TAG, false, "parseConfigFile fail, because file config parse error", new Object[0]);
                    return false;
                } else if (!parseParams((JSONObject) obj, isCotaBroadcast)) {
                    return false;
                }
            }
            return true;
        } catch (JSONException e) {
            reportParseError(isCotaBroadcast, 6);
            LBSLog.e(TAG, false, "parseConfigFile fail, because of parse error", new Object[0]);
            return false;
        }
    }

    private boolean parseParams(JSONObject configObj, boolean isCotaBroadcast) {
        String paraName = configObj.optString(JSON_TAG_NAME);
        if (paraName == null || "".equals(paraName)) {
            LBSLog.w(TAG, false, "parse params fail, because wrong param name", new Object[0]);
            return false;
        } else if (JSON_CONFIG_TYPE_LIST.equals(configObj.optString("type"))) {
            restoreParam(paraName, ("" + generateListToString(configObj.optJSONArray(JSON_TAG_LIST_ADD), JSON_CONFIG_LIST_ADD_PREFIX)) + generateListToString(configObj.optJSONArray(JSON_TAG_LIST_DELETE), "-"));
            return true;
        } else if (JSON_CONFIG_TYPE_PARAM.equals(configObj.optString("type"))) {
            restoreParam(paraName, configObj.optString("value"));
            return true;
        } else {
            LBSLog.w(TAG, false, "parse params fail, because wrong configration type", new Object[0]);
            return false;
        }
    }

    private String generateListToString(JSONArray jsonArray, String prefix) {
        if (jsonArray == null) {
            return "";
        }
        String value = "";
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            value = value + prefix + jsonArray.optString(i) + ",";
        }
        LBSLog.i(TAG, false, "generateListToString : %{public}s", value);
        return value;
    }

    private void restoreParam(String name, String value) {
        ConfigeParam param = new ConfigeParam();
        param.setName(name);
        param.setValue(value);
        this.paramList.add(param);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportParseError(boolean isCotaBroadcast, int errorCode) {
        if (isCotaBroadcast) {
            responseForParaUpdate(errorCode);
        }
        this.paramList.clear();
    }

    private String getCoteParaFilePath(String configPath) {
        String upgradePath = "";
        try {
            String[] cfgFileInfos = HwCfgFilePolicy.getDownloadCfgFile(CONFIG_FILE_DIRECTORY, CONFIG_FILE_DIRECTORY + configPath);
            if (cfgFileInfos == null) {
                LBSLog.e(TAG, false, "Both default and cota config files not exist", new Object[0]);
                return upgradePath;
            }
            String upgradeVersion = cfgFileInfos[1];
            upgradePath = cfgFileInfos[0];
            if (upgradePath.contains("cota")) {
                LBSLog.i(TAG, false, "upgrade cota config file, version is: %{public}s", upgradeVersion);
            } else {
                LBSLog.i(TAG, false, "upgrade system config file, version is: %{public}s", upgradeVersion);
            }
            LBSLog.d(TAG, false, "upgrade file path : %{private}s", upgradePath);
            return upgradePath;
        } catch (NoClassDefFoundError e) {
            LBSLog.e(TAG, false, "getCoteParaFilePath NoClassDefFoundError exception", new Object[0]);
        }
    }

    public void responseForParaUpdate(int result) {
        try {
            Class<?> emcomManagerClass = Class.forName("android.emcom.EmcomManager");
            Object emcomManagerInstance = emcomManagerClass.getDeclaredMethod("getInstance", new Class[0]).invoke(emcomManagerClass, new Object[0]);
            if (emcomManagerInstance != null) {
                Class[] argClass = new Class[3];
                for (Class cls : argClass) {
                    Class cls2 = Integer.TYPE;
                }
                emcomManagerClass.getDeclaredMethod("responseForParaUpgrade", argClass).invoke(emcomManagerInstance, 16, 1, Integer.valueOf(result));
                LBSLog.i(TAG, false, "response done with result: %{public}d", Integer.valueOf(result));
                return;
            }
            LBSLog.w(TAG, false, "emcomManagerInstance is null", new Object[0]);
        } catch (NoSuchMethodException e) {
            LBSLog.e(TAG, false, "response exception: NoSuchMethod", new Object[0]);
        } catch (IllegalAccessException e2) {
            LBSLog.e(TAG, false, "response exception: IllegalAccessException", new Object[0]);
        } catch (ClassNotFoundException e3) {
            LBSLog.e(TAG, false, "response exception: EmcomManager not found", new Object[0]);
        } catch (InvocationTargetException e4) {
            LBSLog.e(TAG, false, "response exception: InvocationTargetException", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public class ConfigeParam {
        private String parameterName;
        private String parameterValue;

        private ConfigeParam() {
        }

        public String getName() {
            return this.parameterName;
        }

        public void setName(String name) {
            this.parameterName = name;
        }

        public String getValue() {
            return this.parameterValue;
        }

        public void setValue(String value) {
            this.parameterValue = value;
        }
    }
}

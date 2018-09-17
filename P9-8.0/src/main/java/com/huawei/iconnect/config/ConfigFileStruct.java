package com.huawei.iconnect.config;

import android.content.Context;
import com.huawei.iconnect.config.btconfig.BtBodyConfigItem;
import com.huawei.iconnect.config.guideconfig.GuideBodyConfigItem;
import com.huawei.iconnect.hwutil.HwLog;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.util.List;

public class ConfigFileStruct {
    private static final String TAG = ConfigFileStruct.class.getSimpleName();
    private static ConfigFileStruct mCfgStruct;
    private List<BtBodyConfigItem> btBodyItems;
    private List<GuideBodyConfigItem> guideBodyItems;
    private ConfigHeader header;

    public static synchronized ConfigFileStruct getCfgFileStruct(Context context, boolean forceUpdate) {
        ConfigFileStruct configFileStruct;
        synchronized (ConfigFileStruct.class) {
            if (mCfgStruct == null) {
                mCfgStruct = createCfgFileStruct(context);
            } else if (forceUpdate) {
                mCfgStruct = createCfgFileStruct(context);
            }
            configFileStruct = mCfgStruct;
        }
        return configFileStruct;
    }

    private static ConfigFileStruct createCfgFileStruct(Context context) {
        ConfigFileStruct cotaCfgStruct;
        ConfigFileStruct cfgStruct;
        ConfigFileStruct assetCfgStruct = ConfigFileParser.parseConfigFile(context, ConfigFileParser.DEFAULT_CONFIG_FILE_OF_DEVICE_GUIDE, true);
        HwLog.d(TAG, "assetCfgStruct" + assetCfgStruct);
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(ConfigFileParser.CONFIG_FILE_REL_PATH, ConfigFileParser.CONFIG_FILE_REL_NAME);
        if (cfgFileInfo == null || cfgFileInfo.length <= 0) {
            HwLog.e(TAG, "Invalid cfgFileInfo");
            cotaCfgStruct = null;
        } else {
            String path = cfgFileInfo[0];
            if (checkCotaFileAvailability(path)) {
                cotaCfgStruct = ConfigFileParser.parseConfigFile(context, path, false);
            } else {
                cotaCfgStruct = null;
            }
        }
        if (assetCfgStruct == null && cotaCfgStruct != null) {
            cfgStruct = cotaCfgStruct;
        } else if (assetCfgStruct != null && cotaCfgStruct == null) {
            cfgStruct = assetCfgStruct;
        } else if (assetCfgStruct != null) {
            ConfigHeader assetHeader = assetCfgStruct.getHeader();
            ConfigHeader cotaHeader = cotaCfgStruct.getHeader();
            if (assetHeader == null || cotaHeader == null) {
                return null;
            }
            String assetFileVersion = assetHeader.getFileVersion();
            String assetSpecVersion = assetHeader.getSpecVersion();
            String cotaFileVersion = cotaHeader.getFileVersion();
            String cotaSpecVersion = cotaHeader.getSpecVersion();
            if (ConfigFileParser.versionCompare(cotaSpecVersion, assetSpecVersion) > 0) {
                cfgStruct = assetCfgStruct;
            } else if (ConfigFileParser.versionCompare(assetSpecVersion, ConfigFileParser.CURRENT_SUPPORT_CONFIG_VERSION) == 0 && ConfigFileParser.versionCompare(cotaSpecVersion, assetSpecVersion) < 0) {
                cfgStruct = assetCfgStruct;
            } else if (ConfigFileParser.versionCompare(cotaSpecVersion, assetSpecVersion) == 0) {
                switch (ConfigFileParser.versionCompare(assetFileVersion, cotaFileVersion)) {
                    case ConfigFileParser.CMP_LESS /*-1*/:
                        cfgStruct = cotaCfgStruct;
                        break;
                    case 0:
                    case 1:
                        cfgStruct = assetCfgStruct;
                        break;
                    default:
                        cfgStruct = null;
                        break;
                }
            } else {
                HwLog.e(TAG, "Unable to handle this case on this version");
                cfgStruct = null;
            }
        } else {
            HwLog.e(TAG, "Both config files are invalid, exit");
            cfgStruct = null;
        }
        return cfgStruct;
    }

    private static boolean checkCotaFileAvailability(String path) {
        return new File(path).isFile();
    }

    public List<GuideBodyConfigItem> getGuideBodyItems() {
        return this.guideBodyItems;
    }

    public void setGuideBodyItems(List<GuideBodyConfigItem> guideBodyItems) {
        this.guideBodyItems = guideBodyItems;
    }

    public List<BtBodyConfigItem> getBtBodyItems() {
        return this.btBodyItems;
    }

    public void setBtBodyItems(List<BtBodyConfigItem> btBodyItems) {
        this.btBodyItems = btBodyItems;
    }

    public ConfigHeader getHeader() {
        return this.header;
    }

    public void setHeader(ConfigHeader header) {
        this.header = header;
    }
}

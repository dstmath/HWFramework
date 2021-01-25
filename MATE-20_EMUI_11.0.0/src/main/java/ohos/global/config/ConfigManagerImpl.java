package ohos.global.config;

import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.global.resource.AccessDeniedException;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLogLabel;

public class ConfigManagerImpl extends ConfigManager {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ConfigManagerImpl");

    @Override // ohos.global.config.ConfigManager
    public Config getConfig(ConfigType configType) throws NotExistException, AccessDeniedException {
        return new ConfigImpl();
    }

    @Override // ohos.global.config.ConfigManager
    public boolean getBoolean(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        SysProp checkConfigType = checkConfigType(configType);
        if (checkConfigType == null || checkConfigType.getKey() == null || checkConfigType.getDef() == null) {
            throw new NotExistException("This ConfigType is not supported yet.");
        }
        String lowerCase = checkConfigType.getDef().toLowerCase();
        if ("true".equals(lowerCase) || "false".equals(lowerCase)) {
            return SystemProperties.getBoolean(checkConfigType.getKey(), Boolean.valueOf(lowerCase).booleanValue()) ^ checkConfigType.getOpposite().booleanValue();
        }
        throw new WrongTypeException("This ConfigType is not a boolean.");
    }

    @Override // ohos.global.config.ConfigManager
    public String getString(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        SysProp checkConfigType = checkConfigType(configType);
        if (checkConfigType == null || checkConfigType.getKey() == null) {
            throw new NotExistException("This ConfigType is not supported yet.");
        }
        String[] split = checkConfigType.getKey().split(";");
        String str = null;
        int i = 0;
        if (checkConfigType.getDef() == null || "".equals(checkConfigType.getDef().replaceAll(";", ""))) {
            while (i < split.length) {
                str = SystemProperties.get(split[i]);
                if (!"".equals(str)) {
                    break;
                }
                i++;
            }
        } else {
            String[] split2 = checkConfigType.getDef().split(";");
            if (split.length == split2.length) {
                while (i < split.length) {
                    str = SystemProperties.get(split[i], split2[i]);
                    if (!"".equals(str)) {
                        break;
                    }
                    i++;
                }
            } else {
                throw new NotExistException("This ConfigType is not supported yet.");
            }
        }
        return str;
    }

    @Override // ohos.global.config.ConfigManager
    public List<String> getStringList(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        return new ArrayList();
    }

    @Override // ohos.global.config.ConfigManager
    public List<Integer> getIntegerList(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        return new ArrayList();
    }

    @Override // ohos.global.config.ConfigManager
    public Map<String, String> getStringMap(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        return new HashMap();
    }

    @Override // ohos.global.config.ConfigManager
    public Map<String, Integer> getIntegerMap(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        return new HashMap();
    }

    private SysProp checkConfigType(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        if (configType != null) {
            try {
                return (SysProp) SysProp.valueOf(SysProp.class, configType.name());
            } catch (IllegalArgumentException unused) {
                throw new NotExistException("This ConfigType is not supported yet.");
            }
        } else {
            throw new WrongTypeException("This ConfigType is null.");
        }
    }
}

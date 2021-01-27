package ohos.global.config;

import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.resource.AccessDeniedException;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLogLabel;
import ohos.system.Parameters;

public class ConfigManagerImpl extends ConfigManager {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ConfigManagerImpl");

    @Override // ohos.global.config.ConfigManager
    public boolean getBoolean(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        SysProp checkConfigType = checkConfigType(configType);
        if (checkConfigType == null || checkConfigType.getKey() == null || checkConfigType.getDef() == null) {
            throw new NotExistException("This ConfigType is not supported yet.");
        }
        String lowerCase = checkConfigType.getDef().toLowerCase();
        if ("true".equals(lowerCase) || "false".equals(lowerCase)) {
            return Parameters.getBoolean(checkConfigType.getKey(), Boolean.valueOf(lowerCase).booleanValue()) ^ checkConfigType.getOpposite().booleanValue();
        }
        throw new WrongTypeException("This ConfigType is not a boolean.");
    }

    @Override // ohos.global.config.ConfigManager
    public String getString(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException {
        SysProp checkConfigType = checkConfigType(configType);
        if (checkConfigType == null || checkConfigType.getKey() == null) {
            throw new NotExistException("This ConfigType is not supported yet.");
        }
        String[] split = checkConfigType.getKey().split(DMSDPConfig.LIST_TO_STRING_SPLIT);
        String str = null;
        int i = 0;
        if (checkConfigType.getDef() == null || "".equals(checkConfigType.getDef().replaceAll(DMSDPConfig.LIST_TO_STRING_SPLIT, ""))) {
            while (i < split.length) {
                str = Parameters.get(split[i]);
                if (!"".equals(str)) {
                    break;
                }
                i++;
            }
        } else {
            String[] split2 = checkConfigType.getDef().split(DMSDPConfig.LIST_TO_STRING_SPLIT);
            if (split.length == split2.length) {
                while (i < split.length) {
                    str = Parameters.get(split[i], split2[i]);
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

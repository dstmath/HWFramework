package ohos.global.config;

import ohos.global.resource.AccessDeniedException;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

public abstract class ConfigManager {
    public abstract boolean getBoolean(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;

    public abstract String getString(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;
}

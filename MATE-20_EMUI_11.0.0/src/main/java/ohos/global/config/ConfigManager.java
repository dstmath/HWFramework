package ohos.global.config;

import java.util.List;
import java.util.Map;
import ohos.global.resource.AccessDeniedException;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

public abstract class ConfigManager {
    public abstract boolean getBoolean(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;

    public abstract Config getConfig(ConfigType configType) throws NotExistException, AccessDeniedException;

    public abstract List<Integer> getIntegerList(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;

    public abstract Map<String, Integer> getIntegerMap(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;

    public abstract String getString(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;

    public abstract List<String> getStringList(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;

    public abstract Map<String, String> getStringMap(ConfigType configType) throws WrongTypeException, NotExistException, AccessDeniedException;
}

package ohos.global.resource;

import java.io.IOException;
import java.util.List;
import ohos.global.config.ConfigManager;
import ohos.global.configuration.Configuration;
import ohos.global.configuration.DeviceCapability;
import ohos.global.resource.solidxml.Pattern;
import ohos.global.resource.solidxml.SolidXml;
import ohos.global.resource.solidxml.Theme;
import ohos.global.resource.solidxml.TypedAttribute;

public abstract class ResourceManager {
    public abstract Pattern createPattern(List<TypedAttribute.AttrData> list);

    public abstract Theme createTheme(List<TypedAttribute.AttrData> list);

    public abstract ConfigManager getConfigManager();

    public abstract Configuration getConfiguration();

    public abstract DeviceCapability getDeviceCapability();

    public abstract Element getElement(int i) throws NotExistException, IOException, WrongTypeException;

    public abstract String getIdentifier(int i) throws NotExistException, IOException;

    public abstract String getMediaPath(int i) throws NotExistException, IOException, WrongTypeException;

    public abstract RawFileEntry getRawFileEntry(String str);

    public abstract Resource getResource(int i) throws NotExistException, IOException;

    public abstract SolidXml getSolidXml(int i) throws NotExistException, IOException, WrongTypeException;

    @Deprecated
    public abstract Theme getTheme(int i) throws NotExistException, IOException, WrongTypeException;

    public abstract void updateConfiguration(Configuration configuration, DeviceCapability deviceCapability);
}

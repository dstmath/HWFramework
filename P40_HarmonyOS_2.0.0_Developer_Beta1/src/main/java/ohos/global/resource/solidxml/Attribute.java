package ohos.global.resource.solidxml;

import ohos.global.resource.ResourceManager;

public abstract class Attribute {
    public abstract String getName();

    public abstract String getStringValue();

    public abstract TypedAttribute getTypedAttribute(ResourceManager resourceManager);
}

package ohos.global.resource.solidxml;

import java.util.List;
import ohos.global.resource.ResourceManager;

public abstract class Node {
    public abstract List<Attribute> getAttributes();

    public abstract Node getChild();

    public abstract String getName();

    public abstract Node getSibling();

    public abstract String getStringValue();

    public abstract List<TypedAttribute> getTypedAttribute(ResourceManager resourceManager);
}

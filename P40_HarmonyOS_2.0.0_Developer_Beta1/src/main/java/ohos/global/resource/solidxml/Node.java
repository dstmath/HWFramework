package ohos.global.resource.solidxml;

import java.util.List;
import ohos.global.resource.ResourceManager;

public abstract class Node {
    public abstract List<Attribute> getAttributes();

    public abstract Node getChild();

    public abstract String getName();

    public abstract Node getSibling();

    public abstract String getStringValue();

    @Deprecated
    public abstract List<TypedAttribute> getTypedAttribute(ResourceManager resourceManager);

    public abstract List<TypedAttribute> getTypedAttributes(ResourceManager resourceManager);

    public abstract List<TypedAttribute> getTypedAttributes(Pattern pattern);

    public abstract List<TypedAttribute> getTypedAttributes(Pattern pattern, String[] strArr);
}

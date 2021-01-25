package ohos.global.resource.solidxml;

import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLogLabel;

public class AttributeImpl extends Attribute {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "AttributeImpl");
    private String name;
    private String value;

    public AttributeImpl(String str, String str2) {
        this.name = str;
        this.value = str2;
    }

    @Override // ohos.global.resource.solidxml.Attribute
    public String getName() {
        return this.name;
    }

    @Override // ohos.global.resource.solidxml.Attribute
    public String getStringValue() {
        return this.value;
    }

    @Override // ohos.global.resource.solidxml.Attribute
    public TypedAttribute getTypedAttribute(ResourceManager resourceManager) {
        return new TypedAttributeImpl(resourceManager, this.name, this.value);
    }
}

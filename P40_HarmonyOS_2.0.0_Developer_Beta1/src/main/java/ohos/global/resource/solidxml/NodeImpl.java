package ohos.global.resource.solidxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class NodeImpl extends Node {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "Nodelmpl");
    int attrCount;
    int attrIndex;
    List<TypedAttribute> attrs;
    int brother;
    int child;
    String name = "";
    String value = "";
    SolidXmllmpl xml;

    public NodeImpl(SolidXmllmpl solidXmllmpl) {
        this.xml = solidXmllmpl;
    }

    @Override // ohos.global.resource.solidxml.Node
    public String getName() {
        return this.name;
    }

    @Override // ohos.global.resource.solidxml.Node
    public String getStringValue() {
        return this.value;
    }

    @Override // ohos.global.resource.solidxml.Node
    public NodeImpl getChild() {
        SolidXmllmpl solidXmllmpl;
        if (this.child == -1 || (solidXmllmpl = this.xml) == null || solidXmllmpl.nodeList == null) {
            return null;
        }
        try {
            return this.xml.nodeList.get(this.child);
        } catch (IndexOutOfBoundsException unused) {
            HiLog.error(LABEL, "getSibling index not valid", new Object[0]);
            return null;
        }
    }

    @Override // ohos.global.resource.solidxml.Node
    public NodeImpl getSibling() {
        SolidXmllmpl solidXmllmpl;
        if (this.brother == -1 || (solidXmllmpl = this.xml) == null || solidXmllmpl.nodeList == null) {
            return null;
        }
        try {
            return this.xml.nodeList.get(this.brother);
        } catch (IndexOutOfBoundsException unused) {
            HiLog.error(LABEL, "getSibling index not valid", new Object[0]);
            return null;
        }
    }

    @Override // ohos.global.resource.solidxml.Node
    public List<Attribute> getAttributes() {
        List<TypedAttribute> list = this.attrs;
        if (list == null || list.size() == 0) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList(this.attrs.size());
        for (TypedAttribute typedAttribute : this.attrs) {
            arrayList.add(new AttributeImpl(typedAttribute.getName(), typedAttribute.getOriginalValue()));
        }
        return arrayList;
    }

    @Override // ohos.global.resource.solidxml.Node
    @Deprecated
    public List<TypedAttribute> getTypedAttribute(ResourceManager resourceManager) {
        return this.attrs;
    }

    @Override // ohos.global.resource.solidxml.Node
    public List<TypedAttribute> getTypedAttributes(ResourceManager resourceManager) {
        return this.attrs;
    }

    @Override // ohos.global.resource.solidxml.Node
    public List<TypedAttribute> getTypedAttributes(Pattern pattern) {
        if (pattern == null) {
            return this.attrs;
        }
        return new ArrayList(combinedWithPattern(pattern).values());
    }

    @Override // ohos.global.resource.solidxml.Node
    public List<TypedAttribute> getTypedAttributes(Pattern pattern, String[] strArr) {
        HashMap<String, TypedAttribute> hashMap;
        if (strArr == null || strArr.length == 0) {
            return new ArrayList();
        }
        if (pattern == null) {
            hashMap = combinedWithPattern(new PatternImpl(null));
        } else {
            hashMap = combinedWithPattern(pattern);
        }
        ArrayList arrayList = new ArrayList(strArr.length);
        for (String str : strArr) {
            TypedAttribute typedAttribute = hashMap.get(str);
            if (typedAttribute != null) {
                arrayList.add(typedAttribute);
            }
        }
        return arrayList;
    }

    private HashMap<String, TypedAttribute> combinedWithPattern(Pattern pattern) {
        HashMap<String, TypedAttribute> patternHash = pattern.getPatternHash();
        HashMap<String, TypedAttribute> hashMap = new HashMap<>(this.attrs.size() + patternHash.size());
        hashMap.putAll(patternHash);
        hashMap.putAll(convertTypedAttributeToHash(this.attrs));
        return hashMap;
    }

    private HashMap<String, TypedAttribute> convertTypedAttributeToHash(List<TypedAttribute> list) {
        HashMap<String, TypedAttribute> hashMap = new HashMap<>(list.size());
        for (TypedAttribute typedAttribute : list) {
            hashMap.put(typedAttribute.getName(), typedAttribute);
        }
        return hashMap;
    }
}

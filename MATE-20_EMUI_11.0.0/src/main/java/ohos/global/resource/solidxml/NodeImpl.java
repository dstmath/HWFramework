package ohos.global.resource.solidxml;

import java.util.ArrayList;
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
    public List<TypedAttribute> getTypedAttribute(ResourceManager resourceManager) {
        return this.attrs;
    }
}

package com.android.server.wifi.hotspot2.omadm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class OMAScalar extends OMANode {
    private final String mValue;

    public OMAScalar(OMAConstructed parent, String name, String context, String value, String... avps) {
        this(parent, name, context, value, OMANode.buildAttributes(avps));
    }

    public OMAScalar(OMAConstructed parent, String name, String context, String value, Map<String, String> avps) {
        super(parent, name, context, avps);
        this.mValue = value;
    }

    public OMAScalar reparent(OMAConstructed parent) {
        return new OMAScalar(parent, getName(), getContext(), this.mValue, getAttributes());
    }

    public String getScalarValue(Iterator<String> it) throws OMAException {
        return this.mValue;
    }

    public OMANode getListValue(Iterator<String> it) throws OMAException {
        throw new OMAException("Scalar encountered in list path: " + getPathString());
    }

    public boolean isLeaf() {
        return true;
    }

    public Collection<OMANode> getChildren() {
        throw new UnsupportedOperationException();
    }

    public String getValue() {
        return this.mValue;
    }

    public OMANode getChild(String name) throws OMAException {
        throw new OMAException("'" + getName() + "' is a scalar node");
    }

    public OMANode addChild(String name, String context, String value, String path) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void toString(StringBuilder sb, int level) {
        sb.append(getPathString()).append('=').append(this.mValue);
        if (getContext() != null) {
            sb.append(" (").append(getContext()).append(')');
        }
        sb.append('\n');
    }

    public void marshal(OutputStream out, int level) throws IOException {
        OMAConstants.indent(level, out);
        OMAConstants.serializeString(getName(), out);
        out.write(61);
        OMAConstants.serializeString(getValue(), out);
        out.write(10);
    }

    public void fillPayload(StringBuilder sb) {
        sb.append('<').append(MOTree.ValueTag).append('>');
        sb.append(OMANode.escape(this.mValue));
        sb.append("</").append(MOTree.ValueTag).append(">\n");
    }
}

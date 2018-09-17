package com.android.server.wifi.hotspot2.omadm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class OMAConstructed extends OMANode {
    private final MultiValueMap<OMANode> mChildren;

    public OMAConstructed(OMAConstructed parent, String name, String context, String... avps) {
        this(parent, name, context, new MultiValueMap(), OMANode.buildAttributes(avps));
    }

    protected OMAConstructed(OMAConstructed parent, String name, String context, MultiValueMap<OMANode> children, Map<String, String> avps) {
        super(parent, name, context, avps);
        this.mChildren = children;
    }

    public OMANode addChild(String name, String context, String value, String pathString) throws IOException {
        int i = 0;
        if (pathString == null) {
            OMANode child;
            if (value != null) {
                child = new OMAScalar(this, name, context, value, new String[0]);
            } else {
                child = new OMAConstructed(this, name, context, new String[0]);
            }
            this.mChildren.put(name, child);
            return child;
        }
        OMANode target = this;
        while (target.getParent() != null) {
            target = target.getParent();
        }
        String[] split = pathString.split("/");
        int length = split.length;
        while (i < length) {
            String element = split[i];
            target = target.getChild(element);
            if (target == null) {
                throw new IOException("No child node '" + element + "' in " + getPathString());
            } else if (target.isLeaf()) {
                throw new IOException("Cannot add child to leaf node: " + getPathString());
            } else {
                i++;
            }
        }
        return target.addChild(name, context, value, null);
    }

    public OMAConstructed reparent(OMAConstructed parent) {
        return new OMAConstructed(parent, getName(), getContext(), this.mChildren, getAttributes());
    }

    public void addChild(OMANode child) {
        this.mChildren.put(child.getName(), child.reparent(this));
    }

    public String getScalarValue(Iterator<String> path) throws OMAException {
        if (path.hasNext()) {
            OMANode child = (OMANode) this.mChildren.get((String) path.next());
            if (child != null) {
                return child.getScalarValue(path);
            }
            return null;
        }
        throw new OMAException("Path too short for " + getPathString());
    }

    public OMANode getListValue(Iterator<String> path) throws OMAException {
        if (!path.hasNext()) {
            return null;
        }
        OMANode child;
        String tag = (String) path.next();
        if (tag.equals("?")) {
            child = (OMANode) this.mChildren.getSingletonValue();
        } else {
            child = (OMANode) this.mChildren.get(tag);
        }
        if (child == null) {
            return null;
        }
        if (path.hasNext()) {
            return child.getListValue(path);
        }
        return child;
    }

    public boolean isLeaf() {
        return false;
    }

    public Collection<OMANode> getChildren() {
        return Collections.unmodifiableCollection(this.mChildren.values());
    }

    public OMANode getChild(String name) {
        return (OMANode) this.mChildren.get(name);
    }

    public OMANode replaceNode(OMANode oldNode, OMANode newNode) {
        return (OMANode) this.mChildren.replace(oldNode.getName(), oldNode, newNode);
    }

    public OMANode removeNode(String key, OMANode node) {
        if (key.equals("?")) {
            return (OMANode) this.mChildren.remove(node);
        }
        return (OMANode) this.mChildren.remove(key, node);
    }

    public String getValue() {
        throw new UnsupportedOperationException();
    }

    public void toString(StringBuilder sb, int level) {
        sb.append(getPathString());
        if (getContext() != null) {
            sb.append(" (").append(getContext()).append(')');
        }
        sb.append('\n');
        for (OMANode node : this.mChildren.values()) {
            node.toString(sb, level + 1);
        }
    }

    public void marshal(OutputStream out, int level) throws IOException {
        OMAConstants.indent(level, out);
        OMAConstants.serializeString(getName(), out);
        if (getContext() != null) {
            out.write(String.format("(%s)", new Object[]{getContext()}).getBytes(StandardCharsets.UTF_8));
        }
        out.write(new byte[]{(byte) 43, (byte) 10});
        for (OMANode child : this.mChildren.values()) {
            child.marshal(out, level + 1);
        }
        OMAConstants.indent(level, out);
        out.write(".\n".getBytes(StandardCharsets.UTF_8));
    }

    public void fillPayload(StringBuilder sb) {
        if (getContext() != null) {
            sb.append('<').append(MOTree.RTPropTag).append(">\n");
            sb.append('<').append(MOTree.TypeTag).append(">\n");
            sb.append('<').append(MOTree.DDFNameTag).append(">");
            sb.append(getContext());
            sb.append("</").append(MOTree.DDFNameTag).append(">\n");
            sb.append("</").append(MOTree.TypeTag).append(">\n");
            sb.append("</").append(MOTree.RTPropTag).append(">\n");
        }
        for (OMANode child : getChildren()) {
            child.toXml(sb);
        }
    }
}

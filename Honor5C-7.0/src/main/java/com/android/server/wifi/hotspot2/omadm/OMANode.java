package com.android.server.wifi.hotspot2.omadm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class OMANode {
    private static final Map<Character, String> sEscapes = null;
    private final Map<String, String> mAttributes;
    private final String mContext;
    private final String mName;
    private final OMAConstructed mParent;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.omadm.OMANode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.omadm.OMANode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.omadm.OMANode.<clinit>():void");
    }

    public abstract OMANode addChild(String str, String str2, String str3, String str4) throws IOException;

    public abstract void fillPayload(StringBuilder stringBuilder);

    public abstract OMANode getChild(String str) throws OMAException;

    public abstract Collection<OMANode> getChildren();

    public abstract OMANode getListValue(Iterator<String> it) throws OMAException;

    public abstract String getScalarValue(Iterator<String> it) throws OMAException;

    public abstract String getValue();

    public abstract boolean isLeaf();

    public abstract void marshal(OutputStream outputStream, int i) throws IOException;

    public abstract OMANode reparent(OMAConstructed oMAConstructed);

    public abstract void toString(StringBuilder stringBuilder, int i);

    protected OMANode(OMAConstructed parent, String name, String context, Map<String, String> avps) {
        this.mParent = parent;
        this.mName = name;
        this.mContext = context;
        this.mAttributes = avps;
    }

    protected static Map<String, String> buildAttributes(String[] avps) {
        if (avps == null) {
            return null;
        }
        Map<String, String> attributes = new HashMap();
        for (int n = 0; n < avps.length; n += 2) {
            attributes.put(avps[n], avps[n + 1]);
        }
        return attributes;
    }

    protected Map<String, String> getAttributes() {
        return this.mAttributes;
    }

    public OMAConstructed getParent() {
        return this.mParent;
    }

    public String getName() {
        return this.mName;
    }

    public String getContext() {
        return this.mContext;
    }

    public List<String> getPath() {
        LinkedList<String> path = new LinkedList();
        for (OMANode node = this; node != null; node = node.getParent()) {
            path.addFirst(node.getName());
        }
        return path;
    }

    public String getPathString() {
        StringBuilder sb = new StringBuilder();
        for (String element : getPath()) {
            sb.append('/').append(element);
        }
        return sb.toString();
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int n = 0; n < s.length(); n++) {
            char ch = s.charAt(n);
            String escape = (String) sEscapes.get(Character.valueOf(ch));
            if (escape != null) {
                sb.append(escape);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public void toXml(StringBuilder sb) {
        sb.append('<').append(MOTree.NodeTag);
        if (!(this.mAttributes == null || this.mAttributes.isEmpty())) {
            for (Entry<String, String> avp : this.mAttributes.entrySet()) {
                sb.append(' ').append((String) avp.getKey()).append("=\"").append(escape((String) avp.getValue())).append('\"');
            }
        }
        sb.append(">\n");
        sb.append('<').append(MOTree.NodeNameTag).append('>');
        sb.append(getName());
        sb.append("</").append(MOTree.NodeNameTag).append(">\n");
        fillPayload(sb);
        sb.append("</").append(MOTree.NodeTag).append(">\n");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    public static OMAConstructed unmarshal(InputStream in) throws IOException {
        OMANode node = buildNode(in, null);
        if (node == null || node.isLeaf()) {
            throw new IOException("Bad OMA tree");
        }
        unmarshal(in, (OMAConstructed) node);
        return (OMAConstructed) node;
    }

    private static void unmarshal(InputStream in, OMAConstructed parent) throws IOException {
        while (true) {
            OMANode node = buildNode(in, parent);
            if (node != null) {
                if (!node.isLeaf()) {
                    unmarshal(in, (OMAConstructed) node);
                }
            } else {
                return;
            }
        }
    }

    private static OMANode buildNode(InputStream in, OMAConstructed parent) throws IOException {
        String name = OMAConstants.deserializeString(in);
        if (name == null) {
            return null;
        }
        String str = null;
        int next = in.read();
        if (next == 40) {
            str = OMAConstants.readURN(in);
            next = in.read();
        }
        if (next == 61) {
            return parent.addChild(name, str, OMAConstants.deserializeString(in), null);
        }
        if (next != 43) {
            throw new IOException("Parse error: expected = or + after node name");
        } else if (parent != null) {
            return parent.addChild(name, str, null, null);
        } else {
            return new OMAConstructed(null, name, str, new String[0]);
        }
    }
}

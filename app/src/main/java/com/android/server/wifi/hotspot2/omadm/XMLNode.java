package com.android.server.wifi.hotspot2.omadm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class XMLNode {
    private static final String CDATA_CLOSE = "]]>";
    private static final String CDATA_OPEN = "<![CDATA[";
    private static final Set<Character> XML_SPECIAL = null;
    private static final String XML_SPECIAL_CHARS = "\"'<>&";
    private final Map<String, NodeAttribute> mAttributes;
    private final List<XMLNode> mChildren;
    private MOTree mMO;
    private final XMLNode mParent;
    private final String mTag;
    private String mText;
    private StringBuilder mTextBuilder;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.omadm.XMLNode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.omadm.XMLNode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.omadm.XMLNode.<clinit>():void");
    }

    public XMLNode(XMLNode parent, String tag, Attributes attributes) throws SAXException {
        this.mTag = tag;
        this.mAttributes = new HashMap();
        if (attributes.getLength() > 0) {
            for (int n = 0; n < attributes.getLength(); n++) {
                this.mAttributes.put(attributes.getQName(n), new NodeAttribute(attributes.getQName(n), attributes.getType(n), attributes.getValue(n)));
            }
        }
        this.mParent = parent;
        this.mChildren = new ArrayList();
        this.mTextBuilder = new StringBuilder();
    }

    public XMLNode(XMLNode parent, String tag, Map<String, String> attributes) {
        this.mTag = tag;
        this.mAttributes = new HashMap(attributes == null ? 0 : attributes.size());
        if (attributes != null) {
            for (Entry<String, String> entry : attributes.entrySet()) {
                this.mAttributes.put((String) entry.getKey(), new NodeAttribute((String) entry.getKey(), "", (String) entry.getValue()));
            }
        }
        this.mParent = parent;
        this.mChildren = new ArrayList();
        this.mTextBuilder = new StringBuilder();
    }

    public boolean equals(Object thatObject) {
        if (thatObject == this) {
            return true;
        }
        if (thatObject.getClass() != XMLNode.class) {
            return false;
        }
        XMLNode that = (XMLNode) thatObject;
        if (!getTag().equals(that.getTag()) || this.mAttributes.size() != that.mAttributes.size() || this.mChildren.size() != that.mChildren.size()) {
            return false;
        }
        for (Entry<String, NodeAttribute> entry : this.mAttributes.entrySet()) {
            if (!((NodeAttribute) entry.getValue()).equals(that.mAttributes.get(entry.getKey()))) {
                return false;
            }
        }
        List<XMLNode> cloneOfThat = new ArrayList(that.mChildren);
        for (XMLNode child : this.mChildren) {
            Iterator<XMLNode> thatChildren = cloneOfThat.iterator();
            boolean found = false;
            while (thatChildren.hasNext()) {
                if (child.equals((XMLNode) thatChildren.next())) {
                    found = true;
                    thatChildren.remove();
                    break;
                    continue;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public void setText(String text) {
        this.mText = text;
        this.mTextBuilder = null;
    }

    public void addText(char[] chs, int start, int length) {
        String s = new String(chs, start, length);
        String trimmed = s.trim();
        if (!trimmed.isEmpty()) {
            if (s.charAt(0) != trimmed.charAt(0)) {
                this.mTextBuilder.append(' ');
            }
            this.mTextBuilder.append(trimmed);
            if (s.charAt(s.length() - 1) != trimmed.charAt(trimmed.length() - 1)) {
                this.mTextBuilder.append(' ');
            }
        }
    }

    public void addChild(XMLNode child) {
        this.mChildren.add(child);
    }

    public void close() throws IOException, SAXException {
        String text = this.mTextBuilder.toString().trim();
        StringBuilder filtered = new StringBuilder(text.length());
        for (int n = 0; n < text.length(); n++) {
            char ch = text.charAt(n);
            if (ch >= ' ') {
                filtered.append(ch);
            }
        }
        this.mText = filtered.toString();
        this.mTextBuilder = null;
        if (MOTree.hasMgmtTreeTag(this.mText)) {
            try {
                NodeAttribute urn = (NodeAttribute) this.mAttributes.get(OMAConstants.SppMOAttribute);
                this.mMO = new OMAParser().parse(this.mText, urn != null ? urn.getValue() : null);
            } catch (SAXException e) {
                this.mMO = null;
            }
        }
    }

    public String getTag() {
        return this.mTag;
    }

    public String getNameSpace() throws OMAException {
        String[] nsn = this.mTag.split(":");
        if (nsn.length == 2) {
            return nsn[0];
        }
        throw new OMAException("Non-namespaced tag: '" + this.mTag + "'");
    }

    public String getStrippedTag() throws OMAException {
        String[] nsn = this.mTag.split(":");
        if (nsn.length == 2) {
            return nsn[1].toLowerCase();
        }
        throw new OMAException("Non-namespaced tag: '" + this.mTag + "'");
    }

    public XMLNode getSoleChild() throws OMAException {
        if (this.mChildren.size() == 1) {
            return (XMLNode) this.mChildren.get(0);
        }
        throw new OMAException("Expected exactly one child to " + this.mTag);
    }

    public XMLNode getParent() {
        return this.mParent;
    }

    public String getText() {
        return this.mText;
    }

    public Map<String, NodeAttribute> getAttributes() {
        return Collections.unmodifiableMap(this.mAttributes);
    }

    public Map<String, String> getTextualAttributes() {
        Map<String, String> map = new HashMap(this.mAttributes.size());
        for (Entry<String, NodeAttribute> entry : this.mAttributes.entrySet()) {
            map.put((String) entry.getKey(), ((NodeAttribute) entry.getValue()).getValue());
        }
        return map;
    }

    public String getAttributeValue(String name) {
        NodeAttribute nodeAttribute = (NodeAttribute) this.mAttributes.get(name);
        if (nodeAttribute != null) {
            return nodeAttribute.getValue();
        }
        return null;
    }

    public List<XMLNode> getChildren() {
        return this.mChildren;
    }

    public MOTree getMOTree() {
        return this.mMO;
    }

    private void toString(char[] indent, StringBuilder sb) {
        Arrays.fill(indent, ' ');
        sb.append(indent).append('<').append(this.mTag);
        for (Entry<String, NodeAttribute> entry : this.mAttributes.entrySet()) {
            sb.append(' ').append((String) entry.getKey()).append("='").append(((NodeAttribute) entry.getValue()).getValue()).append('\'');
        }
        if (this.mText != null && !this.mText.isEmpty()) {
            sb.append('>').append(escapeCdata(this.mText)).append("</").append(this.mTag).append(">\n");
        } else if (this.mChildren.isEmpty()) {
            sb.append("/>\n");
        } else {
            sb.append(">\n");
            char[] subIndent = Arrays.copyOf(indent, indent.length + 2);
            for (XMLNode child : this.mChildren) {
                child.toString(subIndent, sb);
            }
            sb.append(indent).append("</").append(this.mTag).append(">\n");
        }
    }

    private static String escapeCdata(String text) {
        if (!escapable(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(CDATA_OPEN);
        int start = 0;
        while (true) {
            int etoken = text.indexOf(CDATA_CLOSE);
            if (etoken < 0) {
                break;
            }
            sb.append(text.substring(start, etoken + 2)).append(CDATA_CLOSE).append(CDATA_OPEN);
            start = etoken + 2;
        }
        if (start < text.length() - 1) {
            sb.append(text.substring(start));
        }
        sb.append(CDATA_CLOSE);
        return sb.toString();
    }

    private static boolean escapable(String s) {
        for (int n = 0; n < s.length(); n++) {
            if (XML_SPECIAL.contains(Character.valueOf(s.charAt(n)))) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(new char[0], sb);
        return sb.toString();
    }
}

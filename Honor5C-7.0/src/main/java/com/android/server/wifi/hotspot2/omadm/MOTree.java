package com.android.server.wifi.hotspot2.omadm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xml.sax.SAXException;

public class MOTree {
    public static final String DDFNameTag = "DDFName";
    public static final String MgmtTreeTag = "MgmtTree";
    public static final String NodeNameTag = "NodeName";
    public static final String NodeTag = "Node";
    public static final String PathTag = "Path";
    public static final String RTPropTag = "RTProperties";
    public static final String TypeTag = "Type";
    public static final String ValueTag = "Value";
    private final String mDtdRev;
    private final OMAConstructed mRoot;
    private final String mUrn;

    private static class NodeData {
        private final String mName;
        private String mPath;
        private String mValue;

        private NodeData(String name) {
            this.mName = name;
        }

        private void setPath(String path) {
            this.mPath = path;
        }

        private void setValue(String value) {
            this.mValue = value;
        }

        public String getName() {
            return this.mName;
        }

        public String getPath() {
            return this.mPath;
        }

        public String getValue() {
            return this.mValue;
        }
    }

    public MOTree(XMLNode node, String urn) throws IOException, SAXException {
        Iterator<XMLNode> children = node.getChildren().iterator();
        String str = null;
        while (children.hasNext()) {
            XMLNode child = (XMLNode) children.next();
            if (child.getTag().equals(OMAConstants.SyncMLVersionTag)) {
                str = child.getText();
                children.remove();
                break;
            }
        }
        this.mUrn = urn;
        this.mDtdRev = str;
        this.mRoot = new ManagementTreeRoot(node, str);
        for (XMLNode child2 : node.getChildren()) {
            buildNode(this.mRoot, child2);
        }
    }

    public MOTree(String urn, String rev, OMAConstructed root) {
        this.mUrn = urn;
        this.mDtdRev = rev;
        this.mRoot = root;
    }

    public static MOTree buildMgmtTree(String urn, String rev, OMAConstructed root) {
        if (!urn.equals(OMAConstants.PPS_URN) && !urn.equals(OMAConstants.DevInfoURN) && !urn.equals(OMAConstants.DevDetailURN) && !urn.equals(OMAConstants.DevDetailXURN)) {
            return new MOTree(urn, rev, root);
        }
        OMAConstructed realRoot = new ManagementTreeRoot(OMAConstants.OMAVersion);
        realRoot.addChild(root);
        return new MOTree(urn, rev, realRoot);
    }

    public static boolean hasMgmtTreeTag(String text) {
        for (int n = 0; n < text.length(); n++) {
            if (text.charAt(n) > ' ') {
                return text.regionMatches(true, n, "<MgmtTree>", 0, MgmtTreeTag.length() + 2);
            }
        }
        return false;
    }

    private static void buildNode(OMANode parent, XMLNode node) throws IOException {
        if (node.getTag().equals(NodeTag)) {
            Map<String, XMLNode> checkMap = new HashMap(3);
            String context = null;
            List<NodeData> values = new ArrayList();
            List<XMLNode> children = new ArrayList();
            NodeData curValue = null;
            for (XMLNode child : node.getChildren()) {
                XMLNode old = (XMLNode) checkMap.put(child.getTag(), child);
                String tag = child.getTag();
                if (tag.equals(NodeNameTag)) {
                    if (curValue != null) {
                        throw new IOException("NodeName not expected");
                    }
                    curValue = new NodeData(null);
                } else if (tag.equals(PathTag)) {
                    if (curValue == null || curValue.getPath() != null) {
                        throw new IOException("Path not expected");
                    }
                    curValue.setPath(child.getText());
                } else if (tag.equals(ValueTag)) {
                    if (!children.isEmpty()) {
                        throw new IOException("Value in constructed node");
                    } else if (curValue == null || curValue.getValue() != null) {
                        throw new IOException("Value not expected");
                    } else {
                        curValue.setValue(child.getText());
                        values.add(curValue);
                        curValue = null;
                    }
                } else if (tag.equals(RTPropTag)) {
                    if (old != null) {
                        throw new IOException("Duplicate RTProperties");
                    }
                    String str = TypeTag;
                    context = getNextNode(getNextNode(child, tag), DDFNameTag).getText();
                    if (context == null) {
                        throw new IOException("No text in DDFName");
                    }
                } else if (!tag.equals(NodeTag)) {
                    continue;
                } else if (values.isEmpty()) {
                    children.add(child);
                } else {
                    throw new IOException("Scalar node " + node.getText() + " has Node child");
                }
            }
            if (values.isEmpty()) {
                if (curValue == null) {
                    throw new IOException("Missing name");
                }
                OMANode subNode = parent.addChild(curValue.getName(), context, null, curValue.getPath());
                for (XMLNode child2 : children) {
                    buildNode(subNode, child2);
                }
                return;
            } else if (children.isEmpty()) {
                for (NodeData nodeData : values) {
                    parent.addChild(nodeData.getName(), context, nodeData.getValue(), nodeData.getPath());
                }
                return;
            } else {
                throw new IOException("Got both sub nodes and value(s)");
            }
        }
        throw new IOException("Node is a '" + node.getTag() + "' instead of a 'Node'");
    }

    private static XMLNode getNextNode(XMLNode node, String tag) throws IOException {
        if (node == null) {
            throw new IOException("No node for " + tag);
        } else if (node.getChildren().size() != 1) {
            throw new IOException("Expected " + node.getTag() + " to have exactly one child");
        } else {
            XMLNode child = (XMLNode) node.getChildren().iterator().next();
            if (child.getTag().equals(tag)) {
                return child;
            }
            throw new IOException("Expected " + node.getTag() + " to have child '" + tag + "' instead of '" + child.getTag() + "'");
        }
    }

    public String getUrn() {
        return this.mUrn;
    }

    public String getDtdRev() {
        return this.mDtdRev;
    }

    public OMAConstructed getRoot() {
        return this.mRoot;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MO Tree v").append(this.mDtdRev).append(", urn ").append(this.mUrn).append(")\n");
        sb.append(this.mRoot);
        return sb.toString();
    }

    public void marshal(OutputStream out) throws IOException {
        out.write("tree ".getBytes(StandardCharsets.UTF_8));
        OMAConstants.serializeString(this.mDtdRev, out);
        out.write(String.format("(%s)\n", new Object[]{this.mUrn}).getBytes(StandardCharsets.UTF_8));
        this.mRoot.marshal(out, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static MOTree unmarshal(InputStream in) throws IOException {
        boolean strip = true;
        StringBuilder tree = new StringBuilder();
        while (true) {
            int octet = in.read();
            if (octet < 0) {
                break;
            } else if (octet > 32) {
                tree.append((char) octet);
                strip = false;
            } else if (!strip) {
                break;
            }
        }
        if (tree.toString().equals("tree")) {
            String version = OMAConstants.deserializeString(in);
            if (in.read() == 40) {
                return new MOTree(OMAConstants.readURN(in), version, OMANode.unmarshal(in));
            }
            throw new IOException("Expected URN in tree definition");
        }
        throw new IOException("Not a tree: " + tree);
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder();
        this.mRoot.toXml(sb);
        return sb.toString();
    }
}

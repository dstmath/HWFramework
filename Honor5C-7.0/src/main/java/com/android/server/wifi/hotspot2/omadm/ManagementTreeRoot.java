package com.android.server.wifi.hotspot2.omadm;

import java.util.Map.Entry;

public class ManagementTreeRoot extends OMAConstructed {
    private final String mDtdRev;

    public ManagementTreeRoot(XMLNode node, String dtdRev) {
        super(null, MOTree.MgmtTreeTag, null, new MultiValueMap(), node.getTextualAttributes());
        this.mDtdRev = dtdRev;
    }

    public ManagementTreeRoot(String dtdRev) {
        super(null, MOTree.MgmtTreeTag, null, "xmlns", OMAConstants.SyncML);
        this.mDtdRev = dtdRev;
    }

    public void toXml(StringBuilder sb) {
        sb.append('<').append(MOTree.MgmtTreeTag);
        if (!(getAttributes() == null || getAttributes().isEmpty())) {
            for (Entry<String, String> avp : getAttributes().entrySet()) {
                sb.append(' ').append((String) avp.getKey()).append("=\"").append(OMANode.escape((String) avp.getValue())).append('\"');
            }
        }
        sb.append(">\n");
        sb.append('<').append(OMAConstants.SyncMLVersionTag).append('>').append(this.mDtdRev).append("</").append(OMAConstants.SyncMLVersionTag).append(">\n");
        for (OMANode child : getChildren()) {
            child.toXml(sb);
        }
        sb.append("</").append(MOTree.MgmtTreeTag).append(">\n");
    }
}

package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.ProcessingInstruction;

public final class ProcessingInstructionImpl extends LeafNodeImpl implements ProcessingInstruction {
    private String data;
    private String target;

    ProcessingInstructionImpl(DocumentImpl document, String target2, String data2) {
        super(document);
        this.target = target2;
        this.data = data2;
    }

    public String getData() {
        return this.data;
    }

    public String getNodeName() {
        return this.target;
    }

    public short getNodeType() {
        return 7;
    }

    public String getNodeValue() {
        return this.data;
    }

    public String getTarget() {
        return this.target;
    }

    public void setData(String data2) throws DOMException {
        this.data = data2;
    }
}

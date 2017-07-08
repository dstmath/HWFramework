package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.ProcessingInstruction;

public final class ProcessingInstructionImpl extends LeafNodeImpl implements ProcessingInstruction {
    private String data;
    private String target;

    ProcessingInstructionImpl(DocumentImpl document, String target, String data) {
        super(document);
        this.target = target;
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    public String getNodeName() {
        return this.target;
    }

    public short getNodeType() {
        return (short) 7;
    }

    public String getNodeValue() {
        return this.data;
    }

    public String getTarget() {
        return this.target;
    }

    public void setData(String data) throws DOMException {
        this.data = data;
    }
}

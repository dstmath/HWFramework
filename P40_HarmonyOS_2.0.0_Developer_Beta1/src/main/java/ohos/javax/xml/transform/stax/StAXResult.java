package ohos.javax.xml.transform.stax;

import ohos.javax.xml.stream.XMLEventWriter;
import ohos.javax.xml.stream.XMLStreamWriter;
import ohos.javax.xml.transform.Result;

public class StAXResult implements Result {
    public static final String FEATURE = "http://ohos.javax.xml.transform.stax.StAXResult/feature";
    private String systemId = null;
    private XMLEventWriter xmlEventWriter = null;
    private XMLStreamWriter xmlStreamWriter = null;

    @Override // ohos.javax.xml.transform.Result
    public String getSystemId() {
        return null;
    }

    public StAXResult(XMLEventWriter xMLEventWriter) {
        if (xMLEventWriter != null) {
            this.xmlEventWriter = xMLEventWriter;
            return;
        }
        throw new IllegalArgumentException("StAXResult(XMLEventWriter) with XMLEventWriter == null");
    }

    public StAXResult(XMLStreamWriter xMLStreamWriter) {
        if (xMLStreamWriter != null) {
            this.xmlStreamWriter = xMLStreamWriter;
            return;
        }
        throw new IllegalArgumentException("StAXResult(XMLStreamWriter) with XMLStreamWriter == null");
    }

    public XMLEventWriter getXMLEventWriter() {
        return this.xmlEventWriter;
    }

    public XMLStreamWriter getXMLStreamWriter() {
        return this.xmlStreamWriter;
    }

    @Override // ohos.javax.xml.transform.Result
    public void setSystemId(String str) {
        throw new UnsupportedOperationException("StAXResult#setSystemId(systemId) cannot set the system identifier for a StAXResult");
    }
}

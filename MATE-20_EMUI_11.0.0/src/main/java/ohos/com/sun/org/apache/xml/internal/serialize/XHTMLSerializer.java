package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.OutputStream;
import java.io.Writer;

public class XHTMLSerializer extends HTMLSerializer {
    public XHTMLSerializer() {
        super(true, new OutputFormat("xhtml", (String) null, false));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public XHTMLSerializer(OutputFormat outputFormat) {
        super(true, outputFormat == null ? new OutputFormat("xhtml", (String) null, false) : outputFormat);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public XHTMLSerializer(Writer writer, OutputFormat outputFormat) {
        super(true, outputFormat == null ? new OutputFormat("xhtml", (String) null, false) : outputFormat);
        setOutputCharStream(writer);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public XHTMLSerializer(OutputStream outputStream, OutputFormat outputFormat) {
        super(true, outputFormat == null ? new OutputFormat("xhtml", (String) null, false) : outputFormat);
        setOutputByteStream(outputStream);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.HTMLSerializer, ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer, ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public void setOutputFormat(OutputFormat outputFormat) {
        if (outputFormat == null) {
            outputFormat = new OutputFormat("xhtml", (String) null, false);
        }
        super.setOutputFormat(outputFormat);
    }
}

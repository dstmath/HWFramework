package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;

final class SerializerFactoryImpl extends SerializerFactory {
    private String _method;

    SerializerFactoryImpl(String str) {
        this._method = str;
        if (!this._method.equals("xml") && !this._method.equals("html") && !this._method.equals("xhtml") && !this._method.equals("text")) {
            throw new IllegalArgumentException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "MethodNotSupported", new Object[]{str}));
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.SerializerFactory
    public Serializer makeSerializer(OutputFormat outputFormat) {
        Serializer serializer = getSerializer(outputFormat);
        serializer.setOutputFormat(outputFormat);
        return serializer;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.SerializerFactory
    public Serializer makeSerializer(Writer writer, OutputFormat outputFormat) {
        Serializer serializer = getSerializer(outputFormat);
        serializer.setOutputCharStream(writer);
        return serializer;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.SerializerFactory
    public Serializer makeSerializer(OutputStream outputStream, OutputFormat outputFormat) throws UnsupportedEncodingException {
        Serializer serializer = getSerializer(outputFormat);
        serializer.setOutputByteStream(outputStream);
        return serializer;
    }

    private Serializer getSerializer(OutputFormat outputFormat) {
        if (this._method.equals("xml")) {
            return new XMLSerializer(outputFormat);
        }
        if (this._method.equals("html")) {
            return new HTMLSerializer(outputFormat);
        }
        if (this._method.equals("xhtml")) {
            return new XHTMLSerializer(outputFormat);
        }
        if (this._method.equals("text")) {
            return new TextSerializer();
        }
        throw new IllegalStateException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "MethodNotSupported", new Object[]{this._method}));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serialize.SerializerFactory
    public String getSupportedMethod() {
        return this._method;
    }
}

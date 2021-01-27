package com.huawei.android.internal.util;

import com.android.internal.util.FastXmlSerializer;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlSerializer;

public class FastXmlSerializerEx {
    private FastXmlSerializer mFastXmlSerializer = new FastXmlSerializer();

    public FastXmlSerializer getFastXmlSerializer() {
        return this.mFastXmlSerializer;
    }

    public void setFastXmlSerializer(FastXmlSerializer fastXmlSerializer) {
        this.mFastXmlSerializer = fastXmlSerializer;
    }

    public void setOutput(OutputStream os, String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mFastXmlSerializer.setOutput(os, encoding);
    }

    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mFastXmlSerializer.startDocument(encoding, standalone);
    }

    public XmlSerializer startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.mFastXmlSerializer.startTag(namespace, name);
    }

    public XmlSerializer attribute(String namespace, String name, String value) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.mFastXmlSerializer.attribute(namespace, name, value);
    }

    public XmlSerializer endTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.mFastXmlSerializer.endTag(namespace, name);
    }

    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        this.mFastXmlSerializer.endDocument();
    }
}

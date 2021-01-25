package org.ksoap2.serialization;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.kobjects.base64.Base64;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class MarshalBase64 implements Marshal {
    public static Class BYTE_ARRAY_CLASS = new byte[0].getClass();

    @Override // org.ksoap2.serialization.Marshal
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        return Base64.decode(parser.nextText());
    }

    @Override // org.ksoap2.serialization.Marshal
    public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
        writer.text(Base64.encode((byte[]) obj));
    }

    @Override // org.ksoap2.serialization.Marshal
    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "base64Binary", BYTE_ARRAY_CLASS, this);
        cm.addMapping(SoapEnvelope.ENC, "base64", BYTE_ARRAY_CLASS, this);
    }
}

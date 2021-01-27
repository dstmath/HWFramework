package org.ksoap2.serialization;

import java.io.IOException;
import java.math.BigDecimal;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class MarshalFloat implements Marshal {
    @Override // org.ksoap2.serialization.Marshal
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo propertyInfo) throws IOException, XmlPullParserException {
        String stringValue = parser.nextText();
        if (name.equals("float")) {
            return new Float(stringValue);
        }
        if (name.equals("double")) {
            return new Double(stringValue);
        }
        if (name.equals("decimal")) {
            return new BigDecimal(stringValue);
        }
        throw new RuntimeException("float, double, or decimal expected");
    }

    @Override // org.ksoap2.serialization.Marshal
    public void writeInstance(XmlSerializer writer, Object instance) throws IOException {
        writer.text(instance.toString());
    }

    @Override // org.ksoap2.serialization.Marshal
    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "float", Float.class, this);
        cm.addMapping(cm.xsd, "double", Double.class, this);
        cm.addMapping(cm.xsd, "decimal", BigDecimal.class, this);
    }
}

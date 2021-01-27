package org.ksoap2.serialization;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class DM implements Marshal {
    DM() {
    }

    @Override // org.ksoap2.serialization.Marshal
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo excepted) throws IOException, XmlPullParserException {
        String text = parser.nextText();
        char charAt = name.charAt(0);
        if (charAt == 'b') {
            return new Boolean(SoapEnvelope.stringToBoolean(text));
        }
        if (charAt == 'i') {
            return new Integer(Integer.parseInt(text));
        }
        if (charAt == 'l') {
            return new Long(Long.parseLong(text));
        }
        if (charAt == 's') {
            return text;
        }
        throw new RuntimeException();
    }

    @Override // org.ksoap2.serialization.Marshal
    public void writeInstance(XmlSerializer writer, Object instance) throws IOException {
        String str;
        String str2;
        if (instance instanceof AttributeContainer) {
            AttributeContainer attributeContainer = (AttributeContainer) instance;
            int cnt = attributeContainer.getAttributeCount();
            for (int counter = 0; counter < cnt; counter++) {
                AttributeInfo attributeInfo = new AttributeInfo();
                attributeContainer.getAttributeInfo(counter, attributeInfo);
                try {
                    attributeContainer.getAttribute(counter, attributeInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (attributeInfo.getValue() != null) {
                    String namespace = attributeInfo.getNamespace();
                    String name = attributeInfo.getName();
                    if (attributeInfo.getValue() != null) {
                        str2 = attributeInfo.getValue().toString();
                    } else {
                        str2 = "";
                    }
                    writer.attribute(namespace, name, str2);
                }
            }
        } else if (instance instanceof HasAttributes) {
            HasAttributes soapObject = (HasAttributes) instance;
            int cnt2 = soapObject.getAttributeCount();
            for (int counter2 = 0; counter2 < cnt2; counter2++) {
                AttributeInfo attributeInfo2 = new AttributeInfo();
                soapObject.getAttributeInfo(counter2, attributeInfo2);
                try {
                    soapObject.getAttribute(counter2, attributeInfo2);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                if (attributeInfo2.getValue() != null) {
                    String namespace2 = attributeInfo2.getNamespace();
                    String name2 = attributeInfo2.getName();
                    if (attributeInfo2.getValue() != null) {
                        str = attributeInfo2.getValue().toString();
                    } else {
                        str = "";
                    }
                    writer.attribute(namespace2, name2, str);
                }
            }
        }
        if (instance instanceof ValueWriter) {
            ((ValueWriter) instance).write(writer);
        } else {
            writer.text(instance.toString());
        }
    }

    @Override // org.ksoap2.serialization.Marshal
    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "int", PropertyInfo.INTEGER_CLASS, this);
        cm.addMapping(cm.xsd, "long", PropertyInfo.LONG_CLASS, this);
        cm.addMapping(cm.xsd, "string", PropertyInfo.STRING_CLASS, this);
        cm.addMapping(cm.xsd, "boolean", PropertyInfo.BOOLEAN_CLASS, this);
    }
}

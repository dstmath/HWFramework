package org.ksoap2.serialization;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class MarshalHashtable implements Marshal {
    public static final Class HASHTABLE_CLASS = new Hashtable().getClass();
    public static final String NAME = "Map";
    public static final String NAMESPACE = "http://xml.apache.org/xml-soap";
    SoapSerializationEnvelope envelope;

    @Override // org.ksoap2.serialization.Marshal
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        Hashtable instance = new Hashtable();
        String elementName = parser.getName();
        while (parser.nextTag() != 3) {
            SoapObject item = new ItemSoapObject(instance);
            parser.require(2, null, "item");
            parser.nextTag();
            Object key = this.envelope.read(parser, item, 0, null, null, PropertyInfo.OBJECT_TYPE);
            parser.nextTag();
            if (key != null) {
                item.setProperty(0, key);
            }
            Object value = this.envelope.read(parser, item, 1, null, null, PropertyInfo.OBJECT_TYPE);
            parser.nextTag();
            if (value != null) {
                item.setProperty(1, value);
            }
            parser.require(3, null, "item");
        }
        parser.require(3, null, elementName);
        return instance;
    }

    @Override // org.ksoap2.serialization.Marshal
    public void writeInstance(XmlSerializer writer, Object instance) throws IOException {
        Hashtable h = (Hashtable) instance;
        SoapObject item = new SoapObject(null, null);
        item.addProperty("key", null);
        item.addProperty("value", null);
        Enumeration keys = h.keys();
        while (keys.hasMoreElements()) {
            writer.startTag("", "item");
            Object key = keys.nextElement();
            item.setProperty(0, key);
            item.setProperty(1, h.get(key));
            this.envelope.writeObjectBodyWithAttributes(writer, item);
            writer.endTag("", "item");
        }
    }

    class ItemSoapObject extends SoapObject {
        Hashtable h;
        int resolvedIndex = -1;

        ItemSoapObject(Hashtable h2) {
            super(null, null);
            this.h = h2;
            addProperty("key", null);
            addProperty("value", null);
        }

        @Override // org.ksoap2.serialization.SoapObject, org.ksoap2.serialization.KvmSerializable
        public void setProperty(int index, Object value) {
            int i = this.resolvedIndex;
            if (i == -1) {
                super.setProperty(index, value);
                this.resolvedIndex = index;
                return;
            }
            Object resolved = getProperty(i == 0 ? 0 : 1);
            if (index == 0) {
                this.h.put(value, resolved);
            } else {
                this.h.put(resolved, value);
            }
        }
    }

    @Override // org.ksoap2.serialization.Marshal
    public void register(SoapSerializationEnvelope cm) {
        this.envelope = cm;
        cm.addMapping(NAMESPACE, NAME, HASHTABLE_CLASS, this);
    }
}

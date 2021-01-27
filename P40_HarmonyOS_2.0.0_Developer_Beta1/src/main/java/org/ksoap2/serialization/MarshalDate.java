package org.ksoap2.serialization;

import java.io.IOException;
import java.util.Date;
import org.ksoap2.kobjects.isodate.IsoDate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class MarshalDate implements Marshal {
    public static Class DATE_CLASS = new Date().getClass();

    @Override // org.ksoap2.serialization.Marshal
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        return IsoDate.stringToDate(parser.nextText(), 3);
    }

    @Override // org.ksoap2.serialization.Marshal
    public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
        writer.text(IsoDate.dateToString((Date) obj, 3));
    }

    @Override // org.ksoap2.serialization.Marshal
    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "dateTime", DATE_CLASS, this);
    }
}

package org.ksoap2.serialization;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public interface Marshal {
    Object readInstance(XmlPullParser xmlPullParser, String str, String str2, PropertyInfo propertyInfo) throws IOException, XmlPullParserException;

    void register(SoapSerializationEnvelope soapSerializationEnvelope);

    void writeInstance(XmlSerializer xmlSerializer, Object obj) throws IOException;
}

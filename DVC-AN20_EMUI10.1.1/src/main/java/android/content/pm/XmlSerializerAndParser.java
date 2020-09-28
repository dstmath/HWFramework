package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public interface XmlSerializerAndParser<T> {
    @UnsupportedAppUsage
    T createFromXml(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException;

    @UnsupportedAppUsage
    void writeAsXml(T t, XmlSerializer xmlSerializer) throws IOException;
}

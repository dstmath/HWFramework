package android.content.res;

import android.util.AttributeSet;
import org.xmlpull.v1.XmlPullParser;

public interface XmlResourceParser extends XmlPullParser, AttributeSet, AutoCloseable {
    @Override // java.lang.AutoCloseable
    void close();

    @Override // android.util.AttributeSet
    String getAttributeNamespace(int i);
}

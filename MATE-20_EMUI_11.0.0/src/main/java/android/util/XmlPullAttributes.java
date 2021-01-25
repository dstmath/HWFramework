package android.util;

import android.annotation.UnsupportedAppUsage;
import android.media.TtmlUtils;
import com.android.internal.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;

/* access modifiers changed from: package-private */
public class XmlPullAttributes implements AttributeSet {
    @UnsupportedAppUsage
    XmlPullParser mParser;

    @UnsupportedAppUsage
    public XmlPullAttributes(XmlPullParser parser) {
        this.mParser = parser;
    }

    @Override // android.util.AttributeSet
    public int getAttributeCount() {
        return this.mParser.getAttributeCount();
    }

    @Override // android.util.AttributeSet
    public String getAttributeNamespace(int index) {
        return this.mParser.getAttributeNamespace(index);
    }

    @Override // android.util.AttributeSet
    public String getAttributeName(int index) {
        return this.mParser.getAttributeName(index);
    }

    @Override // android.util.AttributeSet
    public String getAttributeValue(int index) {
        return this.mParser.getAttributeValue(index);
    }

    @Override // android.util.AttributeSet
    public String getAttributeValue(String namespace, String name) {
        return this.mParser.getAttributeValue(namespace, name);
    }

    @Override // android.util.AttributeSet
    public String getPositionDescription() {
        return this.mParser.getPositionDescription();
    }

    @Override // android.util.AttributeSet
    public int getAttributeNameResource(int index) {
        return 0;
    }

    @Override // android.util.AttributeSet
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        return XmlUtils.convertValueToList(getAttributeValue(namespace, attribute), options, defaultValue);
    }

    @Override // android.util.AttributeSet
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        return XmlUtils.convertValueToBoolean(getAttributeValue(namespace, attribute), defaultValue);
    }

    @Override // android.util.AttributeSet
    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(namespace, attribute), defaultValue);
    }

    @Override // android.util.AttributeSet
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(namespace, attribute), defaultValue);
    }

    @Override // android.util.AttributeSet
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        return XmlUtils.convertValueToUnsignedInt(getAttributeValue(namespace, attribute), defaultValue);
    }

    @Override // android.util.AttributeSet
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        String s = getAttributeValue(namespace, attribute);
        if (s != null) {
            return Float.parseFloat(s);
        }
        return defaultValue;
    }

    @Override // android.util.AttributeSet
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        return XmlUtils.convertValueToList(getAttributeValue(index), options, defaultValue);
    }

    @Override // android.util.AttributeSet
    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        return XmlUtils.convertValueToBoolean(getAttributeValue(index), defaultValue);
    }

    @Override // android.util.AttributeSet
    public int getAttributeResourceValue(int index, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(index), defaultValue);
    }

    @Override // android.util.AttributeSet
    public int getAttributeIntValue(int index, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(index), defaultValue);
    }

    @Override // android.util.AttributeSet
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return XmlUtils.convertValueToUnsignedInt(getAttributeValue(index), defaultValue);
    }

    @Override // android.util.AttributeSet
    public float getAttributeFloatValue(int index, float defaultValue) {
        String s = getAttributeValue(index);
        if (s != null) {
            return Float.parseFloat(s);
        }
        return defaultValue;
    }

    @Override // android.util.AttributeSet
    public String getIdAttribute() {
        return getAttributeValue(null, "id");
    }

    @Override // android.util.AttributeSet
    public String getClassAttribute() {
        return getAttributeValue(null, "class");
    }

    @Override // android.util.AttributeSet
    public int getIdAttributeResourceValue(int defaultValue) {
        return getAttributeResourceValue(null, "id", defaultValue);
    }

    @Override // android.util.AttributeSet
    public int getStyleAttribute() {
        return getAttributeResourceValue(null, TtmlUtils.TAG_STYLE, 0);
    }
}

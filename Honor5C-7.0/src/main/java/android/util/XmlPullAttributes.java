package android.util;

import com.android.internal.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;

class XmlPullAttributes implements AttributeSet {
    XmlPullParser mParser;

    public XmlPullAttributes(XmlPullParser parser) {
        this.mParser = parser;
    }

    public int getAttributeCount() {
        return this.mParser.getAttributeCount();
    }

    public String getAttributeName(int index) {
        return this.mParser.getAttributeName(index);
    }

    public String getAttributeValue(int index) {
        return this.mParser.getAttributeValue(index);
    }

    public String getAttributeValue(String namespace, String name) {
        return this.mParser.getAttributeValue(namespace, name);
    }

    public String getPositionDescription() {
        return this.mParser.getPositionDescription();
    }

    public int getAttributeNameResource(int index) {
        return 0;
    }

    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        return XmlUtils.convertValueToList(getAttributeValue(namespace, attribute), options, defaultValue);
    }

    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        return XmlUtils.convertValueToBoolean(getAttributeValue(namespace, attribute), defaultValue);
    }

    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(namespace, attribute), defaultValue);
    }

    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(namespace, attribute), defaultValue);
    }

    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        return XmlUtils.convertValueToUnsignedInt(getAttributeValue(namespace, attribute), defaultValue);
    }

    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        String s = getAttributeValue(namespace, attribute);
        if (s != null) {
            return Float.parseFloat(s);
        }
        return defaultValue;
    }

    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        return XmlUtils.convertValueToList(getAttributeValue(index), options, defaultValue);
    }

    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        return XmlUtils.convertValueToBoolean(getAttributeValue(index), defaultValue);
    }

    public int getAttributeResourceValue(int index, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(index), defaultValue);
    }

    public int getAttributeIntValue(int index, int defaultValue) {
        return XmlUtils.convertValueToInt(getAttributeValue(index), defaultValue);
    }

    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return XmlUtils.convertValueToUnsignedInt(getAttributeValue(index), defaultValue);
    }

    public float getAttributeFloatValue(int index, float defaultValue) {
        String s = getAttributeValue(index);
        if (s != null) {
            return Float.parseFloat(s);
        }
        return defaultValue;
    }

    public String getIdAttribute() {
        return getAttributeValue(null, "id");
    }

    public String getClassAttribute() {
        return getAttributeValue(null, "class");
    }

    public int getIdAttributeResourceValue(int defaultValue) {
        return getAttributeResourceValue(null, "id", defaultValue);
    }

    public int getStyleAttribute() {
        return getAttributeResourceValue(null, "style", 0);
    }
}

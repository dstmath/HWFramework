package ohos.agp.components;

import ohos.agp.components.element.Element;
import ohos.agp.utils.Color;
import ohos.app.Context;

public interface Attr {

    public enum AttrType {
        NONE,
        STRING,
        INT,
        LONG,
        FLOAT,
        BOOLEAN,
        DIMENSION,
        ELEMENT,
        COLOR
    }

    boolean getBoolValue();

    Color getColorValue();

    Context getContext();

    int getDimensionValue();

    Element getElement();

    float getFloatValue();

    int getIntegerValue();

    long getLongValue();

    String getName();

    String getStringValue();

    AttrType getType();

    void setContext(Context context);

    void setType(AttrType attrType);
}

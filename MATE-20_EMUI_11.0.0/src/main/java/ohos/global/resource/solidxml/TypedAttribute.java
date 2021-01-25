package ohos.global.resource.solidxml;

import java.io.IOException;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

public abstract class TypedAttribute {
    public static final int BOOLEAN_ATTR = 0;
    public static final int COLOR_ATTR = 1;
    public static final int FLOAT_ATTR = 2;
    public static final int GRAPHIC_ATTR = 8;
    public static final int INTEGER_ATTR = 3;
    public static final int LAYOUT_ATTR = 5;
    public static final int MEDIA_ATTR = 6;
    public static final int PATTERN_ATTR = 7;
    public static final int STRING_ATTR = 4;
    public static final int UNDEFINED_ATTR = -1;

    public abstract boolean getBooleanValue() throws NotExistException, IOException, WrongTypeException;

    public abstract int getColorValue() throws NotExistException, IOException, WrongTypeException;

    public abstract float getFloatValue() throws NotExistException, IOException, WrongTypeException;

    public abstract int getIntegerValue() throws NotExistException, IOException, WrongTypeException;

    public abstract SolidXml getLayoutValue() throws NotExistException, IOException, WrongTypeException;

    public abstract String getMediaValue() throws NotExistException, IOException, WrongTypeException;

    public abstract String getName();

    public abstract String getOriginalValue();

    public abstract Pattern getPatternValue() throws NotExistException, IOException, WrongTypeException;

    public abstract int getResId() throws NotExistException, IOException, WrongTypeException;

    public abstract String getStringValue() throws NotExistException, IOException, WrongTypeException;

    public abstract int getType();
}

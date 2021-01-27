package ohos.global.resource;

import java.io.IOException;
import java.text.Format;
import ohos.global.resource.solidxml.Pattern;
import ohos.global.resource.solidxml.Theme;

public abstract class Element {
    public abstract boolean getBoolean() throws NotExistException, IOException, WrongTypeException;

    public abstract int getColor() throws NotExistException, IOException, WrongTypeException;

    public abstract String getConfig() throws NotExistException, IOException, WrongTypeException;

    public abstract float getFloat() throws NotExistException, IOException, WrongTypeException;

    public abstract int[] getIntArray() throws NotExistException, IOException, WrongTypeException;

    public abstract int getInteger() throws NotExistException, IOException, WrongTypeException;

    public abstract Pattern getPattern() throws NotExistException, IOException, WrongTypeException;

    public abstract String getPluralString(int i) throws NotExistException, IOException, WrongTypeException;

    public abstract String getPluralString(int i, Object... objArr) throws NotExistException, IOException, WrongTypeException;

    public abstract String getString() throws NotExistException, IOException, WrongTypeException;

    public abstract String getString(Object obj, Format format) throws NotExistException, IOException, WrongTypeException;

    public abstract String getString(Object... objArr) throws NotExistException, IOException, WrongTypeException;

    public abstract String getString(Object[] objArr, Format[] formatArr) throws NotExistException, IOException, WrongTypeException;

    public abstract String[] getStringArray() throws NotExistException, IOException, WrongTypeException;

    public abstract Theme getTheme() throws NotExistException, IOException, WrongTypeException;
}

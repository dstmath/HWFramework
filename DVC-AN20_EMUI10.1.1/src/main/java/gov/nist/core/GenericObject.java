package gov.nist.core;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class GenericObject implements Serializable, Cloneable {
    protected static final String AND = "&";
    protected static final String AT = "@";
    protected static final String COLON = ":";
    protected static final String COMMA = ",";
    protected static final String DOT = ".";
    protected static final String DOUBLE_QUOTE = "\"";
    protected static final String EQUALS = "=";
    protected static final String GREATER_THAN = ">";
    protected static final String HT = "\t";
    protected static final String LESS_THAN = "<";
    protected static final String LPAREN = "(";
    protected static final String NEWLINE = "\r\n";
    protected static final String PERCENT = "%";
    protected static final String POUND = "#";
    protected static final String QUESTION = "?";
    protected static final String QUOTE = "'";
    protected static final String RETURN = "\n";
    protected static final String RPAREN = ")";
    protected static final String SEMICOLON = ";";
    protected static final String SLASH = "/";
    protected static final String SP = " ";
    protected static final String STAR = "*";
    static final String[] immutableClassNames = {"String", "Character", "Boolean", "Byte", "Short", "Integer", "Long", "Float", "Double"};
    protected static final Set<Class<?>> immutableClasses = new HashSet(10);
    protected int indentation = 0;
    protected Match matchExpression;
    protected String stringRepresentation = "";

    public abstract String encode();

    static {
        for (int i = 0; i < immutableClassNames.length; i++) {
            try {
                Set<Class<?>> set = immutableClasses;
                set.add(Class.forName("java.lang." + immutableClassNames[i]));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Internal error", e);
            }
        }
    }

    public void setMatcher(Match matchExpression2) {
        if (matchExpression2 != null) {
            this.matchExpression = matchExpression2;
            return;
        }
        throw new IllegalArgumentException("null arg!");
    }

    public Match getMatcher() {
        return this.matchExpression;
    }

    public static Class<?> getClassFromName(String className) {
        try {
            return Class.forName(className);
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }

    public static boolean isMySubclass(Class<?> other) {
        return GenericObject.class.isAssignableFrom(other);
    }

    public static Object makeClone(Object obj) {
        if (obj != null) {
            Class<?> c = obj.getClass();
            Object clone_obj = obj;
            if (immutableClasses.contains(c)) {
                return obj;
            }
            if (c.isArray()) {
                Class<?> ec = c.getComponentType();
                if (!ec.isPrimitive()) {
                    return ((Object[]) obj).clone();
                }
                if (ec == Character.TYPE) {
                    clone_obj = ((char[]) obj).clone();
                } else if (ec == Boolean.TYPE) {
                    clone_obj = ((boolean[]) obj).clone();
                }
                if (ec == Byte.TYPE) {
                    return ((byte[]) obj).clone();
                }
                if (ec == Short.TYPE) {
                    return ((short[]) obj).clone();
                }
                if (ec == Integer.TYPE) {
                    return ((int[]) obj).clone();
                }
                if (ec == Long.TYPE) {
                    return ((long[]) obj).clone();
                }
                if (ec == Float.TYPE) {
                    return ((float[]) obj).clone();
                }
                if (ec == Double.TYPE) {
                    return ((double[]) obj).clone();
                }
                return clone_obj;
            } else if (GenericObject.class.isAssignableFrom(c)) {
                return ((GenericObject) obj).clone();
            } else {
                if (GenericObjectList.class.isAssignableFrom(c)) {
                    return ((GenericObjectList) obj).clone();
                }
                if (!Cloneable.class.isAssignableFrom(c)) {
                    return clone_obj;
                }
                try {
                    return c.getMethod("clone", null).invoke(obj, null);
                } catch (IllegalArgumentException ex) {
                    InternalErrorHandler.handleException(ex);
                    return clone_obj;
                } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    return clone_obj;
                }
            }
        } else {
            throw new NullPointerException("null obj!");
        }
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Internal error");
        }
    }

    public void merge(Object mergeObject) {
        if (mergeObject != null) {
            if (mergeObject.getClass().equals(getClass())) {
                Class<?> myclass = getClass();
                do {
                    Field[] fields = myclass.getDeclaredFields();
                    for (Field f : fields) {
                        int modifier = f.getModifiers();
                        if (!Modifier.isPrivate(modifier) && !Modifier.isStatic(modifier) && !Modifier.isInterface(modifier)) {
                            Class<?> fieldType = f.getType();
                            String fname = fieldType.toString();
                            try {
                                if (!fieldType.isPrimitive()) {
                                    Object obj = f.get(this);
                                    Object mobj = f.get(mergeObject);
                                    if (mobj != null) {
                                        if (obj == null) {
                                            f.set(this, mobj);
                                        } else if (obj instanceof GenericObject) {
                                            ((GenericObject) obj).merge(mobj);
                                        } else {
                                            f.set(this, mobj);
                                        }
                                    }
                                } else if (fname.compareTo("int") == 0) {
                                    f.setInt(this, f.getInt(mergeObject));
                                } else if (fname.compareTo("short") == 0) {
                                    f.setShort(this, f.getShort(mergeObject));
                                } else if (fname.compareTo("char") == 0) {
                                    f.setChar(this, f.getChar(mergeObject));
                                } else if (fname.compareTo("long") == 0) {
                                    f.setLong(this, f.getLong(mergeObject));
                                } else if (fname.compareTo("boolean") == 0) {
                                    f.setBoolean(this, f.getBoolean(mergeObject));
                                } else if (fname.compareTo("double") == 0) {
                                    f.setDouble(this, f.getDouble(mergeObject));
                                } else if (fname.compareTo("float") == 0) {
                                    f.setFloat(this, f.getFloat(mergeObject));
                                }
                            } catch (IllegalAccessException ex1) {
                                ex1.printStackTrace();
                            }
                        }
                    }
                    myclass = myclass.getSuperclass();
                } while (!myclass.equals(GenericObject.class));
                return;
            }
            throw new IllegalArgumentException("Bad override object");
        }
    }

    protected GenericObject() {
    }

    /* access modifiers changed from: protected */
    public String getIndentation() {
        char[] chars = new char[this.indentation];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    /* access modifiers changed from: protected */
    public void sprint(String a) {
        if (a == null) {
            this.stringRepresentation += getIndentation();
            this.stringRepresentation += "<null>\n";
            return;
        }
        if (a.compareTo("}") == 0 || a.compareTo("]") == 0) {
            this.indentation--;
        }
        this.stringRepresentation += getIndentation();
        this.stringRepresentation += a;
        this.stringRepresentation += "\n";
        if (a.compareTo("{") == 0 || a.compareTo("[") == 0) {
            this.indentation++;
        }
    }

    /* access modifiers changed from: protected */
    public void sprint(Object o) {
        sprint(o.toString());
    }

    /* access modifiers changed from: protected */
    public void sprint(int intField) {
        sprint(String.valueOf(intField));
    }

    /* access modifiers changed from: protected */
    public void sprint(short shortField) {
        sprint(String.valueOf((int) shortField));
    }

    /* access modifiers changed from: protected */
    public void sprint(char charField) {
        sprint(String.valueOf(charField));
    }

    /* access modifiers changed from: protected */
    public void sprint(long longField) {
        sprint(String.valueOf(longField));
    }

    /* access modifiers changed from: protected */
    public void sprint(boolean booleanField) {
        sprint(String.valueOf(booleanField));
    }

    /* access modifiers changed from: protected */
    public void sprint(double doubleField) {
        sprint(String.valueOf(doubleField));
    }

    /* access modifiers changed from: protected */
    public void sprint(float floatField) {
        sprint(String.valueOf(floatField));
    }

    /* access modifiers changed from: protected */
    public void dbgPrint() {
        Debug.println(debugDump());
    }

    /* access modifiers changed from: protected */
    public void dbgPrint(String s) {
        Debug.println(s);
    }

    public boolean equals(Object that) {
        if (that == null || !getClass().equals(that.getClass())) {
            return false;
        }
        Class<?> myclass = getClass();
        Class<?> hisclass = that.getClass();
        Class<?> myclass2 = myclass;
        while (true) {
            Field[] fields = myclass2.getDeclaredFields();
            Field[] hisfields = hisclass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                Field g = hisfields[i];
                if ((f.getModifiers() & 2) != 2) {
                    Class<?> fieldType = f.getType();
                    String fieldName = f.getName();
                    if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                        try {
                            if (fieldType.isPrimitive()) {
                                String fname = fieldType.toString();
                                if (fname.compareTo("int") == 0) {
                                    if (f.getInt(this) != g.getInt(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("short") == 0) {
                                    if (f.getShort(this) != g.getShort(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("char") == 0) {
                                    if (f.getChar(this) != g.getChar(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("long") == 0) {
                                    if (f.getLong(this) != g.getLong(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("boolean") == 0) {
                                    if (f.getBoolean(this) != g.getBoolean(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("double") == 0) {
                                    if (f.getDouble(this) != g.getDouble(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("float") == 0 && f.getFloat(this) != g.getFloat(that)) {
                                    return false;
                                }
                            } else if (g.get(that) == f.get(this)) {
                                return true;
                            } else {
                                if (f.get(this) == null || g.get(that) == null) {
                                    return false;
                                }
                                if ((g.get(that) == null && f.get(this) != null) || !f.get(this).equals(g.get(that))) {
                                    return false;
                                }
                            }
                        } catch (IllegalAccessException ex1) {
                            InternalErrorHandler.handleException(ex1);
                        }
                    }
                }
            }
            if (myclass2.equals(GenericObject.class)) {
                return true;
            }
            myclass2 = myclass2.getSuperclass();
            hisclass = hisclass.getSuperclass();
        }
    }

    public boolean match(Object other) {
        if (other == null) {
            return true;
        }
        boolean z = false;
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        GenericObject that = (GenericObject) other;
        Field[] fields = getClass().getDeclaredFields();
        Field[] hisfields = other.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            Field g = hisfields[i];
            if ((f.getModifiers() & 2) != 2) {
                Class<?> fieldType = f.getType();
                String fieldName = f.getName();
                if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                    try {
                        if (fieldType.isPrimitive()) {
                            String fname = fieldType.toString();
                            if (fname.compareTo("int") == 0) {
                                if (f.getInt(this) != g.getInt(that)) {
                                    return z;
                                }
                            } else if (fname.compareTo("short") == 0) {
                                if (f.getShort(this) != g.getShort(that)) {
                                    return z;
                                }
                            } else if (fname.compareTo("char") == 0) {
                                if (f.getChar(this) != g.getChar(that)) {
                                    return z;
                                }
                            } else if (fname.compareTo("long") == 0) {
                                if (f.getLong(this) != g.getLong(that)) {
                                    return z;
                                }
                            } else if (fname.compareTo("boolean") == 0) {
                                if (f.getBoolean(this) != g.getBoolean(that)) {
                                    return z;
                                }
                            } else if (fname.compareTo("double") == 0) {
                                if (f.getDouble(this) != g.getDouble(that)) {
                                    return z;
                                }
                            } else if (fname.compareTo("float") == 0 && f.getFloat(this) != g.getFloat(that)) {
                                return z;
                            }
                        } else {
                            Object myObj = f.get(this);
                            Object hisObj = g.get(that);
                            if (hisObj != null && myObj == null) {
                                return z;
                            }
                            if (hisObj != null || myObj == null) {
                                if (hisObj != null || myObj != null) {
                                    if ((hisObj instanceof String) && (myObj instanceof String)) {
                                        try {
                                            if (((String) hisObj).trim().equals("")) {
                                                z = false;
                                            } else if (((String) myObj).compareToIgnoreCase((String) hisObj) != 0) {
                                                return false;
                                            } else {
                                                z = false;
                                            }
                                        } catch (IllegalAccessException e) {
                                            ex1 = e;
                                            z = false;
                                            InternalErrorHandler.handleException(ex1);
                                        }
                                    } else if (isMySubclass(myObj.getClass()) && !((GenericObject) myObj).match(hisObj)) {
                                        return false;
                                    } else {
                                        if (!GenericObjectList.isMySubclass(myObj.getClass())) {
                                            z = false;
                                        } else if (!((GenericObjectList) myObj).match(hisObj)) {
                                            return false;
                                        } else {
                                            z = false;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IllegalAccessException e2) {
                        ex1 = e2;
                        InternalErrorHandler.handleException(ex1);
                    }
                }
            }
        }
        return true;
    }

    public String debugDump() {
        this.stringRepresentation = "";
        Class<?> myclass = getClass();
        sprint(myclass.getName());
        sprint("{");
        Field[] fields = myclass.getDeclaredFields();
        for (Field f : fields) {
            if ((f.getModifiers() & 2) != 2) {
                Class<?> fieldType = f.getType();
                String fieldName = f.getName();
                if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                    sprint(fieldName + ":");
                    try {
                        if (fieldType.isPrimitive()) {
                            String fname = fieldType.toString();
                            sprint(fname + ":");
                            if (fname.compareTo("int") == 0) {
                                sprint(f.getInt(this));
                            } else if (fname.compareTo("short") == 0) {
                                sprint(f.getShort(this));
                            } else if (fname.compareTo("char") == 0) {
                                sprint(f.getChar(this));
                            } else if (fname.compareTo("long") == 0) {
                                sprint(f.getLong(this));
                            } else if (fname.compareTo("boolean") == 0) {
                                sprint(f.getBoolean(this));
                            } else if (fname.compareTo("double") == 0) {
                                sprint(f.getDouble(this));
                            } else if (fname.compareTo("float") == 0) {
                                sprint(f.getFloat(this));
                            }
                        } else if (GenericObject.class.isAssignableFrom(fieldType)) {
                            if (f.get(this) != null) {
                                sprint(((GenericObject) f.get(this)).debugDump(this.indentation + 1));
                            } else {
                                sprint("<null>");
                            }
                        } else if (!GenericObjectList.class.isAssignableFrom(fieldType)) {
                            if (f.get(this) != null) {
                                sprint(f.get(this).getClass().getName() + ":");
                            } else {
                                sprint(fieldType.getName() + ":");
                            }
                            sprint("{");
                            if (f.get(this) != null) {
                                sprint(f.get(this).toString());
                            } else {
                                sprint("<null>");
                            }
                            sprint("}");
                        } else if (f.get(this) != null) {
                            sprint(((GenericObjectList) f.get(this)).debugDump(this.indentation + 1));
                        } else {
                            sprint("<null>");
                        }
                    } catch (IllegalAccessException e) {
                    } catch (Exception ex) {
                        InternalErrorHandler.handleException(ex);
                    }
                }
            }
        }
        sprint("}");
        return this.stringRepresentation;
    }

    public String debugDump(int indent) {
        this.indentation = indent;
        String retval = debugDump();
        this.indentation = 0;
        return retval;
    }

    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(encode());
        return buffer;
    }
}

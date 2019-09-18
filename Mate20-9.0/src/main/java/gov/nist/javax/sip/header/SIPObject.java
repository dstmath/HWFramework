package gov.nist.javax.sip.header;

import gov.nist.core.GenericObject;
import gov.nist.core.GenericObjectList;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import java.io.PrintStream;
import java.lang.reflect.Field;

public abstract class SIPObject extends GenericObject {
    public abstract String encode();

    protected SIPObject() {
    }

    public void dbgPrint() {
        super.dbgPrint();
    }

    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(encode());
        return buffer;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:89:0x017c, code lost:
        if (r5.equals(gov.nist.javax.sip.header.SIPObject.class) == false) goto L_0x0181;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0180, code lost:
        return true;
     */
    public boolean equals(Object other) {
        SIPObject sIPObject = this;
        boolean z = false;
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        SIPObject that = (SIPObject) other;
        Class myclass = getClass();
        Class hisclass = other.getClass();
        Class myclass2 = myclass;
        while (true) {
            Field[] fields = myclass2.getDeclaredFields();
            if (hisclass.equals(myclass2)) {
                Field[] hisfields = hisclass.getDeclaredFields();
                int i = z;
                while (true) {
                    int i2 = i;
                    if (i2 >= fields.length) {
                        break;
                    }
                    Field f = fields[i2];
                    Field g = hisfields[i2];
                    int modifier = f.getModifiers();
                    if ((modifier & 2) != 2) {
                        Class fieldType = f.getType();
                        String fieldName = f.getName();
                        if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                            try {
                                if (fieldType.isPrimitive()) {
                                    String fname = fieldType.toString();
                                    if (fname.compareTo("int") == 0) {
                                        try {
                                            if (f.getInt(sIPObject) != g.getInt(that)) {
                                                return false;
                                            }
                                        } catch (IllegalAccessException e) {
                                            ex1 = e;
                                            PrintStream printStream = System.out;
                                            printStream.println("accessed field " + fieldName);
                                            PrintStream printStream2 = System.out;
                                            printStream2.println("modifier  " + modifier);
                                            System.out.println("modifier.private  2");
                                            InternalErrorHandler.handleException((Exception) ex1);
                                            i = i2 + 1;
                                            sIPObject = this;
                                        }
                                    } else if (fname.compareTo("short") == 0) {
                                        if (f.getShort(sIPObject) != g.getShort(that)) {
                                            return false;
                                        }
                                    } else if (fname.compareTo("char") == 0) {
                                        if (f.getChar(sIPObject) != g.getChar(that)) {
                                            return false;
                                        }
                                    } else if (fname.compareTo("long") == 0) {
                                        if (f.getLong(sIPObject) != g.getLong(that)) {
                                            return false;
                                        }
                                    } else if (fname.compareTo("boolean") == 0) {
                                        if (f.getBoolean(sIPObject) != g.getBoolean(that)) {
                                            return false;
                                        }
                                    } else if (fname.compareTo("double") == 0) {
                                        if (f.getDouble(sIPObject) != g.getDouble(that)) {
                                            return false;
                                        }
                                    } else if (fname.compareTo("float") == 0 && f.getFloat(sIPObject) != g.getFloat(that)) {
                                        return false;
                                    }
                                } else if (g.get(that) != f.get(sIPObject)) {
                                    if (f.get(sIPObject) == null && g.get(that) != null) {
                                        return false;
                                    }
                                    if (g.get(that) == null && f.get(sIPObject) != null) {
                                        return false;
                                    }
                                    if (!f.get(sIPObject).equals(g.get(that))) {
                                        return false;
                                    }
                                }
                            } catch (IllegalAccessException e2) {
                                ex1 = e2;
                                PrintStream printStream3 = System.out;
                                printStream3.println("accessed field " + fieldName);
                                PrintStream printStream22 = System.out;
                                printStream22.println("modifier  " + modifier);
                                System.out.println("modifier.private  2");
                                InternalErrorHandler.handleException((Exception) ex1);
                                i = i2 + 1;
                                sIPObject = this;
                            }
                        }
                    }
                    i = i2 + 1;
                    sIPObject = this;
                }
            } else {
                return z;
            }
            myclass2 = myclass2.getSuperclass();
            hisclass = hisclass.getSuperclass();
            sIPObject = this;
            z = false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:120:0x01c2, code lost:
        r1 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x01c9, code lost:
        if (r6.equals(gov.nist.javax.sip.header.SIPObject.class) == false) goto L_0x01ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x01cd, code lost:
        return true;
     */
    public boolean match(Object other) {
        int i;
        int i2;
        SIPObject sIPObject = this;
        if (other == null) {
            return true;
        }
        int i3 = 0;
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        GenericObject that = (GenericObject) other;
        Class myclass = getClass();
        Class hisclass = other.getClass();
        Class myclass2 = myclass;
        while (true) {
            Field[] fields = myclass2.getDeclaredFields();
            Field[] hisfields = hisclass.getDeclaredFields();
            int i4 = i3;
            while (true) {
                int i5 = i4;
                if (i5 >= fields.length) {
                    break;
                }
                Field f = fields[i5];
                Field g = hisfields[i5];
                if ((f.getModifiers() & 2) != 2) {
                    Class fieldType = f.getType();
                    String fieldName = f.getName();
                    if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                        try {
                            if (fieldType.isPrimitive()) {
                                String fname = fieldType.toString();
                                if (fname.compareTo("int") == 0) {
                                    try {
                                        if (f.getInt(sIPObject) != g.getInt(that)) {
                                            return false;
                                        }
                                    } catch (IllegalAccessException e) {
                                        ex1 = e;
                                        i2 = 0;
                                        InternalErrorHandler.handleException((Exception) ex1);
                                        i4 = i5 + 1;
                                        i3 = i2;
                                        sIPObject = this;
                                    }
                                } else if (fname.compareTo("short") == 0) {
                                    if (f.getShort(sIPObject) != g.getShort(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("char") == 0) {
                                    if (f.getChar(sIPObject) != g.getChar(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("long") == 0) {
                                    if (f.getLong(sIPObject) != g.getLong(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("boolean") == 0) {
                                    if (f.getBoolean(sIPObject) != g.getBoolean(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("double") == 0) {
                                    if (f.getDouble(sIPObject) != g.getDouble(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("float") != 0) {
                                    InternalErrorHandler.handleException("unknown type");
                                } else if (f.getFloat(sIPObject) != g.getFloat(that)) {
                                    return false;
                                }
                            } else {
                                Object myObj = f.get(sIPObject);
                                Object hisObj = g.get(that);
                                if (hisObj != null && myObj == null) {
                                    return false;
                                }
                                if (hisObj != null || myObj == null) {
                                    if (hisObj != null || myObj != null) {
                                        if (!(hisObj instanceof String) || !(myObj instanceof String)) {
                                            if (hisObj != null && GenericObject.isMySubclass(myObj.getClass()) && GenericObject.isMySubclass(hisObj.getClass()) && myObj.getClass().equals(hisObj.getClass()) && ((GenericObject) hisObj).getMatcher() != null) {
                                                if (!((GenericObject) hisObj).getMatcher().match(((GenericObject) myObj).encode())) {
                                                    return false;
                                                }
                                            } else if (GenericObject.isMySubclass(myObj.getClass()) && !((GenericObject) myObj).match(hisObj)) {
                                                return false;
                                            } else {
                                                if (GenericObjectList.isMySubclass(myObj.getClass()) && !((GenericObjectList) myObj).match(hisObj)) {
                                                    return false;
                                                }
                                            }
                                        } else if (!((String) hisObj).trim().equals("")) {
                                            if (((String) myObj).compareToIgnoreCase((String) hisObj) != 0) {
                                                return false;
                                            }
                                        }
                                    }
                                }
                                i2 = 0;
                                i4 = i5 + 1;
                                i3 = i2;
                                sIPObject = this;
                            }
                            i2 = 0;
                        } catch (IllegalAccessException e2) {
                            ex1 = e2;
                            i2 = i3;
                            InternalErrorHandler.handleException((Exception) ex1);
                            i4 = i5 + 1;
                            i3 = i2;
                            sIPObject = this;
                        }
                        i4 = i5 + 1;
                        i3 = i2;
                        sIPObject = this;
                    }
                }
                i2 = i3;
                i4 = i5 + 1;
                i3 = i2;
                sIPObject = this;
            }
            myclass2 = myclass2.getSuperclass();
            hisclass = hisclass.getSuperclass();
            i3 = i;
            sIPObject = this;
        }
    }

    public String debugDump() {
        this.stringRepresentation = "";
        Class myclass = getClass();
        sprint(myclass.getName());
        sprint("{");
        Field[] fields = myclass.getDeclaredFields();
        for (Field f : fields) {
            if ((f.getModifiers() & 2) != 2) {
                Class fieldType = f.getType();
                String fieldName = f.getName();
                if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                    sprint(fieldName + Separators.COLON);
                    try {
                        if (fieldType.isPrimitive()) {
                            String fname = fieldType.toString();
                            sprint(fname + Separators.COLON);
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
                                sprint(f.get(this).getClass().getName() + Separators.COLON);
                            } else {
                                sprint(fieldType.getName() + Separators.COLON);
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
                    }
                }
            }
        }
        sprint("}");
        return this.stringRepresentation;
    }

    public String debugDump(int indent) {
        int save = this.indentation;
        this.indentation = indent;
        String retval = debugDump();
        this.indentation = save;
        return retval;
    }

    public String toString() {
        return encode();
    }
}

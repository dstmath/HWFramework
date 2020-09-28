package gov.nist.javax.sip.header;

import gov.nist.core.GenericObject;
import gov.nist.core.GenericObjectList;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import java.lang.reflect.Field;

public abstract class SIPObject extends GenericObject {
    @Override // gov.nist.core.GenericObject
    public abstract String encode();

    protected SIPObject() {
    }

    @Override // gov.nist.core.GenericObject
    public void dbgPrint() {
        super.dbgPrint();
    }

    @Override // gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(encode());
        return buffer;
    }

    @Override // gov.nist.core.GenericObject
    public boolean equals(Object other) {
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
            if (!hisclass.equals(myclass2)) {
                return z;
            }
            Field[] hisfields = hisclass.getDeclaredFields();
            int i = 0;
            while (i < fields.length) {
                Field f = fields[i];
                Field g = hisfields[i];
                int modifier = f.getModifiers();
                if ((modifier & 2) != 2) {
                    Class fieldType = f.getType();
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
                            } else if (g.get(that) != f.get(this)) {
                                if (f.get(this) == null && g.get(that) != null) {
                                    return z;
                                }
                                if ((g.get(that) == null && f.get(this) != null) || !f.get(this).equals(g.get(that))) {
                                    return z;
                                }
                            }
                        } catch (IllegalAccessException ex1) {
                            System.out.println("accessed field " + fieldName);
                            System.out.println("modifier  " + modifier);
                            System.out.println("modifier.private  2");
                            InternalErrorHandler.handleException(ex1);
                        }
                    }
                }
                i++;
                z = false;
            }
            if (myclass2.equals(SIPObject.class)) {
                return true;
            }
            myclass2 = myclass2.getSuperclass();
            hisclass = hisclass.getSuperclass();
            z = false;
        }
    }

    @Override // gov.nist.core.GenericObject
    public boolean match(Object other) {
        if (other == null) {
            return true;
        }
        boolean z = false;
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
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                Field g = hisfields[i];
                if ((f.getModifiers() & 2) != 2) {
                    Class fieldType = f.getType();
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
                                } else if (fname.compareTo("float") != 0) {
                                    InternalErrorHandler.handleException("unknown type");
                                } else if (f.getFloat(this) != g.getFloat(that)) {
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
                                        } else if (hisObj != null && GenericObject.isMySubclass(myObj.getClass()) && GenericObject.isMySubclass(hisObj.getClass()) && myObj.getClass().equals(hisObj.getClass()) && ((GenericObject) hisObj).getMatcher() != null) {
                                            if (!((GenericObject) hisObj).getMatcher().match(((GenericObject) myObj).encode())) {
                                                return false;
                                            }
                                            z = false;
                                        } else if (GenericObject.isMySubclass(myObj.getClass()) && !((GenericObject) myObj).match(hisObj)) {
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
            if (myclass2.equals(SIPObject.class)) {
                return true;
            }
            myclass2 = myclass2.getSuperclass();
            hisclass = hisclass.getSuperclass();
        }
    }

    @Override // gov.nist.core.GenericObject
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

    @Override // gov.nist.core.GenericObject
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

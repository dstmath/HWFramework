package gov.nist.javax.sip.address;

import gov.nist.core.GenericObject;
import gov.nist.core.GenericObjectList;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import java.lang.reflect.Field;

public abstract class NetObject extends GenericObject {
    protected static final String CORE_PACKAGE = "gov.nist.core";
    protected static final String GRUU = "gr";
    protected static final String LR = "lr";
    protected static final String MADDR = "maddr";
    protected static final String METHOD = "method";
    protected static final String NET_PACKAGE = "gov.nist.javax.sip.address";
    protected static final String PARSER_PACKAGE = "gov.nist.javax.sip.parser";
    protected static final String PHONE = "phone";
    protected static final String SIP = "sip";
    protected static final String SIPS = "sips";
    protected static final String TCP = "tcp";
    protected static final String TLS = "tls";
    protected static final String TRANSPORT = "transport";
    protected static final String TTL = "ttl";
    protected static final String UDP = "udp";
    protected static final String USER = "user";
    protected static final long serialVersionUID = 6149926203633320729L;

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object that) {
        if (!getClass().equals(that.getClass())) {
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
                            } else if (g.get(that) != f.get(this)) {
                                if (f.get(this) == null && g.get(that) != null) {
                                    return false;
                                }
                                if ((g.get(that) == null && f.get(that) != null) || !f.get(this).equals(g.get(that))) {
                                    return false;
                                }
                            }
                        } catch (IllegalAccessException ex1) {
                            InternalErrorHandler.handleException(ex1);
                        }
                    }
                }
            }
            if (myclass2.equals(NetObject.class)) {
                return true;
            }
            myclass2 = myclass2.getSuperclass();
            hisclass = hisclass.getSuperclass();
        }
    }

    @Override // gov.nist.core.GenericObject
    public boolean match(Object other) {
        IllegalAccessException ex1;
        if (other == null) {
            return true;
        }
        boolean z = false;
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        GenericObject that = (GenericObject) other;
        Class<?> hisclass = other.getClass();
        Class<?> myclass = getClass();
        Class<?> hisclass2 = hisclass;
        while (true) {
            Field[] fields = myclass.getDeclaredFields();
            Field[] hisfields = hisclass2.getDeclaredFields();
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
                                                if (((String) hisObj).equals("")) {
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
                                        } else if (GenericObject.isMySubclass(myObj.getClass()) && GenericObject.isMySubclass(hisObj.getClass()) && myObj.getClass().equals(hisObj.getClass()) && ((GenericObject) hisObj).getMatcher() != null) {
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
            if (myclass.equals(NetObject.class)) {
                return true;
            }
            myclass = myclass.getSuperclass();
            hisclass2 = hisclass2.getSuperclass();
        }
    }

    @Override // gov.nist.core.GenericObject
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

    @Override // java.lang.Object, javax.sip.address.URI
    public String toString() {
        return encode();
    }
}

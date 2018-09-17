package com.android.internal.util;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TypedProperties extends HashMap<String, Object> {
    static final String NULL_STRING = null;
    public static final int STRING_NOT_SET = -1;
    public static final int STRING_NULL = 0;
    public static final int STRING_SET = 1;
    public static final int STRING_TYPE_MISMATCH = -2;
    static final int TYPE_BOOLEAN = 90;
    static final int TYPE_BYTE = 329;
    static final int TYPE_DOUBLE = 2118;
    static final int TYPE_ERROR = -1;
    static final int TYPE_FLOAT = 1094;
    static final int TYPE_INT = 1097;
    static final int TYPE_LONG = 2121;
    static final int TYPE_SHORT = 585;
    static final int TYPE_STRING = 29516;
    static final int TYPE_UNSET = 120;

    public static class ParseException extends IllegalArgumentException {
        ParseException(StreamTokenizer state, String expected) {
            super("expected " + expected + ", saw " + state.toString());
        }
    }

    public static class TypeException extends IllegalArgumentException {
        TypeException(String property, Object value, String requestedType) {
            super(property + " has type " + value.getClass().getName() + ", not " + requestedType);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.TypedProperties.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.TypedProperties.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.TypedProperties.<clinit>():void");
    }

    static StreamTokenizer initTokenizer(Reader r) {
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars(48, 57);
        st.wordChars(65, TYPE_BOOLEAN);
        st.wordChars(97, LogPower.NOTIFICATION_ENQUEUE);
        st.wordChars(95, 95);
        st.wordChars(36, 36);
        st.wordChars(46, 46);
        st.wordChars(45, 45);
        st.wordChars(43, 43);
        st.ordinaryChar(61);
        st.whitespaceChars(32, 32);
        st.whitespaceChars(9, 9);
        st.whitespaceChars(10, 10);
        st.whitespaceChars(13, 13);
        st.quoteChar(34);
        st.slashStarComments(true);
        st.slashSlashComments(true);
        return st;
    }

    static int interpretType(String typeName) {
        if ("unset".equals(typeName)) {
            return TYPE_UNSET;
        }
        if ("boolean".equals(typeName)) {
            return TYPE_BOOLEAN;
        }
        if ("byte".equals(typeName)) {
            return TYPE_BYTE;
        }
        if ("short".equals(typeName)) {
            return TYPE_SHORT;
        }
        if ("int".equals(typeName)) {
            return TYPE_INT;
        }
        if ("long".equals(typeName)) {
            return TYPE_LONG;
        }
        if ("float".equals(typeName)) {
            return TYPE_FLOAT;
        }
        if ("double".equals(typeName)) {
            return TYPE_DOUBLE;
        }
        if ("String".equals(typeName)) {
            return TYPE_STRING;
        }
        return TYPE_ERROR;
    }

    static void parse(Reader r, Map<String, Object> map) throws ParseException, IOException {
        StreamTokenizer st = initTokenizer(r);
        String identifierPattern = "[a-zA-Z_$][0-9a-zA-Z_$]*";
        Pattern propertyNamePattern = Pattern.compile("([a-zA-Z_$][0-9a-zA-Z_$]*\\.)*[a-zA-Z_$][0-9a-zA-Z_$]*");
        do {
            int token = st.nextToken();
            if (token != TYPE_ERROR) {
                if (token != -3) {
                    throw new ParseException(st, "type name");
                }
                int type = interpretType(st.sval);
                if (type == TYPE_ERROR) {
                    throw new ParseException(st, "valid type name");
                }
                st.sval = null;
                if (type == TYPE_UNSET && st.nextToken() != 40) {
                    throw new ParseException(st, "'('");
                } else if (st.nextToken() != -3) {
                    throw new ParseException(st, "property name");
                } else {
                    String propertyName = st.sval;
                    if (propertyNamePattern.matcher(propertyName).matches()) {
                        st.sval = null;
                        if (type == TYPE_UNSET) {
                            if (st.nextToken() != 41) {
                                throw new ParseException(st, "')'");
                            }
                            map.remove(propertyName);
                        } else if (st.nextToken() != 61) {
                            throw new ParseException(st, "'='");
                        } else {
                            Object value = parseValue(st, type);
                            Object oldValue = map.remove(propertyName);
                            if (oldValue == null || value.getClass() == oldValue.getClass()) {
                                map.put(propertyName, value);
                            } else {
                                throw new ParseException(st, "(property previously declared as a different type)");
                            }
                        }
                    }
                    throw new ParseException(st, "valid property name");
                }
            }
            return;
        } while (st.nextToken() == 59);
        throw new ParseException(st, "';'");
    }

    static Object parseValue(StreamTokenizer st, int type) throws IOException {
        int token = st.nextToken();
        if (type == TYPE_BOOLEAN) {
            if (token != -3) {
                throw new ParseException(st, "boolean constant");
            } else if ("true".equals(st.sval)) {
                return Boolean.TRUE;
            } else {
                if ("false".equals(st.sval)) {
                    return Boolean.FALSE;
                }
                throw new ParseException(st, "boolean constant");
            }
        } else if ((type & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) == 73) {
            if (token != -3) {
                throw new ParseException(st, "integer constant");
            }
            try {
                long value = Long.decode(st.sval).longValue();
                int width = (type >> 8) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
                switch (width) {
                    case STRING_SET /*1*/:
                        if (value >= -128 && value <= 127) {
                            return new Byte((byte) ((int) value));
                        }
                        throw new ParseException(st, "8-bit integer constant");
                    case HwCfgFilePolicy.PC /*2*/:
                        if (value >= -32768 && value <= 32767) {
                            return new Short((short) ((int) value));
                        }
                        throw new ParseException(st, "16-bit integer constant");
                    case HwCfgFilePolicy.CUST /*4*/:
                        if (value >= -2147483648L && value <= 2147483647L) {
                            return new Integer((int) value);
                        }
                        throw new ParseException(st, "32-bit integer constant");
                    case PGSdk.TYPE_VIDEO /*8*/:
                        if (value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
                            return new Long(value);
                        }
                        throw new ParseException(st, "64-bit integer constant");
                    default:
                        throw new IllegalStateException("Internal error; unexpected integer type width " + width);
                }
            } catch (NumberFormatException e) {
                throw new ParseException(st, "integer constant");
            }
        } else if ((type & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) == 70) {
            if (token != -3) {
                throw new ParseException(st, "float constant");
            }
            try {
                double value2 = Double.parseDouble(st.sval);
                if (((type >> 8) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) != 4) {
                    return new Double(value2);
                }
                double absValue = Math.abs(value2);
                if (absValue == 0.0d || Double.isInfinite(value2) || Double.isNaN(value2) || (absValue >= 1.401298464324817E-45d && absValue <= 3.4028234663852886E38d)) {
                    return new Float((float) value2);
                }
                throw new ParseException(st, "32-bit float constant");
            } catch (NumberFormatException e2) {
                throw new ParseException(st, "float constant");
            }
        } else if (type != TYPE_STRING) {
            throw new IllegalStateException("Internal error; unknown type " + type);
        } else if (token == 34) {
            return st.sval;
        } else {
            if (token == -3 && "null".equals(st.sval)) {
                return NULL_STRING;
            }
            throw new ParseException(st, "double-quoted string or 'null'");
        }
    }

    public void load(Reader r) throws IOException {
        parse(r, this);
    }

    public Object get(Object key) {
        String value = super.get(key);
        if (value == NULL_STRING) {
            return null;
        }
        return value;
    }

    public boolean getBoolean(String property, boolean def) {
        Object value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        throw new TypeException(property, value, "boolean");
    }

    public byte getByte(String property, byte def) {
        Object value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value instanceof Byte) {
            return ((Byte) value).byteValue();
        }
        throw new TypeException(property, value, "byte");
    }

    public short getShort(String property, short def) {
        Object value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value instanceof Short) {
            return ((Short) value).shortValue();
        }
        throw new TypeException(property, value, "short");
    }

    public int getInt(String property, int def) {
        Object value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        throw new TypeException(property, value, "int");
    }

    public long getLong(String property, long def) {
        Object value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        throw new TypeException(property, value, "long");
    }

    public float getFloat(String property, float def) {
        Object value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value instanceof Float) {
            return ((Float) value).floatValue();
        }
        throw new TypeException(property, value, "float");
    }

    public double getDouble(String property, double def) {
        Object value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value instanceof Double) {
            return ((Double) value).doubleValue();
        }
        throw new TypeException(property, value, "double");
    }

    public String getString(String property, String def) {
        String value = super.get(property);
        if (value == null) {
            return def;
        }
        if (value == NULL_STRING) {
            return null;
        }
        if (value instanceof String) {
            return value;
        }
        throw new TypeException(property, value, "string");
    }

    public boolean getBoolean(String property) {
        return getBoolean(property, false);
    }

    public byte getByte(String property) {
        return getByte(property, (byte) 0);
    }

    public short getShort(String property) {
        return getShort(property, (short) 0);
    }

    public int getInt(String property) {
        return getInt(property, STRING_NULL);
    }

    public long getLong(String property) {
        return getLong(property, 0);
    }

    public float getFloat(String property) {
        return getFloat(property, 0.0f);
    }

    public double getDouble(String property) {
        return getDouble(property, 0.0d);
    }

    public String getString(String property) {
        return getString(property, "");
    }

    public int getStringInfo(String property) {
        String value = super.get(property);
        if (value == null) {
            return TYPE_ERROR;
        }
        if (value == NULL_STRING) {
            return STRING_NULL;
        }
        if (value instanceof String) {
            return STRING_SET;
        }
        return STRING_TYPE_MISMATCH;
    }
}

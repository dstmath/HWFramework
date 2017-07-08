package com.google.protobuf.nano;

import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.util.InformationElementUtil.SupportedRates;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MessageNanoPrinter {
    private static final String INDENT = "  ";
    private static final int MAX_STRING_LEN = 200;

    private MessageNanoPrinter() {
    }

    public static <T extends MessageNano> String print(T message) {
        if (message == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        try {
            print(null, message, new StringBuffer(), buf);
            return buf.toString();
        } catch (IllegalAccessException e) {
            return "Error printing proto: " + e.getMessage();
        } catch (InvocationTargetException e2) {
            return "Error printing proto: " + e2.getMessage();
        }
    }

    private static void print(String identifier, Object object, StringBuffer indentBuf, StringBuffer buf) throws IllegalAccessException, InvocationTargetException {
        if (object != null) {
            if (object instanceof MessageNano) {
                int origIndentBufLength = indentBuf.length();
                if (identifier != null) {
                    buf.append(indentBuf).append(deCamelCaseify(identifier)).append(" <\n");
                    indentBuf.append(INDENT);
                }
                Class<?> clazz = object.getClass();
                for (Field field : clazz.getFields()) {
                    int modifiers = field.getModifiers();
                    String fieldName = field.getName();
                    if (!("cachedSize".equals(fieldName) || (modifiers & 1) != 1 || (modifiers & 8) == 8)) {
                        if (!fieldName.startsWith("_")) {
                            if (!fieldName.endsWith("_")) {
                                Class<?> fieldType = field.getType();
                                Object value = field.get(object);
                                if (!fieldType.isArray()) {
                                    print(fieldName, value, indentBuf, buf);
                                } else if (fieldType.getComponentType() == Byte.TYPE) {
                                    print(fieldName, value, indentBuf, buf);
                                } else {
                                    int len = value == null ? 0 : Array.getLength(value);
                                    for (int i = 0; i < len; i++) {
                                        print(fieldName, Array.get(value, i), indentBuf, buf);
                                    }
                                }
                            }
                        }
                    }
                }
                for (Method method : clazz.getMethods()) {
                    String name = method.getName();
                    if (name.startsWith("set")) {
                        String subfieldName = name.substring(3);
                        try {
                            if (((Boolean) clazz.getMethod("has" + subfieldName, new Class[0]).invoke(object, new Object[0])).booleanValue()) {
                                try {
                                    print(subfieldName, clazz.getMethod("get" + subfieldName, new Class[0]).invoke(object, new Object[0]), indentBuf, buf);
                                } catch (NoSuchMethodException e) {
                                }
                            }
                        } catch (NoSuchMethodException e2) {
                        }
                    }
                }
                if (identifier != null) {
                    indentBuf.setLength(origIndentBufLength);
                    buf.append(indentBuf).append(">\n");
                    return;
                }
                return;
            }
            buf.append(indentBuf).append(deCamelCaseify(identifier)).append(": ");
            if (object instanceof String) {
                buf.append("\"").append(sanitizeString((String) object)).append("\"");
            } else if (object instanceof byte[]) {
                appendQuotedBytes((byte[]) object, buf);
            } else {
                buf.append(object);
            }
            buf.append("\n");
        }
    }

    private static String deCamelCaseify(String identifier) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < identifier.length(); i++) {
            char currentChar = identifier.charAt(i);
            if (i == 0) {
                out.append(Character.toLowerCase(currentChar));
            } else if (Character.isUpperCase(currentChar)) {
                out.append('_').append(Character.toLowerCase(currentChar));
            } else {
                out.append(currentChar);
            }
        }
        return out.toString();
    }

    private static String sanitizeString(String str) {
        if (!str.startsWith("http") && str.length() > MAX_STRING_LEN) {
            str = str.substring(0, MAX_STRING_LEN) + "[...]";
        }
        return escapeString(str);
    }

    private static String escapeString(String str) {
        int strLen = str.length();
        StringBuilder b = new StringBuilder(strLen);
        for (int i = 0; i < strLen; i++) {
            char original = str.charAt(i);
            if (original < ' ' || original > '~' || original == '\"' || original == '\'') {
                b.append(String.format("\\u%04x", new Object[]{Integer.valueOf(original)}));
            } else {
                b.append(original);
            }
        }
        return b.toString();
    }

    private static void appendQuotedBytes(byte[] bytes, StringBuffer builder) {
        if (bytes == null) {
            builder.append("\"\"");
            return;
        }
        builder.append('\"');
        for (byte b : bytes) {
            int ch = b & Constants.BYTE_MASK;
            if (ch == 92 || ch == 34) {
                builder.append('\\').append((char) ch);
            } else if (ch < 32 || ch >= SupportedRates.MASK) {
                builder.append(String.format("\\%03o", new Object[]{Integer.valueOf(ch)}));
            } else {
                builder.append((char) ch);
            }
        }
        builder.append('\"');
    }
}

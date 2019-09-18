package com.android.internal.telephony.protobuf.nano;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

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
        Method[] methodArr;
        int i;
        Field[] fieldArr;
        Object obj = object;
        StringBuffer stringBuffer = indentBuf;
        StringBuffer stringBuffer2 = buf;
        if (obj != null) {
            if (obj instanceof MessageNano) {
                int origIndentBufLength = indentBuf.length();
                if (identifier != null) {
                    stringBuffer2.append(stringBuffer);
                    stringBuffer2.append(deCamelCaseify(identifier));
                    stringBuffer2.append(" <\n");
                    stringBuffer.append(INDENT);
                }
                Class<?> clazz = object.getClass();
                Field[] fields = clazz.getFields();
                int length = fields.length;
                int i2 = 0;
                while (i2 < length) {
                    Field field = fields[i2];
                    int modifiers = field.getModifiers();
                    String fieldName = field.getName();
                    if (!"cachedSize".equals(fieldName) && (modifiers & 1) == 1 && (modifiers & 8) != 8 && !fieldName.startsWith("_") && !fieldName.endsWith("_")) {
                        Class<?> fieldType = field.getType();
                        Object value = field.get(obj);
                        if (fieldType.isArray()) {
                            if (fieldType.getComponentType() != Byte.TYPE) {
                                int len = value == null ? 0 : Array.getLength(value);
                                int i3 = 0;
                                while (true) {
                                    fieldArr = fields;
                                    int i4 = i3;
                                    if (i4 >= len) {
                                        break;
                                    }
                                    print(fieldName, Array.get(value, i4), stringBuffer, stringBuffer2);
                                    i3 = i4 + 1;
                                    fields = fieldArr;
                                    length = length;
                                }
                            } else {
                                print(fieldName, value, stringBuffer, stringBuffer2);
                                fieldArr = fields;
                            }
                            i = length;
                        } else {
                            fieldArr = fields;
                            i = length;
                            print(fieldName, value, stringBuffer, stringBuffer2);
                        }
                    } else {
                        fieldArr = fields;
                        i = length;
                    }
                    i2++;
                    fields = fieldArr;
                    length = i;
                }
                Method[] methods = clazz.getMethods();
                int length2 = methods.length;
                int i5 = 0;
                while (i5 < length2) {
                    String name = methods[i5].getName();
                    if (name.startsWith("set")) {
                        String subfieldName = name.substring(3);
                        try {
                            try {
                                if (((Boolean) clazz.getMethod("has" + subfieldName, new Class[0]).invoke(obj, new Object[0])).booleanValue()) {
                                    try {
                                        methodArr = methods;
                                        try {
                                            print(subfieldName, clazz.getMethod("get" + subfieldName, new Class[0]).invoke(obj, new Object[0]), stringBuffer, stringBuffer2);
                                        } catch (NoSuchMethodException e) {
                                        }
                                    } catch (NoSuchMethodException e2) {
                                        methodArr = methods;
                                    }
                                    i5++;
                                    methods = methodArr;
                                }
                            } catch (NoSuchMethodException e3) {
                                methodArr = methods;
                            }
                        } catch (NoSuchMethodException e4) {
                            methodArr = methods;
                        }
                    }
                    methodArr = methods;
                    i5++;
                    methods = methodArr;
                }
                if (identifier != null) {
                    stringBuffer.setLength(origIndentBufLength);
                    stringBuffer2.append(stringBuffer);
                    stringBuffer2.append(">\n");
                }
            } else if (obj instanceof Map) {
                String identifier2 = deCamelCaseify(identifier);
                for (Map.Entry<?, ?> entry : ((Map) obj).entrySet()) {
                    stringBuffer2.append(stringBuffer);
                    stringBuffer2.append(identifier2);
                    stringBuffer2.append(" <\n");
                    int origIndentBufLength2 = indentBuf.length();
                    stringBuffer.append(INDENT);
                    print("key", entry.getKey(), stringBuffer, stringBuffer2);
                    print("value", entry.getValue(), stringBuffer, stringBuffer2);
                    stringBuffer.setLength(origIndentBufLength2);
                    stringBuffer2.append(stringBuffer);
                    stringBuffer2.append(">\n");
                }
                return;
            } else {
                String identifier3 = deCamelCaseify(identifier);
                stringBuffer2.append(stringBuffer);
                stringBuffer2.append(identifier3);
                stringBuffer2.append(": ");
                if (obj instanceof String) {
                    String stringMessage = sanitizeString((String) obj);
                    stringBuffer2.append("\"");
                    stringBuffer2.append(stringMessage);
                    stringBuffer2.append("\"");
                } else if (obj instanceof byte[]) {
                    appendQuotedBytes((byte[]) obj, stringBuffer2);
                } else {
                    stringBuffer2.append(obj);
                }
                stringBuffer2.append("\n");
                String str = identifier3;
                return;
            }
        }
        String str2 = identifier;
    }

    private static String deCamelCaseify(String identifier) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < identifier.length(); i++) {
            char currentChar = identifier.charAt(i);
            if (i == 0) {
                out.append(Character.toLowerCase(currentChar));
            } else if (Character.isUpperCase(currentChar)) {
                out.append('_');
                out.append(Character.toLowerCase(currentChar));
            } else {
                out.append(currentChar);
            }
        }
        return out.toString();
    }

    private static String sanitizeString(String str) {
        if (!str.startsWith("http") && str.length() > 200) {
            str = str.substring(0, 200) + "[...]";
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
            int ch = b & 255;
            if (ch == 92 || ch == 34) {
                builder.append('\\');
                builder.append((char) ch);
            } else if (ch < 32 || ch >= 127) {
                builder.append(String.format("\\%03o", new Object[]{Integer.valueOf(ch)}));
            } else {
                builder.append((char) ch);
            }
        }
        builder.append('\"');
    }
}

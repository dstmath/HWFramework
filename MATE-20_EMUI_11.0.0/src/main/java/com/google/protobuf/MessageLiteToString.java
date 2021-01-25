package com.google.protobuf;

import com.google.protobuf.GeneratedMessageLite;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/* access modifiers changed from: package-private */
public final class MessageLiteToString {
    private static final String BUILDER_LIST_SUFFIX = "OrBuilderList";
    private static final String BYTES_SUFFIX = "Bytes";
    private static final String LIST_SUFFIX = "List";

    MessageLiteToString() {
    }

    static String toString(MessageLite messageLite, String commentString) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("# ");
        buffer.append(commentString);
        reflectivePrintWithIndent(messageLite, buffer, 0);
        return buffer.toString();
    }

    private static void reflectivePrintWithIndent(MessageLite messageLite, StringBuilder buffer, int indent) {
        Map<String, Method> nameToNoArgMethod;
        boolean hasValue;
        Map<String, Method> nameToNoArgMethod2 = new HashMap<>();
        Map<String, Method> nameToMethod = new HashMap<>();
        Set<String> getters = new TreeSet<>();
        Method[] declaredMethods = messageLite.getClass().getDeclaredMethods();
        int i = 0;
        for (Method method : declaredMethods) {
            nameToMethod.put(method.getName(), method);
            if (method.getParameterTypes().length == 0) {
                nameToNoArgMethod2.put(method.getName(), method);
                if (method.getName().startsWith("get")) {
                    getters.add(method.getName());
                }
            }
        }
        for (String getter : getters) {
            String suffix = getter.replaceFirst("get", "");
            if (suffix.endsWith(LIST_SUFFIX) && !suffix.endsWith(BUILDER_LIST_SUFFIX)) {
                String camelCase = suffix.substring(i, 1).toLowerCase() + suffix.substring(1, suffix.length() - LIST_SUFFIX.length());
                Method listMethod = nameToNoArgMethod2.get("get" + suffix);
                if (listMethod != null) {
                    printField(buffer, indent, camelCaseToSnakeCase(camelCase), GeneratedMessageLite.invokeOrDie(listMethod, messageLite, new Object[i]));
                }
            }
            if (nameToMethod.get("set" + suffix) != null) {
                if (suffix.endsWith(BYTES_SUFFIX)) {
                    if (nameToNoArgMethod2.containsKey("get" + suffix.substring(i, suffix.length() - BYTES_SUFFIX.length()))) {
                    }
                }
                String camelCase2 = suffix.substring(i, 1).toLowerCase() + suffix.substring(1);
                Method getMethod = nameToNoArgMethod2.get("get" + suffix);
                Method hasMethod = nameToNoArgMethod2.get("has" + suffix);
                if (getMethod != null) {
                    Object value = GeneratedMessageLite.invokeOrDie(getMethod, messageLite, new Object[i]);
                    if (hasMethod == null) {
                        nameToNoArgMethod = nameToNoArgMethod2;
                        hasValue = !isDefaultValue(value) ? 1 : i;
                    } else {
                        nameToNoArgMethod = nameToNoArgMethod2;
                        hasValue = ((Boolean) GeneratedMessageLite.invokeOrDie(hasMethod, messageLite, new Object[i])).booleanValue();
                    }
                    if (hasValue != 0) {
                        printField(buffer, indent, camelCaseToSnakeCase(camelCase2), value);
                        nameToNoArgMethod2 = nameToNoArgMethod;
                        i = 0;
                    } else {
                        nameToNoArgMethod2 = nameToNoArgMethod;
                        i = 0;
                    }
                } else {
                    i = 0;
                }
            }
        }
        if (messageLite instanceof GeneratedMessageLite.ExtendableMessage) {
            Iterator<Map.Entry<GeneratedMessageLite.ExtensionDescriptor, Object>> iter = ((GeneratedMessageLite.ExtendableMessage) messageLite).extensions.iterator();
            while (iter.hasNext()) {
                Map.Entry<GeneratedMessageLite.ExtensionDescriptor, Object> entry = iter.next();
                printField(buffer, indent, "[" + entry.getKey().getNumber() + "]", entry.getValue());
            }
        }
        if (((GeneratedMessageLite) messageLite).unknownFields != null) {
            ((GeneratedMessageLite) messageLite).unknownFields.printWithIndent(buffer, indent);
        }
    }

    private static boolean isDefaultValue(Object o) {
        if (o instanceof Boolean) {
            return !((Boolean) o).booleanValue();
        }
        if (o instanceof Integer) {
            if (((Integer) o).intValue() == 0) {
                return true;
            }
            return false;
        } else if (o instanceof Float) {
            if (((Float) o).floatValue() == 0.0f) {
                return true;
            }
            return false;
        } else if (o instanceof Double) {
            if (((Double) o).doubleValue() == 0.0d) {
                return true;
            }
            return false;
        } else if (o instanceof String) {
            return o.equals("");
        } else {
            if (o instanceof ByteString) {
                return o.equals(ByteString.EMPTY);
            }
            if (o instanceof MessageLite) {
                if (o == ((MessageLite) o).getDefaultInstanceForType()) {
                    return true;
                }
                return false;
            } else if (!(o instanceof Enum)) {
                return false;
            } else {
                if (((Enum) o).ordinal() == 0) {
                    return true;
                }
                return false;
            }
        }
    }

    static final void printField(StringBuilder buffer, int indent, String name, Object object) {
        if (object instanceof List) {
            Iterator<?> it = ((List) object).iterator();
            while (it.hasNext()) {
                printField(buffer, indent, name, it.next());
            }
            return;
        }
        buffer.append('\n');
        for (int i = 0; i < indent; i++) {
            buffer.append(' ');
        }
        buffer.append(name);
        if (object instanceof String) {
            buffer.append(": \"");
            buffer.append(TextFormatEscaper.escapeText((String) object));
            buffer.append('\"');
        } else if (object instanceof ByteString) {
            buffer.append(": \"");
            buffer.append(TextFormatEscaper.escapeBytes((ByteString) object));
            buffer.append('\"');
        } else if (object instanceof GeneratedMessageLite) {
            buffer.append(" {");
            reflectivePrintWithIndent((GeneratedMessageLite) object, buffer, indent + 2);
            buffer.append("\n");
            for (int i2 = 0; i2 < indent; i2++) {
                buffer.append(' ');
            }
            buffer.append("}");
        } else {
            buffer.append(": ");
            buffer.append(object.toString());
        }
    }

    private static final String camelCaseToSnakeCase(String camelCase) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                builder.append("_");
            }
            builder.append(Character.toLowerCase(ch));
        }
        return builder.toString();
    }
}

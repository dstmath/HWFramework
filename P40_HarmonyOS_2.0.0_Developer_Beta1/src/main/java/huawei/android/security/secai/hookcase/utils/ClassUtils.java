package huawei.android.security.secai.hookcase.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class ClassUtils {
    private static final Map<String, String> ABBREVIATION_MAP = new HashMap(16);
    private static final int DEFAULT_CAPACITY = 16;
    public static final String INNER_CLASS_SEPARATOR = String.valueOf((char) INNER_CLASS_SEPARATOR_CHAR);
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
    public static final String PACKAGE_SEPARATOR = String.valueOf((char) PACKAGE_SEPARATOR_CHAR);
    public static final char PACKAGE_SEPARATOR_CHAR = '.';
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap(16);
    private static final Map<String, String> REVERSE_ABBREVIATION_MAP = new HashMap(16);
    private static final int TWO = 2;
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP = new HashMap(16);

    static {
        PRIMITIVE_WRAPPER_MAP.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(Byte.TYPE, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(Character.TYPE, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(Short.TYPE, Short.class);
        PRIMITIVE_WRAPPER_MAP.put(Integer.TYPE, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(Long.TYPE, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(Double.TYPE, Double.class);
        PRIMITIVE_WRAPPER_MAP.put(Float.TYPE, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(Void.TYPE, Void.TYPE);
        for (Map.Entry<Class<?>, Class<?>> entry : PRIMITIVE_WRAPPER_MAP.entrySet()) {
            if (!entry.getKey().equals(entry.getValue())) {
                WRAPPER_PRIMITIVE_MAP.put(entry.getValue(), entry.getKey());
            }
        }
        addAbbreviation("int", "I");
        addAbbreviation("boolean", "Z");
        addAbbreviation("float", "F");
        addAbbreviation("long", "J");
        addAbbreviation("short", "S");
        addAbbreviation("byte", "B");
        addAbbreviation("double", "D");
        addAbbreviation("char", "C");
    }

    private static void addAbbreviation(String primitive, String abbreviation) {
        ABBREVIATION_MAP.put(primitive, abbreviation);
        REVERSE_ABBREVIATION_MAP.put(abbreviation, primitive);
    }

    private static String toCanonicalName(String className) {
        if (className != null) {
            String classCanonicalName = className.trim();
            if (classCanonicalName.length() == 0) {
                throw new NullPointerException("className must not be null.");
            } else if (!classCanonicalName.endsWith("[]")) {
                return classCanonicalName;
            } else {
                StringBuilder classNameBuffer = new StringBuilder(16);
                while (classCanonicalName.endsWith("[]")) {
                    classCanonicalName = classCanonicalName.substring(0, classCanonicalName.length() - 2);
                    classNameBuffer.append('[');
                }
                String abbreviation = ABBREVIATION_MAP.get(classCanonicalName);
                if (abbreviation != null) {
                    classNameBuffer.append(abbreviation);
                } else {
                    classNameBuffer.append('L');
                    classNameBuffer.append(classCanonicalName);
                    classNameBuffer.append(';');
                }
                return classNameBuffer.toString();
            }
        } else {
            throw new NullPointerException("className must not be null.");
        }
    }

    private static Class<?> getClass(ClassLoader classLoader, String className, boolean isInitialize) throws ClassNotFoundException {
        try {
            if (!ABBREVIATION_MAP.containsKey(className)) {
                return Class.forName(toCanonicalName(className), isInitialize, classLoader);
            }
            return Class.forName("[" + ABBREVIATION_MAP.get(className), isInitialize, classLoader).getComponentType();
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = className.lastIndexOf(46);
            if (lastDotIndex != -1) {
                return getClass(classLoader, className.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR_CHAR + className.substring(lastDotIndex + 1), isInitialize);
            }
            throw ex;
        }
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        ClassLoader currentClassLoader = classLoader;
        if (currentClassLoader == null) {
            currentClassLoader = Thread.currentThread().getContextClassLoader();
        }
        return findClass(className, currentClassLoader, true);
    }

    public static Class<?> findClass(String className, ClassLoader classLoader, boolean isInitialize) throws ClassNotFoundException {
        return getClass(classLoader, className, isInitialize);
    }
}

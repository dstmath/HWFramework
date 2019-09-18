package org.xml.sax.helpers;

import java.lang.reflect.InvocationTargetException;

class NewInstance {
    NewInstance() {
    }

    static Object newInstance(ClassLoader classLoader, String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class driverClass;
        if (classLoader == null) {
            driverClass = Class.forName(className);
        } else {
            driverClass = classLoader.loadClass(className);
        }
        return driverClass.newInstance();
    }

    static ClassLoader getClassLoader() {
        try {
            try {
                return (ClassLoader) Thread.class.getMethod("getContextClassLoader", new Class[0]).invoke(Thread.currentThread(), new Object[0]);
            } catch (IllegalAccessException e) {
                throw new UnknownError(e.getMessage());
            } catch (InvocationTargetException e2) {
                throw new UnknownError(e2.getMessage());
            }
        } catch (NoSuchMethodException e3) {
            return NewInstance.class.getClassLoader();
        }
    }
}

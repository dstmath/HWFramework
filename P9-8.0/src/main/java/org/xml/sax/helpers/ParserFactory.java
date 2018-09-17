package org.xml.sax.helpers;

import org.xml.sax.Parser;

@Deprecated
public class ParserFactory {
    private ParserFactory() {
    }

    public static Parser makeParser() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NullPointerException, ClassCastException {
        String className = System.getProperty("org.xml.sax.parser");
        if (className != null) {
            return makeParser(className);
        }
        throw new NullPointerException("No value for sax.parser property");
    }

    public static Parser makeParser(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {
        return (Parser) NewInstance.newInstance(NewInstance.getClassLoader(), className);
    }
}

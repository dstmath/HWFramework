package ohos.org.xml.sax.helpers;

import ohos.org.xml.sax.Parser;

public class ParserFactory {
    private static SecuritySupport ss = new SecuritySupport();

    private ParserFactory() {
    }

    public static Parser makeParser() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NullPointerException, ClassCastException {
        String systemProperty = ss.getSystemProperty("org.xml.sax.parser");
        if (systemProperty != null) {
            return makeParser(systemProperty);
        }
        throw new NullPointerException("No value for sax.parser property");
    }

    public static Parser makeParser(String str) throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {
        return (Parser) NewInstance.newInstance(ss.getContextClassLoader(), str);
    }
}

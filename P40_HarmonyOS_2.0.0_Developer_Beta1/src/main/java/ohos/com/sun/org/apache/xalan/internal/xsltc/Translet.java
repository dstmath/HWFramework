package ohos.com.sun.org.apache.xalan.internal.xsltc;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;

public interface Translet {
    void addAuxiliaryClass(Class cls);

    Object addParameter(String str, Object obj);

    void buildKeys(DOM dom, DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler, int i) throws TransletException;

    Class getAuxiliaryClass(String str);

    String[] getNamesArray();

    String[] getNamespaceArray();

    int[] getTypesArray();

    String[] getUrisArray();

    boolean overrideDefaultParser();

    void setOverrideDefaultParser(boolean z);

    void transform(DOM dom, DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler) throws TransletException;

    void transform(DOM dom, SerializationHandler serializationHandler) throws TransletException;

    void transform(DOM dom, SerializationHandler[] serializationHandlerArr) throws TransletException;
}

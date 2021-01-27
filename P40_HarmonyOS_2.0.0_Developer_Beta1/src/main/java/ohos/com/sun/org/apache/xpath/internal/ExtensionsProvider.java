package ohos.com.sun.org.apache.xpath.internal;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncExtFunction;
import ohos.javax.xml.transform.TransformerException;

public interface ExtensionsProvider {
    boolean elementAvailable(String str, String str2) throws TransformerException;

    Object extFunction(String str, String str2, Vector vector, Object obj) throws TransformerException;

    Object extFunction(FuncExtFunction funcExtFunction, Vector vector) throws TransformerException;

    boolean functionAvailable(String str, String str2) throws TransformerException;
}

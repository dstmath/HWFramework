package org.apache.xpath;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xpath.functions.FuncExtFunction;

public interface ExtensionsProvider {
    boolean elementAvailable(String str, String str2) throws TransformerException;

    Object extFunction(String str, String str2, Vector vector, Object obj) throws TransformerException;

    Object extFunction(FuncExtFunction funcExtFunction, Vector vector) throws TransformerException;

    boolean functionAvailable(String str, String str2) throws TransformerException;
}

package org.xmlpull.v1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public interface XmlSerializer {
    XmlSerializer attribute(String str, String str2, String str3) throws IOException, IllegalArgumentException, IllegalStateException;

    void cdsect(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void comment(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void docdecl(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void endDocument() throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer endTag(String str, String str2) throws IOException, IllegalArgumentException, IllegalStateException;

    void entityRef(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void flush() throws IOException;

    int getDepth();

    boolean getFeature(String str);

    String getName();

    String getNamespace();

    String getPrefix(String str, boolean z) throws IllegalArgumentException;

    Object getProperty(String str);

    void ignorableWhitespace(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void processingInstruction(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void setFeature(String str, boolean z) throws IllegalArgumentException, IllegalStateException;

    void setOutput(OutputStream outputStream, String str) throws IOException, IllegalArgumentException, IllegalStateException;

    void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException;

    void setPrefix(String str, String str2) throws IOException, IllegalArgumentException, IllegalStateException;

    void setProperty(String str, Object obj) throws IllegalArgumentException, IllegalStateException;

    void startDocument(String str, Boolean bool) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer startTag(String str, String str2) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer text(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer text(char[] cArr, int i, int i2) throws IOException, IllegalArgumentException, IllegalStateException;
}

package ohos.com.sun.org.apache.xerces.internal.xinclude;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;

public interface XPointerSchema extends XMLComponent, XMLDocumentFilter {
    Object getParent();

    String getXPointerSchemaPointer();

    String getXpointerSchemaName();

    boolean isSubResourceIndentified();

    void reset();

    void setParent(Object obj);

    void setXPointerSchemaName(String str);

    void setXPointerSchemaPointer(String str);
}

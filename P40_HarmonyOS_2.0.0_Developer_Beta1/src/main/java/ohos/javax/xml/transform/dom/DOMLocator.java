package ohos.javax.xml.transform.dom;

import ohos.javax.xml.transform.SourceLocator;
import ohos.org.w3c.dom.Node;

public interface DOMLocator extends SourceLocator {
    Node getOriginatingNode();
}

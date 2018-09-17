package javax.xml.transform.dom;

import javax.xml.transform.SourceLocator;
import org.w3c.dom.Node;

public interface DOMLocator extends SourceLocator {
    Node getOriginatingNode();
}

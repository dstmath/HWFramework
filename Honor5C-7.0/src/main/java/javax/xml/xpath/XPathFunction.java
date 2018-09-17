package javax.xml.xpath;

import java.util.List;

public interface XPathFunction {
    Object evaluate(List list) throws XPathFunctionException;
}

package org.apache.xpath.axes;

import org.apache.xpath.XPathContext;

public interface SubContextList {
    int getLastPos(XPathContext xPathContext);

    int getProximityPosition(XPathContext xPathContext);
}

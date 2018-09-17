package org.w3c.dom.ls;

import org.w3c.dom.traversal.NodeFilter;

public interface LSSerializerFilter extends NodeFilter {
    int getWhatToShow();
}

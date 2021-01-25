package ohos.org.w3c.dom.ls;

import ohos.org.w3c.dom.traversal.NodeFilter;

public interface LSSerializerFilter extends NodeFilter {
    int getWhatToShow();
}

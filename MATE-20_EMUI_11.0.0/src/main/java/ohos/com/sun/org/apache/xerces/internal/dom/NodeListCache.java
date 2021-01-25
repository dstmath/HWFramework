package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.Serializable;

/* access modifiers changed from: package-private */
public class NodeListCache implements Serializable {
    private static final long serialVersionUID = -7927529254918631002L;
    ChildNode fChild;
    int fChildIndex = -1;
    int fLength = -1;
    ParentNode fOwner;
    NodeListCache next;

    NodeListCache(ParentNode parentNode) {
        this.fOwner = parentNode;
    }
}

package ohos.org.w3c.dom.events;

import ohos.org.w3c.dom.Node;

public interface MutationEvent extends Event {
    public static final short ADDITION = 2;
    public static final short MODIFICATION = 1;
    public static final short REMOVAL = 3;

    short getAttrChange();

    String getAttrName();

    String getNewValue();

    String getPrevValue();

    Node getRelatedNode();

    void initMutationEvent(String str, boolean z, boolean z2, Node node, String str2, String str3, String str4, short s);
}

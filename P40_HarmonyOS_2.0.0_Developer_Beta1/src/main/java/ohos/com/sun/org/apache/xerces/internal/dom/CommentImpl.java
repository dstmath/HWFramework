package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.org.w3c.dom.CharacterData;
import ohos.org.w3c.dom.Comment;

public class CommentImpl extends CharacterDataImpl implements CharacterData, Comment {
    static final long serialVersionUID = -2685736833408134044L;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeName() {
        return PsuedoNames.PSEUDONAME_COMMENT;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 8;
    }

    public CommentImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl, str);
    }
}

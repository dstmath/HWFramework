package android.net.wifi.hotspot2.omadm;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XMLNode {
    private final List<XMLNode> mChildren = new ArrayList();
    private final XMLNode mParent;
    private final String mTag;
    private String mText = null;
    private StringBuilder mTextBuilder = new StringBuilder();

    public XMLNode(XMLNode parent, String tag) {
        this.mTag = tag;
        this.mParent = parent;
    }

    public void addText(String text) {
        this.mTextBuilder.append(text);
    }

    public void addChild(XMLNode child) {
        this.mChildren.add(child);
    }

    public void close() {
        this.mText = this.mTextBuilder.toString().trim();
        this.mTextBuilder = null;
    }

    public String getTag() {
        return this.mTag;
    }

    public XMLNode getParent() {
        return this.mParent;
    }

    public String getText() {
        return this.mText;
    }

    public List<XMLNode> getChildren() {
        return this.mChildren;
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof XMLNode)) {
            return false;
        }
        XMLNode that = (XMLNode) thatObject;
        if (TextUtils.equals(this.mTag, that.mTag) && TextUtils.equals(this.mText, that.mText)) {
            z = this.mChildren.equals(that.mChildren);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mTag, this.mText, this.mChildren});
    }
}

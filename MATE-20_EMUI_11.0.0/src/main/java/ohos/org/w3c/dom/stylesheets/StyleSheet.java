package ohos.org.w3c.dom.stylesheets;

import ohos.org.w3c.dom.Node;

public interface StyleSheet {
    boolean getDisabled();

    String getHref();

    MediaList getMedia();

    Node getOwnerNode();

    StyleSheet getParentStyleSheet();

    String getTitle();

    String getType();

    void setDisabled(boolean z);
}

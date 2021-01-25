package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.stylesheets.MediaList;

public interface CSSImportRule extends CSSRule {
    String getHref();

    MediaList getMedia();

    CSSStyleSheet getStyleSheet();
}

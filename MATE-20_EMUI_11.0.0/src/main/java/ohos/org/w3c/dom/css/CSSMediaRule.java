package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.stylesheets.MediaList;

public interface CSSMediaRule extends CSSRule {
    void deleteRule(int i) throws DOMException;

    CSSRuleList getCssRules();

    MediaList getMedia();

    int insertRule(String str, int i) throws DOMException;
}

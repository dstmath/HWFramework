package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.stylesheets.StyleSheet;

public interface CSSStyleSheet extends StyleSheet {
    void deleteRule(int i) throws DOMException;

    CSSRuleList getCssRules();

    CSSRule getOwnerRule();

    int insertRule(String str, int i) throws DOMException;
}

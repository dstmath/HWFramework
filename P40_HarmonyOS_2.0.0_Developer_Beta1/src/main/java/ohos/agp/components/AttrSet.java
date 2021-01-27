package ohos.agp.components;

import java.util.Optional;

public interface AttrSet {
    public static final String STYLE_ATTR = "style";

    Optional<Attr> getAttr(int i);

    Optional<Attr> getAttr(String str);

    int getLength();

    Optional<String> getStyle();
}

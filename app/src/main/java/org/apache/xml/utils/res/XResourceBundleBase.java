package org.apache.xml.utils.res;

import java.util.ListResourceBundle;

public abstract class XResourceBundleBase extends ListResourceBundle {
    public abstract String getMessageKey(int i);

    public abstract String getWarningKey(int i);
}

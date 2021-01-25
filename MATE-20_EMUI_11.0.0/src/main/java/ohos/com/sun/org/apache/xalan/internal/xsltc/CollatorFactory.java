package ohos.com.sun.org.apache.xalan.internal.xsltc;

import java.text.Collator;
import java.util.Locale;

public interface CollatorFactory {
    Collator getCollator(String str, String str2);

    Collator getCollator(Locale locale);
}

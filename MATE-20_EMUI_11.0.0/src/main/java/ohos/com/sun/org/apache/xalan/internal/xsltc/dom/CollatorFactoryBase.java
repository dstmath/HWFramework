package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.text.Collator;
import java.util.Locale;
import ohos.com.sun.org.apache.xalan.internal.xsltc.CollatorFactory;

public class CollatorFactoryBase implements CollatorFactory {
    public static final Collator DEFAULT_COLLATOR = Collator.getInstance();
    public static final Locale DEFAULT_LOCALE = Locale.getDefault();

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.CollatorFactory
    public Collator getCollator(String str, String str2) {
        return Collator.getInstance(new Locale(str, str2));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.CollatorFactory
    public Collator getCollator(Locale locale) {
        if (locale == DEFAULT_LOCALE) {
            return DEFAULT_COLLATOR;
        }
        return Collator.getInstance(locale);
    }
}

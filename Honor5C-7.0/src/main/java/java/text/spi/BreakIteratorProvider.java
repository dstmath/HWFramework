package java.text.spi;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public abstract class BreakIteratorProvider extends LocaleServiceProvider {
    public abstract BreakIterator getCharacterInstance(Locale locale);

    public abstract BreakIterator getLineInstance(Locale locale);

    public abstract BreakIterator getSentenceInstance(Locale locale);

    public abstract BreakIterator getWordInstance(Locale locale);

    protected BreakIteratorProvider() {
    }
}

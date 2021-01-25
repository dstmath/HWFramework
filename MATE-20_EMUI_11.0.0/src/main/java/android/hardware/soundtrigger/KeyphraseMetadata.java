package android.hardware.soundtrigger;

import android.util.ArraySet;
import java.util.Locale;

public class KeyphraseMetadata {
    public final int id;
    public final String keyphrase;
    public final int recognitionModeFlags;
    public final ArraySet<Locale> supportedLocales;

    public KeyphraseMetadata(int id2, String keyphrase2, ArraySet<Locale> supportedLocales2, int recognitionModeFlags2) {
        this.id = id2;
        this.keyphrase = keyphrase2;
        this.supportedLocales = supportedLocales2;
        this.recognitionModeFlags = recognitionModeFlags2;
    }

    public String toString() {
        return "id=" + this.id + ", keyphrase=" + this.keyphrase + ", supported-locales=" + this.supportedLocales + ", recognition-modes=" + this.recognitionModeFlags;
    }

    public boolean supportsPhrase(String phrase) {
        return this.keyphrase.isEmpty() || this.keyphrase.equalsIgnoreCase(phrase);
    }

    public boolean supportsLocale(Locale locale) {
        return this.supportedLocales.isEmpty() || this.supportedLocales.contains(locale);
    }
}

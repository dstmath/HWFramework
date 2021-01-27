package ohos.global.icu.text;

import java.text.CharacterIterator;
import ohos.global.icu.text.DictionaryBreakEngine;

/* access modifiers changed from: package-private */
public interface LanguageBreakEngine {
    int findBreaks(CharacterIterator characterIterator, int i, int i2, DictionaryBreakEngine.DequeI dequeI);

    boolean handles(int i);
}

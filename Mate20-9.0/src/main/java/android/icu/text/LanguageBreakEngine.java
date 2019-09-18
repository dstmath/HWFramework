package android.icu.text;

import android.icu.text.DictionaryBreakEngine;
import java.text.CharacterIterator;

interface LanguageBreakEngine {
    int findBreaks(CharacterIterator characterIterator, int i, int i2, int i3, DictionaryBreakEngine.DequeI dequeI);

    boolean handles(int i, int i2);
}

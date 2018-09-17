package android.icu.text;

import java.text.CharacterIterator;

interface LanguageBreakEngine {
    int findBreaks(CharacterIterator characterIterator, int i, int i2, boolean z, int i3, DequeI dequeI);

    boolean handles(int i, int i2);
}

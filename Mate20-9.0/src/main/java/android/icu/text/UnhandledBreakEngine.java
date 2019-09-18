package android.icu.text;

import android.icu.impl.CharacterIteration;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.DictionaryBreakEngine;
import java.text.CharacterIterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

final class UnhandledBreakEngine implements LanguageBreakEngine {
    final AtomicReferenceArray<UnicodeSet> fHandled = new AtomicReferenceArray<>(5);

    public UnhandledBreakEngine() {
        for (int i = 0; i < this.fHandled.length(); i++) {
            this.fHandled.set(i, new UnicodeSet());
        }
    }

    public boolean handles(int c, int breakType) {
        return breakType >= 0 && breakType < this.fHandled.length() && this.fHandled.get(breakType).contains(c);
    }

    public int findBreaks(CharacterIterator text, int startPos, int endPos, int breakType, DictionaryBreakEngine.DequeI foundBreaks) {
        if (breakType >= 0 && breakType < this.fHandled.length()) {
            UnicodeSet uniset = this.fHandled.get(breakType);
            int c = CharacterIteration.current32(text);
            while (text.getIndex() < endPos && uniset.contains(c)) {
                CharacterIteration.next32(text);
                c = CharacterIteration.current32(text);
            }
        }
        return 0;
    }

    public void handleChar(int c, int breakType) {
        if (breakType >= 0 && breakType < this.fHandled.length() && c != Integer.MAX_VALUE) {
            UnicodeSet originalSet = this.fHandled.get(breakType);
            if (!originalSet.contains(c)) {
                int script = UCharacter.getIntPropertyValue(c, UProperty.SCRIPT);
                UnicodeSet newSet = new UnicodeSet();
                newSet.applyIntPropertyValue(UProperty.SCRIPT, script);
                newSet.addAll(originalSet);
                this.fHandled.set(breakType, newSet);
            }
        }
    }
}

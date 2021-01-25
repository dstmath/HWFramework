package ohos.global.icu.text;

import java.text.CharacterIterator;
import ohos.global.icu.impl.CharacterIteration;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.lang.UProperty;
import ohos.global.icu.text.DictionaryBreakEngine;

/* access modifiers changed from: package-private */
public final class UnhandledBreakEngine implements LanguageBreakEngine {
    volatile UnicodeSet fHandled = new UnicodeSet();

    @Override // ohos.global.icu.text.LanguageBreakEngine
    public boolean handles(int i) {
        return this.fHandled.contains(i);
    }

    @Override // ohos.global.icu.text.LanguageBreakEngine
    public int findBreaks(CharacterIterator characterIterator, int i, int i2, DictionaryBreakEngine.DequeI dequeI) {
        UnicodeSet unicodeSet = this.fHandled;
        int current32 = CharacterIteration.current32(characterIterator);
        while (characterIterator.getIndex() < i2 && unicodeSet.contains(current32)) {
            CharacterIteration.next32(characterIterator);
            current32 = CharacterIteration.current32(characterIterator);
        }
        return 0;
    }

    public void handleChar(int i) {
        UnicodeSet unicodeSet = this.fHandled;
        if (!unicodeSet.contains(i)) {
            int intPropertyValue = UCharacter.getIntPropertyValue(i, UProperty.SCRIPT);
            UnicodeSet unicodeSet2 = new UnicodeSet();
            unicodeSet2.applyIntPropertyValue(UProperty.SCRIPT, intPropertyValue);
            unicodeSet2.addAll(unicodeSet);
            this.fHandled = unicodeSet2;
        }
    }
}

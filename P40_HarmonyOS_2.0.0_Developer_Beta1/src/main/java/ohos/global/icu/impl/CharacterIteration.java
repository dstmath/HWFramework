package ohos.global.icu.impl;

import java.text.CharacterIterator;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.UTF16;

public final class CharacterIteration {
    public static final int DONE32 = Integer.MAX_VALUE;

    private CharacterIteration() {
    }

    public static int next32(CharacterIterator characterIterator) {
        char next;
        char current = characterIterator.current();
        if (current >= 55296 && current <= 56319 && ((next = characterIterator.next()) < 56320 || next > 57343)) {
            characterIterator.previous();
        }
        int next2 = characterIterator.next();
        if (next2 >= 55296) {
            next2 = nextTrail32(characterIterator, next2);
        }
        if (next2 >= 65536 && next2 != Integer.MAX_VALUE) {
            characterIterator.previous();
        }
        return next2;
    }

    public static int nextTrail32(CharacterIterator characterIterator, int i) {
        if (i == 65535 && characterIterator.getIndex() >= characterIterator.getEndIndex()) {
            return Integer.MAX_VALUE;
        }
        if (i > 56319) {
            return i;
        }
        char next = characterIterator.next();
        if (UTF16.isTrailSurrogate(next)) {
            return 65536 + ((i - 55296) << 10) + (next - 56320);
        }
        characterIterator.previous();
        return i;
    }

    public static int previous32(CharacterIterator characterIterator) {
        if (characterIterator.getIndex() <= characterIterator.getBeginIndex()) {
            return Integer.MAX_VALUE;
        }
        char previous = characterIterator.previous();
        if (!UTF16.isTrailSurrogate(previous) || characterIterator.getIndex() <= characterIterator.getBeginIndex()) {
            return previous;
        }
        char previous2 = characterIterator.previous();
        if (UTF16.isLeadSurrogate(previous2)) {
            return 65536 + ((previous2 - 55296) << 10) + (previous - UCharacter.MIN_LOW_SURROGATE);
        }
        characterIterator.next();
        return previous;
    }

    public static int current32(CharacterIterator characterIterator) {
        char current = characterIterator.current();
        if (current < 55296) {
            return current;
        }
        if (UTF16.isLeadSurrogate(current)) {
            char next = characterIterator.next();
            characterIterator.previous();
            if (UTF16.isTrailSurrogate((char) next)) {
                return 65536 + ((current - 55296) << 10) + (next - UCharacter.MIN_LOW_SURROGATE);
            }
            return current;
        } else if (current != 65535 || characterIterator.getIndex() < characterIterator.getEndIndex()) {
            return current;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}

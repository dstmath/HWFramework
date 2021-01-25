package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

import java.text.CharacterIterator;
import ohos.global.icu.text.PluralRules;

public class Match implements Cloneable {
    int[] beginpos = null;
    char[] charSource = null;
    CharacterIterator ciSource = null;
    int[] endpos = null;
    int nofgroups = 0;
    String strSource = null;

    @Override // java.lang.Object
    public synchronized Object clone() {
        Match match;
        match = new Match();
        if (this.nofgroups > 0) {
            match.setNumberOfGroups(this.nofgroups);
            if (this.ciSource != null) {
                match.setSource(this.ciSource);
            }
            if (this.strSource != null) {
                match.setSource(this.strSource);
            }
            for (int i = 0; i < this.nofgroups; i++) {
                match.setBeginning(i, getBeginning(i));
                match.setEnd(i, getEnd(i));
            }
        }
        return match;
    }

    /* access modifiers changed from: protected */
    public void setNumberOfGroups(int i) {
        int i2 = this.nofgroups;
        this.nofgroups = i;
        if (i2 <= 0 || i2 < i || i * 2 < i2) {
            this.beginpos = new int[i];
            this.endpos = new int[i];
        }
        for (int i3 = 0; i3 < i; i3++) {
            this.beginpos[i3] = -1;
            this.endpos[i3] = -1;
        }
    }

    /* access modifiers changed from: protected */
    public void setSource(CharacterIterator characterIterator) {
        this.ciSource = characterIterator;
        this.strSource = null;
        this.charSource = null;
    }

    /* access modifiers changed from: protected */
    public void setSource(String str) {
        this.ciSource = null;
        this.strSource = str;
        this.charSource = null;
    }

    /* access modifiers changed from: protected */
    public void setSource(char[] cArr) {
        this.ciSource = null;
        this.strSource = null;
        this.charSource = cArr;
    }

    /* access modifiers changed from: protected */
    public void setBeginning(int i, int i2) {
        this.beginpos[i] = i2;
    }

    /* access modifiers changed from: protected */
    public void setEnd(int i, int i2) {
        this.endpos[i] = i2;
    }

    public int getNumberOfGroups() {
        int i = this.nofgroups;
        if (i > 0) {
            return i;
        }
        throw new IllegalStateException("A result is not set.");
    }

    public int getBeginning(int i) {
        int[] iArr = this.beginpos;
        if (iArr == null) {
            throw new IllegalStateException("A result is not set.");
        } else if (i >= 0 && this.nofgroups > i) {
            return iArr[i];
        } else {
            throw new IllegalArgumentException("The parameter must be less than " + this.nofgroups + PluralRules.KEYWORD_RULE_SEPARATOR + i);
        }
    }

    public int getEnd(int i) {
        int[] iArr = this.endpos;
        if (iArr == null) {
            throw new IllegalStateException("A result is not set.");
        } else if (i >= 0 && this.nofgroups > i) {
            return iArr[i];
        } else {
            throw new IllegalArgumentException("The parameter must be less than " + this.nofgroups + PluralRules.KEYWORD_RULE_SEPARATOR + i);
        }
    }

    public String getCapturedText(int i) {
        int[] iArr = this.beginpos;
        if (iArr == null) {
            throw new IllegalStateException("match() has never been called.");
        } else if (i < 0 || this.nofgroups <= i) {
            throw new IllegalArgumentException("The parameter must be less than " + this.nofgroups + PluralRules.KEYWORD_RULE_SEPARATOR + i);
        } else {
            int i2 = iArr[i];
            int i3 = this.endpos[i];
            if (i2 < 0 || i3 < 0) {
                return null;
            }
            CharacterIterator characterIterator = this.ciSource;
            if (characterIterator != null) {
                return REUtil.substring(characterIterator, i2, i3);
            }
            String str = this.strSource;
            if (str != null) {
                return str.substring(i2, i3);
            }
            return new String(this.charSource, i2, i3 - i2);
        }
    }
}

package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

public class XMLGrammarPoolImpl implements XMLGrammarPool {
    private static final boolean DEBUG = false;
    protected static final int TABLE_SIZE = 11;
    protected int fGrammarCount;
    protected Entry[] fGrammars;
    protected boolean fPoolIsLocked;

    public XMLGrammarPoolImpl() {
        this.fGrammars = null;
        this.fGrammarCount = 0;
        this.fGrammars = new Entry[11];
        this.fPoolIsLocked = false;
    }

    public XMLGrammarPoolImpl(int i) {
        this.fGrammars = null;
        this.fGrammarCount = 0;
        this.fGrammars = new Entry[i];
        this.fPoolIsLocked = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar[] retrieveInitialGrammarSet(String str) {
        Grammar[] grammarArr;
        synchronized (this.fGrammars) {
            int length = this.fGrammars.length;
            Grammar[] grammarArr2 = new Grammar[this.fGrammarCount];
            int i = 0;
            for (int i2 = 0; i2 < length; i2++) {
                for (Entry entry = this.fGrammars[i2]; entry != null; entry = entry.next) {
                    if (entry.desc.getGrammarType().equals(str)) {
                        grammarArr2[i] = entry.grammar;
                        i++;
                    }
                }
            }
            grammarArr = new Grammar[i];
            System.arraycopy(grammarArr2, 0, grammarArr, 0, i);
        }
        return grammarArr;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void cacheGrammars(String str, Grammar[] grammarArr) {
        if (!this.fPoolIsLocked) {
            for (Grammar grammar : grammarArr) {
                putGrammar(grammar);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
        return getGrammar(xMLGrammarDescription);
    }

    public void putGrammar(Grammar grammar) {
        if (!this.fPoolIsLocked) {
            synchronized (this.fGrammars) {
                XMLGrammarDescription grammarDescription = grammar.getGrammarDescription();
                int hashCode = hashCode(grammarDescription);
                int length = (Integer.MAX_VALUE & hashCode) % this.fGrammars.length;
                for (Entry entry = this.fGrammars[length]; entry != null; entry = entry.next) {
                    if (entry.hash == hashCode && equals(entry.desc, grammarDescription)) {
                        entry.grammar = grammar;
                        return;
                    }
                }
                this.fGrammars[length] = new Entry(hashCode, grammarDescription, grammar, this.fGrammars[length]);
                this.fGrammarCount++;
            }
        }
    }

    public Grammar getGrammar(XMLGrammarDescription xMLGrammarDescription) {
        synchronized (this.fGrammars) {
            int hashCode = hashCode(xMLGrammarDescription);
            for (Entry entry = this.fGrammars[(Integer.MAX_VALUE & hashCode) % this.fGrammars.length]; entry != null; entry = entry.next) {
                if (entry.hash == hashCode && equals(entry.desc, xMLGrammarDescription)) {
                    return entry.grammar;
                }
            }
            return null;
        }
    }

    public Grammar removeGrammar(XMLGrammarDescription xMLGrammarDescription) {
        synchronized (this.fGrammars) {
            int hashCode = hashCode(xMLGrammarDescription);
            int length = (Integer.MAX_VALUE & hashCode) % this.fGrammars.length;
            Entry entry = null;
            for (Entry entry2 = this.fGrammars[length]; entry2 != null; entry2 = entry2.next) {
                if (entry2.hash != hashCode || !equals(entry2.desc, xMLGrammarDescription)) {
                    entry = entry2;
                } else {
                    if (entry != null) {
                        entry.next = entry2.next;
                    } else {
                        this.fGrammars[length] = entry2.next;
                    }
                    Grammar grammar = entry2.grammar;
                    entry2.grammar = null;
                    this.fGrammarCount--;
                    return grammar;
                }
            }
            return null;
        }
    }

    public boolean containsGrammar(XMLGrammarDescription xMLGrammarDescription) {
        synchronized (this.fGrammars) {
            int hashCode = hashCode(xMLGrammarDescription);
            for (Entry entry = this.fGrammars[(Integer.MAX_VALUE & hashCode) % this.fGrammars.length]; entry != null; entry = entry.next) {
                if (entry.hash == hashCode && equals(entry.desc, xMLGrammarDescription)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void lockPool() {
        this.fPoolIsLocked = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void unlockPool() {
        this.fPoolIsLocked = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void clear() {
        int i = 0;
        while (true) {
            Entry[] entryArr = this.fGrammars;
            if (i < entryArr.length) {
                if (entryArr[i] != null) {
                    entryArr[i].clear();
                    this.fGrammars[i] = null;
                }
                i++;
            } else {
                this.fGrammarCount = 0;
                return;
            }
        }
    }

    public boolean equals(XMLGrammarDescription xMLGrammarDescription, XMLGrammarDescription xMLGrammarDescription2) {
        return xMLGrammarDescription.equals(xMLGrammarDescription2);
    }

    public int hashCode(XMLGrammarDescription xMLGrammarDescription) {
        return xMLGrammarDescription.hashCode();
    }

    /* access modifiers changed from: protected */
    public static final class Entry {
        public XMLGrammarDescription desc;
        public Grammar grammar;
        public int hash;
        public Entry next;

        protected Entry(int i, XMLGrammarDescription xMLGrammarDescription, Grammar grammar2, Entry entry) {
            this.hash = i;
            this.desc = xMLGrammarDescription;
            this.grammar = grammar2;
            this.next = entry;
        }

        /* access modifiers changed from: protected */
        public void clear() {
            this.desc = null;
            this.grammar = null;
            Entry entry = this.next;
            if (entry != null) {
                entry.clear();
                this.next = null;
            }
        }
    }
}

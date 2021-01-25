package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription;

final class SoftReferenceGrammarPool implements XMLGrammarPool {
    protected static final int TABLE_SIZE = 11;
    protected static final Grammar[] ZERO_LENGTH_GRAMMAR_ARRAY = new Grammar[0];
    protected int fGrammarCount;
    protected Entry[] fGrammars;
    protected boolean fPoolIsLocked;
    protected final ReferenceQueue fReferenceQueue;

    public SoftReferenceGrammarPool() {
        this.fGrammars = null;
        this.fGrammarCount = 0;
        this.fReferenceQueue = new ReferenceQueue();
        this.fGrammars = new Entry[11];
        this.fPoolIsLocked = false;
    }

    public SoftReferenceGrammarPool(int i) {
        this.fGrammars = null;
        this.fGrammarCount = 0;
        this.fReferenceQueue = new ReferenceQueue();
        this.fGrammars = new Entry[i];
        this.fPoolIsLocked = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar[] retrieveInitialGrammarSet(String str) {
        Grammar[] grammarArr;
        synchronized (this.fGrammars) {
            clean();
            grammarArr = ZERO_LENGTH_GRAMMAR_ARRAY;
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
                clean();
                XMLGrammarDescription grammarDescription = grammar.getGrammarDescription();
                int hashCode = hashCode(grammarDescription);
                int length = (Integer.MAX_VALUE & hashCode) % this.fGrammars.length;
                for (Entry entry = this.fGrammars[length]; entry != null; entry = entry.next) {
                    if (entry.hash == hashCode && equals(entry.desc, grammarDescription)) {
                        if (entry.grammar.get() != grammar) {
                            entry.grammar = new SoftGrammarReference(entry, grammar, this.fReferenceQueue);
                        }
                        return;
                    }
                }
                this.fGrammars[length] = new Entry(hashCode, length, grammarDescription, grammar, this.fGrammars[length], this.fReferenceQueue);
                this.fGrammarCount++;
            }
        }
    }

    public Grammar getGrammar(XMLGrammarDescription xMLGrammarDescription) {
        synchronized (this.fGrammars) {
            clean();
            int hashCode = hashCode(xMLGrammarDescription);
            for (Entry entry = this.fGrammars[(Integer.MAX_VALUE & hashCode) % this.fGrammars.length]; entry != null; entry = entry.next) {
                Grammar grammar = (Grammar) entry.grammar.get();
                if (grammar == null) {
                    removeEntry(entry);
                } else if (entry.hash == hashCode && equals(entry.desc, xMLGrammarDescription)) {
                    return grammar;
                }
            }
            return null;
        }
    }

    public Grammar removeGrammar(XMLGrammarDescription xMLGrammarDescription) {
        synchronized (this.fGrammars) {
            clean();
            int hashCode = hashCode(xMLGrammarDescription);
            for (Entry entry = this.fGrammars[(Integer.MAX_VALUE & hashCode) % this.fGrammars.length]; entry != null; entry = entry.next) {
                if (entry.hash == hashCode && equals(entry.desc, xMLGrammarDescription)) {
                    return removeEntry(entry);
                }
            }
            return null;
        }
    }

    public boolean containsGrammar(XMLGrammarDescription xMLGrammarDescription) {
        synchronized (this.fGrammars) {
            clean();
            int hashCode = hashCode(xMLGrammarDescription);
            for (Entry entry = this.fGrammars[(Integer.MAX_VALUE & hashCode) % this.fGrammars.length]; entry != null; entry = entry.next) {
                if (((Grammar) entry.grammar.get()) == null) {
                    removeEntry(entry);
                } else if (entry.hash == hashCode && equals(entry.desc, xMLGrammarDescription)) {
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
        if (!(xMLGrammarDescription instanceof XMLSchemaDescription)) {
            return xMLGrammarDescription.equals(xMLGrammarDescription2);
        }
        if (!(xMLGrammarDescription2 instanceof XMLSchemaDescription)) {
            return false;
        }
        XMLSchemaDescription xMLSchemaDescription = (XMLSchemaDescription) xMLGrammarDescription;
        XMLSchemaDescription xMLSchemaDescription2 = (XMLSchemaDescription) xMLGrammarDescription2;
        String targetNamespace = xMLSchemaDescription.getTargetNamespace();
        if (targetNamespace != null) {
            if (!targetNamespace.equals(xMLSchemaDescription2.getTargetNamespace())) {
                return false;
            }
        } else if (xMLSchemaDescription2.getTargetNamespace() != null) {
            return false;
        }
        String expandedSystemId = xMLSchemaDescription.getExpandedSystemId();
        if (expandedSystemId != null) {
            if (!expandedSystemId.equals(xMLSchemaDescription2.getExpandedSystemId())) {
                return false;
            }
            return true;
        } else if (xMLSchemaDescription2.getExpandedSystemId() != null) {
            return false;
        } else {
            return true;
        }
    }

    public int hashCode(XMLGrammarDescription xMLGrammarDescription) {
        if (!(xMLGrammarDescription instanceof XMLSchemaDescription)) {
            return xMLGrammarDescription.hashCode();
        }
        XMLSchemaDescription xMLSchemaDescription = (XMLSchemaDescription) xMLGrammarDescription;
        String targetNamespace = xMLSchemaDescription.getTargetNamespace();
        String expandedSystemId = xMLSchemaDescription.getExpandedSystemId();
        int i = 0;
        int hashCode = targetNamespace != null ? targetNamespace.hashCode() : 0;
        if (expandedSystemId != null) {
            i = expandedSystemId.hashCode();
        }
        return hashCode ^ i;
    }

    private Grammar removeEntry(Entry entry) {
        if (entry.prev != null) {
            entry.prev.next = entry.next;
        } else {
            this.fGrammars[entry.bucket] = entry.next;
        }
        if (entry.next != null) {
            entry.next.prev = entry.prev;
        }
        this.fGrammarCount--;
        entry.grammar.entry = null;
        return (Grammar) entry.grammar.get();
    }

    private void clean() {
        Reference poll = this.fReferenceQueue.poll();
        while (poll != null) {
            Entry entry = ((SoftGrammarReference) poll).entry;
            if (entry != null) {
                removeEntry(entry);
            }
            poll = this.fReferenceQueue.poll();
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Entry {
        public int bucket;
        public XMLGrammarDescription desc;
        public SoftGrammarReference grammar;
        public int hash;
        public Entry next;
        public Entry prev = null;

        protected Entry(int i, int i2, XMLGrammarDescription xMLGrammarDescription, Grammar grammar2, Entry entry, ReferenceQueue referenceQueue) {
            this.hash = i;
            this.bucket = i2;
            this.next = entry;
            if (entry != null) {
                entry.prev = this;
            }
            this.desc = xMLGrammarDescription;
            this.grammar = new SoftGrammarReference(this, grammar2, referenceQueue);
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

    /* access modifiers changed from: package-private */
    public static final class SoftGrammarReference extends SoftReference {
        public Entry entry;

        protected SoftGrammarReference(Entry entry2, Grammar grammar, ReferenceQueue referenceQueue) {
            super(grammar, referenceQueue);
            this.entry = entry2;
        }
    }
}

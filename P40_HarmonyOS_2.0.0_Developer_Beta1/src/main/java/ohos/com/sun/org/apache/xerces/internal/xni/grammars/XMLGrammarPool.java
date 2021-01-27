package ohos.com.sun.org.apache.xerces.internal.xni.grammars;

public interface XMLGrammarPool {
    void cacheGrammars(String str, Grammar[] grammarArr);

    void clear();

    void lockPool();

    Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription);

    Grammar[] retrieveInitialGrammarSet(String str);

    void unlockPool();
}

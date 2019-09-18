package com.google.android.util;

import com.google.android.util.AbstractMessageParser;
import java.util.HashMap;
import java.util.Set;

public class SmileyResources implements AbstractMessageParser.Resources {
    private HashMap<String, Integer> mSmileyToRes = new HashMap<>();
    private final AbstractMessageParser.TrieNode smileys = new AbstractMessageParser.TrieNode();

    public SmileyResources(String[] smilies, int[] smileyResIds) {
        for (int i = 0; i < smilies.length; i++) {
            AbstractMessageParser.TrieNode.addToTrie(this.smileys, smilies[i], "");
            this.mSmileyToRes.put(smilies[i], Integer.valueOf(smileyResIds[i]));
        }
    }

    public int getSmileyRes(String smiley) {
        Integer i = this.mSmileyToRes.get(smiley);
        if (i == null) {
            return -1;
        }
        return i.intValue();
    }

    public Set<String> getSchemes() {
        return null;
    }

    public AbstractMessageParser.TrieNode getDomainSuffixes() {
        return null;
    }

    public AbstractMessageParser.TrieNode getSmileys() {
        return this.smileys;
    }

    public AbstractMessageParser.TrieNode getAcronyms() {
        return null;
    }
}

package com.google.android.util;

import com.google.android.util.AbstractMessageParser.Resources;
import com.google.android.util.AbstractMessageParser.TrieNode;
import java.util.HashMap;
import java.util.Set;

public class SmileyResources implements Resources {
    private HashMap<String, Integer> mSmileyToRes;
    private final TrieNode smileys;

    public SmileyResources(String[] smilies, int[] smileyResIds) {
        this.mSmileyToRes = new HashMap();
        this.smileys = new TrieNode();
        for (int i = 0; i < smilies.length; i++) {
            TrieNode.addToTrie(this.smileys, smilies[i], "");
            this.mSmileyToRes.put(smilies[i], Integer.valueOf(smileyResIds[i]));
        }
    }

    public int getSmileyRes(String smiley) {
        Integer i = (Integer) this.mSmileyToRes.get(smiley);
        if (i == null) {
            return -1;
        }
        return i.intValue();
    }

    public Set<String> getSchemes() {
        return null;
    }

    public TrieNode getDomainSuffixes() {
        return null;
    }

    public TrieNode getSmileys() {
        return this.smileys;
    }

    public TrieNode getAcronyms() {
        return null;
    }
}

package com.android.i18n.phonenumbers.prefixmapper;

import gov.nist.core.Separators;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.SortedMap;
import java.util.TreeSet;

abstract class PhonePrefixMapStorageStrategy {
    protected int numOfEntries = 0;
    protected final TreeSet<Integer> possibleLengths = new TreeSet();

    public abstract String getDescription(int i);

    public abstract int getPrefix(int i);

    public abstract void readExternal(ObjectInput objectInput) throws IOException;

    public abstract void readFromSortedMap(SortedMap<Integer, String> sortedMap);

    public abstract void writeExternal(ObjectOutput objectOutput) throws IOException;

    PhonePrefixMapStorageStrategy() {
    }

    public int getNumOfEntries() {
        return this.numOfEntries;
    }

    public TreeSet<Integer> getPossibleLengths() {
        return this.possibleLengths;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        int numOfEntries = getNumOfEntries();
        for (int i = 0; i < numOfEntries; i++) {
            output.append(getPrefix(i)).append("|").append(getDescription(i)).append(Separators.RETURN);
        }
        return output.toString();
    }
}

package com.android.i18n.phonenumbers.prefixmapper;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.SortedMap;

class DefaultMapStorage extends PhonePrefixMapStorageStrategy {
    private String[] descriptions;
    private int[] phoneNumberPrefixes;

    public int getPrefix(int index) {
        return this.phoneNumberPrefixes[index];
    }

    public String getDescription(int index) {
        return this.descriptions[index];
    }

    public void readFromSortedMap(SortedMap<Integer, String> sortedPhonePrefixMap) {
        this.numOfEntries = sortedPhonePrefixMap.size();
        this.phoneNumberPrefixes = new int[this.numOfEntries];
        this.descriptions = new String[this.numOfEntries];
        int index = 0;
        for (Integer intValue : sortedPhonePrefixMap.keySet()) {
            int prefix = intValue.intValue();
            int index2 = index + 1;
            this.phoneNumberPrefixes[index] = prefix;
            this.possibleLengths.add(Integer.valueOf(((int) Math.log10((double) prefix)) + 1));
            index = index2;
        }
        sortedPhonePrefixMap.values().toArray(this.descriptions);
    }

    public void readExternal(ObjectInput objectInput) throws IOException {
        int i;
        this.numOfEntries = objectInput.readInt();
        if (this.phoneNumberPrefixes == null || this.phoneNumberPrefixes.length < this.numOfEntries) {
            this.phoneNumberPrefixes = new int[this.numOfEntries];
        }
        if (this.descriptions == null || this.descriptions.length < this.numOfEntries) {
            this.descriptions = new String[this.numOfEntries];
        }
        for (i = 0; i < this.numOfEntries; i++) {
            this.phoneNumberPrefixes[i] = objectInput.readInt();
            this.descriptions[i] = objectInput.readUTF();
        }
        int sizeOfLengths = objectInput.readInt();
        this.possibleLengths.clear();
        for (i = 0; i < sizeOfLengths; i++) {
            this.possibleLengths.add(Integer.valueOf(objectInput.readInt()));
        }
    }

    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(this.numOfEntries);
        for (int i = 0; i < this.numOfEntries; i++) {
            objectOutput.writeInt(this.phoneNumberPrefixes[i]);
            objectOutput.writeUTF(this.descriptions[i]);
        }
        objectOutput.writeInt(this.possibleLengths.size());
        for (Integer length : this.possibleLengths) {
            objectOutput.writeInt(length.intValue());
        }
    }
}

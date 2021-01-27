package com.android.i18n.phonenumbers.prefixmapper;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.SortedMap;

/* access modifiers changed from: package-private */
public class DefaultMapStorage extends PhonePrefixMapStorageStrategy {
    private String[] descriptions;
    private int[] phoneNumberPrefixes;

    @Override // com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMapStorageStrategy
    public int getPrefix(int index) {
        return this.phoneNumberPrefixes[index];
    }

    @Override // com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMapStorageStrategy
    public String getDescription(int index) {
        return this.descriptions[index];
    }

    @Override // com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMapStorageStrategy
    public void readFromSortedMap(SortedMap<Integer, String> sortedPhonePrefixMap) {
        this.numOfEntries = sortedPhonePrefixMap.size();
        this.phoneNumberPrefixes = new int[this.numOfEntries];
        this.descriptions = new String[this.numOfEntries];
        int index = 0;
        for (Integer num : sortedPhonePrefixMap.keySet()) {
            int prefix = num.intValue();
            this.phoneNumberPrefixes[index] = prefix;
            this.possibleLengths.add(Integer.valueOf(((int) Math.log10((double) prefix)) + 1));
            index++;
        }
        sortedPhonePrefixMap.values().toArray(this.descriptions);
    }

    @Override // com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMapStorageStrategy
    public void readExternal(ObjectInput objectInput) throws IOException {
        this.numOfEntries = objectInput.readInt();
        int[] iArr = this.phoneNumberPrefixes;
        if (iArr == null || iArr.length < this.numOfEntries) {
            this.phoneNumberPrefixes = new int[this.numOfEntries];
        }
        String[] strArr = this.descriptions;
        if (strArr == null || strArr.length < this.numOfEntries) {
            this.descriptions = new String[this.numOfEntries];
        }
        for (int i = 0; i < this.numOfEntries; i++) {
            this.phoneNumberPrefixes[i] = objectInput.readInt();
            this.descriptions[i] = objectInput.readUTF();
        }
        int sizeOfLengths = objectInput.readInt();
        this.possibleLengths.clear();
        for (int i2 = 0; i2 < sizeOfLengths; i2++) {
            this.possibleLengths.add(Integer.valueOf(objectInput.readInt()));
        }
    }

    @Override // com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMapStorageStrategy
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(this.numOfEntries);
        for (int i = 0; i < this.numOfEntries; i++) {
            objectOutput.writeInt(this.phoneNumberPrefixes[i]);
            objectOutput.writeUTF(this.descriptions[i]);
        }
        objectOutput.writeInt(this.possibleLengths.size());
        Iterator it = this.possibleLengths.iterator();
        while (it.hasNext()) {
            objectOutput.writeInt(((Integer) it.next()).intValue());
        }
    }
}

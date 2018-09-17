package com.android.i18n.phonenumbers.prefixmapper;

import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.logging.Logger;

public class PhonePrefixMap implements Externalizable {
    private static final Logger logger = Logger.getLogger(PhonePrefixMap.class.getName());
    private PhonePrefixMapStorageStrategy phonePrefixMapStorage;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    PhonePrefixMapStorageStrategy getPhonePrefixMapStorage() {
        return this.phonePrefixMapStorage;
    }

    private static int getSizeOfPhonePrefixMapStorage(PhonePrefixMapStorageStrategy mapStorage, SortedMap<Integer, String> phonePrefixMap) throws IOException {
        mapStorage.readFromSortedMap(phonePrefixMap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        mapStorage.writeExternal(objectOutputStream);
        objectOutputStream.flush();
        int sizeOfStorage = byteArrayOutputStream.size();
        objectOutputStream.close();
        return sizeOfStorage;
    }

    private PhonePrefixMapStorageStrategy createDefaultMapStorage() {
        return new DefaultMapStorage();
    }

    private PhonePrefixMapStorageStrategy createFlyweightMapStorage() {
        return new FlyweightMapStorage();
    }

    PhonePrefixMapStorageStrategy getSmallerMapStorage(SortedMap<Integer, String> phonePrefixMap) {
        try {
            PhonePrefixMapStorageStrategy flyweightMapStorage = createFlyweightMapStorage();
            int sizeOfFlyweightMapStorage = getSizeOfPhonePrefixMapStorage(flyweightMapStorage, phonePrefixMap);
            PhonePrefixMapStorageStrategy defaultMapStorage = createDefaultMapStorage();
            if (sizeOfFlyweightMapStorage >= getSizeOfPhonePrefixMapStorage(defaultMapStorage, phonePrefixMap)) {
                flyweightMapStorage = defaultMapStorage;
            }
            return flyweightMapStorage;
        } catch (IOException e) {
            logger.severe(e.getMessage());
            return createFlyweightMapStorage();
        }
    }

    public void readPhonePrefixMap(SortedMap<Integer, String> sortedPhonePrefixMap) {
        this.phonePrefixMapStorage = getSmallerMapStorage(sortedPhonePrefixMap);
    }

    public void readExternal(ObjectInput objectInput) throws IOException {
        if (objectInput.readBoolean()) {
            this.phonePrefixMapStorage = new FlyweightMapStorage();
        } else {
            this.phonePrefixMapStorage = new DefaultMapStorage();
        }
        this.phonePrefixMapStorage.readExternal(objectInput);
    }

    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeBoolean(this.phonePrefixMapStorage instanceof FlyweightMapStorage);
        this.phonePrefixMapStorage.writeExternal(objectOutput);
    }

    String lookup(long number) {
        int numOfEntries = this.phonePrefixMapStorage.getNumOfEntries();
        if (numOfEntries == 0) {
            return null;
        }
        long phonePrefix = number;
        int currentIndex = numOfEntries - 1;
        SortedSet<Integer> currentSetOfLengths = this.phonePrefixMapStorage.getPossibleLengths();
        while (currentSetOfLengths.size() > 0) {
            Integer possibleLength = (Integer) currentSetOfLengths.last();
            String phonePrefixStr = String.valueOf(phonePrefix);
            if (phonePrefixStr.length() > possibleLength.intValue()) {
                phonePrefix = Long.parseLong(phonePrefixStr.substring(0, possibleLength.intValue()));
            }
            currentIndex = binarySearch(0, currentIndex, phonePrefix);
            if (currentIndex < 0) {
                return null;
            }
            if (phonePrefix == ((long) this.phonePrefixMapStorage.getPrefix(currentIndex))) {
                return this.phonePrefixMapStorage.getDescription(currentIndex);
            }
            currentSetOfLengths = currentSetOfLengths.headSet(possibleLength);
        }
        return null;
    }

    public String lookup(PhoneNumber number) {
        return lookup(Long.parseLong(number.getCountryCode() + this.phoneUtil.getNationalSignificantNumber(number)));
    }

    private int binarySearch(int start, int end, long value) {
        int current = 0;
        while (start <= end) {
            current = (start + end) >>> 1;
            int currentValue = this.phonePrefixMapStorage.getPrefix(current);
            if (((long) currentValue) == value) {
                return current;
            }
            if (((long) currentValue) > value) {
                current--;
                end = current;
            } else {
                start = current + 1;
            }
        }
        return current;
    }

    public String toString() {
        return this.phonePrefixMapStorage.toString();
    }
}

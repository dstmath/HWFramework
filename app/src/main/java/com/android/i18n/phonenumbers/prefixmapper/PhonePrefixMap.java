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
    private static final Logger LOGGER = null;
    private PhonePrefixMapStorageStrategy phonePrefixMapStorage;
    private final PhoneNumberUtil phoneUtil;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMap.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.prefixmapper.PhonePrefixMap.<clinit>():void");
    }

    PhonePrefixMapStorageStrategy getPhonePrefixMapStorage() {
        return this.phonePrefixMapStorage;
    }

    public PhonePrefixMap() {
        this.phoneUtil = PhoneNumberUtil.getInstance();
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
            LOGGER.severe(e.getMessage());
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

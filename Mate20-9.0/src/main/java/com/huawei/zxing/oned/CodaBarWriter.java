package com.huawei.zxing.oned;

import java.util.Arrays;

public final class CodaBarWriter extends OneDimensionalCodeWriter {
    private static final char[] ALT_START_END_CHARS = {'T', 'N', '*', 'E'};
    private static final char[] START_END_CHARS = {'A', 'B', 'C', 'D'};

    public boolean[] encode(String contents) {
        int i;
        String str = contents;
        if (contents.length() >= 2) {
            int i2 = 0;
            char firstChar = Character.toUpperCase(str.charAt(0));
            int i3 = 1;
            char lastChar = Character.toUpperCase(str.charAt(contents.length() - 1));
            boolean startsEndsNormal = CodaBarReader.arrayContains(START_END_CHARS, firstChar) && CodaBarReader.arrayContains(START_END_CHARS, lastChar);
            boolean startsEndsAlt = CodaBarReader.arrayContains(ALT_START_END_CHARS, firstChar) && CodaBarReader.arrayContains(ALT_START_END_CHARS, lastChar);
            if (startsEndsNormal || startsEndsAlt) {
                char[] charsWhichAreTenLengthEachAfterDecoded = {'/', ':', '+', '.'};
                int resultLength = 20;
                for (int i4 = 1; i4 < contents.length() - 1; i4++) {
                    if (Character.isDigit(str.charAt(i4)) || str.charAt(i4) == '-' || str.charAt(i4) == '$') {
                        resultLength += 9;
                    } else if (CodaBarReader.arrayContains(charsWhichAreTenLengthEachAfterDecoded, str.charAt(i4))) {
                        resultLength += 10;
                    } else {
                        throw new IllegalArgumentException("Cannot encode : '" + str.charAt(i4) + '\'');
                    }
                }
                boolean[] result = new boolean[(resultLength + (contents.length() - 1))];
                int position = 0;
                int index = 0;
                while (index < contents.length()) {
                    char c = Character.toUpperCase(str.charAt(index));
                    if (index == contents.length() - i3) {
                        if (c == '*') {
                            c = 'C';
                        } else if (c == 'E') {
                            c = 'D';
                        } else if (c == 'N') {
                            c = 'B';
                        } else if (c == 'T') {
                            c = 'A';
                        }
                    }
                    int code = 0;
                    int i5 = i2;
                    while (true) {
                        if (i5 >= CodaBarReader.ALPHABET.length) {
                            break;
                        } else if (c == CodaBarReader.ALPHABET[i5]) {
                            code = CodaBarReader.CHARACTER_ENCODINGS[i5];
                            break;
                        } else {
                            i5++;
                        }
                    }
                    int i6 = position;
                    int position2 = i2;
                    int counter = 0;
                    boolean color = true;
                    int position3 = i6;
                    while (position2 < 7) {
                        result[position3] = color;
                        position3++;
                        char firstChar2 = firstChar;
                        if (((code >> (6 - position2)) & 1) == 0 || counter == 1) {
                            color = !color;
                            position2++;
                            counter = 0;
                        } else {
                            counter++;
                        }
                        firstChar = firstChar2;
                    }
                    char firstChar3 = firstChar;
                    i3 = 1;
                    if (index < contents.length() - 1) {
                        i = 0;
                        result[position3] = false;
                        position3++;
                    } else {
                        i = 0;
                    }
                    position = position3;
                    index++;
                    i2 = i;
                    firstChar = firstChar3;
                }
                return result;
            }
            throw new IllegalArgumentException("Codabar should start/end with " + Arrays.toString(START_END_CHARS) + ", or start/end with " + Arrays.toString(ALT_START_END_CHARS));
        }
        throw new IllegalArgumentException("Codabar should start/end with start/stop symbols");
    }
}

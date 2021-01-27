package com.huawei.zxing.oned;

import java.util.Arrays;

public final class CodaBarWriter extends OneDimensionalCodeWriter {
    private static final char[] ALT_START_END_CHARS = {'T', 'N', '*', 'E'};
    private static final char[] START_END_CHARS = {'A', 'B', 'C', 'D'};

    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter
    public boolean[] encode(String contents) {
        boolean z;
        String str = contents;
        if (contents.length() >= 2) {
            char firstChar = Character.toUpperCase(str.charAt(0));
            int i = 1;
            char lastChar = Character.toUpperCase(str.charAt(contents.length() - 1));
            boolean startsEndsNormal = CodaBarReader.arrayContains(START_END_CHARS, firstChar) && CodaBarReader.arrayContains(START_END_CHARS, lastChar);
            boolean startsEndsAlt = CodaBarReader.arrayContains(ALT_START_END_CHARS, firstChar) && CodaBarReader.arrayContains(ALT_START_END_CHARS, lastChar);
            if (startsEndsNormal || startsEndsAlt) {
                int resultLength = 20;
                char[] charsWhichAreTenLengthEachAfterDecoded = {'/', ':', '+', '.'};
                for (int i2 = 1; i2 < contents.length() - 1; i2++) {
                    if (Character.isDigit(str.charAt(i2)) || str.charAt(i2) == '-' || str.charAt(i2) == '$') {
                        resultLength += 9;
                    } else if (CodaBarReader.arrayContains(charsWhichAreTenLengthEachAfterDecoded, str.charAt(i2))) {
                        resultLength += 10;
                    } else {
                        throw new IllegalArgumentException("Cannot encode : '" + str.charAt(i2) + '\'');
                    }
                }
                boolean[] result = new boolean[(resultLength + (contents.length() - 1))];
                int bit = 0;
                int index = 0;
                while (index < contents.length()) {
                    char c = Character.toUpperCase(str.charAt(index));
                    if (index == contents.length() - i) {
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
                    int i3 = 0;
                    while (true) {
                        if (i3 >= CodaBarReader.ALPHABET.length) {
                            break;
                        } else if (c == CodaBarReader.ALPHABET[i3]) {
                            code = CodaBarReader.CHARACTER_ENCODINGS[i3];
                            break;
                        } else {
                            i3++;
                        }
                    }
                    int counter = 0;
                    boolean color = true;
                    int position = bit;
                    int bit2 = 0;
                    while (bit2 < 7) {
                        result[position] = color;
                        position++;
                        if (((code >> (6 - bit2)) & 1) == 0 || counter == 1) {
                            color = !color;
                            bit2++;
                            counter = 0;
                        } else {
                            counter++;
                        }
                    }
                    i = 1;
                    if (index < contents.length() - 1) {
                        z = false;
                        result[position] = false;
                        position++;
                    } else {
                        z = false;
                    }
                    bit = position;
                    index++;
                    str = contents;
                }
                return result;
            }
            throw new IllegalArgumentException("Codabar should start/end with " + Arrays.toString(START_END_CHARS) + ", or start/end with " + Arrays.toString(ALT_START_END_CHARS));
        }
        throw new IllegalArgumentException("Codabar should start/end with start/stop symbols");
    }
}

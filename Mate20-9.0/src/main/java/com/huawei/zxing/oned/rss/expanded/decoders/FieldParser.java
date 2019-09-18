package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.zxing.NotFoundException;

final class FieldParser {
    private static final Object[][] FOUR_DIGIT_DATA_LENGTH = {new Object[]{"7001", 13}, new Object[]{"7002", VARIABLE_LENGTH, 30}, new Object[]{"7003", 10}, new Object[]{"8001", 14}, new Object[]{"8002", VARIABLE_LENGTH, 20}, new Object[]{"8003", VARIABLE_LENGTH, 30}, new Object[]{"8004", VARIABLE_LENGTH, 30}, new Object[]{"8005", 6}, new Object[]{"8006", 18}, new Object[]{"8007", VARIABLE_LENGTH, 30}, new Object[]{"8008", VARIABLE_LENGTH, 12}, new Object[]{"8018", 18}, new Object[]{"8020", VARIABLE_LENGTH, 25}, new Object[]{"8100", 6}, new Object[]{"8101", 10}, new Object[]{"8102", 2}, new Object[]{"8110", VARIABLE_LENGTH, 70}, new Object[]{"8200", VARIABLE_LENGTH, 70}};
    private static final Object[][] THREE_DIGIT_DATA_LENGTH = {new Object[]{"240", VARIABLE_LENGTH, 30}, new Object[]{"241", VARIABLE_LENGTH, 30}, new Object[]{"242", VARIABLE_LENGTH, 6}, new Object[]{"250", VARIABLE_LENGTH, 30}, new Object[]{"251", VARIABLE_LENGTH, 30}, new Object[]{"253", VARIABLE_LENGTH, 17}, new Object[]{"254", VARIABLE_LENGTH, 20}, new Object[]{"400", VARIABLE_LENGTH, 30}, new Object[]{"401", VARIABLE_LENGTH, 30}, new Object[]{"402", 17}, new Object[]{"403", VARIABLE_LENGTH, 30}, new Object[]{"410", 13}, new Object[]{"411", 13}, new Object[]{"412", 13}, new Object[]{"413", 13}, new Object[]{"414", 13}, new Object[]{"420", VARIABLE_LENGTH, 20}, new Object[]{"421", VARIABLE_LENGTH, 15}, new Object[]{"422", 3}, new Object[]{"423", VARIABLE_LENGTH, 15}, new Object[]{"424", 3}, new Object[]{"425", 3}, new Object[]{"426", 3}};
    private static final Object[][] THREE_DIGIT_PLUS_DIGIT_DATA_LENGTH = {new Object[]{"310", 6}, new Object[]{"311", 6}, new Object[]{"312", 6}, new Object[]{"313", 6}, new Object[]{"314", 6}, new Object[]{"315", 6}, new Object[]{"316", 6}, new Object[]{"320", 6}, new Object[]{"321", 6}, new Object[]{"322", 6}, new Object[]{"323", 6}, new Object[]{"324", 6}, new Object[]{"325", 6}, new Object[]{"326", 6}, new Object[]{"327", 6}, new Object[]{"328", 6}, new Object[]{"329", 6}, new Object[]{"330", 6}, new Object[]{"331", 6}, new Object[]{"332", 6}, new Object[]{"333", 6}, new Object[]{"334", 6}, new Object[]{"335", 6}, new Object[]{"336", 6}, new Object[]{"340", 6}, new Object[]{"341", 6}, new Object[]{"342", 6}, new Object[]{"343", 6}, new Object[]{"344", 6}, new Object[]{"345", 6}, new Object[]{"346", 6}, new Object[]{"347", 6}, new Object[]{"348", 6}, new Object[]{"349", 6}, new Object[]{"350", 6}, new Object[]{"351", 6}, new Object[]{"352", 6}, new Object[]{"353", 6}, new Object[]{"354", 6}, new Object[]{"355", 6}, new Object[]{"356", 6}, new Object[]{"357", 6}, new Object[]{"360", 6}, new Object[]{"361", 6}, new Object[]{"362", 6}, new Object[]{"363", 6}, new Object[]{"364", 6}, new Object[]{"365", 6}, new Object[]{"366", 6}, new Object[]{"367", 6}, new Object[]{"368", 6}, new Object[]{"369", 6}, new Object[]{"390", VARIABLE_LENGTH, 15}, new Object[]{"391", VARIABLE_LENGTH, 18}, new Object[]{"392", VARIABLE_LENGTH, 15}, new Object[]{"393", VARIABLE_LENGTH, 18}, new Object[]{"703", VARIABLE_LENGTH, 30}};
    private static final Object[][] TWO_DIGIT_DATA_LENGTH = {new Object[]{"00", 18}, new Object[]{"01", 14}, new Object[]{"02", 14}, new Object[]{"10", VARIABLE_LENGTH, 20}, new Object[]{"11", 6}, new Object[]{"12", 6}, new Object[]{"13", 6}, new Object[]{"15", 6}, new Object[]{"17", 6}, new Object[]{"20", 2}, new Object[]{"21", VARIABLE_LENGTH, 20}, new Object[]{"22", VARIABLE_LENGTH, 29}, new Object[]{"30", VARIABLE_LENGTH, 8}, new Object[]{"37", VARIABLE_LENGTH, 8}, new Object[]{"90", VARIABLE_LENGTH, 30}, new Object[]{"91", VARIABLE_LENGTH, 30}, new Object[]{"92", VARIABLE_LENGTH, 30}, new Object[]{"93", VARIABLE_LENGTH, 30}, new Object[]{"94", VARIABLE_LENGTH, 30}, new Object[]{"95", VARIABLE_LENGTH, 30}, new Object[]{"96", VARIABLE_LENGTH, 30}, new Object[]{"97", VARIABLE_LENGTH, 30}, new Object[]{"98", VARIABLE_LENGTH, 30}, new Object[]{"99", VARIABLE_LENGTH, 30}};
    private static final Object VARIABLE_LENGTH = new Object();

    private FieldParser() {
    }

    static String parseFieldsInGeneralPurpose(String rawInformation) throws NotFoundException {
        if (rawInformation.isEmpty()) {
            return null;
        }
        if (rawInformation.length() >= 2) {
            String firstTwoDigits = rawInformation.substring(0, 2);
            Object[][] objArr = TWO_DIGIT_DATA_LENGTH;
            int length = objArr.length;
            int i = 0;
            while (i < length) {
                Object[] dataLength = objArr[i];
                if (!dataLength[0].equals(firstTwoDigits)) {
                    i++;
                } else if (dataLength[1] == VARIABLE_LENGTH) {
                    return processVariableAI(2, ((Integer) dataLength[2]).intValue(), rawInformation);
                } else {
                    return processFixedAI(2, ((Integer) dataLength[1]).intValue(), rawInformation);
                }
            }
            if (rawInformation.length() >= 3) {
                String firstThreeDigits = rawInformation.substring(0, 3);
                Object[][] objArr2 = THREE_DIGIT_DATA_LENGTH;
                int length2 = objArr2.length;
                int i2 = 0;
                while (i2 < length2) {
                    Object[] dataLength2 = objArr2[i2];
                    if (!dataLength2[0].equals(firstThreeDigits)) {
                        i2++;
                    } else if (dataLength2[1] == VARIABLE_LENGTH) {
                        return processVariableAI(3, ((Integer) dataLength2[2]).intValue(), rawInformation);
                    } else {
                        return processFixedAI(3, ((Integer) dataLength2[1]).intValue(), rawInformation);
                    }
                }
                Object[][] objArr3 = THREE_DIGIT_PLUS_DIGIT_DATA_LENGTH;
                int length3 = objArr3.length;
                int i3 = 0;
                while (i3 < length3) {
                    Object[] dataLength3 = objArr3[i3];
                    if (!dataLength3[0].equals(firstThreeDigits)) {
                        i3++;
                    } else if (dataLength3[1] == VARIABLE_LENGTH) {
                        return processVariableAI(4, ((Integer) dataLength3[2]).intValue(), rawInformation);
                    } else {
                        return processFixedAI(4, ((Integer) dataLength3[1]).intValue(), rawInformation);
                    }
                }
                if (rawInformation.length() >= 4) {
                    String firstFourDigits = rawInformation.substring(0, 4);
                    Object[][] objArr4 = FOUR_DIGIT_DATA_LENGTH;
                    int length4 = objArr4.length;
                    int i4 = 0;
                    while (i4 < length4) {
                        Object[] dataLength4 = objArr4[i4];
                        if (!dataLength4[0].equals(firstFourDigits)) {
                            i4++;
                        } else if (dataLength4[1] == VARIABLE_LENGTH) {
                            return processVariableAI(4, ((Integer) dataLength4[2]).intValue(), rawInformation);
                        } else {
                            return processFixedAI(4, ((Integer) dataLength4[1]).intValue(), rawInformation);
                        }
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
                throw NotFoundException.getNotFoundInstance();
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static String processFixedAI(int aiSize, int fieldSize, String rawInformation) throws NotFoundException {
        if (rawInformation.length() >= aiSize) {
            String ai = rawInformation.substring(0, aiSize);
            if (rawInformation.length() >= aiSize + fieldSize) {
                String field = rawInformation.substring(aiSize, aiSize + fieldSize);
                String result = '(' + ai + ')' + field;
                if (parseFieldsInGeneralPurpose(rawInformation.substring(aiSize + fieldSize)) == null) {
                    return result;
                }
                return result + parsedAI;
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static String processVariableAI(int aiSize, int variableFieldSize, String rawInformation) throws NotFoundException {
        int maxSize;
        String ai = rawInformation.substring(0, aiSize);
        if (rawInformation.length() < aiSize + variableFieldSize) {
            maxSize = rawInformation.length();
        } else {
            maxSize = aiSize + variableFieldSize;
        }
        String field = rawInformation.substring(aiSize, maxSize);
        String result = '(' + ai + ')' + field;
        if (parseFieldsInGeneralPurpose(rawInformation.substring(maxSize)) == null) {
            return result;
        }
        return result + parsedAI;
    }
}

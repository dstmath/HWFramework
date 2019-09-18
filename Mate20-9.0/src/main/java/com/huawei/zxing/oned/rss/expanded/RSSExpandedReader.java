package com.huawei.zxing.oned.rss.expanded;

import android.telephony.HwCarrierConfigManager;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.hishow.AlarmInfoEx;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.internal.telephony.SmsConstantsEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.oned.rss.AbstractRSSReader;
import com.huawei.zxing.oned.rss.DataCharacter;
import com.huawei.zxing.oned.rss.FinderPattern;
import com.huawei.zxing.oned.rss.RSSUtils;
import com.huawei.zxing.oned.rss.expanded.decoders.AbstractExpandedDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RSSExpandedReader extends AbstractRSSReader {
    private static final int[] EVEN_TOTAL_SUBSET = {4, 20, 52, 104, 204};
    private static final int[][] FINDER_PATTERNS = {new int[]{1, 8, 4, 1}, new int[]{3, 6, 4, 1}, new int[]{3, 4, 6, 1}, new int[]{3, 2, 8, 1}, new int[]{2, 6, 5, 1}, new int[]{2, 2, 9, 1}};
    private static final int[][] FINDER_PATTERN_SEQUENCES = {new int[]{0, 0}, new int[]{0, 1, 1}, new int[]{0, 2, 1, 3}, new int[]{0, 4, 1, 3, 2}, new int[]{0, 4, 1, 3, 3, 5}, new int[]{0, 4, 1, 3, 4, 5, 5}, new int[]{0, 0, 1, 1, 2, 2, 3, 3}, new int[]{0, 0, 1, 1, 2, 2, 3, 4, 4}, new int[]{0, 0, 1, 1, 2, 2, 3, 4, 5, 5}, new int[]{0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5}};
    private static final int FINDER_PAT_A = 0;
    private static final int FINDER_PAT_B = 1;
    private static final int FINDER_PAT_C = 2;
    private static final int FINDER_PAT_D = 3;
    private static final int FINDER_PAT_E = 4;
    private static final int FINDER_PAT_F = 5;
    private static final int[] GSUM = {0, 348, 1388, 2948, 3988};
    private static final int MAX_PAIRS = 11;
    private static final int[] SYMBOL_WIDEST = {7, 5, 4, 3, 1};
    private static final int[][] WEIGHTS = {new int[]{1, 3, 9, 27, 81, 32, 96, 77}, new int[]{20, 60, 180, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE, 143, 7, 21, 63}, new int[]{189, 145, 13, 39, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE, 140, 209, 205}, new int[]{193, 157, 49, 147, 19, 57, 171, 91}, new int[]{62, 186, 136, 197, 169, 85, 44, JlogConstantsEx.JLID_EDIT_CONTACT_END}, new int[]{185, 133, 188, 142, 4, 12, 36, MetricConstant.GPS_METRIC_ID_EX}, new int[]{113, AppOpsManagerEx.TYPE_MICROPHONE, 173, 97, 80, 29, 87, 50}, new int[]{150, 28, 84, 41, JlogConstantsEx.JLID_NEW_CONTACT_CLICK, 158, 52, 156}, new int[]{46, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, MotionTypeApps.TYPE_FLIP_MUTE_AOD, 187, JlogConstantsEx.JLID_MMS_MESSAGE_SEARCH, 206, 196, 166}, new int[]{76, 17, 51, 153, 37, 111, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK, 155}, new int[]{43, 129, IccConstantsEx.SMS_RECORD_LENGTH, 106, MetricConstant.BLUETOOTH_METRIC_ID_EX, 110, JlogConstantsEx.JLID_CONTACT_MULTISELECT_BIND_VIEW, 146}, new int[]{16, 48, 144, 10, 30, 90, 59, 177}, new int[]{109, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW, JlogConstantsEx.JLID_MMS_CONVERSATIONS_DELETE, 200, 178, 112, JlogConstantsEx.JLID_EDIT_CONTACT_CLICK, 164}, new int[]{70, 210, 208, MotionTypeApps.TYPE_FLIP_MUTE_CLOCK, 184, 130, 179, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW}, new int[]{SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 191, 151, 31, 93, 68, 204, 190}, new int[]{148, 22, 66, 198, 172, 94, 71, 2}, new int[]{6, 18, 54, 162, 64, HwCarrierConfigManager.HD_ICON_MASK_DIALER, 154, 40}, new int[]{JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, 149, 25, 75, 14, 42, JlogConstantsEx.JLID_NEW_CONTACT_SELECT_ACCOUNT, 167}, new int[]{79, 26, 78, 23, 69, 207, HwFoldScreenManagerEx.POSTURE_OTHER, 175}, new int[]{103, 98, 83, 38, 114, 131, 182, JlogConstantsEx.JLID_NEW_CONTACT_SAVE_CLICK}, new int[]{161, 61, 183, AlarmInfoEx.EVERYDAY_CODE, 170, 88, 53, 159}, new int[]{55, 165, 73, 8, 24, 72, 5, 15}, new int[]{45, 135, 194, SmsConstantsEx.MAX_USER_DATA_SEPTETS, 58, 174, 100, 89}};
    private final List<ExpandedPair> pairs = new ArrayList(11);
    private final List<ExpandedRow> rows = new ArrayList();
    private final int[] startEnd = new int[2];
    private boolean startFromEven = false;

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        this.pairs.clear();
        this.startFromEven = false;
        try {
            return constructResult(decodeRow2pairs(rowNumber, row));
        } catch (NotFoundException e) {
            this.pairs.clear();
            this.startFromEven = true;
            return constructResult(decodeRow2pairs(rowNumber, row));
        }
    }

    public void reset() {
        this.pairs.clear();
        this.rows.clear();
    }

    /* access modifiers changed from: package-private */
    public List<ExpandedPair> decodeRow2pairs(int rowNumber, BitArray row) throws NotFoundException {
        while (true) {
            try {
                this.pairs.add(retrieveNextPair(row, this.pairs, rowNumber));
            } catch (NotFoundException nfe) {
                if (this.pairs.isEmpty()) {
                    throw nfe;
                } else if (checkChecksum()) {
                    return this.pairs;
                } else {
                    boolean tryStackedDecode = !this.rows.isEmpty();
                    storeRow(rowNumber, false);
                    if (tryStackedDecode) {
                        List<ExpandedPair> ps = checkRows(false);
                        if (ps != null) {
                            return ps;
                        }
                        List<ExpandedPair> ps2 = checkRows(true);
                        if (ps2 != null) {
                            return ps2;
                        }
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
            }
        }
    }

    private List<ExpandedPair> checkRows(boolean reverse) {
        if (this.rows.size() > 25) {
            this.rows.clear();
            return null;
        }
        this.pairs.clear();
        if (reverse) {
            Collections.reverse(this.rows);
        }
        List<ExpandedPair> ps = null;
        try {
            ps = checkRows(new ArrayList(), 0);
        } catch (NotFoundException e) {
        }
        if (reverse) {
            Collections.reverse(this.rows);
        }
        return ps;
    }

    private List<ExpandedPair> checkRows(List<ExpandedRow> collectedRows, int currentRow) throws NotFoundException {
        for (int i = currentRow; i < this.rows.size(); i++) {
            ExpandedRow row = this.rows.get(i);
            this.pairs.clear();
            int size = collectedRows.size();
            for (int j = 0; j < size; j++) {
                this.pairs.addAll(collectedRows.get(j).getPairs());
            }
            this.pairs.addAll(row.getPairs());
            if (isValidSequence(this.pairs)) {
                if (checkChecksum()) {
                    return this.pairs;
                }
                List<ExpandedRow> rs = new ArrayList<>();
                rs.addAll(collectedRows);
                rs.add(row);
                try {
                    return checkRows(rs, i + 1);
                } catch (NotFoundException e) {
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static boolean isValidSequence(List<ExpandedPair> pairs2) {
        for (int[] sequence : FINDER_PATTERN_SEQUENCES) {
            if (pairs2.size() <= sequence.length) {
                boolean stop = true;
                int j = 0;
                while (true) {
                    if (j >= pairs2.size()) {
                        break;
                    } else if (pairs2.get(j).getFinderPattern().getValue() != sequence[j]) {
                        stop = false;
                        break;
                    } else {
                        j++;
                    }
                }
                if (stop) {
                    return true;
                }
            }
        }
        return false;
    }

    private void storeRow(int rowNumber, boolean wasReversed) {
        int insertPos = 0;
        boolean prevIsSame = false;
        boolean nextIsSame = false;
        while (true) {
            if (insertPos >= this.rows.size()) {
                break;
            }
            ExpandedRow erow = this.rows.get(insertPos);
            if (erow.getRowNumber() > rowNumber) {
                nextIsSame = erow.isEquivalent(this.pairs);
                break;
            } else {
                prevIsSame = erow.isEquivalent(this.pairs);
                insertPos++;
            }
        }
        if (!nextIsSame && !prevIsSame && !isPartialRow(this.pairs, this.rows)) {
            this.rows.add(insertPos, new ExpandedRow(this.pairs, rowNumber, wasReversed));
            removePartialRows(this.pairs, this.rows);
        }
    }

    private static void removePartialRows(List<ExpandedPair> pairs2, List<ExpandedRow> rows2) {
        Iterator<ExpandedRow> iterator = rows2.iterator();
        while (iterator.hasNext()) {
            ExpandedRow r = iterator.next();
            if (r.getPairs().size() != pairs2.size()) {
                boolean allFound = true;
                Iterator<ExpandedPair> it = r.getPairs().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ExpandedPair p = it.next();
                    boolean found = false;
                    Iterator<ExpandedPair> it2 = pairs2.iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            if (p.equals(it2.next())) {
                                found = true;
                                continue;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (!found) {
                        allFound = false;
                        break;
                    }
                }
                if (allFound) {
                    iterator.remove();
                }
            }
        }
    }

    private static boolean isPartialRow(Iterable<ExpandedPair> pairs2, Iterable<ExpandedRow> rows2) {
        for (ExpandedRow r : rows2) {
            boolean allFound = true;
            Iterator<ExpandedPair> it = pairs2.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ExpandedPair p = it.next();
                boolean found = false;
                Iterator<ExpandedPair> it2 = r.getPairs().iterator();
                while (true) {
                    if (it2.hasNext()) {
                        if (p.equals(it2.next())) {
                            found = true;
                            continue;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!found) {
                    allFound = false;
                    continue;
                    break;
                }
            }
            if (allFound) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public List<ExpandedRow> getRows() {
        return this.rows;
    }

    static Result constructResult(List<ExpandedPair> pairs2) throws NotFoundException, FormatException {
        String resultingString = AbstractExpandedDecoder.createDecoder(BitArrayBuilder.buildBitArray(pairs2)).parseInformation();
        ResultPoint[] firstPoints = pairs2.get(0).getFinderPattern().getResultPoints();
        ResultPoint[] lastPoints = pairs2.get(pairs2.size() - 1).getFinderPattern().getResultPoints();
        return new Result(resultingString, null, new ResultPoint[]{firstPoints[0], firstPoints[1], lastPoints[0], lastPoints[1]}, BarcodeFormat.RSS_EXPANDED);
    }

    private boolean checkChecksum() {
        boolean z = false;
        ExpandedPair firstPair = this.pairs.get(0);
        DataCharacter checkCharacter = firstPair.getLeftChar();
        DataCharacter firstCharacter = firstPair.getRightChar();
        if (firstCharacter == null) {
            return false;
        }
        int s = 2;
        int checksum = firstCharacter.getChecksumPortion();
        for (int i = 1; i < this.pairs.size(); i++) {
            ExpandedPair currentPair = this.pairs.get(i);
            checksum += currentPair.getLeftChar().getChecksumPortion();
            s++;
            DataCharacter currentRightChar = currentPair.getRightChar();
            if (currentRightChar != null) {
                checksum += currentRightChar.getChecksumPortion();
                s++;
            }
        }
        if ((211 * (s - 4)) + (checksum % 211) == checkCharacter.getValue()) {
            z = true;
        }
        return z;
    }

    private static int getNextSecondBar(BitArray row, int initialPos) {
        if (row.get(initialPos)) {
            return row.getNextSet(row.getNextUnset(initialPos));
        }
        return row.getNextUnset(row.getNextSet(initialPos));
    }

    /* access modifiers changed from: package-private */
    public ExpandedPair retrieveNextPair(BitArray row, List<ExpandedPair> previousPairs, int rowNumber) throws NotFoundException {
        FinderPattern pattern;
        DataCharacter rightChar;
        boolean isOddPattern = previousPairs.size() % 2 == 0;
        if (this.startFromEven) {
            isOddPattern = !isOddPattern;
        }
        boolean keepFinding = true;
        int forcedOffset = -1;
        do {
            findNextPair(row, previousPairs, forcedOffset);
            pattern = parseFoundFinderPattern(row, rowNumber, isOddPattern);
            if (pattern == null) {
                forcedOffset = getNextSecondBar(row, this.startEnd[0]);
                continue;
            } else {
                keepFinding = false;
                continue;
            }
        } while (keepFinding);
        DataCharacter leftChar = decodeDataCharacter(row, pattern, isOddPattern, true);
        if (previousPairs.isEmpty() || !previousPairs.get(previousPairs.size() - 1).mustBeLast()) {
            try {
                rightChar = decodeDataCharacter(row, pattern, isOddPattern, false);
            } catch (NotFoundException e) {
                rightChar = null;
            }
            return new ExpandedPair(leftChar, rightChar, pattern, true);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x006a  */
    private void findNextPair(BitArray row, List<ExpandedPair> previousPairs, int forcedOffset) throws NotFoundException {
        int rowOffset;
        int rowOffset2;
        int x;
        BitArray bitArray = row;
        int[] counters = getDecodeFinderCounters();
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int width = row.getSize();
        if (forcedOffset >= 0) {
            rowOffset = forcedOffset;
        } else if (previousPairs.isEmpty() != 0) {
            rowOffset = 0;
        } else {
            rowOffset = previousPairs.get(previousPairs.size() - 1).getFinderPattern().getStartEnd()[1];
            boolean searchingEvenPair = previousPairs.size() % 2 == 0;
            if (this.startFromEven) {
                searchingEvenPair = !searchingEvenPair;
            }
            rowOffset2 = rowOffset;
            boolean isWhite = false;
            while (rowOffset2 < width) {
                isWhite = !bitArray.get(rowOffset2);
                if (!isWhite) {
                    break;
                }
                rowOffset2++;
            }
            int patternStart = rowOffset2;
            int counterPosition = 0;
            boolean isWhite2 = isWhite;
            for (x = rowOffset2; x < width; x++) {
                if (bitArray.get(x) ^ isWhite2) {
                    counters[counterPosition] = counters[counterPosition] + 1;
                } else {
                    if (counterPosition == 3) {
                        if (searchingEvenPair) {
                            reverseCounters(counters);
                        }
                        if (isFinderPattern(counters)) {
                            this.startEnd[0] = patternStart;
                            this.startEnd[1] = x;
                            return;
                        }
                        if (searchingEvenPair) {
                            reverseCounters(counters);
                        }
                        patternStart += counters[0] + counters[1];
                        counters[0] = counters[2];
                        counters[1] = counters[3];
                        counters[2] = 0;
                        counters[3] = 0;
                        counterPosition--;
                    } else {
                        counterPosition++;
                    }
                    counters[counterPosition] = 1;
                    isWhite2 = !isWhite2;
                }
            }
            throw NotFoundException.getNotFoundInstance();
        }
        List<ExpandedPair> list = previousPairs;
        if (previousPairs.size() % 2 == 0) {
        }
        if (this.startFromEven) {
        }
        rowOffset2 = rowOffset;
        boolean isWhite3 = false;
        while (rowOffset2 < width) {
        }
        int patternStart2 = rowOffset2;
        int counterPosition2 = 0;
        boolean isWhite22 = isWhite3;
        while (x < width) {
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static void reverseCounters(int[] counters) {
        int length = counters.length;
        for (int i = 0; i < length / 2; i++) {
            int tmp = counters[i];
            counters[i] = counters[(length - i) - 1];
            counters[(length - i) - 1] = tmp;
        }
    }

    private FinderPattern parseFoundFinderPattern(BitArray row, int rowNumber, boolean oddPattern) {
        int start;
        int firstCounter;
        int firstElementStart;
        BitArray bitArray = row;
        if (oddPattern) {
            int firstElementStart2 = this.startEnd[0] - 1;
            while (firstElementStart2 >= 0 && !bitArray.get(firstElementStart2)) {
                firstElementStart2--;
            }
            int firstElementStart3 = firstElementStart2 + 1;
            firstCounter = this.startEnd[0] - firstElementStart3;
            start = firstElementStart3;
            firstElementStart = this.startEnd[1];
        } else {
            start = this.startEnd[0];
            firstElementStart = bitArray.getNextUnset(this.startEnd[1] + 1);
            firstCounter = firstElementStart - this.startEnd[1];
        }
        int start2 = start;
        int[] counters = getDecodeFinderCounters();
        System.arraycopy(counters, 0, counters, 1, counters.length - 1);
        counters[0] = firstCounter;
        try {
            FinderPattern finderPattern = new FinderPattern(parseFinderValue(counters, FINDER_PATTERNS), new int[]{start2, firstElementStart}, start2, firstElementStart, rowNumber);
            return finderPattern;
        } catch (NotFoundException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public DataCharacter decodeDataCharacter(BitArray row, FinderPattern pattern, boolean isOddPattern, boolean leftChar) throws NotFoundException {
        BitArray bitArray = row;
        int[] counters = getDataCharacterCounters();
        counters[0] = 0;
        int i = 1;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        counters[4] = 0;
        counters[5] = 0;
        counters[6] = 0;
        counters[7] = 0;
        if (leftChar) {
            recordPatternInReverse(bitArray, pattern.getStartEnd()[0], counters);
        } else {
            recordPattern(bitArray, pattern.getStartEnd()[1], counters);
            int i2 = 0;
            for (int j = counters.length - 1; i2 < j; j--) {
                int temp = counters[i2];
                counters[i2] = counters[j];
                counters[j] = temp;
                i2++;
            }
        }
        float elementWidth = ((float) count(counters)) / ((float) 17);
        float expectedElementWidth = ((float) (pattern.getStartEnd()[1] - pattern.getStartEnd()[0])) / 15.0f;
        float f = 0.3f;
        if (Math.abs(elementWidth - expectedElementWidth) / expectedElementWidth <= 0.3f) {
            int[] oddCounts = getOddCounts();
            int[] evenCounts = getEvenCounts();
            float[] oddRoundingErrors = getOddRoundingErrors();
            float[] evenRoundingErrors = getEvenRoundingErrors();
            int i3 = 0;
            while (i3 < counters.length) {
                float value = (1.0f * ((float) counters[i3])) / elementWidth;
                int count = (int) (0.5f + value);
                if (count < i) {
                    if (value >= f) {
                        count = 1;
                    } else {
                        throw NotFoundException.getNotFoundInstance();
                    }
                } else if (count > 8) {
                    if (value <= 8.7f) {
                        count = 8;
                    } else {
                        throw NotFoundException.getNotFoundInstance();
                    }
                }
                int offset = i3 >> 1;
                if ((i3 & 1) == 0) {
                    oddCounts[offset] = count;
                    oddRoundingErrors[offset] = value - ((float) count);
                } else {
                    evenCounts[offset] = count;
                    evenRoundingErrors[offset] = value - ((float) count);
                }
                i3++;
                i = 1;
                f = 0.3f;
            }
            adjustOddEvenCounts(17);
            int weightRowNumber = (((pattern.getValue() * 4) + (isOddPattern ? 0 : 2)) + (leftChar ^ true ? 1 : 0)) - 1;
            int oddSum = 0;
            int oddChecksumPortion = 0;
            for (int i4 = oddCounts.length - 1; i4 >= 0; i4--) {
                if (isNotA1left(pattern, isOddPattern, leftChar)) {
                    oddChecksumPortion += oddCounts[i4] * WEIGHTS[weightRowNumber][2 * i4];
                }
                oddSum += oddCounts[i4];
            }
            int evenChecksumPortion = 0;
            for (int i5 = evenCounts.length - 1; i5 >= 0; i5--) {
                if (isNotA1left(pattern, isOddPattern, leftChar)) {
                    evenChecksumPortion += evenCounts[i5] * WEIGHTS[weightRowNumber][(2 * i5) + 1];
                }
            }
            int i6 = oddChecksumPortion + evenChecksumPortion;
            if ((oddSum & 1) != 0 || oddSum > 13 || oddSum < 4) {
                throw NotFoundException.getNotFoundInstance();
            }
            int group = (13 - oddSum) / 2;
            int oddWidest = SYMBOL_WIDEST[group];
            int[] iArr = counters;
            int vOdd = RSSUtils.getRSSvalue(oddCounts, oddWidest, true);
            int i7 = oddWidest;
            int vEven = RSSUtils.getRSSvalue(evenCounts, 9 - oddWidest, false);
            int i8 = group;
            int i9 = vEven;
            return new DataCharacter((vOdd * EVEN_TOTAL_SUBSET[group]) + vEven + GSUM[group], i6);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static boolean isNotA1left(FinderPattern pattern, boolean isOddPattern, boolean leftChar) {
        return pattern.getValue() != 0 || !isOddPattern || !leftChar;
    }

    private void adjustOddEvenCounts(int numModules) throws NotFoundException {
        int oddSum = count(getOddCounts());
        int evenSum = count(getEvenCounts());
        int mismatch = (oddSum + evenSum) - numModules;
        boolean evenParityBad = false;
        boolean oddParityBad = (oddSum & 1) == 1;
        if ((evenSum & 1) == 0) {
            evenParityBad = true;
        }
        boolean incrementOdd = false;
        boolean decrementOdd = false;
        if (oddSum > 13) {
            decrementOdd = true;
        } else if (oddSum < 4) {
            incrementOdd = true;
        }
        boolean incrementEven = false;
        boolean decrementEven = false;
        if (evenSum > 13) {
            decrementEven = true;
        } else if (evenSum < 4) {
            incrementEven = true;
        }
        if (mismatch == 1) {
            if (oddParityBad) {
                if (!evenParityBad) {
                    decrementOdd = true;
                } else {
                    throw NotFoundException.getNotFoundInstance();
                }
            } else if (evenParityBad) {
                decrementEven = true;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } else if (mismatch == -1) {
            if (oddParityBad) {
                if (!evenParityBad) {
                    incrementOdd = true;
                } else {
                    throw NotFoundException.getNotFoundInstance();
                }
            } else if (evenParityBad) {
                incrementEven = true;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } else if (mismatch != 0) {
            throw NotFoundException.getNotFoundInstance();
        } else if (oddParityBad) {
            if (!evenParityBad) {
                throw NotFoundException.getNotFoundInstance();
            } else if (oddSum < evenSum) {
                incrementOdd = true;
                decrementEven = true;
            } else {
                decrementOdd = true;
                incrementEven = true;
            }
        } else if (evenParityBad) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (incrementOdd) {
            if (!decrementOdd) {
                increment(getOddCounts(), getOddRoundingErrors());
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
        if (decrementOdd) {
            decrement(getOddCounts(), getOddRoundingErrors());
        }
        if (incrementEven) {
            if (!decrementEven) {
                increment(getEvenCounts(), getOddRoundingErrors());
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
        if (decrementEven) {
            decrement(getEvenCounts(), getEvenRoundingErrors());
        }
    }
}

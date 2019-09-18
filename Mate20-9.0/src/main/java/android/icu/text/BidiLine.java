package android.icu.text;

import android.icu.text.Bidi;
import java.util.Arrays;

final class BidiLine {
    BidiLine() {
    }

    static void setTrailingWSStart(Bidi bidi) {
        byte[] dirProps = bidi.dirProps;
        byte[] levels = bidi.levels;
        int start = bidi.length;
        byte paraLevel = bidi.paraLevel;
        if (dirProps[start - 1] == 7) {
            bidi.trailingWSStart = start;
            return;
        }
        while (start > 0 && (Bidi.DirPropFlag(dirProps[start - 1]) & Bidi.MASK_WS) != 0) {
            start--;
        }
        while (start > 0 && levels[start - 1] == paraLevel) {
            start--;
        }
        bidi.trailingWSStart = start;
    }

    static Bidi setLine(Bidi paraBidi, int start, int limit) {
        Bidi lineBidi = new Bidi();
        int length = limit - start;
        lineBidi.resultLength = length;
        lineBidi.originalLength = length;
        lineBidi.length = length;
        lineBidi.text = new char[length];
        System.arraycopy(paraBidi.text, start, lineBidi.text, 0, length);
        lineBidi.paraLevel = paraBidi.GetParaLevelAt(start);
        lineBidi.paraCount = paraBidi.paraCount;
        lineBidi.runs = new BidiRun[0];
        lineBidi.reorderingMode = paraBidi.reorderingMode;
        lineBidi.reorderingOptions = paraBidi.reorderingOptions;
        if (paraBidi.controlCount > 0) {
            for (int j = start; j < limit; j++) {
                if (Bidi.IsBidiControlChar(paraBidi.text[j])) {
                    lineBidi.controlCount++;
                }
            }
            lineBidi.resultLength -= lineBidi.controlCount;
        }
        lineBidi.getDirPropsMemory(length);
        lineBidi.dirProps = lineBidi.dirPropsMemory;
        System.arraycopy(paraBidi.dirProps, start, lineBidi.dirProps, 0, length);
        lineBidi.getLevelsMemory(length);
        lineBidi.levels = lineBidi.levelsMemory;
        System.arraycopy(paraBidi.levels, start, lineBidi.levels, 0, length);
        lineBidi.runCount = -1;
        if (paraBidi.direction == 2) {
            byte[] levels = lineBidi.levels;
            setTrailingWSStart(lineBidi);
            int trailingWSStart = lineBidi.trailingWSStart;
            if (trailingWSStart == 0) {
                lineBidi.direction = (byte) (lineBidi.paraLevel & 1);
            } else {
                byte level = (byte) (levels[0] & 1);
                if (trailingWSStart >= length || (lineBidi.paraLevel & 1) == level) {
                    int i = 1;
                    while (true) {
                        if (i == trailingWSStart) {
                            lineBidi.direction = level;
                            break;
                        } else if ((levels[i] & 1) != level) {
                            lineBidi.direction = 2;
                            break;
                        } else {
                            i++;
                        }
                    }
                } else {
                    lineBidi.direction = 2;
                }
            }
            switch (lineBidi.direction) {
                case 0:
                    lineBidi.paraLevel = (byte) ((lineBidi.paraLevel + 1) & -2);
                    lineBidi.trailingWSStart = 0;
                    break;
                case 1:
                    lineBidi.paraLevel = (byte) (1 | lineBidi.paraLevel);
                    lineBidi.trailingWSStart = 0;
                    break;
            }
        } else {
            lineBidi.direction = paraBidi.direction;
            if (paraBidi.trailingWSStart <= start) {
                lineBidi.trailingWSStart = 0;
            } else if (paraBidi.trailingWSStart < limit) {
                lineBidi.trailingWSStart = paraBidi.trailingWSStart - start;
            } else {
                lineBidi.trailingWSStart = length;
            }
        }
        lineBidi.paraBidi = paraBidi;
        return lineBidi;
    }

    static byte getLevelAt(Bidi bidi, int charIndex) {
        if (bidi.direction != 2 || charIndex >= bidi.trailingWSStart) {
            return bidi.GetParaLevelAt(charIndex);
        }
        return bidi.levels[charIndex];
    }

    static byte[] getLevels(Bidi bidi) {
        int start = bidi.trailingWSStart;
        int length = bidi.length;
        if (start != length) {
            Arrays.fill(bidi.levels, start, length, bidi.paraLevel);
            bidi.trailingWSStart = length;
        }
        if (length >= bidi.levels.length) {
            return bidi.levels;
        }
        byte[] levels = new byte[length];
        System.arraycopy(bidi.levels, 0, levels, 0, length);
        return levels;
    }

    static BidiRun getLogicalRun(Bidi bidi, int logicalPosition) {
        BidiRun newRun = new BidiRun();
        getRuns(bidi);
        int runCount = bidi.runCount;
        int visualStart = 0;
        int logicalLimit = 0;
        BidiRun iRun = bidi.runs[0];
        for (int i = 0; i < runCount; i++) {
            iRun = bidi.runs[i];
            logicalLimit = (iRun.start + iRun.limit) - visualStart;
            if (logicalPosition >= iRun.start && logicalPosition < logicalLimit) {
                break;
            }
            visualStart = iRun.limit;
        }
        newRun.start = iRun.start;
        newRun.limit = logicalLimit;
        newRun.level = iRun.level;
        return newRun;
    }

    static BidiRun getVisualRun(Bidi bidi, int runIndex) {
        int limit;
        int start = bidi.runs[runIndex].start;
        byte level = bidi.runs[runIndex].level;
        if (runIndex > 0) {
            limit = (bidi.runs[runIndex].limit + start) - bidi.runs[runIndex - 1].limit;
        } else {
            limit = bidi.runs[0].limit + start;
        }
        return new BidiRun(start, limit, level);
    }

    static void getSingleRun(Bidi bidi, byte level) {
        bidi.runs = bidi.simpleRuns;
        bidi.runCount = 1;
        bidi.runs[0] = new BidiRun(0, bidi.length, level);
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r10v0, types: [byte] */
    private static void reorderLine(Bidi bidi, byte minLevel, int maxLevel) {
        if (maxLevel > (minLevel | 1)) {
            int minLevel2 = (byte) (minLevel + 1);
            BidiRun[] runs = bidi.runs;
            byte[] levels = bidi.levels;
            int runCount = bidi.runCount;
            if (bidi.trailingWSStart < bidi.length) {
                runCount--;
            }
            while (true) {
                maxLevel = (byte) (maxLevel - 1);
                if (maxLevel < minLevel2) {
                    break;
                }
                int firstRun = 0;
                while (true) {
                    if (firstRun < runCount && levels[runs[firstRun].start] < maxLevel) {
                        firstRun++;
                    } else if (firstRun >= runCount) {
                        break;
                    } else {
                        int limitRun = firstRun;
                        do {
                            limitRun++;
                            if (limitRun >= runCount) {
                                break;
                            }
                        } while (levels[runs[limitRun].start] >= maxLevel);
                        for (int endRun = limitRun - 1; firstRun < endRun; endRun--) {
                            BidiRun tempRun = runs[firstRun];
                            runs[firstRun] = runs[endRun];
                            runs[endRun] = tempRun;
                            firstRun++;
                        }
                        if (limitRun == runCount) {
                            break;
                        }
                        firstRun = limitRun + 1;
                    }
                }
            }
            if ((minLevel2 & 1) == 0) {
                int firstRun2 = 0;
                if (bidi.trailingWSStart == bidi.length) {
                    runCount--;
                }
                while (firstRun2 < runCount) {
                    BidiRun tempRun2 = runs[firstRun2];
                    runs[firstRun2] = runs[runCount];
                    runs[runCount] = tempRun2;
                    firstRun2++;
                    runCount--;
                }
            }
        }
    }

    static int getRunFromLogicalIndex(Bidi bidi, int logicalIndex) {
        BidiRun[] runs = bidi.runs;
        int runCount = bidi.runCount;
        int visualStart = 0;
        for (int i = 0; i < runCount; i++) {
            int length = runs[i].limit - visualStart;
            int logicalStart = runs[i].start;
            if (logicalIndex >= logicalStart && logicalIndex < logicalStart + length) {
                return i;
            }
            visualStart += length;
        }
        throw new IllegalStateException("Internal ICU error in getRunFromLogicalIndex");
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r7v6, types: [byte] */
    static void getRuns(Bidi bidi) {
        Bidi bidi2 = bidi;
        if (bidi2.runCount < 0) {
            if (bidi2.direction != 2) {
                getSingleRun(bidi2, bidi2.paraLevel);
            } else {
                int length = bidi2.length;
                byte[] levels = bidi2.levels;
                int limit = bidi2.trailingWSStart;
                int runCount = 0;
                int level = -1;
                for (int i = 0; i < limit; i++) {
                    if (levels[i] != level) {
                        runCount++;
                        level = levels[i];
                    }
                }
                if (runCount == 1 && limit == length) {
                    getSingleRun(bidi2, levels[0]);
                } else {
                    byte minLevel = Bidi.LEVEL_DEFAULT_LTR;
                    byte maxLevel = 0;
                    if (limit < length) {
                        runCount++;
                    }
                    bidi2.getRunsMemory(runCount);
                    BidiRun[] runs = bidi2.runsMemory;
                    int runIndex = 0;
                    int i2 = 0;
                    do {
                        int start = i2;
                        byte level2 = levels[i2];
                        if (level2 < minLevel) {
                            minLevel = level2;
                        }
                        if (level2 > maxLevel) {
                            maxLevel = level2;
                        }
                        do {
                            i2++;
                            if (i2 >= limit) {
                                break;
                            }
                        } while (levels[i2] == level2);
                        runs[runIndex] = new BidiRun(start, i2 - start, level2);
                        runIndex++;
                    } while (i2 < limit);
                    if (limit < length) {
                        runs[runIndex] = new BidiRun(limit, length - limit, bidi2.paraLevel);
                        if (bidi2.paraLevel < minLevel) {
                            minLevel = bidi2.paraLevel;
                        }
                    }
                    bidi2.runs = runs;
                    bidi2.runCount = runCount;
                    reorderLine(bidi2, minLevel, maxLevel);
                    int limit2 = 0;
                    for (int i3 = 0; i3 < runCount; i3++) {
                        runs[i3].level = levels[runs[i3].start];
                        BidiRun bidiRun = runs[i3];
                        int i4 = bidiRun.limit + limit2;
                        bidiRun.limit = i4;
                        limit2 = i4;
                    }
                    if (runIndex < runCount) {
                        runs[(bidi2.paraLevel & 1) != 0 ? 0 : runIndex].level = bidi2.paraLevel;
                    }
                }
            }
            if (bidi2.insertPoints.size > 0) {
                for (int ip = 0; ip < bidi2.insertPoints.size; ip++) {
                    Bidi.Point point = bidi2.insertPoints.points[ip];
                    bidi2.runs[getRunFromLogicalIndex(bidi2, point.pos)].insertRemove |= point.flag;
                }
            }
            if (bidi2.controlCount > 0) {
                int ic = 0;
                while (true) {
                    int ic2 = ic;
                    if (ic2 >= bidi2.length) {
                        break;
                    }
                    if (Bidi.IsBidiControlChar(bidi2.text[ic2])) {
                        bidi2.runs[getRunFromLogicalIndex(bidi2, ic2)].insertRemove--;
                    }
                    ic = ic2 + 1;
                }
            }
        }
    }

    static int[] prepareReorder(byte[] levels, byte[] pMinLevel, byte[] pMaxLevel) {
        if (levels == null || levels.length <= 0) {
            return null;
        }
        byte minLevel = Bidi.LEVEL_DEFAULT_LTR;
        byte maxLevel = 0;
        int start = levels.length;
        while (start > 0) {
            start--;
            byte level = levels[start];
            if (level < 0 || level > 126) {
                return null;
            }
            if (level < minLevel) {
                minLevel = level;
            }
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
        pMinLevel[0] = minLevel;
        pMaxLevel[0] = maxLevel;
        int[] indexMap = new int[levels.length];
        int start2 = levels.length;
        while (start2 > 0) {
            start2--;
            indexMap[start2] = start2;
        }
        return indexMap;
    }

    static int[] reorderLogical(byte[] levels) {
        byte[] aMinLevel = new byte[1];
        byte[] aMaxLevel = new byte[1];
        int[] indexMap = prepareReorder(levels, aMinLevel, aMaxLevel);
        if (indexMap == null) {
            return null;
        }
        byte minLevel = aMinLevel[0];
        byte maxLevel = aMaxLevel[0];
        if (minLevel == maxLevel && (minLevel & 1) == 0) {
            return indexMap;
        }
        byte minLevel2 = (byte) (minLevel | 1);
        do {
            int start = 0;
            while (true) {
                if (start < levels.length && levels[start] < maxLevel) {
                    start++;
                } else if (start >= levels.length) {
                    break;
                } else {
                    int limit = start;
                    do {
                        limit++;
                        if (limit >= levels.length) {
                            break;
                        }
                    } while (levels[limit] >= maxLevel);
                    int sumOfSosEos = (start + limit) - 1;
                    do {
                        indexMap[start] = sumOfSosEos - indexMap[start];
                        start++;
                    } while (start < limit);
                    if (limit == levels.length) {
                        break;
                    }
                    start = limit + 1;
                }
            }
            maxLevel = (byte) (maxLevel - 1);
        } while (maxLevel >= minLevel2);
        return indexMap;
    }

    static int[] reorderVisual(byte[] levels) {
        byte[] aMinLevel = new byte[1];
        byte[] aMaxLevel = new byte[1];
        int[] indexMap = prepareReorder(levels, aMinLevel, aMaxLevel);
        if (indexMap == null) {
            return null;
        }
        byte minLevel = aMinLevel[0];
        byte maxLevel = aMaxLevel[0];
        if (minLevel == maxLevel && (minLevel & 1) == 0) {
            return indexMap;
        }
        byte minLevel2 = (byte) (minLevel | 1);
        do {
            int start = 0;
            while (true) {
                if (start < levels.length && levels[start] < maxLevel) {
                    start++;
                } else if (start >= levels.length) {
                    break;
                } else {
                    int limit = start;
                    do {
                        limit++;
                        if (limit >= levels.length) {
                            break;
                        }
                    } while (levels[limit] >= maxLevel);
                    for (int end = limit - 1; start < end; end--) {
                        int temp = indexMap[start];
                        indexMap[start] = indexMap[end];
                        indexMap[end] = temp;
                        start++;
                    }
                    if (limit == levels.length) {
                        break;
                    }
                    start = limit + 1;
                }
            }
            maxLevel = (byte) (maxLevel - 1);
        } while (maxLevel >= minLevel2);
        return indexMap;
    }

    static int getVisualIndex(Bidi bidi, int logicalIndex) {
        int visualIndex;
        int limit;
        int start;
        int visualIndex2 = -1;
        int i = 0;
        switch (bidi.direction) {
            case 0:
                visualIndex = logicalIndex;
                break;
            case 1:
                visualIndex = (bidi.length - logicalIndex) - 1;
                break;
            default:
                getRuns(bidi);
                BidiRun[] runs = bidi.runs;
                int visualStart = 0;
                int i2 = 0;
                while (true) {
                    if (i2 < bidi.runCount) {
                        int length = runs[i2].limit - visualStart;
                        int offset = logicalIndex - runs[i2].start;
                        if (offset < 0 || offset >= length) {
                            visualStart += length;
                            i2++;
                        } else if (runs[i2].isEvenRun()) {
                            visualIndex2 = visualStart + offset;
                        } else {
                            visualIndex2 = ((visualStart + length) - offset) - 1;
                        }
                    }
                }
                if (i2 < bidi.runCount) {
                    visualIndex = visualIndex2;
                    break;
                } else {
                    return -1;
                }
        }
        if (bidi.insertPoints.size > 0) {
            BidiRun[] runs2 = bidi.runs;
            int visualStart2 = 0;
            int markFound = 0;
            while (true) {
                int length2 = runs2[i].limit - visualStart2;
                int insertRemove = runs2[i].insertRemove;
                if ((insertRemove & 5) > 0) {
                    markFound++;
                }
                if (visualIndex < runs2[i].limit) {
                    return visualIndex + markFound;
                }
                if ((insertRemove & 10) > 0) {
                    markFound++;
                }
                i++;
                visualStart2 += length2;
            }
        } else if (bidi.controlCount <= 0) {
            return visualIndex;
        } else {
            BidiRun[] runs3 = bidi.runs;
            int visualStart3 = 0;
            int controlFound = 0;
            if (Bidi.IsBidiControlChar(bidi.text[logicalIndex])) {
                return -1;
            }
            while (true) {
                int i3 = i;
                int length3 = runs3[i3].limit - visualStart3;
                int insertRemove2 = runs3[i3].insertRemove;
                if (visualIndex >= runs3[i3].limit) {
                    controlFound -= insertRemove2;
                    visualStart3 += length3;
                    i = i3 + 1;
                } else if (insertRemove2 == 0) {
                    return visualIndex - controlFound;
                } else {
                    if (runs3[i3].isEvenRun()) {
                        start = runs3[i3].start;
                        limit = logicalIndex;
                    } else {
                        start = logicalIndex + 1;
                        limit = runs3[i3].start + length3;
                    }
                    int controlFound2 = controlFound;
                    for (int j = start; j < limit; j++) {
                        if (Bidi.IsBidiControlChar(bidi.text[j])) {
                            controlFound2++;
                        }
                    }
                    return visualIndex - controlFound2;
                }
            }
        }
    }

    static int getLogicalIndex(Bidi bidi, int visualIndex) {
        int i;
        int i2;
        int length;
        int insertRemove;
        Bidi bidi2 = bidi;
        int visualIndex2 = visualIndex;
        BidiRun[] runs = bidi2.runs;
        int runCount = bidi2.runCount;
        if (bidi2.insertPoints.size > 0) {
            int visualStart = 0;
            int markFound = 0;
            int i3 = 0;
            while (true) {
                int length2 = runs[i3].limit - visualStart;
                int insertRemove2 = runs[i3].insertRemove;
                if ((insertRemove2 & 5) > 0) {
                    if (visualIndex2 <= visualStart + markFound) {
                        return -1;
                    }
                    markFound++;
                }
                if (visualIndex2 < runs[i3].limit + markFound) {
                    visualIndex2 -= markFound;
                    break;
                }
                if ((insertRemove2 & 10) > 0) {
                    if (visualIndex2 == visualStart + length2 + markFound) {
                        return -1;
                    }
                    markFound++;
                }
                i3++;
                visualStart += length2;
            }
        } else if (bidi2.controlCount > 0) {
            int visualStart2 = 0;
            int controlFound = 0;
            int i4 = 0;
            while (true) {
                length = runs[i4].limit - visualStart2;
                insertRemove = runs[i4].insertRemove;
                if (visualIndex2 < (runs[i4].limit - controlFound) + insertRemove) {
                    break;
                }
                controlFound -= insertRemove;
                i4++;
                visualStart2 += length;
            }
            if (insertRemove == 0) {
                visualIndex2 += controlFound;
            } else {
                int logicalStart = runs[i4].start;
                boolean evenRun = runs[i4].isEvenRun();
                int logicalEnd = (logicalStart + length) - 1;
                int controlFound2 = controlFound;
                int j = 0;
                while (j < length) {
                    if (Bidi.IsBidiControlChar(bidi2.text[evenRun ? logicalStart + j : logicalEnd - j])) {
                        controlFound2++;
                    }
                    if (visualIndex2 + controlFound2 == visualStart2 + j) {
                        break;
                    }
                    j++;
                    bidi2 = bidi;
                }
                visualIndex2 += controlFound2;
            }
        }
        if (runCount <= 10) {
            int i5 = 0;
            while (true) {
                i = i5;
                if (visualIndex2 < runs[i].limit) {
                    break;
                }
                i5 = i + 1;
            }
        } else {
            int begin = 0;
            int limit = runCount;
            while (true) {
                i2 = (begin + limit) >>> 1;
                if (visualIndex2 >= runs[i2].limit) {
                    begin = i2 + 1;
                } else if (i2 == 0 || visualIndex2 >= runs[i2 - 1].limit) {
                    i = i2;
                } else {
                    limit = i2;
                }
            }
            i = i2;
        }
        int start = runs[i].start;
        if (!runs[i].isEvenRun()) {
            return ((runs[i].limit + start) - visualIndex2) - 1;
        }
        if (i > 0) {
            visualIndex2 -= runs[i - 1].limit;
        }
        return start + visualIndex2;
    }

    static int[] getLogicalMap(Bidi bidi) {
        int visualStart;
        int logicalStart;
        int visualStart2;
        Bidi bidi2 = bidi;
        BidiRun[] runs = bidi2.runs;
        int[] indexMap = new int[bidi2.length];
        if (bidi2.length > bidi2.resultLength) {
            Arrays.fill(indexMap, -1);
        }
        int visualStart3 = 0;
        for (int j = 0; j < bidi2.runCount; j++) {
            int logicalStart2 = runs[j].start;
            int visualLimit = runs[j].limit;
            if (runs[j].isEvenRun()) {
                while (true) {
                    logicalStart = logicalStart2 + 1;
                    visualStart2 = visualStart3 + 1;
                    indexMap[logicalStart2] = visualStart3;
                    if (visualStart2 >= visualLimit) {
                        break;
                    }
                    logicalStart2 = logicalStart;
                    visualStart3 = visualStart2;
                }
                int i = logicalStart;
                visualStart3 = visualStart2;
            } else {
                int logicalStart3 = logicalStart2 + (visualLimit - visualStart3);
                while (true) {
                    logicalStart3--;
                    visualStart = visualStart3 + 1;
                    indexMap[logicalStart3] = visualStart3;
                    if (visualStart >= visualLimit) {
                        break;
                    }
                    visualStart3 = visualStart;
                }
                visualStart3 = visualStart;
            }
        }
        if (bidi2.insertPoints.size > 0) {
            int markFound = 0;
            int runCount = bidi2.runCount;
            BidiRun[] runs2 = bidi2.runs;
            int visualStart4 = 0;
            int i2 = 0;
            while (i2 < runCount) {
                int length = runs2[i2].limit - visualStart4;
                int insertRemove = runs2[i2].insertRemove;
                if ((insertRemove & 5) > 0) {
                    markFound++;
                }
                if (markFound > 0) {
                    int logicalStart4 = runs2[i2].start;
                    int logicalLimit = logicalStart4 + length;
                    for (int j2 = logicalStart4; j2 < logicalLimit; j2++) {
                        indexMap[j2] = indexMap[j2] + markFound;
                    }
                }
                if ((insertRemove & 10) > 0) {
                    markFound++;
                }
                i2++;
                visualStart4 += length;
            }
        } else if (bidi2.controlCount > 0) {
            int runCount2 = bidi2.runCount;
            BidiRun[] runs3 = bidi2.runs;
            int visualStart5 = 0;
            int j3 = 0;
            int i3 = 0;
            while (i3 < runCount2) {
                int length2 = runs3[i3].limit - visualStart5;
                int insertRemove2 = runs3[i3].insertRemove;
                if (j3 - insertRemove2 != 0) {
                    int logicalStart5 = runs3[i3].start;
                    boolean evenRun = runs3[i3].isEvenRun();
                    int logicalLimit2 = logicalStart5 + length2;
                    if (insertRemove2 == 0) {
                        for (int j4 = logicalStart5; j4 < logicalLimit2; j4++) {
                            indexMap[j4] = indexMap[j4] - j3;
                        }
                    } else {
                        int controlFound = j3;
                        for (int j5 = 0; j5 < length2; j5++) {
                            int k = evenRun ? logicalStart5 + j5 : (logicalLimit2 - j5) - 1;
                            if (Bidi.IsBidiControlChar(bidi2.text[k])) {
                                controlFound++;
                                indexMap[k] = -1;
                            } else {
                                indexMap[k] = indexMap[k] - controlFound;
                            }
                        }
                        j3 = controlFound;
                    }
                }
                i3++;
                visualStart5 += length2;
            }
            int controlFound2 = visualStart5;
        }
        return indexMap;
    }

    static int[] getVisualMap(Bidi bidi) {
        int allocLength;
        int idx;
        int idx2;
        int logicalStart;
        Bidi bidi2 = bidi;
        BidiRun[] runs = bidi2.runs;
        if (bidi2.length > bidi2.resultLength) {
            allocLength = bidi2.length;
        } else {
            allocLength = bidi2.resultLength;
        }
        int[] indexMap = new int[allocLength];
        int idx3 = 0;
        int visualStart = 0;
        for (int j = 0; j < bidi2.runCount; j++) {
            int logicalStart2 = runs[j].start;
            int visualLimit = runs[j].limit;
            if (runs[j].isEvenRun()) {
                while (true) {
                    idx2 = idx3 + 1;
                    logicalStart = logicalStart2 + 1;
                    indexMap[idx3] = logicalStart2;
                    visualStart++;
                    if (visualStart >= visualLimit) {
                        break;
                    }
                    idx3 = idx2;
                    logicalStart2 = logicalStart;
                }
                idx3 = idx2;
                int i = logicalStart;
            } else {
                int logicalStart3 = logicalStart2 + (visualLimit - visualStart);
                while (true) {
                    idx = idx3 + 1;
                    logicalStart3--;
                    indexMap[idx3] = logicalStart3;
                    visualStart++;
                    if (visualStart >= visualLimit) {
                        break;
                    }
                    idx3 = idx;
                }
                idx3 = idx;
            }
        }
        if (bidi2.insertPoints.size > 0) {
            int runCount = bidi2.runCount;
            BidiRun[] runs2 = bidi2.runs;
            int markFound = 0;
            for (int i2 = 0; i2 < runCount; i2++) {
                int insertRemove = runs2[i2].insertRemove;
                if ((insertRemove & 5) > 0) {
                    markFound++;
                }
                if ((insertRemove & 10) > 0) {
                    markFound++;
                }
            }
            int k = bidi2.resultLength;
            int i3 = runCount - 1;
            while (i3 >= 0 && markFound > 0) {
                int insertRemove2 = runs2[i3].insertRemove;
                if ((insertRemove2 & 10) > 0) {
                    k--;
                    indexMap[k] = -1;
                    markFound--;
                }
                int visualStart2 = i3 > 0 ? runs2[i3 - 1].limit : 0;
                for (int j2 = runs2[i3].limit - 1; j2 >= visualStart2 && markFound > 0; j2--) {
                    k--;
                    indexMap[k] = indexMap[j2];
                }
                if ((insertRemove2 & 5) > 0) {
                    k--;
                    indexMap[k] = -1;
                    markFound--;
                }
                i3--;
            }
        } else if (bidi2.controlCount > 0) {
            int runCount2 = bidi2.runCount;
            BidiRun[] runs3 = bidi2.runs;
            int k2 = 0;
            int visualStart3 = 0;
            int i4 = 0;
            while (i4 < runCount2) {
                int length = runs3[i4].limit - visualStart3;
                int insertRemove3 = runs3[i4].insertRemove;
                if (insertRemove3 == 0 && k2 == visualStart3) {
                    k2 += length;
                } else if (insertRemove3 == 0) {
                    int visualLimit2 = runs3[i4].limit;
                    int k3 = k2;
                    int j3 = visualStart3;
                    while (j3 < visualLimit2) {
                        indexMap[k3] = indexMap[j3];
                        j3++;
                        k3++;
                    }
                    k2 = k3;
                } else {
                    int logicalStart4 = runs3[i4].start;
                    boolean evenRun = runs3[i4].isEvenRun();
                    int logicalEnd = (logicalStart4 + length) - 1;
                    int k4 = k2;
                    for (int j4 = 0; j4 < length; j4++) {
                        int m = evenRun ? logicalStart4 + j4 : logicalEnd - j4;
                        if (!Bidi.IsBidiControlChar(bidi2.text[m])) {
                            indexMap[k4] = m;
                            k4++;
                        }
                    }
                    k2 = k4;
                }
                i4++;
                visualStart3 += length;
            }
            int i5 = visualStart3;
        }
        if (allocLength == bidi2.resultLength) {
            return indexMap;
        }
        int[] newMap = new int[bidi2.resultLength];
        System.arraycopy(indexMap, 0, newMap, 0, bidi2.resultLength);
        return newMap;
    }

    static int[] invertMap(int[] srcMap) {
        int destLength = -1;
        int count = 0;
        for (int srcEntry : srcMap) {
            if (srcEntry > destLength) {
                destLength = srcEntry;
            }
            if (srcEntry >= 0) {
                count++;
            }
        }
        int destLength2 = destLength + 1;
        int[] destMap = new int[destLength2];
        if (count < destLength2) {
            Arrays.fill(destMap, -1);
        }
        for (int i = 0; i < srcLength; i++) {
            int srcEntry2 = srcMap[i];
            if (srcEntry2 >= 0) {
                destMap[srcEntry2] = i;
            }
        }
        return destMap;
    }
}

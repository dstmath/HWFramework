package android.icu.text;

import java.util.Arrays;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

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
        if (paraBidi.direction == (byte) 2) {
            byte[] levels = lineBidi.levels;
            setTrailingWSStart(lineBidi);
            int trailingWSStart = lineBidi.trailingWSStart;
            if (trailingWSStart == 0) {
                lineBidi.direction = (byte) (lineBidi.paraLevel & 1);
            } else {
                byte level = (byte) (levels[0] & 1);
                if (trailingWSStart >= length || (lineBidi.paraLevel & 1) == level) {
                    for (int i = 1; i != trailingWSStart; i++) {
                        if ((levels[i] & 1) != level) {
                            lineBidi.direction = (byte) 2;
                            break;
                        }
                    }
                    lineBidi.direction = level;
                } else {
                    lineBidi.direction = (byte) 2;
                }
            }
            switch (lineBidi.direction) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                    lineBidi.paraLevel = (byte) ((lineBidi.paraLevel + 1) & -2);
                    lineBidi.trailingWSStart = 0;
                    break;
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    lineBidi.paraLevel = (byte) (lineBidi.paraLevel | 1);
                    lineBidi.trailingWSStart = 0;
                    break;
                default:
                    break;
            }
        }
        lineBidi.direction = paraBidi.direction;
        if (paraBidi.trailingWSStart <= start) {
            lineBidi.trailingWSStart = 0;
        } else if (paraBidi.trailingWSStart < limit) {
            lineBidi.trailingWSStart = paraBidi.trailingWSStart - start;
        } else {
            lineBidi.trailingWSStart = length;
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
            limit = start + bidi.runs[0].limit;
        }
        return new BidiRun(start, limit, level);
    }

    static void getSingleRun(Bidi bidi, byte level) {
        bidi.runs = bidi.simpleRuns;
        bidi.runCount = 1;
        bidi.runs[0] = new BidiRun(0, bidi.length, level);
    }

    private static void reorderLine(Bidi bidi, byte minLevel, byte maxLevel) {
        if (maxLevel > (minLevel | 1)) {
            int firstRun;
            BidiRun tempRun;
            minLevel = (byte) (minLevel + 1);
            BidiRun[] runs = bidi.runs;
            byte[] levels = bidi.levels;
            int runCount = bidi.runCount;
            if (bidi.trailingWSStart < bidi.length) {
                runCount--;
            }
            while (true) {
                maxLevel = (byte) (maxLevel - 1);
                if (maxLevel < minLevel) {
                    break;
                }
                firstRun = 0;
                while (true) {
                    if (firstRun >= runCount || levels[runs[firstRun].start] >= maxLevel) {
                        if (firstRun >= runCount) {
                            break;
                        }
                        int limitRun = firstRun;
                        do {
                            limitRun++;
                            if (limitRun >= runCount) {
                                break;
                            }
                        } while (levels[runs[limitRun].start] >= maxLevel);
                        for (int endRun = limitRun - 1; firstRun < endRun; endRun--) {
                            tempRun = runs[firstRun];
                            runs[firstRun] = runs[endRun];
                            runs[endRun] = tempRun;
                            firstRun++;
                        }
                        if (limitRun == runCount) {
                            break;
                        }
                        firstRun = limitRun + 1;
                    } else {
                        firstRun++;
                    }
                }
            }
            if ((minLevel & 1) == 0) {
                firstRun = 0;
                if (bidi.trailingWSStart == bidi.length) {
                    runCount--;
                }
                while (firstRun < runCount) {
                    tempRun = runs[firstRun];
                    runs[firstRun] = runs[runCount];
                    runs[runCount] = tempRun;
                    firstRun++;
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

    static void getRuns(Bidi bidi) {
        if (bidi.runCount < 0) {
            int runIndex;
            BidiRun bidiRun;
            int i;
            byte b = bidi.direction;
            if (r0 != 2) {
                getSingleRun(bidi, bidi.paraLevel);
            } else {
                int i2;
                int length = bidi.length;
                byte[] levels = bidi.levels;
                byte level = (byte) -1;
                int limit = bidi.trailingWSStart;
                int runCount = 0;
                for (i2 = 0; i2 < limit; i2++) {
                    if (levels[i2] != level) {
                        runCount++;
                        level = levels[i2];
                    }
                }
                if (runCount == 1 && limit == length) {
                    getSingleRun(bidi, levels[0]);
                } else {
                    byte minLevel = Bidi.LEVEL_DEFAULT_LTR;
                    byte maxLevel = (byte) 0;
                    if (limit < length) {
                        runCount++;
                    }
                    bidi.getRunsMemory(runCount);
                    BidiRun[] runs = bidi.runsMemory;
                    runIndex = 0;
                    i2 = 0;
                    do {
                        int start = i2;
                        level = levels[i2];
                        if (level < minLevel) {
                            minLevel = level;
                        }
                        if (level > maxLevel) {
                            maxLevel = level;
                        }
                        do {
                            i2++;
                            if (i2 >= limit) {
                                break;
                            }
                        } while (levels[i2] == level);
                        runs[runIndex] = new BidiRun(start, i2 - start, level);
                        runIndex++;
                    } while (i2 < limit);
                    if (limit < length) {
                        runs[runIndex] = new BidiRun(limit, length - limit, bidi.paraLevel);
                        b = bidi.paraLevel;
                        if (r0 < minLevel) {
                            minLevel = bidi.paraLevel;
                        }
                    }
                    bidi.runs = runs;
                    bidi.runCount = runCount;
                    reorderLine(bidi, minLevel, maxLevel);
                    limit = 0;
                    for (i2 = 0; i2 < runCount; i2++) {
                        bidiRun = runs[i2];
                        bidiRun.level = levels[runs[i2].start];
                        bidiRun = runs[i2];
                        limit += bidiRun.limit;
                        bidiRun.limit = limit;
                    }
                    if (runIndex < runCount) {
                        int trailingRun;
                        if ((bidi.paraLevel & 1) != 0) {
                            trailingRun = 0;
                        } else {
                            trailingRun = runIndex;
                        }
                        bidiRun = runs[trailingRun];
                        bidiRun.level = bidi.paraLevel;
                    }
                }
            }
            if (bidi.insertPoints.size > 0) {
                int ip = 0;
                while (true) {
                    i = bidi.insertPoints.size;
                    if (ip >= r0) {
                        break;
                    }
                    Point point = bidi.insertPoints.points[ip];
                    runIndex = getRunFromLogicalIndex(bidi, point.pos);
                    bidiRun = bidi.runs[runIndex];
                    bidiRun.insertRemove |= point.flag;
                    ip++;
                }
            }
            if (bidi.controlCount > 0) {
                int ic = 0;
                while (true) {
                    i = bidi.length;
                    if (ic >= r0) {
                        break;
                    }
                    if (Bidi.IsBidiControlChar(bidi.text[ic])) {
                        runIndex = getRunFromLogicalIndex(bidi, ic);
                        bidiRun = bidi.runs[runIndex];
                        bidiRun.insertRemove--;
                    }
                    ic++;
                }
            }
        }
    }

    static int[] prepareReorder(byte[] levels, byte[] pMinLevel, byte[] pMaxLevel) {
        if (levels == null || levels.length <= 0) {
            return null;
        }
        byte minLevel = Bidi.LEVEL_DEFAULT_LTR;
        byte maxLevel = (byte) 0;
        int start = levels.length;
        while (start > 0) {
            start--;
            byte level = levels[start];
            if (level < null || level > 126) {
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
        start = levels.length;
        while (start > 0) {
            start--;
            indexMap[start] = start;
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
        minLevel = (byte) (minLevel | 1);
        do {
            int start = 0;
            while (true) {
                if (start >= levels.length || levels[start] >= maxLevel) {
                    if (start < levels.length) {
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
                    } else {
                        break;
                    }
                }
                start++;
            }
            maxLevel = (byte) (maxLevel - 1);
        } while (maxLevel >= minLevel);
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
        minLevel = (byte) (minLevel | 1);
        do {
            int start = 0;
            while (true) {
                if (start >= levels.length || levels[start] >= maxLevel) {
                    if (start < levels.length) {
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
                    } else {
                        break;
                    }
                }
                start++;
            }
            maxLevel = (byte) (maxLevel - 1);
        } while (maxLevel >= minLevel);
        return indexMap;
    }

    static int getVisualIndex(Bidi bidi, int logicalIndex) {
        BidiRun[] runs;
        int visualStart;
        int i;
        int length;
        int visualIndex = -1;
        switch (bidi.direction) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                visualIndex = logicalIndex;
                break;
            case NodeFilter.SHOW_ELEMENT /*1*/:
                visualIndex = (bidi.length - logicalIndex) - 1;
                break;
            default:
                getRuns(bidi);
                runs = bidi.runs;
                visualStart = 0;
                i = 0;
                while (i < bidi.runCount) {
                    length = runs[i].limit - visualStart;
                    int offset = logicalIndex - runs[i].start;
                    if (offset < 0 || offset >= length) {
                        visualStart += length;
                        i++;
                    } else {
                        if (runs[i].isEvenRun()) {
                            visualIndex = visualStart + offset;
                        } else {
                            visualIndex = ((visualStart + length) - offset) - 1;
                        }
                        if (i >= bidi.runCount) {
                            return -1;
                        }
                    }
                }
                if (i >= bidi.runCount) {
                    return -1;
                }
                break;
        }
        int insertRemove;
        if (bidi.insertPoints.size > 0) {
            runs = bidi.runs;
            visualStart = 0;
            int markFound = 0;
            i = 0;
            while (true) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & 5) > 0) {
                    markFound++;
                }
                if (visualIndex < runs[i].limit) {
                    return visualIndex + markFound;
                }
                if ((insertRemove & 10) > 0) {
                    markFound++;
                }
                i++;
                visualStart += length;
            }
        } else if (bidi.controlCount <= 0) {
            return visualIndex;
        } else {
            runs = bidi.runs;
            visualStart = 0;
            int controlFound = 0;
            if (Bidi.IsBidiControlChar(bidi.text[logicalIndex])) {
                return -1;
            }
            i = 0;
            while (true) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if (visualIndex >= runs[i].limit) {
                    controlFound -= insertRemove;
                    i++;
                    visualStart += length;
                } else if (insertRemove == 0) {
                    return visualIndex - controlFound;
                } else {
                    int start;
                    int limit;
                    if (runs[i].isEvenRun()) {
                        start = runs[i].start;
                        limit = logicalIndex;
                    } else {
                        start = logicalIndex + 1;
                        limit = runs[i].start + length;
                    }
                    for (int j = start; j < limit; j++) {
                        if (Bidi.IsBidiControlChar(bidi.text[j])) {
                            controlFound++;
                        }
                    }
                    return visualIndex - controlFound;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static int getLogicalIndex(Bidi bidi, int visualIndex) {
        int i;
        BidiRun[] runs = bidi.runs;
        int runCount = bidi.runCount;
        int visualStart;
        int length;
        int insertRemove;
        if (bidi.insertPoints.size > 0) {
            int markFound = 0;
            visualStart = 0;
            i = 0;
            while (true) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if ((insertRemove & 5) > 0) {
                    if (visualIndex <= visualStart + markFound) {
                        return -1;
                    }
                    markFound++;
                }
                if (visualIndex < runs[i].limit + markFound) {
                    break;
                }
                if ((insertRemove & 10) > 0) {
                    if (visualIndex == (visualStart + length) + markFound) {
                        return -1;
                    }
                    markFound++;
                }
                i++;
                visualStart += length;
            }
            visualIndex -= markFound;
        } else if (bidi.controlCount > 0) {
            int controlFound = 0;
            visualStart = 0;
            i = 0;
            while (true) {
                length = runs[i].limit - visualStart;
                insertRemove = runs[i].insertRemove;
                if (visualIndex < (runs[i].limit - controlFound) + insertRemove) {
                    break;
                }
                controlFound -= insertRemove;
                i++;
                visualStart += length;
            }
            if (insertRemove == 0) {
                visualIndex += controlFound;
            } else {
                int logicalStart = runs[i].start;
                boolean evenRun = runs[i].isEvenRun();
                int logicalEnd = (logicalStart + length) - 1;
                int j = 0;
                while (j < length) {
                    if (Bidi.IsBidiControlChar(bidi.text[evenRun ? logicalStart + j : logicalEnd - j])) {
                        controlFound++;
                    }
                    if (visualIndex + controlFound == visualStart + j) {
                        break;
                    }
                    j++;
                }
                visualIndex += controlFound;
            }
        }
        if (runCount <= 10) {
            i = 0;
            while (true) {
                if (visualIndex < runs[i].limit) {
                    break;
                }
                i++;
            }
        } else {
            int begin = 0;
            int limit = runCount;
            while (true) {
                i = (begin + limit) >>> 1;
                if (visualIndex >= runs[i].limit) {
                    begin = i + 1;
                } else if (i != 0 && visualIndex < runs[i - 1].limit) {
                    limit = i;
                }
            }
        }
        int start = runs[i].start;
        if (runs[i].isEvenRun()) {
            if (i > 0) {
                visualIndex -= runs[i - 1].limit;
            }
            return start + visualIndex;
        }
        return ((runs[i].limit + start) - visualIndex) - 1;
    }

    static int[] getLogicalMap(Bidi bidi) {
        BidiRun[] runs = bidi.runs;
        int[] indexMap = new int[bidi.length];
        if (bidi.length > bidi.resultLength) {
            Arrays.fill(indexMap, -1);
        }
        int visualStart = 0;
        int j = 0;
        while (true) {
            int i = bidi.runCount;
            if (j >= r0) {
                break;
            }
            int logicalStart = runs[j].start;
            int visualLimit = runs[j].limit;
            int visualStart2;
            if (runs[j].isEvenRun()) {
                int logicalStart2;
                while (true) {
                    logicalStart2 = logicalStart + 1;
                    visualStart2 = visualStart + 1;
                    indexMap[logicalStart] = visualStart;
                    if (visualStart2 >= visualLimit) {
                        break;
                    }
                    visualStart = visualStart2;
                    logicalStart = logicalStart2;
                }
                visualStart = visualStart2;
                logicalStart = logicalStart2;
            } else {
                logicalStart += visualLimit - visualStart;
                while (true) {
                    logicalStart--;
                    visualStart2 = visualStart + 1;
                    indexMap[logicalStart] = visualStart;
                    if (visualStart2 >= visualLimit) {
                        break;
                    }
                    visualStart = visualStart2;
                }
                visualStart = visualStart2;
            }
            j++;
        }
        int runCount;
        int i2;
        int length;
        int insertRemove;
        int logicalLimit;
        if (bidi.insertPoints.size > 0) {
            int markFound = 0;
            runCount = bidi.runCount;
            runs = bidi.runs;
            visualStart = 0;
            i2 = 0;
            while (i2 < runCount) {
                length = runs[i2].limit - visualStart;
                insertRemove = runs[i2].insertRemove;
                if ((insertRemove & 5) > 0) {
                    markFound++;
                }
                if (markFound > 0) {
                    logicalStart = runs[i2].start;
                    logicalLimit = logicalStart + length;
                    for (j = logicalStart; j < logicalLimit; j++) {
                        indexMap[j] = indexMap[j] + markFound;
                    }
                }
                if ((insertRemove & 10) > 0) {
                    markFound++;
                }
                i2++;
                visualStart += length;
            }
        } else if (bidi.controlCount > 0) {
            int controlFound = 0;
            runCount = bidi.runCount;
            runs = bidi.runs;
            visualStart = 0;
            i2 = 0;
            while (i2 < runCount) {
                length = runs[i2].limit - visualStart;
                insertRemove = runs[i2].insertRemove;
                if (controlFound - insertRemove != 0) {
                    logicalStart = runs[i2].start;
                    boolean evenRun = runs[i2].isEvenRun();
                    logicalLimit = logicalStart + length;
                    if (insertRemove == 0) {
                        for (j = logicalStart; j < logicalLimit; j++) {
                            indexMap[j] = indexMap[j] - controlFound;
                        }
                    } else {
                        j = 0;
                        while (j < length) {
                            int k = evenRun ? logicalStart + j : (logicalLimit - j) - 1;
                            if (Bidi.IsBidiControlChar(bidi.text[k])) {
                                controlFound++;
                                indexMap[k] = -1;
                            } else {
                                indexMap[k] = indexMap[k] - controlFound;
                            }
                            j++;
                        }
                    }
                }
                i2++;
                visualStart += length;
            }
        }
        return indexMap;
    }

    static int[] getVisualMap(Bidi bidi) {
        int allocLength;
        int i;
        BidiRun[] runs = bidi.runs;
        if (bidi.length > bidi.resultLength) {
            allocLength = bidi.length;
        } else {
            allocLength = bidi.resultLength;
        }
        int[] indexMap = new int[allocLength];
        int visualStart = 0;
        int idx = 0;
        int j = 0;
        while (true) {
            i = bidi.runCount;
            if (j >= r0) {
                break;
            }
            int logicalStart = runs[j].start;
            int visualLimit = runs[j].limit;
            int idx2;
            if (runs[j].isEvenRun()) {
                int logicalStart2;
                while (true) {
                    idx2 = idx + 1;
                    logicalStart2 = logicalStart + 1;
                    indexMap[idx] = logicalStart;
                    visualStart++;
                    if (visualStart >= visualLimit) {
                        break;
                    }
                    idx = idx2;
                    logicalStart = logicalStart2;
                }
                idx = idx2;
                logicalStart = logicalStart2;
            } else {
                logicalStart += visualLimit - visualStart;
                while (true) {
                    idx2 = idx + 1;
                    logicalStart--;
                    indexMap[idx] = logicalStart;
                    visualStart++;
                    if (visualStart >= visualLimit) {
                        break;
                    }
                    idx = idx2;
                }
                idx = idx2;
            }
            j++;
        }
        int runCount;
        int i2;
        int insertRemove;
        int k;
        if (bidi.insertPoints.size > 0) {
            int markFound = 0;
            runCount = bidi.runCount;
            runs = bidi.runs;
            for (i2 = 0; i2 < runCount; i2++) {
                insertRemove = runs[i2].insertRemove;
                if ((insertRemove & 5) > 0) {
                    markFound++;
                }
                if ((insertRemove & 10) > 0) {
                    markFound++;
                }
            }
            k = bidi.resultLength;
            i2 = runCount - 1;
            while (i2 >= 0 && markFound > 0) {
                insertRemove = runs[i2].insertRemove;
                if ((insertRemove & 10) > 0) {
                    k--;
                    indexMap[k] = -1;
                    markFound--;
                }
                visualStart = i2 > 0 ? runs[i2 - 1].limit : 0;
                for (j = runs[i2].limit - 1; j >= visualStart && markFound > 0; j--) {
                    k--;
                    indexMap[k] = indexMap[j];
                }
                if ((insertRemove & 5) > 0) {
                    k--;
                    indexMap[k] = -1;
                    markFound--;
                }
                i2--;
            }
        } else if (bidi.controlCount > 0) {
            runCount = bidi.runCount;
            runs = bidi.runs;
            visualStart = 0;
            k = 0;
            i2 = 0;
            while (i2 < runCount) {
                int length = runs[i2].limit - visualStart;
                insertRemove = runs[i2].insertRemove;
                if (insertRemove == 0 && k == visualStart) {
                    k += length;
                } else if (insertRemove == 0) {
                    visualLimit = runs[i2].limit;
                    j = visualStart;
                    k = k;
                    while (j < visualLimit) {
                        k = k + 1;
                        indexMap[k] = indexMap[j];
                        j++;
                        k = k;
                    }
                    k = k;
                } else {
                    logicalStart = runs[i2].start;
                    boolean evenRun = runs[i2].isEvenRun();
                    int logicalEnd = (logicalStart + length) - 1;
                    j = 0;
                    k = k;
                    while (j < length) {
                        int m = evenRun ? logicalStart + j : logicalEnd - j;
                        if (Bidi.IsBidiControlChar(bidi.text[m])) {
                            k = k;
                        } else {
                            k = k + 1;
                            indexMap[k] = m;
                        }
                        j++;
                        k = k;
                    }
                    k = k;
                }
                i2++;
                visualStart += length;
            }
        }
        i = bidi.resultLength;
        if (allocLength == r0) {
            return indexMap;
        }
        int[] newMap = new int[bidi.resultLength];
        System.arraycopy(indexMap, 0, newMap, 0, bidi.resultLength);
        return newMap;
    }

    static int[] invertMap(int[] srcMap) {
        int i;
        int srcEntry;
        int destLength = -1;
        int count = 0;
        for (int srcEntry2 : srcMap) {
            if (srcEntry2 > destLength) {
                destLength = srcEntry2;
            }
            if (srcEntry2 >= 0) {
                count++;
            }
        }
        destLength++;
        int[] destMap = new int[destLength];
        if (count < destLength) {
            Arrays.fill(destMap, -1);
        }
        for (i = 0; i < srcLength; i++) {
            srcEntry2 = srcMap[i];
            if (srcEntry2 >= 0) {
                destMap[srcEntry2] = i;
            }
        }
        return destMap;
    }
}

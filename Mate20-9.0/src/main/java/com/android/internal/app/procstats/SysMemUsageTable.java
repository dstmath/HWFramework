package com.android.internal.app.procstats;

import android.util.DebugUtils;
import com.android.internal.app.procstats.SparseMappingTable;
import java.io.PrintWriter;

public class SysMemUsageTable extends SparseMappingTable.Table {
    public SysMemUsageTable(SparseMappingTable tableData) {
        super(tableData);
    }

    public void mergeStats(SysMemUsageTable that) {
        int N = that.getKeyCount();
        for (int i = 0; i < N; i++) {
            int key = that.getKeyAt(i);
            mergeStats(SparseMappingTable.getIdFromKey(key), that.getArrayForKey(key), SparseMappingTable.getIndexFromKey(key));
        }
    }

    public void mergeStats(int state, long[] addData, int addOff) {
        int key = getOrAddKey((byte) state, 16);
        mergeSysMemUsage(getArrayForKey(key), SparseMappingTable.getIndexFromKey(key), addData, addOff);
    }

    public long[] getTotalMemUsage() {
        long[] total = new long[16];
        int N = getKeyCount();
        for (int i = 0; i < N; i++) {
            int key = getKeyAt(i);
            mergeSysMemUsage(total, 0, getArrayForKey(key), SparseMappingTable.getIndexFromKey(key));
        }
        return total;
    }

    public static void mergeSysMemUsage(long[] dstData, int dstOff, long[] addData, int addOff) {
        long dstCount = dstData[dstOff + 0];
        long addCount = addData[addOff + 0];
        int i = 16;
        int i2 = 1;
        if (dstCount == 0) {
            dstData[dstOff + 0] = addCount;
            while (true) {
                int i3 = i2;
                if (i3 < 16) {
                    dstData[dstOff + i3] = addData[addOff + i3];
                    i2 = i3 + 1;
                } else {
                    return;
                }
            }
        } else if (addCount > 0) {
            dstData[dstOff + 0] = dstCount + addCount;
            int i4 = 1;
            while (i4 < i) {
                if (dstData[dstOff + i4] > addData[addOff + i4]) {
                    dstData[dstOff + i4] = addData[addOff + i4];
                }
                dstData[dstOff + i4 + i2] = (long) (((((double) dstData[(dstOff + i4) + i2]) * ((double) dstCount)) + (((double) addData[(addOff + i4) + 1]) * ((double) addCount))) / ((double) (dstCount + addCount)));
                if (dstData[dstOff + i4 + 2] < addData[addOff + i4 + 2]) {
                    dstData[dstOff + i4 + 2] = addData[addOff + i4 + 2];
                }
                i4 += 3;
                i = 16;
                i2 = 1;
            }
        }
    }

    public void dump(PrintWriter pw, String prefix, int[] screenStates, int[] memStates) {
        int printedScreen;
        PrintWriter printWriter = pw;
        int[] iArr = screenStates;
        int[] iArr2 = memStates;
        int printedScreen2 = -1;
        int printedScreen3 = 0;
        while (true) {
            int is = printedScreen3;
            if (is < iArr.length) {
                int printedScreen4 = printedScreen2;
                int printedMem = -1;
                int printedMem2 = 0;
                while (true) {
                    int im = printedMem2;
                    if (im >= iArr2.length) {
                        break;
                    }
                    int iscreen = iArr[is];
                    int imem = iArr2[im];
                    int bucket = (iscreen + imem) * 14;
                    long count = getValueForId((byte) bucket, 0);
                    if (count > 0) {
                        pw.print(prefix);
                        if (iArr.length > 1) {
                            DumpUtils.printScreenLabel(printWriter, printedScreen4 != iscreen ? iscreen : -1);
                            printedScreen = iscreen;
                        } else {
                            printedScreen = printedScreen4;
                        }
                        if (iArr2.length > 1) {
                            DumpUtils.printMemLabel(printWriter, printedMem != imem ? imem : -1, 0);
                            printedMem = imem;
                        }
                        int printedMem3 = printedMem;
                        printWriter.print(": ");
                        printWriter.print(count);
                        printWriter.println(" samples:");
                        PrintWriter printWriter2 = printWriter;
                        String str = prefix;
                        long j = count;
                        int i = bucket;
                        dumpCategory(printWriter2, str, "  Cached", i, 1);
                        dumpCategory(printWriter2, str, "  Free", i, 4);
                        dumpCategory(printWriter2, str, "  ZRam", i, 7);
                        dumpCategory(printWriter2, str, "  Kernel", i, 10);
                        dumpCategory(printWriter2, str, "  Native", i, 13);
                        printedScreen4 = printedScreen;
                        printedMem = printedMem3;
                    }
                    printedMem2 = im + 1;
                }
                printedScreen3 = is + 1;
                printedScreen2 = printedScreen4;
            } else {
                return;
            }
        }
    }

    private void dumpCategory(PrintWriter pw, String prefix, String label, int bucket, int index) {
        pw.print(prefix);
        pw.print(label);
        pw.print(": ");
        DebugUtils.printSizeValue(pw, getValueForId((byte) bucket, index) * 1024);
        pw.print(" min, ");
        DebugUtils.printSizeValue(pw, getValueForId((byte) bucket, index + 1) * 1024);
        pw.print(" avg, ");
        DebugUtils.printSizeValue(pw, getValueForId((byte) bucket, index + 2) * 1024);
        pw.println(" max");
    }
}

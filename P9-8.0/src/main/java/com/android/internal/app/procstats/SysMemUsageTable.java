package com.android.internal.app.procstats;

import android.util.DebugUtils;
import com.android.internal.app.procstats.SparseMappingTable.Table;
import java.io.PrintWriter;

public class SysMemUsageTable extends Table {
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
        int i;
        if (dstCount == 0) {
            dstData[dstOff + 0] = addCount;
            for (i = 1; i < 16; i++) {
                dstData[dstOff + i] = addData[addOff + i];
            }
        } else if (addCount > 0) {
            dstData[dstOff + 0] = dstCount + addCount;
            for (i = 1; i < 16; i += 3) {
                if (dstData[dstOff + i] > addData[addOff + i]) {
                    dstData[dstOff + i] = addData[addOff + i];
                }
                dstData[(dstOff + i) + 1] = (long) (((((double) dstData[(dstOff + i) + 1]) * ((double) dstCount)) + (((double) addData[(addOff + i) + 1]) * ((double) addCount))) / ((double) (dstCount + addCount)));
                if (dstData[(dstOff + i) + 2] < addData[(addOff + i) + 2]) {
                    dstData[(dstOff + i) + 2] = addData[(addOff + i) + 2];
                }
            }
        }
    }

    public void dump(PrintWriter pw, String prefix, int[] screenStates, int[] memStates) {
        int printedScreen = -1;
        for (int iscreen : screenStates) {
            int printedMem = -1;
            for (int imem : memStates) {
                int bucket = (iscreen + imem) * 14;
                long count = getValueForId((byte) bucket, 0);
                if (count > 0) {
                    pw.print(prefix);
                    if (screenStates.length > 1) {
                        int i;
                        if (printedScreen != iscreen) {
                            i = iscreen;
                        } else {
                            i = -1;
                        }
                        DumpUtils.printScreenLabel(pw, i);
                        printedScreen = iscreen;
                    }
                    if (memStates.length > 1) {
                        DumpUtils.printMemLabel(pw, printedMem != imem ? imem : -1, 0);
                        printedMem = imem;
                    }
                    pw.print(": ");
                    pw.print(count);
                    pw.println(" samples:");
                    dumpCategory(pw, prefix, "  Cached", bucket, 1);
                    dumpCategory(pw, prefix, "  Free", bucket, 4);
                    dumpCategory(pw, prefix, "  ZRam", bucket, 7);
                    dumpCategory(pw, prefix, "  Kernel", bucket, 10);
                    dumpCategory(pw, prefix, "  Native", bucket, 13);
                }
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

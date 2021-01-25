package android.content.pm.split;

import android.content.pm.PackageParser;
import android.util.IntArray;
import android.util.SparseArray;
import java.lang.Exception;
import java.util.Arrays;
import java.util.BitSet;
import libcore.util.EmptyArray;

public abstract class SplitDependencyLoader<E extends Exception> {
    private final SparseArray<int[]> mDependencies;

    /* access modifiers changed from: protected */
    public abstract void constructSplit(int i, int[] iArr, int i2) throws Exception;

    /* access modifiers changed from: protected */
    public abstract boolean isSplitCached(int i);

    protected SplitDependencyLoader(SparseArray<int[]> dependencies) {
        this.mDependencies = dependencies;
    }

    /* access modifiers changed from: protected */
    public void loadDependenciesForSplit(int splitIdx) throws Exception {
        if (!isSplitCached(splitIdx)) {
            if (splitIdx == 0) {
                constructSplit(0, collectConfigSplitIndices(0), -1);
                return;
            }
            IntArray linearDependencies = new IntArray();
            linearDependencies.add(splitIdx);
            while (true) {
                int[] deps = this.mDependencies.get(splitIdx);
                if (deps == null || deps.length <= 0) {
                    splitIdx = -1;
                } else {
                    splitIdx = deps[0];
                }
                if (splitIdx < 0 || isSplitCached(splitIdx)) {
                    break;
                }
                linearDependencies.add(splitIdx);
            }
            int parentIdx = splitIdx;
            for (int i = linearDependencies.size() - 1; i >= 0; i--) {
                int idx = linearDependencies.get(i);
                constructSplit(idx, collectConfigSplitIndices(idx), parentIdx);
                parentIdx = idx;
            }
        }
    }

    private int[] collectConfigSplitIndices(int splitIdx) {
        int[] deps = this.mDependencies.get(splitIdx);
        if (deps == null || deps.length <= 1) {
            return EmptyArray.INT;
        }
        return Arrays.copyOfRange(deps, 1, deps.length);
    }

    public static class IllegalDependencyException extends Exception {
        private IllegalDependencyException(String message) {
            super(message);
        }
    }

    private static int[] append(int[] src, int elem) {
        if (src == null) {
            return new int[]{elem};
        }
        int[] dst = Arrays.copyOf(src, src.length + 1);
        dst[src.length] = elem;
        return dst;
    }

    public static SparseArray<int[]> createDependenciesFromPackage(PackageParser.PackageLite pkg) throws IllegalDependencyException {
        int depIdx;
        int depIdx2;
        SparseArray<int[]> splitDependencies = new SparseArray<>();
        splitDependencies.put(0, new int[]{-1});
        for (int splitIdx = 0; splitIdx < pkg.splitNames.length; splitIdx++) {
            if (pkg.isFeatureSplits[splitIdx]) {
                String splitDependency = pkg.usesSplitNames[splitIdx];
                if (splitDependency != null) {
                    int depIdx3 = Arrays.binarySearch(pkg.splitNames, splitDependency);
                    if (depIdx3 >= 0) {
                        depIdx2 = depIdx3 + 1;
                    } else {
                        throw new IllegalDependencyException("Split '" + pkg.splitNames[splitIdx] + "' requires split '" + splitDependency + "', which is missing.");
                    }
                } else {
                    depIdx2 = 0;
                }
                splitDependencies.put(splitIdx + 1, new int[]{depIdx2});
            }
        }
        for (int splitIdx2 = 0; splitIdx2 < pkg.splitNames.length; splitIdx2++) {
            if (!pkg.isFeatureSplits[splitIdx2]) {
                String configForSplit = pkg.configForSplit[splitIdx2];
                if (configForSplit == null || configForSplit.equals("")) {
                    depIdx = 0;
                } else {
                    int depIdx4 = Arrays.binarySearch(pkg.splitNames, configForSplit);
                    if (depIdx4 < 0) {
                        throw new IllegalDependencyException("Split '" + pkg.splitNames[splitIdx2] + "' targets split '" + configForSplit + "', which is missing.");
                    } else if (pkg.isFeatureSplits[depIdx4]) {
                        depIdx = depIdx4 + 1;
                    } else {
                        throw new IllegalDependencyException("Split '" + pkg.splitNames[splitIdx2] + "' declares itself as configuration split for a non-feature split '" + pkg.splitNames[depIdx4] + "'");
                    }
                }
                splitDependencies.put(depIdx, append(splitDependencies.get(depIdx), splitIdx2 + 1));
            }
        }
        BitSet bitset = new BitSet();
        int size = splitDependencies.size();
        for (int i = 0; i < size; i++) {
            int splitIdx3 = splitDependencies.keyAt(i);
            bitset.clear();
            while (splitIdx3 != -1) {
                if (!bitset.get(splitIdx3)) {
                    bitset.set(splitIdx3);
                    int[] deps = splitDependencies.get(splitIdx3);
                    splitIdx3 = deps != null ? deps[0] : -1;
                } else {
                    throw new IllegalDependencyException("Cycle detected in split dependencies.");
                }
            }
        }
        return splitDependencies;
    }
}

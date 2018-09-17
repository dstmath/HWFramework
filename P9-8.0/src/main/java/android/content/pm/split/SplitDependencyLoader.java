package android.content.pm.split;

import android.content.pm.PackageParser.PackageLite;
import android.util.IntArray;
import android.util.SparseArray;
import java.util.Arrays;
import java.util.BitSet;
import libcore.util.EmptyArray;

public abstract class SplitDependencyLoader<E extends Exception> {
    private final SparseArray<int[]> mDependencies;

    public static class IllegalDependencyException extends Exception {
        /* synthetic */ IllegalDependencyException(String message, IllegalDependencyException -this1) {
            this(message);
        }

        private IllegalDependencyException(String message) {
            super(message);
        }
    }

    protected abstract void constructSplit(int i, int[] iArr, int i2) throws Exception;

    protected abstract boolean isSplitCached(int i);

    protected SplitDependencyLoader(SparseArray<int[]> dependencies) {
        this.mDependencies = dependencies;
    }

    protected void loadDependenciesForSplit(int splitIdx) throws Exception {
        if (!isSplitCached(splitIdx)) {
            if (splitIdx == 0) {
                constructSplit(0, collectConfigSplitIndices(0), -1);
                return;
            }
            int parentIdx;
            IntArray linearDependencies = new IntArray();
            linearDependencies.add(splitIdx);
            while (true) {
                int[] deps = (int[]) this.mDependencies.get(splitIdx);
                if (deps == null || deps.length <= 0) {
                    splitIdx = -1;
                } else {
                    splitIdx = deps[0];
                }
                if (splitIdx < 0 || isSplitCached(splitIdx)) {
                    parentIdx = splitIdx;
                } else {
                    linearDependencies.add(splitIdx);
                }
            }
            parentIdx = splitIdx;
            for (int i = linearDependencies.size() - 1; i >= 0; i--) {
                int idx = linearDependencies.get(i);
                constructSplit(idx, collectConfigSplitIndices(idx), parentIdx);
                parentIdx = idx;
            }
        }
    }

    private int[] collectConfigSplitIndices(int splitIdx) {
        int[] deps = (int[]) this.mDependencies.get(splitIdx);
        if (deps == null || deps.length <= 1) {
            return EmptyArray.INT;
        }
        return Arrays.copyOfRange(deps, 1, deps.length);
    }

    private static int[] append(int[] src, int elem) {
        if (src == null) {
            return new int[]{elem};
        }
        int[] dst = Arrays.copyOf(src, src.length + 1);
        dst[src.length] = elem;
        return dst;
    }

    public static SparseArray<int[]> createDependenciesFromPackage(PackageLite pkg) throws IllegalDependencyException {
        int splitIdx;
        int depIdx;
        SparseArray<int[]> splitDependencies = new SparseArray();
        splitDependencies.put(0, new int[]{-1});
        for (splitIdx = 0; splitIdx < pkg.splitNames.length; splitIdx++) {
            if (pkg.isFeatureSplits[splitIdx]) {
                int targetIdx;
                String splitDependency = pkg.usesSplitNames[splitIdx];
                if (splitDependency != null) {
                    depIdx = Arrays.binarySearch(pkg.splitNames, splitDependency);
                    if (depIdx < 0) {
                        throw new IllegalDependencyException("Split '" + pkg.splitNames[splitIdx] + "' requires split '" + splitDependency + "', which is missing.", null);
                    }
                    targetIdx = depIdx + 1;
                } else {
                    targetIdx = 0;
                }
                splitDependencies.put(splitIdx + 1, new int[]{targetIdx});
            }
        }
        for (splitIdx = 0; splitIdx < pkg.splitNames.length; splitIdx++) {
            if (!pkg.isFeatureSplits[splitIdx]) {
                int targetSplitIdx;
                String configForSplit = pkg.configForSplit[splitIdx];
                if (configForSplit != null) {
                    depIdx = Arrays.binarySearch(pkg.splitNames, configForSplit);
                    if (depIdx < 0) {
                        throw new IllegalDependencyException("Split '" + pkg.splitNames[splitIdx] + "' targets split '" + configForSplit + "', which is missing.", null);
                    } else if (pkg.isFeatureSplits[depIdx]) {
                        targetSplitIdx = depIdx + 1;
                    } else {
                        throw new IllegalDependencyException("Split '" + pkg.splitNames[splitIdx] + "' declares itself as configuration split for a non-feature split '" + pkg.splitNames[depIdx] + "'", null);
                    }
                }
                targetSplitIdx = 0;
                splitDependencies.put(targetSplitIdx, append((int[]) splitDependencies.get(targetSplitIdx), splitIdx + 1));
            }
        }
        BitSet bitset = new BitSet();
        int size = splitDependencies.size();
        for (int i = 0; i < size; i++) {
            splitIdx = splitDependencies.keyAt(i);
            bitset.clear();
            while (splitIdx != -1) {
                if (bitset.get(splitIdx)) {
                    throw new IllegalDependencyException("Cycle detected in split dependencies.", null);
                }
                bitset.set(splitIdx);
                int[] deps = (int[]) splitDependencies.get(splitIdx);
                splitIdx = deps != null ? deps[0] : -1;
            }
        }
        return splitDependencies;
    }
}

package huawei.android.widget.appbar;

import android.util.ArrayMap;
import huawei.android.widget.appbar.Pools;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/* access modifiers changed from: package-private */
public final class DirectedAcyclicGraph<T> {
    private static final int VALUE_TEN = 10;
    private final ArrayMap<T, ArrayList<T>> mGraph = new ArrayMap<>();
    private final Pools.Pool<ArrayList<T>> mListPool = new Pools.SimplePool(VALUE_TEN);
    private final ArrayList<T> mSortResult = new ArrayList<>();
    private final HashSet<T> mSortTmpMarked = new HashSet<>();

    DirectedAcyclicGraph() {
    }

    /* access modifiers changed from: package-private */
    public void addNode(T node) {
        if (!this.mGraph.containsKey(node)) {
            this.mGraph.put(node, null);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean contains(T node) {
        return this.mGraph.containsKey(node);
    }

    /* access modifiers changed from: package-private */
    public void addEdge(T node, T incomingEdge) {
        if (!this.mGraph.containsKey(node) || !this.mGraph.containsKey(incomingEdge)) {
            throw new IllegalArgumentException("All nodes must be present in the graph before being added as an edge");
        }
        ArrayList<T> edges = this.mGraph.get(node);
        if (edges == null) {
            edges = getEmptyList();
            this.mGraph.put(node, edges);
        }
        edges.add(incomingEdge);
    }

    /* access modifiers changed from: package-private */
    public List getIncomingEdges(T node) {
        return this.mGraph.get(node);
    }

    /* access modifiers changed from: package-private */
    public List<T> getOutgoingEdges(T node) {
        ArrayList<T> result = null;
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(this.mGraph.keyAt(i));
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOutgoingEdges(T node) {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            ArrayList<T> edges = this.mGraph.valueAt(i);
            if (edges != null) {
                poolList(edges);
            }
        }
        this.mGraph.clear();
    }

    /* access modifiers changed from: package-private */
    public ArrayList<T> getSortedList() {
        this.mSortResult.clear();
        this.mSortTmpMarked.clear();
        int size = this.mGraph.size();
        for (int i = 0; i < size; i++) {
            dfs(this.mGraph.keyAt(i), this.mSortResult, this.mSortTmpMarked);
        }
        return this.mSortResult;
    }

    private void dfs(T node, ArrayList<T> result, HashSet<T> tmpMarked) {
        if (!result.contains(node)) {
            if (!tmpMarked.contains(node)) {
                tmpMarked.add(node);
                ArrayList<T> edges = this.mGraph.get(node);
                if (edges != null) {
                    int size = edges.size();
                    for (int i = 0; i < size; i++) {
                        dfs(edges.get(i), result, tmpMarked);
                    }
                }
                tmpMarked.remove(node);
                result.add(node);
                return;
            }
            throw new RuntimeException("This graph contains cyclic dependencies");
        }
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.mGraph.size();
    }

    private ArrayList<T> getEmptyList() {
        ArrayList<T> list = this.mListPool.acquire();
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private void poolList(ArrayList<T> list) {
        list.clear();
        this.mListPool.release(list);
    }
}

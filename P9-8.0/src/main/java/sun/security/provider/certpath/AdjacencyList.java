package sun.security.provider.certpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AdjacencyList {
    private List<List<Vertex>> mOrigList;
    private ArrayList<BuildStep> mStepList = new ArrayList();

    public AdjacencyList(List<List<Vertex>> list) {
        this.mOrigList = list;
        buildList(list, 0, null);
    }

    public Iterator<BuildStep> iterator() {
        return Collections.unmodifiableList(this.mStepList).iterator();
    }

    private boolean buildList(List<List<Vertex>> theList, int index, BuildStep follow) {
        List<Vertex> l = (List) theList.get(index);
        boolean allNegOne = true;
        boolean allXcps = true;
        for (Vertex v : l) {
            if (v.getIndex() != -1) {
                if (((List) theList.get(v.getIndex())).size() != 0) {
                    allNegOne = false;
                }
            } else if (v.getThrowable() == null) {
                allXcps = false;
            }
            this.mStepList.add(new BuildStep(v, 1));
        }
        if (!allNegOne) {
            boolean success = false;
            for (Vertex v2 : l) {
                if (!(v2.getIndex() == -1 || ((List) theList.get(v2.getIndex())).size() == 0)) {
                    BuildStep bs = new BuildStep(v2, 3);
                    this.mStepList.add(bs);
                    success = buildList(theList, v2.getIndex(), bs);
                }
            }
            if (success) {
                return true;
            }
            if (follow == null) {
                this.mStepList.add(new BuildStep(null, 4));
            } else {
                this.mStepList.add(new BuildStep(follow.getVertex(), 2));
            }
            return false;
        } else if (allXcps) {
            if (follow == null) {
                this.mStepList.add(new BuildStep(null, 4));
            } else {
                this.mStepList.add(new BuildStep(follow.getVertex(), 2));
            }
            return false;
        } else {
            List<Vertex> possibles = new ArrayList();
            for (Vertex v22 : l) {
                if (v22.getThrowable() == null) {
                    possibles.-java_util_stream_Collectors-mthref-2(v22);
                }
            }
            if (possibles.size() == 1) {
                this.mStepList.add(new BuildStep((Vertex) possibles.get(0), 5));
            } else {
                this.mStepList.add(new BuildStep((Vertex) possibles.get(0), 5));
            }
            return true;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[\n");
        int i = 0;
        for (List<Vertex> l : this.mOrigList) {
            int i2 = i + 1;
            sb.append("LinkedList[").append(i).append("]:\n");
            for (Vertex step : l) {
                sb.append(step.toString()).append("\n");
            }
            i = i2;
        }
        sb.append("]\n");
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}

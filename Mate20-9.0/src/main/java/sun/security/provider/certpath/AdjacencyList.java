package sun.security.provider.certpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AdjacencyList {
    private List<List<Vertex>> mOrigList;
    private ArrayList<BuildStep> mStepList = new ArrayList<>();

    public AdjacencyList(List<List<Vertex>> list) {
        this.mOrigList = list;
        buildList(list, 0, null);
    }

    public Iterator<BuildStep> iterator() {
        return Collections.unmodifiableList(this.mStepList).iterator();
    }

    private boolean buildList(List<List<Vertex>> theList, int index, BuildStep follow) {
        int i;
        List<List<Vertex>> list = theList;
        List<Vertex> l = theList.get(index);
        boolean allNegOne = true;
        boolean allXcps = true;
        Iterator<Vertex> it = l.iterator();
        while (true) {
            i = -1;
            if (!it.hasNext()) {
                break;
            }
            Vertex v = it.next();
            if (v.getIndex() != -1) {
                if (list.get(v.getIndex()).size() != 0) {
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
                if (!(v2.getIndex() == i || list.get(v2.getIndex()).size() == 0)) {
                    BuildStep bs = new BuildStep(v2, 3);
                    this.mStepList.add(bs);
                    success = buildList(list, v2.getIndex(), bs);
                }
                i = -1;
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
            List<Vertex> possibles = new ArrayList<>();
            for (Vertex v3 : l) {
                if (v3.getThrowable() == null) {
                    possibles.add(v3);
                }
            }
            if (possibles.size() == 1) {
                this.mStepList.add(new BuildStep(possibles.get(0), 5));
            } else {
                this.mStepList.add(new BuildStep(possibles.get(0), 5));
            }
            return true;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[\n");
        int i = 0;
        for (List<Vertex> l : this.mOrigList) {
            sb.append("LinkedList[");
            int i2 = i + 1;
            sb.append(i);
            sb.append("]:\n");
            for (Vertex step : l) {
                sb.append(step.toString());
                sb.append("\n");
            }
            i = i2;
        }
        sb.append("]\n");
        return sb.toString();
    }
}

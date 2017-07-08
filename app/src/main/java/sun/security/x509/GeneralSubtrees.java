package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.util.calendar.BaseCalendar;

public class GeneralSubtrees implements Cloneable {
    private static final int NAME_DIFF_TYPE = -1;
    private static final int NAME_MATCH = 0;
    private static final int NAME_NARROWS = 1;
    private static final int NAME_SAME_TYPE = 3;
    private static final int NAME_WIDENS = 2;
    private final List<GeneralSubtree> trees;

    public GeneralSubtrees() {
        this.trees = new ArrayList();
    }

    private GeneralSubtrees(GeneralSubtrees source) {
        this.trees = new ArrayList(source.trees);
    }

    public GeneralSubtrees(DerValue val) throws IOException {
        this();
        if (val.tag != 48) {
            throw new IOException("Invalid encoding of GeneralSubtrees.");
        }
        while (val.data.available() != 0) {
            add(new GeneralSubtree(val.data.getDerValue()));
        }
    }

    public GeneralSubtree get(int index) {
        return (GeneralSubtree) this.trees.get(index);
    }

    public void remove(int index) {
        this.trees.remove(index);
    }

    public void add(GeneralSubtree tree) {
        if (tree == null) {
            throw new NullPointerException();
        }
        this.trees.add(tree);
    }

    public boolean contains(GeneralSubtree tree) {
        if (tree != null) {
            return this.trees.contains(tree);
        }
        throw new NullPointerException();
    }

    public int size() {
        return this.trees.size();
    }

    public Iterator<GeneralSubtree> iterator() {
        return this.trees.iterator();
    }

    public List<GeneralSubtree> trees() {
        return this.trees;
    }

    public Object clone() {
        return new GeneralSubtrees(this);
    }

    public String toString() {
        return "   GeneralSubtrees:\n" + this.trees.toString() + "\n";
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream seq = new DerOutputStream();
        int n = size();
        for (int i = NAME_MATCH; i < n; i += NAME_NARROWS) {
            get(i).encode(seq);
        }
        out.write((byte) DerValue.tag_SequenceOf, seq);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GeneralSubtrees)) {
            return false;
        }
        return this.trees.equals(((GeneralSubtrees) obj).trees);
    }

    public int hashCode() {
        return this.trees.hashCode();
    }

    private GeneralNameInterface getGeneralNameInterface(int ndx) {
        return getGeneralNameInterface(get(ndx));
    }

    private static GeneralNameInterface getGeneralNameInterface(GeneralSubtree gs) {
        return gs.getName().getName();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void minimize() {
        int i = NAME_MATCH;
        while (i < size()) {
            GeneralNameInterface current = getGeneralNameInterface(i);
            boolean remove1 = false;
            int j = i + NAME_NARROWS;
            while (j < size()) {
                switch (current.constrains(getGeneralNameInterface(j))) {
                    case NAME_DIFF_TYPE /*-1*/:
                    case NAME_SAME_TYPE /*3*/:
                        break;
                    case NAME_MATCH /*0*/:
                        remove1 = true;
                        break;
                    case NAME_NARROWS /*1*/:
                        remove(j);
                        j += NAME_DIFF_TYPE;
                        break;
                    case NAME_WIDENS /*2*/:
                        remove1 = true;
                        break;
                    default:
                        break;
                }
            }
            if (remove1) {
                remove(i);
                i += NAME_DIFF_TYPE;
            }
            i += NAME_NARROWS;
        }
    }

    private GeneralSubtree createWidestSubtree(GeneralNameInterface name) {
        try {
            GeneralName newName;
            switch (name.getType()) {
                case NAME_MATCH /*0*/:
                    newName = new GeneralName(new OtherName(((OtherName) name).getOID(), null));
                    break;
                case NAME_NARROWS /*1*/:
                    newName = new GeneralName(new RFC822Name(""));
                    break;
                case NAME_WIDENS /*2*/:
                    newName = new GeneralName(new DNSName(""));
                    break;
                case NAME_SAME_TYPE /*3*/:
                    newName = new GeneralName(new X400Address((byte[]) null));
                    break;
                case BaseCalendar.WEDNESDAY /*4*/:
                    newName = new GeneralName(new X500Name(""));
                    break;
                case BaseCalendar.THURSDAY /*5*/:
                    newName = new GeneralName(new EDIPartyName(""));
                    break;
                case BaseCalendar.JUNE /*6*/:
                    newName = new GeneralName(new URIName(""));
                    break;
                case BaseCalendar.SATURDAY /*7*/:
                    newName = new GeneralName(new IPAddressName((byte[]) null));
                    break;
                case BaseCalendar.AUGUST /*8*/:
                    newName = new GeneralName(new OIDName(new ObjectIdentifier((int[]) null)));
                    break;
                default:
                    throw new IOException("Unsupported GeneralNameInterface type: " + name.getType());
            }
            return new GeneralSubtree(newName, NAME_MATCH, NAME_DIFF_TYPE);
        } catch (Object e) {
            throw new RuntimeException("Unexpected error: " + e, e);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public GeneralSubtrees intersect(GeneralSubtrees other) {
        if (other == null) {
            throw new NullPointerException("other GeneralSubtrees must not be null");
        }
        GeneralSubtrees newThis = new GeneralSubtrees();
        GeneralSubtrees newExcluded = null;
        if (size() == 0) {
            union(other);
            return null;
        }
        minimize();
        other.minimize();
        int i = NAME_MATCH;
        while (i < size()) {
            GeneralNameInterface thisEntry = getGeneralNameInterface(i);
            boolean sameType = false;
            int j = NAME_MATCH;
            while (j < other.size()) {
                GeneralSubtree otherEntryGS = other.get(j);
                switch (thisEntry.constrains(getGeneralNameInterface(otherEntryGS))) {
                    case NAME_MATCH /*0*/:
                    case NAME_WIDENS /*2*/:
                        sameType = false;
                        break;
                    case NAME_NARROWS /*1*/:
                        remove(i);
                        i += NAME_DIFF_TYPE;
                        newThis.add(otherEntryGS);
                        sameType = false;
                        break;
                    case NAME_SAME_TYPE /*3*/:
                        sameType = true;
                        break;
                    default:
                        break;
                }
            }
            if (sameType) {
                boolean intersection = false;
                for (j = NAME_MATCH; j < size(); j += NAME_NARROWS) {
                    GeneralNameInterface thisAltEntry = getGeneralNameInterface(j);
                    if (thisAltEntry.getType() == thisEntry.getType()) {
                        int k = NAME_MATCH;
                        while (k < other.size()) {
                            int constraintType = thisAltEntry.constrains(other.getGeneralNameInterface(k));
                            if (constraintType == 0 || constraintType == NAME_WIDENS || constraintType == NAME_NARROWS) {
                                intersection = true;
                            } else {
                                k += NAME_NARROWS;
                            }
                        }
                    }
                }
                if (!intersection) {
                    if (newExcluded == null) {
                        newExcluded = new GeneralSubtrees();
                    }
                    GeneralSubtree widestSubtree = createWidestSubtree(thisEntry);
                    if (!newExcluded.contains(widestSubtree)) {
                        newExcluded.add(widestSubtree);
                    }
                }
                remove(i);
                i += NAME_DIFF_TYPE;
            }
            i += NAME_NARROWS;
        }
        if (newThis.size() > 0) {
            union(newThis);
        }
        for (i = NAME_MATCH; i < other.size(); i += NAME_NARROWS) {
            otherEntryGS = other.get(i);
            GeneralNameInterface otherEntry = getGeneralNameInterface(otherEntryGS);
            boolean diffType = false;
            j = NAME_MATCH;
            while (j < size()) {
                switch (getGeneralNameInterface(j).constrains(otherEntry)) {
                    case NAME_DIFF_TYPE /*-1*/:
                        diffType = true;
                        break;
                    case NAME_MATCH /*0*/:
                    case NAME_NARROWS /*1*/:
                    case NAME_WIDENS /*2*/:
                    case NAME_SAME_TYPE /*3*/:
                        diffType = false;
                        break;
                    default:
                        break;
                }
            }
            if (diffType) {
                add(otherEntryGS);
            }
        }
        return newExcluded;
    }

    public void union(GeneralSubtrees other) {
        if (other != null) {
            int n = other.size();
            for (int i = NAME_MATCH; i < n; i += NAME_NARROWS) {
                add(other.get(i));
            }
            minimize();
        }
    }

    public void reduce(GeneralSubtrees excluded) {
        if (excluded != null) {
            int n = excluded.size();
            for (int i = NAME_MATCH; i < n; i += NAME_NARROWS) {
                GeneralNameInterface excludedName = excluded.getGeneralNameInterface(i);
                int j = NAME_MATCH;
                while (j < size()) {
                    switch (excludedName.constrains(getGeneralNameInterface(j))) {
                        case NAME_MATCH /*0*/:
                            remove(j);
                            j += NAME_DIFF_TYPE;
                            break;
                        case NAME_NARROWS /*1*/:
                            remove(j);
                            j += NAME_DIFF_TYPE;
                            break;
                        default:
                            break;
                    }
                    j += NAME_NARROWS;
                }
            }
        }
    }
}

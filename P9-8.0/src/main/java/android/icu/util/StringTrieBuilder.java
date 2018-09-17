package android.icu.util;

import android.icu.text.DateTimePatternGenerator;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class StringTrieBuilder {
    private static final /* synthetic */ int[] -android-icu-util-StringTrieBuilder$StateSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = (StringTrieBuilder.class.desiredAssertionStatus() ^ 1);
    private ValueNode lookupFinalValueNode = new ValueNode();
    private HashMap<Node, Node> nodes = new HashMap();
    private Node root;
    private State state = State.ADDING;
    @Deprecated
    protected StringBuilder strings = new StringBuilder();

    private static abstract class Node {
        protected int offset = 0;

        public abstract int hashCode();

        public abstract void write(StringTrieBuilder stringTrieBuilder);

        public boolean equals(Object other) {
            return this == other || getClass() == other.getClass();
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            return this;
        }

        public Node register(StringTrieBuilder builder) {
            return this;
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.offset = edgeNumber;
            }
            return edgeNumber;
        }

        public final void writeUnlessInsideRightEdge(int firstRight, int lastRight, StringTrieBuilder builder) {
            if (this.offset >= 0) {
                return;
            }
            if (this.offset < lastRight || firstRight < this.offset) {
                write(builder);
            }
        }

        public final int getOffset() {
            return this.offset;
        }
    }

    private static class ValueNode extends Node {
        static final /* synthetic */ boolean -assertionsDisabled = (ValueNode.class.desiredAssertionStatus() ^ 1);
        protected boolean hasValue;
        protected int value;

        public ValueNode(int v) {
            this.hasValue = true;
            this.value = v;
        }

        public final void setValue(int v) {
            if (-assertionsDisabled || !this.hasValue) {
                this.hasValue = true;
                this.value = v;
                return;
            }
            throw new AssertionError();
        }

        private void setFinalValue(int v) {
            this.hasValue = true;
            this.value = v;
        }

        public int hashCode() {
            if (this.hasValue) {
                return 41383797 + this.value;
            }
            return 1118481;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            ValueNode o = (ValueNode) other;
            if (this.hasValue != o.hasValue || (this.hasValue && this.value != o.value)) {
                z = false;
            }
            return z;
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if (start == s.length()) {
                throw new IllegalArgumentException("Duplicate string.");
            }
            ValueNode node = builder.createSuffixNode(s, start, sValue);
            node.setValue(this.value);
            return node;
        }

        public void write(StringTrieBuilder builder) {
            this.offset = builder.writeValueAndFinal(this.value, true);
        }
    }

    private static final class BranchHeadNode extends ValueNode {
        private int length;
        private Node next;

        public BranchHeadNode(int len, Node subNode) {
            this.length = len;
            this.next = subNode;
        }

        public int hashCode() {
            return ((this.length + 248302782) * 37) + this.next.hashCode();
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            BranchHeadNode o = (BranchHeadNode) other;
            if (!(this.length == o.length && this.next == o.next)) {
                z = false;
            }
            return z;
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset != 0) {
                return edgeNumber;
            }
            edgeNumber = this.next.markRightEdgesFirst(edgeNumber);
            this.offset = edgeNumber;
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.next.write(builder);
            if (this.length <= builder.getMinLinearMatch()) {
                this.offset = builder.writeValueAndType(this.hasValue, this.value, this.length - 1);
                return;
            }
            builder.write(this.length - 1);
            this.offset = builder.writeValueAndType(this.hasValue, this.value, 0);
        }
    }

    private static abstract class BranchNode extends Node {
        protected int firstEdgeNumber;
        protected int hash;

        public int hashCode() {
            return this.hash;
        }
    }

    private static final class DynamicBranchNode extends ValueNode {
        private StringBuilder chars = new StringBuilder();
        private ArrayList<Node> equal = new ArrayList();

        public void add(char c, Node node) {
            int i = find(c);
            this.chars.insert(i, c);
            this.equal.add(i, node);
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if (start != s.length()) {
                int start2 = start + 1;
                char c = s.charAt(start);
                int i = find(c);
                if (i >= this.chars.length() || c != this.chars.charAt(i)) {
                    this.chars.insert(i, c);
                    this.equal.add(i, builder.createSuffixNode(s, start2, sValue));
                } else {
                    this.equal.set(i, ((Node) this.equal.get(i)).add(builder, s, start2, sValue));
                }
                return this;
            } else if (this.hasValue) {
                throw new IllegalArgumentException("Duplicate string.");
            } else {
                setValue(sValue);
                return this;
            }
        }

        public Node register(StringTrieBuilder builder) {
            Node head = new BranchHeadNode(this.chars.length(), register(builder, 0, this.chars.length()));
            Node result = head;
            if (this.hasValue) {
                if (builder.matchNodesCanHaveValues()) {
                    head.setValue(this.value);
                } else {
                    result = new IntermediateValueNode(this.value, builder.registerNode(head));
                }
            }
            return builder.registerNode(result);
        }

        private Node register(StringTrieBuilder builder, int start, int limit) {
            int length = limit - start;
            if (length > builder.getMaxBranchLinearSubNodeLength()) {
                int middle = start + (length / 2);
                return builder.registerNode(new SplitBranchNode(this.chars.charAt(middle), register(builder, start, middle), register(builder, middle, limit)));
            }
            ListBranchNode listNode = new ListBranchNode(length);
            do {
                int c = this.chars.charAt(start);
                Node node = (Node) this.equal.get(start);
                if (node.getClass() == ValueNode.class) {
                    listNode.add(c, ((ValueNode) node).value);
                } else {
                    listNode.add(c, node.register(builder));
                }
                start++;
            } while (start < limit);
            return builder.registerNode(listNode);
        }

        private int find(char c) {
            int start = 0;
            int limit = this.chars.length();
            while (start < limit) {
                int i = (start + limit) / 2;
                char middleChar = this.chars.charAt(i);
                if (c < middleChar) {
                    limit = i;
                } else if (c == middleChar) {
                    return i;
                } else {
                    start = i + 1;
                }
            }
            return start;
        }
    }

    private static final class IntermediateValueNode extends ValueNode {
        private Node next;

        public IntermediateValueNode(int v, Node nextNode) {
            this.next = nextNode;
            setValue(v);
        }

        public int hashCode() {
            return ((this.value + 82767594) * 37) + this.next.hashCode();
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            if (this.next != ((IntermediateValueNode) other).next) {
                z = false;
            }
            return z;
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset != 0) {
                return edgeNumber;
            }
            edgeNumber = this.next.markRightEdgesFirst(edgeNumber);
            this.offset = edgeNumber;
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.next.write(builder);
            this.offset = builder.writeValueAndFinal(this.value, false);
        }
    }

    private static final class LinearMatchNode extends ValueNode {
        private int hash;
        private int length;
        private Node next;
        private int stringOffset;
        private CharSequence strings;

        public LinearMatchNode(CharSequence builderStrings, int sOffset, int len, Node nextNode) {
            this.strings = builderStrings;
            this.stringOffset = sOffset;
            this.length = len;
            this.next = nextNode;
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            LinearMatchNode o = (LinearMatchNode) other;
            if (this.length != o.length || this.next != o.next) {
                return false;
            }
            int i = this.stringOffset;
            int j = o.stringOffset;
            int limit = this.stringOffset + this.length;
            while (i < limit) {
                if (this.strings.charAt(i) != this.strings.charAt(j)) {
                    return false;
                }
                i++;
                j++;
            }
            return true;
        }

        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if (start != s.length()) {
                int limit = this.stringOffset + this.length;
                int i = this.stringOffset;
                while (i < limit) {
                    int prefixLength;
                    if (start == s.length()) {
                        prefixLength = i - this.stringOffset;
                        LinearMatchNode suffixNode = new LinearMatchNode(this.strings, i, this.length - prefixLength, this.next);
                        suffixNode.setValue(sValue);
                        this.length = prefixLength;
                        this.next = suffixNode;
                        return this;
                    }
                    char thisChar = this.strings.charAt(i);
                    char newChar = s.charAt(start);
                    if (thisChar != newChar) {
                        Node thisSuffixNode;
                        Node result;
                        Node branchNode = new DynamicBranchNode();
                        if (i == this.stringOffset) {
                            if (this.hasValue) {
                                branchNode.setValue(this.value);
                                this.value = 0;
                                this.hasValue = false;
                            }
                            this.stringOffset++;
                            this.length--;
                            thisSuffixNode = this.length > 0 ? this : this.next;
                            result = branchNode;
                        } else if (i == limit - 1) {
                            this.length--;
                            thisSuffixNode = this.next;
                            this.next = branchNode;
                            result = this;
                        } else {
                            prefixLength = i - this.stringOffset;
                            thisSuffixNode = new LinearMatchNode(this.strings, i + 1, this.length - (prefixLength + 1), this.next);
                            this.length = prefixLength;
                            this.next = branchNode;
                            result = this;
                        }
                        ValueNode newSuffixNode = builder.createSuffixNode(s, start + 1, sValue);
                        branchNode.add(thisChar, thisSuffixNode);
                        branchNode.add(newChar, newSuffixNode);
                        return result;
                    }
                    i++;
                    start++;
                }
                this.next = this.next.add(builder, s, start, sValue);
                return this;
            } else if (this.hasValue) {
                throw new IllegalArgumentException("Duplicate string.");
            } else {
                setValue(sValue);
                return this;
            }
        }

        public Node register(StringTrieBuilder builder) {
            Node result;
            this.next = this.next.register(builder);
            int maxLinearMatchLength = builder.getMaxLinearMatchLength();
            while (this.length > maxLinearMatchLength) {
                int nextOffset = (this.stringOffset + this.length) - maxLinearMatchLength;
                this.length -= maxLinearMatchLength;
                LinearMatchNode suffixNode = new LinearMatchNode(this.strings, nextOffset, maxLinearMatchLength, this.next);
                suffixNode.setHashCode();
                this.next = builder.registerNode(suffixNode);
            }
            if (!this.hasValue || (builder.matchNodesCanHaveValues() ^ 1) == 0) {
                setHashCode();
                result = this;
            } else {
                int intermediateValue = this.value;
                this.value = 0;
                this.hasValue = false;
                setHashCode();
                result = new IntermediateValueNode(intermediateValue, builder.registerNode(this));
            }
            return builder.registerNode(result);
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset != 0) {
                return edgeNumber;
            }
            edgeNumber = this.next.markRightEdgesFirst(edgeNumber);
            this.offset = edgeNumber;
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.next.write(builder);
            builder.write(this.stringOffset, this.length);
            this.offset = builder.writeValueAndType(this.hasValue, this.value, (builder.getMinLinearMatch() + this.length) - 1);
        }

        private void setHashCode() {
            this.hash = ((this.length + 124151391) * 37) + this.next.hashCode();
            if (this.hasValue) {
                this.hash = (this.hash * 37) + this.value;
            }
            int limit = this.stringOffset + this.length;
            for (int i = this.stringOffset; i < limit; i++) {
                this.hash = (this.hash * 37) + this.strings.charAt(i);
            }
        }
    }

    private static final class ListBranchNode extends BranchNode {
        static final /* synthetic */ boolean -assertionsDisabled = (ListBranchNode.class.desiredAssertionStatus() ^ 1);
        private Node[] equal;
        private int length;
        private char[] units;
        private int[] values;

        public ListBranchNode(int capacity) {
            this.hash = 165535188 + capacity;
            this.equal = new Node[capacity];
            this.values = new int[capacity];
            this.units = new char[capacity];
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            ListBranchNode o = (ListBranchNode) other;
            int i = 0;
            while (i < this.length) {
                if (this.units[i] != o.units[i] || this.values[i] != o.values[i] || this.equal[i] != o.equal[i]) {
                    return false;
                }
                i++;
            }
            return true;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset == 0) {
                this.firstEdgeNumber = edgeNumber;
                int step = 0;
                int i = this.length;
                do {
                    i--;
                    Node edge = this.equal[i];
                    if (edge != null) {
                        edgeNumber = edge.markRightEdgesFirst(edgeNumber - step);
                    }
                    step = 1;
                } while (i > 0);
                this.offset = edgeNumber;
            }
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            int unitNumber = this.length - 1;
            Node rightEdge = this.equal[unitNumber];
            int rightEdgeNumber = rightEdge == null ? this.firstEdgeNumber : rightEdge.getOffset();
            do {
                unitNumber--;
                if (this.equal[unitNumber] != null) {
                    this.equal[unitNumber].writeUnlessInsideRightEdge(this.firstEdgeNumber, rightEdgeNumber, builder);
                    continue;
                }
            } while (unitNumber > 0);
            unitNumber = this.length - 1;
            if (rightEdge == null) {
                builder.writeValueAndFinal(this.values[unitNumber], true);
            } else {
                rightEdge.write(builder);
            }
            this.offset = builder.write(this.units[unitNumber]);
            while (true) {
                unitNumber--;
                if (unitNumber >= 0) {
                    int value;
                    boolean isFinal;
                    if (this.equal[unitNumber] == null) {
                        value = this.values[unitNumber];
                        isFinal = true;
                    } else if (-assertionsDisabled || this.equal[unitNumber].getOffset() > 0) {
                        value = this.offset - this.equal[unitNumber].getOffset();
                        isFinal = false;
                    } else {
                        throw new AssertionError();
                    }
                    builder.writeValueAndFinal(value, isFinal);
                    this.offset = builder.write(this.units[unitNumber]);
                } else {
                    return;
                }
            }
        }

        public void add(int c, int value) {
            this.units[this.length] = (char) c;
            this.equal[this.length] = null;
            this.values[this.length] = value;
            this.length++;
            this.hash = (((this.hash * 37) + c) * 37) + value;
        }

        public void add(int c, Node node) {
            this.units[this.length] = (char) c;
            this.equal[this.length] = node;
            this.values[this.length] = 0;
            this.length++;
            this.hash = (((this.hash * 37) + c) * 37) + node.hashCode();
        }
    }

    public enum Option {
        FAST,
        SMALL
    }

    private static final class SplitBranchNode extends BranchNode {
        static final /* synthetic */ boolean -assertionsDisabled = (SplitBranchNode.class.desiredAssertionStatus() ^ 1);
        private Node greaterOrEqual;
        private Node lessThan;
        private char unit;

        public SplitBranchNode(char middleUnit, Node lessThanNode, Node greaterOrEqualNode) {
            this.hash = ((((206918985 + middleUnit) * 37) + lessThanNode.hashCode()) * 37) + greaterOrEqualNode.hashCode();
            this.unit = middleUnit;
            this.lessThan = lessThanNode;
            this.greaterOrEqual = greaterOrEqualNode;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (!super.equals(other)) {
                return false;
            }
            SplitBranchNode o = (SplitBranchNode) other;
            if (!(this.unit == o.unit && this.lessThan == o.lessThan && this.greaterOrEqual == o.greaterOrEqual)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public int markRightEdgesFirst(int edgeNumber) {
            if (this.offset != 0) {
                return edgeNumber;
            }
            this.firstEdgeNumber = edgeNumber;
            edgeNumber = this.lessThan.markRightEdgesFirst(this.greaterOrEqual.markRightEdgesFirst(edgeNumber) - 1);
            this.offset = edgeNumber;
            return edgeNumber;
        }

        public void write(StringTrieBuilder builder) {
            this.lessThan.writeUnlessInsideRightEdge(this.firstEdgeNumber, this.greaterOrEqual.getOffset(), builder);
            this.greaterOrEqual.write(builder);
            if (-assertionsDisabled || this.lessThan.getOffset() > 0) {
                builder.writeDeltaTo(this.lessThan.getOffset());
                this.offset = builder.write(this.unit);
                return;
            }
            throw new AssertionError();
        }
    }

    private enum State {
        ADDING,
        BUILDING_FAST,
        BUILDING_SMALL,
        BUILT
    }

    private static /* synthetic */ int[] -getandroid-icu-util-StringTrieBuilder$StateSwitchesValues() {
        if (-android-icu-util-StringTrieBuilder$StateSwitchesValues != null) {
            return -android-icu-util-StringTrieBuilder$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ADDING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.BUILDING_FAST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.BUILDING_SMALL.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.BUILT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-icu-util-StringTrieBuilder$StateSwitchesValues = iArr;
        return iArr;
    }

    @Deprecated
    protected abstract int getMaxBranchLinearSubNodeLength();

    @Deprecated
    protected abstract int getMaxLinearMatchLength();

    @Deprecated
    protected abstract int getMinLinearMatch();

    @Deprecated
    protected abstract boolean matchNodesCanHaveValues();

    @Deprecated
    protected abstract int write(int i);

    @Deprecated
    protected abstract int write(int i, int i2);

    @Deprecated
    protected abstract int writeDeltaTo(int i);

    @Deprecated
    protected abstract int writeValueAndFinal(int i, boolean z);

    @Deprecated
    protected abstract int writeValueAndType(boolean z, int i, int i2);

    @Deprecated
    protected StringTrieBuilder() {
    }

    @Deprecated
    protected void addImpl(CharSequence s, int value) {
        if (this.state != State.ADDING) {
            throw new IllegalStateException("Cannot add (string, value) pairs after build().");
        } else if (s.length() > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            throw new IndexOutOfBoundsException("The maximum string length is 0xffff.");
        } else if (this.root == null) {
            this.root = createSuffixNode(s, 0, value);
        } else {
            this.root = this.root.add(this, s, 0, value);
        }
    }

    @Deprecated
    protected final void buildImpl(Option buildOption) {
        switch (-getandroid-icu-util-StringTrieBuilder$StateSwitchesValues()[this.state.ordinal()]) {
            case 1:
                if (this.root != null) {
                    if (buildOption != Option.FAST) {
                        this.state = State.BUILDING_SMALL;
                        break;
                    } else {
                        this.state = State.BUILDING_FAST;
                        break;
                    }
                }
                throw new IndexOutOfBoundsException("No (string, value) pairs were added.");
            case 2:
            case 3:
                throw new IllegalStateException("Builder failed and must be clear()ed.");
            case 4:
                return;
        }
        this.root = this.root.register(this);
        this.root.markRightEdgesFirst(-1);
        this.root.write(this);
        this.state = State.BUILT;
    }

    @Deprecated
    protected void clearImpl() {
        this.strings.setLength(0);
        this.nodes.clear();
        this.root = null;
        this.state = State.ADDING;
    }

    private final Node registerNode(Node newNode) {
        if (this.state == State.BUILDING_FAST) {
            return newNode;
        }
        Node oldNode = (Node) this.nodes.get(newNode);
        if (oldNode != null) {
            return oldNode;
        }
        oldNode = (Node) this.nodes.put(newNode, newNode);
        if (-assertionsDisabled || oldNode == null) {
            return newNode;
        }
        throw new AssertionError();
    }

    private final ValueNode registerFinalValue(int value) {
        this.lookupFinalValueNode.setFinalValue(value);
        Node oldNode = (Node) this.nodes.get(this.lookupFinalValueNode);
        if (oldNode != null) {
            return (ValueNode) oldNode;
        }
        ValueNode newNode = new ValueNode(value);
        oldNode = (Node) this.nodes.put(newNode, newNode);
        if (-assertionsDisabled || oldNode == null) {
            return newNode;
        }
        throw new AssertionError();
    }

    private ValueNode createSuffixNode(CharSequence s, int start, int sValue) {
        ValueNode node = registerFinalValue(sValue);
        if (start >= s.length()) {
            return node;
        }
        int offset = this.strings.length();
        this.strings.append(s, start, s.length());
        return new LinearMatchNode(this.strings, offset, s.length() - start, node);
    }
}

package java.text;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class AttributedString {
    private static final int ARRAY_SIZE_INCREMENT = 10;
    int runArraySize;
    Vector<Object>[] runAttributeValues;
    Vector<Attribute>[] runAttributes;
    int runCount;
    int[] runStarts;
    String text;

    private final class AttributeMap extends AbstractMap<Attribute, Object> {
        int beginIndex;
        int endIndex;
        int runIndex;

        AttributeMap(int runIndex, int beginIndex, int endIndex) {
            this.runIndex = runIndex;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }

        public Set<Entry<Attribute, Object>> entrySet() {
            HashSet<Entry<Attribute, Object>> set = new HashSet();
            synchronized (AttributedString.this) {
                int size = AttributedString.this.runAttributes[this.runIndex].size();
                for (int i = 0; i < size; i++) {
                    Attribute key = (Attribute) AttributedString.this.runAttributes[this.runIndex].get(i);
                    Object value = AttributedString.this.runAttributeValues[this.runIndex].get(i);
                    if (value instanceof Annotation) {
                        value = AttributedString.this.getAttributeCheckRange(key, this.runIndex, this.beginIndex, this.endIndex);
                        if (value == null) {
                        }
                    }
                    set.add(new AttributeEntry(key, value));
                }
            }
            return set;
        }

        public Object get(Object key) {
            return AttributedString.this.getAttributeCheckRange((Attribute) key, this.runIndex, this.beginIndex, this.endIndex);
        }
    }

    private final class AttributedStringIterator implements AttributedCharacterIterator {
        private int beginIndex;
        private int currentIndex;
        private int currentRunIndex;
        private int currentRunLimit;
        private int currentRunStart;
        private int endIndex;
        private Attribute[] relevantAttributes;

        AttributedStringIterator(Attribute[] attributes, int beginIndex, int endIndex) {
            if (beginIndex < 0 || beginIndex > endIndex || endIndex > AttributedString.this.length()) {
                throw new IllegalArgumentException("Invalid substring range");
            }
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.currentIndex = beginIndex;
            updateRunInfo();
            if (attributes != null) {
                this.relevantAttributes = (Attribute[]) attributes.clone();
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AttributedStringIterator)) {
                return false;
            }
            AttributedStringIterator that = (AttributedStringIterator) obj;
            return AttributedString.this == that.getString() && this.currentIndex == that.currentIndex && this.beginIndex == that.beginIndex && this.endIndex == that.endIndex;
        }

        public int hashCode() {
            return ((AttributedString.this.text.hashCode() ^ this.currentIndex) ^ this.beginIndex) ^ this.endIndex;
        }

        public Object clone() {
            try {
                return (AttributedStringIterator) super.clone();
            } catch (Throwable e) {
                throw new InternalError(e);
            }
        }

        public char first() {
            return internalSetIndex(this.beginIndex);
        }

        public char last() {
            if (this.endIndex == this.beginIndex) {
                return internalSetIndex(this.endIndex);
            }
            return internalSetIndex(this.endIndex - 1);
        }

        public char current() {
            if (this.currentIndex == this.endIndex) {
                return 65535;
            }
            return AttributedString.this.charAt(this.currentIndex);
        }

        public char next() {
            if (this.currentIndex < this.endIndex) {
                return internalSetIndex(this.currentIndex + 1);
            }
            return 65535;
        }

        public char previous() {
            if (this.currentIndex > this.beginIndex) {
                return internalSetIndex(this.currentIndex - 1);
            }
            return 65535;
        }

        public char setIndex(int position) {
            if (position >= this.beginIndex && position <= this.endIndex) {
                return internalSetIndex(position);
            }
            throw new IllegalArgumentException("Invalid index");
        }

        public int getBeginIndex() {
            return this.beginIndex;
        }

        public int getEndIndex() {
            return this.endIndex;
        }

        public int getIndex() {
            return this.currentIndex;
        }

        public int getRunStart() {
            return this.currentRunStart;
        }

        public int getRunStart(Attribute attribute) {
            if (this.currentRunStart == this.beginIndex || this.currentRunIndex == -1) {
                return this.currentRunStart;
            }
            Object value = getAttribute(attribute);
            int runStart = this.currentRunStart;
            int runIndex = this.currentRunIndex;
            while (runStart > this.beginIndex && AttributedString.valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex - 1))) {
                runIndex--;
                runStart = AttributedString.this.runStarts[runIndex];
            }
            if (runStart < this.beginIndex) {
                runStart = this.beginIndex;
            }
            return runStart;
        }

        public int getRunStart(Set<? extends Attribute> attributes) {
            if (this.currentRunStart == this.beginIndex || this.currentRunIndex == -1) {
                return this.currentRunStart;
            }
            int runStart = this.currentRunStart;
            int runIndex = this.currentRunIndex;
            while (runStart > this.beginIndex && AttributedString.this.attributeValuesMatch(attributes, this.currentRunIndex, runIndex - 1)) {
                runIndex--;
                runStart = AttributedString.this.runStarts[runIndex];
            }
            if (runStart < this.beginIndex) {
                runStart = this.beginIndex;
            }
            return runStart;
        }

        public int getRunLimit() {
            return this.currentRunLimit;
        }

        public int getRunLimit(Attribute attribute) {
            if (this.currentRunLimit == this.endIndex || this.currentRunIndex == -1) {
                return this.currentRunLimit;
            }
            Object value = getAttribute(attribute);
            int runLimit = this.currentRunLimit;
            int runIndex = this.currentRunIndex;
            while (runLimit < this.endIndex && AttributedString.valuesMatch(value, AttributedString.this.getAttribute(attribute, runIndex + 1))) {
                runIndex++;
                runLimit = runIndex < AttributedString.this.runCount + -1 ? AttributedString.this.runStarts[runIndex + 1] : this.endIndex;
            }
            if (runLimit > this.endIndex) {
                runLimit = this.endIndex;
            }
            return runLimit;
        }

        public int getRunLimit(Set<? extends Attribute> attributes) {
            if (this.currentRunLimit == this.endIndex || this.currentRunIndex == -1) {
                return this.currentRunLimit;
            }
            int runLimit = this.currentRunLimit;
            int runIndex = this.currentRunIndex;
            while (runLimit < this.endIndex && AttributedString.this.attributeValuesMatch(attributes, this.currentRunIndex, runIndex + 1)) {
                runIndex++;
                runLimit = runIndex < AttributedString.this.runCount + -1 ? AttributedString.this.runStarts[runIndex + 1] : this.endIndex;
            }
            if (runLimit > this.endIndex) {
                runLimit = this.endIndex;
            }
            return runLimit;
        }

        public Map<Attribute, Object> getAttributes() {
            if (AttributedString.this.runAttributes == null || this.currentRunIndex == -1 || AttributedString.this.runAttributes[this.currentRunIndex] == null) {
                return new Hashtable();
            }
            return new AttributeMap(this.currentRunIndex, this.beginIndex, this.endIndex);
        }

        public Set<Attribute> getAllAttributeKeys() {
            if (AttributedString.this.runAttributes == null) {
                return new HashSet();
            }
            Set<Attribute> keys;
            synchronized (AttributedString.this) {
                keys = new HashSet();
                int i = 0;
                while (i < AttributedString.this.runCount) {
                    if (AttributedString.this.runStarts[i] < this.endIndex && (i == AttributedString.this.runCount - 1 || AttributedString.this.runStarts[i + 1] > this.beginIndex)) {
                        Vector<Attribute> currentRunAttributes = AttributedString.this.runAttributes[i];
                        if (currentRunAttributes != null) {
                            int j = currentRunAttributes.size();
                            while (true) {
                                int j2 = j;
                                j = j2 - 1;
                                if (j2 <= 0) {
                                    break;
                                }
                                keys.add((Attribute) currentRunAttributes.get(j));
                            }
                        }
                    }
                    i++;
                }
            }
            return keys;
        }

        public Object getAttribute(Attribute attribute) {
            int runIndex = this.currentRunIndex;
            if (runIndex < 0) {
                return null;
            }
            return AttributedString.this.getAttributeCheckRange(attribute, runIndex, this.beginIndex, this.endIndex);
        }

        private AttributedString getString() {
            return AttributedString.this;
        }

        private char internalSetIndex(int position) {
            this.currentIndex = position;
            if (position < this.currentRunStart || position >= this.currentRunLimit) {
                updateRunInfo();
            }
            if (this.currentIndex == this.endIndex) {
                return 65535;
            }
            return AttributedString.this.charAt(position);
        }

        private void updateRunInfo() {
            if (this.currentIndex == this.endIndex) {
                int i = this.endIndex;
                this.currentRunLimit = i;
                this.currentRunStart = i;
                this.currentRunIndex = -1;
                return;
            }
            synchronized (AttributedString.this) {
                int runIndex = -1;
                while (runIndex < AttributedString.this.runCount - 1 && AttributedString.this.runStarts[runIndex + 1] <= this.currentIndex) {
                    runIndex++;
                }
                this.currentRunIndex = runIndex;
                if (runIndex >= 0) {
                    this.currentRunStart = AttributedString.this.runStarts[runIndex];
                    if (this.currentRunStart < this.beginIndex) {
                        this.currentRunStart = this.beginIndex;
                    }
                } else {
                    this.currentRunStart = this.beginIndex;
                }
                if (runIndex < AttributedString.this.runCount - 1) {
                    this.currentRunLimit = AttributedString.this.runStarts[runIndex + 1];
                    if (this.currentRunLimit > this.endIndex) {
                        this.currentRunLimit = this.endIndex;
                    }
                } else {
                    this.currentRunLimit = this.endIndex;
                }
            }
        }
    }

    AttributedString(AttributedCharacterIterator[] iterators) {
        if (iterators == null) {
            throw new NullPointerException("Iterators must not be null");
        } else if (iterators.length == 0) {
            this.text = "";
        } else {
            StringBuffer buffer = new StringBuffer();
            for (CharacterIterator appendContents : iterators) {
                appendContents(buffer, appendContents);
            }
            this.text = buffer.toString();
            if (this.text.length() > 0) {
                int offset = 0;
                Map<Attribute, Object> last = null;
                for (AttributedCharacterIterator iterator : iterators) {
                    int start = iterator.getBeginIndex();
                    int end = iterator.getEndIndex();
                    for (int index = start; index < end; index = iterator.getRunLimit()) {
                        iterator.setIndex(index);
                        Map<Attribute, Object> attrs = iterator.getAttributes();
                        if (mapsDiffer(last, attrs)) {
                            setAttributes(attrs, (index - start) + offset);
                        }
                        last = attrs;
                    }
                    offset += end - start;
                }
            }
        }
    }

    public AttributedString(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
    }

    public AttributedString(String text, Map<? extends Attribute, ?> attributes) {
        if (text == null || attributes == null) {
            throw new NullPointerException();
        }
        this.text = text;
        if (text.length() != 0) {
            int attributeCount = attributes.size();
            if (attributeCount > 0) {
                createRunAttributeDataVectors();
                Vector<Attribute> newRunAttributes = new Vector(attributeCount);
                Vector<Object> newRunAttributeValues = new Vector(attributeCount);
                this.runAttributes[0] = newRunAttributes;
                this.runAttributeValues[0] = newRunAttributeValues;
                for (Entry<? extends Attribute, ?> entry : attributes.entrySet()) {
                    newRunAttributes.addElement((Attribute) entry.getKey());
                    newRunAttributeValues.addElement(entry.getValue());
                }
            }
        } else if (!attributes.isEmpty()) {
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
    }

    public AttributedString(AttributedCharacterIterator text) {
        this(text, text.getBeginIndex(), text.getEndIndex(), null);
    }

    public AttributedString(AttributedCharacterIterator text, int beginIndex, int endIndex) {
        this(text, beginIndex, endIndex, null);
    }

    public AttributedString(AttributedCharacterIterator text, int beginIndex, int endIndex, Attribute[] attributes) {
        if (text == null) {
            throw new NullPointerException();
        }
        int textBeginIndex = text.getBeginIndex();
        int textEndIndex = text.getEndIndex();
        if (beginIndex < textBeginIndex || endIndex > textEndIndex || beginIndex > endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        StringBuffer textBuffer = new StringBuffer();
        text.setIndex(beginIndex);
        char c = text.current();
        while (text.getIndex() < endIndex) {
            textBuffer.append(c);
            c = text.next();
        }
        this.text = textBuffer.toString();
        if (beginIndex != endIndex) {
            HashSet<Attribute> keys = new HashSet();
            if (attributes == null) {
                keys.addAll(text.getAllAttributeKeys());
            } else {
                for (Object add : attributes) {
                    keys.add(add);
                }
                keys.retainAll(text.getAllAttributeKeys());
            }
            if (!keys.isEmpty()) {
                Iterator<Attribute> itr = keys.iterator();
                while (itr.hasNext()) {
                    Attribute attributeKey = (Attribute) itr.next();
                    text.setIndex(textBeginIndex);
                    while (text.getIndex() < endIndex) {
                        int start = text.getRunStart(attributeKey);
                        int limit = text.getRunLimit(attributeKey);
                        Object value = text.getAttribute(attributeKey);
                        if (value != null) {
                            if (!(value instanceof Annotation)) {
                                if (start >= endIndex) {
                                    break;
                                } else if (limit > beginIndex) {
                                    if (start < beginIndex) {
                                        start = beginIndex;
                                    }
                                    if (limit > endIndex) {
                                        limit = endIndex;
                                    }
                                    if (start != limit) {
                                        addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                                    }
                                }
                            } else if (start >= beginIndex && limit <= endIndex) {
                                addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                            } else if (limit > endIndex) {
                                break;
                            }
                        }
                        text.setIndex(limit);
                    }
                }
            }
        }
    }

    public void addAttribute(Attribute attribute, Object value) {
        if (attribute == null) {
            throw new NullPointerException();
        }
        int len = length();
        if (len == 0) {
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        addAttributeImpl(attribute, value, 0, len);
    }

    public void addAttribute(Attribute attribute, Object value, int beginIndex, int endIndex) {
        if (attribute == null) {
            throw new NullPointerException();
        } else if (beginIndex < 0 || endIndex > length() || beginIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        } else {
            addAttributeImpl(attribute, value, beginIndex, endIndex);
        }
    }

    public void addAttributes(Map<? extends Attribute, ?> attributes, int beginIndex, int endIndex) {
        if (attributes == null) {
            throw new NullPointerException();
        } else if (beginIndex < 0 || endIndex > length() || beginIndex > endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        } else if (beginIndex != endIndex) {
            if (this.runCount == 0) {
                createRunAttributeDataVectors();
            }
            int beginRunIndex = ensureRunBreak(beginIndex);
            int endRunIndex = ensureRunBreak(endIndex);
            for (Entry<? extends Attribute, ?> entry : attributes.entrySet()) {
                addAttributeRunData((Attribute) entry.getKey(), entry.getValue(), beginRunIndex, endRunIndex);
            }
        } else if (!attributes.isEmpty()) {
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
    }

    private synchronized void addAttributeImpl(Attribute attribute, Object value, int beginIndex, int endIndex) {
        if (this.runCount == 0) {
            createRunAttributeDataVectors();
        }
        addAttributeRunData(attribute, value, ensureRunBreak(beginIndex), ensureRunBreak(endIndex));
    }

    private final void createRunAttributeDataVectors() {
        Vector<Attribute>[] newRunAttributes = new Vector[10];
        Vector<Object>[] newRunAttributeValues = new Vector[10];
        this.runStarts = new int[10];
        this.runAttributes = newRunAttributes;
        this.runAttributeValues = newRunAttributeValues;
        this.runArraySize = 10;
        this.runCount = 1;
    }

    private final int ensureRunBreak(int offset) {
        return ensureRunBreak(offset, true);
    }

    private final int ensureRunBreak(int offset, boolean copyAttrs) {
        if (offset == length()) {
            return this.runCount;
        }
        int runIndex = 0;
        while (runIndex < this.runCount && this.runStarts[runIndex] < offset) {
            runIndex++;
        }
        if (runIndex < this.runCount && this.runStarts[runIndex] == offset) {
            return runIndex;
        }
        int i;
        if (this.runCount == this.runArraySize) {
            int newArraySize = this.runArraySize + 10;
            int[] newRunStarts = new int[newArraySize];
            Vector<Attribute>[] newRunAttributes = new Vector[newArraySize];
            Vector<Object>[] newRunAttributeValues = new Vector[newArraySize];
            for (i = 0; i < this.runArraySize; i++) {
                newRunStarts[i] = this.runStarts[i];
                newRunAttributes[i] = this.runAttributes[i];
                newRunAttributeValues[i] = this.runAttributeValues[i];
            }
            this.runStarts = newRunStarts;
            this.runAttributes = newRunAttributes;
            this.runAttributeValues = newRunAttributeValues;
            this.runArraySize = newArraySize;
        }
        Vector vector = null;
        Vector vector2 = null;
        if (copyAttrs) {
            Collection oldRunAttributes = this.runAttributes[runIndex - 1];
            Collection oldRunAttributeValues = this.runAttributeValues[runIndex - 1];
            if (oldRunAttributes != null) {
                vector = new Vector(oldRunAttributes);
            }
            if (oldRunAttributeValues != null) {
                vector2 = new Vector(oldRunAttributeValues);
            }
        }
        this.runCount++;
        for (i = this.runCount - 1; i > runIndex; i--) {
            this.runStarts[i] = this.runStarts[i - 1];
            this.runAttributes[i] = this.runAttributes[i - 1];
            this.runAttributeValues[i] = this.runAttributeValues[i - 1];
        }
        this.runStarts[runIndex] = offset;
        this.runAttributes[runIndex] = vector;
        this.runAttributeValues[runIndex] = vector2;
        return runIndex;
    }

    private void addAttributeRunData(Attribute attribute, Object value, int beginRunIndex, int endRunIndex) {
        for (int i = beginRunIndex; i < endRunIndex; i++) {
            int keyValueIndex = -1;
            if (this.runAttributes[i] == null) {
                Vector<Attribute> newRunAttributes = new Vector();
                Vector<Object> newRunAttributeValues = new Vector();
                this.runAttributes[i] = newRunAttributes;
                this.runAttributeValues[i] = newRunAttributeValues;
            } else {
                keyValueIndex = this.runAttributes[i].indexOf(attribute);
            }
            if (keyValueIndex == -1) {
                int oldSize = this.runAttributes[i].size();
                this.runAttributes[i].addElement(attribute);
                try {
                    this.runAttributeValues[i].addElement(value);
                } catch (Exception e) {
                    this.runAttributes[i].setSize(oldSize);
                    this.runAttributeValues[i].setSize(oldSize);
                }
            } else {
                this.runAttributeValues[i].set(keyValueIndex, value);
            }
        }
    }

    public AttributedCharacterIterator getIterator() {
        return getIterator(null, 0, length());
    }

    public AttributedCharacterIterator getIterator(Attribute[] attributes) {
        return getIterator(attributes, 0, length());
    }

    public AttributedCharacterIterator getIterator(Attribute[] attributes, int beginIndex, int endIndex) {
        return new AttributedStringIterator(attributes, beginIndex, endIndex);
    }

    int length() {
        return this.text.length();
    }

    private char charAt(int index) {
        return this.text.charAt(index);
    }

    private synchronized Object getAttribute(Attribute attribute, int runIndex) {
        Vector<Attribute> currentRunAttributes = this.runAttributes[runIndex];
        Vector<Object> currentRunAttributeValues = this.runAttributeValues[runIndex];
        if (currentRunAttributes == null) {
            return null;
        }
        int attributeIndex = currentRunAttributes.indexOf(attribute);
        if (attributeIndex == -1) {
            return null;
        }
        return currentRunAttributeValues.elementAt(attributeIndex);
    }

    private Object getAttributeCheckRange(Attribute attribute, int runIndex, int beginIndex, int endIndex) {
        Object value = getAttribute(attribute, runIndex);
        if (value instanceof Annotation) {
            int currIndex;
            if (beginIndex > 0) {
                currIndex = runIndex;
                int runStart = this.runStarts[runIndex];
                while (runStart >= beginIndex && valuesMatch(value, getAttribute(attribute, currIndex - 1))) {
                    currIndex--;
                    runStart = this.runStarts[currIndex];
                }
                if (runStart < beginIndex) {
                    return null;
                }
            }
            int textLength = length();
            if (endIndex < textLength) {
                currIndex = runIndex;
                int runLimit = runIndex < this.runCount + -1 ? this.runStarts[runIndex + 1] : textLength;
                while (runLimit <= endIndex && valuesMatch(value, getAttribute(attribute, currIndex + 1))) {
                    currIndex++;
                    runLimit = currIndex < this.runCount + -1 ? this.runStarts[currIndex + 1] : textLength;
                }
                if (runLimit > endIndex) {
                    return null;
                }
            }
        }
        return value;
    }

    private boolean attributeValuesMatch(Set<? extends Attribute> attributes, int runIndex1, int runIndex2) {
        for (Attribute key : attributes) {
            if (!valuesMatch(getAttribute(key, runIndex1), getAttribute(key, runIndex2))) {
                return false;
            }
        }
        return true;
    }

    private static final boolean valuesMatch(Object value1, Object value2) {
        if (value1 != null) {
            return value1.lambda$-java_util_function_Predicate_4628(value2);
        }
        return value2 == null;
    }

    private final void appendContents(StringBuffer buf, CharacterIterator iterator) {
        int index = iterator.getBeginIndex();
        int end = iterator.getEndIndex();
        int index2 = index;
        while (index2 < end) {
            index = index2 + 1;
            iterator.setIndex(index2);
            buf.append(iterator.current());
            index2 = index;
        }
    }

    private void setAttributes(Map<Attribute, Object> attrs, int offset) {
        if (this.runCount == 0) {
            createRunAttributeDataVectors();
        }
        int index = ensureRunBreak(offset, false);
        if (attrs != null) {
            int size = attrs.size();
            if (size > 0) {
                Vector<Attribute> runAttrs = new Vector(size);
                Vector<Object> runValues = new Vector(size);
                for (Entry<Attribute, Object> entry : attrs.entrySet()) {
                    runAttrs.add((Attribute) entry.getKey());
                    runValues.add(entry.getValue());
                }
                this.runAttributes[index] = runAttrs;
                this.runAttributeValues[index] = runValues;
            }
        }
    }

    private static <K, V> boolean mapsDiffer(Map<K, V> last, Map<K, V> attrs) {
        boolean z = false;
        if (last != null) {
            return last.equals(attrs) ^ 1;
        }
        if (attrs != null && attrs.size() > 0) {
            z = true;
        }
        return z;
    }
}

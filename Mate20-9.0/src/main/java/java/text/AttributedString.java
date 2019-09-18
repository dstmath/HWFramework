package java.text;

import java.text.AttributedCharacterIterator;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class AttributedString {
    private static final int ARRAY_SIZE_INCREMENT = 10;
    int runArraySize;
    Vector<Object>[] runAttributeValues;
    Vector<AttributedCharacterIterator.Attribute>[] runAttributes;
    int runCount;
    int[] runStarts;
    String text;

    private final class AttributeMap extends AbstractMap<AttributedCharacterIterator.Attribute, Object> {
        int beginIndex;
        int endIndex;
        int runIndex;

        AttributeMap(int runIndex2, int beginIndex2, int endIndex2) {
            this.runIndex = runIndex2;
            this.beginIndex = beginIndex2;
            this.endIndex = endIndex2;
        }

        public Set<Map.Entry<AttributedCharacterIterator.Attribute, Object>> entrySet() {
            HashSet<Map.Entry<AttributedCharacterIterator.Attribute, Object>> set = new HashSet<>();
            synchronized (AttributedString.this) {
                int size = AttributedString.this.runAttributes[this.runIndex].size();
                for (int i = 0; i < size; i++) {
                    AttributedCharacterIterator.Attribute key = AttributedString.this.runAttributes[this.runIndex].get(i);
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
            return AttributedString.this.getAttributeCheckRange((AttributedCharacterIterator.Attribute) key, this.runIndex, this.beginIndex, this.endIndex);
        }
    }

    private final class AttributedStringIterator implements AttributedCharacterIterator {
        private int beginIndex;
        private int currentIndex;
        private int currentRunIndex;
        private int currentRunLimit;
        private int currentRunStart;
        private int endIndex;
        private AttributedCharacterIterator.Attribute[] relevantAttributes;

        AttributedStringIterator(AttributedCharacterIterator.Attribute[] attributes, int beginIndex2, int endIndex2) {
            if (beginIndex2 < 0 || beginIndex2 > endIndex2 || endIndex2 > AttributedString.this.length()) {
                throw new IllegalArgumentException("Invalid substring range");
            }
            this.beginIndex = beginIndex2;
            this.endIndex = endIndex2;
            this.currentIndex = beginIndex2;
            updateRunInfo();
            if (attributes != null) {
                this.relevantAttributes = (AttributedCharacterIterator.Attribute[]) attributes.clone();
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
            if (AttributedString.this == that.getString() && this.currentIndex == that.currentIndex && this.beginIndex == that.beginIndex && this.endIndex == that.endIndex) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return ((AttributedString.this.text.hashCode() ^ this.currentIndex) ^ this.beginIndex) ^ this.endIndex;
        }

        public Object clone() {
            try {
                return (AttributedStringIterator) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError((Throwable) e);
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

        public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
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

        public int getRunStart(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
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

        public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
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

        public int getRunLimit(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
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

        public Map<AttributedCharacterIterator.Attribute, Object> getAttributes() {
            if (AttributedString.this.runAttributes == null || this.currentRunIndex == -1 || AttributedString.this.runAttributes[this.currentRunIndex] == null) {
                return new Hashtable();
            }
            return new AttributeMap(this.currentRunIndex, this.beginIndex, this.endIndex);
        }

        public Set<AttributedCharacterIterator.Attribute> getAllAttributeKeys() {
            Set<AttributedCharacterIterator.Attribute> keys;
            if (AttributedString.this.runAttributes == null) {
                return new HashSet();
            }
            synchronized (AttributedString.this) {
                keys = new HashSet<>();
                for (int i = 0; i < AttributedString.this.runCount; i++) {
                    if (AttributedString.this.runStarts[i] < this.endIndex && (i == AttributedString.this.runCount - 1 || AttributedString.this.runStarts[i + 1] > this.beginIndex)) {
                        Vector<AttributedCharacterIterator.Attribute> currentRunAttributes = AttributedString.this.runAttributes[i];
                        if (currentRunAttributes != null) {
                            int j = currentRunAttributes.size();
                            while (true) {
                                int j2 = j - 1;
                                if (j <= 0) {
                                    break;
                                }
                                keys.add(currentRunAttributes.get(j2));
                                j = j2;
                            }
                        }
                    }
                }
            }
            return keys;
        }

        public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
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
            int runIndex;
            int runIndex2 = -1;
            if (this.currentIndex == this.endIndex) {
                int i = this.endIndex;
                this.currentRunLimit = i;
                this.currentRunStart = i;
                this.currentRunIndex = -1;
                return;
            }
            synchronized (AttributedString.this) {
                while (true) {
                    runIndex = runIndex2;
                    if (runIndex >= AttributedString.this.runCount - 1 || AttributedString.this.runStarts[runIndex + 1] > this.currentIndex) {
                        this.currentRunIndex = runIndex;
                    } else {
                        runIndex2 = runIndex + 1;
                    }
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
            int counter = 0;
            for (AttributedCharacterIterator appendContents : iterators) {
                appendContents(buffer, appendContents);
            }
            this.text = buffer.toString();
            if (this.text.length() > 0) {
                int offset = 0;
                Map<AttributedCharacterIterator.Attribute, Object> last = null;
                while (counter < iterators.length) {
                    AttributedCharacterIterator iterator = iterators[counter];
                    int start = iterator.getBeginIndex();
                    int end = iterator.getEndIndex();
                    Map<AttributedCharacterIterator.Attribute, Object> last2 = last;
                    for (int index = start; index < end; index = iterator.getRunLimit()) {
                        iterator.setIndex(index);
                        Map<AttributedCharacterIterator.Attribute, Object> attrs = iterator.getAttributes();
                        if (mapsDiffer(last2, attrs)) {
                            setAttributes(attrs, (index - start) + offset);
                        }
                        last2 = attrs;
                    }
                    offset += end - start;
                    counter++;
                    last = last2;
                }
            }
        }
    }

    public AttributedString(String text2) {
        if (text2 != null) {
            this.text = text2;
            return;
        }
        throw new NullPointerException();
    }

    public AttributedString(String text2, Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        if (text2 == null || attributes == null) {
            throw new NullPointerException();
        }
        this.text = text2;
        if (text2.length() != 0) {
            int attributeCount = attributes.size();
            if (attributeCount > 0) {
                createRunAttributeDataVectors();
                Vector<AttributedCharacterIterator.Attribute> newRunAttributes = new Vector<>(attributeCount);
                Vector<Object> newRunAttributeValues = new Vector<>(attributeCount);
                this.runAttributes[0] = newRunAttributes;
                this.runAttributeValues[0] = newRunAttributeValues;
                for (Map.Entry<? extends AttributedCharacterIterator.Attribute, ?> entry : attributes.entrySet()) {
                    newRunAttributes.addElement((AttributedCharacterIterator.Attribute) entry.getKey());
                    newRunAttributeValues.addElement(entry.getValue());
                }
            }
        } else if (!attributes.isEmpty()) {
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
    }

    public AttributedString(AttributedCharacterIterator text2) {
        this(text2, text2.getBeginIndex(), text2.getEndIndex(), null);
    }

    public AttributedString(AttributedCharacterIterator text2, int beginIndex, int endIndex) {
        this(text2, beginIndex, endIndex, null);
    }

    public AttributedString(AttributedCharacterIterator text2, int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
        if (text2 != null) {
            int textBeginIndex = text2.getBeginIndex();
            int textEndIndex = text2.getEndIndex();
            if (beginIndex < textBeginIndex || endIndex > textEndIndex || beginIndex > endIndex) {
                throw new IllegalArgumentException("Invalid substring range");
            }
            StringBuffer textBuffer = new StringBuffer();
            text2.setIndex(beginIndex);
            char c = text2.current();
            while (text2.getIndex() < endIndex) {
                textBuffer.append(c);
                c = text2.next();
            }
            this.text = textBuffer.toString();
            if (beginIndex != endIndex) {
                HashSet<AttributedCharacterIterator.Attribute> keys = new HashSet<>();
                if (attributes == null) {
                    keys.addAll(text2.getAllAttributeKeys());
                } else {
                    for (AttributedCharacterIterator.Attribute add : attributes) {
                        keys.add(add);
                    }
                    keys.retainAll(text2.getAllAttributeKeys());
                }
                if (!keys.isEmpty()) {
                    Iterator<AttributedCharacterIterator.Attribute> itr = keys.iterator();
                    while (itr.hasNext()) {
                        AttributedCharacterIterator.Attribute attributeKey = itr.next();
                        text2.setIndex(textBeginIndex);
                        while (text2.getIndex() < endIndex) {
                            int start = text2.getRunStart(attributeKey);
                            int limit = text2.getRunLimit(attributeKey);
                            Object value = text2.getAttribute(attributeKey);
                            if (value != null) {
                                if (value instanceof Annotation) {
                                    if (start >= beginIndex && limit <= endIndex) {
                                        addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                                    } else if (limit > endIndex) {
                                        break;
                                    }
                                } else if (start >= endIndex) {
                                    break;
                                } else if (limit > beginIndex) {
                                    start = start < beginIndex ? beginIndex : start;
                                    limit = limit > endIndex ? endIndex : limit;
                                    if (start != limit) {
                                        addAttribute(attributeKey, value, start - beginIndex, limit - beginIndex);
                                    }
                                }
                            }
                            text2.setIndex(limit);
                        }
                    }
                    return;
                }
                return;
            }
            return;
        }
        throw new NullPointerException();
    }

    public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value) {
        if (attribute != null) {
            int len = length();
            if (len != 0) {
                addAttributeImpl(attribute, value, 0, len);
                return;
            }
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
        throw new NullPointerException();
    }

    public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value, int beginIndex, int endIndex) {
        if (attribute == null) {
            throw new NullPointerException();
        } else if (beginIndex < 0 || endIndex > length() || beginIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        } else {
            addAttributeImpl(attribute, value, beginIndex, endIndex);
        }
    }

    public void addAttributes(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes, int beginIndex, int endIndex) {
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
            for (Map.Entry<? extends AttributedCharacterIterator.Attribute, ?> entry : attributes.entrySet()) {
                addAttributeRunData((AttributedCharacterIterator.Attribute) entry.getKey(), entry.getValue(), beginRunIndex, endRunIndex);
            }
        } else if (!attributes.isEmpty()) {
            throw new IllegalArgumentException("Can't add attribute to 0-length text");
        }
    }

    private synchronized void addAttributeImpl(AttributedCharacterIterator.Attribute attribute, Object value, int beginIndex, int endIndex) {
        if (this.runCount == 0) {
            createRunAttributeDataVectors();
        }
        addAttributeRunData(attribute, value, ensureRunBreak(beginIndex), ensureRunBreak(endIndex));
    }

    private final void createRunAttributeDataVectors() {
        this.runStarts = new int[10];
        this.runAttributes = new Vector[10];
        this.runAttributeValues = new Vector[10];
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
        if (this.runCount == this.runArraySize) {
            int newArraySize = this.runArraySize + 10;
            int[] newRunStarts = new int[newArraySize];
            Vector<AttributedCharacterIterator.Attribute>[] newRunAttributes = new Vector[newArraySize];
            Vector<Object>[] newRunAttributeValues = new Vector[newArraySize];
            for (int i = 0; i < this.runArraySize; i++) {
                newRunStarts[i] = this.runStarts[i];
                newRunAttributes[i] = this.runAttributes[i];
                newRunAttributeValues[i] = this.runAttributeValues[i];
            }
            this.runStarts = newRunStarts;
            this.runAttributes = newRunAttributes;
            this.runAttributeValues = newRunAttributeValues;
            this.runArraySize = newArraySize;
        }
        Vector<AttributedCharacterIterator.Attribute> newRunAttributes2 = null;
        Vector<Object> newRunAttributeValues2 = null;
        if (copyAttrs) {
            Vector<AttributedCharacterIterator.Attribute> oldRunAttributes = this.runAttributes[runIndex - 1];
            Vector<Object> oldRunAttributeValues = this.runAttributeValues[runIndex - 1];
            if (oldRunAttributes != null) {
                newRunAttributes2 = new Vector<>((Collection<? extends AttributedCharacterIterator.Attribute>) oldRunAttributes);
            }
            if (oldRunAttributeValues != null) {
                newRunAttributeValues2 = new Vector<>((Collection<? extends Object>) oldRunAttributeValues);
            }
        }
        this.runCount++;
        for (int i2 = this.runCount - 1; i2 > runIndex; i2--) {
            this.runStarts[i2] = this.runStarts[i2 - 1];
            this.runAttributes[i2] = this.runAttributes[i2 - 1];
            this.runAttributeValues[i2] = this.runAttributeValues[i2 - 1];
        }
        this.runStarts[runIndex] = offset;
        this.runAttributes[runIndex] = newRunAttributes2;
        this.runAttributeValues[runIndex] = newRunAttributeValues2;
        return runIndex;
    }

    private void addAttributeRunData(AttributedCharacterIterator.Attribute attribute, Object value, int beginRunIndex, int endRunIndex) {
        for (int i = beginRunIndex; i < endRunIndex; i++) {
            int keyValueIndex = -1;
            if (this.runAttributes[i] == null) {
                Vector<AttributedCharacterIterator.Attribute> newRunAttributes = new Vector<>();
                Vector<Object> newRunAttributeValues = new Vector<>();
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

    public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes) {
        return getIterator(attributes, 0, length());
    }

    public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes, int beginIndex, int endIndex) {
        return new AttributedStringIterator(attributes, beginIndex, endIndex);
    }

    /* access modifiers changed from: package-private */
    public int length() {
        return this.text.length();
    }

    /* access modifiers changed from: private */
    public char charAt(int index) {
        return this.text.charAt(index);
    }

    /* access modifiers changed from: private */
    public synchronized Object getAttribute(AttributedCharacterIterator.Attribute attribute, int runIndex) {
        Vector<AttributedCharacterIterator.Attribute> currentRunAttributes = this.runAttributes[runIndex];
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

    /* access modifiers changed from: private */
    public Object getAttributeCheckRange(AttributedCharacterIterator.Attribute attribute, int runIndex, int beginIndex, int endIndex) {
        Object value = getAttribute(attribute, runIndex);
        if (value instanceof Annotation) {
            if (beginIndex > 0) {
                int currIndex = runIndex;
                int runStart = this.runStarts[currIndex];
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
                int currIndex2 = runIndex;
                int runLimit = currIndex2 < this.runCount + -1 ? this.runStarts[currIndex2 + 1] : textLength;
                while (runLimit <= endIndex && valuesMatch(value, getAttribute(attribute, currIndex2 + 1))) {
                    currIndex2++;
                    runLimit = currIndex2 < this.runCount + -1 ? this.runStarts[currIndex2 + 1] : textLength;
                }
                if (runLimit > endIndex) {
                    return null;
                }
            }
        }
        return value;
    }

    /* access modifiers changed from: private */
    public boolean attributeValuesMatch(Set<? extends AttributedCharacterIterator.Attribute> attributes, int runIndex1, int runIndex2) {
        for (AttributedCharacterIterator.Attribute key : attributes) {
            if (!valuesMatch(getAttribute(key, runIndex1), getAttribute(key, runIndex2))) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static final boolean valuesMatch(Object value1, Object value2) {
        if (value1 != null) {
            return value1.equals(value2);
        }
        return value2 == null;
    }

    private final void appendContents(StringBuffer buf, CharacterIterator iterator) {
        int end = iterator.getEndIndex();
        for (int index = iterator.getBeginIndex(); index < end; index++) {
            iterator.setIndex(index);
            buf.append(iterator.current());
        }
    }

    private void setAttributes(Map<AttributedCharacterIterator.Attribute, Object> attrs, int offset) {
        if (this.runCount == 0) {
            createRunAttributeDataVectors();
        }
        int index = ensureRunBreak(offset, false);
        if (attrs != null) {
            int size = attrs.size();
            int size2 = size;
            if (size > 0) {
                Vector<AttributedCharacterIterator.Attribute> runAttrs = new Vector<>(size2);
                Vector<Object> runValues = new Vector<>(size2);
                for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : attrs.entrySet()) {
                    runAttrs.add(entry.getKey());
                    runValues.add(entry.getValue());
                }
                this.runAttributes[index] = runAttrs;
                this.runAttributeValues[index] = runValues;
            }
        }
    }

    private static <K, V> boolean mapsDiffer(Map<K, V> last, Map<K, V> attrs) {
        boolean z = true;
        if (last != null) {
            return true ^ last.equals(attrs);
        }
        if (attrs == null || attrs.size() <= 0) {
            z = false;
        }
        return z;
    }
}

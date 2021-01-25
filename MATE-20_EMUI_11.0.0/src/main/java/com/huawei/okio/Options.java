package com.huawei.okio;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public final class Options extends AbstractList<ByteString> implements RandomAccess {
    final ByteString[] byteStrings;
    final int[] trie;

    private Options(ByteString[] byteStrings2, int[] trie2) {
        this.byteStrings = byteStrings2;
        this.trie = trie2;
    }

    public static Options of(ByteString... byteStrings2) {
        if (byteStrings2.length == 0) {
            return new Options(new ByteString[0], new int[]{0, -1});
        }
        List<ByteString> list = new ArrayList<>(Arrays.asList(byteStrings2));
        Collections.sort(list);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            indexes.add(-1);
        }
        for (int i2 = 0; i2 < list.size(); i2++) {
            indexes.set(Collections.binarySearch(list, byteStrings2[i2]), Integer.valueOf(i2));
        }
        if (list.get(0).size() != 0) {
            for (int a = 0; a < list.size(); a++) {
                ByteString prefix = list.get(a);
                int b = a + 1;
                while (b < list.size()) {
                    ByteString byteString = list.get(b);
                    if (!byteString.startsWith(prefix)) {
                        continue;
                        break;
                    } else if (byteString.size() == prefix.size()) {
                        throw new IllegalArgumentException("duplicate option: " + byteString);
                    } else if (indexes.get(b).intValue() > indexes.get(a).intValue()) {
                        list.remove(b);
                        indexes.remove(b);
                    } else {
                        b++;
                    }
                }
            }
            Buffer trieBytes = new Buffer();
            buildTrieRecursive(0, trieBytes, 0, list, 0, list.size(), indexes);
            int[] trie2 = new int[intCount(trieBytes)];
            for (int i3 = 0; i3 < trie2.length; i3++) {
                trie2[i3] = trieBytes.readInt();
            }
            if (trieBytes.exhausted()) {
                return new Options((ByteString[]) byteStrings2.clone(), trie2);
            }
            throw new AssertionError();
        }
        throw new IllegalArgumentException("the empty byte string is not a supported option");
    }

    private static void buildTrieRecursive(long nodeOffset, Buffer node, int byteStringOffset, List<ByteString> byteStrings2, int fromIndex, int toIndex, List<Integer> indexes) {
        int fromIndex2;
        ByteString from;
        int prefixIndex;
        int rangeEnd;
        Buffer childNodes;
        int selectChoiceCount;
        int rangeEnd2;
        int prefixIndex2;
        List<Integer> list = indexes;
        if (fromIndex < toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                if (byteStrings2.get(i).size() < byteStringOffset) {
                    throw new AssertionError();
                }
            }
            ByteString from2 = byteStrings2.get(fromIndex);
            ByteString to = byteStrings2.get(toIndex - 1);
            if (byteStringOffset == from2.size()) {
                int prefixIndex3 = list.get(fromIndex).intValue();
                int fromIndex3 = fromIndex + 1;
                fromIndex2 = fromIndex3;
                from = byteStrings2.get(fromIndex3);
                prefixIndex = prefixIndex3;
            } else {
                fromIndex2 = fromIndex;
                from = from2;
                prefixIndex = -1;
            }
            if (from.getByte(byteStringOffset) != to.getByte(byteStringOffset)) {
                int selectChoiceCount2 = 1;
                for (int i2 = fromIndex2 + 1; i2 < toIndex; i2++) {
                    if (byteStrings2.get(i2 - 1).getByte(byteStringOffset) != byteStrings2.get(i2).getByte(byteStringOffset)) {
                        selectChoiceCount2++;
                    }
                }
                long childNodesOffset = nodeOffset + ((long) intCount(node)) + 2 + ((long) (selectChoiceCount2 * 2));
                node.writeInt(selectChoiceCount2);
                node.writeInt(prefixIndex);
                for (int i3 = fromIndex2; i3 < toIndex; i3++) {
                    byte rangeByte = byteStrings2.get(i3).getByte(byteStringOffset);
                    if (i3 == fromIndex2 || rangeByte != byteStrings2.get(i3 - 1).getByte(byteStringOffset)) {
                        node.writeInt(rangeByte & 255);
                    }
                }
                Buffer childNodes2 = new Buffer();
                int rangeStart = fromIndex2;
                while (rangeStart < toIndex) {
                    byte rangeByte2 = byteStrings2.get(rangeStart).getByte(byteStringOffset);
                    int i4 = rangeStart + 1;
                    while (true) {
                        if (i4 >= toIndex) {
                            rangeEnd = toIndex;
                            break;
                        } else if (rangeByte2 != byteStrings2.get(i4).getByte(byteStringOffset)) {
                            rangeEnd = i4;
                            break;
                        } else {
                            i4++;
                        }
                    }
                    if (rangeStart + 1 == rangeEnd && byteStringOffset + 1 == byteStrings2.get(rangeStart).size()) {
                        node.writeInt(list.get(rangeStart).intValue());
                        rangeEnd2 = rangeEnd;
                        childNodes = childNodes2;
                        selectChoiceCount = selectChoiceCount2;
                        prefixIndex2 = prefixIndex;
                    } else {
                        node.writeInt((int) ((childNodesOffset + ((long) intCount(childNodes2))) * -1));
                        rangeEnd2 = rangeEnd;
                        childNodes = childNodes2;
                        selectChoiceCount = selectChoiceCount2;
                        prefixIndex2 = prefixIndex;
                        buildTrieRecursive(childNodesOffset, childNodes2, byteStringOffset + 1, byteStrings2, rangeStart, rangeEnd2, indexes);
                    }
                    rangeStart = rangeEnd2;
                    childNodes2 = childNodes;
                    prefixIndex = prefixIndex2;
                    selectChoiceCount2 = selectChoiceCount;
                    list = indexes;
                }
                node.write(childNodes2, childNodes2.size());
                return;
            }
            int i5 = byteStringOffset;
            int max = Math.min(from.size(), to.size());
            int scanByteCount = 0;
            while (i5 < max && from.getByte(i5) == to.getByte(i5)) {
                scanByteCount++;
                i5++;
            }
            long childNodesOffset2 = nodeOffset + ((long) intCount(node)) + 2 + ((long) scanByteCount) + 1;
            node.writeInt(-scanByteCount);
            node.writeInt(prefixIndex);
            for (int i6 = byteStringOffset; i6 < byteStringOffset + scanByteCount; i6++) {
                node.writeInt(from.getByte(i6) & 255);
            }
            if (fromIndex2 + 1 != toIndex) {
                Buffer childNodes3 = new Buffer();
                node.writeInt((int) ((childNodesOffset2 + ((long) intCount(childNodes3))) * -1));
                buildTrieRecursive(childNodesOffset2, childNodes3, byteStringOffset + scanByteCount, byteStrings2, fromIndex2, toIndex, indexes);
                node.write(childNodes3, childNodes3.size());
            } else if (byteStringOffset + scanByteCount == byteStrings2.get(fromIndex2).size()) {
                node.writeInt(indexes.get(fromIndex2).intValue());
            } else {
                throw new AssertionError();
            }
        } else {
            throw new AssertionError();
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public ByteString get(int i) {
        return this.byteStrings[i];
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public final int size() {
        return this.byteStrings.length;
    }

    private static int intCount(Buffer trieBytes) {
        return (int) (trieBytes.size() / 4);
    }
}

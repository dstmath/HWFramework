package com.huawei.i18n.tmr.address.util;

import java.util.ArrayList;
import java.util.Collections;

public class ItemList {
    private Item item = null;
    private ArrayList<Item> itemList = new ArrayList<>();

    public void add(int begin, int end) {
        this.item = new Item(begin, end);
        this.itemList.add(this.item);
    }

    public void changeEnd(int end) {
        this.item.setEnd(end);
    }

    public int getCurrentBegin() {
        Item item2 = this.item;
        if (item2 != null) {
            return item2.getBegin();
        }
        return 0;
    }

    public int getCurrentEnd() {
        Item item2 = this.item;
        if (item2 != null) {
            return item2.getEnd();
        }
        return 0;
    }

    public int[] toArray() {
        int[] result = new int[((this.itemList.size() * 2) + 1)];
        result[0] = this.itemList.size();
        for (int i = 0; i < this.itemList.size(); i++) {
            result[(i * 2) + 1] = this.itemList.get(i).getBegin();
            result[(i * 2) + 2] = this.itemList.get(i).getEnd() - 1;
        }
        return result;
    }

    public int[] getData() {
        int[] result = new int[((this.itemList.size() * 2) + 1)];
        result[0] = this.itemList.size();
        for (int i = 0; i < this.itemList.size(); i++) {
            result[(i * 2) + 1] = this.itemList.get(i).getBegin();
            result[(i * 2) + 2] = this.itemList.get(i).getEnd();
        }
        return result;
    }

    public void sort() {
        ArrayList<Item> arrayList = this.itemList;
        if (arrayList != null) {
            Collections.sort(arrayList);
        }
    }

    public void delRel() {
        ItemList items = new ItemList();
        int[] data = getData();
        int size = data[0];
        if (size > 0) {
            items.add(data[1], data[2]);
        }
        for (int i = 1; i < size; i++) {
            int currentBegin = items.getCurrentBegin();
            int currentEnd = items.getCurrentEnd();
            int begin = data[(i * 2) + 1];
            int end = data[(i * 2) + 2];
            if (currentBegin != begin || currentEnd != end) {
                items.add(begin, end);
            }
        }
        this.itemList = items.itemList;
    }

    /* access modifiers changed from: package-private */
    public static class Item implements Comparable {
        private int begin;
        private int end;

        Item(int begin2, int end2) {
            this.begin = begin2;
            this.end = end2;
        }

        /* access modifiers changed from: package-private */
        public int getBegin() {
            return this.begin;
        }

        public void setBegin(int begin2) {
            this.begin = begin2;
        }

        public int getEnd() {
            return this.end;
        }

        public void setEnd(int end2) {
            this.end = end2;
        }

        @Override // java.lang.Comparable
        public int compareTo(Object obj) {
            if (obj == null || !(obj instanceof Item)) {
                return 0;
            }
            Item item = (Item) obj;
            int currentBegin = this.begin;
            int currentEnd = this.end;
            int nextBegin = item.begin;
            int nextEnd = item.end;
            if (currentBegin < nextBegin) {
                return -1;
            }
            if (currentBegin == nextBegin) {
                return Integer.compare(currentEnd, nextEnd);
            }
            return 1;
        }
    }
}

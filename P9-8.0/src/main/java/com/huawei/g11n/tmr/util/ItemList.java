package com.huawei.g11n.tmr.util;

import java.util.ArrayList;
import java.util.Collections;

public class ItemList {
    private Item cItem;
    private ArrayList<Item> mList;

    static class Item implements Comparable {
        private int begin;
        private int end;

        public Item(int b, int e) {
            this.begin = b;
            this.end = e;
        }

        public int getBegin() {
            return this.begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return this.end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int compareTo(Object o) {
            Item it = (Item) o;
            int cb = this.begin;
            int ce = this.end;
            int nb = it.begin;
            int ne = it.end;
            if (cb < nb) {
                return -1;
            }
            if (cb != nb) {
                return 1;
            }
            if (ce < ne) {
                return -1;
            }
            if (ce != ne) {
                return 1;
            }
            return 0;
        }
    }

    public ItemList() {
        this.mList = null;
        this.cItem = null;
        this.mList = new ArrayList();
    }

    public void add(int b, int e) {
        this.cItem = new Item(b, e);
        this.mList.add(this.cItem);
    }

    public void changeEnd(int e) {
        this.cItem.setEnd(e);
    }

    public int getCurrenBegin() {
        return this.cItem == null ? 0 : this.cItem.getBegin();
    }

    public int getCurrenEnd() {
        return this.cItem == null ? 0 : this.cItem.getEnd();
    }

    public int[] toArray() {
        int[] r = new int[((this.mList.size() * 2) + 1)];
        r[0] = this.mList.size();
        for (int i = 0; i < this.mList.size(); i++) {
            r[(i * 2) + 1] = ((Item) this.mList.get(i)).getBegin();
            r[(i * 2) + 2] = ((Item) this.mList.get(i)).getEnd() - 1;
        }
        return r;
    }

    public int[] getData() {
        int[] r = new int[((this.mList.size() * 2) + 1)];
        r[0] = this.mList.size();
        for (int i = 0; i < this.mList.size(); i++) {
            r[(i * 2) + 1] = ((Item) this.mList.get(i)).getBegin();
            r[(i * 2) + 2] = ((Item) this.mList.get(i)).getEnd();
        }
        return r;
    }

    public void sort() {
        if (this.mList != null) {
            Collections.sort(this.mList);
        }
    }

    public void delRel() {
        ItemList il = new ItemList();
        int[] r = getData();
        int size = r[0];
        if (size > 0) {
            il.add(r[1], r[2]);
        }
        for (int i = 1; i < size; i++) {
            int b1 = il.getCurrenBegin();
            int e1 = il.getCurrenEnd();
            int b = r[(i * 2) + 1];
            int e = r[(i * 2) + 2];
            if (b1 != b || e1 != e) {
                il.add(b, e);
            }
        }
        this.mList = il.mList;
    }
}

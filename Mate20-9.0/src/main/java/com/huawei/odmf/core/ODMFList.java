package com.huawei.odmf.core;

import android.support.annotation.NonNull;
import com.huawei.odmf.exception.ODMFConcurrentModificationException;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFUnsupportedOperationException;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.JudgeUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ODMFList<E> implements List<E> {
    private int arraySize;
    private ObjectContext ctx;
    private String entityName;
    private List<E> insertList;
    /* access modifiers changed from: private */
    public int listSize;
    private List<E> objList;
    private ManagedObject odmfBaseObj;
    /* access modifiers changed from: private */
    public transient int odmfModCount;
    private int relationshipIndex;
    private List<E> removeList;

    private class ODMFItr implements Iterator<E> {
        int cursor;
        int expectedModCount;
        int odmfLastRet;
        protected int odmfLimit;

        private ODMFItr() {
            this.odmfLimit = ODMFList.this.listSize;
            this.expectedModCount = ODMFList.this.odmfModCount;
            this.odmfLastRet = -1;
        }

        public boolean hasNext() {
            return this.cursor < this.odmfLimit;
        }

        public E next() {
            checkConcurrentModification();
            int i = this.cursor;
            if (i >= this.odmfLimit) {
                throw new NoSuchElementException();
            }
            this.cursor = i + 1;
            this.odmfLastRet = i;
            return ODMFList.this.get(this.odmfLastRet);
        }

        public void remove() {
            if (this.odmfLastRet < 0) {
                throw new ODMFIllegalStateException();
            }
            checkConcurrentModification();
            try {
                ODMFList.this.remove(this.odmfLastRet);
                this.cursor = this.odmfLastRet;
                this.odmfLastRet = -1;
                this.expectedModCount = ODMFList.this.odmfModCount;
                this.odmfLimit--;
            } catch (IndexOutOfBoundsException e) {
                throw new ODMFConcurrentModificationException();
            }
        }

        /* access modifiers changed from: package-private */
        public void checkConcurrentModification() {
            if (ODMFList.this.odmfModCount != this.expectedModCount) {
                throw new ODMFConcurrentModificationException();
            }
        }
    }

    private class ODMFListItr extends ODMFList<E>.ODMFItr implements ListIterator<E> {
        ODMFListItr(int index) {
            super();
            if (index < 0 || index > this.odmfLimit) {
                throw new IndexOutOfBoundsException();
            }
            this.cursor = index;
        }

        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        public E previous() {
            checkConcurrentModification();
            int i = this.cursor - 1;
            if (i < 0) {
                throw new NoSuchElementException();
            }
            this.cursor = i;
            this.odmfLastRet = i;
            return ODMFList.this.get(this.odmfLastRet);
        }

        public int nextIndex() {
            return this.cursor;
        }

        public int previousIndex() {
            return this.cursor - 1;
        }

        public void set(E e) {
            if (this.odmfLastRet < 0) {
                throw new ODMFIllegalStateException();
            }
            checkConcurrentModification();
            try {
                ODMFList.this.set(this.odmfLastRet, e);
            } catch (IndexOutOfBoundsException e2) {
                throw new ODMFConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkConcurrentModification();
            try {
                int i = this.cursor;
                ODMFList.this.add(i, e);
                this.cursor = i + 1;
                this.odmfLastRet = -1;
                this.expectedModCount = ODMFList.this.odmfModCount;
                this.odmfLimit++;
            } catch (IndexOutOfBoundsException e2) {
                throw new ODMFConcurrentModificationException();
            }
        }
    }

    public ODMFList(ObjectContext ctx2, String entityName2) {
        this(ctx2, entityName2, null);
    }

    public ODMFList(ObjectContext ctx2, String entityName2, ManagedObject baseObj) {
        this.listSize = 0;
        this.odmfModCount = 0;
        this.odmfBaseObj = null;
        this.arraySize = 500;
        this.relationshipIndex = -1;
        if (ctx2 == null || entityName2 == null) {
            throw new ODMFIllegalArgumentException("When new ODMFList, at least one input parameter is null");
        }
        this.ctx = ctx2;
        this.entityName = entityName2;
        this.objList = new ArrayList();
        if (baseObj != null) {
            this.odmfBaseObj = baseObj;
            this.insertList = new ArrayList();
            this.removeList = new ArrayList();
        }
    }

    public ManagedObject getBaseObj() {
        return this.odmfBaseObj;
    }

    public int getRelationshipIndex() {
        return this.relationshipIndex;
    }

    public void setRelationshipIndex(int relationShipIndex) {
        this.relationshipIndex = relationShipIndex;
    }

    public int size() {
        return this.listSize;
    }

    public boolean isEmpty() {
        return this.listSize == 0;
    }

    public boolean contains(Object o) {
        JudgeUtils.checkNull(o);
        return this.objList.contains(o);
    }

    public boolean add(E e) {
        addObj(this.listSize, e);
        addUpdate();
        insertAdd(e);
        if (getRelationshipIndex() >= 0 && this.odmfBaseObj != null) {
            ((AManagedObject) this.odmfBaseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return true;
    }

    public void add(int index, E element) {
        addObj(index, element);
        addUpdate();
        insertAdd(element);
        if (getRelationshipIndex() >= 0 && this.odmfBaseObj != null) {
            ((AManagedObject) this.odmfBaseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
    }

    public void addObj(int index, E e) {
        if (index < 0 || index > this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize()");
        }
        checkValue(e);
        this.objList.add(index, e);
        this.odmfModCount++;
        this.listSize++;
    }

    public boolean addAll(@NonNull Collection<? extends E> c) {
        JudgeUtils.checkNull(c);
        List<E> list = new ArrayList<>();
        for (E o : c) {
            checkValue(o);
            list.add(o);
        }
        addObjAll(this.listSize, list);
        addUpdate();
        insertAdd(list);
        return true;
    }

    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        JudgeUtils.checkNull(c);
        if (index < 0 || index > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index " + index + ", listSize is " + this.listSize);
        }
        List<E> list = new ArrayList<>();
        for (E o : c) {
            checkValue(o);
            list.add(o);
        }
        addObjAll(index, list);
        addUpdate();
        insertAdd(list);
        return true;
    }

    public void addObjAll(int index, Collection<? extends E> c) {
        this.objList.addAll(index, c);
        this.listSize += c.size();
        this.odmfModCount++;
    }

    public boolean remove(Object o) {
        checkValue(o);
        if (!this.objList.remove(o)) {
            return false;
        }
        this.listSize--;
        this.odmfModCount++;
        addUpdate();
        insertRemove(o);
        if (getRelationshipIndex() >= 0 && this.odmfBaseObj != null) {
            ((AManagedObject) this.odmfBaseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return true;
    }

    public E remove(int index) {
        if (index < 0 || index >= this.listSize) {
            throw new IndexOutOfBoundsException();
        }
        E remove = this.objList.remove(index);
        this.listSize--;
        this.odmfModCount++;
        addUpdate();
        insertRemove(remove);
        if (getRelationshipIndex() >= 0 && this.odmfBaseObj != null) {
            ((AManagedObject) this.odmfBaseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return remove;
    }

    public boolean containsAll(@NonNull Collection<?> c) {
        JudgeUtils.checkNull(c);
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    public boolean removeAll(@NonNull Collection<?> c) {
        JudgeUtils.checkNull(c);
        boolean flag = false;
        for (Object o : c) {
            try {
                checkValue(o);
                if (this.objList.remove(o)) {
                    this.listSize--;
                    this.odmfModCount++;
                    if (!flag) {
                        flag = true;
                    }
                    insertRemove(o);
                }
            } catch (ODMFIllegalArgumentException e) {
            }
        }
        if (flag) {
            addUpdate();
        }
        return true;
    }

    public boolean retainAll(@NonNull Collection<?> c) {
        JudgeUtils.checkNull(c);
        List<E> list = new ArrayList<>();
        int size = this.objList.size();
        for (int i = 0; i < size; i++) {
            E e = this.objList.get(i);
            if (!c.contains(e)) {
                list.add(e);
                insertRemove(e);
            }
        }
        if (list.isEmpty()) {
            return false;
        }
        this.objList.removeAll(list);
        this.odmfModCount += list.size();
        this.listSize -= list.size();
        addUpdate();
        return true;
    }

    public void clear() {
        for (int i = 0; i < this.listSize; i++) {
            insertRemove(this.objList.get(i));
        }
        this.objList.clear();
        this.listSize = 0;
        this.odmfModCount++;
        addUpdate();
    }

    public E get(int index) {
        if (index >= 0 && index < this.listSize) {
            return this.objList.get(index);
        }
        throw new IndexOutOfBoundsException("index < 0 || index >= listSize");
    }

    public E set(int index, E element) {
        if (index < 0 || index >= this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize");
        }
        checkValue(element);
        E set = this.objList.set(index, element);
        addUpdate();
        insertRemove(set);
        insertAdd(element);
        return set;
    }

    public int indexOf(Object o) {
        JudgeUtils.checkNull(o);
        return this.objList.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        JudgeUtils.checkNull(o);
        return this.objList.lastIndexOf(o);
    }

    @NonNull
    public Object[] toArray() {
        if (this.listSize <= this.arraySize) {
            return this.objList.toArray();
        }
        throw new ODMFUnsupportedOperationException("This operation is not supported, because returning a lots of objects will result in out of memory");
    }

    @NonNull
    public <T> T[] toArray(@NonNull T[] a) {
        if (a.length >= this.listSize) {
            for (int i = 0; i < this.listSize; i++) {
                a[i] = this.objList.get(i);
            }
            return a;
        }
        T[] newArray = (Object[]) ((Object[]) Array.newInstance(a.getClass().getComponentType(), this.listSize));
        for (int i2 = 0; i2 < this.listSize; i2++) {
            newArray[i2] = this.objList.get(i2);
        }
        return newArray;
    }

    @NonNull
    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index ");
        }
        List<E> list = new ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            list.add(this.objList.get(i));
        }
        return list;
    }

    @NonNull
    public Iterator<E> iterator() {
        if (this.objList != null) {
            return new ODMFItr();
        }
        throw new ODMFIllegalStateException("ODMFList has not been initialized.");
    }

    public ListIterator<E> listIterator() {
        if (this.objList != null) {
            return new ODMFListItr(0);
        }
        throw new ODMFIllegalStateException("ODMFList has not been initialized.");
    }

    @NonNull
    public ListIterator<E> listIterator(int index) {
        if (this.objList != null) {
            return new ODMFListItr(index);
        }
        throw new ODMFIllegalStateException("ODMFList has not been initialized.");
    }

    private void checkValue(Object o) {
        JudgeUtils.checkInstance(o);
        checkEntity((ManagedObject) o);
    }

    private void checkEntity(ManagedObject o) {
        JudgeUtils.checkNull(o);
        if (!this.entityName.equals(o.getEntityName())) {
            throw new ODMFIllegalArgumentException(JudgeUtils.INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    private void addUpdate() {
        if (this.odmfBaseObj != null) {
            this.ctx.update(this.odmfBaseObj);
        }
    }

    public boolean clearModify() {
        if (this.odmfBaseObj == null) {
            return false;
        }
        this.insertList.clear();
        this.removeList.clear();
        return true;
    }

    private void insertAdd(E e) {
        if (this.insertList != null && !JudgeUtils.isContainedObject(this.insertList, e)) {
            this.insertList.add(e);
        }
    }

    private void insertAdd(List<E> e) {
        if (this.insertList != null) {
            int size = e.size();
            for (int i = 0; i < size; i++) {
                if (!JudgeUtils.isContainedObject(this.insertList, e.get(i))) {
                    this.insertList.add(e.get(i));
                }
            }
        }
    }

    private void insertRemove(E e) {
        if (this.removeList != null && !JudgeUtils.isContainedObject(this.removeList, e)) {
            this.removeList.add(e);
        }
    }

    public List<E> getInsertList() {
        return this.insertList;
    }

    public List<E> getRemoveList() {
        return this.removeList;
    }
}

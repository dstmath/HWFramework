package com.huawei.odmf.core;

import android.support.annotation.NonNull;
import com.huawei.odmf.exception.ODMFConcurrentModificationException;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFNullPointerException;
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

public class LazyList<E> implements List<E> {
    private int arraySize;
    private ManagedObject baseObj;
    private String entityName;
    private List<E> lazyInsertList;
    private List<E> lazyRemoveList;
    /* access modifiers changed from: private */
    public int listSize;
    /* access modifiers changed from: private */
    public transient int modCount;
    private AObjectContext objectContext;
    private List<ObjectId> objectIDList;
    private int relationshipIndex;

    private class LazyIterator implements Iterator<E> {
        int cursor;
        int expectedModCount;
        int lazyLastRet;
        protected int lazyLimit;

        private LazyIterator() {
            this.lazyLimit = LazyList.this.listSize;
            this.expectedModCount = LazyList.this.modCount;
            this.lazyLastRet = -1;
        }

        public boolean hasNext() {
            return this.cursor < this.lazyLimit;
        }

        public E next() {
            if (LazyList.this.modCount != this.expectedModCount) {
                throw new ODMFConcurrentModificationException();
            }
            int i = this.cursor;
            if (i >= this.lazyLimit) {
                throw new NoSuchElementException();
            }
            this.cursor = i + 1;
            this.lazyLastRet = i;
            return LazyList.this.get(this.lazyLastRet);
        }

        public void remove() {
            if (this.lazyLastRet < 0) {
                throw new ODMFIllegalStateException();
            } else if (LazyList.this.modCount != this.expectedModCount) {
                throw new ODMFConcurrentModificationException();
            } else {
                try {
                    LazyList.this.remove(this.lazyLastRet);
                    this.cursor = this.lazyLastRet;
                    this.lazyLastRet = -1;
                    this.expectedModCount = LazyList.this.modCount;
                    this.lazyLimit--;
                } catch (IndexOutOfBoundsException e) {
                    throw new ODMFConcurrentModificationException();
                }
            }
        }
    }

    private class LazyListIterator extends LazyList<E>.LazyIterator implements ListIterator<E> {
        LazyListIterator(int index) {
            super();
            if (index < 0 || index > this.lazyLimit) {
                throw new IndexOutOfBoundsException();
            }
            this.cursor = index;
        }

        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        public E previous() {
            if (LazyList.this.modCount != this.expectedModCount) {
                throw new ODMFConcurrentModificationException();
            }
            int i = this.cursor - 1;
            if (i < 0) {
                throw new NoSuchElementException();
            }
            this.cursor = i;
            this.lazyLastRet = i;
            return LazyList.this.get(this.lazyLastRet);
        }

        public int nextIndex() {
            return this.cursor;
        }

        public int previousIndex() {
            return this.cursor - 1;
        }

        public void set(E e) {
            if (this.lazyLastRet < 0) {
                throw new ODMFIllegalStateException();
            } else if (LazyList.this.modCount != this.expectedModCount) {
                throw new ODMFConcurrentModificationException();
            } else {
                try {
                    LazyList.this.set(this.lazyLastRet, e);
                } catch (IndexOutOfBoundsException e2) {
                    throw new ODMFConcurrentModificationException();
                }
            }
        }

        public void add(E e) {
            if (LazyList.this.modCount != this.expectedModCount) {
                throw new ODMFConcurrentModificationException();
            }
            try {
                int i = this.cursor;
                LazyList.this.add(i, e);
                this.cursor = i + 1;
                this.lazyLastRet = -1;
                this.expectedModCount = LazyList.this.modCount;
                this.lazyLimit++;
            } catch (IndexOutOfBoundsException e2) {
                throw new ODMFConcurrentModificationException();
            }
        }
    }

    public ManagedObject getBaseObj() {
        return this.baseObj;
    }

    public LazyList(List<ObjectId> objectIDList2, ObjectContext objectContext2, String entityName2) {
        this(objectIDList2, objectContext2, entityName2, null);
    }

    public LazyList(List<ObjectId> objectIDList2, ObjectContext objectContext2, String entityName2, ManagedObject baseObj2) {
        this.objectIDList = null;
        this.objectContext = null;
        this.listSize = 0;
        this.arraySize = 500;
        this.modCount = 0;
        this.baseObj = null;
        this.relationshipIndex = -1;
        if (objectContext2 == null || objectIDList2 == null || entityName2 == null) {
            throw new ODMFIllegalArgumentException("When new LazyList, at least one parameter is null");
        }
        this.objectIDList = objectIDList2;
        this.objectContext = (AObjectContext) objectContext2;
        this.entityName = entityName2;
        this.listSize = objectIDList2.size();
        if (baseObj2 != null) {
            this.baseObj = baseObj2;
            this.lazyInsertList = new ArrayList();
            this.lazyRemoveList = new ArrayList();
        }
    }

    public int getRelationshipIndex() {
        return this.relationshipIndex;
    }

    public void setRelationshipIndex(int relationshipIndex2) {
        this.relationshipIndex = relationshipIndex2;
    }

    public E get(int index) {
        if (index >= 0 && index < this.listSize) {
            return getObject(this.objectIDList.get(index));
        }
        throw new IndexOutOfBoundsException("index < 0 || index >= listSize");
    }

    public int size() {
        return this.listSize;
    }

    public boolean isEmpty() {
        return this.listSize == 0;
    }

    public boolean add(E o) {
        addObj(this.listSize, o);
        addUpdate();
        insertAdd(o);
        if (getRelationshipIndex() >= 0 && this.baseObj != null) {
            ((AManagedObject) this.baseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return true;
    }

    public void add(int index, E o) {
        if (index < 0 || index > this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize()");
        }
        addObj(index, o);
        addUpdate();
        insertAdd(o);
        if (getRelationshipIndex() >= 0 && this.baseObj != null) {
            ((AManagedObject) this.baseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
    }

    public void addObj(int index, E o) {
        if (index < 0 || index > this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize()");
        }
        checkValue(o);
        this.objectIDList.add(index, ((ManagedObject) o).getObjectId());
        this.modCount++;
        this.listSize++;
    }

    public boolean addAll(@NonNull Collection<? extends E> c) {
        JudgeUtils.checkNull(c);
        List<ObjectId> list1 = new ArrayList<>();
        List<E> list2 = new ArrayList<>();
        for (Object next : c) {
            checkValue(next);
            list1.add(((ManagedObject) next).getObjectId());
            list2.add(next);
        }
        addObjAll(this.listSize, list1);
        addUpdate();
        insertAdd(list2);
        return true;
    }

    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        JudgeUtils.checkNull(c);
        if (index < 0 || index > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index " + index + ", listSize is " + this.listSize);
        }
        List<ObjectId> list1 = new ArrayList<>();
        List<E> list2 = new ArrayList<>();
        for (Object next : c) {
            checkValue(next);
            list1.add(((ManagedObject) next).getObjectId());
            list2.add(next);
        }
        addObjAll(index, list1);
        addUpdate();
        insertAdd(list2);
        return true;
    }

    private void addObjAll(int index, Collection<ObjectId> c) {
        this.objectIDList.addAll(index, c);
        this.listSize += c.size();
        this.modCount++;
    }

    public boolean remove(Object o) {
        checkValue(o);
        if (!this.objectIDList.remove(((ManagedObject) o).getObjectId())) {
            return false;
        }
        this.listSize--;
        this.modCount++;
        addUpdate();
        insertRemove(o);
        if (getRelationshipIndex() >= 0 && this.baseObj != null) {
            ((AManagedObject) this.baseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return true;
    }

    public E remove(int index) {
        if (index < 0 || index >= this.listSize) {
            throw new IndexOutOfBoundsException();
        }
        this.listSize--;
        this.modCount++;
        addUpdate();
        E object = getObject(this.objectIDList.remove(index));
        insertRemove(object);
        if (getRelationshipIndex() >= 0 && this.baseObj != null) {
            ((AManagedObject) this.baseObj).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return object;
    }

    public boolean removeAll(@NonNull Collection<?> c) {
        JudgeUtils.checkNull(c);
        boolean flag = false;
        for (Object next : c) {
            try {
                checkValue(next);
                if (this.objectIDList.remove(((ManagedObject) next).getObjectId())) {
                    this.listSize--;
                    this.modCount++;
                    if (!flag) {
                        flag = true;
                    }
                    insertRemove(next);
                }
            } catch (ODMFIllegalArgumentException | ODMFNullPointerException e) {
            }
        }
        if (flag) {
            addUpdate();
        }
        return true;
    }

    public boolean contains(Object o) {
        JudgeUtils.checkInstance(o);
        return this.objectIDList.contains(((ManagedObject) o).getObjectId());
    }

    public boolean containsAll(@NonNull Collection<?> c) {
        JudgeUtils.checkNull(c);
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean retainAll(@NonNull Collection<?> c) {
        JudgeUtils.checkNull(c);
        List<ObjectId> list = new ArrayList<>();
        for (ObjectId id : this.objectIDList) {
            ManagedObject object = getObject(id);
            if (!c.contains(object)) {
                list.add(id);
                insertRemove(object);
            }
        }
        if (list.isEmpty()) {
            return false;
        }
        this.objectIDList.removeAll(list);
        this.modCount += list.size();
        this.listSize -= list.size();
        addUpdate();
        return true;
    }

    public void clear() {
        for (int i = 0; i < this.listSize; i++) {
            insertRemove(getObject(this.objectIDList.get(i)));
        }
        this.objectIDList.clear();
        this.modCount++;
        this.listSize = 0;
        addUpdate();
    }

    public E set(int index, E o) {
        if (index < 0 || index >= this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index >= listSize()");
        }
        checkValue(o);
        addUpdate();
        E object = getObject(this.objectIDList.set(index, ((ManagedObject) o).getObjectId()));
        insertRemove(object);
        insertAdd(o);
        return object;
    }

    @NonNull
    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index ");
        }
        List<E> list = new ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            list.add(getObject(this.objectIDList.get(i)));
        }
        return list;
    }

    public int indexOf(Object o) {
        checkValue(o);
        return this.objectIDList.indexOf(((ManagedObject) o).getObjectId());
    }

    public int lastIndexOf(Object o) {
        checkValue(o);
        return this.objectIDList.lastIndexOf(((ManagedObject) o).getObjectId());
    }

    public ListIterator<E> listIterator() {
        if (this.objectIDList != null) {
            return new LazyListIterator(0);
        }
        throw new ODMFIllegalStateException("objectIDList has not been initialized.");
    }

    @NonNull
    public ListIterator<E> listIterator(int index) {
        if (this.objectIDList != null) {
            return new LazyListIterator(index);
        }
        throw new ODMFIllegalStateException("objectIDList has not been initialized.");
    }

    @NonNull
    public Iterator<E> iterator() {
        if (this.objectIDList != null) {
            return new LazyIterator();
        }
        throw new ODMFIllegalStateException("objectIDList has not been initialized.");
    }

    @NonNull
    public Object[] toArray() {
        if (this.listSize > this.arraySize) {
            throw new ODMFUnsupportedOperationException("This operation is not supported, because returning a lots of objects will result in out of memory");
        }
        List<ManagedObject> list = new ArrayList<>();
        int size = this.objectIDList.size();
        for (int i = 0; i < size; i++) {
            list.add(getObject(this.objectIDList.get(i)));
        }
        return list.toArray();
    }

    @NonNull
    public <T> T[] toArray(T[] a) {
        if (a.length >= this.listSize) {
            for (int i = 0; i < this.listSize; i++) {
                a[i] = getObject(this.objectIDList.get(i));
            }
            return a;
        } else if (this.listSize > this.arraySize) {
            throw new ODMFUnsupportedOperationException("Sorry,this operation is not supported, because returning a lots of objects will result in out of memory");
        } else {
            T[] newArray = (Object[]) ((Object[]) Array.newInstance(a.getClass().getComponentType(), this.listSize));
            for (int i2 = 0; i2 < this.listSize; i2++) {
                newArray[i2] = getObject(this.objectIDList.get(i2));
            }
            return newArray;
        }
    }

    private ManagedObject getObject(ObjectId id) {
        if (id == null) {
            return null;
        }
        return this.objectContext.get(id);
    }

    private void checkValue(Object o) {
        JudgeUtils.checkInstance(o);
        checkEntity((ManagedObject) o);
    }

    private void checkEntity(ManagedObject element) {
        if (!this.entityName.equals(element.getEntityName())) {
            throw new ODMFIllegalArgumentException(JudgeUtils.INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    private void addUpdate() {
        if (this.baseObj != null) {
            this.objectContext.update(this.baseObj);
        }
    }

    public boolean clearModify() {
        if (this.baseObj == null) {
            return false;
        }
        this.lazyInsertList.clear();
        this.lazyRemoveList.clear();
        return true;
    }

    private void insertAdd(E e) {
        if (this.lazyInsertList != null && !JudgeUtils.isContainedObject(this.lazyInsertList, e)) {
            this.lazyInsertList.add(e);
        }
    }

    private void insertAdd(List<E> e) {
        if (this.lazyInsertList != null) {
            int size = e.size();
            for (int i = 0; i < size; i++) {
                if (!JudgeUtils.isContainedObject(this.lazyInsertList, e.get(i))) {
                    this.lazyInsertList.add(e.get(i));
                }
            }
        }
    }

    private void insertRemove(E e) {
        if (this.lazyRemoveList != null && !JudgeUtils.isContainedObject(this.lazyRemoveList, e)) {
            this.lazyRemoveList.add(e);
        }
    }

    public List<E> getInsertList() {
        return this.lazyInsertList;
    }

    public List<E> getRemoveList() {
        return this.lazyRemoveList;
    }
}

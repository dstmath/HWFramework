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
    private static final int arraySize = 500;
    private ObjectContext ctx;
    private String entityName;
    private List<E> insertList;
    private int listSize;
    private List<E> objList;
    private ManagedObject odmfBaseObj;
    private transient int odmfModCount;
    private int relationshipIndex;
    private List<E> removeList;

    public ODMFList(ObjectContext objectContext, String str) {
        this(objectContext, str, null);
    }

    public ODMFList(ObjectContext objectContext, String str, ManagedObject managedObject) {
        this.listSize = 0;
        this.odmfModCount = 0;
        this.odmfBaseObj = null;
        this.relationshipIndex = -1;
        if (objectContext == null || str == null) {
            throw new ODMFIllegalArgumentException("When new ODMFList, at least one input parameter is null");
        }
        this.ctx = objectContext;
        this.entityName = str;
        this.objList = new ArrayList();
        if (managedObject != null) {
            this.odmfBaseObj = managedObject;
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

    public void setRelationshipIndex(int i) {
        this.relationshipIndex = i;
    }

    @Override // java.util.List, java.util.Collection
    public int size() {
        return this.listSize;
    }

    @Override // java.util.List, java.util.Collection
    public boolean isEmpty() {
        return this.listSize == 0;
    }

    @Override // java.util.List, java.util.Collection
    public boolean contains(Object obj) {
        JudgeUtils.checkNull(obj);
        return this.objList.contains(obj);
    }

    @Override // java.util.List, java.util.Collection
    public boolean add(E e) {
        ManagedObject managedObject;
        addObj(this.listSize, e);
        addUpdate();
        insertAdd((ODMFList<E>) e);
        if (getRelationshipIndex() < 0 || (managedObject = this.odmfBaseObj) == null) {
            return true;
        }
        ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        return true;
    }

    @Override // java.util.List
    public void add(int i, E e) {
        ManagedObject managedObject;
        addObj(i, e);
        addUpdate();
        insertAdd((ODMFList<E>) e);
        if (getRelationshipIndex() >= 0 && (managedObject = this.odmfBaseObj) != null) {
            ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
    }

    public void addObj(int i, E e) {
        if (i < 0 || i > this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize()");
        }
        checkValue(e);
        this.objList.add(i, e);
        this.odmfModCount++;
        this.listSize++;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        JudgeUtils.checkNull(collection);
        List<E> arrayList = new ArrayList<>();
        for (Object obj : collection) {
            checkValue(obj);
            arrayList.add(obj);
        }
        addObjAll(this.listSize, arrayList);
        addUpdate();
        insertAdd((List) arrayList);
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List
    public boolean addAll(int i, @NonNull Collection<? extends E> collection) {
        JudgeUtils.checkNull(collection);
        if (i < 0 || i > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index " + i + ", listSize is " + this.listSize);
        }
        List<E> arrayList = new ArrayList<>();
        for (Object obj : collection) {
            checkValue(obj);
            arrayList.add(obj);
        }
        addObjAll(i, arrayList);
        addUpdate();
        insertAdd((List) arrayList);
        return true;
    }

    public void addObjAll(int i, Collection<? extends E> collection) {
        this.objList.addAll(i, collection);
        this.listSize += collection.size();
        this.odmfModCount++;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public boolean remove(Object obj) {
        ManagedObject managedObject;
        checkValue(obj);
        if (!this.objList.remove(obj)) {
            return false;
        }
        this.listSize--;
        this.odmfModCount++;
        addUpdate();
        insertRemove(obj);
        if (getRelationshipIndex() >= 0 && (managedObject = this.odmfBaseObj) != null) {
            ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return true;
    }

    @Override // java.util.List
    public E remove(int i) {
        ManagedObject managedObject;
        if (i < 0 || i >= this.listSize) {
            throw new IndexOutOfBoundsException();
        }
        E remove = this.objList.remove(i);
        this.listSize--;
        this.odmfModCount++;
        insertRemove(remove);
        addUpdate();
        if (getRelationshipIndex() >= 0 && (managedObject = this.odmfBaseObj) != null) {
            ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return remove;
    }

    @Override // java.util.List, java.util.Collection
    public boolean containsAll(@NonNull Collection<?> collection) {
        JudgeUtils.checkNull(collection);
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: com.huawei.odmf.core.ODMFList<E> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public boolean removeAll(@NonNull Collection<?> collection) {
        JudgeUtils.checkNull(collection);
        boolean z = false;
        for (Object obj : collection) {
            try {
                checkValue(obj);
                if (this.objList.remove(obj)) {
                    this.listSize--;
                    this.odmfModCount++;
                    if (!z) {
                        z = true;
                    }
                    insertRemove(obj);
                }
            } catch (ODMFIllegalArgumentException unused) {
            }
        }
        if (z) {
            addUpdate();
        }
        return true;
    }

    @Override // java.util.List, java.util.Collection
    public boolean retainAll(@NonNull Collection<?> collection) {
        JudgeUtils.checkNull(collection);
        ArrayList arrayList = new ArrayList();
        int size = this.objList.size();
        for (int i = 0; i < size; i++) {
            E e = this.objList.get(i);
            if (!collection.contains(e)) {
                arrayList.add(e);
                insertRemove(e);
            }
        }
        if (arrayList.isEmpty()) {
            return false;
        }
        this.objList.removeAll(arrayList);
        this.odmfModCount += arrayList.size();
        this.listSize -= arrayList.size();
        addUpdate();
        return true;
    }

    @Override // java.util.List, java.util.Collection
    public void clear() {
        for (int i = 0; i < this.listSize; i++) {
            insertRemove(this.objList.get(i));
        }
        this.objList.clear();
        this.listSize = 0;
        this.odmfModCount++;
        addUpdate();
    }

    @Override // java.util.List
    public E get(int i) {
        if (i >= 0 && i < this.listSize) {
            return this.objList.get(i);
        }
        throw new IndexOutOfBoundsException("index < 0 || index >= listSize");
    }

    @Override // java.util.List
    public E set(int i, E e) {
        if (i < 0 || i >= this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize");
        }
        checkValue(e);
        E e2 = this.objList.set(i, e);
        addUpdate();
        insertRemove(e2);
        insertAdd((ODMFList<E>) e);
        return e2;
    }

    @Override // java.util.List
    public int indexOf(Object obj) {
        JudgeUtils.checkNull(obj);
        return this.objList.indexOf(obj);
    }

    @Override // java.util.List
    public int lastIndexOf(Object obj) {
        JudgeUtils.checkNull(obj);
        return this.objList.lastIndexOf(obj);
    }

    @Override // java.util.List, java.util.Collection
    @NonNull
    public Object[] toArray() {
        if (this.listSize <= arraySize) {
            return this.objList.toArray();
        }
        throw new ODMFUnsupportedOperationException("This operation is not supported, returning a lots of objects will result in out of memory");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: T[] */
    /* JADX DEBUG: Multi-variable search result rejected for r4v5, resolved type: T[] */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    @NonNull
    public <T> T[] toArray(@NonNull T[] tArr) {
        int i = 0;
        if (tArr.length >= this.listSize) {
            while (i < this.listSize) {
                tArr[i] = this.objList.get(i);
                i++;
            }
            return tArr;
        }
        T[] tArr2 = (T[]) ((Object[]) Array.newInstance(tArr.getClass().getComponentType(), this.listSize));
        while (i < this.listSize) {
            tArr2[i] = this.objList.get(i);
            i++;
        }
        return tArr2;
    }

    @Override // java.util.List
    @NonNull
    public List<E> subList(int i, int i2) {
        if (i < 0 || i > i2 || i2 > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index ");
        }
        ArrayList arrayList = new ArrayList();
        while (i < i2) {
            arrayList.add(this.objList.get(i));
            i++;
        }
        return arrayList;
    }

    @Override // java.util.List, java.util.Collection, java.lang.Iterable
    @NonNull
    public Iterator<E> iterator() {
        if (this.objList != null) {
            return new ODMFItr();
        }
        throw new ODMFIllegalStateException("ODMFList has not been initialized.");
    }

    private class ODMFItr implements Iterator<E> {
        int cursor;
        int expectedModCount;
        int odmfLastRet;
        int odmfLimit;

        private ODMFItr() {
            this.odmfLimit = ODMFList.this.listSize;
            this.expectedModCount = ODMFList.this.odmfModCount;
            this.odmfLastRet = -1;
            this.cursor = 0;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.cursor < this.odmfLimit;
        }

        @Override // java.util.Iterator
        public E next() {
            checkConcurrentModification();
            int i = this.cursor;
            if (i < this.odmfLimit) {
                this.cursor = i + 1;
                this.odmfLastRet = i;
                return (E) ODMFList.this.get(this.odmfLastRet);
            }
            throw new NoSuchElementException();
        }

        @Override // java.util.Iterator
        public void remove() {
            if (this.odmfLastRet >= 0) {
                checkConcurrentModification();
                try {
                    ODMFList.this.remove(this.odmfLastRet);
                    this.cursor = this.odmfLastRet;
                    this.odmfLastRet = -1;
                    this.expectedModCount = ODMFList.this.odmfModCount;
                    this.odmfLimit--;
                } catch (IndexOutOfBoundsException unused) {
                    throw new ODMFConcurrentModificationException();
                }
            } else {
                throw new ODMFIllegalStateException();
            }
        }

        /* access modifiers changed from: package-private */
        public void checkConcurrentModification() {
            if (ODMFList.this.odmfModCount != this.expectedModCount) {
                throw new ODMFConcurrentModificationException();
            }
        }
    }

    @Override // java.util.List
    @NonNull
    public ListIterator<E> listIterator() {
        if (this.objList != null) {
            return new ODMFListItr(0);
        }
        throw new ODMFIllegalStateException("ODMFList has not been initialized.");
    }

    @Override // java.util.List
    @NonNull
    public ListIterator<E> listIterator(int i) {
        if (this.objList != null) {
            return new ODMFListItr(i);
        }
        throw new ODMFIllegalStateException("ODMFList has not been initialized.");
    }

    private class ODMFListItr extends ODMFList<E>.ODMFItr implements ListIterator<E> {
        ODMFListItr(int i) {
            super();
            if (i < 0 || i > this.odmfLimit) {
                throw new IndexOutOfBoundsException();
            }
            this.cursor = i;
        }

        @Override // java.util.ListIterator
        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        @Override // java.util.ListIterator
        public E previous() {
            checkConcurrentModification();
            int i = this.cursor - 1;
            if (i >= 0) {
                this.cursor = i;
                this.odmfLastRet = i;
                return (E) ODMFList.this.get(this.odmfLastRet);
            }
            throw new NoSuchElementException();
        }

        @Override // java.util.ListIterator
        public int nextIndex() {
            return this.cursor;
        }

        @Override // java.util.ListIterator
        public int previousIndex() {
            return this.cursor - 1;
        }

        @Override // java.util.ListIterator
        public void set(E e) {
            if (this.odmfLastRet >= 0) {
                checkConcurrentModification();
                try {
                    ODMFList.this.set(this.odmfLastRet, e);
                } catch (IndexOutOfBoundsException unused) {
                    throw new ODMFConcurrentModificationException();
                }
            } else {
                throw new ODMFIllegalStateException();
            }
        }

        @Override // java.util.ListIterator
        public void add(E e) {
            checkConcurrentModification();
            try {
                int i = this.cursor;
                ODMFList.this.add(i, e);
                this.cursor = i + 1;
                this.odmfLastRet = -1;
                this.expectedModCount = ODMFList.this.odmfModCount;
                this.odmfLimit++;
            } catch (IndexOutOfBoundsException unused) {
                throw new ODMFConcurrentModificationException();
            }
        }
    }

    private void checkValue(Object obj) {
        JudgeUtils.checkInstance(obj);
        checkEntity((ManagedObject) obj);
    }

    private void checkEntity(ManagedObject managedObject) {
        JudgeUtils.checkNull(managedObject);
        if (!this.entityName.equals(managedObject.getEntityName())) {
            throw new ODMFIllegalArgumentException(JudgeUtils.INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    private void addUpdate() {
        ManagedObject managedObject = this.odmfBaseObj;
        if (managedObject != null) {
            this.ctx.update(managedObject);
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
        List<E> list = this.insertList;
        if (list != null && !JudgeUtils.isContainedObject(list, e)) {
            this.insertList.add(e);
        }
    }

    private void insertAdd(List<E> list) {
        if (this.insertList != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (!JudgeUtils.isContainedObject(this.insertList, list.get(i))) {
                    this.insertList.add(list.get(i));
                }
            }
        }
    }

    private void insertRemove(E e) {
        List<E> list = this.removeList;
        if (list != null && !JudgeUtils.isContainedObject(list, e)) {
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

package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;

public class AugmentationsImpl implements Augmentations {
    private AugmentationsItemsContainer fAugmentationsContainer = new SmallContainer();

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.Augmentations
    public Object putItem(String str, Object obj) {
        Object putItem = this.fAugmentationsContainer.putItem(str, obj);
        if (putItem == null && this.fAugmentationsContainer.isFull()) {
            this.fAugmentationsContainer = this.fAugmentationsContainer.expand();
        }
        return putItem;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.Augmentations
    public Object getItem(String str) {
        return this.fAugmentationsContainer.getItem(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.Augmentations
    public Object removeItem(String str) {
        return this.fAugmentationsContainer.removeItem(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.Augmentations
    public Enumeration keys() {
        return this.fAugmentationsContainer.keys();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.Augmentations
    public void removeAllItems() {
        this.fAugmentationsContainer.clear();
    }

    public String toString() {
        return this.fAugmentationsContainer.toString();
    }

    abstract class AugmentationsItemsContainer {
        public abstract void clear();

        public abstract AugmentationsItemsContainer expand();

        public abstract Object getItem(Object obj);

        public abstract boolean isFull();

        public abstract Enumeration keys();

        public abstract Object putItem(Object obj, Object obj2);

        public abstract Object removeItem(Object obj);

        AugmentationsItemsContainer() {
        }
    }

    class SmallContainer extends AugmentationsItemsContainer {
        static final int SIZE_LIMIT = 10;
        final Object[] fAugmentations = new Object[20];
        int fNumEntries = 0;

        SmallContainer() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Enumeration keys() {
            return new SmallContainerKeyEnumeration();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Object getItem(Object obj) {
            for (int i = 0; i < this.fNumEntries * 2; i += 2) {
                if (this.fAugmentations[i].equals(obj)) {
                    return this.fAugmentations[i + 1];
                }
            }
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Object putItem(Object obj, Object obj2) {
            int i = 0;
            while (true) {
                int i2 = this.fNumEntries;
                if (i >= i2 * 2) {
                    Object[] objArr = this.fAugmentations;
                    objArr[i2 * 2] = obj;
                    objArr[(i2 * 2) + 1] = obj2;
                    this.fNumEntries = i2 + 1;
                    return null;
                } else if (this.fAugmentations[i].equals(obj)) {
                    Object[] objArr2 = this.fAugmentations;
                    int i3 = i + 1;
                    Object obj3 = objArr2[i3];
                    objArr2[i3] = obj2;
                    return obj3;
                } else {
                    i += 2;
                }
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Object removeItem(Object obj) {
            int i = 0;
            while (i < this.fNumEntries * 2) {
                if (this.fAugmentations[i].equals(obj)) {
                    Object obj2 = this.fAugmentations[i + 1];
                    while (true) {
                        int i2 = this.fNumEntries;
                        if (i < (i2 * 2) - 2) {
                            Object[] objArr = this.fAugmentations;
                            int i3 = i + 2;
                            objArr[i] = objArr[i3];
                            objArr[i + 1] = objArr[i + 3];
                            i = i3;
                        } else {
                            Object[] objArr2 = this.fAugmentations;
                            objArr2[(i2 * 2) - 2] = null;
                            objArr2[(i2 * 2) - 1] = null;
                            this.fNumEntries = i2 - 1;
                            return obj2;
                        }
                    }
                } else {
                    i += 2;
                }
            }
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public void clear() {
            for (int i = 0; i < this.fNumEntries * 2; i += 2) {
                Object[] objArr = this.fAugmentations;
                objArr[i] = null;
                objArr[i + 1] = null;
            }
            this.fNumEntries = 0;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public boolean isFull() {
            return this.fNumEntries == 10;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public AugmentationsItemsContainer expand() {
            LargeContainer largeContainer = new LargeContainer();
            for (int i = 0; i < this.fNumEntries * 2; i += 2) {
                Object[] objArr = this.fAugmentations;
                largeContainer.putItem(objArr[i], objArr[i + 1]);
            }
            return largeContainer;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("SmallContainer - fNumEntries == ");
            sb.append(this.fNumEntries);
            for (int i = 0; i < 20; i += 2) {
                sb.append("\nfAugmentations[");
                sb.append(i);
                sb.append("] == ");
                sb.append(this.fAugmentations[i]);
                sb.append("; fAugmentations[");
                int i2 = i + 1;
                sb.append(i2);
                sb.append("] == ");
                sb.append(this.fAugmentations[i2]);
            }
            return sb.toString();
        }

        class SmallContainerKeyEnumeration implements Enumeration {
            Object[] enumArray = new Object[SmallContainer.this.fNumEntries];
            int next;

            SmallContainerKeyEnumeration() {
                this.next = 0;
                for (int i = 0; i < SmallContainer.this.fNumEntries; i++) {
                    this.enumArray[i] = SmallContainer.this.fAugmentations[i * 2];
                }
            }

            @Override // java.util.Enumeration
            public boolean hasMoreElements() {
                return this.next < this.enumArray.length;
            }

            @Override // java.util.Enumeration
            public Object nextElement() {
                int i = this.next;
                Object[] objArr = this.enumArray;
                if (i < objArr.length) {
                    Object obj = objArr[i];
                    objArr[i] = null;
                    this.next = i + 1;
                    return obj;
                }
                throw new NoSuchElementException();
            }
        }
    }

    class LargeContainer extends AugmentationsItemsContainer {
        final Map<Object, Object> fAugmentations = new HashMap();

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public AugmentationsItemsContainer expand() {
            return this;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public boolean isFull() {
            return false;
        }

        LargeContainer() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Object getItem(Object obj) {
            return this.fAugmentations.get(obj);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Object putItem(Object obj, Object obj2) {
            return this.fAugmentations.put(obj, obj2);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Object removeItem(Object obj) {
            return this.fAugmentations.remove(obj);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public Enumeration keys() {
            return Collections.enumeration(this.fAugmentations.keySet());
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl.AugmentationsItemsContainer
        public void clear() {
            this.fAugmentations.clear();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("LargeContainer");
            for (Object obj : this.fAugmentations.keySet()) {
                sb.append("\nkey == ");
                sb.append(obj);
                sb.append("; value == ");
                sb.append(this.fAugmentations.get(obj));
            }
            return sb.toString();
        }
    }
}

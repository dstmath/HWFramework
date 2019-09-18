package android.icu.impl;

import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceBundleReader;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceTypeMismatchException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

class ICUResourceBundleImpl extends ICUResourceBundle {
    protected int resource;

    static class ResourceArray extends ResourceContainer {
        public int getType() {
            return 8;
        }

        /* access modifiers changed from: protected */
        public String[] handleGetStringArray() {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            int length = this.value.getSize();
            String[] strings = new String[length];
            int i = 0;
            while (i < length) {
                String s = reader.getString(this.value.getContainerResource(reader, i));
                if (s != null) {
                    strings[i] = s;
                    i++;
                } else {
                    throw new UResourceTypeMismatchException("");
                }
            }
            return strings;
        }

        public String[] getStringArray() {
            return handleGetStringArray();
        }

        /* access modifiers changed from: protected */
        public UResourceBundle handleGet(String indexStr, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            return createBundleObject(Integer.parseInt(indexStr), indexStr, aliasesVisited, requested);
        }

        /* access modifiers changed from: protected */
        public UResourceBundle handleGet(int index, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            return createBundleObject(index, Integer.toString(index), aliasesVisited, requested);
        }

        ResourceArray(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key, resource);
            this.value = this.wholeBundle.reader.getArray(resource);
        }
    }

    private static final class ResourceBinary extends ICUResourceBundleImpl {
        public int getType() {
            return 1;
        }

        public ByteBuffer getBinary() {
            return this.wholeBundle.reader.getBinary(this.resource);
        }

        public byte[] getBinary(byte[] ba) {
            return this.wholeBundle.reader.getBinary(this.resource, ba);
        }

        ResourceBinary(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key, resource);
        }
    }

    static abstract class ResourceContainer extends ICUResourceBundleImpl {
        protected ICUResourceBundleReader.Container value;

        public int getSize() {
            return this.value.getSize();
        }

        public String getString(int index) {
            int res = this.value.getContainerResource(this.wholeBundle.reader, index);
            if (res != -1) {
                String s = this.wholeBundle.reader.getString(res);
                if (s != null) {
                    return s;
                }
                return ICUResourceBundleImpl.super.getString(index);
            }
            throw new IndexOutOfBoundsException();
        }

        /* access modifiers changed from: protected */
        public int getContainerResource(int index) {
            return this.value.getContainerResource(this.wholeBundle.reader, index);
        }

        /* access modifiers changed from: protected */
        public UResourceBundle createBundleObject(int index, String resKey, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            int item = getContainerResource(index);
            if (item != -1) {
                return createBundleObject(resKey, item, aliasesVisited, requested);
            }
            throw new IndexOutOfBoundsException();
        }

        ResourceContainer(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key, resource);
        }

        ResourceContainer(ICUResourceBundle.WholeBundle wholeBundle) {
            super(wholeBundle);
        }
    }

    private static final class ResourceInt extends ICUResourceBundleImpl {
        public int getType() {
            return 7;
        }

        public int getInt() {
            return ICUResourceBundleReader.RES_GET_INT(this.resource);
        }

        public int getUInt() {
            return ICUResourceBundleReader.RES_GET_UINT(this.resource);
        }

        ResourceInt(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key, resource);
        }
    }

    private static final class ResourceIntVector extends ICUResourceBundleImpl {
        public int getType() {
            return 14;
        }

        public int[] getIntVector() {
            return this.wholeBundle.reader.getIntVector(this.resource);
        }

        ResourceIntVector(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key, resource);
        }
    }

    private static final class ResourceString extends ICUResourceBundleImpl {
        private String value;

        public int getType() {
            return 0;
        }

        public String getString() {
            if (this.value != null) {
                return this.value;
            }
            return this.wholeBundle.reader.getString(this.resource);
        }

        ResourceString(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key, resource);
            String s = this.wholeBundle.reader.getString(resource);
            if (s.length() < 12 || CacheValue.futureInstancesWillBeStrong()) {
                this.value = s;
            }
        }
    }

    static class ResourceTable extends ResourceContainer {
        public int getType() {
            return 2;
        }

        /* access modifiers changed from: protected */
        public String getKey(int index) {
            return ((ICUResourceBundleReader.Table) this.value).getKey(this.wholeBundle.reader, index);
        }

        /* access modifiers changed from: protected */
        public Set<String> handleKeySet() {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            TreeSet<String> keySet = new TreeSet<>();
            ICUResourceBundleReader.Table table = (ICUResourceBundleReader.Table) this.value;
            for (int i = 0; i < table.getSize(); i++) {
                keySet.add(table.getKey(reader, i));
            }
            return keySet;
        }

        /* access modifiers changed from: protected */
        public UResourceBundle handleGet(String resKey, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            int i = ((ICUResourceBundleReader.Table) this.value).findTableItem(this.wholeBundle.reader, resKey);
            if (i < 0) {
                return null;
            }
            return createBundleObject(resKey, getContainerResource(i), aliasesVisited, requested);
        }

        /* access modifiers changed from: protected */
        public UResourceBundle handleGet(int index, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            String itemKey = ((ICUResourceBundleReader.Table) this.value).getKey(this.wholeBundle.reader, index);
            if (itemKey != null) {
                return createBundleObject(itemKey, getContainerResource(index), aliasesVisited, requested);
            }
            throw new IndexOutOfBoundsException();
        }

        /* access modifiers changed from: protected */
        public Object handleGetObject(String key) {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            int index = ((ICUResourceBundleReader.Table) this.value).findTableItem(reader, key);
            if (index >= 0) {
                int res = this.value.getContainerResource(reader, index);
                String s = reader.getString(res);
                if (s != null) {
                    return s;
                }
                ICUResourceBundleReader.Container array = reader.getArray(res);
                if (array != null) {
                    int length = array.getSize();
                    String[] strings = new String[length];
                    int j = 0;
                    while (j != length) {
                        String s2 = reader.getString(array.getContainerResource(reader, j));
                        if (s2 != null) {
                            strings[j] = s2;
                            j++;
                        }
                    }
                    return strings;
                }
            }
            return super.handleGetObject(key);
        }

        /* access modifiers changed from: package-private */
        public String findString(String key) {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            int index = ((ICUResourceBundleReader.Table) this.value).findTableItem(reader, key);
            if (index < 0) {
                return null;
            }
            return reader.getString(this.value.getContainerResource(reader, index));
        }

        ResourceTable(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key, resource);
            this.value = this.wholeBundle.reader.getTable(resource);
        }

        ResourceTable(ICUResourceBundle.WholeBundle wholeBundle, int rootRes) {
            super(wholeBundle);
            this.value = wholeBundle.reader.getTable(rootRes);
        }
    }

    protected ICUResourceBundleImpl(ICUResourceBundleImpl container, String key, int resource2) {
        super(container, key);
        this.resource = resource2;
    }

    ICUResourceBundleImpl(ICUResourceBundle.WholeBundle wholeBundle) {
        super(wholeBundle);
        this.resource = wholeBundle.reader.getRootResource();
    }

    public int getResource() {
        return this.resource;
    }

    /* access modifiers changed from: protected */
    public final ICUResourceBundle createBundleObject(String _key, int _resource, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
        int RES_GET_TYPE = ICUResourceBundleReader.RES_GET_TYPE(_resource);
        if (RES_GET_TYPE == 14) {
            return new ResourceIntVector(this, _key, _resource);
        }
        switch (RES_GET_TYPE) {
            case 0:
            case 6:
                return new ResourceString(this, _key, _resource);
            case 1:
                return new ResourceBinary(this, _key, _resource);
            case 2:
            case 4:
            case 5:
                return new ResourceTable(this, _key, _resource);
            case 3:
                return getAliasedResource(this, null, 0, _key, _resource, aliasesVisited, requested);
            case 7:
                return new ResourceInt(this, _key, _resource);
            case 8:
            case 9:
                return new ResourceArray(this, _key, _resource);
            default:
                throw new IllegalStateException("The resource type is unknown");
        }
    }
}

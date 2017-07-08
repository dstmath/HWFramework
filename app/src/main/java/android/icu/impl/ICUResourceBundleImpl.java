package android.icu.impl;

import android.icu.impl.UResource.ArraySink;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.TableSink;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceTypeMismatchException;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

class ICUResourceBundleImpl extends ICUResourceBundle {

    static abstract class ResourceContainer extends ICUResourceBundleImpl {
        protected Container value;

        public int getSize() {
            return this.value.getSize();
        }

        public String getString(int index) {
            int res = this.value.getContainerResource(this.wholeBundle.reader, index);
            if (res == -1) {
                throw new IndexOutOfBoundsException();
            }
            String s = this.wholeBundle.reader.getString(res);
            if (s != null) {
                return s;
            }
            return super.getString(index);
        }

        protected int getContainerResource(int index) {
            return this.value.getContainerResource(this.wholeBundle.reader, index);
        }

        protected UResourceBundle createBundleObject(int index, String resKey, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            int item = getContainerResource(index);
            if (item != -1) {
                return createBundleObject(resKey, item, aliasesVisited, requested);
            }
            throw new IndexOutOfBoundsException();
        }

        ResourceContainer(ICUResourceBundleImpl container, String key) {
            super(container, key);
        }

        ResourceContainer(WholeBundle wholeBundle) {
            super(wholeBundle);
        }
    }

    static class ResourceArray extends ResourceContainer {
        public int getType() {
            return 8;
        }

        protected String[] handleGetStringArray() {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            int length = this.value.getSize();
            String[] strings = new String[length];
            for (int i = 0; i < length; i++) {
                String s = reader.getString(this.value.getContainerResource(reader, i));
                if (s == null) {
                    throw new UResourceTypeMismatchException(XmlPullParser.NO_NAMESPACE);
                }
                strings[i] = s;
            }
            return strings;
        }

        public String[] getStringArray() {
            return handleGetStringArray();
        }

        protected UResourceBundle handleGet(String indexStr, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            return createBundleObject(Integer.parseInt(indexStr), indexStr, aliasesVisited, requested);
        }

        protected UResourceBundle handleGet(int index, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            return createBundleObject(index, Integer.toString(index), aliasesVisited, requested);
        }

        void getAllItems(Key key, ReaderValue readerValue, ArraySink sink) {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            readerValue.reader = reader;
            ((Array) this.value).getAllItems(reader, key, readerValue, sink);
        }

        ResourceArray(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            this.value = this.wholeBundle.reader.getArray(resource);
        }
    }

    private static final class ResourceBinary extends ICUResourceBundleImpl {
        private int resource;

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
            super(container, key);
            this.resource = resource;
        }
    }

    private static final class ResourceInt extends ICUResourceBundleImpl {
        private int resource;

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
            super(container, key);
            this.resource = resource;
        }
    }

    private static final class ResourceIntVector extends ICUResourceBundleImpl {
        private int resource;

        public int getType() {
            return 14;
        }

        public int[] getIntVector() {
            return this.wholeBundle.reader.getIntVector(this.resource);
        }

        ResourceIntVector(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            this.resource = resource;
        }
    }

    private static final class ResourceString extends ICUResourceBundleImpl {
        private int resource;
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
            super(container, key);
            this.resource = resource;
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

        protected String getKey(int index) {
            return ((Table) this.value).getKey(this.wholeBundle.reader, index);
        }

        protected Set<String> handleKeySet() {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            TreeSet<String> keySet = new TreeSet();
            Table table = this.value;
            for (int i = 0; i < table.getSize(); i++) {
                keySet.add(table.getKey(reader, i));
            }
            return keySet;
        }

        protected UResourceBundle handleGet(String resKey, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            int i = ((Table) this.value).findTableItem(this.wholeBundle.reader, resKey);
            if (i < 0) {
                return null;
            }
            return createBundleObject(resKey, getContainerResource(i), aliasesVisited, requested);
        }

        protected UResourceBundle handleGet(int index, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            String itemKey = ((Table) this.value).getKey(this.wholeBundle.reader, index);
            if (itemKey != null) {
                return createBundleObject(itemKey, getContainerResource(index), aliasesVisited, requested);
            }
            throw new IndexOutOfBoundsException();
        }

        protected Object handleGetObject(String key) {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            int index = ((Table) this.value).findTableItem(reader, key);
            if (index >= 0) {
                int res = this.value.getContainerResource(reader, index);
                String s = reader.getString(res);
                if (s != null) {
                    return s;
                }
                Container array = reader.getArray(res);
                if (array != null) {
                    int length = array.getSize();
                    String[] strings = new String[length];
                    int j = 0;
                    while (j != length) {
                        s = reader.getString(array.getContainerResource(reader, j));
                        if (s != null) {
                            strings[j] = s;
                            j++;
                        }
                    }
                    return strings;
                }
            }
            return super.handleGetObject(key);
        }

        String findString(String key) {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            int index = ((Table) this.value).findTableItem(reader, key);
            if (index < 0) {
                return null;
            }
            return reader.getString(this.value.getContainerResource(reader, index));
        }

        void getAllItems(Key key, ReaderValue readerValue, TableSink sink) {
            ICUResourceBundleReader reader = this.wholeBundle.reader;
            readerValue.reader = reader;
            ((Table) this.value).getAllItems(reader, key, readerValue, sink);
        }

        ResourceTable(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            this.value = this.wholeBundle.reader.getTable(resource);
        }

        ResourceTable(WholeBundle wholeBundle, int rootRes) {
            super(wholeBundle);
            this.value = wholeBundle.reader.getTable(rootRes);
        }
    }

    protected ICUResourceBundleImpl(ICUResourceBundleImpl container, String key) {
        super(container, key);
    }

    ICUResourceBundleImpl(WholeBundle wholeBundle) {
        super(wholeBundle);
    }

    protected final ICUResourceBundle createBundleObject(String _key, int _resource, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
        switch (ICUResourceBundleReader.RES_GET_TYPE(_resource)) {
            case XmlPullParser.START_DOCUMENT /*0*/:
            case XmlPullParser.ENTITY_REF /*6*/:
                return new ResourceString(this, _key, _resource);
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return new ResourceBinary(this, _key, _resource);
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
            case NodeFilter.SHOW_TEXT /*4*/:
            case XmlPullParser.CDSECT /*5*/:
                return new ResourceTable(this, _key, _resource);
            case XmlPullParser.END_TAG /*3*/:
                return ICUResourceBundle.getAliasedResource(this, null, 0, _key, _resource, aliasesVisited, requested);
            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                return new ResourceInt(this, _key, _resource);
            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
            case XmlPullParser.COMMENT /*9*/:
                return new ResourceArray(this, _key, _resource);
            case Opcodes.OP_RETURN_VOID /*14*/:
                return new ResourceIntVector(this, _key, _resource);
            default:
                throw new IllegalStateException("The resource type is unknown");
        }
    }
}

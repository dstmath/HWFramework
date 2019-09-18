package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class AbstractBasicFileAttributeView implements BasicFileAttributeView, DynamicFileAttributeView {
    private static final String CREATION_TIME_NAME = "creationTime";
    private static final String FILE_KEY_NAME = "fileKey";
    private static final String IS_DIRECTORY_NAME = "isDirectory";
    private static final String IS_OTHER_NAME = "isOther";
    private static final String IS_REGULAR_FILE_NAME = "isRegularFile";
    private static final String IS_SYMBOLIC_LINK_NAME = "isSymbolicLink";
    private static final String LAST_ACCESS_TIME_NAME = "lastAccessTime";
    private static final String LAST_MODIFIED_TIME_NAME = "lastModifiedTime";
    private static final String SIZE_NAME = "size";
    static final Set<String> basicAttributeNames = Util.newSet(SIZE_NAME, CREATION_TIME_NAME, LAST_ACCESS_TIME_NAME, LAST_MODIFIED_TIME_NAME, FILE_KEY_NAME, IS_DIRECTORY_NAME, IS_REGULAR_FILE_NAME, IS_SYMBOLIC_LINK_NAME, IS_OTHER_NAME);

    static class AttributesBuilder {
        private boolean copyAll;
        private Map<String, Object> map = new HashMap();
        private Set<String> names = new HashSet();

        private AttributesBuilder(Set<String> allowed, String[] requested) {
            for (String name : requested) {
                if (name.equals("*")) {
                    this.copyAll = true;
                } else if (allowed.contains(name)) {
                    this.names.add(name);
                } else {
                    throw new IllegalArgumentException("'" + name + "' not recognized");
                }
            }
        }

        static AttributesBuilder create(Set<String> allowed, String[] requested) {
            return new AttributesBuilder(allowed, requested);
        }

        /* access modifiers changed from: package-private */
        public boolean match(String name) {
            return this.copyAll || this.names.contains(name);
        }

        /* access modifiers changed from: package-private */
        public void add(String name, Object value) {
            this.map.put(name, value);
        }

        /* access modifiers changed from: package-private */
        public Map<String, Object> unmodifiableMap() {
            return Collections.unmodifiableMap(this.map);
        }
    }

    protected AbstractBasicFileAttributeView() {
    }

    public String name() {
        return "basic";
    }

    public void setAttribute(String attribute, Object value) throws IOException {
        if (attribute.equals(LAST_MODIFIED_TIME_NAME)) {
            setTimes((FileTime) value, null, null);
        } else if (attribute.equals(LAST_ACCESS_TIME_NAME)) {
            setTimes(null, (FileTime) value, null);
        } else if (attribute.equals(CREATION_TIME_NAME)) {
            setTimes(null, null, (FileTime) value);
        } else {
            throw new IllegalArgumentException("'" + name() + ":" + attribute + "' not recognized");
        }
    }

    /* access modifiers changed from: package-private */
    public final void addRequestedBasicAttributes(BasicFileAttributes attrs, AttributesBuilder builder) {
        if (builder.match(SIZE_NAME)) {
            builder.add(SIZE_NAME, Long.valueOf(attrs.size()));
        }
        if (builder.match(CREATION_TIME_NAME)) {
            builder.add(CREATION_TIME_NAME, attrs.creationTime());
        }
        if (builder.match(LAST_ACCESS_TIME_NAME)) {
            builder.add(LAST_ACCESS_TIME_NAME, attrs.lastAccessTime());
        }
        if (builder.match(LAST_MODIFIED_TIME_NAME)) {
            builder.add(LAST_MODIFIED_TIME_NAME, attrs.lastModifiedTime());
        }
        if (builder.match(FILE_KEY_NAME)) {
            builder.add(FILE_KEY_NAME, attrs.fileKey());
        }
        if (builder.match(IS_DIRECTORY_NAME)) {
            builder.add(IS_DIRECTORY_NAME, Boolean.valueOf(attrs.isDirectory()));
        }
        if (builder.match(IS_REGULAR_FILE_NAME)) {
            builder.add(IS_REGULAR_FILE_NAME, Boolean.valueOf(attrs.isRegularFile()));
        }
        if (builder.match(IS_SYMBOLIC_LINK_NAME)) {
            builder.add(IS_SYMBOLIC_LINK_NAME, Boolean.valueOf(attrs.isSymbolicLink()));
        }
        if (builder.match(IS_OTHER_NAME)) {
            builder.add(IS_OTHER_NAME, Boolean.valueOf(attrs.isOther()));
        }
    }

    public Map<String, Object> readAttributes(String[] requested) throws IOException {
        AttributesBuilder builder = AttributesBuilder.create(basicAttributeNames, requested);
        addRequestedBasicAttributes(readAttributes(), builder);
        return builder.unmodifiableMap();
    }
}

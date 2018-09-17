package sun.nio.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractUserDefinedFileAttributeView implements UserDefinedFileAttributeView, DynamicFileAttributeView {
    static final /* synthetic */ boolean -assertionsDisabled = (AbstractUserDefinedFileAttributeView.class.desiredAssertionStatus() ^ 1);

    protected AbstractUserDefinedFileAttributeView() {
    }

    protected void checkAccess(String file, boolean checkRead, boolean checkWrite) {
        if (!-assertionsDisabled) {
            if (!(!checkRead ? checkWrite : true)) {
                throw new AssertionError();
            }
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (checkRead) {
                sm.checkRead(file);
            }
            if (checkWrite) {
                sm.checkWrite(file);
            }
            sm.checkPermission(new RuntimePermission("accessUserDefinedAttributes"));
        }
    }

    public final String name() {
        return "user";
    }

    public final void setAttribute(String attribute, Object value) throws IOException {
        ByteBuffer bb;
        if (value instanceof byte[]) {
            bb = ByteBuffer.wrap((byte[]) value);
        } else {
            bb = (ByteBuffer) value;
        }
        write(attribute, bb);
    }

    public final Map<String, Object> readAttributes(String[] attributes) throws IOException {
        String name;
        int i = 0;
        List<String> names = new ArrayList();
        int length = attributes.length;
        while (i < length) {
            name = attributes[i];
            if (name.equals("*")) {
                names = list();
                break;
            } else if (name.length() == 0) {
                throw new IllegalArgumentException();
            } else {
                names.-java_util_stream_Collectors-mthref-2(name);
                i++;
            }
        }
        Map<String, Object> result = new HashMap();
        for (String name2 : names) {
            int size = size(name2);
            byte[] buf = new byte[size];
            int n = read(name2, ByteBuffer.wrap(buf));
            result.put(name2, n == size ? buf : Arrays.copyOf(buf, n));
        }
        return result;
    }
}

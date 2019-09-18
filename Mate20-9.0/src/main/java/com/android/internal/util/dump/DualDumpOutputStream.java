package com.android.internal.util.dump;

import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.IndentingPrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class DualDumpOutputStream {
    private static final String LOG_TAG = DualDumpOutputStream.class.getSimpleName();
    private final LinkedList<DumpObject> mDumpObjects;
    private final IndentingPrintWriter mIpw;
    private final ProtoOutputStream mProtoStream;

    private static class DumpField extends Dumpable {
        private final String mValue;

        private DumpField(String name, String value) {
            super(name);
            this.mValue = value;
        }

        /* access modifiers changed from: package-private */
        public void print(IndentingPrintWriter ipw, boolean printName) {
            if (printName) {
                ipw.println(this.name + "=" + this.mValue);
                return;
            }
            ipw.println(this.mValue);
        }
    }

    private static class DumpObject extends Dumpable {
        private final LinkedHashMap<String, ArrayList<Dumpable>> mSubObjects;

        private DumpObject(String name) {
            super(name);
            this.mSubObjects = new LinkedHashMap<>();
        }

        /* access modifiers changed from: package-private */
        public void print(IndentingPrintWriter ipw, boolean printName) {
            if (printName) {
                ipw.println(this.name + "={");
            } else {
                ipw.println("{");
            }
            ipw.increaseIndent();
            for (ArrayList<Dumpable> subObject : this.mSubObjects.values()) {
                int numDumpables = subObject.size();
                if (numDumpables == 1) {
                    subObject.get(0).print(ipw, true);
                } else {
                    ipw.println(subObject.get(0).name + "=[");
                    ipw.increaseIndent();
                    for (int i = 0; i < numDumpables; i++) {
                        subObject.get(i).print(ipw, false);
                    }
                    ipw.decreaseIndent();
                    ipw.println("]");
                }
            }
            ipw.decreaseIndent();
            ipw.println("}");
        }

        public void add(String fieldName, Dumpable d) {
            ArrayList<Dumpable> l = this.mSubObjects.get(fieldName);
            if (l == null) {
                l = new ArrayList<>(1);
                this.mSubObjects.put(fieldName, l);
            }
            l.add(d);
        }
    }

    private static abstract class Dumpable {
        final String name;

        /* access modifiers changed from: package-private */
        public abstract void print(IndentingPrintWriter indentingPrintWriter, boolean z);

        private Dumpable(String name2) {
            this.name = name2;
        }
    }

    public DualDumpOutputStream(ProtoOutputStream proto) {
        this.mDumpObjects = new LinkedList<>();
        this.mProtoStream = proto;
        this.mIpw = null;
    }

    public DualDumpOutputStream(IndentingPrintWriter ipw) {
        this.mDumpObjects = new LinkedList<>();
        this.mProtoStream = null;
        this.mIpw = ipw;
        this.mDumpObjects.add(new DumpObject(null));
    }

    public void write(String fieldName, long fieldId, double val) {
        if (this.mProtoStream != null) {
            this.mProtoStream.write(fieldId, val);
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, String.valueOf(val)));
        }
    }

    public void write(String fieldName, long fieldId, boolean val) {
        if (this.mProtoStream != null) {
            this.mProtoStream.write(fieldId, val);
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, String.valueOf(val)));
        }
    }

    public void write(String fieldName, long fieldId, int val) {
        if (this.mProtoStream != null) {
            this.mProtoStream.write(fieldId, val);
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, String.valueOf(val)));
        }
    }

    public void write(String fieldName, long fieldId, float val) {
        if (this.mProtoStream != null) {
            this.mProtoStream.write(fieldId, val);
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, String.valueOf(val)));
        }
    }

    public void write(String fieldName, long fieldId, byte[] val) {
        if (this.mProtoStream != null) {
            this.mProtoStream.write(fieldId, val);
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, Arrays.toString(val)));
        }
    }

    public void write(String fieldName, long fieldId, long val) {
        if (this.mProtoStream != null) {
            this.mProtoStream.write(fieldId, val);
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, String.valueOf(val)));
        }
    }

    public void write(String fieldName, long fieldId, String val) {
        if (this.mProtoStream != null) {
            this.mProtoStream.write(fieldId, val);
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, String.valueOf(val)));
        }
    }

    public long start(String fieldName, long fieldId) {
        if (this.mProtoStream != null) {
            return this.mProtoStream.start(fieldId);
        }
        DumpObject d = new DumpObject(fieldName);
        this.mDumpObjects.getLast().add(fieldName, d);
        this.mDumpObjects.addLast(d);
        return (long) System.identityHashCode(d);
    }

    public void end(long token) {
        if (this.mProtoStream != null) {
            this.mProtoStream.end(token);
            return;
        }
        if (((long) System.identityHashCode(this.mDumpObjects.getLast())) != token) {
            String str = LOG_TAG;
            Log.w(str, "Unexpected token for ending " + this.mDumpObjects.getLast().name + " at " + Arrays.toString(Thread.currentThread().getStackTrace()));
        }
        this.mDumpObjects.removeLast();
    }

    public void flush() {
        if (this.mProtoStream != null) {
            this.mProtoStream.flush();
            return;
        }
        if (this.mDumpObjects.size() == 1) {
            this.mDumpObjects.getFirst().print(this.mIpw, false);
            this.mDumpObjects.clear();
            this.mDumpObjects.add(new DumpObject(null));
        }
        this.mIpw.flush();
    }

    public void writeNested(String fieldName, byte[] nestedState) {
        if (this.mIpw == null) {
            Log.w(LOG_TAG, "writeNested does not work for proto logging");
        } else {
            this.mDumpObjects.getLast().add(fieldName, new DumpField(fieldName, new String(nestedState, StandardCharsets.UTF_8).trim()));
        }
    }

    public boolean isProto() {
        return this.mProtoStream != null;
    }
}

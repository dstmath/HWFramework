package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ProcessBuilder {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private List<String> command;
    private File directory;
    private Map<String, String> environment;
    private boolean redirectErrorStream;
    private Redirect[] redirects;

    static class NullInputStream extends InputStream {
        static final NullInputStream INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ProcessBuilder.NullInputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ProcessBuilder.NullInputStream.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.ProcessBuilder.NullInputStream.<clinit>():void");
        }

        private NullInputStream() {
        }

        public int read() {
            return -1;
        }

        public int available() {
            return 0;
        }
    }

    static class NullOutputStream extends OutputStream {
        static final NullOutputStream INSTANCE = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ProcessBuilder.NullOutputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ProcessBuilder.NullOutputStream.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.ProcessBuilder.NullOutputStream.<clinit>():void");
        }

        private NullOutputStream() {
        }

        public void write(int b) throws IOException {
            throw new IOException("Stream closed");
        }
    }

    public static abstract class Redirect {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        public static final Redirect INHERIT = null;
        public static final Redirect PIPE = null;

        /* renamed from: java.lang.ProcessBuilder.Redirect.1 */
        static class AnonymousClass1 extends Redirect {
            AnonymousClass1() {
            }

            public Type type() {
                return Type.PIPE;
            }

            public String toString() {
                return type().toString();
            }
        }

        /* renamed from: java.lang.ProcessBuilder.Redirect.2 */
        static class AnonymousClass2 extends Redirect {
            AnonymousClass2() {
            }

            public Type type() {
                return Type.INHERIT;
            }

            public String toString() {
                return type().toString();
            }
        }

        /* renamed from: java.lang.ProcessBuilder.Redirect.3 */
        static class AnonymousClass3 extends Redirect {
            final /* synthetic */ File val$file;

            AnonymousClass3(File val$file) {
                this.val$file = val$file;
                super();
            }

            public Type type() {
                return Type.READ;
            }

            public File file() {
                return this.val$file;
            }

            public String toString() {
                return "redirect to read from file \"" + this.val$file + "\"";
            }
        }

        /* renamed from: java.lang.ProcessBuilder.Redirect.4 */
        static class AnonymousClass4 extends Redirect {
            final /* synthetic */ File val$file;

            AnonymousClass4(File val$file) {
                this.val$file = val$file;
                super();
            }

            public Type type() {
                return Type.WRITE;
            }

            public File file() {
                return this.val$file;
            }

            public String toString() {
                return "redirect to write to file \"" + this.val$file + "\"";
            }

            boolean append() {
                return false;
            }
        }

        /* renamed from: java.lang.ProcessBuilder.Redirect.5 */
        static class AnonymousClass5 extends Redirect {
            final /* synthetic */ File val$file;

            AnonymousClass5(File val$file) {
                this.val$file = val$file;
                super();
            }

            public Type type() {
                return Type.APPEND;
            }

            public File file() {
                return this.val$file;
            }

            public String toString() {
                return "redirect to append to file \"" + this.val$file + "\"";
            }

            boolean append() {
                return true;
            }
        }

        public enum Type {
            ;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ProcessBuilder.Redirect.Type.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ProcessBuilder.Redirect.Type.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: java.lang.ProcessBuilder.Redirect.Type.<clinit>():void");
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ProcessBuilder.Redirect.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ProcessBuilder.Redirect.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.ProcessBuilder.Redirect.<clinit>():void");
        }

        /* synthetic */ Redirect(Redirect redirect) {
            this();
        }

        public abstract Type type();

        public File file() {
            return null;
        }

        boolean append() {
            throw new UnsupportedOperationException();
        }

        public static Redirect from(File file) {
            if (file != null) {
                return new AnonymousClass3(file);
            }
            throw new NullPointerException();
        }

        public static Redirect to(File file) {
            if (file != null) {
                return new AnonymousClass4(file);
            }
            throw new NullPointerException();
        }

        public static Redirect appendTo(File file) {
            if (file != null) {
                return new AnonymousClass5(file);
            }
            throw new NullPointerException();
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Redirect)) {
                return false;
            }
            Redirect r = (Redirect) obj;
            if (r.type() != type()) {
                return false;
            }
            if (!-assertionsDisabled) {
                if (file() == null) {
                    z = false;
                }
                if (!z) {
                    throw new AssertionError();
                }
            }
            return file().equals(r.file());
        }

        public int hashCode() {
            File file = file();
            if (file == null) {
                return super.hashCode();
            }
            return file.hashCode();
        }

        private Redirect() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ProcessBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ProcessBuilder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ProcessBuilder.<clinit>():void");
    }

    public ProcessBuilder(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
    }

    public ProcessBuilder(String... command) {
        this.command = new ArrayList(command.length);
        for (String arg : command) {
            this.command.add(arg);
        }
    }

    public ProcessBuilder command(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
        return this;
    }

    public ProcessBuilder command(String... command) {
        this.command = new ArrayList(command.length);
        for (String arg : command) {
            this.command.add(arg);
        }
        return this;
    }

    public List<String> command() {
        return this.command;
    }

    public Map<String, String> environment() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("getenv.*"));
        }
        if (this.environment == null) {
            this.environment = ProcessEnvironment.environment();
        }
        if (!-assertionsDisabled) {
            if ((this.environment != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        return this.environment;
    }

    ProcessBuilder environment(String[] envp) {
        int i = 1;
        if (!-assertionsDisabled) {
            if ((this.environment == null ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (envp != null) {
            this.environment = ProcessEnvironment.emptyEnvironment(envp.length);
            if (!-assertionsDisabled) {
                if (this.environment == null) {
                    i = 0;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            for (String envstring : envp) {
                String envstring2;
                if (envstring2.indexOf(0) != -1) {
                    envstring2 = envstring2.replaceFirst("\u0000.*", "");
                }
                int eqlsign = envstring2.indexOf(61, 0);
                if (eqlsign != -1) {
                    this.environment.put(envstring2.substring(0, eqlsign), envstring2.substring(eqlsign + 1));
                }
            }
        }
        return this;
    }

    public File directory() {
        return this.directory;
    }

    public ProcessBuilder directory(File directory) {
        this.directory = directory;
        return this;
    }

    private Redirect[] redirects() {
        if (this.redirects == null) {
            this.redirects = new Redirect[]{Redirect.PIPE, Redirect.PIPE, Redirect.PIPE};
        }
        return this.redirects;
    }

    public ProcessBuilder redirectInput(Redirect source) {
        if (source.type() == Type.WRITE || source.type() == Type.APPEND) {
            throw new IllegalArgumentException("Redirect invalid for reading: " + source);
        }
        redirects()[0] = source;
        return this;
    }

    public ProcessBuilder redirectOutput(Redirect destination) {
        if (destination.type() == Type.READ) {
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        }
        redirects()[1] = destination;
        return this;
    }

    public ProcessBuilder redirectError(Redirect destination) {
        if (destination.type() == Type.READ) {
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        }
        redirects()[2] = destination;
        return this;
    }

    public ProcessBuilder redirectInput(File file) {
        return redirectInput(Redirect.from(file));
    }

    public ProcessBuilder redirectOutput(File file) {
        return redirectOutput(Redirect.to(file));
    }

    public ProcessBuilder redirectError(File file) {
        return redirectError(Redirect.to(file));
    }

    public Redirect redirectInput() {
        return this.redirects == null ? Redirect.PIPE : this.redirects[0];
    }

    public Redirect redirectOutput() {
        return this.redirects == null ? Redirect.PIPE : this.redirects[1];
    }

    public Redirect redirectError() {
        return this.redirects == null ? Redirect.PIPE : this.redirects[2];
    }

    public ProcessBuilder inheritIO() {
        Arrays.fill(redirects(), Redirect.INHERIT);
        return this;
    }

    public boolean redirectErrorStream() {
        return this.redirectErrorStream;
    }

    public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    public Process start() throws IOException {
        String[] cmdarray = (String[]) ((String[]) this.command.toArray(new String[this.command.size()])).clone();
        for (String arg : cmdarray) {
            if (arg == null) {
                throw new NullPointerException();
            }
        }
        String prog = cmdarray[0];
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExec(prog);
        }
        String file = this.directory == null ? null : this.directory.toString();
        try {
            return ProcessImpl.start(cmdarray, this.environment, file, this.redirects, this.redirectErrorStream);
        } catch (Throwable e) {
            String exceptionInfo = ": " + e.getMessage();
            Throwable cause = e;
            try {
                security.checkRead(prog);
            } catch (Throwable ace) {
                exceptionInfo = "";
                cause = ace;
            }
            throw new IOException("Cannot run program \"" + prog + "\"" + (file != null ? " (in directory \"" + file + "\")" : "") + exceptionInfo, cause);
        }
    }
}

package sun.net.www;

import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.StringTokenizer;
import sun.security.x509.GeneralNameInterface;

public class MimeEntry implements Cloneable {
    public static final int LAUNCH_APPLICATION = 3;
    public static final int LOAD_INTO_BROWSER = 1;
    public static final int SAVE_TO_FILE = 2;
    public static final int UNKNOWN = 0;
    static final String[] actionKeywords = null;
    private int action;
    private String command;
    private String description;
    private String[] fileExtensions;
    private String imageFileName;
    boolean starred;
    private String tempFileNameTemplate;
    private String typeName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.MimeEntry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.MimeEntry.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.MimeEntry.<clinit>():void");
    }

    public MimeEntry(String type) {
        this(type, 0, null, null, null);
    }

    MimeEntry(String type, String imageFileName, String extensionString) {
        this.typeName = type.toLowerCase();
        this.action = 0;
        this.command = null;
        this.imageFileName = imageFileName;
        setExtensions(extensionString);
        this.starred = isStarred(this.typeName);
    }

    MimeEntry(String typeName, int action, String command, String tempFileNameTemplate) {
        this.typeName = typeName.toLowerCase();
        this.action = action;
        this.command = command;
        this.imageFileName = null;
        this.fileExtensions = null;
        this.tempFileNameTemplate = tempFileNameTemplate;
    }

    MimeEntry(String typeName, int action, String command, String imageFileName, String[] fileExtensions) {
        this.typeName = typeName.toLowerCase();
        this.action = action;
        this.command = command;
        this.imageFileName = imageFileName;
        this.fileExtensions = fileExtensions;
        this.starred = isStarred(typeName);
    }

    public synchronized String getType() {
        return this.typeName;
    }

    public synchronized void setType(String type) {
        this.typeName = type.toLowerCase();
    }

    public synchronized int getAction() {
        return this.action;
    }

    public synchronized void setAction(int action, String command) {
        this.action = action;
        this.command = command;
    }

    public synchronized void setAction(int action) {
        this.action = action;
    }

    public synchronized String getLaunchString() {
        return this.command;
    }

    public synchronized void setCommand(String command) {
        this.command = command;
    }

    public synchronized String getDescription() {
        return this.description != null ? this.description : this.typeName;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public String getImageFileName() {
        return this.imageFileName;
    }

    public synchronized void setImageFileName(String filename) {
        if (new File(filename).getParent() == null) {
            this.imageFileName = System.getProperty("java.net.ftp.imagepath." + filename);
        } else {
            this.imageFileName = filename;
        }
        if (filename.lastIndexOf(46) < 0) {
            this.imageFileName += ".gif";
        }
    }

    public String getTempFileTemplate() {
        return this.tempFileNameTemplate;
    }

    public synchronized String[] getExtensions() {
        return this.fileExtensions;
    }

    public synchronized String getExtensionsAsList() {
        String extensionsAsString;
        extensionsAsString = "";
        if (this.fileExtensions != null) {
            for (int i = 0; i < this.fileExtensions.length; i += LOAD_INTO_BROWSER) {
                extensionsAsString = extensionsAsString + this.fileExtensions[i];
                if (i < this.fileExtensions.length - 1) {
                    extensionsAsString = extensionsAsString + ",";
                }
            }
        }
        return extensionsAsString;
    }

    public synchronized void setExtensions(String extensionString) {
        StringTokenizer extTokens = new StringTokenizer(extensionString, ",");
        int numExts = extTokens.countTokens();
        String[] extensionStrings = new String[numExts];
        for (int i = 0; i < numExts; i += LOAD_INTO_BROWSER) {
            extensionStrings[i] = ((String) extTokens.nextElement()).trim();
        }
        this.fileExtensions = extensionStrings;
    }

    private boolean isStarred(String typeName) {
        if (typeName == null || typeName.length() <= 0) {
            return false;
        }
        return typeName.endsWith("/*");
    }

    public Object launch(URLConnection urlc, InputStream is, MimeTable mt) throws ApplicationLaunchException {
        switch (this.action) {
            case GeneralNameInterface.NAME_MATCH /*0*/:
                return null;
            case LOAD_INTO_BROWSER /*1*/:
                try {
                    return urlc.getContent();
                } catch (Exception e) {
                    return null;
                }
            case SAVE_TO_FILE /*2*/:
                return is;
            case LAUNCH_APPLICATION /*3*/:
                String threadName = this.command;
                int fst = threadName.indexOf(32);
                if (fst > 0) {
                    threadName = threadName.substring(0, fst);
                }
                return new MimeLauncher(this, urlc, is, mt.getTempFileTemplate(), threadName);
            default:
                return null;
        }
    }

    public boolean matches(String type) {
        if (this.starred) {
            return type.startsWith(this.typeName);
        }
        return type.equals(this.typeName);
    }

    public Object clone() {
        MimeEntry theClone = new MimeEntry(this.typeName);
        theClone.action = this.action;
        theClone.command = this.command;
        theClone.description = this.description;
        theClone.imageFileName = this.imageFileName;
        theClone.tempFileNameTemplate = this.tempFileNameTemplate;
        theClone.fileExtensions = this.fileExtensions;
        return theClone;
    }

    public synchronized String toProperty() {
        StringBuffer buf;
        buf = new StringBuffer();
        String separator = "; ";
        boolean needSeparator = false;
        int action = getAction();
        if (action != 0) {
            buf.append("action=" + actionKeywords[action]);
            needSeparator = true;
        }
        String command = getLaunchString();
        if (command != null && command.length() > 0) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("application=" + command);
            needSeparator = true;
        }
        if (getImageFileName() != null) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("icon=" + getImageFileName());
            needSeparator = true;
        }
        String extensions = getExtensionsAsList();
        if (extensions.length() > 0) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("file_extensions=" + extensions);
            needSeparator = true;
        }
        String description = getDescription();
        if (!(description == null || description.equals(getType()))) {
            if (needSeparator) {
                buf.append(separator);
            }
            buf.append("description=" + description);
        }
        return buf.toString();
    }

    public String toString() {
        return "MimeEntry[contentType=" + this.typeName + ", image=" + this.imageFileName + ", action=" + this.action + ", command=" + this.command + ", extensions=" + getExtensionsAsList() + "]";
    }
}

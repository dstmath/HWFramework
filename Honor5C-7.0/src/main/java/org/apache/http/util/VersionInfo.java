package org.apache.http.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

@Deprecated
public class VersionInfo {
    public static final String PROPERTY_MODULE = "info.module";
    public static final String PROPERTY_RELEASE = "info.release";
    public static final String PROPERTY_TIMESTAMP = "info.timestamp";
    public static final String UNAVAILABLE = "UNAVAILABLE";
    public static final String VERSION_PROPERTY_FILE = "version.properties";
    private final String infoClassloader;
    private final String infoModule;
    private final String infoPackage;
    private final String infoRelease;
    private final String infoTimestamp;

    protected VersionInfo(String pckg, String module, String release, String time, String clsldr) {
        if (pckg == null) {
            throw new IllegalArgumentException("Package identifier must not be null.");
        }
        this.infoPackage = pckg;
        if (module == null) {
            module = UNAVAILABLE;
        }
        this.infoModule = module;
        if (release == null) {
            release = UNAVAILABLE;
        }
        this.infoRelease = release;
        if (time == null) {
            time = UNAVAILABLE;
        }
        this.infoTimestamp = time;
        if (clsldr == null) {
            clsldr = UNAVAILABLE;
        }
        this.infoClassloader = clsldr;
    }

    public final String getPackage() {
        return this.infoPackage;
    }

    public final String getModule() {
        return this.infoModule;
    }

    public final String getRelease() {
        return this.infoRelease;
    }

    public final String getTimestamp() {
        return this.infoTimestamp;
    }

    public final String getClassloader() {
        return this.infoClassloader;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(((((this.infoPackage.length() + 20) + this.infoModule.length()) + this.infoRelease.length()) + this.infoTimestamp.length()) + this.infoClassloader.length());
        sb.append("VersionInfo(").append(this.infoPackage).append(':').append(this.infoModule);
        if (!UNAVAILABLE.equals(this.infoRelease)) {
            sb.append(':').append(this.infoRelease);
        }
        if (!UNAVAILABLE.equals(this.infoTimestamp)) {
            sb.append(':').append(this.infoTimestamp);
        }
        sb.append(')');
        if (!UNAVAILABLE.equals(this.infoClassloader)) {
            sb.append('@').append(this.infoClassloader);
        }
        return sb.toString();
    }

    public static final VersionInfo[] loadVersionInfo(String[] pckgs, ClassLoader clsldr) {
        if (pckgs == null) {
            throw new IllegalArgumentException("Package identifier list must not be null.");
        }
        ArrayList vil = new ArrayList(pckgs.length);
        for (String loadVersionInfo : pckgs) {
            VersionInfo vi = loadVersionInfo(loadVersionInfo, clsldr);
            if (vi != null) {
                vil.add(vi);
            }
        }
        return (VersionInfo[]) vil.toArray(new VersionInfo[vil.size()]);
    }

    public static final VersionInfo loadVersionInfo(String pckg, ClassLoader clsldr) {
        if (pckg == null) {
            throw new IllegalArgumentException("Package identifier must not be null.");
        }
        if (clsldr == null) {
            clsldr = Thread.currentThread().getContextClassLoader();
        }
        Properties vip = null;
        InputStream is;
        try {
            is = clsldr.getResourceAsStream(pckg.replace('.', '/') + "/" + VERSION_PROPERTY_FILE);
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                vip = props;
                is.close();
            }
        } catch (IOException e) {
        } catch (Throwable th) {
            is.close();
        }
        if (vip != null) {
            return fromMap(pckg, vip, clsldr);
        }
        return null;
    }

    protected static final VersionInfo fromMap(String pckg, Map info, ClassLoader clsldr) {
        if (pckg == null) {
            throw new IllegalArgumentException("Package identifier must not be null.");
        }
        String str = null;
        String str2 = null;
        String str3 = null;
        if (info != null) {
            str = (String) info.get(PROPERTY_MODULE);
            if (str != null && str.length() < 1) {
                str = null;
            }
            str2 = (String) info.get(PROPERTY_RELEASE);
            if (str2 != null && (str2.length() < 1 || str2.equals("${pom.version}"))) {
                str2 = null;
            }
            str3 = (String) info.get(PROPERTY_TIMESTAMP);
            if (str3 != null && (str3.length() < 1 || str3.equals("${mvn.timestamp}"))) {
                str3 = null;
            }
        }
        String clsldrstr = null;
        if (clsldr != null) {
            clsldrstr = clsldr.toString();
        }
        return new VersionInfo(pckg, str, str2, str3, clsldrstr);
    }
}

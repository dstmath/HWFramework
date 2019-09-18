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
        if (pckg != null) {
            this.infoPackage = pckg;
            this.infoModule = module != null ? module : UNAVAILABLE;
            this.infoRelease = release != null ? release : UNAVAILABLE;
            this.infoTimestamp = time != null ? time : UNAVAILABLE;
            this.infoClassloader = clsldr != null ? clsldr : UNAVAILABLE;
            return;
        }
        throw new IllegalArgumentException("Package identifier must not be null.");
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
        StringBuffer sb = new StringBuffer(20 + this.infoPackage.length() + this.infoModule.length() + this.infoRelease.length() + this.infoTimestamp.length() + this.infoClassloader.length());
        sb.append("VersionInfo(");
        sb.append(this.infoPackage);
        sb.append(':');
        sb.append(this.infoModule);
        if (!UNAVAILABLE.equals(this.infoRelease)) {
            sb.append(':');
            sb.append(this.infoRelease);
        }
        if (!UNAVAILABLE.equals(this.infoTimestamp)) {
            sb.append(':');
            sb.append(this.infoTimestamp);
        }
        sb.append(')');
        if (!UNAVAILABLE.equals(this.infoClassloader)) {
            sb.append('@');
            sb.append(this.infoClassloader);
        }
        return sb.toString();
    }

    public static final VersionInfo[] loadVersionInfo(String[] pckgs, ClassLoader clsldr) {
        if (pckgs != null) {
            ArrayList vil = new ArrayList(pckgs.length);
            for (String loadVersionInfo : pckgs) {
                VersionInfo vi = loadVersionInfo(loadVersionInfo, clsldr);
                if (vi != null) {
                    vil.add(vi);
                }
            }
            return (VersionInfo[]) vil.toArray(new VersionInfo[vil.size()]);
        }
        throw new IllegalArgumentException("Package identifier list must not be null.");
    }

    public static final VersionInfo loadVersionInfo(String pckg, ClassLoader clsldr) {
        InputStream is;
        if (pckg != null) {
            if (clsldr == null) {
                clsldr = Thread.currentThread().getContextClassLoader();
            }
            Properties vip = null;
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
                throw th;
            }
            if (vip != null) {
                return fromMap(pckg, vip, clsldr);
            }
            return null;
        }
        throw new IllegalArgumentException("Package identifier must not be null.");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    protected static final VersionInfo fromMap(String pckg, Map info, ClassLoader clsldr) {
        if (pckg != null) {
            String module = null;
            String release = null;
            String timestamp = null;
            if (info != null) {
                module = info.get(PROPERTY_MODULE);
                if (module != null && module.length() < 1) {
                    module = null;
                }
                release = info.get(PROPERTY_RELEASE);
                if (release != null && (release.length() < 1 || release.equals("${pom.version}"))) {
                    release = null;
                }
                timestamp = info.get(PROPERTY_TIMESTAMP);
                if (timestamp != null && (timestamp.length() < 1 || timestamp.equals("${mvn.timestamp}"))) {
                    timestamp = null;
                }
            }
            String clsldrstr = null;
            if (clsldr != null) {
                clsldrstr = clsldr.toString();
            }
            VersionInfo versionInfo = new VersionInfo(pckg, module, release, timestamp, clsldrstr);
            return versionInfo;
        }
        throw new IllegalArgumentException("Package identifier must not be null.");
    }
}

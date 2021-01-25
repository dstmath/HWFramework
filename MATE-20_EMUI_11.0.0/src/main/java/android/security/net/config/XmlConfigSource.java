package android.security.net.config;

import android.app.slice.SliceProvider;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.security.net.config.NetworkSecurityConfig;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Pair;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlConfigSource implements ConfigSource {
    private static final int CONFIG_BASE = 0;
    private static final int CONFIG_DEBUG = 2;
    private static final int CONFIG_DOMAIN = 1;
    private final ApplicationInfo mApplicationInfo;
    private Context mContext;
    private final boolean mDebugBuild;
    private NetworkSecurityConfig mDefaultConfig;
    private Set<Pair<Domain, NetworkSecurityConfig>> mDomainMap;
    private boolean mInitialized;
    private final Object mLock = new Object();
    private final int mResourceId;

    public XmlConfigSource(Context context, int resourceId, ApplicationInfo info) {
        this.mContext = context;
        this.mResourceId = resourceId;
        this.mApplicationInfo = new ApplicationInfo(info);
        this.mDebugBuild = (this.mApplicationInfo.flags & 2) != 0;
    }

    @Override // android.security.net.config.ConfigSource
    public Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs() {
        ensureInitialized();
        return this.mDomainMap;
    }

    @Override // android.security.net.config.ConfigSource
    public NetworkSecurityConfig getDefaultConfig() {
        ensureInitialized();
        return this.mDefaultConfig;
    }

    private static final String getConfigString(int configType) {
        if (configType == 0) {
            return "base-config";
        }
        if (configType == 1) {
            return "domain-config";
        }
        if (configType == 2) {
            return "debug-overrides";
        }
        throw new IllegalArgumentException("Unknown config type: " + configType);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0028, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0029, code lost:
        if (r1 != null) goto L_0x002b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002b, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002e, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002f, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0052, code lost:
        throw new java.lang.RuntimeException("Failed to parse XML configuration from " + r6.mContext.getResources().getResourceEntryName(r6.mResourceId), r1);
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x002f A[ExcHandler: NotFoundException | ParserException | IOException | XmlPullParserException (r1v5 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:11:0x0020] */
    private void ensureInitialized() {
        synchronized (this.mLock) {
            if (!this.mInitialized) {
                XmlResourceParser parser = this.mContext.getResources().getXml(this.mResourceId);
                parseNetworkSecurityConfig(parser);
                this.mContext = null;
                this.mInitialized = true;
                if (parser != null) {
                    try {
                        $closeResource(null, parser);
                    } catch (Resources.NotFoundException | ParserException | IOException | XmlPullParserException e) {
                    }
                }
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private Pin parsePin(XmlResourceParser parser) throws IOException, XmlPullParserException, ParserException {
        String digestAlgorithm = parser.getAttributeValue(null, "digest");
        if (!Pin.isSupportedDigestAlgorithm(digestAlgorithm)) {
            throw new ParserException(parser, "Unsupported pin digest algorithm: " + digestAlgorithm);
        } else if (parser.next() == 4) {
            try {
                byte[] decodedDigest = Base64.decode(parser.getText().trim(), 0);
                int expectedLength = Pin.getDigestLength(digestAlgorithm);
                if (decodedDigest.length != expectedLength) {
                    throw new ParserException(parser, "digest length " + decodedDigest.length + " does not match expected length for " + digestAlgorithm + " of " + expectedLength);
                } else if (parser.next() == 3) {
                    return new Pin(digestAlgorithm, decodedDigest);
                } else {
                    throw new ParserException(parser, "pin contains additional elements");
                }
            } catch (IllegalArgumentException e) {
                throw new ParserException(parser, "Invalid pin digest", e);
            }
        } else {
            throw new ParserException(parser, "Missing pin digest");
        }
    }

    private PinSet parsePinSet(XmlResourceParser parser) throws IOException, XmlPullParserException, ParserException {
        String expirationDate = parser.getAttributeValue(null, "expiration");
        long expirationTimestampMilis = Long.MAX_VALUE;
        if (expirationDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                Date date = sdf.parse(expirationDate);
                if (date != null) {
                    expirationTimestampMilis = date.getTime();
                } else {
                    throw new ParserException(parser, "Invalid expiration date in pin-set");
                }
            } catch (ParseException e) {
                throw new ParserException(parser, "Invalid expiration date in pin-set", e);
            }
        }
        int outerDepth = parser.getDepth();
        Set<Pin> pins = new ArraySet<>();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals(SliceProvider.METHOD_PIN)) {
                pins.add(parsePin(parser));
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
        }
        return new PinSet(pins, expirationTimestampMilis);
    }

    private Domain parseDomain(XmlResourceParser parser, Set<String> seenDomains) throws IOException, XmlPullParserException, ParserException {
        boolean includeSubdomains = parser.getAttributeBooleanValue(null, "includeSubdomains", false);
        if (parser.next() == 4) {
            String domain = parser.getText().trim().toLowerCase(Locale.US);
            if (parser.next() != 3) {
                throw new ParserException(parser, "domain contains additional elements");
            } else if (seenDomains.add(domain)) {
                return new Domain(domain, includeSubdomains);
            } else {
                throw new ParserException(parser, domain + " has already been specified");
            }
        } else {
            throw new ParserException(parser, "Domain name missing");
        }
    }

    private CertificatesEntryRef parseCertificatesEntry(XmlResourceParser parser, boolean defaultOverridePins) throws IOException, XmlPullParserException, ParserException {
        CertificateSource source;
        boolean overridePins = parser.getAttributeBooleanValue(null, "overridePins", defaultOverridePins);
        int sourceId = parser.getAttributeResourceValue(null, "src", -1);
        String sourceString = parser.getAttributeValue(null, "src");
        if (sourceString != null) {
            if (sourceId != -1) {
                source = new ResourceCertificateSource(sourceId, this.mContext);
            } else if ("system".equals(sourceString)) {
                source = SystemCertificateSource.getInstance();
            } else if ("user".equals(sourceString)) {
                source = UserCertificateSource.getInstance();
            } else if ("wfa".equals(sourceString)) {
                source = WfaCertificateSource.getInstance();
            } else {
                throw new ParserException(parser, "Unknown certificates src. Should be one of system|user|@resourceVal");
            }
            XmlUtils.skipCurrentTag(parser);
            return new CertificatesEntryRef(source, overridePins);
        }
        throw new ParserException(parser, "certificates element missing src attribute");
    }

    private Collection<CertificatesEntryRef> parseTrustAnchors(XmlResourceParser parser, boolean defaultOverridePins) throws IOException, XmlPullParserException, ParserException {
        int outerDepth = parser.getDepth();
        List<CertificatesEntryRef> anchors = new ArrayList<>();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals("certificates")) {
                anchors.add(parseCertificatesEntry(parser, defaultOverridePins));
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
        }
        return anchors;
    }

    private List<Pair<NetworkSecurityConfig.Builder, Set<Domain>>> parseConfigEntry(XmlResourceParser parser, Set<String> seenDomains, NetworkSecurityConfig.Builder parentBuilder, int configType) throws IOException, XmlPullParserException, ParserException {
        XmlConfigSource xmlConfigSource = this;
        List<Pair<NetworkSecurityConfig.Builder, Set<Domain>>> builders = new ArrayList<>();
        NetworkSecurityConfig.Builder builder = new NetworkSecurityConfig.Builder();
        builder.setParent(parentBuilder);
        Set<Domain> domains = new ArraySet<>();
        boolean seenPinSet = false;
        boolean seenTrustAnchors = false;
        boolean z = false;
        boolean defaultOverridePins = configType == 2;
        parser.getName();
        int outerDepth = parser.getDepth();
        builders.add(new Pair<>(builder, domains));
        int i = 0;
        while (i < parser.getAttributeCount()) {
            String name = parser.getAttributeName(i);
            if ("hstsEnforced".equals(name)) {
                builder.setHstsEnforced(parser.getAttributeBooleanValue(i, z));
            } else if ("cleartextTrafficPermitted".equals(name)) {
                builder.setCleartextTrafficPermitted(parser.getAttributeBooleanValue(i, true));
            }
            i++;
            z = false;
        }
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            String tagName = parser.getName();
            if ("domain".equals(tagName)) {
                if (configType == 1) {
                    domains.add(parseDomain(parser, seenDomains));
                } else {
                    throw new ParserException(parser, "domain element not allowed in " + getConfigString(configType));
                }
            } else if ("trust-anchors".equals(tagName)) {
                if (!seenTrustAnchors) {
                    builder.addCertificatesEntryRefs(xmlConfigSource.parseTrustAnchors(parser, defaultOverridePins));
                    seenTrustAnchors = true;
                } else {
                    throw new ParserException(parser, "Multiple trust-anchor elements not allowed");
                }
            } else if ("pin-set".equals(tagName)) {
                if (configType != 1) {
                    throw new ParserException(parser, "pin-set element not allowed in " + getConfigString(configType));
                } else if (!seenPinSet) {
                    builder.setPinSet(parsePinSet(parser));
                    seenPinSet = true;
                } else {
                    throw new ParserException(parser, "Multiple pin-set elements not allowed");
                }
            } else if (!"domain-config".equals(tagName)) {
                XmlUtils.skipCurrentTag(parser);
            } else if (configType == 1) {
                builders.addAll(xmlConfigSource.parseConfigEntry(parser, seenDomains, builder, configType));
            } else {
                throw new ParserException(parser, "Nested domain-config not allowed in " + getConfigString(configType));
            }
            xmlConfigSource = this;
        }
        if (configType != 1 || !domains.isEmpty()) {
            return builders;
        }
        throw new ParserException(parser, "No domain elements in domain-config");
    }

    private void addDebugAnchorsIfNeeded(NetworkSecurityConfig.Builder debugConfigBuilder, NetworkSecurityConfig.Builder builder) {
        if (debugConfigBuilder != null && debugConfigBuilder.hasCertificatesEntryRefs() && builder.hasCertificatesEntryRefs()) {
            builder.addCertificatesEntryRefs(debugConfigBuilder.getCertificatesEntryRefs());
        }
    }

    private void parseNetworkSecurityConfig(XmlResourceParser parser) throws IOException, XmlPullParserException, ParserException {
        Set<String> seenDomains = new ArraySet<>();
        List<Pair<NetworkSecurityConfig.Builder, Set<Domain>>> builders = new ArrayList<>();
        NetworkSecurityConfig.Builder baseConfigBuilder = null;
        NetworkSecurityConfig.Builder debugConfigBuilder = null;
        boolean seenDebugOverrides = false;
        boolean seenBaseConfig = false;
        XmlUtils.beginDocument(parser, "network-security-config");
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if ("base-config".equals(parser.getName())) {
                if (!seenBaseConfig) {
                    seenBaseConfig = true;
                    baseConfigBuilder = parseConfigEntry(parser, seenDomains, null, 0).get(0).first;
                } else {
                    throw new ParserException(parser, "Only one base-config allowed");
                }
            } else if ("domain-config".equals(parser.getName())) {
                builders.addAll(parseConfigEntry(parser, seenDomains, baseConfigBuilder, 1));
            } else if (!"debug-overrides".equals(parser.getName())) {
                XmlUtils.skipCurrentTag(parser);
            } else if (!seenDebugOverrides) {
                if (this.mDebugBuild) {
                    debugConfigBuilder = parseConfigEntry(parser, null, null, 2).get(0).first;
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
                seenDebugOverrides = true;
            } else {
                throw new ParserException(parser, "Only one debug-overrides allowed");
            }
        }
        if (this.mDebugBuild && debugConfigBuilder == null) {
            debugConfigBuilder = parseDebugOverridesResource();
        }
        NetworkSecurityConfig.Builder platformDefaultBuilder = NetworkSecurityConfig.getDefaultBuilder(this.mApplicationInfo);
        addDebugAnchorsIfNeeded(debugConfigBuilder, platformDefaultBuilder);
        if (baseConfigBuilder != null) {
            baseConfigBuilder.setParent(platformDefaultBuilder);
            addDebugAnchorsIfNeeded(debugConfigBuilder, baseConfigBuilder);
        } else {
            baseConfigBuilder = platformDefaultBuilder;
        }
        Set<Pair<Domain, NetworkSecurityConfig>> configs = new ArraySet<>();
        for (Pair<NetworkSecurityConfig.Builder, Set<Domain>> entry : builders) {
            NetworkSecurityConfig.Builder builder = entry.first;
            Set<Domain> domains = entry.second;
            if (builder.getParent() == null) {
                builder.setParent(baseConfigBuilder);
            }
            addDebugAnchorsIfNeeded(debugConfigBuilder, builder);
            NetworkSecurityConfig config = builder.build();
            for (Domain domain : domains) {
                configs.add(new Pair<>(domain, config));
                seenDomains = seenDomains;
            }
        }
        this.mDefaultConfig = baseConfigBuilder.build();
        this.mDomainMap = configs;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007f, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0080, code lost:
        if (r6 != null) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0082, code lost:
        $closeResource(r4, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0085, code lost:
        throw r7;
     */
    private NetworkSecurityConfig.Builder parseDebugOverridesResource() throws IOException, XmlPullParserException, ParserException {
        Resources resources = this.mContext.getResources();
        int resId = resources.getIdentifier(resources.getResourceEntryName(this.mResourceId) + "_debug", "xml", resources.getResourcePackageName(this.mResourceId));
        if (resId == 0) {
            return null;
        }
        NetworkSecurityConfig.Builder debugConfigBuilder = null;
        XmlResourceParser parser = resources.getXml(resId);
        XmlUtils.beginDocument(parser, "network-security-config");
        int outerDepth = parser.getDepth();
        boolean seenDebugOverrides = false;
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (!"debug-overrides".equals(parser.getName())) {
                XmlUtils.skipCurrentTag(parser);
            } else if (!seenDebugOverrides) {
                if (this.mDebugBuild) {
                    debugConfigBuilder = parseConfigEntry(parser, null, null, 2).get(0).first;
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
                seenDebugOverrides = true;
            } else {
                throw new ParserException(parser, "Only one debug-overrides allowed");
            }
        }
        $closeResource(null, parser);
        return debugConfigBuilder;
    }

    public static class ParserException extends Exception {
        public ParserException(XmlPullParser parser, String message, Throwable cause) {
            super(message + " at: " + parser.getPositionDescription(), cause);
        }

        public ParserException(XmlPullParser parser, String message) {
            this(parser, message, null);
        }
    }
}

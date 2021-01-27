package ohos.com.sun.org.apache.xml.internal.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.BootstrapResolver;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.dmsdp.sdk.DMSDPConfig;
import sun.reflect.misc.ReflectUtil;

public class CatalogManager {
    private static String pAllowPI = "xml.catalog.allowPI";
    private static String pClassname = "xml.catalog.className";
    private static String pFiles = "xml.catalog.files";
    private static String pIgnoreMissing = "xml.catalog.ignoreMissing";
    private static String pPrefer = "xml.catalog.prefer";
    private static String pStatic = "xml.catalog.staticCatalog";
    private static String pVerbosity = "xml.catalog.verbosity";
    private static Catalog staticCatalog = null;
    private static CatalogManager staticManager = new CatalogManager();
    private BootstrapResolver bResolver = new BootstrapResolver();
    private String catalogClassName;
    private String catalogFiles;
    public Debug debug;
    private String defaultCatalogFiles;
    private boolean defaultOasisXMLCatalogPI;
    private boolean defaultPreferPublic;
    private boolean defaultRelativeCatalogs;
    private boolean defaultUseStaticCatalog;
    private int defaultVerbosity;
    private boolean fromPropertiesFile;
    private boolean ignoreMissingProperties;
    private Boolean oasisXMLCatalogPI;
    private boolean overrideDefaultParser;
    private Boolean preferPublic;
    private String propertyFile;
    private URL propertyFileURI;
    private Boolean relativeCatalogs;
    private ResourceBundle resources;
    private Boolean useStaticCatalog;
    private Integer verbosity;

    public CatalogManager() {
        this.ignoreMissingProperties = (SecuritySupport.getSystemProperty(pIgnoreMissing) == null && SecuritySupport.getSystemProperty(pFiles) == null) ? false : true;
        this.propertyFile = "CatalogManager.properties";
        this.propertyFileURI = null;
        this.defaultCatalogFiles = "./xcatalog";
        this.catalogFiles = null;
        this.fromPropertiesFile = false;
        this.defaultVerbosity = 1;
        this.verbosity = null;
        this.defaultPreferPublic = true;
        this.preferPublic = null;
        this.defaultUseStaticCatalog = true;
        this.useStaticCatalog = null;
        this.defaultOasisXMLCatalogPI = true;
        this.oasisXMLCatalogPI = null;
        this.defaultRelativeCatalogs = true;
        this.relativeCatalogs = null;
        this.catalogClassName = null;
        this.debug = null;
        init();
    }

    public CatalogManager(String str) {
        this.ignoreMissingProperties = (SecuritySupport.getSystemProperty(pIgnoreMissing) == null && SecuritySupport.getSystemProperty(pFiles) == null) ? false : true;
        this.propertyFile = "CatalogManager.properties";
        this.propertyFileURI = null;
        this.defaultCatalogFiles = "./xcatalog";
        this.catalogFiles = null;
        this.fromPropertiesFile = false;
        this.defaultVerbosity = 1;
        this.verbosity = null;
        this.defaultPreferPublic = true;
        this.preferPublic = null;
        this.defaultUseStaticCatalog = true;
        this.useStaticCatalog = null;
        this.defaultOasisXMLCatalogPI = true;
        this.oasisXMLCatalogPI = null;
        this.defaultRelativeCatalogs = true;
        this.relativeCatalogs = null;
        this.catalogClassName = null;
        this.debug = null;
        this.propertyFile = str;
        init();
    }

    private void init() {
        this.debug = new Debug();
        if (System.getSecurityManager() == null) {
            this.overrideDefaultParser = true;
        }
    }

    public void setBootstrapResolver(BootstrapResolver bootstrapResolver) {
        this.bResolver = bootstrapResolver;
    }

    public BootstrapResolver getBootstrapResolver() {
        return this.bResolver;
    }

    private synchronized void readProperties() {
        try {
            this.propertyFileURI = CatalogManager.class.getResource(PsuedoNames.PSEUDONAME_ROOT + this.propertyFile);
            InputStream resourceAsStream = CatalogManager.class.getResourceAsStream(PsuedoNames.PSEUDONAME_ROOT + this.propertyFile);
            if (resourceAsStream == null) {
                if (!this.ignoreMissingProperties) {
                    PrintStream printStream = System.err;
                    printStream.println("Cannot find " + this.propertyFile);
                    this.ignoreMissingProperties = true;
                }
                return;
            }
            this.resources = new PropertyResourceBundle(resourceAsStream);
            if (this.verbosity == null) {
                try {
                    int parseInt = Integer.parseInt(this.resources.getString("verbosity").trim());
                    this.debug.setDebug(parseInt);
                    this.verbosity = new Integer(parseInt);
                } catch (Exception unused) {
                }
            }
        } catch (MissingResourceException unused2) {
            if (!this.ignoreMissingProperties) {
                PrintStream printStream2 = System.err;
                printStream2.println("Cannot read " + this.propertyFile);
            }
        } catch (IOException unused3) {
            if (!this.ignoreMissingProperties) {
                PrintStream printStream3 = System.err;
                printStream3.println("Failure trying to read " + this.propertyFile);
            }
        }
    }

    public static CatalogManager getStaticManager() {
        return staticManager;
    }

    public boolean getIgnoreMissingProperties() {
        return this.ignoreMissingProperties;
    }

    public void setIgnoreMissingProperties(boolean z) {
        this.ignoreMissingProperties = z;
    }

    public void ignoreMissingProperties(boolean z) {
        setIgnoreMissingProperties(z);
    }

    private int queryVerbosity() {
        String num = Integer.toString(this.defaultVerbosity);
        String systemProperty = SecuritySupport.getSystemProperty(pVerbosity);
        if (systemProperty == null) {
            if (this.resources == null) {
                readProperties();
            }
            ResourceBundle resourceBundle = this.resources;
            if (resourceBundle != null) {
                try {
                    num = resourceBundle.getString("verbosity");
                } catch (MissingResourceException unused) {
                }
            }
        } else {
            num = systemProperty;
        }
        int i = this.defaultVerbosity;
        try {
            i = Integer.parseInt(num.trim());
        } catch (Exception unused2) {
            PrintStream printStream = System.err;
            printStream.println("Cannot parse verbosity: \"" + num + "\"");
        }
        if (this.verbosity == null) {
            this.debug.setDebug(i);
            this.verbosity = new Integer(i);
        }
        return i;
    }

    public int getVerbosity() {
        if (this.verbosity == null) {
            this.verbosity = new Integer(queryVerbosity());
        }
        return this.verbosity.intValue();
    }

    public void setVerbosity(int i) {
        this.verbosity = new Integer(i);
        this.debug.setDebug(i);
    }

    public int verbosity() {
        return getVerbosity();
    }

    private boolean queryRelativeCatalogs() {
        if (this.resources == null) {
            readProperties();
        }
        ResourceBundle resourceBundle = this.resources;
        if (resourceBundle == null) {
            return this.defaultRelativeCatalogs;
        }
        try {
            String string = resourceBundle.getString("relative-catalogs");
            return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("yes") || string.equalsIgnoreCase("1");
        } catch (MissingResourceException unused) {
            return this.defaultRelativeCatalogs;
        }
    }

    public boolean getRelativeCatalogs() {
        if (this.relativeCatalogs == null) {
            this.relativeCatalogs = new Boolean(queryRelativeCatalogs());
        }
        return this.relativeCatalogs.booleanValue();
    }

    public void setRelativeCatalogs(boolean z) {
        this.relativeCatalogs = new Boolean(z);
    }

    public boolean relativeCatalogs() {
        return getRelativeCatalogs();
    }

    private String queryCatalogFiles() {
        String systemProperty = SecuritySupport.getSystemProperty(pFiles);
        this.fromPropertiesFile = false;
        if (systemProperty == null) {
            if (this.resources == null) {
                readProperties();
            }
            ResourceBundle resourceBundle = this.resources;
            if (resourceBundle != null) {
                try {
                    systemProperty = resourceBundle.getString("catalogs");
                    this.fromPropertiesFile = true;
                } catch (MissingResourceException unused) {
                    PrintStream printStream = System.err;
                    printStream.println(this.propertyFile + ": catalogs not found.");
                    systemProperty = null;
                }
            }
        }
        return systemProperty == null ? this.defaultCatalogFiles : systemProperty;
    }

    public Vector getCatalogFiles() {
        if (this.catalogFiles == null) {
            this.catalogFiles = queryCatalogFiles();
        }
        StringTokenizer stringTokenizer = new StringTokenizer(this.catalogFiles, DMSDPConfig.LIST_TO_STRING_SPLIT);
        Vector vector = new Vector();
        while (stringTokenizer.hasMoreTokens()) {
            String nextToken = stringTokenizer.nextToken();
            if (this.fromPropertiesFile && !relativeCatalogs()) {
                try {
                    nextToken = new URL(this.propertyFileURI, nextToken).toString();
                } catch (MalformedURLException unused) {
                }
            }
            vector.add(nextToken);
        }
        return vector;
    }

    public void setCatalogFiles(String str) {
        this.catalogFiles = str;
        this.fromPropertiesFile = false;
    }

    public Vector catalogFiles() {
        return getCatalogFiles();
    }

    private boolean queryPreferPublic() {
        String systemProperty = SecuritySupport.getSystemProperty(pPrefer);
        if (systemProperty == null) {
            if (this.resources == null) {
                readProperties();
            }
            ResourceBundle resourceBundle = this.resources;
            if (resourceBundle == null) {
                return this.defaultPreferPublic;
            }
            try {
                systemProperty = resourceBundle.getString("prefer");
            } catch (MissingResourceException unused) {
                return this.defaultPreferPublic;
            }
        }
        if (systemProperty == null) {
            return this.defaultPreferPublic;
        }
        return systemProperty.equalsIgnoreCase("public");
    }

    public boolean getPreferPublic() {
        if (this.preferPublic == null) {
            this.preferPublic = new Boolean(queryPreferPublic());
        }
        return this.preferPublic.booleanValue();
    }

    public void setPreferPublic(boolean z) {
        this.preferPublic = new Boolean(z);
    }

    public boolean preferPublic() {
        return getPreferPublic();
    }

    private boolean queryUseStaticCatalog() {
        String systemProperty = SecuritySupport.getSystemProperty(pStatic);
        if (systemProperty == null) {
            if (this.resources == null) {
                readProperties();
            }
            ResourceBundle resourceBundle = this.resources;
            if (resourceBundle == null) {
                return this.defaultUseStaticCatalog;
            }
            try {
                systemProperty = resourceBundle.getString("static-catalog");
            } catch (MissingResourceException unused) {
                return this.defaultUseStaticCatalog;
            }
        }
        if (systemProperty == null) {
            return this.defaultUseStaticCatalog;
        }
        return systemProperty.equalsIgnoreCase("true") || systemProperty.equalsIgnoreCase("yes") || systemProperty.equalsIgnoreCase("1");
    }

    public boolean getUseStaticCatalog() {
        if (this.useStaticCatalog == null) {
            this.useStaticCatalog = new Boolean(queryUseStaticCatalog());
        }
        return this.useStaticCatalog.booleanValue();
    }

    public void setUseStaticCatalog(boolean z) {
        this.useStaticCatalog = new Boolean(z);
    }

    public boolean staticCatalog() {
        return getUseStaticCatalog();
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x008d  */
    public Catalog getPrivateCatalog() {
        Catalog catalog;
        Catalog catalog2 = staticCatalog;
        if (this.useStaticCatalog == null) {
            this.useStaticCatalog = new Boolean(getUseStaticCatalog());
        }
        if (catalog2 == null || !this.useStaticCatalog.booleanValue()) {
            try {
                String catalogClassName2 = getCatalogClassName();
                if (catalogClassName2 == null) {
                    catalog = new Catalog();
                } else {
                    try {
                        catalog2 = (Catalog) ReflectUtil.forName(catalogClassName2).newInstance();
                    } catch (ClassNotFoundException unused) {
                        Debug debug2 = this.debug;
                        debug2.message(1, "Catalog class named '" + catalogClassName2 + "' could not be found. Using default.");
                        catalog = new Catalog();
                    } catch (ClassCastException unused2) {
                        Debug debug3 = this.debug;
                        debug3.message(1, "Class named '" + catalogClassName2 + "' is not a Catalog. Using default.");
                        catalog = new Catalog();
                    }
                    catalog2.setCatalogManager(this);
                    catalog2.setupReaders();
                    catalog2.loadSystemCatalogs();
                    if (this.useStaticCatalog.booleanValue()) {
                        staticCatalog = catalog2;
                    }
                }
                catalog2 = catalog;
                catalog2.setCatalogManager(this);
                catalog2.setupReaders();
                catalog2.loadSystemCatalogs();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.useStaticCatalog.booleanValue()) {
            }
        }
        return catalog2;
    }

    public Catalog getCatalog() {
        Catalog catalog = staticCatalog;
        if (this.useStaticCatalog == null) {
            this.useStaticCatalog = new Boolean(getUseStaticCatalog());
        }
        if (catalog == null || !this.useStaticCatalog.booleanValue()) {
            catalog = getPrivateCatalog();
            if (this.useStaticCatalog.booleanValue()) {
                staticCatalog = catalog;
            }
        }
        return catalog;
    }

    public boolean queryAllowOasisXMLCatalogPI() {
        String systemProperty = SecuritySupport.getSystemProperty(pAllowPI);
        if (systemProperty == null) {
            if (this.resources == null) {
                readProperties();
            }
            ResourceBundle resourceBundle = this.resources;
            if (resourceBundle == null) {
                return this.defaultOasisXMLCatalogPI;
            }
            try {
                systemProperty = resourceBundle.getString("allow-oasis-xml-catalog-pi");
            } catch (MissingResourceException unused) {
                return this.defaultOasisXMLCatalogPI;
            }
        }
        if (systemProperty == null) {
            return this.defaultOasisXMLCatalogPI;
        }
        return systemProperty.equalsIgnoreCase("true") || systemProperty.equalsIgnoreCase("yes") || systemProperty.equalsIgnoreCase("1");
    }

    public boolean getAllowOasisXMLCatalogPI() {
        if (this.oasisXMLCatalogPI == null) {
            this.oasisXMLCatalogPI = new Boolean(queryAllowOasisXMLCatalogPI());
        }
        return this.oasisXMLCatalogPI.booleanValue();
    }

    public boolean overrideDefaultParser() {
        return this.overrideDefaultParser;
    }

    public void setAllowOasisXMLCatalogPI(boolean z) {
        this.oasisXMLCatalogPI = new Boolean(z);
    }

    public boolean allowOasisXMLCatalogPI() {
        return getAllowOasisXMLCatalogPI();
    }

    public String queryCatalogClassName() {
        String systemProperty = SecuritySupport.getSystemProperty(pClassname);
        if (systemProperty == null) {
            if (this.resources == null) {
                readProperties();
            }
            ResourceBundle resourceBundle = this.resources;
            systemProperty = null;
            if (resourceBundle == null) {
                return null;
            }
            try {
                return resourceBundle.getString("catalog-class-name");
            } catch (MissingResourceException unused) {
            }
        }
        return systemProperty;
    }

    public String getCatalogClassName() {
        if (this.catalogClassName == null) {
            this.catalogClassName = queryCatalogClassName();
        }
        return this.catalogClassName;
    }

    public void setCatalogClassName(String str) {
        this.catalogClassName = str;
    }

    public String catalogClassName() {
        return getCatalogClassName();
    }
}

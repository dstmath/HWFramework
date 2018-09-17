package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Base64;
import android.util.LongSparseArray;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.Map.Entry;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class KeySetManagerService {
    public static final int CURRENT_VERSION = 1;
    public static final int FIRST_VERSION = 1;
    public static final long KEYSET_NOT_FOUND = -1;
    protected static final long PUBLIC_KEY_NOT_FOUND = -1;
    static final String TAG = "KeySetManagerService";
    private long lastIssuedKeyId = 0;
    private long lastIssuedKeySetId = 0;
    protected final LongSparseArray<ArraySet<Long>> mKeySetMapping = new LongSparseArray();
    private final LongSparseArray<KeySetHandle> mKeySets = new LongSparseArray();
    private final ArrayMap<String, PackageSetting> mPackages;
    private final LongSparseArray<PublicKeyHandle> mPublicKeys = new LongSparseArray();

    class PublicKeyHandle {
        private final long mId;
        private final PublicKey mKey;
        private int mRefCount;

        /* synthetic */ PublicKeyHandle(KeySetManagerService this$0, long id, int refCount, PublicKey key, PublicKeyHandle -this4) {
            this(id, refCount, key);
        }

        public PublicKeyHandle(long id, PublicKey key) {
            this.mId = id;
            this.mRefCount = 1;
            this.mKey = key;
        }

        private PublicKeyHandle(long id, int refCount, PublicKey key) {
            this.mId = id;
            this.mRefCount = refCount;
            this.mKey = key;
        }

        public long getId() {
            return this.mId;
        }

        public PublicKey getKey() {
            return this.mKey;
        }

        public int getRefCountLPr() {
            return this.mRefCount;
        }

        public void incrRefCountLPw() {
            this.mRefCount++;
        }

        public long decrRefCountLPw() {
            this.mRefCount--;
            return (long) this.mRefCount;
        }
    }

    public KeySetManagerService(ArrayMap<String, PackageSetting> packages) {
        this.mPackages = packages;
    }

    public boolean packageIsSignedByLPr(String packageName, KeySetHandle ks) {
        PackageSetting pkg = (PackageSetting) this.mPackages.get(packageName);
        if (pkg == null) {
            throw new NullPointerException("Invalid package name");
        } else if (pkg.keySetData == null) {
            throw new NullPointerException("Package has no KeySet data");
        } else {
            long id = getIdByKeySetLPr(ks);
            if (id == -1) {
                return false;
            }
            return ((ArraySet) this.mKeySetMapping.get(pkg.keySetData.getProperSigningKeySet())).containsAll((ArraySet) this.mKeySetMapping.get(id));
        }
    }

    public boolean packageIsSignedByExactlyLPr(String packageName, KeySetHandle ks) {
        PackageSetting pkg = (PackageSetting) this.mPackages.get(packageName);
        if (pkg == null) {
            throw new NullPointerException("Invalid package name");
        } else if (pkg.keySetData == null || pkg.keySetData.getProperSigningKeySet() == -1) {
            throw new NullPointerException("Package has no KeySet data");
        } else {
            long id = getIdByKeySetLPr(ks);
            if (id == -1) {
                return false;
            }
            return ((ArraySet) this.mKeySetMapping.get(pkg.keySetData.getProperSigningKeySet())).equals((ArraySet) this.mKeySetMapping.get(id));
        }
    }

    public void assertScannedPackageValid(Package pkg) throws PackageManagerException {
        if (pkg == null || pkg.packageName == null) {
            throw new PackageManagerException(-2, "Passed invalid package to keyset validation.");
        }
        ArraySet<PublicKey> signingKeys = pkg.mSigningKeys;
        if (signingKeys == null || signingKeys.size() <= 0 || signingKeys.contains(null)) {
            throw new PackageManagerException(-2, "Package has invalid signing-key-set.");
        }
        ArrayMap<String, ArraySet<PublicKey>> definedMapping = pkg.mKeySetMapping;
        if (definedMapping != null) {
            if (definedMapping.containsKey(null) || definedMapping.containsValue(null)) {
                throw new PackageManagerException(-2, "Package has null defined key set.");
            }
            int defMapSize = definedMapping.size();
            int i = 0;
            while (i < defMapSize) {
                if (((ArraySet) definedMapping.valueAt(i)).size() <= 0 || ((ArraySet) definedMapping.valueAt(i)).contains(null)) {
                    throw new PackageManagerException(-2, "Package has null/no public keys for defined key-sets.");
                }
                i++;
            }
        }
        ArraySet<String> upgradeAliases = pkg.mUpgradeKeySets;
        if (upgradeAliases == null) {
            return;
        }
        if (definedMapping == null || (definedMapping.keySet().containsAll(upgradeAliases) ^ 1) != 0) {
            throw new PackageManagerException(-2, "Package has upgrade-key-sets without corresponding definitions.");
        }
    }

    public void addScannedPackageLPw(Package pkg) {
        Preconditions.checkNotNull(pkg, "Attempted to add null pkg to ksms.");
        Preconditions.checkNotNull(pkg.packageName, "Attempted to add null pkg to ksms.");
        PackageSetting ps = (PackageSetting) this.mPackages.get(pkg.packageName);
        Preconditions.checkNotNull(ps, "pkg: " + pkg.packageName + "does not have a corresponding entry in mPackages.");
        addSigningKeySetToPackageLPw(ps, pkg.mSigningKeys);
        if (pkg.mKeySetMapping != null) {
            addDefinedKeySetsToPackageLPw(ps, pkg.mKeySetMapping);
            if (pkg.mUpgradeKeySets != null) {
                addUpgradeKeySetsToPackageLPw(ps, pkg.mUpgradeKeySets);
            }
        }
    }

    void addSigningKeySetToPackageLPw(PackageSetting pkg, ArraySet<PublicKey> signingKeys) {
        long signingKeySetId = pkg.keySetData.getProperSigningKeySet();
        if (signingKeySetId != -1) {
            ArraySet<PublicKey> existingKeys = getPublicKeysFromKeySetLPr(signingKeySetId);
            if (existingKeys == null || !existingKeys.equals(signingKeys)) {
                decrementKeySetLPw(signingKeySetId);
            } else {
                return;
            }
        }
        pkg.keySetData.setProperSigningKeySet(addKeySetLPw(signingKeys).getId());
    }

    private long getIdByKeySetLPr(KeySetHandle ks) {
        for (int keySetIndex = 0; keySetIndex < this.mKeySets.size(); keySetIndex++) {
            if (ks.equals((KeySetHandle) this.mKeySets.valueAt(keySetIndex))) {
                return this.mKeySets.keyAt(keySetIndex);
            }
        }
        return -1;
    }

    void addDefinedKeySetsToPackageLPw(PackageSetting pkg, ArrayMap<String, ArraySet<PublicKey>> definedMapping) {
        int i;
        ArrayMap<String, Long> prevDefinedKeySets = pkg.keySetData.getAliases();
        ArrayMap<String, Long> newKeySetAliases = new ArrayMap();
        int defMapSize = definedMapping.size();
        for (i = 0; i < defMapSize; i++) {
            String alias = (String) definedMapping.keyAt(i);
            ArraySet<PublicKey> pubKeys = (ArraySet) definedMapping.valueAt(i);
            if (!(alias == null || pubKeys == null || pubKeys.size() <= 0)) {
                newKeySetAliases.put(alias, Long.valueOf(addKeySetLPw(pubKeys).getId()));
            }
        }
        int prevDefSize = prevDefinedKeySets.size();
        for (i = 0; i < prevDefSize; i++) {
            decrementKeySetLPw(((Long) prevDefinedKeySets.valueAt(i)).longValue());
        }
        pkg.keySetData.removeAllUpgradeKeySets();
        pkg.keySetData.setAliases(newKeySetAliases);
    }

    void addUpgradeKeySetsToPackageLPw(PackageSetting pkg, ArraySet<String> upgradeAliases) {
        int uaSize = upgradeAliases.size();
        for (int i = 0; i < uaSize; i++) {
            pkg.keySetData.addUpgradeKeySet((String) upgradeAliases.valueAt(i));
        }
    }

    public KeySetHandle getKeySetByAliasAndPackageNameLPr(String packageName, String alias) {
        PackageSetting p = (PackageSetting) this.mPackages.get(packageName);
        if (p == null || p.keySetData == null) {
            return null;
        }
        Long keySetId = (Long) p.keySetData.getAliases().get(alias);
        if (keySetId != null) {
            return (KeySetHandle) this.mKeySets.get(keySetId.longValue());
        }
        throw new IllegalArgumentException("Unknown KeySet alias: " + alias);
    }

    public boolean isIdValidKeySetId(long id) {
        return this.mKeySets.get(id) != null;
    }

    public ArraySet<PublicKey> getPublicKeysFromKeySetLPr(long id) {
        ArraySet<Long> pkIds = (ArraySet) this.mKeySetMapping.get(id);
        if (pkIds == null) {
            return null;
        }
        ArraySet<PublicKey> mPubKeys = new ArraySet();
        int pkSize = pkIds.size();
        for (int i = 0; i < pkSize; i++) {
            mPubKeys.add(((PublicKeyHandle) this.mPublicKeys.get(((Long) pkIds.valueAt(i)).longValue())).getKey());
        }
        return mPubKeys;
    }

    public KeySetHandle getSigningKeySetByPackageNameLPr(String packageName) {
        PackageSetting p = (PackageSetting) this.mPackages.get(packageName);
        if (p == null || p.keySetData == null || p.keySetData.getProperSigningKeySet() == -1) {
            return null;
        }
        return (KeySetHandle) this.mKeySets.get(p.keySetData.getProperSigningKeySet());
    }

    private KeySetHandle addKeySetLPw(ArraySet<PublicKey> keys) {
        if (keys == null || keys.size() == 0) {
            throw new IllegalArgumentException("Cannot add an empty set of keys!");
        }
        int i;
        ArraySet<Long> addedKeyIds = new ArraySet(keys.size());
        int kSize = keys.size();
        for (i = 0; i < kSize; i++) {
            addedKeyIds.add(Long.valueOf(addPublicKeyLPw((PublicKey) keys.valueAt(i))));
        }
        long existingKeySetId = getIdFromKeyIdsLPr(addedKeyIds);
        KeySetHandle ks;
        if (existingKeySetId != -1) {
            for (i = 0; i < kSize; i++) {
                decrementPublicKeyLPw(((Long) addedKeyIds.valueAt(i)).longValue());
            }
            ks = (KeySetHandle) this.mKeySets.get(existingKeySetId);
            ks.incrRefCountLPw();
            return ks;
        }
        long id = getFreeKeySetIDLPw();
        ks = new KeySetHandle(id);
        this.mKeySets.put(id, ks);
        this.mKeySetMapping.put(id, addedKeyIds);
        return ks;
    }

    private void decrementKeySetLPw(long id) {
        KeySetHandle ks = (KeySetHandle) this.mKeySets.get(id);
        if (ks != null && ks.decrRefCountLPw() <= 0) {
            ArraySet<Long> pubKeys = (ArraySet) this.mKeySetMapping.get(id);
            int pkSize = pubKeys.size();
            for (int i = 0; i < pkSize; i++) {
                decrementPublicKeyLPw(((Long) pubKeys.valueAt(i)).longValue());
            }
            this.mKeySets.delete(id);
            this.mKeySetMapping.delete(id);
        }
    }

    private void decrementPublicKeyLPw(long id) {
        PublicKeyHandle pk = (PublicKeyHandle) this.mPublicKeys.get(id);
        if (pk != null && pk.decrRefCountLPw() <= 0) {
            this.mPublicKeys.delete(id);
        }
    }

    private long addPublicKeyLPw(PublicKey key) {
        Preconditions.checkNotNull(key, "Cannot add null public key!");
        long id = getIdForPublicKeyLPr(key);
        if (id != -1) {
            ((PublicKeyHandle) this.mPublicKeys.get(id)).incrRefCountLPw();
            return id;
        }
        id = getFreePublicKeyIdLPw();
        this.mPublicKeys.put(id, new PublicKeyHandle(id, key));
        return id;
    }

    private long getIdFromKeyIdsLPr(Set<Long> publicKeyIds) {
        for (int keyMapIndex = 0; keyMapIndex < this.mKeySetMapping.size(); keyMapIndex++) {
            if (((ArraySet) this.mKeySetMapping.valueAt(keyMapIndex)).equals(publicKeyIds)) {
                return this.mKeySetMapping.keyAt(keyMapIndex);
            }
        }
        return -1;
    }

    private long getIdForPublicKeyLPr(PublicKey k) {
        String encodedPublicKey = new String(k.getEncoded());
        for (int publicKeyIndex = 0; publicKeyIndex < this.mPublicKeys.size(); publicKeyIndex++) {
            if (encodedPublicKey.equals(new String(((PublicKeyHandle) this.mPublicKeys.valueAt(publicKeyIndex)).getKey().getEncoded()))) {
                return this.mPublicKeys.keyAt(publicKeyIndex);
            }
        }
        return -1;
    }

    private long getFreeKeySetIDLPw() {
        this.lastIssuedKeySetId++;
        return this.lastIssuedKeySetId;
    }

    private long getFreePublicKeyIdLPw() {
        this.lastIssuedKeyId++;
        return this.lastIssuedKeyId;
    }

    public void removeAppKeySetDataLPw(String packageName) {
        PackageSetting pkg = (PackageSetting) this.mPackages.get(packageName);
        Preconditions.checkNotNull(pkg, "pkg name: " + packageName + "does not have a corresponding entry in mPackages.");
        decrementKeySetLPw(pkg.keySetData.getProperSigningKeySet());
        ArrayMap<String, Long> definedKeySets = pkg.keySetData.getAliases();
        for (int i = 0; i < definedKeySets.size(); i++) {
            decrementKeySetLPw(((Long) definedKeySets.valueAt(i)).longValue());
        }
        clearPackageKeySetDataLPw(pkg);
    }

    private void clearPackageKeySetDataLPw(PackageSetting pkg) {
        pkg.keySetData.setProperSigningKeySet(-1);
        pkg.keySetData.removeAllDefinedKeySets();
        pkg.keySetData.removeAllUpgradeKeySets();
    }

    public String encodePublicKey(PublicKey k) throws IOException {
        return new String(Base64.encode(k.getEncoded(), 2));
    }

    public void dumpLPr(PrintWriter pw, String packageName, DumpState dumpState) {
        boolean printedHeader = false;
        for (Entry<String, PackageSetting> e : this.mPackages.entrySet()) {
            String keySetPackage = (String) e.getKey();
            if (packageName == null || (packageName.equals(keySetPackage) ^ 1) == 0) {
                if (!printedHeader) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Key Set Manager:");
                    printedHeader = true;
                }
                PackageSetting pkg = (PackageSetting) e.getValue();
                pw.print("  [");
                pw.print(keySetPackage);
                pw.println("]");
                if (pkg.keySetData != null) {
                    boolean printedLabel = false;
                    for (Entry<String, Long> entry : pkg.keySetData.getAliases().entrySet()) {
                        if (printedLabel) {
                            pw.print(", ");
                        } else {
                            pw.print("      KeySets Aliases: ");
                            printedLabel = true;
                        }
                        pw.print((String) entry.getKey());
                        pw.print('=');
                        pw.print(Long.toString(((Long) entry.getValue()).longValue()));
                    }
                    if (printedLabel) {
                        pw.println("");
                    }
                    printedLabel = false;
                    if (pkg.keySetData.isUsingDefinedKeySets()) {
                        ArrayMap<String, Long> definedKeySets = pkg.keySetData.getAliases();
                        int dksSize = definedKeySets.size();
                        for (int i = 0; i < dksSize; i++) {
                            if (printedLabel) {
                                pw.print(", ");
                            } else {
                                pw.print("      Defined KeySets: ");
                                printedLabel = true;
                            }
                            pw.print(Long.toString(((Long) definedKeySets.valueAt(i)).longValue()));
                        }
                    }
                    if (printedLabel) {
                        pw.println("");
                    }
                    printedLabel = false;
                    long signingKeySet = pkg.keySetData.getProperSigningKeySet();
                    pw.print("      Signing KeySets: ");
                    pw.print(Long.toString(signingKeySet));
                    pw.println("");
                    if (pkg.keySetData.isUsingUpgradeKeySets()) {
                        for (long keySetId : pkg.keySetData.getUpgradeKeySets()) {
                            if (printedLabel) {
                                pw.print(", ");
                            } else {
                                pw.print("      Upgrade KeySets: ");
                                printedLabel = true;
                            }
                            pw.print(Long.toString(keySetId));
                        }
                    }
                    if (printedLabel) {
                        pw.println("");
                    }
                }
            }
        }
    }

    void writeKeySetManagerServiceLPr(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "keyset-settings");
        serializer.attribute(null, "version", Integer.toString(1));
        writePublicKeysLPr(serializer);
        writeKeySetsLPr(serializer);
        serializer.startTag(null, "lastIssuedKeyId");
        serializer.attribute(null, "value", Long.toString(this.lastIssuedKeyId));
        serializer.endTag(null, "lastIssuedKeyId");
        serializer.startTag(null, "lastIssuedKeySetId");
        serializer.attribute(null, "value", Long.toString(this.lastIssuedKeySetId));
        serializer.endTag(null, "lastIssuedKeySetId");
        serializer.endTag(null, "keyset-settings");
    }

    void writePublicKeysLPr(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "keys");
        for (int pKeyIndex = 0; pKeyIndex < this.mPublicKeys.size(); pKeyIndex++) {
            long id = this.mPublicKeys.keyAt(pKeyIndex);
            String encodedKey = encodePublicKey(((PublicKeyHandle) this.mPublicKeys.valueAt(pKeyIndex)).getKey());
            serializer.startTag(null, "public-key");
            serializer.attribute(null, "identifier", Long.toString(id));
            serializer.attribute(null, "value", encodedKey);
            serializer.endTag(null, "public-key");
        }
        serializer.endTag(null, "keys");
    }

    void writeKeySetsLPr(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "keysets");
        for (int keySetIndex = 0; keySetIndex < this.mKeySetMapping.size(); keySetIndex++) {
            long id = this.mKeySetMapping.keyAt(keySetIndex);
            ArraySet<Long> keys = (ArraySet) this.mKeySetMapping.valueAt(keySetIndex);
            serializer.startTag(null, "keyset");
            serializer.attribute(null, "identifier", Long.toString(id));
            for (Long longValue : keys) {
                long keyId = longValue.longValue();
                serializer.startTag(null, "key-id");
                serializer.attribute(null, "identifier", Long.toString(keyId));
                serializer.endTag(null, "key-id");
            }
            serializer.endTag(null, "keyset");
        }
        serializer.endTag(null, "keysets");
    }

    void readKeySetsLPw(XmlPullParser parser, ArrayMap<Long, Integer> keySetRefCounts) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        String recordedVersionStr = parser.getAttributeValue(null, "version");
        int type;
        if (recordedVersionStr == null) {
            while (true) {
                type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                }
            }
            for (PackageSetting p : this.mPackages.values()) {
                clearPackageKeySetDataLPw(p);
            }
            return;
        }
        int recordedVersion = Integer.parseInt(recordedVersionStr);
        while (true) {
            type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                addRefCountsFromSavedPackagesLPw(keySetRefCounts);
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals("keys")) {
                    readKeysLPw(parser);
                } else if (tagName.equals("keysets")) {
                    readKeySetListLPw(parser);
                } else if (tagName.equals("lastIssuedKeyId")) {
                    this.lastIssuedKeyId = Long.parseLong(parser.getAttributeValue(null, "value"));
                } else if (tagName.equals("lastIssuedKeySetId")) {
                    this.lastIssuedKeySetId = Long.parseLong(parser.getAttributeValue(null, "value"));
                }
            }
        }
        addRefCountsFromSavedPackagesLPw(keySetRefCounts);
    }

    void readKeysLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals("public-key"))) {
                readPublicKeyLPw(parser);
            }
        }
    }

    void readKeySetListLPw(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        long currentKeySetId = 0;
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals("keyset")) {
                    currentKeySetId = Long.parseLong(parser.getAttributeValue(null, "identifier"));
                    this.mKeySets.put(currentKeySetId, new KeySetHandle(currentKeySetId, 0));
                    this.mKeySetMapping.put(currentKeySetId, new ArraySet());
                } else if (tagName.equals("key-id")) {
                    ((ArraySet) this.mKeySetMapping.get(currentKeySetId)).add(Long.valueOf(Long.parseLong(parser.getAttributeValue(null, "identifier"))));
                }
            }
        }
    }

    void readPublicKeyLPw(XmlPullParser parser) throws XmlPullParserException {
        long identifier = Long.parseLong(parser.getAttributeValue(null, "identifier"));
        PublicKey pub = PackageParser.parsePublicKey(parser.getAttributeValue(null, "value"));
        if (pub != null) {
            this.mPublicKeys.put(identifier, new PublicKeyHandle(this, identifier, 0, pub, null));
        }
    }

    private void addRefCountsFromSavedPackagesLPw(ArrayMap<Long, Integer> keySetRefCounts) {
        int i;
        int numRefCounts = keySetRefCounts.size();
        for (i = 0; i < numRefCounts; i++) {
            KeySetHandle ks = (KeySetHandle) this.mKeySets.get(((Long) keySetRefCounts.keyAt(i)).longValue());
            if (ks == null) {
                Slog.wtf(TAG, "Encountered non-existent key-set reference when reading settings");
            } else {
                ks.setRefCountLPw(((Integer) keySetRefCounts.valueAt(i)).intValue());
            }
        }
        ArraySet<Long> orphanedKeySets = new ArraySet();
        int numKeySets = this.mKeySets.size();
        for (i = 0; i < numKeySets; i++) {
            if (((KeySetHandle) this.mKeySets.valueAt(i)).getRefCountLPr() == 0) {
                Slog.wtf(TAG, "Encountered key-set w/out package references when reading settings");
                orphanedKeySets.add(Long.valueOf(this.mKeySets.keyAt(i)));
            }
            ArraySet<Long> pubKeys = (ArraySet) this.mKeySetMapping.valueAt(i);
            int pkSize = pubKeys.size();
            for (int j = 0; j < pkSize; j++) {
                ((PublicKeyHandle) this.mPublicKeys.get(((Long) pubKeys.valueAt(j)).longValue())).incrRefCountLPw();
            }
        }
        int numOrphans = orphanedKeySets.size();
        for (i = 0; i < numOrphans; i++) {
            decrementKeySetLPw(((Long) orphanedKeySets.valueAt(i)).longValue());
        }
    }
}

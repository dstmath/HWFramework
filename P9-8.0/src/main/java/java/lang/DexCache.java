package java.lang;

final class DexCache {
    private long dexFile;
    private String location;
    private int numResolvedCallSites;
    private int numResolvedFields;
    private int numResolvedMethodTypes;
    private int numResolvedMethods;
    private int numResolvedTypes;
    private int numStrings;
    private long resolvedCallSites;
    private long resolvedFields;
    private long resolvedMethodTypes;
    private long resolvedMethods;
    private long resolvedTypes;
    private long strings;

    private DexCache() {
    }
}

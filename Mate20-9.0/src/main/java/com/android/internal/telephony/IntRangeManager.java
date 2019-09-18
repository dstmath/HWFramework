package com.android.internal.telephony;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class IntRangeManager {
    private static final int INITIAL_CLIENTS_ARRAY_SIZE = 4;
    private ArrayList<IntRange> mRanges = new ArrayList<>();

    private class ClientRange {
        final String mClient;
        final int mEndId;
        final int mStartId;

        ClientRange(int startId, int endId, String client) {
            this.mStartId = startId;
            this.mEndId = endId;
            this.mClient = client;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == null || !(o instanceof ClientRange)) {
                return false;
            }
            ClientRange other = (ClientRange) o;
            if (this.mStartId == other.mStartId && this.mEndId == other.mEndId && this.mClient.equals(other.mClient)) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (((this.mStartId * 31) + this.mEndId) * 31) + this.mClient.hashCode();
        }
    }

    private class IntRange {
        final ArrayList<ClientRange> mClients;
        int mEndId;
        int mStartId;

        IntRange(int startId, int endId, String client) {
            this.mStartId = startId;
            this.mEndId = endId;
            this.mClients = new ArrayList<>(4);
            this.mClients.add(new ClientRange(startId, endId, client));
        }

        IntRange(ClientRange clientRange) {
            this.mStartId = clientRange.mStartId;
            this.mEndId = clientRange.mEndId;
            this.mClients = new ArrayList<>(4);
            this.mClients.add(clientRange);
        }

        IntRange(IntRange intRange, int numElements) {
            this.mStartId = intRange.mStartId;
            this.mEndId = intRange.mEndId;
            this.mClients = new ArrayList<>(intRange.mClients.size());
            for (int i = 0; i < numElements; i++) {
                this.mClients.add(intRange.mClients.get(i));
            }
        }

        /* access modifiers changed from: package-private */
        public void insert(ClientRange range) {
            int len = this.mClients.size();
            int insert = -1;
            for (int i = 0; i < len; i++) {
                ClientRange nextRange = this.mClients.get(i);
                if (range.mStartId <= nextRange.mStartId) {
                    if (!range.equals(nextRange)) {
                        if (range.mStartId == nextRange.mStartId && range.mEndId > nextRange.mEndId) {
                            insert = i + 1;
                            if (insert >= len) {
                                break;
                            }
                        } else {
                            this.mClients.add(i, range);
                        }
                    }
                    return;
                }
            }
            if (insert == -1 || insert >= len) {
                this.mClients.add(range);
            } else {
                this.mClients.add(insert, range);
            }
        }
    }

    /* access modifiers changed from: protected */
    public abstract void addRange(int i, int i2, boolean z);

    /* access modifiers changed from: protected */
    public abstract boolean finishUpdate();

    /* access modifiers changed from: protected */
    public abstract void startUpdate();

    protected IntRangeManager() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008b, code lost:
        return true;
     */
    public synchronized boolean enableRange(int startId, int endId, String client) {
        int len = this.mRanges.size();
        if (len != 0) {
            int startIndex = 0;
            while (startIndex < len) {
                IntRange range = this.mRanges.get(startIndex);
                if (startId >= range.mStartId && endId <= range.mEndId) {
                    range.insert(new ClientRange(startId, endId, client));
                    return true;
                } else if (startId - 1 == range.mEndId) {
                    int newRangeEndId = endId;
                    IntRange nextRange = null;
                    if (startIndex + 1 < len) {
                        nextRange = this.mRanges.get(startIndex + 1);
                        if (nextRange.mStartId - 1 > endId) {
                            nextRange = null;
                        } else if (endId <= nextRange.mEndId) {
                            newRangeEndId = nextRange.mStartId - 1;
                        }
                    }
                    if (!tryAddRanges(startId, newRangeEndId, true)) {
                        return false;
                    }
                    range.mEndId = endId;
                    range.insert(new ClientRange(startId, endId, client));
                    if (nextRange != null) {
                        if (range.mEndId < nextRange.mEndId) {
                            range.mEndId = nextRange.mEndId;
                        }
                        range.mClients.addAll(nextRange.mClients);
                        this.mRanges.remove(nextRange);
                    }
                } else if (startId < range.mStartId) {
                    if (endId + 1 < range.mStartId) {
                        if (!tryAddRanges(startId, endId, true)) {
                            return false;
                        }
                        this.mRanges.add(startIndex, new IntRange(startId, endId, client));
                        return true;
                    } else if (endId > range.mEndId) {
                        int endIndex = startIndex + 1;
                        while (endIndex < len) {
                            IntRange endRange = this.mRanges.get(endIndex);
                            if (endId + 1 < endRange.mStartId) {
                                if (!tryAddRanges(startId, endId, true)) {
                                    return false;
                                }
                                range.mStartId = startId;
                                range.mEndId = endId;
                                range.mClients.add(0, new ClientRange(startId, endId, client));
                                int joinIndex = startIndex + 1;
                                for (int i = joinIndex; i < endIndex; i++) {
                                    IntRange joinRange = this.mRanges.get(joinIndex);
                                    range.mClients.addAll(joinRange.mClients);
                                    this.mRanges.remove(joinRange);
                                }
                                return true;
                            } else if (endId > endRange.mEndId) {
                                endIndex++;
                            } else if (!tryAddRanges(startId, endRange.mStartId - 1, true)) {
                                return false;
                            } else {
                                range.mStartId = startId;
                                range.mEndId = endRange.mEndId;
                                range.mClients.add(0, new ClientRange(startId, endId, client));
                                int joinIndex2 = startIndex + 1;
                                for (int i2 = joinIndex2; i2 <= endIndex; i2++) {
                                    IntRange joinRange2 = this.mRanges.get(joinIndex2);
                                    range.mClients.addAll(joinRange2.mClients);
                                    this.mRanges.remove(joinRange2);
                                }
                                return true;
                            }
                        }
                        if (tryAddRanges(startId, endId, true) == 0) {
                            return false;
                        }
                        range.mStartId = startId;
                        range.mEndId = endId;
                        range.mClients.add(0, new ClientRange(startId, endId, client));
                        int joinIndex3 = startIndex + 1;
                        for (int i3 = joinIndex3; i3 < len; i3++) {
                            IntRange joinRange3 = this.mRanges.get(joinIndex3);
                            range.mClients.addAll(joinRange3.mClients);
                            this.mRanges.remove(joinRange3);
                        }
                        return true;
                    } else if (!tryAddRanges(startId, range.mStartId - 1, true)) {
                        return false;
                    } else {
                        range.mStartId = startId;
                        range.mClients.add(0, new ClientRange(startId, endId, client));
                        return true;
                    }
                } else if (startId + 1 > range.mEndId) {
                    startIndex++;
                } else if (endId <= range.mEndId) {
                    range.insert(new ClientRange(startId, endId, client));
                    return true;
                } else {
                    int endIndex2 = startIndex;
                    int testIndex = startIndex + 1;
                    while (true) {
                        if (testIndex >= len) {
                            break;
                        } else if (endId + 1 < this.mRanges.get(testIndex).mStartId) {
                            break;
                        } else {
                            endIndex2 = testIndex;
                            testIndex++;
                        }
                    }
                    if (endIndex2 != startIndex) {
                        IntRange endRange2 = this.mRanges.get(endIndex2);
                        if (!tryAddRanges(range.mEndId + 1, endId <= endRange2.mEndId ? endRange2.mStartId - 1 : endId, true)) {
                            return false;
                        }
                        range.mEndId = endId <= endRange2.mEndId ? endRange2.mEndId : endId;
                        range.insert(new ClientRange(startId, endId, client));
                        int joinIndex4 = startIndex + 1;
                        for (int i4 = joinIndex4; i4 <= endIndex2; i4++) {
                            IntRange joinRange4 = this.mRanges.get(joinIndex4);
                            range.mClients.addAll(joinRange4.mClients);
                            this.mRanges.remove(joinRange4);
                        }
                        return true;
                    } else if (!tryAddRanges(range.mEndId + 1, endId, true)) {
                        return false;
                    } else {
                        range.mEndId = endId;
                        range.insert(new ClientRange(startId, endId, client));
                        return true;
                    }
                }
            }
            if (tryAddRanges(startId, endId, true) == 0) {
                return false;
            }
            this.mRanges.add(new IntRange(startId, endId, client));
            return true;
        } else if (!tryAddRanges(startId, endId, true)) {
            return false;
        } else {
            this.mRanges.add(new IntRange(startId, endId, client));
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0059, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x013e, code lost:
        return true;
     */
    public synchronized boolean disableRange(int startId, int endId, String client) {
        int i = startId;
        int i2 = endId;
        String str = client;
        synchronized (this) {
            int len = this.mRanges.size();
            boolean z = false;
            int i3 = 0;
            while (i3 < len) {
                IntRange range = this.mRanges.get(i3);
                if (i < range.mStartId) {
                    return z;
                }
                if (i2 <= range.mEndId) {
                    ArrayList<ClientRange> clients = range.mClients;
                    int crLength = clients.size();
                    boolean z2 = true;
                    if (crLength == 1) {
                        ClientRange cr = clients.get(z ? 1 : 0);
                        if (cr.mStartId == i && cr.mEndId == i2 && cr.mClient.equals(str)) {
                            this.mRanges.remove(i3);
                            if (updateRanges()) {
                                return true;
                            }
                            this.mRanges.add(i3, range);
                            return z;
                        }
                    } else {
                        boolean updateStarted = false;
                        int largestEndId = Integer.MIN_VALUE;
                        int crIndex = z;
                        while (crIndex < crLength) {
                            ClientRange cr2 = clients.get(crIndex);
                            if (cr2.mStartId != i || cr2.mEndId != i2 || !cr2.mClient.equals(str)) {
                                int len2 = len;
                                ArrayList<ClientRange> clients2 = clients;
                                boolean z3 = z2;
                                if (cr2.mEndId > largestEndId) {
                                    largestEndId = cr2.mEndId;
                                }
                                crIndex++;
                                z2 = z3;
                                len = len2;
                                clients = clients2;
                                i = startId;
                                str = client;
                                z = false;
                            } else if (crIndex != crLength - 1) {
                                IntRange rangeCopy = new IntRange(range, crIndex);
                                if (crIndex == 0) {
                                    int nextStartId = clients.get(z2 ? 1 : 0).mStartId;
                                    if (nextStartId != range.mStartId) {
                                        rangeCopy.mStartId = nextStartId;
                                        updateStarted = true;
                                    }
                                    largestEndId = clients.get(1).mEndId;
                                }
                                ArrayList<IntRange> newRanges = new ArrayList<>();
                                IntRange currentRange = rangeCopy;
                                int nextIndex = crIndex + 1;
                                while (true) {
                                    int nextIndex2 = nextIndex;
                                    if (nextIndex2 >= crLength) {
                                        break;
                                    }
                                    int len3 = len;
                                    ClientRange nextCr = clients.get(nextIndex2);
                                    ArrayList<ClientRange> clients3 = clients;
                                    if (nextCr.mStartId > largestEndId + 1) {
                                        currentRange.mEndId = largestEndId;
                                        newRanges.add(currentRange);
                                        updateStarted = true;
                                        currentRange = new IntRange(nextCr);
                                    } else {
                                        if (currentRange.mEndId < nextCr.mEndId) {
                                            currentRange.mEndId = nextCr.mEndId;
                                        }
                                        currentRange.mClients.add(nextCr);
                                    }
                                    if (nextCr.mEndId > largestEndId) {
                                        largestEndId = nextCr.mEndId;
                                    }
                                    nextIndex = nextIndex2 + 1;
                                    len = len3;
                                    clients = clients3;
                                    int i4 = startId;
                                    String str2 = client;
                                }
                                ArrayList<ClientRange> arrayList = clients;
                                if (largestEndId < i2) {
                                    updateStarted = true;
                                    currentRange.mEndId = largestEndId;
                                }
                                newRanges.add(currentRange);
                                this.mRanges.remove(i3);
                                this.mRanges.addAll(i3, newRanges);
                                if (updateStarted && !updateRanges()) {
                                    this.mRanges.removeAll(newRanges);
                                    this.mRanges.add(i3, range);
                                    return false;
                                }
                            } else if (range.mEndId == largestEndId) {
                                clients.remove(crIndex);
                                return z2;
                            } else {
                                clients.remove(crIndex);
                                range.mEndId = largestEndId;
                                if (updateRanges()) {
                                    return z2;
                                }
                                clients.add(crIndex, cr2);
                                range.mEndId = cr2.mEndId;
                                return z;
                            }
                        }
                        continue;
                    }
                }
                i3++;
                len = len;
                i = startId;
                str = client;
                z = false;
            }
            return false;
        }
    }

    public boolean updateRanges() {
        startUpdate();
        populateAllRanges();
        return finishUpdate();
    }

    /* access modifiers changed from: protected */
    public boolean tryAddRanges(int startId, int endId, boolean selected) {
        startUpdate();
        populateAllRanges();
        addRange(startId, endId, selected);
        return finishUpdate();
    }

    public boolean isEmpty() {
        return this.mRanges.isEmpty();
    }

    private void populateAllRanges() {
        Iterator<IntRange> itr = this.mRanges.iterator();
        while (itr.hasNext()) {
            IntRange currRange = itr.next();
            addRange(currRange.mStartId, currRange.mEndId, true);
        }
    }

    private void populateAllClientRanges() {
        int len = this.mRanges.size();
        for (int i = 0; i < len; i++) {
            IntRange range = this.mRanges.get(i);
            int clientLen = range.mClients.size();
            for (int j = 0; j < clientLen; j++) {
                ClientRange nextRange = range.mClients.get(j);
                addRange(nextRange.mStartId, nextRange.mEndId, true);
            }
        }
    }
}

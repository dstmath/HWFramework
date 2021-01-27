package com.android.server.backup;

public class JobIdManager {
    public static int getJobIdForUserId(int minJobId, int maxJobId, int userId) {
        if (minJobId + userId <= maxJobId) {
            return minJobId + userId;
        }
        throw new RuntimeException("No job IDs available in the given range");
    }
}

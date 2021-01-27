package com.android.server.role;

import android.util.ArrayMap;
import android.util.ArraySet;

public abstract class RoleManagerInternal {
    public abstract ArrayMap<String, ArraySet<String>> getRolesAndHolders(int i);
}

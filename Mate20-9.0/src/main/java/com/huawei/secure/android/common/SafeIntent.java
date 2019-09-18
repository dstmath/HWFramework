package com.huawei.secure.android.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;

@Deprecated
public class SafeIntent extends Intent {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    @Deprecated
    public SafeIntent(Intent intent) {
        super(intent == null ? new Intent() : intent);
    }

    @Deprecated
    public <T extends Parcelable> T getParcelableExtra(String name) {
        try {
            return super.getParcelableExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public String getStringExtra(String name) {
        try {
            return super.getStringExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public boolean getBooleanExtra(String name, boolean defaultValue) {
        try {
            return super.getBooleanExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public int getIntExtra(String name, int defaultValue) {
        try {
            return super.getIntExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public byte getByteExtra(String name, byte defaultValue) {
        try {
            return super.getByteExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public CharSequence getCharSequenceExtra(String name) {
        try {
            return super.getCharSequenceExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public char getCharExtra(String name, char defaultValue) {
        try {
            return super.getCharExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public long getLongExtra(String name, long defaultValue) {
        try {
            return super.getLongExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public float getFloatExtra(String name, float defaultValue) {
        try {
            return super.getFloatExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public double getDoubleExtra(String name, double defaultValue) {
        try {
            return super.getDoubleExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public Serializable getSerializableExtra(String name) {
        try {
            return super.getSerializableExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public ArrayList<CharSequence> getCharSequenceArrayListExtra(String name) {
        try {
            return super.getCharSequenceArrayListExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public ArrayList<Integer> getIntegerArrayListExtra(String name) {
        try {
            return super.getIntegerArrayListExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public ArrayList<String> getStringArrayListExtra(String name) {
        try {
            return super.getStringArrayListExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(String name) {
        try {
            return super.getParcelableArrayListExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public boolean[] getBooleanArrayExtra(String name) {
        try {
            return super.getBooleanArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public Bundle getBundleExtra(String name) {
        try {
            return super.getBundleExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public Bundle getExtras() {
        try {
            return super.getExtras();
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public byte[] getByteArrayExtra(String name) {
        try {
            return super.getByteArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public char[] getCharArrayExtra(String name) {
        try {
            return super.getCharArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public CharSequence[] getCharSequenceArrayExtra(String name) {
        try {
            return super.getCharSequenceArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public double[] getDoubleArrayExtra(String name) {
        try {
            return super.getDoubleArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public float[] getFloatArrayExtra(String name) {
        try {
            return super.getFloatArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public int[] getIntArrayExtra(String name) {
        try {
            return super.getIntArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public long[] getLongArrayExtra(String name) {
        try {
            return super.getLongArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public Parcelable[] getParcelableArrayExtra(String name) {
        try {
            return super.getParcelableArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public String[] getStringArrayExtra(String name) {
        try {
            return super.getStringArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public short getShortExtra(String name, short defaultValue) {
        try {
            return super.getShortExtra(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Deprecated
    public short[] getShortArrayExtra(String name) {
        try {
            return super.getShortArrayExtra(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public boolean hasExtra(String name) {
        try {
            return super.hasExtra(name);
        } catch (Exception e) {
            return false;
        }
    }
}

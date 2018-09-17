package com.huawei.secure.android.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;

public class SafeIntent extends Intent {
    public SafeIntent(Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        super(intent);
    }

    public <T extends Parcelable> T getParcelableExtra(String str) {
        try {
            return super.getParcelableExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public String getStringExtra(String str) {
        try {
            return super.getStringExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean getBooleanExtra(String str, boolean z) {
        try {
            return super.getBooleanExtra(str, z);
        } catch (Exception e) {
            return z;
        }
    }

    public int getIntExtra(String str, int i) {
        try {
            return super.getIntExtra(str, i);
        } catch (Exception e) {
            return i;
        }
    }

    public byte getByteExtra(String str, byte b) {
        try {
            return super.getByteExtra(str, b);
        } catch (Exception e) {
            return b;
        }
    }

    public CharSequence getCharSequenceExtra(String str) {
        try {
            return super.getCharSequenceExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public char getCharExtra(String str, char c) {
        try {
            return super.getCharExtra(str, c);
        } catch (Exception e) {
            return c;
        }
    }

    public long getLongExtra(String str, long j) {
        try {
            return super.getLongExtra(str, j);
        } catch (Exception e) {
            return j;
        }
    }

    public float getFloatExtra(String str, float f) {
        try {
            return super.getFloatExtra(str, f);
        } catch (Exception e) {
            return f;
        }
    }

    public double getDoubleExtra(String str, double d) {
        try {
            return super.getDoubleExtra(str, d);
        } catch (Exception e) {
            return d;
        }
    }

    public Serializable getSerializableExtra(String str) {
        try {
            return super.getSerializableExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<CharSequence> getCharSequenceArrayListExtra(String str) {
        try {
            return super.getCharSequenceArrayListExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<Integer> getIntegerArrayListExtra(String str) {
        try {
            return super.getIntegerArrayListExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<String> getStringArrayListExtra(String str) {
        try {
            return super.getStringArrayListExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(String str) {
        try {
            return super.getParcelableArrayListExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean[] getBooleanArrayExtra(String str) {
        try {
            return super.getBooleanArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public Bundle getBundleExtra(String str) {
        try {
            return super.getBundleExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public Bundle getExtras() {
        try {
            return super.getExtras();
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] getByteArrayExtra(String str) {
        try {
            return super.getByteArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public char[] getCharArrayExtra(String str) {
        try {
            return super.getCharArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public CharSequence[] getCharSequenceArrayExtra(String str) {
        try {
            return super.getCharSequenceArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public double[] getDoubleArrayExtra(String str) {
        try {
            return super.getDoubleArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public float[] getFloatArrayExtra(String str) {
        try {
            return super.getFloatArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public int[] getIntArrayExtra(String str) {
        try {
            return super.getIntArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public long[] getLongArrayExtra(String str) {
        try {
            return super.getLongArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public Parcelable[] getParcelableArrayExtra(String str) {
        try {
            return super.getParcelableArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getStringArrayExtra(String str) {
        try {
            return super.getStringArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }

    public short getShortExtra(String str, short s) {
        try {
            return super.getShortExtra(str, s);
        } catch (Exception e) {
            return s;
        }
    }

    public short[] getShortArrayExtra(String str) {
        try {
            return super.getShortArrayExtra(str);
        } catch (Exception e) {
            return null;
        }
    }
}

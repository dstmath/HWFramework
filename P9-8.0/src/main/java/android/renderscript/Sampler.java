package android.renderscript;

public class Sampler extends BaseObj {
    float mAniso;
    Value mMag;
    Value mMin;
    Value mWrapR;
    Value mWrapS;
    Value mWrapT;

    public static class Builder {
        float mAniso = 1.0f;
        Value mMag = Value.NEAREST;
        Value mMin = Value.NEAREST;
        RenderScript mRS;
        Value mWrapR = Value.WRAP;
        Value mWrapS = Value.WRAP;
        Value mWrapT = Value.WRAP;

        public Builder(RenderScript rs) {
            this.mRS = rs;
        }

        public void setMinification(Value v) {
            if (v == Value.NEAREST || v == Value.LINEAR || v == Value.LINEAR_MIP_LINEAR || v == Value.LINEAR_MIP_NEAREST) {
                this.mMin = v;
                return;
            }
            throw new IllegalArgumentException("Invalid value");
        }

        public void setMagnification(Value v) {
            if (v == Value.NEAREST || v == Value.LINEAR) {
                this.mMag = v;
                return;
            }
            throw new IllegalArgumentException("Invalid value");
        }

        public void setWrapS(Value v) {
            if (v == Value.WRAP || v == Value.CLAMP || v == Value.MIRRORED_REPEAT) {
                this.mWrapS = v;
                return;
            }
            throw new IllegalArgumentException("Invalid value");
        }

        public void setWrapT(Value v) {
            if (v == Value.WRAP || v == Value.CLAMP || v == Value.MIRRORED_REPEAT) {
                this.mWrapT = v;
                return;
            }
            throw new IllegalArgumentException("Invalid value");
        }

        public void setAnisotropy(float v) {
            if (v >= 0.0f) {
                this.mAniso = v;
                return;
            }
            throw new IllegalArgumentException("Invalid value");
        }

        public Sampler create() {
            this.mRS.validate();
            Sampler sampler = new Sampler(this.mRS.nSamplerCreate(this.mMag.mID, this.mMin.mID, this.mWrapS.mID, this.mWrapT.mID, this.mWrapR.mID, this.mAniso), this.mRS);
            sampler.mMin = this.mMin;
            sampler.mMag = this.mMag;
            sampler.mWrapS = this.mWrapS;
            sampler.mWrapT = this.mWrapT;
            sampler.mWrapR = this.mWrapR;
            sampler.mAniso = this.mAniso;
            return sampler;
        }
    }

    public enum Value {
        NEAREST(0),
        LINEAR(1),
        LINEAR_MIP_LINEAR(2),
        LINEAR_MIP_NEAREST(5),
        WRAP(3),
        CLAMP(4),
        MIRRORED_REPEAT(6);
        
        int mID;

        private Value(int id) {
            this.mID = id;
        }
    }

    Sampler(long id, RenderScript rs) {
        super(id, rs);
        this.guard.open("destroy");
    }

    public Value getMinification() {
        return this.mMin;
    }

    public Value getMagnification() {
        return this.mMag;
    }

    public Value getWrapS() {
        return this.mWrapS;
    }

    public Value getWrapT() {
        return this.mWrapT;
    }

    public float getAnisotropy() {
        return this.mAniso;
    }

    public static Sampler CLAMP_NEAREST(RenderScript rs) {
        if (rs.mSampler_CLAMP_NEAREST == null) {
            synchronized (rs) {
                if (rs.mSampler_CLAMP_NEAREST == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.NEAREST);
                    b.setMagnification(Value.NEAREST);
                    b.setWrapS(Value.CLAMP);
                    b.setWrapT(Value.CLAMP);
                    rs.mSampler_CLAMP_NEAREST = b.create();
                }
            }
        }
        return rs.mSampler_CLAMP_NEAREST;
    }

    public static Sampler CLAMP_LINEAR(RenderScript rs) {
        if (rs.mSampler_CLAMP_LINEAR == null) {
            synchronized (rs) {
                if (rs.mSampler_CLAMP_LINEAR == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.LINEAR);
                    b.setMagnification(Value.LINEAR);
                    b.setWrapS(Value.CLAMP);
                    b.setWrapT(Value.CLAMP);
                    rs.mSampler_CLAMP_LINEAR = b.create();
                }
            }
        }
        return rs.mSampler_CLAMP_LINEAR;
    }

    public static Sampler CLAMP_LINEAR_MIP_LINEAR(RenderScript rs) {
        if (rs.mSampler_CLAMP_LINEAR_MIP_LINEAR == null) {
            synchronized (rs) {
                if (rs.mSampler_CLAMP_LINEAR_MIP_LINEAR == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.LINEAR_MIP_LINEAR);
                    b.setMagnification(Value.LINEAR);
                    b.setWrapS(Value.CLAMP);
                    b.setWrapT(Value.CLAMP);
                    rs.mSampler_CLAMP_LINEAR_MIP_LINEAR = b.create();
                }
            }
        }
        return rs.mSampler_CLAMP_LINEAR_MIP_LINEAR;
    }

    public static Sampler WRAP_NEAREST(RenderScript rs) {
        if (rs.mSampler_WRAP_NEAREST == null) {
            synchronized (rs) {
                if (rs.mSampler_WRAP_NEAREST == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.NEAREST);
                    b.setMagnification(Value.NEAREST);
                    b.setWrapS(Value.WRAP);
                    b.setWrapT(Value.WRAP);
                    rs.mSampler_WRAP_NEAREST = b.create();
                }
            }
        }
        return rs.mSampler_WRAP_NEAREST;
    }

    public static Sampler WRAP_LINEAR(RenderScript rs) {
        if (rs.mSampler_WRAP_LINEAR == null) {
            synchronized (rs) {
                if (rs.mSampler_WRAP_LINEAR == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.LINEAR);
                    b.setMagnification(Value.LINEAR);
                    b.setWrapS(Value.WRAP);
                    b.setWrapT(Value.WRAP);
                    rs.mSampler_WRAP_LINEAR = b.create();
                }
            }
        }
        return rs.mSampler_WRAP_LINEAR;
    }

    public static Sampler WRAP_LINEAR_MIP_LINEAR(RenderScript rs) {
        if (rs.mSampler_WRAP_LINEAR_MIP_LINEAR == null) {
            synchronized (rs) {
                if (rs.mSampler_WRAP_LINEAR_MIP_LINEAR == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.LINEAR_MIP_LINEAR);
                    b.setMagnification(Value.LINEAR);
                    b.setWrapS(Value.WRAP);
                    b.setWrapT(Value.WRAP);
                    rs.mSampler_WRAP_LINEAR_MIP_LINEAR = b.create();
                }
            }
        }
        return rs.mSampler_WRAP_LINEAR_MIP_LINEAR;
    }

    public static Sampler MIRRORED_REPEAT_NEAREST(RenderScript rs) {
        if (rs.mSampler_MIRRORED_REPEAT_NEAREST == null) {
            synchronized (rs) {
                if (rs.mSampler_MIRRORED_REPEAT_NEAREST == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.NEAREST);
                    b.setMagnification(Value.NEAREST);
                    b.setWrapS(Value.MIRRORED_REPEAT);
                    b.setWrapT(Value.MIRRORED_REPEAT);
                    rs.mSampler_MIRRORED_REPEAT_NEAREST = b.create();
                }
            }
        }
        return rs.mSampler_MIRRORED_REPEAT_NEAREST;
    }

    public static Sampler MIRRORED_REPEAT_LINEAR(RenderScript rs) {
        if (rs.mSampler_MIRRORED_REPEAT_LINEAR == null) {
            synchronized (rs) {
                if (rs.mSampler_MIRRORED_REPEAT_LINEAR == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.LINEAR);
                    b.setMagnification(Value.LINEAR);
                    b.setWrapS(Value.MIRRORED_REPEAT);
                    b.setWrapT(Value.MIRRORED_REPEAT);
                    rs.mSampler_MIRRORED_REPEAT_LINEAR = b.create();
                }
            }
        }
        return rs.mSampler_MIRRORED_REPEAT_LINEAR;
    }

    public static Sampler MIRRORED_REPEAT_LINEAR_MIP_LINEAR(RenderScript rs) {
        if (rs.mSampler_MIRRORED_REPEAT_LINEAR_MIP_LINEAR == null) {
            synchronized (rs) {
                if (rs.mSampler_MIRRORED_REPEAT_LINEAR_MIP_LINEAR == null) {
                    Builder b = new Builder(rs);
                    b.setMinification(Value.LINEAR_MIP_LINEAR);
                    b.setMagnification(Value.LINEAR);
                    b.setWrapS(Value.MIRRORED_REPEAT);
                    b.setWrapT(Value.MIRRORED_REPEAT);
                    rs.mSampler_MIRRORED_REPEAT_LINEAR_MIP_LINEAR = b.create();
                }
            }
        }
        return rs.mSampler_MIRRORED_REPEAT_LINEAR_MIP_LINEAR;
    }
}

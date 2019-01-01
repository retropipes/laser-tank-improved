package squidpony.squidmath;

/**
 * Created by Tommy Ettinger on 3/17/2017.
 */
public class Noise {
    public static final SeededNoise alternate = new SeededNoise(0xFEEDCAFE);

    public interface Noise1D {
	double getNoise(double x);

	double getNoiseWithSeed(double x, int seed);
    }

    public interface Noise2D {
	double getNoise(double x, double y);

	double getNoiseWithSeed(double x, double y, int seed);
    }

    public interface Noise3D {
	double getNoise(double x, double y, double z);

	double getNoiseWithSeed(double x, double y, double z, int seed);
    }

    public interface Noise4D {
	double getNoise(double x, double y, double z, double w);

	double getNoiseWithSeed(double x, double y, double z, double w, int seed);
    }

    public interface Noise6D {
	double getNoise(double x, double y, double z, double w, double u, double v);

	double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed);
    }

    public static class Layered1D implements Noise1D {
	protected int octaves;
	protected Noise1D basis;
	public double frequency;

	public Layered1D() {
	    this(ValueNoise.instance);
	}

	public Layered1D(final Noise1D basis) {
	    this(basis, 2);
	}

	public Layered1D(final Noise1D basis, final int octaves) {
	    this(basis, octaves, 1.0);
	}

	public Layered1D(final Noise1D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x) {
	    x *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		n += this.basis.getNoise(x * (i_s *= 0.5) + (o << 6)) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(double x, final int seed) {
	    x *= this.frequency;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		// seed2 = PintRNG.determine(seed2);
		n += this.basis.getNoiseWithSeed(x * (i_s *= 0.5), seed2 += 0x9E3779B9) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Layered2D implements Noise2D {
	protected int octaves;
	protected Noise2D basis;
	public double frequency;

	public Layered2D() {
	    this(SeededNoise.instance, 2);
	}

	public Layered2D(final Noise2D basis) {
	    this(basis, 2);
	}

	public Layered2D(final Noise2D basis, final int octaves) {
	    this(basis, octaves, 1.0);
	}

	public Layered2D(final Noise2D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y) {
	    x *= this.frequency;
	    y *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		n += this.basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7)) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		// seed2 = PintRNG.determine(seed2);
		n += this.basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, seed2 += 0x9E3779B9) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Layered3D implements Noise3D {
	protected int octaves;
	protected Noise3D basis;
	public double frequency;

	public Layered3D() {
	    this(SeededNoise.instance, 2);
	}

	public Layered3D(final Noise3D basis) {
	    this(basis, 2);
	}

	public Layered3D(final Noise3D basis, final int octaves) {
	    this(basis, octaves, 1.0);
	}

	public Layered3D(final Noise3D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y, double z) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		n += this.basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		// seed2 = PintRNG.determine(seed2);
		n += this.basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, z * i_s, seed2 += 0x9E3779B9) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Layered4D implements Noise4D {
	protected int octaves;
	protected Noise4D basis;
	public double frequency;

	public Layered4D() {
	    this(SeededNoise.instance, 2);
	}

	public Layered4D(final Noise4D basis) {
	    this(basis, 2);
	}

	public Layered4D(final Noise4D basis, final int octaves) {
	    this(basis, octaves, 1.0);
	}

	public Layered4D(final Noise4D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y, double z, double w) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		n += this.basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8),
			w * i_s + (o << 9)) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		// seed2 = PintRNG.determine(seed2);
		n += this.basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, z * i_s, w * i_s, seed2 += 0x9E3779B9) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Layered6D implements Noise6D {
	protected int octaves;
	protected Noise6D basis;
	public double frequency;

	public Layered6D() {
	    this(SeededNoise.instance, 2);
	}

	public Layered6D(final Noise6D basis) {
	    this(basis, 2);
	}

	public Layered6D(final Noise6D basis, final int octaves) {
	    this(basis, octaves, 1.0);
	}

	public Layered6D(final Noise6D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y, double z, double w, double u, double v) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		n += this.basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8),
			w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11)) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 2.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		// seed2 = PintRNG.determine(seed2);
		n += this.basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, z * i_s, w * i_s, u * i_s, v * i_s,
			seed2 += 0x9E3779B9) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Scaled1D implements Noise1D {
	protected double scaleX;
	protected Noise1D basis;

	public Scaled1D() {
	    this(ValueNoise.instance);
	}

	public Scaled1D(final Noise1D basis) {
	    this(basis, 2.0);
	}

	public Scaled1D(final Noise1D basis, final double scaleX) {
	    this.basis = basis;
	    this.scaleX = scaleX;
	}

	@Override
	public double getNoise(final double x) {
	    return this.basis.getNoise(x * this.scaleX);
	}

	@Override
	public double getNoiseWithSeed(final double x, final int seed) {
	    return this.basis.getNoiseWithSeed(x * this.scaleX, seed);
	}
    }

    public static class Scaled2D implements Noise2D {
	protected double scaleX, scaleY;
	protected Noise2D basis;

	public Scaled2D() {
	    this(SeededNoise.instance);
	}

	public Scaled2D(final Noise2D basis) {
	    this(basis, 2.0, 2.0);
	}

	public Scaled2D(final Noise2D basis, final double scaleX, final double scaleY) {
	    this.basis = basis;
	    this.scaleX = scaleX;
	    this.scaleY = scaleY;
	}

	@Override
	public double getNoise(final double x, final double y) {
	    return this.basis.getNoise(x * this.scaleX, y * this.scaleY);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final int seed) {
	    return this.basis.getNoiseWithSeed(x * this.scaleX, y * this.scaleY, seed);
	}
    }

    public static class Scaled3D implements Noise3D {
	protected double scaleX, scaleY, scaleZ;
	protected Noise3D basis;

	public Scaled3D() {
	    this(SeededNoise.instance);
	}

	public Scaled3D(final Noise3D basis) {
	    this(basis, 2.0, 2.0, 2.0);
	}

	public Scaled3D(final Noise3D basis, final double scaleX, final double scaleY, final double scaleZ) {
	    this.basis = basis;
	    this.scaleX = scaleX;
	    this.scaleY = scaleY;
	    this.scaleZ = scaleZ;
	}

	@Override
	public double getNoise(final double x, final double y, final double z) {
	    return this.basis.getNoise(x * this.scaleX, y * this.scaleY, z * this.scaleZ);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
	    return this.basis.getNoiseWithSeed(x * this.scaleX, y * this.scaleY, z * this.scaleZ, seed);
	}
    }

    public static class Scaled4D implements Noise4D {
	protected double scaleX, scaleY, scaleZ, scaleW;
	protected Noise4D basis;

	public Scaled4D() {
	    this(SeededNoise.instance);
	}

	public Scaled4D(final Noise4D basis) {
	    this(basis, 2.0, 2.0, 2.0, 2.0);
	}

	public Scaled4D(final Noise4D basis, final double scaleX, final double scaleY, final double scaleZ,
		final double scaleW) {
	    this.basis = basis;
	    this.scaleX = scaleX;
	    this.scaleY = scaleY;
	    this.scaleZ = scaleZ;
	    this.scaleW = scaleW;
	}

	@Override
	public double getNoise(final double x, final double y, final double z, final double w) {
	    return this.basis.getNoise(x * this.scaleX, y * this.scaleY, z * this.scaleZ, w * this.scaleW);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
	    return this.basis.getNoiseWithSeed(x * this.scaleX, y * this.scaleY, z * this.scaleZ, w * this.scaleW,
		    seed);
	}
    }

    public static class Scaled6D implements Noise6D {
	protected double scaleX, scaleY, scaleZ, scaleW, scaleU, scaleV;
	protected Noise6D basis;

	public Scaled6D() {
	    this(SeededNoise.instance);
	}

	public Scaled6D(final Noise6D basis) {
	    this(basis, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0);
	}

	public Scaled6D(final Noise6D basis, final double scaleX, final double scaleY, final double scaleZ,
		final double scaleW, final double scaleU, final double scaleV) {
	    this.basis = basis;
	    this.scaleX = scaleX;
	    this.scaleY = scaleY;
	    this.scaleZ = scaleZ;
	    this.scaleW = scaleW;
	    this.scaleU = scaleU;
	    this.scaleV = scaleV;
	}

	@Override
	public double getNoise(final double x, final double y, final double z, final double w, final double u,
		final double v) {
	    return this.basis.getNoise(x * this.scaleX, y * this.scaleY, z * this.scaleZ, w * this.scaleW,
		    u * this.scaleU, v * this.scaleV);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u,
		final double v, final int seed) {
	    return this.basis.getNoiseWithSeed(x * this.scaleX, y * this.scaleY, z * this.scaleZ, w * this.scaleW,
		    u * this.scaleU, v * this.scaleV, seed);
	}
    }

    public static class Ridged2D implements Noise2D {
	protected int octaves;
	public double frequency;
	protected double correct;
	protected Noise2D basis;

	public Ridged2D() {
	    this(SeededNoise.instance, 2, 1.25);
	}

	public Ridged2D(final Noise2D basis) {
	    this(basis, 2, 1.25);
	}

	public Ridged2D(final Noise2D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.setOctaves(octaves);
	}

	public void setOctaves(int octaves) {
	    this.octaves = octaves = Math.max(1, Math.min(63, octaves));
	    for (int o = 0; o < octaves; o++) {
		this.correct += Math.pow(2.0, -o);
	    }
	    this.correct = 1.9 / this.correct;
	}

	@Override
	public double getNoise(double x, double y) {
	    double sum = 0, amp = 1.0;
	    x *= this.frequency;
	    y *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		double n = this.basis.getNoise(x + (i << 6), y + (i << 7));
		n = 1.0 - Math.abs(n);
		sum += amp * n;
		amp *= 0.5;
		x *= 2.0;
		y *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, int seed) {
	    double sum = 0, amp = 1.0;
	    x *= this.frequency;
	    y *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		seed = PintRNG.determine(seed);
		double n = this.basis.getNoiseWithSeed(x, y, seed += 0x9E3779B9);
		n = 1.0 - Math.abs(n);
		sum += amp * n;
		amp *= 0.5;
		x *= 2.0;
		y *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}
    }

    public static class Ridged3D implements Noise3D {
	protected int octaves;
	public double frequency;
	protected double correct;
	protected Noise3D basis;

	public Ridged3D() {
	    this(SeededNoise.instance, 2, 1.25);
	}

	public Ridged3D(final Noise3D basis) {
	    this(basis, 2, 1.25);
	}

	public Ridged3D(final Noise3D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.setOctaves(octaves);
	}

	public void setOctaves(int octaves) {
	    this.octaves = octaves = Math.max(1, Math.min(63, octaves));
	    for (int o = 0; o < octaves; o++) {
		this.correct += Math.pow(2.0, -o);
	    }
	    this.correct = 1.45 / this.correct;
	}

	@Override
	public double getNoise(double x, double y, double z) {
	    double sum = 0, amp = 1.0;
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		double n = this.basis.getNoise(x + (i << 6), y + (i << 7), z + (i << 8));
		n = 1.0 - Math.abs(n);
		sum += amp * n;
		amp *= 0.5;
		x *= 2.0;
		y *= 2.0;
		z *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, int seed) {
	    double sum = 0, amp = 1.0;
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		seed = PintRNG.determine(seed);
		double n = this.basis.getNoiseWithSeed(x, y, z, seed += 0x9E3779B9);
		n = 1.0 - Math.abs(n);
		sum += amp * n;
		amp *= 0.5;
		x *= 2.0;
		y *= 2.0;
		z *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}
    }

    public static class Ridged4D implements Noise4D {
	public double exp[];
	protected int octaves;
	public double frequency, correct;
	public Noise4D basis;

	public Ridged4D() {
	    this(SeededNoise.instance, 2, 1.25);
	}

	public Ridged4D(final Noise4D basis) {
	    this(basis, 2, 1.25);
	}

	public Ridged4D(final Noise4D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.setOctaves(octaves);
	}

	public void setOctaves(int octaves) {
	    this.octaves = octaves = Math.max(1, Math.min(63, octaves));
	    this.exp = new double[octaves];
	    double maxvalue = 0.0;
	    for (int i = 0; i < octaves; ++i) {
		maxvalue += this.exp[i] = Math.pow(2.0, -0.9 * i);
	    }
	    this.correct = 1.41 / maxvalue;
	}

	@Override
	public double getNoise(double x, double y, double z, double w) {
	    double sum = 0.0, n;
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		n = this.basis.getNoise(x + (i << 6), y + (i << 7), z + (i << 8), w + (i << 9));
		n = 1.0 - Math.abs(n);
		sum += n * n * this.exp[i];
		x *= 2.0;
		y *= 2.0;
		z *= 2.0;
		w *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
	    double sum = 0, n;
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		// seed = PintRNG.determine(seed);
		n = this.basis.getNoiseWithSeed(x, y, z, w, seed += 0x9E3779B9);
		n = 1.0 - Math.abs(n);
		sum += n * n * this.exp[i];
		x *= 2.0;
		y *= 2.0;
		z *= 2.0;
		w *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}
    }

    public static class Ridged6D implements Noise6D {
	protected double[] exp;
	protected int octaves;
	public double frequency, correct;
	public Noise6D basis;

	public Ridged6D() {
	    this(SeededNoise.instance, 2, 1.25);
	}

	public Ridged6D(final Noise6D basis) {
	    this(basis, 2, 1.25);
	}

	public Ridged6D(final Noise6D basis, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.setOctaves(octaves);
	}

	public void setOctaves(int octaves) {
	    this.octaves = octaves = Math.max(1, Math.min(63, octaves));
	    this.exp = new double[octaves];
	    double maxvalue = 0.0;
	    for (int i = 0; i < octaves; ++i) {
		maxvalue += this.exp[i] = Math.pow(2.0, -0.9 * i);
	    }
	    this.correct = 1.28 / maxvalue;
	}

	@Override
	public double getNoise(double x, double y, double z, double w, double u, double v) {
	    double sum = 0.0, n;
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		n = this.basis.getNoise(x + (i << 6), y + (i << 7), z + (i << 8), w + (i << 9), u + (i << 10),
			v + (i << 11));
		n = 1.0 - Math.abs(n);
		sum += n * n * this.exp[i];
		x *= 2.0;
		y *= 2.0;
		z *= 2.0;
		w *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
	    double sum = 0, n;
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    for (int i = 0; i < this.octaves; ++i) {
		seed = PintRNG.determine(seed);
		n = this.basis.getNoiseWithSeed(x, y, z, w, u, v, seed += 0x9E3779B9);
		n = 1.0 - Math.abs(n);
		sum += n * n * this.exp[i];
		x *= 2.0;
		y *= 2.0;
		z *= 2.0;
		w *= 2.0;
		u *= 2.0;
		v *= 2.0;
	    }
	    return sum * this.correct - 1.0;
	}
    }
    /*
     * public static class Turbulent2D implements Noise2D { protected int octaves;
     * protected Noise2D basis, disturbance;
     *
     * public Turbulent2D() { this(SeededNoise.instance, alternate, 1); }
     *
     * public Turbulent2D(Noise2D basis, Noise2D disturb) { this(basis, disturb, 1);
     * }
     *
     * public Turbulent2D(Noise2D basis, Noise2D disturb, final int octaves) {
     * this.basis = basis; disturbance = disturb; this.octaves = Math.max(1,
     * Math.min(63, octaves)); }
     *
     * @Override public double getNoise(final double x, final double y) { double n =
     * 0.0, i_s = 1.0, xx, yy; int s = 2 << (octaves - 1); for (int o = 0; o <
     * octaves; o++) { xx = x * (i_s *= 2.0) + (o << 6); yy = y * i_s + (o << 7); n
     * += basis.getNoise(xx, yy) * s + disturbance.getNoise(xx * s, yy * s) * (s >>=
     * 1); } return n / ((3 << octaves) - 3.0); }
     *
     * @Override public double getNoiseWithSeed(final double x, final double y,
     * final int seed) { double n = 0.0, i_s = 1.0, xx, yy;
     *
     * int s = 2 << (octaves - 1), seed2 = seed; for (int o = 0; o < octaves; o++) {
     * seed2 = PintRNG.determine(seed2); xx = x * (i_s *= 2.0) + (o << 6); yy = y *
     * i_s + (o << 7); n += basis.getNoiseWithSeed(xx, yy, seed2) * s +
     * disturbance.getNoiseWithSeed(xx * s, yy * s, seed2) * (s >>= 1); } return n /
     * ((3 << octaves) - 3.0); } } public static class Turbulent3D implements
     * Noise3D { protected int octaves; protected Noise3D basis, disturbance;
     *
     * public Turbulent3D() { this(SeededNoise.instance, alternate, 1); }
     *
     * public Turbulent3D(Noise3D basis, Noise3D disturb) { this(basis, disturb, 1);
     * }
     *
     * public Turbulent3D(Noise3D basis, Noise3D disturb, final int octaves) {
     * this.basis = basis; disturbance = disturb; this.octaves = Math.max(1,
     * Math.min(63, octaves)); }
     *
     * @Override public double getNoise(final double x, final double y, final double
     * z) { double n = 0.0, i_s = 1.0, xx, yy, zz; int s = 2 << (octaves - 1); for
     * (int o = 0; o < octaves; o++) { xx = x * (i_s *= 2.0) + (o << 6); yy = y *
     * i_s + (o << 7); zz = z * i_s + (o << 8); n += basis.getNoise(xx, yy, zz) * s
     * + disturbance.getNoise(xx * s, yy * s, zz * s) * (s >>= 1); } return n / ((3
     * << octaves) - 3.0); }
     *
     * @Override public double getNoiseWithSeed(final double x, final double y,
     * final double z, final int seed) { double n = 0.0, i_s = 1.0, xx, yy, zz; int
     * s = 2 << (octaves - 1), seed2 = seed; for (int o = 0; o < octaves; o++) {
     * seed2 = PintRNG.determine(seed2); xx = x * (i_s *= 2.0) + (o << 6); yy = y *
     * i_s + (o << 7); zz = z * i_s + (o << 8); n += basis.getNoiseWithSeed(xx, yy,
     * zz, seed2) * s + disturbance.getNoiseWithSeed(xx * s, yy * s, zz * s, seed2)
     * * (s >>= 1); } return n / ((3 << octaves) - 3.0); } }
     */

    public static class Turbulent2D implements Noise2D {
	protected int octaves;
	protected Noise2D basis, disturbance;
	public double frequency = 1.0;
	public final double correct;

	public Turbulent2D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Turbulent2D(final Noise2D basis, final Noise2D disturb) {
	    this(basis, disturb, 1);
	}

	public Turbulent2D(final Noise2D basis, final Noise2D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Turbulent2D(final Noise2D basis, final Noise2D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	    this.correct = 1.0 / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoise(double x, double y) {
	    x += this.disturbance.getNoise(x, y);
	    x *= this.frequency;
	    y *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7)) * s;
	    }
	    return n * this.correct;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, int seed) {
	    x += this.disturbance.getNoiseWithSeed(x, y, seed);
	    x *= this.frequency;
	    y *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoiseWithSeed(x * i_s, y * i_s, seed += 0x9E3779B9) * s;
	    }
	    return n * this.correct;
	}
    }

    public static class Turbulent3D implements Noise3D {
	protected int octaves;
	protected Noise3D basis, disturbance;
	public double frequency = 1.0;
	public final double correct;

	public Turbulent3D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Turbulent3D(final Noise3D basis, final Noise3D disturb) {
	    this(basis, disturb, 1);
	}

	public Turbulent3D(final Noise3D basis, final Noise3D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Turbulent3D(final Noise3D basis, final Noise3D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	    this.correct = 1.0 / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoise(double x, double y, double z) {
	    x += this.disturbance.getNoise(x, y, z);
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
	    }
	    return n * this.correct;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, int seed) {
	    x += this.disturbance.getNoiseWithSeed(x, y, z, seed);
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoiseWithSeed(x * i_s, y * i_s, z * i_s, seed += 0x9E3779B9) * s;
	    }
	    return n * this.correct;
	}
    }

    public static class Turbulent4D implements Noise4D {
	protected int octaves;
	protected Noise4D basis, disturbance;
	public double frequency = 1.0;
	public final double correct;

	public Turbulent4D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Turbulent4D(final Noise4D basis, final Noise4D disturb) {
	    this(basis, disturb, 1);
	}

	public Turbulent4D(final Noise4D basis, final Noise4D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Turbulent4D(final Noise4D basis, final Noise4D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	    this.correct = 1.0 / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoise(double x, double y, double z, double w) {
	    x += this.disturbance.getNoise(x, y, z, w);
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9))
			* s;
	    }
	    return n * this.correct;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
	    x += this.disturbance.getNoiseWithSeed(x, y, z, w, seed);
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoiseWithSeed(x * i_s, y * i_s, z * i_s, w * i_s, seed += 0x9E3779B9) * s;
	    }
	    return n * this.correct;
	}
    }

    public static class Turbulent6D implements Noise6D {
	protected int octaves;
	protected Noise6D basis, disturbance;
	public double frequency = 1.0;
	public final double correct;

	public Turbulent6D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Turbulent6D(final Noise6D basis, final Noise6D disturb) {
	    this(basis, disturb, 1);
	}

	public Turbulent6D(final Noise6D basis, final Noise6D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Turbulent6D(final Noise6D basis, final Noise6D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	    this.correct = 1.0 / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoise(double x, double y, double z, double w, double u, double v) {
	    x += this.disturbance.getNoise(x, y, z, w, u, v);
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9),
			u * i_s + (o << 10), v * i_s + (o << 11)) * s;
	    }
	    return n * this.correct;
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
	    x += this.disturbance.getNoiseWithSeed(x, y, z, w, u, v, seed);
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    int s = 1 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		i_s *= 0.5;
		n += this.basis.getNoiseWithSeed(x * i_s, y * i_s, z * i_s, w * i_s, u * i_s, v * i_s,
			seed += 0x9E3779B9) * s;
	    }
	    return n * this.correct;
	}
    }

    public static class Viny2D implements Noise2D {
	protected int octaves;
	protected Noise2D basis, disturbance;
	public double frequency = 1.0;

	public Viny2D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Viny2D(final Noise2D basis, final Noise2D disturb) {
	    this(basis, disturb, 1);
	}

	public Viny2D(final Noise2D basis, final Noise2D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Viny2D(final Noise2D basis, final Noise2D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y) {
	    x *= this.frequency;
	    y *= this.frequency;
	    int s = 2 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0 / s, xx, yy;
	    for (int o = 0; o < this.octaves; o++) {
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		n += this.basis.getNoise(xx, yy) * s + this.disturbance.getNoise(xx, yy) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    int s = 2 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 1.0 / s, xx, yy;
	    for (int o = 0; o < this.octaves; o++) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		n += this.basis.getNoiseWithSeed(xx, yy, seed2) * s
			+ this.disturbance.getNoiseWithSeed(xx, yy, seed2) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}
    }

    public static class Viny3D implements Noise3D {
	protected int octaves;
	protected Noise3D basis, disturbance;
	public double frequency = 1.0;

	public Viny3D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Viny3D(final Noise3D basis, final Noise3D disturb) {
	    this(basis, disturb, 1);
	}

	public Viny3D(final Noise3D basis, final Noise3D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Viny3D(final Noise3D basis, final Noise3D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y, double z) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    int s = 2 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0 / s, xx, yy, zz;
	    for (int o = 0; o < this.octaves; o++) {
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		n += this.basis.getNoise(xx, yy, zz) * s + this.disturbance.getNoise(xx, yy, zz) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    int s = 2 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 1.0 / s, xx, yy, zz;
	    for (int o = 0; o < this.octaves; o++) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		n += this.basis.getNoiseWithSeed(xx, yy, zz, seed2) * s
			+ this.disturbance.getNoiseWithSeed(xx, yy, zz, seed2) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}
    }

    public static class Viny4D implements Noise4D {
	protected int octaves;
	protected Noise4D basis, disturbance;
	public double frequency = 1.0;

	public Viny4D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Viny4D(final Noise4D basis, final Noise4D disturb) {
	    this(basis, disturb, 1);
	}

	public Viny4D(final Noise4D basis, final Noise4D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Viny4D(final Noise4D basis, final Noise4D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y, double z, double w) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    int s = 2 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww;
	    for (int o = 0; o < this.octaves; o++) {
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		n += this.basis.getNoise(xx, yy, zz, ww) * s + this.disturbance.getNoise(xx, yy, zz, ww) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    int s = 2 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww;
	    for (int o = 0; o < this.octaves; o++) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		n += this.basis.getNoiseWithSeed(xx, yy, zz, ww, seed2) * s
			+ this.disturbance.getNoiseWithSeed(xx, yy, zz, ww, seed2) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}
    }

    public static class Viny6D implements Noise6D {
	protected int octaves;
	protected Noise6D basis, disturbance;
	public double frequency = 1.0;

	public Viny6D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Viny6D(final Noise6D basis, final Noise6D disturb) {
	    this(basis, disturb, 1);
	}

	public Viny6D(final Noise6D basis, final Noise6D disturb, final int octaves) {
	    this(basis, disturb, octaves, 1.0);
	}

	public Viny6D(final Noise6D basis, final Noise6D disturb, final int octaves, final double frequency) {
	    this.basis = basis;
	    this.frequency = frequency;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(double x, double y, double z, double w, double u, double v) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    int s = 2 << this.octaves - 1;
	    double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww, uu, vv;
	    for (int o = 0; o < this.octaves; o++) {
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		uu = u * i_s + (o << 10);
		vv = v * i_s + (o << 11);
		n += this.basis.getNoise(xx, yy, zz, ww, uu, vv) * s
			+ this.disturbance.getNoise(xx, yy, zz, ww, uu, vv) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, final int seed) {
	    x *= this.frequency;
	    y *= this.frequency;
	    z *= this.frequency;
	    w *= this.frequency;
	    u *= this.frequency;
	    v *= this.frequency;
	    int s = 2 << this.octaves - 1, seed2 = seed;
	    double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww, uu, vv;
	    for (int o = 0; o < this.octaves; o++) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 2.0) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		uu = u * i_s + (o << 10);
		vv = v * i_s + (o << 11);
		n += this.basis.getNoiseWithSeed(xx, yy, zz, ww, uu, vv, seed2) * s
			+ this.disturbance.getNoiseWithSeed(xx, yy, zz, ww, uu, vv, seed2) * (s >>= 1);
	    }
	    return n / ((3 << this.octaves) - 3.0);
	}
    }

    public static class Slick2D implements Noise2D {
	protected int octaves;
	protected Noise2D basis, disturbance;

	public Slick2D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Slick2D(final Noise2D basis, final Noise2D disturb) {
	    this(basis, disturb, 1);
	}

	public Slick2D(final Noise2D basis, final Noise2D disturb, final int octaves) {
	    this.basis = basis;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(final double x, final double y) {
	    double n = 0.0, i_s = 1.0, xx, yy;
	    int s = 1 << this.octaves - 1;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		n += this.basis.getNoise(xx + this.disturbance.getNoise(x, y), yy) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final int seed) {
	    double n = 0.0, i_s = 1.0, xx, yy;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		n += this.basis.getNoiseWithSeed(xx + this.disturbance.getNoiseWithSeed(x, y, seed2), yy, seed2) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Slick3D implements Noise3D {
	protected int octaves;
	protected Noise3D basis, disturbance;

	public Slick3D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Slick3D(final Noise3D basis, final Noise3D disturb) {
	    this(basis, disturb, 1);
	}

	public Slick3D(final Noise3D basis, final Noise3D disturb, final int octaves) {
	    this.basis = basis;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(final double x, final double y, final double z) {
	    double n = 0.0, i_s = 1.0, xx, yy, zz;
	    int s = 1 << this.octaves - 1;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		n += this.basis.getNoise(xx + this.disturbance.getNoise(x, y, z), yy, zz) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
	    double n = 0.0, i_s = 1.0, xx, yy, zz;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		n += this.basis.getNoiseWithSeed(xx + this.disturbance.getNoiseWithSeed(x, y, z, seed2), yy, zz, seed2)
			* s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Slick4D implements Noise4D {
	protected int octaves;
	protected Noise4D basis, disturbance;

	public Slick4D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Slick4D(final Noise4D basis, final Noise4D disturb) {
	    this(basis, disturb, 1);
	}

	public Slick4D(final Noise4D basis, final Noise4D disturb, final int octaves) {
	    this.basis = basis;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(final double x, final double y, final double z, final double w) {
	    double n = 0.0, i_s = 1.0, xx, yy, zz, ww;
	    int s = 1 << this.octaves - 1;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		n += this.basis.getNoise(xx + this.disturbance.getNoise(x, y, z, w), yy, zz, ww) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
	    double n = 0.0, i_s = 1.0, xx, yy, zz, ww;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		n += this.basis.getNoiseWithSeed(xx + this.disturbance.getNoiseWithSeed(x, y, z, w, seed2), yy, zz, ww,
			seed2) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    public static class Slick6D implements Noise6D {
	protected int octaves;
	protected Noise6D basis, disturbance;

	public Slick6D() {
	    this(SeededNoise.instance, Noise.alternate, 1);
	}

	public Slick6D(final Noise6D basis, final Noise6D disturb) {
	    this(basis, disturb, 1);
	}

	public Slick6D(final Noise6D basis, final Noise6D disturb, final int octaves) {
	    this.basis = basis;
	    this.disturbance = disturb;
	    this.octaves = Math.max(1, Math.min(63, octaves));
	}

	@Override
	public double getNoise(final double x, final double y, final double z, final double w, final double u,
		final double v) {
	    double n = 0.0, i_s = 1.0, xx, yy, zz, ww, uu, vv;
	    int s = 1 << this.octaves - 1;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		uu = u * i_s + (o << 10);
		vv = v * i_s + (o << 11);
		n += this.basis.getNoise(xx + this.disturbance.getNoise(x, y, z, w, u, v), yy, zz, ww, uu, vv) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}

	@Override
	public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u,
		final double v, final int seed) {
	    double n = 0.0, i_s = 1.0, xx, yy, zz, ww, uu, vv;
	    int s = 1 << this.octaves - 1, seed2 = seed;
	    for (int o = 0; o < this.octaves; o++, s >>= 1) {
		seed2 = PintRNG.determine(seed2);
		xx = x * (i_s *= 0.5) + (o << 6);
		yy = y * i_s + (o << 7);
		zz = z * i_s + (o << 8);
		ww = w * i_s + (o << 9);
		uu = u * i_s + (o << 10);
		vv = v * i_s + (o << 11);
		n += this.basis.getNoiseWithSeed(xx + this.disturbance.getNoiseWithSeed(x, y, z, w, u, v, seed2), yy,
			zz, ww, uu, vv, seed2) * s;
	    }
	    return n / ((1 << this.octaves) - 1.0);
	}
    }

    /**
     * Produces a 2D array of noise with values from -1.0 to 1.0 that is seamless on
     * all boundaries. Uses (x,y) order. Allows a seed to change the generated
     * noise. If you need to call this very often, consider
     * {@link #seamless2D(double[][], int, int)}, which re-uses the array.
     *
     * @param width   the width of the array to produce (the length of the outer
     *                layer of arrays)
     * @param height  the height of the array to produce (the length of the inner
     *                arrays)
     * @param seed    an int seed that affects the noise produced, with different
     *                seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise
     *                generations to apply to the same area
     * @return a freshly-allocated seamless-bounded array, a
     *         {@code double[width][height]}.
     */
    public static double[][] seamless2D(final int width, final int height, final int seed, final int octaves) {
	return Noise.seamless2D(new double[width][height], seed, octaves);
    }

    /**
     * Fills the given 2D array (modifying it) with noise, using values from -1.0 to
     * 1.0, that is seamless on all boundaries. This overload doesn't care what you
     * use for x or y axes, it uses the exact size of fill fully. Allows a seed to
     * change the generated noise.
     *
     * @param fill    a 2D array of double; must be rectangular, so it's a good idea
     *                to create with {@code new double[width][height]} or something
     *                similar
     * @param seed    an int seed that affects the noise produced, with different
     *                seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise
     *                generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][] seamless2D(final double[][] fill, final int seed, final int octaves) {
	return Noise.seamless2D(fill, seed, octaves, SeededNoise.instance);
    }

    public static double total = 0.0;

    /**
     * Fills the given 2D array (modifying it) with noise, using values from -1.0 to
     * 1.0, that is seamless on all boundaries. This overload doesn't care what you
     * use for x or y axes, it uses the exact size of fill fully. Allows a seed to
     * change the generated noise. DOES NOT clear the values in fill, so if it
     * already has non-zero elements, the result will be different than if it had
     * been cleared beforehand. That does allow you to utilize this method to add
     * multiple seamless noise values on top of each other, though that allows
     * values to go above or below the normal minimum and maximum (-1.0 to 1.0).
     *
     * @param fill    a 2D array of double; must be rectangular, so it's a good idea
     *                to create with {@code new double[width][height]} or something
     *                similar
     * @param seed    an int seed that affects the noise produced, with different
     *                seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise
     *                generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][] seamless2D(final double[][] fill, final int seed, final int octaves,
	    final Noise.Noise4D generator) {
	final int height, width;
	if (fill == null || (width = fill.length) <= 0 || (height = fill[0].length) <= 0 || octaves <= 0
		|| octaves >= 63) {
	    return fill;
	}
	final double i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height;
	int s = 1 << octaves - 1, seed2 = seed;
	Noise.total = 0.0;
	double p, q, ps, pc, qs, qc, i_s = 0.5 / s;
	for (int o = 0; o < octaves; o++, s >>= 1) {
	    seed2 = PintRNG.determine(seed2);
	    i_s *= 2.0;
	    for (int x = 0; x < width; x++) {
		p = x * i_w;
		ps = Math.sin(p) * i_s;
		pc = Math.cos(p) * i_s;
		for (int y = 0; y < height; y++) {
		    q = y * i_h;
		    qs = Math.sin(q) * i_s;
		    qc = Math.cos(q) * i_s;
		    fill[x][y] += generator.getNoiseWithSeed(pc, ps, qc, qs, seed2) * s;
		}
	    }
	}
	// if(octaves > 1) {
	i_s = 1.0 / ((1 << octaves) - 1.0);
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		Noise.total += fill[x][y] *= i_s;
	    }
	}
	// }
	return fill;
    }

    /**
     * Produces a 3D array of noise with values from -1.0 to 1.0 that is seamless on
     * all boundaries. Allows a seed to change the generated noise. Because most
     * games that would use this would use it for maps, and maps are often top-down,
     * the returned 3D array uses the order (z,x,y), which allows a 2D slice of x
     * and y to be taken as an element from the top-level array. If you need to call
     * this very often, consider {@link #seamless3D(double[][][], int, int)}, which
     * re-uses the array instead of re-generating it.
     *
     * @param width   the width of the array to produce (the length of the middle
     *                layer of arrays)
     * @param height  the height of the array to produce (the length of the
     *                innermost arrays)
     * @param depth   the depth of the array to produce (the length of the outermost
     *                layer of arrays)
     * @param seed    an int seed that affects the noise produced, with different
     *                seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise
     *                generations to apply to the same area
     * @return a freshly-allocated seamless-bounded array, a
     *         {@code double[depth][width][height]}.
     */
    public static double[][][] seamless3D(final int depth, final int width, final int height, final int seed,
	    final int octaves) {
	return Noise.seamless3D(new double[depth][width][height], seed, octaves);
    }

    /**
     * Fills the given 3D array (modifying it) with noise, using values from -1.0 to
     * 1.0, that is seamless on all boundaries. This overload doesn't care what you
     * use for x, y, or z axes, it uses the exact size of fill fully. Allows a seed
     * to change the generated noise.
     *
     * @param fill    a 3D array of double; must be rectangular, so it's a good idea
     *                to create with {@code new double[depth][width][height]} or
     *                something similar
     * @param seed    an int seed that affects the noise produced, with different
     *                seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise
     *                generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][][] seamless3D(final double[][][] fill, final int seed, final int octaves) {
	return Noise.seamless3D(fill, seed, octaves, SeededNoise.instance);
    }

    /**
     * Fills the given 3D array (modifying it) with noise, using values from -1.0 to
     * 1.0, that is seamless on all boundaries. This overload doesn't care what you
     * use for x, y, or z axes, it uses the exact size of fill fully. Allows a seed
     * to change the generated noise.
     *
     * @param fill    a 3D array of double; must be rectangular, so it's a good idea
     *                to create with {@code new double[depth][width][height]} or
     *                something similar
     * @param seed    an int seed that affects the noise produced, with different
     *                seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise
     *                generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][][] seamless3D(final double[][][] fill, final int seed, final int octaves,
	    final Noise.Noise6D generator) {
	final int depth, height, width;
	if (fill == null || (depth = fill.length) <= 0 || (width = fill[0].length) <= 0
		|| (height = fill[0][0].length) <= 0 || octaves <= 0 || octaves >= 63) {
	    return fill;
	}
	final double i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height, i_d = 6.283185307179586 / depth;
	int s = 1 << octaves - 1, seed2 = seed;
	Noise.total = 0.0;
	double p, q, r, ps, pc, qs, qc, rs, rc, i_s = 0.5 / s;
	for (int o = 0; o < octaves; o++, s >>= 1) {
	    seed2 = PintRNG.determine(seed2);
	    i_s *= 2.0;
	    for (int x = 0; x < width; x++) {
		p = x * i_w;
		ps = Math.sin(p) * i_s;
		pc = Math.cos(p) * i_s;
		for (int y = 0; y < height; y++) {
		    q = y * i_h;
		    qs = Math.sin(q) * i_s;
		    qc = Math.cos(q) * i_s;
		    for (int z = 0; z < depth; z++) {
			r = z * i_d;
			rs = Math.sin(r) * i_s;
			rc = Math.cos(r) * i_s;
			fill[z][x][y] += generator.getNoiseWithSeed(pc, ps, qc, qs, rc, rs, seed2) * s;
		    }
		}
	    }
	}
	i_s = 1.0 / ((1 << octaves) - 1.0);
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		for (int z = 0; z < depth; z++) {
		    Noise.total += fill[z][x][y] *= i_s;
		}
	    }
	}
	return fill;
    }

    /**
     * Fills the given 3D array (modifying it) with noise, using values from -1.0 to
     * 1.0, that is seamless on all boundaries. This overload doesn't care what you
     * use for x, y, or z axes, it uses the exact size of fill fully. Allows a seed
     * to change the generated noise.
     *
     * @param fill    a 3D array of double; must be rectangular, so it's a good idea
     *                to create with {@code new double[depth][width][height]} or
     *                something similar
     * @param seed    an int seed that affects the noise produced, with different
     *                seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise
     *                generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static float[][][] seamless3D(final float[][][] fill, final int seed, final int octaves) {
	final int depth, height, width;
	if (fill == null || (depth = fill.length) <= 0 || (width = fill[0].length) <= 0
		|| (height = fill[0][0].length) <= 0 || octaves <= 0 || octaves >= 63) {
	    return fill;
	}
	final double i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height, i_d = 6.283185307179586 / depth;
	int s = 1 << octaves - 1, seed2 = seed;
	double p, q, r, ps, pc, qs, qc, rs, rc, i_s = 0.5 / s;
	for (int o = 0; o < octaves; o++, s >>= 1) {
	    seed2 = PintRNG.determine(seed2);
	    i_s *= 2.0;
	    for (int x = 0; x < width; x++) {
		p = x * i_w;
		ps = Math.sin(p) * i_s;
		pc = Math.cos(p) * i_s;
		for (int y = 0; y < height; y++) {
		    q = y * i_h;
		    qs = Math.sin(q) * i_s;
		    qc = Math.cos(q) * i_s;
		    for (int z = 0; z < depth; z++) {
			r = z * i_d;
			rs = Math.sin(r) * i_s;
			rc = Math.cos(r) * i_s;
			fill[z][x][y] += SeededNoise.noise(pc, ps, qc, qs, rc, rs, seed2) * s;
		    }
		}
	    }
	}
	i_s = 1.0 / ((1 << octaves) - 1.0);
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		for (int z = 0; z < depth; z++) {
		    fill[z][x][y] *= i_s;
		}
	    }
	}
	return fill;
    }
}

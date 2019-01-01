package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * A different kind of noise that has spotted and striped areas, like a tabby
 * cat. Highly experimental and expected to change; currently has significant
 * spiral-shaped artifacts from stretching. Created by Tommy Ettinger on
 * 9/2/2017.
 */
@Beta
public class TabbyNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    public static final TabbyNoise instance = new TabbyNoise();
    public long seedX, seedY, seedZ, seedW, seedU, seedV;
    public double randX, randY, randZ, randW, randU, randV;

    public TabbyNoise() {
	this(0x1337BEEF);
    }

    public TabbyNoise(final int seed) {
	this.randX = TabbyNoise.gauss(this.seedX = ThrustRNG.determine(seed + 0xC6BC279692B5CC83L)) + 0.625;
	this.randY = TabbyNoise.gauss(this.seedY = ThrustRNG.determine(this.seedX + 0xC7BC279692B5CC83L)) + 0.625;
	this.randZ = TabbyNoise.gauss(this.seedZ = ThrustRNG.determine(this.seedY + 0xC8BC279692B5CC83L)) + 0.625;
	this.randW = TabbyNoise.gauss(this.seedW = ThrustRNG.determine(this.seedZ + 0xC9BC279692B5CC83L)) + 0.625;
	this.randU = TabbyNoise.gauss(this.seedU = ThrustRNG.determine(this.seedW + 0xCABC279692B5CC83L)) + 0.625;
	this.randV = TabbyNoise.gauss(this.seedV = ThrustRNG.determine(this.seedU + 0xCBBC279692B5CC83L)) + 0.625;
    }

    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0
     * (yields start) and 1 (yields end). Will smoothly transition toward start or
     * end as a approaches 0 or 1, respectively.
     *
     * @param start a valid float
     *
     * @param end a valid float
     *
     * @param a a float between 0 and 1 inclusive
     *
     * @return a float between x and y inclusive
     */
    public static double querp(final double start, final double end, double a) {
	return (1.0 - (a *= a * a * (a * (a * 6.0 - 15.0) + 10.0))) * start + a * end;
    }

    /**
     * Like {@link Math#floor}, but returns an int. Doesn't consider weird doubles
     * like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as an int
     */
    public static long fastFloor(final double t) {
	return t >= 0 ? (long) t : (long) t - 1;
    }

    public static double gauss(final long state) {
	final long s1 = state + 0x9E3779B97F4A7C15L, s2 = s1 + 0x9E3779B97F4A7C15L,
		y = (s1 ^ s1 >>> 30) * 0x5851F42D4C957F2DL, z = (s2 ^ s2 >>> 30) * 0x5851F42D4C957F2DL;
	return (((y ^ y >>> 28) & 0x7FFFFFL) + ((y ^ y >>> 28) >>> 41) + ((z ^ z >>> 28) & 0x7FFFFFL)
		+ ((z ^ z >>> 28) >>> 41)) * 0x1p-24 - 1.0;
    }

    @Override
    public double getNoise(final double x, final double y) {
	return this.getNoiseWithSeeds(x, y, this.seedX, this.seedY, this.randX, this.randY);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final int seed) {
	final long rs = ThrustRNG.determine(seed ^ (long) ~seed << 32), rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
		ry = (rx >>> 23 ^ rx << 23) * (rx | 1L);
	return this.getNoiseWithSeeds(x, y, rx, ry, TabbyNoise.gauss(rx) + 0.625, TabbyNoise.gauss(ry) + 0.625);
    }

    public double getNoiseWithSeeds(final double x, final double y, final long seedX, final long seedY,
	    final double randX, final double randY) {
	final double grx = randX * 0.625, gry = randY * 0.625, cx = NumberTools.zigzag(x) * (gry + 1.125) * 0x0.93p-1,
		cy = NumberTools.zigzag(y) * (grx + 1.125) * 0x0.93p-1, ax = x + cy * (0.35 + gry),
		ay = y + cx * (0.35 + grx),
		mx = ((seedX & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ax
			+ ((seedX & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ay,
		my = ((seedY & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ay
			+ ((seedY & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ax;
	final long xf = TabbyNoise.fastFloor(mx), yf = TabbyNoise.fastFloor(my);
	return NumberTools.bounce(5.875 + 2.0625 * (TabbyNoise.querp(TabbyNoise.gauss(xf * 0xAE3779B97F4A7E35L),
		TabbyNoise.gauss((xf + 1) * 0xAE3779B97F4A7E35L), mx - xf)
		+ TabbyNoise.querp(TabbyNoise.gauss(yf * 0xBE3779B97F4A7C55L),
			TabbyNoise.gauss((yf + 1) * 0xBE3779B97F4A7C55L), my - yf)));
    }

    @Override
    public double getNoise(final double x, final double y, final double z) {
	return this.getNoiseWithSeeds(x, y, z, this.seedX, this.seedY, this.seedZ, this.randX, this.randY, this.randZ);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
	final long rs = ThrustRNG.determine(seed ^ (long) ~seed << 32), rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
		ry = (rx >>> 23 ^ rx << 23) * (rx | 1L), rz = (ry >>> 23 ^ ry << 23) * (ry | 1L);
	return this.getNoiseWithSeeds(x, y, z, rx, ry, rz, TabbyNoise.gauss(rx) + 0.625, TabbyNoise.gauss(ry) + 0.625,
		TabbyNoise.gauss(rz) + 0.625);
    }

    public double getNoiseWithSeeds(final double x, final double y, final double z, final long seedX, final long seedY,
	    final long seedZ, final double randX, final double randY, final double randZ) {
	final double cx = NumberTools.zigzag(x) * (randY * randZ + 1.125) * 0x0.93p-1,
		cy = NumberTools.zigzag(y) * (randZ * randX + 1.125) * 0x0.93p-1,
		cz = NumberTools.zigzag(z) * (randX * randY + 1.125) * 0x0.93p-1, ax = x + cy * cz, ay = y + cz * cx,
		az = z + cx * cy,
		mx = ((seedX & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ax
			+ ((seedX & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ay
			+ ((seedX & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * az,
		my = ((seedY & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ay
			+ ((seedY & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * az
			+ ((seedY & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * ax,
		mz = ((seedZ & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * az
			+ ((seedZ & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ax
			+ ((seedZ & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * ay;
	final long xf = TabbyNoise.fastFloor(mx), yf = TabbyNoise.fastFloor(my), zf = TabbyNoise.fastFloor(mz);
	return NumberTools.bounce(5.875 + 2.0625 * (TabbyNoise.querp(TabbyNoise.gauss(xf * 0xAE3779B97F4A7E35L),
		TabbyNoise.gauss((xf + 1) * 0xAE3779B97F4A7E35L), mx - xf)
		+ TabbyNoise.querp(TabbyNoise.gauss(yf * 0xBE3779B97F4A7C55L),
			TabbyNoise.gauss((yf + 1) * 0xBE3779B97F4A7C55L), my - yf)
		+ TabbyNoise.querp(TabbyNoise.gauss(zf * 0xCE3779B97F4A7A75L),
			TabbyNoise.gauss((zf + 1) * 0xCE3779B97F4A7A75L), mz - zf)));
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
	return this.getNoiseWithSeeds(x, y, z, w, this.seedX, this.seedY, this.seedZ, this.seedW, this.randX,
		this.randY, this.randZ, this.randW);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
	final long rs = ThrustRNG.determine(seed ^ (long) ~seed << 32), rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
		ry = (rx >>> 23 ^ rx << 23) * (rx | 1L), rz = (ry >>> 23 ^ ry << 23) * (ry | 1L),
		rw = (rz >>> 23 ^ rz << 23) * (rz | 1L);
	return this.getNoiseWithSeeds(x, y, z, w, rx, ry, rz, rw, TabbyNoise.gauss(rx) + 0.625,
		TabbyNoise.gauss(ry) + 0.625, TabbyNoise.gauss(rz) + 0.625, TabbyNoise.gauss(rw) + 0.625);
    }

    public double getNoiseWithSeeds(final double x, final double y, final double z, final double w, final long seedX,
	    final long seedY, final long seedZ, final long seedW, final double randX, final double randY,
	    final double randZ, final double randW) {
	final double cx = NumberTools.zigzag(x) * (randY * randZ + 1.125) * 0x0.93p-1,
		cy = NumberTools.zigzag(y) * (randZ * randW + 1.125) * 0x0.93p-1,
		cz = NumberTools.zigzag(z) * (randW * randX + 1.125) * 0x0.93p-1,
		cw = NumberTools.zigzag(w) * (randX * randY + 1.125) * 0x0.93p-1, ax = x + cz * cw, ay = y + cw * cx,
		az = z + cx * cy, aw = w + cy * cz,
		mx = ((seedX & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ax
			+ ((seedX & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * aw
			+ ((seedX & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * az
			+ ((seedX & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * ay,
		my = ((seedY & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ay
			+ ((seedY & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ax
			+ ((seedY & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * aw
			+ ((seedY & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * az,
		mz = ((seedZ & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * az
			+ ((seedZ & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ay
			+ ((seedZ & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * ax
			+ ((seedZ & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * aw,
		mw = ((seedW & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * aw
			+ ((seedW & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * az
			+ ((seedW & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * ay
			+ ((seedW & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * ax;
	final long xf = TabbyNoise.fastFloor(mx), yf = TabbyNoise.fastFloor(my), zf = TabbyNoise.fastFloor(mz),
		wf = TabbyNoise.fastFloor(mw);
	return NumberTools.bounce(5.875 + 2.0625 * (TabbyNoise.querp(TabbyNoise.gauss(xf * 0xAE3779B97F4A7E35L),
		TabbyNoise.gauss((xf + 1) * 0xAE3779B97F4A7E35L), mx - xf)
		+ TabbyNoise.querp(TabbyNoise.gauss(yf * 0xBE3779B97F4A7C55L),
			TabbyNoise.gauss((yf + 1) * 0xBE3779B97F4A7C55L), my - yf)
		+ TabbyNoise.querp(TabbyNoise.gauss(zf * 0xCE3779B97F4A7A75L),
			TabbyNoise.gauss((zf + 1) * 0xCE3779B97F4A7A75L), mz - zf)
		+ TabbyNoise.querp(TabbyNoise.gauss(wf * 0xDE3779B97F4A7895L),
			TabbyNoise.gauss((wf + 1) * 0xDE3779B97F4A7895L), mw - wf)));
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u,
	    final double v) {
	return this.getNoiseWithSeeds(x, y, z, w, u, v, this.seedX, this.seedY, this.seedZ, this.seedW, this.seedU,
		this.seedV, this.randX, this.randY, this.randZ, this.randW, this.randU, this.randV);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u,
	    final double v, final int seed) {
	final long rs = ThrustRNG.determine(seed ^ (long) ~seed << 32), rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
		ry = (rx >>> 23 ^ rx << 23) * (rx | 1L), rz = (ry >>> 23 ^ ry << 23) * (ry | 1L),
		rw = (rz >>> 23 ^ rz << 23) * (rz | 1L), ru = (rw >>> 23 ^ rw << 23) * (rw | 1L),
		rv = (ru >>> 23 ^ ru << 23) * (ru | 1L);
	return this.getNoiseWithSeeds(x, y, z, w, u, v, rx, ry, rz, rw, ru, rv, TabbyNoise.gauss(rx) + 0.625,
		TabbyNoise.gauss(ry) + 0.625, TabbyNoise.gauss(rz) + 0.625, TabbyNoise.gauss(rw) + 0.625,
		TabbyNoise.gauss(ru) + 0.625, TabbyNoise.gauss(rv) + 0.625);
    }

    public double getNoiseWithSeeds(final double x, final double y, final double z, final double w, final double u,
	    final double v, final long seedX, final long seedY, final long seedZ, final long seedW, final long seedU,
	    final long seedV, final double randX, final double randY, final double randZ, final double randW,
	    final double randU, final double randV) {
	final double cx = NumberTools.zigzag(x) * (randY * randZ + 1.125) * 0x0.93p-1,
		cy = NumberTools.zigzag(y) * (randZ * randW + 1.125) * 0x0.93p-1,
		cz = NumberTools.zigzag(z) * (randW * randU + 1.125) * 0x0.93p-1,
		cw = NumberTools.zigzag(w) * (randU * randV + 1.125) * 0x0.93p-1,
		cu = NumberTools.zigzag(u) * (randV * randX + 1.125) * 0x0.93p-1,
		cv = NumberTools.zigzag(v) * (randX * randY + 1.125) * 0x0.93p-1, ax = x + cw * cu, ay = y + cu * cv,
		az = z + cv * cx, aw = w + cx * cy, au = u + cy * cz, av = v + cz * cw,
		mx = ((seedX & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ax
			+ ((seedX & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * av
			+ ((seedX & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * au
			+ ((seedX & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * aw
			+ ((seedX & 0x1F000000000L | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40 * az
			+ ((seedX & 0x1F00000000000L | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48 * ay,
		my = ((seedY & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * ay
			+ ((seedY & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ax
			+ ((seedY & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * av
			+ ((seedY & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * au
			+ ((seedY & 0x1F000000000L | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40 * aw
			+ ((seedY & 0x1F00000000000L | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48 * az,
		mz = ((seedZ & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * az
			+ ((seedZ & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * ay
			+ ((seedZ & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * ax
			+ ((seedZ & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * av
			+ ((seedZ & 0x1F000000000L | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40 * au
			+ ((seedZ & 0x1F00000000000L | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48 * aw,
		mw = ((seedW & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * aw
			+ ((seedW & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * az
			+ ((seedW & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * ay
			+ ((seedW & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * ax
			+ ((seedW & 0x1F000000000L | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40 * av
			+ ((seedW & 0x1F00000000000L | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48 * au,
		mu = ((seedU & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * au
			+ ((seedU & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * aw
			+ ((seedU & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * az
			+ ((seedU & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * ay
			+ ((seedU & 0x1F000000000L | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40 * ax
			+ ((seedU & 0x1F00000000000L | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48 * av,
		mv = ((seedV & 0x1F0L | 0x2FL) - 0x1FFp-1) * 0x1.4p-8 * av
			+ ((seedV & 0x1F000L | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16 * au
			+ ((seedV & 0x1F00000L | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24 * aw
			+ ((seedV & 0x1F0000000L | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32 * az
			+ ((seedV & 0x1F000000000L | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40 * ay
			+ ((seedV & 0x1F00000000000L | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48 * ax;
	final long xf = TabbyNoise.fastFloor(mx), yf = TabbyNoise.fastFloor(my), zf = TabbyNoise.fastFloor(mz),
		wf = TabbyNoise.fastFloor(mw), uf = TabbyNoise.fastFloor(mu), vf = TabbyNoise.fastFloor(mv);
	return NumberTools.bounce(5.875 + 2.0625 * (TabbyNoise.querp(TabbyNoise.gauss(xf * 0xAE3779B97F4A7E35L),
		TabbyNoise.gauss((xf + 1) * 0xAE3779B97F4A7E35L), mx - xf)
		+ TabbyNoise.querp(TabbyNoise.gauss(yf * 0xBE3779B97F4A7C55L),
			TabbyNoise.gauss((yf + 1) * 0xBE3779B97F4A7C55L), my - yf)
		+ TabbyNoise.querp(TabbyNoise.gauss(zf * 0xCE3779B97F4A7A75L),
			TabbyNoise.gauss((zf + 1) * 0xCE3779B97F4A7A75L), mz - zf)
		+ TabbyNoise.querp(TabbyNoise.gauss(wf * 0xDE3779B97F4A7895L),
			TabbyNoise.gauss((wf + 1) * 0xDE3779B97F4A7895L), mw - wf)
		+ TabbyNoise.querp(TabbyNoise.gauss(uf * 0xEE3779B97F4A76B5L),
			TabbyNoise.gauss((uf + 1) * 0xEE3779B97F4A76B5L), mu - uf)
		+ TabbyNoise.querp(TabbyNoise.gauss(vf * 0xFE3779B97F4A74D5L),
			TabbyNoise.gauss((vf + 1) * 0xFE3779B97F4A74D5L), mv - vf)));
    }
}

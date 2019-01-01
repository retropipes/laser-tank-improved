/*
The MIT License(MIT)
Copyright(c) mxgmn 2016.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

The software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the software.
*/
package squidpony.squidgrid;

import squidpony.ArrayTools;
import squidpony.squidmath.IntDoubleOrderedMap;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

/**
 * Similar to MimicFill, this class can be used to imitate the style of an
 * existing piece of data, but this works on more than just booleans; it can
 * produce similar styles of texture (its original use in SynTex), of map, of
 * item placement, and so on by specifying a different technique for
 * differentiating two int values. <br>
 * Two options are now available; the process() method allows the slow analyze()
 * step to be computed once, before the rest of processing is potentially done
 * many times, but the new neoProcess() method produces comparable or better
 * results and is drastically faster even without needing analysis. This means
 * neoProcess(), based on P.F. Harrison's algorithm in SynTex, is strongly
 * recommended now. <br>
 * Example results of neoProcess(), with a procedural dungeon first and the
 * mimic versions after it:
 * https://gist.github.com/tommyettinger/405336fb0fc74838d806c021aabe77da (you
 * may need to click Raw and zoom out somewhat for it to render well). <br>
 * Ported from https://github.com/mxgmn/SynTex by Tommy Ettinger on 6/9/2016.
 */
public class DetailedMimic {
    /**
     * Constructor that uses an unseeded RNG and, without any instruction otherwise,
     * assumes the ints this is asked to compare are colors in RGBA8888 format. You
     * can specify your own implementation of the AestheticDifference interface (one
     * function) and pass it to other constructors, as well.
     */
    public DetailedMimic() {
	this(AestheticDifference.rgba8888);
    }

    /**
     * Constructor that uses an unseeded RNG (effectively a random seed) and the
     * given AestheticDifference. An example piece of code that implements an
     * AestheticDifference is available in the docs for
     * {@link AestheticDifference#difference(int, int)}; it is also considered a
     * functional interface if you use Java 8 or newer. You can also use the
     * ready-made implementation {@link AestheticDifference#rgba8888} if you have
     * int data that represents RGBA8888 colors, which can be obtained from libGDX
     * Colors or SColors in the display module.
     *
     * @param diff an implementation of the AestheticDifference interface, such as
     *             {@link AestheticDifference#rgba8888}; may be null, but that
     *             forces all calls to processing methods to treat discrete as true
     */
    public DetailedMimic(final AestheticDifference diff) {
	this(diff, new RNG());
    }

    /**
     * Constructor that uses the given RNG and the given AestheticDifference. An
     * example piece of code that implements an AestheticDifference is available in
     * the docs for {@link AestheticDifference#difference(int, int)}; it is also
     * considered a functional interface if you use Java 8 or newer. You can also
     * use the ready-made implementation {@link AestheticDifference#rgba8888} if you
     * have int data that represents RGBA8888 colors, which can be obtained from
     * libGDX Colors or SColors in the display module.
     *
     * @param diff an implementation of the AestheticDifference interface, such as
     *             {@link AestheticDifference#rgba8888}; may be null, but that
     *             forces all calls to processing methods to treat discrete as true
     * @param rng  an RNG to generate random factors; may be seeded to produce
     *             reliable output
     */
    public DetailedMimic(final AestheticDifference diff, final RNG rng) {
	this.random = rng;
	this.difference = diff;
	this.analyzed = null;
    }

    /**
     * DISCOURAGED; use
     * {@link #neoProcess(int[], int, int, int, int, int, int, boolean)} instead,
     * which doesn't need a separate analysis step. Analyzes a sample as a 1D int
     * array and stores the needed info to call
     * {@link #process(int[], int, int, int, int, int, int, double, boolean)} any
     * number of times later on without recalculating some heavy-weight information.
     *
     * @param sample      a 1D array of ints that can be compared by the
     *                    AestheticDifference this uses (or any ints if discrete is
     *                    true)
     * @param width       the width of the area in sample this should use (sample
     *                    can be interpreted as different shapes)
     * @param height      the height of the area in sample this should use (sample
     *                    can be interpreted as different shapes)
     * @param detailLevel how much detail to try for; if 0 or less this does
     *                    nothing, 2 works well in general
     * @param proximity   how far away to consider cells as affecting another; 3
     *                    works well
     * @param discrete    false if this can produce ints other than those in the
     *                    input; true if it uses a fixed set
     */
    public void analyze(final int[] sample, final int width, final int height, final int detailLevel,
	    final int proximity, boolean discrete) {
	discrete |= this.difference == null;
	if (detailLevel > 0) {
	    this.analysis(sample, width, height, detailLevel, proximity, discrete);
	}
    }

    /**
     * DISCOURAGED; use
     * {@link #neoProcess(int[], int, int, int, int, int, int, boolean)} instead,
     * which doesn't need a separate analysis step. Processes a sample as a 1D int
     * array and returns a different 1D int array that mimics the input. If the last
     * time this was called used the same sample, sampleWidth, and sampleHeight
     * parameters, or if {@link #analyze(int[], int, int, int, int, boolean)} was
     * called with its width equal to sampleWidth and its height equal to
     * sampleHeight, then this doesn't need to perform as many expensive
     * calculations.
     *
     * @param sample       a 1D array of ints that can be compared by the
     *                     AestheticDifference this uses (or any ints if discrete is
     *                     true)
     * @param sampleWidth  the width of the area in sample this should use (sample
     *                     can be interpreted as different shapes)
     * @param sampleHeight the height of the area in sample this should use (sample
     *                     can be interpreted as different shapes)
     * @param targetWidth  the desired width of the output
     * @param targetHeight the desired height of the output
     * @param detailLevel  how much detail to try for; if 0 or less this doesn't
     *                     perform analysis and has somewhat lower quality, but 2
     *                     works well in general
     * @param proximity    how far away to consider cells as affecting another; 3
     *                     works well
     * @param temperature  a level of unpredictability in the output relative to the
     *                     input; must be greater than 0
     * @param discrete     false if this can produce ints other than those in the
     *                     input; true if it uses a fixed set
     * @return a new 1D int array that can be interpreted as having targetWidth and
     *         targetHeight, and mimics sample
     */
    public int[] process(final int[] sample, final int sampleWidth, final int sampleHeight, final int targetWidth,
	    final int targetHeight, final int detailLevel, final int proximity, final double temperature,
	    boolean discrete) {
	discrete |= this.difference == null;
	if (detailLevel > 0) {
	    if (this.analyzed == null || this.analyzed.length != sampleWidth || this.analyzed.length == 0
		    || this.analyzed[0].length != sampleHeight) {
		this.analyze(sample, sampleWidth, sampleHeight, detailLevel, proximity, discrete);
	    }
	    return this.coherentSynthesis(sample, sampleWidth, sampleHeight, this.analyzed, proximity, targetWidth,
		    targetHeight, temperature, discrete);
	} else {
	    return this.fullSynthesis(sample, sampleWidth, sampleHeight, proximity, targetWidth, targetHeight,
		    temperature, discrete);
	}
    }

    /**
     * Processes a 1D int array representing 2D storage of values that can be
     * compared by this object's AestheticDifference (or any values if that is null
     * or discrete is true), and returns a 1D array representing data with
     * potentially different dimensions but similar appearance to sample.
     *
     * @param sample       a 1D array of ints that can be compared by the
     *                     AestheticDifference this uses (or any ints if discrete is
     *                     true)
     * @param sampleWidth  the width of the area in sample this should use (sample
     *                     can be interpreted as different shapes)
     * @param sampleHeight the height of the area in sample this should use (sample
     *                     can be interpreted as different shapes)
     * @param targetWidth  the desired width of the output
     * @param targetHeight the desired height of the output
     * @param detailLevel  how much detail to try for; here this will always be
     *                     treated as at least 1
     * @param proximity    how far away to consider cells as affecting another; 3
     *                     works well
     * @param discrete     false if this can produce ints other than those in the
     *                     input; true if it uses a fixed set
     * @return a new 1D int array that can be interpreted as having targetWidth and
     *         targetHeight, and mimics sample
     */
    public int[] neoProcess(final int[] sample, final int sampleWidth, final int sampleHeight, final int targetWidth,
	    final int targetHeight, final int detailLevel, final int proximity, final boolean discrete) {
	return this.reSynthesis(sample, sampleWidth, sampleHeight, proximity, 20, Math.max(1, detailLevel),
		discrete || this.difference == null, targetWidth, targetHeight);
    }

    public RNG random;
    public AestheticDifference difference;
    private int[][] analyzed;

    private void analysis(final int[] bitmap, final int width, final int height, final int K, final int N,
	    final boolean indexed) {
	final int area = width * height;
	this.analyzed = new int[area][];
	final OrderedSet<Integer> points = new OrderedSet<>(area);
	for (int i = 0; i < area; i++) {
	    points.add(i);
	}
	final double[] similarities = new double[area * area];
	for (int i = 0; i < area; i++) {
	    for (int j = 0; j < area; j++) {
		similarities[i * area + j] = similarities[j * area + i] != 0 ? similarities[j * area + i]
			: this.similarity(i, bitmap, width, height, j, bitmap, width, height, N, null, indexed);
	    }
	}
	for (int i = 0; i < area; i++) {
	    this.analyzed[i] = new int[K];
	    final OrderedSet<Integer> copy = new OrderedSet<>(points);
	    this.analyzed[i][0] = i;
	    copy.remove(i);
	    for (int k = 1; k < K; k++) {
		double max = -10000;
		int argmax = -1;
		for (final Integer p : copy) {
		    final double s = similarities[i * area + p];
		    if (s > max) {
			max = s;
			argmax = p;
		    }
		}
		this.analyzed[i][k] = argmax;
		copy.remove(argmax);
	    }
	}
    }

    private int[] coherentSynthesis(final int[] sample, final int SW, final int SH, final int[][] sets, final int N,
	    final int OW, final int OH, final double t, final boolean indexed) {
	final int[] result = new int[OW * OH];
	final Integer[] origins = new Integer[OW * OH];
	final boolean[][] mask = new boolean[SW][SH], cleanMask = new boolean[SW][SH];
	for (int i = 0; i < OW * OH; i++) {
	    final int x = i % OW, y = i / OW;
	    final IntDoubleOrderedMap candidates = new IntDoubleOrderedMap();
	    ArrayTools.insert(cleanMask, mask, 0, 0);
	    for (int dy = -1; dy <= 1; dy++) {
		for (int dx = -1; dx <= 1; dx++) {
		    final int sx = (x + dx + OW) % OW, sy = (y + dy + OH) % OH;
		    final Integer origin = origins[sy * OW + sx];
		    if ((dx != 0 || dy != 0) && origin != null) {
			for (int pi = 0, p; pi < sets[origin].length; pi++) {
			    p = sets[origin][pi];
			    final int ox = (p % SW - dx + SW) % SW, oy = (p / SW - dy + SH) % SH;
			    final double s = this.similarity(oy * SW + ox, sample, SW, SH, i, result, OW, OH, N,
				    origins, indexed);
			    if (!mask[ox][oy]) {
				candidates.put(ox + oy * SW, Math.pow(100, s / t));
			    }
			    mask[ox][oy] = true;
			}
		    }
		}
	    }
	    final int shifted = candidates.isEmpty() ? this.random.nextInt(SW) + this.random.nextInt(SH) * SW
		    : DetailedMimic.weightedRandom(candidates, this.random.nextDouble());
	    origins[i] = shifted;
	    result[i] = sample[shifted];
	}
	return result;
    }

    private int[] fullSynthesis(final int[] sample, final int SW, final int SH, final int N, final int OW, final int OH,
	    final double t, final boolean indexed) {
	final int[] result = new int[OW * OH];
	final Integer[] origins = new Integer[OW * OH];
	if (!indexed) {
	    for (int y = 0; y < OH; y++) {
		for (int x = 0; x < OW; x++) {
		    if (y + N >= OH) {
			result[x + y * OW] = sample[this.random.nextInt(SW * SH)];
			origins[x + y * OW] = -1;
		    }
		}
	    }
	}
	for (int i = 0; i < OW * OH; i++) {
	    final double[] candidates = new double[SW * SH];
	    double max = -10000;
	    int argmax = -1;
	    for (int j = 0; j < SW * SH; j++) {
		final double s = this.similarity(j, sample, SW, SH, i, result, OW, OH, N, origins, indexed);
		if (s > max) {
		    max = s;
		    argmax = j;
		}
		if (indexed) {
		    candidates[j] = Math.pow(100.0, s / t);
		}
	    }
	    if (indexed) {
		argmax = DetailedMimic.weightedRandom(candidates, this.random.nextDouble());
	    }
	    result[i] = sample[argmax];
	    origins[i] = -1;
	}
	return result;
    }

    private int[] reSynthesis(final int[] sample, final int SW, final int SH, final int N, final int M,
	    final int polish, final boolean indexed, final int OW, final int OH) {
	final IntVLA colors = new IntVLA();
	final int[] indexedSample = new int[Math.min(SW * SH, sample.length)];
	for (int j = 0; j < indexedSample.length; j++) {
	    final int color = sample[j];
	    int i = 0;
	    for (int cn = 0; cn < colors.size; cn++) {
		if (colors.get(cn) == color) {
		    break;
		}
		i++;
	    }
	    if (i == colors.size) {
		colors.add(color);
	    }
	    indexedSample[j] = i;
	}
	final int colorsNumber = colors.size;
	double[][] colorMetric = null;
	if (!indexed && colorsNumber <= 1024) {
	    colorMetric = new double[colorsNumber][colorsNumber];
	    for (int x = 0; x < colorsNumber; x++) {
		for (int y = 0; y < colorsNumber; y++) {
		    final int cx = colors.get(x), cy = colors.get(y);
		    colorMetric[x][y] = this.difference.difference(cx, cy);
		}
	    }
	}
	final int[] origins = new int[OW * OH];
	for (int i = 0; i < origins.length; i++) {
	    origins[i] = -1;
	}
	final int[] shuffle = new int[OW * OH];
	for (int i = 0; i < shuffle.length; i++) {
	    final int j = this.random.nextIntHasty(i + 1);
	    if (j != i) {
		shuffle[i] = shuffle[j];
	    }
	    shuffle[j] = i;
	}
	for (int round = 0; round <= polish; round++) {
	    for (int counter = 0; counter < shuffle.length; counter++) {
		final int f = shuffle[counter];
		final int fx = f % OW, fy = f / OW;
		final int neighborsNumber = round > 0 ? 8 : Math.min(8, counter);
		int neighborsFound = 0;
		final int[] candidates = new int[neighborsNumber + M];
		if (neighborsNumber > 0) {
		    final int[] neighbors = new int[neighborsNumber];
		    final int[] x = new int[4], y = new int[4];
		    for (int radius = 1; neighborsFound < neighborsNumber; radius++) {
			x[0] = fx - radius;
			y[0] = fy - radius;
			x[1] = fx - radius;
			y[1] = fy + radius;
			x[2] = fx + radius;
			y[2] = fy + radius;
			x[3] = fx + radius;
			y[3] = fy - radius;
			for (int k = 0; k < 2 * radius; k++) {
			    for (int d = 0; d < 4; d++) {
				x[d] = (x[d] + 10 * OW) % OW;
				y[d] = (y[d] + 10 * OH) % OH;
				if (neighborsFound >= neighborsNumber) {
				    continue;
				}
				final int point = x[d] + y[d] * OW;
				if (origins[point] != -1) {
				    neighbors[neighborsFound] = point;
				    neighborsFound++;
				}
			    }
			    y[0]++;
			    x[1]++;
			    y[2]--;
			    x[3]--;
			}
		    }
		    for (int n = 0; n < neighborsNumber; n++) {
			final int cx = (origins[neighbors[n]] + (f - neighbors[n]) % OW + 100 * SW) % SW;
			final int cy = (origins[neighbors[n]] / SW + f / OW - neighbors[n] / OW + 100 * SH) % SH;
			candidates[n] = cx + cy * SW;
		    }
		}
		for (int m = 0; m < M; m++) {
		    candidates[neighborsNumber + m] = this.random.nextIntHasty(SW * SH);
		}
		double max = -1E+10;
		int argmax = -1;
		for (final int candidate : candidates) {
		    double sum = this.random.nextDouble(0.000001);
		    final int ix = candidate % SW, iy = candidate / SW, jx = f % OW, jy = f / OW;
		    int SX, SY, FX, FY, S, F;
		    int origin;
		    for (int dy = -N; dy <= N; dy++) {
			for (int dx = -N; dx <= N; dx++) {
			    if (dx != 0 || dy != 0) {
				SX = ix + dx;
				if (SX < 0) {
				    SX += SW;
				} else if (SX >= SW) {
				    SX -= SW;
				}
				SY = iy + dy;
				if (SY < 0) {
				    SY += SH;
				} else if (SY >= SH) {
				    SY -= SH;
				}
				FX = jx + dx;
				if (FX < 0) {
				    FX += OW;
				} else if (FX >= OW) {
				    FX -= OW;
				}
				FY = jy + dy;
				if (FY < 0) {
				    FY += OH;
				} else if (FY >= OH) {
				    FY -= OH;
				}
				S = SX + SY * SW;
				F = FX + FY * OW;
				origin = origins[F];
				if (origin != -1) {
				    if (indexed) {
					sum += sample[origin] == sample[S] ? 1 : -1;
				    } else if (colorMetric != null) {
					sum += colorMetric[indexedSample[origin]][indexedSample[S]];
				    } else {
					sum += this.difference.difference(sample[origin], sample[S]);
				    }
				}
			    }
			}
		    }
		    if (sum >= max) {
			max = sum;
			argmax = candidate;
		    }
		}
		origins[f] = argmax;
	    }
	}
	final int[] result = new int[OW * OH];
	for (int i = 0; i < result.length; i++) {
	    result[i] = sample[origins[i]];
	}
	return result;
    }

    private double similarity(final int i1, final int[] b1, final int w1, final int h1, final int i2, final int[] b2,
	    final int w2, final int h2, final int N, final Integer[] origins, final boolean indexed) {
	double sum = 0;
	final int x1 = i1 % w1, y1 = i1 / w1, x2 = i2 % w2, y2 = i2 / w2;
	for (int dy = -N; dy <= 0; dy++) {
	    for (int dx = -N; dy < 0 && dx <= N || dy == 0 && dx < 0; dx++) {
		final int sx1 = (x1 + dx + w1) % w1, sy1 = (y1 + dy + h1) % h1;
		final int sx2 = (x2 + dx + w2) % w2, sy2 = (y2 + dy + h2) % h2;
		final int c1 = b1[sx1 + sy1 * w1];
		final int c2 = b2[sx2 + sy2 * w2];
		if (origins == null || origins[sy2 * w2 + sx2] != null) {
		    if (indexed) {
			sum += c1 == c2 ? 1 : -1;
		    } else {
			sum -= this.difference.difference(c1, c2);
			/*
			 * Color C1 = Color.FromArgb(c1), C2 = Color.FromArgb(c2); sum -= (double)((C1.R
			 * - C2.R) * (C1.R - C2.R) + (C1.G - C2.G) * (C1.G - C2.G) + (C1.B - C2.B) *
			 * (C1.B - C2.B)) / 65536.0;
			 */
		    }
		}
	    }
	}
	return sum;
    }

    static int weightedRandom(final double[] array, final double r) {
	double sum = 0;
	for (final double element : array) {
	    sum += Math.max(1.0, element);
	}
	for (int j = 0; j < array.length; j++) {
	    array[j] /= sum;
	}
	int i = 0;
	double x = 0;
	while (i < array.length) {
	    x += array[i];
	    if (r <= x) {
		return i;
	    }
	    i++;
	}
	return 0;
    }

    static int weightedRandom(final IntDoubleOrderedMap dic, final double r) {
	final int[] ints = dic.keySet().toIntArray();
	final double[] doubles = dic.values().toDoubleArray();
	return ints[DetailedMimic.weightedRandom(doubles, r)];
    }

    /**
     * Utility method to produce 1D int arrays this can process when discrete is
     * true or difference is null.
     *
     * @param map a 2D char array
     * @return an int array that can be used as a sample
     */
    public static int[] convertCharToInt(final char[][] map) {
	final int w = map.length, h = map[0].length;
	final int[] result = new int[w * h];
	for (int x = 0; x < w; x++) {
	    for (int y = 0; y < h; y++) {
		result[x * h + y] = map[x][y];
	    }
	}
	return result;
    }

    /**
     * Utility method that takes a 1D int array that represents chars (such as a
     * sample produced by {@link #convertCharToInt(char[][])} or, more likely, the
     * result of processing such a sample with this class) and returns a 2D char
     * array with the requested width and height (which should match the targetWidth
     * and targetHeight given during processing).
     *
     * @param arr a 1D int array that represents char values
     * @param w   the width that arr can be interpreted as; should probably match
     *            the targetWidth given in processing
     * @param h   the height that arr can be interpreted as; should probably match
     *            the targetHeight given in processing
     * @return a 2D char array with the given width and height, probably filled with
     *         the data from arr
     */
    public static char[][] convertIntToChar(final int[] arr, final int w, final int h) {
	final char[][] result = new char[w][h];
	if (arr == null) {
	    return result;
	}
	for (int x = 0; x < w; x++) {
	    for (int y = 0; y < h; y++) {
		if (x * h + y >= arr.length) {
		    return result;
		}
		result[x][y] = (char) arr[x * h + y];
	    }
	}
	return result;
    }
}
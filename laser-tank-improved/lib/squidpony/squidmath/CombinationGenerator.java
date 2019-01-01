// ============================================================================
//   Copyright 2006-2012 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package squidpony.squidmath;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Combination generator for generating all combinations of a given size from
 * the specified set of elements. For performance reasons, this implementation
 * is restricted to operating with set sizes and combination lengths that
 * produce no more than 2^63 different combinations. <br>
 * Originally part of the <a href="http://maths.uncommons.org/">Uncommon Maths
 * software package</a>.
 *
 * @param <T> The type of element that the combinations are made from.
 * @author Daniel Dyer (modified from the original version written by Michael
 *         Gilleland of Merriam Park Software - <a href=
 *         "http://www.merriampark.com/perm.htm">http://www.merriampark.com/comb.htm</a>).
 * @see PermutationGenerator
 */
public class CombinationGenerator<T> implements Iterable<List<T>>, Serializable {
    private static final long serialVersionUID = 5998145341506278361L;
    private final T[] elements;
    private final int[] combinationIndices;
    private long remainingCombinations;
    private long totalCombinations;

    /**
     * Create a combination generator that generates all combinations of a specified
     * length from the given set.
     *
     * @param elements          The set from which to generate combinations; will be
     *                          used directly (not copied)
     * @param combinationLength The length of the combinations to be generated.
     */
    public CombinationGenerator(final T[] elements, final int combinationLength) {
	if (combinationLength > elements.length) {
	    throw new IllegalArgumentException("Combination length cannot be greater than set size.");
	}
	this.elements = elements;
	this.combinationIndices = new int[combinationLength];
	final BigInteger sizeFactorial = MathExtras.bigFactorial(elements.length);
	final BigInteger lengthFactorial = MathExtras.bigFactorial(combinationLength);
	final BigInteger differenceFactorial = MathExtras.bigFactorial(elements.length - combinationLength);
	final BigInteger total = sizeFactorial.divide(differenceFactorial.multiply(lengthFactorial));
	if (total.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
	    throw new IllegalArgumentException("Total number of combinations must not be more than 2^63.");
	}
	this.totalCombinations = total.longValue();
	this.reset();
    }

    /**
     * Create a combination generator that generates all combinations of a specified
     * length from the given set.
     *
     * @param elements          The set from which to generate combinations.
     * @param combinationLength The length of the combinations to be generated.
     * @param filler            An array of T with the same length as elements or
     *                          less (often 0); needed because GWT can't create a
     *                          generic array. If elements is not a Collection
     *                          defined in the JDK (by GWT), then this should have
     *                          exactly the same length as elements, since GWT can
     *                          create larger versions of an array on its own, but
     *                          our code can't easily.
     */
    @SuppressWarnings("unchecked")
    public CombinationGenerator(final Collection<T> elements, final int combinationLength, final T[] filler) {
	this(elements.toArray(filler), combinationLength);
    }

    /**
     * Reset the combination generator.
     */
    public final void reset() {
	for (int i = 0; i < this.combinationIndices.length; i++) {
	    this.combinationIndices[i] = i;
	}
	this.remainingCombinations = this.totalCombinations;
    }

    /**
     * @return The number of combinations not yet generated.
     */
    public long getRemainingCombinations() {
	return this.remainingCombinations;
    }

    /**
     * Are there more combinations?
     *
     * @return true if there are more combinations available, false otherwise.
     */
    public boolean hasMore() {
	return this.remainingCombinations > 0;
    }

    /**
     * @return The total number of combinations.
     */
    public long getTotalCombinations() {
	return this.totalCombinations;
    }

    /**
     * Generate the next combination and return an array containing the appropriate
     * elements. This overloaded method allows the caller to provide an array that
     * will be used and returned. The purpose of this is to improve performance when
     * iterating over combinations. This method allows a single array instance to be
     * reused.
     *
     * @param destination Provides an array to use to create the combination. The
     *                    specified array must be the same length as a combination.
     * @return The provided array now containing the elements of the combination.
     */
    public T[] nextCombinationAsArray(final T[] destination) {
	if (destination.length != this.combinationIndices.length) {
	    throw new IllegalArgumentException("Destination array must be the same length as combinations.");
	}
	this.generateNextCombinationIndices();
	for (int i = 0; i < this.combinationIndices.length; i++) {
	    destination[i] = this.elements[this.combinationIndices[i]];
	}
	return destination;
    }

    /**
     * Generate the next combination and return a list containing the appropriate
     * elements.
     *
     * @see #nextCombinationAsList(List)
     * @return A list containing the elements that make up the next combination.
     */
    public List<T> nextCombinationAsList() {
	return this.nextCombinationAsList(new ArrayList<T>(this.elements.length));
    }

    /**
     * Generate the next combination and return a list containing the appropriate
     * elements. This overloaded method allows the caller to provide a list that
     * will be used and returned. The purpose of this is to improve performance when
     * iterating over combinations. If the {@link #nextCombinationAsList()} method
     * is used it will create a new list every time. When iterating over
     * combinations this will result in lots of short-lived objects that have to be
     * garbage collected. This method allows a single list instance to be reused in
     * such circumstances.
     *
     * @param destination Provides a list to use to create the combination.
     * @return The provided list now containing the elements of the combination.
     */
    public List<T> nextCombinationAsList(final List<T> destination) {
	this.generateNextCombinationIndices();
	// Generate actual combination.
	destination.clear();
	for (final int i : this.combinationIndices) {
	    destination.add(this.elements[i]);
	}
	return destination;
    }

    /**
     * Generate the indices into the elements array for the next combination. The
     * algorithm is from Kenneth H. Rosen, Discrete Mathematics and Its
     * Applications, 2nd edition (NY: McGraw-Hill, 1991), p. 286.
     */
    private void generateNextCombinationIndices() {
	if (this.remainingCombinations == 0) {
	    throw new IllegalStateException(
		    "There are no combinations remaining.  " + "Generator must have reset() called to continue.");
	} else if (this.remainingCombinations < this.totalCombinations) {
	    int i = this.combinationIndices.length - 1;
	    while (this.combinationIndices[i] == this.elements.length - this.combinationIndices.length + i) {
		i--;
	    }
	    ++this.combinationIndices[i];
	    for (int j = i + 1; j < this.combinationIndices.length; j++) {
		this.combinationIndices[j] = this.combinationIndices[i] + j - i;
	    }
	}
	--this.remainingCombinations;
    }

    /**
     * <p>
     * Provides a read-only iterator for iterating over the combinations generated
     * by this object. This method is the implementation of the {@link Iterable}
     * interface that permits instances of this class to be used with the new-style
     * for loop.
     * </p>
     * <p>
     * For example:
     * </p>
     *
     * <pre>
     * List&lt;Integer&gt; elements = Arrays.asList(1, 2, 3);
     * CombinationGenerator&lt;Integer&gt; combinations = new CombinationGenerator(elements, 2);
     * for (List&lt;Integer&gt; c : combinations) {
     *     // Do something with each combination.
     * }
     * </pre>
     *
     * @return An iterator.
     * @since 1.1
     */
    @Override
    public Iterator<List<T>> iterator() {
	return new Iterator<>() {
	    @Override
	    public boolean hasNext() {
		return CombinationGenerator.this.hasMore();
	    }

	    @Override
	    public List<T> next() {
		return CombinationGenerator.this.nextCombinationAsList();
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException("Iterator does not support removal.");
	    }
	};
    }
}

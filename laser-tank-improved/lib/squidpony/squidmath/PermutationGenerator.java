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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Permutation generator for generating all permutations for all sets up to 20
 * elements in size. While 20 may seem a low limit, bear in mind that the number
 * of permutations of a set of size n is n! For a set of 21 items, the number of
 * permutations is bigger than can be stored in Java's 64-bit long integer data
 * type. Therefore it seems unlikely that you could ever generate, let alone
 * process, all of the permutations in a reasonable time frame. For this reason
 * the implementation is optimised for sets of size 20 or less (this affords
 * better performance by allowing primitive numeric types to be used for
 * calculations rather than {@link java.math.BigInteger}). <br>
 * Originally part of the <a href="http://maths.uncommons.org/">Uncommon Maths
 * software package</a>.
 *
 * @param <T> The type of element that the permutation will consist of.
 * @author Daniel Dyer (modified from the original version written by Michael
 *         Gilleland of Merriam Park Software - <a href=
 *         "http://www.merriampark.com/perm.htm">http://www.merriampark.com/perm.htm</a>).
 * @see CombinationGenerator
 */
public class PermutationGenerator<T> implements Iterable<List<T>>, Serializable {
    private static final long serialVersionUID = 514276118639629743L;
    private final T[] elements;
    private final int[] permutationIndices;
    private long remainingPermutations;
    private long totalPermutations;

    /**
     * Permutation generator that generates all possible orderings of the elements
     * in the specified set.
     *
     * @param elements The elements to permute; will be modified, so this should be
     *                 copied beforehand
     */
    public PermutationGenerator(final T[] elements) {
	if (elements.length > 20) {
	    throw new IllegalArgumentException("Size must be less than or equal to 20.");
	}
	this.elements = elements;
	this.permutationIndices = new int[elements.length];
	this.totalPermutations = MathExtras.factorial(elements.length);
	this.reset();
    }

    /**
     * Permutation generator that generates all possible orderings of the elements
     * in the specified set.
     *
     * @param elements The elements to permute.
     * @param filler   An array of T with the same length as elements or less (often
     *                 0); needed because GWT can't create a generic array. If
     *                 elements is not a Collection defined in the JDK (by GWT),
     *                 then this should have exactly the same length as elements,
     *                 since GWT can create larger versions of an array on its own,
     *                 but our code can't easily.
     */
    @SuppressWarnings("unchecked")
    public PermutationGenerator(final Collection<T> elements, final T[] filler) {
	this(elements.toArray(filler));
    }

    /**
     * Resets the generator state.
     */
    public final void reset() {
	for (int i = 0; i < this.permutationIndices.length; i++) {
	    this.permutationIndices[i] = i;
	}
	this.remainingPermutations = this.totalPermutations;
    }

    /**
     * Returns the number of permutations not yet generated.
     *
     * @return The number of unique permutations still to be generated.
     */
    public long getRemainingPermutations() {
	return this.remainingPermutations;
    }

    /**
     * Returns the total number of unique permutations that can be generated for the
     * given set of elements.
     *
     * @return The total number of permutations.
     */
    public long getTotalPermutations() {
	return this.totalPermutations;
    }

    /**
     * Returns the total number of unique permutations that can be generated for the
     * given count of permute-able elements. Typically used with the static methods
     * of this class that find permutation indices.
     *
     * @param count the number of elements (typically indices) you want to find a
     *              permutation of
     * @return The total number of permutations.
     */
    public static long getTotalPermutations(final int count) {
	return MathExtras.factorial(count);
    }

    /**
     * Returns the total number of unique permutations that can be generated for the
     * given count of permute-able elements. Typically used with the static methods
     * of this class that find permutation indices and involve BigInteger values.
     *
     * @param count the number of elements (typically indices) you want to find a
     *              permutation of
     * @return The total number of permutations.
     */
    public static BigInteger getBigTotalPermutations(final int count) {
	return MathExtras.bigFactorial(count);
    }

    /**
     * Are there more permutations that have not yet been returned?
     *
     * @return true if there are more permutations, false otherwise.
     */
    public boolean hasMore() {
	return this.remainingPermutations > 0;
    }

    /**
     * Generate the next permutation and return an array containing the elements in
     * the appropriate order. This overloaded method allows the caller to provide an
     * array that will be used and returned. The purpose of this is to improve
     * performance when iterating over permutations. This method allows a single
     * array instance to be reused.
     *
     * @param destination Provides an array to use to create the permutation. The
     *                    specified array must be the same length as a permutation.
     *                    This is the array that will be returned, once it has been
     *                    filled with the elements in the appropriate order.
     * @return The next permutation as an array.
     */
    public T[] nextPermutationAsArray(final T[] destination) {
	if (destination.length != this.elements.length) {
	    throw new IllegalArgumentException("Destination array must be the same length as permutations.");
	}
	this.generateNextPermutationIndices();
	// Generate actual permutation.
	for (int i = 0; i < this.permutationIndices.length; i++) {
	    destination[i] = this.elements[this.permutationIndices[i]];
	}
	return destination;
    }

    /**
     * Generate the next permutation and return a list containing the elements in
     * the appropriate order.
     *
     * @see #nextPermutationAsList(List)
     * @return The next permutation as a list.
     */
    public List<T> nextPermutationAsList() {
	final List<T> permutation = new ArrayList<>(this.elements.length);
	return this.nextPermutationAsList(permutation);
    }

    /**
     * Generate the next permutation and return a list containing the elements in
     * the appropriate order. This overloaded method allows the caller to provide a
     * list that will be used and returned. The purpose of this is to improve
     * performance when iterating over permutations. If the
     * {@link #nextPermutationAsList()} method is used it will create a new list
     * every time. When iterating over permutations this will result in lots of
     * short-lived objects that have to be garbage collected. This method allows a
     * single list instance to be reused in such circumstances.
     *
     * @param destination Provides a list to use to create the permutation. This is
     *                    the list that will be returned, once it has been filled
     *                    with the elements in the appropriate order.
     * @return The next permutation as a list.
     */
    public List<T> nextPermutationAsList(final List<T> destination) {
	this.generateNextPermutationIndices();
	// Generate actual permutation.
	destination.clear();
	for (final int i : this.permutationIndices) {
	    destination.add(this.elements[i]);
	}
	return destination;
    }

    /**
     * Generate the indices into the elements array for the next permutation. The
     * algorithm is from Kenneth H. Rosen, Discrete Mathematics and its
     * Applications, 2nd edition (NY: McGraw-Hill, 1991), p. 284)
     */
    private void generateNextPermutationIndices() {
	if (this.remainingPermutations == 0) {
	    throw new IllegalStateException(
		    "There are no permutations remaining.  " + "Generator must be reset to continue using.");
	} else if (this.remainingPermutations < this.totalPermutations) {
	    // Find largest index j with permutationIndices[j] < permutationIndices[j + 1]
	    int j = this.permutationIndices.length - 2;
	    while (this.permutationIndices[j] > this.permutationIndices[j + 1]) {
		j--;
	    }
	    // Find index k such that permutationIndices[k] is smallest integer greater than
	    // permutationIndices[j] to the right of permutationIndices[j].
	    int k = this.permutationIndices.length - 1;
	    while (this.permutationIndices[j] > this.permutationIndices[k]) {
		k--;
	    }
	    // Interchange permutation indices.
	    int temp = this.permutationIndices[k];
	    this.permutationIndices[k] = this.permutationIndices[j];
	    this.permutationIndices[j] = temp;
	    // Put tail end of permutation after jth position in increasing order.
	    int r = this.permutationIndices.length - 1;
	    int s = j + 1;
	    while (r > s) {
		temp = this.permutationIndices[s];
		this.permutationIndices[s] = this.permutationIndices[r];
		this.permutationIndices[r] = temp;
		r--;
		s++;
	    }
	}
	--this.remainingPermutations;
    }

    private int[] getPermutationShift(final T[] perm) {
	final int[] sh = new int[perm.length];
	final boolean[] taken = new boolean[perm.length];
	for (int i = 0; i < perm.length - 1; i++) {
	    int ctr = -1;
	    for (int j = 0; j < perm.length; j++) {
		if (!taken[j]) {
		    ctr++;
		}
		if (perm[j] == this.elements[i]) {
		    taken[j] = true;
		    sh[i] = ctr;
		    break;
		}
	    }
	}
	return sh;
    }

    private int[] getPermutationShift(final List<T> perm) {
	final int length = perm.size();
	final int[] sh = new int[length];
	final boolean[] taken = new boolean[length];
	for (int i = 0; i < length - 1; i++) {
	    int ctr = -1;
	    for (int j = 0; j < length; j++) {
		if (!taken[j]) {
		    ctr++;
		}
		if (perm.get(j) == this.elements[i]) {
		    taken[j] = true;
		    sh[i] = ctr;
		    break;
		}
	    }
	}
	return sh;
    }

    /**
     * Given an array of T that constitutes a permutation of the elements this was
     * constructed with, finds the specific index of the permutation given a
     * factoradic numbering scheme (not used by the rest of this class, except the
     * decodePermutation() method). The index can be passed to decodePermutation to
     * reproduce the permutation passed to this, or modified and then passed to
     * decodePermutation(). Determines equality by identity, not by .equals(), so
     * that equivalent values that have different references/identities can appear
     * in the permuted elements. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param perm an array of T that must be a valid permutation of this object's
     *             elements
     * @return an encoded number that can be used to reconstruct the permutation
     *         when passed to decodePermutation()
     */
    public long encodePermutation(final T[] perm) {
	long e = 0;
	if (perm == null || perm.length != this.elements.length) {
	    return e;
	}
	final int[] shift = this.getPermutationShift(perm);
	for (int i = 1; i < shift.length; i++) {
	    e += shift[i] * MathExtras.factorialsStart[i];
	}
	return e;
    }

    /**
     * Given a List of T that constitutes a permutation of the elements this was
     * constructed with, finds the specific index of the permutation given a
     * factoradic numbering scheme (not used by the rest of this class, except the
     * decodePermutation() method). The index can be passed to decodePermutation to
     * reproduce the permutation passed to this, or modified and then passed to
     * decodePermutation(). Determines equality by identity, not by .equals(), so
     * that equivalent values that have different references/identities can appear
     * in the permuted elements. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param perm a List of T that must be a valid permutation of this object's
     *             elements
     * @return an encoded number that can be used to reconstruct the permutation
     *         when passed to decodePermutation()
     */
    public long encodePermutation(final List<T> perm) {
	long e = 0;
	if (perm == null || perm.size() != this.elements.length) {
	    return e;
	}
	final int[] shift = this.getPermutationShift(perm);
	for (int i = 1; i < shift.length; i++) {
	    e += shift[i] * MathExtras.factorialsStart[i];
	}
	return e;
    }

    private int[] factoradicDecode(long e) {
	final int[] sequence = new int[this.elements.length];
	int base = 2;
	for (int k = 1; k < this.elements.length; k++) {
	    sequence[this.elements.length - 1 - k] = (int) (e % base);
	    e /= base;
	    base++;
	}
	return sequence;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see
     * getTotalPermutations() for how to access this) and an array of T with the
     * same length as the elements this was constructed with, fills the array with
     * the permutation described by the long as a special (factoradic) index into
     * the possible permutations. You can get an index for a specific permutation
     * with encodePermutation() or by generating a random number between 0 and
     * getTotalPermutations(), if you want it randomly. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param encoded     the index encoded as a long
     * @param destination an array of T that must have equivalent length to the
     *                    elements this was constructed with
     * @return the looked-up permutation, which is the same value destination will
     *         be assigned
     */
    public T[] decodePermutation(long encoded, final T[] destination) {
	if (destination == null) {
	    return null;
	}
	encoded %= this.totalPermutations;
	final int[] sequence = this.factoradicDecode(encoded);
	// char[] list = new char[] { 'a', 'b', 'c', 'd', 'e' }; //change for elements
	// char[] permuted = new char[n]; //change for destination
	final boolean[] set = new boolean[this.elements.length];
	for (int i = 0; i < this.elements.length; i++) {
	    final int s = sequence[i];
	    int remainingPosition = 0;
	    int index;
	    // Find the s'th position in the permuted list that has not been set yet.
	    for (index = 0; index < this.elements.length; index++) {
		if (!set[index]) {
		    if (remainingPosition == s) {
			break;
		    }
		    remainingPosition++;
		}
	    }
	    destination[index] = this.elements[i];
	    set[index] = true;
	}
	return destination;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see
     * getTotalPermutations() for how to access this), creates a List filled with
     * the permutation described by the long as a special (factoradic) index into
     * the possible permutations. You can get an index for a specific permutation
     * with encodePermutation() or by generating a random number between 0 and
     * getTotalPermutations(), if you want it randomly. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param encoded the index encoded as a long
     * @return a List of T that corresponds to the permutation at the encoded index
     */
    public List<T> decodePermutation(final long encoded) {
	final ArrayList<T> list = new ArrayList<>(this.elements.length);
	Collections.addAll(list, this.elements);
	return this.decodePermutation(encoded, list);
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see
     * getTotalPermutations() for how to access this) and a List of T with the same
     * length as the elements this was constructed with, fills the List with the
     * permutation described by the long as a special (factoradic) index into the
     * possible permutations. You can get an index for a specific permutation with
     * encodePermutation() or by generating a random number between 0 and
     * getTotalPermutations(), if you want it randomly. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param encoded     the index encoded as a long
     * @param destination a List of T that must have equivalent size to the elements
     *                    this was constructed with
     * @return the looked-up permutation, which is the same value destination will
     *         be assigned
     */
    public List<T> decodePermutation(long encoded, final List<T> destination) {
	if (destination == null) {
	    return null;
	}
	encoded %= this.totalPermutations;
	final int[] sequence = this.factoradicDecode(encoded);
	// char[] list = new char[] { 'a', 'b', 'c', 'd', 'e' }; //change for elements
	// char[] permuted = new char[n]; //change for destination
	final boolean[] set = new boolean[this.elements.length];
	for (int i = 0; i < this.elements.length; i++) {
	    final int s = sequence[i];
	    int remainingPosition = 0;
	    int index;
	    // Find the s'th position in the permuted list that has not been set yet.
	    for (index = 0; index < this.elements.length; index++) {
		if (!set[index]) {
		    if (remainingPosition == s) {
			break;
		    }
		    remainingPosition++;
		}
	    }
	    destination.set(index, this.elements[i]);
	    set[index] = true;
	}
	return destination;
    }

    /**
     * <p>
     * Provides a read-only iterator for iterating over the permutations generated
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
     * PermutationGenerator&lt;Integer&gt; permutations = new PermutationGenerator(elements);
     * for (List&lt;Integer&gt; p : permutations) {
     *     // Do something with each permutation.
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
		return PermutationGenerator.this.hasMore();
	    }

	    @Override
	    public List<T> next() {
		return PermutationGenerator.this.nextPermutationAsList();
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException("Iterator does not support removal.");
	    }
	};
    }

    private static int[] getPermutationShift(final int[] perm) {
	final int[] sh = new int[perm.length];
	final boolean[] taken = new boolean[perm.length];
	for (int i = 0; i < perm.length - 1; i++) {
	    int ctr = -1;
	    for (int j = 0; j < perm.length; j++) {
		if (!taken[j]) {
		    ctr++;
		}
		if (perm[j] == i) {
		    taken[j] = true;
		    sh[i] = ctr;
		    break;
		}
	    }
	}
	return sh;
    }

    /**
     * Given an array of int that constitutes a permutation of indices, where no
     * element in perm is repeated and all ints are less than perm.length, finds the
     * specific index of the permutation given a factoradic numbering scheme (not
     * used by the rest of this class, except the decodePermutation() method). The
     * index can be passed to decodePermutation to reproduce the index permutation
     * passed to this, or modified and then passed to decodePermutation(). <br>
     * If perm is more than 20 items in length, you should use
     * {@link #encodeBigPermutation} instead. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param perm an array of int that is a permutation of the range from 0
     *             (inclusive) to perm.length (exclusive, must be no more than 20)
     * @return an encoded number that can be used to reconstruct the permutation
     *         when passed to decodePermutation()
     */
    public static long encodePermutation(final int[] perm) {
	long e = 0;
	if (perm == null || perm.length <= 0) {
	    return e;
	}
	final int[] shift = PermutationGenerator.getPermutationShift(perm);
	for (int i = 1; i < shift.length; i++) {
	    e += shift[i] * MathExtras.factorialsStart[i];
	}
	return e;
    }

    /**
     * Given an array of int that constitutes a permutation of indices, where no
     * element in perm is repeated and all ints are less than perm.length, finds the
     * specific index of the permutation given a factoradic numbering scheme (not
     * used by the rest of this class, except the decodePermutation() method). The
     * index can be passed to decodePermutation to reproduce the index permutation
     * passed to this, or modified and then passed to decodePermutation(). <br>
     * If perm is 20 items or less in length, you can use {@link #encodePermutation}
     * instead to get a 64-bit encoding. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param perm an array of int that is a permutation of the range from 0
     *             (inclusive) to perm.length (exclusive)
     * @return an encoded number that can be used to reconstruct the permutation
     *         when passed to decodePermutation()
     */
    public static BigInteger encodeBigPermutation(final int[] perm) {
	BigInteger e = BigInteger.ZERO;
	if (perm == null || perm.length <= 0) {
	    return e;
	}
	final int[] shift = PermutationGenerator.getPermutationShift(perm);
	for (int i = 1; i < shift.length; i++) {
	    e = e.add(MathExtras.bigFactorial(i).multiply(BigInteger.valueOf(shift[i])));
	}
	return e;
    }

    private static int[] factoradicDecode(long e, final int count) {
	final int[] sequence = new int[count];
	int base = 2;
	for (int k = 1; k < count; k++) {
	    sequence[count - 1 - k] = (int) (e % base);
	    e /= base;
	    base++;
	}
	return sequence;
    }

    private static int[] factoradicDecode(BigInteger e, final int count) {
	final int[] sequence = new int[count];
	BigInteger base = BigInteger.valueOf(2);
	for (int k = 1; k < count; k++) {
	    sequence[count - 1 - k] = e.mod(base).intValue();
	    e = e.divide(base);
	    base = base.add(BigInteger.ONE);
	}
	return sequence;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see
     * getTotalPermutations() for how to access this) and an int count of how many
     * indices to find a permutation of, returns an array with the permutation of
     * the indices described by the long as a special (factoradic) index into the
     * possible permutations. You can get an index for a specific permutation with
     * encodePermutation() or by generating a random number between 0 and
     * getTotalPermutations(), if you want it randomly. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param encoded the index encoded as a long
     * @param count   an int between 1 and 20, inclusive, that will be the size of
     *                the returned array
     * @return the looked-up permutation as an int array with length equal to count
     */
    public static int[] decodePermutation(long encoded, final int count) {
	if (count <= 0) {
	    return new int[0];
	}
	encoded %= MathExtras.factorial(count);
	final int[] sequence = PermutationGenerator.factoradicDecode(encoded, count), destination = new int[count];
	// char[] list = new char[] { 'a', 'b', 'c', 'd', 'e' }; //change for elements
	// char[] permuted = new char[n]; //change for destination
	final boolean[] set = new boolean[count];
	for (int i = 0; i < count; i++) {
	    final int s = sequence[i];
	    int remainingPosition = 0;
	    int index;
	    // Find the s'th position in the permuted list that has not been set yet.
	    for (index = 0; index < count; index++) {
		if (!set[index]) {
		    if (remainingPosition == s) {
			break;
		    }
		    remainingPosition++;
		}
	    }
	    destination[index] = i;
	    set[index] = true;
	}
	return destination;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see
     * getBigTotalPermutations() for how to access this) and an int count of how
     * many indices to find a permutation of, returns an array with the permutation
     * of the indices described by the long as a special (factoradic) index into the
     * possible permutations. You can get an index for a specific permutation with
     * encodeBigPermutation() or by generating a random number between 0 and
     * getBigTotalPermutations(), if you want it randomly. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param encoded the index encoded as a BigInteger
     * @param count   a positive int that will be the size of the returned array
     * @return the looked-up permutation as an int array with length equal to count
     */
    public static int[] decodePermutation(final BigInteger encoded, final int count) {
	if (count <= 0) {
	    return new int[0];
	}
	final BigInteger enc = encoded.mod(MathExtras.bigFactorial(count));
	final int[] sequence = PermutationGenerator.factoradicDecode(enc, count), destination = new int[count];
	// char[] list = new char[] { 'a', 'b', 'c', 'd', 'e' }; //change for elements
	// char[] permuted = new char[n]; //change for destination
	final boolean[] set = new boolean[count];
	for (int i = 0; i < count; i++) {
	    final int s = sequence[i];
	    int remainingPosition = 0;
	    int index;
	    // Find the s'th position in the permuted list that has not been set yet.
	    for (index = 0; index < count; index++) {
		if (!set[index]) {
		    if (remainingPosition == s) {
			break;
		    }
		    remainingPosition++;
		}
	    }
	    destination[index] = i;
	    set[index] = true;
	}
	return destination;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see
     * getTotalPermutations() for how to access this) and an int count of how many
     * indices to find a permutation of, returns an array with the permutation of
     * the indices described by the long as a special (factoradic) index into the
     * possible permutations. You can get an index for a specific permutation with
     * encodePermutation() or by generating a random number between 0 and
     * getTotalPermutations(), if you want it randomly. This variant adds an int to
     * each item in the returned array, which may be useful if generating indices
     * that don't start at 0. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param encoded the index encoded as a long
     * @param count   an int between 1 and 20, inclusive, that will be the size of
     *                the returned array
     * @param add     an int to add to each item of the permutation
     * @return the looked-up permutation as an int array with length equal to count
     */
    public static int[] decodePermutation(final long encoded, final int count, final int add) {
	final int[] p = PermutationGenerator.decodePermutation(encoded, count);
	for (int i = 0; i < p.length; i++) {
	    p[i] += add;
	}
	return p;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see
     * getBigTotalPermutations() for how to access this) and an int count of how
     * many indices to find a permutation of, returns an array with the permutation
     * of the indices described by the long as a special (factoradic) index into the
     * possible permutations. You can get an index for a specific permutation with
     * encodeBigPermutation() or by generating a random number between 0 and
     * getBigTotalPermutations(), if you want it randomly. This variant adds an int
     * to each item in the returned array, which may be useful if generating indices
     * that don't start at 0. <br>
     * Credit goes to user Joren on StackOverflow,
     * http://stackoverflow.com/a/1506337
     *
     * @param encoded the index encoded as a BigInteger
     * @param count   a positive int that will be the size of the returned array
     * @param add     an int to add to each item of the permutation
     * @return the looked-up permutation as an int array with length equal to count
     */
    public static int[] decodePermutation(final BigInteger encoded, final int count, final int add) {
	final int[] p = PermutationGenerator.decodePermutation(encoded, count);
	for (int i = 0; i < p.length; i++) {
	    p[i] += add;
	}
	return p;
    }
}

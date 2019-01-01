/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <br>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior
 * written permission.
 * <br>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 1.2_01
 */
package regexodus;

class Optimizer {
    private static final int THRESHOLD = 20;

    static Optimizer find(final Term entry) {
	return Optimizer.find(entry, 0);
    }

    private static Optimizer find(final Term term, final int dist) {
//System.out.println("term="+term+", dist="+dist);
	if (term == null) {
	    return null;
	}
	final Term next = term.next;
	final int type = term.type;
	switch (type) {
	case Term.CHAR:
	case Term.REG:
	case Term.REG_I:
	case Term.GROUP_IN:
	    return new Optimizer(term, dist);
	case Term.BITSET:
	case Term.BITSET2:
	    if (term.weight <= Optimizer.THRESHOLD) {
		return new Optimizer(term, dist);
	    } else {
		return Optimizer.find(term.next, dist + 1);
	    }
	case Term.ANY_CHAR:
	case Term.ANY_CHAR_NE:
	    return Optimizer.find(next, dist + 1);
	case Term.REPEAT_MIN_INF:
	case Term.REPEAT_MIN_MAX:
	    if (term.minCount > 0) {
		return Optimizer.find(term.target, dist);
	    } else {
		return null;
	    }
	}
	if (type >= Term.FIRST_TRANSPARENT && type <= Term.LAST_TRANSPARENT) {
	    return Optimizer.find(next, dist);
	}
	return null;
    }

    private final Term atom;
    private final int distance;

    private Optimizer(final Term atom, final int distance) {
	this.atom = atom;
	this.distance = distance;
    }

    Term makeFirst(final Term theFirst) {
	return new Find(this.atom, this.distance, theFirst);
    }

    Term makeBacktrack(final Term back) {
	int min = back.minCount;
	switch (back.type) {
	case Term.BACKTRACK_0:
	    min = 0;
	case Term.BACKTRACK_MIN:
	    return new FindBack(this.atom, this.distance, min, back);
	case Term.BACKTRACK_REG_MIN:
	    return back;
	default:
	    throw new Error("unexpected iterator's backtracker:" + back);
	    // return back;
	}
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final Optimizer optimizer = (Optimizer) o;
	return this.distance == optimizer.distance
		&& (this.atom != null ? this.atom.equals(optimizer.atom) : optimizer.atom == null);
    }

    @Override
    public int hashCode() {
	int result = this.atom != null ? this.atom.hashCode() : 0;
	result = 31 * result + this.distance;
	return result;
    }
}

class Find extends Term {
    Find(final Term target, final int distance, final Term theFirst) {
	switch (target.type) {
	case Term.CHAR:
	case Term.BITSET:
	case Term.BITSET2:
	    this.type = Term.FIND;
	    break;
	case Term.REG:
	case Term.REG_I:
	case Term.GROUP_IN:
	    this.type = Term.FINDREG;
	    break;
	default:
	    throw new IllegalArgumentException("wrong target type: " + Term.termLookup(target.type));
	}
	this.target = target;
	this.distance = distance;
	if (target == theFirst) {
	    this.next = target.next;
	    this.eat = true; // eat the next
	} else {
	    this.next = theFirst;
	    this.eat = false;
	}
    }
}

class FindBack extends Term {
    FindBack(final Term target, final int distance, final int minCount, final Term backtrack) {
	this.minCount = minCount;
	switch (target.type) {
	case Term.CHAR:
	case Term.BITSET:
	case Term.BITSET2:
	    this.type = Term.BACKTRACK_FIND_MIN;
	    break;
	case Term.REG:
	case Term.REG_I:
	case Term.GROUP_IN:
	    this.type = Term.BACKTRACK_FINDREG_MIN;
	    break;
	default:
	    throw new IllegalArgumentException("wrong target type: " + Term.termLookup(target.type));
	}
	this.target = target;
	this.distance = distance;
	final Term next = backtrack.next;
	if (target == next) {
	    this.next = next.next;
	    this.eat = true;
	} else {
	    this.next = next;
	    this.eat = false;
	}
    }
}
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import regexodus.ds.IntBitSet;

public class Term implements REFlags, Serializable {
    private static final long serialVersionUID = 2528136757932720807L;
    // runtime Term types
    static final int CHAR = 0;
    static final int BITSET = 1;
    static final int BITSET2 = 2;
    static final int ANY_CHAR = 4;
    static final int ANY_CHAR_NE = 5;
    static final int REG = 6;
    static final int REG_I = 7;
    static final int FIND = 8;
    static final int FINDREG = 9;
    static final int SUCCESS = 10;
    /* optimization-transparent types */
    static final int BOUNDARY = 11;
    static final int DIRECTION = 12;
    static final int UBOUNDARY = 13;
    static final int UDIRECTION = 14;
    static final int GROUP_IN = 15;
    static final int GROUP_OUT = 16;
    static final int VOID = 17;
    static final int START = 18;
    static final int END = 19;
    static final int END_EOL = 20;
    static final int LINE_START = 21;
    static final int LINE_END = 22;
    static final int LAST_MATCH_END = 23;
    static final int CNT_SET_0 = 24;
    static final int CNT_INC = 25;
    static final int CNT_GT_EQ = 26;
    static final int READ_CNT_LT = 27;
    static final int CRSTORE_CRINC = 28; // store on 'actual' search entry
    static final int CR_SET_0 = 29;
    static final int CR_LT = 30;
    static final int CR_GT_EQ = 31;
    static final int LITERAL_START = 60;
    static final int LITERAL_END = 61;
    /* optimization-nontransparent types */
    static final int BRANCH = 32;
    static final int BRANCH_STORE_CNT = 33;
    static final int BRANCH_STORE_CNT_AUX1 = 34;
    static final int PLOOKAHEAD_IN = 35;
    static final int PLOOKAHEAD_OUT = 36;
    static final int NLOOKAHEAD_IN = 37;
    static final int NLOOKAHEAD_OUT = 38;
    static final int PLOOKBEHIND_IN = 39;
    static final int PLOOKBEHIND_OUT = 40;
    static final int NLOOKBEHIND_IN = 41;
    static final int NLOOKBEHIND_OUT = 42;
    static final int INDEPENDENT_IN = 43; // functionally the same as NLOOKAHEAD_IN
    static final int INDEPENDENT_OUT = 44;
    static final int REPEAT_0_INF = 45;
    static final int REPEAT_MIN_INF = 46;
    static final int REPEAT_MIN_MAX = 47;
    static final int REPEAT_REG_MIN_INF = 48;
    static final int REPEAT_REG_MIN_MAX = 49;
    static final int BACKTRACK_0 = 50;
    static final int BACKTRACK_MIN = 51;
    static final int BACKTRACK_FIND_MIN = 52;
    static final int BACKTRACK_FINDREG_MIN = 53;
    static final int BACKTRACK_REG_MIN = 54;
    static final int MEMREG_CONDITION = 55;
    static final int LOOKAHEAD_CONDITION_IN = 56;
    static final int LOOKAHEAD_CONDITION_OUT = 57;
    static final int LOOKBEHIND_CONDITION_IN = 58;
    static final int LOOKBEHIND_CONDITION_OUT = 59;
    // optimization
    static final int FIRST_TRANSPARENT = Term.BOUNDARY;
    static final int LAST_TRANSPARENT = Term.CR_GT_EQ;
    // compile-time: length of vars[] (see makeTree())
    private static final int VARS_LENGTH = 4;
    // compile-time variable indices:
    private static final int MEMREG_COUNT = 0; // refers current memreg index
    private static final int CNTREG_COUNT = 1; // refers current counters number
    private static final int DEPTH = 2; // refers current depth: (((depth=3)))
    private static final int LOOKAHEAD_COUNT = 3; // refers current memreg index
    private static final int LIMITS_LENGTH = 3;
    private static final int LIMITS_PARSE_RESULT_INDEX = 2;
    private static final int LIMITS_OK = 1;
    private static final int LIMITS_FAILURE = 2;
    private static final int LITERAL_FLAG = 64;
    // static CustomParser[] customParsers=new CustomParser[256];
    // **** CONTROL FLOW ****
    // next-to-execute and next-if-failed commands;
    Term next, failNext;
    // **** TYPES ****
    int type = Term.VOID;
    boolean inverse;
    // used with type=CHAR
    char c;
    // used with type=FIND
    int distance;
    boolean eat;
    // used with type=BITSET(2);
    IntBitSet bitset;
    IntBitSet[] bitset2;
    private boolean[] categoryBitset; // types(unicode categories)
    boolean mode_insensitive;
    boolean mode_reverse;
    boolean mode_bracket;
    // used for optimization with type=BITSET,BITSET2
    int weight;
    // **** MEMORISATION ****
    // memory slot, used with type=REG,GROUP_IN,GROUP_OUT
    int memreg = -1;
    // **** COUNTERS ****
    // max|min number of iterations
    // used with CNT_GT_EQ ,REPEAT_* etc.;
    int minCount, maxCount;
    // used with REPEAT_*,REPEAT_REG_*;
    Term target;
    // a counter slot to increment & compare with maxCount (CNT_INC etc.);
    int cntreg = 0;
    // lookahead group id;
    int lookaheadId;
    // **** COMPILE HELPERS ****
    Term prev;
    Term in;
    Term out;
    Term out1;
    protected Term first;
    Term current;
    // new!!
    Term branchOut;
    // protected boolean newBranch=false,closed=false;
    // protected boolean newBranch=false;
    // for debugging
    private static int instances;
    private final int instanceNum;

    Term() {
	// for debugging
	this.instanceNum = Term.instances;
	Term.instances++;
	this.in = this.out = this;
    }

    Term(final int type) {
	this();
	this.type = type;
    }

    static void makeTree(final String s, final int[] flags, final Pattern re) throws PatternSyntaxException {
	Term.instances = 0;
	final char[] data = s.toCharArray();
	Term.makeTree(data, 0, data.length, flags, re);
    }

    private static void makeTree(final char[] data, final int offset, final int end, final int[] flags,
	    final Pattern re) throws PatternSyntaxException {
	// memreg,counter,depth,lookahead
	final int[] vars = { 1, 0, 0, 0 }; // don't use counters[0]
	// collect iterators for subsequent optimization
	final ArrayList<TermIterator> iterators = new ArrayList<>();
	final HashMap<String, Integer> groupNames = new HashMap<>();
	final Pretokenizer t = new Pretokenizer(data, offset, end);
	final Term term = Term.makeTree(t, data, vars, flags, new Group(), iterators, groupNames);
	// convert closing outer bracket into success term
	term.out.type = Term.SUCCESS;
	// throw out opening bracket
	final Term first = term.next;
	// Optimisation:
	Term optimized = first;
	final Optimizer opt = Optimizer.find(first);
	if (opt != null) {
	    optimized = opt.makeFirst(first);
	}
	for (final TermIterator i : iterators) {
	    i.optimize();
	}
	re.root = optimized;
	re.root = first;
	re.root0 = first;
	re.memregs = vars[Term.MEMREG_COUNT];
	re.counters = vars[Term.CNTREG_COUNT];
	re.lookaheads = vars[Term.LOOKAHEAD_COUNT];
	re.namedGroupMap = groupNames;
    }

    private static Term makeTree(final Pretokenizer t, final char[] data, final int[] vars, final int[] flags,
	    final Term term, final ArrayList<TermIterator> iterators, final HashMap<String, Integer> groupNames)
	    throws PatternSyntaxException {
	if (vars.length != Term.VARS_LENGTH) {
	    throw new IllegalArgumentException("vars.length should be " + Term.VARS_LENGTH + ", not " + vars.length);
	}
	// Term term=new Term(isMemReg? vars[MEMREG_COUNT]: -1);
	// use memreg 0 as insignificant
	// Term term=new Group(isMemReg? vars[MEMREG_COUNT]: 0);
	while (true) {
	    t.next();
	    term.append(t.tOffset, t.tOutside, data, vars, flags, iterators, groupNames);
	    switch (t.ttype) {
	    case Pretokenizer.FLAGS:
		flags[0] = t.flags(flags[0]);
		continue;
	    case Pretokenizer.CLASS_GROUP:
		t.next();
		final Term clg = new Term();
		CharacterClass.parseGroup(data, t.tOffset, t.tOutside, clg, (flags[0] & REFlags.IGNORE_CASE) > 0,
			(flags[0] & REFlags.IGNORE_SPACES) > 0, (flags[0] & REFlags.UNICODE) > 0,
			(flags[0] & REFlags.XML_SCHEMA) > 0);
		term.append(clg);
		continue;
	    case Pretokenizer.PLAIN_GROUP:
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, new int[] { t.flags(flags[0]) }, new Group(), iterators,
			groupNames));
		break;
	    case Pretokenizer.NAMED_GROUP:
		final String gname = t.groupName;
		int id;
		if (Character.isDigit(gname.charAt(0))) {
		    try {
			id = Integer.parseInt(gname);
		    } catch (final NumberFormatException e) {
			throw new PatternSyntaxException("group name starts with digit but is not a number");
		    }
		    if (groupNames.containsValue(id)) {
			if (t.groupDeclared) {
			    throw new PatternSyntaxException("group redeclaration: " + gname
				    + "; use ({=id}...) for multiple group assignments");
			}
		    }
		    if (vars[Term.MEMREG_COUNT] <= id) {
			vars[Term.MEMREG_COUNT] = id + 1;
		    }
		} else {
		    final Integer no = groupNames.get(gname);
		    if (no == null) {
			id = vars[Term.MEMREG_COUNT]++;
			groupNames.put(t.groupName, id);
		    } else {
			if (t.groupDeclared) {
			    throw new PatternSyntaxException(
				    "group redeclaration " + gname + "; use ({=name}...) for group reassignments");
			}
			id = no;
		    }
		}
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, flags, new Group(id), iterators, groupNames));
		break;
	    case '(':
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, flags, new Group(vars[Term.MEMREG_COUNT]++), iterators,
			groupNames));
		break;
	    case Pretokenizer.POS_LOOKAHEAD:
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, flags, new Lookahead(vars[Term.LOOKAHEAD_COUNT]++, true),
			iterators, groupNames));
		break;
	    case Pretokenizer.NEG_LOOKAHEAD:
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, flags, new Lookahead(vars[Term.LOOKAHEAD_COUNT]++, false),
			iterators, groupNames));
		break;
	    case Pretokenizer.POS_LOOKBEHIND:
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, flags, new Lookbehind(vars[Term.LOOKAHEAD_COUNT]++, true),
			iterators, groupNames));
		break;
	    case Pretokenizer.NEG_LOOKBEHIND:
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, flags, new Lookbehind(vars[Term.LOOKAHEAD_COUNT]++, false),
			iterators, groupNames));
		break;
	    case Pretokenizer.INDEPENDENT_REGEX:
		vars[Term.DEPTH]++;
		term.append(Term.makeTree(t, data, vars, flags, new IndependentGroup(vars[Term.LOOKAHEAD_COUNT]++),
			iterators, groupNames));
		break;
	    case Pretokenizer.CONDITIONAL_GROUP:
		vars[Term.DEPTH]++;
		t.next();
		Term fork;
		boolean positive = true;
		switch (t.ttype) {
		case Pretokenizer.NEG_LOOKAHEAD:
		    positive = false;
		case Pretokenizer.POS_LOOKAHEAD:
		    vars[Term.DEPTH]++;
		    final Lookahead la = new Lookahead(vars[Term.LOOKAHEAD_COUNT]++, positive);
		    Term.makeTree(t, data, vars, flags, la, iterators, groupNames);
		    fork = new ConditionalExpr(la);
		    break;
		case Pretokenizer.NEG_LOOKBEHIND:
		    positive = false;
		case Pretokenizer.POS_LOOKBEHIND:
		    vars[Term.DEPTH]++;
		    final Lookbehind lb = new Lookbehind(vars[Term.LOOKAHEAD_COUNT]++, positive);
		    Term.makeTree(t, data, vars, flags, lb, iterators, groupNames);
		    fork = new ConditionalExpr(lb);
		    break;
		case '(':
		    t.next();
		    if (t.ttype != ')') {
			throw new PatternSyntaxException("malformed condition");
		    }
		    int memregNo;
		    if (Character.isDigit(data[t.tOffset])) {
			memregNo = Term.makeNumber(t.tOffset, t.tOutside, data);
		    } else {
			final String gn = new String(data, t.tOffset, t.tOutside - t.tOffset);
			final Integer gno = groupNames.get(gn);
			if (gno == null) {
			    throw new PatternSyntaxException("unknown group name in conditional expr.: " + gn);
			}
			memregNo = gno;
		    }
		    fork = new ConditionalExpr(memregNo);
		    break;
		default:
		    throw new PatternSyntaxException(
			    "malformed conditional expression: " + t.ttype + " '" + (char) t.ttype + "'");
		}
		term.append(Term.makeTree(t, data, vars, flags, fork, iterators, groupNames));
		break;
	    case '|':
		term.newBranch();
		break;
	    case Pretokenizer.END:
		if (vars[Term.DEPTH] > 0) {
		    throw new PatternSyntaxException("unbalanced parenthesis");
		}
		term.close();
		return term;
	    case ')':
		if (vars[Term.DEPTH] <= 0) {
		    throw new PatternSyntaxException("unbalanced parenthesis");
		}
		term.close();
		vars[Term.DEPTH]--;
		return term;
	    case Pretokenizer.COMMENT:
		while (t.ttype != ')') {
		    t.next();
		}
		continue;
	    default:
		throw new PatternSyntaxException("unknown token type: " + t.ttype);
	    }
	}
    }

    private static int makeNumber(final int off, final int out, final char[] data) {
	int n = 0;
	for (int i = off; i < out; i++) {
	    final int d = data[i] - '0';
	    if (d < 0 || d > 9) {
		return -1;
	    }
	    n *= 10;
	    n += d;
	}
	return n;
    }

    private void append(final int offset, final int end, final char[] data, final int[] vars, final int[] flags,
	    final ArrayList<TermIterator> iterators, final HashMap<String, Integer> gmap)
	    throws PatternSyntaxException {
	final int[] limits = new int[3];
	int i = offset;
	Term tmp, current = this.current;
	while (i < end) {
	    final char c = data[i];
	    boolean greedy = true;
	    if ((flags[0] & Term.LITERAL_FLAG) != Term.LITERAL_FLAG) {
		switch (c) {
		// operations
		case '*':
		    if (current == null) {
			throw new PatternSyntaxException("missing term before *");
		    }
		    i++;
		    if (i < end && data[i] == '?') {
			greedy = false;
			i++;
		    }
		    tmp = greedy ? Term.makeGreedyStar(vars, current, iterators) : Term.makeLazyStar(vars, current);
		    current = this.replaceCurrent(tmp);
		    break;
		case '+':
		    if (current == null) {
			throw new PatternSyntaxException("missing term before +");
		    }
		    i++;
		    if (i < end && data[i] == '?') {
			greedy = false;
			i++;
		    }
		    tmp = greedy ? Term.makeGreedyPlus(vars, current, iterators) : Term.makeLazyPlus(vars, current);
		    current = this.replaceCurrent(tmp);
		    break;
		case '?':
		    if (current == null) {
			throw new PatternSyntaxException("missing term before ?");
		    }
		    i++;
		    if (i < end && data[i] == '?') {
			greedy = false;
			i++;
		    }
		    tmp = greedy ? Term.makeGreedyQMark(vars, current) : Term.makeLazyQMark(vars, current);
		    current = this.replaceCurrent(tmp);
		    break;
		case '{':
		    limits[0] = 0;
		    limits[1] = -1;
		    final int le = Term.parseLimits(i + 1, end, data, limits);
		    if (limits[Term.LIMITS_PARSE_RESULT_INDEX] == Term.LIMITS_OK) { // parse ok
			if (current == null) {
			    throw new PatternSyntaxException("missing term before {}");
			}
			i = le;
			if (i < end && data[i] == '?') {
			    greedy = false;
			    i++;
			}
			tmp = greedy ? Term.makeGreedyLimits(vars, current, limits, iterators)
				: Term.makeLazyLimits(vars, current, limits);
			current = this.replaceCurrent(tmp);
			break;
		    } else { // unicode class or named backreference
			if (data[i + 1] == '\\') { // '{\name}' - backreference
			    int p = i + 2;
			    if (p == end) {
				throw new PatternSyntaxException("'group_id' expected");
			    }
			    char cp = data[p];
			    boolean mi = false, mb = false, mr = false;
			    while (Category.Space.contains(cp) || Category.Po.contains(cp)) {
				p++;
				if (p == end) {
				    throw new PatternSyntaxException("'group_id' expected");
				}
				switch (cp) {
				case '@':
				    mi = !mi;
				    break;
				case '/':
				    mr = !mr;
				    break;
				case ':':
				    mb = !mb;
				    break;
				}
				cp = data[p];
			    }
			    final BackReference br = new BackReference(-1, mi || (flags[0] & REFlags.IGNORE_CASE) > 0,
				    mr, mb);
			    i = Term.parseGroupId(data, p, end, br, gmap, '}');
			    current = this.append(br);
			    continue;
			} else {
			    final Term t = new Term();
			    i = CharacterClass.parseName(data, i, end, t, false,
				    (flags[0] & REFlags.IGNORE_SPACES) > 0);
			    current = this.append(t);
			    continue;
			}
		    }
		case '\\':
		    if (i + 4 < end && data[i + 1] == 'k' && data[i + 2] == '<') { // '\k<name>' - backreference
			int p = i + 3;
			if (p == end) {
			    throw new PatternSyntaxException("'group_id' expected");
			}
			char cp = data[p];
			boolean mi = false, mb = false, mr = false;
			while (Category.Space.contains(cp) || Category.Po.contains(cp)) {
			    p++;
			    if (p == end) {
				throw new PatternSyntaxException("'group_id' expected");
			    }
			    switch (cp) {
			    case '@':
				mi = !mi;
				break;
			    case '/':
				mr = !mr;
				break;
			    case ':':
				mb = !mb;
				break;
			    }
			    cp = data[p];
			}
			final BackReference br = new BackReference(-1, mi || (flags[0] & REFlags.IGNORE_CASE) > 0, mr,
				mb);
			i = Term.parseGroupId(data, p, end, br, gmap, '>');
			current = this.append(br);
			continue;
		    }
		case ' ':
		case '\t':
		case '\r':
		case '\n':
		    if ((flags[0] & REFlags.IGNORE_SPACES) > 0) {
			i++;
			continue;
		    }
		    // else go on as default
		    // symbolic items
		default:
		    tmp = new Term();
		    i = this.parseTerm(data, i, end, tmp, flags[0]);
		    if (tmp.type == Term.LITERAL_START) {
			flags[0] |= Term.LITERAL_FLAG;
			continue;
		    } else if (tmp.type == Term.LITERAL_END) {
			flags[0] &= ~Term.LITERAL_FLAG;
			continue;
		    }
		    if (tmp.type == Term.END && i < end) {
			throw new PatternSyntaxException("'$' is not a last term in the group: <"
				+ new String(data, offset, end - offset) + ">");
		    }
		    // "\A"
		    // if(tmp.type==START && i>(offset+1)){
		    // throw new PatternSyntaxException("'^' is not a first term in the group:
		    // <"+new String(data,offset,end-offset)+">");
		    // }
		    current = this.append(tmp);
		    break;
		}
	    } else {
		tmp = new Term();
		i = this.parseTerm(data, i, end, tmp, flags[0]);
		if (tmp.type == Term.LITERAL_START) {
		    flags[0] |= Term.LITERAL_FLAG;
		    continue;
		} else if (tmp.type == Term.LITERAL_END) {
		    flags[0] &= ~Term.LITERAL_FLAG;
		    continue;
		}
		if (tmp.type == Term.END && i < end) {
		    throw new PatternSyntaxException(
			    "'$' is not a last term in the group: <" + new String(data, offset, end - offset) + ">");
		}
		current = this.append(tmp);
	    }
	}
    }
    /*
     * static boolean isIdentifierPart() {
     *
     * }
     */

    private static int parseGroupId(final char[] data, int i, final int end, final Term term,
	    final HashMap<String, Integer> gmap, final char closer) throws PatternSyntaxException {
	int id;
	final int nstart = i;
	if (Character.isDigit(data[i])) {
	    while (Character.isDigit(data[i])) {
		i++;
		if (i == end) {
		    throw new PatternSyntaxException("group_id expected");
		}
	    }
	    id = Term.makeNumber(nstart, i, data);
	} else {
	    while (Category.IdentifierPart.contains(data[i])) {
		i++;
		if (i == end) {
		    throw new PatternSyntaxException("group_id expected");
		}
	    }
	    final String s = new String(data, nstart, i - nstart);
	    final Integer no = gmap.get(s);
	    if (no == null) {
		throw new PatternSyntaxException("backreference to unknown group: " + s);
	    }
	    id = no;
	}
	while (Category.Space.contains(data[i])) {
	    i++;
	    if (i == end) {
		throw new PatternSyntaxException("'" + closer + "' expected");
	    }
	}
	final int c = data[i++];
	if (c != closer) {
	    throw new PatternSyntaxException("'" + closer + "' expected");
	}
	term.memreg = id;
	return i;
    }

    Term append(final Term term) throws PatternSyntaxException {
	// Term prev=this.prev;
	final Term current = this.current;
	if (current == null) {
	    this.in.next = term;
	    term.prev = this.in;
	    this.current = term;
	    return term;
	}
	Term.link(current, term);
	// this.prev=current;
	this.current = term;
	return term;
    }

    Term replaceCurrent(final Term term) throws PatternSyntaxException {
	// Term prev=this.prev;
	final Term prev = this.current.prev;
	if (prev != null) {
	    final Term in = this.in;
	    if (prev == in) {
		// in.next=term;
		// term.prev=in;
		in.next = term.in;
		term.in.prev = in;
	    } else {
		Term.link(prev, term);
	    }
	}
	this.current = term;
	return term;
    }

    private void newBranch() throws PatternSyntaxException {
	this.close();
	this.startNewBranch();
    }

    void close() throws PatternSyntaxException {
	/*
	 * Term prev=this.prev; if(prev!=null){ Term current=this.current;
	 * if(current!=null){ link(prev,current); prev=current; this.current=null; }
	 * link(prev,out); this.prev=null; }
	 */
	final Term current = this.current;
	if (current != null) {
	    Term.linkd(current, this.out);
	} else {
	    this.in.next = this.out;
	}
    }

    private static void link(final Term term, final Term next) {
	Term.linkd(term, next.in);
	next.prev = term;
    }

    private static void linkd(final Term term, final Term next) {
	final Term prev_out = term.out;
	if (prev_out != null) {
	    prev_out.next = next;
	}
	final Term prev_out1 = term.out1;
	if (prev_out1 != null) {
	    prev_out1.next = next;
	}
	final Term prev_branch = term.branchOut;
	if (prev_branch != null) {
	    prev_branch.failNext = next;
	}
    }

    void startNewBranch() throws PatternSyntaxException {
	final Term tmp = this.in.next;
	final Term b = new Branch();
	this.in.next = b;
	b.next = tmp;
	b.in = null;
	b.out = null;
	b.out1 = null;
	b.branchOut = b;
	this.current = b;
    }

    private static Term makeGreedyStar(final int[] vars, final Term term, final ArrayList<TermIterator> iterators)
	    throws PatternSyntaxException {
	// vars[STACK_SIZE]++;
	switch (term.type) {
	case GROUP_IN: {
	    final Term b = new Branch();
	    b.next = term.in;
	    term.out.next = b;
	    b.in = b;
	    b.out = null;
	    b.out1 = null;
	    b.branchOut = b;
	    return b;
	}
	default: {
	    return new TermIterator(term, 0, -1, iterators);
	}
	}
    }

    private static Term makeLazyStar(final int[] vars, final Term term) {
	// vars[STACK_SIZE]++;
	switch (term.type) {
	case GROUP_IN: {
	    final Term b = new Branch();
	    b.failNext = term.in;
	    term.out.next = b;
	    b.in = b;
	    b.out = b;
	    b.out1 = null;
	    b.branchOut = null;
	    return b;
	}
	default: {
	    final Term b = new Branch();
	    b.failNext = term;
	    term.next = b;
	    b.in = b;
	    b.out = b;
	    b.out1 = null;
	    b.branchOut = null;
	    return b;
	}
	}
    }

    private static Term makeGreedyPlus(final int[] vars, final Term term, final ArrayList<TermIterator> iterators)
	    throws PatternSyntaxException {
	// vars[STACK_SIZE]++;
	switch (term.type) {
	case GROUP_IN: {
	    final Term b = new Branch();
	    b.next = term.in;
	    term.out.next = b;
	    b.in = term.in;
	    b.out = null;
	    b.out1 = null;
	    b.branchOut = b;
	    return b;
	}
	default: {
	    return new TermIterator(term, 1, -1, iterators);
	}
	}
    }

    private static Term makeLazyPlus(final int[] vars, final Term term) {
	// vars[STACK_SIZE]++;
	switch (term.type) {
	case GROUP_IN: {
	    final Term b = new Branch();
	    term.out.next = b;
	    b.failNext = term.in;
	    b.in = term.in;
	    b.out = b;
	    b.out1 = null;
	    b.branchOut = null;
	    return b;
	}
	case REG:
	default: {
	    final Term b = new Branch();
	    term.next = b;
	    b.failNext = term;
	    b.in = term;
	    b.out = b;
	    b.out1 = null;
	    b.branchOut = null;
	    return b;
	}
	}
    }

    private static Term makeGreedyQMark(final int[] vars, final Term term) {
	// vars[STACK_SIZE]++;
	switch (term.type) {
	case GROUP_IN: {
	    final Term b = new Branch();
	    b.next = term.in;
	    b.in = b;
	    b.out = term.out;
	    b.out1 = null;
	    b.branchOut = b;
	    return b;
	}
	case REG:
	default: {
	    final Term b = new Branch();
	    b.next = term;
	    b.in = b;
	    b.out = term;
	    b.out1 = null;
	    b.branchOut = b;
	    return b;
	}
	}
    }

    private static Term makeLazyQMark(final int[] vars, final Term term) {
	// vars[STACK_SIZE]++;
	switch (term.type) {
	case GROUP_IN: {
	    final Term b = new Branch();
	    b.failNext = term.in;
	    b.in = b;
	    b.out = b;
	    b.out1 = term.out;
	    b.branchOut = null;
	    return b;
	}
	case REG:
	default: {
	    final Term b = new Branch();
	    b.failNext = term;
	    b.in = b;
	    b.out = b;
	    b.out1 = term;
	    b.branchOut = null;
	    return b;
	}
	}
    }

    private static Term makeGreedyLimits(final int[] vars, final Term term, final int[] limits,
	    final ArrayList<TermIterator> iterators) throws PatternSyntaxException {
	// vars[STACK_SIZE]++;
	final int m = limits[0];
	final int n = limits[1];
	switch (term.type) {
	case GROUP_IN: {
	    final int cntreg = vars[Term.CNTREG_COUNT]++;
	    final Term reset = new Term(Term.CR_SET_0);
	    reset.cntreg = cntreg;
	    final Term b = new Term(Term.BRANCH);
	    final Term inc = new Term(Term.CRSTORE_CRINC);
	    inc.cntreg = cntreg;
	    reset.next = b;
	    if (n >= 0) {
		final Term lt = new Term(Term.CR_LT);
		lt.cntreg = cntreg;
		lt.maxCount = n;
		b.next = lt;
		lt.next = term.in;
	    } else {
		b.next = term.in;
	    }
	    term.out.next = inc;
	    inc.next = b;
	    if (m >= 0) {
		final Term gt = new Term(Term.CR_GT_EQ);
		gt.cntreg = cntreg;
		gt.maxCount = m;
		b.failNext = gt;
		reset.in = reset;
		reset.out = gt;
		reset.out1 = null;
		reset.branchOut = null;
	    } else {
		reset.in = reset;
		reset.out = null;
		reset.out1 = null;
		reset.branchOut = b;
	    }
	    return reset;
	}
	default: {
	    return new TermIterator(term, limits[0], limits[1], iterators);
	}
	}
    }

    private static Term makeLazyLimits(final int[] vars, final Term term, final int[] limits) {
	// vars[STACK_SIZE]++;
	final int m = limits[0];
	final int n = limits[1];
	switch (term.type) {
	case GROUP_IN: {
	    final int cntreg = vars[Term.CNTREG_COUNT]++;
	    final Term reset = new Term(Term.CR_SET_0);
	    reset.cntreg = cntreg;
	    final Term b = new Term(Term.BRANCH);
	    final Term inc = new Term(Term.CRSTORE_CRINC);
	    inc.cntreg = cntreg;
	    reset.next = b;
	    if (n >= 0) {
		final Term lt = new Term(Term.CR_LT);
		lt.cntreg = cntreg;
		lt.maxCount = n;
		b.failNext = lt;
		lt.next = term.in;
	    } else {
		b.failNext = term.in;
	    }
	    term.out.next = inc;
	    inc.next = b;
	    if (m >= 0) {
		final Term gt = new Term(Term.CR_GT_EQ);
		gt.cntreg = cntreg;
		gt.maxCount = m;
		b.next = gt;
		reset.in = reset;
		reset.out = gt;
		reset.out1 = null;
		reset.branchOut = null;
		return reset;
	    } else {
		reset.in = reset;
		reset.out = b;
		reset.out1 = null;
		reset.branchOut = null;
		return reset;
	    }
	}
	case REG:
	default: {
	    final Term reset = new Term(Term.CNT_SET_0);
	    final Term b = new Branch(Term.BRANCH_STORE_CNT);
	    final Term inc = new Term(Term.CNT_INC);
	    reset.next = b;
	    if (n >= 0) {
		final Term lt = new Term(Term.READ_CNT_LT);
		lt.maxCount = n;
		b.failNext = lt;
		lt.next = term;
		term.next = inc;
		inc.next = b;
	    } else {
		b.next = term;
		term.next = inc;
		inc.next = term;
	    }
	    if (m >= 0) {
		final Term gt = new Term(Term.CNT_GT_EQ);
		gt.maxCount = m;
		b.next = gt;
		reset.in = reset;
		reset.out = gt;
		reset.out1 = null;
		reset.branchOut = null;
		return reset;
	    } else {
		reset.in = reset;
		reset.out = b;
		reset.out1 = null;
		reset.branchOut = null;
		return reset;
	    }
	}
	}
    }

    private int parseTerm(final char[] data, int i, final int out, final Term term, final int flags)
	    throws PatternSyntaxException {
	char c = data[i++];
	boolean inv = false;
	if ((flags & Term.LITERAL_FLAG) == Term.LITERAL_FLAG) {
	    switch (c) {
	    case '\\':
		if (i < out + 1 && data[i] == 'E') {
		    term.type = Term.LITERAL_END;
		    return i + 1;
		}
	    default:
		term.type = Term.CHAR;
		if ((flags & REFlags.IGNORE_CASE) == 0) {
		    term.c = c;
		} else {
		    term.c = Category.caseFold(c);
		}
		return i;
	    }
	}
	switch (c) {
	case '[':
	    return CharacterClass.parseClass(data, i, out, term, (flags & REFlags.IGNORE_CASE) > 0,
		    (flags & REFlags.IGNORE_SPACES) > 0, (flags & REFlags.UNICODE) > 0,
		    (flags & REFlags.XML_SCHEMA) > 0);
	case '.':
	    term.type = (flags & REFlags.DOTALL) > 0 ? Term.ANY_CHAR : Term.ANY_CHAR_NE;
	    break;
	case '$':
	    // term.type=mods[MULTILINE_IND]? LINE_END: END; //??
	    term.type = (flags & REFlags.MULTILINE) > 0 ? Term.LINE_END : Term.END_EOL;
	    break;
	case '^':
	    term.type = (flags & REFlags.MULTILINE) > 0 ? Term.LINE_START : Term.START;
	    break;
	case '\\':
	    if (i >= out) {
		throw new PatternSyntaxException("Escape without a character");
	    }
	    c = data[i++];
	    switch (c) {
	    case 'f':
		c = '\f'; // form feed
		break;
	    case 'n':
		c = '\n'; // new line
		break;
	    case 'r':
		c = '\r'; // carriage return
		break;
	    case 't':
		c = '\t'; // tab
		break;
	    case 'u':
		if (i < out - 3) {
		    c = (char) ((CharacterClass.toHexDigit(data[i++]) << 12)
			    + (CharacterClass.toHexDigit(data[i++]) << 8) + (CharacterClass.toHexDigit(data[i++]) << 4)
			    + CharacterClass.toHexDigit(data[i++]));
		} else {
		    c = '\0';
		    i = out;
		}
		break;
	    case 'x': { // hex 2-digit number -> char
		int hex = 0;
		char d;
		if ((d = data[i++]) == '{') {
		    while (i < out && (d = data[i++]) != '}') {
			hex = (hex << 4) + CharacterClass.toHexDigit(d);
			if (hex > 0xffff || i == out) {
			    throw new PatternSyntaxException("\\x{<out of range or incomplete>}");
			}
		    }
		} else {
		    hex = (CharacterClass.toHexDigit(d) << 4) + CharacterClass.toHexDigit(data[i++]);
		}
		c = (char) hex;
		break;
	    }
	    case '0':
	    case 'o': // oct arbitrary-digit number -> char
		int oct = 0;
		for (; i < out;) {
		    final char d = data[i++];
		    if (d >= '0' && d <= '7') {
			oct *= 8;
			oct += d - '0';
			if (oct > 0xffff) {
			    oct -= d - '0';
			    oct /= 8;
			    break;
			}
		    } else {
			break;
		    }
		}
		c = (char) oct;
		break;
	    case 'm': // decimal number -> char
		int dec = 0;
		for (; i < out;) {
		    final char d = data[i++];
		    if (d >= '0' && d <= '9') {
			dec *= 10;
			dec += d - '0';
			if (dec > 0xffff) {
			    dec -= d - '0';
			    dec /= 10;
			    break;
			}
		    } else {
			break;
		    }
		}
		c = (char) dec;
		break;
	    case 'c': // ctrl-char
		c = (char) (data[i++] & 0x1f);
		break;
	    case 'D': // non-digit
		inv = true;
		// go on
	    case 'd': // digit
		CharacterClass.makeDigit(term, inv, (flags & REFlags.UNICODE) > 0);
		return i;
	    case 'S': // non-space
		inv = true;
		// go on
	    case 's': // space
		CharacterClass.makeSpace(term, inv, (flags & REFlags.UNICODE) > 0);
		return i;
	    case 'W': // non-letter
		inv = true;
		// go on
	    case 'w': // letter
		CharacterClass.makeWordChar(term, inv, (flags & REFlags.UNICODE) > 0);
		return i;
	    case 'H':
		inv = true;
	    case 'h':
		CharacterClass.makeHSpace(term, inv, (flags & REFlags.UNICODE) > 0);
		return i;
	    case 'V':
		inv = true;
	    case 'v':
		CharacterClass.makeVSpace(term, inv, (flags & REFlags.UNICODE) > 0);
		return i;
	    case 'B': // non-(word boundary)
		inv = true;
		// go on
	    case 'b': // word boundary
		CharacterClass.makeWordBoundary(term, inv, (flags & REFlags.UNICODE) > 0);
		return i;
	    case '<': // word start
		CharacterClass.makeWordStart(term, (flags & REFlags.UNICODE) > 0);
		return i;
	    case '>': // word end
		CharacterClass.makeWordEnd(term, (flags & REFlags.UNICODE) > 0);
		return i;
	    case 'A': // text beginning
		term.type = Term.START;
		return i;
	    case 'Z': // text end
		term.type = Term.END_EOL;
		return i;
	    case 'z': // text end
		term.type = Term.END;
		return i;
	    case 'G': // end of last match
		term.type = Term.LAST_MATCH_END;
		return i;
	    case 'P': // \\P{..}
		inv = true;
	    case 'p': // \\p{..}
		i = CharacterClass.parseName(data, i, out, term, inv, (flags & REFlags.IGNORE_SPACES) > 0);
		return i;
	    case 'Q':
		term.type = Term.LITERAL_START;
		return i;
	    default:
		if (c >= '1' && c <= '9') {
		    int n = c - '0';
		    while (i < out && (c = data[i]) >= '0' && c <= '9') {
			n = n * 10 + c - '0';
			i++;
		    }
		    term.type = (flags & REFlags.IGNORE_CASE) > 0 ? Term.REG_I : Term.REG;
		    term.memreg = n;
		    return i;
		}
		/*
		 * if(c<256){ CustomParser termp=customParsers[c]; if(termp!=null){
		 * i=termp.parse(i,data,term); return i; } }
		 */
	    }
	    term.type = Term.CHAR;
	    term.c = c;
	    break;
	default:
	    if ((flags & REFlags.IGNORE_CASE) == 0) {
		term.type = Term.CHAR;
		term.c = c;
	    } else {
		term.type = Term.CHAR;
		term.c = Category.caseFold(c);
		// CharacterClass.makeICase(term, c);
	    }
	    break;
	}
	return i;
    }

    // one of {n},{n,},{,n},{n1,n2}
    private static int parseLimits(int i, final int end, final char[] data, final int[] limits)
	    throws PatternSyntaxException {
	if (limits.length != Term.LIMITS_LENGTH) {
	    throw new IllegalArgumentException("limits.length=" + limits.length + ", should be " + Term.LIMITS_LENGTH);
	}
	limits[Term.LIMITS_PARSE_RESULT_INDEX] = Term.LIMITS_OK;
	int ind = 0;
	int v = 0;
	char c;
	while (i < end) {
	    c = data[i++];
	    switch (c) {
	    case ' ':
		continue;
	    case ',':
		if (ind > 0) {
		    throw new PatternSyntaxException("illegal construction: {.. , , ..}");
		}
		limits[ind++] = v;
		v = -1;
		continue;
	    case '}':
		limits[ind] = v;
		if (ind == 0) {
		    limits[1] = v;
		}
		return i;
	    default:
		if (c > '9' || c < '0') {
		    // throw new PatternSyntaxException("illegal symbol in iterator: '{"+c+"}'");
		    limits[Term.LIMITS_PARSE_RESULT_INDEX] = Term.LIMITS_FAILURE;
		    return i;
		}
		if (v < 0) {
		    v = 0;
		}
		v = v * 10 + c - '0';
	    }
	}
	throw new PatternSyntaxException("malformed quantifier");
    }

    static String termLookup(final int t) {
	switch (t) {
	case CHAR:
	    return "CHAR";
	case BITSET:
	    return "BITSET";
	case BITSET2:
	    return "BITSET2";
	case ANY_CHAR:
	    return "ANY_CHAR";
	case ANY_CHAR_NE:
	    return "ANY_CHAR_NE";
	case REG:
	    return "REG";
	case REG_I:
	    return "REG_I";
	case FIND:
	    return "FIND";
	case FINDREG:
	    return "FINDREG";
	case SUCCESS:
	    return "SUCCESS";
	case BOUNDARY:
	    return "BOUNDARY";
	case DIRECTION:
	    return "DIRECTION";
	case UBOUNDARY:
	    return "UBOUNDARY";
	case UDIRECTION:
	    return "UDIRECTION";
	case GROUP_IN:
	    return "GROUP_IN";
	case GROUP_OUT:
	    return "GROUP_OUT";
	case VOID:
	    return "VOID";
	case START:
	    return "START";
	case END:
	    return "END";
	case END_EOL:
	    return "END_EOL";
	case LINE_START:
	    return "LINE_START";
	case LINE_END:
	    return "LINE_END";
	case LAST_MATCH_END:
	    return "LAST_MATCH_END";
	case CNT_SET_0:
	    return "CNT_SET_0";
	case CNT_INC:
	    return "CNT_INC";
	case CNT_GT_EQ:
	    return "CNT_GT_EQ";
	case READ_CNT_LT:
	    return "READ_CNT_LT";
	case CRSTORE_CRINC:
	    return "CRSTORE_CRINC";
	case CR_SET_0:
	    return "CR_SET_0";
	case CR_LT:
	    return "CR_LT";
	case CR_GT_EQ:
	    return "CR_GT_EQ";
	case BRANCH:
	    return "BRANCH";
	case BRANCH_STORE_CNT:
	    return "BRANCH_STORE_CNT";
	case BRANCH_STORE_CNT_AUX1:
	    return "BRANCH_STORE_CNT_AUX1";
	case PLOOKAHEAD_IN:
	    return "PLOOKAHEAD_IN";
	case PLOOKAHEAD_OUT:
	    return "PLOOKAHEAD_OUT";
	case NLOOKAHEAD_IN:
	    return "NLOOKAHEAD_IN";
	case NLOOKAHEAD_OUT:
	    return "NLOOKAHEAD_OUT";
	case PLOOKBEHIND_IN:
	    return "PLOOKBEHIND_IN";
	case PLOOKBEHIND_OUT:
	    return "PLOOKBEHIND_OUT";
	case NLOOKBEHIND_IN:
	    return "NLOOKBEHIND_IN";
	case NLOOKBEHIND_OUT:
	    return "NLOOKBEHIND_OUT";
	case INDEPENDENT_IN:
	    return "INDEPENDENT_IN";
	case INDEPENDENT_OUT:
	    return "INDEPENDENT_OUT";
	case REPEAT_0_INF:
	    return "REPEAT_0_INF";
	case REPEAT_MIN_INF:
	    return "REPEAT_MIN_INF";
	case REPEAT_MIN_MAX:
	    return "REPEAT_MIN_MAX";
	case REPEAT_REG_MIN_INF:
	    return "REPEAT_REG_MIN_INF";
	case REPEAT_REG_MIN_MAX:
	    return "REPEAT_REG_MIN_MAX";
	case BACKTRACK_0:
	    return "BACKTRACK_0";
	case BACKTRACK_MIN:
	    return "BACKTRACK_MIN";
	case BACKTRACK_FIND_MIN:
	    return "BACKTRACK_FIND_MIN";
	case BACKTRACK_FINDREG_MIN:
	    return "BACKTRACK_FINDREG_MIN";
	case BACKTRACK_REG_MIN:
	    return "BACKTRACK_REG_MIN";
	case MEMREG_CONDITION:
	    return "MEMREG_CONDITION";
	case LOOKAHEAD_CONDITION_IN:
	    return "LOOKAHEAD_CONDITION_IN";
	case LOOKAHEAD_CONDITION_OUT:
	    return "LOOKAHEAD_CONDITION_OUT";
	case LOOKBEHIND_CONDITION_IN:
	    return "LOOKBEHIND_CONDITION_IN";
	case LOOKBEHIND_CONDITION_OUT:
	    return "LOOKBEHIND_CONDITION_OUT";
	default:
	    return "UNKNOWN_TERM";
	}
    }

    @Override
    public String toString() {
	final StringBuilder b = new StringBuilder(100);
	// b.append(hashCode());
	b.append(this.instanceNum);
	b.append(' ');
	b.append(Term.termLookup(this.type));
	b.append(": ");
	if (this.inverse) {
	    b.append('^');
	}
	switch (this.type) {
	case VOID:
	    b.append("[]");
	    b.append(" , ");
	    break;
	case CHAR:
	    b.append(CharacterClass.stringValue(this.c));
	    b.append(" , ");
	    break;
	case ANY_CHAR:
	    b.append("dotall, ");
	    break;
	case ANY_CHAR_NE:
	    b.append("dot-eols, ");
	    break;
	case BITSET:
	    b.append('[');
	    b.append(CharacterClass.stringValue0(this.bitset));
	    b.append(']');
	    b.append(" , weight=");
	    b.append(this.weight);
	    b.append(" , ");
	    break;
	case BITSET2:
	    b.append('[');
	    b.append(CharacterClass.stringValue2(this.bitset2));
	    b.append(']');
	    b.append(" , weight2=");
	    b.append(this.weight);
	    b.append(" , ");
	    break;
	case START:
	    b.append("abs.start");
	    break;
	case END:
	    b.append("abs.end");
	    break;
	case END_EOL:
	    b.append("abs.end-eol");
	    break;
	case LINE_START:
	    b.append("line start");
	    break;
	case LINE_END:
	    b.append("line end");
	    break;
	case LAST_MATCH_END:
	    if (this.inverse) {
		b.append("non-");
	    }
	    b.append("BOUNDARY");
	    break;
	case BOUNDARY:
	    if (this.inverse) {
		b.append("non-");
	    }
	    b.append("BOUNDARY");
	    break;
	case UBOUNDARY:
	    if (this.inverse) {
		b.append("non-");
	    }
	    b.append("UBOUNDARY");
	    break;
	case DIRECTION:
	    b.append("DIRECTION");
	    break;
	case UDIRECTION:
	    b.append("UDIRECTION");
	    break;
	case FINDREG:
	    b.append('%');
	case FIND:
	    b.append(">>>{");
	    b.append(this.target);
	    b.append("}, <<");
	    b.append(this.distance);
	    if (this.eat) {
		b.append(",eat");
	    }
	    b.append(", ");
	    break;
	case REPEAT_0_INF:
	    b.append("rpt{");
	    b.append(this.target);
	    b.append(",0,inf}");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case REPEAT_MIN_INF:
	    b.append("rpt{");
	    b.append(this.target);
	    b.append(",");
	    b.append(this.minCount);
	    b.append(",inf}");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case REPEAT_MIN_MAX:
	    b.append("rpt{");
	    b.append(this.target);
	    b.append(",");
	    b.append(this.minCount);
	    b.append(",");
	    b.append(this.maxCount);
	    b.append("}");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case REPEAT_REG_MIN_INF:
	    b.append("rpt{$");
	    b.append(this.memreg);
	    b.append(',');
	    b.append(this.minCount);
	    b.append(",inf}");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case REPEAT_REG_MIN_MAX:
	    b.append("rpt{$");
	    b.append(this.memreg);
	    b.append(',');
	    b.append(this.minCount);
	    b.append(',');
	    b.append(this.maxCount);
	    b.append("}");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case BACKTRACK_0:
	    b.append("back(0)");
	    break;
	case BACKTRACK_MIN:
	    b.append("back(");
	    b.append(this.minCount);
	    b.append(")");
	    break;
	case BACKTRACK_REG_MIN:
	    b.append("back");
	    b.append("_$");
	    b.append(this.memreg);
	    b.append("(");
	    b.append(this.minCount);
	    b.append(")");
	    break;
	case GROUP_IN:
	    b.append('(');
	    if (this.memreg > 0) {
		b.append(this.memreg);
	    }
	    b.append('-');
	    b.append(" , ");
	    break;
	case GROUP_OUT:
	    b.append('-');
	    if (this.memreg > 0) {
		b.append(this.memreg);
	    }
	    b.append(')');
	    b.append(" , ");
	    break;
	case PLOOKAHEAD_IN:
	    b.append('(');
	    b.append("=");
	    b.append(this.lookaheadId);
	    b.append(" , ");
	    break;
	case PLOOKAHEAD_OUT:
	    b.append('=');
	    b.append(this.lookaheadId);
	    b.append(')');
	    b.append(" , ");
	    break;
	case NLOOKAHEAD_IN:
	    b.append("(!");
	    b.append(this.lookaheadId);
	    b.append(" , ");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case NLOOKAHEAD_OUT:
	    b.append('!');
	    b.append(this.lookaheadId);
	    b.append(')');
	    b.append(" , ");
	    break;
	case PLOOKBEHIND_IN:
	    b.append('(');
	    b.append("<=");
	    b.append(this.lookaheadId);
	    b.append(" , dist=");
	    b.append(this.distance);
	    b.append(" , ");
	    break;
	case PLOOKBEHIND_OUT:
	    b.append("<=");
	    b.append(this.lookaheadId);
	    b.append(')');
	    b.append(" , ");
	    break;
	case NLOOKBEHIND_IN:
	    b.append("(<!");
	    b.append(this.lookaheadId);
	    b.append(" , dist=");
	    b.append(this.distance);
	    b.append(" , ");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case NLOOKBEHIND_OUT:
	    b.append("<!");
	    b.append(this.lookaheadId);
	    b.append(')');
	    b.append(" , ");
	    break;
	case MEMREG_CONDITION:
	    b.append("(reg");
	    b.append(this.memreg);
	    b.append("?)");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case LOOKAHEAD_CONDITION_IN:
	    b.append("(cond");
	    b.append(this.lookaheadId);
	    b.append(((Lookahead) this).isPositive ? '=' : '!');
	    b.append(" , ");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case LOOKAHEAD_CONDITION_OUT:
	    b.append("cond");
	    b.append(this.lookaheadId);
	    b.append(")");
	    if (this.failNext != null) {
		b.append(", =>");
		b.append(this.failNext.instanceNum);
		b.append(", ");
	    }
	    break;
	case REG:
	    b.append("$");
	    b.append(this.memreg);
	    b.append(", ");
	    break;
	case SUCCESS:
	    return b.append("END").toString();
	case BRANCH_STORE_CNT_AUX1:
	    b.append("(aux1)");
	case BRANCH_STORE_CNT:
	    b.append("(cnt)");
	case BRANCH:
	    b.append("=>");
	    if (this.failNext != null) {
		b.append(this.failNext.instanceNum);
	    } else {
		b.append("null");
	    }
	    b.append(" , ");
	    break;
	default:
	    b.append('[');
	    switch (this.type) {
	    case CNT_SET_0:
		b.append("cnt=0");
		break;
	    case CNT_INC:
		b.append("cnt++");
		break;
	    case CNT_GT_EQ:
		b.append("cnt>=").append(this.maxCount);
		break;
	    case READ_CNT_LT:
		b.append("->cnt<").append(this.maxCount);
		break;
	    case CRSTORE_CRINC:
		b.append("M(").append(this.memreg).append(")->,Cr(").append(this.cntreg).append(")->,Cr(")
			.append(this.cntreg).append(")++");
		break;
	    case CR_SET_0:
		b.append("Cr(").append(this.cntreg).append(")=0");
		break;
	    case CR_LT:
		b.append("Cr(").append(this.cntreg).append(")<").append(this.maxCount);
		break;
	    case CR_GT_EQ:
		b.append("Cr(").append(this.cntreg).append(")>=").append(this.maxCount);
		break;
	    default:
		b.append("unknown type: ").append(this.type);
	    }
	    b.append("] , ");
	}
	if (this.next != null) {
	    b.append("->");
	    b.append(this.next.instanceNum);
	    b.append(", ");
	}
	// b.append("\r\n");
	return b.toString();
    }

    public String toStringAll() {
	return this.toStringAll(new ArrayList<Integer>());
    }

    private String toStringAll(final ArrayList<Integer> v) {
	v.add(this.instanceNum);
	String s = this.toString();
	if (this.next != null) {
	    if (!v.contains(this.next.instanceNum)) {
		s += "\r\n";
		s += this.next.toStringAll(v);
	    }
	}
	if (this.failNext != null) {
	    if (!v.contains(this.failNext.instanceNum)) {
		s += "\r\n";
		s += this.failNext.toStringAll(v);
	    }
	}
	return s;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final Term term = (Term) o;
	if (this.type != term.type) {
	    return false;
	}
	if (this.inverse != term.inverse) {
	    return false;
	}
	if (this.c != term.c) {
	    return false;
	}
	if (this.distance != term.distance) {
	    return false;
	}
	if (this.eat != term.eat) {
	    return false;
	}
	if (this.weight != term.weight) {
	    return false;
	}
	if (this.memreg != term.memreg) {
	    return false;
	}
	if (this.minCount != term.minCount) {
	    return false;
	}
	if (this.maxCount != term.maxCount) {
	    return false;
	}
	if (this.cntreg != term.cntreg) {
	    return false;
	}
	if (this.lookaheadId != term.lookaheadId) {
	    return false;
	}
	if (this.next != null ? !this.next.equals(term.next) : term.next != null) {
	    return false;
	}
	if (this.bitset != null ? !this.bitset.equals(term.bitset) : term.bitset != null) {
	    return false;
	}
	// Probably incorrect - comparing Object[] arrays with Arrays.equals
	return Arrays.equals(this.bitset2, term.bitset2) && Arrays.equals(this.categoryBitset, term.categoryBitset);
//if (!Arrays.equals(brackets, term.brackets)) return false;
	/*
	 * if (failNext != null ? !failNext.equals(term.failNext) : term.failNext !=
	 * null) return false; if (target != null ? !target.equals(term.target) :
	 * term.target != null) return false; if (prev != null ? !prev.equals(term.prev)
	 * : term.prev != null) return false; if (in != null ? !in.equals(term.in) :
	 * term.in != null) return false; if (out != null ? !out.equals(term.out) :
	 * term.out != null) return false; if (out1 != null ? !out1.equals(term.out1) :
	 * term.out1 != null) return false; if (first != null ?
	 * !first.equals(term.first) : term.first != null) return false; if (current !=
	 * null ? !current.equals(term.current) : term.current != null) return false;
	 * return branchOut != null ? branchOut.equals(term.branchOut) : term.branchOut
	 * == null;
	 */
    }

    @Override
    public int hashCode() {
	int result = this.next != null ? this.next.hashCode() : 0;
	result = 31 * result + this.type;
	result = 31 * result + (this.inverse ? 1 : 0);
	result = 31 * result + this.c;
	result = 31 * result + this.distance;
	result = 31 * result + (this.eat ? 1 : 0);
	result = 31 * result + (this.bitset != null ? this.bitset.hashCode() : 0);
	result = 31 * result + Arrays.hashCode(this.bitset2);
	result = 31 * result + Arrays.hashCode(this.categoryBitset);
	result = 31 * result + this.weight;
	result = 31 * result + this.memreg;
	result = 31 * result + this.minCount;
	result = 31 * result + this.maxCount;
	result = 31 * result + this.cntreg;
	result = 31 * result + this.lookaheadId;
	/*
	 * result = 31 * result + (failNext != null ? failNext.hashCode() : 0); result =
	 * 31 * result + (target != null ? (this == target ? 73 : target.hashCode()) :
	 * 0); result = 31 * result + (prev != null ? (this == prev ? 73 :
	 * prev.hashCode()) : 0); result = 31 * result + (in != null ? (this == in ? 73
	 * : in.hashCode()) : 0); result = 31 * result + (out != null ? (this == out ?
	 * 73 : out.hashCode()) : 0); result = 31 * result + (out1 != null ? (this ==
	 * out1 ? 73 : out1.hashCode()) : 0); result = 31 * result + (first != null ?
	 * (this == first ? 73 : first.hashCode()) : 0); result = 31 * result + (current
	 * != null ? (this == current ? 73 : current.hashCode()) : 0); result = 31 *
	 * result + (branchOut != null ? (this == branchOut ? 73 : branchOut.hashCode())
	 * : 0);
	 */
	return result;
    }
}

class Pretokenizer implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;
    private static final int START = 1;
    static final int END = 2;
    static final int PLAIN_GROUP = 3;
    static final int POS_LOOKAHEAD = 4;
    static final int NEG_LOOKAHEAD = 5;
    static final int POS_LOOKBEHIND = 6;
    static final int NEG_LOOKBEHIND = 7;
    static final int INDEPENDENT_REGEX = 8;
    static final int COMMENT = 9;
    static final int CONDITIONAL_GROUP = 10;
    static final int FLAGS = 11;
    static final int CLASS_GROUP = 12;
    static final int NAMED_GROUP = 13;
    int tOffset;
    int tOutside;
    private int skip;
    private final int end;
    int c;
    int ttype = Pretokenizer.START;
    private final char[] data;
    // results
    private int flags;
    private boolean flagsChanged;
    String groupName;
    boolean groupDeclared;

    Pretokenizer(final char[] data, final int offset, final int end) {
	if (offset < 0 || end > data.length) {
	    throw new IndexOutOfBoundsException("offset=" + offset + ", end=" + end + ", length=" + data.length);
	}
	this.end = end;
	this.tOffset = offset;
	this.tOutside = offset;
	this.data = data;
    }

    int flags(final int def) {
	return this.flagsChanged ? this.flags : def;
    }

    void next() throws PatternSyntaxException {
	int tOffset = this.tOutside;
	int skip = this.skip;
	tOffset += skip;
	this.flagsChanged = false;
	final int end = this.end;
	final char[] data = this.data;
	boolean esc = false;
	for (int i = tOffset; i < end; i++) {
	    char c = data[i];
	    if (esc) {
		if (c == 'Q') {
		    for (; i < end; i++) {
			final char c1 = data[i];
			if (c1 == '\\') {
			    if (i + 1 < end && data[i + 1] == 'E') {
				i++;
				esc = false;
				break;
			    }
			}
		    }
		} else {
		    esc = false;
		}
		continue;
	    }
	    switch (c) {
	    case '\\':
		esc = true;
		continue;
	    case '|':
	    case ')':
		this.ttype = c;
		this.tOffset = tOffset;
		this.tOutside = i;
		this.skip = 1;
		return;
	    case '(':
		if (i + 2 < end && data[i + 1] == '?') {
		    char c1 = data[i + 2];
		    switch (c1) {
		    case ':':
			this.ttype = Pretokenizer.PLAIN_GROUP;
			skip = 3; // "(?:" - skip 3 chars
			break;
		    case '=':
			this.ttype = Pretokenizer.POS_LOOKAHEAD;
			skip = 3; // "(?="
			break;
		    case '!':
			this.ttype = Pretokenizer.NEG_LOOKAHEAD;
			skip = 3; // "(?!"
			break;
		    case '<':
			switch (c1 = data[i + 3]) {
			case '=':
			    this.ttype = Pretokenizer.POS_LOOKBEHIND;
			    skip = 4; // "(?<="
			    break;
			case '!':
			    this.ttype = Pretokenizer.NEG_LOOKBEHIND;
			    skip = 4; // "(?<!"
			    break;
			default:
			    int p = i + 3;
			    skip = 4; // '(?<' + '>'
			    int nstart, nend;
			    nstart = p;
			    if (Category.N.contains(c1)) {
				throw new PatternSyntaxException("number at the start of a named group");
			    }
			    while (Category.IdentifierPart.contains(c1)) {
				c1 = data[++p];
				skip++;
				if (p == end) {
				    throw new PatternSyntaxException("malformed named group");
				}
			    }
			    nend = p;
			    if (c1 != '>') {
				throw new PatternSyntaxException(
					"'>' expected at " + (p - i) + " in " + new String(data, i, end - i));
			    }
			    this.groupName = new String(data, nstart, nend - nstart);
			    this.groupDeclared = true;
			    this.ttype = Pretokenizer.NAMED_GROUP;
			    break;
			// throw new PatternSyntaxException("invalid character after '(?<' : " + c1);
			}
			break;
		    case '>':
			this.ttype = Pretokenizer.INDEPENDENT_REGEX;
			skip = 3; // "(?>"
			break;
		    case '#':
			this.ttype = Pretokenizer.COMMENT;
			skip = 3; // ="(?#".length, the makeTree() skips the rest by itself
			break;
		    case '(':
			this.ttype = Pretokenizer.CONDITIONAL_GROUP;
			skip = 2; // "(?"+"(..." - skip "(?" (2 chars) and parse condition as a group
			break;
		    case '[':
			this.ttype = Pretokenizer.CLASS_GROUP;
			skip = 2; // "(?"+"[..]+...-...&...)" - skip 2 chars and parse a class group
			break;
		    default:
			int mOff, mLen;
			mLoop: for (int p = i + 2; p < end; p++) {
			    final char c2 = data[p];
			    switch (c2) {
			    case '-':
			    case 'i':
			    case 'm':
			    case 's':
			    case 'x':
			    case 'u':
			    case 'X':
				continue mLoop;
			    case ':':
				mOff = i + 2;
				mLen = p - mOff;
				if (mLen > 0) {
				    this.flags = Pattern.parseFlags(data, mOff, mLen);
				    this.flagsChanged = true;
				}
				this.ttype = Pretokenizer.PLAIN_GROUP;
				skip = mLen + 3; // "(?imsx:" mLen=4; skip= "(?".len + ":".len + mLen = 2+1+4=7
				break mLoop;
			    case ')':
				this.flags = Pattern.parseFlags(data, mOff = i + 2, mLen = p - mOff);
				this.flagsChanged = true;
				this.ttype = Pretokenizer.FLAGS;
				skip = mLen + 3; // "(?imsx)" mLen=4, skip="(?".len+")".len+mLen=2+1+4=7
				break mLoop;
			    default:
				throw new PatternSyntaxException("wrong char after \"(?\": " + c2);
			    }
			}
			break;
		    }
		} else if (i + 2 < end && data[i + 1] == '{') { // parse named group: ({name}....),({=name}....)
		    int p = i + 2;
		    skip = 3; // '({' + '}'
		    int nstart, nend;
		    boolean isDecl;
		    c = data[p];
		    while (Category.Space.contains(c)) {
			c = data[++p];
			skip++;
			if (p == end) {
			    throw new PatternSyntaxException("malformed named group");
			}
		    }
		    if (c == '=') {
			isDecl = false;
			c = data[++p];
			skip++;
			if (p == end) {
			    throw new PatternSyntaxException("malformed named group");
			}
		    } else {
			isDecl = true;
		    }
		    nstart = p;
		    while (Category.IdentifierPart.contains(c)) {
			c = data[++p];
			skip++;
			if (p == end) {
			    throw new PatternSyntaxException("malformed named group");
			}
		    }
		    nend = p;
		    while (Category.Space.contains(c)) {
			c = data[++p];
			skip++;
			if (p == end) {
			    throw new PatternSyntaxException("malformed named group");
			}
		    }
		    if (c != '}') {
			throw new PatternSyntaxException(
				"'}' expected at " + (p - i) + " in " + new String(data, i, end - i));
		    }
		    this.groupName = new String(data, nstart, nend - nstart);
		    this.groupDeclared = isDecl;
		    this.ttype = Pretokenizer.NAMED_GROUP;
		} else {
		    this.ttype = '(';
		    skip = 1;
		}
		this.tOffset = tOffset;
		this.tOutside = i;
		this.skip = skip;
		return;
	    case '[':
		loop: for (;; i++) {
		    if (i == end) {
			throw new PatternSyntaxException("malformed character class");
		    }
		    final char c1 = data[i];
		    switch (c1) {
		    case '\\':
			i++;
			continue;
		    case ']':
			break loop;
		    }
		}
	    }
	}
	this.ttype = Pretokenizer.END;
	this.tOffset = tOffset;
	this.tOutside = end;
    }
}

class Branch extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    Branch() {
	this.type = Term.BRANCH;
    }

    Branch(final int type) {
	switch (type) {
	case BRANCH:
	case BRANCH_STORE_CNT:
	case BRANCH_STORE_CNT_AUX1:
	    this.type = type;
	    break;
	default:
	    throw new IllegalArgumentException("not a branch type: " + type);
	}
    }
}

class BackReference extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    BackReference(final int no, final boolean icase, final boolean reverse, final boolean bracket) {
	super(icase ? Term.REG_I : Term.REG);
	this.mode_reverse = reverse;
	this.mode_bracket = bracket;
	this.mode_insensitive = icase;
	this.memreg = no;
    }
}

class Group extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    Group() {
	this(0);
    }

    Group(final int memreg) {
	this.type = Term.GROUP_IN;
	this.memreg = memreg;
	// used in append()
	this.current = null;
	this.in = this;
	this.prev = null;
	this.out = new Term();
	this.out.type = Term.GROUP_OUT;
	this.out.memreg = memreg;
    }
}

class ConditionalExpr extends Group implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;
    private Term node;
    private boolean newBranchStarted = false;
    private boolean linkAsBranch = true;

    ConditionalExpr(final Lookahead la) {
	super(0);
	/*
	 * This all is rather tricky. See how this types are handled in Matcher. The
	 * shortcoming is that we strongly rely upon the internal structure of
	 * Lookahead.
	 */
	la.in.type = Term.LOOKAHEAD_CONDITION_IN;
	la.out.type = Term.LOOKAHEAD_CONDITION_OUT;
	if (la.isPositive) {
	    this.node = la.in;
	    this.linkAsBranch = true;
	    // empty 2'nd branch
	    this.node.failNext = this.out;
	} else {
	    this.node = la.out;
	    this.linkAsBranch = false;
	    // empty 2'nd branch
	    this.node.next = this.out;
	}
	// node.prev=in;
	// in.next=node;
	la.prev = this.in;
	this.in.next = la;
	this.current = la;
	// current=node;
    }

    ConditionalExpr(final Lookbehind lb) {
	super(0);
	/*
	 * This all is rather tricky. See how this types are handled in Matcher. The
	 * shortcoming is that we strongly rely upon the internal structure of
	 * Lookahead.
	 */
	lb.in.type = Term.LOOKBEHIND_CONDITION_IN;
	lb.out.type = Term.LOOKBEHIND_CONDITION_OUT;
	if (lb.isPositive) {
	    this.node = lb.in;
	    this.linkAsBranch = true;
	    // empty 2'nd branch
	    this.node.failNext = this.out;
	} else {
	    this.node = lb.out;
	    this.linkAsBranch = false;
	    // empty 2'nd branch
	    this.node.next = this.out;
	}
	lb.prev = this.in;
	this.in.next = lb;
	this.current = lb;
	// current=node;
    }

    ConditionalExpr(final int memreg) {
	super(0);
	final Term condition = new Term(Term.MEMREG_CONDITION);
	condition.memreg = memreg;
	condition.out = condition;
	condition.out1 = null;
	condition.branchOut = null;
	// default branch
	condition.failNext = this.out;
	this.node = this.current = condition;
	this.linkAsBranch = true;
	condition.prev = this.in;
	this.in.next = condition;
	this.current = condition;
    }

    @Override
    protected void startNewBranch() throws PatternSyntaxException {
	if (this.newBranchStarted) {
	    throw new PatternSyntaxException("attempt to set a 3'd choice in a conditional expr.");
	}
	final Term node = this.node;
	node.out1 = null;
	if (this.linkAsBranch) {
	    node.out = null;
	    node.branchOut = node;
	} else {
	    node.out = node;
	    node.branchOut = null;
	}
	this.newBranchStarted = true;
	this.current = node;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	if (!super.equals(o)) {
	    return false;
	}
	final ConditionalExpr that = (ConditionalExpr) o;
	return this.newBranchStarted == that.newBranchStarted && this.linkAsBranch == that.linkAsBranch
		&& (this.node != null ? this.node.equals(that.node) : that.node == null);
    }

    @Override
    public int hashCode() {
	int result = super.hashCode();
	result = 31 * result + (this.node != null ? this.node.hashCode() : 0);
	result = 31 * result + (this.newBranchStarted ? 1 : 0);
	result = 31 * result + (this.linkAsBranch ? 1 : 0);
	return result;
    }
}

class IndependentGroup extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    IndependentGroup(final int id) {
	super(0);
	this.in = this;
	this.out = new Term();
	this.type = Term.INDEPENDENT_IN;
	this.out.type = Term.INDEPENDENT_OUT;
	this.lookaheadId = this.out.lookaheadId = id;
    }
}

class Lookahead extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;
    final boolean isPositive;

    Lookahead(final int id, final boolean isPositive) {
	this.isPositive = isPositive;
	this.in = this;
	this.out = new Term();
	if (isPositive) {
	    this.type = Term.PLOOKAHEAD_IN;
	    this.out.type = Term.PLOOKAHEAD_OUT;
	} else {
	    this.type = Term.NLOOKAHEAD_IN;
	    this.out.type = Term.NLOOKAHEAD_OUT;
	    this.branchOut = this;
	}
	this.lookaheadId = id;
	this.out.lookaheadId = id;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	if (!super.equals(o)) {
	    return false;
	}
	final Lookahead lookahead = (Lookahead) o;
	return this.isPositive == lookahead.isPositive;
    }

    @Override
    public int hashCode() {
	int result = super.hashCode();
	result = 31 * result + (this.isPositive ? 1 : 0);
	return result;
    }
}

class Lookbehind extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;
    final boolean isPositive;
    private int prevDistance = -1;

    Lookbehind(final int id, final boolean isPositive) {
	this.distance = 0;
	this.isPositive = isPositive;
	this.in = this;
	this.out = new Term();
	if (isPositive) {
	    this.type = Term.PLOOKBEHIND_IN;
	    this.out.type = Term.PLOOKBEHIND_OUT;
	} else {
	    this.type = Term.NLOOKBEHIND_IN;
	    this.out.type = Term.NLOOKBEHIND_OUT;
	    this.branchOut = this;
	}
	this.lookaheadId = id;
	this.out.lookaheadId = id;
    }

    @Override
    protected Term append(final Term t) throws PatternSyntaxException {
	this.distance += Lookbehind.length(t);
	return super.append(t);
    }

    @Override
    protected Term replaceCurrent(final Term t) throws PatternSyntaxException {
	this.distance += Lookbehind.length(t) - Lookbehind.length(this.current);
	return super.replaceCurrent(t);
    }

    private static int length(final Term t) throws PatternSyntaxException {
	final int type = t.type;
	switch (type) {
	case CHAR:
	case BITSET:
	case BITSET2:
	case ANY_CHAR:
	case ANY_CHAR_NE:
	    return 1;
	case BOUNDARY:
	case DIRECTION:
	case UBOUNDARY:
	case UDIRECTION:
	    return 0;
	default:
	    if (type >= Term.FIRST_TRANSPARENT && type <= Term.LAST_TRANSPARENT) {
		return 0;
	    }
	    throw new PatternSyntaxException("variable length element within a lookbehind assertion");
	}
    }

    @Override
    protected void startNewBranch() throws PatternSyntaxException {
	this.prevDistance = this.distance;
	this.distance = 0;
	super.startNewBranch();
    }

    @Override
    protected void close() throws PatternSyntaxException {
	final int pd = this.prevDistance;
	if (pd >= 0) {
	    if (this.distance != pd) {
		throw new PatternSyntaxException("non-equal branch lengths within a lookbehind assertion");
	    }
	}
	super.close();
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	if (!super.equals(o)) {
	    return false;
	}
	final Lookbehind that = (Lookbehind) o;
	return this.isPositive == that.isPositive && this.prevDistance == that.prevDistance;
    }

    @Override
    public int hashCode() {
	int result = super.hashCode();
	result = 31 * result + (this.isPositive ? 1 : 0);
	result = 31 * result + this.prevDistance;
	return result;
    }
}

class TermIterator extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    TermIterator(final Term term, final int min, final int max, final ArrayList<TermIterator> collection)
	    throws PatternSyntaxException {
	collection.add(this);
	switch (term.type) {
	case CHAR:
	case ANY_CHAR:
	case ANY_CHAR_NE:
	case BITSET:
	case BITSET2: {
	    this.target = term;
	    final Term back = new Term();
	    if (min <= 0 && max < 0) {
		this.type = Term.REPEAT_0_INF;
		back.type = Term.BACKTRACK_0;
	    } else if (min > 0 && max < 0) {
		this.type = Term.REPEAT_MIN_INF;
		back.type = Term.BACKTRACK_MIN;
		this.minCount = back.minCount = min;
	    } else {
		this.type = Term.REPEAT_MIN_MAX;
		back.type = Term.BACKTRACK_MIN;
		this.minCount = back.minCount = min;
		this.maxCount = max;
	    }
	    this.failNext = back;
	    this.in = this;
	    this.out = this;
	    this.out1 = back;
	    this.branchOut = null;
	    return;
	}
	case REG: {
	    this.target = term;
	    this.memreg = term.memreg;
	    final Term back = new Term();
	    if (max < 0) {
		this.type = Term.REPEAT_REG_MIN_INF;
		back.type = Term.BACKTRACK_REG_MIN;
		this.minCount = back.minCount = min;
	    } else {
		this.type = Term.REPEAT_REG_MIN_MAX;
		back.type = Term.BACKTRACK_REG_MIN;
		this.minCount = back.minCount = min;
		this.maxCount = max;
	    }
	    this.failNext = back;
	    this.in = this;
	    this.out = this;
	    this.out1 = back;
	    this.branchOut = null;
	    return;
	}
	default:
	    throw new PatternSyntaxException("can't iterate this type: " + term.type);
	}
    }

    void optimize() {
//BACKTRACK_MIN_REG_FIND
	final Term back = this.failNext;
	final Optimizer opt = Optimizer.find(back.next);
	if (opt == null) {
	    return;
	}
	this.failNext = opt.makeBacktrack(back);
    }
}
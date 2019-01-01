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

import java.util.Arrays;

import regexodus.ds.IntBitSet;

class BlockSet implements UnicodeConstants {
    /*
     * private static final Block[][] categoryBits = new
     * Block[CATEGORY_COUNT][BLOCK_COUNT];
     *
     * static { for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
     * int cat = Character.getType((char) i); int blockNo = (i >> 8) & 0xff; Block b
     * = categoryBits[cat][blockNo]; if (b == null) categoryBits[cat][blockNo] = b =
     * new Block(); b.set(i & 0xff); } }
     */
    private boolean positive = true;
    private boolean isLarge = false;
    private IntBitSet block0 = new IntBitSet(); // 1-byte bit set
    private static final IntBitSet emptyBlock0 = new IntBitSet();
    private Block[] blocks; // 2-byte bit set
    private int weight;

    final void reset() {
	this.positive = true;
	this.block0 = null;
	this.blocks = null;
	this.isLarge = false;
	this.weight = 0;
    }

    static void unify(final BlockSet bs, final Term term) {
	if (bs.isLarge) {
	    term.type = Term.BITSET2;
	    term.bitset2 = Block.toBitset2(bs.blocks);
	} else {
	    term.type = Term.BITSET;
	    term.bitset = bs.block0 == null ? BlockSet.emptyBlock0 : bs.block0;
	}
	term.inverse = !bs.positive;
	term.weight = bs.positive ? bs.weight : UnicodeConstants.MAX_WEIGHT - bs.weight;
    }

    final void setPositive(final boolean b) {
	this.positive = b;
    }

    final boolean isPositive() {
	return this.positive;
    }

    final boolean isLarge() {
	return this.isLarge;
    }

    private void enableLargeMode() {
	if (this.isLarge) {
	    return;
	}
	final Block[] blocks = new Block[UnicodeConstants.BLOCK_COUNT];
	this.blocks = blocks;
	if (this.block0 != null) {
	    blocks[0] = new Block(this.block0);
	}
	this.isLarge = true;
    }

    private int getWeight() {
	return this.positive ? this.weight : UnicodeConstants.MAX_WEIGHT - this.weight;
    }

    final void setWordChar(final boolean unicode) {
	if (unicode) {
	    if (!this.isLarge) {
		this.enableLargeMode();
	    }
	    this.weight += Block.add(this.blocks, Category.Word.blocks, 0, UnicodeConstants.BLOCK_COUNT - 1, false);
	    /*
	     * setCategory("Lu"); setCategory("Ll"); setCategory("Lt"); setCategory("Lo");
	     * setCategory("Nd"); setChar('_');
	     */
	} else {
	    this.setRange('a', 'z');
	    this.setRange('A', 'Z');
	    this.setRange('0', '9');
	    this.setChar('_');
	}
    }

    final void setDigit(final boolean unicode) {
	if (unicode) {
	    this.setCategory("Nd");
	} else {
	    this.setRange('0', '9');
	}
    }

    final void setSpace(final boolean unicode) {
	if (unicode) {
	    this.setCategory("G");
	} else {
	    this.setChar(' ');
	    this.setChar('\r');
	    this.setChar('\n');
	    this.setChar('\t');
	    this.setChar('\f');
	}
    }

    final void setHorizontalSpace(final boolean unicode) {
	if (unicode) {
	    this.setCategory("Gh");
	} else {
	    this.setChar(' ');
	    this.setChar('\t');
	}
    }

    final void setVerticalSpace(final boolean unicode) {
	if (unicode) {
	    this.setCategory("Gv");
	} else {
	    this.setChar('\n');
	    this.setChar('\r');
	    this.setChar('\f');
	    this.setChar('\u000B');
	}
    }

    final void setCategory(final String c) {
	if (!this.isLarge) {
	    this.enableLargeMode();
	}
	final Block[] catBits = Category.categories.get(c).blocks;
	this.weight += Block.add(this.blocks, catBits, 0, UnicodeConstants.BLOCK_COUNT - 1, false);
//System.out.println("["+this+"].setCategory("+c+"): weight="+weight);
    }

    final void setChars(final String chars) {
	for (int i = chars.length() - 1; i >= 0; i--) {
	    this.setChar(chars.charAt(i));
	}
    }

    final void setChar(final char c) {
	this.setRange(c, c);
    }

    final void setRange(final char c1, final char c2) {
//System.out.println("["+this+"].setRange("+c1+","+c2+"):");
//if(c1>31 && c1<=126 && c2>31 && c2<=126) System.out.println("setRange('"+c1+"','"+c2+"'):");
//else System.out.println("setRange(["+Integer.toHexString(c1)+"],["+Integer.toHexString(c2)+"]):");
	if (c2 >= 256 || this.isLarge) {
	    int s = 0;
	    if (!this.isLarge) {
		this.enableLargeMode();
	    }
	    final Block[] blocks = this.blocks;
	    for (int c = c1; c <= c2; c++) {
		final int i2 = c >> 8 & 0xff;
		final int i = c & 0xff;
		Block block = blocks[i2];
		if (block == null) {
		    blocks[i2] = block = new Block();
		}
		if (block.set(i)) {
		    s++;
		}
	    }
	    this.weight += s;
	} else {
	    IntBitSet block0 = this.block0;
	    if (block0 == null) {
		this.block0 = block0 = new IntBitSet();
	    }
	    this.weight += BlockSet.set(block0, c1, c2);
	}
    }

    final void add(final BlockSet bs) {
	this.add(bs, false);
    }

    final void add(final BlockSet bs, final boolean inverse) {
	this.weight += BlockSet.addImpl(this, bs, !bs.positive ^ inverse);
    }

    private static int addImpl(final BlockSet bs1, final BlockSet bs2, final boolean inv) {
	int s = 0;
	if (!bs1.isLarge && !bs2.isLarge && !inv) {
	    if (bs2.block0 != null) {
		IntBitSet bits = bs1.block0;
		if (bits == null) {
		    bs1.block0 = bits = new IntBitSet();
		}
		s += BlockSet.add(bits, bs2.block0, 0, UnicodeConstants.BLOCK_SIZE - 1, false);
	    }
	} else {
	    if (!bs1.isLarge) {
		bs1.enableLargeMode();
	    }
	    if (!bs2.isLarge) {
		bs2.enableLargeMode();
	    }
	    s += Block.add(bs1.blocks, bs2.blocks, 0, UnicodeConstants.BLOCK_COUNT - 1, inv);
	}
	return s;
    }

    final void subtract(final BlockSet bs) {
	this.subtract(bs, false);
    }

    private void subtract(final BlockSet bs, final boolean inverse) {
//System.out.println("["+this+"].subtract(["+bs+"],"+inverse+"):");
	this.weight += BlockSet.subtractImpl(this, bs, !bs.positive ^ inverse);
    }

    private static int subtractImpl(final BlockSet bs1, final BlockSet bs2, final boolean inv) {
	int s = 0;
	if (!bs1.isLarge && !bs2.isLarge && !inv) {
	    IntBitSet bits1, bits2;
	    if ((bits2 = bs2.block0) != null) {
		bits1 = bs1.block0;
		if (bits1 == null) {
		    return 0;
		}
		s += BlockSet.subtract(bits1, bits2, false);
	    }
	} else {
	    if (!bs1.isLarge) {
		bs1.enableLargeMode();
	    }
	    if (!bs2.isLarge) {
		bs2.enableLargeMode();
	    }
	    s += Block.subtract(bs1.blocks, bs2.blocks, 0, UnicodeConstants.BLOCK_COUNT - 1, inv);
	}
	return s;
    }

    final void intersect(final BlockSet bs) {
	this.intersect(bs, false);
    }

    private void intersect(final BlockSet bs, final boolean inverse) {
//System.out.println("["+this+"].intersect(["+bs+"],"+inverse+"):");
	this.subtract(bs, !inverse);
    }

    static int add(final IntBitSet bs1, final IntBitSet bs2, final int from, final int to, final boolean inv) {
	final int s = bs1.cardinality();
	if (inv) {
	    bs1.or(bs2.clone().negate());
	} else {
	    bs1.or(bs2);
	}
	return bs1.cardinality() - s;
    }

    static int subtract(final IntBitSet bs1, final IntBitSet bs2, final boolean inv) {
	final int s = -bs1.cardinality();
	if (inv) {
	    bs1.andNot(bs2.clone().negate());
	} else {
	    bs1.andNot(bs2);
	}
	return s + bs1.cardinality();
    }

    private static int set(final IntBitSet arr, final int from, final int to) {
	final int s = arr.cardinality();
	arr.set(from, to);
	return arr.cardinality() - s;
    }

    @Override
    public String toString() {
	final StringBuilder sb = new StringBuilder();
	if (!this.positive) {
	    sb.append('^');
	}
	if (this.isLarge) {
	    sb.append(CharacterClass.stringValue2(Block.toBitset2(this.blocks)));
	} else if (this.block0 != null) {
	    sb.append(CharacterClass.stringValue0(this.block0));
	}
	sb.append('(');
	sb.append(this.getWeight());
	sb.append(')');
	return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final BlockSet blockSet = (BlockSet) o;
	if (this.positive != blockSet.positive) {
	    return false;
	}
	if (this.isLarge != blockSet.isLarge) {
	    return false;
	}
	if (this.weight != blockSet.weight) {
	    return false;
	}
	if (this.block0 != null ? !this.block0.equals(blockSet.block0) : blockSet.block0 != null) {
	    return false;
	}
	// Probably incorrect - comparing Object[] arrays with Arrays.equals
	return Arrays.equals(this.blocks, blockSet.blocks);
    }

    @Override
    public int hashCode() {
	int result = this.positive ? 1 : 0;
	result = 31 * result + (this.isLarge ? 1 : 0);
	result = 31 * result + (this.block0 != null ? this.block0.hashCode() : 0);
	result = 31 * result + Arrays.hashCode(this.blocks);
	result = 31 * result + this.weight;
	return result;
    }
    /*
     * public static void main(String[] args){ //System.out.print("blocks(Lu)=");
     * //System.out.println(CharacterClass.stringValue2(Block.toBitset2(categoryBits
     * [Lu])));
     * //System.out.println("[1][0].get('a')="+categoryBits[1][0].get('a'));
     * //System.out.println("[1][0].get('A')="+categoryBits[1][0].get('A'));
     * //System.out.println("[1][0].get(65)="+categoryBits[1][0].get(65));
     * //System.out.println(""+categoryBits[1][0].get('A')); BlockSet b1=new
     * BlockSet(); //b1.setCategory(Lu); //b1.enableLargeMode();
     * b1.setRange('a','z'); b1.setRange('\u00E0','\u00FF');
     *
     * BlockSet b2=new BlockSet(); //b2.setCategory(Ll); //b2.enableLargeMode();
     * b2.setRange('A','Z'); b2.setRange('\u00C0','\u00DF');
     *
     * BlockSet b=new BlockSet(); //bs.setRange('a','z'); //bs.setRange('A','Z');
     * b.add(b1); b.add(b2,true);
     *
     * System.out.println("b1="+b1); System.out.println("b2="+b2);
     * System.out.println("b=b1+^b2="+b);
     *
     * b.subtract(b1,true);
     *
     * System.out.println("(b1+^b2)-^b1="+b);
     *
     * }
     */
}

class Block implements UnicodeConstants {
    private boolean isFull;
    // private boolean[] bits;
    private IntBitSet bits;
    private boolean shared = false;

    Block() {
    }

    Block(final IntBitSet bits) {
	this.bits = bits;
	this.shared = true;
    }

    final boolean set(final int c) {
//System.out.println("Block.add("+CharacterClass.stringValue2(toBitset2(targets))+","+CharacterClass.stringValue2(toBitset2(addends))+","+from*BLOCK_SIZE+","+to*BLOCK_SIZE+","+inv+"):");
	if (this.isFull) {
	    return false;
	}
	IntBitSet bits = this.bits;
	if (bits == null) {
	    this.bits = bits = new IntBitSet();
	    this.shared = false;
	    bits.set(c);
	    return true;
	}
	if (bits.get(c)) {
	    return false;
	}
	if (this.shared) {
	    bits = Block.copyBits(this);
	}
	bits.set(c);
	return true;
    }

    final boolean get(final int c) {
	return this.isFull || this.bits != null && this.bits.get(c);
    }

    static int add(final Block[] targets, final Block[] addends, final int from, final int to, final boolean inv) {
	int s = 0;
	for (int i = from; i <= to; i++) {
	    final Block addend = addends[i];
	    if (addend == null) {
		if (!inv) {
		    continue;
		}
	    } else if (addend.isFull && inv) {
		continue;
	    }
	    Block target = targets[i];
	    if (target == null) {
		targets[i] = target = new Block();
	    } else if (target.isFull) {
		continue;
	    }
	    s += Block.add(target, addend, inv);
	}
	return s;
    }

    private static int add(final Block target, final Block addend, final boolean inv) {
	// there is provided that !target.isFull
	IntBitSet targetbits, addbits;
	if (addend == null) {
	    if (!inv) {
		return 0;
	    }
	    int s = UnicodeConstants.BLOCK_SIZE;
	    if ((targetbits = target.bits) != null) {
		s -= Block.count(targetbits, 0, UnicodeConstants.BLOCK_SIZE - 1);
	    }
	    target.isFull = true;
	    target.bits = null;
	    target.shared = false;
	    return s;
	} else if (addend.isFull) {
	    if (inv) {
		return 0;
	    }
	    int s = UnicodeConstants.BLOCK_SIZE;
	    if ((targetbits = target.bits) != null) {
		s -= Block.count(targetbits, 0, UnicodeConstants.BLOCK_SIZE - 1);
	    }
	    target.isFull = true;
	    target.bits = null;
	    target.shared = false;
	    return s;
	} else if ((addbits = addend.bits) == null) {
	    if (!inv) {
		return 0;
	    }
	    int s = UnicodeConstants.BLOCK_SIZE;
	    if ((targetbits = target.bits) != null) {
		s -= Block.count(targetbits, 0, UnicodeConstants.BLOCK_SIZE - 1);
	    }
	    target.isFull = true;
	    target.bits = null;
	    target.shared = false;
	    return s;
	} else {
	    if ((targetbits = target.bits) == null) {
		if (!inv) {
		    target.bits = addbits;
		    target.shared = true;
		    return Block.count(addbits, 0, UnicodeConstants.BLOCK_SIZE - 1);
		} else {
		    target.bits = targetbits = Block.emptyBits(null);
		    target.shared = false;
		    return BlockSet.add(targetbits, addbits, 0, UnicodeConstants.BLOCK_SIZE - 1, inv);
		}
	    } else {
		if (target.shared) {
		    targetbits = Block.copyBits(target);
		}
		return BlockSet.add(targetbits, addbits, 0, UnicodeConstants.BLOCK_SIZE - 1, inv);
	    }
	}
    }

    static int subtract(final Block[] targets, final Block[] subtrahends, final int from, final int to,
	    final boolean inv) {
	int s = 0;
	for (int i = from; i <= to; i++) {
	    final Block target = targets[i];
	    if (target == null || !target.isFull && target.bits == null) {
		continue;
	    }
	    final Block subtrahend = subtrahends[i];
	    if (subtrahend == null) {
		if (inv) {
		    if (target.isFull) {
			s -= UnicodeConstants.BLOCK_SIZE;
		    } else {
			s -= Block.count(target.bits, 0, UnicodeConstants.BLOCK_SIZE - 1);
		    }
		    target.isFull = false;
		    target.bits = null;
		    target.shared = false;
		}
	    } else {
		s += Block.subtract(target, subtrahend, inv);
	    }
	}
	return s;
    }

    private static int subtract(final Block target, final Block subtrahend, final boolean inv) {
	IntBitSet targetbits, subbits;
	// there is provided that target.isFull or target.bits!=null
	if (subtrahend.isFull) {
	    if (inv) {
		return 0;
	    }
	    int s = 0;
	    if (target.isFull) {
		s = UnicodeConstants.BLOCK_SIZE;
	    } else {
		s = target.bits.cardinality();
	    }
	    target.isFull = false;
	    target.bits = null;
	    target.shared = false;
	    return s;
	} else if ((subbits = subtrahend.bits) == null) {
	    if (!inv) {
		return 0;
	    }
	    int s = 0;
	    if (target.isFull) {
		s = UnicodeConstants.BLOCK_SIZE;
	    } else {
		s = target.bits.cardinality();
	    }
	    target.isFull = false;
	    target.bits = null;
	    target.shared = false;
	    return s;
	} else {
	    if (target.isFull) {
		final IntBitSet bits = Block.fullBits(target.bits);
		final int s = BlockSet.subtract(bits, subbits, inv);
		target.isFull = false;
		target.shared = false;
		target.bits = bits;
		return s;
	    } else {
		if (target.shared) {
		    targetbits = Block.copyBits(target);
		} else {
		    targetbits = target.bits;
		}
		return BlockSet.subtract(targetbits, subbits, inv);
	    }
	}
    }

    private static IntBitSet copyBits(final Block block) {
	final IntBitSet bits = block.bits.clone();
	block.bits = bits;
	block.shared = false;
	return bits;
    }

    private static IntBitSet fullBits(IntBitSet bits) {
	if (bits == null) {
	    bits = new IntBitSet();
	}
	bits.set(0, UnicodeConstants.BLOCK_SIZE);
	return bits;
    }

    private static IntBitSet emptyBits(IntBitSet bits) {
	if (bits == null) {
	    bits = new IntBitSet();
	} else {
	    bits.clear();
	}
	return bits;
    }

    private static int count(final IntBitSet arr, final int from, final int to) {
	int s = 0;
	for (int i = from; i <= to; i++) {
	    if (arr.get(i)) {
		s++;
	    }
	}
	return s;
    }

    static IntBitSet[] toBitset2(final Block[] blocks) {
	final int len = blocks.length;
	final IntBitSet[] result = new IntBitSet[len];
	for (int i = 0; i < len; i++) {
	    final Block block = blocks[i];
	    if (block == null) {
		continue;
	    }
	    if (block.isFull) {
		result[i] = Block.FULL_BITS;
	    } else {
		result[i] = block.bits;
	    }
	}
	return result;
    }

    private final static IntBitSet EMPTY_BITS = new IntBitSet();
    private final static IntBitSet FULL_BITS = new IntBitSet();

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final Block block = (Block) o;
	if (this.isFull != block.isFull) {
	    return false;
	}
	if (this.shared != block.shared) {
	    return false;
	}
	return this.bits != null ? this.bits.equals(block.bits) : block.bits == null;
    }

    @Override
    public int hashCode() {
	int result = this.isFull ? 1 : 0;
	result = 31 * result + (this.bits != null ? this.bits.hashCode() : 0);
	result = 31 * result + (this.shared ? 1 : 0);
	return result;
    }

    @Override
    public String toString() {
	return "Block{" + "isFull=" + this.isFull + ", bits=" + this.bits + ", shared=" + this.shared + '}';
    }

    static {
	Block.FULL_BITS.set(0, UnicodeConstants.BLOCK_SIZE - 1);
    }
}

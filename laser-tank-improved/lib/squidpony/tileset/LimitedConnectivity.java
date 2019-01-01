/* File generated automatically by TilesetsGenerator.java. Do not edit. This file is committed for convenience. */
package squidpony.tileset;

import squidpony.squidgrid.mapping.styled.Tile;
import squidpony.squidgrid.mapping.styled.Tileset;

/** @author TilesetsGenerator.java */
public class LimitedConnectivity {
    public static final Tileset INSTANCE = new Tileset();
    static {
	/* Initialize #INSTANCE */
	LimitedConnectivity.INSTANCE.config.is_corner = true;
	LimitedConnectivity.INSTANCE.config.num_x_variants = 1;
	LimitedConnectivity.INSTANCE.config.num_y_variants = 1;
	LimitedConnectivity.INSTANCE.config.short_side_length = 9;
	LimitedConnectivity.INSTANCE.config.num_colors[0] = 2;
	LimitedConnectivity.INSTANCE.config.num_colors[1] = 2;
	LimitedConnectivity.INSTANCE.config.num_colors[2] = 2;
	LimitedConnectivity.INSTANCE.config.num_colors[3] = 2;
	LimitedConnectivity.INSTANCE.max_tiles.h = 64;
	LimitedConnectivity.INSTANCE.max_tiles.v = 64;
	LimitedConnectivity.INSTANCE.h_tiles = new Tile[64];
	/* Build h_tiles #0 */
	LimitedConnectivity.INSTANCE.h_tiles[0] = new Tile(0, 0, 0, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #1 */
	LimitedConnectivity.INSTANCE.h_tiles[1] = new Tile(1, 0, 0, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #2 */
	LimitedConnectivity.INSTANCE.h_tiles[2] = new Tile(0, 1, 0, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #3 */
	LimitedConnectivity.INSTANCE.h_tiles[3] = new Tile(1, 1, 0, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #4 */
	LimitedConnectivity.INSTANCE.h_tiles[4] = new Tile(0, 0, 1, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #5 */
	LimitedConnectivity.INSTANCE.h_tiles[5] = new Tile(1, 0, 1, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #6 */
	LimitedConnectivity.INSTANCE.h_tiles[6] = new Tile(0, 1, 1, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #7 */
	LimitedConnectivity.INSTANCE.h_tiles[7] = new Tile(1, 1, 1, 0, 0, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #8 */
	LimitedConnectivity.INSTANCE.h_tiles[8] = new Tile(0, 0, 0, 1, 0, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #9 */
	LimitedConnectivity.INSTANCE.h_tiles[9] = new Tile(1, 0, 0, 1, 0, 0, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #10 */
	LimitedConnectivity.INSTANCE.h_tiles[10] = new Tile(0, 1, 0, 1, 0, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #11 */
	LimitedConnectivity.INSTANCE.h_tiles[11] = new Tile(1, 1, 0, 1, 0, 0, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #12 */
	LimitedConnectivity.INSTANCE.h_tiles[12] = new Tile(0, 0, 1, 1, 0, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #13 */
	LimitedConnectivity.INSTANCE.h_tiles[13] = new Tile(1, 0, 1, 1, 0, 0, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #14 */
	LimitedConnectivity.INSTANCE.h_tiles[14] = new Tile(0, 1, 1, 1, 0, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #15 */
	LimitedConnectivity.INSTANCE.h_tiles[15] = new Tile(1, 1, 1, 1, 0, 0, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #16 */
	LimitedConnectivity.INSTANCE.h_tiles[16] = new Tile(0, 0, 0, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #17 */
	LimitedConnectivity.INSTANCE.h_tiles[17] = new Tile(1, 0, 0, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #18 */
	LimitedConnectivity.INSTANCE.h_tiles[18] = new Tile(0, 1, 0, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #19 */
	LimitedConnectivity.INSTANCE.h_tiles[19] = new Tile(1, 1, 0, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #20 */
	LimitedConnectivity.INSTANCE.h_tiles[20] = new Tile(0, 0, 1, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #21 */
	LimitedConnectivity.INSTANCE.h_tiles[21] = new Tile(1, 0, 1, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #22 */
	LimitedConnectivity.INSTANCE.h_tiles[22] = new Tile(0, 1, 1, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #23 */
	LimitedConnectivity.INSTANCE.h_tiles[23] = new Tile(1, 1, 1, 0, 1, 0, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #24 */
	LimitedConnectivity.INSTANCE.h_tiles[24] = new Tile(0, 0, 0, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #25 */
	LimitedConnectivity.INSTANCE.h_tiles[25] = new Tile(1, 0, 0, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #26 */
	LimitedConnectivity.INSTANCE.h_tiles[26] = new Tile(0, 1, 0, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #27 */
	LimitedConnectivity.INSTANCE.h_tiles[27] = new Tile(1, 1, 0, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #28 */
	LimitedConnectivity.INSTANCE.h_tiles[28] = new Tile(0, 0, 1, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #29 */
	LimitedConnectivity.INSTANCE.h_tiles[29] = new Tile(1, 0, 1, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #30 */
	LimitedConnectivity.INSTANCE.h_tiles[30] = new Tile(0, 1, 1, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #31 */
	LimitedConnectivity.INSTANCE.h_tiles[31] = new Tile(1, 1, 1, 1, 1, 0, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #32 */
	LimitedConnectivity.INSTANCE.h_tiles[32] = new Tile(0, 0, 0, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #33 */
	LimitedConnectivity.INSTANCE.h_tiles[33] = new Tile(1, 0, 0, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #34 */
	LimitedConnectivity.INSTANCE.h_tiles[34] = new Tile(0, 1, 0, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #35 */
	LimitedConnectivity.INSTANCE.h_tiles[35] = new Tile(1, 1, 0, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #36 */
	LimitedConnectivity.INSTANCE.h_tiles[36] = new Tile(0, 0, 1, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #37 */
	LimitedConnectivity.INSTANCE.h_tiles[37] = new Tile(1, 0, 1, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #38 */
	LimitedConnectivity.INSTANCE.h_tiles[38] = new Tile(0, 1, 1, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #39 */
	LimitedConnectivity.INSTANCE.h_tiles[39] = new Tile(1, 1, 1, 0, 0, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #40 */
	LimitedConnectivity.INSTANCE.h_tiles[40] = new Tile(0, 0, 0, 1, 0, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #41 */
	LimitedConnectivity.INSTANCE.h_tiles[41] = new Tile(1, 0, 0, 1, 0, 1, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 496L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #42 */
	LimitedConnectivity.INSTANCE.h_tiles[42] = new Tile(0, 1, 0, 1, 0, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #43 */
	LimitedConnectivity.INSTANCE.h_tiles[43] = new Tile(1, 1, 0, 1, 0, 1, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #44 */
	LimitedConnectivity.INSTANCE.h_tiles[44] = new Tile(0, 0, 1, 1, 0, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #45 */
	LimitedConnectivity.INSTANCE.h_tiles[45] = new Tile(1, 0, 1, 1, 0, 1, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #46 */
	LimitedConnectivity.INSTANCE.h_tiles[46] = new Tile(0, 1, 1, 1, 0, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 0L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #47 */
	LimitedConnectivity.INSTANCE.h_tiles[47] = new Tile(1, 1, 1, 1, 0, 1, 18, 9, 0, 0L, 0L, 56L, 504L, 56L, 0L, 0L,
		0L, 0L, 0L, 0L, 0L, 511L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #48 */
	LimitedConnectivity.INSTANCE.h_tiles[48] = new Tile(0, 0, 0, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #49 */
	LimitedConnectivity.INSTANCE.h_tiles[49] = new Tile(1, 0, 0, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #50 */
	LimitedConnectivity.INSTANCE.h_tiles[50] = new Tile(0, 1, 0, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #51 */
	LimitedConnectivity.INSTANCE.h_tiles[51] = new Tile(1, 1, 0, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #52 */
	LimitedConnectivity.INSTANCE.h_tiles[52] = new Tile(0, 0, 1, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #53 */
	LimitedConnectivity.INSTANCE.h_tiles[53] = new Tile(1, 0, 1, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #54 */
	LimitedConnectivity.INSTANCE.h_tiles[54] = new Tile(0, 1, 1, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 511L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #55 */
	LimitedConnectivity.INSTANCE.h_tiles[55] = new Tile(1, 1, 1, 0, 1, 1, 18, 9, 16, 16L, 16L, 16L, 496L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #56 */
	LimitedConnectivity.INSTANCE.h_tiles[56] = new Tile(0, 0, 0, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #57 */
	LimitedConnectivity.INSTANCE.h_tiles[57] = new Tile(1, 0, 0, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #58 */
	LimitedConnectivity.INSTANCE.h_tiles[58] = new Tile(0, 1, 0, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #59 */
	LimitedConnectivity.INSTANCE.h_tiles[59] = new Tile(1, 1, 0, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 16L, 16L, 16L, 16L);
	/* Build h_tiles #60 */
	LimitedConnectivity.INSTANCE.h_tiles[60] = new Tile(0, 0, 1, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #61 */
	LimitedConnectivity.INSTANCE.h_tiles[61] = new Tile(1, 0, 1, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #62 */
	LimitedConnectivity.INSTANCE.h_tiles[62] = new Tile(0, 1, 1, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 511L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	/* Build h_tiles #63 */
	LimitedConnectivity.INSTANCE.h_tiles[63] = new Tile(1, 1, 1, 1, 1, 1, 18, 9, 0, 0L, 0L, 0L, 496L, 16L, 16L, 16L,
		16L, 16L, 16L, 16L, 16L, 31L, 0L, 0L, 0L, 0L);
	LimitedConnectivity.INSTANCE.v_tiles = new Tile[64];
	/* Build v_tiles #0 */
	LimitedConnectivity.INSTANCE.v_tiles[0] = new Tile(0, 0, 0, 0, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 8223L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #1 */
	LimitedConnectivity.INSTANCE.v_tiles[1] = new Tile(1, 0, 0, 0, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 8223L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #2 */
	LimitedConnectivity.INSTANCE.v_tiles[2] = new Tile(0, 1, 0, 0, 0, 0, 9, 18, 16, 16L, 16L, 28688L, 28703L,
		28688L, 8208L, 8208L, 8208L);
	/* Build v_tiles #3 */
	LimitedConnectivity.INSTANCE.v_tiles[3] = new Tile(1, 1, 0, 0, 0, 0, 9, 18, 16, 16L, 16L, 28688L, 28703L,
		28688L, 8208L, 8208L, 8208L);
	/* Build v_tiles #4 */
	LimitedConnectivity.INSTANCE.v_tiles[4] = new Tile(0, 0, 1, 0, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #5 */
	LimitedConnectivity.INSTANCE.v_tiles[5] = new Tile(1, 0, 1, 0, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #6 */
	LimitedConnectivity.INSTANCE.v_tiles[6] = new Tile(0, 1, 1, 0, 0, 0, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #7 */
	LimitedConnectivity.INSTANCE.v_tiles[7] = new Tile(1, 1, 1, 0, 0, 0, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #8 */
	LimitedConnectivity.INSTANCE.v_tiles[8] = new Tile(0, 0, 0, 1, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 8223L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #9 */
	LimitedConnectivity.INSTANCE.v_tiles[9] = new Tile(1, 0, 0, 1, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 8223L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #10 */
	LimitedConnectivity.INSTANCE.v_tiles[10] = new Tile(0, 1, 0, 1, 0, 0, 9, 18, 16, 16L, 16L, 28688L, 28703L,
		28688L, 8208L, 8208L, 8208L);
	/* Build v_tiles #11 */
	LimitedConnectivity.INSTANCE.v_tiles[11] = new Tile(1, 1, 0, 1, 0, 0, 9, 18, 16, 16L, 16L, 28688L, 28703L,
		28688L, 8208L, 8208L, 8208L);
	/* Build v_tiles #12 */
	LimitedConnectivity.INSTANCE.v_tiles[12] = new Tile(0, 0, 1, 1, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #13 */
	LimitedConnectivity.INSTANCE.v_tiles[13] = new Tile(1, 0, 1, 1, 0, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #14 */
	LimitedConnectivity.INSTANCE.v_tiles[14] = new Tile(0, 1, 1, 1, 0, 0, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #15 */
	LimitedConnectivity.INSTANCE.v_tiles[15] = new Tile(1, 1, 1, 1, 0, 0, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #16 */
	LimitedConnectivity.INSTANCE.v_tiles[16] = new Tile(0, 0, 0, 0, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 16383L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #17 */
	LimitedConnectivity.INSTANCE.v_tiles[17] = new Tile(1, 0, 0, 0, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 16383L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #18 */
	LimitedConnectivity.INSTANCE.v_tiles[18] = new Tile(0, 1, 0, 0, 1, 0, 9, 18, 16, 16L, 16L, 16L, 16383L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #19 */
	LimitedConnectivity.INSTANCE.v_tiles[19] = new Tile(1, 1, 0, 0, 1, 0, 9, 18, 16, 16L, 16L, 16L, 16383L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #20 */
	LimitedConnectivity.INSTANCE.v_tiles[20] = new Tile(0, 0, 1, 0, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #21 */
	LimitedConnectivity.INSTANCE.v_tiles[21] = new Tile(1, 0, 1, 0, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #22 */
	LimitedConnectivity.INSTANCE.v_tiles[22] = new Tile(0, 1, 1, 0, 1, 0, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #23 */
	LimitedConnectivity.INSTANCE.v_tiles[23] = new Tile(1, 1, 1, 0, 1, 0, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #24 */
	LimitedConnectivity.INSTANCE.v_tiles[24] = new Tile(0, 0, 0, 1, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 16383L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #25 */
	LimitedConnectivity.INSTANCE.v_tiles[25] = new Tile(1, 0, 0, 1, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 16383L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #26 */
	LimitedConnectivity.INSTANCE.v_tiles[26] = new Tile(0, 1, 0, 1, 1, 0, 9, 18, 16, 16L, 16L, 16L, 16383L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #27 */
	LimitedConnectivity.INSTANCE.v_tiles[27] = new Tile(1, 1, 0, 1, 1, 0, 9, 18, 16, 16L, 16L, 16L, 16383L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #28 */
	LimitedConnectivity.INSTANCE.v_tiles[28] = new Tile(0, 0, 1, 1, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #29 */
	LimitedConnectivity.INSTANCE.v_tiles[29] = new Tile(1, 0, 1, 1, 1, 0, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #30 */
	LimitedConnectivity.INSTANCE.v_tiles[30] = new Tile(0, 1, 1, 1, 1, 0, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #31 */
	LimitedConnectivity.INSTANCE.v_tiles[31] = new Tile(1, 1, 1, 1, 1, 0, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #32 */
	LimitedConnectivity.INSTANCE.v_tiles[32] = new Tile(0, 0, 0, 0, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #33 */
	LimitedConnectivity.INSTANCE.v_tiles[33] = new Tile(1, 0, 0, 0, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #34 */
	LimitedConnectivity.INSTANCE.v_tiles[34] = new Tile(0, 1, 0, 0, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #35 */
	LimitedConnectivity.INSTANCE.v_tiles[35] = new Tile(1, 1, 0, 0, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #36 */
	LimitedConnectivity.INSTANCE.v_tiles[36] = new Tile(0, 0, 1, 0, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #37 */
	LimitedConnectivity.INSTANCE.v_tiles[37] = new Tile(1, 0, 1, 0, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #38 */
	LimitedConnectivity.INSTANCE.v_tiles[38] = new Tile(0, 1, 1, 0, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #39 */
	LimitedConnectivity.INSTANCE.v_tiles[39] = new Tile(1, 1, 1, 0, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #40 */
	LimitedConnectivity.INSTANCE.v_tiles[40] = new Tile(0, 0, 0, 1, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #41 */
	LimitedConnectivity.INSTANCE.v_tiles[41] = new Tile(1, 0, 0, 1, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #42 */
	LimitedConnectivity.INSTANCE.v_tiles[42] = new Tile(0, 1, 0, 1, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #43 */
	LimitedConnectivity.INSTANCE.v_tiles[43] = new Tile(1, 1, 0, 1, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #44 */
	LimitedConnectivity.INSTANCE.v_tiles[44] = new Tile(0, 0, 1, 1, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #45 */
	LimitedConnectivity.INSTANCE.v_tiles[45] = new Tile(1, 0, 1, 1, 0, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 253983L,
		8208L, 8208L, 8208L, 8208L);
	/* Build v_tiles #46 */
	LimitedConnectivity.INSTANCE.v_tiles[46] = new Tile(0, 1, 1, 1, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #47 */
	LimitedConnectivity.INSTANCE.v_tiles[47] = new Tile(1, 1, 1, 1, 0, 1, 9, 18, 16, 16L, 16L, 16L, 253983L, 8208L,
		8208L, 8208L, 8208L);
	/* Build v_tiles #48 */
	LimitedConnectivity.INSTANCE.v_tiles[48] = new Tile(0, 0, 0, 0, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #49 */
	LimitedConnectivity.INSTANCE.v_tiles[49] = new Tile(1, 0, 0, 0, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #50 */
	LimitedConnectivity.INSTANCE.v_tiles[50] = new Tile(0, 1, 0, 0, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #51 */
	LimitedConnectivity.INSTANCE.v_tiles[51] = new Tile(1, 1, 0, 0, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #52 */
	LimitedConnectivity.INSTANCE.v_tiles[52] = new Tile(0, 0, 1, 0, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #53 */
	LimitedConnectivity.INSTANCE.v_tiles[53] = new Tile(1, 0, 1, 0, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #54 */
	LimitedConnectivity.INSTANCE.v_tiles[54] = new Tile(0, 1, 1, 0, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #55 */
	LimitedConnectivity.INSTANCE.v_tiles[55] = new Tile(1, 1, 1, 0, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #56 */
	LimitedConnectivity.INSTANCE.v_tiles[56] = new Tile(0, 0, 0, 1, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #57 */
	LimitedConnectivity.INSTANCE.v_tiles[57] = new Tile(1, 0, 0, 1, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #58 */
	LimitedConnectivity.INSTANCE.v_tiles[58] = new Tile(0, 1, 0, 1, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #59 */
	LimitedConnectivity.INSTANCE.v_tiles[59] = new Tile(1, 1, 0, 1, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #60 */
	LimitedConnectivity.INSTANCE.v_tiles[60] = new Tile(0, 0, 1, 1, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #61 */
	LimitedConnectivity.INSTANCE.v_tiles[61] = new Tile(1, 0, 1, 1, 1, 1, 9, 18, 8208, 8208L, 8208L, 8208L, 262143L,
		8192L, 8192L, 8192L, 8192L);
	/* Build v_tiles #62 */
	LimitedConnectivity.INSTANCE.v_tiles[62] = new Tile(0, 1, 1, 1, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
	/* Build v_tiles #63 */
	LimitedConnectivity.INSTANCE.v_tiles[63] = new Tile(1, 1, 1, 1, 1, 1, 9, 18, 16, 16L, 16L, 16L, 262143L, 8192L,
		8192L, 8192L, 8192L);
    }
}
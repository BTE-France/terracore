package net.buildtheearth.terracore.geo;

/**
 * The location of a tile in a 2D grid.
 *
 * @param x     the position of the tile along the x-axis
 * @param y     the position of the tile along the y-axis
 * @param zoom  the zoom level of the tile when the grid supports multiple resolutions, can be left 0 otherwise
 *
 * @author Smyler
 */
public record TileLocation(int x, int y, int zoom) {

    /**
     * Creates a new TileLocation with the given x and y coordinates and a zoom level of 0.
     * This constructor is provided for convenience when the zoom level is not relevant.
     *
     * @param x the position of the tile along the x-axis
     * @param y the position of the tile along the y-axis
     */
    public TileLocation(int x, int y) {
        this(x, y, 0);
    }

}

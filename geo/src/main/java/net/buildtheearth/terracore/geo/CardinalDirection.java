package net.buildtheearth.terracore.geo;

import lombok.Getter;
import lombok.NonNull;

import static net.buildtheearth.terracore.geo.GeoMaths.*;

/**
 * Represents one of the eight main cardinal and intercardinal directions.
 * <p>
 * Each direction covers a 45&deg; half-open azimuth sector [min, max), measured clockwise from North
 * (0&deg; = {@link #NORTH}, 90&deg; = {@link #EAST}, 180&deg; = {@link #SOUTH}, 270&deg; = {@link #WEST}).
 * In clockwise order:
 * <ul>
 *     <li>{@link #NORTH}</li>
 *     <li>{@link #NORTHEAST}</li>
 *     <li>{@link #EAST}</li>
 *     <li>{@link #SOUTHEAST}</li>
 *     <li>{@link #SOUTH}</li>
 *     <li>{@link #SOUTHWEST}</li>
 *     <li>{@link #WEST}</li>
 *     <li>{@link #NORTHWEST}</li>
 * </ul>
 *
 * @author Smyler, XboxBedrock
 */
@Getter
public enum CardinalDirection {

    /** North, centered on 0&deg;: azimuths in [337.5&deg;, 360&deg;) and [0&deg;, 22.5&deg;). */
    NORTH(337.5, 22.5),

    /** Northeast, centered on 45&deg;: azimuths in [22.5&deg;, 67.5&deg;). */
    NORTHEAST(22.5, 67.5),

    /** East, centered on 90&deg;: azimuths in [67.5&deg;, 112.5&deg;). */
    EAST(67.5, 112.5),

    /** Southeast, centered on 135&deg;: azimuths in [112.5&deg;, 157.5&deg;). */
    SOUTHEAST(112.5, 157.5),

    /** South, centered on 180&deg;: azimuths in [157.5&deg;, 202.5&deg;). */
    SOUTH(157.5, 202.5),

    /** Southwest, centered on 225&deg;: azimuths in [202.5&deg;, 247.5&deg;). */
    SOUTHWEST(202.5, 247.5),

    /** West, centered on 270&deg;: azimuths in [247.5&deg;, 292.5&deg;). */
    WEST(247.5, 292.5),

    /** Northwest, centered on 315&deg;: azimuths in [292.5&deg;, 337.5&deg;). */
    NORTHWEST(292.5, 337.5);

    /**
     * The start of this direction's azimuth sector in degrees, inclusive.
     * For {@link #NORTH} the sector wraps around 360&deg;/0&deg;, so this is 337.5 while {@link #maxDegrees} is 22.5.
     */
    private final double minDegrees;

    /**
     * The end of this direction's azimuth sector in degrees, exclusive.
     * For {@link #NORTH} the sector wraps around 360&deg;/0&deg;, so this is 22.5 while {@link #minDegrees} is 337.5.
     */
    private final double maxDegrees;

    CardinalDirection(double minDegrees, double maxDegrees) {
        this.minDegrees = minDegrees;
        this.maxDegrees = maxDegrees;
    }

    /**
     * Returns the cardinal direction closest to the given azimuth.
     *
     * @param angle an azimuth, measured clockwise from north, expressed in the given unit
     * @param unit  the unit {@code angle} is expressed in
     *
     * @return the {@link CardinalDirection} the given azimuth is the closest to
     *
     * @throws IllegalArgumentException if the given azimuth is not a finite number
     * @throws NullPointerException     if the given unit is null
     */
    public static CardinalDirection ofAzimuth(float angle, @NonNull AngleUnit unit) {
        angle = normalizeAzimuth(angle, unit);
        angle = unit.toDegrees(angle);
        for (CardinalDirection facingToBeTestedFor : CardinalDirection.values()) {
            if (facingToBeTestedFor == CardinalDirection.NORTH) {
                if (angle >= facingToBeTestedFor.minDegrees() && angle <= 360) {
                    return facingToBeTestedFor;
                } else if (angle >= 0 && angle < facingToBeTestedFor.maxDegrees()) {
                    return facingToBeTestedFor;
                }
            } else if (angle >= facingToBeTestedFor.minDegrees() && angle < facingToBeTestedFor.maxDegrees()) {
                return facingToBeTestedFor;
            }
        }
        throw new IllegalArgumentException("Invalid azimuth: " + angle);
    }

    /**
     * Checks whether the given azimuth faces this cardinal direction.
     *
     * @param azimuth an azimuth measured clockwise from north; any finite value is accepted
     * @param unit    the unit {@code azimuth} is expressed in
     *
     * @return true if the given azimuth faces this cardinal direction
     *
     * @throws IllegalArgumentException if the given azimuth is not a finite number
     * @throws NullPointerException     if the given unit is null
     */
    public boolean matches(float azimuth, @NonNull AngleUnit unit) {
        return ofAzimuth(azimuth, unit) == this;
    }

}

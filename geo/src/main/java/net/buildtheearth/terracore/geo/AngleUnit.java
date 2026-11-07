package net.buildtheearth.terracore.geo;

/**
 * A unit to measure angles, with conversion of angles to degrees and radians.
 *
 * @author Smyler
 */
public enum AngleUnit {

    /** The radian unit: a full turn is 2&pi; radians. */
    RADIANS,

    /** The degree unit: a full turn is 360&deg;. */
    DEGREES;

    /**
     * Converts an angle from this unit to degrees.
     * <br>
     * This is only guaranteed to be lossless for angles that are already in degrees.
     *
     * @param angle the angle to convert
     * @return the angle in degrees
     */
    public double toDegrees(double angle) {
        return switch (this) {
            case RADIANS -> Math.toDegrees(angle);
            case DEGREES -> angle;
        };
    }

    /**
     * Converts an angle from this unit to degrees.
     * <br>
     * This is only guaranteed to be lossless for angles that are already in degrees.
     *
     * @param angle the angle to convert
     * @return the angle in degrees
     */
    public float toDegrees(float angle) {
        return switch (this) {
            case RADIANS -> (float) Math.toDegrees(angle);
            case DEGREES -> angle;
        };
    }

    /**
     * Converts an angle from this unit to radians.
     * <br>
     * This is only guaranteed to be lossless for angles that are already in radians.
     *
     * @param angle the angle to convert
     * @return the angle in radians
     */
    public double toRadians(double angle) {
        return switch (this) {
            case RADIANS -> angle;
            case DEGREES -> Math.toRadians(angle);
        };
    }

    /**
     * Converts an angle from this unit to radians.
     * <br>
     * This is only guaranteed to be lossless for angles that are already in radians.
     *
     * @param angle the angle to convert
     * @return the angle in radians
     */
    public float toRadians(float angle) {
        return switch (this) {
            case RADIANS -> angle;
            case DEGREES -> (float) Math.toRadians(angle);
        };
    }

    /**
     * Converts an angle given in degrees to this unit.
     * <br>
     * This is only guaranteed to be lossless when this unit is {@link #DEGREES}.
     *
     * @param angle the angle in degrees
     * @return the angle in this unit
     */
    public double fromDegrees(double angle) {
        return switch (this) {
            case RADIANS -> Math.toRadians(angle);
            case DEGREES -> angle;
        };
    }

    /**
     * Converts an angle given in degrees to this unit.
     * <br>
     * This is only guaranteed to be lossless when this unit is {@link #DEGREES}.
     *
     * @param angle the angle in degrees
     * @return the angle in this unit
     */
    public float fromDegrees(float angle) {
        return switch (this) {
            case RADIANS -> (float) Math.toRadians(angle);
            case DEGREES -> angle;
        };
    }

    /**
     * Converts an angle given in radians to this unit.
     * <br>
     * This is only guaranteed to be lossless when this unit is {@link #RADIANS}.
     *
     * @param angle the angle in radians
     * @return the angle in this unit
     */
    public double fromRadians(double angle) {
        return switch (this) {
            case RADIANS -> angle;
            case DEGREES -> Math.toDegrees(angle);
        };
    }

    /**
     * Converts an angle given in radians to this unit.
     * <br>
     * This is only guaranteed to be lossless when this unit is {@link #RADIANS}.
     *
     * @param angle the angle in radians
     * @return the angle in this unit
     */
    public float fromRadians(float angle) {
        return switch (this) {
            case RADIANS -> angle;
            case DEGREES -> (float) Math.toDegrees(angle);
        };
    }

}

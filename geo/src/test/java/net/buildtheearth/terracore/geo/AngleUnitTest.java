package net.buildtheearth.terracore.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AngleUnitTest {

    private static final double PRECISION = 1e-7;

    @Test
    void toDegreesConvertsFromRadians() {
        assertEquals(0.0, AngleUnit.RADIANS.toDegrees(0.0), PRECISION);
        assertEquals(180.0, AngleUnit.RADIANS.toDegrees(Math.PI), PRECISION);
        assertEquals(90.0, AngleUnit.RADIANS.toDegrees(Math.PI / 2), PRECISION);
        assertEquals(360.0, AngleUnit.RADIANS.toDegrees(2 * Math.PI), PRECISION);
    }

    @Test
    void toDegreesIsIdentityForDegrees() {
        assertEquals(0.0, AngleUnit.DEGREES.toDegrees(0.0), PRECISION);
        assertEquals(45.5, AngleUnit.DEGREES.toDegrees(45.5), PRECISION);
        assertEquals(-720.25, AngleUnit.DEGREES.toDegrees(-720.25), PRECISION);
    }

    @Test
    void toRadiansConvertsFromDegrees() {
        assertEquals(0.0, AngleUnit.DEGREES.toRadians(0.0), PRECISION);
        assertEquals(Math.PI, AngleUnit.DEGREES.toRadians(180.0), PRECISION);
        assertEquals(Math.PI / 2, AngleUnit.DEGREES.toRadians(90.0), PRECISION);
        assertEquals(2 * Math.PI, AngleUnit.DEGREES.toRadians(360.0), PRECISION);
    }

    @Test
    void toRadiansIsIdentityForRadians() {
        assertEquals(0.0, AngleUnit.RADIANS.toRadians(0.0), PRECISION);
        assertEquals(1.25, AngleUnit.RADIANS.toRadians(1.25), PRECISION);
        assertEquals(-3 * Math.PI, AngleUnit.RADIANS.toRadians(-3 * Math.PI), PRECISION);
    }

    @Test
    void fromDegreesConvertsIntoTargetUnit() {
        assertEquals(Math.PI, AngleUnit.RADIANS.fromDegrees(180.0), PRECISION);
        assertEquals(90.0, AngleUnit.DEGREES.fromDegrees(90.0), PRECISION);
    }

    @Test
    void fromRadiansConvertsIntoTargetUnit() {
        assertEquals(180.0, AngleUnit.DEGREES.fromRadians(Math.PI), PRECISION);
        assertEquals(Math.PI, AngleUnit.RADIANS.fromRadians(Math.PI), PRECISION);
    }

    @Test
    void floatOverloadsMatchDoubleResults() {
        assertEquals((float) Math.toDegrees(Math.PI), AngleUnit.RADIANS.toDegrees((float) Math.PI), 1e-5);
        assertEquals((float) Math.toRadians(180.0), AngleUnit.DEGREES.toRadians(180f), 1e-5);
        assertEquals((float) Math.toRadians(180.0), AngleUnit.RADIANS.fromDegrees(180f), 1e-5);
        assertEquals((float) Math.toDegrees(Math.PI), AngleUnit.DEGREES.fromRadians((float) Math.PI), 1e-5);
        // identity for the matching unit, lossless
        assertEquals(42.5f, AngleUnit.DEGREES.toDegrees(42.5f), 0.0);
        assertEquals(42.5f, AngleUnit.RADIANS.toRadians(42.5f), 0.0);
        assertEquals(42.5f, AngleUnit.DEGREES.fromDegrees(42.5f), 0.0);
        assertEquals(42.5f, AngleUnit.RADIANS.fromRadians(42.5f), 0.0);
    }

}

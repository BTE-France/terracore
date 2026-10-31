package net.buildtheearth.terracore.geo;

import org.junit.jupiter.api.Test;

import static net.buildtheearth.terracore.geo.AngleUnit.*;
import static org.junit.jupiter.api.Assertions.*;

public class CardinalDirectionTest {

    @Test
    void matchesInterpretsRadians() {
        // 0 rad = north, pi/2 = east, pi = south, 3pi/2 = west
        assertTrue(CardinalDirection.NORTH.matches(0f, RADIANS));
        assertTrue(CardinalDirection.EAST.matches((float) (Math.PI / 2), RADIANS));
        assertTrue(CardinalDirection.SOUTH.matches((float) Math.PI, RADIANS));
        assertTrue(CardinalDirection.WEST.matches((float) (3 * Math.PI / 2), RADIANS));

        // intercardinal direction: 45 deg = pi/4
        assertTrue(CardinalDirection.NORTHEAST.matches((float) (Math.PI / 4), RADIANS));
        assertFalse(CardinalDirection.NORTH.matches((float) (Math.PI / 4), RADIANS));

        // values outside [0, 2pi) are normalized
        assertTrue(CardinalDirection.NORTH.matches((float) (-2 * Math.PI), RADIANS));
        assertTrue(CardinalDirection.EAST.matches((float) (Math.PI / 2 + 2 * Math.PI), RADIANS));
    }

    @Test
    void ofAzimuthReturnsExpectedDirection() {
        assertEquals(CardinalDirection.NORTH, CardinalDirection.ofAzimuth(0f, RADIANS));
        assertEquals(CardinalDirection.SOUTHEAST, CardinalDirection.ofAzimuth((float) (3 * Math.PI / 4), RADIANS));
        assertEquals(CardinalDirection.NORTHWEST, CardinalDirection.ofAzimuth((float) (7 * Math.PI / 4), RADIANS));

        // same azimuths expressed in degrees
        assertEquals(CardinalDirection.NORTH, CardinalDirection.ofAzimuth(0f, AngleUnit.DEGREES));
        assertEquals(CardinalDirection.SOUTHEAST, CardinalDirection.ofAzimuth(135f, AngleUnit.DEGREES));
        assertEquals(CardinalDirection.NORTHWEST, CardinalDirection.ofAzimuth(315f, AngleUnit.DEGREES));
        assertEquals(CardinalDirection.EAST, CardinalDirection.ofAzimuth(90f + 360f, AngleUnit.DEGREES));
    }

    @Test
    void boundaryAzimuthsBelongToNextSectorClockwise() {
        assertEquals(CardinalDirection.NORTHEAST, CardinalDirection.ofAzimuth(22.5f, AngleUnit.DEGREES));
        assertEquals(CardinalDirection.NORTH, CardinalDirection.ofAzimuth(337.5f, AngleUnit.DEGREES));
        assertEquals(CardinalDirection.EAST, CardinalDirection.ofAzimuth(67.5f, AngleUnit.DEGREES));
        // 360 wraps to 0, which is north
        assertEquals(CardinalDirection.NORTH, CardinalDirection.ofAzimuth(360f, AngleUnit.DEGREES));
    }

    @Test
    void nonFiniteAzimuthsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> CardinalDirection.NORTH.matches(Float.NaN, DEGREES));
        assertThrows(IllegalArgumentException.class, () -> CardinalDirection.NORTH.matches(Float.POSITIVE_INFINITY, DEGREES));
        assertThrows(IllegalArgumentException.class, () -> CardinalDirection.NORTH.matches(Float.NEGATIVE_INFINITY, DEGREES));
        assertThrows(IllegalArgumentException.class, () -> CardinalDirection.ofAzimuth(Float.NaN, RADIANS));
    }

}

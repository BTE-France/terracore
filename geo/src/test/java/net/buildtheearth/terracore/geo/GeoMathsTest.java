package net.buildtheearth.terracore.geo;

import org.junit.jupiter.api.Test;

import static net.buildtheearth.terracore.geo.GeoMaths.normalizeAzimuth;
import static org.junit.jupiter.api.Assertions.*;

public class GeoMathsTest {

    private static final double PRECISION = 1e-7;

    @Test
    void azimuthInRangeIsUnchanged() {
        assertEquals(0f, normalizeAzimuth(0f, AngleUnit.DEGREES), PRECISION);
        assertEquals(45f, normalizeAzimuth(45f, AngleUnit.DEGREES), PRECISION);
        assertEquals(359.9f, normalizeAzimuth(359.9f, AngleUnit.DEGREES), PRECISION);
    }

    @Test
    void azimuthAbove360WrapsAround() {
        assertEquals(0f, normalizeAzimuth(360f, AngleUnit.DEGREES), PRECISION);
        assertEquals(1f, normalizeAzimuth(361f, AngleUnit.DEGREES), PRECISION);
        assertEquals(0f, normalizeAzimuth(720f, AngleUnit.DEGREES), PRECISION);
        assertEquals(0.25f, normalizeAzimuth(1080.25f, AngleUnit.DEGREES), PRECISION);
    }

    @Test
    void negativeAzimuthWrapsAround() {
        assertEquals(359f, normalizeAzimuth(-1f, AngleUnit.DEGREES), PRECISION);
        assertEquals(270f, normalizeAzimuth(-90f, AngleUnit.DEGREES), PRECISION);
        assertEquals(0f, normalizeAzimuth(-360f, AngleUnit.DEGREES), PRECISION);
        assertEquals(359f, normalizeAzimuth(-361f, AngleUnit.DEGREES), PRECISION);
        assertEquals(359.5f, normalizeAzimuth(-720.5f, AngleUnit.DEGREES), PRECISION);
    }

    @Test
    void azimuthInRadiansIsNormalizedToOneTurn() {
        // looser tolerance: converting float radians -> degrees -> radians loses precision
        final double radPrecision = 1e-5;
        float tau = (float) (2 * Math.PI);
        assertEquals(0f, normalizeAzimuth(0f, AngleUnit.RADIANS), radPrecision);
        assertEquals((float) Math.PI, normalizeAzimuth((float) Math.PI, AngleUnit.RADIANS), radPrecision);
        assertEquals(0f, normalizeAzimuth(tau, AngleUnit.RADIANS), radPrecision);
        assertEquals((float) (Math.PI / 2), normalizeAzimuth(tau + (float) (Math.PI / 2), AngleUnit.RADIANS), radPrecision);
        assertEquals((float) (3 * Math.PI / 2), normalizeAzimuth((float) (-Math.PI / 2), AngleUnit.RADIANS), radPrecision);
    }

    @Test
    void normalizedAzimuthIsAlwaysInRange() {
        for (float azimuth = -1080f; azimuth <= 1080f; azimuth += 0.5f) {
            double normalized = normalizeAzimuth(azimuth, AngleUnit.DEGREES);
            assertTrue(normalized >= 0f && normalized < 360f,
                    "azimuth " + azimuth + " normalized to " + normalized + ", which is out of [0, 360)");
        }
        float tau = (float) (2 * Math.PI);
        for (float azimuth = -12 * tau; azimuth <= 12 * tau; azimuth += 0.05f) {
            double normalized = normalizeAzimuth(azimuth, AngleUnit.RADIANS);
            assertTrue(normalized >= 0f && normalized < tau,
                    "azimuth " + azimuth + " rad normalized to " + normalized + ", which is out of [0, 2pi)");
        }
    }

    @Test
    void nonFiniteAzimuthIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> normalizeAzimuth(Float.NaN, AngleUnit.DEGREES));
        assertThrows(IllegalArgumentException.class, () -> normalizeAzimuth(Float.POSITIVE_INFINITY, AngleUnit.DEGREES));
        assertThrows(IllegalArgumentException.class, () -> normalizeAzimuth(Float.NEGATIVE_INFINITY, AngleUnit.RADIANS));
    }

}

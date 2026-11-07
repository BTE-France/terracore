package net.buildtheearth.terracore.geo;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import static java.lang.Float.*;
import static net.buildtheearth.terracore.geo.AngleUnit.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Miscellaneous mathematical utilities for geographic and spherical calculations.
 */
@UtilityClass
public class GeoMaths {
    /**
     * Square root of 3
     */
    public final double ROOT3 = Math.sqrt(3);

    /**
     * Two times pi
     */
    public final double TAU = 2 * Math.PI;


    /**
     * Converts geographic latitude and longitude coordinates to spherical coordinates on a sphere of radius 1.
     *
     * @param geo - geographic coordinates as a double array of length 2, {longitude, latitude}, in degrees
     * @return the corresponding spherical coordinates in radians: {longitude, colatitude}
     */
    public double[] geo2Spherical(double[] geo) {
        double lambda = Math.toRadians(geo[0]);
        double phi = Math.toRadians(90 - geo[1]);
        return new double[]{ lambda, phi };
    }


    /**
     * Converts spherical coordinates to geographic coordinates on a sphere of radius 1.
     *
     * @param spherical - spherical coordinates in radians as a double array of length 2: {longitude, colatitude}
     * @return the corresponding geographic coordinates in degrees: {longitude, latitude}
     */
    public double[] spherical2Geo(double[] spherical) {
        double lon = Math.toDegrees(spherical[0]);
        double lat = 90 - Math.toDegrees(spherical[1]);
        return new double[]{ lon, lat };
    }


    /**
     * Converts spherical coordinates to Cartesian coordinates on a sphere of radius 1.
     *
     * @param spherical - spherical coordinates in radians as a double array of length 2: {longitude, colatitude}
     * @return the corresponding Cartesian coordinates: {x, y, z}
     */
    public double[] spherical2Cartesian(double[] spherical) {
        double sinphi = Math.sin(spherical[1]);
        double x = sinphi * Math.cos(spherical[0]);
        double y = sinphi * Math.sin(spherical[0]);
        double z = Math.cos(spherical[1]);
        return new double[]{ x, y, z };
    }

    /**
     * Converts Cartesian coordinates to spherical coordinates on a sphere of radius 1.
     *
     * @param cartesian - Cartesian coordinates as a double array of length 3: {x, y, z}
     * @return the spherical coordinates of the corresponding normalized vector
     */
    public double[] cartesian2Spherical(double[] cartesian) {
        double lambda = Math.atan2(cartesian[1], cartesian[0]);
        double phi = Math.atan2(Math.sqrt(cartesian[0] * cartesian[0] + cartesian[1] * cartesian[1]), cartesian[2]);
        return new double[]{ lambda, phi };
    }


    /**
     * Produces the 3x3 rotation matrix for a Z-Y-Z Euler rotation: a rotation by {@code a} about the
     * z-axis, followed by a rotation by {@code b} about the y-axis, followed by a rotation by {@code c}
     * about the z-axis.
     *
     * @param a the first rotation angle about the z-axis, in radians
     * @param b the rotation angle about the y-axis, in radians
     * @param c the second rotation angle about the z-axis, in radians
     * @return the resulting 3x3 rotation matrix
     */
    public double[][] produceZYZRotationMatrix(double a, double b, double c) {

        double sina = Math.sin(a);
        double cosa = Math.cos(a);
        double sinb = Math.sin(b);
        double cosb = Math.cos(b);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);

        double[][] mat = new double[3][3];
        mat[0][0] = cosa * cosb * cosc - sinc * sina;
        mat[0][1] = -sina * cosb * cosc - sinc * cosa;
        mat[0][2] = cosc * sinb;

        mat[1][0] = sinc * cosb * cosa + cosc * sina;
        mat[1][1] = cosc * cosa - sinc * cosb * sina;
        mat[1][2] = sinc * sinb;

        mat[2][0] = -sinb * cosa;
        mat[2][1] = sinb * sina;
        mat[2][2] = cosb;

        return mat;
    }

    /**
     * Multiples the given matrix with the given vector.
     * The matrix is assumed to be square and the vector is assumed to be of the same dimension as the matrix.
     *
     * @param matrix - the matrix as a n*n double array
     * @param vector - the vector as double array of length n
     * @return the result of the multiplication as an array of double on length n
     */
    public double[] matVecProdD(double[][] matrix, double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }
    
    /**
     * Converts all values in a double array from degrees to radians.
     * The array is converted in place.
     *
     * @param arr - array to work on
     */
    public void toRadians(double[] arr) {
    	for(int i=0; i<arr.length; i++) arr[i] = Math.toRadians(arr[i]);
    }

    /**
     * Converts all values in a double array from radians to degrees.
     * The array is converted in place.
     *
     * @param arr - array to work on
     */
    public void toDegrees(double[] arr) {
    	for(int i=0; i<arr.length; i++) arr[i] = Math.toDegrees(arr[i]);
    }

    /**
     * Right-shifts the given value by the given number of bits, safely handling negative shifts and checking for overflow.
     *
     * @param val   the value
     * @param shift the number of bits to shift by
     * @return the shifted value
     */
    public int safeDirectionalShift(int val, int shift) {
        int res;
        if (shift == 0) {
            res = val;
        } else if (shift > 0) {
            res = val << shift;
            checkState(res >> shift == val, "numeric overflow: val: %d, shift: %d", val, shift);
        } else {
            res = val >> -shift;
        }
        return res;
    }
    
    
    /**
     * Clamps a value to the given inclusive range.
     *
     * @param num the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return {@code num} if it lies within [min, max], otherwise the nearest bound
     */
    public double clamp(double num, double min, double max) {
        if (num < min) return min;
        return num > max ? max : num;
    }

    /**
     * Linearly interpolates between {@code min} and {@code max}.
     *
     * @param a   the interpolation factor: 0 returns {@code min}, 1 returns {@code max}; values outside [0, 1] extrapolate
     * @param min the value returned when {@code a} is 0
     * @param max the value returned when {@code a} is 1
     * @return the interpolated value
     */
    public double lerp(final double a, final double min, final double max) {
        return min + a * (max - min);
    }

    /**
     * Normalizes an azimuth angle to the range of one full turn: [0, 360) degrees or [0, 2&pi;) radians,
     * expressed in the given unit.
     *
     * @param azimuth the azimuth angle, expressed in the given unit
     * @param unit    the unit {@code azimuth} is expressed in and the result is returned in
     * @return the equivalent azimuth in the range [0, 360) degrees / [0, 2&pi;) radians, in the given unit
     * @throws IllegalArgumentException if azimuth is not finite
     * @throws NullPointerException     if the unit is null
     */
    public float normalizeAzimuth(float azimuth, @NonNull AngleUnit unit) {
        if (!isFinite(azimuth)) {
            throw new IllegalArgumentException("azimuth must be finite");
        }

        // Already in range: return the input as-is to avoid any precision loss
        if (unit == DEGREES && 0f <= azimuth && azimuth < 360f) {
            return azimuth;
        } else if (unit == RADIANS && 0f <= azimuth && azimuth < TAU) {
            return azimuth;
        }

        float degrees = unit.toDegrees(azimuth);
        degrees = ((degrees % 360f) + 360f) % 360f;
        return unit.fromDegrees(degrees);
    }
    
}

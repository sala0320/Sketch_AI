/*
 * Copyright (c) 2010-2021 Haifeng Li. All rights reserved.
 *
 * Smile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Smile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Smile.  If not, see <https://www.gnu.org/licenses/>.
 */

package smile.interpolation;

/**
 * Bicubic interpolation in a two-dimensional regular grid. Bicubic
 * spline interpolation guarantees the continuity of the first derivatives,
 * as well as the continuity of a cross-derivative.
 * <p>
 * Note that CubicSplineInterpolation2D guarantees the continuity of the
 * first and second function derivatives but bicubic spline guarantees
 * continuity of only gradient and cross-derivative. Second derivatives
 * could be discontinuous.
 * <p>
 * In image processing, bicubic interpolation is often chosen over bilinear
 * interpolation or nearest neighbor in image resampling, when speed is not
 * an issue. Images resampled with bicubic interpolation are smoother and
 * have fewer interpolation artifacts.
 * 
 * @author Haifeng Li
 */
public class BicubicInterpolation implements Interpolation2D {

    /**
     * The coefficients to obtain the 16 quantities c[4][4].
     */
    private static final int[][] wt = {
        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
        {-3, 0, 0, 3, 0, 0, 0, 0,-2, 0, 0,-1, 0, 0, 0, 0},
        { 2, 0, 0,-2, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0},
        { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        { 0, 0, 0, 0,-3, 0, 0, 3, 0, 0, 0, 0,-2, 0, 0,-1},
        { 0, 0, 0, 0, 2, 0, 0,-2, 0, 0, 0, 0, 1, 0, 0, 1},
        {-3, 3, 0, 0,-2,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0,-3, 3, 0, 0,-2,-1, 0, 0},
        { 9,-9, 9,-9, 6, 3,-3,-6, 6,-6,-3, 3, 4, 2, 1, 2},
        {-6, 6,-6, 6,-4,-2, 2, 4,-3, 3, 3,-3,-2,-1,-1,-2},
        { 2,-2, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        { 0, 0, 0, 0, 0, 0, 0, 0, 2,-2, 0, 0, 1, 1, 0, 0},
        {-6, 6,-6, 6,-3,-3, 3, 3,-4, 4, 2,-2,-2,-2,-1,-1},
        { 4,-4, 4,-4, 2, 2,-2,-2, 2,-2,-2, 2, 1, 1, 1, 1}
    };

    /**
     * The number of control points on the first dimension.
     */
    private final int m;
    /**
     * The number of control points on the second dimension.
     */
    private final int n;
    /**
     * The function values at control points.
     */
    private final double[][] yv;
    /**
     * The first dimension of tabulated control points.
     */
    private final double[] x1;
    /**
     * The second dimension of tabulated control points.
     */
    private final double[] x2;
    /**
     * To locate the control point in the first dimension.
     */
    private final LinearInterpolation x1terp;
    /**
     * To locate the control point in the second dimension.
     */
    private final LinearInterpolation x2terp;

    /** The workspace of function values. */
    private final double[] y = new double[4];
    /** The workspace of derivatives. */
    private final double[] y1 = new double[4];
    /** The workspace of derivatives. */
    private final double[] y2 = new double[4];
    /** The workspace of derivatives. */
    private final double[] y12 = new double[4];

    /**
     * Constructor. The value in x1 and x2 must be monotonically increasing.
     * @param x1 the 1st dimension value.
     * @param x2 the 2nd dimension value.
     * @param y the function values at <code>(x1, x2)</code>.
     */
    public BicubicInterpolation(double[] x1, double[] x2, double[][] y) {
        if (x1.length != y.length) {
            throw new IllegalArgumentException("x1.length != y.length");
        }

        if (x2.length != y[0].length) {
            throw new IllegalArgumentException("x2.length != y[0].length");
        }

        m = x1.length;
        n = x2.length;
        x1terp = new LinearInterpolation(x1, x1);
        x2terp = new LinearInterpolation(x2, x2);

        this.x1 = x1;
        this.x2 = x2;
        this.yv = y;
    }

    /**
     * Given arrays y[0..3], y1[0..3], y2[0..3], and y12[0..3], containing
     * the function, gradients, and cross-derivative at the four grid points
     * of a rectangular grid cell (numbered counterclockwise from the lower
     * left), and given d1 and d2, the length of the grid cell in the 1 and 2
     * directions, returns the table c[0..3][0..3] that is used by bcuint
     * for bicubic interpolation.
     */
    private static double[][] bcucof(double[] y, double[] y1, double[] y2, double[] y12, double d1, double d2) {

        double d1d2 = d1 * d2;
        double[] cl = new double[16];
        double[] x = new double[16];
        double[][] c = new double[4][4];

        for (int i = 0; i < 4; i++) {
            x[i] = y[i];
            x[i + 4] = y1[i] * d1;
            x[i + 8] = y2[i] * d2;
            x[i + 12] = y12[i] * d1d2;
        }

        for (int i = 0; i < 16; i++) {
            double xx = 0.0;
            for (int k = 0; k < 16; k++) {
                xx += wt[i][k] * x[k];
            }
            cl[i] = xx;
        }

        for (int i = 0, l = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                c[i][j] = cl[l++];
            }
        }

        return c;
    }

    /**
     * Bicubic interpolation within a grid square.
     */
    private static double bcuint(double[] y, double[] y1, double[] y2, double[] y12,
            double x1l, double x1u, double x2l, double x2u, double x1p, double x2p) {
        
        if (x1u == x1l) {
            throw new IllegalArgumentException("Nearby control points take same value: " + x1u);
        }

        if (x2u == x2l) {
            throw new IllegalArgumentException("Nearby control points take same value: " + x2u);
        }

        double d1 = x1u - x1l;
        double d2 = x2u - x2l;
        double[][] c = bcucof(y, y1, y2, y12, d1, d2);

        double t = (x1p - x1l) / d1;
        double u = (x2p - x2l) / d2;

        double ansy = 0.0;

        for (int i = 3; i >= 0; i--) {
            ansy = t * ansy + ((c[i][3] * u + c[i][2]) * u + c[i][1]) * u + c[i][0];
        }

        return ansy;
    }

    @Override
    public double interpolate(double x1p, double x2p) {
        int j = x1terp.search(x1p);
        int k = x2terp.search(x2p);

        double x1l = x1[j];
        double x1u = x1[j + 1];
        double x2l = x2[k];
        double x2u = x2[k + 1];

        y[0] = yv[j][k];
        y[1] = yv[j+1][k];
        y[2] = yv[j+1][k+1];
        y[3] = yv[j][k+1];

        y1[0] = (j - 1 < 0) ? (yv[j+1][k] - yv[j][k]) / (x1[j+1] - x1[j]) : (yv[j+1][k] - yv[j-1][k]) / (x1[j+1] - x1[j-1]);
        y1[1] = (j + 2 < m) ? (yv[j+2][k]   - yv[j][k])   / (x1[j+2] - x1[j]) : (yv[j+1][k]   - yv[j][k]) / (x1[j+1] - x1[j]);
        y1[2] = (j + 2 < m) ? (yv[j+2][k+1] - yv[j][k+1]) / (x1[j+2] - x1[j]) : (yv[j+1][k+1] - yv[j][k+1]) / (x1[j+1] - x1[j]);
        y1[3] = (j - 1 < 0) ? (yv[j+1][k+1] - yv[j][k+1]) / (x1[j+1] - x1[j]) : (yv[j+1][k+1] - yv[j-1][k+1]) / (x1[j+1] - x1[j-1]);

        y2[0] = (k - 1 < 0) ? (yv[j][k+1] - yv[j][k]) / (x2[k+1] - x2[k]) : (yv[j][k+1] - yv[j][k-1]) / (x2[k+1] - x2[k-1]);
        y2[1] = (k - 1 < 0) ? (yv[j+1][k+1] - yv[j+1][k]) / (x2[k+1] - x2[k]) : (yv[j+1][k+1] - yv[j+1][k-1]) / (x2[k+1] - x2[k-1]);
        y2[2] = (k + 2 < n) ? (yv[j+1][k+2] - yv[j+1][k]) / (x2[k+2] - x2[k]) : (yv[j+1][k+1] - yv[j+1][k]) / (x2[k+1] - x2[k]);
        y2[3] = (k + 2 < n) ? (yv[j][k+2]   - yv[j][k])   / (x2[k+2] - x2[k]) : (yv[j][k+1]   - yv[j][k])   / (x2[k+1] - x2[k]);

        if (k - 1 < 0 && j - 1 < 0)
            y12[0] = (yv[j+1][k+1] - yv[j+1][k] - yv[j][k+1] + yv[j][k]) / ((x1[j+1] - x1[j]) * (x2[k+1] - x2[k]));
        else if (k - 1 < 0)
            y12[0] = (yv[j+1][k+1] - yv[j+1][k] - yv[j-1][k+1] + yv[j-1][k]) / ((x1[j+1] - x1[j-1]) * (x2[k+1] - x2[k]));
        else if (j - 1 < 0)
            y12[0] = (yv[j+1][k+1] - yv[j+1][k-1] - yv[j][k+1] + yv[j][k-1]) / ((x1[j+1] - x1[j]) * (x2[k+1] - x2[k-1]));
        else
            y12[0] = (yv[j+1][k+1] - yv[j+1][k-1] - yv[j-1][k+1] + yv[j-1][k-1]) / ((x1[j+1] - x1[j-1]) * (x2[k+1] - x2[k-1]));

        if (j + 2 < m) {
            if (k - 1 < 0) {
                y12[1] = (yv[j + 2][k + 1] - yv[j + 2][k] - yv[j][k + 1] + yv[j][k]) / ((x1[j + 2] - x1[j]) * (x2[k + 1] - x2[k]));
            } else {
                y12[1] = (yv[j + 2][k + 1] - yv[j + 2][k - 1] - yv[j][k + 1] + yv[j][k - 1]) / ((x1[j + 2] - x1[j]) * (x2[k + 1] - x2[k - 1]));
            }
        } else {
            if (k - 1 < 0) {
                y12[1] = (yv[j + 1][k + 1] - yv[j + 1][k] - yv[j][k + 1] + yv[j][k]) / ((x1[j + 1] - x1[j]) * (x2[k + 1] - x2[k]));
            } else {
                y12[1] = (yv[j + 1][k + 1] - yv[j + 1][k - 1] - yv[j][k + 1] + yv[j][k - 1]) / ((x1[j + 1] - x1[j]) * (x2[k + 1] - x2[k - 1]));
            }
        }
    
        if (j + 2 < m && k + 2 < n) {
            y12[2] = (yv[j + 2][k + 2] - yv[j + 2][k] - yv[j][k + 2] + yv[j][k]) / ((x1[j + 2] - x1[j]) * (x2[k + 2] - x2[k]));
        } else if (j + 2 < m) {
            y12[2] = (yv[j + 2][k + 1] - yv[j + 2][k] - yv[j][k + 1] + yv[j][k]) / ((x1[j + 2] - x1[j]) * (x2[k + 1] - x2[k]));
        } else if (k + 2 < n) {
            y12[2] = (yv[j + 1][k + 2] - yv[j + 1][k] - yv[j][k + 2] + yv[j][k]) / ((x1[j + 1] - x1[j]) * (x2[k + 2] - x2[k]));
        } else {
            y12[2] = (yv[j + 1][k + 1] - yv[j + 1][k] - yv[j][k + 1] + yv[j][k]) / ((x1[j + 1] - x1[j]) * (x2[k + 1] - x2[k]));
        }

        if (k + 2 < n) {
            if (j - 1 < 0) {
                y12[3] = (yv[j + 1][k + 2] - yv[j + 1][k] - yv[j][k + 2] + yv[j][k]) / ((x1[j + 1] - x1[j]) * (x2[k + 2] - x2[k]));
            } else {
                y12[3] = (yv[j + 1][k + 2] - yv[j + 1][k] - yv[j - 1][k + 2] + yv[j - 1][k]) / ((x1[j + 1] - x1[j - 1]) * (x2[k + 2] - x2[k]));
            }
        } else {
            if (j - 1 < 0) {
                y12[3] = (yv[j + 1][k + 1] - yv[j + 1][k] - yv[j][k + 1] + yv[j][k]) / ((x1[j + 1] - x1[j]) * (x2[k + 1] - x2[k]));
            } else {
                y12[3] = (yv[j + 1][k + 1] - yv[j + 1][k] - yv[j - 1][k + 1] + yv[j - 1][k]) / ((x1[j + 1] - x1[j - 1]) * (x2[k + 1] - x2[k]));
            }
        }

        return bcuint(y, y1, y2, y12, x1l, x1u, x2l, x2u, x1p, x2p);
    }

    @Override
    public String toString() {
        return "BiCubic Interpolation";
    }
}

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

package smile.wavelet;

import java.util.Arrays;
import smile.math.MathEx;

/**
 * A wavelet is a wave-like oscillation with an amplitude that starts out at
 * zero, increases, and then decreases back to zero. Like the fast Fourier
 * transform (FFT), the discrete wavelet transform (DWT) is a fast, linear
 * operation that operates on a data vector whose length is an integer power
 * of 2, transforming it into a numerically different vector of the same length.
 * The wavelet transform is invertible and in fact orthogonal. Both FFT and DWT
 * can be viewed as a rotation in function space.
 * 
 * @author Haifeng Li
 */
public class Wavelet {

    /**
     * The number of coefficients.
     */
    private final int ncof;
    /**
     * Centering.
     */
    private final int ioff;
    private final int joff;
    /**
     * Wavelet coefficients.
     */
    private final double[] cc;
    private final double[] cr;

    /**
     * Workspace.
     */
    private double[] workspace = new double[1024];

    /**
     * Constructor. Create a wavelet with given coefficients.
     * @param coefficients wavelet coefficients.
     */
    public Wavelet(double[] coefficients) {
        ncof = coefficients.length;

        ioff = joff = -(ncof >> 1);

        cc = coefficients;

        double sig = -1.0;
        cr = new double[ncof];
        for (int i = 0; i < ncof; i++) {
            cr[ncof - 1 - i] = sig * cc[i];
            sig = -sig;
        }
    }

    /**
     * Applies the wavelet filter to a signal vector a[0, n-1].
     * @param a the signal vector.
     * @param n the length of vector.
     */
    void forward(double[] a, int n) {
        if (n < ncof) {
            return;
        }

        if (n > workspace.length) {
            workspace = new double[n];
        } else {
            Arrays.fill(workspace, 0, n, 0.0);
        }

        int nmod = ncof * n;
        int n1 = n - 1;
        int nh = n >> 1;

        for (int ii = 0, i = 0; i < n; i += 2, ii++) {
            int ni = i + 1 + nmod + ioff;
            int nj = i + 1 + nmod + joff;
            for (int k = 0; k < ncof; k++) {
                int jf = n1 & (ni + k + 1);
                int jr = n1 & (nj + k + 1);
                workspace[ii] += cc[k] * a[jf];
                workspace[ii + nh] += cr[k] * a[jr];
            }
        }

        System.arraycopy(workspace, 0, a, 0, n);
    }

    /**
     * Applies the inverse wavelet filter to a signal vector a[0, n-1].
     * @param a the signal vector.
     * @param n the length of vector.
     */
    void backward(double[] a, int n) {
        if (n < ncof) {
            return;
        }

        if (n > workspace.length) {
            workspace = new double[n];
        } else {
            Arrays.fill(workspace, 0, n, 0.0);
        }

        int nmod = ncof * n;
        int n1 = n - 1;
        int nh = n >> 1;

        for (int ii = 0, i = 0; i < n; i += 2, ii++) {
            double ai = a[ii];
            double ai1 = a[ii + nh];
            int ni = i + 1 + nmod + ioff;
            int nj = i + 1 + nmod + joff;
            for (int k = 0; k < ncof; k++) {
                int jf = n1 & (ni + k + 1);
                int jr = n1 & (nj + k + 1);
                workspace[jf] += cc[k] * ai;
                workspace[jr] += cr[k] * ai1;
            }
        }

        System.arraycopy(workspace, 0, a, 0, n);
    }

    /**
     * Discrete wavelet transform.
     * @param a the signal vector.
     */
    public void transform(double[] a) {
        int n = a.length;

        if (!MathEx.isPower2(n)) {
            throw new IllegalArgumentException("The data vector size is not a power of 2.");
        }

        if (n < ncof) {
            throw new IllegalArgumentException("The data vector size is less than wavelet coefficient size.");
        }

        for (int nn = n; nn >= ncof; nn >>= 1) {
            forward(a, nn);
        }
    }

    /**
     * Inverse discrete wavelet transform.
     * @param a the signal vector.
     */
    public void inverse(double[] a) {
        int n = a.length;

        if (!MathEx.isPower2(n)) {
            throw new IllegalArgumentException("The data vector size is not a power of 2.");
        }

        if (n < ncof) {
            throw new IllegalArgumentException("The data vector size is less than wavelet coefficient size.");
        }

        int start = n >> (int) Math.floor(MathEx.log2(n/(ncof-1.)));
        for (int nn = start; nn <= n; nn <<= 1) {
            backward(a, nn);
        }
    }
}

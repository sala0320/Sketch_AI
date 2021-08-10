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
 * Piecewise linear interpolation. Linear interpolation, sometimes known as
 * lerp, is quick and easy, but it is not very precise. Another disadvantage
 * is that the interpolant is not differentiable at the control points x.
 * 
 * @author Haifeng Li
 */
public class LinearInterpolation extends AbstractInterpolation {

    /**
     * Constructor.
     *
     * @param x the tabulated points.
     * @param y the function values at <code>x</code>.
     */
    public LinearInterpolation(double[] x, double[] y) {
        super(x, y);
    }

    @Override
    public double rawinterp(int j, double x) {
        if (xx[j] == xx[j + 1]) {
            return yy[j];
        } else {
            return yy[j] + ((x - xx[j]) / (xx[j + 1] - xx[j])) * (yy[j + 1] - yy[j]);
        }
    }

    @Override
    public String toString() {
        return "Linear Interpolation";
    }
}


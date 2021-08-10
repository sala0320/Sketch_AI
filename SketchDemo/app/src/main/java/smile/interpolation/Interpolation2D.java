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

import java.io.Serializable;

/**
 * Interpolation of 2-dimensional data.
 *
 * @author Haifeng Li
 */
public interface Interpolation2D extends Serializable {

    /**
     * Interpolate the data at a given 2-dimensional point.
     *
     * @param x1 the 1st dimension value.
     * @param x2 the 2nd dimension value.
     * @return the interpolated function value.
     */
    double interpolate(double x1, double x2);
}

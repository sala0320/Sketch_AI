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

package smile.gap;

/**
 * A measure to evaluate the fitness of chromosomes.
 *
 * @param <T> the type of <code>Chromosome</code>.
 *
 * @author Haifeng Li
 */
public interface Fitness<T extends Chromosome> {
    /**
     * Returns the non-negative fitness value of a chromosome. Large values
     * indicate better fitness.
     * @param chromosome the chromosome.
     * @return the fitness score.
     */
    double score(T chromosome);
}

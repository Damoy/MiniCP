/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.search;

import minicp.engine.core.IntVar;
import minicp.examples.TSPMinRegret;

public class Selector {

    public static final Alternative[] TRUE = new Alternative[0];

    public static Alternative[] branch(Alternative... alternatives) {
        return alternatives;
    }

    @FunctionalInterface
    public interface Filter<T> {
        boolean call(T x);
    }

    @FunctionalInterface
    public interface ValueFun<T> {
        float call(T x);
    }

    @FunctionalInterface
    public interface BranchOn<T> {
        Alternative[] call(T x);
    }

    public static <T> Choice selectMin(T[] x, Filter<T> p, ValueFun<T> f, BranchOn<T> body) {
        return () -> {
            T sel = null;
            for (T xi : x) {
                if (p.call(xi)) {
                    sel = sel == null || (f.call(xi) < f.call(sel)) ? xi : sel; // < order here
                }
            }
            if (sel == null) {
                return TRUE;
            } else {
                return body.call(sel);
            }
        };
    }
    
    @FunctionalInterface
    public interface ValueFun2<T, T2> {
        float call(T x, T2 x2);
    }
    
    public static <T> Choice minRegret(T[] x, Filter<T> p, BranchOn<T> body, IntVar[] succ, int[][] distMatrix) {
        return () -> {
            T sel = null;
            
            for (T xi : x) {
                if (p.call(xi)) {
                	// keep the maximum distance of closest cities
                    sel = sel == null || (getRegret((IntVar) xi, succ, distMatrix) > getRegret((IntVar) sel, succ, distMatrix)) ? xi : sel;
                }
            }
            if (sel == null) {
                return TRUE;
            } else {
                return body.call(sel);
            }
        };
    }
    
    /**
     * Get the distance between the two closest successors of @param succi.<br>
     * @param succi the successor we are interested in
     * @param succ the array of successors
     * @param distMatrix the distance data
     */
    public static int getRegret(IntVar succi, IntVar[] succ, int[][] distMatrix) {
    	// get the index of succi in succ
    	int index = TSPMinRegret.getIndex(succ, succi);
    	// get the succi's successors distance
    	int[] cityRow = distMatrix[index];
    	
    	// the closest and second closest cities indices and values
    	int closestIndex = -1;
    	int closestValue = Integer.MAX_VALUE;
    	int secondClosestIndex = -1;
    	int secondClosestValue = Integer.MAX_VALUE;
    	
    	// looks for the succi's nearest cities distances by iterating over
    	// its domain
    	for(int i = succi.getMin(); i <= succi.getMax(); i++) {
    		if(succi.contains(i)) {
    			if(cityRow[i] < closestValue) {
    				// update the closest distance
    				closestValue = cityRow[i];
    				closestIndex = i;
    			}
    		}
    	}
    	
    	for(int i = succi.getMin(); i <= succi.getMax(); i++) {
    		// ignoring city already handled
    		if(closestIndex != -1 && i == closestIndex)
    			continue;
    		
    		if(succi.contains(i)) {
    			if(cityRow[i] < secondClosestValue) {
    				secondClosestValue = cityRow[i];
    				secondClosestIndex = i;
    			}
    		}
    	}

    	check(closestIndex != secondClosestIndex, "The closest and second closest cities are the same.");
    	
    	// return d2 - d1
    	return secondClosestValue - closestValue;
    }
    
    private static void check(boolean cond, String errorMsg) {
    	if(!cond)
    		throw new IllegalStateException(errorMsg);
    }

}

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

package minicp.engine.constraints;

import minicp.engine.core.BoolVar;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.util.InconsistencyException;

public class IsLessOrEqual extends Constraint { // b <=> x <= c

    private final BoolVar b;
    private final IntVar x;
    private final int c;

    public IsLessOrEqual(BoolVar b, IntVar x, int c) {
        super(x.getSolver());
        this.b = b;
        this.x = x;
        this.c = c;
    }

    @Override
    public void post() throws InconsistencyException {
        if (b.isTrue()) { // -- 1
        	// x <= c
        	// updating x, removing [c + 1, ..]
            x.removeAbove(c);
        } else if (b.isFalse()) { // -- 2
        	// x > c
        	// updating x, removing [0, c]
            x.removeBelow(c + 1);
        } else if (x.isBound()) { // -- 3
        	// x is bound, so is x <= c ?
            b.assign(x.getMin() <= c);
        } else if (!x.contains(c)) { // -- 4
        	// c does not belong to [0, x]
            b.assign(false);
        } else {
        	// storing the above lambdas
        	
            b.whenBind(() -> {
            	// -- 1
                if (b.isTrue()) {
                	x.removeAbove(c);
                } else {
                	// should deactivate the constraint as it is entailed
                	// -- 2
                	x.removeBelow(c + 1);
                }
            });
            
            // -- 3
            x.whenBind(() -> {
            	b.assign(x.getMin() <= c);
            });
            
            // -- 4
            x.whenDomainChange(() -> {
//            	if(x.getMin() > c || x.getMax() <= c)
//            		b.assign(x.getMin() <= c);
            	
                if(x.getMin() > c) {
                	b.assign(false);
                }
                
                if(x.getMax() <= c) {
                	b.assign(true);
                }
            });
        }
    }
}

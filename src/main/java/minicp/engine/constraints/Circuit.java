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

import static minicp.cp.Factory.*;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;

public class Circuit extends Constraint {

    private final IntVar[] x;
    private final ReversibleInt[] dest;
    private final ReversibleInt[] orig;
    private final ReversibleInt[] lengthToDest;

    /**
     * x represents an Hamiltonian circuit on the cities {0..x.length-1}
     * where x[i] is the city visited after city i
     * @param x
     */
    public Circuit(IntVar[] x) {
        super(x[0].getSolver());
        this.x = x;
        dest = new ReversibleInt[x.length];
        orig = new ReversibleInt[x.length];
        lengthToDest = new ReversibleInt[x.length];
        for (int i = 0; i < x.length; i++) {
            dest[i] = new ReversibleInt(cp.getTrail(),i);
            orig[i] = new ReversibleInt(cp.getTrail(),i);
            lengthToDest[i] = new ReversibleInt(cp.getTrail(),0);
        }
    }


    @Override
    public void post() throws InconsistencyException {
        cp.post(allDifferent(x));
        
    	if(x.length > 1) {
        // immediate circuit
            for(int i = 0; i < x.length; i++) {
            	int index = i;
            	IntVar current = x[i];
            	
            	if(!current.isBound()) {
                	current.remove(i);
                	current.removeBelow(0);
                	current.removeAbove(x.length - 1);
                	current.whenBind(() -> bind(index));
            	} else {
            		bind(i);
            	}
            }
    	}
    }
    
    private void bind(int i) throws InconsistencyException {
    	// call whenBind, so current is bound
    	IntVar current = x[i];
    	int currentVal = current.getMin();
    	
    	for(int j = 0; j < x.length; j++) {
    		// if dest[j] == i
    		if(dest[j].getValue() == i) {
        		// dest[j] = dest[x[i]]
    			dest[j].setValue(dest[currentVal].getValue());
        		// l[j] += l[x[i]] + 1
    			lengthToDest[j].setValue(lengthToDest[j].getValue() + lengthToDest[currentVal].getValue() + 1);
    		}
    		
    		// if orig[j] == x[i]
    		if(orig[j].getValue() == currentVal) {
    			// orig[j] = orig[i]
    			orig[j].setValue(orig[i].getValue());
    		}
    		
    		// deleting edges for wrong cycles
    		if (orig[j].getValue() == j && lengthToDest[j].getValue() < x.length - 1) {
    			x[dest[j].getValue()].remove(j);
    		}
    	}
    }
    
}

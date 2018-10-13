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

import minicp.reversible.Trail;
import minicp.util.InconsistencyException;

import java.util.LinkedList;
import java.util.List;

public class DFSearch {

    private Choice choice;
    private Trail state;

    private List<SolutionListener> solutionListeners = new LinkedList<SolutionListener>();
    private List<FailListener> failListeners = new LinkedList<FailListener>();


    @FunctionalInterface
    public interface SolutionListener {
        void solutionFound();
    }
    public DFSearch onSolution(SolutionListener listener) {
        solutionListeners.add(listener);
        return this;
    }

    public void notifySolutionFound() {
        solutionListeners.forEach(s -> s.solutionFound());
    }

    @FunctionalInterface
    public interface FailListener {
        void failure();
    }

    public DFSearch onFail(FailListener listener) {
        failListeners.add(listener);
        return this;
    }

    public void notifyFailure() {
        failListeners.forEach(s -> s.failure());
    }

    public DFSearch(Trail state, Choice branching) {
        this.state = state;
        this.choice = branching;
    }

    public SearchStatistics start(SearchLimit limit) {
        SearchStatistics statistics = new SearchStatistics();
        int level = state.getLevel();
        try {
            dfs(statistics,limit);
        } catch (StopSearchException e) {}
        state.popUntil(level);
        return statistics;
    }

    public SearchStatistics start() {
        return start(statistics -> false);
    }
    
    private void dfs(SearchStatistics statistics, SearchLimit limit) {
        if (limit.stopSearch(statistics)) throw new StopSearchException();
        Alternative [] alternatives = choice.call();
        if (alternatives.length == 0) {
            statistics.nSolutions++;
            notifySolutionFound();
        }
        else {
            for (Alternative alt : alternatives) {
                state.push();
                try {
                    statistics.nNodes++;
                    alt.call();
                    dfs(statistics,limit);
                } catch (InconsistencyException e) {
                    notifyFailure();
                    statistics.nFailures++;
                }
                state.pop();
            }
        }
    }


//    private ReversibleStack<Alternative> stack(Trail trail, Alternative... ts){
//    	ReversibleStack<Alternative> stack = new ReversibleStack<>(trail);
//    	for(Alternative alt : ts)
//    		stack.push(alt);
//    	return stack;
//    }
    
//	private Stack<Alternative> stack(Alternative... ts) {
//		Stack<Alternative> stack = new Stack<>();
//		for (Alternative alt : ts)
//			stack.push(alt);
//		return stack;
//	}
    
    // reversible constraint but not reversible stack
	// TODO at each level memorize the array of alternatives
//	// and the index of what we 
//    private void dfs(SearchStatistics statistics, SearchLimit limit) {
//        if (limit.stopSearch(statistics)) throw new StopSearchException();
//        
//        // Stack<Alternative> stack = stack(state, alternatives);
//        Alternative[] firsts = choice.call();
//        Stack<Alternative> stack = stack(firsts);
//        int alternativeIndex = 0;
//        int alternativesCount = firsts.length;
//        
//        while(!stack.isEmpty()) {
//        	
//        	Alternative current = stack.pop();
//        	
//        	// state.getLevel();
//        	// state.popUntil(state.getLevel());
//        	
//        	
//            state.push();
//            try {
//                statistics.nNodes++;
//                current.call();
//            } catch (InconsistencyException e) {
//                notifyFailure();
//                statistics.nFailures++;
//            }
//            state.pop();
//        }
//        
////        if (alternatives.length == 0) {
////            statistics.nSolutions++;
////            notifySolutionFound();
////        }
////        else {
////            for (Alternative alt : alternatives) {
////                state.push();
////                try {
////                    statistics.nNodes++;
////                    alt.call();
////        				dfs
////                } catch (InconsistencyException e) {
////                    notifyFailure();
////                    statistics.nFailures++;
////                }
////                state.pop();
////            }
////        }
//    }
    
//    private void dfs(SearchStatistics statistics, SearchLimit limit) {
//        if (limit.stopSearch(statistics)) throw new StopSearchException();
//        Stack<Alternative[]> stack = new Stack<>();
//        
//        do {
//        	
//        	Alternative[] currents = stack.pop();
//        	
//        	for(Alternative alternative : currents) {
//        		 state.push();
//                 try {
//                     statistics.nNodes++;
//                     alternative.call();
//                 } catch (InconsistencyException e) {
//                     notifyFailure();
//                     statistics.nFailures++;
//                 }
//                 state.pop();
//        	}
//        	
//        	// 
//        	// Alternative[] currents = choice.call();
//        	// stack.push(currents);
//        	
//        } while(!stack.isEmpty());
//    }
    
    // private int[] indexes;
    
    
//    private Map<Alternative, Boolean> visits = new HashMap<>();
//    private boolean visited(Alternative alternative) {
//    	return visits.containsKey(alternative);
//    }
//    
//    private void visit(Alternative alternative) {
//    	visits.put(alternative, true);
//    }
    
    // stack the further uppering
//    private void dfsSTFU(SearchStatistics statistics, SearchLimit limit) {
//        if (limit.stopSearch(statistics)) throw new StopSearchException();
//        
//        // create the queue
//        Stack<Alternative[]> stack = new Stack<>();
//        // get first alternatives
//        Alternative[] firsts = choice.call();
//        // add it to the queue
//        stack.push(firsts);
//        
//        // TODO add array of indexes ?
//        
//        // while there is something in the queue
//        while(!stack.isEmpty()) {
//        	
//        	// pop
//        	Alternative[] currents = stack.pop();
//        	
//        	// here TODO iterate over current
//        	for(int i = 0; i < currents.length; i++){
//        		state.push();
//        		Alternative current = currents[0];
//        		
//        		if(!visited(current)) {
//            		try {
//            			statistics.nNodes++;
//    					current.call();
//    					visit(current);
//    				} catch (InconsistencyException e) {
//    					statistics.nFailures++;
//    					notifyFailure();
//    				}
//            		
//            		state.pop();
//        		} else {
//        			Alternative[] toPush = new Alternative[currents.length - i - 1];
//        			for(int j = i; j < currents.length; j++) {
//        				toPush[j - i] = currents[j];
//        			}
//        			
//        			stack.push(toPush);
//        		}
//        	}
//        }
//    }
//    
//    private void pureRecDFS(SearchStatistics statistics, SearchLimit limit) {
//        if (limit.stopSearch(statistics)) throw new StopSearchException();
//        Alternative [] alternatives = choice.call();
//        if (alternatives.length == 0) {
//            statistics.nSolutions++;
//            notifySolutionFound();
//        }
//        
//        // Stack<Alternative[]> stack
//        // Stack<Integer> indices
//        
//        else {
//        	// compteur
//            for (Alternative alt : alternatives) {
//            	// pile.peek
//            	// assert peek == alternatives
//            	// 
//            	
//            	// to add: push, pop from stack instead of choice
//            	
//            	// on va avoir une pile d'indices
//                state.push();
//                try {
//                    statistics.nNodes++;
//                    alt.call();
//                    dfs(statistics,limit);
//                } catch (InconsistencyException e) {
//                    notifyFailure();
//                    statistics.nFailures++;
//                }
//                state.pop();
//            }
//        }
//    }
//    
//    private void badDFS(SearchStatistics statistics, SearchLimit limit) {
//        if (limit.stopSearch(statistics)) throw new StopSearchException();
//        
//        Alternative [] alternatives = choice.call();
//        Stack<Alternative[]> stack = new Stack<>();
//        Stack<Integer> indices = new Stack<>();
//        stack.push(alternatives);
//        indices.push(state.getLevel());
//        
//        if (alternatives.length == 0) {
//            statistics.nSolutions++;
//            notifySolutionFound();
//        }
//        
//        else {
//        	int alternativesCount = 0;
//        	
//            for (Alternative alt : alternatives) {
//            	assert(stack.peek() == alternatives);
//            	assert(indices.peek() == alternativesCount);
//            	alternativesCount++;
//            	
//            	// to add: push, pop from stack instead of choice
//            	
//            	// on va avoir une pile d'indices
//                state.push();
//                try {
//                    statistics.nNodes++;
//                    alt.call();
//                    dfs(statistics,limit);
//                } catch (InconsistencyException e) {
//                    notifyFailure();
//                    statistics.nFailures++;
//                }
//                state.pop();
//            }
//        }
//    }
    
//    private void dfs(SearchStatistics statistics, SearchLimit limit) {
//        // create the queue
//        Stack<Alternative[]> alternatives = new Stack<>();
//        // the indices, levels storage
//        Stack<Integer> indices = new Stack<>();
//        
//        // get first alternatives
//        Alternative[] firsts = choice.call();
//        // add it to the queue
//        alternatives.push(firsts);
//        
//        while(!alternatives.isEmpty()) {
//            if (limit.stopSearch(statistics)) throw new StopSearchException();
//            
//        	Alternative[] currents = alternatives.pop();
//        	
//        	int indice = currents.length;
//        	
//        	if(currents.length == 0) {
//                statistics.nSolutions++;
//                notifySolutionFound();
//                indices.pop();
//        	}
//        	else {
//            	indices.push(indice);
//                state.push();
//            	for(Alternative alternative : currents) {
//            		try {
//						alternative.call();
//						
//						// choice call ?
//						Alternative[] call = choice.call();
//						Alternative[] toAdd = new Alternative[call.length];
//						
//						
//						
//					} catch (InconsistencyException e) {
//	                    statistics.nNodes++;
//	                    notifyFailure();
//					}
//            	}
//            	state.pop();
//        	}
//        }
//    }
    
    // work with xav: 16h51
    // THE ONLY GOD ONE
//	private void dfs(SearchStatistics statistics, SearchLimit limit) {
//		// create stacks
//		Stack<Alternative[]> stack = new Stack<>();
//		Stack<Integer> indices = new Stack<>();
//		// add base index
//		indices.push(0);
//		
//		// get first alternatives
//		Alternative[] alternatives = choice.call();
//		// and add it to the stack
//		stack.push(alternatives);
//		
//		// while queue is not empty
//		while (!(stack.isEmpty())) {
//			if (limit.stopSearch(statistics))
//				throw new StopSearchException();
//			
//			// look to the top of the alternatives stack
//			alternatives = stack.peek();
//			// look and destroy last index
//			int index = indices.pop();
//
//			// if no more alternatives
//			// we have a solution
//			if (alternatives.length == 0) {
//				// destroy last alternatives
//				stack.pop();
//				// increase solutions count
//				statistics.nSolutions++;
//				// notify the listeners that a solution has been found
//				notifySolutionFound();
//				// go up in the tree
//				state.pop();
//			} else {
//				try {
//					// or == (for safety)
//					// test for backtrack
//					if (alternatives.length <= index) {
//						// destroy last alternatives
//						stack.pop();
//						
//						// if no more alternatives
//						// why continue loop ?
//						if (stack.isEmpty())
//							break;
//						
//						// go up
//						state.pop();
//						
////						if(!stack.isEmpty())
////							state.pop();
//						
//					} else {
//						// go down
//						state.push();
//						
//						// call current alternative
//						// which is selected thanks to the
//						// index saved
//						alternatives[index].call(); // throws InconsistencyException
//						// update the alternatives
//						// by calling another time
//						alternatives = choice.call();
//						
//						// add the new alternatives  
//						stack.push(alternatives);
//						
//						// increase the nodes count
//						statistics.nNodes++;
//						
//						// update the current index
//						// and add it
//						indices.push(index + 1);
//						// add one more index
//						// which is used for the next alternatives
//						indices.push(0);
//					}
//				} catch (InconsistencyException e) {
//					// notify the listeners that we encountered
//					// a failure
//					notifyFailure();
//					// increase the failures count
//					statistics.nFailures++;
//					// go up in tree
//					state.pop();
//				}
//			}
//		}
//    }
}




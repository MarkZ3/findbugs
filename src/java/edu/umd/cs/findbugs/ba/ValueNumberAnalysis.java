/*
 * Bytecode Analysis Framework
 * Copyright (C) 2003, University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.daveho.ba;

import java.util.*;

// We require BCEL 5.0 or later.
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class ValueNumberAnalysis extends ForwardDataflowAnalysis<ValueNumberFrame> {
	private MethodGen methodGen;
	private ValueNumberFactory factory;

	public ValueNumberAnalysis(MethodGen methodGen) {
		this.methodGen = methodGen;
		this.factory = new ValueNumberFactory();
	}

	public ValueNumberFrame createFact() {
		return new ValueNumberFrame(methodGen.getMaxLocals(), factory);
	}

	public void copy(ValueNumberFrame source, ValueNumberFrame dest) {
		dest.copyFrom(source);
	}

	public void initEntryFact(ValueNumberFrame result) {
		int maxNumber = 0;

		// At entry to the method, each local has (as far as we know) a unique value.
		int numSlots = result.getNumSlots();
		for (int i = 0; i < numSlots; ++i)
			result.setValue(i, factory.getValueNumber(maxNumber++));

		// Set the max number in the frame.
		// From there it will propagate to every reachable part
		// of the CFG.
		result.setNextValueNumber(maxNumber);
	}

	public void initResultFact(ValueNumberFrame result) {
		result.setTop();
	}

	public void makeFactTop(ValueNumberFrame fact) {
		fact.setTop();
	}

	public void isFactValid() {
		return fact.isValid();
	}

	public boolean same(ValueNumberFrame fact1, ValueNumberFrame fact2) {
		return fact1.sameAs(fact2);
	}

	public void transfer(BasicBlock basicBlock, InstructionHandle end, ValueNumberFrame start, ValueNumberFrame result) throws DataflowAnalysisException {
		// TODO: implement
	}

	public void meetInto(ValueNumberFrame fact, Edge edge, ValueNumberFrame result) throws DataflowAnalysisException {
		if (edge.getDest().isExceptionHandler() && fact.isValid()) {
			// Special case: when merging predecessor facts for entry to
			// an exception handler, we clear the stack and push a
			// single entry for the exception object.  That way, the locals
			// can still be merged.
			ValueNumberFrame tmpFact = createFact();
			tmpFact.copyFrom(fact);
			tmpFact.clearStack();
			tmpFact.pushValue(tmpFact.createFreshValue());
			fact = tmpFact;
		}
		result.mergeWith(fact);
	}
}

// vim:ts=4

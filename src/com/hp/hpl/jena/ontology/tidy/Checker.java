package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 * This class implements the OWL Syntax Checker from the OWL Test Cases WD.
 * <h4>Implementation issues concerning
 * <a href="http://www.w3.org/TR/2003/WD-owl-semantics-20030331/">OWL Semantics
 * &mp; Abstract Syntax</a></h4>
 * <ul>
 * <li>S&AS does not seem to say that the object of an annotation triple
 * must be one of the IDs, it seems to allow an arbitrary URIref.
 * We require it to have some type, reflecting the <a href=
 *  "http://lists.w3.org/Archives/Public/www-webont-wg/2003Mar/0066.html" 
 * >decision on OWL DL
 * Syntax</a>.</li>
 * <li>The <a href= 
 * "http://lists.w3.org/Archives/Public/www-webont-wg/2003Apr/0003.html" 
 * >Hamiltonian path</a> constraint on owl:equivalentClass is not implemented,
 * instead this implements <a href= 
 * "http://lists.w3.org/Archives/Public/www-webont-wg/2003Apr/0004.html"
 * >fix 2</a>.</li>
 * 
 * 
 * @author jjc
 *
 */
public class Checker extends EnhGraph {
	static private Personality personality =
		new GraphPersonality().add(CNodeI.class, CNode.factory);
	private Set checkedGraphs = new HashSet();
	/**
	 * A miscellaneous set of triples that could not be added
	 * with {@link #add(Triple)}.
	 */
	private Graph problems;

	private int errorCnt = 0;

	Checker(Graph g) {
		super(g, personality);
	}

	void add(Graph g) {
		if (!checkedGraphs.contains(g)) {
			// Add every triple
			ClosableIterator it = null;
			try {
				it = g.find(null, null, null);
				while (it.hasNext()) {
					add((Triple) it.next());
				}
			} finally {
				if (it != null)
					it.close();
			}
			checkedGraphs.add(g);
		}
	}
	final private int Shift = Grammar.NBits;
	final private int Mask = (1 << Shift) - 1;
	/**
	 * 
	 * @param t A triple from a graph being checked.
	 */
	private void add(Triple t) {
		CNodeI s = (CNodeI) getNodeAs(t.getSubject(), CNodeI.class);
		CNodeI p = (CNodeI) getNodeAs(t.getPredicate(), CNodeI.class);
		CNodeI o = (CNodeI) getNodeAs(t.getObject(), CNodeI.class);
		final int key =
			(s.getCategories() << (2 * Shift))
				| (p.getCategories() << Shift)
				| o.getCategories();
		int nkey = Grammar.addTriple(key);
		if (nkey == Grammar.Failure) {
			problems.add(t);
			errorCnt++;
		} else {
			int m = nkey & Mask;
			if (m == Grammar.EmptyCategorySet)
				errorCnt++;
			o.setCategories(m);
			nkey >>= Shift;
			m = nkey & Mask;
			if (m == Grammar.EmptyCategorySet)
				errorCnt++;
			p.setCategories(m);
			nkey >>= Shift;
			m = nkey & Mask;
			if (m == Grammar.EmptyCategorySet)
				errorCnt++;
			s.setCategories(m);
		}
		o.incrObjectCount(1);
		if ((key & (Mask | (Mask << Shift)))
			== ((Grammar.rdftype << Shift) | Grammar.rdfList)) {
			s.incrObjectCount(0);
		}
	}

}

/*

Copyright (C) SYSTAP, LLC 2006-2007.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/
package com.bigdata.rdf.internal.constraints;

import java.util.Map;

import com.bigdata.bop.BOp;
import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.IValueExpression;
import com.bigdata.bop.NV;
import com.bigdata.rdf.error.SparqlTypeErrorException;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.model.BigdataLiteral;
import com.bigdata.rdf.model.BigdataURI;
import com.bigdata.rdf.sparql.ast.DummyConstantNode;

/**
 * This function has an origin of ONE (1) not ZERO (0). The start and offset
 * parameters are doubles and are ROUNDED using xpath:numeric-round to obtain
 * the appropriate integer values. A negative or zero starting offset is treated
 * as indicating the first character in the string, which is at index ONE (1).
 * When the length parameter is not specified then it is assumed to be infinite.
 * 
 * @see http://www.w3.org/TR/xpath-functions/#func-substring
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class SubstrBOp extends AbstractLiteralBOp<IV> {

    private static final long serialVersionUID = -7022953617164154412L;

    @SuppressWarnings("rawtypes")
    public SubstrBOp(//
            final IValueExpression<? extends IV> x,
            final IValueExpression<? extends IV> start,
            final IValueExpression<? extends IV> length, String lex) {
     
        this(new BOp[] { x, start, length }, NV.asMap(new NV(
                Annotations.NAMESPACE, lex)));
        
    }

    public SubstrBOp(BOp[] args, Map<String, Object> anns) {

        super(args, anns);
        
        if (args.length < 2 || args[0] == null || args[1] == null)
            throw new IllegalArgumentException();

    }

    public SubstrBOp(SubstrBOp op) {
        super(op);
    }

    public Requirement getRequirement() {
        return Requirement.SOMETIMES;
    }

    @SuppressWarnings("rawtypes")
    public IV _get(final IBindingSet bs) throws SparqlTypeErrorException {

        // The literal.
        
        final IV literalArg = getAndCheckIfMaterializedLiteral(0, bs);
        
        final BigdataLiteral lit = literalValue(literalArg);

        /* 
         * The starting offset for the substring.
         */

        final IV startArg = get(1).get(bs);

        // Negative values and zero are treated as ONE.
        final int start = Math.max(1,
                (int) Math.round(literalValue(startArg).doubleValue()));

        /*
         * The length of the substring (optional argument).
         */

        String label = lit.getLabel();

        if (arity() > 2 && get(2) != null) {

            /*
             * substr(start,length)
             */

            final IV lengthArg = get(2).get(bs);

            final int length = Math.min(label.length(),
                    (int) Math.round(literalValue(lengthArg).doubleValue()));

//            final int length = (int) Math.round(literalValue(lengthArg).doubleValue());
            
            label = label
                    .substring(start-1, Math.min(label.length(), start-1+length));

        } else {

            label = label.substring(start-1);

        }

        /*
         * Generate appropriate literal.
         */

        final String lang = lit.getLanguage();
        
        final BigdataURI dt = lit.getDatatype();
        
        if (lang != null) {

            /*
             * Language code literal.
             */

            final BigdataLiteral str = getValueFactory().createLiteral(label,
                    lang);

            return DummyConstantNode.toDummyIV(str);

        } else if (dt != null) {

            /*
             * Datatype literal.
             */

            final BigdataLiteral str = getValueFactory().createLiteral(label,
                    dt);

            return DummyConstantNode.toDummyIV(str);

        } else {

            /*
             * Return new simple literal using Literal.getLabel()
             */

            final BigdataLiteral str = getValueFactory().createLiteral(label);

            return DummyConstantNode.toDummyIV(str);

        }

    }

}

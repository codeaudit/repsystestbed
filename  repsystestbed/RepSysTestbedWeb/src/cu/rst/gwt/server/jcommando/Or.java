/* zlib/libpng license
 *
 * This software is provided 'as-is', without any express or implied warranty. In
 * no event will the authors be held liable for any damages arising from the use of
 * this software.
 *
 * Permission is granted to anyone to use this software for any purpose, including
 * commercial applications, and to alter it and redistribute it freely, subject to
 * the following restrictions:
 *
 *   1. The origin of this software must not be misrepresented; you must not
 *      claim that you wrote the original software. If you use this software in
 *      a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *
 *   2. Altered source versions must be plainly marked as such, and must not be
 *      misrepresented as being the original software.
 *
 *   3. This notice may not be removed or altered from any source distribution.
 *
 * Copyright (c) 2005, Brett Wooldridge
 * Created on May 26, 2005
 */

package cu.rst.gwt.server.jcommando;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Brett Wooldridge
 *
 */
public class Or extends Grouping
{

   /**
    * Returns true if the 'or' requirement is satisfied.
    *
    * @param optionSet the set of options to check
    * @return true if this requirement is satisfied, false otherwise
    *
    * @see cu.rst.gwt.server.jcommando.Grouping#satisfied(java.util.Set)
    */
   public boolean satisfied(Set optionSet)
   {
      boolean anySatisfied = false;
      Iterator iter = groupings.iterator();
      while (iter.hasNext())
      {
         Grouping grouping = (Grouping) iter.next();
         anySatisfied |= grouping.satisfied(optionSet);
      }

      return anySatisfied || anyPresent(optionSet);
   }


   // ======================================================================
   //                    P A C K A G E   M E T H O D S
   // ======================================================================

   String generateConstructor()
   {
      StringWriter sw = new StringWriter();
      PrintWriter  pw = new PrintWriter(sw);

      pw.println("      Or " + id + " = new Or();");
      return sw.toString();
   }


   // ======================================================================
   //                    P R I V A T E   M E T H O D S
   // ======================================================================

   private boolean anyPresent(Set optionSet)
   {
      Iterator iter = optionSet.iterator();
      while (iter.hasNext())
      {
         if (options.contains(iter.next()))
         {
            return true;
         }
      }
      return false;
   }
}

/*
 * Copyright 2018 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ibis.constellation.impl;

import ibis.constellation.AbstractContext;
import ibis.constellation.OrContext;
import ibis.constellation.Context;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ContextMatch {
    
    public static boolean doMatch(Context a, Context b) {
   
        if (a.getRangeEnd() < b.getRangeStart()) {
            // a is entirely before b

 //           System.err.println("NO Match RangeContext " + a.getName() + " " + a.getRangeStart() + "-" + a.getRangeEnd() + " RankContext " + b.getName() + " " + b.getRangeStart() + "-" + b.getRangeEnd());
            
            return false;
        }
        
        if (b.getRangeEnd() < a.getRangeStart()) {
            // b is entirely before a
            
    //        System.err.println("NO Match RangeContext " + a.getName() + " " + a.getRangeStart() + "-" + a.getRangeEnd() + " RankContext " + b.getName() + " " + b.getRangeStart() + "-" + b.getRangeEnd());
            
            return false;
        }
        
        // In all other cases there is overlap in the range, so check if the name matches. 
        
        boolean match = a.getName().equals(b.getName());
        
   //     System.err.println((match ? "YES" : "NO") + " Match RangeContext " + a.getName() + " " + a.getRangeStart() + "-" + a.getRangeEnd() + " RankContext " + b.getName() + " " + b.getRangeStart() + "-" + b.getRangeEnd());
        
        return match;
    }
    
    public static boolean doMatch(OrContext a, Context b) {
        
        for (Context c : a) {
            if (doMatch(c, b)) { 
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean doMatch(OrContext a, OrContext b) {
            
        for (Context c1 : a) {
            for (Context c2 : b) {
                if (doMatch(c1, c2)) { 
                    return true;
                }
            }
        }
        
        return false;
    }
        
    public static boolean match(AbstractContext a, AbstractContext b) {
        
        if (a == null || b == null) { 
            return false;
        }
        
        if (a instanceof Context) {
            if (b instanceof Context) {
                return doMatch((Context)a, (Context)b);
            } else { 
                return doMatch((OrContext)b, (Context)a);
            }
        } else { // a instanceof OrContext
            if (b instanceof Context) {
                return doMatch((OrContext)a, (Context)b);
            } else {  
                return doMatch((OrContext)a, (OrContext)b);
            }
        }    
    }
}

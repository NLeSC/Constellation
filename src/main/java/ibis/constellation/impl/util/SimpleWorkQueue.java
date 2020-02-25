/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
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
package ibis.constellation.impl.util;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.Context;
import ibis.constellation.OrContext;
import ibis.constellation.StealStrategy;
import ibis.constellation.impl.ActivityRecord;

public class SimpleWorkQueue extends WorkQueue {

    public static final Logger log = LoggerFactory.getLogger(SimpleWorkQueue.class);
   
    private final HashMap<String, SortedRangeList> lists = new HashMap<String, SortedRangeList>();
   
    private int size;

    public SimpleWorkQueue(String id) {
        super(id);
    }

    @Override
    public synchronized int size() {
        return size;
    }

    private void enqueueRange(Context c, ActivityRecord a) { 
                
        SortedRangeList tmp = lists.get(c.getName());

        if (tmp == null) {
            tmp = new SortedRangeList(c.getName());
            lists.put(c.getName(), tmp);
        }

        tmp.insert(a, c.getRangeStart(), c.getRangeEnd());
        size++;
    }
    
    private void enqueueOr(OrContext c, ActivityRecord a) { 
        for (Context rc : c) { 
            enqueueRange(rc, a);
        }
    }
    
    @Override
    public synchronized void enqueue(ActivityRecord a) {

        AbstractContext c = a.getContext();

        if (c instanceof Context) {
            enqueueRange((Context) c, a);
        } else { 
            enqueueOr((OrContext) c, a);
        }
    }
    
    private ActivityRecord stealRange(Context c, StealStrategy s) {
        
        if (log.isDebugEnabled()) {
            log.debug("Matching context: " + c  + " (len = " + lists.size() + ")");
        }

        SortedRangeList tmp = lists.get(c.getName());
        
        if (tmp == null) {
            if (log.isDebugEnabled()) {
                log.debug("SortedRangeList == null");
            }

            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("SortedRangeList == " + tmp.size());
        }
        
        ActivityRecord r = null;
        
        if (StealStrategy.BIGGEST.equals(s)) { 
            r = tmp.removeBiggestInRange(c.getRangeStart(), c.getRangeEnd());
        } else { 
            r = tmp.removeSmallestInRange(c.getRangeStart(), c.getRangeEnd());
        }
        
        if (log.isDebugEnabled()) {
            log.debug(" steal == " + r);
        }

        if (r != null) { 
            // Code added to remove r from all lists. --Ceriel
            AbstractContext ctxt = r.getContext();
            if (ctxt instanceof OrContext) {
                // Yes, there may be other lists in which it exists
                for (Context rc : (OrContext) ctxt) {
                    removeByReference(rc, r);
                }
            }
            size--;
        }
        
        return r;
        
    }
    
    private boolean removeByReference(Context c, ActivityRecord r) {
        
        SortedRangeList tmp = lists.get(c.getName());

        if (tmp == null) {
            return false;
        }
        
        return tmp.removeByReference(r);
    }
    
    private ActivityRecord stealOr(OrContext c, StealStrategy s) {
        
        ActivityRecord tmp = null;
        
        Iterator<Context> itt = c.iterator();

        while (tmp == null && itt.hasNext()) { 
            tmp = stealRange(itt.next(), s);
        }

        if (tmp == null) {
            return null;
        }

        // No, not here. it should be removed from all lists, also if the steal attempt only has a single context.
        // Solving it in stealRange. --Ceriel
        //        for (Context rc : c) {
        //            removeByReference(rc, tmp);
        //        }

        return tmp;
    }
    
    
    @Override
    public synchronized ActivityRecord steal(AbstractContext c, StealStrategy s) {

        if (c instanceof Context) {
            return stealRange((Context) c, s);
        } else { 
            return stealOr((OrContext) c, s);
        }
    }
}

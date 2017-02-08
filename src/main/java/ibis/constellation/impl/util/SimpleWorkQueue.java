package ibis.constellation.impl.util;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.OrContext;
import ibis.constellation.Context;
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
        
        for (Context rc : c) {
            removeByReference(rc, tmp);
        }
        
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

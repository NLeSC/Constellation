package ibis.constellation.extra;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.StealStrategy;
import ibis.constellation.context.ActivityContext;
import ibis.constellation.context.ExecutorContext;
import ibis.constellation.context.OrActivityContext;
import ibis.constellation.context.OrExecutorContext;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitExecutorContext;
import ibis.constellation.impl.ActivityRecord;

public class SmartSortedWorkQueue extends WorkQueue {

    public static final Logger log = LoggerFactory.getLogger(SmartSortedWorkQueue.class);

    // We maintain two lists here, which reflect the relative complexity of
    // the context associated with the jobs:
    //
    // 'UNIT' jobs are likely to have limited suitable locations, but
    // their context matching is easy
    // 'OR' jobs may have more suitable locations, but their context matching
    // is more expensive

    // protected final HashMap<ActivityIdentifier, ActivityRecord> ids = new
    // HashMap<ActivityIdentifier, ActivityRecord>();

    private final HashMap<String, SortedList> unit = new HashMap<String, SortedList>();

    private final HashMap<String, SortedList> or = new HashMap<String, SortedList>();

    private int size;

    public SmartSortedWorkQueue(String id) {
        super(id);
    }

    @Override
    public synchronized int size() {
        return size;
    }

    private ActivityRecord getUnit(UnitExecutorContext c, StealStrategy s) {

        SortedList tmp = unit.get(c.getName());

        if (tmp == null) {
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Matching context string: " + c.getName());
        }

        assert tmp.size() > 0;

        ActivityRecord a = null;

        switch (s.getStrategy()) {
        //        case StealStrategy._BIGGEST:
        //        case StealStrategy._ANY:
        default:
            a = tmp.removeTail();
            break;

        case StealStrategy._SMALLEST:
            a = tmp.removeHead();
            break;

        case StealStrategy._VALUE:
            a = tmp.removeOneInRange(s.getValue(), s.getValue());
            break;

        case StealStrategy._RANGE:
            a = tmp.removeOneInRange(s.getStartOfRange(), s.getEndOfRange());
            break;
        }

        assert a != null;

        if (tmp.size() == 0) {
            unit.remove(c.getName());
        }

        size--;

        // ids.remove(a.identifier());

        return a;
    }

    private ActivityRecord getOr(UnitExecutorContext c, StealStrategy s) {

        SortedList tmp = or.get(c.getName());

        if (tmp == null) {
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Matching context string: " + c.getName());
        }

        assert (tmp.size() > 0);

        ActivityRecord a = null;

        switch (s.getStrategy()) {
        //        case StealStrategy._BIGGEST:
        //        case StealStrategy._ANY:
        default:
            a = tmp.removeTail();
            break;

        case StealStrategy._SMALLEST:
            a = tmp.removeHead();
            break;

        case StealStrategy._VALUE:
            a = tmp.removeOneInRange(s.getValue(), s.getValue());
            break;

        case StealStrategy._RANGE:
            a = tmp.removeOneInRange(s.getStartOfRange(), s.getEndOfRange());
            break;
        }

        assert a != null;

        if (tmp.size() == 0) {
            or.remove(c.getName());
        }

        // Remove entry for this ActivityRecord from all lists....
        OrActivityContext cntx = (OrActivityContext) a.getActivity().getContext();

        for (int i = 0; i < cntx.size(); i++) {

            UnitActivityContext u = cntx.get(i);

            // Remove this activity from all entries in the 'or' table
            tmp = or.get(u.getName());

            if (tmp != null) {
                tmp.removeByReference(a);

                if (tmp.size() == 0) {
                    or.remove(u.getName());
                }
            }
        }

        size--;

        // ids.remove(a.identifier());

        return a;
    }

    private void enqueueUnit(UnitActivityContext c, ActivityRecord a) {

        SortedList tmp = unit.get(c.getName());

        if (tmp == null) {
            tmp = new SortedList(c.getName());
            unit.put(c.getName(), tmp);
        }

        tmp.insert(a, c.getRank());
        size++;
        // ids.put(a.identifier(), a);
    }

    private void enqueueOr(OrActivityContext c, ActivityRecord a) {

        for (int i = 0; i < c.size(); i++) {

            UnitActivityContext uc = c.get(i);

            SortedList tmp = or.get(uc.getName());

            if (tmp == null) {
                tmp = new SortedList(uc.getName());
                or.put(uc.getName(), tmp);
            }

            tmp.insert(a, uc.getRank());
        }

        size++;
        // ids.put(a.identifier(), a);
    }

    @Override
    public synchronized void enqueue(ActivityRecord a) {

        ActivityContext c = a.getActivity().getContext();

        if (c instanceof UnitActivityContext) {
            enqueueUnit((UnitActivityContext) c, a);
            return;
        }

        assert (c instanceof OrActivityContext);
        enqueueOr((OrActivityContext) c, a);
    }

    @Override
    public synchronized ActivityRecord steal(ExecutorContext c, StealStrategy s) {

        if (c instanceof UnitExecutorContext) {

            UnitExecutorContext tmp = (UnitExecutorContext) c;

            ActivityRecord a = getUnit(tmp, s);

            if (a == null) {
                a = getOr(tmp, s);
            }

            return a;
        }

        assert (c instanceof OrExecutorContext);

        OrExecutorContext o = (OrExecutorContext) c;

        for (int i = 0; i < o.size(); i++) {

            UnitExecutorContext ctx = o.get(i);

            ActivityRecord a = getUnit(ctx, s);

            if (a != null) {
                return a;
            }

            a = getOr(ctx, s);

            if (a != null) {
                return a;
            }
        }

        return null;
    }
}

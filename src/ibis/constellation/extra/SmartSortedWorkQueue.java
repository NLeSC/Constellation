package ibis.constellation.extra;

import ibis.constellation.ActivityContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.StealStrategy;
import ibis.constellation.WorkerContext;
import ibis.constellation.context.OrActivityContext;
import ibis.constellation.context.OrWorkerContext;
import ibis.constellation.context.UnitActivityContext;
import ibis.constellation.context.UnitWorkerContext;
import ibis.constellation.impl.ActivityRecord;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartSortedWorkQueue extends WorkQueue {

    public static final Logger log = LoggerFactory
            .getLogger(SmartSortedWorkQueue.class);

    // We maintain two lists here, which reflect the relative complexity of
    // the context associated with the jobs:
    //
    // 'UNIT' jobs are likely to have limited suitable locations, but
    // their context matching is easy
    // 'OR' jobs may have more suitable locations, but their context matching
    // is more expensive

    protected final HashMap<ActivityIdentifier, ActivityRecord> ids = new HashMap<ActivityIdentifier, ActivityRecord>();

    protected final HashMap<String, SortedList> unit = new HashMap<String, SortedList>();

    protected final HashMap<String, SortedList> or = new HashMap<String, SortedList>();

    protected int size;

    public SmartSortedWorkQueue(String id) {
        super(id);
    }

    @Override
    public int size() {
        return size;
    }

    private ActivityRecord getUnit(String name, boolean head) {

        SortedList tmp = unit.get(name);

        if (tmp == null) {
            return null;
        }

        // FIXME: SANITY CHECK -- should not happen ?
        if (tmp.size() == 0) {
            log.error("(getUnit1): unit.get returned null unexpectedly!");
            return null;
        }

        ActivityRecord a;

        if (head) {
            a = tmp.removeHead();
        } else {
            a = tmp.removeTail();
        }

        // FIXME: SANITY CHECK -- should not happen ?
        if (a == null) {
            log.error(
                    "(getUnit1): removeHead/Tail returned null unexpectedly!");
            return null;
        }

        if (tmp.size() == 0) {
            unit.remove(name);
        }

        size--;

        ids.remove(a.identifier());

        return a;
    }

    private ActivityRecord getUnit(UnitWorkerContext c, StealStrategy s) {

        SortedList tmp = unit.get(c.name);

        if (tmp == null) {
            return null;
        }

        // FIXME: SANITY CHECK -- should not happen ?
        if (tmp.size() == 0) {
            log.error("(getUnit2): unit.get returned null unexpectedly!");
            return null;
        }

        ActivityRecord a = null;

        switch (s.strategy) {
        case StealStrategy._BIGGEST:
        case StealStrategy._ANY:
            a = tmp.removeTail();
            break;

        case StealStrategy._SMALLEST:
            a = tmp.removeHead();
            break;

        case StealStrategy._VALUE:
        case StealStrategy._RANGE:
            a = tmp.removeOneInRange(s.start, s.end);
            break;
        }

        // FIXME: SANITY CHECK -- should not happen ?
        if (a == null) {
            log.error(
                    "(getUnit2): removeHead/Tail/Range returned null unexpectedly!");
            return null;
        }

        if (tmp.size() == 0) {
            unit.remove(c.name);
        }

        size--;

        ids.remove(a.identifier());

        return a;
    }

    private ActivityRecord getOr(String name, boolean head) {

        SortedList tmp = or.get(name);

        if (tmp == null) {
            return null;
        }

        // FIXME: SANITY CHECK -- should not happen ?
        if (tmp.size() == 0) {
            log.error("(getOr1): or.get returned null unexpectedly!");
            return null;
        }

        ActivityRecord a = null;

        if (head) {
            a = tmp.removeHead();
        } else {
            a = tmp.removeTail();
        }

        if (tmp.size() == 0) {
            or.remove(name);
        }

        // FIXME: SANITY CHECK -- should not happen ?
        if (a == null) {
            log.error("(getOr1): removeHead/Tail returned null unexpectedly!");
            return null;
        }

        // Remove entry for this ActivityRecord from all lists....
        UnitActivityContext[] all = ((OrActivityContext) a.activity
                .getContext()).getContexts();

        for (int i = 0; i < all.length; i++) {

            // Remove this activity from all entries in the 'or' table
            tmp = or.get(all[i].name);

            if (tmp != null) {
                tmp.removeByReference(a);

                if (tmp.size() == 0) {
                    or.remove(all[i].name);
                }
            }
        }

        size--;

        ids.remove(a.identifier());

        return a;
    }

    private ActivityRecord getOr(UnitWorkerContext c, StealStrategy s) {

        SortedList tmp = or.get(c.name);

        if (tmp == null) {
            return null;
        }

        if (tmp.size() == 0) {
            log.error("(getOr2): or.get returned null unexpectedly!");
            return null;
        }

        ActivityRecord a = null;

        switch (s.strategy) {
        case StealStrategy._BIGGEST:
        case StealStrategy._ANY:
            a = tmp.removeTail();
            break;

        case StealStrategy._SMALLEST:
            a = tmp.removeHead();
            break;

        case StealStrategy._VALUE:
        case StealStrategy._RANGE:
            a = tmp.removeOneInRange(s.start, s.end);
            break;
        }

        // FIXME: SANITY CHECK -- should not happen ?
        if (a == null) {
            log.error("(getOr2): removeHead/Tail returned null unexpectedly!");
            return null;
        }

        if (tmp.size() == 0) {
            or.remove(c.name);
        }

        // Remove entry for this ActivityRecord from all lists....
        OrActivityContext cntx = (OrActivityContext) a.activity.getContext();

        for (int i = 0; i < cntx.size(); i++) {

            UnitActivityContext u = cntx.get(i);

            // Remove this activity from all entries in the 'or' table
            tmp = or.get(u.name);

            if (tmp != null) {
                tmp.removeByReference(a);

                if (tmp.size() == 0) {
                    or.remove(u.name);
                }
            }
        }

        size--;

        ids.remove(a.identifier());

        return a;
    }

    @Override
    public ActivityRecord dequeue(boolean head) {

        if (size == 0) {
            return null;
        }

        if (unit.size() > 0) {
            return getUnit(unit.keySet().iterator().next(), head);
        }

        if (or.size() > 0) {
            return getOr(or.keySet().iterator().next(), head);
        }

        return null;
    }

    private void enqueueUnit(UnitActivityContext c, ActivityRecord a) {

        SortedList tmp = unit.get(c.name);

        if (tmp == null) {
            tmp = new SortedList(c.name);
            unit.put(c.name, tmp);
        }

        tmp.insert(a, c.rank);
        size++;
        ids.put(a.identifier(), a);
    }

    private void enqueueOr(OrActivityContext c, ActivityRecord a) {

        for (int i = 0; i < c.size(); i++) {

            UnitActivityContext uc = c.get(i);

            SortedList tmp = or.get(uc.name);

            if (tmp == null) {
                tmp = new SortedList(uc.name);
                or.put(uc.name, tmp);
            }

            tmp.insert(a, uc.rank);
        }

        size++;
        ids.put(a.identifier(), a);
    }

    @Override
    public void enqueue(ActivityRecord a) {

        ActivityContext c = a.activity.getContext();

        if (c.isUnit()) {
            enqueueUnit((UnitActivityContext) c, a);
            return;
        }

        if (c.isOr()) {
            enqueueOr((OrActivityContext) c, a);
            return;
        }

        log.error(id + "EEP: ran into unknown Context Type ! " + c);
    }

    @Override
    public ActivityRecord steal(WorkerContext c, StealStrategy s) {

        if (c.isUnit()) {

            UnitWorkerContext tmp = (UnitWorkerContext) c;

            ActivityRecord a = getUnit(tmp, s);

            if (a == null) {
                a = getOr(tmp, s);
            }

            return a;
        }

        if (c.isOr()) {

            OrWorkerContext o = (OrWorkerContext) c;

            for (int i = 0; i < o.size(); i++) {

                UnitWorkerContext ctx = o.get(i);

                ActivityRecord a = getUnit(ctx, s);

                if (a != null) {
                    return a;
                }

                a = getOr(ctx, s);

                if (a != null) {
                    return a;
                }
            }
        }

        return null;
    }

    @Override
    public boolean contains(ActivityIdentifier id) {
        return ids.containsKey(id);
    }

    @Override
    public ActivityRecord lookup(ActivityIdentifier id) {
        return ids.get(id);
    }

    @Override
    public boolean deliver(ActivityIdentifier id, Event e) {

        ActivityRecord ar = ids.get(id);

        if (ar != null) {
            ar.enqueue(e);
            return true;
        }

        return false;
    }
}

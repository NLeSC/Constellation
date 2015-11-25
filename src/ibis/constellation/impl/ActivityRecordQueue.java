package ibis.constellation.impl;

/*
 import ibis.constellation.ActivityContext;
 import ibis.constellation.ActivityIdentifier;
 import ibis.constellation.WorkerContext;
 import ibis.constellation.context.OrActivityContext;
 import ibis.constellation.context.OrWorkerContext;

 import java.util.HashMap;
 import java.util.Iterator;
 */
public class ActivityRecordQueue {

    // UNUSED ?
    /*
     * private final HashMap<ActivityIdentifier, ActivityRecord> map = new
     * HashMap<ActivityIdentifier, ActivityRecord>();
     * 
     * private final HashMap<ActivityContext, HashMap<ActivityIdentifier,
     * ActivityRecord>> contextMap = new HashMap<ActivityContext,
     * HashMap<ActivityIdentifier, ActivityRecord>>();
     * 
     * public ActivityRecordQueue() { }
     * 
     * private ActivityContext [] flatten(ActivityContext c) {
     * 
     * if (c == null) { return new ActivityContext[0]; } else if (c.isOr()) {
     * return ((OrActivityContext) c).getContexts(); } else { return new
     * ActivityContext [] { c }; } }
     * 
     * 
     * private ActivityRecord remove(ActivityIdentifier id) {
     * 
     * ActivityRecord tmp = map.remove(id);
     * 
     * if (tmp != null) {
     * 
     * ActivityContext [] c = flatten(tmp.activity.getContext());
     * 
     * for (ActivityContext t : c) { HashMap<ActivityIdentifier, ActivityRecord>
     * m = contextMap.get(t); m.remove(id); } }
     * 
     * return tmp; }
     * 
     * public void add(ActivityRecord [] a) {
     * 
     * if (a == null || a.length == 0) { return; }
     * 
     * for (int i=0;i<a.length;i++) { if (a[i] != null) { add(a[i]); } } }
     * 
     * public synchronized void add(ActivityRecord a) {
     * 
     * if (a == null) { return; }
     * 
     * map.put(a.identifier(), a);
     * 
     * ActivityContext [] c = flatten(a.activity.getContext());
     * 
     * for (ActivityContext t : c) { HashMap<ActivityIdentifier, ActivityRecord>
     * m = contextMap.get(t);
     * 
     * if (m == null) { m = new HashMap<ActivityIdentifier, ActivityRecord>();
     * contextMap.put(t, m); }
     * 
     * m.put(a.identifier(), a); } }
     * 
     * public synchronized ActivityRecord get(ActivityIdentifier id) { return
     * map.get(id); }
     * 
     * 
     * private ActivityIdentifier selectForSteal(WorkerContext [] wc) {
     * 
     * for (WorkerContext w : wc) {
     * 
     * HashMap<ActivityIdentifier, ActivityRecord> m = contextMap.get(w);
     * 
     * if (m != null && m.size() > 0) { // Man this is expensive!
     * Iterator<ActivityIdentifier> itt = m.keySet().iterator(); return
     * itt.next(); } }
     * 
     * return null; }
     * 
     * public synchronized ActivityRecord steal(WorkerContext c) {
     * 
     * ActivityIdentifier id = null;
     * 
     * if (c.isOr()) {
     * 
     * OrWorkerContext s = (OrWorkerContext) c;
     * 
     * if (c.size() > 0) { id = selectForSteal(s.getContexts()); }
     * 
     * } else { id = selectForSteal(new WorkerContext[] { c }); }
     * 
     * if (id == null) { // No suitable ActivityRecord found! return null; }
     * 
     * return remove(id); }
     */

}

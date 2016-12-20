package test.pipeline.inbalance;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.context.UnitActivityContext;

public class Stage5 extends Activity {

    private static final long serialVersionUID = -2003940189338627474L;

    private final ActivityIdentifier parent;
    private final long sleep;

    private Data result3;
    private Data result4;

    public Stage5(ActivityIdentifier parent, int index, long sleep) {

        super(new UnitActivityContext("E", index), true);

        this.parent = parent;
        this.sleep = sleep;
    }

    @Override
    public int initialize(Constellation c) {
        return SUSPEND;
    }

    @Override
    public void cleanup(Constellation c) {

        Data result = processData();

        System.out.println("Finished pipeline: " + result.index);

        c.send(new Event(identifier(), parent, result));
    }

    private Data processData() {

        // Simulate some processing here that takes 'sleep' time
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (Exception e) {
                // ignored
            }
        }

        return new Data(result3.index, 5, result3.data);
    }

    @Override
    public int process(Constellation c, Event e) {

        Data data = (Data) e.getData();

        if (data.stage == 3) {
            result3 = data;
        } else {
            result4 = data;
        }

        if (result3 != null && result4 != null) {
            return FINISH;
        } else {
            return SUSPEND;
        }
    }
}

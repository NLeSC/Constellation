package test.pipeline.simple;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.SimpleActivity;
import ibis.constellation.context.UnitActivityContext;

public class Pipeline extends SimpleActivity {

    private static final long serialVersionUID = -3987089095770723454L;

    private final int index;
    private final int current;
    private final int last;
    private final long sleep;
    private final Object data;

    public Pipeline(ActivityIdentifier parent, int index, int current, int last, long sleep, Object data) {

        super(parent, new UnitActivityContext("X", current));

        this.index = index;
        this.current = current;
        this.last = last;
        this.sleep = sleep;
        this.data = data;
    }

    @Override
    public void simpleActivity() {

        System.out.println("RUNNING pipeline " + index + " " + current + " " + last);

        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (Exception e) {
                // ignored
            }
        }

        if (current == last) {

            System.out.println("Sending pipeline reply");

            send(new Event(identifier(), getParent(), data));
        } else {

            System.out.println("Submitting pipeline stage: " + index + " " + (current + 1) + " " + last);

            submit(new Pipeline(getParent(), index, current + 1, last, sleep, data));
        }
    }
}

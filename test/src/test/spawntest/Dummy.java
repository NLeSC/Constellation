package test.spawntest;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Event;
import ibis.constellation.SimpleActivity;
import ibis.constellation.context.UnitActivityContext;

public class Dummy extends SimpleActivity {

    private static final long serialVersionUID = 5970093414747228592L;

    public Dummy(ActivityIdentifier parent) {
        super(parent, new UnitActivityContext("TEST", 1));
    }

    @Override
    public void simpleActivity() {

        double tmp = 0.33333333;

        long time = System.nanoTime();

        do {
            tmp = Math.cos(tmp);
        } while (System.nanoTime() - time < 100000);

        send(new Event(identifier(), getParent(), null));
        finish();
    }

}

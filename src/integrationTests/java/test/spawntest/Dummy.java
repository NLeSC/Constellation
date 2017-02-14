package test.spawntest;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.Context;
import ibis.constellation.util.SimpleActivity;

public class Dummy extends SimpleActivity {

    private static final long serialVersionUID = 5970093414747228592L;

    public Dummy(ActivityIdentifier parent) {
        super(parent, new Context("TEST", 1, 1));
    }

    @Override
    public void simpleActivity(Constellation c) {

        double tmp = 0.33333333;

        long time = System.nanoTime();

        do {
            tmp = Math.cos(tmp);
        } while (System.nanoTime() - time < 100000);

        c.send(new Event(identifier(), getParent(), null));
    }

}

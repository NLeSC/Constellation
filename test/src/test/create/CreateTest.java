package test.create;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Executor;
import ibis.constellation.SimpleExecutor;
import ibis.constellation.StealStrategy;
import ibis.constellation.context.UnitExecutorContext;

public class CreateTest {

    public static void main(String[] args) {

        // Simple test that creates, starts and stops a cohort
        try {
            int executors = Integer.parseInt(args[0]);

            Executor[] e = new Executor[executors];

            for (int i = 0; i < executors; i++) {
                e[i] = new SimpleExecutor(new UnitExecutorContext("DEFAULT"),
                        StealStrategy.ANY);
            }

            Constellation c = ConstellationFactory.createConstellation(e);
            c.activate();
            c.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

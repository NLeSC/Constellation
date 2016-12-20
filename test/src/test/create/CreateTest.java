package test.create;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.context.UnitExecutorContext;

public class CreateTest {

    public static void main(String[] args) {

        // Simple test that creates, starts and stops a constellation.
        try {
            int executors = Integer.parseInt(args[0]);

            ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DEFAULT"));
            Constellation c = ConstellationFactory.createConstellation(config, executors);
            c.activate();
            c.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

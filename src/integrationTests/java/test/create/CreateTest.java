package test.create;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.context.UnitExecutorContext;

public class CreateTest {

    @Test
    public void createDefault() throws ConstellationCreationException {
        
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
                
        ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DEFAULT"));
        Constellation c = ConstellationFactory.createConstellation(p, config);
        c.activate();
        c.done();
    }

    @Test
    public void createOne() throws ConstellationCreationException {
        int count = 1;

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
        
        ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DEFAULT"));
        Constellation c = ConstellationFactory.createConstellation(p, config, count);
        
        c.activate();
        c.done();
    }

    @Test
    public void createFourTheSame() throws ConstellationCreationException {
        int count = 4;
        
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
        
        ConstellationConfiguration config = new ConstellationConfiguration(new UnitExecutorContext("DEFAULT"));
        Constellation c = ConstellationFactory.createConstellation(p, config, count);
        c.activate();
        c.done();
    }
    
    @Test
    public void createFourDifferent() throws ConstellationCreationException {
        
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
        
        ConstellationConfiguration [] config = new ConstellationConfiguration[4];
        
        config[0] = new ConstellationConfiguration(new UnitExecutorContext("ONE"));
        config[1] = new ConstellationConfiguration(new UnitExecutorContext("TWO"));
        config[2] = new ConstellationConfiguration(new UnitExecutorContext("THREE"));
        config[3] = new ConstellationConfiguration(new UnitExecutorContext("FOUR"));
        
        Constellation c = ConstellationFactory.createConstellation(p, config);
        c.activate();
        c.done();
    }
}

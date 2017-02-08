package test.create;

import java.util.Properties;

import org.junit.Test;

import ibis.constellation.Constellation;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationCreationException;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Context;

public class CreateTest {

    @Test
    public void createDefault() throws ConstellationCreationException {
        
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");

        Context cxt = new Context("DEFAULT", 0, 0);
        
        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(p, config);
        c.activate();
        c.done();
    }

    @Test
    public void createOne() throws ConstellationCreationException {
        int count = 1;

        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
        
        Context cxt = new Context("DEFAULT", 0, 0);
        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(p, config, count);
        
        c.activate();
        c.done();
    }

    @Test
    public void createFourTheSame() throws ConstellationCreationException {
        int count = 4;
        
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
        
        Context cxt = new Context("DEFAULT", 0, 0);
        ConstellationConfiguration config = new ConstellationConfiguration(cxt);
        Constellation c = ConstellationFactory.createConstellation(p, config, count);
        c.activate();
        c.done();
    }
    
    @Test
    public void createFourDifferent() throws ConstellationCreationException {
        
        Properties p = new Properties();
        p.put("ibis.constellation.distributed", "false");
        
        ConstellationConfiguration [] config = new ConstellationConfiguration[4];
        
        config[0] = new ConstellationConfiguration(new Context("ONE", 1, 1));
        config[1] = new ConstellationConfiguration(new Context("TWO", 2, 2));
        config[2] = new ConstellationConfiguration(new Context("THREE", 3, 3));
        config[3] = new ConstellationConfiguration(new Context("FOUR", 4, 4));
        
        Constellation c = ConstellationFactory.createConstellation(p, config);
        c.activate();
        c.done();
    }
}

package ibis.constellation;

import ibis.constellation.context.UnitWorkerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleExecutor extends Executor {

    public static final Logger logger = LoggerFactory
            .getLogger(SimpleExecutor.class);
    private static final long serialVersionUID = -2498570099898761363L;

    public SimpleExecutor(StealPool pool, StealPool stealFrom, WorkerContext c,
            StealStrategy local, StealStrategy constellation,
            StealStrategy remote) {
        super(pool, stealFrom, c, local, constellation, remote);
    }

    public SimpleExecutor(StealPool pool, StealPool stealFrom,
            WorkerContext c) {
        super(pool, stealFrom, c, StealStrategy.ANY, StealStrategy.ANY,
                StealStrategy.ANY);
    }

    public SimpleExecutor(StealPool pool, StealPool stealFrom, WorkerContext c,
            StealStrategy st) {
        super(pool, stealFrom, c, st, st, st);
    }

    public SimpleExecutor() {
        super(StealPool.WORLD, StealPool.WORLD, UnitWorkerContext.DEFAULT,
                StealStrategy.ANY, StealStrategy.ANY, StealStrategy.ANY);
    }

    public SimpleExecutor(WorkerContext wc) {
        super(StealPool.WORLD, StealPool.WORLD, wc, StealStrategy.ANY,
                StealStrategy.ANY, StealStrategy.ANY);
    }

    public SimpleExecutor(WorkerContext wc, StealStrategy s) {
        super(StealPool.WORLD, StealPool.WORLD, wc, s, s, s);
    }

    public SimpleExecutor(WorkerContext wc, StealStrategy local,
            StealStrategy constellation, StealStrategy remote) {
        super(StealPool.WORLD, StealPool.WORLD, wc, local, constellation,
                remote);
    }

    public SimpleExecutor(WorkerContext wc, StealStrategy local,
            StealStrategy remote) {
        super(StealPool.WORLD, StealPool.WORLD, wc, local, remote, remote);
    }

    @Override
    public void run() {

        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder(
                    "\nStarting Executor: " + identifier() + "\n");

            sb.append("        context: " + context + "\n");
            sb.append("           pool: " + myPool + "\n");
            sb.append("    steals from: " + stealsFrom + "\n");
            sb.append("          local: " + localStealStrategy + "\n");
            sb.append("  constellation: " + constellationStealStrategy + "\n");
            sb.append("         remote: " + remoteStealStrategy + "\n");
            sb.append("--------------------------");

            logger.info(sb.toString());
        }

        boolean done = false;

        while (!done) {
            done = processActivities();
        }

        logger.info("Executor done!");

    }

}

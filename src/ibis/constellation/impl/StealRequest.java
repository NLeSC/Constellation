package ibis.constellation.impl;

import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;
import ibis.constellation.WorkerContext;

public class StealRequest extends Message {

    private static final long serialVersionUID = 2655647847327367590L;

    public final WorkerContext context;
    public final StealStrategy localStrategy;
    public final StealStrategy constellationStrategy;
    public final StealStrategy remoteStrategy;
    public final StealPool pool;
    public final int size;

    // Note allowRestricted is set to false when the StealRequest traverses the
    // network.
    private transient boolean isLocal;

    public StealRequest(final ConstellationIdentifier source,
            final WorkerContext context, final StealStrategy localStrategy,
            final StealStrategy constellationStrategy,
            final StealStrategy remoteStrategy, final StealPool pool,
            final int size) {

        super(source);
        this.context = context;
        this.localStrategy = localStrategy;
        this.constellationStrategy = constellationStrategy;
        this.remoteStrategy = remoteStrategy;
        this.pool = pool;
        this.size = size;

        isLocal = true;
    }

    @Override
    public boolean requiresRandomSelection() {
        return true;
    }

    public void setRemote() {
        isLocal = false;
    }

    public boolean isLocal() {
        return isLocal;
    }
}

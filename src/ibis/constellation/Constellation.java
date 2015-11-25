package ibis.constellation;

public interface Constellation {

    public ActivityIdentifier submit(Activity job);

    public void send(Event e);

    public void cancel(ActivityIdentifier activity);

    public boolean activate();

    public void done(Concluder concluder);

    public void done();

    public boolean isMaster();

    public ConstellationIdentifier identifier();

    public WorkerContext getContext();

    public Stats getStats();
}

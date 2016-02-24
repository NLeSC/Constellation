package ibis.constellation;

public abstract class Stats {

    protected Stats() {
    }

    public abstract CTimer getTimer();

    public abstract CTimer getTimer(String standardDevice,
            String standardThread, String standardAction);
}

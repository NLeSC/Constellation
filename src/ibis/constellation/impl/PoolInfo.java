package ibis.constellation.impl;

import ibis.ipl.IbisIdentifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class PoolInfo implements Serializable {

    final String tag;

    final IbisIdentifier master;
    final boolean isMaster;
    final boolean isDummy;

    private long timestamp;
    private ArrayList<IbisIdentifier> members;

    PoolInfo(String tag, IbisIdentifier master, boolean isMaster) {
        this.tag = tag;
        this.master = master;
        this.isMaster = isMaster;
        this.isDummy = false;
        members = new ArrayList<IbisIdentifier>();
        members.add(master);
        timestamp = 1;
    }

    PoolInfo(String tag) {
        this.tag = tag;
        this.master = null;
        this.isMaster = false;
        this.isDummy = true;
        members = new ArrayList<IbisIdentifier>();
        timestamp = 1;
    }

    PoolInfo(PoolInfo other, IbisIdentifier master) {
        this.tag = other.tag;
        this.master = master;
        this.isMaster = true;
        this.isDummy = true;
        members = other.members;
        timestamp = other.timestamp;
        members.add(master);
    }

    synchronized boolean hasMembers() {
        return members.size() != 0;
    }

    synchronized void addMember(IbisIdentifier id) {
        members.add(id);
        timestamp++;
    }

    synchronized void removeMember(IbisIdentifier id) {
        members.remove(id);
        timestamp++;
    }

    synchronized int nMembers() {
        return members.size();
    }

    synchronized long currentTimeStamp() {
        return timestamp;
    }

    synchronized IbisIdentifier selectRandom(Random random) {
        return members.get(random.nextInt(members.size()));
    }
}

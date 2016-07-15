package ibis.constellation.impl.pool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.ipl.IbisIdentifier;

class PoolInfo implements Serializable {

    private static final Logger logger = LoggerFactory
            .getLogger(PoolInfo.class);

    final String tag;

    final IbisIdentifier master;
    final boolean isMaster;
    final boolean isDummy;

    private long timestamp;
    private ArrayList<IbisIdentifier> members;

    PoolInfo(String tag, IbisIdentifier master, boolean isMaster) {
        if (logger.isInfoEnabled()) {
            logger.info(
                    "Creating pool with tag " + tag + " and member " + master);
        }
        this.tag = tag;
        this.master = master;
        this.isMaster = isMaster;
        this.isDummy = false;
        members = new ArrayList<IbisIdentifier>();
        members.add(master);
        timestamp = 1;
    }

    PoolInfo(PoolInfo orig) {
        this.tag = orig.tag;
        this.master = orig.master;
        this.isMaster = orig.isMaster;
        this.isDummy = orig.isDummy;
        this.timestamp = orig.timestamp;
        this.members = new ArrayList<IbisIdentifier>(orig.members);
    }

    PoolInfo(String tag) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating pool with tag " + tag);
        }
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
        if (logger.isInfoEnabled()) {
            logger.info("Adding " + id + " to pool with tag " + tag);
        }
        members.add(id);
        timestamp++;
    }

    synchronized void removeMember(IbisIdentifier id) {
        if (logger.isInfoEnabled()) {
            logger.info("Removing " + id + " from pool with tag " + tag);
        }
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
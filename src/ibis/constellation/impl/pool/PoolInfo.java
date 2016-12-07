package ibis.constellation.impl.pool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.impl.pool.communication.NodeIdentifier;

class PoolInfo implements Serializable {

    private static final long serialVersionUID = -5390055224656923666L;

    private static final Logger logger = LoggerFactory.getLogger(PoolInfo.class);

    final String tag;

    final NodeIdentifier master;
    final boolean isMaster;
    final boolean isDummy;

    private long timestamp;
    private ArrayList<NodeIdentifier> members;

    PoolInfo(String tag, NodeIdentifier master, boolean isMaster) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating pool with tag " + tag + " and member " + master);
        }
        this.tag = tag;
        this.master = master;
        this.isMaster = isMaster;
        this.isDummy = false;
        members = new ArrayList<NodeIdentifier>();
        members.add(master);
        timestamp = 1;
    }

    PoolInfo(PoolInfo orig) {
        this.tag = orig.tag;
        this.master = orig.master;
        this.isMaster = orig.isMaster;
        this.isDummy = orig.isDummy;
        this.timestamp = orig.timestamp;
        this.members = new ArrayList<NodeIdentifier>(orig.members);
    }

    PoolInfo(String tag) {
        if (logger.isInfoEnabled()) {
            logger.info("Creating pool with tag " + tag);
        }
        this.tag = tag;
        this.master = null;
        this.isMaster = false;
        this.isDummy = true;
        members = new ArrayList<NodeIdentifier>();
        timestamp = 1;
    }

    PoolInfo(PoolInfo other, NodeIdentifier master) {
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

    synchronized void addMember(NodeIdentifier id) {
        if (logger.isInfoEnabled()) {
            logger.info("Adding " + id + " to pool with tag " + tag);
        }
        members.add(id);
        timestamp++;
    }

    synchronized void removeMember(NodeIdentifier id) {
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

    synchronized NodeIdentifier selectRandom(Random random) {
        NodeIdentifier id = members.get(random.nextInt(members.size()));
        if (logger.isInfoEnabled()) {
            logger.info("Selecting " + id + " from list of " + members.size() + " members");
        }
        return id;
    }
}
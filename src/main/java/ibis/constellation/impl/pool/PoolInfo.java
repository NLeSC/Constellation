/*
 * Copyright 2019 Vrije Universiteit Amsterdam
 *                Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private final String tag;

    private final NodeIdentifier master;
    private final boolean isMaster;
    private final boolean isDummy;

    public ArrayList<NodeIdentifier> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<NodeIdentifier> members) {
        this.members = members;
    }

    public String getTag() {
        return tag;
    }

    public NodeIdentifier getMaster() {
        return master;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public boolean isDummy() {
        return isDummy;
    }

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

    public synchronized boolean hasMembers() {
        return members.size() != 0;
    }

    public synchronized void addMember(NodeIdentifier id) {
        if (logger.isInfoEnabled()) {
            logger.info("Adding " + id + " to pool with tag " + tag);
        }
        members.add(id);
        timestamp++;
    }

    public synchronized void removeMember(NodeIdentifier id) {
        if (logger.isInfoEnabled()) {
            logger.info("Removing " + id + " from pool with tag " + tag);
        }
        members.remove(id);
        timestamp++;
    }

    public synchronized int nMembers() {
        return members.size();
    }

    public synchronized long currentTimeStamp() {
        return timestamp;
    }

    public synchronized NodeIdentifier selectRandom(Random random) {
        NodeIdentifier id = members.get(random.nextInt(members.size()));
        if (logger.isDebugEnabled()) {
            logger.debug("Selecting " + id + " from list of " + members.size() + " members");
        }
        return id;
    }
}

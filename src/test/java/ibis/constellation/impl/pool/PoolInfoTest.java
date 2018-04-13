/**
 * Copyright 2013 Netherlands eScience Center
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import ibis.constellation.impl.pool.communication.NodeIdentifier;
import ibis.constellation.impl.pool.communication.ibis.NodeIdentifierImpl;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.impl.Location;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class PoolInfoTest {

    @Test
    public void testCreate1() {
        PoolInfo tmp = new PoolInfo("Hello");
        assertEquals(tmp.getTag(), "Hello");
    }

    @Test
    public void testCreate2() {
        PoolInfo tmp = new PoolInfo("Hello");
        assertTrue(tmp.isDummy());
    }

    @Test
    public void testMaster1() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo("Hello", new NodeIdentifierImpl(id), true);
        assertTrue(tmp.isMaster());
    }

    @Test
    public void testMaster2() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo("Hello", new NodeIdentifierImpl(id), false);
        assertFalse(tmp.isMaster());
    }

    @Test
    public void testMaster3() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        PoolInfo tmp = new PoolInfo("Hello", id, true);
        assertEquals(tmp.getMaster(), id);
    }

    @Test
    public void testMaster4() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo("Hello", new NodeIdentifierImpl(id), true);
        assertFalse(tmp.isDummy());
    }

    @Test
    public void testMaster5() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo("Hello", new NodeIdentifierImpl(id), true);
        assertEquals(tmp.currentTimeStamp(), 1);
    }

    @Test
    public void testCopyConstructor1() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", new NodeIdentifierImpl(id), true));
        assertEquals(tmp.getTag(), "Hello");
    }

    @Test
    public void testCopyConstructor2() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", id, true));
        assertEquals(tmp.getMaster(), id);
    }

    @Test
    public void testCopyConstructor3() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", new NodeIdentifierImpl(id), true));
        assertTrue(tmp.isMaster());
    }

    @Test
    public void testCopyConstructor4() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", new NodeIdentifierImpl(id), true));
        assertEquals(tmp.currentTimeStamp(), 1);
    }

    @Test
    public void testCopyConstructor5() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", new NodeIdentifierImpl(id), true));
        assertFalse(tmp.isDummy());
    }

    @Test
    public void testCopyConstructor6() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", new NodeIdentifierImpl(id), true));

        IbisIdentifier id2 = new FakeIbisIdentifier(l, "ibis2", "pool", "tag");
        PoolInfo tmp2 = new PoolInfo(tmp, new NodeIdentifierImpl(id2));

        assertTrue(tmp2.isDummy());
    }

    @Test
    public void testCopyConstructor7() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", new NodeIdentifierImpl(id), true));

        IbisIdentifier id2 = new FakeIbisIdentifier(l, "ibis2", "pool", "tag");
        PoolInfo tmp2 = new PoolInfo(tmp, new NodeIdentifierImpl(id2));

        assertTrue(tmp2.isMaster());
    }

    @Test
    public void testCopyConstructor8() {
        Location l = new Location("loc1");
        IbisIdentifier id = new FakeIbisIdentifier(l, "ibis", "pool", "tag");
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", new NodeIdentifierImpl(id), true));

        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));
        PoolInfo tmp2 = new PoolInfo(tmp, id2);

        assertEquals(tmp2.getMaster(), id2);
    }

    @Test
    public void testCopyConstructor9() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        PoolInfo tmp = new PoolInfo(new PoolInfo("Hello", id, true));

        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));
        PoolInfo tmp2 = new PoolInfo(tmp, id2);

        ArrayList<NodeIdentifier> list = new ArrayList<NodeIdentifier>();
        list.add(id);
        list.add(id2);

        assertEquals(tmp2.getMembers(), list);
    }

    @Test
    public void testAddMembers1() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);
        tmp.addMember(id2);

        assertEquals(tmp.currentTimeStamp(), 2);
    }

    @Test
    public void testAddMembers2() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);
        tmp.addMember(id2);

        ArrayList<NodeIdentifier> list = new ArrayList<NodeIdentifier>();

        list.add(id);
        list.add(id2);

        assertEquals(tmp.getMembers(), list);
    }

    @Test
    public void testAddMembers3() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);
        tmp.addMember(id2);

        assertEquals(tmp.nMembers(), 2);
    }

    @Test
    public void testAddMembers4() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);

        assertEquals(tmp.nMembers(), 1);
    }

    @Test
    public void testRemoveMembers1() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);
        tmp.addMember(id2);
        tmp.removeMember(id);

        assertEquals(tmp.nMembers(), 1);
    }

    @Test
    public void testRemoveMembers2() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));
        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);
        tmp.addMember(id2);
        tmp.removeMember(id);

        ArrayList<NodeIdentifier> list = new ArrayList<NodeIdentifier>();
        list.add(id2);

        assertEquals(tmp.getMembers(), list);
    }

    @Test
    public void testHasMembers1() {
        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);

        assertTrue(tmp.hasMembers());
    }

    @Test
    public void testHasMembers2() {
        PoolInfo tmp = new PoolInfo("Hello");
        assertFalse(tmp.hasMembers());
    }

    @Test
    public void testSetMembers1() {

        Location l = new Location("loc1");
        NodeIdentifier id = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis1", "pool", "tag"));
        NodeIdentifier id2 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis2", "pool", "tag"));
        NodeIdentifier id3 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis3", "pool", "tag"));
        NodeIdentifier id4 = new NodeIdentifierImpl(new FakeIbisIdentifier(l, "ibis4", "pool", "tag"));

        PoolInfo tmp = new PoolInfo("Hello", id, true);
        tmp.addMember(id2);

        ArrayList<NodeIdentifier> list = new ArrayList<NodeIdentifier>();
        list.add(id3);
        list.add(id4);

        tmp.setMembers(list);

        assertEquals(tmp.getMembers(), list);
    }

}

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

import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class FakeIbisIdentifier implements IbisIdentifier {

    private static final long serialVersionUID = 1L;
    private final Location location;
    private final String name;
    private final String poolName;
    private final String tag;

    public FakeIbisIdentifier(Location location, String name, String poolName, String tag) {
        this.location = location;
        this.name = name;
        this.poolName = poolName;
        this.tag = tag;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IbisIdentifier arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see ibis.ipl.IbisIdentifier#location()
     */
    @Override
    public Location location() {
        return location;
    }

    /* (non-Javadoc)
     * @see ibis.ipl.IbisIdentifier#name()
     */
    @Override
    public String name() {
        return name;
    }

    /* (non-Javadoc)
     * @see ibis.ipl.IbisIdentifier#poolName()
     */
    @Override
    public String poolName() {
        return poolName;
    }

    /* (non-Javadoc)
     * @see ibis.ipl.IbisIdentifier#tag()
     */
    @Override
    public byte[] tag() {
        return tag.getBytes();
    }

    /* (non-Javadoc)
     * @see ibis.ipl.IbisIdentifier#tagAsString()
     */
    @Override
    public String tagAsString() {
        return tag;
    }
}

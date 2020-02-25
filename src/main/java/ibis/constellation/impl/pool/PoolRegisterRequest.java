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

import ibis.constellation.impl.pool.communication.NodeIdentifier;

class PoolRegisterRequest implements Serializable {

    private static final long serialVersionUID = -4258898100133094472L;

    public NodeIdentifier source;
    public String tag;

    PoolRegisterRequest(NodeIdentifier source, String tag) {
        this.source = source;
        this.tag = tag;
    }
}
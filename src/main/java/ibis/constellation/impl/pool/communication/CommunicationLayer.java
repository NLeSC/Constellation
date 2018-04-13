/*
 * Copyright 2018 Netherlands eScience Center
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
package ibis.constellation.impl.pool.communication;

import java.io.IOException;

public interface CommunicationLayer {

    public NodeIdentifier getMyIdentifier();

    public boolean sendMessage(NodeIdentifier dest, Message m);

    // Message receipt is implicit, in that the upcall() method of Pool gets
    // called.

    public NodeIdentifier getMaster();

    public int getRank();

    public int getPoolSize();

    public void terminate() throws IOException;

    public void cleanup();

    public NodeIdentifier getElectionResult(String electTag, long timeout) throws IOException;

    public NodeIdentifier elect(String electTag) throws IOException;

    public void activate();

    public NodeIdentifier[] getNodeIdentifiers();

    public void cleanup(NodeIdentifier node);
}

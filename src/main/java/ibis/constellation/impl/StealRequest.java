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
package ibis.constellation.impl;

import ibis.constellation.AbstractContext;
import ibis.constellation.StealPool;
import ibis.constellation.StealStrategy;

public class StealRequest extends AbstractMessage {

    private static final long serialVersionUID = 2655647847327367590L;

    public final AbstractContext context;
    public final StealStrategy localStrategy;
    public final StealStrategy constellationStrategy;
    public final StealStrategy remoteStrategy;
    public final StealPool pool;
    public final int size;

    // Note allowRestricted is set to false when the StealRequest traverses the network.
    private transient boolean isLocal;

    public StealRequest(final ConstellationIdentifierImpl source, final AbstractContext context,
            final StealStrategy localStrategy, final StealStrategy constellationStrategy, final StealStrategy remoteStrategy,
            final StealPool pool, final int size) {

        super(source);

        checkNull(context, "Context may not be null");
        checkNull(localStrategy, "Local steal strategy may not be null");
        checkNull(constellationStrategy, "Constellation steal strategy may not be null");
        checkNull(remoteStrategy, "Remote steal strategy may not be null");
        checkNull(pool, "Steal pool may not be null");

        this.context = context;
        this.localStrategy = localStrategy;
        this.constellationStrategy = constellationStrategy;
        this.remoteStrategy = remoteStrategy;
        this.pool = pool;
        this.size = size;

        isLocal = true;
    }

    public void setRemote() {
        isLocal = false;
    }

    public boolean isLocal() {
        return isLocal;
    }
}

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
package ibis.constellation.impl.util;

import ibis.constellation.AbstractContext;
import ibis.constellation.StealStrategy;
import ibis.constellation.impl.ActivityRecord;

public abstract class WorkQueue {

    private final String id;

    protected WorkQueue(String id) {
        this.id = id;
    }

    public abstract void enqueue(ActivityRecord a);

    public abstract ActivityRecord steal(AbstractContext c, StealStrategy s);

    public abstract int size();

    public void enqueue(ActivityRecord[] a) {
        for (ActivityRecord element : a) {
            enqueue(element);
        }
    }

    // TODO: fix steal to allow multiple in one go!
    public int steal(AbstractContext c, StealStrategy s, ActivityRecord[] dst, int off, int len) {

        for (int i = off; i < off + len; i++) {
            dst[i] = steal(c, s);

            if (dst[i] == null) {
                return (i - off);
            }
        }

        return len;
    }

    protected final String getId() {
        return id;
    }

}

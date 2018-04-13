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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import ibis.constellation.StealPool;
import ibis.constellation.util.ByteBuffers;
import ibis.constellation.AbstractContext;

public class StealReply extends AbstractMessage implements ByteBuffers {

    private static final long serialVersionUID = 2655647847327367590L;

    private final StealPool pool;
    private final AbstractContext context;
    private final ActivityRecord[] work;

    public StealReply(final ConstellationIdentifierImpl source, final ConstellationIdentifierImpl target, final StealPool pool,
            final AbstractContext context, final ActivityRecord work) {

        super(source, target);

        if (work == null) {
            this.work = null;
        } else {
            this.work = new ActivityRecord[] { work };
        }
        this.pool = pool;
        this.context = context;
    }

    public StealReply(final ConstellationIdentifierImpl source, final ConstellationIdentifierImpl target, final StealPool pool,
            final AbstractContext context, final ActivityRecord[] work) {
        super(source, target);

        this.pool = pool;
        this.work = work;
        this.context = context;
    }

    public boolean isEmpty() {
        return (work == null || work.length == 0);
    }

    public StealPool getPool() {
        return pool;
    }

    public AbstractContext getContext() {
        return context;
    }

    public ActivityRecord[] getWork() {
        return work;
    }

    public int getSize() {

        if (work == null) {
            return 0;
        }

        // Note: assumes array is filled!
        return work.length;
    }

    @Override
    public void pushByteBuffers(List<ByteBuffer> list) {
        if (work != null) {
            for (Object a : work) {
                if (a != null && a instanceof ByteBuffers) {
                    ((ByteBuffers) a).pushByteBuffers(list);
                }
            }
        }
    }

    @Override
    public void popByteBuffers(List<ByteBuffer> list) {
        if (work != null) {
            for (Object a : work) {
                if (a != null && a instanceof ByteBuffers) {
                    ((ByteBuffers) a).popByteBuffers(list);
                }
            }
        }
    }

    @Override
    public String toString() {
        if (work == null) {
            return "no jobs";
        }
        return Arrays.toString(work);
    }
}

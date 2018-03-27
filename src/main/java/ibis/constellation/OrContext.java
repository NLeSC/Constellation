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

package ibis.constellation;

import java.util.Iterator;

/**
 * An <code>OrContext</code> represents a context that consists of several (more than 1) contexts. This may for instance represent
 * the fact that an activity may be executed by more than one type of executor, or that an executor may run more than one type of
 * activity.
 */
public class OrContext extends AbstractContext implements Iterable<Context> {

    /* Generated */
    private static final long serialVersionUID = 9000504548592658255L;

    private final Context[] contexts;

    private class MyIterator implements Iterator<Context> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < contexts.length;
        }

        @Override
        public Context next() {
            return contexts[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported by this Iterator");
        }
    }

    /**
     * Constructs an OrContext consisting of a list of Contexts.
     *
     * @param contexts
     *            the list of contexts.
     * @exception IllegalArgumentException
     *                is thrown when the length of the list of contexts is smaller than 2, or any of them is null.
     */
    public OrContext(Context... contexts) {
        super();

        if (contexts == null || contexts.length < 2) {
            throw new IllegalArgumentException("OrContext requires at least 2 Contexts");
        }

        for (Context context : contexts) {
            if (context == null) {
                throw new IllegalArgumentException("OrContext does not allow sparse arguments");
            }
        }

        // FIXME: do more checks!?
        this.contexts = contexts.clone();
    }

    /**
     * Returns the number of contexts of which this OrContext exists.
     *
     * @return the number of contexts.
     */
    public int size() {
        return contexts.length;
    }

    /**
     * Returns the Context corresponding to the specified index.
     *
     * @param index
     *            the index
     * @return the corresponding Context.
     * @throws IllegalArgumentException
     *             if the specified index is out of range.
     */
    public Context get(int index) {

        if (index < 0 || index >= contexts.length) {
            throw new IllegalArgumentException("Index " + index + " out of range!");
        }

        return contexts[index];
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("OrContext(");

        for (int i = 0; i < contexts.length; i++) {
            sb.append(contexts[i].toString());

            if (i < contexts.length - 1) {
                sb.append(", ");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    @Override
    public Iterator<Context> iterator() {
        return new MyIterator();
    }
}

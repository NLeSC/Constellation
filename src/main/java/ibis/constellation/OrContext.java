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

import java.util.Arrays;
import java.util.Iterator;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class OrContext extends AbstractContext implements Iterable<Context> {
    
    /* Generated */
    private static final long serialVersionUID = 9000504548592658255L;

    private final Context [] contexts;
    
    private class MyIterator implements Iterator<Context> {

        int index = 0;
        
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
        }
    }
    
    public OrContext(Context ... contexts) { 
        super();
        
        if (contexts == null || contexts.length < 2) { 
            throw new IllegalArgumentException("OrContext requires at least 2 RangeContexts");
        }
        
        for (int i=0;i<contexts.length;i++) { 
            if (contexts[i] == null) { 
                throw new IllegalArgumentException("OrContext does not allow sparse arguments");
            }
        }
        
        //System.err.println("Created ORCONTEXT " + Arrays.toString(contexts));
        
        // FIXME: do more checks!?
        this.contexts = contexts.clone();
    }

    public int size() { 
        return contexts.length;
    }
    
    public Context get(int index) { 
        
        if (index < 0 || index >= contexts.length) { 
            throw new IllegalArgumentException("Index " + index + " out of range!");
        }
        
        return contexts[index];
    }
    
    public String toString() { 
        
        StringBuilder sb = new StringBuilder("OrContext(");
        
        for (int i=0;i<contexts.length;i++) { 
            sb.append(contexts[i].toString());
            
            if (i < contexts.length-1) { 
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

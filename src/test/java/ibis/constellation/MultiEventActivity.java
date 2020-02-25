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
package ibis.constellation;

import java.util.ArrayList;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class MultiEventActivity extends Activity {

    private static final long serialVersionUID = -4021583343422065387L;
   
    public final int count;
    
    public boolean initialized = false;
    public int gotEvents = 0;
    public boolean clean = false;
    public ArrayList<Event> events = new ArrayList<>();
    
    public MultiEventActivity(int count) {
        this(new Context("DEFAULT", 0, 0), count);
    }

    public MultiEventActivity(AbstractContext c, int count) {
        this(c, true, true, count);
    }
    
    public MultiEventActivity(AbstractContext c, boolean expectsEvents, int count) {
        this(c, true, expectsEvents, count);
    }
    
    public MultiEventActivity(AbstractContext c, boolean mayBeStolen, boolean expectsEvents, int count) {
        super(c, mayBeStolen, expectsEvents);
        this.count = count;
    }
    
    @Override
    public void setIdentifier(ActivityIdentifier id) {
        super.setIdentifier(id);
    }
    
    @Override
    public int initialize(Constellation constellation) {
        initialized = true;
        return SUSPEND;
    }

    @Override
    public int process(Constellation constellation, Event event) {
        gotEvents++;
        events.add(event);
        
        if (gotEvents < count) { 
            return SUSPEND;
        } else { 
            return FINISH;
        }
    }

    @Override
    public void cleanup(Constellation constellation) {
        clean = true;
    }
    
    @Override
    public String toString() { 
        return "MEA";
    }
}

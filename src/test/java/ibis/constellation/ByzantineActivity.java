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

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ByzantineActivity extends Activity {

    private static final long serialVersionUID = -4021583343422065387L;
    
    int initializeResponse;
    int processResponse;
    
    public ByzantineActivity(AbstractContext c, int initializeResponse, int processResponse) {
        super(c, true);
        this.initializeResponse = initializeResponse;
        this.processResponse = processResponse;
    }
        
    @Override
    public void setIdentifier(ActivityIdentifier id) {
        super.setIdentifier(id);
    }
    
    @Override
    public int initialize(Constellation constellation) {
        return initializeResponse;
    }

    @Override
    public int process(Constellation constellation, Event event) {
        return processResponse;
    }

    @Override
    public void cleanup(Constellation constellation) {
    }
}

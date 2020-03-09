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
package ibis.constellation.impl;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.ConstellationIdentifier;
import ibis.constellation.Event;

import nl.junglecomputing.timer.Timer;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class FakeConstellation implements Constellation {

    @Override
    public ActivityIdentifier submit(Activity activity) {
        return null;
    }

    @Override
    public void send(Event e) {
        // nothing
    }

    @Override
    public boolean activate() {
        return true;
    }

    @Override
    public void done() {
        // nothing
    }

    @Override
    public boolean isMaster() {
        return true;
    }

    @Override
    public ConstellationIdentifier identifier() {
        return null;
    }

    @Override
    public Timer getTimer(String device, String thread, String action) {
        return null;
    }

    @Override
    public Timer getTimer() {
        return null;
    }

    @Override
    public Timer getOverallTimer() {
        return null;
    }
}

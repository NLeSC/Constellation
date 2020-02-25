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
package ibis.constellation.util;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Event;

/**
 * A <code>SimpleActivity</code> is an activity that only requires a single invocation of its {@link #simpleActivity} method. In
 * particular, it has (default) implementations of {@link Activity#initialize} and {@link Activity#cleanup}, and it does not
 * expect events. It does provide a concept of a parent activity.
 */
public abstract class SimpleActivity extends Activity {

    private static final long serialVersionUID = 1937343247220443457L;

    private final ActivityIdentifier parent;

    /**
     * Constructs a <code>SimpleActivity</code> with the specified parameters. This activity can only be executed by a local
     * executor.
     *
     * @param parent
     *            the activity identifier of the parent activity (may be <code>null</code>).
     * @param context
     *            the context that specifies which executors can actually execute this activity.
     */
    protected SimpleActivity(ActivityIdentifier parent, AbstractContext context) {
        this(parent, context, true);
    }

    /**
     * Constructs a <code>SimpleActivity</code> with the specified parameters.
     *
     * @param parent
     *            the activity identifier of the parent activity (may be <code>null</code>).
     * @param context
     *            the context that specifies which executors can actually execute this activity.
     * @param mayBeStolen
     *            when set, this activity may be stolen by other Constellations.
     */
    protected SimpleActivity(ActivityIdentifier parent, AbstractContext context, boolean mayBeStolen) {
        super(context, mayBeStolen, false);
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     *
     * This version of {@link Activity#initialize} calls {@link #simpleActivity} and returns {@link #FINISH}.
     */
    @Override
    public int initialize(Constellation c) {
        simpleActivity(c);
        return FINISH;
    }

    @Override
    public final int process(Constellation c, Event e) {
        // not used
        return FINISH;
    }

    @Override
    public final void cleanup(Constellation c) {
        // not used
    }

    /**
     * This method, to be implemented by the activity, is called once, after which the activity will finish.
     *
     * @param c
     *            the {@link Constellation} on which the Activity is running
     */
    public abstract void simpleActivity(Constellation c);

    /**
     * Returns the activity identifier of the parent (as set in the constructor), so it may be <code>null</code>).
     *
     * @return the parent activity identifier
     */
    public ActivityIdentifier getParent() {
        return parent;
    }
}

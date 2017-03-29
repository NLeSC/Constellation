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

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the properties that affect the behavior of constellation.
 */
public class ConstellationProperties extends Properties {

    private static final long serialVersionUID = -5316962541092148997L;

    public static final Logger logger = LoggerFactory.getLogger(ConstellationProperties.class);

    /**
     * All properties start with the prefix "ibis.constellation.".
     */
    public static final String S_PREFIX = "ibis.constellation.";

    /**
     * The "distributed" property is a boolean property instructing the {@link ConstellationFactory} whether to create a
     * distributed constellation or not. The default is "true".
     */
    public static final String S_DISTRIBUTED = S_PREFIX + "distributed";

    public final boolean DISTRIBUTED;

    /**
     * The "stealing" property is a string property defining the steal strategy to use. There are three: <br>
     * "pool" indicates: steal from anyone <br>
     * "mw" indicates: master-worker, so only steal from master <br>
     * "none" indicates: don't steal at all. <br>
     * The default value is: "pool".
     */
    public static final String S_STEALSTRATEGY = S_PREFIX + "stealing";

    public final String STEALSTRATEGY;

    private static final String S_REMOTESTEAL_PREFIX = S_PREFIX + "remotesteal.";

    private static final String S_STEAL_PREFIX = S_PREFIX + "steal.";

    /**
     * The "remotesteal.throttle" property is a boolean property indicating whether only one outstanding remote steal request per
     * UnitExecutorContext is allowed. The default value is "false".
     */
    public static final String S_REMOTESTEAL_THROTTLE = S_REMOTESTEAL_PREFIX + "throttle";

    public final boolean REMOTESTEAL_THROTTLE;

    /**
     * The "remotesteal.timeout" property is an integer property indicating the timeout for remote steal requests, in
     * milliseconds. The default is "5000".
     */
    public static final String S_REMOTESTEAL_TIMEOUT = S_REMOTESTEAL_PREFIX + "timeout";

    public final int REMOTESTEAL_TIMEOUT;

    /**
     * The "profile" property is a boolean property indicating whether constellation should provide some timing information. The
     * default is "false".
     */
    public static final String S_PROFILE = S_PREFIX + "profile";

    public final boolean PROFILE;

    /**
     * The "profile.communication" property is a boolean property indicating whether constellation should provide some timing
     * information on steql or event messages. The default is "false". Note: the overhead for this profiler can be large.
     */
    public static final String S_PROFILE_COMMUNICATION = S_PROFILE + ".communication";

    public final boolean PROFILE_COMMUNICATION;

    /**
     * The "profile.steal" property is a boolean property indicating whether constellation should provide some timing information
     * on steqls. The default is "false". Note: the overhead for this profiler can be large.
     */
    public static final String S_PROFILE_STEAL = S_PROFILE + ".steal";

    public final boolean PROFILE_STEAL;

    /**
     * The "profile.output" property is a string property indicating an output file to write profile info to. If not specified
     * (default), <code>System.out</code> is used.
     */
    public static final String S_PROFILE_OUTPUT = S_PROFILE + ".output";

    public final String PROFILE_OUTPUT;

    /**
     * The "printStatistics" property is a boolean property indicating whether some constellation statistics should be printed or
     * not. Default is "false".
     *
     * See also {@link #S_STATISTICS_OUTPUT}.
     */
    public static final String S_STATISTICS = S_PREFIX + "statistics";

    public final boolean STATISTICS;

    /**
     * The "statistics.output" property is a string property indicating an output file to write statistics to. If not specified
     * (default), <code>System.out</code> is used.
     */
    public static final String S_STATISTICS_OUTPUT = S_STATISTICS + ".output";

    public final String STATISTICS_OUTPUT;

    /**
     * The "steal.delay" property is an integer property, specifying the minimum time interval between failed steal attempts, in
     * milliseconds. The default is "20".
     */
    public static final String S_STEAL_DELAY = S_STEAL_PREFIX + "delay";

    public final int STEAL_DELAY;

    /**
     * The "steal.size" property is an integer property, specifying how many activities to try and steal on each steal attempt,
     * for local steals. The default is "1".
     */
    public static final String S_STEAL_SIZE = S_STEAL_PREFIX + "size";

    /**
     * The "remotesteal.size" property is an integer property, specifying how many activities to try and steal on each steal
     * attempt, for remote steals. The default is "1".
     */
    public static final String S_REMOTESTEAL_SIZE = S_REMOTESTEAL_PREFIX + "size";

    /** Value of the "steal.size" property. */
    public final int STEAL_SIZE;

    /** Value of the "steal.size" property. */
    public final int REMOTESTEAL_SIZE;

    /**
     * The "steal.ignoreEmptyReplies" property is a boolean property determining whether empty steal replies should be given or
     * not. The default is "false".
     */
    public static final String S_STEAL_IGNORE_EMPTY_REPLIES = S_STEAL_PREFIX + "ignoreEmptyReplies";

    /** Value of the "steal.ignoreEmptyReplies" property. */
    public final boolean STEAL_IGNORE_EMPTY_REPLIES;

    /**
     * The "closed" property is a boolean property indicating whether the current run is a closed run, that is, whether the total
     * number of nodes involved is fixed. If true, the property "poolSize" should be set to the number of nodes.
     */
    public static final String S_CLOSED = S_PREFIX + "closed";

    /** Value of the "closed" property. */
    public final boolean CLOSED;

    /**
     * The "poolSize" property is an integer property indicating the pool size if the current run is a closed run, that is, when
     * the total number of nodes involved is fixed.
     */
    public static final String S_POOLSIZE = S_PREFIX + "poolSize";

    /** Value of the "closed" property. */
    public final int POOLSIZE;

    /**
     * The "master" property is a boolean property indicating whether the current constellation instance is a candidate to be the
     * master. At least one of the constellation instances should be. See {@link Constellation#isMaster()}. The default is "true".
     * Note that only one constellation instance will actually become the master. For that instance,
     * {@link Constellation#isMaster()} will return true, for the other instances it will return false.
     */
    public static final String S_MASTER = S_PREFIX + "master";

    /** Value of the "master" property. */
    public final boolean MASTER;

    /**
     * When an executor spawns new activities, these new activities are initially added to queues that are local to this executor.
     * This makes them easily accessible for this executor, but not so easily accessible for other executors. The "queue.limit"
     * property defines the maximum length of these local queues. When this limit is reached, new activities are pushed onto
     * queues higher up in the system, making them more easily accessible for other executors. The default value is "100".
     */
    public static final String S_QUEUED_JOB_LIMIT = S_PREFIX + "queue.limit";

    /** Value of the "queue.limit" property. */
    public final int QUEUED_JOB_LIMIT;

    /**
     * Creates a <code>ConstellationProperties</code> object using the specified properties.
     *
     * @param p
     *            the properties
     */
    public ConstellationProperties(Properties p) {
        super(p);

        MASTER = getBooleanProperty(S_MASTER, true);
        CLOSED = getBooleanProperty(S_CLOSED, false);
        POOLSIZE = getIntProperty(S_POOLSIZE, -1);
        DISTRIBUTED = getBooleanProperty(S_DISTRIBUTED, true);
        PROFILE = getBooleanProperty(S_PROFILE, false);
        PROFILE_COMMUNICATION = getBooleanProperty(S_PROFILE_COMMUNICATION, false);
        PROFILE_STEAL = getBooleanProperty(S_PROFILE_STEAL, false);
        PROFILE_OUTPUT = getProperty(S_PROFILE_OUTPUT);
        STATISTICS = getBooleanProperty(S_STATISTICS, false);
        STATISTICS_OUTPUT = getProperty(S_STATISTICS_OUTPUT);
        REMOTESTEAL_THROTTLE = getBooleanProperty(S_REMOTESTEAL_THROTTLE, false);
        STEAL_DELAY = getIntProperty(S_STEAL_DELAY, 20);
        STEAL_IGNORE_EMPTY_REPLIES = getBooleanProperty(S_STEAL_IGNORE_EMPTY_REPLIES, false);
        STEAL_SIZE = getIntProperty(S_STEAL_SIZE, 1);
        REMOTESTEAL_SIZE = getIntProperty(S_REMOTESTEAL_SIZE, 1);
        STEALSTRATEGY = getProperty(S_STEALSTRATEGY, "pool");
        REMOTESTEAL_TIMEOUT = getIntProperty(S_REMOTESTEAL_TIMEOUT, 5000);
        QUEUED_JOB_LIMIT = getIntProperty(S_QUEUED_JOB_LIMIT, 100);
        if (logger.isInfoEnabled()) {
            logger.info("MASTER = " + MASTER);
            logger.info("CLOSED = " + CLOSED);
            if (CLOSED) {
                logger.info("POOLSIZE = " + POOLSIZE);
            }
            logger.info("DISTRIBUTED = " + DISTRIBUTED);
            logger.info("PROFILE = " + PROFILE);
            logger.info("PROFILE_COMMUNICATION = " + PROFILE_COMMUNICATION);
            logger.info("PROFILE_STEAL = " + PROFILE_STEAL);
            logger.info("STATISTICS = " + STATISTICS);
            logger.info("STATISTICS_OUTPUT = " + STATISTICS_OUTPUT);
            logger.info("REMOTESTEAL_THROTTLE = " + REMOTESTEAL_THROTTLE);
            logger.info("STEAL_DELAY = " + STEAL_DELAY);
            logger.info("STEAL_IGNORE_EMPTY_REPLIES = " + STEAL_IGNORE_EMPTY_REPLIES);
            logger.info("STEAL_SIZE = " + STEAL_SIZE);
            logger.info("REMOTESTEAL_SIZE = " + REMOTESTEAL_SIZE);
            logger.info("STEALSTRATEGY = " + STEALSTRATEGY);
            logger.info("REMOTESTEAL_TIMEOUT = " + REMOTESTEAL_TIMEOUT);
            logger.info("QUEUED_JOB_LIMIT = " + QUEUED_JOB_LIMIT);
        }
    }

    /**
     * Convenience constructor, using the system properties.
     */
    public ConstellationProperties() {
        this(System.getProperties());
    }

    //    /**
    //     * Returns true if property <code>name</code> is defined and has a value that is conventionally associated with 'true' (as in
    //     * Ant): any of 1, on, true, yes, or nothing.
    //     *
    //     * @return true if property is defined and set
    //     * @param name
    //     *            property name
    //     */
    //    private boolean getBooleanProperty(String name) {
    //        return getBooleanProperty(name, false);
    //    }

    /**
     * Returns true if property <code>name</code> has a value that is conventionally associated with 'true' (as in Ant): any of 1,
     * on, true, yes, or nothing. If the property is not defined, return the specified default value.
     *
     * @return true if property is defined and set
     * @param key
     *            property name
     * @param defaultValue
     *            the value that is returned if the property is absent
     */
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);

        if (value != null) {
            return value.equals("1") || value.equals("on") || value.equals("") || value.equals("true") || value.equals("yes");
        }

        return defaultValue;
    }

    //    /**
    //     * Returns the integer value of property.
    //     *
    //     * @return the integer value of property
    //     * @param key
    //     *            property name
    //     * @throws NumberFormatException
    //     *             if the property is undefined or not an integer
    //     */
    //    private int getIntProperty(String key) {
    //        String value = getProperty(key);
    //
    //        if (value == null) {
    //            throw new NumberFormatException("property undefined: " + key);
    //        }
    //
    //        try {
    //            return Integer.parseInt(value);
    //        } catch (NumberFormatException e) {
    //            throw new NumberFormatException("Integer expected for property " + key + ", not \"" + value + "\"");
    //        }
    //    }

    /**
     * Returns the integer value of property.
     *
     * @return the integer value of property
     * @param key
     *            property name
     * @param defaultValue
     *            default value if the property is undefined
     * @throws NumberFormatException
     *             if the property defined and not an integer
     */
    private int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Integer expected for property " + key + ", not \"" + value + "\"");
        }
    }

}

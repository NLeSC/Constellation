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
 * This exception gets thrown when an activity is submitted from which constellation can figure out that there will not be a
 * suitable executor that can execute it.
 *
 */
public class NoSuitableExecutorException extends Exception {

    private static final long serialVersionUID = 5816385974217284589L;

    /**
     * Creates a NoSuitableExecutorException.
     *
     * @param s
     *            describes the reason.
     */
    public NoSuitableExecutorException(String s) {
        super(s);
    }

    /**
     * Creates a NoSuitableExecutorException.
     *
     * @param s
     *            describes the reason.
     * @param cause
     *            a nested exception that is the cause of this.
     */
    public NoSuitableExecutorException(String s, Throwable cause) {
        super(s, cause);
    }
}

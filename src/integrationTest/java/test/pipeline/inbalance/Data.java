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
package test.pipeline.inbalance;

import java.io.Serializable;

public class Data implements Serializable {

    private static final long serialVersionUID = -1823086406536340980L;

    public final int index;
    public final int stage;
    public final byte[] data;

    public Data(int index, int stage, byte[] data) {
        this.index = index;
        this.stage = stage;
        this.data = data;
    }
}

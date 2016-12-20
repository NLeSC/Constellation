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

import ibis.constellation.context.ExecutorContext;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ConstellationConfiguration {

    private ExecutorContext context = null;

    private StealPool belongsTo = StealPool.WORLD;
    private StealPool stealsFrom = StealPool.WORLD;
    
    private StealStrategy localStealStrategy = StealStrategy.ANY;
    private StealStrategy constellationStealStrategy = StealStrategy.ANY;
    private StealStrategy remoteStealStrategy = StealStrategy.ANY;
    
    public ConstellationConfiguration(ExecutorContext context, StealPool belongsTo, StealPool stealsFrom, 
            StealStrategy localStealStrategy, StealStrategy constellationStealStrategy, StealStrategy remoteStealStrategy) {  
        super();
        this.context = context;
        this.localStealStrategy = localStealStrategy;
        this.constellationStealStrategy = constellationStealStrategy;
        this.remoteStealStrategy = remoteStealStrategy;
        this.belongsTo = belongsTo;
        this.stealsFrom = stealsFrom;
    }

    public ConstellationConfiguration(ExecutorContext context, StealStrategy localStealStrategy, 
            StealStrategy constellationStealStrategy, StealStrategy remoteStealStrategy) {  
        super();
        this.context = context;
        this.localStealStrategy = localStealStrategy;
        this.constellationStealStrategy = constellationStealStrategy;
        this.remoteStealStrategy = remoteStealStrategy;
    }
 
    public ConstellationConfiguration(ExecutorContext context, StealStrategy stealStrategy) { 
        super();
        this.context = context;
        this.localStealStrategy = stealStrategy;
        this.constellationStealStrategy = stealStrategy;
        this.remoteStealStrategy = stealStrategy;
    }
 
    public ConstellationConfiguration(ExecutorContext context, StealStrategy localStealStrategy, StealStrategy remoteStealStrategy) { 
        super();
        this.context = context;
        this.localStealStrategy = localStealStrategy;
        this.constellationStealStrategy = remoteStealStrategy;
        this.remoteStealStrategy = remoteStealStrategy;
    }
    
    public ConstellationConfiguration(ExecutorContext context) { 
        this.context = context;
    }
    
    public ExecutorContext getContext() {
        return context;
    }
    
    public void setContext(ExecutorContext context) {
        this.context = context;
    }
    
    public StealStrategy getLocalStealStrategy() {
        return localStealStrategy;
    }
    
    public void setLocalStealStrategy(StealStrategy localStealStrategy) {
        this.localStealStrategy = localStealStrategy;
    }
    
    public StealStrategy getConstellationStealStrategy() {
        return constellationStealStrategy;
    }
    
    public void setConstellationStealStrategy(StealStrategy constellationStealStrategy) {
        this.constellationStealStrategy = constellationStealStrategy;
    }
    
    public StealStrategy getRemoteStealStrategy() {
        return remoteStealStrategy;
    }
    
    public void setRemoteStealStrategy(StealStrategy remoteStealStrategy) {
        this.remoteStealStrategy = remoteStealStrategy;
    }
    
    public StealPool getBelongsToPool() {
        return belongsTo;
    }
    
    public void setBelongsToPool(StealPool myPool) {
        this.belongsTo = myPool;
    }
    
    public StealPool getStealsFrom() {
        return stealsFrom;
    }
    
    public void setStealsFrom(StealPool stealsFrom) {
        this.stealsFrom = stealsFrom;
    }
}

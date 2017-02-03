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

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class ConstellationConfiguration {

    private AbstractContext context = null;

    private StealPool belongsTo;
    private StealPool stealsFrom;
    
    private StealStrategy localStealStrategy;
    private StealStrategy constellationStealStrategy;
    private StealStrategy remoteStealStrategy;
    
    public ConstellationConfiguration(AbstractContext context, StealPool belongsTo, StealPool stealsFrom, 
            StealStrategy localStealStrategy, StealStrategy constellationStealStrategy, StealStrategy remoteStealStrategy) {  
        super();
        
        if (context == null) { 
            throw new IllegalArgumentException("Context may not be null");
        } else { 
            this.context = context;
        }
        
        if (localStealStrategy == null) { 
            throw new IllegalArgumentException("Local steal strategy may not be null");
        } else { 
            this.localStealStrategy = localStealStrategy;
        }
        
        if (constellationStealStrategy == null) { 
            throw new IllegalArgumentException("Constellation steal strategy may not be null");
        } else { 
            this.constellationStealStrategy = constellationStealStrategy;
        }
           
        if (remoteStealStrategy == null) { 
            throw new IllegalArgumentException("Remote steal strategy may not be null");
        } else { 
            this.remoteStealStrategy = remoteStealStrategy;
        }

        if (belongsTo == null) { 
            throw new IllegalArgumentException("Steal pool this Constellation belongs to may not be null");
        } else { 
            this.belongsTo = belongsTo;
        }
        
        if (stealsFrom == null) { 
            throw new IllegalArgumentException("Steal pool this Constellation steals from may not be null");
        } else { 
            this.stealsFrom = stealsFrom;
        }
    }
    
    public ConstellationConfiguration(AbstractContext context, StealStrategy localStealStrategy, StealStrategy constellationStealStrategy,
            StealStrategy remoteStealStrategy) {  
        this(context, StealPool.WORLD, StealPool.WORLD, localStealStrategy, constellationStealStrategy, remoteStealStrategy);
    }
 
    public ConstellationConfiguration(AbstractContext context, StealStrategy stealStrategy) { 
        this(context, StealPool.WORLD, StealPool.WORLD, stealStrategy, stealStrategy, stealStrategy);
    }
 
    public ConstellationConfiguration(AbstractContext context, StealStrategy localStealStrategy, StealStrategy remoteStealStrategy) { 
        this(context, StealPool.WORLD, StealPool.WORLD, localStealStrategy, remoteStealStrategy, remoteStealStrategy);
    }
    
    public ConstellationConfiguration(AbstractContext context) { 
        this(context, StealStrategy.SMALLEST);
    }
    
    public AbstractContext getContext() {
        return context;
    }
    
    public void setContext(AbstractContext context) {
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

package ibis.cohort.context;

import java.util.Arrays;
import java.util.Comparator;

import ibis.cohort.Context;

public class UnitContext extends Context {

    private static final long serialVersionUID = 6134114690113562356L;

    public static final Context DEFAULT = new UnitContext("DEFAULT");
       
    public final String name; 
    
    protected final int hashCode;
    
    public UnitContext(String name) {
        
        super();
        
        if (name == null) { 
            throw new IllegalArgumentException("name cannot be null!");
        }
        
        this.name = name;
        this.hashCode = name.hashCode();
    }
    
    @Override
    public boolean isUnit() { 
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
      
        if (getClass() != obj.getClass()) {
            return false;
        }
      
        UnitContext other = (UnitContext) obj;
        
        if (hashCode != other.hashCode) { 
            return false;
        }
        
        return name.equals(((UnitContext) obj).name);
    }
    
    public String toString() { 
        return "UnitContext(" + name + ")";
    }

    @Override
    public boolean satisfiedBy(Context other) {
     
        if (other == null) { 
            return false;
        }
        
        if (other.isUnit()) { 
            return equals(other);
        }
        
        if (other.isOr()) { 
            return ((OrContext)other).contains(this);
        }
        
        return false;
    }
    
    protected static class UnitContextSorter implements Comparator<UnitContext> {

        public int compare(UnitContext u1, UnitContext u2) {
            
            if (u1.hashCode == u2.hashCode) { 
                return 0;
            } else if (u1.hashCode < u2.hashCode) { 
                return -1;
            } else {    
                return 1;
            }
        }
    }
    
    public static UnitContext [] sort(UnitContext [] in) { 
        Arrays.sort(in, new UnitContextSorter());
        return in;
    }
    
    public static int generateHash(UnitContext [] in) { 
      
        // NOTE: result depends on order of elements in array! 
        int hashCode = 1;
       
        for (int i=0;i<in.length;i++) {
            hashCode = 31*hashCode + (in[i] == null ? 0 : in[i].hashCode);
        }
       
        return hashCode;
    }
}

package good.notes.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LWW Element Set backed by an hashMap with a timestamp for each element
 *
 * @param <T> the element type
 */
public class LWWSet<T> {

    private Map<T,Long> addSet;
    private Map<T,Long> removeSet;

    public LWWSet(T type) {
        this.addSet = new ConcurrentHashMap<T, Long>();
        this.removeSet = new ConcurrentHashMap<T,Long>();
    }
    /**
     * Adds elements
     *
     * @param element to be added
     * @param timeStamp positive timestamp at which the element got added
     */
    public void add(T element, long timeStamp){
        assert timeStamp >0;
        addToSet(addSet,element,timeStamp);
    }

    /**
     * Removes elements
     *
     * @param element to be added
     * @param timeStamp positive timeStamp at which the element got removed
     */
    public void remove(T element, long timeStamp) {
        assert timeStamp>0;
        addToSet(removeSet, element, timeStamp);
    }

    /**
     * Returns <tt>true</tt> if this LWW Set contains the specified value.
     * An element value is in the set if it is in addSet, and it is not in removeSet with a higher timestamp
     *
     * @param value value whose presence in this Set is to be tested
     * @return <tt>true</tt> if this LWW Set contains the specified value.
     */
    public boolean contains(T value) {
        long addTimestamp = addSet.containsKey(value)? addSet.get(value) : -1;
        long removeTimestamp = removeSet.containsKey(value)? removeSet.get(value):-1;

        boolean notPresent = addTimestamp == -1 && removeTimestamp == -1;
        if (notPresent) {
            return false;
        }

        boolean onlyAdded = addTimestamp > 0L && removeTimestamp == -1L;
        boolean addedAfterRemoval = addTimestamp >= removeTimestamp;
        return onlyAdded || addedAfterRemoval;

    }

    /**
     * Adds given element to provided set only if the timestamp is higher
     *
     * @param set either addSet/removeSet
     * @param element to be added
     * @param timeStamp positive timestamp at which the element got added
     */
    private void addToSet(Map<T, Long> set, T element, long timeStamp) {
        if (set.containsKey(element)) {
            synchronized (set) {
                long storedTimestamp = set.get(element);
                if (storedTimestamp < timeStamp) {
                    set.put(element, timeStamp);
                }
            }
        } else {
            set.putIfAbsent(element, timeStamp);
        }
    }

    /**
     * List values of this LWW Set
     * Merging two replicas takes the union of their add-sets and remove-sets.
     * An element e is in the set if it is in addSet, and it is not in removeSet with a higher timestamp
     *
     * @return List of values
     */
    public List<T> values() {
        ArrayList values = new ArrayList();
        for (Map.Entry<T, Long> addedEntry : addSet.entrySet()) {
            T entry = addedEntry.getKey();
            long addTimestamp = addedEntry.getValue();
            long removeTimestamp = removeSet.containsKey(entry)? removeSet.get(entry):-1;
            if (addTimestamp >= removeTimestamp) {
                values.add(entry);
            }
        }
        return values;
    }

    /*
     * For unit Testing
     */
    Map<T,Long> getAddSet(){
        return Collections.unmodifiableMap(this.addSet);
    }
}

package good.notes.test

import spock.lang.Specification

class LWWSetTest extends Specification {
    void "Add New Value"() {
        given: "a LWW with String"
        LWWSet sampleSet = new LWWSet(String.class)

        when: "we add a new value"
        def value = "Test String"
        sampleSet.add(value,System.currentTimeMillis())

        then: "the added value is present"
        sampleSet.contains(value)
    }

    void "Remove Value with higher time stamp"(){
        given: "a LWW of type String has an existing value"
        LWWSet sampleSet = new LWWSet(String.class)
        def value = "Test String"
        sampleSet.add(value,1L)

        when:"we remove the existing value with higher time stamp"
        sampleSet.remove(value,2L)

        then:"the value is no longer present"
        !sampleSet.contains(value)

    }

    void "Remove Value with lower time stamp"(){
        given: "a LWW with String has an existing value"
        LWWSet sampleSet = new LWWSet(String.class)
        def value = "Test String"
        sampleSet.add(value,2L)

        when:"we remove a value with lower time stamp"

        sampleSet.remove(value,1L)

        then:"the value is still present"
        sampleSet.contains(value)

    }

    void "Add after removal with higher timestamp"(){
        given: "a LWW with String had value that was removed"
        LWWSet sampleSet = new LWWSet(String.class)
        def value = "Test String"
        sampleSet.add(value,1L)
        sampleSet.remove(value, 2L)

        when:"we add again the same value"
        sampleSet.add(value,3L)

        then:"the value gets added again"
        sampleSet.contains(value)
        verifyAll {sampleSet.addSet.get(value) == 3L}
    }

    void "Add after removal with lower timestamp"(){
        given: "a LWW with String had value that was removed"
        LWWSet sampleSet = new LWWSet(String.class)
        def value = "Test String"
        sampleSet.add(value,1L)
        sampleSet.remove(value, 3L)

        when:"we add again the same value"
        sampleSet.add(value,2L)

        then:"the value does not get added again"
        !sampleSet.contains(value)
    }

    void "Add duplicate Value with same time stamp"() {
        given: "a LWW with String has an existing value"
        LWWSet sampleSet = Spy(LWWSet,constructorArgs: [String.class])
        def value = "Test String"
        sampleSet.add(value,2L)
        def spySet = sampleSet.addSet

        when: "we try adding the same value again"
        sampleSet.add(value,2L)

        then: "No put to addSet"
        interaction {
            0 * spySet.put(_,2L)
        }
    }

    void "List all Values"() {
        given: "a LWW with String"
        LWWSet sampleSet = new LWWSet(String.class)

        when: "we add a new value"
        def value = "Test String"
        sampleSet.add(value,System.currentTimeMillis())

        then: "we list values"
        sampleSet.values()
    }

    void " Does not contains the value"() {
        given: "a LWW with String"
        LWWSet sampleSet = new LWWSet(String.class)

        when: "we add a new value"
        def value = "Test String"
        sampleSet.add(value,System.currentTimeMillis())

        then: "we list values"
        !sampleSet.contains("some other")
    }


}

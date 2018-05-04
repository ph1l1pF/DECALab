package analysis.exercise2;

import analysis.FileState;
import analysis.FileStateFact;
import analysis.ForwardAnalysis;
import analysis.VulnerabilityReporter;
import soot.Body;
import soot.Unit;
import soot.Value;
import soot.ValueBox;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypeStateAnalysis extends ForwardAnalysis<Set<FileStateFact>> {

    private boolean vulnerabilityFound = false;

    private Map<FileStateFact, Set<Value>> factsToAliases = new HashMap<>();
    private Map<FileStateFact, FileState> factsToStates = new HashMap<>();

    public TypeStateAnalysis(Body body, VulnerabilityReporter reporter) {
        super(body, reporter);
    }

    @Override
    protected void flowThrough(Set<FileStateFact> in, Unit unit, Set<FileStateFact> out) {
        copy(in, out);

        System.out.println("flowthrough begin");

        // TODO: Implement your flow function here.
        String targetRegister = null;
        String targetType = null;
        boolean newStatement = false;
        Value boxValueStore = null;
        for (ValueBox box : unit.getUseAndDefBoxes()) {
            System.out.println("box value: " + box.getValue());
            System.out.println("box value type: " + box.getValue().getType());
            String boxValueType = box.getValue().getType().toString();

            if (targetRegister != null) {
                String boxValue = box.getValue().toString();
                if (boxValue.equals("new " + targetType)) {
                    //add new FileStateFact to set
                    System.out.println("found new call. adding: " + targetRegister);
                    FileStateFact newFact = new FileStateFact(new HashSet<Value>(), FileState.New);
                    factsToStates.put(newFact, FileState.New);
                    factsToAliases.put(newFact, new HashSet<Value>());
                    System.out.println("Value to add to new fact: " + boxValueStore.toString());
                    newFact.addAlias(boxValueStore);
                    newStatement = true;
                } else {
                    String methodCallInit = "specialinvoke " + targetRegister + ".<" + targetType + ": void <init>()>()";
                    String methodCallOpen = "virtualinvoke " + targetRegister + ".<" + targetType + ": void open()>()";
                    String methodCallClose = "virtualinvoke " + targetRegister + ".<" + targetType + ": void close()>()";

                    if (boxValue.equals(methodCallInit)) {
                        System.out.println("found init call");


                    } else {
                        if (unit.toString().contains(" = ") && !newStatement) {
                            System.out.println("found assignment statement");
                            //set alias
                        }
                    }
                    if (boxValue.equals(methodCallOpen)) {
                        System.out.println("found open call");
                    }
                    if (boxValue.equals(methodCallClose)) {
                        System.out.println("found close call");
                    }


                }
            }
            if (boxValueType.endsWith(".File")) {
                targetRegister = box.getValue().toString();
                targetType = boxValueType;
                boxValueStore = box.getValue();
            }
        }
        newStatement = false;
        targetRegister = null;
        boxValueStore = null;
        targetType = null;
        this.prettyPrint(in, unit, out);
        // for(ValueBox defBox : unit.getDefBoxes()){
        //     String defBoxValueType = defBox.getValue().getType().toString();
        //     System.out.println("defbox value: " + defBox.getValue());
        //     System.out.println("defbox value type: " + defBoxValueType);
        //     // if(defBoxValueType.endsWith(".File")){
        //     //     System.out.println("found file operation!");
        //     //     System.out.println("def box value: " + defBox.getValue());
        //     //     if(in.isEmpty()){

        //     //         String useBoxValueString = unit.getUseBoxes().get(0).getValue().toString();
        //     //         String expectedStringInit = "specialinvoke " +  defBox.getValue() + ".<" +defBoxValueType + ": void <init>()>()";
        //     //         String expectedStringNew = "new " + defBoxValueType;
        //     //         System.out.println("new method: " + expectedStringNew);
        //     //         System.out.println("use box value string: " + useBoxValueString + " expectedString: " + expectedStringInit);
        //     //         if(!useBoxValueString.equals(expectedStringInit) && !useBoxValueString.equals(expectedStringNew)){
        //     //             System.out.println("that was unexepcted");
        //     //         }
        //     //     }
        //     // }
        // }
        // for(ValueBox useBox : unit.getUseBoxes()){
        //     System.out.println("use box value: " + useBox.getValue());
        //     System.out.println("use box value type: " + useBox.getValue().getType());
        // }
        // for(ValueBox box : unit.getUseAndDefBoxes()){

        //     System.out.println("unit class: " + box.getValue());
        //     System.out.println("value type: " + box.getValue().getType());

        // }

        //if unit == file operation
        // if unit is in in in set (check aliases)
        //  switch operation
        //    validate if operation is legit depending on state in set

        System.out.println("flowthrough end");

        if (vulnerabilityFound) {
            reporter.reportVulnerability(this.method.getSignature(), unit);
        }

    }

    @Override
    protected Set<FileStateFact> newInitialFlow() {

        Set<FileStateFact> set = new HashSet<>();
        //set.add(new FileStateFact(new HashSet<Value>(), FileState.Init));
        return set;
    }

    @Override
    protected void copy(Set<FileStateFact> source, Set<FileStateFact> dest) {
        for (FileStateFact fact : source) {
            FileStateFact copyState = fact.copy();
            factsToAliases.put(copyState, factsToAliases.get(fact));
            factsToStates.put(copyState, factsToStates.get(fact));
            dest.add(copyState);
        }
    }

    @Override
    protected void merge(Set<FileStateFact> in1, Set<FileStateFact> in2, Set<FileStateFact> out) {
        // TODO: Implement the merge function here.
        System.out.println("merge begin");
        vulnerabilityFound = false;

        for (FileStateFact stateFact1 : in1) {
            for (FileStateFact stateFact2 : in2) {
                if (commonAlias(factsToAliases.get(stateFact1), factsToAliases.get(stateFact2))) {
                    // merge
                    Set<Value> union = new HashSet<>();
                    factsToAliases.get(stateFact1).forEach(s -> union.add(s));
                    factsToAliases.get(stateFact2).forEach(s -> union.add(s));

                    FileState newState = null;

                    if (stateFact1.getState().equals(stateFact2.getState())) {
                        newState = stateFact1.getState();
                    } else if ((stateFact1.getState().equals(FileState.Close) && stateFact2.getState().equals(FileState.Init)) ||
                            (stateFact2.getState().equals(FileState.Close) && stateFact1.getState().equals(FileState.Init))) {
                        newState = FileState.Close;
                    } else if ((stateFact1.getState().equals(FileState.Init) && stateFact2.getState().equals(FileState.Open)) ||
                            (stateFact2.getState().equals(FileState.Init) && stateFact1.getState().equals(FileState.Open))) {
                        newState = FileState.Open;
                    } else {
                        vulnerabilityFound = true;
                    }

                    FileStateFact mergedStateFact = new FileStateFact(union, newState);
                    out.add(mergedStateFact);
                }


            }
        }


    }

    private boolean commonAlias(Set<Value> s1, Set<Value> s2) {
        for (Value v1 : s1) {
            if (s2.contains(v1)) {
                return true;
            }

        }
        return false;
    }

}

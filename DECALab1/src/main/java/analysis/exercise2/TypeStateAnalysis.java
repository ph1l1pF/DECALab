package analysis.exercise2;

import analysis.FileState;
import analysis.FileStateFact;
import analysis.ForwardAnalysis;
import analysis.VulnerabilityReporter;
import soot.Body;
import soot.Unit;
import soot.Value;
import soot.ValueBox;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

public class TypeStateAnalysis extends ForwardAnalysis<Set<FileStateFact>> {

    public TypeStateAnalysis(Body body, VulnerabilityReporter reporter) {
        super(body, reporter);
    }
    HashMap<String, FileState> registerToStateMap = new HashMap<String, FileState>();
    @Override
    protected void flowThrough(Set<FileStateFact> in, Unit unit, Set<FileStateFact> out) {

        // TODO: Implement your flow function here.
        String targetRegister = null;
        String targetType = null;
        boolean newStatement = false;
        Value boxValueStore = null;

        for(ValueBox box : unit.getUseAndDefBoxes()){
            System.out.println("box value: " + box.getValue());
            System.out.println("box value type: " + box.getValue().getType());
            String boxValueType = box.getValue().getType().toString();

            if(targetRegister != null){
                String boxValue = box.getValue().toString();
                if(boxValue.equals("new " + targetType)){
                    //add new FileStateFact to set
                    System.out.println("found new call. adding: " + targetRegister);
                    FileStateFact newFact = new FileStateFact(new HashSet<Value>(), FileState.Init);
                    System.out.println("Value to add to new fact: " + boxValueStore.toString());
                    newFact.addAlias(boxValueStore);
                    in.add(newFact);
                    registerToStateMap.put(targetRegister, FileState.Init);
                    newStatement = true;
                }
                else{
                    String methodCallInit = "specialinvoke " + targetRegister + ".<" + targetType + ": void <init>()>()";
                    String methodCallOpen = "virtualinvoke " + targetRegister + ".<" + targetType + ": void open()>()";
                    String methodCallClose = "virtualinvoke " + targetRegister + ".<" + targetType + ": void close()>()";
                    
                    if(boxValue.equals(methodCallInit)){
                        System.out.println("found init call");
                        if(registerToStateMap.containsKey(targetRegister)){
                            System.out.println("found map entry for " + targetRegister);

                        }
                      
                    }
                    else{
                        if(unit.toString().contains(" = ") && !newStatement){
                            System.out.println("found assignment statement");
                            //set alias
                        }
                    }
                    if(boxValue.equals(methodCallOpen)){
                        System.out.println("found open call");
                        if(registerToStateMap.containsKey(targetRegister)){
                            System.out.println("found map entry for open: " + targetRegister);
                            if(registerToStateMap.get(targetRegister) == FileState.Init){
                                System.out.println("open seems to be permittet");
                            }
                            else{
                                this.reporter.reportVulnerability(this.method.getSignature(), unit);
                            }
                            
                        }
                    }
                    if(boxValue.equals(methodCallClose)){
                        if(registerToStateMap.containsKey(targetRegister)){
                            System.out.println("found map entry for close: " + targetRegister);

                        }
                        else{
                            System.out.println("close without init");
                            this.reporter.reportVulnerability(this.method.getSignature(), unit);
                        }
                        System.out.println("found close call");
                    }

                    
                }
            }
            if(boxValueType.endsWith(".File")){
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

        if(unit.toString().equals("return")){
            System.out.println("found return");
        }

        copy(in, out);
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
            dest.add(fact.copy());
        }
    }

    @Override
    protected void merge(Set<FileStateFact> in1, Set<FileStateFact> in2, Set<FileStateFact> out) {
        // TODO: Implement the merge function here.
    
        for (FileStateFact stateFact1 : in1) {
            for (FileStateFact stateFact2 : in2) {
                if (commonAlias(stateFact1.getAliases(), stateFact2.getAliases())) {
                    // merge
                    Set<Value> union = new HashSet<>();
                    stateFact1.getAliases().forEach(s -> union.add(s));
                    stateFact2.getAliases().forEach(s -> union.add(s));

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
						//reporter.reportVulnerability(this.method, ????);
                    }

                    FileStateFact mergedStateFact = new FileStateFact(union, newState);
                    out.add(mergedStateFact);
                }


            }
        }
   // }

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

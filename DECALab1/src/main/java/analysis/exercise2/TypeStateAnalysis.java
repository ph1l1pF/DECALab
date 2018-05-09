package analysis.exercise2;

import analysis.FileState;
import analysis.FileStateFact;
import analysis.ForwardAnalysis;
import analysis.VulnerabilityReporter;
import soot.Body;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.Type;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

public class TypeStateAnalysis extends ForwardAnalysis<Set<FileStateFact>> {

    private boolean vulnerabilityFound = false;

    private HashMap<String, Set<Value>> factsToAliases = new HashMap<String, Set<Value>>();
    private HashMap<String, FileState> registerToStateMap = new HashMap<String, FileState>();

    public TypeStateAnalysis(Body body, VulnerabilityReporter reporter) {
        super(body, reporter);
    }
    private HashSet<String> declaredVariables = new HashSet<String>();
    private HashMap<String, Value> declaredVariablesValue = new HashMap<String, Value>();
    @Override
    protected void flowThrough(Set<FileStateFact> in, Unit unit, Set<FileStateFact> out) {

        prettyPrint(in, unit, out);

        for(ValueBox box : unit.getDefBoxes()){
            Value defBoxValue = box.getValue();
            Type defBoxType = defBoxValue.getType();
            System.out.println("def box value: " + defBoxValue);
            System.out.println("def box value type: " + defBoxType);

            if(defBoxType.toString().endsWith(".File")){
                System.out.println("Found declaration of new file object");
                declaredVariables.add(defBoxValue.toString());
                declaredVariablesValue.put(defBoxValue.toString(), defBoxValue);
            }
         }

         Value fileVariable = null;
         Type fileType = null; 
         for(ValueBox box : unit.getUseBoxes()){
            Value useBoxValue = box.getValue();
            Type useBoxType = useBoxValue.getType();
            System.out.println("use box value: " + useBoxValue);
            System.out.println("use box value type: " + useBoxType);
            if(useBoxType.toString().endsWith(".File")){
                fileVariable = useBoxValue;
                fileType = useBoxType;
            }

            if(unit.toString().contains(" = ")){
                System.out.println("else part, unit statement could be an assignment " + unit.toString());
                String [] variables = unit.toString().split("=");
                //possibly already in file set
                String source = variables[1].trim();
                //has to be added as alias
                String target = variables[0].trim();
                System.out.println("found assignment, source: " + source + " and target: " + target);
                FileStateFact f = getFileStateFact(in, source);
                if(f != null){
                    System.out.println("Found file fact to add alias for: " + f.toString());
                    f.addAlias(declaredVariablesValue.get(target));
                }
            }
            else{
                if(fileVariable != null){
                    String methodCallInit = "specialinvoke " + fileVariable.toString() + ".<" + fileType.toString() + ": void <init>()>()";
                    String methodCallOpen = "virtualinvoke " + fileVariable.toString() + ".<" + fileType.toString() + ": void open()>()";
                    String methodCallClose = "virtualinvoke " + fileVariable.toString() + ".<" + fileType.toString() + ": void close()>()";
                    if(declaredVariables.contains(fileVariable.toString())){
                        if(useBoxValue.toString().equals(methodCallInit)){
                            HashSet<Value> alias = new HashSet<Value>();
                            alias.add(fileVariable);
                            FileStateFact f = new FileStateFact(alias, FileState.Init);
                            in.add(f);
                        }
                        else{
                            FileStateFact f = getFileStateFact(in, fileVariable);
                            if(f != null){
                                if(useBoxValue.toString().equals(methodCallOpen)){
            
                                    if(f.getState() == FileState.Init || f.getState() == FileState.Close){
                                        System.out.println("updated file state from init to open");
                                        f.updateState(FileState.Open);
                                        }
                                    else{
                                        //report error 
                                        System.out.println("Vulnarbility 1");
                                        reporter.reportVulnerability(this.method.getSignature(), unit);
                                    }
                                }
                                else{
                                    if(useBoxValue.toString().equals(methodCallClose)){
                                        if(f.getState() == FileState.Init || f.getState() == FileState.Open ){
                                            f.updateState(FileState.Close);
                                        }
                                        else{
                                            //report error 
                                            System.out.println("Vulnarbility 2");
                                            reporter.reportVulnerability(this.method.getSignature(), unit);
                                        }
                                    }
                                }
                            }
                            else{
                            //report error
                            //reporter.reportVulnerability(this.method.getSignature(), unit);
                            }
                        }
                    }
                    else{
                        //variable wasn't declared before
                    }
                }
            }
        }

        if(unit.toString().equals("return")){
            System.out.println("found return");
            for(FileStateFact f : in){
                if(f.isOpened()){
                    reporter.reportVulnerability(this.method.getSignature(), unit);
                }

            }
        }

        // if (vulnerabilityFound) {
        //     reporter.reportVulnerability(this.method.getSignature(), unit);
        // }
        copy(in, out);
        prettyPrint(in, unit, out);
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
            dest.add(copyState);
        }
    }

    @Override
    protected void merge(Set<FileStateFact> in1, Set<FileStateFact> in2, Set<FileStateFact> out) {
        // TODO: Implement the merge function here.

        vulnerabilityFound = false;

        Set<FileStateFact> fileStateFactsWithPartners1 = new HashSet<>();
        Set<FileStateFact> fileStateFactsWithPartners2 = new HashSet<>();

        // for (FileStateFact stateFact1 : in1) {
        //     for (FileStateFact stateFact2 : in2) {
        //         if (commonAlias(factsToAliases.get(stateFact1), factsToAliases.get(stateFact2))) {

        //             fileStateFactsWithPartners1.add(stateFact1);
        //             fileStateFactsWithPartners2.add(stateFact2);

        //             // merge
        //             Set<Value> union = new HashSet<>();
        //             factsToAliases.get(stateFact1).forEach(s -> union.add(s));
        //             factsToAliases.get(stateFact2).forEach(s -> union.add(s));

        //             FileState newState = null;

        //             if (stateFact1.getState().equals(stateFact2.getState())) {
        //                 newState = stateFact1.getState();
        //             } else if ((stateFact1.getState().equals(FileState.Close) && stateFact2.getState().equals(FileState.Init)) ||
        //                     (stateFact2.getState().equals(FileState.Close) && stateFact1.getState().equals(FileState.Init))) {
        //                 newState = FileState.Close;
        //             } else if ((stateFact1.getState().equals(FileState.Init) && stateFact2.getState().equals(FileState.Open)) ||
        //                     (stateFact2.getState().equals(FileState.Init) && stateFact1.getState().equals(FileState.Open))) {
        //                 newState = FileState.Open;
        //             } else {
        //                 vulnerabilityFound = true;
        //             }

        //             FileStateFact mergedStateFact = new FileStateFact(union, newState);
        //             out.add(mergedStateFact);
        //         }

           // }
        // }

        // // add those fact which were not merged
        // for (FileStateFact st : in1) {
        //     if (!fileStateFactsWithPartners1.contains(st)) {
        //         out.add(st);
        //     }
        // }
        // for (FileStateFact st : in2) {
        //     if (!fileStateFactsWithPartners2.contains(st)) {
        //         out.add(st);
        //     }
        // }

    }

    private FileStateFact getFileStateFact(Set<FileStateFact> in, Value variable){
        for(FileStateFact f : in){
            if(f.containsAlias(variable)){
                return f;
            }
        }
        return null;
    }
    private FileStateFact getFileStateFact(Set<FileStateFact> in, String variable){
        for(FileStateFact f : in){
            if(f.containsAlias(variable)){
                return f;
            }
        }
        return null;
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

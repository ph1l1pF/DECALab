package analysis.exercise2;

import analysis.FileState;
import analysis.FileStateFact;
import analysis.ForwardAnalysis;
import analysis.VulnerabilityReporter;
import soot.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeStateAnalysis extends ForwardAnalysis<Set<FileStateFact>> {

    private boolean vulnerabilityFound = false;

    public TypeStateAnalysis(Body body, VulnerabilityReporter reporter) {
        super(body, reporter);
    }

    private HashSet<String> declaredVariables = new HashSet<String>();

    @Override
    protected void flowThrough(Set<FileStateFact> in, Unit unit, Set<FileStateFact> out) {
        prettyPrint(in, unit, out);
        copy(in, out);

        List<Unit> unitsWithVuln = new ArrayList<>();

        //Find all Occurences of File
        for (ValueBox box : unit.getDefBoxes()) {
            Value defBoxValue = box.getValue();
            Type defBoxType = defBoxValue.getType();

            if (defBoxType.toString().endsWith(".File")) {
                declaredVariables.add(defBoxValue.toString());
            }
        }

        Value fileVariable = null;
        Type fileType = null;
        for (ValueBox box : unit.getUseBoxes()) {
            Value useBoxValue = box.getValue();
            Type useBoxType = useBoxValue.getType();
            if (useBoxType.toString().endsWith(".File")) {
                fileVariable = useBoxValue;
                fileType = useBoxType;
            }

            //If Assignment
            if (unit instanceof JAssignStmt) {
                JAssignStmt assignStmt = (JAssignStmt) unit;
                Value target = assignStmt.leftBox.getValue();
                Value source = assignStmt.rightBox.getValue();
                System.out.println("found assignment, source: " + source + " and target: " + target);
                FileStateFact fact = getFileStateFact(out, source);
                if (fact != null) {
                    FileStateFact f = getFileStateFact(out, source);
                    System.out.println("Found file fact to add alias for: " + f.toString());//Todo berücksichtige fälle für nicht File
                    f.addAlias(target);
                }

            } else {
                //only if unit contains something about file
                if (fileVariable != null && unit instanceof JInvokeStmt) {
//                    JSpecialInvokeExpr specialInvokeExpr = (JSpecialInvokeExpr) invokeStmt.getInvokeExprBox().getValue();
//                    Value variable= specialInvokeExpr.getBaseBox().getValue();

                    String methodCallInit = "specialinvoke " + fileVariable.toString() + ".<" + fileType.toString() + ": void <init>()>()";
                    String methodCallOpen = "virtualinvoke " + fileVariable.toString() + ".<" + fileType.toString() + ": void open()>()";
                    String methodCallClose = "virtualinvoke " + fileVariable.toString() + ".<" + fileType.toString() + ": void close()>()";

                    if (useBoxValue.toString().equals(methodCallInit)) {
                        //INIT
                        FileStateFact currentFact = getFileStateFact(out, fileVariable);
                        if (currentFact != null) {
                            //Todo Passt so noch nicht
                            throw new IllegalArgumentException();
//                               currentFact.updateState(FileState.Init);
                        } else {
                            HashSet<Value> alias = new HashSet<Value>();
                            alias.add(fileVariable);
                            FileStateFact f = new FileStateFact(alias, FileState.Init);
                            out.add(f);
                        }
                    } else if (useBoxValue.toString().equals(methodCallOpen)) {
                        //OPEN
                        FileStateFact currentFact = getFileStateFact(out, fileVariable);
                        if (currentFact == null) {
                            unitsWithVuln.add(unit);
                        }
                        if (!(currentFact.getState() == FileState.Init || currentFact.getState() == FileState.Open)) {
                            unitsWithVuln.add(unit);
                        }
                        currentFact.updateState(FileState.Open);

                    } else if (useBoxValue.toString().equals(methodCallClose)) {
                        //Close
                        FileStateFact currentFact = getFileStateFact(out, fileVariable);
                        if (currentFact == null) {

                            unitsWithVuln.add(unit);
                        }

                        if (!(currentFact.getState() == FileState.Init || currentFact.getState() == FileState.Open)) {
                            unitsWithVuln.add(unit);
                        }
                        currentFact.updateState(FileState.Close);
                    }
                }
            }
        }

        if (unit.toString().equals("return")) {

            for (FileStateFact fileStateFact : out) {

                if (fileStateFact != null) {
                    getAliases(fileStateFact);
                }
                HashSet<Value> alias = new HashSet<Value>();
                //Value testValue= (Value) fileVariable.clone();
                alias.add(fileVariable);
                //alias.add(testValue);
                FileStateFact testFact = new FileStateFact(alias, FileState.Init);
            }
            for (FileStateFact f : out) {
                if (f.isOpened()) {
//                    reporter.reportVulnerability(this.method.getSignature(), unit);
                    unitsWithVuln.add(unit);
                }
            }
        }

        System.out.println(unitsWithVuln.size() + " , " + method.getSignature());

        for (Unit unitWithVuln : unitsWithVuln) {
            reporter.reportVulnerability(this.method.getSignature(), unitWithVuln);
        }

//         if (vulnerabilityFound) {
//             reporter.reportVulnerability(this.method.getSignature(), unit);
//         }
//        prettyPrint(in, unit, out);
    }

//    private FileStateFact hasFileStateFact(Value fileVariable, Set<FileStateFact> out) {
//        for (FileStateFact fileStateFact : out) {
//            if (fileStateFact.containsAlias(fileVariable)) {
//                return fileStateFact;
//            }
//        }
//        return null;
//    }

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

//        vulnerabilityFound = false;
//
//        Set<FileStateFact> fileStateFactsWithPartners1 = new HashSet<>();
//        Set<FileStateFact> fileStateFactsWithPartners2 = new HashSet<>();
//
//        for (FileStateFact stateFact1 : in1) {
//            for (FileStateFact stateFact2 : in2) {
//                if (commonAlias(factsToAliases.get(stateFact1), factsToAliases.get(stateFact2))) {
//
//                    fileStateFactsWithPartners1.add(stateFact1);
//                    fileStateFactsWithPartners2.add(stateFact2);
//
//                    // merge
//                    Set<Value> union = new HashSet<>();
//                    factsToAliases.get(stateFact1).forEach(s -> union.add(s));
//                    factsToAliases.get(stateFact2).forEach(s -> union.add(s));
//
//                    FileState newState = null;
//
//                    if (stateFact1.getState().equals(stateFact2.getState())) {
//                        newState = stateFact1.getState();
//                    } else if ((stateFact1.getState().equals(FileState.Close) && stateFact2.getState().equals(FileState.Init)) ||
//                            (stateFact2.getState().equals(FileState.Close) && stateFact1.getState().equals(FileState.Init))) {
//                        newState = FileState.Close;
//                    } else if ((stateFact1.getState().equals(FileState.Init) && stateFact2.getState().equals(FileState.Open)) ||
//                            (stateFact2.getState().equals(FileState.Init) && stateFact1.getState().equals(FileState.Open))) {
//                        newState = FileState.Open;
//                    } else {
//                        vulnerabilityFound = true;
//                    }
//
//                    FileStateFact mergedStateFact = new FileStateFact(union, newState);
//                    out.add(mergedStateFact);
//                }
//
//            }
//        }
//
//        // add those fact which were not merged
//        for (FileStateFact st : in1) {
//            if (!fileStateFactsWithPartners1.contains(st)) {
//                out.add(st);
//            }
//        }
//        for (FileStateFact st : in2) {
//            if (!fileStateFactsWithPartners2.contains(st)) {
//                out.add(st);
//            }
//        }

    }

    private FileStateFact getFileStateFact(Set<FileStateFact> in, Value variable) {
        for (FileStateFact f : in) {
            if (f.containsAlias(variable)) {
                return f;
            }
        }
//        throw new IllegalArgumentException();
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


    private Set<String> getAliases(FileStateFact fileStateFact) {

        String factString = fileStateFact.toString();
        System.out.println(factString);
        int start = factString.indexOf("[");
        int end = factString.indexOf("]");
        String tmp = factString.substring(start + 1, end);
        System.out.println(tmp);
        String[] aliases = tmp.split(",");
        System.out.println(aliases);
        HashSet<String> set = new HashSet<>();
        for (String alias : aliases) {
            set.add(alias);
        }
        return set;
    }

}

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
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;

import java.util.HashSet;
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

        //Find all Occurences of File
        for (ValueBox box : unit.getDefBoxes()) {
            Value defBoxValue = box.getValue();
            Type defBoxType = defBoxValue.getType();
            System.out.println("def box value: " + defBoxValue);
            System.out.println("def box value type: " + defBoxType);

            if (defBoxType.toString().endsWith(".File")) {
                System.out.println("Found declaration of new file object");
                declaredVariables.add(defBoxValue.toString());
            }
        }

        Value fileVariable = null;
        Type fileType = null;
        for (ValueBox box : unit.getUseBoxes()) {
            Value useBoxValue = box.getValue();
            Type useBoxType = useBoxValue.getType();
            System.out.println("use box value: " + useBoxValue);
            System.out.println("use box value type: " + useBoxType);
            if (useBoxType.toString().endsWith(".File")) {
                fileVariable = useBoxValue;
                fileType = useBoxType;
            }

            //If Assignment
            if (unit instanceof JAssignStmt) {
//                String[] variables = unit.toString().split("=");
//                //possibly already in file set
//                String source = variables[1].trim();
//                //has to be added as alias
//                String target = variables[0].trim();

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
                    JInvokeStmt invokeStmt = (JInvokeStmt) unit;
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
                            reporter.reportVulnerability(this.method.getSignature(), unit);
                        } else {
                            if (currentFact.getState() == FileState.Init || currentFact.getState() == FileState.Open) {
                                currentFact.updateState(FileState.Open);
                            } else {
                                reporter.reportVulnerability(this.method.getSignature(), unit);
                            }
                        }
                    } else if (useBoxValue.toString().equals(methodCallClose)) {
                        //Close
                        FileStateFact currentFact = getFileStateFact(out, fileVariable);
                        if (currentFact == null) {

                            reporter.reportVulnerability(this.method.getSignature(), unit);
                        } else {

                            if (currentFact.getState() == FileState.Init || currentFact.getState() == FileState.Open) {
                                currentFact.updateState(FileState.Close);
                            } else {
                                reporter.reportVulnerability(this.method.getSignature(), unit);
                            }
                        }
                    }
                }
            }
        }

        if (unit.toString().equals("return")) {
            System.out.println("found return");

            for (FileStateFact fileStateFact : out) {
                System.out.println("FileStateFact: Aliases: ");
                System.out.println(fileStateFact.toString());
                System.out.println(" State: ");
                System.out.println(fileStateFact.getState());

                if (fileStateFact != null) {

                    getAliases(fileStateFact);
                }
                System.out.println(" DEBug: ");
                System.out.println(" Debug: ");
                HashSet<Value> alias = new HashSet<Value>();
                //Value testValue= (Value) fileVariable.clone();
                alias.add(fileVariable);
                //alias.add(testValue);
                FileStateFact testFact = new FileStateFact(alias, FileState.Init);
                System.out.println("FileStateFact: Aliases: ");
                System.out.println(testFact.toString());
                System.out.println(" State: ");
                System.out.println(testFact.getState());
            }
            System.out.println(out.toString());
            for (FileStateFact f : out) {
                if (f.isOpened()) {
                    reporter.reportVulnerability(this.method.getSignature(), unit);
                }

            }
        }

        // if (vulnerabilityFound) {
        //     reporter.reportVulnerability(this.method.getSignature(), unit);
        // }
        prettyPrint(in, unit, out);
    }

    private FileStateFact hasFileStateFact(Value fileVariable, Set<FileStateFact> out) {
        for (FileStateFact fileStateFact : out) {
            if (fileStateFact.containsAlias(fileVariable)) {
                return fileStateFact;
            }
        }
        return null;
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

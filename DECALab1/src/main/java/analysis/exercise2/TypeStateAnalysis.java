package analysis.exercise2;

import analysis.FileState;
import analysis.FileStateFact;
import analysis.ForwardAnalysis;
import analysis.VulnerabilityReporter;
import soot.Body;
import soot.Unit;
import soot.Value;

import java.util.HashSet;
import java.util.Set;

public class TypeStateAnalysis extends ForwardAnalysis<Set<FileStateFact>> {

    public TypeStateAnalysis(Body body, VulnerabilityReporter reporter) {
        super(body, reporter);
    }

    @Override
    protected void flowThrough(Set<FileStateFact> in, Unit unit, Set<FileStateFact> out) {
        copy(in, out);
        // TODO: Implement your flow function here.
        prettyPrint(in, unit, out);


    }

    @Override
    protected Set<FileStateFact> newInitialFlow() {
        // TODO: Implement your initialization here.
        Set<FileStateFact> set = new HashSet<>();
        set.add(new FileStateFact(new HashSet<Value>(), FileState.Init));
        return set;
    }

    @Override
    protected void copy(Set<FileStateFact> source, Set<FileStateFact> dest) {
        // TODO: Implement the copy function here.
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
						reporter.reportVulnerability(this.method, ????);
                    }

                    FileStateFact mergedStateFact = new FileStateFact(union, newState);
                    out.add(mergedStateFact);
                }


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

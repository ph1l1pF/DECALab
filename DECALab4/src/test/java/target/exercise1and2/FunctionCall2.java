package target.exercise1and2;

public class FunctionCall2 {

    int increment(int i) {
        return i + 1;
    }

    public void entryPoint() {
        int i = 100;
        int j = 200;
        int k = increment(i);
        int l = increment(j);
    }

}

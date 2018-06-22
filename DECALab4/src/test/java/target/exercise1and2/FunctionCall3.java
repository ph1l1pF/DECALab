package target.exercise1and2;

public class FunctionCall3 {

    int add(int a, int b) {
        return a + 13;
    }

    public void entryPoint() {
        int i = 100;
        int j = 200;
        int k = add(i, j);
        int l = add(42, 13);
    }

}

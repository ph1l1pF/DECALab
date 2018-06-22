package target.exercise1and2;

public class FunctionCall {

    int id(int i) {
        return i;
    }

    public void entryPoint() {
        int i = 100;
        int j = 200;
        int k = id(300);
        int l = id(400);
    }

}

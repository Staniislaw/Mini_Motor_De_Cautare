package ro.usv;

public class Pereche <K,V>{
    K n1;
    V n2;
    public Pereche(K n1, V n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public K getN1() {
        return n1;
    }

    public V getN2() {
        return n2;
    }

    @Override
    public String toString() {
        return "(" +n1 +", " + n2 + ')';
    }
}
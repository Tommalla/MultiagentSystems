import jade.content.Predicate;


public class BlottoResult implements Predicate {
    private int result;


    public BlottoResult() {
        result = 0;
    }

    public BlottoResult(int paramInt) {
        result = paramInt;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int paramInt) {
        result = paramInt;
    }
}
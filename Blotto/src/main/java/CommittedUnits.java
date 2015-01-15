import jade.content.Predicate;


public class CommittedUnits implements Predicate {
    private int value;

    public CommittedUnits()
    {
        value = 0;
    }

    public CommittedUnits(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue (int result)
    {
        this.value = value;
    }
}

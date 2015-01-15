import jade.content.AgentAction;


public class GetBlottoResult implements AgentAction
{
    private Allocation allocation;

    public void setAllocation(Allocation paramAllocation)
    {
        allocation = paramAllocation;
    }

    public Allocation getAllocation()
    {
        return this.allocation;
    }
}
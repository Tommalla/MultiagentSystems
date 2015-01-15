import jade.content.AgentAction;


public class GetBlottoResult implements AgentAction
{
    private Allocation allocation;

    public GetBlottoResult(Allocation allocation) {
        this.allocation = allocation;
    }

    public Allocation getAllocation()
    {
        return this.allocation;
    }
}
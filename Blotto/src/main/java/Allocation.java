import jade.content.Concept;
import jade.util.leap.List;


public class Allocation implements Concept {
    private List assignment;

    public List getAssignment()
    {
        return assignment;
    }

    public void setAssignment(List paramList) {
        assignment = paramList;
    }
}
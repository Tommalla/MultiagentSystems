import jade.content.Concept;
import jade.util.leap.List;


public class Allocation implements Concept {
    protected List assignment;

    public Allocation(List assignment) {
        this.assignment = assignment;
    }

    public void setAssignment(List assignment) {
        this.assignment = assignment;
    }

    public List getAssignment()
    {
        return assignment;
    }
}
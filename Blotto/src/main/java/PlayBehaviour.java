import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlayBehaviour extends Behaviour {

    @Override
    public void action() {
    }

    @Override
    public boolean done() {
        return ((BlottoAgent)this.myAgent).units == 0;
    }
}

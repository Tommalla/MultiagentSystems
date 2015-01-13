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
        Logger.getGlobal().info("Action in playBehaviour");
        // TODO
        // Get agents from DF
        try {
            // Obtaining known writers from DF
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd  = new ServiceDescription();
            sd.setType("Blotto");
            dfd.addServices(sd);

            DFAgentDescription[] result = null;
            result = DFService.search(this.myAgent, dfd);

            System.out.println(result.length + " results" );
            if (result.length > 0) {
                System.out.println(" " + result[0].getName());
            }

        } catch (FIPAException ex) {
            //FIXME
        }
        // Start CNP
        // Pair up

    }

    @Override
    public boolean done() {
        return ((BlottoAgent)this.myAgent).units == 0;
    }
}

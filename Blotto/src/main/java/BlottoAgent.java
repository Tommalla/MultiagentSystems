import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BlottoAgent extends Agent {
    public int units;

    @Override
    protected void setup() {
        // register agent
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("Blotto");
        sd.setName(getLocalName() + "-Blotto");
        dfd.addServices(sd);
        dfd.addOntologies("blotto-ontology");
        dfd.addProtocols("fipa-contract-net");
        dfd.addLanguages("fipa-sl");

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // TODO how to handle messages before 60 secs?
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.

        c.add(Calendar.MINUTE, 1); // Adds 1 minute
        this.addBehaviour(new WaitBehaviour(this, Date.from(c.toInstant())));
    }
}

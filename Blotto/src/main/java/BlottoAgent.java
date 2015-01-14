import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BlottoAgent extends Agent {
    public int units;
    DataStore dataStore;

    @Override
    protected void setup() {
        // Get the number of units.
        units = Integer.parseInt(getArguments()[0].toString());
        dataStore = new DataStore();

        // Register agent.
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

        // Setup the recognized langugages and ontologies
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(BlottoOntology.getInstance());

        // Add the waiting behaviour.
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.

        c.add(Calendar.MINUTE, 1); // Adds 1 minute.
        this.addBehaviour(new WaitBehaviour(this, Date.from(c.toInstant()), dataStore));

        MessageTemplate mt = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        this.addBehaviour(new ResponderBehaviour(this, mt, dataStore));
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {}
    }
}

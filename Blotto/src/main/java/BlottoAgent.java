import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;


public class BlottoAgent extends Agent {
    public int units;
    private static final long timeout = 60000;

    @Override
    protected void setup() {
        // Get the number of units.
        units = Integer.parseInt(getArguments()[0].toString());

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
        this.addBehaviour(new WaitBehaviour(this, timeout));

        MessageTemplate mt = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        this.addBehaviour(new ResponderBehaviour(this, mt));
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {}
    }

    public List<AID> getAgentsFromDF() {
        List<AID> result = new LinkedList<AID>();

        DFAgentDescription agentDescription = new DFAgentDescription();
        final ServiceDescription sd = new ServiceDescription();
        sd.setType("Blotto");
        agentDescription.addServices(sd);
        try {
            for (DFAgentDescription description : DFService.search(this, agentDescription)) {
                final AID aid = description.getName();
                if (!aid.equals(getAID())) {
                    result.add(aid);
                }
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return result;
    }

    public CommittedUnits extractCommittedUnits(ACLMessage request) {
        // Extracting committed units.

        if (!FIPANames.ContentLanguage.FIPA_SL.equals(request.getLanguage()))
        {
            throw new IllegalArgumentException(
                    "Unrecognized content language: '" + request.getLanguage() +
                            "'I recognize fipa-sl content only.");
        }

        if (!BlottoOntology.ONTOLOGY_NAME.equals(request.getOntology()))
        {
            throw new IllegalArgumentException("Unrecognized ontology: I recognize blotto-ontology only.");
        }

        ContentManager cm = getContentManager();
        try
        {
            // FIXME!!!
            return (CommittedUnits) cm.extractContent(request);
        }
        catch (Codec.CodecException ex)
        {
            throw new IllegalArgumentException("Content is invalid: " + ex.getMessage());
        }
        catch (OntologyException ex)
        {
            throw new IllegalArgumentException("Content is invalid: " + ex.getMessage());
        }
        catch (NullPointerException ex)
        {
            throw new IllegalArgumentException("Content is invalid: " + ex.getMessage());
        }
    }
}

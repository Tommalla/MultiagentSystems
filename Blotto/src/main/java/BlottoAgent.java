import jade.content.ContentElement;
import jade.content.ContentElementList;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BlottoAgent extends Agent {
    private static final String BLOTTO = "Blotto";
    private static final long WAIT_TIMEOUT = 60000;
    private static final Logger logger = Logger.getLogger(BlottoAgent.class.getName());
    private int units;
    // The current number of active propose or accept-proposal 'transactions'.
    private int activeTransations = 0;
    // Has all the necessary communication finished?
    private final List results = new ArrayList<String>();


    public List<AID> getAgentsFromDF() {
        List<AID> result = new LinkedList<AID>();

        DFAgentDescription agentDescription = new DFAgentDescription();
        final ServiceDescription sd = new ServiceDescription();
        sd.setType(BLOTTO);
        agentDescription.addServices(sd);
        try {
            for (DFAgentDescription description : DFService.search(this, agentDescription)) {
                final AID aid = description.getName();
                if (!aid.equals(getAID())) {
                    result.add(aid);
                }
            }
        } catch (FIPAException e) {
            logger.log(Level.SEVERE, "Cannot get agents' list from DF: {0}", e.getMessage());
        }

        return result;
    }


    public int getUnits() {
        return units;
    }


    public void startTransaction(int unitsGiven) {
        activeTransations++;
        units -= unitsGiven;
    }


    // A successful finish of a transaction.
    public void finishTransaction() {
        activeTransations--;
        if (isFinished()) {
            // Please kill me after some timeout.
            addBehaviour(new CloseBehaviour(this));
        }
    }


    public void breakTransaction(int unitsRestored) {
        activeTransations--;
        units += unitsRestored;
    }


    public boolean isFinished() {
        return units == 0 && activeTransations == 0;
    }


    public WaitBehaviour getNewWaitBehaviour(long timeout) {
        return new WaitBehaviour(this, timeout);
    }


    public InitiatorBehaviour getNewInitiatorBehaviour() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        cfp.setOntology(BlottoOntology.ONTOLOGY_NAME);
        return new InitiatorBehaviour(this, cfp);
    }


    public void addResult(String otherAgent, int result) {
        results.add(getLocalName() + " & " + otherAgent + ": " + result);
    }


    public Action extractPlayBlottoAction(ACLMessage request) {
        return (Action)extractContentElement(request, 0);
    }


    public CommittedUnits extractCommittedUnits(ACLMessage request) {
        return (CommittedUnits)extractContentElement(request, 1);
    }


    public BlottoResult extractBlottoResult(ACLMessage request) {
        return (BlottoResult)extractContent(request);
    }


    public void fillMessage(ACLMessage msg, ContentElement ce) {
        try {
                getContentManager().fillContent(msg, ce);
        } catch (Codec.CodecException ex) {
            logger.log(Level.SEVERE, "Couldn't construct message: {0}", ex.getMessage());
        } catch (OntologyException ex) {
            logger.log(Level.SEVERE, "Couldn't construct message: {0}", ex.getMessage());
        }
    }


    @Override
    protected void setup() {
        // Get the number of units.
        units = Integer.parseInt(getArguments()[0].toString());

        // Register agent.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd  = new ServiceDescription();
        sd.setType(BLOTTO);
        sd.setName(getLocalName() + "-" + BLOTTO);
        dfd.addServices(sd);
        dfd.addOntologies(BlottoOntology.ONTOLOGY_NAME);
        dfd.addProtocols(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        dfd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            logger.log(Level.SEVERE, "Cannot register in df: {0}", fe.getMessage());
        }

        // Setup the recognized langugages and ontologies
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(BlottoOntology.getInstance());

        // Add the waiting behaviour.
        addBehaviour(getNewWaitBehaviour(WAIT_TIMEOUT));

        MessageTemplate mt = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        for (int i = 0; i < 7; ++i) {
            addBehaviour(new ResponderBehaviour(this, mt));
        }
    }


    @Override
    protected void takeDown() {
        for (Object o: results) {
            System.out.println((String)o);
        }

        try {
            DFService.deregister(this);
        } catch (FIPAException e) {}
    }


    private Object extractContentElement(ACLMessage request, int index) {
        return ((ContentElementList)extractContent(request)).get(index);
    }


    private Object extractContent(ACLMessage request) {
        if (!FIPANames.ContentLanguage.FIPA_SL.equals(request.getLanguage())) {
            throw new InvalidContentException(
                    "Unrecognized content language: '" + request.getLanguage() +
                            "'I recognize fipa-sl content only.");
        }

        if (!BlottoOntology.ONTOLOGY_NAME.equals(request.getOntology())) {
            throw new InvalidContentException("Unrecognized ontology: I recognize blotto-ontology only.");
        }

        ContentManager cm = getContentManager();
        try {
            return cm.extractContent(request);
        } catch (Codec.CodecException ex) {
            throw new InvalidContentException("Content is invalid: " + ex.getMessage());
        } catch (OntologyException ex) {
            throw new InvalidContentException("Content is invalid: " + ex.getMessage());
        } catch (NullPointerException ex) {
            throw new InvalidContentException("Content is invalid: " + ex.getMessage());
        }
    }
}

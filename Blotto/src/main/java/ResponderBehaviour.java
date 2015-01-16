import jade.content.ContentElementList;
import jade.content.lang.Codec;
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
import java.util.logging.Logger;


public class ResponderBehaviour extends ContractNetResponder {
    public int givenUnits = 0;
    private static Logger logger = Logger.getLogger(ResponderBehaviour.class.getName());

    public ResponderBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
        try {
            AID arbitrator = getArbitrator();
            ACLMessage playMsg = new ACLMessage(ACLMessage.REQUEST);
            playMsg.addReceiver(arbitrator);
            playMsg.addReplyTo(myAgent.getAID());
            playMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            playMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            playMsg.setOntology(BlottoOntology.ONTOLOGY_NAME);

            registerHandleAcceptProposal(new PlayBehaviour(a, playMsg, this, arbitrator));
        } catch (FIPAException ex) {
            logger.severe("Couldn't find arbitrator: " + ex.getMessage());
        }
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp)
    {
        // Prepare the propose message to be sent in response
        BlottoAgent agent = (BlottoAgent)myAgent;
        int minUnits = agent.extractCommittedUnits(cfp).getValue();
        ACLMessage response = cfp.createReply();

        System.out.println("Got CFP with minUnits: " + minUnits);

        if (minUnits <= agent.getUnits()) {
            // We can, so let's propose.
            response.setPerformative(ACLMessage.PROPOSE);

            // Try to give all units.
            ContentElementList cel = new ContentElementList();
            cel.add(agent.extractPlayBlottoAction(cfp));
            cel.add(new CommittedUnits(agent.getUnits()));

            giveUnits(agent.getUnits());

            try
            {
                myAgent.getContentManager().fillContent(response, cel);
            }
            catch (Codec.CodecException ex)
            {
                // ignore
            }
            catch (OntologyException ex)
            {
                // ignore
            }
        } else {
            // Not enough units.
            response.setPerformative(ACLMessage.REFUSE);
        }

        return response;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        reclaimUnits();
    }

    private AID getArbitrator() throws FIPAException {
        DFAgentDescription temp = new DFAgentDescription();
        final ServiceDescription sd = new ServiceDescription();
        sd.setType("Blotto-Play");
        temp.addServices(sd);
        // We return the first one that fits the description.
        return DFService.search(myAgent, temp)[0].getName();
    }


    public void giveUnits(int given) {
        ((BlottoAgent)myAgent).startTransaction(given);
        givenUnits = given;
    }


    public void reclaimUnits() {
        ((BlottoAgent)myAgent).breakTransaction(givenUnits);
        givenUnits = 0;
    }
}

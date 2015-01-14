import jade.content.ContentElementList;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Vector;
import java.util.logging.Logger;


public class InitiatorBehaviour extends ContractNetInitiator {
    DFAgentDescription[] agents = null;

    public InitiatorBehaviour(Agent a, ACLMessage cfp, DataStore store) {
        super(a, cfp, store);
    }

    @Override
    public void onStart() {
        Logger.getGlobal().info("InitiatorBehaviour: Getting agents from DF");

        try {
            // Obtaining known agents from DF.
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd  = new ServiceDescription();
            sd.setType("Blotto");
            dfd.addServices(sd);

            agents = DFService.search(this.myAgent, dfd);

            System.out.println(agents.length + " results" );

        } catch (FIPAException ex) {
            //FIXME
        }
        // TODO send CFPs
        // Pair up
    }

    @Override
    protected Vector prepareCfps(ACLMessage cfp) {
        Vector<ACLMessage> msgs = new Vector<ACLMessage>();
        System.out.println(cfp);
        for (int i = 0; i < agents.length; ++i) {
            ACLMessage newMsg = new ACLMessage(ACLMessage.CFP);
            newMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            newMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            newMsg.setOntology(BlottoOntology.ONTOLOGY_NAME);
            newMsg.addReceiver(agents[i].getName());
            newMsg.addReplyTo(myAgent.getAID());
            // FIXME add replyByDate

            // Try to give all units.
            ContentElementList cel = new ContentElementList();
            cel.add(new CommittedUnits());
             try
            {
                myAgent.getContentManager().fillContent(newMsg, cel);
            }
            catch (Codec.CodecException ex)
            {
                // ignore
            }
            catch (OntologyException ex)
            {
                // ignore
            }

            msgs.add(newMsg);
        }
        return msgs;
    }

}

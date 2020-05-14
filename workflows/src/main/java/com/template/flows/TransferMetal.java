package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TransferMetal extends FlowLogic<SignedTransaction> {


    private  String metalName;
    private  int weight;
    private Party newOwner;
    private int input=0;
    private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retreiving the notary");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating the transaction");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing the transaction");
    private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty");
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Finalising the transaction");

     private  final  ProgressTracker progressTracker= new ProgressTracker(
         RETRIEVING_NOTARY,SIGNING_TRANSACTION,GENERATING_TRANSACTION,COUNTERPARTY_SESSION,FINALISING_TRANSACTION
    );

    public TransferMetal(String metalName, int weight, Party newOwner) {
        this.metalName = metalName;
        this.weight = weight;
        this.newOwner = newOwner;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }
 // --------------------------------------checkForMetalStates--------------------------
    StateAndRef<MetalState> checkForMetalStates() throws FlowException
    {
        QueryCriteria  generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<MetalState>> MetalStates= getServiceHub().getVaultService().queryBy(MetalState.class,generalCriteria).getStates();
       boolean inputFound=false;
       int t= MetalStates.size();
       for(int x=0;x<t;x++)
       {
         if(MetalStates.get(x).getState().getData().getMetalName().equals(metalName)&& MetalStates.get(x).getState().getData().getWeight()==weight)
         {
             input=x;
             inputFound=true;
         }
       }




        if(inputFound)
        {
            System.out.println("\nInput Found");
        }
        else
        {
            System.out.println("\nInput not found");
        }

        return MetalStates.get(input);
    }
    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.

        //Retrieving notary identities
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        StateAndRef<MetalState> inputState=null;
        inputState =checkForMetalStates();
       Party ISSUER= inputState.getState().getData().getIssuer();
        MetalState outputState = new MetalState(metalName,weight,ISSUER,newOwner);
        Command cmd= new Command(new MetalContract.Transfer(),getOurIdentity().getOwningKey());


        // Create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder transactionBuilder= new TransactionBuilder(notary)
                .addOutputState(outputState,MetalContract.CID).addCommand(cmd);
        transactionBuilder.addInputState(inputState);
        // sign the transaction

        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTransaction= getServiceHub().signInitialTransaction(transactionBuilder);

        // counterparty flow session

        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession  otherPartySession = initiateFlow(newOwner);
        FlowSession mintPartySession= initiateFlow(ISSUER);

        // finality flow
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return  subFlow(new FinalityFlow(signedTransaction,otherPartySession,mintPartySession));


    }
}

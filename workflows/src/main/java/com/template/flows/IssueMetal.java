package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IssueMetal extends FlowLogic<SignedTransaction> {


    private  String metalName;
    private  int weight;
    private Party owner;
    private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retreiving the notary");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating the transaction");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing the transaction");
    private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty");
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Finalising the transaction");

     private  final  ProgressTracker progressTracker= new ProgressTracker(
         RETRIEVING_NOTARY,SIGNING_TRANSACTION,GENERATING_TRANSACTION,COUNTERPARTY_SESSION,FINALISING_TRANSACTION
    );

    public IssueMetal(String metalName, int weight, Party owner) {
        this.metalName = metalName;
        this.weight = weight;
        this.owner = owner;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.

        //Retrieving notary identities
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);


        MetalState outputState = new MetalState(metalName,weight,getOurIdentity(),owner);
        Command cmd= new Command(new MetalContract.Issue(),getOurIdentity().getOwningKey());
        // Create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder transactionBuilder= new TransactionBuilder(notary)
                .addOutputState(outputState,MetalContract.CID).addCommand(cmd);
        // sign the transaction

        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTransaction= getServiceHub().signInitialTransaction(transactionBuilder);

        // counterparty flow session

        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession  otherPartySession = initiateFlow(owner);

        // finality flow
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return  subFlow(new FinalityFlow(signedTransaction,otherPartySession));


    }
}

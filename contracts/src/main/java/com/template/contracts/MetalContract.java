package com.template.contracts;

import com.sun.istack.NotNull;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

// ************
// * Contract *
// ************
public class MetalContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String CID = "com.template.contracts.MetalContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Must have exactly one command");
        }
        Command command = tx.getCommand(0);
        CommandData commandType = command.getValue();
        List<PublicKey> requiredSigners = command.getSigners();

        //--------------------------Issue command Rules--------------
        if(commandType instanceof Issue)
        {
            //shape
         if(tx.getInputs().size()!=0) {throw  new IllegalArgumentException("Issue Transaction can not have input state");}
         if(tx.getOutputs().size()!=1){ throw  new IllegalArgumentException("Issue command must have exactly one output state");}



            //content
         ContractState outputstate= tx.getOutput(0);
         if(!(outputstate instanceof MetalState)) {throw  new IllegalArgumentException("outputstate must be instance of Metal State");}
         MetalState metalState = (MetalState)(outputstate);
         if(!(metalState.getMetalName().equals("Gold"))&&!(metalState.getMetalName().equals("Silver")))
         {
             throw  new IllegalArgumentException("Metal State must have Gold or Silver");
         }

            //signers
          Party issuer= metalState.getIssuer();
          PublicKey issuerkey=issuer.getOwningKey();
          if(!(requiredSigners.contains(issuerkey))) throw new IllegalArgumentException("Issuer must sign the transaction");

        }


    //---------------------------Transfer command------------------------

     else if(commandType instanceof Transfer)
    {
        //shape
        if(tx.getInputs().size()!=1) {throw  new IllegalArgumentException("Issue Transaction should  have one  input state");}
        if(tx.getOutputs().size()!=1){ throw  new IllegalArgumentException("Issue command must have exactly one output state");}



        //content
        ContractState inputstate = tx.getInput(0);
        ContractState outputstate= tx.getOutput(0);
        if(!(outputstate instanceof MetalState)) {throw  new IllegalArgumentException("outputstate must be instance of Metal State");}
        MetalState metalState = (MetalState)(inputstate);
        if(!(metalState.getMetalName().equals("Gold"))&&!(metalState.getMetalName().equals("Silver")))
        {
            throw  new IllegalArgumentException("Metal State must have Gold or Silver");
        }

        //signers
        Party owner= metalState.getIssuer();
        PublicKey ownerkey=owner.getOwningKey();
        if(!(requiredSigners.contains(ownerkey))) throw new IllegalArgumentException("Owner must sign the transaction");

    }
     else
        {
            throw  new IllegalArgumentException("Invalid type ");
        }
}

// Used to indicate the transaction's intent.
    public  static   class  Issue implements CommandData{}
    public static    class  Transfer implements  CommandData{}
}
package com.template.contracts;

import com.template.states.MetalState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;
import  static  net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity Mint=new TestIdentity(new CordaX500Name("mint","","GB"));
    private final TestIdentity TraderA=new TestIdentity(new CordaX500Name("traderA","","GB"));
    private final TestIdentity TraderB=new TestIdentity(new CordaX500Name("traderB","","GB"));

    private final MockServices ledgerServices = new MockServices();
   MetalState metalState = new MetalState("Gold",10,Mint.getParty(),TraderA.getParty());
   MetalState metalStateInput=  new MetalState("Gold",10,Mint.getParty(),TraderA.getParty());
    MetalState metalStateOutput=  new MetalState("Gold",10,Mint.getParty(),TraderB.getParty());

    @Test
    public void metalContractImplementsContract() {
    assert (new MetalContract() instanceof Contract);
    }

    //-------------------Issue command---------------------------
  @Test
    public void MetalContractRequiresZeroInputsInIssueTransaction(){
        transaction(ledgerServices,tx->{
            //has an input,will fail
            tx.input(MetalContract.CID,metalState);
            tx.command(Mint.getPublicKey(),new MetalContract.Issue());
            tx.fails();
            return  null;
        });

      transaction(ledgerServices,tx->{
          //Doesn't have an input,will verify
          tx.output(MetalContract.CID,metalState);
          tx.command(Mint.getPublicKey(),new MetalContract.Issue());
          tx.verifies();
          return  null;
      });
    }

    @Test
    public void MetalContractRequiresOneOutputInIssueTransaction(){
        transaction(ledgerServices,tx->{
            //has more than one output,will fail
            tx.output(MetalContract.CID,metalState);
            tx.output(MetalContract.CID,metalState);
            tx.command(Mint.getPublicKey(),new MetalContract.Issue());
            tx.fails();
            return  null;
        });

        transaction(ledgerServices,tx->{
            //Have one output ,will verify
            tx.output(MetalContract.CID,metalState);
            tx.command(Mint.getPublicKey(),new MetalContract.Issue());
            tx.verifies();
            return  null;
        });
    }
    @Test
    public void MetalContractRequiresTheTransactionOutputToBeAMetalState(){
        transaction(ledgerServices,tx->{
            //doesn't have output transaction as metal state,will fail
            tx.output(MetalContract.CID,new DummyState());
            tx.command(Mint.getPublicKey(),new MetalContract.Issue());
            tx.fails();
            return  null;
        });

        transaction(ledgerServices,tx->{
            //Have metalState in output transaction ,will verify
            tx.output(MetalContract.CID,metalState);
            tx.command(Mint.getPublicKey(),new MetalContract.Issue());
            tx.verifies();
            return  null;
        });
    }
    @Test
    public void MetalContractRequiresTheTransactionCommandToBeAnIssueCommand(){
        transaction(ledgerServices,tx->{
            //doesn't have issue command,will fail
            tx.output(MetalContract.CID,metalState);
            tx.command(Mint.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return  null;
        });

        transaction(ledgerServices,tx->{
            //Have issue state ,will verify
            tx.output(MetalContract.CID,metalState);
            tx.command(Mint.getPublicKey(),new MetalContract.Issue());
            tx.verifies();
            return  null;
        });
    }

    @Test
    public void MetalContractRequiresTheIssuerToBeARequiredSignerInTheTransaction(){
        transaction(ledgerServices,tx->{
            //Issuer is not a required signer,will fail
            tx.output(MetalContract.CID,metalState);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return  null;
        });

        transaction(ledgerServices,tx->{
            //issuer is a required signer,will verify
            tx.output(MetalContract.CID,metalState);
            tx.command(Mint.getPublicKey(),new MetalContract.Issue());
            tx.verifies();
            return  null;
        });
    }

 //------------------------Transfer command tests-------------------------

    @Test
    public void MetalContractRequiresOneInputAndOneOutputInTransferTransaction(){
        transaction(ledgerServices,tx->{
            //has an input and an ouput,will verify
            tx.input(MetalContract.CID,metalStateInput);
            tx.output(MetalContract.CID,metalStateOutput);
            tx.command(TraderA.getPublicKey(),new MetalContract.Transfer());
            tx.verifies();
            return  null;
        });

        transaction(ledgerServices,tx->{
            //Doesn't have an input,will fail
            tx.output(MetalContract.CID,metalStateOutput);
            tx.command(TraderA.getPublicKey(),new MetalContract.Transfer());
            tx.fails();
            return  null;
        });
        transaction(ledgerServices,tx->{
            //Doesn't have an output,will fail
            tx.input(MetalContract.CID,metalStateInput);
            tx.command(TraderA.getPublicKey(),new MetalContract.Transfer());
            tx.fails();
            return  null;
        });

    }
    @Test
    public void MetalContractRequiresTheTransactionCommandToBeATransferCommand(){
        transaction(ledgerServices,tx->{
            //doesn't have transfer command,will fail
            tx.input(MetalContract.CID,metalStateInput);
            tx.output(MetalContract.CID,metalStateOutput);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return  null;
        });

        transaction(ledgerServices,tx->{
            //Have transfer command ,will verify
            tx.input(MetalContract.CID,metalStateInput);
            tx.output(MetalContract.CID,metalStateOutput);
            tx.command(TraderA.getPublicKey(),new  MetalContract.Transfer() );
            tx.verifies();
            return  null;
        });
    }
    @Test
    public void MetalContractRequiresTheOwnerToBeARequiredSignerInTheTransaction(){
        transaction(ledgerServices,tx->{
            tx.input(MetalContract.CID,metalStateInput);
            tx.output(MetalContract.CID,metalStateOutput);
            tx.command(Mint.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return  null;
        });

        transaction(ledgerServices,tx->{
            //issuer is a required signer,will verify
            tx.input(MetalContract.CID,metalStateInput);
            tx.output(MetalContract.CID,metalStateOutput);
            tx.command(TraderA.getPublicKey(),new MetalContract.Issue());
            tx.verifies();
            return  null;
        });
    }


}
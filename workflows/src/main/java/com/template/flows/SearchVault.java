package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.MetalState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SearchVault extends FlowLogic<Void> {

   void searchForAllState()
   {
        //----------------------------consumed states----------------------------

       QueryCriteria consumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
       List<StateAndRef<MetalState>> consumedMetalStates= getServiceHub().getVaultService().queryBy(MetalState.class,consumedCriteria).getStates();
       int c= consumedMetalStates.size();
         if(consumedMetalStates.size()<1)
           {
               System.out.println("\n No consumedmetalstates found");
           }
           else
           {
               System.out.println("Consumed states available"+consumedMetalStates.size());
           }
       for(int i=0;i<c;i++) {
           System.out.println("\n Metal Name :" + consumedMetalStates.get(i).getState().getData().getMetalName());
           System.out.println("  Metal Weight:" + consumedMetalStates.get(i).getState().getData().getWeight());
           System.out.println(" Owner :" + consumedMetalStates.get(i).getState().getData().getOwner());
           System.out.println(" Issuer :" + consumedMetalStates.get(i).getState().getData().getIssuer());

       }
   //----------------------------------------unconsumed states-------------------------------------

           QueryCriteria unconsumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
           List<StateAndRef<MetalState>> unconsumedMetalStates= getServiceHub().getVaultService().queryBy(MetalState.class,unconsumedCriteria).getStates();
           int u= consumedMetalStates.size();
           if(consumedMetalStates.size()<1)
           {
               System.out.println("\n No Unconsumedmetalstates found");
           }
           else
           {
               System.out.println("UnConsumed states available"+unconsumedMetalStates.size());
           }
           for(int j=0;j<u;j++)
           {
               System.out.println("\n Metal Name :"+unconsumedMetalStates.get(j).getState().getData().getMetalName());
               System.out.println("  Metal Weight:"+unconsumedMetalStates.get(j).getState().getData().getWeight());
               System.out.println(" Owner :"+unconsumedMetalStates.get(j).getState().getData().getOwner());
               System.out.println(" Issuer :"+unconsumedMetalStates.get(j).getState().getData().getIssuer());
       }

   }
    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.
        searchForAllState();

        return null;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import java.util.List;

/**
 * Topology based best fit
 *
 * @author Atakan
 */
public class TBFDatacenterBroker extends BrokerStrategy{

    public TBFDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void createSingleVm(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void createGroupVm(List<Integer> g, Double[][] t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

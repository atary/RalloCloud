/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim;

import java.util.List;

/**
 *
 * @author Atakan
 */
public class VmSchedulerSpaceShareQueued extends VmScheduler{

    public VmSchedulerSpaceShareQueued(List<? extends Pe> pelist) {
        super(pelist);
    }

    @Override
    public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deallocatePesForVm(Vm vm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

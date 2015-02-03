package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.Vm;

public class BwProvisionerNetworked extends BwProvisioner{

    public BwProvisionerNetworked(long bw) {
        super(bw);
    }

    @Override
    public boolean allocateBwForVm(Vm vm, long bw) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getAllocatedBwForVm(Vm vm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deallocateBwForVm(Vm vm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSuitableForVm(Vm vm, long bw) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deallocateBwForAllVms() {
        super.deallocateBwForAllVms(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}

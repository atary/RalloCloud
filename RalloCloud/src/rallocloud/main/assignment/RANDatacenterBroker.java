/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import java.util.ArrayList;
import java.util.Collections;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 *
 * @author Atakan
 */
public class RANDatacenterBroker extends BrokerStrategy{

    public RANDatacenterBroker(String name) throws Exception {
        super(name);
    }
    
    @Override
    protected void processResourceCharacteristics(SimEvent ev) {
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
                setDatacenterRequestedIdsList(new ArrayList<Integer>());
                Collections.shuffle(getDatacenterIdsList());
                createVmsInDatacenter(getDatacenterIdsList().get(0));
        }
    }
}

package cloudsim.rr;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import java.util.List;

public class roundrobin {
	public void applyRoundRobinAllocation(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int vmIndex = 0;
        for (Cloudlet cloudlet : cloudletList) {
            Vm vm = vmList.get(vmIndex);
            cloudlet.setVmId(vm.getId());
            vmIndex = (vmIndex + 1) % vmList.size();
        }
    }

}

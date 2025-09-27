package cloudsim.Hybrid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class HybridGJO_WOA_LoadBalancing {
    public static void main(String[] args) {
        try {
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            
            CloudSim.init(numUsers, calendar, traceFlag);
            
            Datacenter datacenter = createDatacenter("Datacenter_0");
            DatacenterBroker broker = new DatacenterBroker("Broker");
            
            List<Vm> vmList = createVMs(broker.getId(), 8);
            List<Cloudlet> cloudletList = createCloudlets(broker.getId(), 2100);
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            
            HybridGJO_WOA optimizer = new HybridGJO_WOA(cloudletList, vmList);
            
            long startTime = System.currentTimeMillis();
            optimizer.runOptimization();
            long endTime = System.currentTimeMillis();
            
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            
            printCloudletList(cloudletList, startTime, endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(400000)));

        hostList.add(new Host(0, new RamProvisionerSimple(65536), new BwProvisionerSimple(10000),
                1000000, peList, new VmSchedulerTimeShared(peList)));

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.001, 0.0);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }

    private static List<Vm> createVMs(int userId, int numVMs) {
        List<Vm> vms = new ArrayList<>();
        for (int i = 0; i < numVMs; i++) {
            vms.add(new Vm(i, userId, 25000, 1, 65536 / numVMs, 1000, 10000, "Xen", new CloudletSchedulerTimeShared()));
        }
        return vms;
    }

    private static List<Cloudlet> createCloudlets(int userId, int numCloudlets) {
        List<Cloudlet> cloudlets = new ArrayList<>();
        for (int i = 0; i < numCloudlets; i++) {
            cloudlets.add(new Cloudlet(i, 200000, 1, 300, 300, new UtilizationModelFull(),
                    new UtilizationModelFull(), new UtilizationModelFull()));
            cloudlets.get(i).setUserId(userId);
        }
        return cloudlets;
    }

    private static void printCloudletList(List<Cloudlet> list, long startTime, long endTime) {
        DecimalFormat dft = new DecimalFormat("###.##");
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID\tSTATUS\tData center ID\tVM ID\tResponse Time\tStart Time\tFinish Time");
        
        // Map to store the highest actual CPU time per VM
        Map<Integer, Double> vmActualCPUTime = new HashMap<>();
        
        for (Cloudlet cloudlet : list) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                double actualCPUTime = cloudlet.getActualCPUTime();
                
                // Store the highest actual CPU time for each VM
                vmActualCPUTime.put(cloudlet.getVmId(), 
                    Math.max(vmActualCPUTime.getOrDefault(cloudlet.getVmId(), 0.0), actualCPUTime));
                
                System.out.printf("%-10d\t%-10s\t%-14d\t%-6d\t%-8s\t%-12s\t%-12s%n",
                        cloudlet.getCloudletId(), "SUCCESS", cloudlet.getResourceId(), cloudlet.getVmId(),
                        dft.format(actualCPUTime), dft.format(cloudlet.getExecStartTime()),
                        dft.format(cloudlet.getFinishTime()));
            }
        }
        
        // Print highest actual CPU time per VM
        System.out.println("\nActual CPU Time per VM:");
        for (Map.Entry<Integer, Double> entry : vmActualCPUTime.entrySet()) {
            System.out.printf("%d - %s%n", entry.getKey(), dft.format(entry.getValue()));
        }
        
        // Calculate and print overall execution time and RPS
        double totalTime = (endTime - startTime) / 1000.0;
        double rps = list.size() / totalTime;
        System.out.printf("\nExecution Time: %.2f%n", totalTime);
        System.out.printf("Requests Per Second (RPS): %.2f%n", rps);
    }
}
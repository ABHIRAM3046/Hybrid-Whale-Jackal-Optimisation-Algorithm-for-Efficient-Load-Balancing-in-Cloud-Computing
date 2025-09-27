package cloudsim.woagjo;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;

public class GJOCloudSimExample {
    public static void main(String[] args) {
        try {
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            Datacenter datacenter = createDatacenter("Datacenter_0");

            DatacenterBroker broker = new DatacenterBroker("Broker");

            List<Vm> vmList = createVMs(broker.getId(), 8);  // Increased VMs to avoid out-of-bounds issue
            List<Cloudlet> cloudletList = createCloudlets(broker.getId(), 2100);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            GoldenJackalAlgorithm gjo = new GoldenJackalAlgorithm(cloudletList.size(), vmList.size());
            
            long startTime = System.currentTimeMillis();
            gjo.run(cloudletList, vmList);
            long endTime = System.currentTimeMillis();

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            printCloudletList(cloudletList);

            System.out.printf("Start Time: %d ms%n", startTime);
            System.out.printf("End Time: %d ms%n", endTime);
            double totalTimeInSeconds = (endTime - startTime) / 1000.0;
            double rps = cloudletList.size() / totalTimeInSeconds;
            System.out.printf("Total Cloudlets: %d%n", cloudletList.size());
            System.out.printf("Execution Time: %.2f seconds%n", totalTimeInSeconds);
            System.out.printf("Requests Per Second (RPS): %.2f%n", rps);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(500000)));

        int ram = 524288;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(
                0, new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw), storage,
                peList, new VmSchedulerTimeShared(peList)
        ));

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.001, 0.0
        );

        try {
            return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static List<Vm> createVMs(int userId, int numVMs) {
        List<Vm> vms = new ArrayList<>();

        long size = 10000;
        int ram =  524288/numVMs;       
        int mips = 25000;
        long bw = 1000;
        int pesNumber = 1;
        String vmm = "Xen";

        for (int i = 0; i < numVMs; i++) {
            Vm vm = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vms.add(vm);
        }

        return vms;
    }
    
    private static List<Cloudlet> createCloudlets(int userId, int numCloudlets) {
        List<Cloudlet> cloudlets = new ArrayList<>();

        long length = 200000;
        int pesNumber = 1;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < numCloudlets; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(userId);
            cloudlets.add(cloudlet);
        }

        return cloudlets;
    }
  
    private static void printCloudletList(List<Cloudlet> list) {
        DecimalFormat dft = new DecimalFormat("###.##");
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID\tSTATUS\t\tData center ID\tVM ID\tResponse Time\tStart Time\tFinish Time");

        for (Cloudlet cloudlet : list) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                System.out.printf("%-10d\t%-10s\t%-14d\t%-6d\t%-8s\t%-12s\t%-12s%n",
                        cloudlet.getCloudletId(),
                        "SUCCESS",
                        cloudlet.getResourceId(),
                        cloudlet.getVmId(),
                        dft.format(cloudlet.getActualCPUTime()),
                        dft.format(cloudlet.getExecStartTime()),
                        dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}

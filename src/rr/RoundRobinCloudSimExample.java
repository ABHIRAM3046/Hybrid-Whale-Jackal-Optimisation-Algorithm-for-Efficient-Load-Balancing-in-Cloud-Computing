package cloudsim.rr;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class RoundRobinCloudSimExample {

    public static void main(String[] args) {
        try {
            // Step 1: Initialize the CloudSim library
            int numUsers = 1;  // Number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;  // Mean trace events

            // Initialize CloudSim
            CloudSim.init(numUsers, calendar, traceFlag);

            // Step 2: Create Datacenter
            Datacenter datacenter = createDatacenter("Datacenter_0");

            // Step 3: Create Broker
            DatacenterBroker broker = new DatacenterBroker("Broker");

            // Step 4: Create VMs and Cloudlets
            List<Vm> vmList = createVMs(broker.getId(), 8); // 8 VMs
            List<Cloudlet> cloudletList = createCloudlets(broker.getId(), 2100); // 2100 Cloudlets
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // Step 5: Apply Round Robin for resource allocation
            applyRoundRobinAllocation(cloudletList, vmList);

            // Start time tracking
            long startTime = System.currentTimeMillis();

            // Step 6: Start CloudSim Simulation
            CloudSim.startSimulation();

            // Step 7: Stop CloudSim simulation
            CloudSim.stopSimulation();

            // End time tracking
            long endTime = System.currentTimeMillis();

            // Step 8: Retrieve and display Cloudlet execution results
            printCloudletList(cloudletList);

            // Step 9: Calculate and display Requests Per Second (RPS)
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

    // Method to create a Datacenter
    public static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        int mips = 400000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        int ram = 524288;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(
                0,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        ));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double costPerSec = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone, costPerSec, costPerMem, costPerStorage, costPerBw
        );

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    // Method to create VMs
    private static List<Vm> createVMs(int userId, int numVMs) {
        List<Vm> vms = new ArrayList<>();

        long size = 10000;
        int ram =  524288 / numVMs;
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

    // Method to create Cloudlets
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

    // Method to apply Round Robin Allocation
    private static void applyRoundRobinAllocation(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int vmIndex = 0;
        for (Cloudlet cloudlet : cloudletList) {
            Vm vm = vmList.get(vmIndex);
            cloudlet.setVmId(vm.getId());
            vmIndex = (vmIndex + 1) % vmList.size();
        }
    }

    // Method to print cloudlet execution details in structured format
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

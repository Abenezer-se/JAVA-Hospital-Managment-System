package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            HospitalServiceImpl service = new HospitalServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("HospitalService", service);
            System.out.println("RMI Server running on port 1099...");
        } catch (Exception e) {
            System.out.println("RMI Server error: " + e.getMessage());
        }
    }
}
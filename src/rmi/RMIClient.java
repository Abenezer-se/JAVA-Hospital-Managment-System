package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            HospitalService service = (HospitalService) registry.lookup("HospitalService");
            System.out.println("Total Patients    : " + service.getTotalPatients());
            System.out.println("Total Doctors     : " + service.getTotalDoctors());
            System.out.println("Total Appointments: " + service.getTotalAppointments());
            System.out.println("Total Revenue     : ETB " + service.getTotalRevenue());
            System.out.println(service.getServerStatus());
        } catch (Exception e) {
            System.out.println("RMI Client error: " + e.getMessage());
        }
    }
}
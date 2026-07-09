package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HospitalService extends Remote {
    int getTotalPatients() throws RemoteException;

    int getTotalDoctors() throws RemoteException;

    int getTotalAppointments() throws RemoteException;

    double getTotalRevenue() throws RemoteException;

    String getServerStatus() throws RemoteException;
}
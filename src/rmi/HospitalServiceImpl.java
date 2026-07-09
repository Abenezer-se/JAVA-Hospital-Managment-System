package rmi;

import database.DBConnection;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

public class HospitalServiceImpl extends UnicastRemoteObject implements HospitalService {
    public HospitalServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public int getTotalPatients() throws RemoteException {
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM patients")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getTotalDoctors() throws RemoteException {
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM doctors")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getTotalAppointments() throws RemoteException {
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM appointments")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public double getTotalRevenue() throws RemoteException {
        try (Connection con = DBConnection.getConnection();
                ResultSet rs = con.createStatement().executeQuery(
                        "SELECT IFNULL(SUM(total_amount),0) FROM payments WHERE payment_status='PAID'")) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String getServerStatus() throws RemoteException {
        return "Hospital RMI Server is RUNNING";
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.scene.control.Label;

/**
 *
 * @author taleb
 */
public class UsersMysql {

    private static String driverName = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/";
    private static String dbName = "face_detector_dba";
    private static String userName = "root";
    private static String password = "utP@j<b.";

    public static void insert(final String nom, final String prenom, final String telephone, final String date_naissance, final int id) {
        System.out.println("Insert user!");
        Connection con ;
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url + dbName, userName, password);
            Statement st = con.createStatement();

            PreparedStatement pre
                    = con.prepareStatement("insert into users values(?,?,?,?,?)");

            pre.setInt(1, id);
            pre.setString(2, nom);
            pre.setString(3, prenom);
            pre.setString(4, date_naissance);
            pre.setString(5, telephone);
            pre.executeUpdate();
            System.out.println("Successfully inserted the file into the database!");

            pre.close();
            con.close();
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
        }
    }

    public synchronized static User read(int id, Label detected) {
        System.out.println("Retrive user Example! "+id);

        Connection con ;
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url + dbName, userName, password);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from users where id = " + id);
        
            while (rs.next()) {
                int id_user = rs.getInt(1);
                String nom = rs.getString(2);
                String prenom = rs.getString(3);
                String date_naissance = rs.getString(4);
                String telephone= rs.getString(5);
                final User user = new User(id_user, nom, prenom, date_naissance, telephone);
                System.out.println("user = "+user);
                detected.setText(user.getNom() + " "+user.getPrenom());
                return user;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return new User(-1, "not", "found", "non", "non");
    }

   
}

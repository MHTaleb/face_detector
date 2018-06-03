/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mysql;


import java.sql.*;
import java.io.*;

/**
 *
 * @author taleb
 */
public class ImagesMysql {

       private static String driverName = "com.mysql.jdbc.Driver";
       private static String url = "jdbc:mysql://localhost:3306/";
       private static String dbName = "face_detector_dba";
       private static String userName = "root";
       private static String password = "utP@j<b.";
    public static void insert(final String image_path, final String imageName) {
        System.out.println("Insert Image Example!");
        Connection con = null;
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url + dbName, userName, password);
            Statement st = con.createStatement();
            File imgfile = new File(image_path);

            FileInputStream fin = new FileInputStream(imgfile);

            PreparedStatement pre
                    = con.prepareStatement("insert into Image values(?,?,?)");

            pre.setString(1, imageName);
            pre.setInt(2, 3);
            pre.setBinaryStream(3, (InputStream) fin, (int) imgfile.length());
            pre.executeUpdate();
            System.out.println("Successfully inserted the file into the database!");

            pre.close();
            con.close();
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
        }
    }
    
    
    public static void read(){
    System.out.println("Retrive Image Example!");

    Connection con = null;
    try{
        Class.forName(driverName);
        con = DriverManager.getConnection(url+dbName,userName,password);
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from image");
        int i = 0;
        while (rs.next()) {
            String imageName = rs.getString(1);
            InputStream in = rs.getBinaryStream(3);
            OutputStream f = new FileOutputStream(new File(imageName+".png"));
            i++;
            int c = 0;
            while ((c = in.read()) > -1) {
                f.write(c);
            }
            f.close();
            in.close();
        }
    }catch(Exception ex){
        System.out.println(ex.getMessage());
    }
}
    
}

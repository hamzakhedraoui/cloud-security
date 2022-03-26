import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class Database {
    String dbURL = "jdbc:mysql://localhost:3306/cloud";
    String username = "newuser";
    String password = "password";

    public Database() {
    }

    public void updateAes(String value) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "update info set aes = '"+value+"' ;";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("Database updated successfully ");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void initialize(){
        updateAes("");
        updateDir("");
        updatePart1("");
        updatePart2("");
        updatePart3("");
        updatePart4("");
    }

    public void updatePart1(String value) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "update info set part1 = '"+value+"' ;";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("Database updated successfully ");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updatePart2(String value) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "update info set part2 = '"+value+"' ;";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("Database updated successfully ");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updatePart3(String value) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "update info set part3 = '"+value+"' ;";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("Database updated successfully ");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updatePart4(String value) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "update info set part4 = '"+value+"' ;";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("Database updated successfully ");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateDir(String value) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "update info set dir = '"+value+"';";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("Database updated successfully ");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void updatePubkey(String value) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "update info set pubkey = '"+value+"' ;";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("Database updated successfully ");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public HashMap<String,String> selectAll(){
        HashMap<String,String> info = new HashMap<String,String>();
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            String sql = "SELECT * FROM info";

            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);

            while (result.next()){
                info.put("aes",result.getString("aes"));
                info.put("part1",result.getString("part1"));
                info.put("part2",result.getString("part2"));
                info.put("part3",result.getString("part3"));
                info.put("part4",result.getString("part4"));
                info.put("dir",result.getString("dir"));
                info.put("pubkey",result.getString("pubkey"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return info;
    }
/*    create table info(
    aes varchar(300),
    part1 varchar(300),
    part2 varchar(300),
    part3 varchar(300),
     part4 varchar(300),
    dir varchar(300),
    pubkey varchar(300));
    insert into info values ("","","","","","","");
    */

}

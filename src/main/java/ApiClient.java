import com.google.common.io.BaseEncoding;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class ApiClient {

    public String connect(String userName, String password) {
        try {
            Database database = new Database();
            String urlString = "http://localhost:8000/api/connect/?username=" + userName + "&password=" + password + "";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            //Getting the response code
            int responsecode = conn.getResponseCode();
            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {
                String inline = "";
                Scanner scanner = new Scanner(url.openStream());
                //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }
                //Close the scanner
                scanner.close();
                //Using the JSON simple library parse the string into a json object
                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline);
                //Get the required object from the above created object
                String status = (String) data_obj.get("status");
                System.out.println("the status is : " + status);
                long id = (Long) data_obj.get("id");
                System.out.println("the id is : " + id);
                if (status.equals("yes")) {
                    database.updateId("" + id);
                    return "yes";
                } else {
                    return "no";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "no";
    }

    public String saveEccPart(String part) {
        try {
            String[] partShares = {"", "", "", ""};

            int remainder = part.length() % 4;
            //System.out.println("remainder : " + remainder);
            int partLength = part.length() - remainder;
            //System.out.println("partLength : " + partLength);
            int partDevidedByFour = partLength / 4;
            //System.out.println("partDevidedByFour : " + partDevidedByFour);
            int counter = 0;
            int co = 0;
            char[] partArray = part.toCharArray();
            for (int i = 0; i < 4; i++) {
                counter = i * partDevidedByFour;
                //System.out.println("counter : " + counter);
                for (int j = 0; j < partDevidedByFour; j++) {
                    co = counter + j;
                    if(partArray[co] == '+'){
                        partShares[i] = partShares[i] + "%2b";
                    }else {
                        partShares[i] = partShares[i] + partArray[co];
                    }
                    //System.out.println("i : " + i + " co : " + co + " j : " + j + " " + partShares[i]);
                }
            }
            for (int i = 0; i < remainder; i++) {
                co = 4 * partDevidedByFour + i;
                partShares[3] = partShares[3] + partArray[co];
                //System.out.println("i : " + i + " co : " + co + " " + partArray[co]);
            }
/*           for (int i = 0; i < partShares.length; i++) {
                System.out.println("share " + i + " lengthe :" + partShares[i].length() + " String : " + partShares[i]);
            }*/
            for (int i = 0; i < partShares.length; i++) {
                Database database = new Database();
                HashMap<String, String> info = database.selectAll();
                int partn = i+1;
                String urlString = "http://localhost:8000/api/saveeccpart/?id=" + info.get("id") + "&partn=" + partn + "&part=" + partShares[i] + "";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                //Getting the response code
                int responsecode = conn.getResponseCode();
                if (responsecode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responsecode);
                } else {
                    String inline = "";
                    Scanner scanner = new Scanner(url.openStream());
                    //Write all the JSON data into a string using a scanner
                    while (scanner.hasNext()) {
                        inline += scanner.nextLine();
                    }
                    //Close the scanner
                    scanner.close();
                    //Using the JSON simple library parse the string into a json object
                    JSONParser parse = new JSONParser();
                    JSONObject data_obj = (JSONObject) parse.parse(inline);
                    //Get the required object from the above created object
                    String status = (String) data_obj.get("status");
                    System.out.println("the status in save part is is : " + status);
                }
            }
            return "yes";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "no";
    }
    public void save(String part){

    }

    public byte[] getEccPart() {
        try {
            String[] partShares = {"", "", "", ""};
            Database database = new Database();
            HashMap<String, String> info = database.selectAll();
            for (int i = 0; i < partShares.length; i++) {
                int partn = i+1;
                String urlString = "http://localhost:8000/api/geteccpart/?id=" + info.get("id") + "&partn="+partn+"";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                //Getting the response code
                int responsecode = conn.getResponseCode();
                if (responsecode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responsecode);
                } else {
                    String inline = "";
                    Scanner scanner = new Scanner(url.openStream());
                    //Write all the JSON data into a string using a scanner
                    while (scanner.hasNext()) {
                        inline += scanner.nextLine();
                    }
                    //Close the scanner
                    scanner.close();
                    //Using the JSON simple library parse the string into a json object
                    JSONParser parse = new JSONParser();
                    JSONObject data_obj = (JSONObject) parse.parse(inline);
                    //Get the required object from the above created object
                    String status = (String) data_obj.get("status");
                    System.out.println("the status is : " + status);
                    String Eccpart = (String) data_obj.get("part");
                    System.out.println("Eccpart : " + Eccpart);
                    partShares[i] = Eccpart;
                }
            }
            String partCombination = partShares[0]+""+partShares[1]+""+partShares[2]+""+partShares[3];
            byte[] finalArray = BaseEncoding.base64Url().decode(partCombination);
            return finalArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
    public String check(String fileName, String hash){
        try {
            Database database = new Database();
            HashMap<String, String> info = database.selectAll();
            String urlString = "http://localhost:8000/api/check/?id=" + info.get("id") + "&filehash=" + hash + "&filename=" + fileName + "";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            //Getting the response code
            int responsecode = conn.getResponseCode();
            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {
                String inline = "";
                Scanner scanner = new Scanner(url.openStream());
                //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }
                //Close the scanner
                scanner.close();
                //Using the JSON simple library parse the string into a json object
                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline);
                //Get the required object from the above created object
                String status = (String) data_obj.get("status");
                System.out.println("the status of the hash is : "+status);
                if (status.equals("yes")) {
                    JOptionPane.showMessageDialog(null, data_obj.get("message"));
                    return "yes";
                } else {
                    JOptionPane.showMessageDialog( null, data_obj.get("message") , "Error as Title",
                            JOptionPane.ERROR_MESSAGE );
                    return "no";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "no";
    }

}

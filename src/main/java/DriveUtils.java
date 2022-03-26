import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;


public class DriveUtils {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    //private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    //Scopes
    //https://www.googleapis.com/auth/drive
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    //private ArrayList<String> scopesList = new ArrayList<>();
    //private List<String> SCOPES = scopesList;

    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = DriveUtils.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<File> showFiles() {
        List<File> files = null;
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            //System.out.println("Current user name: " + getCredentials(HTTP_TRANSPORT).toString());
            FileList result = service.files().list()
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }
    public String uploadFile(String path){
        String fileId = "";
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            String[] splitPath = path.split(java.io.File.separator);
            String fileName = splitPath[splitPath.length-1];
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            java.io.File filePath = new java.io.File(path);
            FileContent mediaContent = new FileContent("text/txt", filePath);
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            fileId = file.getId();
            System.out.println("File ID: " + file.getId());
        }catch (Exception e){
            e.printStackTrace();
        }
        return fileId;
    }
    public void downloadFile(com.google.api.services.drive.model.File file,String path){
        try{
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            String fileId = file.getId();

            FileOutputStream stream = new FileOutputStream(path+java.io.File.separator+file.getName());
            OutputStream outputStream = new ByteArrayOutputStream();
            service.files().get(fileId)
                    .executeMediaAndDownloadTo(stream);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void uploadParts(String part1,String part2,String workingDir){
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            byte[] part1Bytes = Base64.getDecoder().decode(part1);
            byte[] part2Bytes = Base64.getDecoder().decode(part2);
            String pathToFile1 = workingDir+java.io.File.separator+"part1.txt";
            String pathToFile2 = workingDir+java.io.File.separator+"part2.txt";
            java.io.File file = new java.io.File(pathToFile1);
            file.createNewFile();
            Path path = Paths.get(pathToFile1);
            Files.write(path, part1Bytes);
            java.io.File file1 = new java.io.File(pathToFile2);
            file1.createNewFile();
            Path path1 = Paths.get(pathToFile2);
            Files.write(path1, part2Bytes);

            File part1Metadata = new File();
            part1Metadata.setName("part1.txt");
            File part2Metadata = new File();
            part2Metadata.setName("part2.txt");

            java.io.File filePath1 = new java.io.File(pathToFile1);
            java.io.File filePath2 = new java.io.File(pathToFile2);
            FileContent mediaContent = new FileContent("text/txt", filePath1);
            File Uploadfile = service.files().create(part1Metadata, mediaContent)
                    .setFields("id")
                    .execute();
            String fileId = Uploadfile.getId();
            System.out.println("File ID: " + Uploadfile.getId());
            filePath1.delete();

            FileContent mediaContent1 = new FileContent("text/txt", filePath2);
            File Uploadfile1 = service.files().create(part2Metadata, mediaContent1)
                    .setFields("id")
                    .execute();
            String fileId1 = Uploadfile1.getId();
            System.out.println("File ID: " + Uploadfile1.getId());
            filePath2.delete();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void testDriveUtils() throws IOException, GeneralSecurityException {
        try {
            System.out.println("starting main ....");
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            System.out.println("Current user name: " + getCredentials(HTTP_TRANSPORT).toString());
            FileList result = service.files().list()
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
            /*File fileMetadata = new File();
            fileMetadata.setName("My Report");
            //fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");

            java.io.File filePath = new java.io.File("/home/sophesrex/meoruane.png");
            FileContent mediaContent = new FileContent("image/png", filePath);
            File file =
                    service.files().create(fileMetadata, mediaContent).setFields("id")
                            .execute();
            System.out.println("File ID: " + file.getId());*/
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
            // Print out the message and errors
        }


    }
}

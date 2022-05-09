import com.google.common.io.BaseEncoding;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GUI extends JFrame {

    private JScrollPane scrollBar;
    private JButton generateAESKey;
    private JButton generateECCKeypair;
    private JButton uploadFile;
    private JButton downloadFile;
    private JButton showFiles;
    private JButton encrypt;
    private JButton decrypt;
    private JButton shoosSavingFolder;
    private JPanel mainPanel;
    private JPanel downPanel;
    private JPanel buttons;
    private JPanel console;
    private JTextArea consoleArea;
    private JMenuBar menuBar;
    private JMenu ECCmenu;
    private JMenu AESmenu;
    private JMenu mainMenu;
    private JMenu enc_dec;
    private JMenuItem exportECCKeys;
    private JMenuItem importECCKeys;
    private JMenuItem exportAES;
    private JMenuItem importAES;
    private JMenuItem deleteKeys;
    private JMenuItem clearLog;
    private JMenuItem encrypteAndUpload;
    private JMenuItem downloadAndDecrypt;
    private JMenuItem secondMenu;
    private ECCUtils eccUtils = new ECCUtils();
    private String[] ECCkeys = {"", ""};
    private String AESKey = "";
    private String plainAES = "";
    private String logs = "";
    private String workingDir = "";
    private Database database = new Database();
    HashMap<Integer, byte[]> parts = new HashMap<Integer, byte[]>();
    private final DriveUtils driveUtils = new DriveUtils();
    com.google.api.services.drive.model.File[] filesArray;
    private ApiClient api = new ApiClient();

    public GUI() {
        super("cloud encryption");
        mainPanel = new JPanel();
        downPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        downPanel.setLayout(new GridLayout(2, 1, 5, 0));
        mainPanel.setBackground(Color.CYAN);
        downPanel.setBackground(Color.CYAN);
        //getting information  from the database;
        initiateKeys();
        setSize(830, 550);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setLayout(new GridLayout(2, 1, 5, 0));

        buttons = new JPanel();
        buttons.setSize(new Dimension(600, 150));
        buttons.setBackground(Color.CYAN);
        console = new JPanel();
        console.setBackground(Color.CYAN);
        consoleArea = new JTextArea(12, 50);
        consoleArea.setEnabled(false);
        scrollBar = new JScrollPane(consoleArea);
        scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        consoleArea.setCaretColor(Color.green);
        consoleArea.setSelectionColor(Color.white);
        //consoleArea.setDisabledTextColor(Color.red);
        consoleArea.setBackground(new Color(6, 23, 31));
        consoleArea.setForeground(Color.white);
        Font font = new Font("Segoe Script", Font.BOLD, 15);
        consoleArea.setFont(font);
        generateAESKey = new JButton("AES key");
        generateECCKeypair = new JButton("ECC Keys");
        uploadFile = new JButton("Upload");
        downloadFile = new JButton("Download");
        showFiles = new JButton("Show files");
        encrypt = new JButton("Encrypt");
        decrypt = new JButton("Decrypt");
        shoosSavingFolder = new JButton("saving Folder");
        menuBar = new JMenuBar();
        ECCmenu = new JMenu("KEYS");
        AESmenu = new JMenu("AES");
        enc_dec = new JMenu("Tools");
        mainMenu = new JMenu("ECC");

        encrypteAndUpload = new JMenuItem("encrypt and upload");
        downloadAndDecrypt = new JMenuItem("download and decrypt");
        exportECCKeys = new JMenuItem("Export ECC Keys");
        importECCKeys = new JMenuItem("Import ECC Key");
        exportAES = new JMenuItem("Export AES key");
        importAES = new JMenuItem("import AES key");
        deleteKeys = new JMenuItem("Delete Keys");
        clearLog = new JMenuItem("Clear Log");
        ECCmenu.add(importECCKeys);
        ECCmenu.add(exportECCKeys);
        mainMenu.add(ECCmenu);
        AESmenu.add(importAES);
        AESmenu.add(exportAES);
        mainMenu.add(AESmenu);
        enc_dec.add(encrypteAndUpload);
        enc_dec.add(downloadAndDecrypt);
        mainMenu.add(deleteKeys);
        mainMenu.add(clearLog);
        menuBar.add(mainMenu);
        menuBar.add(enc_dec);


        checkEnabelaty();

        buttons.setLayout(new GridLayout(4, 4, 8, 30));
        console.setLayout(new FlowLayout());

        clearLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                logs = "";
                consoleArea.setText(logs);
            }
        });
        exportECCKeys.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ECCkeys[0].equals("") && !ECCkeys[1].equals("")) {
                    try {
                        String keys = ECCkeys[0] + "-----" + ECCkeys[1];
                        addLog("keys export : " + keys);
                        byte[] keysBytes = keys.getBytes();
                        String fileName = workingDir + File.separator + "ECCKeys.txt";
                        File file = new File(fileName);
                        file.createNewFile();
                        Path path = Paths.get(fileName);
                        Files.write(path, keysBytes);
                        System.out.println("export ecc keys");
                        addLog("export Ecc keys");
                        checkEnabelaty();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        importECCKeys.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String fromFile = "";
                try {
                    JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    j.setAcceptAllFileFilterUsed(false);
                    j.setDialogTitle("Select ECCkeys file");
                    FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .txt files", "txt");
                    j.addChoosableFileFilter(restrict);
                    int r = j.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        fromFile = j.getSelectedFile().getAbsolutePath();
                        byte[] fileContent = Files.readAllBytes(Paths.get(fromFile));
                        String keys = new String(fileContent);
                        addLog("keys import : " + keys);
                        ECCkeys[0] = keys.split("-----")[0];
                        ECCkeys[1] = keys.split("-----")[1];
                        database.updatePubkey(ECCkeys[0]);
                        addLog("public key : " + ECCkeys[0]);
                        addLog("private key : " + ECCkeys[1]);
                        Map<Integer, byte[]> parts = eccUtils.splitKey(ECCkeys[1]);
                        database.updatePart1(Base64.getEncoder().encodeToString(parts.get(1)));
                        api.saveEccPart(BaseEncoding.base64Url().encode(parts.get(2)));

                        driveUtils.uploadParts(Base64.getEncoder().encodeToString(parts.get(3)), Base64.getEncoder().encodeToString(parts.get(4)), workingDir);
                        for (Map.Entry<Integer, byte[]> entry : parts.entrySet()) {
                            byte[] part = Base64.getDecoder().decode(Base64.getEncoder().encodeToString(entry.getValue()));
                            addLog("Key = " + entry.getKey() +
                                    ", Value = " + Base64.getEncoder().encodeToString(part));
                        }
                        addLog("ECC Keys generated and 2 parts have been uploaded to the cloud...");
                        //addLog("AESkey has been imported");
                        checkEnabelaty();
                    } else
                        addLog("no file has been picked...");
                    //EncryptorAesGcm.decryptFile(toFile, todecFile, AESKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        exportAES.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!AESKey.equals("")) {
                    try {
                        byte[] keyBytes = Base64.getDecoder().decode(AESKey);
                        String fileName = workingDir + File.separator + "AESKeyEncrypted.txt";
                        File file = new File(fileName);
                        file.createNewFile();
                        Path path = Paths.get(fileName);
                        Files.write(path, keyBytes);
                        checkEnabelaty();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        });
        importAES.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!ECCkeys[0].equals("") && !ECCkeys[1].equals("")) {
                    String fromFile = "";
                    try {
                        JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                        j.setAcceptAllFileFilterUsed(false);
                        j.setDialogTitle("Select AESkeys file");
                        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .txt files", "txt");
                        j.addChoosableFileFilter(restrict);
                        int r = j.showOpenDialog(null);
                        if (r == JFileChooser.APPROVE_OPTION) {
                            fromFile = j.getSelectedFile().getAbsolutePath();
                            byte[] fileContent = Files.readAllBytes(Paths.get(fromFile));
                            AESKey = Base64.getEncoder().encodeToString(fileContent);
                            database.updateAes(AESKey);
                            addLog("AESkey has been imported");
                            checkEnabelaty();
                        } else
                            addLog("no file has been picked...");
                        //EncryptorAesGcm.decryptFile(toFile, todecFile, AESKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        deleteKeys.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ECCkeys[0] = "";
                ECCkeys[1] = "";
                AESKey = "";
                database.updatePubkey("");
                database.updateAes("");
                database.updatePart1("");
                api.saveEccPart("");
                java.util.List<com.google.api.services.drive.model.File> files = driveUtils.showFiles();
                if (files == null || files.isEmpty()) {
                    System.out.println("No files found.");
                    addLog("No files found");
                } else {

                    for (com.google.api.services.drive.model.File file : files) {
                        if(file.getName().equals("part1.txt")){
                            driveUtils.deleteFile(file);
                        }
                        if(file.getName().equals("part2.txt")){
                            driveUtils.deleteFile(file);
                        }
                    }
                }
                checkEnabelaty();
            }
        });
        encrypteAndUpload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String fromFile = ""; // from resources folder
                String toFile = "";
                //String todecFile = File.separator + "home" + File.separator + "sophesrex" + File.separator + "Public" + File.separator + "readme.txt";
                try {
                    JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    j.setAcceptAllFileFilterUsed(false);
                    j.setDialogTitle("Select a .txt file");
                    FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .txt files", "txt");
                    j.addChoosableFileFilter(restrict);
                    int r = j.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        //encrypt
                        fromFile = j.getSelectedFile().getAbsolutePath();
                        String[] splitPath = fromFile.split(File.separator);
                        toFile = workingDir + File.separator + "Enc-" + splitPath[splitPath.length - 1];
                        EncryptorAesGcm.encryptFile(fromFile, toFile, plainAES);

                        //upload
                        byte[] fileContent = Files.readAllBytes(Paths.get(toFile));
                        String sha3_256 = ShaUtils.bytesToHex(ShaUtils.digest(fileContent));
                        String[] splitPath2 = toFile.split(File.separator);
                        String fileId = driveUtils.uploadFile(toFile);
                        api.check(splitPath2[splitPath2.length-1],sha3_256);
                        if (fileId.equals("")) {
                            addLog("Problem uploading try again...");
                        } else {
                            addLog("upload finished the file ID in the Drive : " + fileId);
                        }
                        addLog("file has been encrypted successfully.");
                    } else
                        addLog("no file has been picked...");
                    //EncryptorAesGcm.decryptFile(toFile, todecFile, AESKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkEnabelaty();
            }
        });
        downloadAndDecrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String ID = JOptionPane.showInputDialog("Provide file Number to download:");
                int number = -1;
                try {
                    if (filesArray == null) {
                        java.util.List<com.google.api.services.drive.model.File> files = driveUtils.showFiles();
                        if (files == null || files.isEmpty()) {
                            System.out.println("No files found.");
                            addLog("No files found");
                        } else {
                            System.out.println("Files:");
                            filesArray = new com.google.api.services.drive.model.File[files.size()];
                            int counter = 0;
                            for (com.google.api.services.drive.model.File file : files) {
                                filesArray[counter] = file;
                                //addLog(String.format("#%s-- %s (%s)\n",counter+1 ,file.getName(), file.getId()));
                            }
                        }
                    }
                    number = Integer.parseInt(ID);
                    driveUtils.downloadFile(filesArray[number - 1], workingDir);
                    //decryption
                    String fromFile = workingDir+ File.separator +filesArray[number-1].getName();
                    String[] splitPath = fromFile.split("/");
                    if (splitPath[splitPath.length - 1].contains("Enc-")) {
                        String toFile = workingDir + File.separator + "Dec-" + splitPath[splitPath.length - 1].split("-")[1];
                        EncryptorAesGcm.decryptFile(fromFile, toFile, plainAES);
                        addLog("file has been decrypted successfully.");
                    } else {
                        addLog("this file is not encrypted...");
                    }

                    addLog("Download completed Successfully.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "you need provide a valid number.");
                }
                checkEnabelaty();
            }
        });
        generateAESKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    SecretKey secretKey = AES.getAESKey(256);
                    plainAES = AES.convertSecretKeyToString(secretKey);
                    AESKey = eccUtils.textEncrypt(plainAES, ECCkeys[0]);
                    database.updateAes(AESKey);
                    addLog("AES Key : " + plainAES);
                    addLog("AES key hase been generate and encrypted successfully..");
                    checkEnabelaty();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        generateECCKeypair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String[] keys = eccUtils.generateKeyPairs();
                ECCkeys[0] = keys[0];
                ECCkeys[1] = keys[1];
                database.updatePubkey(ECCkeys[0]);
                addLog("public key : " + ECCkeys[0]);
                addLog("private key : " + ECCkeys[1]);
                Map<Integer, byte[]> parts = eccUtils.splitKey(ECCkeys[1]);
                database.updatePart1(Base64.getEncoder().encodeToString(parts.get(1)));
                //api.saveEccPart(Base64.getEncoder().encodeToString(parts.get(2)));
                api.saveEccPart(BaseEncoding.base64Url().encode(parts.get(2)));
                driveUtils.uploadParts(Base64.getEncoder().encodeToString(parts.get(3)), Base64.getEncoder().encodeToString(parts.get(4)), workingDir);
                for (Map.Entry<Integer, byte[]> entry : parts.entrySet()) {
                    byte[] part = Base64.getDecoder().decode(Base64.getEncoder().encodeToString(entry.getValue()));
                    addLog("Key = " + entry.getKey() +
                            ", Value = " + Base64.getEncoder().encodeToString(part));
                }
                addLog("ECC Keys generated and 2 parts have been uploaded to the cloud...");
                checkEnabelaty();
            }
        });

        encrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String fromFile = ""; // from resources folder
                String toFile = "";
                //String todecFile = File.separator + "home" + File.separator + "sophesrex" + File.separator + "Public" + File.separator + "readme.txt";
                try {
                    JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    j.setAcceptAllFileFilterUsed(false);
                    j.setDialogTitle("Select a .txt file");
                    FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .txt files", "txt");
                    j.addChoosableFileFilter(restrict);
                    int r = j.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        fromFile = j.getSelectedFile().getAbsolutePath();
                        String[] splitPath = fromFile.split(File.separator);
                        toFile = workingDir + File.separator + "Enc-" + splitPath[splitPath.length - 1];
                        EncryptorAesGcm.encryptFile(fromFile, toFile, plainAES);
                        addLog("file has been encrypted successfully.");
                    } else
                        addLog("no file has been picked...");
                    //EncryptorAesGcm.decryptFile(toFile, todecFile, AESKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkEnabelaty();
            }
        });

        decrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String fromFile = ""; // from resources folder
                String toFile = "";
                //String todecFile = File.separator + "home" + File.separator + "sophesrex" + File.separator + "Public" + File.separator + "readme.txt";
                try {
                    JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    j.setAcceptAllFileFilterUsed(false);
                    j.setDialogTitle("Select a .txt file");
                    FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .txt files", "txt");
                    j.addChoosableFileFilter(restrict);
                    int r = j.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        fromFile = j.getSelectedFile().getAbsolutePath();
                        String[] splitPath = fromFile.split("/");
                        if (splitPath[splitPath.length - 1].contains("Enc-")) {
                            toFile = workingDir + File.separator + "Dec-" + splitPath[splitPath.length - 1].split("-")[1];
                            EncryptorAesGcm.decryptFile(fromFile, toFile, plainAES);
                            addLog("file has been decrypted successfully.");
                        } else {
                            addLog("this file is not encrypted...");
                        }
                    } else
                        addLog("no file has been picked...");
                    //EncryptorAesGcm.decryptFile(toFile, todecFile, AESKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkEnabelaty();
            }
        });

        shoosSavingFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView());
                j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int r = j.showSaveDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    // set the label to the path of the selected directory
                    workingDir = j.getSelectedFile().getAbsolutePath();
                    database.updateDir(workingDir);
                    addLog("working dir : " + workingDir);
                } else {
                    addLog("the user cancelled the operation no directory selected");
                }
                checkEnabelaty();
            }
        });

        uploadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    j.setAcceptAllFileFilterUsed(false);
                    j.setDialogTitle("Select a .txt file");
                    FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .txt files", "txt");
                    j.addChoosableFileFilter(restrict);
                    int r = j.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        String filePath = j.getSelectedFile().getAbsolutePath();
                        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
                        String sha3_256 = ShaUtils.bytesToHex(ShaUtils.digest(fileContent));
                        String[] splitPath = filePath.split(File.separator);
                        String fileId = driveUtils.uploadFile(filePath);
                        api.check(splitPath[splitPath.length-1],sha3_256);
                        if (fileId.equals("")) {
                            addLog("Problem uploading try again...");
                        } else {
                            addLog("upload finished the file ID in the Drive : " + fileId);
                        }
                    } else
                        addLog("no file has been picked...");
                    //EncryptorAesGcm.decryptFile(toFile, todecFile, AESKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkEnabelaty();
            }
        });

        downloadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String ID = JOptionPane.showInputDialog("Provide file Number to download:");
                int number = -1;
                try {
                    if (filesArray == null) {
                        java.util.List<com.google.api.services.drive.model.File> files = driveUtils.showFiles();
                        if (files == null || files.isEmpty()) {
                            System.out.println("No files found.");
                            addLog("No files found");
                        } else {
                            System.out.println("Files:");
                            filesArray = new com.google.api.services.drive.model.File[files.size()];
                            int counter = 0;
                            for (com.google.api.services.drive.model.File file : files) {
                                filesArray[counter] = file;
                                //addLog(String.format("#%s-- %s (%s)\n",counter+1 ,file.getName(), file.getId()));
                            }
                        }
                    }
                    number = Integer.parseInt(ID);
                    driveUtils.downloadFile(filesArray[number - 1], workingDir);
                    addLog("Download completed Successfully.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "you need provide a valid number.");
                }
                checkEnabelaty();
            }
        });

        showFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                java.util.List<com.google.api.services.drive.model.File> files = driveUtils.showFiles();
                if (files == null || files.isEmpty()) {
                    System.out.println("No files found.");
                    addLog("No files found");
                } else {
                    System.out.println("Files:");
                    filesArray = new com.google.api.services.drive.model.File[files.size()];
                    int counter = 0;
                    for (com.google.api.services.drive.model.File file : files) {
                        filesArray[counter] = file;
                        addLog(String.format("#%s-- %s (%s)\n", counter + 1, file.getName(), file.getId()));
                        counter++;
                    }
                }
                checkEnabelaty();
            }
        });


        buttons.add(new JLabel("generate Aes Key :"));
        buttons.add(generateAESKey);
        buttons.add(new JLabel("generate ECC Keys :"));
        buttons.add(generateECCKeypair);
        buttons.add(new JLabel("encrypt file :"));
        buttons.add(encrypt);
        buttons.add(new JLabel("decrypt file :"));
        buttons.add(decrypt);
        buttons.add(new JLabel("upload the file :"));
        buttons.add(uploadFile);
        buttons.add(new JLabel("download a file :"));
        buttons.add(downloadFile);
        buttons.add(new JLabel("show my cloud files :"));
        buttons.add(showFiles);
        buttons.add(new JLabel("shoos saving folder :"));
        buttons.add(shoosSavingFolder);
        console.add(scrollBar);
        mainPanel.add(menuBar,BorderLayout.NORTH);
        downPanel.add(buttons);
        downPanel.add(console);
        mainPanel.add(downPanel,BorderLayout.SOUTH);
        this.add(mainPanel);

    }

    private void addLog(String log) {
        this.logs = logs + log + "\n";
        this.consoleArea.setText(this.logs);
    }

    private void checkEnabelaty() {
        if (workingDir.equals("")) {
            generateECCKeypair.setEnabled(false);
            generateAESKey.setEnabled(false);
            uploadFile.setEnabled(false);
            downloadFile.setEnabled(false);
            encrypt.setEnabled(false);
            decrypt.setEnabled(false);

            importECCKeys.setEnabled(false);
            importAES.setEnabled(false);
            exportAES.setEnabled(false);
            exportECCKeys.setEnabled(false);
            deleteKeys.setEnabled(false);
        } else {
            if (ECCkeys[0].equals("") || ECCkeys[1].equals("")) {
                importECCKeys.setEnabled(true);
                generateECCKeypair.setEnabled(true);
                importAES.setEnabled(false);
                exportAES.setEnabled(false);
                exportECCKeys.setEnabled(false);
                deleteKeys.setEnabled(false);
                generateAESKey.setEnabled(false);
                uploadFile.setEnabled(false);
                downloadFile.setEnabled(false);
                encrypt.setEnabled(false);
                decrypt.setEnabled(false);
            } else {
                if (AESKey.equals("")) {
                    importAES.setEnabled(true);
                    generateAESKey.setEnabled(true);
                    generateECCKeypair.setEnabled(false);
                    exportECCKeys.setEnabled(false);
                    importECCKeys.setEnabled(false);
                    exportAES.setEnabled(false);
                    deleteKeys.setEnabled(false);
                    uploadFile.setEnabled(false);
                    downloadFile.setEnabled(false);
                    encrypt.setEnabled(false);
                    decrypt.setEnabled(false);
                } else {
                    generateAESKey.setEnabled(false);
                    generateECCKeypair.setEnabled(false);
                    uploadFile.setEnabled(true);
                    downloadFile.setEnabled(true);
                    encrypt.setEnabled(true);
                    decrypt.setEnabled(true);
                    exportAES.setEnabled(true);
                    exportECCKeys.setEnabled(true);
                    importECCKeys.setEnabled(false);
                    importAES.setEnabled(false);
                    deleteKeys.setEnabled(true);
                }
            }

        }
    }


    private void initiateKeys() {
        boolean isPart1Exist = false;
        com.google.api.services.drive.model.File part1File = null;
        boolean isPart2Exist = false;
        com.google.api.services.drive.model.File part2File = null;
        try {
            java.util.List<com.google.api.services.drive.model.File> files = driveUtils.showFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                filesArray = new com.google.api.services.drive.model.File[files.size()];
                int counter = 0;
                for (com.google.api.services.drive.model.File file : files) {
                    filesArray[counter] = file;
                    if (file.getName().equals("part1.txt")) {
                        part1File = file;
                        isPart1Exist = true;
                    }
                    if (file.getName().equals("part2.txt")) {
                        part2File = file;
                        isPart2Exist = true;
                    }
                }
            }
            HashMap<String, String> info = database.selectAll();
            workingDir = info.get("dir");
            if (isPart1Exist && isPart2Exist) {
                //load part to parts hashmap
                driveUtils.downloadFile(part1File, workingDir);
                driveUtils.downloadFile(part2File, workingDir);
                byte[] part1Content = Files.readAllBytes(Paths.get(workingDir + File.separator + "part1.txt"));
                byte[] part2Content = Files.readAllBytes(Paths.get(workingDir + File.separator + "part2.txt"));
                AESKey = info.get("aes");
                parts.put(1, Base64.getDecoder().decode(info.get("part1")));
                parts.put(2, api.getEccPart());
                //parts.put(2,api.getEccPart().getBytes(StandardCharsets.UTF_8));
                parts.put(3, part1Content);
                parts.put(4, part2Content);
                ECCkeys[0] = info.get("pubkey");
                java.io.File part1ToDelete = new File(workingDir + File.separator + "part1.txt");
                java.io.File part2ToDelete = new File(workingDir + File.separator + "part2.txt");
                part1ToDelete.delete();
                part2ToDelete.delete();
                //decrypte the AESKey from the database;
                //use parts that stored in database and cloud and audit entity;
                if (!AESKey.equals("") &&
                        !parts.get(1).equals("") &&
                        !parts.get(1).equals("") &&
                        !parts.get(3).equals("") &&
                        !parts.get(4).equals("")) {
                    ECCkeys[1] = eccUtils.joinKey(parts);
                    plainAES = eccUtils.textDecrypt(AESKey, ECCkeys[1]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

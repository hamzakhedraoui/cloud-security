import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGui extends JFrame {
    private JTextField userName;
    private JLabel userLabel;
    private JPasswordField password;
    private JLabel passwordLable;
    private JButton login;
    private JPanel mainPanel;
    private JPanel downPanle;
    private ApiClient api = new ApiClient();
    LoginGui(){
        mainPanel = new JPanel();
        downPanle = new JPanel();
        this.setLayout(new BorderLayout());
        mainPanel.setLayout(new GridLayout(2,2,10,10));
        downPanle.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.setSize(300,120);
        userName = new JTextField();
        userLabel = new JLabel("User Name: ");
        password = new JPasswordField();
        passwordLable = new JLabel("Password");
        login = new JButton("login");
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String responce = api.connect(userName.getText(),password.getText());
                if(responce.equals("yes")){
                    dispose();
                    System.out.println("frame disposed");
                    GUI gui = new GUI();
                    gui.show(true);
                }
            }
        });
        mainPanel.add(userLabel);
        mainPanel.add(userName);
        mainPanel.add(passwordLable);
        mainPanel.add(password);
        downPanle.add(login,BorderLayout.CENTER);
        this.add(mainPanel,BorderLayout.NORTH);
        this.add(downPanle,BorderLayout.SOUTH);
    }
}

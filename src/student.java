import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import se.datadosen.component.RiverLayout;

public class student {
    Connection conn;

    JFrame frame;
    String frameTitle = "칵테일 데이터베이스 클라이언트";

    //텍스트 필드
    JTextField id;
    JTextField name;
    JTextField major;
    JTextField gender;
    JTextField grade;
    //(삭제, 수정, 추가, 저장)버튼
    JButton bDelete;
    JButton bUpdate;
    JButton bAdd;
    JButton bSave;
    //이름 리스트
    JList names = new JList();

    public static void main(String[] args) {
        student client = new student();
        client.setGUI();//데이터베이스 조작 GUI
        client.dbConnectionInit();//데이터베이스 연동
    }
    private void setGUI() {
        frame = new JFrame(frameTitle);

        JPanel leftTopPanel = new JPanel(new RiverLayout());

        JScrollPane cScroller = new JScrollPane(names);
        cScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        names.setVisibleRowCount(6);
        names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        names.setFixedCellWidth(100);
        leftTopPanel.add("br center", new JLabel("이 름"));
        leftTopPanel.add("p center", cScroller);

        id = new JTextField(20);
        name = new JTextField(20);
        major = new JTextField(20);
        gender = new JTextField(20);
        grade = new JTextField(20);

        JPanel rightTopPanel = new JPanel(new RiverLayout());

        rightTopPanel.add("br center", new JLabel("학 생 정 보"));
        rightTopPanel.add("p left", new JLabel("학 번"));
        rightTopPanel.add("tab", id);
        rightTopPanel.add("br", new JLabel("이 름"));
        rightTopPanel.add("tab", name);
        rightTopPanel.add("br", new JLabel("학 과 명"));
        rightTopPanel.add("tab", major);
        rightTopPanel.add("br", new JLabel("성 별"));
        rightTopPanel.add("tab", gender);
        rightTopPanel.add("br", new JLabel("성 적"));
        rightTopPanel.add("tab", grade);

        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.add(leftTopPanel);
        topPanel.add(rightTopPanel);

        bDelete = new JButton("삭제");
        bUpdate = new JButton("수정");
        bAdd = new JButton("추가");
        bSave = new JButton("저장");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(bDelete);
        bottomPanel.add(bUpdate);
        bottomPanel.add(bAdd);
        bottomPanel.add(bSave);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        names.addListSelectionListener(new NameListListener());
        bDelete.addActionListener(new ButtonListener());
        bUpdate.addActionListener(new ButtonListener());
        bAdd.addActionListener(new ButtonListener());
        bSave.addActionListener(new ButtonListener());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.setSize(700, 350);
        frame.setVisible(true);
    }
    //데이터베이스 연동
    private void dbConnectionInit() {
        try {
            //JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/student", "root", "mite");
            prepareList();
        }catch(ClassNotFoundException cnfe) {
            System.out.println("JDBC 드라이버 클래스를 찾을 수 없습니다: " + cnfe.getMessage());
        }catch(Exception ex) {
            System.out.println("DB 연결 에러: " + ex.getMessage());
        }
    }
    public void prepareList() {
        try {
            //Statement 객체를 만드는 메소드를 이용하여 테이블의 데이터를 읽어온다.
            Statement stmt = conn.createStatement();
            //select문을 실행하여 rs라는 이름의 배열에 저장한다.
            ResultSet rs = stmt.executeQuery("SELECT 이름 FROM student");
            Vector<String> list = new Vector<String>();
            while(rs.next()) {
                list.add(rs.getString("이름"));
            }
            stmt.close();
            Collections.sort(list);
            names.setListData(list);
            if(!list.isEmpty())
                names.setSelectedIndex(0);
        }catch(SQLException sqlex) {
            System.out.println("SQL 에러: " + sqlex.getMessage());
            sqlex.printStackTrace();
        }
    }
    public class NameListListener implements ListSelectionListener{
        @Override
        public void valueChanged(ListSelectionEvent lse) {
            if(!lse.getValueIsAdjusting() && !names.isSelectionEmpty()) {
                try {
                    Statement stmt = conn.createStatement();
                    //선택된 이름에 해당하는 데이터들을 텍스트필드에 저장
                    ResultSet rs = stmt.executeQuery("SELECT * FROM student WHERE 이름 = '" + (String)names.getSelectedValue() + "'");
                    rs.next();
                    id.setText(Double.toString(rs.getDouble("학번")));
                    name.setText(rs.getString("이름"));
                    major.setText(rs.getString("학과명"));
                    gender.setText(rs.getString("성별"));
                    grade.setText(Double.toString(rs.getDouble("성적")));
                    stmt.close();
                }catch(SQLException sqlex) {
                    System.out.println("SQL 애러 : " + sqlex.getMessage());
                    sqlex.printStackTrace();
                }catch(Exception ex) {
                    System.out.println("DB Handling 에러(리스트 리스너) : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
    public class ButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == bDelete) {
                try {
                    Statement stmt = conn.createStatement();
                    String sql = "delete from student where 이름=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, name.getText().trim());
                    ps.executeUpdate();
                    stmt.close();
                    prepareList();
                }catch(SQLException sqlex) {
                    System.out.println("SQL 에러 : " + sqlex.getMessage());
                    sqlex.printStackTrace();
                }catch (Exception ex) {
                    System.out.println("DB Handling 에러(DELETE 리스너) : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }else if(e.getSource() == bUpdate) {
                try {
                    Statement stmt = conn.createStatement();
                    String sql = "UPDATE student SET 학과명=?, 성별 =?, 성적=? where 이름 = '" + name.getText().trim() + "'";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, major.getText().trim());
                    ps.setString(2, gender.getText().trim());
                    ps.setString(3, grade.getText().trim());
                    ps.executeUpdate();
                    stmt.close();
                    prepareList();
                }catch(SQLException sqlex) {
                    System.out.println("SQL 에러 : " + sqlex.getMessage());
                    sqlex.printStackTrace();
                } catch (Exception ex) {
                    System.out.println("DB Handling 에러(DELETE 리스너) : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }else if(e.getSource() == bSave) {
                try {
                    Statement stmt = conn.createStatement();
                    String sql = "INSERT INTO student (학번, 이름, 학과명, 성별, 성적) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, id.getText().trim());
                    ps.setString(2, name.getText().trim());
                    ps.setString(3, major.getText().trim());
                    ps.setString(4, gender.getText().trim());
                    ps.setString(5, grade.getText().trim());

                    ps.executeUpdate();
                    ps.close();
                    stmt.close();
                    prepareList();
                } catch (SQLException sqlex) {
                    System.out.println("SQL 에러 : " + sqlex.getMessage());
                    sqlex.printStackTrace();
                } catch (Exception ex) {
                    System.out.println("DB Handling 에러(SAVE 리스너) : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }else if(e.getSource() == bAdd) {
                id.setText("");
                name.setText("");
                major.setText("");
                gender.setText("");
                grade.setText("");
                names.clearSelection();
            }
        }
    }
}

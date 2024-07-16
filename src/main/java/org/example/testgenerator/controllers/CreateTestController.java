package org.example.testgenerator.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class CreateTestController {

    private static Connection instance = null;

    private Properties properties;

    @FXML
    private ChoiceBox<String> choiceBox = new ChoiceBox<>();

    @FXML
    private TextField chaptersField;

    @FXML
    private TextField questionAmountField;

    @FXML
    private TextField testNameField;

    @FXML
    private TextField answerNameField;

    @FXML
    private TextField pathField;

    @FXML
    private Button create;

    @FXML
    private void createTest() {
        int questionAmount;
        String testName = testNameField.getText();
        String answerName = answerNameField.getText();
        String path = pathField.getText();
        String chapters = chaptersField.getText();
        String tableName = choiceBox.getValue();
        if(testName.equals("") || answerName.equals("") || path.equals("") || chapters.equals("") ||
                Objects.equals(tableName, null)) {
            showWarning("Eroare", "Eroare", "Sunt câmpuri necompletate");
            return;
        }
        try {
            questionAmount = Integer.parseInt(questionAmountField.getText());
        } catch (Exception e) {
            showWarning("Eroare", "Eroare", "Numărul de întrebări trebuie scris folosind cifre");
            return;
        }
        XWPFDocument testDocument = new XWPFDocument();
        XWPFDocument answersDocument = new XWPFDocument();
        Connection connection = getConnection();
        String sql = createQuery(tableName, chapters, questionAmount);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int questionNumber = 1;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String q = resultSet.getString("text");
                    String a = resultSet.getString("a");
                    String b = resultSet.getString("b");
                    String c = resultSet.getString("c");
                    String d = resultSet.getString("d");
                    String e = resultSet.getString("e");
                    String[] answers = {a,b,c,d,e};
                    String chapter = resultSet.getString("capitol");
                    Integer question = resultSet.getInt("nrintrebare");
                    String answer = resultSet.getString("raspunsuri");
                    XWPFParagraph testParagraph = testDocument.createParagraph();
                    XWPFRun run = testParagraph.createRun();
                    String rawText =questionNumber + ". " + q;
                    String text =  URLDecoder.decode(URLEncoder.encode(new String(rawText.getBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    run.setText(text);
                    for (String s : answers) {
                        testParagraph = testDocument.createParagraph();
                        run = testParagraph.createRun();
                        rawText = s;
                        text = URLDecoder.decode(URLEncoder.encode(new String(rawText.getBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                        run.setText(text);
                    }
                    testParagraph = testDocument.createParagraph();
                    run = testParagraph.createRun();
                    run.setText("\n");

                    XWPFParagraph answerParagraph = answersDocument.createParagraph();
                    run = answerParagraph.createRun();
                    rawText = questionNumber + ": Capitolul " + chapter + " - Întrebarea " + question + " - Răspuns: " + answer;
                    System.out.println(rawText);
                    text =  URLDecoder.decode(URLEncoder.encode(new String(rawText.getBytes(), StandardCharsets.UTF_8), "UTF-8"), "UTF-8");
                    run.setText(text);
                    questionNumber++;
                }
            }
        } catch (SQLException e) {
            showWarning("Eroare", "Eroare", "Eroare în accesarea bazei de date");
        } catch (UnsupportedEncodingException e) {
            showWarning("Eroare", "Eroare", "Eroare în (de)codarea caracterelor");
        }
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                showWarning("Eroare", "Eroare", "Crearea folderului a esuat");
                return;
            }
        }
        try (FileOutputStream out = new FileOutputStream(new File(path, testName+".docx"))) {
            testDocument.write(out);
            System.out.println("Test creat cu succes.");
        } catch (IOException e) {
            showWarning("Eroare", "Eroare", "Crearea testului a esuat");
            return;
        }

        try (FileOutputStream out = new FileOutputStream(new File(path, answerName+".docx"))) {
            answersDocument.write(out);
            System.out.println("Barem creat cu succes.");
        } catch (IOException e) {
            showWarning("Eroare", "Eroare", "Crearea baremului a esuat");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Succes");
        alert.setHeaderText("Succes");
        alert.setContentText("Crearea testului și a baremului a fost efectuată cu succes");
        alert.show();

    }

    private void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    private Connection getNewConnection() {
        String database = properties.getProperty("database");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        Connection connection;
        try {
            connection = DriverManager.getConnection(database, props);
            return connection;
        } catch (SQLException e) {
            System.out.println("Error getting connection " + e);
            return null;
        }
    }

    public Connection getConnection() {
        try {
            if (instance == null || instance.isClosed())
                instance = getNewConnection();
        } catch (SQLException e) {
            System.out.println("Error DB " + e);
        }
        return instance;
    }

    public String createQuery(String tableName,String chapters,int questionAmount) {
        StringBuilder sql = new StringBuilder("select * from ");
        boolean closeParantheses = false;
        sql.append(tableName);
        if(!Objects.equals(chapters, "toate")) {
            sql.append(" where capitol in (");
            ArrayList<String> chapterList = new ArrayList<>(Arrays.asList(chapters.split(",")));
            for (String chapter : chapterList) {
                ArrayList<String> exactChapters = new ArrayList<>(Arrays.asList(chapter.split("-")));
                if(chapter.contains("-")) {
                    if (Objects.equals(exactChapters.get(0), "")) {
                        for (int index = 1; index <= Integer.parseInt(exactChapters.get(1)); index++)
                            sql.append(index).append(",");
                    } else {
                        if (exactChapters.size() == 1) {
                            sql.deleteCharAt(sql.length() - 1);
                            sql.append(") or capitol >").append(exactChapters.get(0));
                            closeParantheses = true;
                        } else {
                            for (int index = Integer.parseInt(exactChapters.get(0)); index <= Integer.parseInt(exactChapters.get(1)); index++)
                                sql.append(index).append(",");
                        }
                    }
                }
                else {
                    sql.append(exactChapters.get(0)).append(",");
                }

            }
            if(!closeParantheses) {
                sql.deleteCharAt(sql.length() - 1);
                sql.append(")");
            }
        }
        sql.append(" order by random() limit ").append(questionAmount);
        return sql.toString();
    }

    public void setup(Properties properties) {
        this.properties = properties;
        Connection connection = getConnection();
        try (PreparedStatement statement = connection.prepareStatement("SELECT table_name\n" +
                "FROM information_schema.tables\n" +
                "WHERE table_schema='public'\n" +
                "  AND table_type='BASE TABLE'")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("table_name");
                    choiceBox.getItems().add(tableName);
                }
            }
        } catch (SQLException e) {
            return;
        }
    }

}

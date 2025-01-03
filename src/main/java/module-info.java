module org.example.java67 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.java67 to javafx.fxml;
    exports org.example.java67;
}
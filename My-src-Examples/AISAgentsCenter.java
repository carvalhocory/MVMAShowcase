
package aisconsole;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AISAgentsCenter //extends Application
{
    //Empty table and an observable list to hold the error data.
    private final TableView<errorEntry> errorTable = new TableView<>();
    private ObservableList<errorEntry> errorData;
    
    private static AISAgentsCenter ref = null;
    
    private static String[] errorDescriptions = new String[] {
        "This vessel's reported MMSI is not of a correct format.", 
        "This vessel's reported ETA is not a real date/time.",
        "This vessel's AIS message contains one or more @ characters.",
        "This vessel does not have a set destination.",
        "This vessel is not identified by a recognized type.",
        "This vessel is not identified by a name."};
    
    private static String[] priorityLevels = new String[] {
        "Low",
        "Medium",
        "High"};
    
    int selection = 0;
    
    private static String dbPath = "";
    
    private AISAgentsCenter() {
            
    }
    
    public static synchronized AISAgentsCenter getAISAgentsCenter() {
        if (ref == null) 
            ref = new AISAgentsCenter();
        return ref;
    }
 
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
 
    public void setDBFile(String dbPath) {
        this.dbPath = dbPath;
    }
    
    //@Override
    public Scene createScene() {
        
        Scene scene = new Scene(new Group());
                
        //Label above the tabPane containing the TableViews.
        Label label = new Label(" Agents Center");
        label.setFont(new Font("Open Sans", 20));
        
        //This VBox will hold both the label and tabPane.
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(0, 0, 0, 0));
        
        //MMSI for the vessel the error came from.
        TableColumn MMSI = new TableColumn("MMSI");
        MMSI.setMinWidth(100);
        MMSI.setCellValueFactory(new PropertyValueFactory<>("MMSI"));

        //Priority level of the given error.
        TableColumn Priority = new TableColumn("PRIORITY");
        Priority.setMinWidth(150);
        Priority.setCellValueFactory(new PropertyValueFactory<>("Priority"));
        
        //Description of the error.
        TableColumn Description = new TableColumn("Description");
        Description.setMinWidth(450);
        Description.setCellValueFactory(new PropertyValueFactory<>("Description"));
        
        //Time of the message that the error was stored from.
        TableColumn DTG = new TableColumn("DTG");
        DTG.setMinWidth(150);
        DTG.setCellValueFactory(new PropertyValueFactory<>("DTG"));
        
        //Columns are added to each tab with their respective data types.
        errorTable.getColumns().addAll(MMSI, Priority, Description, DTG);
        
        //The content within the characteristics tab.
        BorderPane errorContent = new BorderPane();
        //Vbox holds text field, search options and search button.
        HBox errorStatus = new HBox();
        //Set spacing.
        errorStatus.setSpacing(5);
        errorStatus.setPadding(new Insets(10, 0, 0, 10));
        
        ChoiceBox cb = new ChoiceBox(FXCollections.observableArrayList(
        "All", "MMSI", "ETA", "@", "Destination", "Type", "Name"));
        
        cb.getSelectionModel().selectFirst();
        
        cb.setTooltip(new Tooltip("Changes the focus of the search."));

        //Listener to be used to tell the SQL statements what to search for.
        cb.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> ov, 
                Number old_val, Number new_val) -> {
                selection = new_val.intValue();
                System.out.println(selection);
            });
        
        //Refresh
        Button refresh = new Button("Refresh");
        refresh.setOnAction((ActionEvent e) -> {
            final Task<Void> error = errorUpdate(selection);
            new Thread(error).start();});
        
        //Holds the characteristics table.
        VBox errorTableHolder = new VBox();
        errorTableHolder.setPadding(new Insets(10, 0, 0, 0));

        //Adds the buttons, searchbox, dropdown to respective VBox/HBox
        errorStatus.getChildren().addAll(refresh, cb);
        errorTableHolder.getChildren().addAll(errorTable);
        
        //Search box on top, table below it.
        errorContent.setCenter(errorStatus);
        errorContent.setBottom(errorTableHolder);
        
        errorContent.setStyle("-fx-background-color: darkslategray");
        vbox.getChildren().addAll(label, errorContent);
        scene.getStylesheets().add
        (AISInterface.class.getResource("AISLook.css").toExternalForm());

        ((Group) scene.getRoot()).getChildren().addAll(vbox);
       
        //errorData = FXCollections.observableArrayList(errorRefresh());
        //errorTable.setItems(errorData);
 
        return scene;
        }
    
    public Task<Void> errorUpdate(int selection) {
    return new Task<Void>(){
        @Override
        protected Void call() throws Exception {
                errorData = FXCollections.observableArrayList(errorRefresh(selection));
                errorTable.setItems(errorData);
                return null;
            }
        };
    }     
    
    public ArrayList errorRefresh(int selection) {
        Connection eSearch;
        eSearch = null;
        Statement eStmt;
        eStmt = null;
        ArrayList<errorEntry> errorTemp = new ArrayList<>();
        setDBFile(dbPath);
        try {
            Class.forName("org.sqlite.JDBC");
            eSearch = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            eSearch.setAutoCommit(false);
            //System.out.println("Opened database successfully");
            
            eStmt = eSearch.createStatement();
            ResultSet rs = eStmt.executeQuery("SELECT * FROM ERRORS;");
            if(selection == 0) {
            while ( rs.next() ) {
                int MMSI = rs.getInt("MMSI");
                int Priority = rs.getInt("PRIORITY");
                int Description = rs.getInt("Description");
                int DTG = rs.getInt("DTG");
                
                errorEntry newEntry = new errorEntry(MMSI, priorityLevels[Priority-1], errorDescriptions[Description-1], DTG);
                errorTemp.add(newEntry);
                }
            } else {
                rs = eStmt.executeQuery("SELECT * FROM ERRORS WHERE DESCRIPTION" + "='" + selection + "';");
                while ( rs.next() ) {
                int MMSI = rs.getInt("MMSI");
                int Priority = rs.getInt("PRIORITY");
                int Description = rs.getInt("DESCRIPTION");
                int DTG = rs.getInt("DTG");
                
                errorEntry newEntry = new errorEntry(MMSI, priorityLevels[Priority-1], errorDescriptions[Description-1], DTG);
                errorTemp.add(newEntry);
                }
            }
        rs.close();
        eStmt.close();
        eSearch.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
            System.out.println("Operation done successfully");
            return errorTemp;
    }
    
    public class errorEntry {
        private final SimpleIntegerProperty MMSI;
        private final SimpleStringProperty Priority;
        private final SimpleStringProperty Description;
        private final SimpleIntegerProperty DTG;
        
        private errorEntry(int M, String P, String D, int DT) {
            this.MMSI = new SimpleIntegerProperty(M);
            this.Priority = new SimpleStringProperty(P);
            this.Description = new SimpleStringProperty(D);
            this.DTG = new SimpleIntegerProperty(DT);
            }
        
        public int getMMSI() {
            return MMSI.get();
        }
        
        public void setMMSI(int M) {
            MMSI.set(M);
        }
        
        public String getPriority() {
            return Priority.get();
        }
        
        public void setPriority(String P) {
            Priority.set(P);
        }
        
        public String getDescription() {
            return Description.get();
        }
        
        public void setDescription(String D) {
            Description.set(D);
        }
        
        public int getDTG() {
            return DTG.get();
        }
        
        public void setDTG(int DT) {
            DTG.set(DT);
        }    
    }
}

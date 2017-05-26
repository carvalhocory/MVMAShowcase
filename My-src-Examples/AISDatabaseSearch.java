
package aisconsole;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class AISDatabaseSearch //extends Application
{
    private static AISDatabaseSearch ref = null;

    //Empty tables and Observable Lists to hold data for them.
    private final TableView<characteristicsEntry> characteristicsTable = new TableView<>();
    private ObservableList<characteristicsEntry> characteristicsData;
    
    private final TableView<positionEntry> positionTable = new TableView<>();
    private ObservableList<positionEntry> positionData;
    
    private final TableView<kinematicsEntry> kinematicsTable = new TableView<>();
    private ObservableList<kinematicsEntry> kinematicsData;

    //For the "concentrated search" dropdown menus.
    private static int characteristicsSelect = 0;
    private static int positionSelect = 0;
    private static int kinematicsSelect = 0;
    
    private static final String[] types = new String[] {
        "Ship Name", "Ship Type", "Callsign", "IMO Number", "Dimensions",
        "ETA", "Destination", "Draught"};
    
    private static final String[] sources = new String[] {"Log"};
    
    private AISDatabaseSearch() {
        
    }
    
    public static synchronized AISDatabaseSearch getAISDatabaseSearch() 
    {
        if (ref == null)
            ref = new AISDatabaseSearch();
        return ref;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
 
    private static String dbPath = "";
    
    public void setDBFile(String dbPath) {
        this.dbPath = dbPath;
    }
    
    //@Override
    public Scene createScene() {
        
        Scene scene = new Scene(new Group());
                
        //Label above the tabPane containing the TableViews.
        Label label = new Label(" AIS Database");
        label.setFont(new Font("Open Sans", 20));
        
        //This VBox will hold both the label and tabPane.
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(0, 0, 0, 0));
        
        //Search status labels.
        Label characteristicsStatus = new Label("");
        Label positionStatus = new Label("");
        Label kinematicsStatus = new Label("");
    
        
        //Styling for search status to match background.
        characteristicsStatus.setFont(new Font("Open Sans", 16));
        positionStatus.setFont(new Font("Open Sans", 16));
        kinematicsStatus.setFont(new Font("Open Sans", 16));
        
        characteristicsStatus.setTextFill(Color.web("white"));
        positionStatus.setTextFill(Color.web("white"));
        kinematicsStatus.setTextFill(Color.web("white"));

        //Table Columns below were initially shared until I ran in to
        //bugs displaying values that had their own rows across each,
        //such as MMSI and DTG, and are now specific to each table due to that.*/
        
        //Characteristics columns and values.
        TableColumn cMMSI = new TableColumn("MMSI");
        cMMSI.setMinWidth(150);
        cMMSI.setCellValueFactory(new PropertyValueFactory<>("MMSI"));
        
        TableColumn Type = new TableColumn("Type");
        Type.setMinWidth(150);
        Type.setCellValueFactory(new PropertyValueFactory<>("Type"));
        
        TableColumn cSource = new TableColumn("Source");
        cSource.setMinWidth(150);
        cSource.setCellValueFactory(new PropertyValueFactory<>("Source"));

        TableColumn Data = new TableColumn("DATA");
        Data.setMinWidth(150);
        Data.setCellValueFactory(new PropertyValueFactory<>("Data"));

        TableColumn DData = new TableColumn("DDATA");
        DData.setMinWidth(150);
        DData.setCellValueFactory(new PropertyValueFactory<>("DData"));

        TableColumn cDTG = new TableColumn("DTG");
        cDTG.setMinWidth(150);
        cDTG.setCellValueFactory(new PropertyValueFactory<>("DTG"));
        
        //Position columns and values.
        TableColumn pMMSI = new TableColumn("MMSI");
        pMMSI.setMinWidth(150);
        pMMSI.setCellValueFactory(new PropertyValueFactory<>("MMSI"));
        
        TableColumn pSource = new TableColumn("Source");
        pSource.setMinWidth(150);
        pSource.setCellValueFactory(new PropertyValueFactory<>("Source"));
        
        TableColumn Latitude = new TableColumn("Latitude");
        Latitude.setMinWidth(150);
        Latitude.setCellValueFactory(new PropertyValueFactory<>("Latitude"));

        TableColumn Longitude = new TableColumn("Longitude");
        Longitude.setMinWidth(150);
        Longitude.setCellValueFactory(new PropertyValueFactory<>("Longitude"));

        TableColumn pDTG = new TableColumn("DTG");
        pDTG.setMinWidth(150);
        pDTG.setCellValueFactory(new PropertyValueFactory<>("DTG"));
        
        TableColumn storeDate = new TableColumn("Storedate");
        storeDate.setMinWidth(150);
        storeDate.setCellValueFactory(new PropertyValueFactory<>("Storedate"));
        
        //Kinematics columns and values.
        TableColumn kMMSI = new TableColumn("MMSI");
        kMMSI.setMinWidth(150);
        kMMSI.setCellValueFactory(new PropertyValueFactory<>("MMSI"));
        
        TableColumn Course = new TableColumn("Course");
        Course.setMinWidth(150);
        Course.setCellValueFactory(new PropertyValueFactory<>("Course"));
        
        TableColumn Speed = new TableColumn("Speed");
        Speed.setMinWidth(150);
        Speed.setCellValueFactory(new PropertyValueFactory<>("Speed"));
        
        TableColumn Heading = new TableColumn("Heading");
        Heading.setMinWidth(150);
        Heading.setCellValueFactory(new PropertyValueFactory<>("Heading"));
        
        TableColumn Turnrate = new TableColumn("Turnrate");
        Turnrate.setMinWidth(150);
        Turnrate.setCellValueFactory(new PropertyValueFactory<>("Turnrate"));
        
        TableColumn kDTG = new TableColumn("DTG");
        kDTG.setMinWidth(150);
        kDTG.setCellValueFactory(new PropertyValueFactory<>("DTG"));
        
        //Columns are added to each tab with their respective data types.
        characteristicsTable.getColumns().addAll(cMMSI, Type, cSource, Data, DData, cDTG);
        positionTable.getColumns().addAll(pMMSI, pSource, Latitude, Longitude, pDTG, storeDate);
        kinematicsTable.getColumns().addAll(kMMSI, Course, Speed, Heading, Turnrate, kDTG);
        
        //tabPane is held by the tabHolder.
        BorderPane tabHolder = new BorderPane();
        
        //Create a TabPane to hold our tables in a more organized manner, they 
        //are set to not have the ability to be closed as to not break the program.
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        //The content within the characteristics tab.
        BorderPane characteristicsContent = new BorderPane();
        //Vbox holds text field, search options and search button.
        HBox characteristicsSearch = new HBox();
        //Set spacing.
        characteristicsSearch.setSpacing(5);
        characteristicsSearch.setPadding(new Insets(10, 0, 0, 10));
        //Text field for search content.
        TextField characteristicsField = new TextField();
        characteristicsField.setMaxWidth(150);
        characteristicsField.setPromptText("Enter Search Criteria");
        //Dropdown menu for changing the focus of the search.
        ChoiceBox cb = new ChoiceBox(FXCollections.observableArrayList(
        "MMSI", "Type", "Source", "Data", "DData", "DTG"));
        
        cb.getSelectionModel().selectFirst();
        
        cb.setTooltip(new Tooltip("Changes the focus of the search."));
        
        String[] characteristicsOptions = new String[] {
        "MMSI", "TYPE", "SOURCE", "DATA", "DDATA", "DTG"};

        //Listener to be used to tell the SQL statements what to search for.
        cb.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> ov, 
                Number old_val, Number new_val) -> {
                //System.out.println(characteristicsOptions[new_val.intValue()]);
                characteristicsSelect = new_val.intValue();
            });
        
        //Submit button.
        Button characteristicsSubmit = new Button("Search");
        characteristicsSubmit.setOnAction((ActionEvent e) -> {
            String option = characteristicsOptions[characteristicsSelect];
            String field = characteristicsField.getText();
            characteristicsStatus.setText("Searching...");
            final Task<Void> searchDB = SearchDB(option, field, "characteristics", 1);
            searchDB.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    characteristicsStatus.setText("Search Complete!");
                }
            });
            new Thread(searchDB).start();        });
        
        //Translate button.
        Button characteristicsTranslated = new Button("Translate");
        characteristicsTranslated.setOnAction((ActionEvent e) -> {
            String option = characteristicsOptions[characteristicsSelect];
            String field = characteristicsField.getText();
            characteristicsStatus.setText("Translating...");
            final Task<Void> searchDB = SearchDB(option, field, "characteristics", 2);
            searchDB.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    characteristicsStatus.setText("Translation Complete!");
                }
            });
            new Thread(searchDB).start();        });
        
        //Holds the characteristics table.
        VBox characteristicsTableHolder = new VBox();
        characteristicsTableHolder.setPadding(new Insets(10, 0, 0, 0));

        //Adds the buttons, searchbox, dropdown to respective VBox/HBox
        characteristicsSearch.getChildren().addAll(characteristicsField, cb, characteristicsSubmit, characteristicsTranslated, characteristicsStatus);
        characteristicsTableHolder.getChildren().addAll(characteristicsTable);
        
        //Search box on top, table below it.
        characteristicsContent.setCenter(characteristicsSearch);
        characteristicsContent.setBottom(characteristicsTableHolder);
        
        //Create characteristics tab.
        Tab characteristicsTab = new Tab();
        characteristicsTab.setText("Characteristics");
        characteristicsTab.setContent(characteristicsContent);
        
        //The content within the position tab.
        BorderPane positionContent = new BorderPane();
        //Vbox holds text field, search options and search button.
        HBox positionSearch = new HBox();
        //Set spacing.
        positionSearch.setSpacing(5);
        positionSearch.setPadding(new Insets(10, 0, 0, 10));
        //Text field for search content.
        TextField positionField = new TextField();
        positionField.setMaxWidth(150);
        positionField.setPromptText("Enter Search Criteria");
        //Dropdown menu for changing the focus of the search.
        ChoiceBox pb = new ChoiceBox(FXCollections.observableArrayList(
        "MMSI", "Source", "Latitude", "Longitude", "DTG", "Store Date"));
        
        pb.getSelectionModel().selectFirst();
       
        pb.setTooltip(new Tooltip("Changes the focus of the search."));
        
        String[] positionOptions = new String[] {
        "MMSI", "SOURCE", "LATITUDE", "LONGITUDE", "DTG", "STORE_DATE"};
        
        //Listener to be used to tell the SQL statements what to search for.
        pb.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> ov, 
                Number old_val, Number new_val) -> {
                //System.out.println(positionOptions[new_val.intValue()]);
                positionSelect = new_val.intValue();
            });
        
        //Submit button.
        Button positionSubmit = new Button("Search");
        positionSubmit.setOnAction((ActionEvent e) -> {
            String option = positionOptions[positionSelect];
            String field = positionField.getText();
            positionStatus.setText("Searching...");
            final Task<Void> searchDB = SearchDB(option, field, "position", 1);
            searchDB.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    positionStatus.setText("Search Complete!");
                }
            });
            new Thread(searchDB).start();
        });
        
        //Holds the position table.
        VBox positionTableHolder = new VBox();
        positionTableHolder.setPadding(new Insets(10, 0, 0, 0));

        //Adds the buttons, searchbox, dropdown to respective VBox/HBox
        positionSearch.getChildren().addAll(positionField, pb, positionSubmit, positionStatus);
        positionTableHolder.getChildren().addAll(positionTable);
        
        //Search box on top, table below it.
        positionContent.setCenter(positionSearch);
        positionContent.setBottom(positionTableHolder);
        
        //Create position tab.
        Tab positionTab = new Tab();
        positionTab.setText("Position");
        positionTab.setContent(positionContent);
        
        //The content within the kinematics tab.
        BorderPane kinematicsContent = new BorderPane();
        //Vbox holds text field, search options and search button.
        HBox kinematicsSearch = new HBox();
        //Set spacing.
        kinematicsSearch.setSpacing(5);
        kinematicsSearch.setPadding(new Insets(10, 0, 0, 10));
        //Text field for search content.
        TextField kinematicsField = new TextField();
        kinematicsField.setMaxWidth(150);
        kinematicsField.setPromptText("Enter Search Criteria");
        //Dropdown menu for changing the focus of the search.
        ChoiceBox kb = new ChoiceBox(FXCollections.observableArrayList(
        "MMSI", "Course", "Speed", "Heading", "Turnrate", "DTG"));
        
        kb.getSelectionModel().selectFirst();
        
        kb.setTooltip(new Tooltip("Changes the focus of the search."));
        
        String[] kinematicsOptions = new String[] {
        "MMSI", "COURSE", "SPEED", "HEADING", "TURNRATE", "DTG"};

        //Listener to be used to tell the SQL statements what to search for.
        kb.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> ov, 
                Number old_val, Number new_val) -> {
                //System.out.println(kinematicsOptions[new_val.intValue()]);
                kinematicsSelect = new_val.intValue();
            });
        
        //Submit button.
        Button kinematicsSubmit = new Button("Search");
        kinematicsSubmit.setOnAction((ActionEvent e) -> {
            String option = kinematicsOptions[kinematicsSelect];
            String field = kinematicsField.getText();
            kinematicsStatus.setText("Searching...");
            final Task<Void> searchDB = SearchDB(option, field, "kinematics", 1);
            searchDB.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    kinematicsStatus.setText("Search Complete!");
                }
            });
            new Thread(searchDB).start();
        });
        //Holds the kinematics table.
        VBox kinematicsTableHolder = new VBox();
        kinematicsTableHolder.setPadding(new Insets(10, 0, 0, 0));

        //Adds the buttons, searchbox, dropdown to respective VBox/HBox
        kinematicsSearch.getChildren().addAll(kinematicsField, kb, kinematicsSubmit, kinematicsStatus);
        kinematicsTableHolder.getChildren().addAll(kinematicsTable);
        
        //Search box on top, table below it.
        kinematicsContent.setCenter(kinematicsSearch);
        kinematicsContent.setBottom(kinematicsTableHolder);
        
        //Create kinematics tab.
        Tab kinematicsTab = new Tab();
        kinematicsTab.setText("Kinematics");
        kinematicsTab.setContent(kinematicsContent);
        
        //Add all the created tabs to the tabPane.
        tabPane.getTabs().addAll(characteristicsTab, positionTab, kinematicsTab);
        
        tabHolder.setCenter(tabPane);
        tabHolder.setStyle("-fx-background-color: darkslategray");
        vbox.getChildren().addAll(label, tabHolder);
        scene.getStylesheets().add
        (AISInterface.class.getResource("AISLook.css").toExternalForm());

       ((Group) scene.getRoot()).getChildren().addAll(vbox);
 
       //stage.setScene(scene);
       //stage.show();
        return scene;
    }
    
    public Task<Void> SearchDB(String option, String field, String type, int o) {
    return new Task<Void>(){
        @Override
        protected Void call() throws Exception {
                switch (type) {
                    case "characteristics":
                        characteristicsData = FXCollections.observableArrayList(characteristicsSearch(option, field, o));
                        characteristicsTable.setItems(characteristicsData);
                        break;
                    case "position":
                        positionData = FXCollections.observableArrayList(positionSearch(option, field));
                        positionTable.setItems(positionData);
                        break;
                    case "kinematics":
                        kinematicsData = FXCollections.observableArrayList(kinematicsSearch(option, field));
                        kinematicsTable.setItems(kinematicsData);
                        break;
                }            
                return null;
            }
        };
    }     
 
    public ArrayList characteristicsSearch(String option, String search, int o)
    {
        Connection cSearch;
        cSearch = null;
        Statement pStmt;
        pStmt = null;
        ArrayList<characteristicsEntry> characteristicsTemp = new ArrayList<>();
        ArrayList<characteristicsEntry> translatedTemp = new ArrayList<>();
        setDBFile(dbPath);
        try 
        {
            Class.forName("org.sqlite.JDBC");
            cSearch = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            cSearch.setAutoCommit(false);
            //System.out.println("Opened database successfully");
            
            pStmt = cSearch.createStatement();
            ResultSet rs = pStmt.executeQuery(
            "SELECT * FROM CHARACTERISTICS WHERE " + option + "='" + search + "';");
            while ( rs.next() ) 
            {
                int MMSI = rs.getInt("MMSI");
                int Type = rs.getInt("TYPE");
                int Source  = rs.getInt("SOURCE");
                String  Data = rs.getString("DATA");
                int DData = rs.getInt("DDATA");
                int DTG = rs.getInt("DTG");
                
                //Fill with regualar data or translation based on selection.
                if(o == 1) {
                characteristicsEntry newEntry = new characteristicsEntry(
                        MMSI, Integer.toString(Type), Integer.toString(Source), 
                        Data, Integer.toString(DData), DTG);
                characteristicsTemp.add(newEntry);
                } else {
                characteristicsEntry tEntry = new characteristicsEntry(
                        MMSI, types[Type-1], sources[Source-1], 
                        Data, Integer.toString(DData), DTG);
                if(Type == 2)
                {
                    tEntry.setDData(AISContact.getType(DData));
                }
                translatedTemp.add(tEntry);
                }
            }
        rs.close();
        pStmt.close();
        cSearch.close();
        } catch ( Exception e ) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
            System.out.println("Operation done successfully");
            if(o == 1) {
            return characteristicsTemp;
            } else {
            return translatedTemp;    
            }
    }
    
    public ArrayList positionSearch(String option, String search)
    {
        Connection pSearch;
        pSearch = null;
        Statement pStmt;
        pStmt = null;
        ArrayList<positionEntry> positionTemp = new ArrayList<>();
        setDBFile(dbPath);
        try 
        {
            Class.forName("org.sqlite.JDBC");
            pSearch = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            pSearch.setAutoCommit(false);
            //System.out.println("Opened database successfully");

            pStmt = pSearch.createStatement();
            ResultSet rs = pStmt.executeQuery(
            "SELECT * FROM POSITION WHERE " + option + "='" + search + "';");
            while ( rs.next() ) 
            {
                int MMSI = rs.getInt("MMSI");
                int Source  = rs.getInt("SOURCE");
                double Latitude = rs.getDouble("LATITUDE");
                double Longitude = rs.getDouble("LONGITUDE");
                int DTG = rs.getInt("DTG");
                int Storedate = rs.getInt("STORE_DATE");
                
                positionEntry newEntry = new positionEntry(
                        MMSI, Source, Latitude, Longitude, DTG, Storedate);
                positionTemp.add(newEntry);
            }
        rs.close();
        pStmt.close();
        pSearch.close();
        } catch ( Exception e ) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
            System.out.println("Operation done successfully");
            return positionTemp;
    }
    
    public ArrayList kinematicsSearch(String option, String search)
    {
        Connection kSearch;
        kSearch = null;
        Statement kStmt;
        kStmt = null;
        ArrayList<kinematicsEntry> kinematicsTemp = new ArrayList<>();
        setDBFile(dbPath);
        try 
        {
            Class.forName("org.sqlite.JDBC");
            kSearch = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            kSearch.setAutoCommit(false);
            //System.out.println("Opened database successfully");

            kStmt = kSearch.createStatement();
            ResultSet rs = kStmt.executeQuery(
            "SELECT * FROM KINEMATICS WHERE " + option + "='" + search + "';");
            while ( rs.next() ) 
            {
                int MMSI = rs.getInt("MMSI");
                int Course  = rs.getInt("COURSE");
                int Speed = rs.getInt("SPEED");
                int Heading = rs.getInt("HEADING");
                int Turnrate = rs.getInt("TURNRATE");
                int DTG = rs.getInt("DTG");
                
                kinematicsEntry newEntry = new kinematicsEntry(
                        MMSI, Course, Speed, Heading, Turnrate, DTG);
                kinematicsTemp.add(newEntry);
            }
        rs.close();
        kStmt.close();
        kSearch.close();
        } catch ( Exception e ) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
            System.out.println("Operation done successfully");
            return kinematicsTemp;
    }
    
    public boolean isStored(int id)
    {
        Connection cSearch;
        cSearch = null;
        Statement pStmt;
        pStmt = null;
        boolean stored = false;
        setDBFile(dbPath);
        try 
        {
            Class.forName("org.sqlite.JDBC");
            cSearch = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            cSearch.setAutoCommit(false);
            //System.out.println("Opened database successfully");
            
            pStmt = cSearch.createStatement();
            ResultSet rs = pStmt.executeQuery(
            "SELECT * FROM CHARACTERISTICS WHERE MMSI" + "='" + id + "';");
            while ( rs.next() ) 
            {
                stored = true;
                //int MMSI = rs.getInt("MMSI");
            }
        rs.close();
        pStmt.close();
        cSearch.close();
        } catch ( Exception e ) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
            return stored;
    }

    public class characteristicsEntry
    {
        private final SimpleIntegerProperty MMSI;
        private final SimpleStringProperty Type;
        private final SimpleStringProperty Source;
        private final SimpleStringProperty Data;
        private final SimpleStringProperty DData;
        private final SimpleIntegerProperty DTG;
        
        private characteristicsEntry(
                int M, String T, String S, String D, String DD, int DT) 
            {
            this.MMSI = new SimpleIntegerProperty(M);
            this.Type = new SimpleStringProperty(T);
            this.Source = new SimpleStringProperty(S);
            this.Data = new SimpleStringProperty(D);
            this.DData = new SimpleStringProperty(DD);
            this.DTG = new SimpleIntegerProperty(DT);
            }
        
        public int getMMSI() {
            return MMSI.get();
        }
        
        public void setMMSI(int M) {
            MMSI.set(M);
        }
        
        public String getType() {
            return Type.get();
        }
        
        public void setType(String T) {
            Type.set(T);
        }
        
        public String getSource() {
            return Source.get();
        }
        
        public void setSource(String S) {
            Source.set(S);
        }
        
        public String getData() {
            return Data.get();
        }
        
        public void setData(String D) {
           Data.set(D);
        }
        
        public String getDData() {
            return DData.get();
        }
        
        public void setDData(String DD) {
            DData.set(DD);
        }
        
        public int getDTG() {
            return DTG.get();
        }
        
        public void setDTG(int DT) {
            DTG.set(DT);
        }
    }
    
    public class positionEntry
    {
        private final SimpleIntegerProperty MMSI;
        private final SimpleIntegerProperty Source;
        private final SimpleDoubleProperty Latitude;
        private final SimpleDoubleProperty Longitude;
        private final SimpleIntegerProperty DTG;
        private final SimpleIntegerProperty Storedate;
        
        private positionEntry(
                int M, int S, double LT, double LG, int DT, int SD) 
            {
            this.MMSI = new SimpleIntegerProperty(M);
            this.Source = new SimpleIntegerProperty(S);
            this.Latitude = new SimpleDoubleProperty(LT);
            this.Longitude = new SimpleDoubleProperty(LG);
            this.DTG = new SimpleIntegerProperty(DT);
            this.Storedate = new SimpleIntegerProperty(SD);
            }
        
        public int getMMSI() {
            return MMSI.get();
        }
        
        public void setMMSI(int M) {
            MMSI.set(M);
        }
        
        public int getSource() {
            return Source.get();
        }
        
        public void setSource(int S) {
            Source.set(S);
        }
        
        public double getLatitude() {
            return Latitude.get();
        }
        
        public void Latitude(double LT) {
           Latitude.set(LT);
        }
        
        public double getLongitude() {
            return Longitude.get();
        }
        
        public void setLongitude(double LG) {
            Latitude.set(LG);
        }
        
        public int getDTG() {
            return DTG.get();
        }
        
        public void setDTG(int DT) {
            DTG.set(DT);
        }
        
        public int getStoredate() {
            return Storedate.get();
        }
        
        public void setStoredate(int SD) {
            Storedate.set(SD);
        }
    }
    
    public class kinematicsEntry
    {
        private final SimpleIntegerProperty MMSI;
        private final SimpleIntegerProperty Course;
        private final SimpleIntegerProperty Speed;
        private final SimpleIntegerProperty Heading;
        private final SimpleIntegerProperty Turnrate;
        private final SimpleIntegerProperty DTG;
        
        private kinematicsEntry(
                int M, int C, int S, int H, int TR, int DT) 
            {
            this.MMSI = new SimpleIntegerProperty(M);
            this.Course = new SimpleIntegerProperty(C);
            this.Speed = new SimpleIntegerProperty(S);
            this.Heading = new SimpleIntegerProperty(H);
            this.Turnrate = new SimpleIntegerProperty(TR);
            this.DTG = new SimpleIntegerProperty(DT);
            }
        
        public int getMMSI() {
            return MMSI.get();
        }
        
        public void setMMSI(int M) {
            MMSI.set(M);
        }
        
        public int getCourse() {
            return Course.get();
        }
        
        public void setCourse(int C) {
            Course.set(C);
        }
        
        public int getSpeed() {
            return Speed.get();
        }
        
        public void Latitude(int S) {
           Speed.set(S);
        }
        
        public int getHeading() {
            return Heading.get();
        }
        
        public void setHeading(int H) {
            Heading.set(H);
        }
        
        public int getTurnrate() {
            return Turnrate.get();
        }
        
        public void setTurnrate(int TR) {
            Turnrate.set(TR);
        }
        
        public int getDTG() {
            return DTG.get();
        }
        
        public void setDTG(int DT) {
            DTG.set(DT);
        }    
    }
}


package aisconsole;

import java.sql.*;
import nl.esi.metis.aisparser.AISMessage;
import nl.esi.metis.aisparser.AISMessage05;
import nl.esi.metis.aisparser.AISMessagePositionReport;
import nl.esi.metis.aisparser.UtilsDimensions30;
import nl.esi.metis.aisparser.UtilsEta;

public class AISDatabase {
    private static Connection c;
    private static Statement stmt;
    private static AISDatabase ref = null;
    private static int characteristicsCount;
    private static int characteristicsDelay;
    private static int typeCount;
    private static int sourceID;
    private static String dbPath = "";
    private static boolean loaded = false;
    
    private AISDatabase() 
    {
        c = null;
        stmt = null;
        characteristicsCount = 0;
        typeCount = 0;
        sourceID = 1;
        characteristicsDelay = 2000;
    }
    
    public static AISDatabase getAISDatabase() 
    {
        if (ref == null)
            ref = new AISDatabase();
        return ref;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException 
    {
        throw new CloneNotSupportedException();
    }
    
    public void setDBFile(String dbPath) {
        this.dbPath = dbPath;
    }
    
    public int openDatabase()
    {
        
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            c.setAutoCommit(false);
            loaded = true;
            System.out.println("sjdfl;asfk");
        } catch ( ClassNotFoundException | SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return 0;
        }
        return 1;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void closeDB() 
    {
        System.out.print("Closing database - ");
        try {
            c.commit();
            stmt.close();
            c.close();
            System.out.println("Successful.");
        } catch (Exception e)
        {

        }
    }
    
    public void initialDatabase()
    {
      try {
        stmt = c.createStatement();

        String sql = "CREATE TABLE CTYPES " +
                     "(ID INT PRIMARY KEY     NOT NULL," +
                     " TYPE           TEXT    NOT NULL)"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (1, 'Ship Name');"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (2, 'Ship Type');"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (3, 'CallSign');"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (4, 'IMO Number');"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (5, 'Dimensions');"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (6, 'ETA');"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (7, 'Destination');"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO CTYPES (ID, TYPE) " +
                     "VALUES (8, 'Draught');"; 
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE SOURCES " +
                     "(ID INT PRIMARY KEY     NOT NULL," +
                     " DESCRIPTION    TEXT    NOT NULL," +
                     " SHORTNAME      TEXT    NOT NULL," +
                     " URL            TEXT    NOT NULL)";  
        stmt.executeUpdate(sql);

        sql = "INSERT INTO SOURCES (ID, DESCRIPTION, SHORTNAME, URL) " +
                             "VALUES (1, 'Log Files', 'Logs', 'Path');"; 
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE CHARACTERISTICS " +
                     "(MMSI             INT   NOT NULL," +
                     " TYPE             INT   NOT NULL," +
                     " SOURCE           INT   NOT NULL," +
                     " DATA             TEXT          ," +
                     " DDATA            INT           ," +
                     " DTG              INT           )";  
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE POSITION " +
                     "(MMSI             INT   NOT NULL," +
                     " SOURCE           INT   NOT NULL," +
                     " LATITUDE         DOUBLE        ," +
                     " LONGITUDE        DOUBLE        ," +
                     " DTG              INT          ," +
                     " STORE_DATE       INT  NOT NULL)";  
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE KINEMATICS " +
                     "(MMSI             INT   NOT NULL," +
                     " COURSE           INT   NOT NULL," +
                     " SPEED            INT   NOT NULL," +
                     " HEADING          INT   NOT NULL," +
                     " TURNRATE         INT   NOT NULL," +
                     " DTG              INT   NOT NULL)";  
        stmt.executeUpdate(sql);
        
        sql = "CREATE TABLE ERRORS " +
                     "(MMSI             INT    NOT NULL," +
                     " PRIORITY         INT    NOT NULL," +
                     " DESCRIPTION      TEXT   NOT NULL," +
                     " DTG              INT    NOT NULL)";  
        stmt.executeUpdate(sql);            
        
        c.commit();
    
    } catch ( SQLException e ) 
    {
        System.out.println("Database tables already created.");
        return;
    }
        System.out.println("Table created successfully.");
    }
    
    public void updatePositionKinematics(AISMessage message)
    {
        int userID = message.getUserID();
        AISMessagePositionReport positionReport = (AISMessagePositionReport)message;
        try 
        {
            String sql = "INSERT INTO POSITION (MMSI, SOURCE, LATITUDE, " +
            "LONGITUDE, DTG, STORE_DATE) " +
            "VALUES ("+ userID + ", " + sourceID + ", " + 
            positionReport.getLatitudeInDegrees() + ", " +
            positionReport.getLongitudeInDegrees() + ", " + 
            (long)message.getProvenance().getTime() + ", " + System.currentTimeMillis()/1000 + ");";

            stmt.executeUpdate(sql);
            
            sql = "INSERT INTO KINEMATICS (MMSI, COURSE, SPEED, HEADING, " +
            "TURNRATE, DTG) " +
            "VALUES (" + userID + ", " + positionReport.getCourseOverGround() + 
            ", " + positionReport.getSpeedOverGround() + ", " + 
            positionReport.getTrueHeading() + ", " + 
            positionReport.getRateOfTurn() + ", " + 
            (long)message.getProvenance().getTime() + ");";
            
            stmt.executeUpdate(sql);       
            
        } catch ( Exception e ) 
        {
        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    public void updateCharacteristics(AISMessage message)
    {
        int userID = message.getUserID();
        
        AISMessage05 staticData = (AISMessage05) message;
        
        boolean etaError = false;
        boolean mmsiError = false;
        boolean destinationError = false;
        boolean atError = message.toString().contains("@");
        boolean typeError = false;
        boolean nameError = false;
        int first = 0;
        int type = staticData.getTypeOfShipAndCargoType();
        
        //Check for a suspicious MMSI outside of the standard range.
        String parse = Integer.toString(userID);
        
        if(parse.length() >= 3) {
            first = Integer.parseInt(parse.substring(0, 3));
            if(parse.length() < 9)
            {
                mmsiError = true;
            }    
        } else {
            mmsiError = true;
        }
        
        if(first < 201 || first > 775) {
            mmsiError = true;
        }
        
        /*All Information, even things such as numbers are converted to Strings
        here, as the SQL statements will work out properly regardless.*/
        String[] ctypes = new String[8];
        
        //Apoostrophes removed due to SQL rejections.
        ctypes[0] = staticData.getName().replace('\'', '-').replace('\"', '-').replace('\\', '-').trim(); 
        if(ctypes[0].equals(""))
        {
            ctypes[0] = "Name N/A";
            nameError = true;
        }
        
        //Integer from 0-99 gets returned.
        ctypes[1] = Integer.toString(type);
        if(type >= 19 || type > 99) 
        {
            typeError = true;
        }
        
        //CallSign from the parser is good as is and ready to write in.
        ctypes[2] = staticData.getCallSign().trim(); 
        
        //ImoNumber is ready to write as is, 0 means N/A.
        ctypes[3] = Integer.toString(staticData.getImoNumber());
        
        /*Dimensions are thrown in to the UtilsDimensions30 method, which
        returns them in a #, #, #, # (bow, stern, port, starboard) format.*/
        ctypes[4] = UtilsDimensions30.toString(staticData.getDimension());
        
        //Uses the decipherETA method to change to YYYY/MM/DD HH:MM.
        try {
            ctypes[5] = Long.toString(UtilsEta.convertToTime(staticData.getEta(), message.getProvenance().getTime()));//Integer.toString(1);
        } catch (Exception e) {
                //System.err.println("Exception thrown!! " + e);
                etaError = true;
            }
        
        //Apoostrophes removed due to SQL rejections.
        ctypes[6] = staticData.getDestination().replace('\'', '-').replace('\"', '-').replace('\\', '-').trim();
        if(ctypes[6].equals(""))
        {
            ctypes[6] = "Destination N/A";
            destinationError = true;
        }
        
        //Draught is a simple integer and is ready as is, 0 means N/A.
        ctypes[7] = Integer.toString(staticData.getMaximumPresentStaticDraught());
        
        if(mmsiError == true) {
           updateAgentsCenter(userID, 3, (int)message.getProvenance().getTime(), 1); 
        }
        
        if(etaError == true) {
            updateAgentsCenter(userID, 1, (int)message.getProvenance().getTime(), 2);   
        }
        
        if(atError == true) {
            updateAgentsCenter(userID, 3, (int)message.getProvenance().getTime(), 3);   
        }

        if(destinationError == true) {
            updateAgentsCenter(userID, 2, (int)message.getProvenance().getTime(), 4);   
        }
        
        if(typeError == true) {
            updateAgentsCenter(userID, 2, (int)message.getProvenance().getTime(), 5);   
        }
        
        if(nameError == true) {
            updateAgentsCenter(userID, 2, (int)message.getProvenance().getTime(), 6);   
        }
        
        try 
        {
            for(int i = 0; i < 8; i++) 
            {
               typeCount = i + 1;
               /*The reason for the logic in this if statement is that, we have
               DATA and DDATA, and since the characteristics are made up of
               strings or integers, we need to make sure they are written in to
               the correct column corresponding to their data type.*/
               if(i == 0 || i == 2 || i == 4 || i == 6)
               {
                String sql = "INSERT INTO CHARACTERISTICS (MMSI, TYPE, SOURCE, "
                                                        + "DATA, DDATA, DTG) " +
                "VALUES ("+ userID + ", " + typeCount + ", " + sourceID + ", '" 
                                  + ctypes[i] + "', " + null + ", " 
                         + (long)message.getProvenance().getTime() + ");";
                
                stmt.executeUpdate(sql);
                
               } else {
                   String sql = "INSERT INTO CHARACTERISTICS (MMSI, TYPE, SOURCE, "
                                                        + "DATA, DDATA, DTG) " +
                "VALUES ("+ userID + ", " + typeCount + ", " + sourceID + ", '" 
                                  + null + "', " + ctypes[i] + ", " 
                         + (long)message.getProvenance().getTime() + ");";
                stmt.executeUpdate(sql);
               }
                characteristicsCount++;
            }
                if ((characteristicsCount >= characteristicsDelay)) 
                {
                    characteristicsCount = 0;
                    c.commit();
                }
            
        } catch ( Exception e ) 
        {
        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    public void updateAgentsCenter(int mmsi, int p, int dtg, int o)
    {
        try {
            String sql = "INSERT INTO ERRORS (MMSI, PRIORITY, DESCRIPTION, DTG) " +
                "VALUES ("+ mmsi + ", " + p + ", " + o + ", "  
                         + dtg + ");";
            
            stmt.executeUpdate(sql);
            
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage());
            }
    }
    
}

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.sql.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class StationClient extends Thread {

    private final ServerWorker stationWorker;
    private String login;
    public String stationName;
    private String landArea;
    private String fieldCrop;
    private StationSensors stationSensors;
    public Boolean active;

    public StationClient(ServerWorker worker, String login, String stationName) {
        this.stationWorker = worker;
        this.login = login;
        this.stationName = stationName;
        active = true;
    }

    @Override
    public void run()  // Thread which handles the client socket
    {
        try {
            this.stationSensors = new StationSensors(this);
            pullStationData();
            stationSensors.startMessageReader(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // stationSensors.startMessageReader(); ???????????????????????????????????? Possible delete
    }

    boolean updateFields(String fieldToUpdate, String value) throws IOException {

        System.out.println("Here"); ////////////////////////////////////////////////////////////////////////////////////

        if("area".equalsIgnoreCase(fieldToUpdate)){
            this.landArea = value;
        }
        else if("crop".equalsIgnoreCase(fieldToUpdate)){
            this.fieldCrop = value;
        }
        else if("name".equalsIgnoreCase(fieldToUpdate)){
            this.stationName = value;
        }
        else {
            System.out.println("Update on "+stationName+" unsuccessful");
            return false;
        }

        String path = System.getProperty("user.dir") + "/";

        File inputFile = new File(path+"StationData.txt");
        File tempFile = new File(path+"StationDataTemp.txt");

        ArrayList<String> fileData = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        //BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String currentLine;

        while((currentLine = reader.readLine()) != null) {
            if (currentLine.length() > 0) {
                String[] tokens = StringUtils.split(currentLine, ",");
                if (!login.equalsIgnoreCase(tokens[0])) {
                    //writer.write(currentLine + System.getProperty("line.separator"));
                    fileData.add(currentLine);
                }
            }
        }

        BufferedWriter clearWriter = new BufferedWriter(new FileWriter(inputFile, false));
        clearWriter.write(login + "," + stationName + "," + landArea + "," + fieldCrop + System.getProperty("line.separator"));
        clearWriter.close();

        String line;

        BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, true));
        for(int i = 0; i < fileData.size(); i++ ){
            if(i == fileData.size()-1)line = fileData.get(i);
            else line = fileData.get(i) +System.getProperty("line.separator");
            writer.append(line);
        }

        writer.close();
        reader.close();

        System.out.println("Update on "+stationName+" successful");
        return true;
    }

    private void pullStationData() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("StationData.txt"));

        String currentLine;
        while((currentLine = reader.readLine()) != null) {
            if (currentLine.length() > 0) {
                String[] tokens = StringUtils.split(currentLine, ",");
                if (login.equalsIgnoreCase(tokens[0])) {
                    landArea = tokens[2];
                    fieldCrop = tokens[3];
                    break;
                }
            }
        }
        reader.close();
    }

    public void stopSensor(){
        this.active = false;
    }

    public ArrayList<String> getDataBrief() throws IOException {
        ArrayList<String> data = new ArrayList<>();
        String fileName = stationName + "_SensorData.txt";

        FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        while((line = bufferedReader.readLine()) != null){
            data.add(line);
        }
        bufferedReader.close();
        return data;
    }

    public boolean downloadData(){
        String fileName = stationName + "_SensorData.txt";
        File source = new File(fileName);

        if(source.exists()){

            String home = System.getProperty("user.home");
            File dest = new File(home+"/Downloads/" + fileName);

            try {
                FileUtils.copyFile(source, dest);
                System.out.println(stationName + " data downloaded.");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
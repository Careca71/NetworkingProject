package main.java.Utils;

import java.io.*;


// Utility class to read the configgile

public class MyConfigFIleReader {
    private static final String lato_server = "**********  LATO_SERVER **********\n";
    private static final String SERVER = "SERVER";
    private static final String TCPPORT = "TCPPORT";
    private static final String RMIPORT = "RMIPORT";
    private static final String MULTICAST = "MULTICAST";
    private static final String MCASTPORT = "MCASTPORT";
    private static final String NOTIFYPORT = "NOTIFYPORT";
    private static final String TIMEOUT = "TIMEOUT";

    private String address;
    private int tcpport;
    private int rmiport;
    private String multicast;
    private int mcaport;
    private int notifyport;
    private int timeout;


    public MyConfigFIleReader() {

    }

    //method to read the server configfile
    public void read_Server_config_file(File configfile) {
        if (configfile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(configfile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("="))
                        tokenline(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(lato_server + "Configfile inesistente!\n");
            System.exit(1);
        }
    }

    //method to read the server configfile
    public void read_Client_config_file(File configfile) {
        if (configfile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(configfile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("="))
                        tokenline(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(lato_server + "Configfile inesistente!\n");
            System.exit(1);
        }
    }

    private void tokenline(String line) {
        String[] tmp;
        tmp = line.split("=");
        if (tmp[0].equals(SERVER)) {
            address = tmp[1];
        }
        if (tmp[0].equals(TCPPORT)) {
            tcpport = Integer.parseInt(tmp[1]);
        }
        if (tmp[0].equals(RMIPORT)) {
            rmiport = Integer.parseInt(tmp[1]);
        }
        if (tmp[0].equals(MULTICAST)) {
            multicast = tmp[1];
        }
        if (tmp[0].equals(MCASTPORT)) {
            mcaport = Integer.parseInt(tmp[1]);
        }
        if (tmp[0].equals(NOTIFYPORT)) {
            notifyport = Integer.parseInt(tmp[1]);
        }
        if (tmp[0].equals(TIMEOUT)) {
            timeout = Integer.parseInt(tmp[1]);
        }

    }

    public String getAddress() {
        return address;
    }

    public int getTcpport() {
        return tcpport;
    }

    public int getRmiport() {return rmiport;}

    public String getMulticast() {return multicast;}

    public int getMcaport() {return mcaport;}

    public int getNotifyport() {return notifyport;}

    public int getTimeout() {return timeout;}

}

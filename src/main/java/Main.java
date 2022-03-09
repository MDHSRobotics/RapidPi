import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/*
JSON format:
{
    "team": <team number>,
    "ntmode": <"client" or "server", "client" if unspecified>
    "cameras": [
        {
            "name": <camera name>
            "path": <path, e.g. "/dev/video0">
            "pixel format": <"MJPEG", "YUYV", etc>                      // optional
            "width": <video mode width>                                 // optional
            "height": <video mode height>                               // optional
            "fps": <video mode fps>                                     // optional
            "brightness": <percentage brightness>                       // optional
            "white balance": <"auto", "hold", value>                    // optional
            "exposure": <"auto", "hold", value>                         // optional
            "properties": [                                             // optional
                {
                    "name": <property name>
                    "value": <property value>
                }
            ],
            "stream": {                                                 // optional
                "properties": [
                    {
                        "name": <stream property name>
                        "value": <stream property value>
                    }
                ]
            }
        }
    ]
    "switched cameras": [
       {
           "name": <virtual camera name>
           "key": <network table key used for selection>
           // if NT value is a string, it's treated as a name
           // if NT value is a double, it's treated as an integer index
       }
   ]
}
*/

public final class Main {
    private static String configFile = "/boot/frc.json";

    @SuppressWarnings("MemberName")
    public static class CameraConfig {
        public String name;
        public String path;
        public JsonObject config;
        public JsonElement streamConfig;
    }

    @SuppressWarnings("MemberName")
    public static class SwitchedCameraConfig {
        public String name;
        public String key;
    };

    public static int team;
    public static boolean server;
    public static List<CameraConfig> cameraConfigs = new ArrayList<>();
    public static List<SwitchedCameraConfig> switchedCameraConfigs = new ArrayList<>();
    public static List<VideoSource> cameras = new ArrayList<>();

    private Main() {
    }

    /**
     * Report parse error.
     */
    public static void parseError(String str) {
        System.err.println("config error in '" + configFile + "': " + str);
    }


    /**
     * Read configuration file.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static boolean readConfig() {
        // parse file
        JsonElement top;
        try {
            top = new JsonParser().parse(Files.newBufferedReader(Paths.get(configFile)));
        } catch (IOException ex) {
            System.err.println("could not open '" + configFile + "': " + ex);
            return false;
        }

        // top level must be an object
        if (!top.isJsonObject()) {
            parseError("must be JSON object");
            return false;
        }
        JsonObject obj = top.getAsJsonObject();

        // team number
        JsonElement teamElement = obj.get("team");
        if (teamElement == null) {
            parseError("could not read team number");
            return false;
        }
        team = teamElement.getAsInt();

        // ntmode (optional)
        if (obj.has("ntmode")) {
            String str = obj.get("ntmode").getAsString();
            if ("client".equalsIgnoreCase(str)) {
                server = false;
            } else if ("server".equalsIgnoreCase(str)) {
                server = true;
            } else {
                parseError("could not understand ntmode value '" + str + "'");
            }
        }

        return true;
    }


    /**
     * Main.
     */
    public static void main(String... args) {
        if (args.length > 0) {
            configFile = args[0];
        }

        // read configuration
        if (!readConfig()) {
            return;
        }

        // start NetworkTables
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
        if (server) {
            System.out.println("Setting up NetworkTables server");
            ntinst.startServer();
        } else {
            System.out.println("Setting up NetworkTables client for team " + team);
            ntinst.startClientTeam(team);
        }

        // start the distance sensor
        // https://www.techcoil.com/blog/helpful-gpio-pinout-resources-that-you-can-reference-while-connecting-sensors-to-your-raspberry-pi-3/
        // https://pinout.xyz/pinout/wiringpi
        // https://tutorials-raspberrypi.com/raspberry-pi-ultrasonic-sensor-hc-sr04/
        // https://www.modmypi.com/blog/hc-sr04-ultrasonic-range-sensor-on-the-raspberry-pi
        // Pin echoPin = RaspiPin.GPIO_05; // PI4J custom numbering (pin 20)
        // Pin trigPin = RaspiPin.GPIO_04; // PI4J custom numbering (pin 18)
        // DistanceMonitor monitor = new DistanceMonitor(echoPin, trigPin);

        // loop forever
        for (;;) {
            // try {
            //     System.out.printf("%1$d,%2$.3f%n", System.currentTimeMillis(), monitor.measureDistance());
            // }
            // catch (TimeoutException e) {
            //     System.err.println("ERORREWARRAWEF GREGFj'peargjpoaggo: " + e);
            // }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }
}
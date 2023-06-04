# jsp-bytecode-webshell

Dynamic JSP webshell that receives Java class bytecode, loads and execute it, returning the output to the operator. The client generates the bytecode during runtime using a payload template that calls `java.Runtime.exec()` with a provided string as input, XORs it and sends to JSP page.


![](https://raw.githubusercontent.com/joaovarelas/jsp-bytecode-webshell/main/image01.png)

## How to use


1. Upload `webshell.jsp` to target server (e.g. as Tomcat WAR package)
    - `jar cvf app.war webshell.jsp`

2. Compile and run `client.java` and provide the webshell URL argument:
    - `javac client.java && java client http://127.0.0.1:8080/app/webshell.jsp`



## References 

- https://github.com/fa1c0n1/MyJSPWebshell


- Only for educational and ethical use-cases. 

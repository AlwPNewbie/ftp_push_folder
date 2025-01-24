import java.net.*
import java.io.*

def telnetConnect(String host, int port, String user, String password) {
    try {
        // Connect to the FTP server
        Socket socket = new Socket(host, port)
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)

        // Read the welcome message from the server
        println "Server Response: ${reader.readLine()}"

        // Send the USER command
        writer.println("USER ${user}")
        println "Server Response: ${reader.readLine()}"

        // Send the PASS command
        writer.println("PASS ${password}")
        println "Server Response: ${reader.readLine()}"

        // Close the connection after the login
        writer.println("QUIT")
        println "Server Response: ${reader.readLine()}"

        // Close the socket
        socket.close()
    } catch (Exception e) {
        println "Error: ${e.message}"
    }
}

// Read credentials from a file
def credentialsFile = new File('ftp_credentials.txt')
def credentials = credentialsFile.readLines()
def username = credentials[0]
def password = credentials[1]

// FTP server details
def ftpHost = "192.168.56.101"
def ftpPort = 21

// Call the function with credentials and FTP server info
telnetConnect(ftpHost, ftpPort, username, password)

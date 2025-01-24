@Grab(group='commons-net', module='commons-net', version='3.9.0') // Automatically downloads the library
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File

def uploadDirectoryToFTP(String server, int port, String user, String pass, String localDir, String remoteDir) {
    FTPClient ftpClient = new FTPClient()

    try {
        // Connect and login to the FTP server
        ftpClient.connect(server, port)
        println "Connected to FTP server: $server"
        
        boolean loggedIn = ftpClient.login(user, pass)
        if (!loggedIn) {
            println "Failed to log in to FTP server with provided credentials."
            return
        }
        
        ftpClient.enterLocalPassiveMode() // Use passive mode
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
        println "Successfully logged in as $user"

        // Check current remote working directory
        def remotePwd = ftpClient.printWorkingDirectory()
        println "Current remote directory: $remotePwd"

        // Ensure the remote folder exists (create if needed)
        String remoteFolder = "$remoteDir/Ruffles"
        if (!ftpClient.changeWorkingDirectory(remoteFolder)) {
            println "Remote folder does not exist. Creating: $remoteFolder"
            boolean created = ftpClient.makeDirectory(remoteFolder)
            if (!created) {
                println "Failed to create remote folder: $remoteFolder"
                return
            }
            ftpClient.changeWorkingDirectory(remoteFolder)
        }
        println "Using remote folder: $remoteFolder"

        // Check if the local directory exists
        File localFile = new File(localDir)
        if (!localFile.exists()) {
            println "Local directory does not exist: $localDir"
            return
        }

        // Upload all files from the local folder to the remote folder
        uploadFilesRecursively(ftpClient, localFile, remoteFolder)

        println "Upload process complete!"

    } catch (Exception ex) {
        println "Error: ${ex.message}"
        ex.printStackTrace()
    } finally {
        try {
            ftpClient.logout()
            ftpClient.disconnect()
            println "Disconnected from FTP server."
        } catch (IOException e) {
            println "Error disconnecting: ${e.message}"
        }
    }
}

def uploadFilesRecursively(FTPClient ftpClient, File dir, String remotePath) {
    println "Entering directory: $dir"
    
    dir.eachFile { file ->
        if (file.isFile()) {
            // Upload the file
            println "Uploading file: ${file.name} to $remotePath"
            file.withInputStream { fis ->
                boolean uploaded = ftpClient.storeFile("$remotePath/${file.name}", fis)
                if (uploaded) {
                    println "Successfully uploaded: ${file.name}"
                } else {
                    println "Failed to upload: ${file.name}"
                }
            }
        } else if (file.isDirectory()) {
            // Recursively handle subdirectories
            String newRemoteDir = "$remotePath/${file.name}"
            if (!ftpClient.changeWorkingDirectory(newRemoteDir)) {
                println "Creating remote directory: $newRemoteDir"
                boolean created = ftpClient.makeDirectory(newRemoteDir)
                if (created) {
                    println "Created remote directory: $newRemoteDir"
                    ftpClient.changeWorkingDirectory(newRemoteDir)
                } else {
                    println "Failed to create remote directory: $newRemoteDir"
                    return
                }
            }
            uploadFilesRecursively(ftpClient, file, newRemoteDir) // Recursively upload subdirectories
        }
    }
}

// Read credentials and folder path from a file
def credentialsFile = new File('ftp_credentials.txt')
def credentials = credentialsFile.readLines()

def username = credentials[0]  // FTP username
def password = credentials[1]  // FTP password
def ftpHost = credentials[2]   // FTP server IP or hostname
def ftpPort = Integer.parseInt(credentials[3]) // FTP server port
def localDir = new File('Ruffles').absolutePath // Path to the "Ruffles" folder
def remoteDir = "/ftp/files"  // Remote directory on the server (base folder where Ruffles will go)

uploadDirectoryToFTP(ftpHost, ftpPort, username, password, localDir, remoteDir)

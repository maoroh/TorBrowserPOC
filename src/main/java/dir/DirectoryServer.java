package dir;

import java.io.IOException;

public class DirectoryServer {

    public static void main(String[] args) throws IOException {
        Directory dirServer = new Directory();
        dirServer.startListening();
    }
}

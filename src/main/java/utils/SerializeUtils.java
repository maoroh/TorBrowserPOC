package utils;

import java.io.*;

public class SerializeUtils {

    public static byte [] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
        objectOutputStream.writeObject(object);
        return bos.toByteArray();
    }

    public static <T> T deserializeObject(byte [] data) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
        return (T) objectInputStream.readObject();
    }
}

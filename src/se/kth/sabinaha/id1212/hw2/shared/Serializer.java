package se.kth.sabinaha.id1212.hw2.shared;

import java.io.*;

public class Serializer {

    /**
     * Serializes the object with a prepended 6 byte long header indicating the length of the following message
     * @param objectToSerialize The object to serialize
     * @return A byte array of the serialized object
     */
    public static byte[] serializeObject(Object objectToSerialize) throws IOException {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        byte[] byteObject;
        ObjectOutput objectOutputStream = new ObjectOutputStream(byteOutStream);

        objectOutputStream.writeObject(objectToSerialize);
        objectOutputStream.flush();
        byteObject = byteOutStream.toByteArray();
        objectOutputStream.close();
        byteOutStream.close();
        return byteObject;
    }

    /**
     * Deserializes a byte-serialized object.
     * @param byteObj The byte-array object to deserialize
     * @return A restored Object. You must cast it into the type it is yourself.
     * @throws IOException If there is a I/O problem, this is thrown.
     * @throws ClassNotFoundException If a unknown class is restored.
     */
    public static Object deserialize(byte[] byteObj) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(byteObj);
        ObjectInput inputObject = new ObjectInputStream(inStream);

        return inputObject.readObject();

    }
}

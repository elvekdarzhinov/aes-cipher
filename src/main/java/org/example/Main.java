package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    private static final String KEY_FILE = "key.txt";
    private static final String INPUT_FILE = "input.txt";
    private static final String OUTPUT_FILE = "output.txt";

    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.out.println("Syntax: [-e | -d] -k keyFile -i inputFile -o outputFile");
//            return;
//        }
//        if (args.length != 8) {
//            throw new IllegalArgumentException("Incorrect number of arguments.");
//        }

        boolean encrypt = true;
        String inputFile = INPUT_FILE;
        String outputFile = OUTPUT_FILE;
        String keyFile = KEY_FILE;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-e" -> encrypt = true;
                case "-d" -> encrypt = false;
                case "-k" -> keyFile = args[i + 1];
                case "-i" -> inputFile = args[i + 1];
                case "-o" -> outputFile = args[i + 1];
            }
        }

        try (BufferedReader inputReader = new BufferedReader(new FileReader(inputFile));
             BufferedReader keyReader = new BufferedReader(new FileReader(keyFile));
             BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile))) {

            byte[] input = convert(inputReader.readLine());
            byte[] key = convert(keyReader.readLine());

//            IdeaCipher ideaCipher = new IdeaCipher(key, rounds);
//
//            for (int i = 0; i < input.length; i += IdeaCipher.BLOCK_SIZE) {
//                byte[] output = ideaCipher.crypt(input, i, encrypt);
//                outputWriter.write(byteToString(output));
//            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static String byteToString(byte[] array) {
        StringBuilder sb = new StringBuilder();

        for (var b : array) {
            sb.append(String.format("%02x", b & 0xFF)).append(" ");
        }

        return sb.substring(0, sb.length() - 1);
    }

    private static byte[] convert(String input) {
        String[] byteString = input.split(" ");
        byte[] bytes = new byte[byteString.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(byteString[i], 16);
        }
        return bytes;
    }

}

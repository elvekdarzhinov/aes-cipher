package org.example;

public class Helper {

    public static final int BLOCK_SIZE = 16; // bytes
    public static final int Nb = 4;
    public static final int KEY_SIZE = 16; // bytes
    public static final int Nk = 4;
    public static final int Nr = 10; // number of rounds

    public static void Cipher(byte[] in, byte[] out) {
        byte[][] state = makeState(in);
    }

    public static byte[][] makeState(byte[] input) {
        byte[][] state = new byte[4][4];
        for (int rows = 0; rows < 4; rows++) {
            for (int columns = 0; columns < 4; columns++) {
                state[rows][columns] = input[rows + 4 * columns];
            }
        }
        return state;
    }

    public static byte[] makeOutput(byte[][] state) {
        byte[] output = new byte[16];
        for (int rows = 0; rows < state.length; rows++) {
            for (int columns = 0; columns < state[0].length; columns++) {
                output[rows + 4 * columns] = state[rows][columns];
            }
        }
        return output;
    }

}

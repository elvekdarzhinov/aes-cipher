package org.example;

public class Aes {

    private static final int Nb = 4; // number of columns (32-bit words) in block
    private static final int Nk = 4; // number of 32-bit words in key
    private static final int Nr = 10; // number of rounds

    public static byte[] encrypt(byte[] in, byte[] key) {
        byte[][] state = makeState(in);
        byte[][] keyWords = keyExpansion(key);

        addRoundKey(state, keyWords, 0);

        for (int round = 1; round < Nr; round++) {
            subBytes(state);
            shiftRows(state);
            mixColumns(state, M);
            addRoundKey(state, keyWords, round * Nb);
        }

        subBytes(state);
        shiftRows(state);
        addRoundKey(state, keyWords, Nr * Nb);

        return makeOutput(state);
    }

    public static byte[] decrypt(byte[] in, byte[] key) {
        byte[][] state = makeState(in);
        byte[][] keyWords = keyExpansion(key);

        addRoundKey(state, keyWords, Nr * Nb);

        for (int round = Nr - 1; round >= 1; round--) {
            invShiftRows(state);
            invSubBytes(state);
            addRoundKey(state, keyWords, round * Nb);
            invMixColumns(state);
        }

        invShiftRows(state);
        invSubBytes(state);
        addRoundKey(state, keyWords, 0);

        return makeOutput(state);
    }

    private static void subBytes(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) Sbox[state[i][j] & 0xff];
            }
        }
    }

    private static void invSubBytes(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) invSbox[state[i][j] & 0xff];
            }
        }
    }

    private static void shiftRows(byte[][] state) {
        state[1] = shiftWord(state[1], 1);
        state[2] = shiftWord(state[2], 2);
        state[3] = shiftWord(state[3], 3);
    }

    private static void invShiftRows(byte[][] state) {
        state[1] = shiftWord(state[1], -1);
        state[2] = shiftWord(state[2], -2);
        state[3] = shiftWord(state[3], -3);
    }

    private static void mixColumns(byte[][] state, byte[][] M) {
        byte[][] tmp = new byte[4][4];

        for (int i = 0; i < 4; i++) {
            System.arraycopy(state[i], 0, tmp[i], 0, 4);
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) (galoisMul(M[i][0], tmp[0][j]) ^ galoisMul(M[i][1], tmp[1][j]) ^
                        galoisMul(M[i][2], tmp[2][j]) ^ galoisMul(M[i][3], tmp[3][j]));
            }
        }
    }

    private static void invMixColumns(byte[][] state) {
        mixColumns(state, invM);
    }

    private static void addRoundKey(byte[][] state, byte[][] keyWords, int offset) {
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                state[row][column] ^=  keyWords[column + offset][row];
            }
        }
    }

    private static byte[] xorWords(byte[] a, byte[] b) {
        byte[] out = new byte[4];

        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }

        return out;
    }

    private static byte[][] keyExpansion(byte[] key) {
        byte[][] keyWords = new byte[44][4];

        int i = 0;
        while (i < Nk) {
            System.arraycopy(key, 4 * i, keyWords[i], 0, 4);
            i++;
        }

        i = Nk;
        while (i < Nb * (Nr + 1)) {
            byte[] tmp = keyWords[i - 1];

            if (i % Nk == 0) {
                tmp = xorWords(subWord(shiftWord(tmp, 1)), Rcon[i / Nk]);
            }
            keyWords[i] = xorWords(keyWords[i - Nk], tmp);
            i++;
        }

        return keyWords;
    }

    private static byte[] shiftWord(byte[] in, int offset) {
        byte[] out = new byte[4];

        for (int i = 0; i < 4; i++) {
            out[i] = in[(i + offset + 4) % 4];
        }

        return out;
    }

    private static byte[] subWord(byte[] in) {
        byte[] out = new byte[4];

        for (int i = 0; i < in.length; i++) {
            out[i] = (byte) Sbox[in[i] & 0xff];
        }

        return out;
    }

    private static byte galoisMul(byte u, byte v) {
        byte result = 0;

        for (int i = 0; i < 8; i++) {
            if ((u & 0x01) != 0) {
                result ^= v;
            }
            int flag = (v & 0x80);
            v <<= 1;
            if (flag != 0) {
                v ^= 0x1b; // x^8 + x^4 + x^3 + x + 1
            }
            u >>= 1;
        }

        return result;
    }

    private static byte[][] makeState(byte[] input) {
        byte[][] state = new byte[4][4];

        for (int rows = 0; rows < 4; rows++) {
            for (int columns = 0; columns < 4; columns++) {
                state[rows][columns] = input[rows + 4 * columns];
            }
        }

        return state;
    }

    private static byte[] makeOutput(byte[][] state) {
        byte[] output = new byte[16];

        for (int rows = 0; rows < state.length; rows++) {
            for (int columns = 0; columns < state[0].length; columns++) {
                output[rows + 4 * columns] = state[rows][columns];
            }
        }

        return output;
    }

    private static final int[] Sbox = {
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    };

    private static final int[] invSbox = {
            0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
            0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
            0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
            0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
            0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
            0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
            0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
            0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
            0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
            0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
            0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
            0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
            0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
            0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
            0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
            0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
    };

    private static final byte[][] Rcon = {
            {0x00, 0x00, 0x00, 0x00},
            {0x01, 0x00, 0x00, 0x00},
            {0x02, 0x00, 0x00, 0x00},
            {0x04, 0x00, 0x00, 0x00},
            {0x08, 0x00, 0x00, 0x00},
            {0x10, 0x00, 0x00, 0x00},
            {0x20, 0x00, 0x00, 0x00},
            {0x40, 0x00, 0x00, 0x00},
            {(byte) 0x80, 0x00, 0x00, 0x00},
            {0x1b, 0x00, 0x00, 0x00},
            {0x36, 0x00, 0x00, 0x00}
    };

    private static final byte[][] M = {{0x02, 0x03, 0x01, 0x01},
                                       {0x01, 0x02, 0x03, 0x01},
                                       {0x01, 0x01, 0x02, 0x03},
                                       {0x03, 0x01, 0x01, 0x02}};

    private static final byte[][] invM = {{0x0e, 0x0b, 0x0d, 0x09},
                                          {0x09, 0x0e, 0x0b, 0x0d},
                                          {0x0d, 0x09, 0x0e, 0x0b},
                                          {0x0b, 0x0d, 0x09, 0x0e}};

}

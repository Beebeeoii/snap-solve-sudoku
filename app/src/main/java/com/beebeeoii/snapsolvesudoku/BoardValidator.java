package com.beebeeoii.snapsolvesudoku;

import java.util.ArrayList;

public class BoardValidator {

    private int[][] board;

    private boolean boardValid = true;

    private ArrayList<int[]> errors = new ArrayList<int[]>();

    public BoardValidator (int[][] board) {
        this.board = board;

        validate();
    }

    public ArrayList<int[]> getErrors() {
        return errors;
    }

    public int getNoDigits() {
        String boardString = "";
        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col ++) {
                boardString += board[row][col];
            }
        }

        return boardString.replace("0", "").length();
    }


    public boolean validate () {
        if (board == null || board.length != 9 || board[0].length != 9) {
            return false;
        }

        // check each column
        for (int i = 0; i < 9; i++) {
            boolean[] m = new boolean[9];
            String[] n = new String[9];
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    if (m[(int) (board[i][j] - 1)]) {

                        int numberRepeatedIndex = board[i][j] - 1;

                        int[] errorCell = {i, j};
                        int[] errorCell2 = {Integer.parseInt(Character.toString((n[numberRepeatedIndex]).charAt(0))), Integer.parseInt(Character.toString(n[numberRepeatedIndex].charAt(1)))};
                        errors.add(errorCell);
                        errors.add(errorCell2);

                        if (boardValid) {
                            boardValid = false;
                        }
                    } else {
                        m[(int) (board[i][j] - 1)] = true;
                        n[(int) (board[i][j] - 1)] = String.valueOf(i).concat(String.valueOf(j));
                    }
                }
            }
        }

        //check each row
        for (int j = 0; j < 9; j++) {
            boolean[] m = new boolean[9];
            String[] n = new String[9];
            for (int i = 0; i < 9; i++) {
                if (board[i][j] != 0) {
                    if (m[(int) (board[i][j] - 1)]) {

                        int numberRepeatedIndex = board[i][j] - 1;

                        int[] errorCell = {i, j};
                        int[] errorCell2 = {Integer.parseInt(Character.toString((n[numberRepeatedIndex]).charAt(0))), Integer.parseInt(Character.toString(n[numberRepeatedIndex].charAt(1)))};
                        errors.add(errorCell);
                        errors.add(errorCell2);

                        if (boardValid) {
                            boardValid = false;
                        }
                    }
                    m[(int) (board[i][j] - 1)] = true;
                    n[(int) (board[i][j] - 1)] = String.valueOf(i).concat(String.valueOf(j));
                }
            }
        }

        //check each 3*3 matrix
        for (int block = 0; block < 9; block ++) {
            boolean[] m = new boolean[9];
            String[] n = new String[9];
            for (int i = block / 3 * 3; i < block / 3 * 3 + 3; i ++) {
                for (int j = block % 3 * 3; j < block % 3 * 3 + 3; j ++) {
                    if (board[i][j] != 0) {
                        if (m[(int) (board[i][j] - 1)]) {

                            int numberRepeatedIndex = board[i][j] - 1;

                            int[] errorCell = {i, j};
                            int[] errorCell2 = {Integer.parseInt(Character.toString((n[numberRepeatedIndex]).charAt(0))), Integer.parseInt(Character.toString(n[numberRepeatedIndex].charAt(1)))};
                            errors.add(errorCell);
                            errors.add(errorCell2);

                            if (boardValid) {
                                boardValid = false;
                            }
                        }

                        m[(int) (board[i][j] - 1)] = true;
                        n[(int) (board[i][j] - 1)] = String.valueOf(i).concat(String.valueOf(j));
                    }
                }
            }
        }

        if (boardValid) {
            return true;
        } else {
            return false;
        }

    }
}

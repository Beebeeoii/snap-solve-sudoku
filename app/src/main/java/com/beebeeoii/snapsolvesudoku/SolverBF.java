package com.beebeeoii.snapsolvesudoku;

import androidx.fragment.app.FragmentActivity;

import static com.beebeeoii.snapsolvesudoku.Manual.progressStatus;

public class SolverBF {

    private int quad[][] = new int[9][10];
    private int rr[][] = new int[9][10];
    private int cr[][] = new int[9][10];

    private int maxSols;
    private int solCounter = 0;
    private String solutions = "";
    private FragmentActivity activity;

    private int[][] board;

    public SolverBF(int[][] board, int maxSols, FragmentActivity activity) {
        this.board = board;
        this.maxSols = maxSols;
        this.activity = activity;
        main();
    }

    public int getSolCounter() {
        return solCounter;
    }

    public String getSolutions() {
        return solutions;
    }

    public int quad(int x, int y) {
        return 3*(x/3) + y/3;
    }

    public boolean putable(int x, int y, int v) {
        int q = quad(x, y);
        if(rr[x][v] == 1 || cr[y][v] == 1 || quad[q][v] !=0)return false;
        return true;
    }

    public void solve(int x, int y) {

        if (solCounter == maxSols) {
            return;
        }

        //FOUND
        if (x == 8 && y == 9) {

            printBoard();

            return;
        }
        if (y == 9) {
            y=0;
            x++;
        }

        //ALREADY PLACED IN GIVEN PUZZLE
        if (board[x][y]!=0) {
            solve(x, y+1);
            return;
        }

        for(int i=1;i<=9;++i) {
            int q = quad(x, y);

            if(putable(x, y, i)) {
                quad[q][i] = 1;
                rr[x][i] = 1;
                cr[y][i] = 1;

                board[x][y] = i;
                solve(x, y+1);
                board[x][y] = 0;

                rr[x][i] = 0;
                cr[y][i] = 0;
                quad[q][i] = 0;
            }
        }
    }

    public void main() {
        for(int i=0;i<9;++i) {
            for(int j=0;j<9;++j) {
                if(board[i][j] != 0) {
                    int q = quad(i, j);
                    int v = board[i][j];
                    rr[i][v] = 1;
                    cr[j][v] = 1;
                    quad[q][board[i][j]] = 1;
                }
            }
        }
        solve(0, 0);
    }

    public void printBoard() {

        for(int i=0;i<9;++i) {
            for(int j=0;j<9;++j) {
                solutions += board[i][j];
            }
        }

        solutions += " ";
        solCounter += 1;
        activity.runOnUiThread(() -> progressStatus.setText(solCounter + " solutions found"));
    }

}

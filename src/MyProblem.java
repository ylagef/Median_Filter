import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MyProblem {
    public static HashMap<Integer, Output> outputs = new HashMap<>(); //Global HashMap with the Output objects.
    public static MyMatrix matrix;
    public static double[][] convertedMatrix;
    static int numThreads;
    public static List<Thread> threadList = new ArrayList<>(numThreads); //ArrayList for the created Threads.

    public static void main(String[] args) {
        numThreads = Integer.parseInt(args[0]);

        int numRows = 6;
        int numCols = 4;

        matrix = new MyMatrix(numRows, numCols); //Generates new matrix
        convertedMatrix = new double[numRows][numCols]; //Reserves space
        matrix.constantFill(2); //Fill matrix
        System.out.println(matrix.toString());

        //Do the median filter with threads
        matrix.medianFilter();

        //Check threads end
        for (Thread t : threadList) { //For each thread, wait until is ended.
            try {
                t.join(); //join() is a function that waits until Thread t is finished.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Print final matrix
        for (double[] d : convertedMatrix) {
            for (Double d1 : d) {
                System.out.print(d1 + " \t");
            }
            System.out.println();
        }
        //Then, this foreach ends when every thread is ended.

        //Print final message
        System.out.println("\nProgram of exercise 3 has terminated.");
    }
}

class MyThread implements Runnable {
    private Integer startR;
    private Integer startC;
    private Integer endR;
    private Integer endC;

    MyThread(int sr, int sc, int er, int ec) {
        this.startR = sr;
        this.startC = sc;
        this.endR = er;
        this.endC = ec;
    }

    //Code going to be executed by the thread.
    @Override
    public void run() {
        int numRows = endR - startR;

        while (numRows >= 1) {
            for (int c = startC; c < MyProblem.matrix.getColsSize(); c++) {
                MyProblem.convertedMatrix[startR][c] = (new Cell(startR, c)).getMedium();
            }
            startR++;
            startC = 0;
            numRows--;
        }

        for (int c = 0; c <= endC; c++) {
            MyProblem.convertedMatrix[endR][c] = (new Cell(endR, c)).getMedium();
        }
    }
}

//Class used for storing each Thread timing.
class Output {
    private long sentInterrupt, interrupted;

    Output(long sentInterrupt) {
        this.sentInterrupt = sentInterrupt;
        this.interrupted = 0;
    }

    public void setInterrupted(long interrupted) {
        this.interrupted = interrupted;
    }

    @Override
    public String toString() {
        String info = "sentInterrupt: " + sentInterrupt + " - interrupted: " + interrupted;
        String results = "\n\ts-i: " + (interrupted - sentInterrupt);
        return info + results;
    }
}

class MyMatrix {
    private int[][] matrix;

    MyMatrix(int ySize, int xSize) {
        this.matrix = new int[ySize][xSize];
    }

    public void randomFill(int bound) {
        Random rand = new Random();
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                matrix[r][c] = rand.nextInt(bound); //Random from 0 to bound
            }
        }
    }

    public void constantFill(int step) {
        int aux = -step;
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                matrix[r][c] = aux += step;
            }
        }
    }

    public void medianFilter() {
        System.out.println("- - - - - - - - - - - - - - - - - - - - -\n");

        int cellsQuantity = this.getRowsSize() * this.getColsSize();

        int cellsPerThread;
        if (MyProblem.numThreads >= cellsQuantity) {
            //If more threads than cells, just one thread per cell.
            cellsPerThread = 1;
            MyProblem.numThreads = cellsQuantity;
        } else {
            cellsPerThread = cellsQuantity / MyProblem.numThreads;
        }

        //Assigning cells to threads
        int id = 0;
        int cells = 1, endR = 0, endC = 0, startR = 0, startC = 0;
        for (int x = 0; x < cellsQuantity; x++) {
            if (MyProblem.threadList.size() == MyProblem.numThreads - 1) {
                MyProblem.threadList.add(id, new Thread(new MyThread(startR, startC, getRowsSize() - 1, getColsSize() - 1), Integer.toString(id)));
                MyProblem.threadList.get(id).start();
                break;
            }

            if (cells == cellsPerThread) {
                MyProblem.threadList.add(id, new Thread(new MyThread(startR, startC, endR, endC), Integer.toString(id)));
                MyProblem.threadList.get(id++).start();
                if (endC == getColsSize() - 1) {
                    startR = endR + 1;
                    startC = 0;
                } else {
                    startR = endR;
                    startC = endC + 1;
                }
                cells = 0;
            }

            cells++;
            endC++; //Next col

            if (endC == getColsSize()) {
                endC = 0; //Col 0
                endR++; //Next row
            }
        }
    }

    public int getValue(int r, int c) {
        return matrix[r][c];
    }

    public int getRowsSize() {
        return matrix.length;
    }

    public int getColsSize() {
        return matrix[0].length;
    }

    @Override
    public String toString() {
        StringBuilder toPrint = new StringBuilder();

        for (int[] row : matrix) {
            for (int cell : row) {
                toPrint.append(cell).append("\t\t ");
            }
            toPrint.append("\n");
        }

        return toPrint.toString();
    }
}

class Cell {
    private int r, c;

    Cell(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public double getMedium() {
        int colsSize = MyProblem.matrix.getColsSize();
        int rowsSize = MyProblem.matrix.getRowsSize();

        int actual = MyProblem.matrix.getValue(r, c);

        int top, bottom, left, right, topLeft, topRight, bottomLeft, bottomRight;

        // I KNOW that this way is not optimal. Otherwise it was a secure way for checking all chances.
        if (r == 0) {//First row
            bottom = MyProblem.matrix.getValue(r + 1, c);
            top = MyProblem.matrix.getValue(r + 1, c);

            if (c == 0) {//First col
                left = MyProblem.matrix.getValue(r, c + 1);
                right = MyProblem.matrix.getValue(r, c + 1);
                topLeft = MyProblem.matrix.getValue(r + 1, c + 1);
                topRight = MyProblem.matrix.getValue(r + 1, c + 1);
                bottomLeft = MyProblem.matrix.getValue(r + 1, c + 1);
                bottomRight = MyProblem.matrix.getValue(r + 1, c + 1);
            } else if (c == colsSize - 1) {//Last col
                left = MyProblem.matrix.getValue(r, c - 1);
                right = MyProblem.matrix.getValue(r, c - 1);
                topLeft = MyProblem.matrix.getValue(r + 1, c - 1);
                topRight = MyProblem.matrix.getValue(r + 1, c - 1);
                bottomLeft = MyProblem.matrix.getValue(r + 1, c - 1);
                bottomRight = MyProblem.matrix.getValue(r + 1, c - 1);
            } else {//From 1 to last-1 col
                left = MyProblem.matrix.getValue(r, c - 1);
                right = MyProblem.matrix.getValue(r, c + 1);
                topLeft = MyProblem.matrix.getValue(r + 1, c - 1);
                topRight = MyProblem.matrix.getValue(r + 1, c + 1);
                bottomLeft = MyProblem.matrix.getValue(r + 1, c - 1);
                bottomRight = MyProblem.matrix.getValue(r + 1, c + 1);
            }
        } else if (r == rowsSize - 1) {//Last row
            top = MyProblem.matrix.getValue(r - 1, c);
            bottom = MyProblem.matrix.getValue(r - 1, c);

            if (c == 0) {//First col
                left = MyProblem.matrix.getValue(r, c + 1);
                right = MyProblem.matrix.getValue(r, c + 1);
                topLeft = MyProblem.matrix.getValue(r - 1, c + 1);
                topRight = MyProblem.matrix.getValue(r - 1, c + 1);
                bottomLeft = MyProblem.matrix.getValue(r - 1, c + 1);
                bottomRight = MyProblem.matrix.getValue(r - 1, c + 1);
            } else if (c == colsSize - 1) {//Last col
                left = MyProblem.matrix.getValue(r, c - 1);
                right = MyProblem.matrix.getValue(r, c - 1);
                topLeft = MyProblem.matrix.getValue(r - 1, c - 1);
                topRight = MyProblem.matrix.getValue(r - 1, c - 1);
                bottomLeft = MyProblem.matrix.getValue(r - 1, c - 1);
                bottomRight = MyProblem.matrix.getValue(r - 1, c - 1);
            } else {//From 1 to last-1 col
                left = MyProblem.matrix.getValue(r, c - 1);
                right = MyProblem.matrix.getValue(r, c + 1);
                topLeft = MyProblem.matrix.getValue(r - 1, c - 1);
                topRight = MyProblem.matrix.getValue(r - 1, c + 1);
                bottomLeft = MyProblem.matrix.getValue(r - 1, c - 1);
                bottomRight = MyProblem.matrix.getValue(r - 1, c + 1);
            }
        } else {//From 1 to last-1 row
            if (c == 0) {//First col
                top = MyProblem.matrix.getValue(r - 1, c);
                bottom = MyProblem.matrix.getValue(r + 1, c);
                left = MyProblem.matrix.getValue(r, c + 1);
                right = MyProblem.matrix.getValue(r, c + 1);
                topLeft = MyProblem.matrix.getValue(r - 1, c + 1);
                topRight = MyProblem.matrix.getValue(r - 1, c + 1);
                bottomLeft = MyProblem.matrix.getValue(r + 1, c + 1);
                bottomRight = MyProblem.matrix.getValue(r + 1, c + 1);
            } else if (c == colsSize - 1) {//Last col
                top = MyProblem.matrix.getValue(r - 1, c);
                bottom = MyProblem.matrix.getValue(r + 1, c);
                left = MyProblem.matrix.getValue(r, c - 1);
                right = MyProblem.matrix.getValue(r, c - 1);
                topLeft = MyProblem.matrix.getValue(r - 1, c - 1);
                topRight = MyProblem.matrix.getValue(r - 1, c - 1);
                bottomLeft = MyProblem.matrix.getValue(r + 1, c - 1);
                bottomRight = MyProblem.matrix.getValue(r + 1, c - 1);
            } else {//From 1 to last-1 col
                top = MyProblem.matrix.getValue(r - 1, c);
                bottom = MyProblem.matrix.getValue(r + 1, c);
                left = MyProblem.matrix.getValue(r, c - 1);
                right = MyProblem.matrix.getValue(r, c + 1);
                topLeft = MyProblem.matrix.getValue(r - 1, c - 1);
                topRight = MyProblem.matrix.getValue(r - 1, c + 1);
                bottomLeft = MyProblem.matrix.getValue(r + 1, c - 1);
                bottomRight = MyProblem.matrix.getValue(r + 1, c + 1);
            }
        }

        double medium = (actual + top + bottom + left + right + topLeft + topRight + bottomLeft + bottomRight) / 8.0;
        medium = Math.floor(medium * 100) / 100; //For having just 2 decimals

        return medium;
    }

    public int getR() {
        return r;
    }

    public int getC() {
        return c;
    }
}
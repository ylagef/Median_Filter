import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyProblem {
    public static MyMatrix matrix;
    public static double[][] convertedMatrix;

    static int numThreads, filter;
    public static boolean byCells;

    public static List<Thread> threadList = new ArrayList<>(numThreads); //ArrayList for the created Threads.

    public static void main(String[] args) {
        long initTime = System.currentTimeMillis();
        numThreads = Integer.parseInt(args[0]);
        if (Integer.parseInt(args[1]) == 1) {
            byCells = true;
        }

        System.out.print(numThreads);

        int numRows = Integer.parseInt(args[2]);
        int numCols = Integer.parseInt(args[3]);

        matrix = new MyMatrix(numRows, numCols); //Generates new matrix
        convertedMatrix = new double[numRows][numCols]; //Reserves space

        //matrix.constantFill(1); //Constant fill of matrix
        matrix.randomFill(1000); //Random fill of matrix
        //System.out.println(matrix.toString()); //Print matrix

        //Do the median filter with threads
        filter = 2;
        matrix.medianFilter(); // Filter size on argument

        //Check threads end
        for (Thread t : threadList) { //For each thread, wait until is ended.
            try {
                t.join(); //join() is a function that waits until Thread t is finished.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

/*
        //Print final matrix
        for (double[] d : convertedMatrix) {
            for (Double d1 : d) {
                System.out.print(d1 + " \t");
            }
            System.out.println();
        }
        //Then, this foreach ends when every thread is ended.

        System.out.println("\nProgram of exercise 3 has terminated."); //Print final message
*/

        long endTime = System.currentTimeMillis();
        System.out.print(" " + (endTime - initTime) + "\n");
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
                MyProblem.convertedMatrix[startR][c] = (new Cell(startR, c)).getMedianFilter();
            }
            startR++;
            startC = 0;
            numRows--;
        }

        for (int c = startC; c <= endC; c++) {
            MyProblem.convertedMatrix[endR][c] = (new Cell(endR, c)).getMedianFilter();
        }
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
        int cellsQuantity = this.getRowsSize() * this.getColsSize();
        if (MyProblem.byCells) {
            long init_assignation = System.currentTimeMillis();
            int cellsPerThread;
            if (MyProblem.numThreads >= cellsQuantity) {
                //If more threads than cells, just one thread per cell.
                cellsPerThread = 1;
                MyProblem.numThreads = cellsQuantity;
            } else {
                cellsPerThread = cellsQuantity / MyProblem.numThreads;
            }

            //Assigning cells to threads. By blocks.
            int id = 0;
            int endR, endC, startR = 0, startC = 0;
            int[][] times = new int[MyProblem.numThreads][4];

            while (id != MyProblem.numThreads - 1) { //While not last thread.
                if (startC + cellsPerThread >= getColsSize()) { // If doesnt fit on actual row
                    int cellsForAssign = cellsPerThread - (getColsSize() - startC);
                    int fullRows = cellsForAssign / getColsSize();
                    cellsForAssign = cellsForAssign - getColsSize() * fullRows;
                    if (cellsForAssign == 0) {
                        endC = getColsSize() - 1;
                        endR = startR + fullRows;
                    } else {
                        endC = cellsForAssign - 1;
                        endR = startR + fullRows + 1;
                    }
                } else {
                    endC = startC + cellsPerThread - 1;
                    endR = startR;
                }

                times[id][0] = startR;
                times[id][1] = startC;
                times[id][2] = endR;
                times[id][3] = endC;

                if (endC == getColsSize() - 1) {
                    startC = 0;
                    startR = endR + 1;
                } else {
                    startC = endC + 1;
                    startR = endR;
                }

                id++;
            }

            // Last thread goes until the end
            times[id][0] = startR;
            times[id][1] = startC;
            times[id][2] = getRowsSize() - 1;
            times[id][3] = getColsSize() - 1;

            int threadId = 0;
            for (int[] i : times) {
                MyProblem.threadList.add(threadId, new Thread(new MyThread(i[0], i[1], i[2], i[3]), Integer.toString(threadId)));
                MyProblem.threadList.get(threadId++).start();
            }

            long taken = System.currentTimeMillis() - init_assignation;
            //System.out.println(" " + taken + "\n");
        } else {
            long init_assignation = System.currentTimeMillis();

            int rowsPerThread, usableThreads;
            if (getRowsSize() <= MyProblem.numThreads) {
                rowsPerThread = 1;
                usableThreads = getRowsSize();
            } else {
                rowsPerThread = getRowsSize() / MyProblem.numThreads;
                usableThreads = MyProblem.numThreads;
            }

            int id = 0, startRow = 0, endRow = -1;
            for (int t = 0; t < usableThreads; t++) {
                endRow += rowsPerThread;

                if (t == usableThreads - 1) {
                    MyProblem.threadList.add(id, new Thread(new MyThread(startRow, 0, getRowsSize() - 1, getColsSize() - 1), Integer.toString(id)));
                } else {
                    MyProblem.threadList.add(id, new Thread(new MyThread(startRow, 0, endRow, getColsSize() - 1), Integer.toString(id)));
                }


                MyProblem.threadList.get(id++).start();
                startRow = endRow + 1;
            }
            long taken = System.currentTimeMillis() - init_assignation;
            //System.out.print(" " + taken + "\n");
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

    public double getMedianFilter() {
        int colsSize = MyProblem.matrix.getColsSize();
        int rowsSize = MyProblem.matrix.getRowsSize();

        double total = 0;
        for (int row = this.r - MyProblem.filter; row <= this.r + MyProblem.filter; row++) {
            for (int col = this.c - MyProblem.filter; col <= this.c + MyProblem.filter; col++) {
                if (row < 0 || row >= rowsSize || col < 0 || col >= colsSize) {
                    Cell mirror = new Cell(row, col).getMirror(this);
                    total += MyProblem.matrix.getValue(mirror.getR(), mirror.getC());
                } else {
                    total += MyProblem.matrix.getValue(row, col);
                }
            }
        }

        double divider = Math.pow(2 * MyProblem.filter + 1, 2);

        return Math.ceil((total / divider) * 100) / 100;
    }

    private Cell getMirror(Cell actual) {
        Cell toret = new Cell(0, 0);

        if (r < 0) {
            toret.setR(-r);
        } else if (r < MyProblem.matrix.getRowsSize()) {
            toret.setR(r);
        } else {
            int rOut = actual.getR() - r + actual.getR();
            toret.setR(rOut);
        }

        if (c < 0) {
            toret.setC(-c);
        } else if (c < MyProblem.matrix.getColsSize()) {
            toret.setC(c);
        } else {
            int cOut = actual.getC() - c + actual.getC();
            toret.setC(cOut);
        }

        return toret;
    }

    private int getR() {
        return r;
    }

    private int getC() {
        return c;
    }

    private void setR(int r) {
        this.r = r;
    }

    private void setC(int c) {
        this.c = c;
    }
}
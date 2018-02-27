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

        matrix = new MyMatrix(3, 3); //Generates new matrix
        convertedMatrix = new double[3][3]; //Reserves space
        matrix.constantFill(2); //Fill matrix
        System.out.println(matrix.toString());


        //Do the median filter
        matrix.medianFilter();
        for (double[] d : convertedMatrix) {
            for (Double d1 : d) {
                System.out.print(d1 + " \t");
            }
            System.out.println();
        }

        //Check threads end
        for (Thread t : threadList) { //For each thread, wait until is ended.
            try {
                t.join(); //join() is a function that waits until Thread t is finished.
                //System.out.println("T" + t.getName() + outputs.get(Integer.parseInt(t.getName())).toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Then, this foreach ends when every thread is ended.

        //Print final message
        System.out.println("\nProgram of exercise 3 has terminated.");
    }
}

class MyThread implements Runnable {
    private ThreadLocal<Cell> startCell = new ThreadLocal<>();
    private ThreadLocal<Cell> endCell = new ThreadLocal<>();

    public MyThread(Cell startCell, Cell endCell) {
        this.startCell.set(startCell);
        this.endCell.set(endCell);
    }

    //Code going to be executed by the thread.
    @Override
    public void run() {
        double[][] auxMatrix = new double[MyProblem.matrix.getRowsSize()][MyProblem.matrix.getColsSize()]; // Create auxiliary matrix
        for (int r = startCell.get().getR(); r < endCell.get().getR(); r++) {
            for (int c = startCell.get().getC(); c < endCell.get().getC(); c++) {
                double mediumValue;

                Cell auxCell = new Cell(r, c);

                mediumValue = auxCell.getMedium();

                auxMatrix[r][c] = mediumValue;
            }
        }
        MyProblem.convertedMatrix = auxMatrix;
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

    public double[][] medianFilter() {
        System.out.println("---------------------\n");

        int cellsQuantity = this.getRowsSize() * this.getColsSize();
        int cellsPerThread = cellsQuantity / MyProblem.numThreads;

        int sr = 0, sc = 0, er, ec;
        int id = 0;
        for (int assigned = 0; assigned < cellsQuantity; assigned += cellsPerThread) {
            er = (assigned + cellsPerThread) % this.getColsSize();
            ec = assigned + (cellsPerThread - (er * this.getColsSize()))-1;

            MyProblem.threadList.add(id, new Thread(new MyThread(new Cell(sr, sc), new Cell(er, ec)), Integer.toString(id)));
            MyProblem.threadList.get(id++).start();

            if (ec != this.getColsSize()-1) {
                sr = er;
                sc = ec + 1;
            } else {
                sr = er + 1;
                sc = 0;
            }
        }

        //Create and start Threads. As much as selected by the argument value.
        for (int i = 0; i < MyProblem.numThreads; ++i) {

            MyProblem.threadList.add(i, new Thread(new MyThread(new Cell(1, 2), new Cell(1, 2)), Integer.toString(i)));
            MyProblem.threadList.get(i).start();
        }


        double[][] auxMatrix = new double[this.getRowsSize()][this.getColsSize()]; // Create auxiliary matrix
        for (int r = 0; r < this.getRowsSize(); r++) {
            for (int c = 0; c < this.getColsSize(); c++) {
                double mediumValue;

                Cell auxCell = new Cell(r, c);

                mediumValue = auxCell.getMedium();

                auxMatrix[r][c] = mediumValue;
            }
        }
        return auxMatrix;
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
                toPrint.append(cell).append("\t");
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
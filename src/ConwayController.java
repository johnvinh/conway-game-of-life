import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConwayController {
    ConwayModel model;
    ConwayView view;
    Thread thread;
    private boolean gameRunning = false;
    private int threadSleep = 1000;
    private final int THREAD_SLEEP_INTERVAL = 300;

    public ConwayController(ConwayModel model, ConwayView view) {
        this.model = model;
        this.view = view;
        JButton[][] cells = view.getCells();
        initializeCellControls(cells);
        view.getSetDimensionsButton().addActionListener(new SetDimensionsClick());
        view.getStartButton().addActionListener(new StartButtonClick());
        view.getStopButton().addActionListener(new StopButtonClick());
        view.getSpeedUpButton().addActionListener(new SpeedUpButtonClick());
        view.getSlowDownButton().addActionListener(new SlowDownButtonClick());
    }

    public void initializeCellControls(JButton[][] cells) {
        int dim = model.getDim();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                cells[i][j].addActionListener(new CellButtonClick());
            }
        }
    }

    private class CellButtonClick implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (gameRunning) {
                return;
            }
            JButton target = (JButton) e.getSource();
            if (target.getBackground() == Color.WHITE) {
                target.setBackground(Color.BLACK);
            } else {
                target.setBackground(Color.WHITE);
            }
        }
    }

    private class SetDimensionsClick implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int newDimensions = Integer.parseInt(view.getDimSelection().getText());

            view.getCellLayout().setRows(newDimensions);
            view.getCellLayout().setColumns(newDimensions);
            JPanel panel = view.getCellPanel();
            panel.removeAll();
            view.setCells(new JButton[newDimensions][newDimensions]);
            model.setDim(newDimensions);
            view.initializeCells(newDimensions);
            initializeCellControls(view.getCells());
            panel.revalidate();
            panel.repaint();
        }
    }

    private boolean isAlive(JButton cell) {
        return cell.getBackground() == Color.BLACK;
    }

    private void toggleCell(JButton cell) {
        if (cell.getBackground() == Color.WHITE) {
            cell.setBackground(Color.BLACK);
        } else {
            cell.setBackground(Color.WHITE);
        }
    }
    private int getNumAliveNeighbours(JButton[][] cells, int row, int col) {
        // There are 8 possible adjacent neighbors
        int numNeighbours = 0;
        int[][] rowsCols = {{row, col+1}, {row, col-1}, {row-1, col}, {row+1, col}, {row-1, col-1}, {row-1, col+1},
                {row+1, col-1}, {row+1, col+1}};
        for (int i = 0; i < 8; i++) {
            JButton cell;
            try {
                cell = cells[rowsCols[i][0]][rowsCols[i][1]];
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

            if (isAlive(cell)) {
                numNeighbours++;
            }
        }

        return numNeighbours;
    }

    private class StartButtonClick implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            thread = new Thread(new GameRun());
            thread.start();
            ((JButton) e.getSource()).setEnabled(false);
            view.getSetDimensionsButton().setEnabled(false);
            view.getTicks().setText("0");
        }
    }

    private class StopButtonClick implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            gameRunning = false;
            thread.interrupt();
        }
    }

    private class SpeedUpButtonClick implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (threadSleep >= THREAD_SLEEP_INTERVAL) {
                threadSleep -= THREAD_SLEEP_INTERVAL;
            }
        }
    }

    private class SlowDownButtonClick implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            threadSleep += THREAD_SLEEP_INTERVAL;
        }
    }

    private class GameRun implements Runnable {

        @Override
        public void run() {
            view.getStopButton().setEnabled(true);
            boolean moreTicksNeeded;
            gameRunning = true;
            while (!thread.isInterrupted()) {
                moreTicksNeeded = false;
                JButton[][] cells = view.getCells();
                int dim = model.getDim();
                // One tick of time
                for (int i = 0; i < dim; i++) {
                    for (int j = 0; j < dim; j++) {
                        int numAliveNeighbours = getNumAliveNeighbours(cells, i, j);
                        if (isAlive(cells[i][j])) {
                            // Underpopulation
                            if (numAliveNeighbours < 2) {
                                toggleCell(cells[i][j]);
                                moreTicksNeeded = true;
                            } else if (numAliveNeighbours > 3) {
                                toggleCell(cells[i][j]);
                                moreTicksNeeded = true;
                            }
                        } else {
                            // Reproduction
                            if (numAliveNeighbours == 3) {
                                toggleCell(cells[i][j]);
                                moreTicksNeeded = true;
                            }
                        }
                    }
                }

                int currentTicks = Integer.parseInt(view.getTicks().getText());
                // A tick is one second
                try {
                    Thread.sleep(threadSleep);
                } catch (InterruptedException e) {
                    JOptionPane.showConfirmDialog(null,
                            "Simulation complete in " + currentTicks + " ticks!",
                            "Simulation Complete", JOptionPane.DEFAULT_OPTION);
                    view.getStartButton().setEnabled(true);
                    view.getSetDimensionsButton().setEnabled(true);
                    view.getStopButton().setEnabled(false);
                    gameRunning = false;
                    return;
                }

                if (!moreTicksNeeded) {
                    JOptionPane.showConfirmDialog(null,
                            "Simulation complete in " + currentTicks + " ticks!",
                            "Simulation Complete", JOptionPane.DEFAULT_OPTION);
                    view.getStartButton().setEnabled(true);
                    view.getSetDimensionsButton().setEnabled(true);
                    view.getStopButton().setEnabled(false);
                    gameRunning = false;
                    thread.interrupt();
                    return;
                }
                view.getTicks().setText(String.valueOf(currentTicks + 1));
            }
        }
    }
}

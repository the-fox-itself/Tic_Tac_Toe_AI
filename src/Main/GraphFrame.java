package Main;

import org.ejml.simple.SimpleMatrix;

import javax.swing.*;
import java.awt.*;

import static Main.Main.*;

public class GraphFrame {
    public static JFrame frame = new JFrame();
    
    public static void displayGraph() {
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.add(new DrawPanel());

        frame.setVisible(true);
    }

    public static class Loop extends Thread {
        @Override
        public void run() {
            while (true) {
                frame.repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class DrawPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            for (int layer = 0; layer < neuronValuesMatrices.size(); layer++) {
                int numOfNeurons = neuronValuesMatrices.get(layer).getNumCols();
                for (int neuron = 0; neuron < numOfNeurons; neuron++) {
                    int x = 20+layer*150;
                    int y = (int) (frame.getHeight()/2+(neuron-(double)(numOfNeurons)/2)*30);
                    g.drawOval(x, y, 20, 20);
                    g.drawString(String.valueOf((double) (Math.round(neuronValuesMatrices.get(layer).get(neuron)*100))/100), x, y);
                }
            }

            for (int layer = 0; layer < synapseWeightsMatrices.size(); layer++) {
                int numOfNeurons1 = synapseWeightsMatrices.get(layer).getNumRows();
                for (int neuron1 = 0; neuron1 < numOfNeurons1; neuron1++) {
                    int numOfNeurons2 = synapseWeightsMatrices.get(layer).getNumCols();
                    for (int neuron2 = 0; neuron2 < numOfNeurons2; neuron2++) {
                        double weight = sigmoid(synapseWeightsMatrices.get(layer).get(neuron1, neuron2));
                        if (weight > 0.5)
                            g.setColor(new Color(0, 255, 0, (int) (weight*255)));
                        else
                            g.setColor(new Color(255, 0, 0, (int) (Math.abs(weight)*255)));
                        int x1 = 20+layer*150+20;
                        int y1 = (int) (frame.getHeight()/2+(neuron1-(double)(numOfNeurons1)/2)*30)+10;
                        int x2 = 20+(layer+1)*150;
                        int y2 = (int) (frame.getHeight()/2+(neuron2-(double)(numOfNeurons2)/2)*30)+10;
                        g.drawLine(x1, y1, x2, y2);
                    }
                }
            }

            g.setColor(new Color(0));
            g.drawString("Cost: "+cost, 50, frame.getHeight()-50);

//            g.fillOval((int) (System.nanoTime()/Math.pow(10, 9)-startTime), (int) (50+cost*50), 5, 5);
        }
    }
}

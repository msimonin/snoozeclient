/**
 * Copyright (C) 2010-2012 Eugen Feller, INRIA <eugen.feller@inria.fr>
 *
 * This file is part of Snooze, a scalable, autonomic, and
 * energy-aware virtual machine (VM) management framework.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package org.inria.myriads.snoozeclient.systemtree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.inria.myriads.snoozeclient.systemtree.graph.SystemGraphGenerator;
import org.inria.myriads.snoozeclient.systemtree.transformers.VertexColorTransformer;
import org.inria.myriads.snoozeclient.systemtree.transformers.VertexShapeTransformer;
import org.inria.myriads.snoozeclient.util.BootstrapUtilis;
import org.inria.myriads.snoozecommon.communication.NetworkAddress;
import org.inria.myriads.snoozecommon.communication.groupmanager.GroupManagerDescription;
import org.inria.myriads.snoozecommon.guard.Guard;
import org.inria.myriads.snoozecommon.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System tree visualizer class.
 * 
 * @author Eugen Feller
 */
public final class SystemTreeVisualizer extends JFrame
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(SystemTreeVisualizer.class);
    
    /** Default polling interval .*/
    private static String DEFAULT_POLLING_INTERVAL = "3";
    
    /** Serial. */
    private static final long serialVersionUID = -1877608073494399896L;
        
    /** Visualization viewer. */
    private VisualizationViewer<String, Integer> visualizationViewer_;
    
    /** Bootstrap nodes. */
    private List<NetworkAddress> bootstrapNodes_;
    
    /** Visualization panel. */
    private JPanel visualizationPanel_;
    
    /** Polling interval. */
    private JTextField pollingInterval_;

    /** Terminated variable. */
    private boolean isTerminated_;
    
    /** Running status. */
    private JTextField runningStatus_;

    /** System graph generator. */
    private SystemGraphGenerator graphGenerator_;
    
    /**
     * Constructor.
     * 
     * @param bootstrapNodes   The bootstrap nodes
     * @param graphGenerator   The graph generator
     */
    public SystemTreeVisualizer(List<NetworkAddress> bootstrapNodes,
                                SystemGraphGenerator graphGenerator)
    {
        super("Snooze Hierarchy Visualizer");
        Guard.check(bootstrapNodes, graphGenerator); 
        
        bootstrapNodes_ = bootstrapNodes;
        graphGenerator_ = graphGenerator;
        initializeGUI();
    }

    /**
     * Starts the polling.
     */
    private void startPolling()
    {
        String message = null;
        
        while (!isTerminated_)
        {
            log_.debug("Updating the tree view!");                
            try
            {
                long pollingInterval = 
                    TimeUtils.convertSecondsToMilliseconds(Integer.valueOf(pollingInterval_.getText()));
                Thread.sleep(pollingInterval);
                
                GroupManagerDescription groupLeader = BootstrapUtilis.getGroupLeaderDescription(bootstrapNodes_);
                Forest<String, Integer> graph = graphGenerator_.generateGraph(groupLeader);
                message = "System graph generated!";

                visualizationViewer_ = reinitializeVisualizationViewer(graph);  
                updateGUI();
            }
            catch (InterruptedException exception) 
            {
                message = exception.getMessage();
                log_.debug("Interrupted exception", exception);
            }
            catch (Exception exception) 
            {
                message = exception.getMessage();
                log_.debug("Exception", exception);
            } 
            finally
            {
                if (!isTerminated_)
                {
                    updateStatusMessage(message);
                }
            }
        }
        
        log_.debug("Polling terminated!");
        updateStatusMessage("Stopped");
    }
    
    /**
     * Updates the status message.
     * 
     * @param message       The message
     */
    private void updateStatusMessage(final String message)
    {        
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                runningStatus_.setText(message);
            }
        });         
    }
    
    /**
     * Triggers a GUI update.
     */
    private void updateGUI()
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                log_.debug("Starting to update the GUI!");
                if (visualizationViewer_ != null)
                {                
                    GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visualizationViewer_);
                    visualizationPanel_.removeAll();
                    visualizationPanel_.add(scrollPane, BorderLayout.CENTER);
                    visualizationPanel_.revalidate();
                    visualizationPanel_.repaint();
                }
            }
        });        
    }
    
    /**
     * Initializes the GUI.
     */
    protected void initializeGUI() 
    {
        setResizable(false);
        setPreferredSize(new Dimension(1050, 800));
        setBackground(Color.WHITE);
        setDefaultLookAndFeelDecorated(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               
        visualizationPanel_ = new JPanel();
        JPanel managementPanel = new JPanel();
        managementPanel.setLayout(new GridLayout(0, 3, 0, 0));
        
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("Control"));
        managementPanel.add(controlPanel);
        
        JButton startDrawing = new JButton("Start");
        startDrawing.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent arg0) 
            {
                if (runningStatus_.getText().equals("Stopped"))
                {
                    runningStatus_.setText("Starting...");
                    isTerminated_ = false;
                    Thread updateThread = new Thread() 
                    {
                        public void run() 
                        {
                            startPolling();
                        }
                    };
                    updateThread.start();
                }
            }
        });
        controlPanel.setLayout(new GridLayout(1, 3, 0, 0));
        controlPanel.add(startDrawing);
        
        JButton stopDrawing = new JButton("Stop");
        stopDrawing.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent arg0)  
            {
                isTerminated_ = true;
            }
        });
        controlPanel.add(stopDrawing);
        
        JPanel zoomPanel = new JPanel();
        managementPanel.add(zoomPanel);
        zoomPanel.setLayout(new GridLayout(0, 2, 0, 0));
        
        JButton zoomIn = new JButton("+");
        zoomPanel.add(zoomIn);
        final ScalingControl scaler = new CrossoverScalingControl();
        zoomIn.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent arg0) 
            {
                if (visualizationViewer_ != null)
                {
                    scaler.scale(visualizationViewer_, 1.1f, visualizationViewer_.getCenter());
                } else
                {
                    runningStatus_.setText("No visualization data available");
                }
            }
        });
        
        JButton zoomOut = new JButton("-");
        zoomPanel.add(zoomOut);
        
        zoomOut.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent arg0) 
            {
                if (visualizationViewer_ != null)
                {
                    scaler.scale(visualizationViewer_, 1 / 1.1f, visualizationViewer_.getCenter());
                } else
                {
                    runningStatus_.setText("No visualization data available");
                }
            }
        });
        zoomPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));
        
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                      .addComponent(managementPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 970, 
                                    Short.MAX_VALUE)
                      .addComponent(visualizationPanel_, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 970, 
                                    Short.MAX_VALUE))
                    .addContainerGap())
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(visualizationPanel_, GroupLayout.PREFERRED_SIZE, 650, GroupLayout.PREFERRED_SIZE)
                    .addGap(18)
                    .addComponent(managementPanel, GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addContainerGap())
        );
        
        JPanel pollingPanel = new JPanel();
        managementPanel.add(pollingPanel);
        pollingPanel.setLayout(new GridLayout(0, 1, 0, 0));
        
        pollingInterval_ = new JTextField();
        pollingPanel.add(pollingInterval_);
        pollingInterval_.setHorizontalAlignment(SwingConstants.CENTER);
        pollingInterval_.setBackground(UIManager.getColor("Button.background"));
        pollingInterval_.setText(DEFAULT_POLLING_INTERVAL);
        pollingInterval_.setBorder(BorderFactory.createTitledBorder("Polling Interval"));
        pollingInterval_.setColumns(3);
        
        runningStatus_ = new JTextField();
        runningStatus_.setEditable(false);
        runningStatus_.setHorizontalAlignment(SwingConstants.CENTER);
        runningStatus_.setText("Stopped");
        runningStatus_.setBorder(BorderFactory.createTitledBorder("Status"));
        pollingPanel.add(runningStatus_);
        runningStatus_.setColumns(30);
        
        visualizationPanel_.setLayout(new BorderLayout(0, 0));
        getContentPane().setLayout(groupLayout);
        pack();
    }

    
    /**
     * Reinitializes the visualization viewer.
     * 
     * @param graph     The graph
     * @return          The visualization viewer
     */         
    @SuppressWarnings("unchecked")
    private VisualizationViewer<String, Integer> reinitializeVisualizationViewer(Forest<String, Integer> graph)
    {
        log_.debug("Creating new visualization viewer");       
        RadialTreeLayout radialLayout = new RadialTreeLayout<String, Integer>(graph);
        radialLayout.setSize(new Dimension(700, 400));
        
        VisualizationViewer visualizationViewer = new VisualizationViewer<String, Integer>(radialLayout, 
                                                                                           new Dimension(600, 600));  
        visualizationViewer.setBackground(Color.white);
        visualizationViewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        visualizationViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        visualizationViewer.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray)); 
        visualizationViewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        
        DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        visualizationViewer.setGraphMouse(graphMouse);
        
        Transformer vertexColorTransformer = new VertexColorTransformer();       
        visualizationViewer.getRenderContext().setVertexFillPaintTransformer(vertexColorTransformer);

        Transformer vertexShapeTransformer = new VertexShapeTransformer();
        visualizationViewer.getRenderContext().setVertexShapeTransformer(vertexShapeTransformer);  
        return visualizationViewer;
    }
}

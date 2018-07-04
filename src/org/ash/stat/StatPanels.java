/*
 *-------------------
 * The DetailPanels.java is part of ASH Viewer
 *-------------------
 * 
 * ASH Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASH Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASH Viewer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2009, Alex Kardapolov, All rights reserved.
 *
 */
package org.ash.stat;

import org.ash.stat.StatGantt;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ash.database.ASHDatabase;
import org.ash.gui.StatusBar;
import org.ash.util.Options;
import org.jfree.chart.ChartPanel;

public class StatPanels extends JPanel{

	/** The MainFrame. */
	private JFrame mainFrame;
	
	/** The database. */
	private ASHDatabase database;
	
	/** The tabbed pane */
	private JTabbedPane tabsDetail;
	
	/** The stacked chart main object for Wait Types */
	private StatStackedChart CPUStackedChartMainObjectDetail;
	
	/** The split pane main for Wait Types */
	private JSplitPane CPUSplitPaneMainDetail;
	
	/** The chart chart panel for Wait Types */
	private ChartPanel CPUChartPanel;
	
	/** The top sqls and sessions for Wait Types. */
	private StatGantt CPUSqlsAndSessions;

	 /** The status bar. */
    private StatusBar statusBar;
	
	/** The max cpu. */
	private double maxCpu;
	
	/** The divider. */
	private int dividerLocation = 290;
	
	/**
	 * Constructor DetailFrame
	 * 
	 * @param mainFrame0
	 * @param database0
	 * @param statusBar0
	 */
	public StatPanels(JFrame mainFrame0, ASHDatabase database0, StatusBar statusBar0){
		this.mainFrame = mainFrame0;
		this.database = database0;
		this.statusBar = statusBar0;
		this.tabsDetail = new JTabbedPane();
		
		this.CPUStackedChartMainObjectDetail = new StatStackedChart (this.database, Options.getInstance().getResource("CPULabel.text"));
		
		this.initialize();
	}
	
	/**
	 * Initialize DetailFrame
	 */
	private void initialize() {
		
		this.setLayout(new BorderLayout());
		this.setVisible(true);
		
		this.CPUSplitPaneMainDetail = new JSplitPane();
		
		this.CPUChartPanel = this.CPUStackedChartMainObjectDetail.createChartPanel();
		
		/** Gantt graph */
		this.CPUSqlsAndSessions = new StatGantt(this.mainFrame, this.database, Options.getInstance().getResource("CPULabel.text"));
		
		this.CPUSplitPaneMainDetail.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.CPUSplitPaneMainDetail.add(this.CPUChartPanel, "top");
		this.CPUSplitPaneMainDetail.add(this.CPUSqlsAndSessions, "bottom");
		this.CPUSplitPaneMainDetail.setDividerLocation(this.dividerLocation);
		this.CPUSplitPaneMainDetail.setOneTouchExpandable(true);

		
		// this.tabsDetail.add(this.CPUSplitPaneMainDetail, Options.getInstance().getResource("CPULabel.text"));
		
		this.CPUChartPanel.addListenerReleaseMouse(this.CPUSqlsAndSessions);
		
		this.CPUChartPanel.addListenerReleaseMouse(this.statusBar);
	
		ChangeListener changeListener = new ChangeListener() {
		      public void stateChanged(ChangeEvent changeEvent) {
		        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
		        int index = sourceTabbedPane.getSelectedIndex();
		        statusBar.updateLabelStringDetail(
		        		sourceTabbedPane.getTitleAt(index));
		      }
		    };
		 // tabsDetail.addChangeListener(changeListener);
		
		// this.add(tabsDetail, BorderLayout.CENTER);
		this.add(this.CPUSplitPaneMainDetail, BorderLayout.CENTER);
	}
	
	/**
	 * Load data to dataset
	 */
	public void loadDataToDataSet(){
		// this.database.saveStackedXYAreaChartDetail(this.CPUStackedChartMainObjectDetail, Options.getInstance().getResource("CPULabel.text"));
		
		this.database.initialLoadingDataToChartPanelDataSetDetail();
		
		this.CPUStackedChartMainObjectDetail.setTitle();

		this.setThresholdMaxCpu();
	}
	
	/**
	 * Set the threshold max cpu (detail).
	 * @param maxCpu
	 */
	public void setThresholdMaxCpu(Double maxCpu){
		this.maxCpu = maxCpu;
	}
	
	/**
     * Set upper bound of range axis (detail)
     * @param bound
     */
    public void setUpperBoundOfRangeAxis(double bound){
	    	this.CPUStackedChartMainObjectDetail.setUpperBoundOfRangeAxis(bound);
    }
	
    /**
     * Get thumbnail details charts
     * 
     * @return
     */
    public JPanel getThumbnailDetailPanel(){
    	JPanel mainPanel = new JPanel(new GridLayout(3, 5));
    	
    	BufferedImage thumbCPU = this.CPUStackedChartMainObjectDetail.createBufferedImage(120, 80, 360, 240,null);
    	ImageIcon imageCPU = new ImageIcon(thumbCPU);
        mainPanel.add(new JButton(imageCPU));

        
        mainPanel.add(new JPanel());
        
    	return mainPanel;
    }
    
	 /**
     * Sets the threshold max cpu for all charts.
     */
    private void setThresholdMaxCpu(){  	
	    	this.CPUStackedChartMainObjectDetail.setThresholdMaxCpu(this.maxCpu);
      }
    
   /**
	* Update xAxis label.
	* @param time the time
	*/
   public void updatexAxisLabel(double time){  	  
	   this.CPUStackedChartMainObjectDetail.updatexAxisLabel(time);
   }
    
   /**
    * Set top sql value
    * @param value
    */
   public void setTopSqlsSqlText(int value){
	   this.CPUSqlsAndSessions.setTopSqlsSqlText(value);
   }
   
   /**
    * Select top sql plan
    * 
    * @param value
    */
   public void setSelectSqlPlan(boolean isSelect){
	   this.CPUSqlsAndSessions.setSelectSqlPlan(isSelect);
   }
   
   /**
    * Get name of selected tab
    * @return name
    */
   public String getCurrentTabName(){
	   return this.tabsDetail.getTitleAt(this.tabsDetail.getSelectedIndex());
   }
}

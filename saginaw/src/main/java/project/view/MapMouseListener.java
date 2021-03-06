package project.view;

import project.controller.Controller;
import project.model.Model;

import javax.swing.event.MouseInputListener;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

/**
 * User: Alan P. Sexton Date: 21/06/13 Time: 00:52
 */
class MapMouseListener implements MouseInputListener

{
	Model		model;
	View		view;
	Controller	controller;

	int			x1;
	int			y1;
	int			x2;
	int			y2;
	boolean		mouseDown	= false;
	boolean draggingMap = true;

	public MapMouseListener(Model model, View view, Controller controller)
	{
		this.model = model;
		this.view = view;
		this.controller = controller;
	}

	public void paint(Graphics g)
	{
		if (mouseDown)
		{
//			Color col = g.getColor();
//			g.setColor(Color.RED);
//			if (x1 <= x2)
//			{
//				if (y1 <= y2)
//					g.drawRect(x1, y1, x2 - x1, y2 - y1);
//				else
//					g.drawRect(x1, y2, x2 - x1, y1 - y2);
//			}
//			else
//			{
//				if (y1 <= y2)
//					g.drawRect(x2, y1, x1 - x2, y2 - y1);
//				else
//					g.drawRect(x2, y2, x1 - x2, y1 - y2);
//			}
//			g.setColor(col);

//			controller.moveMap(x2 - x1, y2 - y1);
			view.getMapPane().repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
//		long startTime = System.nanoTime();
		if(e.getButton() == 3){
//			System.out.println(System.nanoTime());
//			long startTime = System.nanoTime();
			double[] loc = view.getClickCoordinate(e.getX(), e.getY());
//			long endTime = System.nanoTime();
//			System.out.println("getClick: " + (((float) endTime - (float)startTime) / 1000000000));
//			System.out.println(loc[0] + " " + loc[1]);
			model.getRoute().addToEnd(loc);
//			System.out.println("getClick: " + (((float) endTime - (float)startTime) / 1000000000));
			view.repaint();
			view.updateInfo();
		}
//		long endTime = System.nanoTime();
//		System.out.println("mouseClicked: " + (((float) endTime - (float)startTime) / 1000000000));
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		draggingMap = !model.clickedRoute(view.getMapPane().canvasToGeo(e.getX(), e.getY()));
		if (!model.isActive())
			return;
		x1 = e.getX();
		y1 = e.getY();
		mouseDown = true;
		view.getMapPane().addMouseMotionListener(this);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (!model.isActive())
			return;
		view.getMapPane().removeMouseMotionListener(this);
		mouseDown = false;
		x2 = e.getX();
		y2 = e.getY();
		controller.moveMap(x2 - x1, y2 - y1);
		model.getRoute().endAlteration();
		model.loadFullRoute();
		view.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		x2 = e.getX();
		y2 = e.getY();
		if(draggingMap) {
			controller.moveMap(-(x2 - x1), y2 - y1);
		} else {
			double[] loc = view.getClickCoordinate(e.getX(), e.getY());
//			System.out.println(loc[0] + " " + loc[1]);
//            long startTime = System.nanoTime();
			model.getRoute().makeAdjustment(loc);
//			long endTime = System.nanoTime();
//            System.out.println("Route time: " + (((float) endTime - (float)startTime) / 1000000000));
//			System.out.println(model.getMarkers().get(1)[0] + " " + model.getMarkers().get(1)[1]);
		}
		x1 = x2;
		y1 = y2;
		view.getMapPane().repaint();
		view.updateInfo();
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}


}

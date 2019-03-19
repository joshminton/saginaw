package project.view;

import project.controller.Controller;
import project.douglas.DouglasPeucker;
import project.map.MyMap2;
import project.map.Place;
import project.model.Model;
import project.model.Route;
import project.model.Segment;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.MouseEvent;

/**
 * User: Alan P. Sexton Date: 20/06/13 Time: 18:00
 */
class MapPane extends JPanel
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7552020673193783095L;
	private Model				model;
	private View				view;

	private CanvasMouseListener	mouseListener;
	private CanvasMouseWheelListener mouseWheelListener;
	private CanvasKeyboardListener keyListener;

	private BufferedImage image;

	private Tile[][] tileGrid;
	private HashMap<Integer, Tile[][]> layers;

	private Point2D centre;
	private Point2D origin;
	private Point2D topLeft, bottomRight;
	private double scale;
	private double zoom;
	private double oX, oY;
	private double imageEdge;
	private int paneX = 1200;
	private int paneY = 800;
	private int xDimension, yDimension;
	private int level, modifier;
	private double dougTolerance;
	private boolean simplifyRoute;
	private boolean grid;

	private BufferedImage start, middle, end;

	/**
	 * The default constructor should NEVER be called. It has been made private
	 * so that no other class can create a MapPane except by initialising it
	 * properly (i.e. by calling the parameterised constructor)
	 */
	@SuppressWarnings("unused")
	private MapPane()
	{
	}

	/**
	 * Create a <code>MapPane</code> object initialised to the given
	 * <code>View</code> and <code>Model</code>
	 *
	 * @param view
	 *            The View object that encapsulates the whole GUI
	 * @param model
	 *            The Model object that encapsulates the (view-independent) data
	 *            of the application
	 * @param controller
	 *            The Controller object that handles all operations
	 */
	public MapPane(Model model, View view, Controller controller)
	{
		this.view = view;
		this.model = model;
		mouseListener = new CanvasMouseListener(this.model, this.view,
				controller);
		mouseWheelListener = new CanvasMouseWheelListener(this.model, this.view, controller);
		keyListener = new CanvasKeyboardListener(view, controller);
		addMouseListener(mouseListener);
		addMouseWheelListener(mouseWheelListener);
		addKeyListener(keyListener);
		this.setSize((int) paneX, (int) paneY);
		origin = model.getOrigin();
		layers = new HashMap<Integer, Tile[][]>();
		imageEdge = model.getImageEdge();

		image = new BufferedImage(paneX, paneY, 1);

		simplifyRoute = false;

		grid = false;

		try{
			String filename = "res/icon/start.png";
			File inputfile = new File(filename);
			start = ImageIO.read(inputfile);
			filename = "res/icon/middle.png";
			inputfile = new File(filename);
			middle = ImageIO.read(inputfile);
            filename = "res/icon/finish.png";
            inputfile = new File(filename);
            end = ImageIO.read(inputfile);
		}catch(IOException e){
			System.out.println("Failed image load.");
		}

		xDimension = model.getMap().getTileWidth();
		yDimension = model.getMap().getTileHeight();
		this.scale = model.getScale().doubleValue();
		for(int l = 1; l < (MyMap2.MAX_LEVEL); l *= 2){
			tileGrid = new Tile[(int) Math.ceil(xDimension / (double) l)][(int) Math.ceil(yDimension / (double) l)];
			TileManager tm = new TileManager(tileGrid, this);
			if(l == 1){
				Thread t = new Thread(tm);
				t.setPriority(Thread.MIN_PRIORITY);
				t.start();
			}
//			System.out.println(l + " is " +  (int) Math.ceil(xDimension / (double) l) + ", " + (int) Math.ceil(yDimension / (double) l));
			for(int x = 0; x < tileGrid.length; x++){
				for(int y = 0; y < tileGrid[0].length; y++){
					tileGrid[x][y] = new Tile((l * x), (l * y), l, scale, imageEdge, model.getRegion());
					Point2D.Double topLeft = new Point2D.Double(origin.getX() + ((imageEdge / (scale / l)) * x), origin.getY() - ((imageEdge / (scale / l)) * y));
					Point2D.Double bottomRight = new Point2D.Double(topLeft.getX() + (imageEdge / (scale / l)), topLeft.getY() - (imageEdge / (scale / l)));
					tileGrid[x][y].setTopLeftAndBottomRight(topLeft, bottomRight);
				}
			}
			layers.put(l, tileGrid);
//			System.out.println();
		}

		dougTolerance = 1;
	}

	/**
	 * The method that is called to paint the contents of this component
	 *
	 * @param gOld
	 *            The <code>Graphics</code> object used to do the actual drawing
	 */
	protected void paintComponent(Graphics gOld)
	{
		super.paintComponent(gOld);

		Graphics g = image.getGraphics();

		centre = model.getCentre();
		scale = model.getScale().doubleValue();
		zoom = model.getZ();
		topLeft = new Point2D.Double((centre.getX() - (((paneX / 2) * zoom) / scale)), (centre.getY() + (((paneY / 2) * zoom) / scale)));
		bottomRight = new Point2D.Double((centre.getX() + (((paneX / 2) * zoom) / scale)), (centre.getY() - (((paneY / 2) * zoom) / scale)));

//		System.out.println("MapPane zoom: " + zoom);

		g.setColor(new Color(153, 204, 255));
		g.fillRect(0, 0, paneX, paneY);


		Tile t;
		Point2D.Double p, tl;

		boolean flag = true;											//would it be more efficient to declare this outside the method? Ask generally!!!

		level = model.getLevel();
//		System.out.println("MapPane level: " + level);
		modifier = level;

		tileGrid = layers.get(modifier);

		LOOP: for(int x = 0; x < tileGrid.length; x++){
			for(int y = 0; y < tileGrid[0].length; y++){
				if(tileGrid[x][y].overlaps(topLeft, bottomRight)){
//					System.out.println("VISIBLE " + x + " " + y);
					flag = true;
					t = tileGrid[x][y];
//					System.out.println(topLeft + " " + bottomRight);
					p = geoToCanvas(t.getTopLeft());
//					System.out.println(p);
					g.drawImage(t.getImage(), (int) p.getX(), (int) p.getY(), (int) (imageEdge / (zoom / modifier)), (int) (imageEdge / (zoom / modifier)), null);
					if(grid){
						g.setColor(Color.RED);
						g.drawRect((int) p.getX(), (int) p.getY(), (int) (imageEdge / (zoom / modifier)), (int) (imageEdge / (zoom / modifier)));
					}
				}
			}
		}

		drawMarkers((Graphics2D) g);

		g.setColor(Color.RED);
		((Graphics2D) g).setStroke(new BasicStroke(6));


        if(model.getRoute().hasRoute()){
            drawRoute(model.getRoute(), (Graphics2D) g);
        }

//        drawPlaces();


		if (model.isActive())
		{
			// Draw the display image on the full size mapPane
//			g2.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);

			// In case there is some animation going on (e.g. mouse dragging),
			// call this to
			// paint the intermediate images
			mouseListener.paint(g);
		}

		gOld.drawImage(image, 0, 0, null);
	}

	/**
	 * Get the preferred size of the mapPane.
	 * 
	 * @return The <code>Dimension</code> object containing the size of the
	 *         underlying image in the model, if one exits, or
	 *         <code>(0,0)</code> if it does not.
	 */
	public Dimension getPreferredSize()
	{
		return model.getDimensions();
	}

	public void drawRoute(Route route, Graphics2D g){

		Color startColor = Color.GREEN;
		Color endColor = Color.RED;
		int segments = route.getSegments().size();
		int rDiff, gDiff, bDiff;
		if(segments > 1){
			rDiff = (endColor.getRed() - startColor.getRed()) / (segments - 1);
			gDiff = (endColor.getGreen() - startColor.getGreen()) / (segments - 1);
			bDiff = (endColor.getBlue() - startColor.getBlue()) / (segments - 1);
		} else {
			rDiff = 0;
			gDiff = 0;
			bDiff = 0;
		}
//
		int x = 0;
		for(Segment segment : route.getSegments()) {
		    if(segment.hasRoute()){
		    	ArrayList<Point2D.Double> segmentPoints = segment.getPoints();
		    	if(simplifyRoute){
		    		segmentPoints = DouglasPeucker.simplify(segment.getPoints(), dougTolerance);
				}
				Path2D path = new Path2D.Double();
				Point2D first = geoToCanvas(segmentPoints.get(0));
				path.moveTo((int) first.getX(), (int) first.getY());
				for(Point2D.Double point : segmentPoints){
					point = geoToCanvas(point);
					path.lineTo((int) point.getX(), (int) point.getY());
				}
				Color segmentColor = new Color(startColor.getRed() + (rDiff * x), startColor.getGreen() + (gDiff * x), startColor.getBlue() + (bDiff * x), 175);
				g.setColor(segmentColor.darker());
				g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.draw(path);
				segmentColor = new Color(startColor.getRed() + (rDiff * x), startColor.getGreen() + (gDiff * x), startColor.getBlue() + (bDiff * x), 175);
				g.setColor(segmentColor);
				g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.draw(path);
            }
            x++;
		}

		long endTime = System.nanoTime();
//		System.out.println("drawRoute: " + (((float) endTime - (float)startTime) / 1000000000));
//		System.out.print(System.nanoTime());
	}

	public void drawMarkers(Graphics2D g){
		boolean first = true;
		for(double[] m : model.getRoute().getWaypoints()){
//			System.out.println(m[0] + m[1]);
			Point2D marker = geoToCanvas(m);
//			System.out.println(marker.getX() + " " + marker.getY());
			if(model.getRoute().getWaypoints().indexOf(m) == 0){
				g.drawImage(start, (int) marker.getX() - (start.getWidth() / 2), (int) marker.getY() - start.getHeight(), start.getWidth(), start.getHeight(), null, null);
			} else if(model.getRoute().getWaypoints().indexOf(m) == (model.getRoute().getWaypoints().size() - 1)){
				g.drawImage(end, (int) marker.getX() - (end.getWidth() / 2), (int) marker.getY() - end.getHeight(), end.getWidth(), end.getHeight(), null, null);
			} else {
                g.drawImage(middle, (int) marker.getX() - (end.getWidth() / 2), (int) marker.getY() - end.getHeight(), end.getWidth(), end.getHeight(), null, null);
            }
		}
	}

	public void drawPlaces(){
		if(zoom <= 32) {
			for (Place p : model.getMap().getTowns()) {
				Point2D loc = geoToCanvas(p.getLocation());
				if(loc.getX() < paneX + 100 && loc.getX() > -100 && loc.getY() < paneY + 100 && loc.getY() > -100){
					writeTextNicely(p.getName(), loc);
				}

			}
		} else {
			for(Place p : model.getMap().getCities()){
				Point2D loc = geoToCanvas(p.getLocation());
				if(loc.getX() < paneX + 100 && loc.getX() > -100 && loc.getY() < paneY + 100 && loc.getY() > -100){
					writeTextNicely(p.getName(), loc);
				}
			}
		}

	}

	public void writeTextNicely(String text, Point2D location){
		Graphics2D g = (Graphics2D) image.getGraphics();
		Font f = new Font("Montserrat-Regular", Font.BOLD, 14);

		GlyphVector glyphVector = f.createGlyphVector(g.getFontRenderContext(), text);
		Rectangle2D box = glyphVector.getVisualBounds();

		double x = location.getX() - box.getWidth() / 2;
		double y = location.getY() - box.getHeight() / 2;
		// create a glyph vector from your text

		AffineTransform transform = g.getTransform();
		transform.translate(x, y);
		g.transform(transform);


		// get the shape object
		Shape textShape = glyphVector.getOutline();

		// activate anti aliasing for text rendering (if you want it to look nice)
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(3.0f));
		g.draw(textShape); // draw outline
		g.setColor(Color.BLACK);
		g.fill(textShape); // fill the shape

	}

	public Point2D.Double geoToCanvas(Point2D geoCoord){
		Double x = (geoCoord.getX() - topLeft.getX()) * (scale / zoom);
		Double y = (topLeft.getY() - geoCoord.getY()) * (scale / zoom);
		return new Point2D.Double(x, y);
	}

	public Point2D.Double geoToCanvas(double[] geoCoord){
		Double x = (geoCoord[0] - topLeft.getX()) * (scale / zoom);
		Double y = (topLeft.getY() - geoCoord[1]) * (scale / zoom);
		return new Point2D.Double(x, y);
	}

	public double[] canvasToGeo(int x, int y) {
		double geoX = topLeft.getX() + (x / (scale / zoom));
		double geoY = topLeft.getY() - (y / (scale / zoom));
		return new double[]{geoX, geoY};
	}

	public void toggleDoug(){
		simplifyRoute = !simplifyRoute;
	}

	public void downDoug(){
		dougTolerance /= 1.25;
		System.out.println(dougTolerance);
	}

	public void upDoug() {
		dougTolerance *= 1.25;
		System.out.println(dougTolerance);}

	public double[] getClickCoordinate(int x, int y){
		return canvasToGeo(x, y);
	}

	public Point2D getTopLeft(){
		return topLeft;
	}

	public Point2D getBottomRight(){
		return bottomRight;
	}

	protected void changeMapSize(int width, int height){
		image = new BufferedImage(width, height, 1);
		this.paneX = width;
		this.paneY = height;
	}

	protected void toggleGrid(){
		grid = !grid;
		repaint();
	}
}

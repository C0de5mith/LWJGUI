package lwjgui.scene.control;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector2d;
import org.joml.Vector4d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

import lwjgui.Color;
import lwjgui.Context;
import lwjgui.LWJGUI;
import lwjgui.LWJGUIWindow;
import lwjgui.collections.ObservableList;
import lwjgui.event.ChangeEvent;
import lwjgui.event.MouseEvent;
import lwjgui.geometry.Orientation;
import lwjgui.geometry.Pos;
import lwjgui.scene.Node;
import lwjgui.scene.layout.StackPane;
import lwjgui.theme.Theme;

public class SplitPane extends Control {
	private Orientation orientation;
	private ArrayList<Divider> dividers = new ArrayList<Divider>();
	private ArrayList<DividerNode> divider_nodes = new ArrayList<DividerNode>();
	private HashMap<Divider,Integer> divider_cache = new HashMap<Divider,Integer>();
	private ObservableList<Node> items = new ObservableList<Node>();
	private StackPane divider_holder;
	private int dividerThickness = 6;
	
	private Divider grabbedDivider;
	private Divider hovered;
	
	public SplitPane() {
		this.setAlignment(Pos.TOP_LEFT);
		
		this.divider_holder = new StackPane();
		this.divider_holder.setFillToParentHeight(true);
		this.divider_holder.setFillToParentWidth(true);
		this.divider_holder.setBackground(Color.TRANSPARENT);
		this.children.add(divider_holder);
		
		this.items.setAddCallback(new ChangeEvent<Node>() {
			@Override
			public void onEvent(Node changed) {
				recalculateDividers();
			}
		});
		
		this.items.setRemoveCallback(new ChangeEvent<Node>() {
			@Override
			public void onEvent(Node changed) {
				recalculateDividers();
			}
		});
		
		this.mouseReleasedEvent = new MouseEvent() {
			@Override
			public void onEvent(int button) {
				System.out.println("A");
			}
		};
		
		this.setOrientation(Orientation.VERTICAL);
	}
	
	@Override
	public void position(Node parent) {
		super.position(parent);
		
		grabDividers();
	}
	
	private void clickDividers() {
		if ( hovered == null ) {
			return;
		}
		
		LWJGUIWindow window = LWJGUI.getWindowFromContext(GLFW.glfwGetCurrentContext());
		Context context = window.getContext();
		double mx = context.getMouseX();
		double my = context.getMouseY();
		
		grabbedDivider = hovered;
		mouseGrabLocation.set(mx, my);
	}
	
	private Divider getDividerUnderMouse() {
		LWJGUIWindow window = LWJGUI.getWindowFromContext(GLFW.glfwGetCurrentContext());
		Context context = window.getContext();
		double mx = context.getMouseX();
		double my = context.getMouseY();
		
		for (int i = 0; i < dividers.size(); i++) {
			Divider d = dividers.get(i);
			Vector4d bounds = getDividerBounds(d);
			//bounds.add(this.getAbsoluteX(), this.getAbsoluteY(), 0, 0);
			//mx += this.getAbsoluteX();
			//my += this.getAbsoluteY();
			
			if ( mx > bounds.x && mx < bounds.x+bounds.z && my > bounds.y && my < bounds.y+bounds.w) {
				return d;
			}
		}
		
		return null;
	}
	
	private boolean click = false;
	private boolean released = true;
	private Vector2d mouseGrabLocation = new Vector2d();
	private void grabDividers() {
		
		// Get mouse pressed
		int mouse = GLFW.glfwGetMouseButton(GLFW.glfwGetCurrentContext(), GLFW.GLFW_MOUSE_BUTTON_LEFT);
		
		// Check if we're clicking
		if ( !click && mouse == GLFW.GLFW_PRESS && released )
			click = true;
		else if ( click && mouse == GLFW.GLFW_PRESS) {
			released = false;
			click = false;
		} else if ( mouse != GLFW.GLFW_PRESS )
			released = true;
		
		if ( click ) {
			clickDividers();
		}
		
		if ( grabbedDivider == null )
			return;
		
		// If mouse not pressed, not holding divider
		if ( mouse != GLFW.GLFW_PRESS ) {
			grabbedDivider = null;
			return;
		}
		
		// Get mouse coordinates
		LWJGUIWindow window = LWJGUI.getWindowFromContext(GLFW.glfwGetCurrentContext());
		Context context = window.getContext();
		double mx = context.getMouseX() - mouseGrabLocation.x;
		double my = context.getMouseY() - mouseGrabLocation.y;
		
		// If we're holding onto a divider
		double pChange = pixelSpaceToDividerSpace(mx);
		if ( this.orientation == Orientation.HORIZONTAL ) 
			pChange = pixelSpaceToDividerSpace((int) my);
		
		this.setDividerPosition(divider_cache.get(grabbedDivider), grabbedDivider.position+pChange);
		mouseGrabLocation.add(mx, my); 
	}

	@Override
	protected void resize() {
		super.resize();

		float filledLen = 0;
		double maxLen = this.getWidth();
		if ( this.orientation.equals(Orientation.HORIZONTAL) ) {
			maxLen = this.getHeight();
		}
		
		for (int i = 0; i < divider_nodes.size(); i++) {
			DividerNode d = this.divider_nodes.get(i);
			double left = 0;
			double right = 1;
			double subt = 0;
			
			// Has a left divider
			if ( i > 0 ) {
				left = this.dividers.get(i-1).position;
				subt += dividerThickness/2f;
			}
			// Has a right divider
			if ( i < divider_nodes.size()-1 ) {
				right = this.dividers.get(i).position;
				subt += dividerThickness/2f;
			}
			
			// Calculate length of divider node
			double len = ((right-left)*maxLen) - subt;
			double t = Math.ceil(len-0.25); // Round up to eliminate rounding issues.
			
			if ( d.getChildren().size() > 0 )
				d.setAlignment(d.getChildren().get(0).getAlignment());
			
			// Set size
			if ( this.orientation.equals(Orientation.VERTICAL) ) {
				d.setFillToParentWidth(false);
				d.setFillToParentHeight(true);
				d.setPrefWidth(t);
				d.setMinWidth(t);
				d.setMaxWidth(t);
				d.setLocalPosition(divider_holder, filledLen, 0);
			} else {
				d.setFillToParentWidth(true);
				d.setFillToParentHeight(false);
				d.setPrefHeight(t);
				d.setMinHeight(t);
				d.setMaxHeight(t);
				d.setLocalPosition(divider_holder, 0, filledLen);
			}
			
			filledLen += len + dividerThickness;
		}
	}

	public ObservableList<Node> getItems() {
		return items;
	}
	
	public void setOrientation( Orientation orientation ) {
		this.orientation = orientation;
		
		// Re add dividers into holder
		this.recalculateDividers();
	}
	
	@Override
	public boolean isResizeable() {
		return false;
	}
	
	private Vector4d getDividerBounds(Divider d) {
		double percent = d.getPosition();
		int dividerWidth = dividerThickness;
		int dividerHeight = (int) getHeight();
		int dividerX = (int) ((getAbsoluteX() + getWidth()*percent)-(dividerThickness/2d));
		int dividerY = (int) getAbsoluteY();
		if ( orientation.equals(Orientation.HORIZONTAL) ) {
			dividerWidth = (int) getWidth();
			dividerHeight = dividerThickness;
			dividerX = (int) getAbsoluteX();	
			dividerY = (int) ((getAbsoluteY() + getHeight()*percent)-(dividerThickness/2d));
		}
		
		return new Vector4d(dividerX, dividerY, dividerWidth, dividerHeight);
	}
	
	private void recalculateDividers() {
		this.divider_cache.clear();
		ArrayList<Divider> t = new ArrayList<Divider>();
		int amtDiv = this.items.size()-1;
		for (int i = 0; i < amtDiv; i++) {
			Divider d = new Divider();
			d.setPosition((i+1)/(double)(amtDiv+1));
			t.add(d);
			this.divider_cache.put(d, i);
		}
		dividers = t;
		
		synchronized(divider_nodes) {
			this.divider_holder.getChildren().clear();
			this.divider_nodes.clear();
			for (int i = 0; i < items.size(); i++) {
				DividerNode dn = new DividerNode(items.get(i));
				this.divider_nodes.add(dn);
				this.divider_holder.getChildren().add(dn);
			}
		}
		
		resize();
		/*ObservableList<Node> n = new ObservableList<Node>();
		ObservableList<Node> old = this.children;
		for (int i = 0; i < old.size(); i++) {
			n.add(old.get(i));
		}
		for (int i = 0; i < t.size(); i++) {
			n.add(new DividerNode(t.get(i)));
		}
		this.divider_cache = n;*/
	}
	
    private double pixelSpaceToDividerSpace(double mx) {
    		double maxLen = getWidth();
    		if ( this.orientation.equals(Orientation.HORIZONTAL) )
    			maxLen = getHeight();
    		
    		return mx/maxLen;
	}

	@Override
	public void render(Context context) {
		long vg = context.getNVG();
		
		for (int i = 0; i < children.size(); i++) {
			// Clip to my bounds
			clip(context);
			
			// Draw child
			Node child = children.get(i);
			child.render(context);
		}

		clip(context);
		for (int i = 0; i < dividers.size(); i++) {
			Divider divider = dividers.get(i);
			Vector4d bounds = getDividerBounds(divider);
			
			// Main bar
			hovered = getDividerUnderMouse();
			Color col = Theme.currentTheme().getControlOutline();
			NanoVG.nnvgBeginPath(vg);
			NanoVG.nvgFillColor(vg, col.getNVG());
			NanoVG.nvgRect(vg, (int)bounds.x, (int)bounds.y, (int)bounds.z, (int)bounds.w);
			NanoVG.nvgFill(vg);
			
			// Inner Gradient
			NanoVG.nvgTranslate(vg, (int)bounds.x, (int)bounds.y);
				if ( this.orientation.equals(Orientation.VERTICAL) ) {
					NVGPaint bg = NanoVG.nvgLinearGradient(vg, 0, 0, (int)bounds.z, 0, Theme.currentTheme().getControlHover().getNVG(), Theme.currentTheme().getControlOutline().getNVG(), NVGPaint.calloc());
					NanoVG.nvgBeginPath(vg);
					NanoVG.nvgRect(vg, 1, 0, (int)bounds.z-2,(int)bounds.w);
					NanoVG.nvgFillPaint(vg, bg);
					NanoVG.nvgFill(vg);
				} else {
					NVGPaint bg = NanoVG.nvgLinearGradient(vg, 0, 0, 0, (int)bounds.w, Theme.currentTheme().getControlHover().getNVG(), Theme.currentTheme().getControlOutline().getNVG(), NVGPaint.calloc());
					NanoVG.nvgBeginPath(vg);
					NanoVG.nvgRect(vg, 0, 1, (int)bounds.z,(int)bounds.w-2);
					NanoVG.nvgFillPaint(vg, bg);
					NanoVG.nvgFill(vg);
				}
			NanoVG.nvgTranslate(vg, (int)-bounds.x, (int)-bounds.y);
		}
		
		Color outlineColor = Theme.currentTheme().getControlOutline();
		NanoVG.nvgBeginPath(vg);
		NanoVG.nvgRect(vg, (int)this.getAbsoluteX(), (int)this.getAbsoluteY(), (int)getWidth(), (int)getHeight());
		NanoVG.nvgStrokeColor(vg, outlineColor.getNVG());
		NanoVG.nvgStrokeWidth(vg, 1f);
		NanoVG.nvgStroke(vg);
	}
	
	public void setDividerPosition( int index, double position ) {
		Divider d = dividers.get(index);
		
		// Get left/right dividers
		Divider left = null;
		Divider right = null;
		if ( index > 0 ) 
			left = dividers.get(index-1);
		if ( index < dividers.size()-1 )
			right = dividers.get(index+1);
		
		// Get divider thickness in divider space
		double dthick = pixelSpaceToDividerSpace(dividerThickness);
		
		// Get min max bounds
		double minPos = dthick/2d;
		double maxPos = 1-dthick/2d;
		if ( left != null )
			minPos = left.position+dthick;
		if ( right != null )
			maxPos = right.position-dthick;
		
		// Clamp position
		position = Math.min( maxPos, Math.max(minPos, position) );
		
		// Set position
		d.position = position;
		
		// Reposition divider nodes
		resize();
	}

	/**
     * Represents a single divider in the SplitPane.
     * @since JavaFX 2.0
     */
    public static class Divider {
        private double position = 0.5;
        public final void setPosition(double value) {
            position = value;
        }

        public final double getPosition() {
            return position;
        }
    }
    
    static class DividerNode extends StackPane {
    	
		public DividerNode(Node node) {
			this.getChildren().add(node);
			this.setFillToParentWidth(true);
			this.setFillToParentHeight(true);
			this.flag_clip = true;
		}

		@Override
		public boolean isResizeable() {
			return false;
		}
    }
}
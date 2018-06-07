package lwjgui.scene.control;

import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

import lwjgui.Color;
import lwjgui.Context;
import lwjgui.collections.ObservableList;
import lwjgui.event.ChangeEvent;
import lwjgui.geometry.Pos;
import lwjgui.scene.layout.VBox;
import lwjgui.theme.Theme;

public class ContextMenu extends PopupWindow {
	private VBox internalBox;
	private ContextMenu childMenu;
	
	private ObservableList<MenuItem> items = new ObservableList<MenuItem>();
	
	public ContextMenu() {
		this.setAutoHide(true);
		this.internalBox = new VBox();
		this.children.add(this.internalBox);
		
		this.items.setAddCallback(new ChangeEvent<MenuItem>() {
			@Override
			public void onEvent(MenuItem changed) {
				recalculate();
			}
		});
		
		this.items.setRemoveCallback(new ChangeEvent<MenuItem>() {
			@Override
			public void onEvent(MenuItem changed) {
				recalculate();
			}
		});
	}
	
	protected void recalculate() {
		resize(1024,1024);
		internalBox.getChildren().clear();
		for (int i = 0; i < items.size(); i++) {
			internalBox.getChildren().add(items.get(i));
		}
		for (int i = 0; i < 8; i++) {
			internalBox.position(this);
		}
		resize(internalBox.getWidth(), internalBox.getHeight());		
	}

	public ContextMenu getChild() {
		return this.childMenu;
	}
	
	protected void resize( double sx, double sy ) {
		this.setMinSize(sx, sy);
		this.setMaxSize(sx, sy);
	}
	
	@Override
	public void close() {
		if ( childMenu != null ) {
			childMenu.close();
		}
		
		super.close();
	}

	@Override
	public boolean isResizeable() {
		return false;
	}

	@Override
	public void render(Context context) {
		// Position the menu
		this.position(this.getScene());
		this.setAbsolutePosition( absoluteX, absoluteY );
		
		// Position internal box inside menu
		this.setAlignment(Pos.TOP_LEFT);
		internalBox.position(this);
		
		// Get my width, and resize internal buttons so that they match the width of the box
		double innerWidth = this.getInnerBounds().getWidth();
		for (int i = 0; i < items.size(); i++) {
			items.get(i).setPrefWidth(innerWidth);
			items.get(i).setMinWidth(innerWidth);
		}
		
		// If the mouse is ontop of me, then set the mouse entered flag to true
		// This is used to autohide the menu when the mouse leaves (if flag set)
		if ( context.isHovered(this) ) {
			this.mouseEntered = true;
		}
		
		// Draw Drop Shadow
		long vg = context.getNVG();
		int x = (int) getAbsoluteX();
		int y = (int) getAbsoluteY();
		int w = (int) getWidth();
		int h = (int) getHeight();
		float r = 4;
		float feather = 12;
		float yOff = 1;
		NVGPaint paint = NanoVG.nvgBoxGradient(vg, x+2,y+yOff+1, w-4,h+yOff,r, feather, Theme.currentTheme().getShadow().getNVG(), Color.TRANSPARENT.getNVG(), NVGPaint.create());
		NanoVG.nvgBeginPath(vg);
		NanoVG.nvgRect(vg, x-feather*2,y-feather*2, w+feather*4,h+feather*4);
		NanoVG.nvgFillPaint(vg, paint);
		NanoVG.nvgFill(vg);
		
		// Draw Outline
		NanoVG.nvgBeginPath(context.getNVG());
		NanoVG.nvgRect(context.getNVG(), (int)x-1, (int)y-1, (int)w+2, (int)h+2);
		NanoVG.nvgFillColor(context.getNVG(), Theme.currentTheme().getControlOutline().getNVG());
		NanoVG.nvgFill(context.getNVG());
		
		// Render insides
		this.internalBox.render(context);
	}

	public ObservableList<MenuItem> getItems() {
		return this.items;
	}
}
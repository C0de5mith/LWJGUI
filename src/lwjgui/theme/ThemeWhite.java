package lwjgui.theme;

import lwjgui.Color;

public class ThemeWhite extends Theme {

	public static Color backgroundColor		= Color.WHITE;
	public static Color paneColor			= Color.WHITE_SMOKE;
	public static Color selectColor			= Color.AQUA;
	public static Color selectColorPassive	= Color.LIGHT_GRAY;
	public static Color shadow				= new Color(32, 32, 32, 100);
	public static Color textColor			= Color.DARK_GRAY;
	public static Color controlColor			= new Color(240, 240, 240);
	public static Color controlColorAlt		= controlColor.darker();
	public static Color controlHoverColor	= Color.WHITE;
	public static Color controlOutlineColor	= Color.DIM_GRAY;

	@Override
	public Color getBackground() {
		return backgroundColor;
	}
	
	@Override
	public Color getPane() {
		return paneColor;
	}

	@Override
	public Color getSelection() {
		return selectColor;
	}

	@Override
	public Color getSelectionPassive() {
		return selectColorPassive;
	}

	@Override
	public Color getShadow() {
		return shadow;
	}

	@Override
	public Color getText() {
		return textColor;
	}

	@Override
	public Color getControl() {
		return controlColor;
	}

	@Override
	public Color getControlAlt() {
		return controlColorAlt;
	}

	@Override
	public Color getControlOutline() {
		return controlOutlineColor;
	}

	@Override
	public Color getControlHover() {
		return controlHoverColor;
	}
}
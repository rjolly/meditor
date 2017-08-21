package jscl.editor;

import java.lang.reflect.Field;
import java.text.BreakIterator;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.View;

class LabelView extends javax.swing.text.LabelView {
	public LabelView(Element elem) {
		super(elem);
	}
	
	@Override
	public float getMinimumSpan(int axis) {
		switch (axis) {
			case View.X_AXIS:
				if (minimumSpan < 0) {
					minimumSpan = 0;
					int p0 = getStartOffset();
					int p1 = getEndOffset();
					while (p1 > p0) {
						int breakSpot = getBreakSpot(p0, p1);
						if (breakSpot == BreakIterator.DONE) {
							// the rest of the view is non-breakable
							breakSpot = p0;
						}
						minimumSpan = Math.max(minimumSpan,
								getPartialSpan(breakSpot, p1));
						// Note: getBreakSpot returns the *last* breakspot
						p1 = breakSpot - 1;
					}
				}
				return minimumSpan;
			case View.Y_AXIS:
				return super.getMinimumSpan(axis);
			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
		}
	}

	@Override
	public int getBreakWeight(int axis, float pos, float len) {
		if (axis == View.X_AXIS) {
			checkPainter();
			int p0 = getStartOffset();
			int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
			return p1 == p0 ? View.BadBreakWeight :
				   getBreakSpot(p0, p1) != BreakIterator.DONE ?
							View.ExcellentBreakWeight : View.GoodBreakWeight;
		}
		return super.getBreakWeight(axis, pos, len);
	}

	@Override
	public View breakView(int axis, int p0, float pos, float len) {
		if (axis == View.X_AXIS) {
			checkPainter();
			int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
			int breakSpot = getBreakSpot(p0, p1);

			if (breakSpot != -1) {
				p1 = breakSpot;
			}
			// else, no break in the region, return a fragment of the
			// bounded region.
			if (p0 == getStartOffset() && p1 == getEndOffset()) {
				return this;
			}
			LabelView v = (LabelView) createFragment(p0, p1);
			v.setX((int) pos);
			return v;
		}
		return this;
	}

	private int getBreakSpot(int p0, int p1) {
		return p1;
	}

	private float minimumSpan = -1;

	private void setX(int i) {
		try {
			final Field field = GlyphView.class.getDeclaredField("x");
			field.setAccessible(true);
			field.setInt(this, i);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}

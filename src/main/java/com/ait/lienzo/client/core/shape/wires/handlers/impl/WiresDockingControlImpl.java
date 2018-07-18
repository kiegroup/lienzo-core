package com.ait.lienzo.client.core.shape.wires.handlers.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.ait.lienzo.client.core.shape.wires.IDockingAcceptor;
import com.ait.lienzo.client.core.shape.wires.MagnetManager;
import com.ait.lienzo.client.core.shape.wires.PickerPart;
import com.ait.lienzo.client.core.shape.wires.WiresContainer;
import com.ait.lienzo.client.core.shape.wires.WiresLayer;
import com.ait.lienzo.client.core.shape.wires.WiresMagnet;
import com.ait.lienzo.client.core.shape.wires.WiresManager;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeEndEvent;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeEndHandler;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeStartEvent;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeStartHandler;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeStepEvent;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeStepHandler;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresDockingControl;
import com.ait.lienzo.client.core.shape.wires.picker.ColorMapBackedPicker;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.util.Geometry;
import com.google.gwt.event.shared.HandlerRegistration;

public class WiresDockingControlImpl extends AbstractWiresParentPickerControl
        implements WiresDockingControl {

    private Point2D initialPathLocation;
    private Point2D intersection;
    private double  xRatio;
    private double  yRatio;

    private final Collection<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public WiresDockingControlImpl(WiresShape shape,
                                   ColorMapBackedPicker.PickerOptions pickerOptions) {
        super(shape,
              pickerOptions);
    }

    public WiresDockingControlImpl(WiresParentPickerControlImpl parentPickerControl) {
        super(parentPickerControl);
    }

    @Override
    protected void beforeMoveStart(double x,
                                   double y) {
        super.beforeMoveStart(x,
                              y);

        initialPathLocation = getShape().getPath().getComputedLocation();
    }

    @Override
    public WiresDockingControl setEnabled(final boolean enabled) {
        if (enabled) {
            enable();
        } else {
            disable();
        }
        return this;
    }

    @Override
    protected void afterMoveStart(double x,
                                  double y) {
        super.afterMoveStart(x,
                             y);
        final WiresShape shape = getShape();
        if (null != shape.getDockedTo()) {
            shape.setDockedTo(null);
        }
    }

    @Override
    protected boolean afterMove(double dx,
                                double dy) {
        intersection = null;
        if (isAllow()) {
            WiresShape shape = getShape();
            WiresShape parent = (WiresShape) getParent();
            final Point2D parentLocation = parent.getComputedLocation();
            BoundingBox box = shape.getPath().getBoundingBox();


            final double shapeX = initialPathLocation.getX() + dx + (box.getWidth() / 2) - parentLocation.getX();
            final double shapeY = initialPathLocation.getY() + dy + (box.getHeight() / 2)- parentLocation.getY();

            this.intersection = Geometry.findIntersection((int) shapeX, (int) shapeY,
                                                          parent.getPath());
        }
        return null != this.intersection;
    }

    @Override
    protected boolean afterMoveComplete() {
        super.afterMoveComplete();
        return true;
    }

    @Override
    public void clear() {
        initialPathLocation = null;
        intersection = null;
    }

    private void removeHandlers() {
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
        handlerRegistrations.clear();
    }

    @Override
    public Point2D getAdjust() {
        if (isEnabled() && intersection != null) {
            Point2D candidateLocation = getCandidateLocation();
            final Point2D absLoc = getParent().getComputedLocation();
            double dx = absLoc.getX() + candidateLocation.getX() - initialPathLocation.getX();
            double dy = absLoc.getY() + candidateLocation.getY() - initialPathLocation.getY();
            Point2D point = new Point2D(dx, dy);
            return point;
        }
        return new Point2D(0, 0);
    }

    @Override
    public boolean isAllow() {
        final WiresLayer m_layer = getParentPickerControl().getWiresLayer();
        final WiresManager wiresManager = m_layer.getWiresManager();
        final IDockingAcceptor dockingAcceptor = wiresManager.getDockingAcceptor();
        return !isEnabled() ||
                null != getParent() &&
                        null != getParentShapePart() &&
                        getParent() instanceof WiresShape &&
                        getParentShapePart() == PickerPart.ShapePart.BORDER &&
                        (dockingAcceptor.dockingAllowed(getParent(),
                                                        getShape()));
    }

    @Override
    public boolean accept() {
        return !isEnabled() || (isAllow() && _isAccept());
    }

    @Override
    public Point2D getCandidateLocation() {
        return getCandidateLocation(getShape());
    }

    private Point2D getCandidateLocation(WiresShape shape) {
        BoundingBox box = shape.getPath().getBoundingBox();
        double x = intersection.getX() - (box.getWidth() / 2);
        double y = intersection.getY() -  (box.getHeight() / 2);
        return new Point2D(x, y);
    }

    @Override
    public void execute() {
        if (isEnabled()) {
            dock(getShape(),
                 getParent());
        }
    }

    @Override
    public void reset() {
        if (isEnabled()) {
            if (getParentPickerControl().getShapeLocationControl().isStartDocked() &&
                    getParentPickerControl().getInitialParent() != getShape().getParent()) {
                dock(getShape(),
                     getParentPickerControl().getInitialParent());
            }
        }
    }

    private boolean _isAccept() {
        final WiresLayer m_layer = getParentPickerControl().getWiresLayer();
        final WiresManager wiresManager = m_layer.getWiresManager();
        final IDockingAcceptor dockingAcceptor = wiresManager.getDockingAcceptor();
        return null != getParent() &&
                null != getParentShapePart()
                && getParentShapePart() == PickerPart.ShapePart.BORDER
                && dockingAcceptor.acceptDocking(getParent(),
                                                 getShape());
    }

    @Override
    public void dock(final WiresShape shape,
                     final WiresContainer parent) {
        if (null != shape.getDockedTo()) {
            undock(shape, shape.getDockedTo());
        }

        shape.removeFromParent();
        parent.add(shape);
        shape.setDockedTo(parent);
        final WiresShape parentWireShape = (WiresShape) parent;

        //recalculate location during shape resizing
        final BoundingBox shapeBox = shape.getPath().getBoundingBox();

        xRatio = -1;
        yRatio = -1;
        handlerRegistrations.add(parentWireShape.addWiresResizeStepHandler(new WiresResizeStepHandler() {
            @Override
            public void onShapeResizeStep(WiresResizeStepEvent event) {
                if ( xRatio == -1 && yRatio == -1 )
                {
                    // this is a hack, to ensure it runs on the first before any resize computations
                    BoundingBox parentBox = event.getShape().getPath().getBoundingBox();

                    // make sure everything is shifted to have x/y greater than 0
                    double normaliseX = parentBox.getX() >= 0 ? 0 : 0 - parentBox.getX();
                    double normaliseY = parentBox.getY() >= 0 ? 0 : 0 - parentBox.getY();

                    Point2D location = shape.getLocation();
                    xRatio = Geometry.getRatio(location.getX() + normaliseX + (shapeBox.getWidth() / 2), parentBox.getX() + normaliseX, parentBox.getWidth());
                    yRatio = Geometry.getRatio(location.getY() + normaliseY + (shapeBox.getHeight() / 2), parentBox.getY() + normaliseY, parentBox.getHeight());
                }
                shape.setLocation(new Point2D(event.getX() + (event.getWidth() * xRatio) - (shapeBox.getWidth() / 2),
                                              event.getY() + (event.getHeight() * yRatio) - (shapeBox.getHeight() / 2)));

                shape.shapeMoved();
            }
        }));
        handlerRegistrations.add(parentWireShape.addWiresResizeEndHandler(new WiresResizeEndHandler() {
            @Override
            public void onShapeResizeEnd(WiresResizeEndEvent event) {
                shape.setLocation(new Point2D(event.getX() + (event.getWidth() * xRatio) - (shapeBox.getWidth() / 2),
                                              event.getY() + (event.getHeight() * yRatio) - (shapeBox.getHeight() / 2)));
                shape.shapeMoved();
                shape.getControl().getAlignAndDistributeControl().updateIndex();
            }
        }));
    }

    @Override
    public void undock(WiresShape shape, WiresContainer parent) {
        parent.remove(shape);
        shape.setDockedTo(null);
        removeHandlers();
    }
}
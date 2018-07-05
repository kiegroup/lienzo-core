package com.ait.lienzo.client.core.shape.wires.handlers.impl;

import com.ait.lienzo.client.core.shape.wires.IDockingAcceptor;
import com.ait.lienzo.client.core.shape.wires.MagnetManager;
import com.ait.lienzo.client.core.shape.wires.PickerPart;
import com.ait.lienzo.client.core.shape.wires.WiresContainer;
import com.ait.lienzo.client.core.shape.wires.WiresLayer;
import com.ait.lienzo.client.core.shape.wires.WiresMagnet;
import com.ait.lienzo.client.core.shape.wires.WiresManager;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeStepEvent;
import com.ait.lienzo.client.core.shape.wires.event.WiresResizeStepHandler;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresDockingControl;
import com.ait.lienzo.client.core.shape.wires.picker.ColorMapBackedPicker;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.util.Geometry;
import com.ait.tooling.nativetools.client.event.HandlerRegistrationManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class WiresDockingControlImpl extends AbstractWiresParentPickerControl
        implements WiresDockingControl {

    private final HandlerRegistrationManager      m_handlerRegistrations;
    private Point2D                               m_absInitialPathLocation;
    private Point2D                               m_intersection;
    private Point2D                               m_absDockPosition;
    private WiresMagnet                           m_magnet;

    public WiresDockingControlImpl(final WiresShape shape,
                                   final ColorMapBackedPicker.PickerOptions pickerOptions) {
        super(shape,
              pickerOptions);
        m_handlerRegistrations = new HandlerRegistrationManager();
    }

    public WiresDockingControlImpl(final WiresParentPickerControlImpl parentPickerControl) {
        this(parentPickerControl, new HandlerRegistrationManager());
    }

    WiresDockingControlImpl(final WiresParentPickerControlImpl parentPickerControl,
                                   final HandlerRegistrationManager registrationManager) {
        super(parentPickerControl);
        m_handlerRegistrations = registrationManager;
    }

    @Override
    protected void beforeMoveStart(double x,
                                   double y) {
        super.beforeMoveStart(x,
                              y);
        m_absInitialPathLocation = getShape().getPath().getComputedLocation();
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
        super.afterMove(dx,
                        dy);
        m_intersection = null;
        if (isAllow()) {
            final Point2D location = getParentPickerControl().getCurrentLocation();
            final Point2D absLoc = getParent().getComputedLocation();// convert to local xy of the path
            this.m_intersection =
                    Geometry.findIntersection((int) (location.getX() - absLoc.getX()),
                                              (int) (location.getY() - absLoc.getY()),
                                              ((WiresShape) getParent()).getPath());
        }
        return null != this.m_intersection;
    }

    @Override
    public void clear() {
        m_absInitialPathLocation = null;
        m_intersection = null;
    }

    @Override
    public Point2D getAdjust() {
        if (isEnabled() && m_intersection != null) {
            m_magnet = getCloserMagnet(getShape(), getParent(), false);
            m_absDockPosition = calculateAbsCandidateLocation(m_magnet);
            return m_absDockPosition.sub(m_absInitialPathLocation);
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
                                                        getShape())) &&
                        null != getCloserMagnet(getShape(), getParent(), false);
    }

    @Override
    public boolean accept() {
        return !isEnabled() || (isAllow() && _isAccept());
    }

    private Point2D getCandidateAbsoluteLocation() {
        return m_absDockPosition;
    }

    private boolean isDocked() {
        return null != getShape().getDockedTo();
    }
    @Override
    public Point2D getCandidateLocation() {
        return isDocked() ?
                m_absDockPosition.sub(getShape().getDockedTo().getComputedLocation()) :
                m_absDockPosition;
    }

    @Override
    public void execute() {
        if (isEnabled()) {
            dock(getParent());
            move(getCandidateAbsoluteLocation());
        }
    }

    @Override
    public void reset() {
        if (isEnabled()) {
            if (getParentPickerControl().getShapeLocationControl().isStartDocked() &&
                    getParentPickerControl().getInitialParent() != getShape().getParent()) {
                dock(getParentPickerControl().getInitialParent());
                moveChild(getShape(),
                     getParentPickerControl().getInitialParent(),
                    getParentPickerControl().getShapeLocationControl().getShapeInitialLocation());
            }
        }
    }

    @Override
    public void destroy() {
        clear();
        m_handlerRegistrations.destroy();
        m_absDockPosition = null;
        m_magnet = null;
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

    private Point2D calculateAbsCandidateLocation(WiresMagnet shapeMagnet) {
        // Use the control x,y as this is absolute (Magnet x, y is relative)
        final Point2D trgLocation = new Point2D(shapeMagnet.getControl().getX(), shapeMagnet.getControl().getY());
        final BoundingBox box = getShape().getPath().getBoundingBox();
        final double newX = trgLocation.getX() - (box.getWidth() / 2);
        final double newY = trgLocation.getY() - (box.getHeight() / 2);
        return new Point2D(newX, newY);
    }

    /**
     * Reurn the closer magnet
     * @param shape
     * @param parent
     * @param allowOverlap should allow overlapping docked shape or not
     * @return closer magnet or null if none are available
     */
    private WiresMagnet getCloserMagnet(WiresShape shape, WiresContainer parent, boolean allowOverlap) {
        final WiresShape parentShape = (WiresShape) parent;
        final MagnetManager.Magnets magnets = parentShape.getMagnets();
        final Point2D shapeLocation = shape.getComputedLocation();
        final Point2D shapeCenter = Geometry.findCenter(shape.getPath().getBoundingBox());
        final double shapeX = shapeCenter.getX() + shapeLocation.getX();
        final double shapeY = shapeCenter.getY() + shapeLocation.getY();
        int magnetIndex = -1;
        Double minDistance = null;

        //not considering the zero magnet, that is the center.
        for (int i = 1; i < magnets.size(); i++) {
            WiresMagnet magnet = magnets.getMagnet(i);
            //skip magnet that has shape over it
            if (allowOverlap || !hasShapeOnMagnet(magnet, parentShape)) {
                final double magnetX = magnet.getControl().getLocation().getX();
                final double magnetY = magnet.getControl().getLocation().getY();
                final double distance = Geometry.distance(magnetX, magnetY, shapeX, shapeY);
                //getting shorter distance
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance;
                    magnetIndex = i;
                }
            }
        }
        return (magnetIndex > 0 ? magnets.getMagnet(magnetIndex) : null);
    }

    private boolean hasShapeOnMagnet(WiresMagnet magnet, WiresShape parent) {
        for (WiresShape child : parent.getChildShapes().toList()) {
            if (parent.equals(child.getDockedTo()) && !child.equals(getShape()) && magnet.equals(getCloserMagnet(child, parent, true))) {
                return true;
            }
        }
        return false;
    }

    void move(final Point2D absLocation) {
        moveChild(getShape(),
                  getParent(),
                  absLocation);
    }

    private void moveChild(final WiresShape shape,
                           final WiresContainer parent,
                           final Point2D absLocation) {
        final Point2D relLocation = absLocation.sub(parent.getComputedLocation());
        shape.setLocation(relLocation);
    }


    @Override
    public void dock(final WiresContainer parent) {
        final WiresShape shape = getShape();
        if (null != shape.getDockedTo()) {
            undock();
        }

        shape.removeFromParent();
        parent.add(shape);
        shape.setDockedTo(parent);

        //recalculate location during shape resizing
        if (parent instanceof WiresShape) {
            final WiresShape parentWireShape = (WiresShape) parent;
            registerHandler(parentWireShape.addWiresResizeStepHandler(new WiresResizeStepHandler() {
                @Override
                public void onShapeResizeStep(WiresResizeStepEvent event) {
                    final Point2D updatedAbsLocation = calculateAbsCandidateLocation(m_magnet);
                    move(updatedAbsLocation);
                }
            }));
        }
    }

    @Override
    public void undock() {
        final WiresShape shape = getShape();
        final WiresContainer parent = getShape().getDockedTo();
        if (null != parent) {
            parent.remove(shape);
            shape.setDockedTo(null);
        }
        removeHandlers();
    }

    private void registerHandler(HandlerRegistration handler) {
        m_handlerRegistrations.register(handler);
    }

    private void removeHandlers() {
        m_handlerRegistrations.clear();
    }
}
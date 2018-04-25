/*
 * Copyright (c) 2018 Ahome' Innovation Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ait.lienzo.client.core.shape.growable;

import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragEndHandler;
import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveHandler;
import com.ait.lienzo.client.core.event.NodeDragStartEvent;
import com.ait.lienzo.client.core.event.NodeDragStartHandler;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.wires.WiresManager;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.tooling.nativetools.client.collection.NFastArrayList;
import com.google.gwt.user.client.ui.ScrollPanel;

public class WiresShapeScrollStateDragHandler implements IScrollStateDragHandler<WiresShape> {

    private static final int MOVE_ADJUSTMENT = 5;
    private static final int CANVAS_EXPAND_SIZE = 100;
    private static final int SHAPE_ADJUSTMENT_SIZE = 20;

    private LienzoPanel contentPanel;
    private ScrollPanel scrollPanel;
    private boolean preserveDragbox = false;
    private BoundingBox dragStartBox;
    private WiresManager wiresManager;
    private GrowableLienzoScrollState scrollState;

    public WiresShapeScrollStateDragHandler(GrowableLienzoPanel growableLienzoPanel, GrowableLienzoScrollState scrollState) {
        this.contentPanel = growableLienzoPanel.getContentLienzoPanel();
        this.scrollPanel = growableLienzoPanel.getScrollPanel();
        this.wiresManager = WiresManager.get(growableLienzoPanel.getContentLayer());
        this.scrollState = scrollState;
    }

    public void checkBoundaries(int deltaX,
                                int deltaY,
                                String shapeUuid) {

        // CHECK RIGHT
        if (deltaX > 0) {

            // IF DRAGGING OBJECT RIGHT OUTSIDE VISIBLE WINDOW
            if ((dragStartBox.getMaxX() + deltaX + (dragStartBox.getWidth() / 2))
                    > (scrollPanel.getHorizontalScrollPosition() + scrollPanel.getOffsetWidth())) {

                // SCROLL AREA AVAILABLE
                if (scrollState.getScrollArea("right") > 0) {
                    scrollPanel.setHorizontalScrollPosition(scrollPanel.getHorizontalScrollPosition() + MOVE_ADJUSTMENT);
                } else { // GROW PANEL WIDTH
                    contentPanel.getViewport().setPixelSize(
                            contentPanel.getViewport().getWidth() + CANVAS_EXPAND_SIZE,
                            contentPanel.getViewport().getHeight());

                    preserveDragbox = true;
                    contentPanel.draw();
                }
            }
            checkVerticalBoundaries(deltaY, shapeUuid);

            // CHECK LEFT
        } else if (deltaX < 0) {

            // IF DRAGGING OBJECT LEFT OUTSIDE VISIBLE WINDOW
            if (dragStartBox.getMinX() + deltaX < scrollState.getScrollArea("left")) {

                // SCROLL AREA AVAILABLE
                if (scrollState.getScrollArea("left") > 0) {
                    scrollPanel.setHorizontalScrollPosition(scrollPanel.getHorizontalScrollPosition() - MOVE_ADJUSTMENT);
                } else { // GROW PANEL WIDTH
                    contentPanel.setSize(
                            String.valueOf(contentPanel.getWidth() + CANVAS_EXPAND_SIZE) + "px",
                            String.valueOf(contentPanel.getHeight()) + "px");

                    contentPanel.getViewport().setPixelSize(
                            contentPanel.getViewport().getWidth() + CANVAS_EXPAND_SIZE,
                            contentPanel.getViewport().getHeight());

                    shiftShapesRight(shapeUuid);
                    scrollPanel.setHorizontalScrollPosition(
                            scrollPanel.getHorizontalScrollPosition() + SHAPE_ADJUSTMENT_SIZE);

                    preserveDragbox = true;
                    contentPanel.draw();
                }
            }
            checkVerticalBoundaries(deltaY, shapeUuid);
        }
    }

    public void checkVerticalBoundaries(int deltaY, String shapeUuid) {

        // CHECK BOTTOM
        if (deltaY > 0) {

            // IF DRAGGING OBJECT DOWN OUTSIDE VISIBLE WINDOW
            if (dragStartBox.getMaxY() + deltaY + (dragStartBox.getHeight() / 2)
                    > scrollPanel.getVerticalScrollPosition() + scrollPanel.getOffsetHeight()) {

                // SCROLL AREA AVAILABLE
                if (scrollState.getScrollArea("bottom") > 0) {
                    scrollPanel.setVerticalScrollPosition(scrollPanel.getVerticalScrollPosition() + MOVE_ADJUSTMENT);
                } else { // GROW PANEL HEIGHT
                    contentPanel.getViewport().setPixelSize(
                            contentPanel.getViewport().getWidth(),
                            contentPanel.getViewport().getHeight() + CANVAS_EXPAND_SIZE);

                    preserveDragbox = true;
                    contentPanel.draw();
                }
            }

            // CHECK TOP
        } else if (deltaY < 0) {

            // IF DRAGGING OBJECT UP OUTSIDE VISIBLE WINDOW
            if (dragStartBox.getMinY() + deltaY < scrollState.getScrollArea("top")) {

                // SCROLL AREA AVAILABLE
                if (scrollState.getScrollArea("top") > 0) {
                    scrollPanel.setVerticalScrollPosition(scrollPanel.getVerticalScrollPosition() - MOVE_ADJUSTMENT);
                } else { // GROW PANEL HEIGHT
                    contentPanel.setSize(
                            String.valueOf(contentPanel.getWidth()) + "px",
                            String.valueOf(contentPanel.getHeight() + CANVAS_EXPAND_SIZE) + "px");

                    shiftShapesDown(shapeUuid);
                    contentPanel.getViewport().setPixelSize(
                            contentPanel.getViewport().getWidth(),
                            contentPanel.getViewport().getHeight() + CANVAS_EXPAND_SIZE);

                    preserveDragbox = true;
                    contentPanel.draw();
                }
            }
        }
    }

    private void shiftShapesDown(String dragShapeId) {

        NFastArrayList<WiresShape> wiresShapes = wiresManager.getLayer().getChildShapes();
        for (WiresShape shape : wiresShapes) {
            shape.setLocation(new Point2D(shape.getX(), shape.getY() +
                    (shape.uuid().equals(dragShapeId) ? SHAPE_ADJUSTMENT_SIZE : CANVAS_EXPAND_SIZE)));
        }
    }

    private void shiftShapesRight(String dragShapeId) {

        NFastArrayList<WiresShape> wiresShapes = wiresManager.getLayer().getChildShapes();
        for (WiresShape shape : wiresShapes) {
            shape.setLocation(new Point2D(shape.getX() + (shape.uuid().equals(dragShapeId)
                    ? SHAPE_ADJUSTMENT_SIZE : CANVAS_EXPAND_SIZE), shape.getY()));
        }
    }

    public void addDragHandlers(final WiresShape shape) {

        shape.getGroup().addNodeDragStartHandler(new NodeDragStartHandler() {
            @Override
            public void onNodeDragStart(NodeDragStartEvent event) {

                Group source = (Group) event.getSource();

                if (!preserveDragbox) {
                    dragStartBox = source.getComputedBoundingPoints().getBoundingBox();
                    preserveDragbox = false;
                }
            }
        });

        shape.getGroup().addNodeDragMoveHandler(new NodeDragMoveHandler() {
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {

                scrollState.update(event.getDragContext().getDx(),
                                   event.getDragContext().getDy(),
                                   shape.uuid());
            }
        });

        shape.getGroup().addNodeDragEndHandler(new NodeDragEndHandler() {
            @Override
            public void onNodeDragEnd(NodeDragEndEvent event) {
                preserveDragbox = false;
            }
        });
    }

    public void register(Object shapeObj, boolean draggable) {

        WiresShape shape = (WiresShape) shapeObj;
        wiresManager.register(shape);

        if (draggable) {
            shape.setDraggable(true);
            addDragHandlers(shape);
        }
    }
}

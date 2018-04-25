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

import com.ait.tooling.nativetools.client.collection.NFastStringMap;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * GrowableLienzoScrollState serves as a utility for tracking scrollbar state & provides hooks for drag/drop handling
 * near both the {@link com.ait.lienzo.client.core.shape.GrowableLienzoPanel} underlying
 * {@link com.google.gwt.user.client.ui.ScrollPanel} and {@link com.ait.lienzo.client.widget.LienzoPanel} edges.
 * <p>
 * <ul>
 * <li>GrowableLienzoScrollState takes the associated GrowableLienzoPanel as an input parameter.</li>
 * </ul>
 */
public class GrowableLienzoScrollState {

    private NFastStringMap<Integer> scrollAreas;
    private GrowableLienzoPanel growableLienzoPanel;
    private IScrollStateDragHandler dragHandler;

    public GrowableLienzoScrollState(GrowableLienzoPanel growableLienzoPanel) {
        this.growableLienzoPanel = growableLienzoPanel;
        this.scrollAreas = new NFastStringMap<>();
    }

    public void update(int deltaX,
                       int deltaY,
                       String shapeUuid) {

        ScrollPanel scrollPanel = growableLienzoPanel.getScrollPanel();
        scrollAreas.put("left", scrollPanel.getHorizontalScrollPosition()
                - scrollPanel.getMinimumHorizontalScrollPosition());

        scrollAreas.put("right", scrollPanel.getMaximumHorizontalScrollPosition()
                - scrollPanel.getHorizontalScrollPosition());

        scrollAreas.put("top", scrollPanel.getVerticalScrollPosition()
                - scrollPanel.getMinimumVerticalScrollPosition());

        scrollAreas.put("bottom", scrollPanel.getMaximumVerticalScrollPosition()
                - scrollPanel.getVerticalScrollPosition());

        dragHandler.checkBoundaries(deltaX, deltaY, shapeUuid);
    }

    public Integer getScrollArea(String region) {
        return scrollAreas.get(region);
    }

    public void register(Object shape, boolean draggable) {

        if (null == dragHandler) {
            dragHandler = ScrollStateDragHandlerFactory.create(shape, growableLienzoPanel, this);
        }
        dragHandler.register(shape, draggable);
    }
}

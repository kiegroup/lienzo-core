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

import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * GrowableLienzoPanel acts as an extension of {@link com.google.gwt.user.client.ui.ScrollPanel}, serving as a
 * Scrollable Container wrapping a {@link com.ait.lienzo.client.widget.LienzoPanel}
 * <p>
 * <ul>
 * <li>A GrowableLienzoPanel takes width and height of the underlying content LienzoPanel and overlaying ScrollPanel as input parameters.</li>
 * </ul>
 */
public class GrowableLienzoPanel {

    private ScrollPanel scrollPanel;
    private LienzoPanel contentLienzoPanel;
    private Layer contentLayer;
    private GrowableLienzoScrollState scrollState;

    public GrowableLienzoPanel(int lienzoPanelWidth, int lienzoPanelHeight, int scrollPanelWidth, int scrollPanelHeight) {

        contentLienzoPanel = new LienzoPanel(lienzoPanelWidth, lienzoPanelHeight);
        contentLienzoPanel.setBackgroundLayer(DefaultBackgroundGridLayer.build());

        scrollPanel = new ScrollPanel(contentLienzoPanel);
        scrollPanel.setSize(scrollPanelWidth + "px", scrollPanelHeight + "px");
        scrollPanel.setAlwaysShowScrollBars(true);

        contentLayer = new Layer();
        contentLienzoPanel.add(contentLayer);

        scrollState = new GrowableLienzoScrollState(this);

        RootPanel.get().add(scrollPanel);
    }

    public ScrollPanel getScrollPanel() {
        return scrollPanel;
    }

    public LienzoPanel getContentLienzoPanel() {
        return contentLienzoPanel;
    }

    public Layer getContentLayer() {
        return contentLayer;
    }

    public void addShape(Object shape, Boolean draggable) {
        scrollState.register(shape, draggable);
    }
}
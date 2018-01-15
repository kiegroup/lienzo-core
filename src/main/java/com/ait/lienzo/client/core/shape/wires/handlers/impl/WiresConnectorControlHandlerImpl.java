package com.ait.lienzo.client.core.shape.wires.handlers.impl;

import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragStartEvent;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickEvent;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.wires.WiresConnector;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresConnectorControl;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresConnectorControlHandler;
import com.google.gwt.core.client.GWT;

public class WiresConnectorControlHandlerImpl implements WiresConnectorControlHandler {

    private WiresConnector m_connector;
    private WiresConnectorControl m_connectorControl;

    public WiresConnectorControlHandlerImpl(WiresConnector connector, WiresConnectorControl connectorControl) {
        this.m_connector = connector;
        this.m_connectorControl = connectorControl;
    }

    @Override
    public void onNodeMouseDoubleClick(final NodeMouseDoubleClickEvent event) {
        if (m_connector.getPointHandles().isVisible()) {
            m_connectorControl.destroyControlPoint((IPrimitive<?>) event.getSource());
            m_connector.getLine().getLayer().batch();
        }
    }

    @Override
    public void onNodeDragEnd(NodeDragEndEvent event) {
        //no default implementation
    }

    @Override
    public void onNodeDragStart(NodeDragStartEvent event) {
        //no default implementation
    }

    @Override
    public void onNodeDragMove(NodeDragMoveEvent event) {
        //no default implementation
    }
}

package com.ait.lienzo.client.core.shape.wires.handlers.impl;

import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragStartEvent;
import com.ait.lienzo.client.core.event.NodeMouseClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickEvent;
import com.ait.lienzo.client.core.shape.wires.SelectionManager;
import com.ait.lienzo.client.core.shape.wires.WiresConnector;
import com.ait.lienzo.client.core.shape.wires.WiresManager;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresConnectorControl;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresConnectorHandler;
import com.ait.tooling.common.api.java.util.function.Consumer;
import com.google.gwt.user.client.Timer;

public class WiresConnectorHandlerImpl implements WiresConnectorHandler {

    private final WiresConnector m_connector;
    private final WiresManager m_wiresManager;
    private final Consumer<NodeMouseClickEvent> clickEventConsumer;
    private final Consumer<NodeMouseDoubleClickEvent> doubleClickEventConsumer;

    public WiresConnectorHandlerImpl(final WiresConnector connector,
                                     final WiresManager wiresManager) {
        this(connector,
             wiresManager,
             new DefaultClickHandlers(wiresManager, connector));
    }

    public WiresConnectorHandlerImpl(final WiresConnector connector,
                                     final WiresManager wiresManager,
                                     final DefaultClickHandlers defaultClickHandlers) {
        this(connector,
             wiresManager,
             defaultClickHandlers.onClick(),
             defaultClickHandlers.onDoubleClick());
    }

    public WiresConnectorHandlerImpl(final WiresConnector connector,
                                     final WiresManager wiresManager,
                                     final Consumer<NodeMouseClickEvent> clickEventConsumer,
                                     final Consumer<NodeMouseDoubleClickEvent> doubleClickEventConsumer) {
        this.m_connector = connector;
        this.m_wiresManager = wiresManager;
        this.clickEventConsumer = clickEventConsumer;
        this.doubleClickEventConsumer = doubleClickEventConsumer;
    }

    @Override
    public void onNodeDragStart(final NodeDragStartEvent event) {
        this.getControl().onMoveStart(event.getDragContext().getDragStartX(),
                                   event.getDragContext().getDragStartY());
    }

    @Override
    public void onNodeDragMove(final NodeDragMoveEvent event) {
        this.getControl().onMove(event.getDragContext().getDragStartX(),
                              event.getDragContext().getDragStartY());
    }

    @Override
    public void onNodeDragEnd(final NodeDragEndEvent event) {
        if (getControl().onMoveComplete()) {
            getControl().execute();
        } else {
            getControl().reset();
        }
    }

    @Override
    public void onNodeMouseClick(final NodeMouseClickEvent event) {
       clickEventConsumer.accept(event);
    }

    @Override
    public void onNodeMouseDoubleClick(final NodeMouseDoubleClickEvent event) {
        doubleClickEventConsumer.accept(event);
    }

    public WiresConnectorControl getControl() {
        return m_connector.getControl();
    }

    WiresConnector getConnector() {
        return m_connector;
    }

    WiresManager getWiresManager() {
        return m_wiresManager;
    }

    public static class DefaultClickHandlers {

        private final Timer clickTimer;
        private final WiresConnector connector;
        private final boolean switchVisiblityOnClick;
        private boolean isShiftKeyDown;

        public DefaultClickHandlers(final WiresManager wiresManager,
                                    final WiresConnector connector) {
            this(connector,
                 wiresManager.getSelectionManager(),
                 true);
        }

        public DefaultClickHandlers(final WiresManager wiresManager,
                                    final WiresConnector connector,
                                    final boolean switchVisiblityOnClick) {
            this(connector,
                 wiresManager.getSelectionManager(),
                 switchVisiblityOnClick);
        }

        private DefaultClickHandlers(final WiresConnector connector,
                                    final SelectionManager selectionManager,
                                     final boolean switchVisiblityOnClick) {
            this.connector = connector;
            this.switchVisiblityOnClick = switchVisiblityOnClick;
            this.clickTimer = new Timer() {
                @Override
                public void run()
                {
                    if (selectionManager != null) {
                        selectionManager.selected(connector, isShiftKeyDown);
                    }
                    if (switchVisiblityOnClick) {
                        final WiresConnectorControl control = connector.getControl();
                        if (control.areControlPointsVisible()) {
                            control.hideControlPoints();
                        } else {
                            control.showControlPoints();
                        }
                    }
                }
            };
        }

        public Consumer<NodeMouseClickEvent> onClick() {
            return new Consumer<NodeMouseClickEvent>() {
                @Override
                public void accept(NodeMouseClickEvent event) {
                    cancelTimerIfRunning();
                    isShiftKeyDown = event.isShiftKeyDown();
                    clickTimer.schedule(150);
                }
            };
        }

        public Consumer<NodeMouseDoubleClickEvent> onDoubleClick() {
            return new Consumer<NodeMouseDoubleClickEvent>() {
                @Override
                public void accept(NodeMouseDoubleClickEvent event) {
                    clickTimer.cancel();
                    connector.getControl().addControlPoint(event.getX(), event.getY());
                }
            };
        }

        private void cancelTimerIfRunning() {
            if (clickTimer.isRunning()) {
                clickTimer.cancel();
            }
        }

    }

}
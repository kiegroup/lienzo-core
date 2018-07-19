package com.ait.lienzo.client.core.shape.wires;

public interface IControlPointsAcceptor
{
    public static final IControlPointsAcceptor ALL  = new DefaultControlPointsAcceptor(true);

    public static final IControlPointsAcceptor NONE = new DefaultControlPointsAcceptor(false);

    public boolean add(WiresConnector connector,
                       int index,
                       double x,
                       double y);

    public boolean move(WiresConnector connector,
                       int index,
                        double tx,
                        double ty);

    public void update(WiresConnector connector);

    public boolean delete(WiresConnector connector,
                          int index);

    public static class DefaultControlPointsAcceptor implements IControlPointsAcceptor {
        private final boolean accept;

        public DefaultControlPointsAcceptor(final boolean accept)
        {
            this.accept = accept;
        }

        @Override
        public boolean add(final WiresConnector connector,
                           final int index,
                           final double x,
                           final double y)
        {
            return accept;
        }

        @Override
        public boolean move(final WiresConnector connector,
                                      final int index,
                                      final double tx,
                                      final double ty)
        {
            return accept;
        }

        @Override
        public void update(WiresConnector connector)
        {
        }

        @Override
        public boolean delete(final WiresConnector connector,
                              final int index)
        {
            return accept;
        }

    }
}

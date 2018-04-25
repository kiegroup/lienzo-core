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

import com.ait.lienzo.client.core.shape.GridLayer;
import com.ait.lienzo.client.core.shape.Line;

/***
 * DefaultBackgroundGridLayer provides a basic background GridLayer with predefined dash & line configuration.
 */
public class DefaultBackgroundGridLayer {

    private static final String PRIMARY_LINE_COLOR = "#0000FF";
    private static final String SECONDARY_LINE_COLOR = "#00FF00";

    private static final int PRIMARY_SIZE = 100;
    private static final int SECONDARY_SIZE = 25;

    private static final double ALPHA = 0.2;
    private static final int DASH_LENGTH = 2;

    public static GridLayer build() {

        Line primaryLine = new Line()
                .setStrokeColor(PRIMARY_LINE_COLOR)
                .setAlpha(ALPHA);

        Line secondaryLine = new Line()
                .setStrokeColor(SECONDARY_LINE_COLOR)
                .setAlpha(ALPHA)
                .setDashArray(DASH_LENGTH, DASH_LENGTH);

        return new GridLayer(PRIMARY_SIZE, primaryLine, SECONDARY_SIZE, secondaryLine);

    }
}

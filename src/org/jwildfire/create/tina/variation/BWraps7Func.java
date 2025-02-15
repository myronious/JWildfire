/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2021 Andreas Maschke

  This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
  General Public License as published by the Free Software Foundation; either version 2.1 of the 
  License, or (at your option) any later version.
 
  This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this software; 
  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jwildfire.create.tina.variation;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class BWraps7Func extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_CELLSIZE = "cellsize";
  private static final String PARAM_SPACE = "space";
  private static final String PARAM_GAIN = "gain";
  private static final String PARAM_INNER_TWIST = "inner_twist";
  private static final String PARAM_OUTER_TWIST = "outer_twist";

  private static final String[] paramNames = {PARAM_CELLSIZE, PARAM_SPACE, PARAM_GAIN, PARAM_INNER_TWIST, PARAM_OUTER_TWIST};

  private double cellsize = 1.0;
  private double space = 0.0;
  private double gain = 2.0;
  private double inner_twist = 0.0;
  private double outer_twist = 0.0;
  // precalculated
  private double _g2;
  private double _r2;
  private double _rfactor;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // Bubble Wrap - WIP Plugin by slobo777
    // http://slobo777.deviantart.com/art/Bubble-Wrap-WIP-Plugin-112370125 
    double Vx, Vy; // V is "global" vector,
    double Cx, Cy; // C is "cell centre" vector
    double Lx, Ly; // L is "local" bubble vector
    double r, theta, s, c;

    Vx = pAffineTP.x;
    Vy = pAffineTP.y;

    if (fabs(cellsize) < SMALL_EPSILON) {
      // Linear if cells are too small
      pVarTP.x += pAmount * Vx;
      pVarTP.y += pAmount * Vy;

      if (pContext.isPreserveZCoordinate()) {
        pVarTP.z += pAmount * pAffineTP.z;
      }

      return;
    }

    Cx = (floor(Vx / cellsize) + 0.5) * cellsize;
    Cy = (floor(Vy / cellsize) + 0.5) * cellsize;

    Lx = Vx - Cx;
    Ly = Vy - Cy;

    if ((Lx * Lx + Ly * Ly) > _r2) {
      // Linear if outside the bubble
      pVarTP.x += pAmount * Vx;
      pVarTP.y += pAmount * Vy;

      if (pContext.isPreserveZCoordinate()) {
        pVarTP.z += pAmount * pAffineTP.z;
      }

      return;
    }

    // We're in the bubble!

    // Bubble distortion on local co-ordinates:
    Lx *= _g2;
    Ly *= _g2;
    r = _rfactor / ((Lx * Lx + Ly * Ly) / 4.0 + 1.0);
    Lx *= r;
    Ly *= r;

    // Spin around the centre:
    r = (Lx * Lx + Ly * Ly) / _r2; // r should be 0.0 - 1.0
    theta = inner_twist * (1.0 - r) + outer_twist * r;
    s = sin(theta);
    c = cos(theta);

    // Add rotated local vectors direct to centre (avoids use of temp storage)
    Vx = Cx + c * Lx + s * Ly;
    Vy = Cy - s * Lx + c * Ly;

    // Finally add values in
    pVarTP.x += pAmount * Vx;
    pVarTP.y += pAmount * Vy;

    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }

  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[]{cellsize, space, gain, inner_twist, outer_twist};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_CELLSIZE.equalsIgnoreCase(pName))
      cellsize = pValue;
    else if (PARAM_SPACE.equalsIgnoreCase(pName))
      space = pValue;
    else if (PARAM_GAIN.equalsIgnoreCase(pName))
      gain = pValue;
    else if (PARAM_INNER_TWIST.equalsIgnoreCase(pName))
      inner_twist = pValue;
    else if (PARAM_OUTER_TWIST.equalsIgnoreCase(pName))
      outer_twist = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "bwraps7";
  }

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    double radius = 0.5 * (cellsize / (1.0 + space * space));

    // g2 is multiplier for radius
    _g2 = gain * gain + 1.0e-6;

    // Start max_bubble as maximum x or y value before applying bubble
    double max_bubble = _g2 * radius;

    if (max_bubble > 2.0) {
      // Values greater than 2.0 "recurve" round the back of the bubble
      max_bubble = 1.0;
    } else {
      // Expand smaller bubble to fill the space
      max_bubble *= 1.0 / ((max_bubble * max_bubble) / 4.0 + 1.0);
    }

    _r2 = radius * radius;
    _rfactor = radius / max_bubble;
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SUPPORTS_GPU, VariationFuncType.VARTYPE_SUPPORTED_BY_SWAN};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "   float radius = 0.5f * (__bwraps7_cellsize / (1.0f + __bwraps7_space * __bwraps7_space));\n"
        + "    float _g2 = __bwraps7_gain * __bwraps7_gain ADD_EPSILON;\n"
        + "    float max_bubble = _g2 * radius;\n"
        + "    if (max_bubble > 2.0f) {\n"
        + "      max_bubble = 1.0f;\n"
        + "    } else {\n"
        + "      max_bubble *= 1.0f / ((max_bubble * max_bubble) / 4.0f + 1.0f);\n"
        + "    }\n"
        + "    float _r2 = radius * radius;\n"
        + "    float _rfactor = radius / max_bubble;"
        + "    float Vx, Vy;\n"
        + "    float Cx, Cy;\n"
        + "    float Lx, Ly;\n"
        + "    float r, theta, s, c;\n"
        + "    Vx = __x;\n"
        + "    Vy = __y;\n"
        + "    if (fabs(__bwraps7_cellsize) < epsilon) {\n"
        + "      __px += __bwraps7 * Vx;\n"
        + "      __py += __bwraps7 * Vy;\n"
        + "    }\n"
        + "    else {\n"
        + "      Cx = (floorf(Vx / __bwraps7_cellsize) + 0.5f) * __bwraps7_cellsize;\n"
        + "      Cy = (floorf(Vy / __bwraps7_cellsize) + 0.5f) * __bwraps7_cellsize;\n"
        + "      Lx = Vx - Cx;\n"
        + "      Ly = Vy - Cy;\n"
        + "      if ((Lx * Lx + Ly * Ly) > _r2) {\n"
        + "        __px += __bwraps7 * Vx;\n"
        + "        __py += __bwraps7 * Vy;\n"
        + "      }\n"
        + "      else {\n"
        + "        Lx *= _g2;\n"
        + "        Ly *= _g2;\n"
        + "        r = _rfactor / ((Lx * Lx + Ly * Ly) / 4.0f + 1.0f);\n"
        + "        Lx *= r;\n"
        + "        Ly *= r;\n"
        + "        r = (Lx * Lx + Ly * Ly) / _r2;\n"
        + "        theta = __bwraps7_inner_twist * (1.0f - r) + __bwraps7_outer_twist * r;\n"
        + "        s = sinf(theta);\n"
        + "        c = cosf(theta);\n"
        + "        Vx = Cx + c * Lx + s * Ly;\n"
        + "        Vy = Cy - s * Lx + c * Ly;\n"
        + "        __px += __bwraps7 * Vx;\n"
        + "        __py += __bwraps7 * Vy;\n"
        + "      }\n"
        + "    }\n"
        + (context.isPreserveZCoordinate() ? "__pz += __bwraps7 * __z;\n" : "");
  }
}

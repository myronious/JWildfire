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

import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class CircleCropFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_RADIUS = "radius";
  private static final String PARAM_X = "x";
  private static final String PARAM_Y = "y";
  private static final String PARAM_SCATTER_AREA = "scatter_area";
  private static final String PARAM_ZERO = "zero";

  private static final String[] paramNames = {PARAM_RADIUS, PARAM_X, PARAM_Y, PARAM_SCATTER_AREA, PARAM_ZERO};

  private double radius = 1.0;
  private double x = 0.0;
  private double y = 0.0;
  private double scatter_area = 0.0;
  private int zero = 1;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // circlecrop by Xyrus02, http://xyrus02.deviantart.com/art/CircleCrop-Plugins-185353309
    double x0 = x;
    double y0 = y;
    double cr = radius;
    double ca = cA;
    double vv = pAmount;

    pAffineTP.x -= x0;
    pAffineTP.y -= y0;

    double rad = sqrt(pAffineTP.x * pAffineTP.x + pAffineTP.y * pAffineTP.y);
    double ang = atan2(pAffineTP.y, pAffineTP.x);
    double rdc = cr + (pContext.random() * 0.5 * ca);

    boolean esc = rad > cr;
    boolean cr0 = zero == 1;

    double s = sin(ang);
    double c = cos(ang);

    pVarTP.doHide = false;
    if (cr0 && esc) {
      pVarTP.x = pVarTP.y = 0;
      pVarTP.doHide = true;
    } else if (cr0 && !esc) {
      pVarTP.x += vv * pAffineTP.x + x0;
      pVarTP.y += vv * pAffineTP.y + y0;
    } else if (!cr0 && esc) {
      pVarTP.x += vv * rdc * c + x0;
      pVarTP.y += vv * rdc * s + y0;
    } else if (!cr0 && !esc) {
      pVarTP.x += vv * pAffineTP.x + x0;
      pVarTP.y += vv * pAffineTP.y + y0;
    }

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
    return new Object[]{radius, x, y, scatter_area, zero};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_RADIUS.equalsIgnoreCase(pName))
      radius = pValue;
    else if (PARAM_X.equalsIgnoreCase(pName))
      x = pValue;
    else if (PARAM_Y.equalsIgnoreCase(pName))
      y = pValue;
    else if (PARAM_SCATTER_AREA.equalsIgnoreCase(pName))
      scatter_area = pValue;
    else if (PARAM_ZERO.equalsIgnoreCase(pName))
      zero = Tools.FTOI(pValue);
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public String getName() {
    return "circlecrop";
  }

  private double cA;

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    cA = max(-1.0, min(scatter_area, 1.0));
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_CROP, VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return "float cA = fmaxf(-1.0, fminf(__circlecrop_scatter_area, 1.0));\n"
        + "float x0 = __circlecrop_x;\n"
        + "    float y0 = __circlecrop_y;\n"
        + "    float cr = __circlecrop_radius;\n"
        + "    float ca = cA;\n"
        + "    float vv = __circlecrop;\n"
        + "\n"
        + "    __x -= x0;\n"
        + "    __y -= y0;\n"
        + "\n"
        + "    float rad = sqrtf(__x * __x + __y * __y);\n"
        + "    float ang = atan2f(__y, __x);\n"
        + "    float rdc = cr + (RANDFLOAT() * 0.5 * ca);\n"
        + "\n"
        + "    short esc = rad > cr;\n"
        + "    short cr0 = lroundf(__circlecrop_zero) == 1;\n"
        + "\n"
        + "    float s = sinf(ang);\n"
        + "    float c = cosf(ang);\n"
        + "\n"
        + "    __doHide = false;\n"
        + "    if (cr0 && esc) {\n"
        + "      __px = __py = 0;\n"
        + "      __doHide = true;\n"
        + "    } else if (cr0 && !esc) {\n"
        + "      __px += vv * __x + x0;\n"
        + "      __py += vv * __y + y0;\n"
        + "    } else if (!cr0 && esc) {\n"
        + "      __px += vv * rdc * c + x0;\n"
        + "      __py += vv * rdc * s + y0;\n"
        + "    } else if (!cr0 && !esc) {\n"
        + "      __px += vv * __x + x0;\n"
        + "      __py += vv * __y + y0;\n"
        + "    }\n"
        + "\n"
        + (context.isPreserveZCoordinate() ? "      __pz += __circlecrop * __z;\n" : "");
  }
}

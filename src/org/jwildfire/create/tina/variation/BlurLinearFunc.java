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

public class BlurLinearFunc extends VariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  private static final String PARAM_LENGTH = "length";
  private static final String PARAM_ANGLE = "angle";
  private static final String[] paramNames = {PARAM_LENGTH, PARAM_ANGLE};

  private double length = 1.0;
  private double angle = 0.5;
  private double s = 0.0;
  private double c = 1.0;

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    // made in 2009 by Joel Faber - transcribed by DarkBeam 2017

    double r = length * pContext.random();

    pVarTP.x += pAmount * (pAffineTP.x + r * c);
    pVarTP.y += pAmount * (pAffineTP.y + r * s);

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
    return new Object[]{length, angle};
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_ANGLE.equalsIgnoreCase(pName))
      angle = Tools.limitValue(pValue, 0, M_2PI);
    else if (PARAM_LENGTH.equalsIgnoreCase(pName))
      length = pValue;
    else
      throw new IllegalArgumentException(pName);
  }

  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    s = sin(angle);
    c = cos(angle);
  }

  @Override
  public String getName() {
    return "blur_linear";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D, VariationFuncType.VARTYPE_SUPPORTS_GPU, VariationFuncType.VARTYPE_SUPPORTED_BY_SWAN};
  }

  @Override
  public String getGPUCode(FlameTransformationContext context) {
    // based on code from the cudaLibrary.xml compilation, created by Steven Brodhead Sr.
    return "float cosa;\n"
        + "float sina;\n"
        + "sincosf(__blur_linear_angle, &sina, &cosa);\n"
        + "float r    = __blur_linear_length * RANDFLOAT();\n"
        + "__px += __blur_linear * (__x + r * cosa);\n"
        + "__py += __blur_linear * (__y + r * sina);\n"
        + (context.isPreserveZCoordinate() ? "__pz += __blur_linear*__z;\n" : "");
  }
}

/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2011 Andreas Maschke
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

import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

import static org.jwildfire.base.mathlib.MathLib.*;

public class HolesqFunc extends SimpleVariationFunc implements SupportsGPU {
  private static final long serialVersionUID = 1L;

  @Override
  // holesq by DarkBeam
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double x = pAmount * pAffineTP.x;
    double y = pAmount * pAffineTP.y;
    double fax = fabs(x);
    double fay = fabs(y);

    if (fax+fay>1) {
      pVarTP.x += x;
      pVarTP.y += y;
    } else {
      double t;
      if (fax > fay) {
        t = (x-fay+1)*.5;
        if (x<0) t = (x+fay-1)*.5;
        pVarTP.x += t;
        pVarTP.y += y;
      }
      else {
        t = (y-fax+1)*.5;
        if (y<0) t = (y+fax-1)*.5;
        pVarTP.x += x;
        pVarTP.y += t;
      }
    }
    if (pContext.isPreserveZCoordinate()) {
      pVarTP.z += pAmount * pAffineTP.z;
    }
  }

  @Override
  public String getName() {
    return "holesq";
  }

  @Override
  public VariationFuncType[] getVariationTypes() {
    return new VariationFuncType[]{VariationFuncType.VARTYPE_2D,VariationFuncType.VARTYPE_SUPPORTS_GPU};
  }
  @Override
  public String getGPUCode(FlameTransformationContext context) {
    return   "    float x = __holesq * __x;"
    		+"    float y = __holesq * __y;"
    		+"    float fax = fabsf(x);"
    		+"    float fay = fabsf(y);"
    		+"    if (fax+fay>1) {"
    		+"      __px += x;"
    		+"      __py += y;"
    		+"    } else {"
    		+"      float t;"
    		+"      if (fax > fay) {"
    		+"        t = (x-fay+1)*.5;"
    		+"        if (x<0) t = (x+fay-1)*.5;"
    		+"        __px += t;"
    		+"        __py += y;"
    		+"      }"
    		+"      else {"
    		+"        t = (y-fax+1)*.5;"
    		+"        if (y<0) t = (y+fax-1)*.5;"
    		+"        __px += x;"
    		+"        __py += t;"
    		+"      }"
    		+"    }"
            + (context.isPreserveZCoordinate() ? "__pz += __holesq *__z;" : "");
  }

}